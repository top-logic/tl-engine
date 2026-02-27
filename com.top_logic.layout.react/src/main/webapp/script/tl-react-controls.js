import { React as e, useTLFieldValue as E, getComponent as D, useTLState as k, useTLCommand as y, TLChild as R, useTLUpload as N, useTLDataUrl as w, register as b } from "tl-react-bridge";
const { useCallback: U } = e, S = ({ state: l }) => {
  const [s, o] = E(), n = U(
    (t) => {
      o(t.target.value);
    },
    [o]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "text",
      value: s ?? "",
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactTextInput"
    }
  );
}, { useCallback: A } = e, F = ({ state: l, config: s }) => {
  const [o, n] = E(), t = A(
    (r) => {
      const c = r.target.value, i = c === "" ? null : Number(c);
      n(i);
    },
    [n]
  ), a = s != null && s.decimal ? "0.01" : "1";
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "number",
      value: o != null ? String(o) : "",
      onChange: t,
      step: a,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactNumberInput"
    }
  );
}, { useCallback: B } = e, P = ({ state: l }) => {
  const [s, o] = E(), n = B(
    (t) => {
      o(t.target.value || null);
    },
    [o]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "date",
      value: s ?? "",
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactDatePicker"
    }
  );
}, { useCallback: I } = e, M = ({ state: l, config: s }) => {
  const [o, n] = E(), t = I(
    (r) => {
      n(r.target.value || null);
    },
    [n]
  ), a = l.options ?? (s == null ? void 0 : s.options) ?? [];
  return /* @__PURE__ */ e.createElement(
    "select",
    {
      value: o ?? "",
      onChange: t,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactSelect"
    },
    /* @__PURE__ */ e.createElement("option", { value: "" }),
    a.map((r) => /* @__PURE__ */ e.createElement("option", { key: r.value, value: r.value }, r.label))
  );
}, { useCallback: O } = e, $ = ({ state: l }) => {
  const [s, o] = E(), n = O(
    (t) => {
      o(t.target.checked);
    },
    [o]
  );
  return /* @__PURE__ */ e.createElement(
    "input",
    {
      type: "checkbox",
      checked: s === !0,
      onChange: n,
      disabled: l.disabled === !0 || l.editable === !1,
      className: "tlReactCheckbox"
    }
  );
}, j = ({ controlId: l, state: s }) => {
  const o = s.columns ?? [], n = s.rows ?? [];
  return /* @__PURE__ */ e.createElement("table", { className: "tlReactTable" }, /* @__PURE__ */ e.createElement("thead", null, /* @__PURE__ */ e.createElement("tr", null, o.map((t) => /* @__PURE__ */ e.createElement("th", { key: t.name }, t.label)))), /* @__PURE__ */ e.createElement("tbody", null, n.map((t, a) => /* @__PURE__ */ e.createElement("tr", { key: a }, o.map((r) => {
    const c = r.cellModule ? D(r.cellModule) : void 0, i = t[r.name];
    if (c) {
      const m = { value: i, editable: s.editable };
      return /* @__PURE__ */ e.createElement("td", { key: r.name }, /* @__PURE__ */ e.createElement(
        c,
        {
          controlId: l + "-" + a + "-" + r.name,
          state: m
        }
      ));
    }
    return /* @__PURE__ */ e.createElement("td", { key: r.name }, i != null ? String(i) : "");
  })))));
}, { useCallback: x } = e, V = ({ command: l, label: s, disabled: o }) => {
  const n = k(), t = y(), a = l ?? "click", r = s ?? n.label, c = o ?? n.disabled === !0, i = x(() => {
    t(a);
  }, [t, a]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: i,
      disabled: c,
      className: "tlReactButton"
    },
    r
  );
}, { useCallback: z } = e, W = ({ command: l, label: s, active: o, disabled: n }) => {
  const t = k(), a = y(), r = l ?? "click", c = s ?? t.label, i = o ?? t.active === !0, m = n ?? t.disabled === !0, v = z(() => {
    a(r);
  }, [a, r]);
  return /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      onClick: v,
      disabled: m,
      className: "tlReactButton" + (i ? " tlReactButtonActive" : "")
    },
    c
  );
}, q = () => {
  const l = k(), s = y(), o = l.count ?? 0, n = l.label ?? "React Counter";
  return /* @__PURE__ */ e.createElement("div", { className: "tlCounter" }, /* @__PURE__ */ e.createElement("h3", { className: "tlCounter__title" }, n), /* @__PURE__ */ e.createElement("div", { className: "tlCounter__controls" }, /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("decrement") }, "−"), /* @__PURE__ */ e.createElement("span", { className: "tlCounter__value" }, o), /* @__PURE__ */ e.createElement("button", { className: "tlCounter__button", onClick: () => s("increment") }, "+")), /* @__PURE__ */ e.createElement("p", { className: "tlCounter__description" }, "State is managed on the server. Each click dispatches a command via POST, and the updated count is pushed back via SSE."));
}, { useCallback: G } = e, H = () => {
  const l = k(), s = y(), o = l.tabs ?? [], n = l.activeTabId, t = G((a) => {
    a !== n && s("selectTab", { tabId: a });
  }, [s, n]);
  return /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar" }, /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__tabs", role: "tablist" }, o.map((a) => /* @__PURE__ */ e.createElement(
    "button",
    {
      key: a.id,
      role: "tab",
      "aria-selected": a.id === n,
      className: "tlReactTabBar__tab" + (a.id === n ? " tlReactTabBar__tab--active" : ""),
      onClick: () => t(a.id)
    },
    a.label
  ))), /* @__PURE__ */ e.createElement("div", { className: "tlReactTabBar__content", role: "tabpanel" }, l.activeContent && /* @__PURE__ */ e.createElement(R, { control: l.activeContent })));
}, J = () => {
  const l = k(), s = l.title, o = l.fields ?? [];
  return /* @__PURE__ */ e.createElement("div", { className: "tlFieldList" }, s && /* @__PURE__ */ e.createElement("h3", { className: "tlFieldList__title" }, s), /* @__PURE__ */ e.createElement("div", { className: "tlFieldList__fields" }, o.map((n, t) => /* @__PURE__ */ e.createElement("div", { key: t, className: "tlFieldList__item" }, /* @__PURE__ */ e.createElement(R, { control: n })))));
}, K = () => {
  const l = k(), s = N(), [o, n] = e.useState("idle"), t = e.useRef(null), a = e.useRef([]), r = e.useRef(null), c = l.status ?? "idle", i = l.error, m = c === "received" ? "idle" : o !== "idle" ? o : c, v = e.useCallback(async () => {
    if (o === "recording") {
      const d = t.current;
      d && d.state !== "inactive" && d.stop();
      return;
    }
    if (o !== "uploading")
      try {
        const d = await navigator.mediaDevices.getUserMedia({ audio: !0 });
        r.current = d, a.current = [];
        const f = MediaRecorder.isTypeSupported("audio/webm") ? "audio/webm" : "", L = new MediaRecorder(d, f ? { mimeType: f } : void 0);
        t.current = L, L.ondataavailable = (_) => {
          _.data.size > 0 && a.current.push(_.data);
        }, L.onstop = async () => {
          d.getTracks().forEach((g) => g.stop()), r.current = null;
          const _ = new Blob(a.current, { type: L.mimeType || "audio/webm" });
          if (a.current = [], _.size === 0) {
            n("idle");
            return;
          }
          n("uploading");
          const u = new FormData();
          u.append("audio", _, "recording.webm"), await s(u), n("idle");
        }, L.start(), n("recording");
      } catch (d) {
        console.error("[TLAudioRecorder] Microphone access denied or unavailable:", d), n("idle");
      }
  }, [o, s]), p = m === "recording" ? "Stop recording" : m === "uploading" ? "Uploading…" : "Record audio", C = m === "uploading", h = ["tlAudioRecorder__button"];
  return m === "recording" && h.push("tlAudioRecorder__button--recording"), m === "uploading" && h.push("tlAudioRecorder__button--uploading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioRecorder" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: h.join(" "),
      onClick: v,
      disabled: C,
      title: p,
      "aria-label": p
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioRecorder__icon${m === "recording" ? " tlAudioRecorder__icon--stop" : ""}` })
  ), i && /* @__PURE__ */ e.createElement("span", { className: "tlAudioRecorder__status tlAudioRecorder__status--error" }, i));
}, Q = () => {
  const l = k(), s = w(), o = !!l.hasAudio, n = l.dataRevision ?? 0, [t, a] = e.useState(o ? "idle" : "disabled"), r = e.useRef(null), c = e.useRef(null), i = e.useRef(n);
  e.useEffect(() => {
    o ? t === "disabled" && a("idle") : (r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), a("disabled"));
  }, [o]), e.useEffect(() => {
    n !== i.current && (i.current = n, r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null), (t === "playing" || t === "paused" || t === "loading") && a("idle"));
  }, [n]), e.useEffect(() => () => {
    r.current && (r.current.pause(), r.current = null), c.current && (URL.revokeObjectURL(c.current), c.current = null);
  }, []);
  const m = e.useCallback(async () => {
    if (t === "disabled" || t === "loading")
      return;
    if (t === "playing") {
      r.current && r.current.pause(), a("paused");
      return;
    }
    if (t === "paused" && r.current) {
      r.current.play(), a("playing");
      return;
    }
    if (!c.current) {
      a("loading");
      try {
        const d = await fetch(s);
        if (!d.ok) {
          console.error("[TLAudioPlayer] Failed to fetch audio:", d.status), a("idle");
          return;
        }
        const f = await d.blob();
        c.current = URL.createObjectURL(f);
      } catch (d) {
        console.error("[TLAudioPlayer] Fetch error:", d), a("idle");
        return;
      }
    }
    const h = new Audio(c.current);
    r.current = h, h.onended = () => {
      a("idle");
    }, h.play(), a("playing");
  }, [t, s]), v = t === "loading" ? "Loading…" : t === "playing" ? "Pause audio" : t === "disabled" ? "No audio" : "Play audio", p = t === "disabled" || t === "loading", C = ["tlAudioPlayer__button"];
  return t === "playing" && C.push("tlAudioPlayer__button--playing"), t === "loading" && C.push("tlAudioPlayer__button--loading"), /* @__PURE__ */ e.createElement("div", { className: "tlAudioPlayer" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: C.join(" "),
      onClick: m,
      disabled: p,
      title: v,
      "aria-label": v
    },
    /* @__PURE__ */ e.createElement("span", { className: `tlAudioPlayer__icon${t === "playing" ? " tlAudioPlayer__icon--pause" : ""}` })
  ));
}, X = () => {
  const l = k(), s = N(), [o, n] = e.useState("idle"), [t, a] = e.useState(!1), r = e.useRef(null), c = l.status ?? "idle", i = l.error, m = l.accept ?? "", v = c === "received" ? "idle" : o !== "idle" ? o : c, p = e.useCallback(async (u) => {
    n("uploading");
    const g = new FormData();
    g.append("file", u, u.name), await s(g), n("idle");
  }, [s]), C = e.useCallback((u) => {
    var T;
    const g = (T = u.target.files) == null ? void 0 : T[0];
    g && p(g);
  }, [p]), h = e.useCallback(() => {
    var u;
    o !== "uploading" && ((u = r.current) == null || u.click());
  }, [o]), d = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!0);
  }, []), f = e.useCallback((u) => {
    u.preventDefault(), u.stopPropagation(), a(!1);
  }, []), L = e.useCallback((u) => {
    var T;
    if (u.preventDefault(), u.stopPropagation(), a(!1), o === "uploading") return;
    const g = (T = u.dataTransfer.files) == null ? void 0 : T[0];
    g && p(g);
  }, [o, p]), _ = v === "uploading";
  return /* @__PURE__ */ e.createElement(
    "div",
    {
      className: `tlFileUpload${t ? " tlFileUpload--dragover" : ""}`,
      onDragOver: d,
      onDragLeave: f,
      onDrop: L
    },
    /* @__PURE__ */ e.createElement(
      "input",
      {
        ref: r,
        type: "file",
        accept: m || void 0,
        onChange: C,
        style: { display: "none" }
      }
    ),
    /* @__PURE__ */ e.createElement(
      "button",
      {
        type: "button",
        className: "tlFileUpload__button",
        onClick: h,
        disabled: _
      },
      v === "uploading" ? "Uploading…" : "Choose File"
    ),
    i && /* @__PURE__ */ e.createElement("span", { className: "tlFileUpload__status tlFileUpload__status--error" }, i)
  );
}, Y = () => {
  const l = k(), s = w(), o = y(), n = !!l.hasData, t = l.dataRevision ?? 0, a = l.fileName ?? "download", r = !!l.clearable, [c, i] = e.useState(!1), m = e.useCallback(async () => {
    if (!(!n || c)) {
      i(!0);
      try {
        const p = s + (s.includes("?") ? "&" : "?") + "rev=" + t, C = await fetch(p);
        if (!C.ok) {
          console.error("[TLDownload] Failed to fetch data:", C.status);
          return;
        }
        const h = await C.blob(), d = URL.createObjectURL(h), f = document.createElement("a");
        f.href = d, f.download = a, f.style.display = "none", document.body.appendChild(f), f.click(), document.body.removeChild(f), URL.revokeObjectURL(d);
      } catch (p) {
        console.error("[TLDownload] Fetch error:", p);
      } finally {
        i(!1);
      }
    }
  }, [n, c, s, t, a]), v = e.useCallback(async () => {
    n && await o("clear");
  }, [n, o]);
  return n ? /* @__PURE__ */ e.createElement("div", { className: "tlDownload" }, /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__downloadBtn" + (c ? " tlDownload__downloadBtn--downloading" : ""),
      onClick: m,
      disabled: c,
      title: c ? "Downloading…" : "Download " + a,
      "aria-label": c ? "Downloading…" : "Download " + a
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__downloadIcon", viewBox: "0 0 16 16", width: "16", height: "16", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M8 1v9m0 0L4.5 6.5M8 10l3.5-3.5M2 13h12", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round", strokeLinejoin: "round", fill: "none" }))
  ), /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName", title: a }, a), r && /* @__PURE__ */ e.createElement(
    "button",
    {
      type: "button",
      className: "tlDownload__clearBtn",
      onClick: v,
      title: "Clear",
      "aria-label": "Clear file"
    },
    /* @__PURE__ */ e.createElement("svg", { className: "tlDownload__clearIcon", viewBox: "0 0 16 16", width: "14", height: "14", "aria-hidden": "true" }, /* @__PURE__ */ e.createElement("path", { d: "M4 4l8 8M12 4l-8 8", stroke: "currentColor", strokeWidth: "1.5", strokeLinecap: "round" }))
  )) : /* @__PURE__ */ e.createElement("div", { className: "tlDownload tlDownload--empty" }, /* @__PURE__ */ e.createElement("span", { className: "tlDownload__fileName tlDownload__fileName--empty" }, "No file"));
};
b("TLButton", V);
b("TLToggleButton", W);
b("TLTextInput", S);
b("TLNumberInput", F);
b("TLDatePicker", P);
b("TLSelect", M);
b("TLCheckbox", $);
b("TLTable", j);
b("TLCounter", q);
b("TLTabBar", H);
b("TLFieldList", J);
b("TLAudioRecorder", K);
b("TLAudioPlayer", Q);
b("TLFileUpload", X);
b("TLDownload", Y);
