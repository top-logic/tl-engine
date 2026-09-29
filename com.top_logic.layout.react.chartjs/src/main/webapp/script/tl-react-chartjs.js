var ul = Object.defineProperty;
var dl = (i, t, e) => t in i ? ul(i, t, { enumerable: !0, configurable: !0, writable: !0, value: e }) : i[t] = e;
var O = (i, t, e) => dl(i, typeof t != "symbol" ? t + "" : t, e);
import { React as U, useTLCommand as fl, register as gl } from "tl-react-bridge";
/*!
 * @kurkle/color v0.3.4
 * https://github.com/kurkle/color#readme
 * (c) 2024 Jukka Kurkela
 * Released under the MIT License
 */
function di(i) {
  return i + 0.5 | 0;
}
const Zt = (i, t, e) => Math.max(Math.min(i, e), t);
function Ue(i) {
  return Zt(di(i * 2.55), 0, 255);
}
function te(i) {
  return Zt(di(i * 255), 0, 255);
}
function Wt(i) {
  return Zt(di(i / 2.55) / 100, 0, 1);
}
function Hs(i) {
  return Zt(di(i * 100), 0, 100);
}
const Mt = { 0: 0, 1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7, 8: 8, 9: 9, A: 10, B: 11, C: 12, D: 13, E: 14, F: 15, a: 10, b: 11, c: 12, d: 13, e: 14, f: 15 }, In = [..."0123456789ABCDEF"], pl = (i) => In[i & 15], ml = (i) => In[(i & 240) >> 4] + In[i & 15], ki = (i) => (i & 240) >> 4 === (i & 15), bl = (i) => ki(i.r) && ki(i.g) && ki(i.b) && ki(i.a);
function xl(i) {
  var t = i.length, e;
  return i[0] === "#" && (t === 4 || t === 5 ? e = {
    r: 255 & Mt[i[1]] * 17,
    g: 255 & Mt[i[2]] * 17,
    b: 255 & Mt[i[3]] * 17,
    a: t === 5 ? Mt[i[4]] * 17 : 255
  } : (t === 7 || t === 9) && (e = {
    r: Mt[i[1]] << 4 | Mt[i[2]],
    g: Mt[i[3]] << 4 | Mt[i[4]],
    b: Mt[i[5]] << 4 | Mt[i[6]],
    a: t === 9 ? Mt[i[7]] << 4 | Mt[i[8]] : 255
  })), e;
}
const _l = (i, t) => i < 255 ? t(i) : "";
function yl(i) {
  var t = bl(i) ? pl : ml;
  return i ? "#" + t(i.r) + t(i.g) + t(i.b) + _l(i.a, t) : void 0;
}
const vl = /^(hsla?|hwb|hsv)\(\s*([-+.e\d]+)(?:deg)?[\s,]+([-+.e\d]+)%[\s,]+([-+.e\d]+)%(?:[\s,]+([-+.e\d]+)(%)?)?\s*\)$/;
function wr(i, t, e) {
  const n = t * Math.min(e, 1 - e), s = (o, r = (o + i / 30) % 12) => e - n * Math.max(Math.min(r - 3, 9 - r, 1), -1);
  return [s(0), s(8), s(4)];
}
function Ml(i, t, e) {
  const n = (s, o = (s + i / 60) % 6) => e - e * t * Math.max(Math.min(o, 4 - o, 1), 0);
  return [n(5), n(3), n(1)];
}
function Sl(i, t, e) {
  const n = wr(i, 1, 0.5);
  let s;
  for (t + e > 1 && (s = 1 / (t + e), t *= s, e *= s), s = 0; s < 3; s++)
    n[s] *= 1 - t - e, n[s] += t;
  return n;
}
function kl(i, t, e, n, s) {
  return i === s ? (t - e) / n + (t < e ? 6 : 0) : t === s ? (e - i) / n + 2 : (i - t) / n + 4;
}
function ns(i) {
  const e = i.r / 255, n = i.g / 255, s = i.b / 255, o = Math.max(e, n, s), r = Math.min(e, n, s), a = (o + r) / 2;
  let l, c, h;
  return o !== r && (h = o - r, c = a > 0.5 ? h / (2 - o - r) : h / (o + r), l = kl(e, n, s, h, o), l = l * 60 + 0.5), [l | 0, c || 0, a];
}
function ss(i, t, e, n) {
  return (Array.isArray(t) ? i(t[0], t[1], t[2]) : i(t, e, n)).map(te);
}
function os(i, t, e) {
  return ss(wr, i, t, e);
}
function wl(i, t, e) {
  return ss(Sl, i, t, e);
}
function Tl(i, t, e) {
  return ss(Ml, i, t, e);
}
function Tr(i) {
  return (i % 360 + 360) % 360;
}
function Pl(i) {
  const t = vl.exec(i);
  let e = 255, n;
  if (!t)
    return;
  t[5] !== n && (e = t[6] ? Ue(+t[5]) : te(+t[5]));
  const s = Tr(+t[2]), o = +t[3] / 100, r = +t[4] / 100;
  return t[1] === "hwb" ? n = wl(s, o, r) : t[1] === "hsv" ? n = Tl(s, o, r) : n = os(s, o, r), {
    r: n[0],
    g: n[1],
    b: n[2],
    a: e
  };
}
function Cl(i, t) {
  var e = ns(i);
  e[0] = Tr(e[0] + t), e = os(e), i.r = e[0], i.g = e[1], i.b = e[2];
}
function Ol(i) {
  if (!i)
    return;
  const t = ns(i), e = t[0], n = Hs(t[1]), s = Hs(t[2]);
  return i.a < 255 ? `hsla(${e}, ${n}%, ${s}%, ${Wt(i.a)})` : `hsl(${e}, ${n}%, ${s}%)`;
}
const Ws = {
  x: "dark",
  Z: "light",
  Y: "re",
  X: "blu",
  W: "gr",
  V: "medium",
  U: "slate",
  A: "ee",
  T: "ol",
  S: "or",
  B: "ra",
  C: "lateg",
  D: "ights",
  R: "in",
  Q: "turquois",
  E: "hi",
  P: "ro",
  O: "al",
  N: "le",
  M: "de",
  L: "yello",
  F: "en",
  K: "ch",
  G: "arks",
  H: "ea",
  I: "ightg",
  J: "wh"
}, js = {
  OiceXe: "f0f8ff",
  antiquewEte: "faebd7",
  aqua: "ffff",
  aquamarRe: "7fffd4",
  azuY: "f0ffff",
  beige: "f5f5dc",
  bisque: "ffe4c4",
  black: "0",
  blanKedOmond: "ffebcd",
  Xe: "ff",
  XeviTet: "8a2be2",
  bPwn: "a52a2a",
  burlywood: "deb887",
  caMtXe: "5f9ea0",
  KartYuse: "7fff00",
  KocTate: "d2691e",
  cSO: "ff7f50",
  cSnflowerXe: "6495ed",
  cSnsilk: "fff8dc",
  crimson: "dc143c",
  cyan: "ffff",
  xXe: "8b",
  xcyan: "8b8b",
  xgTMnPd: "b8860b",
  xWay: "a9a9a9",
  xgYF: "6400",
  xgYy: "a9a9a9",
  xkhaki: "bdb76b",
  xmagFta: "8b008b",
  xTivegYF: "556b2f",
  xSange: "ff8c00",
  xScEd: "9932cc",
  xYd: "8b0000",
  xsOmon: "e9967a",
  xsHgYF: "8fbc8f",
  xUXe: "483d8b",
  xUWay: "2f4f4f",
  xUgYy: "2f4f4f",
  xQe: "ced1",
  xviTet: "9400d3",
  dAppRk: "ff1493",
  dApskyXe: "bfff",
  dimWay: "696969",
  dimgYy: "696969",
  dodgerXe: "1e90ff",
  fiYbrick: "b22222",
  flSOwEte: "fffaf0",
  foYstWAn: "228b22",
  fuKsia: "ff00ff",
  gaRsbSo: "dcdcdc",
  ghostwEte: "f8f8ff",
  gTd: "ffd700",
  gTMnPd: "daa520",
  Way: "808080",
  gYF: "8000",
  gYFLw: "adff2f",
  gYy: "808080",
  honeyMw: "f0fff0",
  hotpRk: "ff69b4",
  RdianYd: "cd5c5c",
  Rdigo: "4b0082",
  ivSy: "fffff0",
  khaki: "f0e68c",
  lavFMr: "e6e6fa",
  lavFMrXsh: "fff0f5",
  lawngYF: "7cfc00",
  NmoncEffon: "fffacd",
  ZXe: "add8e6",
  ZcSO: "f08080",
  Zcyan: "e0ffff",
  ZgTMnPdLw: "fafad2",
  ZWay: "d3d3d3",
  ZgYF: "90ee90",
  ZgYy: "d3d3d3",
  ZpRk: "ffb6c1",
  ZsOmon: "ffa07a",
  ZsHgYF: "20b2aa",
  ZskyXe: "87cefa",
  ZUWay: "778899",
  ZUgYy: "778899",
  ZstAlXe: "b0c4de",
  ZLw: "ffffe0",
  lime: "ff00",
  limegYF: "32cd32",
  lRF: "faf0e6",
  magFta: "ff00ff",
  maPon: "800000",
  VaquamarRe: "66cdaa",
  VXe: "cd",
  VScEd: "ba55d3",
  VpurpN: "9370db",
  VsHgYF: "3cb371",
  VUXe: "7b68ee",
  VsprRggYF: "fa9a",
  VQe: "48d1cc",
  VviTetYd: "c71585",
  midnightXe: "191970",
  mRtcYam: "f5fffa",
  mistyPse: "ffe4e1",
  moccasR: "ffe4b5",
  navajowEte: "ffdead",
  navy: "80",
  Tdlace: "fdf5e6",
  Tive: "808000",
  TivedBb: "6b8e23",
  Sange: "ffa500",
  SangeYd: "ff4500",
  ScEd: "da70d6",
  pOegTMnPd: "eee8aa",
  pOegYF: "98fb98",
  pOeQe: "afeeee",
  pOeviTetYd: "db7093",
  papayawEp: "ffefd5",
  pHKpuff: "ffdab9",
  peru: "cd853f",
  pRk: "ffc0cb",
  plum: "dda0dd",
  powMrXe: "b0e0e6",
  purpN: "800080",
  YbeccapurpN: "663399",
  Yd: "ff0000",
  Psybrown: "bc8f8f",
  PyOXe: "4169e1",
  saddNbPwn: "8b4513",
  sOmon: "fa8072",
  sandybPwn: "f4a460",
  sHgYF: "2e8b57",
  sHshell: "fff5ee",
  siFna: "a0522d",
  silver: "c0c0c0",
  skyXe: "87ceeb",
  UXe: "6a5acd",
  UWay: "708090",
  UgYy: "708090",
  snow: "fffafa",
  sprRggYF: "ff7f",
  stAlXe: "4682b4",
  tan: "d2b48c",
  teO: "8080",
  tEstN: "d8bfd8",
  tomato: "ff6347",
  Qe: "40e0d0",
  viTet: "ee82ee",
  JHt: "f5deb3",
  wEte: "ffffff",
  wEtesmoke: "f5f5f5",
  Lw: "ffff00",
  LwgYF: "9acd32"
};
function Dl() {
  const i = {}, t = Object.keys(js), e = Object.keys(Ws);
  let n, s, o, r, a;
  for (n = 0; n < t.length; n++) {
    for (r = a = t[n], s = 0; s < e.length; s++)
      o = e[s], a = a.replace(o, Ws[o]);
    o = parseInt(js[r], 16), i[a] = [o >> 16 & 255, o >> 8 & 255, o & 255];
  }
  return i;
}
let wi;
function Al(i) {
  wi || (wi = Dl(), wi.transparent = [0, 0, 0, 0]);
  const t = wi[i.toLowerCase()];
  return t && {
    r: t[0],
    g: t[1],
    b: t[2],
    a: t.length === 4 ? t[3] : 255
  };
}
const El = /^rgba?\(\s*([-+.\d]+)(%)?[\s,]+([-+.e\d]+)(%)?[\s,]+([-+.e\d]+)(%)?(?:[\s,/]+([-+.e\d]+)(%)?)?\s*\)$/;
function Rl(i) {
  const t = El.exec(i);
  let e = 255, n, s, o;
  if (t) {
    if (t[7] !== n) {
      const r = +t[7];
      e = t[8] ? Ue(r) : Zt(r * 255, 0, 255);
    }
    return n = +t[1], s = +t[3], o = +t[5], n = 255 & (t[2] ? Ue(n) : Zt(n, 0, 255)), s = 255 & (t[4] ? Ue(s) : Zt(s, 0, 255)), o = 255 & (t[6] ? Ue(o) : Zt(o, 0, 255)), {
      r: n,
      g: s,
      b: o,
      a: e
    };
  }
}
function Ll(i) {
  return i && (i.a < 255 ? `rgba(${i.r}, ${i.g}, ${i.b}, ${Wt(i.a)})` : `rgb(${i.r}, ${i.g}, ${i.b})`);
}
const _n = (i) => i <= 31308e-7 ? i * 12.92 : Math.pow(i, 1 / 2.4) * 1.055 - 0.055, we = (i) => i <= 0.04045 ? i / 12.92 : Math.pow((i + 0.055) / 1.055, 2.4);
function Il(i, t, e) {
  const n = we(Wt(i.r)), s = we(Wt(i.g)), o = we(Wt(i.b));
  return {
    r: te(_n(n + e * (we(Wt(t.r)) - n))),
    g: te(_n(s + e * (we(Wt(t.g)) - s))),
    b: te(_n(o + e * (we(Wt(t.b)) - o))),
    a: i.a + e * (t.a - i.a)
  };
}
function Ti(i, t, e) {
  if (i) {
    let n = ns(i);
    n[t] = Math.max(0, Math.min(n[t] + n[t] * e, t === 0 ? 360 : 1)), n = os(n), i.r = n[0], i.g = n[1], i.b = n[2];
  }
}
function Pr(i, t) {
  return i && Object.assign(t || {}, i);
}
function Ys(i) {
  var t = { r: 0, g: 0, b: 0, a: 255 };
  return Array.isArray(i) ? i.length >= 3 && (t = { r: i[0], g: i[1], b: i[2], a: 255 }, i.length > 3 && (t.a = te(i[3]))) : (t = Pr(i, { r: 0, g: 0, b: 0, a: 1 }), t.a = te(t.a)), t;
}
function Fl(i) {
  return i.charAt(0) === "r" ? Rl(i) : Pl(i);
}
class ni {
  constructor(t) {
    if (t instanceof ni)
      return t;
    const e = typeof t;
    let n;
    e === "object" ? n = Ys(t) : e === "string" && (n = xl(t) || Al(t) || Fl(t)), this._rgb = n, this._valid = !!n;
  }
  get valid() {
    return this._valid;
  }
  get rgb() {
    var t = Pr(this._rgb);
    return t && (t.a = Wt(t.a)), t;
  }
  set rgb(t) {
    this._rgb = Ys(t);
  }
  rgbString() {
    return this._valid ? Ll(this._rgb) : void 0;
  }
  hexString() {
    return this._valid ? yl(this._rgb) : void 0;
  }
  hslString() {
    return this._valid ? Ol(this._rgb) : void 0;
  }
  mix(t, e) {
    if (t) {
      const n = this.rgb, s = t.rgb;
      let o;
      const r = e === o ? 0.5 : e, a = 2 * r - 1, l = n.a - s.a, c = ((a * l === -1 ? a : (a + l) / (1 + a * l)) + 1) / 2;
      o = 1 - c, n.r = 255 & c * n.r + o * s.r + 0.5, n.g = 255 & c * n.g + o * s.g + 0.5, n.b = 255 & c * n.b + o * s.b + 0.5, n.a = r * n.a + (1 - r) * s.a, this.rgb = n;
    }
    return this;
  }
  interpolate(t, e) {
    return t && (this._rgb = Il(this._rgb, t._rgb, e)), this;
  }
  clone() {
    return new ni(this.rgb);
  }
  alpha(t) {
    return this._rgb.a = te(t), this;
  }
  clearer(t) {
    const e = this._rgb;
    return e.a *= 1 - t, this;
  }
  greyscale() {
    const t = this._rgb, e = di(t.r * 0.3 + t.g * 0.59 + t.b * 0.11);
    return t.r = t.g = t.b = e, this;
  }
  opaquer(t) {
    const e = this._rgb;
    return e.a *= 1 + t, this;
  }
  negate() {
    const t = this._rgb;
    return t.r = 255 - t.r, t.g = 255 - t.g, t.b = 255 - t.b, this;
  }
  lighten(t) {
    return Ti(this._rgb, 2, t), this;
  }
  darken(t) {
    return Ti(this._rgb, 2, -t), this;
  }
  saturate(t) {
    return Ti(this._rgb, 1, t), this;
  }
  desaturate(t) {
    return Ti(this._rgb, 1, -t), this;
  }
  rotate(t) {
    return Cl(this._rgb, t), this;
  }
}
/*!
 * Chart.js v4.5.1
 * https://www.chartjs.org
 * (c) 2025 Chart.js Contributors
 * Released under the MIT License
 */
function Bt() {
}
const zl = /* @__PURE__ */ (() => {
  let i = 0;
  return () => i++;
})();
function V(i) {
  return i == null;
}
function q(i) {
  if (Array.isArray && Array.isArray(i))
    return !0;
  const t = Object.prototype.toString.call(i);
  return t.slice(0, 7) === "[object" && t.slice(-6) === "Array]";
}
function H(i) {
  return i !== null && Object.prototype.toString.call(i) === "[object Object]";
}
function K(i) {
  return (typeof i == "number" || i instanceof Number) && isFinite(+i);
}
function xt(i, t) {
  return K(i) ? i : t;
}
function I(i, t) {
  return typeof i > "u" ? t : i;
}
const Nl = (i, t) => typeof i == "string" && i.endsWith("%") ? parseFloat(i) / 100 : +i / t, Cr = (i, t) => typeof i == "string" && i.endsWith("%") ? parseFloat(i) / 100 * t : +i;
function L(i, t, e) {
  if (i && typeof i.call == "function")
    return i.apply(e, t);
}
function B(i, t, e, n) {
  let s, o, r;
  if (q(i))
    for (o = i.length, s = 0; s < o; s++)
      t.call(e, i[s], s);
  else if (H(i))
    for (r = Object.keys(i), o = r.length, s = 0; s < o; s++)
      t.call(e, i[r[s]], r[s]);
}
function $i(i, t) {
  let e, n, s, o;
  if (!i || !t || i.length !== t.length)
    return !1;
  for (e = 0, n = i.length; e < n; ++e)
    if (s = i[e], o = t[e], s.datasetIndex !== o.datasetIndex || s.index !== o.index)
      return !1;
  return !0;
}
function qi(i) {
  if (q(i))
    return i.map(qi);
  if (H(i)) {
    const t = /* @__PURE__ */ Object.create(null), e = Object.keys(i), n = e.length;
    let s = 0;
    for (; s < n; ++s)
      t[e[s]] = qi(i[e[s]]);
    return t;
  }
  return i;
}
function Or(i) {
  return [
    "__proto__",
    "prototype",
    "constructor"
  ].indexOf(i) === -1;
}
function Bl(i, t, e, n) {
  if (!Or(i))
    return;
  const s = t[i], o = e[i];
  H(s) && H(o) ? si(s, o, n) : t[i] = qi(o);
}
function si(i, t, e) {
  const n = q(t) ? t : [
    t
  ], s = n.length;
  if (!H(i))
    return i;
  e = e || {};
  const o = e.merger || Bl;
  let r;
  for (let a = 0; a < s; ++a) {
    if (r = n[a], !H(r))
      continue;
    const l = Object.keys(r);
    for (let c = 0, h = l.length; c < h; ++c)
      o(l[c], i, r, e);
  }
  return i;
}
function Ke(i, t) {
  return si(i, t, {
    merger: Vl
  });
}
function Vl(i, t, e) {
  if (!Or(i))
    return;
  const n = t[i], s = e[i];
  H(n) && H(s) ? Ke(n, s) : Object.prototype.hasOwnProperty.call(t, i) || (t[i] = qi(s));
}
const Us = {
  // Chart.helpers.core resolveObjectKey should resolve empty key to root object
  "": (i) => i,
  // default resolvers
  x: (i) => i.x,
  y: (i) => i.y
};
function Hl(i) {
  const t = i.split("."), e = [];
  let n = "";
  for (const s of t)
    n += s, n.endsWith("\\") ? n = n.slice(0, -1) + "." : (e.push(n), n = "");
  return e;
}
function Wl(i) {
  const t = Hl(i);
  return (e) => {
    for (const n of t) {
      if (n === "")
        break;
      e = e && e[n];
    }
    return e;
  };
}
function ee(i, t) {
  return (Us[t] || (Us[t] = Wl(t)))(i);
}
function rs(i) {
  return i.charAt(0).toUpperCase() + i.slice(1);
}
const oi = (i) => typeof i < "u", ie = (i) => typeof i == "function", Xs = (i, t) => {
  if (i.size !== t.size)
    return !1;
  for (const e of i)
    if (!t.has(e))
      return !1;
  return !0;
};
function jl(i) {
  return i.type === "mouseup" || i.type === "click" || i.type === "contextmenu";
}
const W = Math.PI, X = 2 * W, Yl = X + W, Zi = Number.POSITIVE_INFINITY, Ul = W / 180, J = W / 2, he = W / 4, $s = W * 2 / 3, Gt = Math.log10, St = Math.sign;
function me(i, t, e) {
  return Math.abs(i - t) < e;
}
function qs(i) {
  const t = Math.round(i);
  i = me(i, t, i / 1e3) ? t : i;
  const e = Math.pow(10, Math.floor(Gt(i))), n = i / e;
  return (n <= 1 ? 1 : n <= 2 ? 2 : n <= 5 ? 5 : 10) * e;
}
function Xl(i) {
  const t = [], e = Math.sqrt(i);
  let n;
  for (n = 1; n < e; n++)
    i % n === 0 && (t.push(n), t.push(i / n));
  return e === (e | 0) && t.push(e), t.sort((s, o) => s - o).pop(), t;
}
function $l(i) {
  return typeof i == "symbol" || typeof i == "object" && i !== null && !(Symbol.toPrimitive in i || "toString" in i || "valueOf" in i);
}
function Oe(i) {
  return !$l(i) && !isNaN(parseFloat(i)) && isFinite(i);
}
function ql(i, t) {
  const e = Math.round(i);
  return e - t <= i && e + t >= i;
}
function Dr(i, t, e) {
  let n, s, o;
  for (n = 0, s = i.length; n < s; n++)
    o = i[n][e], isNaN(o) || (t.min = Math.min(t.min, o), t.max = Math.max(t.max, o));
}
function Tt(i) {
  return i * (W / 180);
}
function as(i) {
  return i * (180 / W);
}
function Zs(i) {
  if (!K(i))
    return;
  let t = 1, e = 0;
  for (; Math.round(i * t) / t !== i; )
    t *= 10, e++;
  return e;
}
function Ar(i, t) {
  const e = t.x - i.x, n = t.y - i.y, s = Math.sqrt(e * e + n * n);
  let o = Math.atan2(n, e);
  return o < -0.5 * W && (o += X), {
    angle: o,
    distance: s
  };
}
function Fn(i, t) {
  return Math.sqrt(Math.pow(t.x - i.x, 2) + Math.pow(t.y - i.y, 2));
}
function Zl(i, t) {
  return (i - t + Yl) % X - W;
}
function lt(i) {
  return (i % X + X) % X;
}
function ri(i, t, e, n) {
  const s = lt(i), o = lt(t), r = lt(e), a = lt(o - s), l = lt(r - s), c = lt(s - o), h = lt(s - r);
  return s === o || s === r || n && o === r || a > l && c < h;
}
function ot(i, t, e) {
  return Math.max(t, Math.min(e, i));
}
function Gl(i) {
  return ot(i, -32768, 32767);
}
function jt(i, t, e, n = 1e-6) {
  return i >= Math.min(t, e) - n && i <= Math.max(t, e) + n;
}
function ls(i, t, e) {
  e = e || ((r) => i[r] < t);
  let n = i.length - 1, s = 0, o;
  for (; n - s > 1; )
    o = s + n >> 1, e(o) ? s = o : n = o;
  return {
    lo: s,
    hi: n
  };
}
const Yt = (i, t, e, n) => ls(i, e, n ? (s) => {
  const o = i[s][t];
  return o < e || o === e && i[s + 1][t] === e;
} : (s) => i[s][t] < e), Kl = (i, t, e) => ls(i, e, (n) => i[n][t] >= e);
function Jl(i, t, e) {
  let n = 0, s = i.length;
  for (; n < s && i[n] < t; )
    n++;
  for (; s > n && i[s - 1] > e; )
    s--;
  return n > 0 || s < i.length ? i.slice(n, s) : i;
}
const Er = [
  "push",
  "pop",
  "shift",
  "splice",
  "unshift"
];
function Ql(i, t) {
  if (i._chartjs) {
    i._chartjs.listeners.push(t);
    return;
  }
  Object.defineProperty(i, "_chartjs", {
    configurable: !0,
    enumerable: !1,
    value: {
      listeners: [
        t
      ]
    }
  }), Er.forEach((e) => {
    const n = "_onData" + rs(e), s = i[e];
    Object.defineProperty(i, e, {
      configurable: !0,
      enumerable: !1,
      value(...o) {
        const r = s.apply(this, o);
        return i._chartjs.listeners.forEach((a) => {
          typeof a[n] == "function" && a[n](...o);
        }), r;
      }
    });
  });
}
function Gs(i, t) {
  const e = i._chartjs;
  if (!e)
    return;
  const n = e.listeners, s = n.indexOf(t);
  s !== -1 && n.splice(s, 1), !(n.length > 0) && (Er.forEach((o) => {
    delete i[o];
  }), delete i._chartjs);
}
function Rr(i) {
  const t = new Set(i);
  return t.size === i.length ? i : Array.from(t);
}
const Lr = (function() {
  return typeof window > "u" ? function(i) {
    return i();
  } : window.requestAnimationFrame;
})();
function Ir(i, t) {
  let e = [], n = !1;
  return function(...s) {
    e = s, n || (n = !0, Lr.call(window, () => {
      n = !1, i.apply(t, e);
    }));
  };
}
function tc(i, t) {
  let e;
  return function(...n) {
    return t ? (clearTimeout(e), e = setTimeout(i, t, n)) : i.apply(this, n), t;
  };
}
const cs = (i) => i === "start" ? "left" : i === "end" ? "right" : "center", at = (i, t, e) => i === "start" ? t : i === "end" ? e : (t + e) / 2, ec = (i, t, e, n) => i === (n ? "left" : "right") ? e : i === "center" ? (t + e) / 2 : t;
function Fr(i, t, e) {
  const n = t.length;
  let s = 0, o = n;
  if (i._sorted) {
    const { iScale: r, vScale: a, _parsed: l } = i, c = i.dataset && i.dataset.options ? i.dataset.options.spanGaps : null, h = r.axis, { min: u, max: d, minDefined: g, maxDefined: m } = r.getUserBounds();
    if (g) {
      if (s = Math.min(
        // @ts-expect-error Need to type _parsed
        Yt(l, h, u).lo,
        // @ts-expect-error Need to fix types on _lookupByKey
        e ? n : Yt(t, h, r.getPixelForValue(u)).lo
      ), c) {
        const b = l.slice(0, s + 1).reverse().findIndex((_) => !V(_[a.axis]));
        s -= Math.max(0, b);
      }
      s = ot(s, 0, n - 1);
    }
    if (m) {
      let b = Math.max(
        // @ts-expect-error Need to type _parsed
        Yt(l, r.axis, d, !0).hi + 1,
        // @ts-expect-error Need to fix types on _lookupByKey
        e ? 0 : Yt(t, h, r.getPixelForValue(d), !0).hi + 1
      );
      if (c) {
        const _ = l.slice(b - 1).findIndex((y) => !V(y[a.axis]));
        b += Math.max(0, _);
      }
      o = ot(b, s, n) - s;
    } else
      o = n - s;
  }
  return {
    start: s,
    count: o
  };
}
function zr(i) {
  const { xScale: t, yScale: e, _scaleRanges: n } = i, s = {
    xmin: t.min,
    xmax: t.max,
    ymin: e.min,
    ymax: e.max
  };
  if (!n)
    return i._scaleRanges = s, !0;
  const o = n.xmin !== t.min || n.xmax !== t.max || n.ymin !== e.min || n.ymax !== e.max;
  return Object.assign(n, s), o;
}
const Pi = (i) => i === 0 || i === 1, Ks = (i, t, e) => -(Math.pow(2, 10 * (i -= 1)) * Math.sin((i - t) * X / e)), Js = (i, t, e) => Math.pow(2, -10 * i) * Math.sin((i - t) * X / e) + 1, Je = {
  linear: (i) => i,
  easeInQuad: (i) => i * i,
  easeOutQuad: (i) => -i * (i - 2),
  easeInOutQuad: (i) => (i /= 0.5) < 1 ? 0.5 * i * i : -0.5 * (--i * (i - 2) - 1),
  easeInCubic: (i) => i * i * i,
  easeOutCubic: (i) => (i -= 1) * i * i + 1,
  easeInOutCubic: (i) => (i /= 0.5) < 1 ? 0.5 * i * i * i : 0.5 * ((i -= 2) * i * i + 2),
  easeInQuart: (i) => i * i * i * i,
  easeOutQuart: (i) => -((i -= 1) * i * i * i - 1),
  easeInOutQuart: (i) => (i /= 0.5) < 1 ? 0.5 * i * i * i * i : -0.5 * ((i -= 2) * i * i * i - 2),
  easeInQuint: (i) => i * i * i * i * i,
  easeOutQuint: (i) => (i -= 1) * i * i * i * i + 1,
  easeInOutQuint: (i) => (i /= 0.5) < 1 ? 0.5 * i * i * i * i * i : 0.5 * ((i -= 2) * i * i * i * i + 2),
  easeInSine: (i) => -Math.cos(i * J) + 1,
  easeOutSine: (i) => Math.sin(i * J),
  easeInOutSine: (i) => -0.5 * (Math.cos(W * i) - 1),
  easeInExpo: (i) => i === 0 ? 0 : Math.pow(2, 10 * (i - 1)),
  easeOutExpo: (i) => i === 1 ? 1 : -Math.pow(2, -10 * i) + 1,
  easeInOutExpo: (i) => Pi(i) ? i : i < 0.5 ? 0.5 * Math.pow(2, 10 * (i * 2 - 1)) : 0.5 * (-Math.pow(2, -10 * (i * 2 - 1)) + 2),
  easeInCirc: (i) => i >= 1 ? i : -(Math.sqrt(1 - i * i) - 1),
  easeOutCirc: (i) => Math.sqrt(1 - (i -= 1) * i),
  easeInOutCirc: (i) => (i /= 0.5) < 1 ? -0.5 * (Math.sqrt(1 - i * i) - 1) : 0.5 * (Math.sqrt(1 - (i -= 2) * i) + 1),
  easeInElastic: (i) => Pi(i) ? i : Ks(i, 0.075, 0.3),
  easeOutElastic: (i) => Pi(i) ? i : Js(i, 0.075, 0.3),
  easeInOutElastic(i) {
    return Pi(i) ? i : i < 0.5 ? 0.5 * Ks(i * 2, 0.1125, 0.45) : 0.5 + 0.5 * Js(i * 2 - 1, 0.1125, 0.45);
  },
  easeInBack(i) {
    return i * i * ((1.70158 + 1) * i - 1.70158);
  },
  easeOutBack(i) {
    return (i -= 1) * i * ((1.70158 + 1) * i + 1.70158) + 1;
  },
  easeInOutBack(i) {
    let t = 1.70158;
    return (i /= 0.5) < 1 ? 0.5 * (i * i * (((t *= 1.525) + 1) * i - t)) : 0.5 * ((i -= 2) * i * (((t *= 1.525) + 1) * i + t) + 2);
  },
  easeInBounce: (i) => 1 - Je.easeOutBounce(1 - i),
  easeOutBounce(i) {
    return i < 1 / 2.75 ? 7.5625 * i * i : i < 2 / 2.75 ? 7.5625 * (i -= 1.5 / 2.75) * i + 0.75 : i < 2.5 / 2.75 ? 7.5625 * (i -= 2.25 / 2.75) * i + 0.9375 : 7.5625 * (i -= 2.625 / 2.75) * i + 0.984375;
  },
  easeInOutBounce: (i) => i < 0.5 ? Je.easeInBounce(i * 2) * 0.5 : Je.easeOutBounce(i * 2 - 1) * 0.5 + 0.5
};
function hs(i) {
  if (i && typeof i == "object") {
    const t = i.toString();
    return t === "[object CanvasPattern]" || t === "[object CanvasGradient]";
  }
  return !1;
}
function Qs(i) {
  return hs(i) ? i : new ni(i);
}
function yn(i) {
  return hs(i) ? i : new ni(i).saturate(0.5).darken(0.1).hexString();
}
const ic = [
  "x",
  "y",
  "borderWidth",
  "radius",
  "tension"
], nc = [
  "color",
  "borderColor",
  "backgroundColor"
];
function sc(i) {
  i.set("animation", {
    delay: void 0,
    duration: 1e3,
    easing: "easeOutQuart",
    fn: void 0,
    from: void 0,
    loop: void 0,
    to: void 0,
    type: void 0
  }), i.describe("animation", {
    _fallback: !1,
    _indexable: !1,
    _scriptable: (t) => t !== "onProgress" && t !== "onComplete" && t !== "fn"
  }), i.set("animations", {
    colors: {
      type: "color",
      properties: nc
    },
    numbers: {
      type: "number",
      properties: ic
    }
  }), i.describe("animations", {
    _fallback: "animation"
  }), i.set("transitions", {
    active: {
      animation: {
        duration: 400
      }
    },
    resize: {
      animation: {
        duration: 0
      }
    },
    show: {
      animations: {
        colors: {
          from: "transparent"
        },
        visible: {
          type: "boolean",
          duration: 0
        }
      }
    },
    hide: {
      animations: {
        colors: {
          to: "transparent"
        },
        visible: {
          type: "boolean",
          easing: "linear",
          fn: (t) => t | 0
        }
      }
    }
  });
}
function oc(i) {
  i.set("layout", {
    autoPadding: !0,
    padding: {
      top: 0,
      right: 0,
      bottom: 0,
      left: 0
    }
  });
}
const to = /* @__PURE__ */ new Map();
function rc(i, t) {
  t = t || {};
  const e = i + JSON.stringify(t);
  let n = to.get(e);
  return n || (n = new Intl.NumberFormat(i, t), to.set(e, n)), n;
}
function fi(i, t, e) {
  return rc(t, e).format(i);
}
const Nr = {
  values(i) {
    return q(i) ? i : "" + i;
  },
  numeric(i, t, e) {
    if (i === 0)
      return "0";
    const n = this.chart.options.locale;
    let s, o = i;
    if (e.length > 1) {
      const c = Math.max(Math.abs(e[0].value), Math.abs(e[e.length - 1].value));
      (c < 1e-4 || c > 1e15) && (s = "scientific"), o = ac(i, e);
    }
    const r = Gt(Math.abs(o)), a = isNaN(r) ? 1 : Math.max(Math.min(-1 * Math.floor(r), 20), 0), l = {
      notation: s,
      minimumFractionDigits: a,
      maximumFractionDigits: a
    };
    return Object.assign(l, this.options.ticks.format), fi(i, n, l);
  },
  logarithmic(i, t, e) {
    if (i === 0)
      return "0";
    const n = e[t].significand || i / Math.pow(10, Math.floor(Gt(i)));
    return [
      1,
      2,
      3,
      5,
      10,
      15
    ].includes(n) || t > 0.8 * e.length ? Nr.numeric.call(this, i, t, e) : "";
  }
};
function ac(i, t) {
  let e = t.length > 3 ? t[2].value - t[1].value : t[1].value - t[0].value;
  return Math.abs(e) >= 1 && i !== Math.floor(i) && (e = i - Math.floor(i)), e;
}
var en = {
  formatters: Nr
};
function lc(i) {
  i.set("scale", {
    display: !0,
    offset: !1,
    reverse: !1,
    beginAtZero: !1,
    bounds: "ticks",
    clip: !0,
    grace: 0,
    grid: {
      display: !0,
      lineWidth: 1,
      drawOnChartArea: !0,
      drawTicks: !0,
      tickLength: 8,
      tickWidth: (t, e) => e.lineWidth,
      tickColor: (t, e) => e.color,
      offset: !1
    },
    border: {
      display: !0,
      dash: [],
      dashOffset: 0,
      width: 1
    },
    title: {
      display: !1,
      text: "",
      padding: {
        top: 4,
        bottom: 4
      }
    },
    ticks: {
      minRotation: 0,
      maxRotation: 50,
      mirror: !1,
      textStrokeWidth: 0,
      textStrokeColor: "",
      padding: 3,
      display: !0,
      autoSkip: !0,
      autoSkipPadding: 3,
      labelOffset: 0,
      callback: en.formatters.values,
      minor: {},
      major: {},
      align: "center",
      crossAlign: "near",
      showLabelBackdrop: !1,
      backdropColor: "rgba(255, 255, 255, 0.75)",
      backdropPadding: 2
    }
  }), i.route("scale.ticks", "color", "", "color"), i.route("scale.grid", "color", "", "borderColor"), i.route("scale.border", "color", "", "borderColor"), i.route("scale.title", "color", "", "color"), i.describe("scale", {
    _fallback: !1,
    _scriptable: (t) => !t.startsWith("before") && !t.startsWith("after") && t !== "callback" && t !== "parser",
    _indexable: (t) => t !== "borderDash" && t !== "tickBorderDash" && t !== "dash"
  }), i.describe("scales", {
    _fallback: "scale"
  }), i.describe("scale.ticks", {
    _scriptable: (t) => t !== "backdropPadding" && t !== "callback",
    _indexable: (t) => t !== "backdropPadding"
  });
}
const _e = /* @__PURE__ */ Object.create(null), zn = /* @__PURE__ */ Object.create(null);
function Qe(i, t) {
  if (!t)
    return i;
  const e = t.split(".");
  for (let n = 0, s = e.length; n < s; ++n) {
    const o = e[n];
    i = i[o] || (i[o] = /* @__PURE__ */ Object.create(null));
  }
  return i;
}
function vn(i, t, e) {
  return typeof t == "string" ? si(Qe(i, t), e) : si(Qe(i, ""), t);
}
class cc {
  constructor(t, e) {
    this.animation = void 0, this.backgroundColor = "rgba(0,0,0,0.1)", this.borderColor = "rgba(0,0,0,0.1)", this.color = "#666", this.datasets = {}, this.devicePixelRatio = (n) => n.chart.platform.getDevicePixelRatio(), this.elements = {}, this.events = [
      "mousemove",
      "mouseout",
      "click",
      "touchstart",
      "touchmove"
    ], this.font = {
      family: "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif",
      size: 12,
      style: "normal",
      lineHeight: 1.2,
      weight: null
    }, this.hover = {}, this.hoverBackgroundColor = (n, s) => yn(s.backgroundColor), this.hoverBorderColor = (n, s) => yn(s.borderColor), this.hoverColor = (n, s) => yn(s.color), this.indexAxis = "x", this.interaction = {
      mode: "nearest",
      intersect: !0,
      includeInvisible: !1
    }, this.maintainAspectRatio = !0, this.onHover = null, this.onClick = null, this.parsing = !0, this.plugins = {}, this.responsive = !0, this.scale = void 0, this.scales = {}, this.showLine = !0, this.drawActiveElementsOnTop = !0, this.describe(t), this.apply(e);
  }
  set(t, e) {
    return vn(this, t, e);
  }
  get(t) {
    return Qe(this, t);
  }
  describe(t, e) {
    return vn(zn, t, e);
  }
  override(t, e) {
    return vn(_e, t, e);
  }
  route(t, e, n, s) {
    const o = Qe(this, t), r = Qe(this, n), a = "_" + e;
    Object.defineProperties(o, {
      [a]: {
        value: o[e],
        writable: !0
      },
      [e]: {
        enumerable: !0,
        get() {
          const l = this[a], c = r[s];
          return H(l) ? Object.assign({}, c, l) : I(l, c);
        },
        set(l) {
          this[a] = l;
        }
      }
    });
  }
  apply(t) {
    t.forEach((e) => e(this));
  }
}
var Z = /* @__PURE__ */ new cc({
  _scriptable: (i) => !i.startsWith("on"),
  _indexable: (i) => i !== "events",
  hover: {
    _fallback: "interaction"
  },
  interaction: {
    _scriptable: !1,
    _indexable: !1
  }
}, [
  sc,
  oc,
  lc
]);
function hc(i) {
  return !i || V(i.size) || V(i.family) ? null : (i.style ? i.style + " " : "") + (i.weight ? i.weight + " " : "") + i.size + "px " + i.family;
}
function Gi(i, t, e, n, s) {
  let o = t[s];
  return o || (o = t[s] = i.measureText(s).width, e.push(s)), o > n && (n = o), n;
}
function uc(i, t, e, n) {
  n = n || {};
  let s = n.data = n.data || {}, o = n.garbageCollect = n.garbageCollect || [];
  n.font !== t && (s = n.data = {}, o = n.garbageCollect = [], n.font = t), i.save(), i.font = t;
  let r = 0;
  const a = e.length;
  let l, c, h, u, d;
  for (l = 0; l < a; l++)
    if (u = e[l], u != null && !q(u))
      r = Gi(i, s, o, r, u);
    else if (q(u))
      for (c = 0, h = u.length; c < h; c++)
        d = u[c], d != null && !q(d) && (r = Gi(i, s, o, r, d));
  i.restore();
  const g = o.length / 2;
  if (g > e.length) {
    for (l = 0; l < g; l++)
      delete s[o[l]];
    o.splice(0, g);
  }
  return r;
}
function ue(i, t, e) {
  const n = i.currentDevicePixelRatio, s = e !== 0 ? Math.max(e / 2, 0.5) : 0;
  return Math.round((t - s) * n) / n + s;
}
function eo(i, t) {
  !t && !i || (t = t || i.getContext("2d"), t.save(), t.resetTransform(), t.clearRect(0, 0, i.width, i.height), t.restore());
}
function Nn(i, t, e, n) {
  Br(i, t, e, n, null);
}
function Br(i, t, e, n, s) {
  let o, r, a, l, c, h, u, d;
  const g = t.pointStyle, m = t.rotation, b = t.radius;
  let _ = (m || 0) * Ul;
  if (g && typeof g == "object" && (o = g.toString(), o === "[object HTMLImageElement]" || o === "[object HTMLCanvasElement]")) {
    i.save(), i.translate(e, n), i.rotate(_), i.drawImage(g, -g.width / 2, -g.height / 2, g.width, g.height), i.restore();
    return;
  }
  if (!(isNaN(b) || b <= 0)) {
    switch (i.beginPath(), g) {
      // Default includes circle
      default:
        s ? i.ellipse(e, n, s / 2, b, 0, 0, X) : i.arc(e, n, b, 0, X), i.closePath();
        break;
      case "triangle":
        h = s ? s / 2 : b, i.moveTo(e + Math.sin(_) * h, n - Math.cos(_) * b), _ += $s, i.lineTo(e + Math.sin(_) * h, n - Math.cos(_) * b), _ += $s, i.lineTo(e + Math.sin(_) * h, n - Math.cos(_) * b), i.closePath();
        break;
      case "rectRounded":
        c = b * 0.516, l = b - c, r = Math.cos(_ + he) * l, u = Math.cos(_ + he) * (s ? s / 2 - c : l), a = Math.sin(_ + he) * l, d = Math.sin(_ + he) * (s ? s / 2 - c : l), i.arc(e - u, n - a, c, _ - W, _ - J), i.arc(e + d, n - r, c, _ - J, _), i.arc(e + u, n + a, c, _, _ + J), i.arc(e - d, n + r, c, _ + J, _ + W), i.closePath();
        break;
      case "rect":
        if (!m) {
          l = Math.SQRT1_2 * b, h = s ? s / 2 : l, i.rect(e - h, n - l, 2 * h, 2 * l);
          break;
        }
        _ += he;
      /* falls through */
      case "rectRot":
        u = Math.cos(_) * (s ? s / 2 : b), r = Math.cos(_) * b, a = Math.sin(_) * b, d = Math.sin(_) * (s ? s / 2 : b), i.moveTo(e - u, n - a), i.lineTo(e + d, n - r), i.lineTo(e + u, n + a), i.lineTo(e - d, n + r), i.closePath();
        break;
      case "crossRot":
        _ += he;
      /* falls through */
      case "cross":
        u = Math.cos(_) * (s ? s / 2 : b), r = Math.cos(_) * b, a = Math.sin(_) * b, d = Math.sin(_) * (s ? s / 2 : b), i.moveTo(e - u, n - a), i.lineTo(e + u, n + a), i.moveTo(e + d, n - r), i.lineTo(e - d, n + r);
        break;
      case "star":
        u = Math.cos(_) * (s ? s / 2 : b), r = Math.cos(_) * b, a = Math.sin(_) * b, d = Math.sin(_) * (s ? s / 2 : b), i.moveTo(e - u, n - a), i.lineTo(e + u, n + a), i.moveTo(e + d, n - r), i.lineTo(e - d, n + r), _ += he, u = Math.cos(_) * (s ? s / 2 : b), r = Math.cos(_) * b, a = Math.sin(_) * b, d = Math.sin(_) * (s ? s / 2 : b), i.moveTo(e - u, n - a), i.lineTo(e + u, n + a), i.moveTo(e + d, n - r), i.lineTo(e - d, n + r);
        break;
      case "line":
        r = s ? s / 2 : Math.cos(_) * b, a = Math.sin(_) * b, i.moveTo(e - r, n - a), i.lineTo(e + r, n + a);
        break;
      case "dash":
        i.moveTo(e, n), i.lineTo(e + Math.cos(_) * (s ? s / 2 : b), n + Math.sin(_) * b);
        break;
      case !1:
        i.closePath();
        break;
    }
    i.fill(), t.borderWidth > 0 && i.stroke();
  }
}
function It(i, t, e) {
  return e = e || 0.5, !t || i && i.x > t.left - e && i.x < t.right + e && i.y > t.top - e && i.y < t.bottom + e;
}
function nn(i, t) {
  i.save(), i.beginPath(), i.rect(t.left, t.top, t.right - t.left, t.bottom - t.top), i.clip();
}
function sn(i) {
  i.restore();
}
function dc(i, t, e, n, s) {
  if (!t)
    return i.lineTo(e.x, e.y);
  if (s === "middle") {
    const o = (t.x + e.x) / 2;
    i.lineTo(o, t.y), i.lineTo(o, e.y);
  } else s === "after" != !!n ? i.lineTo(t.x, e.y) : i.lineTo(e.x, t.y);
  i.lineTo(e.x, e.y);
}
function fc(i, t, e, n) {
  if (!t)
    return i.lineTo(e.x, e.y);
  i.bezierCurveTo(n ? t.cp1x : t.cp2x, n ? t.cp1y : t.cp2y, n ? e.cp2x : e.cp1x, n ? e.cp2y : e.cp1y, e.x, e.y);
}
function gc(i, t) {
  t.translation && i.translate(t.translation[0], t.translation[1]), V(t.rotation) || i.rotate(t.rotation), t.color && (i.fillStyle = t.color), t.textAlign && (i.textAlign = t.textAlign), t.textBaseline && (i.textBaseline = t.textBaseline);
}
function pc(i, t, e, n, s) {
  if (s.strikethrough || s.underline) {
    const o = i.measureText(n), r = t - o.actualBoundingBoxLeft, a = t + o.actualBoundingBoxRight, l = e - o.actualBoundingBoxAscent, c = e + o.actualBoundingBoxDescent, h = s.strikethrough ? (l + c) / 2 : c;
    i.strokeStyle = i.fillStyle, i.beginPath(), i.lineWidth = s.decorationWidth || 2, i.moveTo(r, h), i.lineTo(a, h), i.stroke();
  }
}
function mc(i, t) {
  const e = i.fillStyle;
  i.fillStyle = t.color, i.fillRect(t.left, t.top, t.width, t.height), i.fillStyle = e;
}
function ye(i, t, e, n, s, o = {}) {
  const r = q(t) ? t : [
    t
  ], a = o.strokeWidth > 0 && o.strokeColor !== "";
  let l, c;
  for (i.save(), i.font = s.string, gc(i, o), l = 0; l < r.length; ++l)
    c = r[l], o.backdrop && mc(i, o.backdrop), a && (o.strokeColor && (i.strokeStyle = o.strokeColor), V(o.strokeWidth) || (i.lineWidth = o.strokeWidth), i.strokeText(c, e, n, o.maxWidth)), i.fillText(c, e, n, o.maxWidth), pc(i, e, n, c, o), n += Number(s.lineHeight);
  i.restore();
}
function ai(i, t) {
  const { x: e, y: n, w: s, h: o, radius: r } = t;
  i.arc(e + r.topLeft, n + r.topLeft, r.topLeft, 1.5 * W, W, !0), i.lineTo(e, n + o - r.bottomLeft), i.arc(e + r.bottomLeft, n + o - r.bottomLeft, r.bottomLeft, W, J, !0), i.lineTo(e + s - r.bottomRight, n + o), i.arc(e + s - r.bottomRight, n + o - r.bottomRight, r.bottomRight, J, 0, !0), i.lineTo(e + s, n + r.topRight), i.arc(e + s - r.topRight, n + r.topRight, r.topRight, 0, -J, !0), i.lineTo(e + r.topLeft, n);
}
const bc = /^(normal|(\d+(?:\.\d+)?)(px|em|%)?)$/, xc = /^(normal|italic|initial|inherit|unset|(oblique( -?[0-9]?[0-9]deg)?))$/;
function _c(i, t) {
  const e = ("" + i).match(bc);
  if (!e || e[1] === "normal")
    return t * 1.2;
  switch (i = +e[2], e[3]) {
    case "px":
      return i;
    case "%":
      i /= 100;
      break;
  }
  return t * i;
}
const yc = (i) => +i || 0;
function us(i, t) {
  const e = {}, n = H(t), s = n ? Object.keys(t) : t, o = H(i) ? n ? (r) => I(i[r], i[t[r]]) : (r) => i[r] : () => i;
  for (const r of s)
    e[r] = yc(o(r));
  return e;
}
function Vr(i) {
  return us(i, {
    top: "y",
    right: "x",
    bottom: "y",
    left: "x"
  });
}
function be(i) {
  return us(i, [
    "topLeft",
    "topRight",
    "bottomLeft",
    "bottomRight"
  ]);
}
function ht(i) {
  const t = Vr(i);
  return t.width = t.left + t.right, t.height = t.top + t.bottom, t;
}
function it(i, t) {
  i = i || {}, t = t || Z.font;
  let e = I(i.size, t.size);
  typeof e == "string" && (e = parseInt(e, 10));
  let n = I(i.style, t.style);
  n && !("" + n).match(xc) && (console.warn('Invalid font style specified: "' + n + '"'), n = void 0);
  const s = {
    family: I(i.family, t.family),
    lineHeight: _c(I(i.lineHeight, t.lineHeight), e),
    size: e,
    style: n,
    weight: I(i.weight, t.weight),
    string: ""
  };
  return s.string = hc(s), s;
}
function Xe(i, t, e, n) {
  let s, o, r;
  for (s = 0, o = i.length; s < o; ++s)
    if (r = i[s], r !== void 0 && r !== void 0)
      return r;
}
function vc(i, t, e) {
  const { min: n, max: s } = i, o = Cr(t, (s - n) / 2), r = (a, l) => e && a === 0 ? 0 : a + l;
  return {
    min: r(n, -Math.abs(o)),
    max: r(s, o)
  };
}
function se(i, t) {
  return Object.assign(Object.create(i), t);
}
function ds(i, t = [
  ""
], e, n, s = () => i[0]) {
  const o = e || i;
  typeof n > "u" && (n = Yr("_fallback", i));
  const r = {
    [Symbol.toStringTag]: "Object",
    _cacheable: !0,
    _scopes: i,
    _rootScopes: o,
    _fallback: n,
    _getTarget: s,
    override: (a) => ds([
      a,
      ...i
    ], t, o, n)
  };
  return new Proxy(r, {
    /**
    * A trap for the delete operator.
    */
    deleteProperty(a, l) {
      return delete a[l], delete a._keys, delete i[0][l], !0;
    },
    /**
    * A trap for getting property values.
    */
    get(a, l) {
      return Wr(a, l, () => Oc(l, t, i, a));
    },
    /**
    * A trap for Object.getOwnPropertyDescriptor.
    * Also used by Object.hasOwnProperty.
    */
    getOwnPropertyDescriptor(a, l) {
      return Reflect.getOwnPropertyDescriptor(a._scopes[0], l);
    },
    /**
    * A trap for Object.getPrototypeOf.
    */
    getPrototypeOf() {
      return Reflect.getPrototypeOf(i[0]);
    },
    /**
    * A trap for the in operator.
    */
    has(a, l) {
      return no(a).includes(l);
    },
    /**
    * A trap for Object.getOwnPropertyNames and Object.getOwnPropertySymbols.
    */
    ownKeys(a) {
      return no(a);
    },
    /**
    * A trap for setting property values.
    */
    set(a, l, c) {
      const h = a._storage || (a._storage = s());
      return a[l] = h[l] = c, delete a._keys, !0;
    }
  });
}
function De(i, t, e, n) {
  const s = {
    _cacheable: !1,
    _proxy: i,
    _context: t,
    _subProxy: e,
    _stack: /* @__PURE__ */ new Set(),
    _descriptors: Hr(i, n),
    setContext: (o) => De(i, o, e, n),
    override: (o) => De(i.override(o), t, e, n)
  };
  return new Proxy(s, {
    /**
    * A trap for the delete operator.
    */
    deleteProperty(o, r) {
      return delete o[r], delete i[r], !0;
    },
    /**
    * A trap for getting property values.
    */
    get(o, r, a) {
      return Wr(o, r, () => Sc(o, r, a));
    },
    /**
    * A trap for Object.getOwnPropertyDescriptor.
    * Also used by Object.hasOwnProperty.
    */
    getOwnPropertyDescriptor(o, r) {
      return o._descriptors.allKeys ? Reflect.has(i, r) ? {
        enumerable: !0,
        configurable: !0
      } : void 0 : Reflect.getOwnPropertyDescriptor(i, r);
    },
    /**
    * A trap for Object.getPrototypeOf.
    */
    getPrototypeOf() {
      return Reflect.getPrototypeOf(i);
    },
    /**
    * A trap for the in operator.
    */
    has(o, r) {
      return Reflect.has(i, r);
    },
    /**
    * A trap for Object.getOwnPropertyNames and Object.getOwnPropertySymbols.
    */
    ownKeys() {
      return Reflect.ownKeys(i);
    },
    /**
    * A trap for setting property values.
    */
    set(o, r, a) {
      return i[r] = a, delete o[r], !0;
    }
  });
}
function Hr(i, t = {
  scriptable: !0,
  indexable: !0
}) {
  const { _scriptable: e = t.scriptable, _indexable: n = t.indexable, _allKeys: s = t.allKeys } = i;
  return {
    allKeys: s,
    scriptable: e,
    indexable: n,
    isScriptable: ie(e) ? e : () => e,
    isIndexable: ie(n) ? n : () => n
  };
}
const Mc = (i, t) => i ? i + rs(t) : t, fs = (i, t) => H(t) && i !== "adapters" && (Object.getPrototypeOf(t) === null || t.constructor === Object);
function Wr(i, t, e) {
  if (Object.prototype.hasOwnProperty.call(i, t) || t === "constructor")
    return i[t];
  const n = e();
  return i[t] = n, n;
}
function Sc(i, t, e) {
  const { _proxy: n, _context: s, _subProxy: o, _descriptors: r } = i;
  let a = n[t];
  return ie(a) && r.isScriptable(t) && (a = kc(t, a, i, e)), q(a) && a.length && (a = wc(t, a, i, r.isIndexable)), fs(t, a) && (a = De(a, s, o && o[t], r)), a;
}
function kc(i, t, e, n) {
  const { _proxy: s, _context: o, _subProxy: r, _stack: a } = e;
  if (a.has(i))
    throw new Error("Recursion detected: " + Array.from(a).join("->") + "->" + i);
  a.add(i);
  let l = t(o, r || n);
  return a.delete(i), fs(i, l) && (l = gs(s._scopes, s, i, l)), l;
}
function wc(i, t, e, n) {
  const { _proxy: s, _context: o, _subProxy: r, _descriptors: a } = e;
  if (typeof o.index < "u" && n(i))
    return t[o.index % t.length];
  if (H(t[0])) {
    const l = t, c = s._scopes.filter((h) => h !== l);
    t = [];
    for (const h of l) {
      const u = gs(c, s, i, h);
      t.push(De(u, o, r && r[i], a));
    }
  }
  return t;
}
function jr(i, t, e) {
  return ie(i) ? i(t, e) : i;
}
const Tc = (i, t) => i === !0 ? t : typeof i == "string" ? ee(t, i) : void 0;
function Pc(i, t, e, n, s) {
  for (const o of t) {
    const r = Tc(e, o);
    if (r) {
      i.add(r);
      const a = jr(r._fallback, e, s);
      if (typeof a < "u" && a !== e && a !== n)
        return a;
    } else if (r === !1 && typeof n < "u" && e !== n)
      return null;
  }
  return !1;
}
function gs(i, t, e, n) {
  const s = t._rootScopes, o = jr(t._fallback, e, n), r = [
    ...i,
    ...s
  ], a = /* @__PURE__ */ new Set();
  a.add(n);
  let l = io(a, r, e, o || e, n);
  return l === null || typeof o < "u" && o !== e && (l = io(a, r, o, l, n), l === null) ? !1 : ds(Array.from(a), [
    ""
  ], s, o, () => Cc(t, e, n));
}
function io(i, t, e, n, s) {
  for (; e; )
    e = Pc(i, t, e, n, s);
  return e;
}
function Cc(i, t, e) {
  const n = i._getTarget();
  t in n || (n[t] = {});
  const s = n[t];
  return q(s) && H(e) ? e : s || {};
}
function Oc(i, t, e, n) {
  let s;
  for (const o of t)
    if (s = Yr(Mc(o, i), e), typeof s < "u")
      return fs(i, s) ? gs(e, n, i, s) : s;
}
function Yr(i, t) {
  for (const e of t) {
    if (!e)
      continue;
    const n = e[i];
    if (typeof n < "u")
      return n;
  }
}
function no(i) {
  let t = i._keys;
  return t || (t = i._keys = Dc(i._scopes)), t;
}
function Dc(i) {
  const t = /* @__PURE__ */ new Set();
  for (const e of i)
    for (const n of Object.keys(e).filter((s) => !s.startsWith("_")))
      t.add(n);
  return Array.from(t);
}
function Ur(i, t, e, n) {
  const { iScale: s } = i, { key: o = "r" } = this._parsing, r = new Array(n);
  let a, l, c, h;
  for (a = 0, l = n; a < l; ++a)
    c = a + e, h = t[c], r[a] = {
      r: s.parse(ee(h, o), c)
    };
  return r;
}
const Ac = Number.EPSILON || 1e-14, Ae = (i, t) => t < i.length && !i[t].skip && i[t], Xr = (i) => i === "x" ? "y" : "x";
function Ec(i, t, e, n) {
  const s = i.skip ? t : i, o = t, r = e.skip ? t : e, a = Fn(o, s), l = Fn(r, o);
  let c = a / (a + l), h = l / (a + l);
  c = isNaN(c) ? 0 : c, h = isNaN(h) ? 0 : h;
  const u = n * c, d = n * h;
  return {
    previous: {
      x: o.x - u * (r.x - s.x),
      y: o.y - u * (r.y - s.y)
    },
    next: {
      x: o.x + d * (r.x - s.x),
      y: o.y + d * (r.y - s.y)
    }
  };
}
function Rc(i, t, e) {
  const n = i.length;
  let s, o, r, a, l, c = Ae(i, 0);
  for (let h = 0; h < n - 1; ++h)
    if (l = c, c = Ae(i, h + 1), !(!l || !c)) {
      if (me(t[h], 0, Ac)) {
        e[h] = e[h + 1] = 0;
        continue;
      }
      s = e[h] / t[h], o = e[h + 1] / t[h], a = Math.pow(s, 2) + Math.pow(o, 2), !(a <= 9) && (r = 3 / Math.sqrt(a), e[h] = s * r * t[h], e[h + 1] = o * r * t[h]);
    }
}
function Lc(i, t, e = "x") {
  const n = Xr(e), s = i.length;
  let o, r, a, l = Ae(i, 0);
  for (let c = 0; c < s; ++c) {
    if (r = a, a = l, l = Ae(i, c + 1), !a)
      continue;
    const h = a[e], u = a[n];
    r && (o = (h - r[e]) / 3, a[`cp1${e}`] = h - o, a[`cp1${n}`] = u - o * t[c]), l && (o = (l[e] - h) / 3, a[`cp2${e}`] = h + o, a[`cp2${n}`] = u + o * t[c]);
  }
}
function Ic(i, t = "x") {
  const e = Xr(t), n = i.length, s = Array(n).fill(0), o = Array(n);
  let r, a, l, c = Ae(i, 0);
  for (r = 0; r < n; ++r)
    if (a = l, l = c, c = Ae(i, r + 1), !!l) {
      if (c) {
        const h = c[t] - l[t];
        s[r] = h !== 0 ? (c[e] - l[e]) / h : 0;
      }
      o[r] = a ? c ? St(s[r - 1]) !== St(s[r]) ? 0 : (s[r - 1] + s[r]) / 2 : s[r - 1] : s[r];
    }
  Rc(i, s, o), Lc(i, o, t);
}
function Ci(i, t, e) {
  return Math.max(Math.min(i, e), t);
}
function Fc(i, t) {
  let e, n, s, o, r, a = It(i[0], t);
  for (e = 0, n = i.length; e < n; ++e)
    r = o, o = a, a = e < n - 1 && It(i[e + 1], t), o && (s = i[e], r && (s.cp1x = Ci(s.cp1x, t.left, t.right), s.cp1y = Ci(s.cp1y, t.top, t.bottom)), a && (s.cp2x = Ci(s.cp2x, t.left, t.right), s.cp2y = Ci(s.cp2y, t.top, t.bottom)));
}
function zc(i, t, e, n, s) {
  let o, r, a, l;
  if (t.spanGaps && (i = i.filter((c) => !c.skip)), t.cubicInterpolationMode === "monotone")
    Ic(i, s);
  else {
    let c = n ? i[i.length - 1] : i[0];
    for (o = 0, r = i.length; o < r; ++o)
      a = i[o], l = Ec(c, a, i[Math.min(o + 1, r - (n ? 0 : 1)) % r], t.tension), a.cp1x = l.previous.x, a.cp1y = l.previous.y, a.cp2x = l.next.x, a.cp2y = l.next.y, c = a;
  }
  t.capBezierPoints && Fc(i, e);
}
function ps() {
  return typeof window < "u" && typeof document < "u";
}
function ms(i) {
  let t = i.parentNode;
  return t && t.toString() === "[object ShadowRoot]" && (t = t.host), t;
}
function Ki(i, t, e) {
  let n;
  return typeof i == "string" ? (n = parseInt(i, 10), i.indexOf("%") !== -1 && (n = n / 100 * t.parentNode[e])) : n = i, n;
}
const on = (i) => i.ownerDocument.defaultView.getComputedStyle(i, null);
function Nc(i, t) {
  return on(i).getPropertyValue(t);
}
const Bc = [
  "top",
  "right",
  "bottom",
  "left"
];
function xe(i, t, e) {
  const n = {};
  e = e ? "-" + e : "";
  for (let s = 0; s < 4; s++) {
    const o = Bc[s];
    n[o] = parseFloat(i[t + "-" + o + e]) || 0;
  }
  return n.width = n.left + n.right, n.height = n.top + n.bottom, n;
}
const Vc = (i, t, e) => (i > 0 || t > 0) && (!e || !e.shadowRoot);
function Hc(i, t) {
  const e = i.touches, n = e && e.length ? e[0] : i, { offsetX: s, offsetY: o } = n;
  let r = !1, a, l;
  if (Vc(s, o, i.target))
    a = s, l = o;
  else {
    const c = t.getBoundingClientRect();
    a = n.clientX - c.left, l = n.clientY - c.top, r = !0;
  }
  return {
    x: a,
    y: l,
    box: r
  };
}
function Lt(i, t) {
  if ("native" in i)
    return i;
  const { canvas: e, currentDevicePixelRatio: n } = t, s = on(e), o = s.boxSizing === "border-box", r = xe(s, "padding"), a = xe(s, "border", "width"), { x: l, y: c, box: h } = Hc(i, e), u = r.left + (h && a.left), d = r.top + (h && a.top);
  let { width: g, height: m } = t;
  return o && (g -= r.width + a.width, m -= r.height + a.height), {
    x: Math.round((l - u) / g * e.width / n),
    y: Math.round((c - d) / m * e.height / n)
  };
}
function Wc(i, t, e) {
  let n, s;
  if (t === void 0 || e === void 0) {
    const o = i && ms(i);
    if (!o)
      t = i.clientWidth, e = i.clientHeight;
    else {
      const r = o.getBoundingClientRect(), a = on(o), l = xe(a, "border", "width"), c = xe(a, "padding");
      t = r.width - c.width - l.width, e = r.height - c.height - l.height, n = Ki(a.maxWidth, o, "clientWidth"), s = Ki(a.maxHeight, o, "clientHeight");
    }
  }
  return {
    width: t,
    height: e,
    maxWidth: n || Zi,
    maxHeight: s || Zi
  };
}
const Kt = (i) => Math.round(i * 10) / 10;
function jc(i, t, e, n) {
  const s = on(i), o = xe(s, "margin"), r = Ki(s.maxWidth, i, "clientWidth") || Zi, a = Ki(s.maxHeight, i, "clientHeight") || Zi, l = Wc(i, t, e);
  let { width: c, height: h } = l;
  if (s.boxSizing === "content-box") {
    const d = xe(s, "border", "width"), g = xe(s, "padding");
    c -= g.width + d.width, h -= g.height + d.height;
  }
  return c = Math.max(0, c - o.width), h = Math.max(0, n ? c / n : h - o.height), c = Kt(Math.min(c, r, l.maxWidth)), h = Kt(Math.min(h, a, l.maxHeight)), c && !h && (h = Kt(c / 2)), (t !== void 0 || e !== void 0) && n && l.height && h > l.height && (h = l.height, c = Kt(Math.floor(h * n))), {
    width: c,
    height: h
  };
}
function so(i, t, e) {
  const n = t || 1, s = Kt(i.height * n), o = Kt(i.width * n);
  i.height = Kt(i.height), i.width = Kt(i.width);
  const r = i.canvas;
  return r.style && (e || !r.style.height && !r.style.width) && (r.style.height = `${i.height}px`, r.style.width = `${i.width}px`), i.currentDevicePixelRatio !== n || r.height !== s || r.width !== o ? (i.currentDevicePixelRatio = n, r.height = s, r.width = o, i.ctx.setTransform(n, 0, 0, n, 0, 0), !0) : !1;
}
const Yc = (function() {
  let i = !1;
  try {
    const t = {
      get passive() {
        return i = !0, !1;
      }
    };
    ps() && (window.addEventListener("test", null, t), window.removeEventListener("test", null, t));
  } catch {
  }
  return i;
})();
function oo(i, t) {
  const e = Nc(i, t), n = e && e.match(/^(\d+)(\.\d+)?px$/);
  return n ? +n[1] : void 0;
}
function ge(i, t, e, n) {
  return {
    x: i.x + e * (t.x - i.x),
    y: i.y + e * (t.y - i.y)
  };
}
function Uc(i, t, e, n) {
  return {
    x: i.x + e * (t.x - i.x),
    y: n === "middle" ? e < 0.5 ? i.y : t.y : n === "after" ? e < 1 ? i.y : t.y : e > 0 ? t.y : i.y
  };
}
function Xc(i, t, e, n) {
  const s = {
    x: i.cp2x,
    y: i.cp2y
  }, o = {
    x: t.cp1x,
    y: t.cp1y
  }, r = ge(i, s, e), a = ge(s, o, e), l = ge(o, t, e), c = ge(r, a, e), h = ge(a, l, e);
  return ge(c, h, e);
}
const $c = function(i, t) {
  return {
    x(e) {
      return i + i + t - e;
    },
    setWidth(e) {
      t = e;
    },
    textAlign(e) {
      return e === "center" ? e : e === "right" ? "left" : "right";
    },
    xPlus(e, n) {
      return e - n;
    },
    leftForLtr(e, n) {
      return e - n;
    }
  };
}, qc = function() {
  return {
    x(i) {
      return i;
    },
    setWidth(i) {
    },
    textAlign(i) {
      return i;
    },
    xPlus(i, t) {
      return i + t;
    },
    leftForLtr(i, t) {
      return i;
    }
  };
};
function Ce(i, t, e) {
  return i ? $c(t, e) : qc();
}
function $r(i, t) {
  let e, n;
  (t === "ltr" || t === "rtl") && (e = i.canvas.style, n = [
    e.getPropertyValue("direction"),
    e.getPropertyPriority("direction")
  ], e.setProperty("direction", t, "important"), i.prevTextDirection = n);
}
function qr(i, t) {
  t !== void 0 && (delete i.prevTextDirection, i.canvas.style.setProperty("direction", t[0], t[1]));
}
function Zr(i) {
  return i === "angle" ? {
    between: ri,
    compare: Zl,
    normalize: lt
  } : {
    between: jt,
    compare: (t, e) => t - e,
    normalize: (t) => t
  };
}
function ro({ start: i, end: t, count: e, loop: n, style: s }) {
  return {
    start: i % e,
    end: t % e,
    loop: n && (t - i + 1) % e === 0,
    style: s
  };
}
function Zc(i, t, e) {
  const { property: n, start: s, end: o } = e, { between: r, normalize: a } = Zr(n), l = t.length;
  let { start: c, end: h, loop: u } = i, d, g;
  if (u) {
    for (c += l, h += l, d = 0, g = l; d < g && r(a(t[c % l][n]), s, o); ++d)
      c--, h--;
    c %= l, h %= l;
  }
  return h < c && (h += l), {
    start: c,
    end: h,
    loop: u,
    style: i.style
  };
}
function Gr(i, t, e) {
  if (!e)
    return [
      i
    ];
  const { property: n, start: s, end: o } = e, r = t.length, { compare: a, between: l, normalize: c } = Zr(n), { start: h, end: u, loop: d, style: g } = Zc(i, t, e), m = [];
  let b = !1, _ = null, y, v, w;
  const S = () => l(s, w, y) && a(s, w) !== 0, k = () => a(o, y) === 0 || l(o, w, y), P = () => b || S(), C = () => !b || k();
  for (let D = h, E = h; D <= u; ++D)
    v = t[D % r], !v.skip && (y = c(v[n]), y !== w && (b = l(y, s, o), _ === null && P() && (_ = a(y, s) === 0 ? D : E), _ !== null && C() && (m.push(ro({
      start: _,
      end: D,
      loop: d,
      count: r,
      style: g
    })), _ = null), E = D, w = y));
  return _ !== null && m.push(ro({
    start: _,
    end: u,
    loop: d,
    count: r,
    style: g
  })), m;
}
function Kr(i, t) {
  const e = [], n = i.segments;
  for (let s = 0; s < n.length; s++) {
    const o = Gr(n[s], i.points, t);
    o.length && e.push(...o);
  }
  return e;
}
function Gc(i, t, e, n) {
  let s = 0, o = t - 1;
  if (e && !n)
    for (; s < t && !i[s].skip; )
      s++;
  for (; s < t && i[s].skip; )
    s++;
  for (s %= t, e && (o += s); o > s && i[o % t].skip; )
    o--;
  return o %= t, {
    start: s,
    end: o
  };
}
function Kc(i, t, e, n) {
  const s = i.length, o = [];
  let r = t, a = i[t], l;
  for (l = t + 1; l <= e; ++l) {
    const c = i[l % s];
    c.skip || c.stop ? a.skip || (n = !1, o.push({
      start: t % s,
      end: (l - 1) % s,
      loop: n
    }), t = r = c.stop ? l : null) : (r = l, a.skip && (t = l)), a = c;
  }
  return r !== null && o.push({
    start: t % s,
    end: r % s,
    loop: n
  }), o;
}
function Jc(i, t) {
  const e = i.points, n = i.options.spanGaps, s = e.length;
  if (!s)
    return [];
  const o = !!i._loop, { start: r, end: a } = Gc(e, s, o, n);
  if (n === !0)
    return ao(i, [
      {
        start: r,
        end: a,
        loop: o
      }
    ], e, t);
  const l = a < r ? a + s : a, c = !!i._fullLoop && r === 0 && a === s - 1;
  return ao(i, Kc(e, r, l, c), e, t);
}
function ao(i, t, e, n) {
  return !n || !n.setContext || !e ? t : Qc(i, t, e, n);
}
function Qc(i, t, e, n) {
  const s = i._chart.getContext(), o = lo(i.options), { _datasetIndex: r, options: { spanGaps: a } } = i, l = e.length, c = [];
  let h = o, u = t[0].start, d = u;
  function g(m, b, _, y) {
    const v = a ? -1 : 1;
    if (m !== b) {
      for (m += l; e[m % l].skip; )
        m -= v;
      for (; e[b % l].skip; )
        b += v;
      m % l !== b % l && (c.push({
        start: m % l,
        end: b % l,
        loop: _,
        style: y
      }), h = y, u = b % l);
    }
  }
  for (const m of t) {
    u = a ? u : m.start;
    let b = e[u % l], _;
    for (d = u + 1; d <= m.end; d++) {
      const y = e[d % l];
      _ = lo(n.setContext(se(s, {
        type: "segment",
        p0: b,
        p1: y,
        p0DataIndex: (d - 1) % l,
        p1DataIndex: d % l,
        datasetIndex: r
      }))), th(_, h) && g(u, d - 1, m.loop, h), b = y, h = _;
    }
    u < d - 1 && g(u, d - 1, m.loop, h);
  }
  return c;
}
function lo(i) {
  return {
    backgroundColor: i.backgroundColor,
    borderCapStyle: i.borderCapStyle,
    borderDash: i.borderDash,
    borderDashOffset: i.borderDashOffset,
    borderJoinStyle: i.borderJoinStyle,
    borderWidth: i.borderWidth,
    borderColor: i.borderColor
  };
}
function th(i, t) {
  if (!t)
    return !1;
  const e = [], n = function(s, o) {
    return hs(o) ? (e.includes(o) || e.push(o), e.indexOf(o)) : o;
  };
  return JSON.stringify(i, n) !== JSON.stringify(t, n);
}
function Oi(i, t, e) {
  return i.options.clip ? i[e] : t[e];
}
function eh(i, t) {
  const { xScale: e, yScale: n } = i;
  return e && n ? {
    left: Oi(e, t, "left"),
    right: Oi(e, t, "right"),
    top: Oi(n, t, "top"),
    bottom: Oi(n, t, "bottom")
  } : t;
}
function Jr(i, t) {
  const e = t._clip;
  if (e.disabled)
    return !1;
  const n = eh(t, i.chartArea);
  return {
    left: e.left === !1 ? 0 : n.left - (e.left === !0 ? 0 : e.left),
    right: e.right === !1 ? i.width : n.right + (e.right === !0 ? 0 : e.right),
    top: e.top === !1 ? 0 : n.top - (e.top === !0 ? 0 : e.top),
    bottom: e.bottom === !1 ? i.height : n.bottom + (e.bottom === !0 ? 0 : e.bottom)
  };
}
/*!
 * Chart.js v4.5.1
 * https://www.chartjs.org
 * (c) 2025 Chart.js Contributors
 * Released under the MIT License
 */
class ih {
  constructor() {
    this._request = null, this._charts = /* @__PURE__ */ new Map(), this._running = !1, this._lastDate = void 0;
  }
  _notify(t, e, n, s) {
    const o = e.listeners[s], r = e.duration;
    o.forEach((a) => a({
      chart: t,
      initial: e.initial,
      numSteps: r,
      currentStep: Math.min(n - e.start, r)
    }));
  }
  _refresh() {
    this._request || (this._running = !0, this._request = Lr.call(window, () => {
      this._update(), this._request = null, this._running && this._refresh();
    }));
  }
  _update(t = Date.now()) {
    let e = 0;
    this._charts.forEach((n, s) => {
      if (!n.running || !n.items.length)
        return;
      const o = n.items;
      let r = o.length - 1, a = !1, l;
      for (; r >= 0; --r)
        l = o[r], l._active ? (l._total > n.duration && (n.duration = l._total), l.tick(t), a = !0) : (o[r] = o[o.length - 1], o.pop());
      a && (s.draw(), this._notify(s, n, t, "progress")), o.length || (n.running = !1, this._notify(s, n, t, "complete"), n.initial = !1), e += o.length;
    }), this._lastDate = t, e === 0 && (this._running = !1);
  }
  _getAnims(t) {
    const e = this._charts;
    let n = e.get(t);
    return n || (n = {
      running: !1,
      initial: !0,
      items: [],
      listeners: {
        complete: [],
        progress: []
      }
    }, e.set(t, n)), n;
  }
  listen(t, e, n) {
    this._getAnims(t).listeners[e].push(n);
  }
  add(t, e) {
    !e || !e.length || this._getAnims(t).items.push(...e);
  }
  has(t) {
    return this._getAnims(t).items.length > 0;
  }
  start(t) {
    const e = this._charts.get(t);
    e && (e.running = !0, e.start = Date.now(), e.duration = e.items.reduce((n, s) => Math.max(n, s._duration), 0), this._refresh());
  }
  running(t) {
    if (!this._running)
      return !1;
    const e = this._charts.get(t);
    return !(!e || !e.running || !e.items.length);
  }
  stop(t) {
    const e = this._charts.get(t);
    if (!e || !e.items.length)
      return;
    const n = e.items;
    let s = n.length - 1;
    for (; s >= 0; --s)
      n[s].cancel();
    e.items = [], this._notify(t, e, Date.now(), "complete");
  }
  remove(t) {
    return this._charts.delete(t);
  }
}
var Vt = /* @__PURE__ */ new ih();
const co = "transparent", nh = {
  boolean(i, t, e) {
    return e > 0.5 ? t : i;
  },
  color(i, t, e) {
    const n = Qs(i || co), s = n.valid && Qs(t || co);
    return s && s.valid ? s.mix(n, e).hexString() : t;
  },
  number(i, t, e) {
    return i + (t - i) * e;
  }
};
class sh {
  constructor(t, e, n, s) {
    const o = e[n];
    s = Xe([
      t.to,
      s,
      o,
      t.from
    ]);
    const r = Xe([
      t.from,
      o,
      s
    ]);
    this._active = !0, this._fn = t.fn || nh[t.type || typeof r], this._easing = Je[t.easing] || Je.linear, this._start = Math.floor(Date.now() + (t.delay || 0)), this._duration = this._total = Math.floor(t.duration), this._loop = !!t.loop, this._target = e, this._prop = n, this._from = r, this._to = s, this._promises = void 0;
  }
  active() {
    return this._active;
  }
  update(t, e, n) {
    if (this._active) {
      this._notify(!1);
      const s = this._target[this._prop], o = n - this._start, r = this._duration - o;
      this._start = n, this._duration = Math.floor(Math.max(r, t.duration)), this._total += o, this._loop = !!t.loop, this._to = Xe([
        t.to,
        e,
        s,
        t.from
      ]), this._from = Xe([
        t.from,
        s,
        e
      ]);
    }
  }
  cancel() {
    this._active && (this.tick(Date.now()), this._active = !1, this._notify(!1));
  }
  tick(t) {
    const e = t - this._start, n = this._duration, s = this._prop, o = this._from, r = this._loop, a = this._to;
    let l;
    if (this._active = o !== a && (r || e < n), !this._active) {
      this._target[s] = a, this._notify(!0);
      return;
    }
    if (e < 0) {
      this._target[s] = o;
      return;
    }
    l = e / n % 2, l = r && l > 1 ? 2 - l : l, l = this._easing(Math.min(1, Math.max(0, l))), this._target[s] = this._fn(o, a, l);
  }
  wait() {
    const t = this._promises || (this._promises = []);
    return new Promise((e, n) => {
      t.push({
        res: e,
        rej: n
      });
    });
  }
  _notify(t) {
    const e = t ? "res" : "rej", n = this._promises || [];
    for (let s = 0; s < n.length; s++)
      n[s][e]();
  }
}
class Qr {
  constructor(t, e) {
    this._chart = t, this._properties = /* @__PURE__ */ new Map(), this.configure(e);
  }
  configure(t) {
    if (!H(t))
      return;
    const e = Object.keys(Z.animation), n = this._properties;
    Object.getOwnPropertyNames(t).forEach((s) => {
      const o = t[s];
      if (!H(o))
        return;
      const r = {};
      for (const a of e)
        r[a] = o[a];
      (q(o.properties) && o.properties || [
        s
      ]).forEach((a) => {
        (a === s || !n.has(a)) && n.set(a, r);
      });
    });
  }
  _animateOptions(t, e) {
    const n = e.options, s = rh(t, n);
    if (!s)
      return [];
    const o = this._createAnimations(s, n);
    return n.$shared && oh(t.options.$animations, n).then(() => {
      t.options = n;
    }, () => {
    }), o;
  }
  _createAnimations(t, e) {
    const n = this._properties, s = [], o = t.$animations || (t.$animations = {}), r = Object.keys(e), a = Date.now();
    let l;
    for (l = r.length - 1; l >= 0; --l) {
      const c = r[l];
      if (c.charAt(0) === "$")
        continue;
      if (c === "options") {
        s.push(...this._animateOptions(t, e));
        continue;
      }
      const h = e[c];
      let u = o[c];
      const d = n.get(c);
      if (u)
        if (d && u.active()) {
          u.update(d, h, a);
          continue;
        } else
          u.cancel();
      if (!d || !d.duration) {
        t[c] = h;
        continue;
      }
      o[c] = u = new sh(d, t, c, h), s.push(u);
    }
    return s;
  }
  update(t, e) {
    if (this._properties.size === 0) {
      Object.assign(t, e);
      return;
    }
    const n = this._createAnimations(t, e);
    if (n.length)
      return Vt.add(this._chart, n), !0;
  }
}
function oh(i, t) {
  const e = [], n = Object.keys(t);
  for (let s = 0; s < n.length; s++) {
    const o = i[n[s]];
    o && o.active() && e.push(o.wait());
  }
  return Promise.all(e);
}
function rh(i, t) {
  if (!t)
    return;
  let e = i.options;
  if (!e) {
    i.options = t;
    return;
  }
  return e.$shared && (i.options = e = Object.assign({}, e, {
    $shared: !1,
    $animations: {}
  })), e;
}
function ho(i, t) {
  const e = i && i.options || {}, n = e.reverse, s = e.min === void 0 ? t : 0, o = e.max === void 0 ? t : 0;
  return {
    start: n ? o : s,
    end: n ? s : o
  };
}
function ah(i, t, e) {
  if (e === !1)
    return !1;
  const n = ho(i, e), s = ho(t, e);
  return {
    top: s.end,
    right: n.end,
    bottom: s.start,
    left: n.start
  };
}
function lh(i) {
  let t, e, n, s;
  return H(i) ? (t = i.top, e = i.right, n = i.bottom, s = i.left) : t = e = n = s = i, {
    top: t,
    right: e,
    bottom: n,
    left: s,
    disabled: i === !1
  };
}
function ta(i, t) {
  const e = [], n = i._getSortedDatasetMetas(t);
  let s, o;
  for (s = 0, o = n.length; s < o; ++s)
    e.push(n[s].index);
  return e;
}
function uo(i, t, e, n = {}) {
  const s = i.keys, o = n.mode === "single";
  let r, a, l, c;
  if (t === null)
    return;
  let h = !1;
  for (r = 0, a = s.length; r < a; ++r) {
    if (l = +s[r], l === e) {
      if (h = !0, n.all)
        continue;
      break;
    }
    c = i.values[l], K(c) && (o || t === 0 || St(t) === St(c)) && (t += c);
  }
  return !h && !n.all ? 0 : t;
}
function ch(i, t) {
  const { iScale: e, vScale: n } = t, s = e.axis === "x" ? "x" : "y", o = n.axis === "x" ? "x" : "y", r = Object.keys(i), a = new Array(r.length);
  let l, c, h;
  for (l = 0, c = r.length; l < c; ++l)
    h = r[l], a[l] = {
      [s]: h,
      [o]: i[h]
    };
  return a;
}
function Mn(i, t) {
  const e = i && i.options.stacked;
  return e || e === void 0 && t.stack !== void 0;
}
function hh(i, t, e) {
  return `${i.id}.${t.id}.${e.stack || e.type}`;
}
function uh(i) {
  const { min: t, max: e, minDefined: n, maxDefined: s } = i.getUserBounds();
  return {
    min: n ? t : Number.NEGATIVE_INFINITY,
    max: s ? e : Number.POSITIVE_INFINITY
  };
}
function dh(i, t, e) {
  const n = i[t] || (i[t] = {});
  return n[e] || (n[e] = {});
}
function fo(i, t, e, n) {
  for (const s of t.getMatchingVisibleMetas(n).reverse()) {
    const o = i[s.index];
    if (e && o > 0 || !e && o < 0)
      return s.index;
  }
  return null;
}
function go(i, t) {
  const { chart: e, _cachedMeta: n } = i, s = e._stacks || (e._stacks = {}), { iScale: o, vScale: r, index: a } = n, l = o.axis, c = r.axis, h = hh(o, r, n), u = t.length;
  let d;
  for (let g = 0; g < u; ++g) {
    const m = t[g], { [l]: b, [c]: _ } = m, y = m._stacks || (m._stacks = {});
    d = y[c] = dh(s, h, b), d[a] = _, d._top = fo(d, r, !0, n.type), d._bottom = fo(d, r, !1, n.type);
    const v = d._visualValues || (d._visualValues = {});
    v[a] = _;
  }
}
function Sn(i, t) {
  const e = i.scales;
  return Object.keys(e).filter((n) => e[n].axis === t).shift();
}
function fh(i, t) {
  return se(i, {
    active: !1,
    dataset: void 0,
    datasetIndex: t,
    index: t,
    mode: "default",
    type: "dataset"
  });
}
function gh(i, t, e) {
  return se(i, {
    active: !1,
    dataIndex: t,
    parsed: void 0,
    raw: void 0,
    element: e,
    index: t,
    mode: "default",
    type: "data"
  });
}
function Ve(i, t) {
  const e = i.controller.index, n = i.vScale && i.vScale.axis;
  if (n) {
    t = t || i._parsed;
    for (const s of t) {
      const o = s._stacks;
      if (!o || o[n] === void 0 || o[n][e] === void 0)
        return;
      delete o[n][e], o[n]._visualValues !== void 0 && o[n]._visualValues[e] !== void 0 && delete o[n]._visualValues[e];
    }
  }
}
const kn = (i) => i === "reset" || i === "none", po = (i, t) => t ? i : Object.assign({}, i), ph = (i, t, e) => i && !t.hidden && t._stacked && {
  keys: ta(e, !0),
  values: null
};
class Pt {
  constructor(t, e) {
    this.chart = t, this._ctx = t.ctx, this.index = e, this._cachedDataOpts = {}, this._cachedMeta = this.getMeta(), this._type = this._cachedMeta.type, this.options = void 0, this._parsing = !1, this._data = void 0, this._objectData = void 0, this._sharedOptions = void 0, this._drawStart = void 0, this._drawCount = void 0, this.enableOptionSharing = !1, this.supportsDecimation = !1, this.$context = void 0, this._syncList = [], this.datasetElementType = new.target.datasetElementType, this.dataElementType = new.target.dataElementType, this.initialize();
  }
  initialize() {
    const t = this._cachedMeta;
    this.configure(), this.linkScales(), t._stacked = Mn(t.vScale, t), this.addElements(), this.options.fill && !this.chart.isPluginEnabled("filler") && console.warn("Tried to use the 'fill' option without the 'Filler' plugin enabled. Please import and register the 'Filler' plugin and make sure it is not disabled in the options");
  }
  updateIndex(t) {
    this.index !== t && Ve(this._cachedMeta), this.index = t;
  }
  linkScales() {
    const t = this.chart, e = this._cachedMeta, n = this.getDataset(), s = (u, d, g, m) => u === "x" ? d : u === "r" ? m : g, o = e.xAxisID = I(n.xAxisID, Sn(t, "x")), r = e.yAxisID = I(n.yAxisID, Sn(t, "y")), a = e.rAxisID = I(n.rAxisID, Sn(t, "r")), l = e.indexAxis, c = e.iAxisID = s(l, o, r, a), h = e.vAxisID = s(l, r, o, a);
    e.xScale = this.getScaleForId(o), e.yScale = this.getScaleForId(r), e.rScale = this.getScaleForId(a), e.iScale = this.getScaleForId(c), e.vScale = this.getScaleForId(h);
  }
  getDataset() {
    return this.chart.data.datasets[this.index];
  }
  getMeta() {
    return this.chart.getDatasetMeta(this.index);
  }
  getScaleForId(t) {
    return this.chart.scales[t];
  }
  _getOtherScale(t) {
    const e = this._cachedMeta;
    return t === e.iScale ? e.vScale : e.iScale;
  }
  reset() {
    this._update("reset");
  }
  _destroy() {
    const t = this._cachedMeta;
    this._data && Gs(this._data, this), t._stacked && Ve(t);
  }
  _dataCheck() {
    const t = this.getDataset(), e = t.data || (t.data = []), n = this._data;
    if (H(e)) {
      const s = this._cachedMeta;
      this._data = ch(e, s);
    } else if (n !== e) {
      if (n) {
        Gs(n, this);
        const s = this._cachedMeta;
        Ve(s), s._parsed = [];
      }
      e && Object.isExtensible(e) && Ql(e, this), this._syncList = [], this._data = e;
    }
  }
  addElements() {
    const t = this._cachedMeta;
    this._dataCheck(), this.datasetElementType && (t.dataset = new this.datasetElementType());
  }
  buildOrUpdateElements(t) {
    const e = this._cachedMeta, n = this.getDataset();
    let s = !1;
    this._dataCheck();
    const o = e._stacked;
    e._stacked = Mn(e.vScale, e), e.stack !== n.stack && (s = !0, Ve(e), e.stack = n.stack), this._resyncElements(t), (s || o !== e._stacked) && (go(this, e._parsed), e._stacked = Mn(e.vScale, e));
  }
  configure() {
    const t = this.chart.config, e = t.datasetScopeKeys(this._type), n = t.getOptionScopes(this.getDataset(), e, !0);
    this.options = t.createResolver(n, this.getContext()), this._parsing = this.options.parsing, this._cachedDataOpts = {};
  }
  parse(t, e) {
    const { _cachedMeta: n, _data: s } = this, { iScale: o, _stacked: r } = n, a = o.axis;
    let l = t === 0 && e === s.length ? !0 : n._sorted, c = t > 0 && n._parsed[t - 1], h, u, d;
    if (this._parsing === !1)
      n._parsed = s, n._sorted = !0, d = s;
    else {
      q(s[t]) ? d = this.parseArrayData(n, s, t, e) : H(s[t]) ? d = this.parseObjectData(n, s, t, e) : d = this.parsePrimitiveData(n, s, t, e);
      const g = () => u[a] === null || c && u[a] < c[a];
      for (h = 0; h < e; ++h)
        n._parsed[h + t] = u = d[h], l && (g() && (l = !1), c = u);
      n._sorted = l;
    }
    r && go(this, d);
  }
  parsePrimitiveData(t, e, n, s) {
    const { iScale: o, vScale: r } = t, a = o.axis, l = r.axis, c = o.getLabels(), h = o === r, u = new Array(s);
    let d, g, m;
    for (d = 0, g = s; d < g; ++d)
      m = d + n, u[d] = {
        [a]: h || o.parse(c[m], m),
        [l]: r.parse(e[m], m)
      };
    return u;
  }
  parseArrayData(t, e, n, s) {
    const { xScale: o, yScale: r } = t, a = new Array(s);
    let l, c, h, u;
    for (l = 0, c = s; l < c; ++l)
      h = l + n, u = e[h], a[l] = {
        x: o.parse(u[0], h),
        y: r.parse(u[1], h)
      };
    return a;
  }
  parseObjectData(t, e, n, s) {
    const { xScale: o, yScale: r } = t, { xAxisKey: a = "x", yAxisKey: l = "y" } = this._parsing, c = new Array(s);
    let h, u, d, g;
    for (h = 0, u = s; h < u; ++h)
      d = h + n, g = e[d], c[h] = {
        x: o.parse(ee(g, a), d),
        y: r.parse(ee(g, l), d)
      };
    return c;
  }
  getParsed(t) {
    return this._cachedMeta._parsed[t];
  }
  getDataElement(t) {
    return this._cachedMeta.data[t];
  }
  applyStack(t, e, n) {
    const s = this.chart, o = this._cachedMeta, r = e[t.axis], a = {
      keys: ta(s, !0),
      values: e._stacks[t.axis]._visualValues
    };
    return uo(a, r, o.index, {
      mode: n
    });
  }
  updateRangeFromParsed(t, e, n, s) {
    const o = n[e.axis];
    let r = o === null ? NaN : o;
    const a = s && n._stacks[e.axis];
    s && a && (s.values = a, r = uo(s, o, this._cachedMeta.index)), t.min = Math.min(t.min, r), t.max = Math.max(t.max, r);
  }
  getMinMax(t, e) {
    const n = this._cachedMeta, s = n._parsed, o = n._sorted && t === n.iScale, r = s.length, a = this._getOtherScale(t), l = ph(e, n, this.chart), c = {
      min: Number.POSITIVE_INFINITY,
      max: Number.NEGATIVE_INFINITY
    }, { min: h, max: u } = uh(a);
    let d, g;
    function m() {
      g = s[d];
      const b = g[a.axis];
      return !K(g[t.axis]) || h > b || u < b;
    }
    for (d = 0; d < r && !(!m() && (this.updateRangeFromParsed(c, t, g, l), o)); ++d)
      ;
    if (o) {
      for (d = r - 1; d >= 0; --d)
        if (!m()) {
          this.updateRangeFromParsed(c, t, g, l);
          break;
        }
    }
    return c;
  }
  getAllParsedValues(t) {
    const e = this._cachedMeta._parsed, n = [];
    let s, o, r;
    for (s = 0, o = e.length; s < o; ++s)
      r = e[s][t.axis], K(r) && n.push(r);
    return n;
  }
  getMaxOverflow() {
    return !1;
  }
  getLabelAndValue(t) {
    const e = this._cachedMeta, n = e.iScale, s = e.vScale, o = this.getParsed(t);
    return {
      label: n ? "" + n.getLabelForValue(o[n.axis]) : "",
      value: s ? "" + s.getLabelForValue(o[s.axis]) : ""
    };
  }
  _update(t) {
    const e = this._cachedMeta;
    this.update(t || "default"), e._clip = lh(I(this.options.clip, ah(e.xScale, e.yScale, this.getMaxOverflow())));
  }
  update(t) {
  }
  draw() {
    const t = this._ctx, e = this.chart, n = this._cachedMeta, s = n.data || [], o = e.chartArea, r = [], a = this._drawStart || 0, l = this._drawCount || s.length - a, c = this.options.drawActiveElementsOnTop;
    let h;
    for (n.dataset && n.dataset.draw(t, o, a, l), h = a; h < a + l; ++h) {
      const u = s[h];
      u.hidden || (u.active && c ? r.push(u) : u.draw(t, o));
    }
    for (h = 0; h < r.length; ++h)
      r[h].draw(t, o);
  }
  getStyle(t, e) {
    const n = e ? "active" : "default";
    return t === void 0 && this._cachedMeta.dataset ? this.resolveDatasetElementOptions(n) : this.resolveDataElementOptions(t || 0, n);
  }
  getContext(t, e, n) {
    const s = this.getDataset();
    let o;
    if (t >= 0 && t < this._cachedMeta.data.length) {
      const r = this._cachedMeta.data[t];
      o = r.$context || (r.$context = gh(this.getContext(), t, r)), o.parsed = this.getParsed(t), o.raw = s.data[t], o.index = o.dataIndex = t;
    } else
      o = this.$context || (this.$context = fh(this.chart.getContext(), this.index)), o.dataset = s, o.index = o.datasetIndex = this.index;
    return o.active = !!e, o.mode = n, o;
  }
  resolveDatasetElementOptions(t) {
    return this._resolveElementOptions(this.datasetElementType.id, t);
  }
  resolveDataElementOptions(t, e) {
    return this._resolveElementOptions(this.dataElementType.id, e, t);
  }
  _resolveElementOptions(t, e = "default", n) {
    const s = e === "active", o = this._cachedDataOpts, r = t + "-" + e, a = o[r], l = this.enableOptionSharing && oi(n);
    if (a)
      return po(a, l);
    const c = this.chart.config, h = c.datasetElementScopeKeys(this._type, t), u = s ? [
      `${t}Hover`,
      "hover",
      t,
      ""
    ] : [
      t,
      ""
    ], d = c.getOptionScopes(this.getDataset(), h), g = Object.keys(Z.elements[t]), m = () => this.getContext(n, s, e), b = c.resolveNamedOptions(d, g, m, u);
    return b.$shared && (b.$shared = l, o[r] = Object.freeze(po(b, l))), b;
  }
  _resolveAnimations(t, e, n) {
    const s = this.chart, o = this._cachedDataOpts, r = `animation-${e}`, a = o[r];
    if (a)
      return a;
    let l;
    if (s.options.animation !== !1) {
      const h = this.chart.config, u = h.datasetAnimationScopeKeys(this._type, e), d = h.getOptionScopes(this.getDataset(), u);
      l = h.createResolver(d, this.getContext(t, n, e));
    }
    const c = new Qr(s, l && l.animations);
    return l && l._cacheable && (o[r] = Object.freeze(c)), c;
  }
  getSharedOptions(t) {
    if (t.$shared)
      return this._sharedOptions || (this._sharedOptions = Object.assign({}, t));
  }
  includeOptions(t, e) {
    return !e || kn(t) || this.chart._animationsDisabled;
  }
  _getSharedOptions(t, e) {
    const n = this.resolveDataElementOptions(t, e), s = this._sharedOptions, o = this.getSharedOptions(n), r = this.includeOptions(e, o) || o !== s;
    return this.updateSharedOptions(o, e, n), {
      sharedOptions: o,
      includeOptions: r
    };
  }
  updateElement(t, e, n, s) {
    kn(s) ? Object.assign(t, n) : this._resolveAnimations(e, s).update(t, n);
  }
  updateSharedOptions(t, e, n) {
    t && !kn(e) && this._resolveAnimations(void 0, e).update(t, n);
  }
  _setStyle(t, e, n, s) {
    t.active = s;
    const o = this.getStyle(e, s);
    this._resolveAnimations(e, n, s).update(t, {
      options: !s && this.getSharedOptions(o) || o
    });
  }
  removeHoverStyle(t, e, n) {
    this._setStyle(t, n, "active", !1);
  }
  setHoverStyle(t, e, n) {
    this._setStyle(t, n, "active", !0);
  }
  _removeDatasetHoverStyle() {
    const t = this._cachedMeta.dataset;
    t && this._setStyle(t, void 0, "active", !1);
  }
  _setDatasetHoverStyle() {
    const t = this._cachedMeta.dataset;
    t && this._setStyle(t, void 0, "active", !0);
  }
  _resyncElements(t) {
    const e = this._data, n = this._cachedMeta.data;
    for (const [a, l, c] of this._syncList)
      this[a](l, c);
    this._syncList = [];
    const s = n.length, o = e.length, r = Math.min(o, s);
    r && this.parse(0, r), o > s ? this._insertElements(s, o - s, t) : o < s && this._removeElements(o, s - o);
  }
  _insertElements(t, e, n = !0) {
    const s = this._cachedMeta, o = s.data, r = t + e;
    let a;
    const l = (c) => {
      for (c.length += e, a = c.length - 1; a >= r; a--)
        c[a] = c[a - e];
    };
    for (l(o), a = t; a < r; ++a)
      o[a] = new this.dataElementType();
    this._parsing && l(s._parsed), this.parse(t, e), n && this.updateElements(o, t, e, "reset");
  }
  updateElements(t, e, n, s) {
  }
  _removeElements(t, e) {
    const n = this._cachedMeta;
    if (this._parsing) {
      const s = n._parsed.splice(t, e);
      n._stacked && Ve(n, s);
    }
    n.data.splice(t, e);
  }
  _sync(t) {
    if (this._parsing)
      this._syncList.push(t);
    else {
      const [e, n, s] = t;
      this[e](n, s);
    }
    this.chart._dataChanges.push([
      this.index,
      ...t
    ]);
  }
  _onDataPush() {
    const t = arguments.length;
    this._sync([
      "_insertElements",
      this.getDataset().data.length - t,
      t
    ]);
  }
  _onDataPop() {
    this._sync([
      "_removeElements",
      this._cachedMeta.data.length - 1,
      1
    ]);
  }
  _onDataShift() {
    this._sync([
      "_removeElements",
      0,
      1
    ]);
  }
  _onDataSplice(t, e) {
    e && this._sync([
      "_removeElements",
      t,
      e
    ]);
    const n = arguments.length - 2;
    n && this._sync([
      "_insertElements",
      t,
      n
    ]);
  }
  _onDataUnshift() {
    this._sync([
      "_insertElements",
      0,
      arguments.length
    ]);
  }
}
O(Pt, "defaults", {}), O(Pt, "datasetElementType", null), O(Pt, "dataElementType", null);
function mh(i, t) {
  if (!i._cache.$bar) {
    const e = i.getMatchingVisibleMetas(t);
    let n = [];
    for (let s = 0, o = e.length; s < o; s++)
      n = n.concat(e[s].controller.getAllParsedValues(i));
    i._cache.$bar = Rr(n.sort((s, o) => s - o));
  }
  return i._cache.$bar;
}
function bh(i) {
  const t = i.iScale, e = mh(t, i.type);
  let n = t._length, s, o, r, a;
  const l = () => {
    r === 32767 || r === -32768 || (oi(a) && (n = Math.min(n, Math.abs(r - a) || n)), a = r);
  };
  for (s = 0, o = e.length; s < o; ++s)
    r = t.getPixelForValue(e[s]), l();
  for (a = void 0, s = 0, o = t.ticks.length; s < o; ++s)
    r = t.getPixelForTick(s), l();
  return n;
}
function xh(i, t, e, n) {
  const s = e.barThickness;
  let o, r;
  return V(s) ? (o = t.min * e.categoryPercentage, r = e.barPercentage) : (o = s * n, r = 1), {
    chunk: o / n,
    ratio: r,
    start: t.pixels[i] - o / 2
  };
}
function _h(i, t, e, n) {
  const s = t.pixels, o = s[i];
  let r = i > 0 ? s[i - 1] : null, a = i < s.length - 1 ? s[i + 1] : null;
  const l = e.categoryPercentage;
  r === null && (r = o - (a === null ? t.end - t.start : a - o)), a === null && (a = o + o - r);
  const c = o - (o - Math.min(r, a)) / 2 * l;
  return {
    chunk: Math.abs(a - r) / 2 * l / n,
    ratio: e.barPercentage,
    start: c
  };
}
function yh(i, t, e, n) {
  const s = e.parse(i[0], n), o = e.parse(i[1], n), r = Math.min(s, o), a = Math.max(s, o);
  let l = r, c = a;
  Math.abs(r) > Math.abs(a) && (l = a, c = r), t[e.axis] = c, t._custom = {
    barStart: l,
    barEnd: c,
    start: s,
    end: o,
    min: r,
    max: a
  };
}
function ea(i, t, e, n) {
  return q(i) ? yh(i, t, e, n) : t[e.axis] = e.parse(i, n), t;
}
function mo(i, t, e, n) {
  const s = i.iScale, o = i.vScale, r = s.getLabels(), a = s === o, l = [];
  let c, h, u, d;
  for (c = e, h = e + n; c < h; ++c)
    d = t[c], u = {}, u[s.axis] = a || s.parse(r[c], c), l.push(ea(d, u, o, c));
  return l;
}
function wn(i) {
  return i && i.barStart !== void 0 && i.barEnd !== void 0;
}
function vh(i, t, e) {
  return i !== 0 ? St(i) : (t.isHorizontal() ? 1 : -1) * (t.min >= e ? 1 : -1);
}
function Mh(i) {
  let t, e, n, s, o;
  return i.horizontal ? (t = i.base > i.x, e = "left", n = "right") : (t = i.base < i.y, e = "bottom", n = "top"), t ? (s = "end", o = "start") : (s = "start", o = "end"), {
    start: e,
    end: n,
    reverse: t,
    top: s,
    bottom: o
  };
}
function Sh(i, t, e, n) {
  let s = t.borderSkipped;
  const o = {};
  if (!s) {
    i.borderSkipped = o;
    return;
  }
  if (s === !0) {
    i.borderSkipped = {
      top: !0,
      right: !0,
      bottom: !0,
      left: !0
    };
    return;
  }
  const { start: r, end: a, reverse: l, top: c, bottom: h } = Mh(i);
  s === "middle" && e && (i.enableBorderRadius = !0, (e._top || 0) === n ? s = c : (e._bottom || 0) === n ? s = h : (o[bo(h, r, a, l)] = !0, s = c)), o[bo(s, r, a, l)] = !0, i.borderSkipped = o;
}
function bo(i, t, e, n) {
  return n ? (i = kh(i, t, e), i = xo(i, e, t)) : i = xo(i, t, e), i;
}
function kh(i, t, e) {
  return i === t ? e : i === e ? t : i;
}
function xo(i, t, e) {
  return i === "start" ? t : i === "end" ? e : i;
}
function wh(i, { inflateAmount: t }, e) {
  i.inflateAmount = t === "auto" ? e === 1 ? 0.33 : 0 : t;
}
class Ni extends Pt {
  parsePrimitiveData(t, e, n, s) {
    return mo(t, e, n, s);
  }
  parseArrayData(t, e, n, s) {
    return mo(t, e, n, s);
  }
  parseObjectData(t, e, n, s) {
    const { iScale: o, vScale: r } = t, { xAxisKey: a = "x", yAxisKey: l = "y" } = this._parsing, c = o.axis === "x" ? a : l, h = r.axis === "x" ? a : l, u = [];
    let d, g, m, b;
    for (d = n, g = n + s; d < g; ++d)
      b = e[d], m = {}, m[o.axis] = o.parse(ee(b, c), d), u.push(ea(ee(b, h), m, r, d));
    return u;
  }
  updateRangeFromParsed(t, e, n, s) {
    super.updateRangeFromParsed(t, e, n, s);
    const o = n._custom;
    o && e === this._cachedMeta.vScale && (t.min = Math.min(t.min, o.min), t.max = Math.max(t.max, o.max));
  }
  getMaxOverflow() {
    return 0;
  }
  getLabelAndValue(t) {
    const e = this._cachedMeta, { iScale: n, vScale: s } = e, o = this.getParsed(t), r = o._custom, a = wn(r) ? "[" + r.start + ", " + r.end + "]" : "" + s.getLabelForValue(o[s.axis]);
    return {
      label: "" + n.getLabelForValue(o[n.axis]),
      value: a
    };
  }
  initialize() {
    this.enableOptionSharing = !0, super.initialize();
    const t = this._cachedMeta;
    t.stack = this.getDataset().stack;
  }
  update(t) {
    const e = this._cachedMeta;
    this.updateElements(e.data, 0, e.data.length, t);
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", { index: r, _cachedMeta: { vScale: a } } = this, l = a.getBasePixel(), c = a.isHorizontal(), h = this._getRuler(), { sharedOptions: u, includeOptions: d } = this._getSharedOptions(e, s);
    for (let g = e; g < e + n; g++) {
      const m = this.getParsed(g), b = o || V(m[a.axis]) ? {
        base: l,
        head: l
      } : this._calculateBarValuePixels(g), _ = this._calculateBarIndexPixels(g, h), y = (m._stacks || {})[a.axis], v = {
        horizontal: c,
        base: b.base,
        enableBorderRadius: !y || wn(m._custom) || r === y._top || r === y._bottom,
        x: c ? b.head : _.center,
        y: c ? _.center : b.head,
        height: c ? _.size : Math.abs(b.size),
        width: c ? Math.abs(b.size) : _.size
      };
      d && (v.options = u || this.resolveDataElementOptions(g, t[g].active ? "active" : s));
      const w = v.options || t[g].options;
      Sh(v, w, y, r), wh(v, w, h.ratio), this.updateElement(t[g], g, v, s);
    }
  }
  _getStacks(t, e) {
    const { iScale: n } = this._cachedMeta, s = n.getMatchingVisibleMetas(this._type).filter((h) => h.controller.options.grouped), o = n.options.stacked, r = [], a = this._cachedMeta.controller.getParsed(e), l = a && a[n.axis], c = (h) => {
      const u = h._parsed.find((g) => g[n.axis] === l), d = u && u[h.vScale.axis];
      if (V(d) || isNaN(d))
        return !0;
    };
    for (const h of s)
      if (!(e !== void 0 && c(h)) && ((o === !1 || r.indexOf(h.stack) === -1 || o === void 0 && h.stack === void 0) && r.push(h.stack), h.index === t))
        break;
    return r.length || r.push(void 0), r;
  }
  _getStackCount(t) {
    return this._getStacks(void 0, t).length;
  }
  _getAxisCount() {
    return this._getAxis().length;
  }
  getFirstScaleIdForIndexAxis() {
    const t = this.chart.scales, e = this.chart.options.indexAxis;
    return Object.keys(t).filter((n) => t[n].axis === e).shift();
  }
  _getAxis() {
    const t = {}, e = this.getFirstScaleIdForIndexAxis();
    for (const n of this.chart.data.datasets)
      t[I(this.chart.options.indexAxis === "x" ? n.xAxisID : n.yAxisID, e)] = !0;
    return Object.keys(t);
  }
  _getStackIndex(t, e, n) {
    const s = this._getStacks(t, n), o = e !== void 0 ? s.indexOf(e) : -1;
    return o === -1 ? s.length - 1 : o;
  }
  _getRuler() {
    const t = this.options, e = this._cachedMeta, n = e.iScale, s = [];
    let o, r;
    for (o = 0, r = e.data.length; o < r; ++o)
      s.push(n.getPixelForValue(this.getParsed(o)[n.axis], o));
    const a = t.barThickness;
    return {
      min: a || bh(e),
      pixels: s,
      start: n._startPixel,
      end: n._endPixel,
      stackCount: this._getStackCount(),
      scale: n,
      grouped: t.grouped,
      ratio: a ? 1 : t.categoryPercentage * t.barPercentage
    };
  }
  _calculateBarValuePixels(t) {
    const { _cachedMeta: { vScale: e, _stacked: n, index: s }, options: { base: o, minBarLength: r } } = this, a = o || 0, l = this.getParsed(t), c = l._custom, h = wn(c);
    let u = l[e.axis], d = 0, g = n ? this.applyStack(e, l, n) : u, m, b;
    g !== u && (d = g - u, g = u), h && (u = c.barStart, g = c.barEnd - c.barStart, u !== 0 && St(u) !== St(c.barEnd) && (d = 0), d += u);
    const _ = !V(o) && !h ? o : d;
    let y = e.getPixelForValue(_);
    if (this.chart.getDataVisibility(t) ? m = e.getPixelForValue(d + g) : m = y, b = m - y, Math.abs(b) < r) {
      b = vh(b, e, a) * r, u === a && (y -= b / 2);
      const v = e.getPixelForDecimal(0), w = e.getPixelForDecimal(1), S = Math.min(v, w), k = Math.max(v, w);
      y = Math.max(Math.min(y, k), S), m = y + b, n && !h && (l._stacks[e.axis]._visualValues[s] = e.getValueForPixel(m) - e.getValueForPixel(y));
    }
    if (y === e.getPixelForValue(a)) {
      const v = St(b) * e.getLineWidthForValue(a) / 2;
      y += v, b -= v;
    }
    return {
      size: b,
      base: y,
      head: m,
      center: m + b / 2
    };
  }
  _calculateBarIndexPixels(t, e) {
    const n = e.scale, s = this.options, o = s.skipNull, r = I(s.maxBarThickness, 1 / 0);
    let a, l;
    const c = this._getAxisCount();
    if (e.grouped) {
      const h = o ? this._getStackCount(t) : e.stackCount, u = s.barThickness === "flex" ? _h(t, e, s, h * c) : xh(t, e, s, h * c), d = this.chart.options.indexAxis === "x" ? this.getDataset().xAxisID : this.getDataset().yAxisID, g = this._getAxis().indexOf(I(d, this.getFirstScaleIdForIndexAxis())), m = this._getStackIndex(this.index, this._cachedMeta.stack, o ? t : void 0) + g;
      a = u.start + u.chunk * m + u.chunk / 2, l = Math.min(r, u.chunk * u.ratio);
    } else
      a = n.getPixelForValue(this.getParsed(t)[n.axis], t), l = Math.min(r, e.min * e.ratio);
    return {
      base: a - l / 2,
      head: a + l / 2,
      center: a,
      size: l
    };
  }
  draw() {
    const t = this._cachedMeta, e = t.vScale, n = t.data, s = n.length;
    let o = 0;
    for (; o < s; ++o)
      this.getParsed(o)[e.axis] !== null && !n[o].hidden && n[o].draw(this._ctx);
  }
}
O(Ni, "id", "bar"), O(Ni, "defaults", {
  datasetElementType: !1,
  dataElementType: "bar",
  categoryPercentage: 0.8,
  barPercentage: 0.9,
  grouped: !0,
  animations: {
    numbers: {
      type: "number",
      properties: [
        "x",
        "y",
        "base",
        "width",
        "height"
      ]
    }
  }
}), O(Ni, "overrides", {
  scales: {
    _index_: {
      type: "category",
      offset: !0,
      grid: {
        offset: !0
      }
    },
    _value_: {
      type: "linear",
      beginAtZero: !0
    }
  }
});
class Bi extends Pt {
  initialize() {
    this.enableOptionSharing = !0, super.initialize();
  }
  parsePrimitiveData(t, e, n, s) {
    const o = super.parsePrimitiveData(t, e, n, s);
    for (let r = 0; r < o.length; r++)
      o[r]._custom = this.resolveDataElementOptions(r + n).radius;
    return o;
  }
  parseArrayData(t, e, n, s) {
    const o = super.parseArrayData(t, e, n, s);
    for (let r = 0; r < o.length; r++) {
      const a = e[n + r];
      o[r]._custom = I(a[2], this.resolveDataElementOptions(r + n).radius);
    }
    return o;
  }
  parseObjectData(t, e, n, s) {
    const o = super.parseObjectData(t, e, n, s);
    for (let r = 0; r < o.length; r++) {
      const a = e[n + r];
      o[r]._custom = I(a && a.r && +a.r, this.resolveDataElementOptions(r + n).radius);
    }
    return o;
  }
  getMaxOverflow() {
    const t = this._cachedMeta.data;
    let e = 0;
    for (let n = t.length - 1; n >= 0; --n)
      e = Math.max(e, t[n].size(this.resolveDataElementOptions(n)) / 2);
    return e > 0 && e;
  }
  getLabelAndValue(t) {
    const e = this._cachedMeta, n = this.chart.data.labels || [], { xScale: s, yScale: o } = e, r = this.getParsed(t), a = s.getLabelForValue(r.x), l = o.getLabelForValue(r.y), c = r._custom;
    return {
      label: n[t] || "",
      value: "(" + a + ", " + l + (c ? ", " + c : "") + ")"
    };
  }
  update(t) {
    const e = this._cachedMeta.data;
    this.updateElements(e, 0, e.length, t);
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", { iScale: r, vScale: a } = this._cachedMeta, { sharedOptions: l, includeOptions: c } = this._getSharedOptions(e, s), h = r.axis, u = a.axis;
    for (let d = e; d < e + n; d++) {
      const g = t[d], m = !o && this.getParsed(d), b = {}, _ = b[h] = o ? r.getPixelForDecimal(0.5) : r.getPixelForValue(m[h]), y = b[u] = o ? a.getBasePixel() : a.getPixelForValue(m[u]);
      b.skip = isNaN(_) || isNaN(y), c && (b.options = l || this.resolveDataElementOptions(d, g.active ? "active" : s), o && (b.options.radius = 0)), this.updateElement(g, d, b, s);
    }
  }
  resolveDataElementOptions(t, e) {
    const n = this.getParsed(t);
    let s = super.resolveDataElementOptions(t, e);
    s.$shared && (s = Object.assign({}, s, {
      $shared: !1
    }));
    const o = s.radius;
    return e !== "active" && (s.radius = 0), s.radius += I(n && n._custom, o), s;
  }
}
O(Bi, "id", "bubble"), O(Bi, "defaults", {
  datasetElementType: !1,
  dataElementType: "point",
  animations: {
    numbers: {
      type: "number",
      properties: [
        "x",
        "y",
        "borderWidth",
        "radius"
      ]
    }
  }
}), O(Bi, "overrides", {
  scales: {
    x: {
      type: "linear"
    },
    y: {
      type: "linear"
    }
  }
});
function Th(i, t, e) {
  let n = 1, s = 1, o = 0, r = 0;
  if (t < X) {
    const a = i, l = a + t, c = Math.cos(a), h = Math.sin(a), u = Math.cos(l), d = Math.sin(l), g = (w, S, k) => ri(w, a, l, !0) ? 1 : Math.max(S, S * e, k, k * e), m = (w, S, k) => ri(w, a, l, !0) ? -1 : Math.min(S, S * e, k, k * e), b = g(0, c, u), _ = g(J, h, d), y = m(W, c, u), v = m(W + J, h, d);
    n = (b - y) / 2, s = (_ - v) / 2, o = -(b + y) / 2, r = -(_ + v) / 2;
  }
  return {
    ratioX: n,
    ratioY: s,
    offsetX: o,
    offsetY: r
  };
}
class pe extends Pt {
  constructor(t, e) {
    super(t, e), this.enableOptionSharing = !0, this.innerRadius = void 0, this.outerRadius = void 0, this.offsetX = void 0, this.offsetY = void 0;
  }
  linkScales() {
  }
  parse(t, e) {
    const n = this.getDataset().data, s = this._cachedMeta;
    if (this._parsing === !1)
      s._parsed = n;
    else {
      let o = (l) => +n[l];
      if (H(n[t])) {
        const { key: l = "value" } = this._parsing;
        o = (c) => +ee(n[c], l);
      }
      let r, a;
      for (r = t, a = t + e; r < a; ++r)
        s._parsed[r] = o(r);
    }
  }
  _getRotation() {
    return Tt(this.options.rotation - 90);
  }
  _getCircumference() {
    return Tt(this.options.circumference);
  }
  _getRotationExtents() {
    let t = X, e = -X;
    for (let n = 0; n < this.chart.data.datasets.length; ++n)
      if (this.chart.isDatasetVisible(n) && this.chart.getDatasetMeta(n).type === this._type) {
        const s = this.chart.getDatasetMeta(n).controller, o = s._getRotation(), r = s._getCircumference();
        t = Math.min(t, o), e = Math.max(e, o + r);
      }
    return {
      rotation: t,
      circumference: e - t
    };
  }
  update(t) {
    const e = this.chart, { chartArea: n } = e, s = this._cachedMeta, o = s.data, r = this.getMaxBorderWidth() + this.getMaxOffset(o) + this.options.spacing, a = Math.max((Math.min(n.width, n.height) - r) / 2, 0), l = Math.min(Nl(this.options.cutout, a), 1), c = this._getRingWeight(this.index), { circumference: h, rotation: u } = this._getRotationExtents(), { ratioX: d, ratioY: g, offsetX: m, offsetY: b } = Th(u, h, l), _ = (n.width - r) / d, y = (n.height - r) / g, v = Math.max(Math.min(_, y) / 2, 0), w = Cr(this.options.radius, v), S = Math.max(w * l, 0), k = (w - S) / this._getVisibleDatasetWeightTotal();
    this.offsetX = m * w, this.offsetY = b * w, s.total = this.calculateTotal(), this.outerRadius = w - k * this._getRingWeightOffset(this.index), this.innerRadius = Math.max(this.outerRadius - k * c, 0), this.updateElements(o, 0, o.length, t);
  }
  _circumference(t, e) {
    const n = this.options, s = this._cachedMeta, o = this._getCircumference();
    return e && n.animation.animateRotate || !this.chart.getDataVisibility(t) || s._parsed[t] === null || s.data[t].hidden ? 0 : this.calculateCircumference(s._parsed[t] * o / X);
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", r = this.chart, a = r.chartArea, c = r.options.animation, h = (a.left + a.right) / 2, u = (a.top + a.bottom) / 2, d = o && c.animateScale, g = d ? 0 : this.innerRadius, m = d ? 0 : this.outerRadius, { sharedOptions: b, includeOptions: _ } = this._getSharedOptions(e, s);
    let y = this._getRotation(), v;
    for (v = 0; v < e; ++v)
      y += this._circumference(v, o);
    for (v = e; v < e + n; ++v) {
      const w = this._circumference(v, o), S = t[v], k = {
        x: h + this.offsetX,
        y: u + this.offsetY,
        startAngle: y,
        endAngle: y + w,
        circumference: w,
        outerRadius: m,
        innerRadius: g
      };
      _ && (k.options = b || this.resolveDataElementOptions(v, S.active ? "active" : s)), y += w, this.updateElement(S, v, k, s);
    }
  }
  calculateTotal() {
    const t = this._cachedMeta, e = t.data;
    let n = 0, s;
    for (s = 0; s < e.length; s++) {
      const o = t._parsed[s];
      o !== null && !isNaN(o) && this.chart.getDataVisibility(s) && !e[s].hidden && (n += Math.abs(o));
    }
    return n;
  }
  calculateCircumference(t) {
    const e = this._cachedMeta.total;
    return e > 0 && !isNaN(t) ? X * (Math.abs(t) / e) : 0;
  }
  getLabelAndValue(t) {
    const e = this._cachedMeta, n = this.chart, s = n.data.labels || [], o = fi(e._parsed[t], n.options.locale);
    return {
      label: s[t] || "",
      value: o
    };
  }
  getMaxBorderWidth(t) {
    let e = 0;
    const n = this.chart;
    let s, o, r, a, l;
    if (!t) {
      for (s = 0, o = n.data.datasets.length; s < o; ++s)
        if (n.isDatasetVisible(s)) {
          r = n.getDatasetMeta(s), t = r.data, a = r.controller;
          break;
        }
    }
    if (!t)
      return 0;
    for (s = 0, o = t.length; s < o; ++s)
      l = a.resolveDataElementOptions(s), l.borderAlign !== "inner" && (e = Math.max(e, l.borderWidth || 0, l.hoverBorderWidth || 0));
    return e;
  }
  getMaxOffset(t) {
    let e = 0;
    for (let n = 0, s = t.length; n < s; ++n) {
      const o = this.resolveDataElementOptions(n);
      e = Math.max(e, o.offset || 0, o.hoverOffset || 0);
    }
    return e;
  }
  _getRingWeightOffset(t) {
    let e = 0;
    for (let n = 0; n < t; ++n)
      this.chart.isDatasetVisible(n) && (e += this._getRingWeight(n));
    return e;
  }
  _getRingWeight(t) {
    return Math.max(I(this.chart.data.datasets[t].weight, 1), 0);
  }
  _getVisibleDatasetWeightTotal() {
    return this._getRingWeightOffset(this.chart.data.datasets.length) || 1;
  }
}
O(pe, "id", "doughnut"), O(pe, "defaults", {
  datasetElementType: !1,
  dataElementType: "arc",
  animation: {
    animateRotate: !0,
    animateScale: !1
  },
  animations: {
    numbers: {
      type: "number",
      properties: [
        "circumference",
        "endAngle",
        "innerRadius",
        "outerRadius",
        "startAngle",
        "x",
        "y",
        "offset",
        "borderWidth",
        "spacing"
      ]
    }
  },
  cutout: "50%",
  rotation: 0,
  circumference: 360,
  radius: "100%",
  spacing: 0,
  indexAxis: "r"
}), O(pe, "descriptors", {
  _scriptable: (t) => t !== "spacing",
  _indexable: (t) => t !== "spacing" && !t.startsWith("borderDash") && !t.startsWith("hoverBorderDash")
}), O(pe, "overrides", {
  aspectRatio: 1,
  plugins: {
    legend: {
      labels: {
        generateLabels(t) {
          const e = t.data, { labels: { pointStyle: n, textAlign: s, color: o, useBorderRadius: r, borderRadius: a } } = t.legend.options;
          return e.labels.length && e.datasets.length ? e.labels.map((l, c) => {
            const u = t.getDatasetMeta(0).controller.getStyle(c);
            return {
              text: l,
              fillStyle: u.backgroundColor,
              fontColor: o,
              hidden: !t.getDataVisibility(c),
              lineDash: u.borderDash,
              lineDashOffset: u.borderDashOffset,
              lineJoin: u.borderJoinStyle,
              lineWidth: u.borderWidth,
              strokeStyle: u.borderColor,
              textAlign: s,
              pointStyle: n,
              borderRadius: r && (a || u.borderRadius),
              index: c
            };
          }) : [];
        }
      },
      onClick(t, e, n) {
        n.chart.toggleDataVisibility(e.index), n.chart.update();
      }
    }
  }
});
class Vi extends Pt {
  initialize() {
    this.enableOptionSharing = !0, this.supportsDecimation = !0, super.initialize();
  }
  update(t) {
    const e = this._cachedMeta, { dataset: n, data: s = [], _dataset: o } = e, r = this.chart._animationsDisabled;
    let { start: a, count: l } = Fr(e, s, r);
    this._drawStart = a, this._drawCount = l, zr(e) && (a = 0, l = s.length), n._chart = this.chart, n._datasetIndex = this.index, n._decimated = !!o._decimated, n.points = s;
    const c = this.resolveDatasetElementOptions(t);
    this.options.showLine || (c.borderWidth = 0), c.segment = this.options.segment, this.updateElement(n, void 0, {
      animated: !r,
      options: c
    }, t), this.updateElements(s, a, l, t);
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", { iScale: r, vScale: a, _stacked: l, _dataset: c } = this._cachedMeta, { sharedOptions: h, includeOptions: u } = this._getSharedOptions(e, s), d = r.axis, g = a.axis, { spanGaps: m, segment: b } = this.options, _ = Oe(m) ? m : Number.POSITIVE_INFINITY, y = this.chart._animationsDisabled || o || s === "none", v = e + n, w = t.length;
    let S = e > 0 && this.getParsed(e - 1);
    for (let k = 0; k < w; ++k) {
      const P = t[k], C = y ? P : {};
      if (k < e || k >= v) {
        C.skip = !0;
        continue;
      }
      const D = this.getParsed(k), E = V(D[g]), F = C[d] = r.getPixelForValue(D[d], k), R = C[g] = o || E ? a.getBasePixel() : a.getPixelForValue(l ? this.applyStack(a, D, l) : D[g], k);
      C.skip = isNaN(F) || isNaN(R) || E, C.stop = k > 0 && Math.abs(D[d] - S[d]) > _, b && (C.parsed = D, C.raw = c.data[k]), u && (C.options = h || this.resolveDataElementOptions(k, P.active ? "active" : s)), y || this.updateElement(P, k, C, s), S = D;
    }
  }
  getMaxOverflow() {
    const t = this._cachedMeta, e = t.dataset, n = e.options && e.options.borderWidth || 0, s = t.data || [];
    if (!s.length)
      return n;
    const o = s[0].size(this.resolveDataElementOptions(0)), r = s[s.length - 1].size(this.resolveDataElementOptions(s.length - 1));
    return Math.max(n, o, r) / 2;
  }
  draw() {
    const t = this._cachedMeta;
    t.dataset.updateControlPoints(this.chart.chartArea, t.iScale.axis), super.draw();
  }
}
O(Vi, "id", "line"), O(Vi, "defaults", {
  datasetElementType: "line",
  dataElementType: "point",
  showLine: !0,
  spanGaps: !1
}), O(Vi, "overrides", {
  scales: {
    _index_: {
      type: "category"
    },
    _value_: {
      type: "linear"
    }
  }
});
class ti extends Pt {
  constructor(t, e) {
    super(t, e), this.innerRadius = void 0, this.outerRadius = void 0;
  }
  getLabelAndValue(t) {
    const e = this._cachedMeta, n = this.chart, s = n.data.labels || [], o = fi(e._parsed[t].r, n.options.locale);
    return {
      label: s[t] || "",
      value: o
    };
  }
  parseObjectData(t, e, n, s) {
    return Ur.bind(this)(t, e, n, s);
  }
  update(t) {
    const e = this._cachedMeta.data;
    this._updateRadius(), this.updateElements(e, 0, e.length, t);
  }
  getMinMax() {
    const t = this._cachedMeta, e = {
      min: Number.POSITIVE_INFINITY,
      max: Number.NEGATIVE_INFINITY
    };
    return t.data.forEach((n, s) => {
      const o = this.getParsed(s).r;
      !isNaN(o) && this.chart.getDataVisibility(s) && (o < e.min && (e.min = o), o > e.max && (e.max = o));
    }), e;
  }
  _updateRadius() {
    const t = this.chart, e = t.chartArea, n = t.options, s = Math.min(e.right - e.left, e.bottom - e.top), o = Math.max(s / 2, 0), r = Math.max(n.cutoutPercentage ? o / 100 * n.cutoutPercentage : 1, 0), a = (o - r) / t.getVisibleDatasetCount();
    this.outerRadius = o - a * this.index, this.innerRadius = this.outerRadius - a;
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", r = this.chart, l = r.options.animation, c = this._cachedMeta.rScale, h = c.xCenter, u = c.yCenter, d = c.getIndexAngle(0) - 0.5 * W;
    let g = d, m;
    const b = 360 / this.countVisibleElements();
    for (m = 0; m < e; ++m)
      g += this._computeAngle(m, s, b);
    for (m = e; m < e + n; m++) {
      const _ = t[m];
      let y = g, v = g + this._computeAngle(m, s, b), w = r.getDataVisibility(m) ? c.getDistanceFromCenterForValue(this.getParsed(m).r) : 0;
      g = v, o && (l.animateScale && (w = 0), l.animateRotate && (y = v = d));
      const S = {
        x: h,
        y: u,
        innerRadius: 0,
        outerRadius: w,
        startAngle: y,
        endAngle: v,
        options: this.resolveDataElementOptions(m, _.active ? "active" : s)
      };
      this.updateElement(_, m, S, s);
    }
  }
  countVisibleElements() {
    const t = this._cachedMeta;
    let e = 0;
    return t.data.forEach((n, s) => {
      !isNaN(this.getParsed(s).r) && this.chart.getDataVisibility(s) && e++;
    }), e;
  }
  _computeAngle(t, e, n) {
    return this.chart.getDataVisibility(t) ? Tt(this.resolveDataElementOptions(t, e).angle || n) : 0;
  }
}
O(ti, "id", "polarArea"), O(ti, "defaults", {
  dataElementType: "arc",
  animation: {
    animateRotate: !0,
    animateScale: !0
  },
  animations: {
    numbers: {
      type: "number",
      properties: [
        "x",
        "y",
        "startAngle",
        "endAngle",
        "innerRadius",
        "outerRadius"
      ]
    }
  },
  indexAxis: "r",
  startAngle: 0
}), O(ti, "overrides", {
  aspectRatio: 1,
  plugins: {
    legend: {
      labels: {
        generateLabels(t) {
          const e = t.data;
          if (e.labels.length && e.datasets.length) {
            const { labels: { pointStyle: n, color: s } } = t.legend.options;
            return e.labels.map((o, r) => {
              const l = t.getDatasetMeta(0).controller.getStyle(r);
              return {
                text: o,
                fillStyle: l.backgroundColor,
                strokeStyle: l.borderColor,
                fontColor: s,
                lineWidth: l.borderWidth,
                pointStyle: n,
                hidden: !t.getDataVisibility(r),
                index: r
              };
            });
          }
          return [];
        }
      },
      onClick(t, e, n) {
        n.chart.toggleDataVisibility(e.index), n.chart.update();
      }
    }
  },
  scales: {
    r: {
      type: "radialLinear",
      angleLines: {
        display: !1
      },
      beginAtZero: !0,
      grid: {
        circular: !0
      },
      pointLabels: {
        display: !1
      },
      startAngle: 0
    }
  }
});
class Bn extends pe {
}
O(Bn, "id", "pie"), O(Bn, "defaults", {
  cutout: 0,
  rotation: 0,
  circumference: 360,
  radius: "100%"
});
class Hi extends Pt {
  getLabelAndValue(t) {
    const e = this._cachedMeta.vScale, n = this.getParsed(t);
    return {
      label: e.getLabels()[t],
      value: "" + e.getLabelForValue(n[e.axis])
    };
  }
  parseObjectData(t, e, n, s) {
    return Ur.bind(this)(t, e, n, s);
  }
  update(t) {
    const e = this._cachedMeta, n = e.dataset, s = e.data || [], o = e.iScale.getLabels();
    if (n.points = s, t !== "resize") {
      const r = this.resolveDatasetElementOptions(t);
      this.options.showLine || (r.borderWidth = 0);
      const a = {
        _loop: !0,
        _fullLoop: o.length === s.length,
        options: r
      };
      this.updateElement(n, void 0, a, t);
    }
    this.updateElements(s, 0, s.length, t);
  }
  updateElements(t, e, n, s) {
    const o = this._cachedMeta.rScale, r = s === "reset";
    for (let a = e; a < e + n; a++) {
      const l = t[a], c = this.resolveDataElementOptions(a, l.active ? "active" : s), h = o.getPointPositionForValue(a, this.getParsed(a).r), u = r ? o.xCenter : h.x, d = r ? o.yCenter : h.y, g = {
        x: u,
        y: d,
        angle: h.angle,
        skip: isNaN(u) || isNaN(d),
        options: c
      };
      this.updateElement(l, a, g, s);
    }
  }
}
O(Hi, "id", "radar"), O(Hi, "defaults", {
  datasetElementType: "line",
  dataElementType: "point",
  indexAxis: "r",
  showLine: !0,
  elements: {
    line: {
      fill: "start"
    }
  }
}), O(Hi, "overrides", {
  aspectRatio: 1,
  scales: {
    r: {
      type: "radialLinear"
    }
  }
});
class Wi extends Pt {
  getLabelAndValue(t) {
    const e = this._cachedMeta, n = this.chart.data.labels || [], { xScale: s, yScale: o } = e, r = this.getParsed(t), a = s.getLabelForValue(r.x), l = o.getLabelForValue(r.y);
    return {
      label: n[t] || "",
      value: "(" + a + ", " + l + ")"
    };
  }
  update(t) {
    const e = this._cachedMeta, { data: n = [] } = e, s = this.chart._animationsDisabled;
    let { start: o, count: r } = Fr(e, n, s);
    if (this._drawStart = o, this._drawCount = r, zr(e) && (o = 0, r = n.length), this.options.showLine) {
      this.datasetElementType || this.addElements();
      const { dataset: a, _dataset: l } = e;
      a._chart = this.chart, a._datasetIndex = this.index, a._decimated = !!l._decimated, a.points = n;
      const c = this.resolveDatasetElementOptions(t);
      c.segment = this.options.segment, this.updateElement(a, void 0, {
        animated: !s,
        options: c
      }, t);
    } else this.datasetElementType && (delete e.dataset, this.datasetElementType = !1);
    this.updateElements(n, o, r, t);
  }
  addElements() {
    const { showLine: t } = this.options;
    !this.datasetElementType && t && (this.datasetElementType = this.chart.registry.getElement("line")), super.addElements();
  }
  updateElements(t, e, n, s) {
    const o = s === "reset", { iScale: r, vScale: a, _stacked: l, _dataset: c } = this._cachedMeta, h = this.resolveDataElementOptions(e, s), u = this.getSharedOptions(h), d = this.includeOptions(s, u), g = r.axis, m = a.axis, { spanGaps: b, segment: _ } = this.options, y = Oe(b) ? b : Number.POSITIVE_INFINITY, v = this.chart._animationsDisabled || o || s === "none";
    let w = e > 0 && this.getParsed(e - 1);
    for (let S = e; S < e + n; ++S) {
      const k = t[S], P = this.getParsed(S), C = v ? k : {}, D = V(P[m]), E = C[g] = r.getPixelForValue(P[g], S), F = C[m] = o || D ? a.getBasePixel() : a.getPixelForValue(l ? this.applyStack(a, P, l) : P[m], S);
      C.skip = isNaN(E) || isNaN(F) || D, C.stop = S > 0 && Math.abs(P[g] - w[g]) > y, _ && (C.parsed = P, C.raw = c.data[S]), d && (C.options = u || this.resolveDataElementOptions(S, k.active ? "active" : s)), v || this.updateElement(k, S, C, s), w = P;
    }
    this.updateSharedOptions(u, s, h);
  }
  getMaxOverflow() {
    const t = this._cachedMeta, e = t.data || [];
    if (!this.options.showLine) {
      let a = 0;
      for (let l = e.length - 1; l >= 0; --l)
        a = Math.max(a, e[l].size(this.resolveDataElementOptions(l)) / 2);
      return a > 0 && a;
    }
    const n = t.dataset, s = n.options && n.options.borderWidth || 0;
    if (!e.length)
      return s;
    const o = e[0].size(this.resolveDataElementOptions(0)), r = e[e.length - 1].size(this.resolveDataElementOptions(e.length - 1));
    return Math.max(s, o, r) / 2;
  }
}
O(Wi, "id", "scatter"), O(Wi, "defaults", {
  datasetElementType: !1,
  dataElementType: "point",
  showLine: !1,
  fill: !1
}), O(Wi, "overrides", {
  interaction: {
    mode: "point"
  },
  scales: {
    x: {
      type: "linear"
    },
    y: {
      type: "linear"
    }
  }
});
var Ph = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  BarController: Ni,
  BubbleController: Bi,
  DoughnutController: pe,
  LineController: Vi,
  PieController: Bn,
  PolarAreaController: ti,
  RadarController: Hi,
  ScatterController: Wi
});
function de() {
  throw new Error("This method is not implemented: Check that a complete date adapter is provided.");
}
class bs {
  constructor(t) {
    O(this, "options");
    this.options = t || {};
  }
  /**
  * Override default date adapter methods.
  * Accepts type parameter to define options type.
  * @example
  * Chart._adapters._date.override<{myAdapterOption: string}>({
  *   init() {
  *     console.log(this.options.myAdapterOption);
  *   }
  * })
  */
  static override(t) {
    Object.assign(bs.prototype, t);
  }
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  init() {
  }
  formats() {
    return de();
  }
  parse() {
    return de();
  }
  format() {
    return de();
  }
  add() {
    return de();
  }
  diff() {
    return de();
  }
  startOf() {
    return de();
  }
  endOf() {
    return de();
  }
}
var Ch = {
  _date: bs
};
function Oh(i, t, e, n) {
  const { controller: s, data: o, _sorted: r } = i, a = s._cachedMeta.iScale, l = i.dataset && i.dataset.options ? i.dataset.options.spanGaps : null;
  if (a && t === a.axis && t !== "r" && r && o.length) {
    const c = a._reversePixels ? Kl : Yt;
    if (n) {
      if (s._sharedOptions) {
        const h = o[0], u = typeof h.getRange == "function" && h.getRange(t);
        if (u) {
          const d = c(o, t, e - u), g = c(o, t, e + u);
          return {
            lo: d.lo,
            hi: g.hi
          };
        }
      }
    } else {
      const h = c(o, t, e);
      if (l) {
        const { vScale: u } = s._cachedMeta, { _parsed: d } = i, g = d.slice(0, h.lo + 1).reverse().findIndex((b) => !V(b[u.axis]));
        h.lo -= Math.max(0, g);
        const m = d.slice(h.hi).findIndex((b) => !V(b[u.axis]));
        h.hi += Math.max(0, m);
      }
      return h;
    }
  }
  return {
    lo: 0,
    hi: o.length - 1
  };
}
function rn(i, t, e, n, s) {
  const o = i.getSortedVisibleDatasetMetas(), r = e[t];
  for (let a = 0, l = o.length; a < l; ++a) {
    const { index: c, data: h } = o[a], { lo: u, hi: d } = Oh(o[a], t, r, s);
    for (let g = u; g <= d; ++g) {
      const m = h[g];
      m.skip || n(m, c, g);
    }
  }
}
function Dh(i) {
  const t = i.indexOf("x") !== -1, e = i.indexOf("y") !== -1;
  return function(n, s) {
    const o = t ? Math.abs(n.x - s.x) : 0, r = e ? Math.abs(n.y - s.y) : 0;
    return Math.sqrt(Math.pow(o, 2) + Math.pow(r, 2));
  };
}
function Tn(i, t, e, n, s) {
  const o = [];
  return !s && !i.isPointInArea(t) || rn(i, e, t, function(a, l, c) {
    !s && !It(a, i.chartArea, 0) || a.inRange(t.x, t.y, n) && o.push({
      element: a,
      datasetIndex: l,
      index: c
    });
  }, !0), o;
}
function Ah(i, t, e, n) {
  let s = [];
  function o(r, a, l) {
    const { startAngle: c, endAngle: h } = r.getProps([
      "startAngle",
      "endAngle"
    ], n), { angle: u } = Ar(r, {
      x: t.x,
      y: t.y
    });
    ri(u, c, h) && s.push({
      element: r,
      datasetIndex: a,
      index: l
    });
  }
  return rn(i, e, t, o), s;
}
function Eh(i, t, e, n, s, o) {
  let r = [];
  const a = Dh(e);
  let l = Number.POSITIVE_INFINITY;
  function c(h, u, d) {
    const g = h.inRange(t.x, t.y, s);
    if (n && !g)
      return;
    const m = h.getCenterPoint(s);
    if (!(!!o || i.isPointInArea(m)) && !g)
      return;
    const _ = a(t, m);
    _ < l ? (r = [
      {
        element: h,
        datasetIndex: u,
        index: d
      }
    ], l = _) : _ === l && r.push({
      element: h,
      datasetIndex: u,
      index: d
    });
  }
  return rn(i, e, t, c), r;
}
function Pn(i, t, e, n, s, o) {
  return !o && !i.isPointInArea(t) ? [] : e === "r" && !n ? Ah(i, t, e, s) : Eh(i, t, e, n, s, o);
}
function _o(i, t, e, n, s) {
  const o = [], r = e === "x" ? "inXRange" : "inYRange";
  let a = !1;
  return rn(i, e, t, (l, c, h) => {
    l[r] && l[r](t[e], s) && (o.push({
      element: l,
      datasetIndex: c,
      index: h
    }), a = a || l.inRange(t.x, t.y, s));
  }), n && !a ? [] : o;
}
var Rh = {
  modes: {
    index(i, t, e, n) {
      const s = Lt(t, i), o = e.axis || "x", r = e.includeInvisible || !1, a = e.intersect ? Tn(i, s, o, n, r) : Pn(i, s, o, !1, n, r), l = [];
      return a.length ? (i.getSortedVisibleDatasetMetas().forEach((c) => {
        const h = a[0].index, u = c.data[h];
        u && !u.skip && l.push({
          element: u,
          datasetIndex: c.index,
          index: h
        });
      }), l) : [];
    },
    dataset(i, t, e, n) {
      const s = Lt(t, i), o = e.axis || "xy", r = e.includeInvisible || !1;
      let a = e.intersect ? Tn(i, s, o, n, r) : Pn(i, s, o, !1, n, r);
      if (a.length > 0) {
        const l = a[0].datasetIndex, c = i.getDatasetMeta(l).data;
        a = [];
        for (let h = 0; h < c.length; ++h)
          a.push({
            element: c[h],
            datasetIndex: l,
            index: h
          });
      }
      return a;
    },
    point(i, t, e, n) {
      const s = Lt(t, i), o = e.axis || "xy", r = e.includeInvisible || !1;
      return Tn(i, s, o, n, r);
    },
    nearest(i, t, e, n) {
      const s = Lt(t, i), o = e.axis || "xy", r = e.includeInvisible || !1;
      return Pn(i, s, o, e.intersect, n, r);
    },
    x(i, t, e, n) {
      const s = Lt(t, i);
      return _o(i, s, "x", e.intersect, n);
    },
    y(i, t, e, n) {
      const s = Lt(t, i);
      return _o(i, s, "y", e.intersect, n);
    }
  }
};
const ia = [
  "left",
  "top",
  "right",
  "bottom"
];
function He(i, t) {
  return i.filter((e) => e.pos === t);
}
function yo(i, t) {
  return i.filter((e) => ia.indexOf(e.pos) === -1 && e.box.axis === t);
}
function We(i, t) {
  return i.sort((e, n) => {
    const s = t ? n : e, o = t ? e : n;
    return s.weight === o.weight ? s.index - o.index : s.weight - o.weight;
  });
}
function Lh(i) {
  const t = [];
  let e, n, s, o, r, a;
  for (e = 0, n = (i || []).length; e < n; ++e)
    s = i[e], { position: o, options: { stack: r, stackWeight: a = 1 } } = s, t.push({
      index: e,
      box: s,
      pos: o,
      horizontal: s.isHorizontal(),
      weight: s.weight,
      stack: r && o + r,
      stackWeight: a
    });
  return t;
}
function Ih(i) {
  const t = {};
  for (const e of i) {
    const { stack: n, pos: s, stackWeight: o } = e;
    if (!n || !ia.includes(s))
      continue;
    const r = t[n] || (t[n] = {
      count: 0,
      placed: 0,
      weight: 0,
      size: 0
    });
    r.count++, r.weight += o;
  }
  return t;
}
function Fh(i, t) {
  const e = Ih(i), { vBoxMaxWidth: n, hBoxMaxHeight: s } = t;
  let o, r, a;
  for (o = 0, r = i.length; o < r; ++o) {
    a = i[o];
    const { fullSize: l } = a.box, c = e[a.stack], h = c && a.stackWeight / c.weight;
    a.horizontal ? (a.width = h ? h * n : l && t.availableWidth, a.height = s) : (a.width = n, a.height = h ? h * s : l && t.availableHeight);
  }
  return e;
}
function zh(i) {
  const t = Lh(i), e = We(t.filter((c) => c.box.fullSize), !0), n = We(He(t, "left"), !0), s = We(He(t, "right")), o = We(He(t, "top"), !0), r = We(He(t, "bottom")), a = yo(t, "x"), l = yo(t, "y");
  return {
    fullSize: e,
    leftAndTop: n.concat(o),
    rightAndBottom: s.concat(l).concat(r).concat(a),
    chartArea: He(t, "chartArea"),
    vertical: n.concat(s).concat(l),
    horizontal: o.concat(r).concat(a)
  };
}
function vo(i, t, e, n) {
  return Math.max(i[e], t[e]) + Math.max(i[n], t[n]);
}
function na(i, t) {
  i.top = Math.max(i.top, t.top), i.left = Math.max(i.left, t.left), i.bottom = Math.max(i.bottom, t.bottom), i.right = Math.max(i.right, t.right);
}
function Nh(i, t, e, n) {
  const { pos: s, box: o } = e, r = i.maxPadding;
  if (!H(s)) {
    e.size && (i[s] -= e.size);
    const u = n[e.stack] || {
      size: 0,
      count: 1
    };
    u.size = Math.max(u.size, e.horizontal ? o.height : o.width), e.size = u.size / u.count, i[s] += e.size;
  }
  o.getPadding && na(r, o.getPadding());
  const a = Math.max(0, t.outerWidth - vo(r, i, "left", "right")), l = Math.max(0, t.outerHeight - vo(r, i, "top", "bottom")), c = a !== i.w, h = l !== i.h;
  return i.w = a, i.h = l, e.horizontal ? {
    same: c,
    other: h
  } : {
    same: h,
    other: c
  };
}
function Bh(i) {
  const t = i.maxPadding;
  function e(n) {
    const s = Math.max(t[n] - i[n], 0);
    return i[n] += s, s;
  }
  i.y += e("top"), i.x += e("left"), e("right"), e("bottom");
}
function Vh(i, t) {
  const e = t.maxPadding;
  function n(s) {
    const o = {
      left: 0,
      top: 0,
      right: 0,
      bottom: 0
    };
    return s.forEach((r) => {
      o[r] = Math.max(t[r], e[r]);
    }), o;
  }
  return n(i ? [
    "left",
    "right"
  ] : [
    "top",
    "bottom"
  ]);
}
function $e(i, t, e, n) {
  const s = [];
  let o, r, a, l, c, h;
  for (o = 0, r = i.length, c = 0; o < r; ++o) {
    a = i[o], l = a.box, l.update(a.width || t.w, a.height || t.h, Vh(a.horizontal, t));
    const { same: u, other: d } = Nh(t, e, a, n);
    c |= u && s.length, h = h || d, l.fullSize || s.push(a);
  }
  return c && $e(s, t, e, n) || h;
}
function Di(i, t, e, n, s) {
  i.top = e, i.left = t, i.right = t + n, i.bottom = e + s, i.width = n, i.height = s;
}
function Mo(i, t, e, n) {
  const s = e.padding;
  let { x: o, y: r } = t;
  for (const a of i) {
    const l = a.box, c = n[a.stack] || {
      placed: 0,
      weight: 1
    }, h = a.stackWeight / c.weight || 1;
    if (a.horizontal) {
      const u = t.w * h, d = c.size || l.height;
      oi(c.start) && (r = c.start), l.fullSize ? Di(l, s.left, r, e.outerWidth - s.right - s.left, d) : Di(l, t.left + c.placed, r, u, d), c.start = r, c.placed += u, r = l.bottom;
    } else {
      const u = t.h * h, d = c.size || l.width;
      oi(c.start) && (o = c.start), l.fullSize ? Di(l, o, s.top, d, e.outerHeight - s.bottom - s.top) : Di(l, o, t.top + c.placed, d, u), c.start = o, c.placed += u, o = l.right;
    }
  }
  t.x = o, t.y = r;
}
var ct = {
  addBox(i, t) {
    i.boxes || (i.boxes = []), t.fullSize = t.fullSize || !1, t.position = t.position || "top", t.weight = t.weight || 0, t._layers = t._layers || function() {
      return [
        {
          z: 0,
          draw(e) {
            t.draw(e);
          }
        }
      ];
    }, i.boxes.push(t);
  },
  removeBox(i, t) {
    const e = i.boxes ? i.boxes.indexOf(t) : -1;
    e !== -1 && i.boxes.splice(e, 1);
  },
  configure(i, t, e) {
    t.fullSize = e.fullSize, t.position = e.position, t.weight = e.weight;
  },
  update(i, t, e, n) {
    if (!i)
      return;
    const s = ht(i.options.layout.padding), o = Math.max(t - s.width, 0), r = Math.max(e - s.height, 0), a = zh(i.boxes), l = a.vertical, c = a.horizontal;
    B(i.boxes, (b) => {
      typeof b.beforeLayout == "function" && b.beforeLayout();
    });
    const h = l.reduce((b, _) => _.box.options && _.box.options.display === !1 ? b : b + 1, 0) || 1, u = Object.freeze({
      outerWidth: t,
      outerHeight: e,
      padding: s,
      availableWidth: o,
      availableHeight: r,
      vBoxMaxWidth: o / 2 / h,
      hBoxMaxHeight: r / 2
    }), d = Object.assign({}, s);
    na(d, ht(n));
    const g = Object.assign({
      maxPadding: d,
      w: o,
      h: r,
      x: s.left,
      y: s.top
    }, s), m = Fh(l.concat(c), u);
    $e(a.fullSize, g, u, m), $e(l, g, u, m), $e(c, g, u, m) && $e(l, g, u, m), Bh(g), Mo(a.leftAndTop, g, u, m), g.x += g.w, g.y += g.h, Mo(a.rightAndBottom, g, u, m), i.chartArea = {
      left: g.left,
      top: g.top,
      right: g.left + g.w,
      bottom: g.top + g.h,
      height: g.h,
      width: g.w
    }, B(a.chartArea, (b) => {
      const _ = b.box;
      Object.assign(_, i.chartArea), _.update(g.w, g.h, {
        left: 0,
        top: 0,
        right: 0,
        bottom: 0
      });
    });
  }
};
class sa {
  acquireContext(t, e) {
  }
  releaseContext(t) {
    return !1;
  }
  addEventListener(t, e, n) {
  }
  removeEventListener(t, e, n) {
  }
  getDevicePixelRatio() {
    return 1;
  }
  getMaximumSize(t, e, n, s) {
    return e = Math.max(0, e || t.width), n = n || t.height, {
      width: e,
      height: Math.max(0, s ? Math.floor(e / s) : n)
    };
  }
  isAttached(t) {
    return !0;
  }
  updateConfig(t) {
  }
}
class Hh extends sa {
  acquireContext(t) {
    return t && t.getContext && t.getContext("2d") || null;
  }
  updateConfig(t) {
    t.options.animation = !1;
  }
}
const ji = "$chartjs", Wh = {
  touchstart: "mousedown",
  touchmove: "mousemove",
  touchend: "mouseup",
  pointerenter: "mouseenter",
  pointerdown: "mousedown",
  pointermove: "mousemove",
  pointerup: "mouseup",
  pointerleave: "mouseout",
  pointerout: "mouseout"
}, So = (i) => i === null || i === "";
function jh(i, t) {
  const e = i.style, n = i.getAttribute("height"), s = i.getAttribute("width");
  if (i[ji] = {
    initial: {
      height: n,
      width: s,
      style: {
        display: e.display,
        height: e.height,
        width: e.width
      }
    }
  }, e.display = e.display || "block", e.boxSizing = e.boxSizing || "border-box", So(s)) {
    const o = oo(i, "width");
    o !== void 0 && (i.width = o);
  }
  if (So(n))
    if (i.style.height === "")
      i.height = i.width / (t || 2);
    else {
      const o = oo(i, "height");
      o !== void 0 && (i.height = o);
    }
  return i;
}
const oa = Yc ? {
  passive: !0
} : !1;
function Yh(i, t, e) {
  i && i.addEventListener(t, e, oa);
}
function Uh(i, t, e) {
  i && i.canvas && i.canvas.removeEventListener(t, e, oa);
}
function Xh(i, t) {
  const e = Wh[i.type] || i.type, { x: n, y: s } = Lt(i, t);
  return {
    type: e,
    chart: t,
    native: i,
    x: n !== void 0 ? n : null,
    y: s !== void 0 ? s : null
  };
}
function Ji(i, t) {
  for (const e of i)
    if (e === t || e.contains(t))
      return !0;
}
function $h(i, t, e) {
  const n = i.canvas, s = new MutationObserver((o) => {
    let r = !1;
    for (const a of o)
      r = r || Ji(a.addedNodes, n), r = r && !Ji(a.removedNodes, n);
    r && e();
  });
  return s.observe(document, {
    childList: !0,
    subtree: !0
  }), s;
}
function qh(i, t, e) {
  const n = i.canvas, s = new MutationObserver((o) => {
    let r = !1;
    for (const a of o)
      r = r || Ji(a.removedNodes, n), r = r && !Ji(a.addedNodes, n);
    r && e();
  });
  return s.observe(document, {
    childList: !0,
    subtree: !0
  }), s;
}
const li = /* @__PURE__ */ new Map();
let ko = 0;
function ra() {
  const i = window.devicePixelRatio;
  i !== ko && (ko = i, li.forEach((t, e) => {
    e.currentDevicePixelRatio !== i && t();
  }));
}
function Zh(i, t) {
  li.size || window.addEventListener("resize", ra), li.set(i, t);
}
function Gh(i) {
  li.delete(i), li.size || window.removeEventListener("resize", ra);
}
function Kh(i, t, e) {
  const n = i.canvas, s = n && ms(n);
  if (!s)
    return;
  const o = Ir((a, l) => {
    const c = s.clientWidth;
    e(a, l), c < s.clientWidth && e();
  }, window), r = new ResizeObserver((a) => {
    const l = a[0], c = l.contentRect.width, h = l.contentRect.height;
    c === 0 && h === 0 || o(c, h);
  });
  return r.observe(s), Zh(i, o), r;
}
function Cn(i, t, e) {
  e && e.disconnect(), t === "resize" && Gh(i);
}
function Jh(i, t, e) {
  const n = i.canvas, s = Ir((o) => {
    i.ctx !== null && e(Xh(o, i));
  }, i);
  return Yh(n, t, s), s;
}
class Qh extends sa {
  acquireContext(t, e) {
    const n = t && t.getContext && t.getContext("2d");
    return n && n.canvas === t ? (jh(t, e), n) : null;
  }
  releaseContext(t) {
    const e = t.canvas;
    if (!e[ji])
      return !1;
    const n = e[ji].initial;
    [
      "height",
      "width"
    ].forEach((o) => {
      const r = n[o];
      V(r) ? e.removeAttribute(o) : e.setAttribute(o, r);
    });
    const s = n.style || {};
    return Object.keys(s).forEach((o) => {
      e.style[o] = s[o];
    }), e.width = e.width, delete e[ji], !0;
  }
  addEventListener(t, e, n) {
    this.removeEventListener(t, e);
    const s = t.$proxies || (t.$proxies = {}), r = {
      attach: $h,
      detach: qh,
      resize: Kh
    }[e] || Jh;
    s[e] = r(t, e, n);
  }
  removeEventListener(t, e) {
    const n = t.$proxies || (t.$proxies = {}), s = n[e];
    if (!s)
      return;
    ({
      attach: Cn,
      detach: Cn,
      resize: Cn
    }[e] || Uh)(t, e, s), n[e] = void 0;
  }
  getDevicePixelRatio() {
    return window.devicePixelRatio;
  }
  getMaximumSize(t, e, n, s) {
    return jc(t, e, n, s);
  }
  isAttached(t) {
    const e = t && ms(t);
    return !!(e && e.isConnected);
  }
}
function tu(i) {
  return !ps() || typeof OffscreenCanvas < "u" && i instanceof OffscreenCanvas ? Hh : Qh;
}
class Ct {
  constructor() {
    O(this, "x");
    O(this, "y");
    O(this, "active", !1);
    O(this, "options");
    O(this, "$animations");
  }
  tooltipPosition(t) {
    const { x: e, y: n } = this.getProps([
      "x",
      "y"
    ], t);
    return {
      x: e,
      y: n
    };
  }
  hasValue() {
    return Oe(this.x) && Oe(this.y);
  }
  getProps(t, e) {
    const n = this.$animations;
    if (!e || !n)
      return this;
    const s = {};
    return t.forEach((o) => {
      s[o] = n[o] && n[o].active() ? n[o]._to : this[o];
    }), s;
  }
}
O(Ct, "defaults", {}), O(Ct, "defaultRoutes");
function eu(i, t) {
  const e = i.options.ticks, n = iu(i), s = Math.min(e.maxTicksLimit || n, n), o = e.major.enabled ? su(t) : [], r = o.length, a = o[0], l = o[r - 1], c = [];
  if (r > s)
    return ou(t, c, o, r / s), c;
  const h = nu(o, t, s);
  if (r > 0) {
    let u, d;
    const g = r > 1 ? Math.round((l - a) / (r - 1)) : null;
    for (Ai(t, c, h, V(g) ? 0 : a - g, a), u = 0, d = r - 1; u < d; u++)
      Ai(t, c, h, o[u], o[u + 1]);
    return Ai(t, c, h, l, V(g) ? t.length : l + g), c;
  }
  return Ai(t, c, h), c;
}
function iu(i) {
  const t = i.options.offset, e = i._tickSize(), n = i._length / e + (t ? 0 : 1), s = i._maxLength / e;
  return Math.floor(Math.min(n, s));
}
function nu(i, t, e) {
  const n = ru(i), s = t.length / e;
  if (!n)
    return Math.max(s, 1);
  const o = Xl(n);
  for (let r = 0, a = o.length - 1; r < a; r++) {
    const l = o[r];
    if (l > s)
      return l;
  }
  return Math.max(s, 1);
}
function su(i) {
  const t = [];
  let e, n;
  for (e = 0, n = i.length; e < n; e++)
    i[e].major && t.push(e);
  return t;
}
function ou(i, t, e, n) {
  let s = 0, o = e[0], r;
  for (n = Math.ceil(n), r = 0; r < i.length; r++)
    r === o && (t.push(i[r]), s++, o = e[s * n]);
}
function Ai(i, t, e, n, s) {
  const o = I(n, 0), r = Math.min(I(s, i.length), i.length);
  let a = 0, l, c, h;
  for (e = Math.ceil(e), s && (l = s - n, e = l / Math.floor(l / e)), h = o; h < 0; )
    a++, h = Math.round(o + a * e);
  for (c = Math.max(o, 0); c < r; c++)
    c === h && (t.push(i[c]), a++, h = Math.round(o + a * e));
}
function ru(i) {
  const t = i.length;
  let e, n;
  if (t < 2)
    return !1;
  for (n = i[0], e = 1; e < t; ++e)
    if (i[e] - i[e - 1] !== n)
      return !1;
  return n;
}
const au = (i) => i === "left" ? "right" : i === "right" ? "left" : i, wo = (i, t, e) => t === "top" || t === "left" ? i[t] + e : i[t] - e, To = (i, t) => Math.min(t || i, i);
function Po(i, t) {
  const e = [], n = i.length / t, s = i.length;
  let o = 0;
  for (; o < s; o += n)
    e.push(i[Math.floor(o)]);
  return e;
}
function lu(i, t, e) {
  const n = i.ticks.length, s = Math.min(t, n - 1), o = i._startPixel, r = i._endPixel, a = 1e-6;
  let l = i.getPixelForTick(s), c;
  if (!(e && (n === 1 ? c = Math.max(l - o, r - l) : t === 0 ? c = (i.getPixelForTick(1) - l) / 2 : c = (l - i.getPixelForTick(s - 1)) / 2, l += s < t ? c : -c, l < o - a || l > r + a)))
    return l;
}
function cu(i, t) {
  B(i, (e) => {
    const n = e.gc, s = n.length / 2;
    let o;
    if (s > t) {
      for (o = 0; o < s; ++o)
        delete e.data[n[o]];
      n.splice(0, s);
    }
  });
}
function je(i) {
  return i.drawTicks ? i.tickLength : 0;
}
function Co(i, t) {
  if (!i.display)
    return 0;
  const e = it(i.font, t), n = ht(i.padding);
  return (q(i.text) ? i.text.length : 1) * e.lineHeight + n.height;
}
function hu(i, t) {
  return se(i, {
    scale: t,
    type: "scale"
  });
}
function uu(i, t, e) {
  return se(i, {
    tick: e,
    index: t,
    type: "tick"
  });
}
function du(i, t, e) {
  let n = cs(i);
  return (e && t !== "right" || !e && t === "right") && (n = au(n)), n;
}
function fu(i, t, e, n) {
  const { top: s, left: o, bottom: r, right: a, chart: l } = i, { chartArea: c, scales: h } = l;
  let u = 0, d, g, m;
  const b = r - s, _ = a - o;
  if (i.isHorizontal()) {
    if (g = at(n, o, a), H(e)) {
      const y = Object.keys(e)[0], v = e[y];
      m = h[y].getPixelForValue(v) + b - t;
    } else e === "center" ? m = (c.bottom + c.top) / 2 + b - t : m = wo(i, e, t);
    d = a - o;
  } else {
    if (H(e)) {
      const y = Object.keys(e)[0], v = e[y];
      g = h[y].getPixelForValue(v) - _ + t;
    } else e === "center" ? g = (c.left + c.right) / 2 - _ + t : g = wo(i, e, t);
    m = at(n, r, s), u = e === "left" ? -J : J;
  }
  return {
    titleX: g,
    titleY: m,
    maxWidth: d,
    rotation: u
  };
}
class ve extends Ct {
  constructor(t) {
    super(), this.id = t.id, this.type = t.type, this.options = void 0, this.ctx = t.ctx, this.chart = t.chart, this.top = void 0, this.bottom = void 0, this.left = void 0, this.right = void 0, this.width = void 0, this.height = void 0, this._margins = {
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    }, this.maxWidth = void 0, this.maxHeight = void 0, this.paddingTop = void 0, this.paddingBottom = void 0, this.paddingLeft = void 0, this.paddingRight = void 0, this.axis = void 0, this.labelRotation = void 0, this.min = void 0, this.max = void 0, this._range = void 0, this.ticks = [], this._gridLineItems = null, this._labelItems = null, this._labelSizes = null, this._length = 0, this._maxLength = 0, this._longestTextCache = {}, this._startPixel = void 0, this._endPixel = void 0, this._reversePixels = !1, this._userMax = void 0, this._userMin = void 0, this._suggestedMax = void 0, this._suggestedMin = void 0, this._ticksLength = 0, this._borderValue = 0, this._cache = {}, this._dataLimitsCached = !1, this.$context = void 0;
  }
  init(t) {
    this.options = t.setContext(this.getContext()), this.axis = t.axis, this._userMin = this.parse(t.min), this._userMax = this.parse(t.max), this._suggestedMin = this.parse(t.suggestedMin), this._suggestedMax = this.parse(t.suggestedMax);
  }
  parse(t, e) {
    return t;
  }
  getUserBounds() {
    let { _userMin: t, _userMax: e, _suggestedMin: n, _suggestedMax: s } = this;
    return t = xt(t, Number.POSITIVE_INFINITY), e = xt(e, Number.NEGATIVE_INFINITY), n = xt(n, Number.POSITIVE_INFINITY), s = xt(s, Number.NEGATIVE_INFINITY), {
      min: xt(t, n),
      max: xt(e, s),
      minDefined: K(t),
      maxDefined: K(e)
    };
  }
  getMinMax(t) {
    let { min: e, max: n, minDefined: s, maxDefined: o } = this.getUserBounds(), r;
    if (s && o)
      return {
        min: e,
        max: n
      };
    const a = this.getMatchingVisibleMetas();
    for (let l = 0, c = a.length; l < c; ++l)
      r = a[l].controller.getMinMax(this, t), s || (e = Math.min(e, r.min)), o || (n = Math.max(n, r.max));
    return e = o && e > n ? n : e, n = s && e > n ? e : n, {
      min: xt(e, xt(n, e)),
      max: xt(n, xt(e, n))
    };
  }
  getPadding() {
    return {
      left: this.paddingLeft || 0,
      top: this.paddingTop || 0,
      right: this.paddingRight || 0,
      bottom: this.paddingBottom || 0
    };
  }
  getTicks() {
    return this.ticks;
  }
  getLabels() {
    const t = this.chart.data;
    return this.options.labels || (this.isHorizontal() ? t.xLabels : t.yLabels) || t.labels || [];
  }
  getLabelItems(t = this.chart.chartArea) {
    return this._labelItems || (this._labelItems = this._computeLabelItems(t));
  }
  beforeLayout() {
    this._cache = {}, this._dataLimitsCached = !1;
  }
  beforeUpdate() {
    L(this.options.beforeUpdate, [
      this
    ]);
  }
  update(t, e, n) {
    const { beginAtZero: s, grace: o, ticks: r } = this.options, a = r.sampleSize;
    this.beforeUpdate(), this.maxWidth = t, this.maxHeight = e, this._margins = n = Object.assign({
      left: 0,
      right: 0,
      top: 0,
      bottom: 0
    }, n), this.ticks = null, this._labelSizes = null, this._gridLineItems = null, this._labelItems = null, this.beforeSetDimensions(), this.setDimensions(), this.afterSetDimensions(), this._maxLength = this.isHorizontal() ? this.width + n.left + n.right : this.height + n.top + n.bottom, this._dataLimitsCached || (this.beforeDataLimits(), this.determineDataLimits(), this.afterDataLimits(), this._range = vc(this, o, s), this._dataLimitsCached = !0), this.beforeBuildTicks(), this.ticks = this.buildTicks() || [], this.afterBuildTicks();
    const l = a < this.ticks.length;
    this._convertTicksToLabels(l ? Po(this.ticks, a) : this.ticks), this.configure(), this.beforeCalculateLabelRotation(), this.calculateLabelRotation(), this.afterCalculateLabelRotation(), r.display && (r.autoSkip || r.source === "auto") && (this.ticks = eu(this, this.ticks), this._labelSizes = null, this.afterAutoSkip()), l && this._convertTicksToLabels(this.ticks), this.beforeFit(), this.fit(), this.afterFit(), this.afterUpdate();
  }
  configure() {
    let t = this.options.reverse, e, n;
    this.isHorizontal() ? (e = this.left, n = this.right) : (e = this.top, n = this.bottom, t = !t), this._startPixel = e, this._endPixel = n, this._reversePixels = t, this._length = n - e, this._alignToPixels = this.options.alignToPixels;
  }
  afterUpdate() {
    L(this.options.afterUpdate, [
      this
    ]);
  }
  beforeSetDimensions() {
    L(this.options.beforeSetDimensions, [
      this
    ]);
  }
  setDimensions() {
    this.isHorizontal() ? (this.width = this.maxWidth, this.left = 0, this.right = this.width) : (this.height = this.maxHeight, this.top = 0, this.bottom = this.height), this.paddingLeft = 0, this.paddingTop = 0, this.paddingRight = 0, this.paddingBottom = 0;
  }
  afterSetDimensions() {
    L(this.options.afterSetDimensions, [
      this
    ]);
  }
  _callHooks(t) {
    this.chart.notifyPlugins(t, this.getContext()), L(this.options[t], [
      this
    ]);
  }
  beforeDataLimits() {
    this._callHooks("beforeDataLimits");
  }
  determineDataLimits() {
  }
  afterDataLimits() {
    this._callHooks("afterDataLimits");
  }
  beforeBuildTicks() {
    this._callHooks("beforeBuildTicks");
  }
  buildTicks() {
    return [];
  }
  afterBuildTicks() {
    this._callHooks("afterBuildTicks");
  }
  beforeTickToLabelConversion() {
    L(this.options.beforeTickToLabelConversion, [
      this
    ]);
  }
  generateTickLabels(t) {
    const e = this.options.ticks;
    let n, s, o;
    for (n = 0, s = t.length; n < s; n++)
      o = t[n], o.label = L(e.callback, [
        o.value,
        n,
        t
      ], this);
  }
  afterTickToLabelConversion() {
    L(this.options.afterTickToLabelConversion, [
      this
    ]);
  }
  beforeCalculateLabelRotation() {
    L(this.options.beforeCalculateLabelRotation, [
      this
    ]);
  }
  calculateLabelRotation() {
    const t = this.options, e = t.ticks, n = To(this.ticks.length, t.ticks.maxTicksLimit), s = e.minRotation || 0, o = e.maxRotation;
    let r = s, a, l, c;
    if (!this._isVisible() || !e.display || s >= o || n <= 1 || !this.isHorizontal()) {
      this.labelRotation = s;
      return;
    }
    const h = this._getLabelSizes(), u = h.widest.width, d = h.highest.height, g = ot(this.chart.width - u, 0, this.maxWidth);
    a = t.offset ? this.maxWidth / n : g / (n - 1), u + 6 > a && (a = g / (n - (t.offset ? 0.5 : 1)), l = this.maxHeight - je(t.grid) - e.padding - Co(t.title, this.chart.options.font), c = Math.sqrt(u * u + d * d), r = as(Math.min(Math.asin(ot((h.highest.height + 6) / a, -1, 1)), Math.asin(ot(l / c, -1, 1)) - Math.asin(ot(d / c, -1, 1)))), r = Math.max(s, Math.min(o, r))), this.labelRotation = r;
  }
  afterCalculateLabelRotation() {
    L(this.options.afterCalculateLabelRotation, [
      this
    ]);
  }
  afterAutoSkip() {
  }
  beforeFit() {
    L(this.options.beforeFit, [
      this
    ]);
  }
  fit() {
    const t = {
      width: 0,
      height: 0
    }, { chart: e, options: { ticks: n, title: s, grid: o } } = this, r = this._isVisible(), a = this.isHorizontal();
    if (r) {
      const l = Co(s, e.options.font);
      if (a ? (t.width = this.maxWidth, t.height = je(o) + l) : (t.height = this.maxHeight, t.width = je(o) + l), n.display && this.ticks.length) {
        const { first: c, last: h, widest: u, highest: d } = this._getLabelSizes(), g = n.padding * 2, m = Tt(this.labelRotation), b = Math.cos(m), _ = Math.sin(m);
        if (a) {
          const y = n.mirror ? 0 : _ * u.width + b * d.height;
          t.height = Math.min(this.maxHeight, t.height + y + g);
        } else {
          const y = n.mirror ? 0 : b * u.width + _ * d.height;
          t.width = Math.min(this.maxWidth, t.width + y + g);
        }
        this._calculatePadding(c, h, _, b);
      }
    }
    this._handleMargins(), a ? (this.width = this._length = e.width - this._margins.left - this._margins.right, this.height = t.height) : (this.width = t.width, this.height = this._length = e.height - this._margins.top - this._margins.bottom);
  }
  _calculatePadding(t, e, n, s) {
    const { ticks: { align: o, padding: r }, position: a } = this.options, l = this.labelRotation !== 0, c = a !== "top" && this.axis === "x";
    if (this.isHorizontal()) {
      const h = this.getPixelForTick(0) - this.left, u = this.right - this.getPixelForTick(this.ticks.length - 1);
      let d = 0, g = 0;
      l ? c ? (d = s * t.width, g = n * e.height) : (d = n * t.height, g = s * e.width) : o === "start" ? g = e.width : o === "end" ? d = t.width : o !== "inner" && (d = t.width / 2, g = e.width / 2), this.paddingLeft = Math.max((d - h + r) * this.width / (this.width - h), 0), this.paddingRight = Math.max((g - u + r) * this.width / (this.width - u), 0);
    } else {
      let h = e.height / 2, u = t.height / 2;
      o === "start" ? (h = 0, u = t.height) : o === "end" && (h = e.height, u = 0), this.paddingTop = h + r, this.paddingBottom = u + r;
    }
  }
  _handleMargins() {
    this._margins && (this._margins.left = Math.max(this.paddingLeft, this._margins.left), this._margins.top = Math.max(this.paddingTop, this._margins.top), this._margins.right = Math.max(this.paddingRight, this._margins.right), this._margins.bottom = Math.max(this.paddingBottom, this._margins.bottom));
  }
  afterFit() {
    L(this.options.afterFit, [
      this
    ]);
  }
  isHorizontal() {
    const { axis: t, position: e } = this.options;
    return e === "top" || e === "bottom" || t === "x";
  }
  isFullSize() {
    return this.options.fullSize;
  }
  _convertTicksToLabels(t) {
    this.beforeTickToLabelConversion(), this.generateTickLabels(t);
    let e, n;
    for (e = 0, n = t.length; e < n; e++)
      V(t[e].label) && (t.splice(e, 1), n--, e--);
    this.afterTickToLabelConversion();
  }
  _getLabelSizes() {
    let t = this._labelSizes;
    if (!t) {
      const e = this.options.ticks.sampleSize;
      let n = this.ticks;
      e < n.length && (n = Po(n, e)), this._labelSizes = t = this._computeLabelSizes(n, n.length, this.options.ticks.maxTicksLimit);
    }
    return t;
  }
  _computeLabelSizes(t, e, n) {
    const { ctx: s, _longestTextCache: o } = this, r = [], a = [], l = Math.floor(e / To(e, n));
    let c = 0, h = 0, u, d, g, m, b, _, y, v, w, S, k;
    for (u = 0; u < e; u += l) {
      if (m = t[u].label, b = this._resolveTickFontOptions(u), s.font = _ = b.string, y = o[_] = o[_] || {
        data: {},
        gc: []
      }, v = b.lineHeight, w = S = 0, !V(m) && !q(m))
        w = Gi(s, y.data, y.gc, w, m), S = v;
      else if (q(m))
        for (d = 0, g = m.length; d < g; ++d)
          k = m[d], !V(k) && !q(k) && (w = Gi(s, y.data, y.gc, w, k), S += v);
      r.push(w), a.push(S), c = Math.max(w, c), h = Math.max(S, h);
    }
    cu(o, e);
    const P = r.indexOf(c), C = a.indexOf(h), D = (E) => ({
      width: r[E] || 0,
      height: a[E] || 0
    });
    return {
      first: D(0),
      last: D(e - 1),
      widest: D(P),
      highest: D(C),
      widths: r,
      heights: a
    };
  }
  getLabelForValue(t) {
    return t;
  }
  getPixelForValue(t, e) {
    return NaN;
  }
  getValueForPixel(t) {
  }
  getPixelForTick(t) {
    const e = this.ticks;
    return t < 0 || t > e.length - 1 ? null : this.getPixelForValue(e[t].value);
  }
  getPixelForDecimal(t) {
    this._reversePixels && (t = 1 - t);
    const e = this._startPixel + t * this._length;
    return Gl(this._alignToPixels ? ue(this.chart, e, 0) : e);
  }
  getDecimalForPixel(t) {
    const e = (t - this._startPixel) / this._length;
    return this._reversePixels ? 1 - e : e;
  }
  getBasePixel() {
    return this.getPixelForValue(this.getBaseValue());
  }
  getBaseValue() {
    const { min: t, max: e } = this;
    return t < 0 && e < 0 ? e : t > 0 && e > 0 ? t : 0;
  }
  getContext(t) {
    const e = this.ticks || [];
    if (t >= 0 && t < e.length) {
      const n = e[t];
      return n.$context || (n.$context = uu(this.getContext(), t, n));
    }
    return this.$context || (this.$context = hu(this.chart.getContext(), this));
  }
  _tickSize() {
    const t = this.options.ticks, e = Tt(this.labelRotation), n = Math.abs(Math.cos(e)), s = Math.abs(Math.sin(e)), o = this._getLabelSizes(), r = t.autoSkipPadding || 0, a = o ? o.widest.width + r : 0, l = o ? o.highest.height + r : 0;
    return this.isHorizontal() ? l * n > a * s ? a / n : l / s : l * s < a * n ? l / n : a / s;
  }
  _isVisible() {
    const t = this.options.display;
    return t !== "auto" ? !!t : this.getMatchingVisibleMetas().length > 0;
  }
  _computeGridLineItems(t) {
    const e = this.axis, n = this.chart, s = this.options, { grid: o, position: r, border: a } = s, l = o.offset, c = this.isHorizontal(), u = this.ticks.length + (l ? 1 : 0), d = je(o), g = [], m = a.setContext(this.getContext()), b = m.display ? m.width : 0, _ = b / 2, y = function(Y) {
      return ue(n, Y, b);
    };
    let v, w, S, k, P, C, D, E, F, R, z, Q;
    if (r === "top")
      v = y(this.bottom), C = this.bottom - d, E = v - _, R = y(t.top) + _, Q = t.bottom;
    else if (r === "bottom")
      v = y(this.top), R = t.top, Q = y(t.bottom) - _, C = v + _, E = this.top + d;
    else if (r === "left")
      v = y(this.right), P = this.right - d, D = v - _, F = y(t.left) + _, z = t.right;
    else if (r === "right")
      v = y(this.left), F = t.left, z = y(t.right) - _, P = v + _, D = this.left + d;
    else if (e === "x") {
      if (r === "center")
        v = y((t.top + t.bottom) / 2 + 0.5);
      else if (H(r)) {
        const Y = Object.keys(r)[0], $ = r[Y];
        v = y(this.chart.scales[Y].getPixelForValue($));
      }
      R = t.top, Q = t.bottom, C = v + _, E = C + d;
    } else if (e === "y") {
      if (r === "center")
        v = y((t.left + t.right) / 2);
      else if (H(r)) {
        const Y = Object.keys(r)[0], $ = r[Y];
        v = y(this.chart.scales[Y].getPixelForValue($));
      }
      P = v - _, D = P - d, F = t.left, z = t.right;
    }
    const nt = I(s.ticks.maxTicksLimit, u), j = Math.max(1, Math.ceil(u / nt));
    for (w = 0; w < u; w += j) {
      const Y = this.getContext(w), $ = o.setContext(Y), bt = a.setContext(Y), et = $.lineWidth, Ut = $.color, Se = bt.dash || [], _t = bt.dashOffset, oe = $.tickWidth, kt = $.tickColor, re = $.tickBorderDash || [], Ft = $.tickBorderDashOffset;
      S = lu(this, w, l), S !== void 0 && (k = ue(n, S, et), c ? P = D = F = z = k : C = E = R = Q = k, g.push({
        tx1: P,
        ty1: C,
        tx2: D,
        ty2: E,
        x1: F,
        y1: R,
        x2: z,
        y2: Q,
        width: et,
        color: Ut,
        borderDash: Se,
        borderDashOffset: _t,
        tickWidth: oe,
        tickColor: kt,
        tickBorderDash: re,
        tickBorderDashOffset: Ft
      }));
    }
    return this._ticksLength = u, this._borderValue = v, g;
  }
  _computeLabelItems(t) {
    const e = this.axis, n = this.options, { position: s, ticks: o } = n, r = this.isHorizontal(), a = this.ticks, { align: l, crossAlign: c, padding: h, mirror: u } = o, d = je(n.grid), g = d + h, m = u ? -h : g, b = -Tt(this.labelRotation), _ = [];
    let y, v, w, S, k, P, C, D, E, F, R, z, Q = "middle";
    if (s === "top")
      P = this.bottom - m, C = this._getXAxisLabelAlignment();
    else if (s === "bottom")
      P = this.top + m, C = this._getXAxisLabelAlignment();
    else if (s === "left") {
      const j = this._getYAxisLabelAlignment(d);
      C = j.textAlign, k = j.x;
    } else if (s === "right") {
      const j = this._getYAxisLabelAlignment(d);
      C = j.textAlign, k = j.x;
    } else if (e === "x") {
      if (s === "center")
        P = (t.top + t.bottom) / 2 + g;
      else if (H(s)) {
        const j = Object.keys(s)[0], Y = s[j];
        P = this.chart.scales[j].getPixelForValue(Y) + g;
      }
      C = this._getXAxisLabelAlignment();
    } else if (e === "y") {
      if (s === "center")
        k = (t.left + t.right) / 2 - g;
      else if (H(s)) {
        const j = Object.keys(s)[0], Y = s[j];
        k = this.chart.scales[j].getPixelForValue(Y);
      }
      C = this._getYAxisLabelAlignment(d).textAlign;
    }
    e === "y" && (l === "start" ? Q = "top" : l === "end" && (Q = "bottom"));
    const nt = this._getLabelSizes();
    for (y = 0, v = a.length; y < v; ++y) {
      w = a[y], S = w.label;
      const j = o.setContext(this.getContext(y));
      D = this.getPixelForTick(y) + o.labelOffset, E = this._resolveTickFontOptions(y), F = E.lineHeight, R = q(S) ? S.length : 1;
      const Y = R / 2, $ = j.color, bt = j.textStrokeColor, et = j.textStrokeWidth;
      let Ut = C;
      r ? (k = D, C === "inner" && (y === v - 1 ? Ut = this.options.reverse ? "left" : "right" : y === 0 ? Ut = this.options.reverse ? "right" : "left" : Ut = "center"), s === "top" ? c === "near" || b !== 0 ? z = -R * F + F / 2 : c === "center" ? z = -nt.highest.height / 2 - Y * F + F : z = -nt.highest.height + F / 2 : c === "near" || b !== 0 ? z = F / 2 : c === "center" ? z = nt.highest.height / 2 - Y * F : z = nt.highest.height - R * F, u && (z *= -1), b !== 0 && !j.showLabelBackdrop && (k += F / 2 * Math.sin(b))) : (P = D, z = (1 - R) * F / 2);
      let Se;
      if (j.showLabelBackdrop) {
        const _t = ht(j.backdropPadding), oe = nt.heights[y], kt = nt.widths[y];
        let re = z - _t.top, Ft = 0 - _t.left;
        switch (Q) {
          case "middle":
            re -= oe / 2;
            break;
          case "bottom":
            re -= oe;
            break;
        }
        switch (C) {
          case "center":
            Ft -= kt / 2;
            break;
          case "right":
            Ft -= kt;
            break;
          case "inner":
            y === v - 1 ? Ft -= kt : y > 0 && (Ft -= kt / 2);
            break;
        }
        Se = {
          left: Ft,
          top: re,
          width: kt + _t.width,
          height: oe + _t.height,
          color: j.backdropColor
        };
      }
      _.push({
        label: S,
        font: E,
        textOffset: z,
        options: {
          rotation: b,
          color: $,
          strokeColor: bt,
          strokeWidth: et,
          textAlign: Ut,
          textBaseline: Q,
          translation: [
            k,
            P
          ],
          backdrop: Se
        }
      });
    }
    return _;
  }
  _getXAxisLabelAlignment() {
    const { position: t, ticks: e } = this.options;
    if (-Tt(this.labelRotation))
      return t === "top" ? "left" : "right";
    let s = "center";
    return e.align === "start" ? s = "left" : e.align === "end" ? s = "right" : e.align === "inner" && (s = "inner"), s;
  }
  _getYAxisLabelAlignment(t) {
    const { position: e, ticks: { crossAlign: n, mirror: s, padding: o } } = this.options, r = this._getLabelSizes(), a = t + o, l = r.widest.width;
    let c, h;
    return e === "left" ? s ? (h = this.right + o, n === "near" ? c = "left" : n === "center" ? (c = "center", h += l / 2) : (c = "right", h += l)) : (h = this.right - a, n === "near" ? c = "right" : n === "center" ? (c = "center", h -= l / 2) : (c = "left", h = this.left)) : e === "right" ? s ? (h = this.left + o, n === "near" ? c = "right" : n === "center" ? (c = "center", h -= l / 2) : (c = "left", h -= l)) : (h = this.left + a, n === "near" ? c = "left" : n === "center" ? (c = "center", h += l / 2) : (c = "right", h = this.right)) : c = "right", {
      textAlign: c,
      x: h
    };
  }
  _computeLabelArea() {
    if (this.options.ticks.mirror)
      return;
    const t = this.chart, e = this.options.position;
    if (e === "left" || e === "right")
      return {
        top: 0,
        left: this.left,
        bottom: t.height,
        right: this.right
      };
    if (e === "top" || e === "bottom")
      return {
        top: this.top,
        left: 0,
        bottom: this.bottom,
        right: t.width
      };
  }
  drawBackground() {
    const { ctx: t, options: { backgroundColor: e }, left: n, top: s, width: o, height: r } = this;
    e && (t.save(), t.fillStyle = e, t.fillRect(n, s, o, r), t.restore());
  }
  getLineWidthForValue(t) {
    const e = this.options.grid;
    if (!this._isVisible() || !e.display)
      return 0;
    const s = this.ticks.findIndex((o) => o.value === t);
    return s >= 0 ? e.setContext(this.getContext(s)).lineWidth : 0;
  }
  drawGrid(t) {
    const e = this.options.grid, n = this.ctx, s = this._gridLineItems || (this._gridLineItems = this._computeGridLineItems(t));
    let o, r;
    const a = (l, c, h) => {
      !h.width || !h.color || (n.save(), n.lineWidth = h.width, n.strokeStyle = h.color, n.setLineDash(h.borderDash || []), n.lineDashOffset = h.borderDashOffset, n.beginPath(), n.moveTo(l.x, l.y), n.lineTo(c.x, c.y), n.stroke(), n.restore());
    };
    if (e.display)
      for (o = 0, r = s.length; o < r; ++o) {
        const l = s[o];
        e.drawOnChartArea && a({
          x: l.x1,
          y: l.y1
        }, {
          x: l.x2,
          y: l.y2
        }, l), e.drawTicks && a({
          x: l.tx1,
          y: l.ty1
        }, {
          x: l.tx2,
          y: l.ty2
        }, {
          color: l.tickColor,
          width: l.tickWidth,
          borderDash: l.tickBorderDash,
          borderDashOffset: l.tickBorderDashOffset
        });
      }
  }
  drawBorder() {
    const { chart: t, ctx: e, options: { border: n, grid: s } } = this, o = n.setContext(this.getContext()), r = n.display ? o.width : 0;
    if (!r)
      return;
    const a = s.setContext(this.getContext(0)).lineWidth, l = this._borderValue;
    let c, h, u, d;
    this.isHorizontal() ? (c = ue(t, this.left, r) - r / 2, h = ue(t, this.right, a) + a / 2, u = d = l) : (u = ue(t, this.top, r) - r / 2, d = ue(t, this.bottom, a) + a / 2, c = h = l), e.save(), e.lineWidth = o.width, e.strokeStyle = o.color, e.beginPath(), e.moveTo(c, u), e.lineTo(h, d), e.stroke(), e.restore();
  }
  drawLabels(t) {
    if (!this.options.ticks.display)
      return;
    const n = this.ctx, s = this._computeLabelArea();
    s && nn(n, s);
    const o = this.getLabelItems(t);
    for (const r of o) {
      const a = r.options, l = r.font, c = r.label, h = r.textOffset;
      ye(n, c, 0, h, l, a);
    }
    s && sn(n);
  }
  drawTitle() {
    const { ctx: t, options: { position: e, title: n, reverse: s } } = this;
    if (!n.display)
      return;
    const o = it(n.font), r = ht(n.padding), a = n.align;
    let l = o.lineHeight / 2;
    e === "bottom" || e === "center" || H(e) ? (l += r.bottom, q(n.text) && (l += o.lineHeight * (n.text.length - 1))) : l += r.top;
    const { titleX: c, titleY: h, maxWidth: u, rotation: d } = fu(this, l, e, a);
    ye(t, n.text, 0, 0, o, {
      color: n.color,
      maxWidth: u,
      rotation: d,
      textAlign: du(a, e, s),
      textBaseline: "middle",
      translation: [
        c,
        h
      ]
    });
  }
  draw(t) {
    this._isVisible() && (this.drawBackground(), this.drawGrid(t), this.drawBorder(), this.drawTitle(), this.drawLabels(t));
  }
  _layers() {
    const t = this.options, e = t.ticks && t.ticks.z || 0, n = I(t.grid && t.grid.z, -1), s = I(t.border && t.border.z, 0);
    return !this._isVisible() || this.draw !== ve.prototype.draw ? [
      {
        z: e,
        draw: (o) => {
          this.draw(o);
        }
      }
    ] : [
      {
        z: n,
        draw: (o) => {
          this.drawBackground(), this.drawGrid(o), this.drawTitle();
        }
      },
      {
        z: s,
        draw: () => {
          this.drawBorder();
        }
      },
      {
        z: e,
        draw: (o) => {
          this.drawLabels(o);
        }
      }
    ];
  }
  getMatchingVisibleMetas(t) {
    const e = this.chart.getSortedVisibleDatasetMetas(), n = this.axis + "AxisID", s = [];
    let o, r;
    for (o = 0, r = e.length; o < r; ++o) {
      const a = e[o];
      a[n] === this.id && (!t || a.type === t) && s.push(a);
    }
    return s;
  }
  _resolveTickFontOptions(t) {
    const e = this.options.ticks.setContext(this.getContext(t));
    return it(e.font);
  }
  _maxDigits() {
    const t = this._resolveTickFontOptions(0).lineHeight;
    return (this.isHorizontal() ? this.width : this.height) / t;
  }
}
class Ei {
  constructor(t, e, n) {
    this.type = t, this.scope = e, this.override = n, this.items = /* @__PURE__ */ Object.create(null);
  }
  isForType(t) {
    return Object.prototype.isPrototypeOf.call(this.type.prototype, t.prototype);
  }
  register(t) {
    const e = Object.getPrototypeOf(t);
    let n;
    mu(e) && (n = this.register(e));
    const s = this.items, o = t.id, r = this.scope + "." + o;
    if (!o)
      throw new Error("class does not have id: " + t);
    return o in s || (s[o] = t, gu(t, r, n), this.override && Z.override(t.id, t.overrides)), r;
  }
  get(t) {
    return this.items[t];
  }
  unregister(t) {
    const e = this.items, n = t.id, s = this.scope;
    n in e && delete e[n], s && n in Z[s] && (delete Z[s][n], this.override && delete _e[n]);
  }
}
function gu(i, t, e) {
  const n = si(/* @__PURE__ */ Object.create(null), [
    e ? Z.get(e) : {},
    Z.get(t),
    i.defaults
  ]);
  Z.set(t, n), i.defaultRoutes && pu(t, i.defaultRoutes), i.descriptors && Z.describe(t, i.descriptors);
}
function pu(i, t) {
  Object.keys(t).forEach((e) => {
    const n = e.split("."), s = n.pop(), o = [
      i
    ].concat(n).join("."), r = t[e].split("."), a = r.pop(), l = r.join(".");
    Z.route(o, s, l, a);
  });
}
function mu(i) {
  return "id" in i && "defaults" in i;
}
class bu {
  constructor() {
    this.controllers = new Ei(Pt, "datasets", !0), this.elements = new Ei(Ct, "elements"), this.plugins = new Ei(Object, "plugins"), this.scales = new Ei(ve, "scales"), this._typedRegistries = [
      this.controllers,
      this.scales,
      this.elements
    ];
  }
  add(...t) {
    this._each("register", t);
  }
  remove(...t) {
    this._each("unregister", t);
  }
  addControllers(...t) {
    this._each("register", t, this.controllers);
  }
  addElements(...t) {
    this._each("register", t, this.elements);
  }
  addPlugins(...t) {
    this._each("register", t, this.plugins);
  }
  addScales(...t) {
    this._each("register", t, this.scales);
  }
  getController(t) {
    return this._get(t, this.controllers, "controller");
  }
  getElement(t) {
    return this._get(t, this.elements, "element");
  }
  getPlugin(t) {
    return this._get(t, this.plugins, "plugin");
  }
  getScale(t) {
    return this._get(t, this.scales, "scale");
  }
  removeControllers(...t) {
    this._each("unregister", t, this.controllers);
  }
  removeElements(...t) {
    this._each("unregister", t, this.elements);
  }
  removePlugins(...t) {
    this._each("unregister", t, this.plugins);
  }
  removeScales(...t) {
    this._each("unregister", t, this.scales);
  }
  _each(t, e, n) {
    [
      ...e
    ].forEach((s) => {
      const o = n || this._getRegistryForType(s);
      n || o.isForType(s) || o === this.plugins && s.id ? this._exec(t, o, s) : B(s, (r) => {
        const a = n || this._getRegistryForType(r);
        this._exec(t, a, r);
      });
    });
  }
  _exec(t, e, n) {
    const s = rs(t);
    L(n["before" + s], [], n), e[t](n), L(n["after" + s], [], n);
  }
  _getRegistryForType(t) {
    for (let e = 0; e < this._typedRegistries.length; e++) {
      const n = this._typedRegistries[e];
      if (n.isForType(t))
        return n;
    }
    return this.plugins;
  }
  _get(t, e, n) {
    const s = e.get(t);
    if (s === void 0)
      throw new Error('"' + t + '" is not a registered ' + n + ".");
    return s;
  }
}
var Rt = /* @__PURE__ */ new bu();
class xu {
  constructor() {
    this._init = void 0;
  }
  notify(t, e, n, s) {
    if (e === "beforeInit" && (this._init = this._createDescriptors(t, !0), this._notify(this._init, t, "install")), this._init === void 0)
      return;
    const o = s ? this._descriptors(t).filter(s) : this._descriptors(t), r = this._notify(o, t, e, n);
    return e === "afterDestroy" && (this._notify(o, t, "stop"), this._notify(this._init, t, "uninstall"), this._init = void 0), r;
  }
  _notify(t, e, n, s) {
    s = s || {};
    for (const o of t) {
      const r = o.plugin, a = r[n], l = [
        e,
        s,
        o.options
      ];
      if (L(a, l, r) === !1 && s.cancelable)
        return !1;
    }
    return !0;
  }
  invalidate() {
    V(this._cache) || (this._oldCache = this._cache, this._cache = void 0);
  }
  _descriptors(t) {
    if (this._cache)
      return this._cache;
    const e = this._cache = this._createDescriptors(t);
    return this._notifyStateChanges(t), e;
  }
  _createDescriptors(t, e) {
    const n = t && t.config, s = I(n.options && n.options.plugins, {}), o = _u(n);
    return s === !1 && !e ? [] : vu(t, o, s, e);
  }
  _notifyStateChanges(t) {
    const e = this._oldCache || [], n = this._cache, s = (o, r) => o.filter((a) => !r.some((l) => a.plugin.id === l.plugin.id));
    this._notify(s(e, n), t, "stop"), this._notify(s(n, e), t, "start");
  }
}
function _u(i) {
  const t = {}, e = [], n = Object.keys(Rt.plugins.items);
  for (let o = 0; o < n.length; o++)
    e.push(Rt.getPlugin(n[o]));
  const s = i.plugins || [];
  for (let o = 0; o < s.length; o++) {
    const r = s[o];
    e.indexOf(r) === -1 && (e.push(r), t[r.id] = !0);
  }
  return {
    plugins: e,
    localIds: t
  };
}
function yu(i, t) {
  return !t && i === !1 ? null : i === !0 ? {} : i;
}
function vu(i, { plugins: t, localIds: e }, n, s) {
  const o = [], r = i.getContext();
  for (const a of t) {
    const l = a.id, c = yu(n[l], s);
    c !== null && o.push({
      plugin: a,
      options: Mu(i.config, {
        plugin: a,
        local: e[l]
      }, c, r)
    });
  }
  return o;
}
function Mu(i, { plugin: t, local: e }, n, s) {
  const o = i.pluginScopeKeys(t), r = i.getOptionScopes(n, o);
  return e && t.defaults && r.push(t.defaults), i.createResolver(r, s, [
    ""
  ], {
    scriptable: !1,
    indexable: !1,
    allKeys: !0
  });
}
function Vn(i, t) {
  const e = Z.datasets[i] || {};
  return ((t.datasets || {})[i] || {}).indexAxis || t.indexAxis || e.indexAxis || "x";
}
function Su(i, t) {
  let e = i;
  return i === "_index_" ? e = t : i === "_value_" && (e = t === "x" ? "y" : "x"), e;
}
function ku(i, t) {
  return i === t ? "_index_" : "_value_";
}
function Oo(i) {
  if (i === "x" || i === "y" || i === "r")
    return i;
}
function wu(i) {
  if (i === "top" || i === "bottom")
    return "x";
  if (i === "left" || i === "right")
    return "y";
}
function Hn(i, ...t) {
  if (Oo(i))
    return i;
  for (const e of t) {
    const n = e.axis || wu(e.position) || i.length > 1 && Oo(i[0].toLowerCase());
    if (n)
      return n;
  }
  throw new Error(`Cannot determine type of '${i}' axis. Please provide 'axis' or 'position' option.`);
}
function Do(i, t, e) {
  if (e[t + "AxisID"] === i)
    return {
      axis: t
    };
}
function Tu(i, t) {
  if (t.data && t.data.datasets) {
    const e = t.data.datasets.filter((n) => n.xAxisID === i || n.yAxisID === i);
    if (e.length)
      return Do(i, "x", e[0]) || Do(i, "y", e[0]);
  }
  return {};
}
function Pu(i, t) {
  const e = _e[i.type] || {
    scales: {}
  }, n = t.scales || {}, s = Vn(i.type, t), o = /* @__PURE__ */ Object.create(null);
  return Object.keys(n).forEach((r) => {
    const a = n[r];
    if (!H(a))
      return console.error(`Invalid scale configuration for scale: ${r}`);
    if (a._proxy)
      return console.warn(`Ignoring resolver passed as options for scale: ${r}`);
    const l = Hn(r, a, Tu(r, i), Z.scales[a.type]), c = ku(l, s), h = e.scales || {};
    o[r] = Ke(/* @__PURE__ */ Object.create(null), [
      {
        axis: l
      },
      a,
      h[l],
      h[c]
    ]);
  }), i.data.datasets.forEach((r) => {
    const a = r.type || i.type, l = r.indexAxis || Vn(a, t), h = (_e[a] || {}).scales || {};
    Object.keys(h).forEach((u) => {
      const d = Su(u, l), g = r[d + "AxisID"] || d;
      o[g] = o[g] || /* @__PURE__ */ Object.create(null), Ke(o[g], [
        {
          axis: d
        },
        n[g],
        h[u]
      ]);
    });
  }), Object.keys(o).forEach((r) => {
    const a = o[r];
    Ke(a, [
      Z.scales[a.type],
      Z.scale
    ]);
  }), o;
}
function aa(i) {
  const t = i.options || (i.options = {});
  t.plugins = I(t.plugins, {}), t.scales = Pu(i, t);
}
function la(i) {
  return i = i || {}, i.datasets = i.datasets || [], i.labels = i.labels || [], i;
}
function Cu(i) {
  return i = i || {}, i.data = la(i.data), aa(i), i;
}
const Ao = /* @__PURE__ */ new Map(), ca = /* @__PURE__ */ new Set();
function Ri(i, t) {
  let e = Ao.get(i);
  return e || (e = t(), Ao.set(i, e), ca.add(e)), e;
}
const Ye = (i, t, e) => {
  const n = ee(t, e);
  n !== void 0 && i.add(n);
};
class Ou {
  constructor(t) {
    this._config = Cu(t), this._scopeCache = /* @__PURE__ */ new Map(), this._resolverCache = /* @__PURE__ */ new Map();
  }
  get platform() {
    return this._config.platform;
  }
  get type() {
    return this._config.type;
  }
  set type(t) {
    this._config.type = t;
  }
  get data() {
    return this._config.data;
  }
  set data(t) {
    this._config.data = la(t);
  }
  get options() {
    return this._config.options;
  }
  set options(t) {
    this._config.options = t;
  }
  get plugins() {
    return this._config.plugins;
  }
  update() {
    const t = this._config;
    this.clearCache(), aa(t);
  }
  clearCache() {
    this._scopeCache.clear(), this._resolverCache.clear();
  }
  datasetScopeKeys(t) {
    return Ri(t, () => [
      [
        `datasets.${t}`,
        ""
      ]
    ]);
  }
  datasetAnimationScopeKeys(t, e) {
    return Ri(`${t}.transition.${e}`, () => [
      [
        `datasets.${t}.transitions.${e}`,
        `transitions.${e}`
      ],
      [
        `datasets.${t}`,
        ""
      ]
    ]);
  }
  datasetElementScopeKeys(t, e) {
    return Ri(`${t}-${e}`, () => [
      [
        `datasets.${t}.elements.${e}`,
        `datasets.${t}`,
        `elements.${e}`,
        ""
      ]
    ]);
  }
  pluginScopeKeys(t) {
    const e = t.id, n = this.type;
    return Ri(`${n}-plugin-${e}`, () => [
      [
        `plugins.${e}`,
        ...t.additionalOptionScopes || []
      ]
    ]);
  }
  _cachedScopes(t, e) {
    const n = this._scopeCache;
    let s = n.get(t);
    return (!s || e) && (s = /* @__PURE__ */ new Map(), n.set(t, s)), s;
  }
  getOptionScopes(t, e, n) {
    const { options: s, type: o } = this, r = this._cachedScopes(t, n), a = r.get(e);
    if (a)
      return a;
    const l = /* @__PURE__ */ new Set();
    e.forEach((h) => {
      t && (l.add(t), h.forEach((u) => Ye(l, t, u))), h.forEach((u) => Ye(l, s, u)), h.forEach((u) => Ye(l, _e[o] || {}, u)), h.forEach((u) => Ye(l, Z, u)), h.forEach((u) => Ye(l, zn, u));
    });
    const c = Array.from(l);
    return c.length === 0 && c.push(/* @__PURE__ */ Object.create(null)), ca.has(e) && r.set(e, c), c;
  }
  chartOptionScopes() {
    const { options: t, type: e } = this;
    return [
      t,
      _e[e] || {},
      Z.datasets[e] || {},
      {
        type: e
      },
      Z,
      zn
    ];
  }
  resolveNamedOptions(t, e, n, s = [
    ""
  ]) {
    const o = {
      $shared: !0
    }, { resolver: r, subPrefixes: a } = Eo(this._resolverCache, t, s);
    let l = r;
    if (Au(r, e)) {
      o.$shared = !1, n = ie(n) ? n() : n;
      const c = this.createResolver(t, n, a);
      l = De(r, n, c);
    }
    for (const c of e)
      o[c] = l[c];
    return o;
  }
  createResolver(t, e, n = [
    ""
  ], s) {
    const { resolver: o } = Eo(this._resolverCache, t, n);
    return H(e) ? De(o, e, void 0, s) : o;
  }
}
function Eo(i, t, e) {
  let n = i.get(t);
  n || (n = /* @__PURE__ */ new Map(), i.set(t, n));
  const s = e.join();
  let o = n.get(s);
  return o || (o = {
    resolver: ds(t, e),
    subPrefixes: e.filter((a) => !a.toLowerCase().includes("hover"))
  }, n.set(s, o)), o;
}
const Du = (i) => H(i) && Object.getOwnPropertyNames(i).some((t) => ie(i[t]));
function Au(i, t) {
  const { isScriptable: e, isIndexable: n } = Hr(i);
  for (const s of t) {
    const o = e(s), r = n(s), a = (r || o) && i[s];
    if (o && (ie(a) || Du(a)) || r && q(a))
      return !0;
  }
  return !1;
}
var Eu = "4.5.1";
const Ru = [
  "top",
  "bottom",
  "left",
  "right",
  "chartArea"
];
function Ro(i, t) {
  return i === "top" || i === "bottom" || Ru.indexOf(i) === -1 && t === "x";
}
function Lo(i, t) {
  return function(e, n) {
    return e[i] === n[i] ? e[t] - n[t] : e[i] - n[i];
  };
}
function Io(i) {
  const t = i.chart, e = t.options.animation;
  t.notifyPlugins("afterRender"), L(e && e.onComplete, [
    i
  ], t);
}
function Lu(i) {
  const t = i.chart, e = t.options.animation;
  L(e && e.onProgress, [
    i
  ], t);
}
function ha(i) {
  return ps() && typeof i == "string" ? i = document.getElementById(i) : i && i.length && (i = i[0]), i && i.canvas && (i = i.canvas), i;
}
const Yi = {}, Fo = (i) => {
  const t = ha(i);
  return Object.values(Yi).filter((e) => e.canvas === t).pop();
};
function Iu(i, t, e) {
  const n = Object.keys(i);
  for (const s of n) {
    const o = +s;
    if (o >= t) {
      const r = i[s];
      delete i[s], (e > 0 || o > t) && (i[o + e] = r);
    }
  }
}
function Fu(i, t, e, n) {
  return !e || i.type === "mouseout" ? null : n ? t : i;
}
var qt;
let xs = (qt = class {
  static register(...t) {
    Rt.add(...t), zo();
  }
  static unregister(...t) {
    Rt.remove(...t), zo();
  }
  constructor(t, e) {
    const n = this.config = new Ou(e), s = ha(t), o = Fo(s);
    if (o)
      throw new Error("Canvas is already in use. Chart with ID '" + o.id + "' must be destroyed before the canvas with ID '" + o.canvas.id + "' can be reused.");
    const r = n.createResolver(n.chartOptionScopes(), this.getContext());
    this.platform = new (n.platform || tu(s))(), this.platform.updateConfig(n);
    const a = this.platform.acquireContext(s, r.aspectRatio), l = a && a.canvas, c = l && l.height, h = l && l.width;
    if (this.id = zl(), this.ctx = a, this.canvas = l, this.width = h, this.height = c, this._options = r, this._aspectRatio = this.aspectRatio, this._layers = [], this._metasets = [], this._stacks = void 0, this.boxes = [], this.currentDevicePixelRatio = void 0, this.chartArea = void 0, this._active = [], this._lastEvent = void 0, this._listeners = {}, this._responsiveListeners = void 0, this._sortedMetasets = [], this.scales = {}, this._plugins = new xu(), this.$proxies = {}, this._hiddenIndices = {}, this.attached = !1, this._animationsDisabled = void 0, this.$context = void 0, this._doResize = tc((u) => this.update(u), r.resizeDelay || 0), this._dataChanges = [], Yi[this.id] = this, !a || !l) {
      console.error("Failed to create chart: can't acquire context from the given item");
      return;
    }
    Vt.listen(this, "complete", Io), Vt.listen(this, "progress", Lu), this._initialize(), this.attached && this.update();
  }
  get aspectRatio() {
    const { options: { aspectRatio: t, maintainAspectRatio: e }, width: n, height: s, _aspectRatio: o } = this;
    return V(t) ? e && o ? o : s ? n / s : null : t;
  }
  get data() {
    return this.config.data;
  }
  set data(t) {
    this.config.data = t;
  }
  get options() {
    return this._options;
  }
  set options(t) {
    this.config.options = t;
  }
  get registry() {
    return Rt;
  }
  _initialize() {
    return this.notifyPlugins("beforeInit"), this.options.responsive ? this.resize() : so(this, this.options.devicePixelRatio), this.bindEvents(), this.notifyPlugins("afterInit"), this;
  }
  clear() {
    return eo(this.canvas, this.ctx), this;
  }
  stop() {
    return Vt.stop(this), this;
  }
  resize(t, e) {
    Vt.running(this) ? this._resizeBeforeDraw = {
      width: t,
      height: e
    } : this._resize(t, e);
  }
  _resize(t, e) {
    const n = this.options, s = this.canvas, o = n.maintainAspectRatio && this.aspectRatio, r = this.platform.getMaximumSize(s, t, e, o), a = n.devicePixelRatio || this.platform.getDevicePixelRatio(), l = this.width ? "resize" : "attach";
    this.width = r.width, this.height = r.height, this._aspectRatio = this.aspectRatio, so(this, a, !0) && (this.notifyPlugins("resize", {
      size: r
    }), L(n.onResize, [
      this,
      r
    ], this), this.attached && this._doResize(l) && this.render());
  }
  ensureScalesHaveIDs() {
    const e = this.options.scales || {};
    B(e, (n, s) => {
      n.id = s;
    });
  }
  buildOrUpdateScales() {
    const t = this.options, e = t.scales, n = this.scales, s = Object.keys(n).reduce((r, a) => (r[a] = !1, r), {});
    let o = [];
    e && (o = o.concat(Object.keys(e).map((r) => {
      const a = e[r], l = Hn(r, a), c = l === "r", h = l === "x";
      return {
        options: a,
        dposition: c ? "chartArea" : h ? "bottom" : "left",
        dtype: c ? "radialLinear" : h ? "category" : "linear"
      };
    }))), B(o, (r) => {
      const a = r.options, l = a.id, c = Hn(l, a), h = I(a.type, r.dtype);
      (a.position === void 0 || Ro(a.position, c) !== Ro(r.dposition)) && (a.position = r.dposition), s[l] = !0;
      let u = null;
      if (l in n && n[l].type === h)
        u = n[l];
      else {
        const d = Rt.getScale(h);
        u = new d({
          id: l,
          type: h,
          ctx: this.ctx,
          chart: this
        }), n[u.id] = u;
      }
      u.init(a, t);
    }), B(s, (r, a) => {
      r || delete n[a];
    }), B(n, (r) => {
      ct.configure(this, r, r.options), ct.addBox(this, r);
    });
  }
  _updateMetasets() {
    const t = this._metasets, e = this.data.datasets.length, n = t.length;
    if (t.sort((s, o) => s.index - o.index), n > e) {
      for (let s = e; s < n; ++s)
        this._destroyDatasetMeta(s);
      t.splice(e, n - e);
    }
    this._sortedMetasets = t.slice(0).sort(Lo("order", "index"));
  }
  _removeUnreferencedMetasets() {
    const { _metasets: t, data: { datasets: e } } = this;
    t.length > e.length && delete this._stacks, t.forEach((n, s) => {
      e.filter((o) => o === n._dataset).length === 0 && this._destroyDatasetMeta(s);
    });
  }
  buildOrUpdateControllers() {
    const t = [], e = this.data.datasets;
    let n, s;
    for (this._removeUnreferencedMetasets(), n = 0, s = e.length; n < s; n++) {
      const o = e[n];
      let r = this.getDatasetMeta(n);
      const a = o.type || this.config.type;
      if (r.type && r.type !== a && (this._destroyDatasetMeta(n), r = this.getDatasetMeta(n)), r.type = a, r.indexAxis = o.indexAxis || Vn(a, this.options), r.order = o.order || 0, r.index = n, r.label = "" + o.label, r.visible = this.isDatasetVisible(n), r.controller)
        r.controller.updateIndex(n), r.controller.linkScales();
      else {
        const l = Rt.getController(a), { datasetElementType: c, dataElementType: h } = Z.datasets[a];
        Object.assign(l, {
          dataElementType: Rt.getElement(h),
          datasetElementType: c && Rt.getElement(c)
        }), r.controller = new l(this, n), t.push(r.controller);
      }
    }
    return this._updateMetasets(), t;
  }
  _resetElements() {
    B(this.data.datasets, (t, e) => {
      this.getDatasetMeta(e).controller.reset();
    }, this);
  }
  reset() {
    this._resetElements(), this.notifyPlugins("reset");
  }
  update(t) {
    const e = this.config;
    e.update();
    const n = this._options = e.createResolver(e.chartOptionScopes(), this.getContext()), s = this._animationsDisabled = !n.animation;
    if (this._updateScales(), this._checkEventBindings(), this._updateHiddenIndices(), this._plugins.invalidate(), this.notifyPlugins("beforeUpdate", {
      mode: t,
      cancelable: !0
    }) === !1)
      return;
    const o = this.buildOrUpdateControllers();
    this.notifyPlugins("beforeElementsUpdate");
    let r = 0;
    for (let c = 0, h = this.data.datasets.length; c < h; c++) {
      const { controller: u } = this.getDatasetMeta(c), d = !s && o.indexOf(u) === -1;
      u.buildOrUpdateElements(d), r = Math.max(+u.getMaxOverflow(), r);
    }
    r = this._minPadding = n.layout.autoPadding ? r : 0, this._updateLayout(r), s || B(o, (c) => {
      c.reset();
    }), this._updateDatasets(t), this.notifyPlugins("afterUpdate", {
      mode: t
    }), this._layers.sort(Lo("z", "_idx"));
    const { _active: a, _lastEvent: l } = this;
    l ? this._eventHandler(l, !0) : a.length && this._updateHoverStyles(a, a, !0), this.render();
  }
  _updateScales() {
    B(this.scales, (t) => {
      ct.removeBox(this, t);
    }), this.ensureScalesHaveIDs(), this.buildOrUpdateScales();
  }
  _checkEventBindings() {
    const t = this.options, e = new Set(Object.keys(this._listeners)), n = new Set(t.events);
    (!Xs(e, n) || !!this._responsiveListeners !== t.responsive) && (this.unbindEvents(), this.bindEvents());
  }
  _updateHiddenIndices() {
    const { _hiddenIndices: t } = this, e = this._getUniformDataChanges() || [];
    for (const { method: n, start: s, count: o } of e) {
      const r = n === "_removeElements" ? -o : o;
      Iu(t, s, r);
    }
  }
  _getUniformDataChanges() {
    const t = this._dataChanges;
    if (!t || !t.length)
      return;
    this._dataChanges = [];
    const e = this.data.datasets.length, n = (o) => new Set(t.filter((r) => r[0] === o).map((r, a) => a + "," + r.splice(1).join(","))), s = n(0);
    for (let o = 1; o < e; o++)
      if (!Xs(s, n(o)))
        return;
    return Array.from(s).map((o) => o.split(",")).map((o) => ({
      method: o[1],
      start: +o[2],
      count: +o[3]
    }));
  }
  _updateLayout(t) {
    if (this.notifyPlugins("beforeLayout", {
      cancelable: !0
    }) === !1)
      return;
    ct.update(this, this.width, this.height, t);
    const e = this.chartArea, n = e.width <= 0 || e.height <= 0;
    this._layers = [], B(this.boxes, (s) => {
      n && s.position === "chartArea" || (s.configure && s.configure(), this._layers.push(...s._layers()));
    }, this), this._layers.forEach((s, o) => {
      s._idx = o;
    }), this.notifyPlugins("afterLayout");
  }
  _updateDatasets(t) {
    if (this.notifyPlugins("beforeDatasetsUpdate", {
      mode: t,
      cancelable: !0
    }) !== !1) {
      for (let e = 0, n = this.data.datasets.length; e < n; ++e)
        this.getDatasetMeta(e).controller.configure();
      for (let e = 0, n = this.data.datasets.length; e < n; ++e)
        this._updateDataset(e, ie(t) ? t({
          datasetIndex: e
        }) : t);
      this.notifyPlugins("afterDatasetsUpdate", {
        mode: t
      });
    }
  }
  _updateDataset(t, e) {
    const n = this.getDatasetMeta(t), s = {
      meta: n,
      index: t,
      mode: e,
      cancelable: !0
    };
    this.notifyPlugins("beforeDatasetUpdate", s) !== !1 && (n.controller._update(e), s.cancelable = !1, this.notifyPlugins("afterDatasetUpdate", s));
  }
  render() {
    this.notifyPlugins("beforeRender", {
      cancelable: !0
    }) !== !1 && (Vt.has(this) ? this.attached && !Vt.running(this) && Vt.start(this) : (this.draw(), Io({
      chart: this
    })));
  }
  draw() {
    let t;
    if (this._resizeBeforeDraw) {
      const { width: n, height: s } = this._resizeBeforeDraw;
      this._resizeBeforeDraw = null, this._resize(n, s);
    }
    if (this.clear(), this.width <= 0 || this.height <= 0 || this.notifyPlugins("beforeDraw", {
      cancelable: !0
    }) === !1)
      return;
    const e = this._layers;
    for (t = 0; t < e.length && e[t].z <= 0; ++t)
      e[t].draw(this.chartArea);
    for (this._drawDatasets(); t < e.length; ++t)
      e[t].draw(this.chartArea);
    this.notifyPlugins("afterDraw");
  }
  _getSortedDatasetMetas(t) {
    const e = this._sortedMetasets, n = [];
    let s, o;
    for (s = 0, o = e.length; s < o; ++s) {
      const r = e[s];
      (!t || r.visible) && n.push(r);
    }
    return n;
  }
  getSortedVisibleDatasetMetas() {
    return this._getSortedDatasetMetas(!0);
  }
  _drawDatasets() {
    if (this.notifyPlugins("beforeDatasetsDraw", {
      cancelable: !0
    }) === !1)
      return;
    const t = this.getSortedVisibleDatasetMetas();
    for (let e = t.length - 1; e >= 0; --e)
      this._drawDataset(t[e]);
    this.notifyPlugins("afterDatasetsDraw");
  }
  _drawDataset(t) {
    const e = this.ctx, n = {
      meta: t,
      index: t.index,
      cancelable: !0
    }, s = Jr(this, t);
    this.notifyPlugins("beforeDatasetDraw", n) !== !1 && (s && nn(e, s), t.controller.draw(), s && sn(e), n.cancelable = !1, this.notifyPlugins("afterDatasetDraw", n));
  }
  isPointInArea(t) {
    return It(t, this.chartArea, this._minPadding);
  }
  getElementsAtEventForMode(t, e, n, s) {
    const o = Rh.modes[e];
    return typeof o == "function" ? o(this, t, n, s) : [];
  }
  getDatasetMeta(t) {
    const e = this.data.datasets[t], n = this._metasets;
    let s = n.filter((o) => o && o._dataset === e).pop();
    return s || (s = {
      type: null,
      data: [],
      dataset: null,
      controller: null,
      hidden: null,
      xAxisID: null,
      yAxisID: null,
      order: e && e.order || 0,
      index: t,
      _dataset: e,
      _parsed: [],
      _sorted: !1
    }, n.push(s)), s;
  }
  getContext() {
    return this.$context || (this.$context = se(null, {
      chart: this,
      type: "chart"
    }));
  }
  getVisibleDatasetCount() {
    return this.getSortedVisibleDatasetMetas().length;
  }
  isDatasetVisible(t) {
    const e = this.data.datasets[t];
    if (!e)
      return !1;
    const n = this.getDatasetMeta(t);
    return typeof n.hidden == "boolean" ? !n.hidden : !e.hidden;
  }
  setDatasetVisibility(t, e) {
    const n = this.getDatasetMeta(t);
    n.hidden = !e;
  }
  toggleDataVisibility(t) {
    this._hiddenIndices[t] = !this._hiddenIndices[t];
  }
  getDataVisibility(t) {
    return !this._hiddenIndices[t];
  }
  _updateVisibility(t, e, n) {
    const s = n ? "show" : "hide", o = this.getDatasetMeta(t), r = o.controller._resolveAnimations(void 0, s);
    oi(e) ? (o.data[e].hidden = !n, this.update()) : (this.setDatasetVisibility(t, n), r.update(o, {
      visible: n
    }), this.update((a) => a.datasetIndex === t ? s : void 0));
  }
  hide(t, e) {
    this._updateVisibility(t, e, !1);
  }
  show(t, e) {
    this._updateVisibility(t, e, !0);
  }
  _destroyDatasetMeta(t) {
    const e = this._metasets[t];
    e && e.controller && e.controller._destroy(), delete this._metasets[t];
  }
  _stop() {
    let t, e;
    for (this.stop(), Vt.remove(this), t = 0, e = this.data.datasets.length; t < e; ++t)
      this._destroyDatasetMeta(t);
  }
  destroy() {
    this.notifyPlugins("beforeDestroy");
    const { canvas: t, ctx: e } = this;
    this._stop(), this.config.clearCache(), t && (this.unbindEvents(), eo(t, e), this.platform.releaseContext(e), this.canvas = null, this.ctx = null), delete Yi[this.id], this.notifyPlugins("afterDestroy");
  }
  toBase64Image(...t) {
    return this.canvas.toDataURL(...t);
  }
  bindEvents() {
    this.bindUserEvents(), this.options.responsive ? this.bindResponsiveEvents() : this.attached = !0;
  }
  bindUserEvents() {
    const t = this._listeners, e = this.platform, n = (o, r) => {
      e.addEventListener(this, o, r), t[o] = r;
    }, s = (o, r, a) => {
      o.offsetX = r, o.offsetY = a, this._eventHandler(o);
    };
    B(this.options.events, (o) => n(o, s));
  }
  bindResponsiveEvents() {
    this._responsiveListeners || (this._responsiveListeners = {});
    const t = this._responsiveListeners, e = this.platform, n = (l, c) => {
      e.addEventListener(this, l, c), t[l] = c;
    }, s = (l, c) => {
      t[l] && (e.removeEventListener(this, l, c), delete t[l]);
    }, o = (l, c) => {
      this.canvas && this.resize(l, c);
    };
    let r;
    const a = () => {
      s("attach", a), this.attached = !0, this.resize(), n("resize", o), n("detach", r);
    };
    r = () => {
      this.attached = !1, s("resize", o), this._stop(), this._resize(0, 0), n("attach", a);
    }, e.isAttached(this.canvas) ? a() : r();
  }
  unbindEvents() {
    B(this._listeners, (t, e) => {
      this.platform.removeEventListener(this, e, t);
    }), this._listeners = {}, B(this._responsiveListeners, (t, e) => {
      this.platform.removeEventListener(this, e, t);
    }), this._responsiveListeners = void 0;
  }
  updateHoverStyle(t, e, n) {
    const s = n ? "set" : "remove";
    let o, r, a, l;
    for (e === "dataset" && (o = this.getDatasetMeta(t[0].datasetIndex), o.controller["_" + s + "DatasetHoverStyle"]()), a = 0, l = t.length; a < l; ++a) {
      r = t[a];
      const c = r && this.getDatasetMeta(r.datasetIndex).controller;
      c && c[s + "HoverStyle"](r.element, r.datasetIndex, r.index);
    }
  }
  getActiveElements() {
    return this._active || [];
  }
  setActiveElements(t) {
    const e = this._active || [], n = t.map(({ datasetIndex: o, index: r }) => {
      const a = this.getDatasetMeta(o);
      if (!a)
        throw new Error("No dataset found at index " + o);
      return {
        datasetIndex: o,
        element: a.data[r],
        index: r
      };
    });
    !$i(n, e) && (this._active = n, this._lastEvent = null, this._updateHoverStyles(n, e));
  }
  notifyPlugins(t, e, n) {
    return this._plugins.notify(this, t, e, n);
  }
  isPluginEnabled(t) {
    return this._plugins._cache.filter((e) => e.plugin.id === t).length === 1;
  }
  _updateHoverStyles(t, e, n) {
    const s = this.options.hover, o = (l, c) => l.filter((h) => !c.some((u) => h.datasetIndex === u.datasetIndex && h.index === u.index)), r = o(e, t), a = n ? t : o(t, e);
    r.length && this.updateHoverStyle(r, s.mode, !1), a.length && s.mode && this.updateHoverStyle(a, s.mode, !0);
  }
  _eventHandler(t, e) {
    const n = {
      event: t,
      replay: e,
      cancelable: !0,
      inChartArea: this.isPointInArea(t)
    }, s = (r) => (r.options.events || this.options.events).includes(t.native.type);
    if (this.notifyPlugins("beforeEvent", n, s) === !1)
      return;
    const o = this._handleEvent(t, e, n.inChartArea);
    return n.cancelable = !1, this.notifyPlugins("afterEvent", n, s), (o || n.changed) && this.render(), this;
  }
  _handleEvent(t, e, n) {
    const { _active: s = [], options: o } = this, r = e, a = this._getActiveElements(t, s, n, r), l = jl(t), c = Fu(t, this._lastEvent, n, l);
    n && (this._lastEvent = null, L(o.onHover, [
      t,
      a,
      this
    ], this), l && L(o.onClick, [
      t,
      a,
      this
    ], this));
    const h = !$i(a, s);
    return (h || e) && (this._active = a, this._updateHoverStyles(a, s, e)), this._lastEvent = c, h;
  }
  _getActiveElements(t, e, n, s) {
    if (t.type === "mouseout")
      return [];
    if (!n)
      return e;
    const o = this.options.hover;
    return this.getElementsAtEventForMode(t, o.mode, o, s);
  }
}, O(qt, "defaults", Z), O(qt, "instances", Yi), O(qt, "overrides", _e), O(qt, "registry", Rt), O(qt, "version", Eu), O(qt, "getChart", Fo), qt);
function zo() {
  return B(xs.instances, (i) => i._plugins.invalidate());
}
function zu(i, t, e) {
  const { startAngle: n, x: s, y: o, outerRadius: r, innerRadius: a, options: l } = t, { borderWidth: c, borderJoinStyle: h } = l, u = Math.min(c / r, lt(n - e));
  if (i.beginPath(), i.arc(s, o, r - c / 2, n + u / 2, e - u / 2), a > 0) {
    const d = Math.min(c / a, lt(n - e));
    i.arc(s, o, a + c / 2, e - d / 2, n + d / 2, !0);
  } else {
    const d = Math.min(c / 2, r * lt(n - e));
    if (h === "round")
      i.arc(s, o, d, e - W / 2, n + W / 2, !0);
    else if (h === "bevel") {
      const g = 2 * d * d, m = -g * Math.cos(e + W / 2) + s, b = -g * Math.sin(e + W / 2) + o, _ = g * Math.cos(n + W / 2) + s, y = g * Math.sin(n + W / 2) + o;
      i.lineTo(m, b), i.lineTo(_, y);
    }
  }
  i.closePath(), i.moveTo(0, 0), i.rect(0, 0, i.canvas.width, i.canvas.height), i.clip("evenodd");
}
function Nu(i, t, e) {
  const { startAngle: n, pixelMargin: s, x: o, y: r, outerRadius: a, innerRadius: l } = t;
  let c = s / a;
  i.beginPath(), i.arc(o, r, a, n - c, e + c), l > s ? (c = s / l, i.arc(o, r, l, e + c, n - c, !0)) : i.arc(o, r, s, e + J, n - J), i.closePath(), i.clip();
}
function Bu(i) {
  return us(i, [
    "outerStart",
    "outerEnd",
    "innerStart",
    "innerEnd"
  ]);
}
function Vu(i, t, e, n) {
  const s = Bu(i.options.borderRadius), o = (e - t) / 2, r = Math.min(o, n * t / 2), a = (l) => {
    const c = (e - Math.min(o, l)) * n / 2;
    return ot(l, 0, Math.min(o, c));
  };
  return {
    outerStart: a(s.outerStart),
    outerEnd: a(s.outerEnd),
    innerStart: ot(s.innerStart, 0, r),
    innerEnd: ot(s.innerEnd, 0, r)
  };
}
function Te(i, t, e, n) {
  return {
    x: e + i * Math.cos(t),
    y: n + i * Math.sin(t)
  };
}
function Qi(i, t, e, n, s, o) {
  const { x: r, y: a, startAngle: l, pixelMargin: c, innerRadius: h } = t, u = Math.max(t.outerRadius + n + e - c, 0), d = h > 0 ? h + n + e + c : 0;
  let g = 0;
  const m = s - l;
  if (n) {
    const j = h > 0 ? h - n : 0, Y = u > 0 ? u - n : 0, $ = (j + Y) / 2, bt = $ !== 0 ? m * $ / ($ + n) : m;
    g = (m - bt) / 2;
  }
  const b = Math.max(1e-3, m * u - e / W) / u, _ = (m - b) / 2, y = l + _ + g, v = s - _ - g, { outerStart: w, outerEnd: S, innerStart: k, innerEnd: P } = Vu(t, d, u, v - y), C = u - w, D = u - S, E = y + w / C, F = v - S / D, R = d + k, z = d + P, Q = y + k / R, nt = v - P / z;
  if (i.beginPath(), o) {
    const j = (E + F) / 2;
    if (i.arc(r, a, u, E, j), i.arc(r, a, u, j, F), S > 0) {
      const et = Te(D, F, r, a);
      i.arc(et.x, et.y, S, F, v + J);
    }
    const Y = Te(z, v, r, a);
    if (i.lineTo(Y.x, Y.y), P > 0) {
      const et = Te(z, nt, r, a);
      i.arc(et.x, et.y, P, v + J, nt + Math.PI);
    }
    const $ = (v - P / d + (y + k / d)) / 2;
    if (i.arc(r, a, d, v - P / d, $, !0), i.arc(r, a, d, $, y + k / d, !0), k > 0) {
      const et = Te(R, Q, r, a);
      i.arc(et.x, et.y, k, Q + Math.PI, y - J);
    }
    const bt = Te(C, y, r, a);
    if (i.lineTo(bt.x, bt.y), w > 0) {
      const et = Te(C, E, r, a);
      i.arc(et.x, et.y, w, y - J, E);
    }
  } else {
    i.moveTo(r, a);
    const j = Math.cos(E) * u + r, Y = Math.sin(E) * u + a;
    i.lineTo(j, Y);
    const $ = Math.cos(F) * u + r, bt = Math.sin(F) * u + a;
    i.lineTo($, bt);
  }
  i.closePath();
}
function Hu(i, t, e, n, s) {
  const { fullCircles: o, startAngle: r, circumference: a } = t;
  let l = t.endAngle;
  if (o) {
    Qi(i, t, e, n, l, s);
    for (let c = 0; c < o; ++c)
      i.fill();
    isNaN(a) || (l = r + (a % X || X));
  }
  return Qi(i, t, e, n, l, s), i.fill(), l;
}
function Wu(i, t, e, n, s) {
  const { fullCircles: o, startAngle: r, circumference: a, options: l } = t, { borderWidth: c, borderJoinStyle: h, borderDash: u, borderDashOffset: d, borderRadius: g } = l, m = l.borderAlign === "inner";
  if (!c)
    return;
  i.setLineDash(u || []), i.lineDashOffset = d, m ? (i.lineWidth = c * 2, i.lineJoin = h || "round") : (i.lineWidth = c, i.lineJoin = h || "bevel");
  let b = t.endAngle;
  if (o) {
    Qi(i, t, e, n, b, s);
    for (let _ = 0; _ < o; ++_)
      i.stroke();
    isNaN(a) || (b = r + (a % X || X));
  }
  m && Nu(i, t, b), l.selfJoin && b - r >= W && g === 0 && h !== "miter" && zu(i, t, b), o || (Qi(i, t, e, n, b, s), i.stroke());
}
class qe extends Ct {
  constructor(e) {
    super();
    O(this, "circumference");
    O(this, "endAngle");
    O(this, "fullCircles");
    O(this, "innerRadius");
    O(this, "outerRadius");
    O(this, "pixelMargin");
    O(this, "startAngle");
    this.options = void 0, this.circumference = void 0, this.startAngle = void 0, this.endAngle = void 0, this.innerRadius = void 0, this.outerRadius = void 0, this.pixelMargin = 0, this.fullCircles = 0, e && Object.assign(this, e);
  }
  inRange(e, n, s) {
    const o = this.getProps([
      "x",
      "y"
    ], s), { angle: r, distance: a } = Ar(o, {
      x: e,
      y: n
    }), { startAngle: l, endAngle: c, innerRadius: h, outerRadius: u, circumference: d } = this.getProps([
      "startAngle",
      "endAngle",
      "innerRadius",
      "outerRadius",
      "circumference"
    ], s), g = (this.options.spacing + this.options.borderWidth) / 2, m = I(d, c - l), b = ri(r, l, c) && l !== c, _ = m >= X || b, y = jt(a, h + g, u + g);
    return _ && y;
  }
  getCenterPoint(e) {
    const { x: n, y: s, startAngle: o, endAngle: r, innerRadius: a, outerRadius: l } = this.getProps([
      "x",
      "y",
      "startAngle",
      "endAngle",
      "innerRadius",
      "outerRadius"
    ], e), { offset: c, spacing: h } = this.options, u = (o + r) / 2, d = (a + l + h + c) / 2;
    return {
      x: n + Math.cos(u) * d,
      y: s + Math.sin(u) * d
    };
  }
  tooltipPosition(e) {
    return this.getCenterPoint(e);
  }
  draw(e) {
    const { options: n, circumference: s } = this, o = (n.offset || 0) / 4, r = (n.spacing || 0) / 2, a = n.circular;
    if (this.pixelMargin = n.borderAlign === "inner" ? 0.33 : 0, this.fullCircles = s > X ? Math.floor(s / X) : 0, s === 0 || this.innerRadius < 0 || this.outerRadius < 0)
      return;
    e.save();
    const l = (this.startAngle + this.endAngle) / 2;
    e.translate(Math.cos(l) * o, Math.sin(l) * o);
    const c = 1 - Math.sin(Math.min(W, s || 0)), h = o * c;
    e.fillStyle = n.backgroundColor, e.strokeStyle = n.borderColor, Hu(e, this, h, r, a), Wu(e, this, h, r, a), e.restore();
  }
}
O(qe, "id", "arc"), O(qe, "defaults", {
  borderAlign: "center",
  borderColor: "#fff",
  borderDash: [],
  borderDashOffset: 0,
  borderJoinStyle: void 0,
  borderRadius: 0,
  borderWidth: 2,
  offset: 0,
  spacing: 0,
  angle: void 0,
  circular: !0,
  selfJoin: !1
}), O(qe, "defaultRoutes", {
  backgroundColor: "backgroundColor"
}), O(qe, "descriptors", {
  _scriptable: !0,
  _indexable: (e) => e !== "borderDash"
});
function ua(i, t, e = t) {
  i.lineCap = I(e.borderCapStyle, t.borderCapStyle), i.setLineDash(I(e.borderDash, t.borderDash)), i.lineDashOffset = I(e.borderDashOffset, t.borderDashOffset), i.lineJoin = I(e.borderJoinStyle, t.borderJoinStyle), i.lineWidth = I(e.borderWidth, t.borderWidth), i.strokeStyle = I(e.borderColor, t.borderColor);
}
function ju(i, t, e) {
  i.lineTo(e.x, e.y);
}
function Yu(i) {
  return i.stepped ? dc : i.tension || i.cubicInterpolationMode === "monotone" ? fc : ju;
}
function da(i, t, e = {}) {
  const n = i.length, { start: s = 0, end: o = n - 1 } = e, { start: r, end: a } = t, l = Math.max(s, r), c = Math.min(o, a), h = s < r && o < r || s > a && o > a;
  return {
    count: n,
    start: l,
    loop: t.loop,
    ilen: c < l && !h ? n + c - l : c - l
  };
}
function Uu(i, t, e, n) {
  const { points: s, options: o } = t, { count: r, start: a, loop: l, ilen: c } = da(s, e, n), h = Yu(o);
  let { move: u = !0, reverse: d } = n || {}, g, m, b;
  for (g = 0; g <= c; ++g)
    m = s[(a + (d ? c - g : g)) % r], !m.skip && (u ? (i.moveTo(m.x, m.y), u = !1) : h(i, b, m, d, o.stepped), b = m);
  return l && (m = s[(a + (d ? c : 0)) % r], h(i, b, m, d, o.stepped)), !!l;
}
function Xu(i, t, e, n) {
  const s = t.points, { count: o, start: r, ilen: a } = da(s, e, n), { move: l = !0, reverse: c } = n || {};
  let h = 0, u = 0, d, g, m, b, _, y;
  const v = (S) => (r + (c ? a - S : S)) % o, w = () => {
    b !== _ && (i.lineTo(h, _), i.lineTo(h, b), i.lineTo(h, y));
  };
  for (l && (g = s[v(0)], i.moveTo(g.x, g.y)), d = 0; d <= a; ++d) {
    if (g = s[v(d)], g.skip)
      continue;
    const S = g.x, k = g.y, P = S | 0;
    P === m ? (k < b ? b = k : k > _ && (_ = k), h = (u * h + S) / ++u) : (w(), i.lineTo(S, k), m = P, u = 0, b = _ = k), y = k;
  }
  w();
}
function Wn(i) {
  const t = i.options, e = t.borderDash && t.borderDash.length;
  return !i._decimated && !i._loop && !t.tension && t.cubicInterpolationMode !== "monotone" && !t.stepped && !e ? Xu : Uu;
}
function $u(i) {
  return i.stepped ? Uc : i.tension || i.cubicInterpolationMode === "monotone" ? Xc : ge;
}
function qu(i, t, e, n) {
  let s = t._path;
  s || (s = t._path = new Path2D(), t.path(s, e, n) && s.closePath()), ua(i, t.options), i.stroke(s);
}
function Zu(i, t, e, n) {
  const { segments: s, options: o } = t, r = Wn(t);
  for (const a of s)
    ua(i, o, a.style), i.beginPath(), r(i, t, a, {
      start: e,
      end: e + n - 1
    }) && i.closePath(), i.stroke();
}
const Gu = typeof Path2D == "function";
function Ku(i, t, e, n) {
  Gu && !t.options.segment ? qu(i, t, e, n) : Zu(i, t, e, n);
}
class Jt extends Ct {
  constructor(t) {
    super(), this.animated = !0, this.options = void 0, this._chart = void 0, this._loop = void 0, this._fullLoop = void 0, this._path = void 0, this._points = void 0, this._segments = void 0, this._decimated = !1, this._pointsUpdated = !1, this._datasetIndex = void 0, t && Object.assign(this, t);
  }
  updateControlPoints(t, e) {
    const n = this.options;
    if ((n.tension || n.cubicInterpolationMode === "monotone") && !n.stepped && !this._pointsUpdated) {
      const s = n.spanGaps ? this._loop : this._fullLoop;
      zc(this._points, n, t, s, e), this._pointsUpdated = !0;
    }
  }
  set points(t) {
    this._points = t, delete this._segments, delete this._path, this._pointsUpdated = !1;
  }
  get points() {
    return this._points;
  }
  get segments() {
    return this._segments || (this._segments = Jc(this, this.options.segment));
  }
  first() {
    const t = this.segments, e = this.points;
    return t.length && e[t[0].start];
  }
  last() {
    const t = this.segments, e = this.points, n = t.length;
    return n && e[t[n - 1].end];
  }
  interpolate(t, e) {
    const n = this.options, s = t[e], o = this.points, r = Kr(this, {
      property: e,
      start: s,
      end: s
    });
    if (!r.length)
      return;
    const a = [], l = $u(n);
    let c, h;
    for (c = 0, h = r.length; c < h; ++c) {
      const { start: u, end: d } = r[c], g = o[u], m = o[d];
      if (g === m) {
        a.push(g);
        continue;
      }
      const b = Math.abs((s - g[e]) / (m[e] - g[e])), _ = l(g, m, b, n.stepped);
      _[e] = t[e], a.push(_);
    }
    return a.length === 1 ? a[0] : a;
  }
  pathSegment(t, e, n) {
    return Wn(this)(t, this, e, n);
  }
  path(t, e, n) {
    const s = this.segments, o = Wn(this);
    let r = this._loop;
    e = e || 0, n = n || this.points.length - e;
    for (const a of s)
      r &= o(t, this, a, {
        start: e,
        end: e + n - 1
      });
    return !!r;
  }
  draw(t, e, n, s) {
    const o = this.options || {};
    (this.points || []).length && o.borderWidth && (t.save(), Ku(t, this, n, s), t.restore()), this.animated && (this._pointsUpdated = !1, this._path = void 0);
  }
}
O(Jt, "id", "line"), O(Jt, "defaults", {
  borderCapStyle: "butt",
  borderDash: [],
  borderDashOffset: 0,
  borderJoinStyle: "miter",
  borderWidth: 3,
  capBezierPoints: !0,
  cubicInterpolationMode: "default",
  fill: !1,
  spanGaps: !1,
  stepped: !1,
  tension: 0
}), O(Jt, "defaultRoutes", {
  backgroundColor: "backgroundColor",
  borderColor: "borderColor"
}), O(Jt, "descriptors", {
  _scriptable: !0,
  _indexable: (t) => t !== "borderDash" && t !== "fill"
});
function No(i, t, e, n) {
  const s = i.options, { [e]: o } = i.getProps([
    e
  ], n);
  return Math.abs(t - o) < s.radius + s.hitRadius;
}
class Ui extends Ct {
  constructor(e) {
    super();
    O(this, "parsed");
    O(this, "skip");
    O(this, "stop");
    this.options = void 0, this.parsed = void 0, this.skip = void 0, this.stop = void 0, e && Object.assign(this, e);
  }
  inRange(e, n, s) {
    const o = this.options, { x: r, y: a } = this.getProps([
      "x",
      "y"
    ], s);
    return Math.pow(e - r, 2) + Math.pow(n - a, 2) < Math.pow(o.hitRadius + o.radius, 2);
  }
  inXRange(e, n) {
    return No(this, e, "x", n);
  }
  inYRange(e, n) {
    return No(this, e, "y", n);
  }
  getCenterPoint(e) {
    const { x: n, y: s } = this.getProps([
      "x",
      "y"
    ], e);
    return {
      x: n,
      y: s
    };
  }
  size(e) {
    e = e || this.options || {};
    let n = e.radius || 0;
    n = Math.max(n, n && e.hoverRadius || 0);
    const s = n && e.borderWidth || 0;
    return (n + s) * 2;
  }
  draw(e, n) {
    const s = this.options;
    this.skip || s.radius < 0.1 || !It(this, n, this.size(s) / 2) || (e.strokeStyle = s.borderColor, e.lineWidth = s.borderWidth, e.fillStyle = s.backgroundColor, Nn(e, s, this.x, this.y));
  }
  getRange() {
    const e = this.options || {};
    return e.radius + e.hitRadius;
  }
}
O(Ui, "id", "point"), /**
* @type {any}
*/
O(Ui, "defaults", {
  borderWidth: 1,
  hitRadius: 1,
  hoverBorderWidth: 1,
  hoverRadius: 4,
  pointStyle: "circle",
  radius: 3,
  rotation: 0
}), /**
* @type {any}
*/
O(Ui, "defaultRoutes", {
  backgroundColor: "backgroundColor",
  borderColor: "borderColor"
});
function fa(i, t) {
  const { x: e, y: n, base: s, width: o, height: r } = i.getProps([
    "x",
    "y",
    "base",
    "width",
    "height"
  ], t);
  let a, l, c, h, u;
  return i.horizontal ? (u = r / 2, a = Math.min(e, s), l = Math.max(e, s), c = n - u, h = n + u) : (u = o / 2, a = e - u, l = e + u, c = Math.min(n, s), h = Math.max(n, s)), {
    left: a,
    top: c,
    right: l,
    bottom: h
  };
}
function Qt(i, t, e, n) {
  return i ? 0 : ot(t, e, n);
}
function Ju(i, t, e) {
  const n = i.options.borderWidth, s = i.borderSkipped, o = Vr(n);
  return {
    t: Qt(s.top, o.top, 0, e),
    r: Qt(s.right, o.right, 0, t),
    b: Qt(s.bottom, o.bottom, 0, e),
    l: Qt(s.left, o.left, 0, t)
  };
}
function Qu(i, t, e) {
  const { enableBorderRadius: n } = i.getProps([
    "enableBorderRadius"
  ]), s = i.options.borderRadius, o = be(s), r = Math.min(t, e), a = i.borderSkipped, l = n || H(s);
  return {
    topLeft: Qt(!l || a.top || a.left, o.topLeft, 0, r),
    topRight: Qt(!l || a.top || a.right, o.topRight, 0, r),
    bottomLeft: Qt(!l || a.bottom || a.left, o.bottomLeft, 0, r),
    bottomRight: Qt(!l || a.bottom || a.right, o.bottomRight, 0, r)
  };
}
function td(i) {
  const t = fa(i), e = t.right - t.left, n = t.bottom - t.top, s = Ju(i, e / 2, n / 2), o = Qu(i, e / 2, n / 2);
  return {
    outer: {
      x: t.left,
      y: t.top,
      w: e,
      h: n,
      radius: o
    },
    inner: {
      x: t.left + s.l,
      y: t.top + s.t,
      w: e - s.l - s.r,
      h: n - s.t - s.b,
      radius: {
        topLeft: Math.max(0, o.topLeft - Math.max(s.t, s.l)),
        topRight: Math.max(0, o.topRight - Math.max(s.t, s.r)),
        bottomLeft: Math.max(0, o.bottomLeft - Math.max(s.b, s.l)),
        bottomRight: Math.max(0, o.bottomRight - Math.max(s.b, s.r))
      }
    }
  };
}
function On(i, t, e, n) {
  const s = t === null, o = e === null, a = i && !(s && o) && fa(i, n);
  return a && (s || jt(t, a.left, a.right)) && (o || jt(e, a.top, a.bottom));
}
function ed(i) {
  return i.topLeft || i.topRight || i.bottomLeft || i.bottomRight;
}
function id(i, t) {
  i.rect(t.x, t.y, t.w, t.h);
}
function Dn(i, t, e = {}) {
  const n = i.x !== e.x ? -t : 0, s = i.y !== e.y ? -t : 0, o = (i.x + i.w !== e.x + e.w ? t : 0) - n, r = (i.y + i.h !== e.y + e.h ? t : 0) - s;
  return {
    x: i.x + n,
    y: i.y + s,
    w: i.w + o,
    h: i.h + r,
    radius: i.radius
  };
}
class Xi extends Ct {
  constructor(t) {
    super(), this.options = void 0, this.horizontal = void 0, this.base = void 0, this.width = void 0, this.height = void 0, this.inflateAmount = void 0, t && Object.assign(this, t);
  }
  draw(t) {
    const { inflateAmount: e, options: { borderColor: n, backgroundColor: s } } = this, { inner: o, outer: r } = td(this), a = ed(r.radius) ? ai : id;
    t.save(), (r.w !== o.w || r.h !== o.h) && (t.beginPath(), a(t, Dn(r, e, o)), t.clip(), a(t, Dn(o, -e, r)), t.fillStyle = n, t.fill("evenodd")), t.beginPath(), a(t, Dn(o, e)), t.fillStyle = s, t.fill(), t.restore();
  }
  inRange(t, e, n) {
    return On(this, t, e, n);
  }
  inXRange(t, e) {
    return On(this, t, null, e);
  }
  inYRange(t, e) {
    return On(this, null, t, e);
  }
  getCenterPoint(t) {
    const { x: e, y: n, base: s, horizontal: o } = this.getProps([
      "x",
      "y",
      "base",
      "horizontal"
    ], t);
    return {
      x: o ? (e + s) / 2 : e,
      y: o ? n : (n + s) / 2
    };
  }
  getRange(t) {
    return t === "x" ? this.width / 2 : this.height / 2;
  }
}
O(Xi, "id", "bar"), O(Xi, "defaults", {
  borderSkipped: "start",
  borderWidth: 0,
  borderRadius: 0,
  inflateAmount: "auto",
  pointStyle: void 0
}), O(Xi, "defaultRoutes", {
  backgroundColor: "backgroundColor",
  borderColor: "borderColor"
});
var nd = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  ArcElement: qe,
  BarElement: Xi,
  LineElement: Jt,
  PointElement: Ui
});
const jn = [
  "rgb(54, 162, 235)",
  "rgb(255, 99, 132)",
  "rgb(255, 159, 64)",
  "rgb(255, 205, 86)",
  "rgb(75, 192, 192)",
  "rgb(153, 102, 255)",
  "rgb(201, 203, 207)"
  // grey
], Bo = /* @__PURE__ */ jn.map((i) => i.replace("rgb(", "rgba(").replace(")", ", 0.5)"));
function ga(i) {
  return jn[i % jn.length];
}
function pa(i) {
  return Bo[i % Bo.length];
}
function sd(i, t) {
  return i.borderColor = ga(t), i.backgroundColor = pa(t), ++t;
}
function od(i, t) {
  return i.backgroundColor = i.data.map(() => ga(t++)), t;
}
function rd(i, t) {
  return i.backgroundColor = i.data.map(() => pa(t++)), t;
}
function ad(i) {
  let t = 0;
  return (e, n) => {
    const s = i.getDatasetMeta(n).controller;
    s instanceof pe ? t = od(e, t) : s instanceof ti ? t = rd(e, t) : s && (t = sd(e, t));
  };
}
function Vo(i) {
  let t;
  for (t in i)
    if (i[t].borderColor || i[t].backgroundColor)
      return !0;
  return !1;
}
function ld(i) {
  return i && (i.borderColor || i.backgroundColor);
}
function cd() {
  return Z.borderColor !== "rgba(0,0,0,0.1)" || Z.backgroundColor !== "rgba(0,0,0,0.1)";
}
var hd = {
  id: "colors",
  defaults: {
    enabled: !0,
    forceOverride: !1
  },
  beforeLayout(i, t, e) {
    if (!e.enabled)
      return;
    const { data: { datasets: n }, options: s } = i.config, { elements: o } = s, r = Vo(n) || ld(s) || o && Vo(o) || cd();
    if (!e.forceOverride && r)
      return;
    const a = ad(i);
    n.forEach(a);
  }
};
function ud(i, t, e, n, s) {
  const o = s.samples || n;
  if (o >= e)
    return i.slice(t, t + e);
  const r = [], a = (e - 2) / (o - 2);
  let l = 0;
  const c = t + e - 1;
  let h = t, u, d, g, m, b;
  for (r[l++] = i[h], u = 0; u < o - 2; u++) {
    let _ = 0, y = 0, v;
    const w = Math.floor((u + 1) * a) + 1 + t, S = Math.min(Math.floor((u + 2) * a) + 1, e) + t, k = S - w;
    for (v = w; v < S; v++)
      _ += i[v].x, y += i[v].y;
    _ /= k, y /= k;
    const P = Math.floor(u * a) + 1 + t, C = Math.min(Math.floor((u + 1) * a) + 1, e) + t, { x: D, y: E } = i[h];
    for (g = m = -1, v = P; v < C; v++)
      m = 0.5 * Math.abs((D - _) * (i[v].y - E) - (D - i[v].x) * (y - E)), m > g && (g = m, d = i[v], b = v);
    r[l++] = d, h = b;
  }
  return r[l++] = i[c], r;
}
function dd(i, t, e, n) {
  let s = 0, o = 0, r, a, l, c, h, u, d, g, m, b;
  const _ = [], y = t + e - 1, v = i[t].x, S = i[y].x - v;
  for (r = t; r < t + e; ++r) {
    a = i[r], l = (a.x - v) / S * n, c = a.y;
    const k = l | 0;
    if (k === h)
      c < m ? (m = c, u = r) : c > b && (b = c, d = r), s = (o * s + a.x) / ++o;
    else {
      const P = r - 1;
      if (!V(u) && !V(d)) {
        const C = Math.min(u, d), D = Math.max(u, d);
        C !== g && C !== P && _.push({
          ...i[C],
          x: s
        }), D !== g && D !== P && _.push({
          ...i[D],
          x: s
        });
      }
      r > 0 && P !== g && _.push(i[P]), _.push(a), h = k, o = 0, m = b = c, u = d = g = r;
    }
  }
  return _;
}
function ma(i) {
  if (i._decimated) {
    const t = i._data;
    delete i._decimated, delete i._data, Object.defineProperty(i, "data", {
      configurable: !0,
      enumerable: !0,
      writable: !0,
      value: t
    });
  }
}
function Ho(i) {
  i.data.datasets.forEach((t) => {
    ma(t);
  });
}
function fd(i, t) {
  const e = t.length;
  let n = 0, s;
  const { iScale: o } = i, { min: r, max: a, minDefined: l, maxDefined: c } = o.getUserBounds();
  return l && (n = ot(Yt(t, o.axis, r).lo, 0, e - 1)), c ? s = ot(Yt(t, o.axis, a).hi + 1, n, e) - n : s = e - n, {
    start: n,
    count: s
  };
}
var gd = {
  id: "decimation",
  defaults: {
    algorithm: "min-max",
    enabled: !1
  },
  beforeElementsUpdate: (i, t, e) => {
    if (!e.enabled) {
      Ho(i);
      return;
    }
    const n = i.width;
    i.data.datasets.forEach((s, o) => {
      const { _data: r, indexAxis: a } = s, l = i.getDatasetMeta(o), c = r || s.data;
      if (Xe([
        a,
        i.options.indexAxis
      ]) === "y" || !l.controller.supportsDecimation)
        return;
      const h = i.scales[l.xAxisID];
      if (h.type !== "linear" && h.type !== "time" || i.options.parsing)
        return;
      let { start: u, count: d } = fd(l, c);
      const g = e.threshold || 4 * n;
      if (d <= g) {
        ma(s);
        return;
      }
      V(r) && (s._data = c, delete s.data, Object.defineProperty(s, "data", {
        configurable: !0,
        enumerable: !0,
        get: function() {
          return this._decimated;
        },
        set: function(b) {
          this._data = b;
        }
      }));
      let m;
      switch (e.algorithm) {
        case "lttb":
          m = ud(c, u, d, n, e);
          break;
        case "min-max":
          m = dd(c, u, d, n);
          break;
        default:
          throw new Error(`Unsupported decimation algorithm '${e.algorithm}'`);
      }
      s._decimated = m;
    });
  },
  destroy(i) {
    Ho(i);
  }
};
function pd(i, t, e) {
  const n = i.segments, s = i.points, o = t.points, r = [];
  for (const a of n) {
    let { start: l, end: c } = a;
    c = an(l, c, s);
    const h = Yn(e, s[l], s[c], a.loop);
    if (!t.segments) {
      r.push({
        source: a,
        target: h,
        start: s[l],
        end: s[c]
      });
      continue;
    }
    const u = Kr(t, h);
    for (const d of u) {
      const g = Yn(e, o[d.start], o[d.end], d.loop), m = Gr(a, s, g);
      for (const b of m)
        r.push({
          source: b,
          target: d,
          start: {
            [e]: Wo(h, g, "start", Math.max)
          },
          end: {
            [e]: Wo(h, g, "end", Math.min)
          }
        });
    }
  }
  return r;
}
function Yn(i, t, e, n) {
  if (n)
    return;
  let s = t[i], o = e[i];
  return i === "angle" && (s = lt(s), o = lt(o)), {
    property: i,
    start: s,
    end: o
  };
}
function md(i, t) {
  const { x: e = null, y: n = null } = i || {}, s = t.points, o = [];
  return t.segments.forEach(({ start: r, end: a }) => {
    a = an(r, a, s);
    const l = s[r], c = s[a];
    n !== null ? (o.push({
      x: l.x,
      y: n
    }), o.push({
      x: c.x,
      y: n
    })) : e !== null && (o.push({
      x: e,
      y: l.y
    }), o.push({
      x: e,
      y: c.y
    }));
  }), o;
}
function an(i, t, e) {
  for (; t > i; t--) {
    const n = e[t];
    if (!isNaN(n.x) && !isNaN(n.y))
      break;
  }
  return t;
}
function Wo(i, t, e, n) {
  return i && t ? n(i[e], t[e]) : i ? i[e] : t ? t[e] : 0;
}
function ba(i, t) {
  let e = [], n = !1;
  return q(i) ? (n = !0, e = i) : e = md(i, t), e.length ? new Jt({
    points: e,
    options: {
      tension: 0
    },
    _loop: n,
    _fullLoop: n
  }) : null;
}
function jo(i) {
  return i && i.fill !== !1;
}
function bd(i, t, e) {
  let s = i[t].fill;
  const o = [
    t
  ];
  let r;
  if (!e)
    return s;
  for (; s !== !1 && o.indexOf(s) === -1; ) {
    if (!K(s))
      return s;
    if (r = i[s], !r)
      return !1;
    if (r.visible)
      return s;
    o.push(s), s = r.fill;
  }
  return !1;
}
function xd(i, t, e) {
  const n = Md(i);
  if (H(n))
    return isNaN(n.value) ? !1 : n;
  let s = parseFloat(n);
  return K(s) && Math.floor(s) === s ? _d(n[0], t, s, e) : [
    "origin",
    "start",
    "end",
    "stack",
    "shape"
  ].indexOf(n) >= 0 && n;
}
function _d(i, t, e, n) {
  return (i === "-" || i === "+") && (e = t + e), e === t || e < 0 || e >= n ? !1 : e;
}
function yd(i, t) {
  let e = null;
  return i === "start" ? e = t.bottom : i === "end" ? e = t.top : H(i) ? e = t.getPixelForValue(i.value) : t.getBasePixel && (e = t.getBasePixel()), e;
}
function vd(i, t, e) {
  let n;
  return i === "start" ? n = e : i === "end" ? n = t.options.reverse ? t.min : t.max : H(i) ? n = i.value : n = t.getBaseValue(), n;
}
function Md(i) {
  const t = i.options, e = t.fill;
  let n = I(e && e.target, e);
  return n === void 0 && (n = !!t.backgroundColor), n === !1 || n === null ? !1 : n === !0 ? "origin" : n;
}
function Sd(i) {
  const { scale: t, index: e, line: n } = i, s = [], o = n.segments, r = n.points, a = kd(t, e);
  a.push(ba({
    x: null,
    y: t.bottom
  }, n));
  for (let l = 0; l < o.length; l++) {
    const c = o[l];
    for (let h = c.start; h <= c.end; h++)
      wd(s, r[h], a);
  }
  return new Jt({
    points: s,
    options: {}
  });
}
function kd(i, t) {
  const e = [], n = i.getMatchingVisibleMetas("line");
  for (let s = 0; s < n.length; s++) {
    const o = n[s];
    if (o.index === t)
      break;
    o.hidden || e.unshift(o.dataset);
  }
  return e;
}
function wd(i, t, e) {
  const n = [];
  for (let s = 0; s < e.length; s++) {
    const o = e[s], { first: r, last: a, point: l } = Td(o, t, "x");
    if (!(!l || r && a)) {
      if (r)
        n.unshift(l);
      else if (i.push(l), !a)
        break;
    }
  }
  i.push(...n);
}
function Td(i, t, e) {
  const n = i.interpolate(t, e);
  if (!n)
    return {};
  const s = n[e], o = i.segments, r = i.points;
  let a = !1, l = !1;
  for (let c = 0; c < o.length; c++) {
    const h = o[c], u = r[h.start][e], d = r[h.end][e];
    if (jt(s, u, d)) {
      a = s === u, l = s === d;
      break;
    }
  }
  return {
    first: a,
    last: l,
    point: n
  };
}
class xa {
  constructor(t) {
    this.x = t.x, this.y = t.y, this.radius = t.radius;
  }
  pathSegment(t, e, n) {
    const { x: s, y: o, radius: r } = this;
    return e = e || {
      start: 0,
      end: X
    }, t.arc(s, o, r, e.end, e.start, !0), !n.bounds;
  }
  interpolate(t) {
    const { x: e, y: n, radius: s } = this, o = t.angle;
    return {
      x: e + Math.cos(o) * s,
      y: n + Math.sin(o) * s,
      angle: o
    };
  }
}
function Pd(i) {
  const { chart: t, fill: e, line: n } = i;
  if (K(e))
    return Cd(t, e);
  if (e === "stack")
    return Sd(i);
  if (e === "shape")
    return !0;
  const s = Od(i);
  return s instanceof xa ? s : ba(s, n);
}
function Cd(i, t) {
  const e = i.getDatasetMeta(t);
  return e && i.isDatasetVisible(t) ? e.dataset : null;
}
function Od(i) {
  return (i.scale || {}).getPointPositionForValue ? Ad(i) : Dd(i);
}
function Dd(i) {
  const { scale: t = {}, fill: e } = i, n = yd(e, t);
  if (K(n)) {
    const s = t.isHorizontal();
    return {
      x: s ? n : null,
      y: s ? null : n
    };
  }
  return null;
}
function Ad(i) {
  const { scale: t, fill: e } = i, n = t.options, s = t.getLabels().length, o = n.reverse ? t.max : t.min, r = vd(e, t, o), a = [];
  if (n.grid.circular) {
    const l = t.getPointPositionForValue(0, o);
    return new xa({
      x: l.x,
      y: l.y,
      radius: t.getDistanceFromCenterForValue(r)
    });
  }
  for (let l = 0; l < s; ++l)
    a.push(t.getPointPositionForValue(l, r));
  return a;
}
function An(i, t, e) {
  const n = Pd(t), { chart: s, index: o, line: r, scale: a, axis: l } = t, c = r.options, h = c.fill, u = c.backgroundColor, { above: d = u, below: g = u } = h || {}, m = s.getDatasetMeta(o), b = Jr(s, m);
  n && r.points.length && (nn(i, e), Ed(i, {
    line: r,
    target: n,
    above: d,
    below: g,
    area: e,
    scale: a,
    axis: l,
    clip: b
  }), sn(i));
}
function Ed(i, t) {
  const { line: e, target: n, above: s, below: o, area: r, scale: a, clip: l } = t, c = e._loop ? "angle" : t.axis;
  i.save();
  let h = o;
  o !== s && (c === "x" ? (Yo(i, n, r.top), En(i, {
    line: e,
    target: n,
    color: s,
    scale: a,
    property: c,
    clip: l
  }), i.restore(), i.save(), Yo(i, n, r.bottom)) : c === "y" && (Uo(i, n, r.left), En(i, {
    line: e,
    target: n,
    color: o,
    scale: a,
    property: c,
    clip: l
  }), i.restore(), i.save(), Uo(i, n, r.right), h = s)), En(i, {
    line: e,
    target: n,
    color: h,
    scale: a,
    property: c,
    clip: l
  }), i.restore();
}
function Yo(i, t, e) {
  const { segments: n, points: s } = t;
  let o = !0, r = !1;
  i.beginPath();
  for (const a of n) {
    const { start: l, end: c } = a, h = s[l], u = s[an(l, c, s)];
    o ? (i.moveTo(h.x, h.y), o = !1) : (i.lineTo(h.x, e), i.lineTo(h.x, h.y)), r = !!t.pathSegment(i, a, {
      move: r
    }), r ? i.closePath() : i.lineTo(u.x, e);
  }
  i.lineTo(t.first().x, e), i.closePath(), i.clip();
}
function Uo(i, t, e) {
  const { segments: n, points: s } = t;
  let o = !0, r = !1;
  i.beginPath();
  for (const a of n) {
    const { start: l, end: c } = a, h = s[l], u = s[an(l, c, s)];
    o ? (i.moveTo(h.x, h.y), o = !1) : (i.lineTo(e, h.y), i.lineTo(h.x, h.y)), r = !!t.pathSegment(i, a, {
      move: r
    }), r ? i.closePath() : i.lineTo(e, u.y);
  }
  i.lineTo(e, t.first().y), i.closePath(), i.clip();
}
function En(i, t) {
  const { line: e, target: n, property: s, color: o, scale: r, clip: a } = t, l = pd(e, n, s);
  for (const { source: c, target: h, start: u, end: d } of l) {
    const { style: { backgroundColor: g = o } = {} } = c, m = n !== !0;
    i.save(), i.fillStyle = g, Rd(i, r, a, m && Yn(s, u, d)), i.beginPath();
    const b = !!e.pathSegment(i, c);
    let _;
    if (m) {
      b ? i.closePath() : Xo(i, n, d, s);
      const y = !!n.pathSegment(i, h, {
        move: b,
        reverse: !0
      });
      _ = b && y, _ || Xo(i, n, u, s);
    }
    i.closePath(), i.fill(_ ? "evenodd" : "nonzero"), i.restore();
  }
}
function Rd(i, t, e, n) {
  const s = t.chart.chartArea, { property: o, start: r, end: a } = n || {};
  if (o === "x" || o === "y") {
    let l, c, h, u;
    o === "x" ? (l = r, c = s.top, h = a, u = s.bottom) : (l = s.left, c = r, h = s.right, u = a), i.beginPath(), e && (l = Math.max(l, e.left), h = Math.min(h, e.right), c = Math.max(c, e.top), u = Math.min(u, e.bottom)), i.rect(l, c, h - l, u - c), i.clip();
  }
}
function Xo(i, t, e, n) {
  const s = t.interpolate(e, n);
  s && i.lineTo(s.x, s.y);
}
var Ld = {
  id: "filler",
  afterDatasetsUpdate(i, t, e) {
    const n = (i.data.datasets || []).length, s = [];
    let o, r, a, l;
    for (r = 0; r < n; ++r)
      o = i.getDatasetMeta(r), a = o.dataset, l = null, a && a.options && a instanceof Jt && (l = {
        visible: i.isDatasetVisible(r),
        index: r,
        fill: xd(a, r, n),
        chart: i,
        axis: o.controller.options.indexAxis,
        scale: o.vScale,
        line: a
      }), o.$filler = l, s.push(l);
    for (r = 0; r < n; ++r)
      l = s[r], !(!l || l.fill === !1) && (l.fill = bd(s, r, e.propagate));
  },
  beforeDraw(i, t, e) {
    const n = e.drawTime === "beforeDraw", s = i.getSortedVisibleDatasetMetas(), o = i.chartArea;
    for (let r = s.length - 1; r >= 0; --r) {
      const a = s[r].$filler;
      a && (a.line.updateControlPoints(o, a.axis), n && a.fill && An(i.ctx, a, o));
    }
  },
  beforeDatasetsDraw(i, t, e) {
    if (e.drawTime !== "beforeDatasetsDraw")
      return;
    const n = i.getSortedVisibleDatasetMetas();
    for (let s = n.length - 1; s >= 0; --s) {
      const o = n[s].$filler;
      jo(o) && An(i.ctx, o, i.chartArea);
    }
  },
  beforeDatasetDraw(i, t, e) {
    const n = t.meta.$filler;
    !jo(n) || e.drawTime !== "beforeDatasetDraw" || An(i.ctx, n, i.chartArea);
  },
  defaults: {
    propagate: !0,
    drawTime: "beforeDatasetDraw"
  }
};
const $o = (i, t) => {
  let { boxHeight: e = t, boxWidth: n = t } = i;
  return i.usePointStyle && (e = Math.min(e, t), n = i.pointStyleWidth || Math.min(n, t)), {
    boxWidth: n,
    boxHeight: e,
    itemHeight: Math.max(t, e)
  };
}, Id = (i, t) => i !== null && t !== null && i.datasetIndex === t.datasetIndex && i.index === t.index;
class qo extends Ct {
  constructor(t) {
    super(), this._added = !1, this.legendHitBoxes = [], this._hoveredItem = null, this.doughnutMode = !1, this.chart = t.chart, this.options = t.options, this.ctx = t.ctx, this.legendItems = void 0, this.columnSizes = void 0, this.lineWidths = void 0, this.maxHeight = void 0, this.maxWidth = void 0, this.top = void 0, this.bottom = void 0, this.left = void 0, this.right = void 0, this.height = void 0, this.width = void 0, this._margins = void 0, this.position = void 0, this.weight = void 0, this.fullSize = void 0;
  }
  update(t, e, n) {
    this.maxWidth = t, this.maxHeight = e, this._margins = n, this.setDimensions(), this.buildLabels(), this.fit();
  }
  setDimensions() {
    this.isHorizontal() ? (this.width = this.maxWidth, this.left = this._margins.left, this.right = this.width) : (this.height = this.maxHeight, this.top = this._margins.top, this.bottom = this.height);
  }
  buildLabels() {
    const t = this.options.labels || {};
    let e = L(t.generateLabels, [
      this.chart
    ], this) || [];
    t.filter && (e = e.filter((n) => t.filter(n, this.chart.data))), t.sort && (e = e.sort((n, s) => t.sort(n, s, this.chart.data))), this.options.reverse && e.reverse(), this.legendItems = e;
  }
  fit() {
    const { options: t, ctx: e } = this;
    if (!t.display) {
      this.width = this.height = 0;
      return;
    }
    const n = t.labels, s = it(n.font), o = s.size, r = this._computeTitleHeight(), { boxWidth: a, itemHeight: l } = $o(n, o);
    let c, h;
    e.font = s.string, this.isHorizontal() ? (c = this.maxWidth, h = this._fitRows(r, o, a, l) + 10) : (h = this.maxHeight, c = this._fitCols(r, s, a, l) + 10), this.width = Math.min(c, t.maxWidth || this.maxWidth), this.height = Math.min(h, t.maxHeight || this.maxHeight);
  }
  _fitRows(t, e, n, s) {
    const { ctx: o, maxWidth: r, options: { labels: { padding: a } } } = this, l = this.legendHitBoxes = [], c = this.lineWidths = [
      0
    ], h = s + a;
    let u = t;
    o.textAlign = "left", o.textBaseline = "middle";
    let d = -1, g = -h;
    return this.legendItems.forEach((m, b) => {
      const _ = n + e / 2 + o.measureText(m.text).width;
      (b === 0 || c[c.length - 1] + _ + 2 * a > r) && (u += h, c[c.length - (b > 0 ? 0 : 1)] = 0, g += h, d++), l[b] = {
        left: 0,
        top: g,
        row: d,
        width: _,
        height: s
      }, c[c.length - 1] += _ + a;
    }), u;
  }
  _fitCols(t, e, n, s) {
    const { ctx: o, maxHeight: r, options: { labels: { padding: a } } } = this, l = this.legendHitBoxes = [], c = this.columnSizes = [], h = r - t;
    let u = a, d = 0, g = 0, m = 0, b = 0;
    return this.legendItems.forEach((_, y) => {
      const { itemWidth: v, itemHeight: w } = Fd(n, e, o, _, s);
      y > 0 && g + w + 2 * a > h && (u += d + a, c.push({
        width: d,
        height: g
      }), m += d + a, b++, d = g = 0), l[y] = {
        left: m,
        top: g,
        col: b,
        width: v,
        height: w
      }, d = Math.max(d, v), g += w + a;
    }), u += d, c.push({
      width: d,
      height: g
    }), u;
  }
  adjustHitBoxes() {
    if (!this.options.display)
      return;
    const t = this._computeTitleHeight(), { legendHitBoxes: e, options: { align: n, labels: { padding: s }, rtl: o } } = this, r = Ce(o, this.left, this.width);
    if (this.isHorizontal()) {
      let a = 0, l = at(n, this.left + s, this.right - this.lineWidths[a]);
      for (const c of e)
        a !== c.row && (a = c.row, l = at(n, this.left + s, this.right - this.lineWidths[a])), c.top += this.top + t + s, c.left = r.leftForLtr(r.x(l), c.width), l += c.width + s;
    } else {
      let a = 0, l = at(n, this.top + t + s, this.bottom - this.columnSizes[a].height);
      for (const c of e)
        c.col !== a && (a = c.col, l = at(n, this.top + t + s, this.bottom - this.columnSizes[a].height)), c.top = l, c.left += this.left + s, c.left = r.leftForLtr(r.x(c.left), c.width), l += c.height + s;
    }
  }
  isHorizontal() {
    return this.options.position === "top" || this.options.position === "bottom";
  }
  draw() {
    if (this.options.display) {
      const t = this.ctx;
      nn(t, this), this._draw(), sn(t);
    }
  }
  _draw() {
    const { options: t, columnSizes: e, lineWidths: n, ctx: s } = this, { align: o, labels: r } = t, a = Z.color, l = Ce(t.rtl, this.left, this.width), c = it(r.font), { padding: h } = r, u = c.size, d = u / 2;
    let g;
    this.drawTitle(), s.textAlign = l.textAlign("left"), s.textBaseline = "middle", s.lineWidth = 0.5, s.font = c.string;
    const { boxWidth: m, boxHeight: b, itemHeight: _ } = $o(r, u), y = function(P, C, D) {
      if (isNaN(m) || m <= 0 || isNaN(b) || b < 0)
        return;
      s.save();
      const E = I(D.lineWidth, 1);
      if (s.fillStyle = I(D.fillStyle, a), s.lineCap = I(D.lineCap, "butt"), s.lineDashOffset = I(D.lineDashOffset, 0), s.lineJoin = I(D.lineJoin, "miter"), s.lineWidth = E, s.strokeStyle = I(D.strokeStyle, a), s.setLineDash(I(D.lineDash, [])), r.usePointStyle) {
        const F = {
          radius: b * Math.SQRT2 / 2,
          pointStyle: D.pointStyle,
          rotation: D.rotation,
          borderWidth: E
        }, R = l.xPlus(P, m / 2), z = C + d;
        Br(s, F, R, z, r.pointStyleWidth && m);
      } else {
        const F = C + Math.max((u - b) / 2, 0), R = l.leftForLtr(P, m), z = be(D.borderRadius);
        s.beginPath(), Object.values(z).some((Q) => Q !== 0) ? ai(s, {
          x: R,
          y: F,
          w: m,
          h: b,
          radius: z
        }) : s.rect(R, F, m, b), s.fill(), E !== 0 && s.stroke();
      }
      s.restore();
    }, v = function(P, C, D) {
      ye(s, D.text, P, C + _ / 2, c, {
        strikethrough: D.hidden,
        textAlign: l.textAlign(D.textAlign)
      });
    }, w = this.isHorizontal(), S = this._computeTitleHeight();
    w ? g = {
      x: at(o, this.left + h, this.right - n[0]),
      y: this.top + h + S,
      line: 0
    } : g = {
      x: this.left + h,
      y: at(o, this.top + S + h, this.bottom - e[0].height),
      line: 0
    }, $r(this.ctx, t.textDirection);
    const k = _ + h;
    this.legendItems.forEach((P, C) => {
      s.strokeStyle = P.fontColor, s.fillStyle = P.fontColor;
      const D = s.measureText(P.text).width, E = l.textAlign(P.textAlign || (P.textAlign = r.textAlign)), F = m + d + D;
      let R = g.x, z = g.y;
      l.setWidth(this.width), w ? C > 0 && R + F + h > this.right && (z = g.y += k, g.line++, R = g.x = at(o, this.left + h, this.right - n[g.line])) : C > 0 && z + k > this.bottom && (R = g.x = R + e[g.line].width + h, g.line++, z = g.y = at(o, this.top + S + h, this.bottom - e[g.line].height));
      const Q = l.x(R);
      if (y(Q, z, P), R = ec(E, R + m + d, w ? R + F : this.right, t.rtl), v(l.x(R), z, P), w)
        g.x += F + h;
      else if (typeof P.text != "string") {
        const nt = c.lineHeight;
        g.y += _a(P, nt) + h;
      } else
        g.y += k;
    }), qr(this.ctx, t.textDirection);
  }
  drawTitle() {
    const t = this.options, e = t.title, n = it(e.font), s = ht(e.padding);
    if (!e.display)
      return;
    const o = Ce(t.rtl, this.left, this.width), r = this.ctx, a = e.position, l = n.size / 2, c = s.top + l;
    let h, u = this.left, d = this.width;
    if (this.isHorizontal())
      d = Math.max(...this.lineWidths), h = this.top + c, u = at(t.align, u, this.right - d);
    else {
      const m = this.columnSizes.reduce((b, _) => Math.max(b, _.height), 0);
      h = c + at(t.align, this.top, this.bottom - m - t.labels.padding - this._computeTitleHeight());
    }
    const g = at(a, u, u + d);
    r.textAlign = o.textAlign(cs(a)), r.textBaseline = "middle", r.strokeStyle = e.color, r.fillStyle = e.color, r.font = n.string, ye(r, e.text, g, h, n);
  }
  _computeTitleHeight() {
    const t = this.options.title, e = it(t.font), n = ht(t.padding);
    return t.display ? e.lineHeight + n.height : 0;
  }
  _getLegendItemAt(t, e) {
    let n, s, o;
    if (jt(t, this.left, this.right) && jt(e, this.top, this.bottom)) {
      for (o = this.legendHitBoxes, n = 0; n < o.length; ++n)
        if (s = o[n], jt(t, s.left, s.left + s.width) && jt(e, s.top, s.top + s.height))
          return this.legendItems[n];
    }
    return null;
  }
  handleEvent(t) {
    const e = this.options;
    if (!Bd(t.type, e))
      return;
    const n = this._getLegendItemAt(t.x, t.y);
    if (t.type === "mousemove" || t.type === "mouseout") {
      const s = this._hoveredItem, o = Id(s, n);
      s && !o && L(e.onLeave, [
        t,
        s,
        this
      ], this), this._hoveredItem = n, n && !o && L(e.onHover, [
        t,
        n,
        this
      ], this);
    } else n && L(e.onClick, [
      t,
      n,
      this
    ], this);
  }
}
function Fd(i, t, e, n, s) {
  const o = zd(n, i, t, e), r = Nd(s, n, t.lineHeight);
  return {
    itemWidth: o,
    itemHeight: r
  };
}
function zd(i, t, e, n) {
  let s = i.text;
  return s && typeof s != "string" && (s = s.reduce((o, r) => o.length > r.length ? o : r)), t + e.size / 2 + n.measureText(s).width;
}
function Nd(i, t, e) {
  let n = i;
  return typeof t.text != "string" && (n = _a(t, e)), n;
}
function _a(i, t) {
  const e = i.text ? i.text.length : 0;
  return t * e;
}
function Bd(i, t) {
  return !!((i === "mousemove" || i === "mouseout") && (t.onHover || t.onLeave) || t.onClick && (i === "click" || i === "mouseup"));
}
var Vd = {
  id: "legend",
  _element: qo,
  start(i, t, e) {
    const n = i.legend = new qo({
      ctx: i.ctx,
      options: e,
      chart: i
    });
    ct.configure(i, n, e), ct.addBox(i, n);
  },
  stop(i) {
    ct.removeBox(i, i.legend), delete i.legend;
  },
  beforeUpdate(i, t, e) {
    const n = i.legend;
    ct.configure(i, n, e), n.options = e;
  },
  afterUpdate(i) {
    const t = i.legend;
    t.buildLabels(), t.adjustHitBoxes();
  },
  afterEvent(i, t) {
    t.replay || i.legend.handleEvent(t.event);
  },
  defaults: {
    display: !0,
    position: "top",
    align: "center",
    fullSize: !0,
    reverse: !1,
    weight: 1e3,
    onClick(i, t, e) {
      const n = t.datasetIndex, s = e.chart;
      s.isDatasetVisible(n) ? (s.hide(n), t.hidden = !0) : (s.show(n), t.hidden = !1);
    },
    onHover: null,
    onLeave: null,
    labels: {
      color: (i) => i.chart.options.color,
      boxWidth: 40,
      padding: 10,
      generateLabels(i) {
        const t = i.data.datasets, { labels: { usePointStyle: e, pointStyle: n, textAlign: s, color: o, useBorderRadius: r, borderRadius: a } } = i.legend.options;
        return i._getSortedDatasetMetas().map((l) => {
          const c = l.controller.getStyle(e ? 0 : void 0), h = ht(c.borderWidth);
          return {
            text: t[l.index].label,
            fillStyle: c.backgroundColor,
            fontColor: o,
            hidden: !l.visible,
            lineCap: c.borderCapStyle,
            lineDash: c.borderDash,
            lineDashOffset: c.borderDashOffset,
            lineJoin: c.borderJoinStyle,
            lineWidth: (h.width + h.height) / 4,
            strokeStyle: c.borderColor,
            pointStyle: n || c.pointStyle,
            rotation: c.rotation,
            textAlign: s || c.textAlign,
            borderRadius: r && (a || c.borderRadius),
            datasetIndex: l.index
          };
        }, this);
      }
    },
    title: {
      color: (i) => i.chart.options.color,
      display: !1,
      position: "center",
      text: ""
    }
  },
  descriptors: {
    _scriptable: (i) => !i.startsWith("on"),
    labels: {
      _scriptable: (i) => ![
        "generateLabels",
        "filter",
        "sort"
      ].includes(i)
    }
  }
};
class _s extends Ct {
  constructor(t) {
    super(), this.chart = t.chart, this.options = t.options, this.ctx = t.ctx, this._padding = void 0, this.top = void 0, this.bottom = void 0, this.left = void 0, this.right = void 0, this.width = void 0, this.height = void 0, this.position = void 0, this.weight = void 0, this.fullSize = void 0;
  }
  update(t, e) {
    const n = this.options;
    if (this.left = 0, this.top = 0, !n.display) {
      this.width = this.height = this.right = this.bottom = 0;
      return;
    }
    this.width = this.right = t, this.height = this.bottom = e;
    const s = q(n.text) ? n.text.length : 1;
    this._padding = ht(n.padding);
    const o = s * it(n.font).lineHeight + this._padding.height;
    this.isHorizontal() ? this.height = o : this.width = o;
  }
  isHorizontal() {
    const t = this.options.position;
    return t === "top" || t === "bottom";
  }
  _drawArgs(t) {
    const { top: e, left: n, bottom: s, right: o, options: r } = this, a = r.align;
    let l = 0, c, h, u;
    return this.isHorizontal() ? (h = at(a, n, o), u = e + t, c = o - n) : (r.position === "left" ? (h = n + t, u = at(a, s, e), l = W * -0.5) : (h = o - t, u = at(a, e, s), l = W * 0.5), c = s - e), {
      titleX: h,
      titleY: u,
      maxWidth: c,
      rotation: l
    };
  }
  draw() {
    const t = this.ctx, e = this.options;
    if (!e.display)
      return;
    const n = it(e.font), o = n.lineHeight / 2 + this._padding.top, { titleX: r, titleY: a, maxWidth: l, rotation: c } = this._drawArgs(o);
    ye(t, e.text, 0, 0, n, {
      color: e.color,
      maxWidth: l,
      rotation: c,
      textAlign: cs(e.align),
      textBaseline: "middle",
      translation: [
        r,
        a
      ]
    });
  }
}
function Hd(i, t) {
  const e = new _s({
    ctx: i.ctx,
    options: t,
    chart: i
  });
  ct.configure(i, e, t), ct.addBox(i, e), i.titleBlock = e;
}
var Wd = {
  id: "title",
  _element: _s,
  start(i, t, e) {
    Hd(i, e);
  },
  stop(i) {
    const t = i.titleBlock;
    ct.removeBox(i, t), delete i.titleBlock;
  },
  beforeUpdate(i, t, e) {
    const n = i.titleBlock;
    ct.configure(i, n, e), n.options = e;
  },
  defaults: {
    align: "center",
    display: !1,
    font: {
      weight: "bold"
    },
    fullSize: !0,
    padding: 10,
    position: "top",
    text: "",
    weight: 2e3
  },
  defaultRoutes: {
    color: "color"
  },
  descriptors: {
    _scriptable: !0,
    _indexable: !1
  }
};
const Li = /* @__PURE__ */ new WeakMap();
var jd = {
  id: "subtitle",
  start(i, t, e) {
    const n = new _s({
      ctx: i.ctx,
      options: e,
      chart: i
    });
    ct.configure(i, n, e), ct.addBox(i, n), Li.set(i, n);
  },
  stop(i) {
    ct.removeBox(i, Li.get(i)), Li.delete(i);
  },
  beforeUpdate(i, t, e) {
    const n = Li.get(i);
    ct.configure(i, n, e), n.options = e;
  },
  defaults: {
    align: "center",
    display: !1,
    font: {
      weight: "normal"
    },
    fullSize: !0,
    padding: 0,
    position: "top",
    text: "",
    weight: 1500
  },
  defaultRoutes: {
    color: "color"
  },
  descriptors: {
    _scriptable: !0,
    _indexable: !1
  }
};
const Ze = {
  average(i) {
    if (!i.length)
      return !1;
    let t, e, n = /* @__PURE__ */ new Set(), s = 0, o = 0;
    for (t = 0, e = i.length; t < e; ++t) {
      const a = i[t].element;
      if (a && a.hasValue()) {
        const l = a.tooltipPosition();
        n.add(l.x), s += l.y, ++o;
      }
    }
    return o === 0 || n.size === 0 ? !1 : {
      x: [
        ...n
      ].reduce((a, l) => a + l) / n.size,
      y: s / o
    };
  },
  nearest(i, t) {
    if (!i.length)
      return !1;
    let e = t.x, n = t.y, s = Number.POSITIVE_INFINITY, o, r, a;
    for (o = 0, r = i.length; o < r; ++o) {
      const l = i[o].element;
      if (l && l.hasValue()) {
        const c = l.getCenterPoint(), h = Fn(t, c);
        h < s && (s = h, a = l);
      }
    }
    if (a) {
      const l = a.tooltipPosition();
      e = l.x, n = l.y;
    }
    return {
      x: e,
      y: n
    };
  }
};
function Et(i, t) {
  return t && (q(t) ? Array.prototype.push.apply(i, t) : i.push(t)), i;
}
function Ht(i) {
  return (typeof i == "string" || i instanceof String) && i.indexOf(`
`) > -1 ? i.split(`
`) : i;
}
function Yd(i, t) {
  const { element: e, datasetIndex: n, index: s } = t, o = i.getDatasetMeta(n).controller, { label: r, value: a } = o.getLabelAndValue(s);
  return {
    chart: i,
    label: r,
    parsed: o.getParsed(s),
    raw: i.data.datasets[n].data[s],
    formattedValue: a,
    dataset: o.getDataset(),
    dataIndex: s,
    datasetIndex: n,
    element: e
  };
}
function Zo(i, t) {
  const e = i.chart.ctx, { body: n, footer: s, title: o } = i, { boxWidth: r, boxHeight: a } = t, l = it(t.bodyFont), c = it(t.titleFont), h = it(t.footerFont), u = o.length, d = s.length, g = n.length, m = ht(t.padding);
  let b = m.height, _ = 0, y = n.reduce((S, k) => S + k.before.length + k.lines.length + k.after.length, 0);
  if (y += i.beforeBody.length + i.afterBody.length, u && (b += u * c.lineHeight + (u - 1) * t.titleSpacing + t.titleMarginBottom), y) {
    const S = t.displayColors ? Math.max(a, l.lineHeight) : l.lineHeight;
    b += g * S + (y - g) * l.lineHeight + (y - 1) * t.bodySpacing;
  }
  d && (b += t.footerMarginTop + d * h.lineHeight + (d - 1) * t.footerSpacing);
  let v = 0;
  const w = function(S) {
    _ = Math.max(_, e.measureText(S).width + v);
  };
  return e.save(), e.font = c.string, B(i.title, w), e.font = l.string, B(i.beforeBody.concat(i.afterBody), w), v = t.displayColors ? r + 2 + t.boxPadding : 0, B(n, (S) => {
    B(S.before, w), B(S.lines, w), B(S.after, w);
  }), v = 0, e.font = h.string, B(i.footer, w), e.restore(), _ += m.width, {
    width: _,
    height: b
  };
}
function Ud(i, t) {
  const { y: e, height: n } = t;
  return e < n / 2 ? "top" : e > i.height - n / 2 ? "bottom" : "center";
}
function Xd(i, t, e, n) {
  const { x: s, width: o } = n, r = e.caretSize + e.caretPadding;
  if (i === "left" && s + o + r > t.width || i === "right" && s - o - r < 0)
    return !0;
}
function $d(i, t, e, n) {
  const { x: s, width: o } = e, { width: r, chartArea: { left: a, right: l } } = i;
  let c = "center";
  return n === "center" ? c = s <= (a + l) / 2 ? "left" : "right" : s <= o / 2 ? c = "left" : s >= r - o / 2 && (c = "right"), Xd(c, i, t, e) && (c = "center"), c;
}
function Go(i, t, e) {
  const n = e.yAlign || t.yAlign || Ud(i, e);
  return {
    xAlign: e.xAlign || t.xAlign || $d(i, t, e, n),
    yAlign: n
  };
}
function qd(i, t) {
  let { x: e, width: n } = i;
  return t === "right" ? e -= n : t === "center" && (e -= n / 2), e;
}
function Zd(i, t, e) {
  let { y: n, height: s } = i;
  return t === "top" ? n += e : t === "bottom" ? n -= s + e : n -= s / 2, n;
}
function Ko(i, t, e, n) {
  const { caretSize: s, caretPadding: o, cornerRadius: r } = i, { xAlign: a, yAlign: l } = e, c = s + o, { topLeft: h, topRight: u, bottomLeft: d, bottomRight: g } = be(r);
  let m = qd(t, a);
  const b = Zd(t, l, c);
  return l === "center" ? a === "left" ? m += c : a === "right" && (m -= c) : a === "left" ? m -= Math.max(h, d) + s : a === "right" && (m += Math.max(u, g) + s), {
    x: ot(m, 0, n.width - t.width),
    y: ot(b, 0, n.height - t.height)
  };
}
function Ii(i, t, e) {
  const n = ht(e.padding);
  return t === "center" ? i.x + i.width / 2 : t === "right" ? i.x + i.width - n.right : i.x + n.left;
}
function Jo(i) {
  return Et([], Ht(i));
}
function Gd(i, t, e) {
  return se(i, {
    tooltip: t,
    tooltipItems: e,
    type: "tooltip"
  });
}
function Qo(i, t) {
  const e = t && t.dataset && t.dataset.tooltip && t.dataset.tooltip.callbacks;
  return e ? i.override(e) : i;
}
const ya = {
  beforeTitle: Bt,
  title(i) {
    if (i.length > 0) {
      const t = i[0], e = t.chart.data.labels, n = e ? e.length : 0;
      if (this && this.options && this.options.mode === "dataset")
        return t.dataset.label || "";
      if (t.label)
        return t.label;
      if (n > 0 && t.dataIndex < n)
        return e[t.dataIndex];
    }
    return "";
  },
  afterTitle: Bt,
  beforeBody: Bt,
  beforeLabel: Bt,
  label(i) {
    if (this && this.options && this.options.mode === "dataset")
      return i.label + ": " + i.formattedValue || i.formattedValue;
    let t = i.dataset.label || "";
    t && (t += ": ");
    const e = i.formattedValue;
    return V(e) || (t += e), t;
  },
  labelColor(i) {
    const e = i.chart.getDatasetMeta(i.datasetIndex).controller.getStyle(i.dataIndex);
    return {
      borderColor: e.borderColor,
      backgroundColor: e.backgroundColor,
      borderWidth: e.borderWidth,
      borderDash: e.borderDash,
      borderDashOffset: e.borderDashOffset,
      borderRadius: 0
    };
  },
  labelTextColor() {
    return this.options.bodyColor;
  },
  labelPointStyle(i) {
    const e = i.chart.getDatasetMeta(i.datasetIndex).controller.getStyle(i.dataIndex);
    return {
      pointStyle: e.pointStyle,
      rotation: e.rotation
    };
  },
  afterLabel: Bt,
  afterBody: Bt,
  beforeFooter: Bt,
  footer: Bt,
  afterFooter: Bt
};
function gt(i, t, e, n) {
  const s = i[t].call(e, n);
  return typeof s > "u" ? ya[t].call(e, n) : s;
}
class Un extends Ct {
  constructor(t) {
    super(), this.opacity = 0, this._active = [], this._eventPosition = void 0, this._size = void 0, this._cachedAnimations = void 0, this._tooltipItems = [], this.$animations = void 0, this.$context = void 0, this.chart = t.chart, this.options = t.options, this.dataPoints = void 0, this.title = void 0, this.beforeBody = void 0, this.body = void 0, this.afterBody = void 0, this.footer = void 0, this.xAlign = void 0, this.yAlign = void 0, this.x = void 0, this.y = void 0, this.height = void 0, this.width = void 0, this.caretX = void 0, this.caretY = void 0, this.labelColors = void 0, this.labelPointStyles = void 0, this.labelTextColors = void 0;
  }
  initialize(t) {
    this.options = t, this._cachedAnimations = void 0, this.$context = void 0;
  }
  _resolveAnimations() {
    const t = this._cachedAnimations;
    if (t)
      return t;
    const e = this.chart, n = this.options.setContext(this.getContext()), s = n.enabled && e.options.animation && n.animations, o = new Qr(this.chart, s);
    return s._cacheable && (this._cachedAnimations = Object.freeze(o)), o;
  }
  getContext() {
    return this.$context || (this.$context = Gd(this.chart.getContext(), this, this._tooltipItems));
  }
  getTitle(t, e) {
    const { callbacks: n } = e, s = gt(n, "beforeTitle", this, t), o = gt(n, "title", this, t), r = gt(n, "afterTitle", this, t);
    let a = [];
    return a = Et(a, Ht(s)), a = Et(a, Ht(o)), a = Et(a, Ht(r)), a;
  }
  getBeforeBody(t, e) {
    return Jo(gt(e.callbacks, "beforeBody", this, t));
  }
  getBody(t, e) {
    const { callbacks: n } = e, s = [];
    return B(t, (o) => {
      const r = {
        before: [],
        lines: [],
        after: []
      }, a = Qo(n, o);
      Et(r.before, Ht(gt(a, "beforeLabel", this, o))), Et(r.lines, gt(a, "label", this, o)), Et(r.after, Ht(gt(a, "afterLabel", this, o))), s.push(r);
    }), s;
  }
  getAfterBody(t, e) {
    return Jo(gt(e.callbacks, "afterBody", this, t));
  }
  getFooter(t, e) {
    const { callbacks: n } = e, s = gt(n, "beforeFooter", this, t), o = gt(n, "footer", this, t), r = gt(n, "afterFooter", this, t);
    let a = [];
    return a = Et(a, Ht(s)), a = Et(a, Ht(o)), a = Et(a, Ht(r)), a;
  }
  _createItems(t) {
    const e = this._active, n = this.chart.data, s = [], o = [], r = [];
    let a = [], l, c;
    for (l = 0, c = e.length; l < c; ++l)
      a.push(Yd(this.chart, e[l]));
    return t.filter && (a = a.filter((h, u, d) => t.filter(h, u, d, n))), t.itemSort && (a = a.sort((h, u) => t.itemSort(h, u, n))), B(a, (h) => {
      const u = Qo(t.callbacks, h);
      s.push(gt(u, "labelColor", this, h)), o.push(gt(u, "labelPointStyle", this, h)), r.push(gt(u, "labelTextColor", this, h));
    }), this.labelColors = s, this.labelPointStyles = o, this.labelTextColors = r, this.dataPoints = a, a;
  }
  update(t, e) {
    const n = this.options.setContext(this.getContext()), s = this._active;
    let o, r = [];
    if (!s.length)
      this.opacity !== 0 && (o = {
        opacity: 0
      });
    else {
      const a = Ze[n.position].call(this, s, this._eventPosition);
      r = this._createItems(n), this.title = this.getTitle(r, n), this.beforeBody = this.getBeforeBody(r, n), this.body = this.getBody(r, n), this.afterBody = this.getAfterBody(r, n), this.footer = this.getFooter(r, n);
      const l = this._size = Zo(this, n), c = Object.assign({}, a, l), h = Go(this.chart, n, c), u = Ko(n, c, h, this.chart);
      this.xAlign = h.xAlign, this.yAlign = h.yAlign, o = {
        opacity: 1,
        x: u.x,
        y: u.y,
        width: l.width,
        height: l.height,
        caretX: a.x,
        caretY: a.y
      };
    }
    this._tooltipItems = r, this.$context = void 0, o && this._resolveAnimations().update(this, o), t && n.external && n.external.call(this, {
      chart: this.chart,
      tooltip: this,
      replay: e
    });
  }
  drawCaret(t, e, n, s) {
    const o = this.getCaretPosition(t, n, s);
    e.lineTo(o.x1, o.y1), e.lineTo(o.x2, o.y2), e.lineTo(o.x3, o.y3);
  }
  getCaretPosition(t, e, n) {
    const { xAlign: s, yAlign: o } = this, { caretSize: r, cornerRadius: a } = n, { topLeft: l, topRight: c, bottomLeft: h, bottomRight: u } = be(a), { x: d, y: g } = t, { width: m, height: b } = e;
    let _, y, v, w, S, k;
    return o === "center" ? (S = g + b / 2, s === "left" ? (_ = d, y = _ - r, w = S + r, k = S - r) : (_ = d + m, y = _ + r, w = S - r, k = S + r), v = _) : (s === "left" ? y = d + Math.max(l, h) + r : s === "right" ? y = d + m - Math.max(c, u) - r : y = this.caretX, o === "top" ? (w = g, S = w - r, _ = y - r, v = y + r) : (w = g + b, S = w + r, _ = y + r, v = y - r), k = w), {
      x1: _,
      x2: y,
      x3: v,
      y1: w,
      y2: S,
      y3: k
    };
  }
  drawTitle(t, e, n) {
    const s = this.title, o = s.length;
    let r, a, l;
    if (o) {
      const c = Ce(n.rtl, this.x, this.width);
      for (t.x = Ii(this, n.titleAlign, n), e.textAlign = c.textAlign(n.titleAlign), e.textBaseline = "middle", r = it(n.titleFont), a = n.titleSpacing, e.fillStyle = n.titleColor, e.font = r.string, l = 0; l < o; ++l)
        e.fillText(s[l], c.x(t.x), t.y + r.lineHeight / 2), t.y += r.lineHeight + a, l + 1 === o && (t.y += n.titleMarginBottom - a);
    }
  }
  _drawColorBox(t, e, n, s, o) {
    const r = this.labelColors[n], a = this.labelPointStyles[n], { boxHeight: l, boxWidth: c } = o, h = it(o.bodyFont), u = Ii(this, "left", o), d = s.x(u), g = l < h.lineHeight ? (h.lineHeight - l) / 2 : 0, m = e.y + g;
    if (o.usePointStyle) {
      const b = {
        radius: Math.min(c, l) / 2,
        pointStyle: a.pointStyle,
        rotation: a.rotation,
        borderWidth: 1
      }, _ = s.leftForLtr(d, c) + c / 2, y = m + l / 2;
      t.strokeStyle = o.multiKeyBackground, t.fillStyle = o.multiKeyBackground, Nn(t, b, _, y), t.strokeStyle = r.borderColor, t.fillStyle = r.backgroundColor, Nn(t, b, _, y);
    } else {
      t.lineWidth = H(r.borderWidth) ? Math.max(...Object.values(r.borderWidth)) : r.borderWidth || 1, t.strokeStyle = r.borderColor, t.setLineDash(r.borderDash || []), t.lineDashOffset = r.borderDashOffset || 0;
      const b = s.leftForLtr(d, c), _ = s.leftForLtr(s.xPlus(d, 1), c - 2), y = be(r.borderRadius);
      Object.values(y).some((v) => v !== 0) ? (t.beginPath(), t.fillStyle = o.multiKeyBackground, ai(t, {
        x: b,
        y: m,
        w: c,
        h: l,
        radius: y
      }), t.fill(), t.stroke(), t.fillStyle = r.backgroundColor, t.beginPath(), ai(t, {
        x: _,
        y: m + 1,
        w: c - 2,
        h: l - 2,
        radius: y
      }), t.fill()) : (t.fillStyle = o.multiKeyBackground, t.fillRect(b, m, c, l), t.strokeRect(b, m, c, l), t.fillStyle = r.backgroundColor, t.fillRect(_, m + 1, c - 2, l - 2));
    }
    t.fillStyle = this.labelTextColors[n];
  }
  drawBody(t, e, n) {
    const { body: s } = this, { bodySpacing: o, bodyAlign: r, displayColors: a, boxHeight: l, boxWidth: c, boxPadding: h } = n, u = it(n.bodyFont);
    let d = u.lineHeight, g = 0;
    const m = Ce(n.rtl, this.x, this.width), b = function(D) {
      e.fillText(D, m.x(t.x + g), t.y + d / 2), t.y += d + o;
    }, _ = m.textAlign(r);
    let y, v, w, S, k, P, C;
    for (e.textAlign = r, e.textBaseline = "middle", e.font = u.string, t.x = Ii(this, _, n), e.fillStyle = n.bodyColor, B(this.beforeBody, b), g = a && _ !== "right" ? r === "center" ? c / 2 + h : c + 2 + h : 0, S = 0, P = s.length; S < P; ++S) {
      for (y = s[S], v = this.labelTextColors[S], e.fillStyle = v, B(y.before, b), w = y.lines, a && w.length && (this._drawColorBox(e, t, S, m, n), d = Math.max(u.lineHeight, l)), k = 0, C = w.length; k < C; ++k)
        b(w[k]), d = u.lineHeight;
      B(y.after, b);
    }
    g = 0, d = u.lineHeight, B(this.afterBody, b), t.y -= o;
  }
  drawFooter(t, e, n) {
    const s = this.footer, o = s.length;
    let r, a;
    if (o) {
      const l = Ce(n.rtl, this.x, this.width);
      for (t.x = Ii(this, n.footerAlign, n), t.y += n.footerMarginTop, e.textAlign = l.textAlign(n.footerAlign), e.textBaseline = "middle", r = it(n.footerFont), e.fillStyle = n.footerColor, e.font = r.string, a = 0; a < o; ++a)
        e.fillText(s[a], l.x(t.x), t.y + r.lineHeight / 2), t.y += r.lineHeight + n.footerSpacing;
    }
  }
  drawBackground(t, e, n, s) {
    const { xAlign: o, yAlign: r } = this, { x: a, y: l } = t, { width: c, height: h } = n, { topLeft: u, topRight: d, bottomLeft: g, bottomRight: m } = be(s.cornerRadius);
    e.fillStyle = s.backgroundColor, e.strokeStyle = s.borderColor, e.lineWidth = s.borderWidth, e.beginPath(), e.moveTo(a + u, l), r === "top" && this.drawCaret(t, e, n, s), e.lineTo(a + c - d, l), e.quadraticCurveTo(a + c, l, a + c, l + d), r === "center" && o === "right" && this.drawCaret(t, e, n, s), e.lineTo(a + c, l + h - m), e.quadraticCurveTo(a + c, l + h, a + c - m, l + h), r === "bottom" && this.drawCaret(t, e, n, s), e.lineTo(a + g, l + h), e.quadraticCurveTo(a, l + h, a, l + h - g), r === "center" && o === "left" && this.drawCaret(t, e, n, s), e.lineTo(a, l + u), e.quadraticCurveTo(a, l, a + u, l), e.closePath(), e.fill(), s.borderWidth > 0 && e.stroke();
  }
  _updateAnimationTarget(t) {
    const e = this.chart, n = this.$animations, s = n && n.x, o = n && n.y;
    if (s || o) {
      const r = Ze[t.position].call(this, this._active, this._eventPosition);
      if (!r)
        return;
      const a = this._size = Zo(this, t), l = Object.assign({}, r, this._size), c = Go(e, t, l), h = Ko(t, l, c, e);
      (s._to !== h.x || o._to !== h.y) && (this.xAlign = c.xAlign, this.yAlign = c.yAlign, this.width = a.width, this.height = a.height, this.caretX = r.x, this.caretY = r.y, this._resolveAnimations().update(this, h));
    }
  }
  _willRender() {
    return !!this.opacity;
  }
  draw(t) {
    const e = this.options.setContext(this.getContext());
    let n = this.opacity;
    if (!n)
      return;
    this._updateAnimationTarget(e);
    const s = {
      width: this.width,
      height: this.height
    }, o = {
      x: this.x,
      y: this.y
    };
    n = Math.abs(n) < 1e-3 ? 0 : n;
    const r = ht(e.padding), a = this.title.length || this.beforeBody.length || this.body.length || this.afterBody.length || this.footer.length;
    e.enabled && a && (t.save(), t.globalAlpha = n, this.drawBackground(o, t, s, e), $r(t, e.textDirection), o.y += r.top, this.drawTitle(o, t, e), this.drawBody(o, t, e), this.drawFooter(o, t, e), qr(t, e.textDirection), t.restore());
  }
  getActiveElements() {
    return this._active || [];
  }
  setActiveElements(t, e) {
    const n = this._active, s = t.map(({ datasetIndex: a, index: l }) => {
      const c = this.chart.getDatasetMeta(a);
      if (!c)
        throw new Error("Cannot find a dataset at index " + a);
      return {
        datasetIndex: a,
        element: c.data[l],
        index: l
      };
    }), o = !$i(n, s), r = this._positionChanged(s, e);
    (o || r) && (this._active = s, this._eventPosition = e, this._ignoreReplayEvents = !0, this.update(!0));
  }
  handleEvent(t, e, n = !0) {
    if (e && this._ignoreReplayEvents)
      return !1;
    this._ignoreReplayEvents = !1;
    const s = this.options, o = this._active || [], r = this._getActiveElements(t, o, e, n), a = this._positionChanged(r, t), l = e || !$i(r, o) || a;
    return l && (this._active = r, (s.enabled || s.external) && (this._eventPosition = {
      x: t.x,
      y: t.y
    }, this.update(!0, e))), l;
  }
  _getActiveElements(t, e, n, s) {
    const o = this.options;
    if (t.type === "mouseout")
      return [];
    if (!s)
      return e.filter((a) => this.chart.data.datasets[a.datasetIndex] && this.chart.getDatasetMeta(a.datasetIndex).controller.getParsed(a.index) !== void 0);
    const r = this.chart.getElementsAtEventForMode(t, o.mode, o, n);
    return o.reverse && r.reverse(), r;
  }
  _positionChanged(t, e) {
    const { caretX: n, caretY: s, options: o } = this, r = Ze[o.position].call(this, t, e);
    return r !== !1 && (n !== r.x || s !== r.y);
  }
}
O(Un, "positioners", Ze);
var Kd = {
  id: "tooltip",
  _element: Un,
  positioners: Ze,
  afterInit(i, t, e) {
    e && (i.tooltip = new Un({
      chart: i,
      options: e
    }));
  },
  beforeUpdate(i, t, e) {
    i.tooltip && i.tooltip.initialize(e);
  },
  reset(i, t, e) {
    i.tooltip && i.tooltip.initialize(e);
  },
  afterDraw(i) {
    const t = i.tooltip;
    if (t && t._willRender()) {
      const e = {
        tooltip: t
      };
      if (i.notifyPlugins("beforeTooltipDraw", {
        ...e,
        cancelable: !0
      }) === !1)
        return;
      t.draw(i.ctx), i.notifyPlugins("afterTooltipDraw", e);
    }
  },
  afterEvent(i, t) {
    if (i.tooltip) {
      const e = t.replay;
      i.tooltip.handleEvent(t.event, e, t.inChartArea) && (t.changed = !0);
    }
  },
  defaults: {
    enabled: !0,
    external: null,
    position: "average",
    backgroundColor: "rgba(0,0,0,0.8)",
    titleColor: "#fff",
    titleFont: {
      weight: "bold"
    },
    titleSpacing: 2,
    titleMarginBottom: 6,
    titleAlign: "left",
    bodyColor: "#fff",
    bodySpacing: 2,
    bodyFont: {},
    bodyAlign: "left",
    footerColor: "#fff",
    footerSpacing: 2,
    footerMarginTop: 6,
    footerFont: {
      weight: "bold"
    },
    footerAlign: "left",
    padding: 6,
    caretPadding: 2,
    caretSize: 5,
    cornerRadius: 6,
    boxHeight: (i, t) => t.bodyFont.size,
    boxWidth: (i, t) => t.bodyFont.size,
    multiKeyBackground: "#fff",
    displayColors: !0,
    boxPadding: 0,
    borderColor: "rgba(0,0,0,0)",
    borderWidth: 0,
    animation: {
      duration: 400,
      easing: "easeOutQuart"
    },
    animations: {
      numbers: {
        type: "number",
        properties: [
          "x",
          "y",
          "width",
          "height",
          "caretX",
          "caretY"
        ]
      },
      opacity: {
        easing: "linear",
        duration: 200
      }
    },
    callbacks: ya
  },
  defaultRoutes: {
    bodyFont: "font",
    footerFont: "font",
    titleFont: "font"
  },
  descriptors: {
    _scriptable: (i) => i !== "filter" && i !== "itemSort" && i !== "external",
    _indexable: !1,
    callbacks: {
      _scriptable: !1,
      _indexable: !1
    },
    animation: {
      _fallback: !1
    },
    animations: {
      _fallback: "animation"
    }
  },
  additionalOptionScopes: [
    "interaction"
  ]
}, Jd = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  Colors: hd,
  Decimation: gd,
  Filler: Ld,
  Legend: Vd,
  SubTitle: jd,
  Title: Wd,
  Tooltip: Kd
});
const Qd = (i, t, e, n) => (typeof t == "string" ? (e = i.push(t) - 1, n.unshift({
  index: e,
  label: t
})) : isNaN(t) && (e = null), e);
function tf(i, t, e, n) {
  const s = i.indexOf(t);
  if (s === -1)
    return Qd(i, t, e, n);
  const o = i.lastIndexOf(t);
  return s !== o ? e : s;
}
const ef = (i, t) => i === null ? null : ot(Math.round(i), 0, t);
function tr(i) {
  const t = this.getLabels();
  return i >= 0 && i < t.length ? t[i] : i;
}
class Xn extends ve {
  constructor(t) {
    super(t), this._startValue = void 0, this._valueRange = 0, this._addedLabels = [];
  }
  init(t) {
    const e = this._addedLabels;
    if (e.length) {
      const n = this.getLabels();
      for (const { index: s, label: o } of e)
        n[s] === o && n.splice(s, 1);
      this._addedLabels = [];
    }
    super.init(t);
  }
  parse(t, e) {
    if (V(t))
      return null;
    const n = this.getLabels();
    return e = isFinite(e) && n[e] === t ? e : tf(n, t, I(e, t), this._addedLabels), ef(e, n.length - 1);
  }
  determineDataLimits() {
    const { minDefined: t, maxDefined: e } = this.getUserBounds();
    let { min: n, max: s } = this.getMinMax(!0);
    this.options.bounds === "ticks" && (t || (n = 0), e || (s = this.getLabels().length - 1)), this.min = n, this.max = s;
  }
  buildTicks() {
    const t = this.min, e = this.max, n = this.options.offset, s = [];
    let o = this.getLabels();
    o = t === 0 && e === o.length - 1 ? o : o.slice(t, e + 1), this._valueRange = Math.max(o.length - (n ? 0 : 1), 1), this._startValue = this.min - (n ? 0.5 : 0);
    for (let r = t; r <= e; r++)
      s.push({
        value: r
      });
    return s;
  }
  getLabelForValue(t) {
    return tr.call(this, t);
  }
  configure() {
    super.configure(), this.isHorizontal() || (this._reversePixels = !this._reversePixels);
  }
  getPixelForValue(t) {
    return typeof t != "number" && (t = this.parse(t)), t === null ? NaN : this.getPixelForDecimal((t - this._startValue) / this._valueRange);
  }
  getPixelForTick(t) {
    const e = this.ticks;
    return t < 0 || t > e.length - 1 ? null : this.getPixelForValue(e[t].value);
  }
  getValueForPixel(t) {
    return Math.round(this._startValue + this.getDecimalForPixel(t) * this._valueRange);
  }
  getBasePixel() {
    return this.bottom;
  }
}
O(Xn, "id", "category"), O(Xn, "defaults", {
  ticks: {
    callback: tr
  }
});
function nf(i, t) {
  const e = [], { bounds: s, step: o, min: r, max: a, precision: l, count: c, maxTicks: h, maxDigits: u, includeBounds: d } = i, g = o || 1, m = h - 1, { min: b, max: _ } = t, y = !V(r), v = !V(a), w = !V(c), S = (_ - b) / (u + 1);
  let k = qs((_ - b) / m / g) * g, P, C, D, E;
  if (k < 1e-14 && !y && !v)
    return [
      {
        value: b
      },
      {
        value: _
      }
    ];
  E = Math.ceil(_ / k) - Math.floor(b / k), E > m && (k = qs(E * k / m / g) * g), V(l) || (P = Math.pow(10, l), k = Math.ceil(k * P) / P), s === "ticks" ? (C = Math.floor(b / k) * k, D = Math.ceil(_ / k) * k) : (C = b, D = _), y && v && o && ql((a - r) / o, k / 1e3) ? (E = Math.round(Math.min((a - r) / k, h)), k = (a - r) / E, C = r, D = a) : w ? (C = y ? r : C, D = v ? a : D, E = c - 1, k = (D - C) / E) : (E = (D - C) / k, me(E, Math.round(E), k / 1e3) ? E = Math.round(E) : E = Math.ceil(E));
  const F = Math.max(Zs(k), Zs(C));
  P = Math.pow(10, V(l) ? F : l), C = Math.round(C * P) / P, D = Math.round(D * P) / P;
  let R = 0;
  for (y && (d && C !== r ? (e.push({
    value: r
  }), C < r && R++, me(Math.round((C + R * k) * P) / P, r, er(r, S, i)) && R++) : C < r && R++); R < E; ++R) {
    const z = Math.round((C + R * k) * P) / P;
    if (v && z > a)
      break;
    e.push({
      value: z
    });
  }
  return v && d && D !== a ? e.length && me(e[e.length - 1].value, a, er(a, S, i)) ? e[e.length - 1].value = a : e.push({
    value: a
  }) : (!v || D === a) && e.push({
    value: D
  }), e;
}
function er(i, t, { horizontal: e, minRotation: n }) {
  const s = Tt(n), o = (e ? Math.sin(s) : Math.cos(s)) || 1e-3, r = 0.75 * t * ("" + i).length;
  return Math.min(t / o, r);
}
class tn extends ve {
  constructor(t) {
    super(t), this.start = void 0, this.end = void 0, this._startValue = void 0, this._endValue = void 0, this._valueRange = 0;
  }
  parse(t, e) {
    return V(t) || (typeof t == "number" || t instanceof Number) && !isFinite(+t) ? null : +t;
  }
  handleTickRangeOptions() {
    const { beginAtZero: t } = this.options, { minDefined: e, maxDefined: n } = this.getUserBounds();
    let { min: s, max: o } = this;
    const r = (l) => s = e ? s : l, a = (l) => o = n ? o : l;
    if (t) {
      const l = St(s), c = St(o);
      l < 0 && c < 0 ? a(0) : l > 0 && c > 0 && r(0);
    }
    if (s === o) {
      let l = o === 0 ? 1 : Math.abs(o * 0.05);
      a(o + l), t || r(s - l);
    }
    this.min = s, this.max = o;
  }
  getTickLimit() {
    const t = this.options.ticks;
    let { maxTicksLimit: e, stepSize: n } = t, s;
    return n ? (s = Math.ceil(this.max / n) - Math.floor(this.min / n) + 1, s > 1e3 && (console.warn(`scales.${this.id}.ticks.stepSize: ${n} would result generating up to ${s} ticks. Limiting to 1000.`), s = 1e3)) : (s = this.computeTickLimit(), e = e || 11), e && (s = Math.min(e, s)), s;
  }
  computeTickLimit() {
    return Number.POSITIVE_INFINITY;
  }
  buildTicks() {
    const t = this.options, e = t.ticks;
    let n = this.getTickLimit();
    n = Math.max(2, n);
    const s = {
      maxTicks: n,
      bounds: t.bounds,
      min: t.min,
      max: t.max,
      precision: e.precision,
      step: e.stepSize,
      count: e.count,
      maxDigits: this._maxDigits(),
      horizontal: this.isHorizontal(),
      minRotation: e.minRotation || 0,
      includeBounds: e.includeBounds !== !1
    }, o = this._range || this, r = nf(s, o);
    return t.bounds === "ticks" && Dr(r, this, "value"), t.reverse ? (r.reverse(), this.start = this.max, this.end = this.min) : (this.start = this.min, this.end = this.max), r;
  }
  configure() {
    const t = this.ticks;
    let e = this.min, n = this.max;
    if (super.configure(), this.options.offset && t.length) {
      const s = (n - e) / Math.max(t.length - 1, 1) / 2;
      e -= s, n += s;
    }
    this._startValue = e, this._endValue = n, this._valueRange = n - e;
  }
  getLabelForValue(t) {
    return fi(t, this.chart.options.locale, this.options.ticks.format);
  }
}
class $n extends tn {
  determineDataLimits() {
    const { min: t, max: e } = this.getMinMax(!0);
    this.min = K(t) ? t : 0, this.max = K(e) ? e : 1, this.handleTickRangeOptions();
  }
  computeTickLimit() {
    const t = this.isHorizontal(), e = t ? this.width : this.height, n = Tt(this.options.ticks.minRotation), s = (t ? Math.sin(n) : Math.cos(n)) || 1e-3, o = this._resolveTickFontOptions(0);
    return Math.ceil(e / Math.min(40, o.lineHeight / s));
  }
  getPixelForValue(t) {
    return t === null ? NaN : this.getPixelForDecimal((t - this._startValue) / this._valueRange);
  }
  getValueForPixel(t) {
    return this._startValue + this.getDecimalForPixel(t) * this._valueRange;
  }
}
O($n, "id", "linear"), O($n, "defaults", {
  ticks: {
    callback: en.formatters.numeric
  }
});
const ci = (i) => Math.floor(Gt(i)), fe = (i, t) => Math.pow(10, ci(i) + t);
function ir(i) {
  return i / Math.pow(10, ci(i)) === 1;
}
function nr(i, t, e) {
  const n = Math.pow(10, e), s = Math.floor(i / n);
  return Math.ceil(t / n) - s;
}
function sf(i, t) {
  const e = t - i;
  let n = ci(e);
  for (; nr(i, t, n) > 10; )
    n++;
  for (; nr(i, t, n) < 10; )
    n--;
  return Math.min(n, ci(i));
}
function of(i, { min: t, max: e }) {
  t = xt(i.min, t);
  const n = [], s = ci(t);
  let o = sf(t, e), r = o < 0 ? Math.pow(10, Math.abs(o)) : 1;
  const a = Math.pow(10, o), l = s > o ? Math.pow(10, s) : 0, c = Math.round((t - l) * r) / r, h = Math.floor((t - l) / a / 10) * a * 10;
  let u = Math.floor((c - h) / Math.pow(10, o)), d = xt(i.min, Math.round((l + h + u * Math.pow(10, o)) * r) / r);
  for (; d < e; )
    n.push({
      value: d,
      major: ir(d),
      significand: u
    }), u >= 10 ? u = u < 15 ? 15 : 20 : u++, u >= 20 && (o++, u = 2, r = o >= 0 ? 1 : r), d = Math.round((l + h + u * Math.pow(10, o)) * r) / r;
  const g = xt(i.max, d);
  return n.push({
    value: g,
    major: ir(g),
    significand: u
  }), n;
}
class qn extends ve {
  constructor(t) {
    super(t), this.start = void 0, this.end = void 0, this._startValue = void 0, this._valueRange = 0;
  }
  parse(t, e) {
    const n = tn.prototype.parse.apply(this, [
      t,
      e
    ]);
    if (n === 0) {
      this._zero = !0;
      return;
    }
    return K(n) && n > 0 ? n : null;
  }
  determineDataLimits() {
    const { min: t, max: e } = this.getMinMax(!0);
    this.min = K(t) ? Math.max(0, t) : null, this.max = K(e) ? Math.max(0, e) : null, this.options.beginAtZero && (this._zero = !0), this._zero && this.min !== this._suggestedMin && !K(this._userMin) && (this.min = t === fe(this.min, 0) ? fe(this.min, -1) : fe(this.min, 0)), this.handleTickRangeOptions();
  }
  handleTickRangeOptions() {
    const { minDefined: t, maxDefined: e } = this.getUserBounds();
    let n = this.min, s = this.max;
    const o = (a) => n = t ? n : a, r = (a) => s = e ? s : a;
    n === s && (n <= 0 ? (o(1), r(10)) : (o(fe(n, -1)), r(fe(s, 1)))), n <= 0 && o(fe(s, -1)), s <= 0 && r(fe(n, 1)), this.min = n, this.max = s;
  }
  buildTicks() {
    const t = this.options, e = {
      min: this._userMin,
      max: this._userMax
    }, n = of(e, this);
    return t.bounds === "ticks" && Dr(n, this, "value"), t.reverse ? (n.reverse(), this.start = this.max, this.end = this.min) : (this.start = this.min, this.end = this.max), n;
  }
  getLabelForValue(t) {
    return t === void 0 ? "0" : fi(t, this.chart.options.locale, this.options.ticks.format);
  }
  configure() {
    const t = this.min;
    super.configure(), this._startValue = Gt(t), this._valueRange = Gt(this.max) - Gt(t);
  }
  getPixelForValue(t) {
    return (t === void 0 || t === 0) && (t = this.min), t === null || isNaN(t) ? NaN : this.getPixelForDecimal(t === this.min ? 0 : (Gt(t) - this._startValue) / this._valueRange);
  }
  getValueForPixel(t) {
    const e = this.getDecimalForPixel(t);
    return Math.pow(10, this._startValue + e * this._valueRange);
  }
}
O(qn, "id", "logarithmic"), O(qn, "defaults", {
  ticks: {
    callback: en.formatters.logarithmic,
    major: {
      enabled: !0
    }
  }
});
function Zn(i) {
  const t = i.ticks;
  if (t.display && i.display) {
    const e = ht(t.backdropPadding);
    return I(t.font && t.font.size, Z.font.size) + e.height;
  }
  return 0;
}
function rf(i, t, e) {
  return e = q(e) ? e : [
    e
  ], {
    w: uc(i, t.string, e),
    h: e.length * t.lineHeight
  };
}
function sr(i, t, e, n, s) {
  return i === n || i === s ? {
    start: t - e / 2,
    end: t + e / 2
  } : i < n || i > s ? {
    start: t - e,
    end: t
  } : {
    start: t,
    end: t + e
  };
}
function af(i) {
  const t = {
    l: i.left + i._padding.left,
    r: i.right - i._padding.right,
    t: i.top + i._padding.top,
    b: i.bottom - i._padding.bottom
  }, e = Object.assign({}, t), n = [], s = [], o = i._pointLabels.length, r = i.options.pointLabels, a = r.centerPointLabels ? W / o : 0;
  for (let l = 0; l < o; l++) {
    const c = r.setContext(i.getPointLabelContext(l));
    s[l] = c.padding;
    const h = i.getPointPosition(l, i.drawingArea + s[l], a), u = it(c.font), d = rf(i.ctx, u, i._pointLabels[l]);
    n[l] = d;
    const g = lt(i.getIndexAngle(l) + a), m = Math.round(as(g)), b = sr(m, h.x, d.w, 0, 180), _ = sr(m, h.y, d.h, 90, 270);
    lf(e, t, g, b, _);
  }
  i.setCenterPoint(t.l - e.l, e.r - t.r, t.t - e.t, e.b - t.b), i._pointLabelItems = uf(i, n, s);
}
function lf(i, t, e, n, s) {
  const o = Math.abs(Math.sin(e)), r = Math.abs(Math.cos(e));
  let a = 0, l = 0;
  n.start < t.l ? (a = (t.l - n.start) / o, i.l = Math.min(i.l, t.l - a)) : n.end > t.r && (a = (n.end - t.r) / o, i.r = Math.max(i.r, t.r + a)), s.start < t.t ? (l = (t.t - s.start) / r, i.t = Math.min(i.t, t.t - l)) : s.end > t.b && (l = (s.end - t.b) / r, i.b = Math.max(i.b, t.b + l));
}
function cf(i, t, e) {
  const n = i.drawingArea, { extra: s, additionalAngle: o, padding: r, size: a } = e, l = i.getPointPosition(t, n + s + r, o), c = Math.round(as(lt(l.angle + J))), h = gf(l.y, a.h, c), u = df(c), d = ff(l.x, a.w, u);
  return {
    visible: !0,
    x: l.x,
    y: h,
    textAlign: u,
    left: d,
    top: h,
    right: d + a.w,
    bottom: h + a.h
  };
}
function hf(i, t) {
  if (!t)
    return !0;
  const { left: e, top: n, right: s, bottom: o } = i;
  return !(It({
    x: e,
    y: n
  }, t) || It({
    x: e,
    y: o
  }, t) || It({
    x: s,
    y: n
  }, t) || It({
    x: s,
    y: o
  }, t));
}
function uf(i, t, e) {
  const n = [], s = i._pointLabels.length, o = i.options, { centerPointLabels: r, display: a } = o.pointLabels, l = {
    extra: Zn(o) / 2,
    additionalAngle: r ? W / s : 0
  };
  let c;
  for (let h = 0; h < s; h++) {
    l.padding = e[h], l.size = t[h];
    const u = cf(i, h, l);
    n.push(u), a === "auto" && (u.visible = hf(u, c), u.visible && (c = u));
  }
  return n;
}
function df(i) {
  return i === 0 || i === 180 ? "center" : i < 180 ? "left" : "right";
}
function ff(i, t, e) {
  return e === "right" ? i -= t : e === "center" && (i -= t / 2), i;
}
function gf(i, t, e) {
  return e === 90 || e === 270 ? i -= t / 2 : (e > 270 || e < 90) && (i -= t), i;
}
function pf(i, t, e) {
  const { left: n, top: s, right: o, bottom: r } = e, { backdropColor: a } = t;
  if (!V(a)) {
    const l = be(t.borderRadius), c = ht(t.backdropPadding);
    i.fillStyle = a;
    const h = n - c.left, u = s - c.top, d = o - n + c.width, g = r - s + c.height;
    Object.values(l).some((m) => m !== 0) ? (i.beginPath(), ai(i, {
      x: h,
      y: u,
      w: d,
      h: g,
      radius: l
    }), i.fill()) : i.fillRect(h, u, d, g);
  }
}
function mf(i, t) {
  const { ctx: e, options: { pointLabels: n } } = i;
  for (let s = t - 1; s >= 0; s--) {
    const o = i._pointLabelItems[s];
    if (!o.visible)
      continue;
    const r = n.setContext(i.getPointLabelContext(s));
    pf(e, r, o);
    const a = it(r.font), { x: l, y: c, textAlign: h } = o;
    ye(e, i._pointLabels[s], l, c + a.lineHeight / 2, a, {
      color: r.color,
      textAlign: h,
      textBaseline: "middle"
    });
  }
}
function va(i, t, e, n) {
  const { ctx: s } = i;
  if (e)
    s.arc(i.xCenter, i.yCenter, t, 0, X);
  else {
    let o = i.getPointPosition(0, t);
    s.moveTo(o.x, o.y);
    for (let r = 1; r < n; r++)
      o = i.getPointPosition(r, t), s.lineTo(o.x, o.y);
  }
}
function bf(i, t, e, n, s) {
  const o = i.ctx, r = t.circular, { color: a, lineWidth: l } = t;
  !r && !n || !a || !l || e < 0 || (o.save(), o.strokeStyle = a, o.lineWidth = l, o.setLineDash(s.dash || []), o.lineDashOffset = s.dashOffset, o.beginPath(), va(i, e, r, n), o.closePath(), o.stroke(), o.restore());
}
function xf(i, t, e) {
  return se(i, {
    label: e,
    index: t,
    type: "pointLabel"
  });
}
class Ge extends tn {
  constructor(t) {
    super(t), this.xCenter = void 0, this.yCenter = void 0, this.drawingArea = void 0, this._pointLabels = [], this._pointLabelItems = [];
  }
  setDimensions() {
    const t = this._padding = ht(Zn(this.options) / 2), e = this.width = this.maxWidth - t.width, n = this.height = this.maxHeight - t.height;
    this.xCenter = Math.floor(this.left + e / 2 + t.left), this.yCenter = Math.floor(this.top + n / 2 + t.top), this.drawingArea = Math.floor(Math.min(e, n) / 2);
  }
  determineDataLimits() {
    const { min: t, max: e } = this.getMinMax(!1);
    this.min = K(t) && !isNaN(t) ? t : 0, this.max = K(e) && !isNaN(e) ? e : 0, this.handleTickRangeOptions();
  }
  computeTickLimit() {
    return Math.ceil(this.drawingArea / Zn(this.options));
  }
  generateTickLabels(t) {
    tn.prototype.generateTickLabels.call(this, t), this._pointLabels = this.getLabels().map((e, n) => {
      const s = L(this.options.pointLabels.callback, [
        e,
        n
      ], this);
      return s || s === 0 ? s : "";
    }).filter((e, n) => this.chart.getDataVisibility(n));
  }
  fit() {
    const t = this.options;
    t.display && t.pointLabels.display ? af(this) : this.setCenterPoint(0, 0, 0, 0);
  }
  setCenterPoint(t, e, n, s) {
    this.xCenter += Math.floor((t - e) / 2), this.yCenter += Math.floor((n - s) / 2), this.drawingArea -= Math.min(this.drawingArea / 2, Math.max(t, e, n, s));
  }
  getIndexAngle(t) {
    const e = X / (this._pointLabels.length || 1), n = this.options.startAngle || 0;
    return lt(t * e + Tt(n));
  }
  getDistanceFromCenterForValue(t) {
    if (V(t))
      return NaN;
    const e = this.drawingArea / (this.max - this.min);
    return this.options.reverse ? (this.max - t) * e : (t - this.min) * e;
  }
  getValueForDistanceFromCenter(t) {
    if (V(t))
      return NaN;
    const e = t / (this.drawingArea / (this.max - this.min));
    return this.options.reverse ? this.max - e : this.min + e;
  }
  getPointLabelContext(t) {
    const e = this._pointLabels || [];
    if (t >= 0 && t < e.length) {
      const n = e[t];
      return xf(this.getContext(), t, n);
    }
  }
  getPointPosition(t, e, n = 0) {
    const s = this.getIndexAngle(t) - J + n;
    return {
      x: Math.cos(s) * e + this.xCenter,
      y: Math.sin(s) * e + this.yCenter,
      angle: s
    };
  }
  getPointPositionForValue(t, e) {
    return this.getPointPosition(t, this.getDistanceFromCenterForValue(e));
  }
  getBasePosition(t) {
    return this.getPointPositionForValue(t || 0, this.getBaseValue());
  }
  getPointLabelPosition(t) {
    const { left: e, top: n, right: s, bottom: o } = this._pointLabelItems[t];
    return {
      left: e,
      top: n,
      right: s,
      bottom: o
    };
  }
  drawBackground() {
    const { backgroundColor: t, grid: { circular: e } } = this.options;
    if (t) {
      const n = this.ctx;
      n.save(), n.beginPath(), va(this, this.getDistanceFromCenterForValue(this._endValue), e, this._pointLabels.length), n.closePath(), n.fillStyle = t, n.fill(), n.restore();
    }
  }
  drawGrid() {
    const t = this.ctx, e = this.options, { angleLines: n, grid: s, border: o } = e, r = this._pointLabels.length;
    let a, l, c;
    if (e.pointLabels.display && mf(this, r), s.display && this.ticks.forEach((h, u) => {
      if (u !== 0 || u === 0 && this.min < 0) {
        l = this.getDistanceFromCenterForValue(h.value);
        const d = this.getContext(u), g = s.setContext(d), m = o.setContext(d);
        bf(this, g, l, r, m);
      }
    }), n.display) {
      for (t.save(), a = r - 1; a >= 0; a--) {
        const h = n.setContext(this.getPointLabelContext(a)), { color: u, lineWidth: d } = h;
        !d || !u || (t.lineWidth = d, t.strokeStyle = u, t.setLineDash(h.borderDash), t.lineDashOffset = h.borderDashOffset, l = this.getDistanceFromCenterForValue(e.reverse ? this.min : this.max), c = this.getPointPosition(a, l), t.beginPath(), t.moveTo(this.xCenter, this.yCenter), t.lineTo(c.x, c.y), t.stroke());
      }
      t.restore();
    }
  }
  drawBorder() {
  }
  drawLabels() {
    const t = this.ctx, e = this.options, n = e.ticks;
    if (!n.display)
      return;
    const s = this.getIndexAngle(0);
    let o, r;
    t.save(), t.translate(this.xCenter, this.yCenter), t.rotate(s), t.textAlign = "center", t.textBaseline = "middle", this.ticks.forEach((a, l) => {
      if (l === 0 && this.min >= 0 && !e.reverse)
        return;
      const c = n.setContext(this.getContext(l)), h = it(c.font);
      if (o = this.getDistanceFromCenterForValue(this.ticks[l].value), c.showLabelBackdrop) {
        t.font = h.string, r = t.measureText(a.label).width, t.fillStyle = c.backdropColor;
        const u = ht(c.backdropPadding);
        t.fillRect(-r / 2 - u.left, -o - h.size / 2 - u.top, r + u.width, h.size + u.height);
      }
      ye(t, a.label, 0, -o, h, {
        color: c.color,
        strokeColor: c.textStrokeColor,
        strokeWidth: c.textStrokeWidth
      });
    }), t.restore();
  }
  drawTitle() {
  }
}
O(Ge, "id", "radialLinear"), O(Ge, "defaults", {
  display: !0,
  animate: !0,
  position: "chartArea",
  angleLines: {
    display: !0,
    lineWidth: 1,
    borderDash: [],
    borderDashOffset: 0
  },
  grid: {
    circular: !1
  },
  startAngle: 0,
  ticks: {
    showLabelBackdrop: !0,
    callback: en.formatters.numeric
  },
  pointLabels: {
    backdropColor: void 0,
    backdropPadding: 2,
    display: !0,
    font: {
      size: 10
    },
    callback(t) {
      return t;
    },
    padding: 5,
    centerPointLabels: !1
  }
}), O(Ge, "defaultRoutes", {
  "angleLines.color": "borderColor",
  "pointLabels.color": "color",
  "ticks.color": "color"
}), O(Ge, "descriptors", {
  angleLines: {
    _fallback: "grid"
  }
});
const ln = {
  millisecond: {
    common: !0,
    size: 1,
    steps: 1e3
  },
  second: {
    common: !0,
    size: 1e3,
    steps: 60
  },
  minute: {
    common: !0,
    size: 6e4,
    steps: 60
  },
  hour: {
    common: !0,
    size: 36e5,
    steps: 24
  },
  day: {
    common: !0,
    size: 864e5,
    steps: 30
  },
  week: {
    common: !1,
    size: 6048e5,
    steps: 4
  },
  month: {
    common: !0,
    size: 2628e6,
    steps: 12
  },
  quarter: {
    common: !1,
    size: 7884e6,
    steps: 4
  },
  year: {
    common: !0,
    size: 3154e7
  }
}, mt = /* @__PURE__ */ Object.keys(ln);
function or(i, t) {
  return i - t;
}
function rr(i, t) {
  if (V(t))
    return null;
  const e = i._adapter, { parser: n, round: s, isoWeekday: o } = i._parseOpts;
  let r = t;
  return typeof n == "function" && (r = n(r)), K(r) || (r = typeof n == "string" ? e.parse(r, n) : e.parse(r)), r === null ? null : (s && (r = s === "week" && (Oe(o) || o === !0) ? e.startOf(r, "isoWeek", o) : e.startOf(r, s)), +r);
}
function ar(i, t, e, n) {
  const s = mt.length;
  for (let o = mt.indexOf(i); o < s - 1; ++o) {
    const r = ln[mt[o]], a = r.steps ? r.steps : Number.MAX_SAFE_INTEGER;
    if (r.common && Math.ceil((e - t) / (a * r.size)) <= n)
      return mt[o];
  }
  return mt[s - 1];
}
function _f(i, t, e, n, s) {
  for (let o = mt.length - 1; o >= mt.indexOf(e); o--) {
    const r = mt[o];
    if (ln[r].common && i._adapter.diff(s, n, r) >= t - 1)
      return r;
  }
  return mt[e ? mt.indexOf(e) : 0];
}
function yf(i) {
  for (let t = mt.indexOf(i) + 1, e = mt.length; t < e; ++t)
    if (ln[mt[t]].common)
      return mt[t];
}
function lr(i, t, e) {
  if (!e)
    i[t] = !0;
  else if (e.length) {
    const { lo: n, hi: s } = ls(e, t), o = e[n] >= t ? e[n] : e[s];
    i[o] = !0;
  }
}
function vf(i, t, e, n) {
  const s = i._adapter, o = +s.startOf(t[0].value, n), r = t[t.length - 1].value;
  let a, l;
  for (a = o; a <= r; a = +s.add(a, 1, n))
    l = e[a], l >= 0 && (t[l].major = !0);
  return t;
}
function cr(i, t, e) {
  const n = [], s = {}, o = t.length;
  let r, a;
  for (r = 0; r < o; ++r)
    a = t[r], s[a] = r, n.push({
      value: a,
      major: !1
    });
  return o === 0 || !e ? n : vf(i, n, s, e);
}
class hi extends ve {
  constructor(t) {
    super(t), this._cache = {
      data: [],
      labels: [],
      all: []
    }, this._unit = "day", this._majorUnit = void 0, this._offsets = {}, this._normalized = !1, this._parseOpts = void 0;
  }
  init(t, e = {}) {
    const n = t.time || (t.time = {}), s = this._adapter = new Ch._date(t.adapters.date);
    s.init(e), Ke(n.displayFormats, s.formats()), this._parseOpts = {
      parser: n.parser,
      round: n.round,
      isoWeekday: n.isoWeekday
    }, super.init(t), this._normalized = e.normalized;
  }
  parse(t, e) {
    return t === void 0 ? null : rr(this, t);
  }
  beforeLayout() {
    super.beforeLayout(), this._cache = {
      data: [],
      labels: [],
      all: []
    };
  }
  determineDataLimits() {
    const t = this.options, e = this._adapter, n = t.time.unit || "day";
    let { min: s, max: o, minDefined: r, maxDefined: a } = this.getUserBounds();
    function l(c) {
      !r && !isNaN(c.min) && (s = Math.min(s, c.min)), !a && !isNaN(c.max) && (o = Math.max(o, c.max));
    }
    (!r || !a) && (l(this._getLabelBounds()), (t.bounds !== "ticks" || t.ticks.source !== "labels") && l(this.getMinMax(!1))), s = K(s) && !isNaN(s) ? s : +e.startOf(Date.now(), n), o = K(o) && !isNaN(o) ? o : +e.endOf(Date.now(), n) + 1, this.min = Math.min(s, o - 1), this.max = Math.max(s + 1, o);
  }
  _getLabelBounds() {
    const t = this.getLabelTimestamps();
    let e = Number.POSITIVE_INFINITY, n = Number.NEGATIVE_INFINITY;
    return t.length && (e = t[0], n = t[t.length - 1]), {
      min: e,
      max: n
    };
  }
  buildTicks() {
    const t = this.options, e = t.time, n = t.ticks, s = n.source === "labels" ? this.getLabelTimestamps() : this._generate();
    t.bounds === "ticks" && s.length && (this.min = this._userMin || s[0], this.max = this._userMax || s[s.length - 1]);
    const o = this.min, r = this.max, a = Jl(s, o, r);
    return this._unit = e.unit || (n.autoSkip ? ar(e.minUnit, this.min, this.max, this._getLabelCapacity(o)) : _f(this, a.length, e.minUnit, this.min, this.max)), this._majorUnit = !n.major.enabled || this._unit === "year" ? void 0 : yf(this._unit), this.initOffsets(s), t.reverse && a.reverse(), cr(this, a, this._majorUnit);
  }
  afterAutoSkip() {
    this.options.offsetAfterAutoskip && this.initOffsets(this.ticks.map((t) => +t.value));
  }
  initOffsets(t = []) {
    let e = 0, n = 0, s, o;
    this.options.offset && t.length && (s = this.getDecimalForValue(t[0]), t.length === 1 ? e = 1 - s : e = (this.getDecimalForValue(t[1]) - s) / 2, o = this.getDecimalForValue(t[t.length - 1]), t.length === 1 ? n = o : n = (o - this.getDecimalForValue(t[t.length - 2])) / 2);
    const r = t.length < 3 ? 0.5 : 0.25;
    e = ot(e, 0, r), n = ot(n, 0, r), this._offsets = {
      start: e,
      end: n,
      factor: 1 / (e + 1 + n)
    };
  }
  _generate() {
    const t = this._adapter, e = this.min, n = this.max, s = this.options, o = s.time, r = o.unit || ar(o.minUnit, e, n, this._getLabelCapacity(e)), a = I(s.ticks.stepSize, 1), l = r === "week" ? o.isoWeekday : !1, c = Oe(l) || l === !0, h = {};
    let u = e, d, g;
    if (c && (u = +t.startOf(u, "isoWeek", l)), u = +t.startOf(u, c ? "day" : r), t.diff(n, e, r) > 1e5 * a)
      throw new Error(e + " and " + n + " are too far apart with stepSize of " + a + " " + r);
    const m = s.ticks.source === "data" && this.getDataTimestamps();
    for (d = u, g = 0; d < n; d = +t.add(d, a, r), g++)
      lr(h, d, m);
    return (d === n || s.bounds === "ticks" || g === 1) && lr(h, d, m), Object.keys(h).sort(or).map((b) => +b);
  }
  getLabelForValue(t) {
    const e = this._adapter, n = this.options.time;
    return n.tooltipFormat ? e.format(t, n.tooltipFormat) : e.format(t, n.displayFormats.datetime);
  }
  format(t, e) {
    const s = this.options.time.displayFormats, o = this._unit, r = e || s[o];
    return this._adapter.format(t, r);
  }
  _tickFormatFunction(t, e, n, s) {
    const o = this.options, r = o.ticks.callback;
    if (r)
      return L(r, [
        t,
        e,
        n
      ], this);
    const a = o.time.displayFormats, l = this._unit, c = this._majorUnit, h = l && a[l], u = c && a[c], d = n[e], g = c && u && d && d.major;
    return this._adapter.format(t, s || (g ? u : h));
  }
  generateTickLabels(t) {
    let e, n, s;
    for (e = 0, n = t.length; e < n; ++e)
      s = t[e], s.label = this._tickFormatFunction(s.value, e, t);
  }
  getDecimalForValue(t) {
    return t === null ? NaN : (t - this.min) / (this.max - this.min);
  }
  getPixelForValue(t) {
    const e = this._offsets, n = this.getDecimalForValue(t);
    return this.getPixelForDecimal((e.start + n) * e.factor);
  }
  getValueForPixel(t) {
    const e = this._offsets, n = this.getDecimalForPixel(t) / e.factor - e.end;
    return this.min + n * (this.max - this.min);
  }
  _getLabelSize(t) {
    const e = this.options.ticks, n = this.ctx.measureText(t).width, s = Tt(this.isHorizontal() ? e.maxRotation : e.minRotation), o = Math.cos(s), r = Math.sin(s), a = this._resolveTickFontOptions(0).size;
    return {
      w: n * o + a * r,
      h: n * r + a * o
    };
  }
  _getLabelCapacity(t) {
    const e = this.options.time, n = e.displayFormats, s = n[e.unit] || n.millisecond, o = this._tickFormatFunction(t, 0, cr(this, [
      t
    ], this._majorUnit), s), r = this._getLabelSize(o), a = Math.floor(this.isHorizontal() ? this.width / r.w : this.height / r.h) - 1;
    return a > 0 ? a : 1;
  }
  getDataTimestamps() {
    let t = this._cache.data || [], e, n;
    if (t.length)
      return t;
    const s = this.getMatchingVisibleMetas();
    if (this._normalized && s.length)
      return this._cache.data = s[0].controller.getAllParsedValues(this);
    for (e = 0, n = s.length; e < n; ++e)
      t = t.concat(s[e].controller.getAllParsedValues(this));
    return this._cache.data = this.normalize(t);
  }
  getLabelTimestamps() {
    const t = this._cache.labels || [];
    let e, n;
    if (t.length)
      return t;
    const s = this.getLabels();
    for (e = 0, n = s.length; e < n; ++e)
      t.push(rr(this, s[e]));
    return this._cache.labels = this._normalized ? t : this.normalize(t);
  }
  normalize(t) {
    return Rr(t.sort(or));
  }
}
O(hi, "id", "time"), O(hi, "defaults", {
  bounds: "data",
  adapters: {},
  time: {
    parser: !1,
    unit: !1,
    round: !1,
    isoWeekday: !1,
    minUnit: "millisecond",
    displayFormats: {}
  },
  ticks: {
    source: "auto",
    callback: !1,
    major: {
      enabled: !1
    }
  }
});
function Fi(i, t, e) {
  let n = 0, s = i.length - 1, o, r, a, l;
  e ? (t >= i[n].pos && t <= i[s].pos && ({ lo: n, hi: s } = Yt(i, "pos", t)), { pos: o, time: a } = i[n], { pos: r, time: l } = i[s]) : (t >= i[n].time && t <= i[s].time && ({ lo: n, hi: s } = Yt(i, "time", t)), { time: o, pos: a } = i[n], { time: r, pos: l } = i[s]);
  const c = r - o;
  return c ? a + (l - a) * (t - o) / c : a;
}
class Gn extends hi {
  constructor(t) {
    super(t), this._table = [], this._minPos = void 0, this._tableRange = void 0;
  }
  initOffsets() {
    const t = this._getTimestampsForTable(), e = this._table = this.buildLookupTable(t);
    this._minPos = Fi(e, this.min), this._tableRange = Fi(e, this.max) - this._minPos, super.initOffsets(t);
  }
  buildLookupTable(t) {
    const { min: e, max: n } = this, s = [], o = [];
    let r, a, l, c, h;
    for (r = 0, a = t.length; r < a; ++r)
      c = t[r], c >= e && c <= n && s.push(c);
    if (s.length < 2)
      return [
        {
          time: e,
          pos: 0
        },
        {
          time: n,
          pos: 1
        }
      ];
    for (r = 0, a = s.length; r < a; ++r)
      h = s[r + 1], l = s[r - 1], c = s[r], Math.round((h + l) / 2) !== c && o.push({
        time: c,
        pos: r / (a - 1)
      });
    return o;
  }
  _generate() {
    const t = this.min, e = this.max;
    let n = super.getDataTimestamps();
    return (!n.includes(t) || !n.length) && n.splice(0, 0, t), (!n.includes(e) || n.length === 1) && n.push(e), n.sort((s, o) => s - o);
  }
  _getTimestampsForTable() {
    let t = this._cache.all || [];
    if (t.length)
      return t;
    const e = this.getDataTimestamps(), n = this.getLabelTimestamps();
    return e.length && n.length ? t = this.normalize(e.concat(n)) : t = e.length ? e : n, t = this._cache.all = t, t;
  }
  getDecimalForValue(t) {
    return (Fi(this._table, t) - this._minPos) / this._tableRange;
  }
  getValueForPixel(t) {
    const e = this._offsets, n = this.getDecimalForPixel(t) / e.factor - e.end;
    return Fi(this._table, n * this._tableRange + this._minPos, !0);
  }
}
O(Gn, "id", "timeseries"), O(Gn, "defaults", hi.defaults);
var Mf = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  CategoryScale: Xn,
  LinearScale: $n,
  LogarithmicScale: qn,
  RadialLinearScale: Ge,
  TimeScale: hi,
  TimeSeriesScale: Gn
});
const Sf = [
  Ph,
  nd,
  Jd,
  Mf
], kf = U.createElement;
U.createElement;
U.Fragment;
const {
  useState: Og,
  useRef: hr,
  useEffect: Pe,
  useCallback: Dg,
  useMemo: Ag,
  forwardRef: wf,
  createRef: Eg,
  createElement: Rg,
  createContext: Lg,
  useContext: Ig,
  useReducer: Fg,
  useImperativeHandle: zg,
  useLayoutEffect: Ng,
  memo: Bg,
  Fragment: Vg,
  Children: Hg,
  isValidElement: Wg,
  cloneElement: jg
} = U, Ma = "label";
function ur(i, t) {
  typeof i == "function" ? i(t) : i && (i.current = t);
}
function Tf(i, t) {
  const e = i.options;
  e && t && Object.assign(e, t);
}
function Sa(i, t) {
  i.labels = t;
}
function ka(i, t, e = Ma) {
  const n = [];
  i.datasets = t.map((s) => {
    const o = i.datasets.find((r) => r[e] === s[e]);
    return !o || !s.data || n.includes(o) ? {
      ...s
    } : (n.push(o), Object.assign(o, s), o);
  });
}
function Pf(i, t = Ma) {
  const e = {
    labels: [],
    datasets: []
  };
  return Sa(e, i.labels), ka(e, i.datasets, t), e;
}
function Cf(i, t) {
  const { height: e = 150, width: n = 300, redraw: s = !1, datasetIdKey: o, type: r, data: a, options: l, plugins: c = [], fallbackContent: h, updateMode: u, ...d } = i, g = hr(null), m = hr(null), b = () => {
    g.current && (m.current = new xs(g.current, {
      type: r,
      data: Pf(a, o),
      options: l && {
        ...l
      },
      plugins: c
    }), ur(t, m.current));
  }, _ = () => {
    ur(t, null), m.current && (m.current.destroy(), m.current = null);
  };
  return Pe(() => {
    !s && m.current && l && Tf(m.current, l);
  }, [
    s,
    l
  ]), Pe(() => {
    !s && m.current && Sa(m.current.config.data, a.labels);
  }, [
    s,
    a.labels
  ]), Pe(() => {
    !s && m.current && a.datasets && ka(m.current.config.data, a.datasets, o);
  }, [
    s,
    a.datasets
  ]), Pe(() => {
    m.current && (s ? (_(), setTimeout(b)) : m.current.update(u));
  }, [
    s,
    l,
    a.labels,
    a.datasets,
    u
  ]), Pe(() => {
    m.current && (_(), setTimeout(b));
  }, [
    r
  ]), Pe(() => (b(), () => _()), []), /* @__PURE__ */ kf("canvas", {
    ref: g,
    role: "img",
    height: e,
    width: n,
    ...d,
    children: h
  });
}
const Of = /* @__PURE__ */ wf(Cf);
function Df(i) {
  return i && i.__esModule && Object.prototype.hasOwnProperty.call(i, "default") ? i.default : i;
}
var Rn = { exports: {} };
/*! Hammer.JS - v2.0.7 - 2016-04-22
 * http://hammerjs.github.io/
 *
 * Copyright (c) 2016 Jorik Tangelder;
 * Licensed under the MIT license */
var dr;
function Af() {
  return dr || (dr = 1, (function(i) {
    (function(t, e, n, s) {
      var o = ["", "webkit", "Moz", "MS", "ms", "o"], r = e.createElement("div"), a = "function", l = Math.round, c = Math.abs, h = Date.now;
      function u(f, p, x) {
        return setTimeout(w(f, x), p);
      }
      function d(f, p, x) {
        return Array.isArray(f) ? (g(f, x[p], x), !0) : !1;
      }
      function g(f, p, x) {
        var M;
        if (f)
          if (f.forEach)
            f.forEach(p, x);
          else if (f.length !== s)
            for (M = 0; M < f.length; )
              p.call(x, f[M], M, f), M++;
          else
            for (M in f)
              f.hasOwnProperty(M) && p.call(x, f[M], M, f);
      }
      function m(f, p, x) {
        var M = "DEPRECATED METHOD: " + p + `
` + x + ` AT 
`;
        return function() {
          var T = new Error("get-stack-trace"), A = T && T.stack ? T.stack.replace(/^[^\(]+?[\n$]/gm, "").replace(/^\s+at\s+/gm, "").replace(/^Object.<anonymous>\s*\(/gm, "{anonymous}()@") : "Unknown Stack Trace", N = t.console && (t.console.warn || t.console.log);
          return N && N.call(t.console, M, A), f.apply(this, arguments);
        };
      }
      var b;
      typeof Object.assign != "function" ? b = function(p) {
        if (p === s || p === null)
          throw new TypeError("Cannot convert undefined or null to object");
        for (var x = Object(p), M = 1; M < arguments.length; M++) {
          var T = arguments[M];
          if (T !== s && T !== null)
            for (var A in T)
              T.hasOwnProperty(A) && (x[A] = T[A]);
        }
        return x;
      } : b = Object.assign;
      var _ = m(function(p, x, M) {
        for (var T = Object.keys(x), A = 0; A < T.length; )
          (!M || M && p[T[A]] === s) && (p[T[A]] = x[T[A]]), A++;
        return p;
      }, "extend", "Use `assign`."), y = m(function(p, x) {
        return _(p, x, !0);
      }, "merge", "Use `assign`.");
      function v(f, p, x) {
        var M = p.prototype, T;
        T = f.prototype = Object.create(M), T.constructor = f, T._super = M, x && b(T, x);
      }
      function w(f, p) {
        return function() {
          return f.apply(p, arguments);
        };
      }
      function S(f, p) {
        return typeof f == a ? f.apply(p && p[0] || s, p) : f;
      }
      function k(f, p) {
        return f === s ? p : f;
      }
      function P(f, p, x) {
        g(F(p), function(M) {
          f.addEventListener(M, x, !1);
        });
      }
      function C(f, p, x) {
        g(F(p), function(M) {
          f.removeEventListener(M, x, !1);
        });
      }
      function D(f, p) {
        for (; f; ) {
          if (f == p)
            return !0;
          f = f.parentNode;
        }
        return !1;
      }
      function E(f, p) {
        return f.indexOf(p) > -1;
      }
      function F(f) {
        return f.trim().split(/\s+/g);
      }
      function R(f, p, x) {
        if (f.indexOf && !x)
          return f.indexOf(p);
        for (var M = 0; M < f.length; ) {
          if (x && f[M][x] == p || !x && f[M] === p)
            return M;
          M++;
        }
        return -1;
      }
      function z(f) {
        return Array.prototype.slice.call(f, 0);
      }
      function Q(f, p, x) {
        for (var M = [], T = [], A = 0; A < f.length; ) {
          var N = f[A][p];
          R(T, N) < 0 && M.push(f[A]), T[A] = N, A++;
        }
        return M = M.sort(function(rt, dt) {
          return rt[p] > dt[p];
        }), M;
      }
      function nt(f, p) {
        for (var x, M, T = p[0].toUpperCase() + p.slice(1), A = 0; A < o.length; ) {
          if (x = o[A], M = x ? x + T : p, M in f)
            return M;
          A++;
        }
        return s;
      }
      var j = 1;
      function Y() {
        return j++;
      }
      function $(f) {
        var p = f.ownerDocument || f;
        return p.defaultView || p.parentWindow || t;
      }
      var bt = /mobile|tablet|ip(ad|hone|od)|android/i, et = "ontouchstart" in t, Ut = nt(t, "PointerEvent") !== s, Se = et && bt.test(navigator.userAgent), _t = "touch", oe = "pen", kt = "mouse", re = "kinect", Ft = 25, ut = 1, ae = 2, tt = 4, ft = 8, gi = 1, Re = 2, Le = 4, Ie = 8, Fe = 16, Ot = Re | Le, le = Ie | Fe, Ms = Ot | le, Ss = ["x", "y"], pi = ["clientX", "clientY"];
      function yt(f, p) {
        var x = this;
        this.manager = f, this.callback = p, this.element = f.element, this.target = f.options.inputTarget, this.domHandler = function(M) {
          S(f.options.enable, [f]) && x.handler(M);
        }, this.init();
      }
      yt.prototype = {
        /**
         * should handle the inputEvent data and trigger the callback
         * @virtual
         */
        handler: function() {
        },
        /**
         * bind the events
         */
        init: function() {
          this.evEl && P(this.element, this.evEl, this.domHandler), this.evTarget && P(this.target, this.evTarget, this.domHandler), this.evWin && P($(this.element), this.evWin, this.domHandler);
        },
        /**
         * unbind the events
         */
        destroy: function() {
          this.evEl && C(this.element, this.evEl, this.domHandler), this.evTarget && C(this.target, this.evTarget, this.domHandler), this.evWin && C($(this.element), this.evWin, this.domHandler);
        }
      };
      function Na(f) {
        var p, x = f.options.inputClass;
        return x ? p = x : Ut ? p = hn : Se ? p = xi : et ? p = un : p = bi, new p(f, Ba);
      }
      function Ba(f, p, x) {
        var M = x.pointers.length, T = x.changedPointers.length, A = p & ut && M - T === 0, N = p & (tt | ft) && M - T === 0;
        x.isFirst = !!A, x.isFinal = !!N, A && (f.session = {}), x.eventType = p, Va(f, x), f.emit("hammer.input", x), f.recognize(x), f.session.prevInput = x;
      }
      function Va(f, p) {
        var x = f.session, M = p.pointers, T = M.length;
        x.firstInput || (x.firstInput = ks(p)), T > 1 && !x.firstMultiple ? x.firstMultiple = ks(p) : T === 1 && (x.firstMultiple = !1);
        var A = x.firstInput, N = x.firstMultiple, st = N ? N.center : A.center, rt = p.center = ws(M);
        p.timeStamp = h(), p.deltaTime = p.timeStamp - A.timeStamp, p.angle = cn(st, rt), p.distance = mi(st, rt), Ha(x, p), p.offsetDirection = Ps(p.deltaX, p.deltaY);
        var dt = Ts(p.deltaTime, p.deltaX, p.deltaY);
        p.overallVelocityX = dt.x, p.overallVelocityY = dt.y, p.overallVelocity = c(dt.x) > c(dt.y) ? dt.x : dt.y, p.scale = N ? Ya(N.pointers, M) : 1, p.rotation = N ? ja(N.pointers, M) : 0, p.maxPointers = x.prevInput ? p.pointers.length > x.prevInput.maxPointers ? p.pointers.length : x.prevInput.maxPointers : p.pointers.length, Wa(x, p);
        var At = f.element;
        D(p.srcEvent.target, At) && (At = p.srcEvent.target), p.target = At;
      }
      function Ha(f, p) {
        var x = p.center, M = f.offsetDelta || {}, T = f.prevDelta || {}, A = f.prevInput || {};
        (p.eventType === ut || A.eventType === tt) && (T = f.prevDelta = {
          x: A.deltaX || 0,
          y: A.deltaY || 0
        }, M = f.offsetDelta = {
          x: x.x,
          y: x.y
        }), p.deltaX = T.x + (x.x - M.x), p.deltaY = T.y + (x.y - M.y);
      }
      function Wa(f, p) {
        var x = f.lastInterval || p, M = p.timeStamp - x.timeStamp, T, A, N, st;
        if (p.eventType != ft && (M > Ft || x.velocity === s)) {
          var rt = p.deltaX - x.deltaX, dt = p.deltaY - x.deltaY, At = Ts(M, rt, dt);
          A = At.x, N = At.y, T = c(At.x) > c(At.y) ? At.x : At.y, st = Ps(rt, dt), f.lastInterval = p;
        } else
          T = x.velocity, A = x.velocityX, N = x.velocityY, st = x.direction;
        p.velocity = T, p.velocityX = A, p.velocityY = N, p.direction = st;
      }
      function ks(f) {
        for (var p = [], x = 0; x < f.pointers.length; )
          p[x] = {
            clientX: l(f.pointers[x].clientX),
            clientY: l(f.pointers[x].clientY)
          }, x++;
        return {
          timeStamp: h(),
          pointers: p,
          center: ws(p),
          deltaX: f.deltaX,
          deltaY: f.deltaY
        };
      }
      function ws(f) {
        var p = f.length;
        if (p === 1)
          return {
            x: l(f[0].clientX),
            y: l(f[0].clientY)
          };
        for (var x = 0, M = 0, T = 0; T < p; )
          x += f[T].clientX, M += f[T].clientY, T++;
        return {
          x: l(x / p),
          y: l(M / p)
        };
      }
      function Ts(f, p, x) {
        return {
          x: p / f || 0,
          y: x / f || 0
        };
      }
      function Ps(f, p) {
        return f === p ? gi : c(f) >= c(p) ? f < 0 ? Re : Le : p < 0 ? Ie : Fe;
      }
      function mi(f, p, x) {
        x || (x = Ss);
        var M = p[x[0]] - f[x[0]], T = p[x[1]] - f[x[1]];
        return Math.sqrt(M * M + T * T);
      }
      function cn(f, p, x) {
        x || (x = Ss);
        var M = p[x[0]] - f[x[0]], T = p[x[1]] - f[x[1]];
        return Math.atan2(T, M) * 180 / Math.PI;
      }
      function ja(f, p) {
        return cn(p[1], p[0], pi) + cn(f[1], f[0], pi);
      }
      function Ya(f, p) {
        return mi(p[0], p[1], pi) / mi(f[0], f[1], pi);
      }
      var Ua = {
        mousedown: ut,
        mousemove: ae,
        mouseup: tt
      }, Xa = "mousedown", $a = "mousemove mouseup";
      function bi() {
        this.evEl = Xa, this.evWin = $a, this.pressed = !1, yt.apply(this, arguments);
      }
      v(bi, yt, {
        /**
         * handle mouse events
         * @param {Object} ev
         */
        handler: function(p) {
          var x = Ua[p.type];
          x & ut && p.button === 0 && (this.pressed = !0), x & ae && p.which !== 1 && (x = tt), this.pressed && (x & tt && (this.pressed = !1), this.callback(this.manager, x, {
            pointers: [p],
            changedPointers: [p],
            pointerType: kt,
            srcEvent: p
          }));
        }
      });
      var qa = {
        pointerdown: ut,
        pointermove: ae,
        pointerup: tt,
        pointercancel: ft,
        pointerout: ft
      }, Za = {
        2: _t,
        3: oe,
        4: kt,
        5: re
        // see https://twitter.com/jacobrossi/status/480596438489890816
      }, Cs = "pointerdown", Os = "pointermove pointerup pointercancel";
      t.MSPointerEvent && !t.PointerEvent && (Cs = "MSPointerDown", Os = "MSPointerMove MSPointerUp MSPointerCancel");
      function hn() {
        this.evEl = Cs, this.evWin = Os, yt.apply(this, arguments), this.store = this.manager.session.pointerEvents = [];
      }
      v(hn, yt, {
        /**
         * handle mouse events
         * @param {Object} ev
         */
        handler: function(p) {
          var x = this.store, M = !1, T = p.type.toLowerCase().replace("ms", ""), A = qa[T], N = Za[p.pointerType] || p.pointerType, st = N == _t, rt = R(x, p.pointerId, "pointerId");
          A & ut && (p.button === 0 || st) ? rt < 0 && (x.push(p), rt = x.length - 1) : A & (tt | ft) && (M = !0), !(rt < 0) && (x[rt] = p, this.callback(this.manager, A, {
            pointers: x,
            changedPointers: [p],
            pointerType: N,
            srcEvent: p
          }), M && x.splice(rt, 1));
        }
      });
      var Ga = {
        touchstart: ut,
        touchmove: ae,
        touchend: tt,
        touchcancel: ft
      }, Ka = "touchstart", Ja = "touchstart touchmove touchend touchcancel";
      function Ds() {
        this.evTarget = Ka, this.evWin = Ja, this.started = !1, yt.apply(this, arguments);
      }
      v(Ds, yt, {
        handler: function(p) {
          var x = Ga[p.type];
          if (x === ut && (this.started = !0), !!this.started) {
            var M = Qa.call(this, p, x);
            x & (tt | ft) && M[0].length - M[1].length === 0 && (this.started = !1), this.callback(this.manager, x, {
              pointers: M[0],
              changedPointers: M[1],
              pointerType: _t,
              srcEvent: p
            });
          }
        }
      });
      function Qa(f, p) {
        var x = z(f.touches), M = z(f.changedTouches);
        return p & (tt | ft) && (x = Q(x.concat(M), "identifier")), [x, M];
      }
      var tl = {
        touchstart: ut,
        touchmove: ae,
        touchend: tt,
        touchcancel: ft
      }, el = "touchstart touchmove touchend touchcancel";
      function xi() {
        this.evTarget = el, this.targetIds = {}, yt.apply(this, arguments);
      }
      v(xi, yt, {
        handler: function(p) {
          var x = tl[p.type], M = il.call(this, p, x);
          M && this.callback(this.manager, x, {
            pointers: M[0],
            changedPointers: M[1],
            pointerType: _t,
            srcEvent: p
          });
        }
      });
      function il(f, p) {
        var x = z(f.touches), M = this.targetIds;
        if (p & (ut | ae) && x.length === 1)
          return M[x[0].identifier] = !0, [x, x];
        var T, A, N = z(f.changedTouches), st = [], rt = this.target;
        if (A = x.filter(function(dt) {
          return D(dt.target, rt);
        }), p === ut)
          for (T = 0; T < A.length; )
            M[A[T].identifier] = !0, T++;
        for (T = 0; T < N.length; )
          M[N[T].identifier] && st.push(N[T]), p & (tt | ft) && delete M[N[T].identifier], T++;
        if (st.length)
          return [
            // merge targetTouches with changedTargetTouches so it contains ALL touches, including 'end' and 'cancel'
            Q(A.concat(st), "identifier"),
            st
          ];
      }
      var nl = 2500, As = 25;
      function un() {
        yt.apply(this, arguments);
        var f = w(this.handler, this);
        this.touch = new xi(this.manager, f), this.mouse = new bi(this.manager, f), this.primaryTouch = null, this.lastTouches = [];
      }
      v(un, yt, {
        /**
         * handle mouse and touch events
         * @param {Hammer} manager
         * @param {String} inputEvent
         * @param {Object} inputData
         */
        handler: function(p, x, M) {
          var T = M.pointerType == _t, A = M.pointerType == kt;
          if (!(A && M.sourceCapabilities && M.sourceCapabilities.firesTouchEvents)) {
            if (T)
              sl.call(this, x, M);
            else if (A && ol.call(this, M))
              return;
            this.callback(p, x, M);
          }
        },
        /**
         * remove the event listeners
         */
        destroy: function() {
          this.touch.destroy(), this.mouse.destroy();
        }
      });
      function sl(f, p) {
        f & ut ? (this.primaryTouch = p.changedPointers[0].identifier, Es.call(this, p)) : f & (tt | ft) && Es.call(this, p);
      }
      function Es(f) {
        var p = f.changedPointers[0];
        if (p.identifier === this.primaryTouch) {
          var x = { x: p.clientX, y: p.clientY };
          this.lastTouches.push(x);
          var M = this.lastTouches, T = function() {
            var A = M.indexOf(x);
            A > -1 && M.splice(A, 1);
          };
          setTimeout(T, nl);
        }
      }
      function ol(f) {
        for (var p = f.srcEvent.clientX, x = f.srcEvent.clientY, M = 0; M < this.lastTouches.length; M++) {
          var T = this.lastTouches[M], A = Math.abs(p - T.x), N = Math.abs(x - T.y);
          if (A <= As && N <= As)
            return !0;
        }
        return !1;
      }
      var Rs = nt(r.style, "touchAction"), Ls = Rs !== s, Is = "compute", Fs = "auto", dn = "manipulation", ce = "none", ze = "pan-x", Ne = "pan-y", _i = al();
      function fn(f, p) {
        this.manager = f, this.set(p);
      }
      fn.prototype = {
        /**
         * set the touchAction value on the element or enable the polyfill
         * @param {String} value
         */
        set: function(f) {
          f == Is && (f = this.compute()), Ls && this.manager.element.style && _i[f] && (this.manager.element.style[Rs] = f), this.actions = f.toLowerCase().trim();
        },
        /**
         * just re-set the touchAction value
         */
        update: function() {
          this.set(this.manager.options.touchAction);
        },
        /**
         * compute the value for the touchAction property based on the recognizer's settings
         * @returns {String} value
         */
        compute: function() {
          var f = [];
          return g(this.manager.recognizers, function(p) {
            S(p.options.enable, [p]) && (f = f.concat(p.getTouchAction()));
          }), rl(f.join(" "));
        },
        /**
         * this method is called on each input cycle and provides the preventing of the browser behavior
         * @param {Object} input
         */
        preventDefaults: function(f) {
          var p = f.srcEvent, x = f.offsetDirection;
          if (this.manager.session.prevented) {
            p.preventDefault();
            return;
          }
          var M = this.actions, T = E(M, ce) && !_i[ce], A = E(M, Ne) && !_i[Ne], N = E(M, ze) && !_i[ze];
          if (T) {
            var st = f.pointers.length === 1, rt = f.distance < 2, dt = f.deltaTime < 250;
            if (st && rt && dt)
              return;
          }
          if (!(N && A) && (T || A && x & Ot || N && x & le))
            return this.preventSrc(p);
        },
        /**
         * call preventDefault to prevent the browser's default behavior (scrolling in most cases)
         * @param {Object} srcEvent
         */
        preventSrc: function(f) {
          this.manager.session.prevented = !0, f.preventDefault();
        }
      };
      function rl(f) {
        if (E(f, ce))
          return ce;
        var p = E(f, ze), x = E(f, Ne);
        return p && x ? ce : p || x ? p ? ze : Ne : E(f, dn) ? dn : Fs;
      }
      function al() {
        if (!Ls)
          return !1;
        var f = {}, p = t.CSS && t.CSS.supports;
        return ["auto", "manipulation", "pan-y", "pan-x", "pan-x pan-y", "none"].forEach(function(x) {
          f[x] = p ? t.CSS.supports("touch-action", x) : !0;
        }), f;
      }
      var yi = 1, vt = 2, ke = 4, Xt = 8, zt = Xt, Be = 16, Dt = 32;
      function Nt(f) {
        this.options = b({}, this.defaults, f || {}), this.id = Y(), this.manager = null, this.options.enable = k(this.options.enable, !0), this.state = yi, this.simultaneous = {}, this.requireFail = [];
      }
      Nt.prototype = {
        /**
         * @virtual
         * @type {Object}
         */
        defaults: {},
        /**
         * set options
         * @param {Object} options
         * @return {Recognizer}
         */
        set: function(f) {
          return b(this.options, f), this.manager && this.manager.touchAction.update(), this;
        },
        /**
         * recognize simultaneous with an other recognizer.
         * @param {Recognizer} otherRecognizer
         * @returns {Recognizer} this
         */
        recognizeWith: function(f) {
          if (d(f, "recognizeWith", this))
            return this;
          var p = this.simultaneous;
          return f = vi(f, this), p[f.id] || (p[f.id] = f, f.recognizeWith(this)), this;
        },
        /**
         * drop the simultaneous link. it doesnt remove the link on the other recognizer.
         * @param {Recognizer} otherRecognizer
         * @returns {Recognizer} this
         */
        dropRecognizeWith: function(f) {
          return d(f, "dropRecognizeWith", this) ? this : (f = vi(f, this), delete this.simultaneous[f.id], this);
        },
        /**
         * recognizer can only run when an other is failing
         * @param {Recognizer} otherRecognizer
         * @returns {Recognizer} this
         */
        requireFailure: function(f) {
          if (d(f, "requireFailure", this))
            return this;
          var p = this.requireFail;
          return f = vi(f, this), R(p, f) === -1 && (p.push(f), f.requireFailure(this)), this;
        },
        /**
         * drop the requireFailure link. it does not remove the link on the other recognizer.
         * @param {Recognizer} otherRecognizer
         * @returns {Recognizer} this
         */
        dropRequireFailure: function(f) {
          if (d(f, "dropRequireFailure", this))
            return this;
          f = vi(f, this);
          var p = R(this.requireFail, f);
          return p > -1 && this.requireFail.splice(p, 1), this;
        },
        /**
         * has require failures boolean
         * @returns {boolean}
         */
        hasRequireFailures: function() {
          return this.requireFail.length > 0;
        },
        /**
         * if the recognizer can recognize simultaneous with an other recognizer
         * @param {Recognizer} otherRecognizer
         * @returns {Boolean}
         */
        canRecognizeWith: function(f) {
          return !!this.simultaneous[f.id];
        },
        /**
         * You should use `tryEmit` instead of `emit` directly to check
         * that all the needed recognizers has failed before emitting.
         * @param {Object} input
         */
        emit: function(f) {
          var p = this, x = this.state;
          function M(T) {
            p.manager.emit(T, f);
          }
          x < Xt && M(p.options.event + zs(x)), M(p.options.event), f.additionalEvent && M(f.additionalEvent), x >= Xt && M(p.options.event + zs(x));
        },
        /**
         * Check that all the require failure recognizers has failed,
         * if true, it emits a gesture event,
         * otherwise, setup the state to FAILED.
         * @param {Object} input
         */
        tryEmit: function(f) {
          if (this.canEmit())
            return this.emit(f);
          this.state = Dt;
        },
        /**
         * can we emit?
         * @returns {boolean}
         */
        canEmit: function() {
          for (var f = 0; f < this.requireFail.length; ) {
            if (!(this.requireFail[f].state & (Dt | yi)))
              return !1;
            f++;
          }
          return !0;
        },
        /**
         * update the recognizer
         * @param {Object} inputData
         */
        recognize: function(f) {
          var p = b({}, f);
          if (!S(this.options.enable, [this, p])) {
            this.reset(), this.state = Dt;
            return;
          }
          this.state & (zt | Be | Dt) && (this.state = yi), this.state = this.process(p), this.state & (vt | ke | Xt | Be) && this.tryEmit(p);
        },
        /**
         * return the state of the recognizer
         * the actual recognizing happens in this method
         * @virtual
         * @param {Object} inputData
         * @returns {Const} STATE
         */
        process: function(f) {
        },
        // jshint ignore:line
        /**
         * return the preferred touch-action
         * @virtual
         * @returns {Array}
         */
        getTouchAction: function() {
        },
        /**
         * called when the gesture isn't allowed to recognize
         * like when another is being recognized or it is disabled
         * @virtual
         */
        reset: function() {
        }
      };
      function zs(f) {
        return f & Be ? "cancel" : f & Xt ? "end" : f & ke ? "move" : f & vt ? "start" : "";
      }
      function Ns(f) {
        return f == Fe ? "down" : f == Ie ? "up" : f == Re ? "left" : f == Le ? "right" : "";
      }
      function vi(f, p) {
        var x = p.manager;
        return x ? x.get(f) : f;
      }
      function wt() {
        Nt.apply(this, arguments);
      }
      v(wt, Nt, {
        /**
         * @namespace
         * @memberof AttrRecognizer
         */
        defaults: {
          /**
           * @type {Number}
           * @default 1
           */
          pointers: 1
        },
        /**
         * Used to check if it the recognizer receives valid input, like input.distance > 10.
         * @memberof AttrRecognizer
         * @param {Object} input
         * @returns {Boolean} recognized
         */
        attrTest: function(f) {
          var p = this.options.pointers;
          return p === 0 || f.pointers.length === p;
        },
        /**
         * Process the input and return the state for the recognizer
         * @memberof AttrRecognizer
         * @param {Object} input
         * @returns {*} State
         */
        process: function(f) {
          var p = this.state, x = f.eventType, M = p & (vt | ke), T = this.attrTest(f);
          return M && (x & ft || !T) ? p | Be : M || T ? x & tt ? p | Xt : p & vt ? p | ke : vt : Dt;
        }
      });
      function Mi() {
        wt.apply(this, arguments), this.pX = null, this.pY = null;
      }
      v(Mi, wt, {
        /**
         * @namespace
         * @memberof PanRecognizer
         */
        defaults: {
          event: "pan",
          threshold: 10,
          pointers: 1,
          direction: Ms
        },
        getTouchAction: function() {
          var f = this.options.direction, p = [];
          return f & Ot && p.push(Ne), f & le && p.push(ze), p;
        },
        directionTest: function(f) {
          var p = this.options, x = !0, M = f.distance, T = f.direction, A = f.deltaX, N = f.deltaY;
          return T & p.direction || (p.direction & Ot ? (T = A === 0 ? gi : A < 0 ? Re : Le, x = A != this.pX, M = Math.abs(f.deltaX)) : (T = N === 0 ? gi : N < 0 ? Ie : Fe, x = N != this.pY, M = Math.abs(f.deltaY))), f.direction = T, x && M > p.threshold && T & p.direction;
        },
        attrTest: function(f) {
          return wt.prototype.attrTest.call(this, f) && (this.state & vt || !(this.state & vt) && this.directionTest(f));
        },
        emit: function(f) {
          this.pX = f.deltaX, this.pY = f.deltaY;
          var p = Ns(f.direction);
          p && (f.additionalEvent = this.options.event + p), this._super.emit.call(this, f);
        }
      });
      function gn() {
        wt.apply(this, arguments);
      }
      v(gn, wt, {
        /**
         * @namespace
         * @memberof PinchRecognizer
         */
        defaults: {
          event: "pinch",
          threshold: 0,
          pointers: 2
        },
        getTouchAction: function() {
          return [ce];
        },
        attrTest: function(f) {
          return this._super.attrTest.call(this, f) && (Math.abs(f.scale - 1) > this.options.threshold || this.state & vt);
        },
        emit: function(f) {
          if (f.scale !== 1) {
            var p = f.scale < 1 ? "in" : "out";
            f.additionalEvent = this.options.event + p;
          }
          this._super.emit.call(this, f);
        }
      });
      function pn() {
        Nt.apply(this, arguments), this._timer = null, this._input = null;
      }
      v(pn, Nt, {
        /**
         * @namespace
         * @memberof PressRecognizer
         */
        defaults: {
          event: "press",
          pointers: 1,
          time: 251,
          // minimal time of the pointer to be pressed
          threshold: 9
          // a minimal movement is ok, but keep it low
        },
        getTouchAction: function() {
          return [Fs];
        },
        process: function(f) {
          var p = this.options, x = f.pointers.length === p.pointers, M = f.distance < p.threshold, T = f.deltaTime > p.time;
          if (this._input = f, !M || !x || f.eventType & (tt | ft) && !T)
            this.reset();
          else if (f.eventType & ut)
            this.reset(), this._timer = u(function() {
              this.state = zt, this.tryEmit();
            }, p.time, this);
          else if (f.eventType & tt)
            return zt;
          return Dt;
        },
        reset: function() {
          clearTimeout(this._timer);
        },
        emit: function(f) {
          this.state === zt && (f && f.eventType & tt ? this.manager.emit(this.options.event + "up", f) : (this._input.timeStamp = h(), this.manager.emit(this.options.event, this._input)));
        }
      });
      function mn() {
        wt.apply(this, arguments);
      }
      v(mn, wt, {
        /**
         * @namespace
         * @memberof RotateRecognizer
         */
        defaults: {
          event: "rotate",
          threshold: 0,
          pointers: 2
        },
        getTouchAction: function() {
          return [ce];
        },
        attrTest: function(f) {
          return this._super.attrTest.call(this, f) && (Math.abs(f.rotation) > this.options.threshold || this.state & vt);
        }
      });
      function bn() {
        wt.apply(this, arguments);
      }
      v(bn, wt, {
        /**
         * @namespace
         * @memberof SwipeRecognizer
         */
        defaults: {
          event: "swipe",
          threshold: 10,
          velocity: 0.3,
          direction: Ot | le,
          pointers: 1
        },
        getTouchAction: function() {
          return Mi.prototype.getTouchAction.call(this);
        },
        attrTest: function(f) {
          var p = this.options.direction, x;
          return p & (Ot | le) ? x = f.overallVelocity : p & Ot ? x = f.overallVelocityX : p & le && (x = f.overallVelocityY), this._super.attrTest.call(this, f) && p & f.offsetDirection && f.distance > this.options.threshold && f.maxPointers == this.options.pointers && c(x) > this.options.velocity && f.eventType & tt;
        },
        emit: function(f) {
          var p = Ns(f.offsetDirection);
          p && this.manager.emit(this.options.event + p, f), this.manager.emit(this.options.event, f);
        }
      });
      function Si() {
        Nt.apply(this, arguments), this.pTime = !1, this.pCenter = !1, this._timer = null, this._input = null, this.count = 0;
      }
      v(Si, Nt, {
        /**
         * @namespace
         * @memberof PinchRecognizer
         */
        defaults: {
          event: "tap",
          pointers: 1,
          taps: 1,
          interval: 300,
          // max time between the multi-tap taps
          time: 250,
          // max time of the pointer to be down (like finger on the screen)
          threshold: 9,
          // a minimal movement is ok, but keep it low
          posThreshold: 10
          // a multi-tap can be a bit off the initial position
        },
        getTouchAction: function() {
          return [dn];
        },
        process: function(f) {
          var p = this.options, x = f.pointers.length === p.pointers, M = f.distance < p.threshold, T = f.deltaTime < p.time;
          if (this.reset(), f.eventType & ut && this.count === 0)
            return this.failTimeout();
          if (M && T && x) {
            if (f.eventType != tt)
              return this.failTimeout();
            var A = this.pTime ? f.timeStamp - this.pTime < p.interval : !0, N = !this.pCenter || mi(this.pCenter, f.center) < p.posThreshold;
            this.pTime = f.timeStamp, this.pCenter = f.center, !N || !A ? this.count = 1 : this.count += 1, this._input = f;
            var st = this.count % p.taps;
            if (st === 0)
              return this.hasRequireFailures() ? (this._timer = u(function() {
                this.state = zt, this.tryEmit();
              }, p.interval, this), vt) : zt;
          }
          return Dt;
        },
        failTimeout: function() {
          return this._timer = u(function() {
            this.state = Dt;
          }, this.options.interval, this), Dt;
        },
        reset: function() {
          clearTimeout(this._timer);
        },
        emit: function() {
          this.state == zt && (this._input.tapCount = this.count, this.manager.emit(this.options.event, this._input));
        }
      });
      function $t(f, p) {
        return p = p || {}, p.recognizers = k(p.recognizers, $t.defaults.preset), new xn(f, p);
      }
      $t.VERSION = "2.0.7", $t.defaults = {
        /**
         * set if DOM events are being triggered.
         * But this is slower and unused by simple implementations, so disabled by default.
         * @type {Boolean}
         * @default false
         */
        domEvents: !1,
        /**
         * The value for the touchAction property/fallback.
         * When set to `compute` it will magically set the correct value based on the added recognizers.
         * @type {String}
         * @default compute
         */
        touchAction: Is,
        /**
         * @type {Boolean}
         * @default true
         */
        enable: !0,
        /**
         * EXPERIMENTAL FEATURE -- can be removed/changed
         * Change the parent input target element.
         * If Null, then it is being set the to main element.
         * @type {Null|EventTarget}
         * @default null
         */
        inputTarget: null,
        /**
         * force an input class
         * @type {Null|Function}
         * @default null
         */
        inputClass: null,
        /**
         * Default recognizer setup when calling `Hammer()`
         * When creating a new Manager these will be skipped.
         * @type {Array}
         */
        preset: [
          // RecognizerClass, options, [recognizeWith, ...], [requireFailure, ...]
          [mn, { enable: !1 }],
          [gn, { enable: !1 }, ["rotate"]],
          [bn, { direction: Ot }],
          [Mi, { direction: Ot }, ["swipe"]],
          [Si],
          [Si, { event: "doubletap", taps: 2 }, ["tap"]],
          [pn]
        ],
        /**
         * Some CSS properties can be used to improve the working of Hammer.
         * Add them to this method and they will be set when creating a new Manager.
         * @namespace
         */
        cssProps: {
          /**
           * Disables text selection to improve the dragging gesture. Mainly for desktop browsers.
           * @type {String}
           * @default 'none'
           */
          userSelect: "none",
          /**
           * Disable the Windows Phone grippers when pressing an element.
           * @type {String}
           * @default 'none'
           */
          touchSelect: "none",
          /**
           * Disables the default callout shown when you touch and hold a touch target.
           * On iOS, when you touch and hold a touch target such as a link, Safari displays
           * a callout containing information about the link. This property allows you to disable that callout.
           * @type {String}
           * @default 'none'
           */
          touchCallout: "none",
          /**
           * Specifies whether zooming is enabled. Used by IE10>
           * @type {String}
           * @default 'none'
           */
          contentZooming: "none",
          /**
           * Specifies that an entire element should be draggable instead of its contents. Mainly for desktop browsers.
           * @type {String}
           * @default 'none'
           */
          userDrag: "none",
          /**
           * Overrides the highlight color shown when the user taps a link or a JavaScript
           * clickable element in iOS. This property obeys the alpha value, if specified.
           * @type {String}
           * @default 'rgba(0,0,0,0)'
           */
          tapHighlightColor: "rgba(0,0,0,0)"
        }
      };
      var ll = 1, Bs = 2;
      function xn(f, p) {
        this.options = b({}, $t.defaults, p || {}), this.options.inputTarget = this.options.inputTarget || f, this.handlers = {}, this.session = {}, this.recognizers = [], this.oldCssProps = {}, this.element = f, this.input = Na(this), this.touchAction = new fn(this, this.options.touchAction), Vs(this, !0), g(this.options.recognizers, function(x) {
          var M = this.add(new x[0](x[1]));
          x[2] && M.recognizeWith(x[2]), x[3] && M.requireFailure(x[3]);
        }, this);
      }
      xn.prototype = {
        /**
         * set options
         * @param {Object} options
         * @returns {Manager}
         */
        set: function(f) {
          return b(this.options, f), f.touchAction && this.touchAction.update(), f.inputTarget && (this.input.destroy(), this.input.target = f.inputTarget, this.input.init()), this;
        },
        /**
         * stop recognizing for this session.
         * This session will be discarded, when a new [input]start event is fired.
         * When forced, the recognizer cycle is stopped immediately.
         * @param {Boolean} [force]
         */
        stop: function(f) {
          this.session.stopped = f ? Bs : ll;
        },
        /**
         * run the recognizers!
         * called by the inputHandler function on every movement of the pointers (touches)
         * it walks through all the recognizers and tries to detect the gesture that is being made
         * @param {Object} inputData
         */
        recognize: function(f) {
          var p = this.session;
          if (!p.stopped) {
            this.touchAction.preventDefaults(f);
            var x, M = this.recognizers, T = p.curRecognizer;
            (!T || T && T.state & zt) && (T = p.curRecognizer = null);
            for (var A = 0; A < M.length; )
              x = M[A], p.stopped !== Bs && // 1
              (!T || x == T || // 2
              x.canRecognizeWith(T)) ? x.recognize(f) : x.reset(), !T && x.state & (vt | ke | Xt) && (T = p.curRecognizer = x), A++;
          }
        },
        /**
         * get a recognizer by its event name.
         * @param {Recognizer|String} recognizer
         * @returns {Recognizer|Null}
         */
        get: function(f) {
          if (f instanceof Nt)
            return f;
          for (var p = this.recognizers, x = 0; x < p.length; x++)
            if (p[x].options.event == f)
              return p[x];
          return null;
        },
        /**
         * add a recognizer to the manager
         * existing recognizers with the same event name will be removed
         * @param {Recognizer} recognizer
         * @returns {Recognizer|Manager}
         */
        add: function(f) {
          if (d(f, "add", this))
            return this;
          var p = this.get(f.options.event);
          return p && this.remove(p), this.recognizers.push(f), f.manager = this, this.touchAction.update(), f;
        },
        /**
         * remove a recognizer by name or instance
         * @param {Recognizer|String} recognizer
         * @returns {Manager}
         */
        remove: function(f) {
          if (d(f, "remove", this))
            return this;
          if (f = this.get(f), f) {
            var p = this.recognizers, x = R(p, f);
            x !== -1 && (p.splice(x, 1), this.touchAction.update());
          }
          return this;
        },
        /**
         * bind event
         * @param {String} events
         * @param {Function} handler
         * @returns {EventEmitter} this
         */
        on: function(f, p) {
          if (f !== s && p !== s) {
            var x = this.handlers;
            return g(F(f), function(M) {
              x[M] = x[M] || [], x[M].push(p);
            }), this;
          }
        },
        /**
         * unbind event, leave emit blank to remove all handlers
         * @param {String} events
         * @param {Function} [handler]
         * @returns {EventEmitter} this
         */
        off: function(f, p) {
          if (f !== s) {
            var x = this.handlers;
            return g(F(f), function(M) {
              p ? x[M] && x[M].splice(R(x[M], p), 1) : delete x[M];
            }), this;
          }
        },
        /**
         * emit event to the listeners
         * @param {String} event
         * @param {Object} data
         */
        emit: function(f, p) {
          this.options.domEvents && cl(f, p);
          var x = this.handlers[f] && this.handlers[f].slice();
          if (!(!x || !x.length)) {
            p.type = f, p.preventDefault = function() {
              p.srcEvent.preventDefault();
            };
            for (var M = 0; M < x.length; )
              x[M](p), M++;
          }
        },
        /**
         * destroy the manager and unbinds all events
         * it doesn't unbind dom events, that is the user own responsibility
         */
        destroy: function() {
          this.element && Vs(this, !1), this.handlers = {}, this.session = {}, this.input.destroy(), this.element = null;
        }
      };
      function Vs(f, p) {
        var x = f.element;
        if (x.style) {
          var M;
          g(f.options.cssProps, function(T, A) {
            M = nt(x.style, A), p ? (f.oldCssProps[M] = x.style[M], x.style[M] = T) : x.style[M] = f.oldCssProps[M] || "";
          }), p || (f.oldCssProps = {});
        }
      }
      function cl(f, p) {
        var x = e.createEvent("Event");
        x.initEvent(f, !0, !0), x.gesture = p, p.target.dispatchEvent(x);
      }
      b($t, {
        INPUT_START: ut,
        INPUT_MOVE: ae,
        INPUT_END: tt,
        INPUT_CANCEL: ft,
        STATE_POSSIBLE: yi,
        STATE_BEGAN: vt,
        STATE_CHANGED: ke,
        STATE_ENDED: Xt,
        STATE_RECOGNIZED: zt,
        STATE_CANCELLED: Be,
        STATE_FAILED: Dt,
        DIRECTION_NONE: gi,
        DIRECTION_LEFT: Re,
        DIRECTION_RIGHT: Le,
        DIRECTION_UP: Ie,
        DIRECTION_DOWN: Fe,
        DIRECTION_HORIZONTAL: Ot,
        DIRECTION_VERTICAL: le,
        DIRECTION_ALL: Ms,
        Manager: xn,
        Input: yt,
        TouchAction: fn,
        TouchInput: xi,
        MouseInput: bi,
        PointerEventInput: hn,
        TouchMouseInput: un,
        SingleTouchInput: Ds,
        Recognizer: Nt,
        AttrRecognizer: wt,
        Tap: Si,
        Pan: Mi,
        Swipe: bn,
        Pinch: gn,
        Rotate: mn,
        Press: pn,
        on: P,
        off: C,
        each: g,
        merge: y,
        extend: _,
        assign: b,
        inherit: v,
        bindFn: w,
        prefixed: nt
      });
      var hl = typeof t < "u" ? t : typeof self < "u" ? self : {};
      hl.Hammer = $t, i.exports ? i.exports = $t : t[n] = $t;
    })(window, document, "Hammer");
  })(Rn)), Rn.exports;
}
var Ef = Af();
const ei = /* @__PURE__ */ Df(Ef);
/*!
* chartjs-plugin-zoom v2.2.0
* https://www.chartjs.org/chartjs-plugin-zoom/2.2.0/
 * (c) 2016-2024 chartjs-plugin-zoom Contributors
 * Released under the MIT License
 */
const ui = (i) => i && i.enabled && i.modifierKey, wa = (i, t) => i && t[i + "Key"], ys = (i, t) => i && !t[i + "Key"];
function ne(i, t, e) {
  return i === void 0 ? !0 : typeof i == "string" ? i.indexOf(t) !== -1 : typeof i == "function" ? i({ chart: e }).indexOf(t) !== -1 : !1;
}
function Ln(i, t) {
  return typeof i == "function" && (i = i({ chart: t })), typeof i == "string" ? { x: i.indexOf("x") !== -1, y: i.indexOf("y") !== -1 } : { x: !1, y: !1 };
}
function Rf(i, t) {
  let e;
  return function() {
    return clearTimeout(e), e = setTimeout(i, t), t;
  };
}
function Lf({ x: i, y: t }, e) {
  const n = e.scales, s = Object.keys(n);
  for (let o = 0; o < s.length; o++) {
    const r = n[s[o]];
    if (t >= r.top && t <= r.bottom && i >= r.left && i <= r.right)
      return r;
  }
  return null;
}
function Ta(i, t, e) {
  const { mode: n = "xy", scaleMode: s, overScaleMode: o } = i || {}, r = Lf(t, e), a = Ln(n, e), l = Ln(s, e);
  if (o) {
    const h = Ln(o, e);
    for (const u of ["x", "y"])
      h[u] && (l[u] = a[u], a[u] = !1);
  }
  if (r && l[r.axis])
    return [r];
  const c = [];
  return B(e.scales, function(h) {
    a[h.axis] && c.push(h);
  }), c;
}
const Kn = /* @__PURE__ */ new WeakMap();
function G(i) {
  let t = Kn.get(i);
  return t || (t = {
    originalScaleLimits: {},
    updatedScaleLimits: {},
    handlers: {},
    panDelta: {},
    dragging: !1,
    panning: !1
  }, Kn.set(i, t)), t;
}
function If(i) {
  Kn.delete(i);
}
function Pa(i, t, e, n) {
  const s = Math.max(0, Math.min(1, (i - t) / e || 0)), o = 1 - s;
  return {
    min: n * s,
    max: n * o
  };
}
function Ca(i, t) {
  const e = i.isHorizontal() ? t.x : t.y;
  return i.getValueForPixel(e);
}
function Oa(i, t, e) {
  const n = i.max - i.min, s = n * (t - 1), o = Ca(i, e);
  return Pa(o, i.min, n, s);
}
function Ff(i, t, e) {
  const n = Ca(i, e);
  if (n === void 0)
    return { min: i.min, max: i.max };
  const s = Math.log10(i.min), o = Math.log10(i.max), r = Math.log10(n), a = o - s, l = a * (t - 1), c = Pa(r, s, a, l);
  return {
    min: Math.pow(10, s + c.min),
    max: Math.pow(10, o - c.max)
  };
}
function zf(i, t) {
  return t && (t[i.id] || t[i.axis]) || {};
}
function fr(i, t, e, n, s) {
  let o = e[n];
  if (o === "original") {
    const r = i.originalScaleLimits[t.id][n];
    o = I(r.options, r.scale);
  }
  return I(o, s);
}
function Nf(i, t, e) {
  const n = i.getValueForPixel(t), s = i.getValueForPixel(e);
  return {
    min: Math.min(n, s),
    max: Math.max(n, s)
  };
}
function Bf(i, { min: t, max: e, minLimit: n, maxLimit: s }, o) {
  const r = (i - e + t) / 2;
  t -= r, e += r;
  const a = o.min.options ?? o.min.scale, l = o.max.options ?? o.max.scale, c = i / 1e6;
  return me(t, a, c) && (t = a), me(e, l, c) && (e = l), t < n ? (t = n, e = Math.min(n + i, s)) : e > s && (e = s, t = Math.max(s - i, n)), { min: t, max: e };
}
function Me(i, { min: t, max: e }, n, s = !1) {
  const o = G(i.chart), { options: r } = i, a = zf(i, n), { minRange: l = 0 } = a, c = fr(o, i, a, "min", -1 / 0), h = fr(o, i, a, "max", 1 / 0);
  if (s === "pan" && (t < c || e > h))
    return !0;
  const u = i.max - i.min, d = s ? Math.max(e - t, l) : u;
  if (s && d === l && u <= l)
    return !0;
  const g = Bf(d, { min: t, max: e, minLimit: c, maxLimit: h }, o.originalScaleLimits[i.id]);
  return r.min = g.min, r.max = g.max, o.updatedScaleLimits[i.id] = g, i.parse(g.min) !== i.min || i.parse(g.max) !== i.max;
}
function Vf(i, t, e, n) {
  const s = Oa(i, t, e), o = { min: i.min + s.min, max: i.max - s.max };
  return Me(i, o, n, !0);
}
function Hf(i, t, e, n) {
  const s = Ff(i, t, e);
  return Me(i, s, n, !0);
}
function Wf(i, t, e, n) {
  Me(i, Nf(i, t, e), n, !0);
}
const gr = (i) => i === 0 || isNaN(i) ? 0 : i < 0 ? Math.min(Math.round(i), -1) : Math.max(Math.round(i), 1);
function jf(i) {
  const e = i.getLabels().length - 1;
  i.min > 0 && (i.min -= 1), i.max < e && (i.max += 1);
}
function Yf(i, t, e, n) {
  const s = Oa(i, t, e);
  i.min === i.max && t < 1 && jf(i);
  const o = { min: i.min + gr(s.min), max: i.max - gr(s.max) };
  return Me(i, o, n, !0);
}
function Uf(i) {
  return i.isHorizontal() ? i.width : i.height;
}
function Xf(i, t, e) {
  const s = i.getLabels().length - 1;
  let { min: o, max: r } = i;
  const a = Math.max(r - o, 1), l = Math.round(Uf(i) / Math.max(a, 10)), c = Math.round(Math.abs(t / l));
  let h;
  return t < -l ? (r = Math.min(r + c, s), o = a === 1 ? r : r - a, h = r === s) : t > l && (o = Math.max(0, o - c), r = a === 1 ? o : o + a, h = o === 0), Me(i, { min: o, max: r }, e) || h;
}
const $f = {
  second: 500,
  minute: 30 * 1e3,
  hour: 1800 * 1e3,
  day: 720 * 60 * 1e3,
  week: 3.5 * 24 * 60 * 60 * 1e3,
  month: 360 * 60 * 60 * 1e3,
  quarter: 1440 * 60 * 60 * 1e3,
  year: 4368 * 60 * 60 * 1e3
};
function Da(i, t, e, n = !1) {
  const { min: s, max: o, options: r } = i, a = r.time && r.time.round, l = $f[a] || 0, c = i.getValueForPixel(i.getPixelForValue(s + l) - t), h = i.getValueForPixel(i.getPixelForValue(o + l) - t);
  return isNaN(c) || isNaN(h) ? !0 : Me(i, { min: c, max: h }, e, n ? "pan" : !1);
}
function pr(i, t, e) {
  return Da(i, t, e, !0);
}
const Jn = {
  category: Yf,
  default: Vf,
  logarithmic: Hf
}, Qn = {
  default: Wf
}, ts = {
  category: Xf,
  default: Da,
  logarithmic: pr,
  timeseries: pr
};
function qf(i, t, e) {
  const { id: n, options: { min: s, max: o } } = i;
  if (!t[n] || !e[n])
    return !0;
  const r = e[n];
  return r.min !== s || r.max !== o;
}
function mr(i, t) {
  B(i, (e, n) => {
    t[n] || delete i[n];
  });
}
function Ee(i, t) {
  const { scales: e } = i, { originalScaleLimits: n, updatedScaleLimits: s } = t;
  return B(e, function(o) {
    qf(o, n, s) && (n[o.id] = {
      min: { scale: o.min, options: o.options.min },
      max: { scale: o.max, options: o.options.max }
    });
  }), mr(n, e), mr(s, e), n;
}
function br(i, t, e, n) {
  const s = Jn[i.type] || Jn.default;
  L(s, [i, t, e, n]);
}
function xr(i, t, e, n) {
  const s = Qn[i.type] || Qn.default;
  L(s, [i, t, e, n]);
}
function Zf(i) {
  const t = i.chartArea;
  return {
    x: (t.left + t.right) / 2,
    y: (t.top + t.bottom) / 2
  };
}
function vs(i, t, e = "none", n = "api") {
  const { x: s = 1, y: o = 1, focalPoint: r = Zf(i) } = typeof t == "number" ? { x: t, y: t } : t, a = G(i), { options: { limits: l, zoom: c } } = a;
  Ee(i, a);
  const h = s !== 1, u = o !== 1, d = Ta(c, r, i);
  B(d || i.scales, function(g) {
    g.isHorizontal() && h ? br(g, s, r, l) : !g.isHorizontal() && u && br(g, o, r, l);
  }), i.update(e), L(c.onZoom, [{ chart: i, trigger: n }]);
}
function Aa(i, t, e, n = "none", s = "api") {
  const o = G(i), { options: { limits: r, zoom: a } } = o, { mode: l = "xy" } = a;
  Ee(i, o);
  const c = ne(l, "x", i), h = ne(l, "y", i);
  B(i.scales, function(u) {
    u.isHorizontal() && c ? xr(u, t.x, e.x, r) : !u.isHorizontal() && h && xr(u, t.y, e.y, r);
  }), i.update(n), L(a.onZoom, [{ chart: i, trigger: s }]);
}
function Gf(i, t, e, n = "none", s = "api") {
  var a;
  const o = G(i);
  Ee(i, o);
  const r = i.scales[t];
  Me(r, e, void 0, !0), i.update(n), L((a = o.options.zoom) == null ? void 0 : a.onZoom, [{ chart: i, trigger: s }]);
}
function Kf(i, t = "default") {
  const e = G(i), n = Ee(i, e);
  B(i.scales, function(s) {
    const o = s.options;
    n[s.id] ? (o.min = n[s.id].min.options, o.max = n[s.id].max.options) : (delete o.min, delete o.max), delete e.updatedScaleLimits[s.id];
  }), i.update(t), L(e.options.zoom.onZoomComplete, [{ chart: i }]);
}
function Jf(i, t) {
  const e = i.originalScaleLimits[t];
  if (!e)
    return;
  const { min: n, max: s } = e;
  return I(s.options, s.scale) - I(n.options, n.scale);
}
function Qf(i) {
  const t = G(i);
  let e = 1, n = 1;
  return B(i.scales, function(s) {
    const o = Jf(t, s.id);
    if (o) {
      const r = Math.round(o / (s.max - s.min) * 100) / 100;
      e = Math.min(e, r), n = Math.max(n, r);
    }
  }), e < 1 ? e : n;
}
function _r(i, t, e, n) {
  const { panDelta: s } = n, o = s[i.id] || 0;
  St(o) === St(t) && (t += o);
  const r = ts[i.type] || ts.default;
  L(r, [i, t, e]) ? s[i.id] = 0 : s[i.id] = t;
}
function Ea(i, t, e, n = "none") {
  const { x: s = 0, y: o = 0 } = typeof t == "number" ? { x: t, y: t } : t, r = G(i), { options: { pan: a, limits: l } } = r, { onPan: c } = a || {};
  Ee(i, r);
  const h = s !== 0, u = o !== 0;
  B(e || i.scales, function(d) {
    d.isHorizontal() && h ? _r(d, s, l, r) : !d.isHorizontal() && u && _r(d, o, l, r);
  }), i.update(n), L(c, [{ chart: i }]);
}
function Ra(i) {
  const t = G(i);
  Ee(i, t);
  const e = {};
  for (const n of Object.keys(i.scales)) {
    const { min: s, max: o } = t.originalScaleLimits[n] || { min: {}, max: {} };
    e[n] = { min: s.scale, max: o.scale };
  }
  return e;
}
function tg(i) {
  const t = G(i), e = {};
  for (const n of Object.keys(i.scales))
    e[n] = t.updatedScaleLimits[n];
  return e;
}
function eg(i) {
  const t = Ra(i);
  for (const e of Object.keys(i.scales)) {
    const { min: n, max: s } = t[e];
    if (n !== void 0 && i.scales[e].min !== n || s !== void 0 && i.scales[e].max !== s)
      return !0;
  }
  return !1;
}
function yr(i) {
  const t = G(i);
  return t.panning || t.dragging;
}
const vr = (i, t, e) => Math.min(e, Math.max(t, i));
function pt(i, t) {
  const { handlers: e } = G(i), n = e[t];
  n && n.target && (n.target.removeEventListener(t, n), delete e[t]);
}
function ii(i, t, e, n) {
  const { handlers: s, options: o } = G(i), r = s[e];
  if (r && r.target === t)
    return;
  pt(i, e), s[e] = (l) => n(i, l, o), s[e].target = t;
  const a = e === "wheel" ? !1 : void 0;
  t.addEventListener(e, s[e], { passive: a });
}
function ig(i, t) {
  const e = G(i);
  e.dragStart && (e.dragging = !0, e.dragEnd = t, i.update("none"));
}
function ng(i, t) {
  const e = G(i);
  !e.dragStart || t.key !== "Escape" || (pt(i, "keydown"), e.dragging = !1, e.dragStart = e.dragEnd = null, i.update("none"));
}
function es(i, t) {
  if (i.target !== t.canvas) {
    const e = t.canvas.getBoundingClientRect();
    return {
      x: i.clientX - e.left,
      y: i.clientY - e.top
    };
  }
  return Lt(i, t);
}
function La(i, t, e) {
  const { onZoomStart: n, onZoomRejected: s } = e;
  if (n) {
    const o = es(t, i);
    if (L(n, [{ chart: i, event: t, point: o }]) === !1)
      return L(s, [{ chart: i, event: t }]), !1;
  }
}
function sg(i, t) {
  if (i.legend) {
    const o = Lt(t, i);
    if (It(o, i.legend))
      return;
  }
  const e = G(i), { pan: n, zoom: s = {} } = e.options;
  if (t.button !== 0 || wa(ui(n), t) || ys(ui(s.drag), t))
    return L(s.onZoomRejected, [{ chart: i, event: t }]);
  La(i, t, s) !== !1 && (e.dragStart = t, ii(i, i.canvas.ownerDocument, "mousemove", ig), ii(i, window.document, "keydown", ng));
}
function og({ begin: i, end: t }, e) {
  let n = t.x - i.x, s = t.y - i.y;
  const o = Math.abs(n / s);
  o > e ? n = Math.sign(n) * Math.abs(s * e) : o < e && (s = Math.sign(s) * Math.abs(n / e)), t.x = i.x + n, t.y = i.y + s;
}
function Mr(i, t, e, { min: n, max: s, prop: o }) {
  i[n] = vr(Math.min(e.begin[o], e.end[o]), t[n], t[s]), i[s] = vr(Math.max(e.begin[o], e.end[o]), t[n], t[s]);
}
function rg(i, t, e) {
  const n = {
    begin: es(t.dragStart, i),
    end: es(t.dragEnd, i)
  };
  if (e) {
    const s = i.chartArea.width / i.chartArea.height;
    og(n, s);
  }
  return n;
}
function Ia(i, t, e, n) {
  const s = ne(t, "x", i), o = ne(t, "y", i), { top: r, left: a, right: l, bottom: c, width: h, height: u } = i.chartArea, d = { top: r, left: a, right: l, bottom: c }, g = rg(i, e, n && s && o);
  s && Mr(d, i.chartArea, g, { min: "left", max: "right", prop: "x" }), o && Mr(d, i.chartArea, g, { min: "top", max: "bottom", prop: "y" });
  const m = d.right - d.left, b = d.bottom - d.top;
  return {
    ...d,
    width: m,
    height: b,
    zoomX: s && m ? 1 + (h - m) / h : 1,
    zoomY: o && b ? 1 + (u - b) / u : 1
  };
}
function ag(i, t) {
  const e = G(i);
  if (!e.dragStart)
    return;
  pt(i, "mousemove");
  const { mode: n, onZoomComplete: s, drag: { threshold: o = 0, maintainAspectRatio: r } } = e.options.zoom, a = Ia(i, n, { dragStart: e.dragStart, dragEnd: t }, r), l = ne(n, "x", i) ? a.width : 0, c = ne(n, "y", i) ? a.height : 0, h = Math.sqrt(l * l + c * c);
  if (e.dragStart = e.dragEnd = null, h <= o) {
    e.dragging = !1, i.update("none");
    return;
  }
  Aa(i, { x: a.left, y: a.top }, { x: a.right, y: a.bottom }, "zoom", "drag"), e.dragging = !1, e.filterNextClick = !0, L(s, [{ chart: i }]);
}
function lg(i, t, e) {
  if (ys(ui(e.wheel), t)) {
    L(e.onZoomRejected, [{ chart: i, event: t }]);
    return;
  }
  if (La(i, t, e) !== !1 && (t.cancelable && t.preventDefault(), t.deltaY !== void 0))
    return !0;
}
function cg(i, t) {
  const { handlers: { onZoomComplete: e }, options: { zoom: n } } = G(i);
  if (!lg(i, t, n))
    return;
  const s = t.target.getBoundingClientRect(), o = n.wheel.speed, r = t.deltaY >= 0 ? 2 - 1 / (1 - o) : 1 + o, a = {
    x: r,
    y: r,
    focalPoint: {
      x: t.clientX - s.left,
      y: t.clientY - s.top
    }
  };
  vs(i, a, "zoom", "wheel"), L(e, [{ chart: i }]);
}
function hg(i, t, e, n) {
  e && (G(i).handlers[t] = Rf(() => L(e, [{ chart: i }]), n));
}
function ug(i, t) {
  const e = i.canvas, { wheel: n, drag: s, onZoomComplete: o } = t.zoom;
  n.enabled ? (ii(i, e, "wheel", cg), hg(i, "onZoomComplete", o, 250)) : pt(i, "wheel"), s.enabled ? (ii(i, e, "mousedown", sg), ii(i, e.ownerDocument, "mouseup", ag)) : (pt(i, "mousedown"), pt(i, "mousemove"), pt(i, "mouseup"), pt(i, "keydown"));
}
function dg(i) {
  pt(i, "mousedown"), pt(i, "mousemove"), pt(i, "mouseup"), pt(i, "wheel"), pt(i, "click"), pt(i, "keydown");
}
function fg(i, t) {
  return function(e, n) {
    const { pan: s, zoom: o = {} } = t.options;
    if (!s || !s.enabled)
      return !1;
    const r = n && n.srcEvent;
    return r && !t.panning && n.pointerType === "mouse" && (ys(ui(s), r) || wa(ui(o.drag), r)) ? (L(s.onPanRejected, [{ chart: i, event: n }]), !1) : !0;
  };
}
function gg(i, t) {
  const e = Math.abs(i.clientX - t.clientX), n = Math.abs(i.clientY - t.clientY), s = e / n;
  let o, r;
  return s > 0.3 && s < 1.7 ? o = r = !0 : e > n ? o = !0 : r = !0, { x: o, y: r };
}
function Fa(i, t, e) {
  if (t.scale) {
    const { center: n, pointers: s } = e, o = 1 / t.scale * e.scale, r = e.target.getBoundingClientRect(), a = gg(s[0], s[1]), l = t.options.zoom.mode, c = {
      x: a.x && ne(l, "x", i) ? o : 1,
      y: a.y && ne(l, "y", i) ? o : 1,
      focalPoint: {
        x: n.x - r.left,
        y: n.y - r.top
      }
    };
    vs(i, c, "zoom", "pinch"), t.scale = e.scale;
  }
}
function pg(i, t, e) {
  if (t.options.zoom.pinch.enabled) {
    const n = Lt(e, i);
    L(t.options.zoom.onZoomStart, [{ chart: i, event: e, point: n }]) === !1 ? (t.scale = null, L(t.options.zoom.onZoomRejected, [{ chart: i, event: e }])) : t.scale = 1;
  }
}
function mg(i, t, e) {
  t.scale && (Fa(i, t, e), t.scale = null, L(t.options.zoom.onZoomComplete, [{ chart: i }]));
}
function za(i, t, e) {
  const n = t.delta;
  n && (t.panning = !0, Ea(i, { x: e.deltaX - n.x, y: e.deltaY - n.y }, t.panScales), t.delta = { x: e.deltaX, y: e.deltaY });
}
function bg(i, t, e) {
  const { enabled: n, onPanStart: s, onPanRejected: o } = t.options.pan;
  if (!n)
    return;
  const r = e.target.getBoundingClientRect(), a = {
    x: e.center.x - r.left,
    y: e.center.y - r.top
  };
  if (L(s, [{ chart: i, event: e, point: a }]) === !1)
    return L(o, [{ chart: i, event: e }]);
  t.panScales = Ta(t.options.pan, a, i), t.delta = { x: 0, y: 0 }, za(i, t, e);
}
function xg(i, t) {
  t.delta = null, t.panning && (t.panning = !1, t.filterNextClick = !0, L(t.options.pan.onPanComplete, [{ chart: i }]));
}
const is = /* @__PURE__ */ new WeakMap();
function Sr(i, t) {
  const e = G(i), n = i.canvas, { pan: s, zoom: o } = t, r = new ei.Manager(n);
  o && o.pinch.enabled && (r.add(new ei.Pinch()), r.on("pinchstart", (a) => pg(i, e, a)), r.on("pinch", (a) => Fa(i, e, a)), r.on("pinchend", (a) => mg(i, e, a))), s && s.enabled && (r.add(new ei.Pan({
    threshold: s.threshold,
    enable: fg(i, e)
  })), r.on("panstart", (a) => bg(i, e, a)), r.on("panmove", (a) => za(i, e, a)), r.on("panend", () => xg(i, e))), is.set(i, r);
}
function kr(i) {
  const t = is.get(i);
  t && (t.remove("pinchstart"), t.remove("pinch"), t.remove("pinchend"), t.remove("panstart"), t.remove("pan"), t.remove("panend"), t.destroy(), is.delete(i));
}
function _g(i, t) {
  var r, a, l, c;
  const { pan: e, zoom: n } = i, { pan: s, zoom: o } = t;
  return ((a = (r = n == null ? void 0 : n.zoom) == null ? void 0 : r.pinch) == null ? void 0 : a.enabled) !== ((c = (l = o == null ? void 0 : o.zoom) == null ? void 0 : l.pinch) == null ? void 0 : c.enabled) || (e == null ? void 0 : e.enabled) !== (s == null ? void 0 : s.enabled) || (e == null ? void 0 : e.threshold) !== (s == null ? void 0 : s.threshold);
}
var yg = "2.2.0";
function zi(i, t, e) {
  const n = e.zoom.drag, { dragStart: s, dragEnd: o } = G(i);
  if (n.drawTime !== t || !o)
    return;
  const { left: r, top: a, width: l, height: c } = Ia(i, e.zoom.mode, { dragStart: s, dragEnd: o }, n.maintainAspectRatio), h = i.ctx;
  h.save(), h.beginPath(), h.fillStyle = n.backgroundColor || "rgba(225,225,225,0.3)", h.fillRect(r, a, l, c), n.borderWidth > 0 && (h.lineWidth = n.borderWidth, h.strokeStyle = n.borderColor || "rgba(225,225,225)", h.strokeRect(r, a, l, c)), h.restore();
}
var vg = {
  id: "zoom",
  version: yg,
  defaults: {
    pan: {
      enabled: !1,
      mode: "xy",
      threshold: 10,
      modifierKey: null
    },
    zoom: {
      wheel: {
        enabled: !1,
        speed: 0.1,
        modifierKey: null
      },
      drag: {
        enabled: !1,
        drawTime: "beforeDatasetsDraw",
        modifierKey: null
      },
      pinch: {
        enabled: !1
      },
      mode: "xy"
    }
  },
  start: function(i, t, e) {
    const n = G(i);
    n.options = e, Object.prototype.hasOwnProperty.call(e.zoom, "enabled") && console.warn("The option `zoom.enabled` is no longer supported. Please use `zoom.wheel.enabled`, `zoom.drag.enabled`, or `zoom.pinch.enabled`."), (Object.prototype.hasOwnProperty.call(e.zoom, "overScaleMode") || Object.prototype.hasOwnProperty.call(e.pan, "overScaleMode")) && console.warn("The option `overScaleMode` is deprecated. Please use `scaleMode` instead (and update `mode` as desired)."), ei && Sr(i, e), i.pan = (s, o, r) => Ea(i, s, o, r), i.zoom = (s, o) => vs(i, s, o), i.zoomRect = (s, o, r) => Aa(i, s, o, r), i.zoomScale = (s, o, r) => Gf(i, s, o, r), i.resetZoom = (s) => Kf(i, s), i.getZoomLevel = () => Qf(i), i.getInitialScaleBounds = () => Ra(i), i.getZoomedScaleBounds = () => tg(i), i.isZoomedOrPanned = () => eg(i), i.isZoomingOrPanning = () => yr(i);
  },
  beforeEvent(i, { event: t }) {
    if (yr(i))
      return !1;
    if (t.type === "click" || t.type === "mouseup") {
      const e = G(i);
      if (e.filterNextClick)
        return e.filterNextClick = !1, !1;
    }
  },
  beforeUpdate: function(i, t, e) {
    const n = G(i), s = n.options;
    n.options = e, _g(s, e) && (kr(i), Sr(i, e)), ug(i, e);
  },
  beforeDatasetsDraw(i, t, e) {
    zi(i, "beforeDatasetsDraw", e);
  },
  afterDatasetsDraw(i, t, e) {
    zi(i, "afterDatasetsDraw", e);
  },
  beforeDraw(i, t, e) {
    zi(i, "beforeDraw", e);
  },
  afterDraw(i, t, e) {
    zi(i, "afterDraw", e);
  },
  stop: function(i) {
    dg(i), ei && kr(i), If(i);
  },
  panFunctions: ts,
  zoomFunctions: Jn,
  zoomRectFunctions: Qn
};
function Mg(i, t) {
  return U.useMemo(() => {
    var o, r;
    if (!i || !t) return i;
    const e = { ...i }, n = e.data ? { ...e.data } : {};
    if (t.palette && n.datasets) {
      const a = t.palette, l = e.type === "pie" || e.type === "doughnut" || e.type === "polarArea";
      n.datasets = n.datasets.map((c, h) => {
        const u = { ...c };
        if (!u.backgroundColor)
          if (l || c.type === "pie" || c.type === "doughnut" || c.type === "polarArea") {
            const d = Array.isArray(u.data) ? u.data.length : 0;
            u.backgroundColor = Array.from(
              { length: d },
              (g, m) => a[m % a.length] + "cc"
            ), u.borderColor || (u.borderColor = Array.from(
              { length: d },
              (g, m) => a[m % a.length]
            ));
          } else {
            const d = a[h % a.length];
            e.type === "line" || e.type === "radar" || c.type === "line" || c.type === "radar" ? (u.backgroundColor = d + "33", u.fill === void 0 && (u.fill = !1)) : u.backgroundColor = d + "cc", u.borderColor || (u.borderColor = d);
          }
        return u;
      });
    }
    e.data = n;
    const s = { ...e.options };
    if (t.textColor || t.gridColor) {
      const a = { ...s.scales || {} };
      for (const l of Object.keys(a))
        a[l] = { ...a[l] }, t.gridColor && !((o = a[l].grid) != null && o.color) && (a[l].grid = { ...a[l].grid, color: t.gridColor }), t.textColor && !((r = a[l].ticks) != null && r.color) && (a[l].ticks = { ...a[l].ticks, color: t.textColor });
      s.scales = a;
    }
    return e.options = s, e;
  }, [i, t]);
}
function Sg(i) {
  const t = fl(), e = (i == null ? void 0 : i.datasets.some((g) => g.clickable)) ?? !1, n = (i == null ? void 0 : i.datasets.some((g) => g.legendClickable)) ?? !1, s = (i == null ? void 0 : i.datasets.some((g) => g.hasTooltip)) ?? !1, [o, r] = U.useState({ x: 0, y: 0, visible: !1, datasetIndex: -1, index: -1 }), a = U.useCallback((g, m) => {
    if (m.length > 0) {
      const { datasetIndex: b, index: _ } = m[0];
      t("elementClick", { datasetIndex: b, index: _, zone: "datapoint" });
    }
  }, [t]), l = U.useCallback((g, m, b) => {
    const _ = b.chart, y = m.datasetIndex;
    _.isDatasetVisible(y) ? (_.hide(y), m.hidden = !0) : (_.show(y), m.hidden = !1), t("elementClick", { datasetIndex: y, index: -1, zone: "legend" });
  }, [t]), c = U.useRef(null), h = U.useCallback((g, m, b, _) => {
    r({ x: b, y: _, visible: !0, datasetIndex: g, index: m }), c.current != null && clearTimeout(c.current), c.current = window.setTimeout(() => {
      t("tooltip", { datasetIndex: g, index: m }), c.current = null;
    }, 200);
  }, [t]), u = U.useCallback(() => {
    c.current != null && (clearTimeout(c.current), c.current = null), r((g) => ({ ...g, visible: !1 }));
  }, []);
  return U.useEffect(() => () => {
    c.current != null && clearTimeout(c.current);
  }, []), { ...U.useMemo(() => {
    const g = {};
    return e && (g.onClick = a), n && (g.plugins = { legend: { onClick: l } }), { callbacks: g, hasAnyTooltip: s, onTooltip: h, onTooltipHide: u };
  }, [e, n, s, a, l, h, u]), tooltipPos: o };
}
const kg = ({ content: i, position: t }) => !t.visible || !i || !i.html || i.datasetIndex !== t.datasetIndex || i.index !== t.index ? null : /* @__PURE__ */ U.createElement(
  "div",
  {
    className: "tlReactChart__tooltip",
    style: { left: t.x + "px", top: t.y - 10 + "px" },
    dangerouslySetInnerHTML: { __html: i.html }
  }
);
xs.register(...Sf, vg);
const wg = ({ controlId: i, state: t }) => {
  var v, w;
  const e = U.useRef(null), n = t.chartConfig, s = t.interactions, o = t.themeColors, r = t.zoomEnabled, a = t.cssClass, l = t.error, c = t.noDataMessage, h = t.tooltipContent, u = Mg(n, o), { callbacks: d, hasAnyTooltip: g, onTooltip: m, onTooltipHide: b, tooltipPos: _ } = Sg(s), y = U.useMemo(() => {
    var k, P, C;
    if (!u) return {};
    const S = { ...u.options };
    return S.responsive = !0, S.maintainAspectRatio = !1, d.onClick && (S.onClick = d.onClick), (k = d.plugins) != null && k.legend && (S.plugins = {
      ...S.plugins,
      legend: { ...(P = S.plugins) == null ? void 0 : P.legend, ...d.plugins.legend }
    }), r && (S.plugins = {
      ...S.plugins,
      zoom: {
        zoom: { wheel: { enabled: !0 }, pinch: { enabled: !0 }, mode: "xy" },
        pan: { enabled: !0, mode: "xy" }
      }
    }), g && (S.plugins = {
      ...S.plugins,
      tooltip: {
        ...(C = S.plugins) == null ? void 0 : C.tooltip,
        enabled: !1,
        external: (D) => {
          var F;
          const E = D.tooltip;
          if (E.opacity === 0)
            b();
          else if (((F = E.dataPoints) == null ? void 0 : F.length) > 0) {
            const R = E.dataPoints[0];
            m(R.datasetIndex, R.dataIndex, E.caretX, E.caretY);
          }
        }
      }
    }), S;
  }, [u, d, r, g, m]);
  return l ? /* @__PURE__ */ U.createElement("div", { id: i, className: "tlReactChart tlReactChart--error " + (a || "") }, /* @__PURE__ */ U.createElement("div", { className: "tlReactChart__error" }, l)) : !((w = (v = u == null ? void 0 : u.data) == null ? void 0 : v.datasets) != null && w.length) && c ? /* @__PURE__ */ U.createElement("div", { id: i, className: "tlReactChart tlReactChart--noData " + (a || "") }, /* @__PURE__ */ U.createElement("div", { className: "tlReactChart__noData" }, c)) : u ? /* @__PURE__ */ U.createElement("div", { id: i, className: "tlReactChart " + (a || ""), "aria-label": `${u.type} chart` }, /* @__PURE__ */ U.createElement(
    Of,
    {
      ref: e,
      type: u.type,
      data: u.data,
      options: y
    }
  ), r && /* @__PURE__ */ U.createElement(
    "button",
    {
      className: "tlReactChart__zoomReset",
      onClick: () => {
        var S;
        return (S = e.current) == null ? void 0 : S.resetZoom();
      }
    },
    "Reset Zoom"
  ), /* @__PURE__ */ U.createElement(kg, { content: h, position: _ })) : /* @__PURE__ */ U.createElement("div", { id: i, className: "tlReactChart " + (a || "") });
};
gl("TLChart", wg);
