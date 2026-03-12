import { React as xl, useTLState as Nh, useTLCommand as Vh, register as Wh } from "tl-react-bridge";
let yn = [], wl = [];
(() => {
  let n = "lc,34,7n,7,7b,19,,,,2,,2,,,20,b,1c,l,g,,2t,7,2,6,2,2,,4,z,,u,r,2j,b,1m,9,9,,o,4,,9,,3,,5,17,3,3b,f,,w,1j,,,,4,8,4,,3,7,a,2,t,,1m,,,,2,4,8,,9,,a,2,q,,2,2,1l,,4,2,4,2,2,3,3,,u,2,3,,b,2,1l,,4,5,,2,4,,k,2,m,6,,,1m,,,2,,4,8,,7,3,a,2,u,,1n,,,,c,,9,,14,,3,,1l,3,5,3,,4,7,2,b,2,t,,1m,,2,,2,,3,,5,2,7,2,b,2,s,2,1l,2,,,2,4,8,,9,,a,2,t,,20,,4,,2,3,,,8,,29,,2,7,c,8,2q,,2,9,b,6,22,2,r,,,,,,1j,e,,5,,2,5,b,,10,9,,2u,4,,6,,2,2,2,p,2,4,3,g,4,d,,2,2,6,,f,,jj,3,qa,3,t,3,t,2,u,2,1s,2,,7,8,,2,b,9,,19,3,3b,2,y,,3a,3,4,2,9,,6,3,63,2,2,,1m,,,7,,,,,2,8,6,a,2,,1c,h,1r,4,1c,7,,,5,,14,9,c,2,w,4,2,2,,3,1k,,,2,3,,,3,1m,8,2,2,48,3,,d,,7,4,,6,,3,2,5i,1m,,5,ek,,5f,x,2da,3,3x,,2o,w,fe,6,2x,2,n9w,4,,a,w,2,28,2,7k,,3,,4,,p,2,5,,47,2,q,i,d,,12,8,p,b,1a,3,1c,,2,4,2,2,13,,1v,6,2,2,2,2,c,,8,,1b,,1f,,,3,2,2,5,2,,,16,2,8,,6m,,2,,4,,fn4,,kh,g,g,g,a6,2,gt,,6a,,45,5,1ae,3,,2,5,4,14,3,4,,4l,2,fx,4,ar,2,49,b,4w,,1i,f,1k,3,1d,4,2,2,1x,3,10,5,,8,1q,,c,2,1g,9,a,4,2,,2n,3,2,,,2,6,,4g,,3,8,l,2,1l,2,,,,,m,,e,7,3,5,5f,8,2,3,,,n,,29,,2,6,,,2,,,2,,2,6j,,2,4,6,2,,2,r,2,2d,8,2,,,2,2y,,,,2,6,,,2t,3,2,4,,5,77,9,,2,6t,,a,2,,,4,,40,4,2,2,4,,w,a,14,6,2,4,8,,9,6,2,3,1a,d,,2,ba,7,,6,,,2a,m,2,7,,2,,2,3e,6,3,,,2,,7,,,20,2,3,,,,9n,2,f0b,5,1n,7,t4,,1r,4,29,,f5k,2,43q,,,3,4,5,8,8,2,7,u,4,44,3,1iz,1j,4,1e,8,,e,,m,5,,f,11s,7,,h,2,7,,2,,5,79,7,c5,4,15s,7,31,7,240,5,gx7k,2o,3k,6o".split(",").map((t) => t ? parseInt(t, 36) : 1);
  for (let t = 0, e = 0; t < n.length; t++)
    (t % 2 ? wl : yn).push(e = e + n[t]);
})();
function Hh(n) {
  if (n < 768) return !1;
  for (let t = 0, e = yn.length; ; ) {
    let i = t + e >> 1;
    if (n < yn[i]) e = i;
    else if (n >= wl[i]) t = i + 1;
    else return !0;
    if (t == e) return !1;
  }
}
function Fr(n) {
  return n >= 127462 && n <= 127487;
}
const zr = 8205;
function Fh(n, t, e = !0, i = !0) {
  return (e ? kl : zh)(n, t, i);
}
function kl(n, t, e) {
  if (t == n.length) return t;
  t && vl(n.charCodeAt(t)) && Sl(n.charCodeAt(t - 1)) && t--;
  let i = Xs(n, t);
  for (t += Qr(i); t < n.length; ) {
    let s = Xs(n, t);
    if (i == zr || s == zr || e && Hh(s))
      t += Qr(s), i = s;
    else if (Fr(s)) {
      let r = 0, o = t - 2;
      for (; o >= 0 && Fr(Xs(n, o)); )
        r++, o -= 2;
      if (r % 2 == 0) break;
      t += 2;
    } else
      break;
  }
  return t;
}
function zh(n, t, e) {
  for (; t > 0; ) {
    let i = kl(n, t - 2, e);
    if (i < t) return i;
    t--;
  }
  return 0;
}
function Xs(n, t) {
  let e = n.charCodeAt(t);
  if (!Sl(e) || t + 1 == n.length) return e;
  let i = n.charCodeAt(t + 1);
  return vl(i) ? (e - 55296 << 10) + (i - 56320) + 65536 : e;
}
function vl(n) {
  return n >= 56320 && n < 57344;
}
function Sl(n) {
  return n >= 55296 && n < 56320;
}
function Qr(n) {
  return n < 65536 ? 1 : 2;
}
class I {
  /**
  Get the line description around the given position.
  */
  lineAt(t) {
    if (t < 0 || t > this.length)
      throw new RangeError(`Invalid position ${t} in document of length ${this.length}`);
    return this.lineInner(t, !1, 1, 0);
  }
  /**
  Get the description for the given (1-based) line number.
  */
  line(t) {
    if (t < 1 || t > this.lines)
      throw new RangeError(`Invalid line number ${t} in ${this.lines}-line document`);
    return this.lineInner(t, !0, 1, 0);
  }
  /**
  Replace a range of the text with the given content.
  */
  replace(t, e, i) {
    [t, e] = Fe(this, t, e);
    let s = [];
    return this.decompose(
      0,
      t,
      s,
      2
      /* Open.To */
    ), i.length && i.decompose(
      0,
      i.length,
      s,
      3
      /* Open.To */
    ), this.decompose(
      e,
      this.length,
      s,
      1
      /* Open.From */
    ), zt.from(s, this.length - (e - t) + i.length);
  }
  /**
  Append another document to this one.
  */
  append(t) {
    return this.replace(this.length, this.length, t);
  }
  /**
  Retrieve the text between the given points.
  */
  slice(t, e = this.length) {
    [t, e] = Fe(this, t, e);
    let i = [];
    return this.decompose(t, e, i, 0), zt.from(i, e - t);
  }
  /**
  Test whether this text is equal to another instance.
  */
  eq(t) {
    if (t == this)
      return !0;
    if (t.length != this.length || t.lines != this.lines)
      return !1;
    let e = this.scanIdentical(t, 1), i = this.length - this.scanIdentical(t, -1), s = new ni(this), r = new ni(t);
    for (let o = e, l = e; ; ) {
      if (s.next(o), r.next(o), o = 0, s.lineBreak != r.lineBreak || s.done != r.done || s.value != r.value)
        return !1;
      if (l += s.value.length, s.done || l >= i)
        return !0;
    }
  }
  /**
  Iterate over the text. When `dir` is `-1`, iteration happens
  from end to start. This will return lines and the breaks between
  them as separate strings.
  */
  iter(t = 1) {
    return new ni(this, t);
  }
  /**
  Iterate over a range of the text. When `from` > `to`, the
  iterator will run in reverse.
  */
  iterRange(t, e = this.length) {
    return new Ol(this, t, e);
  }
  /**
  Return a cursor that iterates over the given range of lines,
  _without_ returning the line breaks between, and yielding empty
  strings for empty lines.
  
  When `from` and `to` are given, they should be 1-based line numbers.
  */
  iterLines(t, e) {
    let i;
    if (t == null)
      i = this.iter();
    else {
      e == null && (e = this.lines + 1);
      let s = this.line(t).from;
      i = this.iterRange(s, Math.max(s, e == this.lines + 1 ? this.length : e <= 1 ? 0 : this.line(e - 1).to));
    }
    return new Cl(i);
  }
  /**
  Return the document as a string, using newline characters to
  separate lines.
  */
  toString() {
    return this.sliceString(0);
  }
  /**
  Convert the document to an array of lines (which can be
  deserialized again via [`Text.of`](https://codemirror.net/6/docs/ref/#state.Text^of)).
  */
  toJSON() {
    let t = [];
    return this.flatten(t), t;
  }
  /**
  @internal
  */
  constructor() {
  }
  /**
  Create a `Text` instance for the given array of lines.
  */
  static of(t) {
    if (t.length == 0)
      throw new RangeError("A document must have at least one line");
    return t.length == 1 && !t[0] ? I.empty : t.length <= 32 ? new K(t) : zt.from(K.split(t, []));
  }
}
class K extends I {
  constructor(t, e = Qh(t)) {
    super(), this.text = t, this.length = e;
  }
  get lines() {
    return this.text.length;
  }
  get children() {
    return null;
  }
  lineInner(t, e, i, s) {
    for (let r = 0; ; r++) {
      let o = this.text[r], l = s + o.length;
      if ((e ? i : l) >= t)
        return new $h(s, l, i, o);
      s = l + 1, i++;
    }
  }
  decompose(t, e, i, s) {
    let r = t <= 0 && e >= this.length ? this : new K($r(this.text, t, e), Math.min(e, this.length) - Math.max(0, t));
    if (s & 1) {
      let o = i.pop(), l = Ji(r.text, o.text.slice(), 0, r.length);
      if (l.length <= 32)
        i.push(new K(l, o.length + r.length));
      else {
        let a = l.length >> 1;
        i.push(new K(l.slice(0, a)), new K(l.slice(a)));
      }
    } else
      i.push(r);
  }
  replace(t, e, i) {
    if (!(i instanceof K))
      return super.replace(t, e, i);
    [t, e] = Fe(this, t, e);
    let s = Ji(this.text, Ji(i.text, $r(this.text, 0, t)), e), r = this.length + i.length - (e - t);
    return s.length <= 32 ? new K(s, r) : zt.from(K.split(s, []), r);
  }
  sliceString(t, e = this.length, i = `
`) {
    [t, e] = Fe(this, t, e);
    let s = "";
    for (let r = 0, o = 0; r <= e && o < this.text.length; o++) {
      let l = this.text[o], a = r + l.length;
      r > t && o && (s += i), t < a && e > r && (s += l.slice(Math.max(0, t - r), e - r)), r = a + 1;
    }
    return s;
  }
  flatten(t) {
    for (let e of this.text)
      t.push(e);
  }
  scanIdentical() {
    return 0;
  }
  static split(t, e) {
    let i = [], s = -1;
    for (let r of t)
      i.push(r), s += r.length + 1, i.length == 32 && (e.push(new K(i, s)), i = [], s = -1);
    return s > -1 && e.push(new K(i, s)), e;
  }
}
class zt extends I {
  constructor(t, e) {
    super(), this.children = t, this.length = e, this.lines = 0;
    for (let i of t)
      this.lines += i.lines;
  }
  lineInner(t, e, i, s) {
    for (let r = 0; ; r++) {
      let o = this.children[r], l = s + o.length, a = i + o.lines - 1;
      if ((e ? a : l) >= t)
        return o.lineInner(t, e, i, s);
      s = l + 1, i = a + 1;
    }
  }
  decompose(t, e, i, s) {
    for (let r = 0, o = 0; o <= e && r < this.children.length; r++) {
      let l = this.children[r], a = o + l.length;
      if (t <= a && e >= o) {
        let h = s & ((o <= t ? 1 : 0) | (a >= e ? 2 : 0));
        o >= t && a <= e && !h ? i.push(l) : l.decompose(t - o, e - o, i, h);
      }
      o = a + 1;
    }
  }
  replace(t, e, i) {
    if ([t, e] = Fe(this, t, e), i.lines < this.lines)
      for (let s = 0, r = 0; s < this.children.length; s++) {
        let o = this.children[s], l = r + o.length;
        if (t >= r && e <= l) {
          let a = o.replace(t - r, e - r, i), h = this.lines - o.lines + a.lines;
          if (a.lines < h >> 4 && a.lines > h >> 6) {
            let f = this.children.slice();
            return f[s] = a, new zt(f, this.length - (e - t) + i.length);
          }
          return super.replace(r, l, a);
        }
        r = l + 1;
      }
    return super.replace(t, e, i);
  }
  sliceString(t, e = this.length, i = `
`) {
    [t, e] = Fe(this, t, e);
    let s = "";
    for (let r = 0, o = 0; r < this.children.length && o <= e; r++) {
      let l = this.children[r], a = o + l.length;
      o > t && r && (s += i), t < a && e > o && (s += l.sliceString(t - o, e - o, i)), o = a + 1;
    }
    return s;
  }
  flatten(t) {
    for (let e of this.children)
      e.flatten(t);
  }
  scanIdentical(t, e) {
    if (!(t instanceof zt))
      return 0;
    let i = 0, [s, r, o, l] = e > 0 ? [0, 0, this.children.length, t.children.length] : [this.children.length - 1, t.children.length - 1, -1, -1];
    for (; ; s += e, r += e) {
      if (s == o || r == l)
        return i;
      let a = this.children[s], h = t.children[r];
      if (a != h)
        return i + a.scanIdentical(h, e);
      i += a.length + 1;
    }
  }
  static from(t, e = t.reduce((i, s) => i + s.length + 1, -1)) {
    let i = 0;
    for (let d of t)
      i += d.lines;
    if (i < 32) {
      let d = [];
      for (let p of t)
        p.flatten(d);
      return new K(d, e);
    }
    let s = Math.max(
      32,
      i >> 5
      /* Tree.BranchShift */
    ), r = s << 1, o = s >> 1, l = [], a = 0, h = -1, f = [];
    function c(d) {
      let p;
      if (d.lines > r && d instanceof zt)
        for (let g of d.children)
          c(g);
      else d.lines > o && (a > o || !a) ? (u(), l.push(d)) : d instanceof K && a && (p = f[f.length - 1]) instanceof K && d.lines + p.lines <= 32 ? (a += d.lines, h += d.length + 1, f[f.length - 1] = new K(p.text.concat(d.text), p.length + 1 + d.length)) : (a + d.lines > s && u(), a += d.lines, h += d.length + 1, f.push(d));
    }
    function u() {
      a != 0 && (l.push(f.length == 1 ? f[0] : zt.from(f, h)), h = -1, a = f.length = 0);
    }
    for (let d of t)
      c(d);
    return u(), l.length == 1 ? l[0] : new zt(l, e);
  }
}
I.empty = /* @__PURE__ */ new K([""], 0);
function Qh(n) {
  let t = -1;
  for (let e of n)
    t += e.length + 1;
  return t;
}
function Ji(n, t, e = 0, i = 1e9) {
  for (let s = 0, r = 0, o = !0; r < n.length && s <= i; r++) {
    let l = n[r], a = s + l.length;
    a >= e && (a > i && (l = l.slice(0, i - s)), s < e && (l = l.slice(e - s)), o ? (t[t.length - 1] += l, o = !1) : t.push(l)), s = a + 1;
  }
  return t;
}
function $r(n, t, e) {
  return Ji(n, [""], t, e);
}
class ni {
  constructor(t, e = 1) {
    this.dir = e, this.done = !1, this.lineBreak = !1, this.value = "", this.nodes = [t], this.offsets = [e > 0 ? 1 : (t instanceof K ? t.text.length : t.children.length) << 1];
  }
  nextInner(t, e) {
    for (this.done = this.lineBreak = !1; ; ) {
      let i = this.nodes.length - 1, s = this.nodes[i], r = this.offsets[i], o = r >> 1, l = s instanceof K ? s.text.length : s.children.length;
      if (o == (e > 0 ? l : 0)) {
        if (i == 0)
          return this.done = !0, this.value = "", this;
        e > 0 && this.offsets[i - 1]++, this.nodes.pop(), this.offsets.pop();
      } else if ((r & 1) == (e > 0 ? 0 : 1)) {
        if (this.offsets[i] += e, t == 0)
          return this.lineBreak = !0, this.value = `
`, this;
        t--;
      } else if (s instanceof K) {
        let a = s.text[o + (e < 0 ? -1 : 0)];
        if (this.offsets[i] += e, a.length > Math.max(0, t))
          return this.value = t == 0 ? a : e > 0 ? a.slice(t) : a.slice(0, a.length - t), this;
        t -= a.length;
      } else {
        let a = s.children[o + (e < 0 ? -1 : 0)];
        t > a.length ? (t -= a.length, this.offsets[i] += e) : (e < 0 && this.offsets[i]--, this.nodes.push(a), this.offsets.push(e > 0 ? 1 : (a instanceof K ? a.text.length : a.children.length) << 1));
      }
    }
  }
  next(t = 0) {
    return t < 0 && (this.nextInner(-t, -this.dir), t = this.value.length), this.nextInner(t, this.dir);
  }
}
class Ol {
  constructor(t, e, i) {
    this.value = "", this.done = !1, this.cursor = new ni(t, e > i ? -1 : 1), this.pos = e > i ? t.length : 0, this.from = Math.min(e, i), this.to = Math.max(e, i);
  }
  nextInner(t, e) {
    if (e < 0 ? this.pos <= this.from : this.pos >= this.to)
      return this.value = "", this.done = !0, this;
    t += Math.max(0, e < 0 ? this.pos - this.to : this.from - this.pos);
    let i = e < 0 ? this.pos - this.from : this.to - this.pos;
    t > i && (t = i), i -= t;
    let { value: s } = this.cursor.next(t);
    return this.pos += (s.length + t) * e, this.value = s.length <= i ? s : e < 0 ? s.slice(s.length - i) : s.slice(0, i), this.done = !this.value, this;
  }
  next(t = 0) {
    return t < 0 ? t = Math.max(t, this.from - this.pos) : t > 0 && (t = Math.min(t, this.to - this.pos)), this.nextInner(t, this.cursor.dir);
  }
  get lineBreak() {
    return this.cursor.lineBreak && this.value != "";
  }
}
class Cl {
  constructor(t) {
    this.inner = t, this.afterBreak = !0, this.value = "", this.done = !1;
  }
  next(t = 0) {
    let { done: e, lineBreak: i, value: s } = this.inner.next(t);
    return e && this.afterBreak ? (this.value = "", this.afterBreak = !1) : e ? (this.done = !0, this.value = "") : i ? this.afterBreak ? this.value = "" : (this.afterBreak = !0, this.next()) : (this.value = s, this.afterBreak = !1), this;
  }
  get lineBreak() {
    return !1;
  }
}
typeof Symbol < "u" && (I.prototype[Symbol.iterator] = function() {
  return this.iter();
}, ni.prototype[Symbol.iterator] = Ol.prototype[Symbol.iterator] = Cl.prototype[Symbol.iterator] = function() {
  return this;
});
class $h {
  /**
  @internal
  */
  constructor(t, e, i, s) {
    this.from = t, this.to = e, this.number = i, this.text = s;
  }
  /**
  The length of the line (not including any line break after it).
  */
  get length() {
    return this.to - this.from;
  }
}
function Fe(n, t, e) {
  return t = Math.max(0, Math.min(n.length, t)), [t, Math.max(t, Math.min(n.length, e))];
}
function st(n, t, e = !0, i = !0) {
  return Fh(n, t, e, i);
}
function jh(n) {
  return n >= 56320 && n < 57344;
}
function Xh(n) {
  return n >= 55296 && n < 56320;
}
function Ft(n, t) {
  let e = n.charCodeAt(t);
  if (!Xh(e) || t + 1 == n.length)
    return e;
  let i = n.charCodeAt(t + 1);
  return jh(i) ? (e - 55296 << 10) + (i - 56320) + 65536 : e;
}
function Al(n) {
  return n <= 65535 ? String.fromCharCode(n) : (n -= 65536, String.fromCharCode((n >> 10) + 55296, (n & 1023) + 56320));
}
function se(n) {
  return n < 65536 ? 1 : 2;
}
const bn = /\r\n?|\n/;
var ot = /* @__PURE__ */ (function(n) {
  return n[n.Simple = 0] = "Simple", n[n.TrackDel = 1] = "TrackDel", n[n.TrackBefore = 2] = "TrackBefore", n[n.TrackAfter = 3] = "TrackAfter", n;
})(ot || (ot = {}));
class Ut {
  // Sections are encoded as pairs of integers. The first is the
  // length in the current document, and the second is -1 for
  // unaffected sections, and the length of the replacement content
  // otherwise. So an insertion would be (0, n>0), a deletion (n>0,
  // 0), and a replacement two positive numbers.
  /**
  @internal
  */
  constructor(t) {
    this.sections = t;
  }
  /**
  The length of the document before the change.
  */
  get length() {
    let t = 0;
    for (let e = 0; e < this.sections.length; e += 2)
      t += this.sections[e];
    return t;
  }
  /**
  The length of the document after the change.
  */
  get newLength() {
    let t = 0;
    for (let e = 0; e < this.sections.length; e += 2) {
      let i = this.sections[e + 1];
      t += i < 0 ? this.sections[e] : i;
    }
    return t;
  }
  /**
  False when there are actual changes in this set.
  */
  get empty() {
    return this.sections.length == 0 || this.sections.length == 2 && this.sections[1] < 0;
  }
  /**
  Iterate over the unchanged parts left by these changes. `posA`
  provides the position of the range in the old document, `posB`
  the new position in the changed document.
  */
  iterGaps(t) {
    for (let e = 0, i = 0, s = 0; e < this.sections.length; ) {
      let r = this.sections[e++], o = this.sections[e++];
      o < 0 ? (t(i, s, r), s += r) : s += o, i += r;
    }
  }
  /**
  Iterate over the ranges changed by these changes. (See
  [`ChangeSet.iterChanges`](https://codemirror.net/6/docs/ref/#state.ChangeSet.iterChanges) for a
  variant that also provides you with the inserted text.)
  `fromA`/`toA` provides the extent of the change in the starting
  document, `fromB`/`toB` the extent of the replacement in the
  changed document.
  
  When `individual` is true, adjacent changes (which are kept
  separate for [position mapping](https://codemirror.net/6/docs/ref/#state.ChangeDesc.mapPos)) are
  reported separately.
  */
  iterChangedRanges(t, e = !1) {
    xn(this, t, e);
  }
  /**
  Get a description of the inverted form of these changes.
  */
  get invertedDesc() {
    let t = [];
    for (let e = 0; e < this.sections.length; ) {
      let i = this.sections[e++], s = this.sections[e++];
      s < 0 ? t.push(i, s) : t.push(s, i);
    }
    return new Ut(t);
  }
  /**
  Compute the combined effect of applying another set of changes
  after this one. The length of the document after this set should
  match the length before `other`.
  */
  composeDesc(t) {
    return this.empty ? t : t.empty ? this : Tl(this, t);
  }
  /**
  Map this description, which should start with the same document
  as `other`, over another set of changes, so that it can be
  applied after it. When `before` is true, map as if the changes
  in `this` happened before the ones in `other`.
  */
  mapDesc(t, e = !1) {
    return t.empty ? this : wn(this, t, e);
  }
  mapPos(t, e = -1, i = ot.Simple) {
    let s = 0, r = 0;
    for (let o = 0; o < this.sections.length; ) {
      let l = this.sections[o++], a = this.sections[o++], h = s + l;
      if (a < 0) {
        if (h > t)
          return r + (t - s);
        r += l;
      } else {
        if (i != ot.Simple && h >= t && (i == ot.TrackDel && s < t && h > t || i == ot.TrackBefore && s < t || i == ot.TrackAfter && h > t))
          return null;
        if (h > t || h == t && e < 0 && !l)
          return t == s || e < 0 ? r : r + a;
        r += a;
      }
      s = h;
    }
    if (t > s)
      throw new RangeError(`Position ${t} is out of range for changeset of length ${s}`);
    return r;
  }
  /**
  Check whether these changes touch a given range. When one of the
  changes entirely covers the range, the string `"cover"` is
  returned.
  */
  touchesRange(t, e = t) {
    for (let i = 0, s = 0; i < this.sections.length && s <= e; ) {
      let r = this.sections[i++], o = this.sections[i++], l = s + r;
      if (o >= 0 && s <= e && l >= t)
        return s < t && l > e ? "cover" : !0;
      s = l;
    }
    return !1;
  }
  /**
  @internal
  */
  toString() {
    let t = "";
    for (let e = 0; e < this.sections.length; ) {
      let i = this.sections[e++], s = this.sections[e++];
      t += (t ? " " : "") + i + (s >= 0 ? ":" + s : "");
    }
    return t;
  }
  /**
  Serialize this change desc to a JSON-representable value.
  */
  toJSON() {
    return this.sections;
  }
  /**
  Create a change desc from its JSON representation (as produced
  by [`toJSON`](https://codemirror.net/6/docs/ref/#state.ChangeDesc.toJSON).
  */
  static fromJSON(t) {
    if (!Array.isArray(t) || t.length % 2 || t.some((e) => typeof e != "number"))
      throw new RangeError("Invalid JSON representation of ChangeDesc");
    return new Ut(t);
  }
  /**
  @internal
  */
  static create(t) {
    return new Ut(t);
  }
}
class G extends Ut {
  constructor(t, e) {
    super(t), this.inserted = e;
  }
  /**
  Apply the changes to a document, returning the modified
  document.
  */
  apply(t) {
    if (this.length != t.length)
      throw new RangeError("Applying change set to a document with the wrong length");
    return xn(this, (e, i, s, r, o) => t = t.replace(s, s + (i - e), o), !1), t;
  }
  mapDesc(t, e = !1) {
    return wn(this, t, e, !0);
  }
  /**
  Given the document as it existed _before_ the changes, return a
  change set that represents the inverse of this set, which could
  be used to go from the document created by the changes back to
  the document as it existed before the changes.
  */
  invert(t) {
    let e = this.sections.slice(), i = [];
    for (let s = 0, r = 0; s < e.length; s += 2) {
      let o = e[s], l = e[s + 1];
      if (l >= 0) {
        e[s] = l, e[s + 1] = o;
        let a = s >> 1;
        for (; i.length < a; )
          i.push(I.empty);
        i.push(o ? t.slice(r, r + o) : I.empty);
      }
      r += o;
    }
    return new G(e, i);
  }
  /**
  Combine two subsequent change sets into a single set. `other`
  must start in the document produced by `this`. If `this` goes
  `docA` → `docB` and `other` represents `docB` → `docC`, the
  returned value will represent the change `docA` → `docC`.
  */
  compose(t) {
    return this.empty ? t : t.empty ? this : Tl(this, t, !0);
  }
  /**
  Given another change set starting in the same document, maps this
  change set over the other, producing a new change set that can be
  applied to the document produced by applying `other`. When
  `before` is `true`, order changes as if `this` comes before
  `other`, otherwise (the default) treat `other` as coming first.
  
  Given two changes `A` and `B`, `A.compose(B.map(A))` and
  `B.compose(A.map(B, true))` will produce the same document. This
  provides a basic form of [operational
  transformation](https://en.wikipedia.org/wiki/Operational_transformation),
  and can be used for collaborative editing.
  */
  map(t, e = !1) {
    return t.empty ? this : wn(this, t, e, !0);
  }
  /**
  Iterate over the changed ranges in the document, calling `f` for
  each, with the range in the original document (`fromA`-`toA`)
  and the range that replaces it in the new document
  (`fromB`-`toB`).
  
  When `individual` is true, adjacent changes are reported
  separately.
  */
  iterChanges(t, e = !1) {
    xn(this, t, e);
  }
  /**
  Get a [change description](https://codemirror.net/6/docs/ref/#state.ChangeDesc) for this change
  set.
  */
  get desc() {
    return Ut.create(this.sections);
  }
  /**
  @internal
  */
  filter(t) {
    let e = [], i = [], s = [], r = new fi(this);
    t: for (let o = 0, l = 0; ; ) {
      let a = o == t.length ? 1e9 : t[o++];
      for (; l < a || l == a && r.len == 0; ) {
        if (r.done)
          break t;
        let f = Math.min(r.len, a - l);
        it(s, f, -1);
        let c = r.ins == -1 ? -1 : r.off == 0 ? r.ins : 0;
        it(e, f, c), c > 0 && re(i, e, r.text), r.forward(f), l += f;
      }
      let h = t[o++];
      for (; l < h; ) {
        if (r.done)
          break t;
        let f = Math.min(r.len, h - l);
        it(e, f, -1), it(s, f, r.ins == -1 ? -1 : r.off == 0 ? r.ins : 0), r.forward(f), l += f;
      }
    }
    return {
      changes: new G(e, i),
      filtered: Ut.create(s)
    };
  }
  /**
  Serialize this change set to a JSON-representable value.
  */
  toJSON() {
    let t = [];
    for (let e = 0; e < this.sections.length; e += 2) {
      let i = this.sections[e], s = this.sections[e + 1];
      s < 0 ? t.push(i) : s == 0 ? t.push([i]) : t.push([i].concat(this.inserted[e >> 1].toJSON()));
    }
    return t;
  }
  /**
  Create a change set for the given changes, for a document of the
  given length, using `lineSep` as line separator.
  */
  static of(t, e, i) {
    let s = [], r = [], o = 0, l = null;
    function a(f = !1) {
      if (!f && !s.length)
        return;
      o < e && it(s, e - o, -1);
      let c = new G(s, r);
      l = l ? l.compose(c.map(l)) : c, s = [], r = [], o = 0;
    }
    function h(f) {
      if (Array.isArray(f))
        for (let c of f)
          h(c);
      else if (f instanceof G) {
        if (f.length != e)
          throw new RangeError(`Mismatched change set length (got ${f.length}, expected ${e})`);
        a(), l = l ? l.compose(f.map(l)) : f;
      } else {
        let { from: c, to: u = c, insert: d } = f;
        if (c > u || c < 0 || u > e)
          throw new RangeError(`Invalid change range ${c} to ${u} (in doc of length ${e})`);
        let p = d ? typeof d == "string" ? I.of(d.split(i || bn)) : d : I.empty, g = p.length;
        if (c == u && g == 0)
          return;
        c < o && a(), c > o && it(s, c - o, -1), it(s, u - c, g), re(r, s, p), o = u;
      }
    }
    return h(t), a(!l), l;
  }
  /**
  Create an empty changeset of the given length.
  */
  static empty(t) {
    return new G(t ? [t, -1] : [], []);
  }
  /**
  Create a changeset from its JSON representation (as produced by
  [`toJSON`](https://codemirror.net/6/docs/ref/#state.ChangeSet.toJSON).
  */
  static fromJSON(t) {
    if (!Array.isArray(t))
      throw new RangeError("Invalid JSON representation of ChangeSet");
    let e = [], i = [];
    for (let s = 0; s < t.length; s++) {
      let r = t[s];
      if (typeof r == "number")
        e.push(r, -1);
      else {
        if (!Array.isArray(r) || typeof r[0] != "number" || r.some((o, l) => l && typeof o != "string"))
          throw new RangeError("Invalid JSON representation of ChangeSet");
        if (r.length == 1)
          e.push(r[0], 0);
        else {
          for (; i.length < s; )
            i.push(I.empty);
          i[s] = I.of(r.slice(1)), e.push(r[0], i[s].length);
        }
      }
    }
    return new G(e, i);
  }
  /**
  @internal
  */
  static createSet(t, e) {
    return new G(t, e);
  }
}
function it(n, t, e, i = !1) {
  if (t == 0 && e <= 0)
    return;
  let s = n.length - 2;
  s >= 0 && e <= 0 && e == n[s + 1] ? n[s] += t : s >= 0 && t == 0 && n[s] == 0 ? n[s + 1] += e : i ? (n[s] += t, n[s + 1] += e) : n.push(t, e);
}
function re(n, t, e) {
  if (e.length == 0)
    return;
  let i = t.length - 2 >> 1;
  if (i < n.length)
    n[n.length - 1] = n[n.length - 1].append(e);
  else {
    for (; n.length < i; )
      n.push(I.empty);
    n.push(e);
  }
}
function xn(n, t, e) {
  let i = n.inserted;
  for (let s = 0, r = 0, o = 0; o < n.sections.length; ) {
    let l = n.sections[o++], a = n.sections[o++];
    if (a < 0)
      s += l, r += l;
    else {
      let h = s, f = r, c = I.empty;
      for (; h += l, f += a, a && i && (c = c.append(i[o - 2 >> 1])), !(e || o == n.sections.length || n.sections[o + 1] < 0); )
        l = n.sections[o++], a = n.sections[o++];
      t(s, h, r, f, c), s = h, r = f;
    }
  }
}
function wn(n, t, e, i = !1) {
  let s = [], r = i ? [] : null, o = new fi(n), l = new fi(t);
  for (let a = -1; ; ) {
    if (o.done && l.len || l.done && o.len)
      throw new Error("Mismatched change set lengths");
    if (o.ins == -1 && l.ins == -1) {
      let h = Math.min(o.len, l.len);
      it(s, h, -1), o.forward(h), l.forward(h);
    } else if (l.ins >= 0 && (o.ins < 0 || a == o.i || o.off == 0 && (l.len < o.len || l.len == o.len && !e))) {
      let h = l.len;
      for (it(s, l.ins, -1); h; ) {
        let f = Math.min(o.len, h);
        o.ins >= 0 && a < o.i && o.len <= f && (it(s, 0, o.ins), r && re(r, s, o.text), a = o.i), o.forward(f), h -= f;
      }
      l.next();
    } else if (o.ins >= 0) {
      let h = 0, f = o.len;
      for (; f; )
        if (l.ins == -1) {
          let c = Math.min(f, l.len);
          h += c, f -= c, l.forward(c);
        } else if (l.ins == 0 && l.len < f)
          f -= l.len, l.next();
        else
          break;
      it(s, h, a < o.i ? o.ins : 0), r && a < o.i && re(r, s, o.text), a = o.i, o.forward(o.len - f);
    } else {
      if (o.done && l.done)
        return r ? G.createSet(s, r) : Ut.create(s);
      throw new Error("Mismatched change set lengths");
    }
  }
}
function Tl(n, t, e = !1) {
  let i = [], s = e ? [] : null, r = new fi(n), o = new fi(t);
  for (let l = !1; ; ) {
    if (r.done && o.done)
      return s ? G.createSet(i, s) : Ut.create(i);
    if (r.ins == 0)
      it(i, r.len, 0, l), r.next();
    else if (o.len == 0 && !o.done)
      it(i, 0, o.ins, l), s && re(s, i, o.text), o.next();
    else {
      if (r.done || o.done)
        throw new Error("Mismatched change set lengths");
      {
        let a = Math.min(r.len2, o.len), h = i.length;
        if (r.ins == -1) {
          let f = o.ins == -1 ? -1 : o.off ? 0 : o.ins;
          it(i, a, f, l), s && f && re(s, i, o.text);
        } else o.ins == -1 ? (it(i, r.off ? 0 : r.len, a, l), s && re(s, i, r.textBit(a))) : (it(i, r.off ? 0 : r.len, o.off ? 0 : o.ins, l), s && !o.off && re(s, i, o.text));
        l = (r.ins > a || o.ins >= 0 && o.len > a) && (l || i.length > h), r.forward2(a), o.forward(a);
      }
    }
  }
}
class fi {
  constructor(t) {
    this.set = t, this.i = 0, this.next();
  }
  next() {
    let { sections: t } = this.set;
    this.i < t.length ? (this.len = t[this.i++], this.ins = t[this.i++]) : (this.len = 0, this.ins = -2), this.off = 0;
  }
  get done() {
    return this.ins == -2;
  }
  get len2() {
    return this.ins < 0 ? this.len : this.ins;
  }
  get text() {
    let { inserted: t } = this.set, e = this.i - 2 >> 1;
    return e >= t.length ? I.empty : t[e];
  }
  textBit(t) {
    let { inserted: e } = this.set, i = this.i - 2 >> 1;
    return i >= e.length && !t ? I.empty : e[i].slice(this.off, t == null ? void 0 : this.off + t);
  }
  forward(t) {
    t == this.len ? this.next() : (this.len -= t, this.off += t);
  }
  forward2(t) {
    this.ins == -1 ? this.forward(t) : t == this.ins ? this.next() : (this.ins -= t, this.off += t);
  }
}
class we {
  constructor(t, e, i) {
    this.from = t, this.to = e, this.flags = i;
  }
  /**
  The anchor of the range—the side that doesn't move when you
  extend it.
  */
  get anchor() {
    return this.flags & 32 ? this.to : this.from;
  }
  /**
  The head of the range, which is moved when the range is
  [extended](https://codemirror.net/6/docs/ref/#state.SelectionRange.extend).
  */
  get head() {
    return this.flags & 32 ? this.from : this.to;
  }
  /**
  True when `anchor` and `head` are at the same position.
  */
  get empty() {
    return this.from == this.to;
  }
  /**
  If this is a cursor that is explicitly associated with the
  character on one of its sides, this returns the side. -1 means
  the character before its position, 1 the character after, and 0
  means no association.
  */
  get assoc() {
    return this.flags & 8 ? -1 : this.flags & 16 ? 1 : 0;
  }
  /**
  The bidirectional text level associated with this cursor, if
  any.
  */
  get bidiLevel() {
    let t = this.flags & 7;
    return t == 7 ? null : t;
  }
  /**
  The goal column (stored vertical offset) associated with a
  cursor. This is used to preserve the vertical position when
  [moving](https://codemirror.net/6/docs/ref/#view.EditorView.moveVertically) across
  lines of different length.
  */
  get goalColumn() {
    let t = this.flags >> 6;
    return t == 16777215 ? void 0 : t;
  }
  /**
  Map this range through a change, producing a valid range in the
  updated document.
  */
  map(t, e = -1) {
    let i, s;
    return this.empty ? i = s = t.mapPos(this.from, e) : (i = t.mapPos(this.from, 1), s = t.mapPos(this.to, -1)), i == this.from && s == this.to ? this : new we(i, s, this.flags);
  }
  /**
  Extend this range to cover at least `from` to `to`.
  */
  extend(t, e = t) {
    if (t <= this.anchor && e >= this.anchor)
      return k.range(t, e);
    let i = Math.abs(t - this.anchor) > Math.abs(e - this.anchor) ? t : e;
    return k.range(this.anchor, i);
  }
  /**
  Compare this range to another range.
  */
  eq(t, e = !1) {
    return this.anchor == t.anchor && this.head == t.head && this.goalColumn == t.goalColumn && (!e || !this.empty || this.assoc == t.assoc);
  }
  /**
  Return a JSON-serializable object representing the range.
  */
  toJSON() {
    return { anchor: this.anchor, head: this.head };
  }
  /**
  Convert a JSON representation of a range to a `SelectionRange`
  instance.
  */
  static fromJSON(t) {
    if (!t || typeof t.anchor != "number" || typeof t.head != "number")
      throw new RangeError("Invalid JSON representation for SelectionRange");
    return k.range(t.anchor, t.head);
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new we(t, e, i);
  }
}
class k {
  constructor(t, e) {
    this.ranges = t, this.mainIndex = e;
  }
  /**
  Map a selection through a change. Used to adjust the selection
  position for changes.
  */
  map(t, e = -1) {
    return t.empty ? this : k.create(this.ranges.map((i) => i.map(t, e)), this.mainIndex);
  }
  /**
  Compare this selection to another selection. By default, ranges
  are compared only by position. When `includeAssoc` is true,
  cursor ranges must also have the same
  [`assoc`](https://codemirror.net/6/docs/ref/#state.SelectionRange.assoc) value.
  */
  eq(t, e = !1) {
    if (this.ranges.length != t.ranges.length || this.mainIndex != t.mainIndex)
      return !1;
    for (let i = 0; i < this.ranges.length; i++)
      if (!this.ranges[i].eq(t.ranges[i], e))
        return !1;
    return !0;
  }
  /**
  Get the primary selection range. Usually, you should make sure
  your code applies to _all_ ranges, by using methods like
  [`changeByRange`](https://codemirror.net/6/docs/ref/#state.EditorState.changeByRange).
  */
  get main() {
    return this.ranges[this.mainIndex];
  }
  /**
  Make sure the selection only has one range. Returns a selection
  holding only the main range from this selection.
  */
  asSingle() {
    return this.ranges.length == 1 ? this : new k([this.main], 0);
  }
  /**
  Extend this selection with an extra range.
  */
  addRange(t, e = !0) {
    return k.create([t].concat(this.ranges), e ? 0 : this.mainIndex + 1);
  }
  /**
  Replace a given range with another range, and then normalize the
  selection to merge and sort ranges if necessary.
  */
  replaceRange(t, e = this.mainIndex) {
    let i = this.ranges.slice();
    return i[e] = t, k.create(i, this.mainIndex);
  }
  /**
  Convert this selection to an object that can be serialized to
  JSON.
  */
  toJSON() {
    return { ranges: this.ranges.map((t) => t.toJSON()), main: this.mainIndex };
  }
  /**
  Create a selection from a JSON representation.
  */
  static fromJSON(t) {
    if (!t || !Array.isArray(t.ranges) || typeof t.main != "number" || t.main >= t.ranges.length)
      throw new RangeError("Invalid JSON representation for EditorSelection");
    return new k(t.ranges.map((e) => we.fromJSON(e)), t.main);
  }
  /**
  Create a selection holding a single range.
  */
  static single(t, e = t) {
    return new k([k.range(t, e)], 0);
  }
  /**
  Sort and merge the given set of ranges, creating a valid
  selection.
  */
  static create(t, e = 0) {
    if (t.length == 0)
      throw new RangeError("A selection needs at least one range");
    for (let i = 0, s = 0; s < t.length; s++) {
      let r = t[s];
      if (r.empty ? r.from <= i : r.from < i)
        return k.normalized(t.slice(), e);
      i = r.to;
    }
    return new k(t, e);
  }
  /**
  Create a cursor selection range at the given position. You can
  safely ignore the optional arguments in most situations.
  */
  static cursor(t, e = 0, i, s) {
    return we.create(t, t, (e == 0 ? 0 : e < 0 ? 8 : 16) | (i == null ? 7 : Math.min(6, i)) | (s ?? 16777215) << 6);
  }
  /**
  Create a selection range.
  */
  static range(t, e, i, s) {
    let r = (i ?? 16777215) << 6 | (s == null ? 7 : Math.min(6, s));
    return e < t ? we.create(e, t, 48 | r) : we.create(t, e, (e > t ? 8 : 0) | r);
  }
  /**
  @internal
  */
  static normalized(t, e = 0) {
    let i = t[e];
    t.sort((s, r) => s.from - r.from), e = t.indexOf(i);
    for (let s = 1; s < t.length; s++) {
      let r = t[s], o = t[s - 1];
      if (r.empty ? r.from <= o.to : r.from < o.to) {
        let l = o.from, a = Math.max(r.to, o.to);
        s <= e && e--, t.splice(--s, 2, r.anchor > r.head ? k.range(a, l) : k.range(l, a));
      }
    }
    return new k(t, e);
  }
}
function Ml(n, t) {
  for (let e of n.ranges)
    if (e.to > t)
      throw new RangeError("Selection points outside of document");
}
let rr = 0;
class M {
  constructor(t, e, i, s, r) {
    this.combine = t, this.compareInput = e, this.compare = i, this.isStatic = s, this.id = rr++, this.default = t([]), this.extensions = typeof r == "function" ? r(this) : r;
  }
  /**
  Returns a facet reader for this facet, which can be used to
  [read](https://codemirror.net/6/docs/ref/#state.EditorState.facet) it but not to define values for it.
  */
  get reader() {
    return this;
  }
  /**
  Define a new facet.
  */
  static define(t = {}) {
    return new M(t.combine || ((e) => e), t.compareInput || ((e, i) => e === i), t.compare || (t.combine ? (e, i) => e === i : or), !!t.static, t.enables);
  }
  /**
  Returns an extension that adds the given value to this facet.
  */
  of(t) {
    return new ts([], this, 0, t);
  }
  /**
  Create an extension that computes a value for the facet from a
  state. You must take care to declare the parts of the state that
  this value depends on, since your function is only called again
  for a new state when one of those parts changed.
  
  In cases where your value depends only on a single field, you'll
  want to use the [`from`](https://codemirror.net/6/docs/ref/#state.Facet.from) method instead.
  */
  compute(t, e) {
    if (this.isStatic)
      throw new Error("Can't compute a static facet");
    return new ts(t, this, 1, e);
  }
  /**
  Create an extension that computes zero or more values for this
  facet from a state.
  */
  computeN(t, e) {
    if (this.isStatic)
      throw new Error("Can't compute a static facet");
    return new ts(t, this, 2, e);
  }
  from(t, e) {
    return e || (e = (i) => i), this.compute([t], (i) => e(i.field(t)));
  }
}
function or(n, t) {
  return n == t || n.length == t.length && n.every((e, i) => e === t[i]);
}
class ts {
  constructor(t, e, i, s) {
    this.dependencies = t, this.facet = e, this.type = i, this.value = s, this.id = rr++;
  }
  dynamicSlot(t) {
    var e;
    let i = this.value, s = this.facet.compareInput, r = this.id, o = t[r] >> 1, l = this.type == 2, a = !1, h = !1, f = [];
    for (let c of this.dependencies)
      c == "doc" ? a = !0 : c == "selection" ? h = !0 : (((e = t[c.id]) !== null && e !== void 0 ? e : 1) & 1) == 0 && f.push(t[c.id]);
    return {
      create(c) {
        return c.values[o] = i(c), 1;
      },
      update(c, u) {
        if (a && u.docChanged || h && (u.docChanged || u.selection) || kn(c, f)) {
          let d = i(c);
          if (l ? !jr(d, c.values[o], s) : !s(d, c.values[o]))
            return c.values[o] = d, 1;
        }
        return 0;
      },
      reconfigure: (c, u) => {
        let d, p = u.config.address[r];
        if (p != null) {
          let g = as(u, p);
          if (this.dependencies.every((m) => m instanceof M ? u.facet(m) === c.facet(m) : m instanceof At ? u.field(m, !1) == c.field(m, !1) : !0) || (l ? jr(d = i(c), g, s) : s(d = i(c), g)))
            return c.values[o] = g, 0;
        } else
          d = i(c);
        return c.values[o] = d, 1;
      }
    };
  }
}
function jr(n, t, e) {
  if (n.length != t.length)
    return !1;
  for (let i = 0; i < n.length; i++)
    if (!e(n[i], t[i]))
      return !1;
  return !0;
}
function kn(n, t) {
  let e = !1;
  for (let i of t)
    ri(n, i) & 1 && (e = !0);
  return e;
}
function qh(n, t, e) {
  let i = e.map((a) => n[a.id]), s = e.map((a) => a.type), r = i.filter((a) => !(a & 1)), o = n[t.id] >> 1;
  function l(a) {
    let h = [];
    for (let f = 0; f < i.length; f++) {
      let c = as(a, i[f]);
      if (s[f] == 2)
        for (let u of c)
          h.push(u);
      else
        h.push(c);
    }
    return t.combine(h);
  }
  return {
    create(a) {
      for (let h of i)
        ri(a, h);
      return a.values[o] = l(a), 1;
    },
    update(a, h) {
      if (!kn(a, r))
        return 0;
      let f = l(a);
      return t.compare(f, a.values[o]) ? 0 : (a.values[o] = f, 1);
    },
    reconfigure(a, h) {
      let f = kn(a, i), c = h.config.facets[t.id], u = h.facet(t);
      if (c && !f && or(e, c))
        return a.values[o] = u, 0;
      let d = l(a);
      return t.compare(d, u) ? (a.values[o] = u, 0) : (a.values[o] = d, 1);
    }
  };
}
const Di = /* @__PURE__ */ M.define({ static: !0 });
class At {
  constructor(t, e, i, s, r) {
    this.id = t, this.createF = e, this.updateF = i, this.compareF = s, this.spec = r, this.provides = void 0;
  }
  /**
  Define a state field.
  */
  static define(t) {
    let e = new At(rr++, t.create, t.update, t.compare || ((i, s) => i === s), t);
    return t.provide && (e.provides = t.provide(e)), e;
  }
  create(t) {
    let e = t.facet(Di).find((i) => i.field == this);
    return ((e == null ? void 0 : e.create) || this.createF)(t);
  }
  /**
  @internal
  */
  slot(t) {
    let e = t[this.id] >> 1;
    return {
      create: (i) => (i.values[e] = this.create(i), 1),
      update: (i, s) => {
        let r = i.values[e], o = this.updateF(r, s);
        return this.compareF(r, o) ? 0 : (i.values[e] = o, 1);
      },
      reconfigure: (i, s) => {
        let r = i.facet(Di), o = s.facet(Di), l;
        return (l = r.find((a) => a.field == this)) && l != o.find((a) => a.field == this) ? (i.values[e] = l.create(i), 1) : s.config.address[this.id] != null ? (i.values[e] = s.field(this), 0) : (i.values[e] = this.create(i), 1);
      }
    };
  }
  /**
  Returns an extension that enables this field and overrides the
  way it is initialized. Can be useful when you need to provide a
  non-default starting value for the field.
  */
  init(t) {
    return [this, Di.of({ field: this, create: t })];
  }
  /**
  State field instances can be used as
  [`Extension`](https://codemirror.net/6/docs/ref/#state.Extension) values to enable the field in a
  given state.
  */
  get extension() {
    return this;
  }
}
const be = { lowest: 4, low: 3, default: 2, high: 1, highest: 0 };
function Ge(n) {
  return (t) => new Pl(t, n);
}
const wi = {
  /**
  The highest precedence level, for extensions that should end up
  near the start of the precedence ordering.
  */
  highest: /* @__PURE__ */ Ge(be.highest),
  /**
  A higher-than-default precedence, for extensions that should
  come before those with default precedence.
  */
  high: /* @__PURE__ */ Ge(be.high),
  /**
  The default precedence, which is also used for extensions
  without an explicit precedence.
  */
  default: /* @__PURE__ */ Ge(be.default),
  /**
  A lower-than-default precedence.
  */
  low: /* @__PURE__ */ Ge(be.low),
  /**
  The lowest precedence level. Meant for things that should end up
  near the end of the extension order.
  */
  lowest: /* @__PURE__ */ Ge(be.lowest)
};
class Pl {
  constructor(t, e) {
    this.inner = t, this.prec = e;
  }
}
class ki {
  /**
  Create an instance of this compartment to add to your [state
  configuration](https://codemirror.net/6/docs/ref/#state.EditorStateConfig.extensions).
  */
  of(t) {
    return new vn(this, t);
  }
  /**
  Create an [effect](https://codemirror.net/6/docs/ref/#state.TransactionSpec.effects) that
  reconfigures this compartment.
  */
  reconfigure(t) {
    return ki.reconfigure.of({ compartment: this, extension: t });
  }
  /**
  Get the current content of the compartment in the state, or
  `undefined` if it isn't present.
  */
  get(t) {
    return t.config.compartments.get(this);
  }
}
class vn {
  constructor(t, e) {
    this.compartment = t, this.inner = e;
  }
}
class ls {
  constructor(t, e, i, s, r, o) {
    for (this.base = t, this.compartments = e, this.dynamicSlots = i, this.address = s, this.staticValues = r, this.facets = o, this.statusTemplate = []; this.statusTemplate.length < i.length; )
      this.statusTemplate.push(
        0
        /* SlotStatus.Unresolved */
      );
  }
  staticFacet(t) {
    let e = this.address[t.id];
    return e == null ? t.default : this.staticValues[e >> 1];
  }
  static resolve(t, e, i) {
    let s = [], r = /* @__PURE__ */ Object.create(null), o = /* @__PURE__ */ new Map();
    for (let u of Uh(t, e, o))
      u instanceof At ? s.push(u) : (r[u.facet.id] || (r[u.facet.id] = [])).push(u);
    let l = /* @__PURE__ */ Object.create(null), a = [], h = [];
    for (let u of s)
      l[u.id] = h.length << 1, h.push((d) => u.slot(d));
    let f = i == null ? void 0 : i.config.facets;
    for (let u in r) {
      let d = r[u], p = d[0].facet, g = f && f[u] || [];
      if (d.every(
        (m) => m.type == 0
        /* Provider.Static */
      ))
        if (l[p.id] = a.length << 1 | 1, or(g, d))
          a.push(i.facet(p));
        else {
          let m = p.combine(d.map((y) => y.value));
          a.push(i && p.compare(m, i.facet(p)) ? i.facet(p) : m);
        }
      else {
        for (let m of d)
          m.type == 0 ? (l[m.id] = a.length << 1 | 1, a.push(m.value)) : (l[m.id] = h.length << 1, h.push((y) => m.dynamicSlot(y)));
        l[p.id] = h.length << 1, h.push((m) => qh(m, p, d));
      }
    }
    let c = h.map((u) => u(l));
    return new ls(t, o, c, l, a, r);
  }
}
function Uh(n, t, e) {
  let i = [[], [], [], [], []], s = /* @__PURE__ */ new Map();
  function r(o, l) {
    let a = s.get(o);
    if (a != null) {
      if (a <= l)
        return;
      let h = i[a].indexOf(o);
      h > -1 && i[a].splice(h, 1), o instanceof vn && e.delete(o.compartment);
    }
    if (s.set(o, l), Array.isArray(o))
      for (let h of o)
        r(h, l);
    else if (o instanceof vn) {
      if (e.has(o.compartment))
        throw new RangeError("Duplicate use of compartment in extensions");
      let h = t.get(o.compartment) || o.inner;
      e.set(o.compartment, h), r(h, l);
    } else if (o instanceof Pl)
      r(o.inner, o.prec);
    else if (o instanceof At)
      i[l].push(o), o.provides && r(o.provides, l);
    else if (o instanceof ts)
      i[l].push(o), o.facet.extensions && r(o.facet.extensions, be.default);
    else {
      let h = o.extension;
      if (!h)
        throw new Error(`Unrecognized extension value in extension set (${o}). This sometimes happens because multiple instances of @codemirror/state are loaded, breaking instanceof checks.`);
      r(h, l);
    }
  }
  return r(n, be.default), i.reduce((o, l) => o.concat(l));
}
function ri(n, t) {
  if (t & 1)
    return 2;
  let e = t >> 1, i = n.status[e];
  if (i == 4)
    throw new Error("Cyclic dependency between fields and/or facets");
  if (i & 2)
    return i;
  n.status[e] = 4;
  let s = n.computeSlot(n, n.config.dynamicSlots[e]);
  return n.status[e] = 2 | s;
}
function as(n, t) {
  return t & 1 ? n.config.staticValues[t >> 1] : n.values[t >> 1];
}
const Dl = /* @__PURE__ */ M.define(), Sn = /* @__PURE__ */ M.define({
  combine: (n) => n.some((t) => t),
  static: !0
}), Bl = /* @__PURE__ */ M.define({
  combine: (n) => n.length ? n[0] : void 0,
  static: !0
}), Rl = /* @__PURE__ */ M.define(), Ll = /* @__PURE__ */ M.define(), El = /* @__PURE__ */ M.define(), Il = /* @__PURE__ */ M.define({
  combine: (n) => n.length ? n[0] : !1
});
class Jt {
  /**
  @internal
  */
  constructor(t, e) {
    this.type = t, this.value = e;
  }
  /**
  Define a new type of annotation.
  */
  static define() {
    return new Kh();
  }
}
class Kh {
  /**
  Create an instance of this annotation.
  */
  of(t) {
    return new Jt(this, t);
  }
}
class _h {
  /**
  @internal
  */
  constructor(t) {
    this.map = t;
  }
  /**
  Create a [state effect](https://codemirror.net/6/docs/ref/#state.StateEffect) instance of this
  type.
  */
  of(t) {
    return new N(this, t);
  }
}
class N {
  /**
  @internal
  */
  constructor(t, e) {
    this.type = t, this.value = e;
  }
  /**
  Map this effect through a position mapping. Will return
  `undefined` when that ends up deleting the effect.
  */
  map(t) {
    let e = this.type.map(this.value, t);
    return e === void 0 ? void 0 : e == this.value ? this : new N(this.type, e);
  }
  /**
  Tells you whether this effect object is of a given
  [type](https://codemirror.net/6/docs/ref/#state.StateEffectType).
  */
  is(t) {
    return this.type == t;
  }
  /**
  Define a new effect type. The type parameter indicates the type
  of values that his effect holds. It should be a type that
  doesn't include `undefined`, since that is used in
  [mapping](https://codemirror.net/6/docs/ref/#state.StateEffect.map) to indicate that an effect is
  removed.
  */
  static define(t = {}) {
    return new _h(t.map || ((e) => e));
  }
  /**
  Map an array of effects through a change set.
  */
  static mapEffects(t, e) {
    if (!t.length)
      return t;
    let i = [];
    for (let s of t) {
      let r = s.map(e);
      r && i.push(r);
    }
    return i;
  }
}
N.reconfigure = /* @__PURE__ */ N.define();
N.appendConfig = /* @__PURE__ */ N.define();
class Y {
  constructor(t, e, i, s, r, o) {
    this.startState = t, this.changes = e, this.selection = i, this.effects = s, this.annotations = r, this.scrollIntoView = o, this._doc = null, this._state = null, i && Ml(i, e.newLength), r.some((l) => l.type == Y.time) || (this.annotations = r.concat(Y.time.of(Date.now())));
  }
  /**
  @internal
  */
  static create(t, e, i, s, r, o) {
    return new Y(t, e, i, s, r, o);
  }
  /**
  The new document produced by the transaction. Contrary to
  [`.state`](https://codemirror.net/6/docs/ref/#state.Transaction.state)`.doc`, accessing this won't
  force the entire new state to be computed right away, so it is
  recommended that [transaction
  filters](https://codemirror.net/6/docs/ref/#state.EditorState^transactionFilter) use this getter
  when they need to look at the new document.
  */
  get newDoc() {
    return this._doc || (this._doc = this.changes.apply(this.startState.doc));
  }
  /**
  The new selection produced by the transaction. If
  [`this.selection`](https://codemirror.net/6/docs/ref/#state.Transaction.selection) is undefined,
  this will [map](https://codemirror.net/6/docs/ref/#state.EditorSelection.map) the start state's
  current selection through the changes made by the transaction.
  */
  get newSelection() {
    return this.selection || this.startState.selection.map(this.changes);
  }
  /**
  The new state created by the transaction. Computed on demand
  (but retained for subsequent access), so it is recommended not to
  access it in [transaction
  filters](https://codemirror.net/6/docs/ref/#state.EditorState^transactionFilter) when possible.
  */
  get state() {
    return this._state || this.startState.applyTransaction(this), this._state;
  }
  /**
  Get the value of the given annotation type, if any.
  */
  annotation(t) {
    for (let e of this.annotations)
      if (e.type == t)
        return e.value;
  }
  /**
  Indicates whether the transaction changed the document.
  */
  get docChanged() {
    return !this.changes.empty;
  }
  /**
  Indicates whether this transaction reconfigures the state
  (through a [configuration compartment](https://codemirror.net/6/docs/ref/#state.Compartment) or
  with a top-level configuration
  [effect](https://codemirror.net/6/docs/ref/#state.StateEffect^reconfigure).
  */
  get reconfigured() {
    return this.startState.config != this.state.config;
  }
  /**
  Returns true if the transaction has a [user
  event](https://codemirror.net/6/docs/ref/#state.Transaction^userEvent) annotation that is equal to
  or more specific than `event`. For example, if the transaction
  has `"select.pointer"` as user event, `"select"` and
  `"select.pointer"` will match it.
  */
  isUserEvent(t) {
    let e = this.annotation(Y.userEvent);
    return !!(e && (e == t || e.length > t.length && e.slice(0, t.length) == t && e[t.length] == "."));
  }
}
Y.time = /* @__PURE__ */ Jt.define();
Y.userEvent = /* @__PURE__ */ Jt.define();
Y.addToHistory = /* @__PURE__ */ Jt.define();
Y.remote = /* @__PURE__ */ Jt.define();
function Gh(n, t) {
  let e = [];
  for (let i = 0, s = 0; ; ) {
    let r, o;
    if (i < n.length && (s == t.length || t[s] >= n[i]))
      r = n[i++], o = n[i++];
    else if (s < t.length)
      r = t[s++], o = t[s++];
    else
      return e;
    !e.length || e[e.length - 1] < r ? e.push(r, o) : e[e.length - 1] < o && (e[e.length - 1] = o);
  }
}
function Nl(n, t, e) {
  var i;
  let s, r, o;
  return e ? (s = t.changes, r = G.empty(t.changes.length), o = n.changes.compose(t.changes)) : (s = t.changes.map(n.changes), r = n.changes.mapDesc(t.changes, !0), o = n.changes.compose(s)), {
    changes: o,
    selection: t.selection ? t.selection.map(r) : (i = n.selection) === null || i === void 0 ? void 0 : i.map(s),
    effects: N.mapEffects(n.effects, s).concat(N.mapEffects(t.effects, r)),
    annotations: n.annotations.length ? n.annotations.concat(t.annotations) : t.annotations,
    scrollIntoView: n.scrollIntoView || t.scrollIntoView
  };
}
function On(n, t, e) {
  let i = t.selection, s = Ee(t.annotations);
  return t.userEvent && (s = s.concat(Y.userEvent.of(t.userEvent))), {
    changes: t.changes instanceof G ? t.changes : G.of(t.changes || [], e, n.facet(Bl)),
    selection: i && (i instanceof k ? i : k.single(i.anchor, i.head)),
    effects: Ee(t.effects),
    annotations: s,
    scrollIntoView: !!t.scrollIntoView
  };
}
function Vl(n, t, e) {
  let i = On(n, t.length ? t[0] : {}, n.doc.length);
  t.length && t[0].filter === !1 && (e = !1);
  for (let r = 1; r < t.length; r++) {
    t[r].filter === !1 && (e = !1);
    let o = !!t[r].sequential;
    i = Nl(i, On(n, t[r], o ? i.changes.newLength : n.doc.length), o);
  }
  let s = Y.create(n, i.changes, i.selection, i.effects, i.annotations, i.scrollIntoView);
  return Zh(e ? Yh(s) : s);
}
function Yh(n) {
  let t = n.startState, e = !0;
  for (let s of t.facet(Rl)) {
    let r = s(n);
    if (r === !1) {
      e = !1;
      break;
    }
    Array.isArray(r) && (e = e === !0 ? r : Gh(e, r));
  }
  if (e !== !0) {
    let s, r;
    if (e === !1)
      r = n.changes.invertedDesc, s = G.empty(t.doc.length);
    else {
      let o = n.changes.filter(e);
      s = o.changes, r = o.filtered.mapDesc(o.changes).invertedDesc;
    }
    n = Y.create(t, s, n.selection && n.selection.map(r), N.mapEffects(n.effects, r), n.annotations, n.scrollIntoView);
  }
  let i = t.facet(Ll);
  for (let s = i.length - 1; s >= 0; s--) {
    let r = i[s](n);
    r instanceof Y ? n = r : Array.isArray(r) && r.length == 1 && r[0] instanceof Y ? n = r[0] : n = Vl(t, Ee(r), !1);
  }
  return n;
}
function Zh(n) {
  let t = n.startState, e = t.facet(El), i = n;
  for (let s = e.length - 1; s >= 0; s--) {
    let r = e[s](n);
    r && Object.keys(r).length && (i = Nl(i, On(t, r, n.changes.newLength), !0));
  }
  return i == n ? n : Y.create(t, n.changes, n.selection, i.effects, i.annotations, i.scrollIntoView);
}
const Jh = [];
function Ee(n) {
  return n == null ? Jh : Array.isArray(n) ? n : [n];
}
var kt = /* @__PURE__ */ (function(n) {
  return n[n.Word = 0] = "Word", n[n.Space = 1] = "Space", n[n.Other = 2] = "Other", n;
})(kt || (kt = {}));
const tf = /[\u00df\u0587\u0590-\u05f4\u0600-\u06ff\u3040-\u309f\u30a0-\u30ff\u3400-\u4db5\u4e00-\u9fcc\uac00-\ud7af]/;
let Cn;
try {
  Cn = /* @__PURE__ */ new RegExp("[\\p{Alphabetic}\\p{Number}_]", "u");
} catch {
}
function ef(n) {
  if (Cn)
    return Cn.test(n);
  for (let t = 0; t < n.length; t++) {
    let e = n[t];
    if (/\w/.test(e) || e > "" && (e.toUpperCase() != e.toLowerCase() || tf.test(e)))
      return !0;
  }
  return !1;
}
function sf(n) {
  return (t) => {
    if (!/\S/.test(t))
      return kt.Space;
    if (ef(t))
      return kt.Word;
    for (let e = 0; e < n.length; e++)
      if (t.indexOf(n[e]) > -1)
        return kt.Word;
    return kt.Other;
  };
}
class V {
  constructor(t, e, i, s, r, o) {
    this.config = t, this.doc = e, this.selection = i, this.values = s, this.status = t.statusTemplate.slice(), this.computeSlot = r, o && (o._state = this);
    for (let l = 0; l < this.config.dynamicSlots.length; l++)
      ri(this, l << 1);
    this.computeSlot = null;
  }
  field(t, e = !0) {
    let i = this.config.address[t.id];
    if (i == null) {
      if (e)
        throw new RangeError("Field is not present in this state");
      return;
    }
    return ri(this, i), as(this, i);
  }
  /**
  Create a [transaction](https://codemirror.net/6/docs/ref/#state.Transaction) that updates this
  state. Any number of [transaction specs](https://codemirror.net/6/docs/ref/#state.TransactionSpec)
  can be passed. Unless
  [`sequential`](https://codemirror.net/6/docs/ref/#state.TransactionSpec.sequential) is set, the
  [changes](https://codemirror.net/6/docs/ref/#state.TransactionSpec.changes) (if any) of each spec
  are assumed to start in the _current_ document (not the document
  produced by previous specs), and its
  [selection](https://codemirror.net/6/docs/ref/#state.TransactionSpec.selection) and
  [effects](https://codemirror.net/6/docs/ref/#state.TransactionSpec.effects) are assumed to refer
  to the document created by its _own_ changes. The resulting
  transaction contains the combined effect of all the different
  specs. For [selection](https://codemirror.net/6/docs/ref/#state.TransactionSpec.selection), later
  specs take precedence over earlier ones.
  */
  update(...t) {
    return Vl(this, t, !0);
  }
  /**
  @internal
  */
  applyTransaction(t) {
    let e = this.config, { base: i, compartments: s } = e;
    for (let l of t.effects)
      l.is(ki.reconfigure) ? (e && (s = /* @__PURE__ */ new Map(), e.compartments.forEach((a, h) => s.set(h, a)), e = null), s.set(l.value.compartment, l.value.extension)) : l.is(N.reconfigure) ? (e = null, i = l.value) : l.is(N.appendConfig) && (e = null, i = Ee(i).concat(l.value));
    let r;
    e ? r = t.startState.values.slice() : (e = ls.resolve(i, s, this), r = new V(e, this.doc, this.selection, e.dynamicSlots.map(() => null), (a, h) => h.reconfigure(a, this), null).values);
    let o = t.startState.facet(Sn) ? t.newSelection : t.newSelection.asSingle();
    new V(e, t.newDoc, o, r, (l, a) => a.update(l, t), t);
  }
  /**
  Create a [transaction spec](https://codemirror.net/6/docs/ref/#state.TransactionSpec) that
  replaces every selection range with the given content.
  */
  replaceSelection(t) {
    return typeof t == "string" && (t = this.toText(t)), this.changeByRange((e) => ({
      changes: { from: e.from, to: e.to, insert: t },
      range: k.cursor(e.from + t.length)
    }));
  }
  /**
  Create a set of changes and a new selection by running the given
  function for each range in the active selection. The function
  can return an optional set of changes (in the coordinate space
  of the start document), plus an updated range (in the coordinate
  space of the document produced by the call's own changes). This
  method will merge all the changes and ranges into a single
  changeset and selection, and return it as a [transaction
  spec](https://codemirror.net/6/docs/ref/#state.TransactionSpec), which can be passed to
  [`update`](https://codemirror.net/6/docs/ref/#state.EditorState.update).
  */
  changeByRange(t) {
    let e = this.selection, i = t(e.ranges[0]), s = this.changes(i.changes), r = [i.range], o = Ee(i.effects);
    for (let l = 1; l < e.ranges.length; l++) {
      let a = t(e.ranges[l]), h = this.changes(a.changes), f = h.map(s);
      for (let u = 0; u < l; u++)
        r[u] = r[u].map(f);
      let c = s.mapDesc(h, !0);
      r.push(a.range.map(c)), s = s.compose(f), o = N.mapEffects(o, f).concat(N.mapEffects(Ee(a.effects), c));
    }
    return {
      changes: s,
      selection: k.create(r, e.mainIndex),
      effects: o
    };
  }
  /**
  Create a [change set](https://codemirror.net/6/docs/ref/#state.ChangeSet) from the given change
  description, taking the state's document length and line
  separator into account.
  */
  changes(t = []) {
    return t instanceof G ? t : G.of(t, this.doc.length, this.facet(V.lineSeparator));
  }
  /**
  Using the state's [line
  separator](https://codemirror.net/6/docs/ref/#state.EditorState^lineSeparator), create a
  [`Text`](https://codemirror.net/6/docs/ref/#state.Text) instance from the given string.
  */
  toText(t) {
    return I.of(t.split(this.facet(V.lineSeparator) || bn));
  }
  /**
  Return the given range of the document as a string.
  */
  sliceDoc(t = 0, e = this.doc.length) {
    return this.doc.sliceString(t, e, this.lineBreak);
  }
  /**
  Get the value of a state [facet](https://codemirror.net/6/docs/ref/#state.Facet).
  */
  facet(t) {
    let e = this.config.address[t.id];
    return e == null ? t.default : (ri(this, e), as(this, e));
  }
  /**
  Convert this state to a JSON-serializable object. When custom
  fields should be serialized, you can pass them in as an object
  mapping property names (in the resulting object, which should
  not use `doc` or `selection`) to fields.
  */
  toJSON(t) {
    let e = {
      doc: this.sliceDoc(),
      selection: this.selection.toJSON()
    };
    if (t)
      for (let i in t) {
        let s = t[i];
        s instanceof At && this.config.address[s.id] != null && (e[i] = s.spec.toJSON(this.field(t[i]), this));
      }
    return e;
  }
  /**
  Deserialize a state from its JSON representation. When custom
  fields should be deserialized, pass the same object you passed
  to [`toJSON`](https://codemirror.net/6/docs/ref/#state.EditorState.toJSON) when serializing as
  third argument.
  */
  static fromJSON(t, e = {}, i) {
    if (!t || typeof t.doc != "string")
      throw new RangeError("Invalid JSON representation for EditorState");
    let s = [];
    if (i) {
      for (let r in i)
        if (Object.prototype.hasOwnProperty.call(t, r)) {
          let o = i[r], l = t[r];
          s.push(o.init((a) => o.spec.fromJSON(l, a)));
        }
    }
    return V.create({
      doc: t.doc,
      selection: k.fromJSON(t.selection),
      extensions: e.extensions ? s.concat([e.extensions]) : s
    });
  }
  /**
  Create a new state. You'll usually only need this when
  initializing an editor—updated states are created by applying
  transactions.
  */
  static create(t = {}) {
    let e = ls.resolve(t.extensions || [], /* @__PURE__ */ new Map()), i = t.doc instanceof I ? t.doc : I.of((t.doc || "").split(e.staticFacet(V.lineSeparator) || bn)), s = t.selection ? t.selection instanceof k ? t.selection : k.single(t.selection.anchor, t.selection.head) : k.single(0);
    return Ml(s, i.length), e.staticFacet(Sn) || (s = s.asSingle()), new V(e, i, s, e.dynamicSlots.map(() => null), (r, o) => o.create(r), null);
  }
  /**
  The size (in columns) of a tab in the document, determined by
  the [`tabSize`](https://codemirror.net/6/docs/ref/#state.EditorState^tabSize) facet.
  */
  get tabSize() {
    return this.facet(V.tabSize);
  }
  /**
  Get the proper [line-break](https://codemirror.net/6/docs/ref/#state.EditorState^lineSeparator)
  string for this state.
  */
  get lineBreak() {
    return this.facet(V.lineSeparator) || `
`;
  }
  /**
  Returns true when the editor is
  [configured](https://codemirror.net/6/docs/ref/#state.EditorState^readOnly) to be read-only.
  */
  get readOnly() {
    return this.facet(Il);
  }
  /**
  Look up a translation for the given phrase (via the
  [`phrases`](https://codemirror.net/6/docs/ref/#state.EditorState^phrases) facet), or return the
  original string if no translation is found.
  
  If additional arguments are passed, they will be inserted in
  place of markers like `$1` (for the first value) and `$2`, etc.
  A single `$` is equivalent to `$1`, and `$$` will produce a
  literal dollar sign.
  */
  phrase(t, ...e) {
    for (let i of this.facet(V.phrases))
      if (Object.prototype.hasOwnProperty.call(i, t)) {
        t = i[t];
        break;
      }
    return e.length && (t = t.replace(/\$(\$|\d*)/g, (i, s) => {
      if (s == "$")
        return "$";
      let r = +(s || 1);
      return !r || r > e.length ? i : e[r - 1];
    })), t;
  }
  /**
  Find the values for a given language data field, provided by the
  the [`languageData`](https://codemirror.net/6/docs/ref/#state.EditorState^languageData) facet.
  
  Examples of language data fields are...
  
  - [`"commentTokens"`](https://codemirror.net/6/docs/ref/#commands.CommentTokens) for specifying
    comment syntax.
  - [`"autocomplete"`](https://codemirror.net/6/docs/ref/#autocomplete.autocompletion^config.override)
    for providing language-specific completion sources.
  - [`"wordChars"`](https://codemirror.net/6/docs/ref/#state.EditorState.charCategorizer) for adding
    characters that should be considered part of words in this
    language.
  - [`"closeBrackets"`](https://codemirror.net/6/docs/ref/#autocomplete.CloseBracketConfig) controls
    bracket closing behavior.
  */
  languageDataAt(t, e, i = -1) {
    let s = [];
    for (let r of this.facet(Dl))
      for (let o of r(this, e, i))
        Object.prototype.hasOwnProperty.call(o, t) && s.push(o[t]);
    return s;
  }
  /**
  Return a function that can categorize strings (expected to
  represent a single [grapheme cluster](https://codemirror.net/6/docs/ref/#state.findClusterBreak))
  into one of:
  
   - Word (contains an alphanumeric character or a character
     explicitly listed in the local language's `"wordChars"`
     language data, which should be a string)
   - Space (contains only whitespace)
   - Other (anything else)
  */
  charCategorizer(t) {
    let e = this.languageDataAt("wordChars", t);
    return sf(e.length ? e[0] : "");
  }
  /**
  Find the word at the given position, meaning the range
  containing all [word](https://codemirror.net/6/docs/ref/#state.CharCategory.Word) characters
  around it. If no word characters are adjacent to the position,
  this returns null.
  */
  wordAt(t) {
    let { text: e, from: i, length: s } = this.doc.lineAt(t), r = this.charCategorizer(t), o = t - i, l = t - i;
    for (; o > 0; ) {
      let a = st(e, o, !1);
      if (r(e.slice(a, o)) != kt.Word)
        break;
      o = a;
    }
    for (; l < s; ) {
      let a = st(e, l);
      if (r(e.slice(l, a)) != kt.Word)
        break;
      l = a;
    }
    return o == l ? null : k.range(o + i, l + i);
  }
}
V.allowMultipleSelections = Sn;
V.tabSize = /* @__PURE__ */ M.define({
  combine: (n) => n.length ? n[0] : 4
});
V.lineSeparator = Bl;
V.readOnly = Il;
V.phrases = /* @__PURE__ */ M.define({
  compare(n, t) {
    let e = Object.keys(n), i = Object.keys(t);
    return e.length == i.length && e.every((s) => n[s] == t[s]);
  }
});
V.languageData = Dl;
V.changeFilter = Rl;
V.transactionFilter = Ll;
V.transactionExtender = El;
ki.reconfigure = /* @__PURE__ */ N.define();
function vi(n, t, e = {}) {
  let i = {};
  for (let s of n)
    for (let r of Object.keys(s)) {
      let o = s[r], l = i[r];
      if (l === void 0)
        i[r] = o;
      else if (!(l === o || o === void 0)) if (Object.hasOwnProperty.call(e, r))
        i[r] = e[r](l, o);
      else
        throw new Error("Config merge conflict for field " + r);
    }
  for (let s in t)
    i[s] === void 0 && (i[s] = t[s]);
  return i;
}
class le {
  /**
  Compare this value with another value. Used when comparing
  rangesets. The default implementation compares by identity.
  Unless you are only creating a fixed number of unique instances
  of your value type, it is a good idea to implement this
  properly.
  */
  eq(t) {
    return this == t;
  }
  /**
  Create a [range](https://codemirror.net/6/docs/ref/#state.Range) with this value.
  */
  range(t, e = t) {
    return An.create(t, e, this);
  }
}
le.prototype.startSide = le.prototype.endSide = 0;
le.prototype.point = !1;
le.prototype.mapMode = ot.TrackDel;
function lr(n, t) {
  return n == t || n.constructor == t.constructor && n.eq(t);
}
let An = class Wl {
  constructor(t, e, i) {
    this.from = t, this.to = e, this.value = i;
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new Wl(t, e, i);
  }
};
function Tn(n, t) {
  return n.from - t.from || n.value.startSide - t.value.startSide;
}
class ar {
  constructor(t, e, i, s) {
    this.from = t, this.to = e, this.value = i, this.maxPoint = s;
  }
  get length() {
    return this.to[this.to.length - 1];
  }
  // Find the index of the given position and side. Use the ranges'
  // `from` pos when `end == false`, `to` when `end == true`.
  findIndex(t, e, i, s = 0) {
    let r = i ? this.to : this.from;
    for (let o = s, l = r.length; ; ) {
      if (o == l)
        return o;
      let a = o + l >> 1, h = r[a] - t || (i ? this.value[a].endSide : this.value[a].startSide) - e;
      if (a == o)
        return h >= 0 ? o : l;
      h >= 0 ? l = a : o = a + 1;
    }
  }
  between(t, e, i, s) {
    for (let r = this.findIndex(e, -1e9, !0), o = this.findIndex(i, 1e9, !1, r); r < o; r++)
      if (s(this.from[r] + t, this.to[r] + t, this.value[r]) === !1)
        return !1;
  }
  map(t, e) {
    let i = [], s = [], r = [], o = -1, l = -1;
    for (let a = 0; a < this.value.length; a++) {
      let h = this.value[a], f = this.from[a] + t, c = this.to[a] + t, u, d;
      if (f == c) {
        let p = e.mapPos(f, h.startSide, h.mapMode);
        if (p == null || (u = d = p, h.startSide != h.endSide && (d = e.mapPos(f, h.endSide), d < u)))
          continue;
      } else if (u = e.mapPos(f, h.startSide), d = e.mapPos(c, h.endSide), u > d || u == d && h.startSide > 0 && h.endSide <= 0)
        continue;
      (d - u || h.endSide - h.startSide) < 0 || (o < 0 && (o = u), h.point && (l = Math.max(l, d - u)), i.push(h), s.push(u - o), r.push(d - o));
    }
    return { mapped: i.length ? new ar(s, r, i, l) : null, pos: o };
  }
}
class L {
  constructor(t, e, i, s) {
    this.chunkPos = t, this.chunk = e, this.nextLayer = i, this.maxPoint = s;
  }
  /**
  @internal
  */
  static create(t, e, i, s) {
    return new L(t, e, i, s);
  }
  /**
  @internal
  */
  get length() {
    let t = this.chunk.length - 1;
    return t < 0 ? 0 : Math.max(this.chunkEnd(t), this.nextLayer.length);
  }
  /**
  The number of ranges in the set.
  */
  get size() {
    if (this.isEmpty)
      return 0;
    let t = this.nextLayer.size;
    for (let e of this.chunk)
      t += e.value.length;
    return t;
  }
  /**
  @internal
  */
  chunkEnd(t) {
    return this.chunkPos[t] + this.chunk[t].length;
  }
  /**
  Update the range set, optionally adding new ranges or filtering
  out existing ones.
  
  (Note: The type parameter is just there as a kludge to work
  around TypeScript variance issues that prevented `RangeSet<X>`
  from being a subtype of `RangeSet<Y>` when `X` is a subtype of
  `Y`.)
  */
  update(t) {
    let { add: e = [], sort: i = !1, filterFrom: s = 0, filterTo: r = this.length } = t, o = t.filter;
    if (e.length == 0 && !o)
      return this;
    if (i && (e = e.slice().sort(Tn)), this.isEmpty)
      return e.length ? L.of(e) : this;
    let l = new Hl(this, null, -1).goto(0), a = 0, h = [], f = new ze();
    for (; l.value || a < e.length; )
      if (a < e.length && (l.from - e[a].from || l.startSide - e[a].value.startSide) >= 0) {
        let c = e[a++];
        f.addInner(c.from, c.to, c.value) || h.push(c);
      } else l.rangeIndex == 1 && l.chunkIndex < this.chunk.length && (a == e.length || this.chunkEnd(l.chunkIndex) < e[a].from) && (!o || s > this.chunkEnd(l.chunkIndex) || r < this.chunkPos[l.chunkIndex]) && f.addChunk(this.chunkPos[l.chunkIndex], this.chunk[l.chunkIndex]) ? l.nextChunk() : ((!o || s > l.to || r < l.from || o(l.from, l.to, l.value)) && (f.addInner(l.from, l.to, l.value) || h.push(An.create(l.from, l.to, l.value))), l.next());
    return f.finishInner(this.nextLayer.isEmpty && !h.length ? L.empty : this.nextLayer.update({ add: h, filter: o, filterFrom: s, filterTo: r }));
  }
  /**
  Map this range set through a set of changes, return the new set.
  */
  map(t) {
    if (t.empty || this.isEmpty)
      return this;
    let e = [], i = [], s = -1;
    for (let o = 0; o < this.chunk.length; o++) {
      let l = this.chunkPos[o], a = this.chunk[o], h = t.touchesRange(l, l + a.length);
      if (h === !1)
        s = Math.max(s, a.maxPoint), e.push(a), i.push(t.mapPos(l));
      else if (h === !0) {
        let { mapped: f, pos: c } = a.map(l, t);
        f && (s = Math.max(s, f.maxPoint), e.push(f), i.push(c));
      }
    }
    let r = this.nextLayer.map(t);
    return e.length == 0 ? r : new L(i, e, r || L.empty, s);
  }
  /**
  Iterate over the ranges that touch the region `from` to `to`,
  calling `f` for each. There is no guarantee that the ranges will
  be reported in any specific order. When the callback returns
  `false`, iteration stops.
  */
  between(t, e, i) {
    if (!this.isEmpty) {
      for (let s = 0; s < this.chunk.length; s++) {
        let r = this.chunkPos[s], o = this.chunk[s];
        if (e >= r && t <= r + o.length && o.between(r, t - r, e - r, i) === !1)
          return;
      }
      this.nextLayer.between(t, e, i);
    }
  }
  /**
  Iterate over the ranges in this set, in order, including all
  ranges that end at or after `from`.
  */
  iter(t = 0) {
    return ci.from([this]).goto(t);
  }
  /**
  @internal
  */
  get isEmpty() {
    return this.nextLayer == this;
  }
  /**
  Iterate over the ranges in a collection of sets, in order,
  starting from `from`.
  */
  static iter(t, e = 0) {
    return ci.from(t).goto(e);
  }
  /**
  Iterate over two groups of sets, calling methods on `comparator`
  to notify it of possible differences.
  */
  static compare(t, e, i, s, r = -1) {
    let o = t.filter((c) => c.maxPoint > 0 || !c.isEmpty && c.maxPoint >= r), l = e.filter((c) => c.maxPoint > 0 || !c.isEmpty && c.maxPoint >= r), a = Xr(o, l, i), h = new Ye(o, a, r), f = new Ye(l, a, r);
    i.iterGaps((c, u, d) => qr(h, c, f, u, d, s)), i.empty && i.length == 0 && qr(h, 0, f, 0, 0, s);
  }
  /**
  Compare the contents of two groups of range sets, returning true
  if they are equivalent in the given range.
  */
  static eq(t, e, i = 0, s) {
    s == null && (s = 999999999);
    let r = t.filter((f) => !f.isEmpty && e.indexOf(f) < 0), o = e.filter((f) => !f.isEmpty && t.indexOf(f) < 0);
    if (r.length != o.length)
      return !1;
    if (!r.length)
      return !0;
    let l = Xr(r, o), a = new Ye(r, l, 0).goto(i), h = new Ye(o, l, 0).goto(i);
    for (; ; ) {
      if (a.to != h.to || !Mn(a.active, h.active) || a.point && (!h.point || !lr(a.point, h.point)))
        return !1;
      if (a.to > s)
        return !0;
      a.next(), h.next();
    }
  }
  /**
  Iterate over a group of range sets at the same time, notifying
  the iterator about the ranges covering every given piece of
  content. Returns the open count (see
  [`SpanIterator.span`](https://codemirror.net/6/docs/ref/#state.SpanIterator.span)) at the end
  of the iteration.
  */
  static spans(t, e, i, s, r = -1) {
    let o = new Ye(t, null, r).goto(e), l = e, a = o.openStart;
    for (; ; ) {
      let h = Math.min(o.to, i);
      if (o.point) {
        let f = o.activeForPoint(o.to), c = o.pointFrom < e ? f.length + 1 : o.point.startSide < 0 ? f.length : Math.min(f.length, a);
        s.point(l, h, o.point, f, c, o.pointRank), a = Math.min(o.openEnd(h), f.length);
      } else h > l && (s.span(l, h, o.active, a), a = o.openEnd(h));
      if (o.to > i)
        return a + (o.point && o.to > i ? 1 : 0);
      l = o.to, o.next();
    }
  }
  /**
  Create a range set for the given range or array of ranges. By
  default, this expects the ranges to be _sorted_ (by start
  position and, if two start at the same position,
  `value.startSide`). You can pass `true` as second argument to
  cause the method to sort them.
  */
  static of(t, e = !1) {
    let i = new ze();
    for (let s of t instanceof An ? [t] : e ? nf(t) : t)
      i.add(s.from, s.to, s.value);
    return i.finish();
  }
  /**
  Join an array of range sets into a single set.
  */
  static join(t) {
    if (!t.length)
      return L.empty;
    let e = t[t.length - 1];
    for (let i = t.length - 2; i >= 0; i--)
      for (let s = t[i]; s != L.empty; s = s.nextLayer)
        e = new L(s.chunkPos, s.chunk, e, Math.max(s.maxPoint, e.maxPoint));
    return e;
  }
}
L.empty = /* @__PURE__ */ new L([], [], null, -1);
function nf(n) {
  if (n.length > 1)
    for (let t = n[0], e = 1; e < n.length; e++) {
      let i = n[e];
      if (Tn(t, i) > 0)
        return n.slice().sort(Tn);
      t = i;
    }
  return n;
}
L.empty.nextLayer = L.empty;
class ze {
  finishChunk(t) {
    this.chunks.push(new ar(this.from, this.to, this.value, this.maxPoint)), this.chunkPos.push(this.chunkStart), this.chunkStart = -1, this.setMaxPoint = Math.max(this.setMaxPoint, this.maxPoint), this.maxPoint = -1, t && (this.from = [], this.to = [], this.value = []);
  }
  /**
  Create an empty builder.
  */
  constructor() {
    this.chunks = [], this.chunkPos = [], this.chunkStart = -1, this.last = null, this.lastFrom = -1e9, this.lastTo = -1e9, this.from = [], this.to = [], this.value = [], this.maxPoint = -1, this.setMaxPoint = -1, this.nextLayer = null;
  }
  /**
  Add a range. Ranges should be added in sorted (by `from` and
  `value.startSide`) order.
  */
  add(t, e, i) {
    this.addInner(t, e, i) || (this.nextLayer || (this.nextLayer = new ze())).add(t, e, i);
  }
  /**
  @internal
  */
  addInner(t, e, i) {
    let s = t - this.lastTo || i.startSide - this.last.endSide;
    if (s <= 0 && (t - this.lastFrom || i.startSide - this.last.startSide) < 0)
      throw new Error("Ranges must be added sorted by `from` position and `startSide`");
    return s < 0 ? !1 : (this.from.length == 250 && this.finishChunk(!0), this.chunkStart < 0 && (this.chunkStart = t), this.from.push(t - this.chunkStart), this.to.push(e - this.chunkStart), this.last = i, this.lastFrom = t, this.lastTo = e, this.value.push(i), i.point && (this.maxPoint = Math.max(this.maxPoint, e - t)), !0);
  }
  /**
  @internal
  */
  addChunk(t, e) {
    if ((t - this.lastTo || e.value[0].startSide - this.last.endSide) < 0)
      return !1;
    this.from.length && this.finishChunk(!0), this.setMaxPoint = Math.max(this.setMaxPoint, e.maxPoint), this.chunks.push(e), this.chunkPos.push(t);
    let i = e.value.length - 1;
    return this.last = e.value[i], this.lastFrom = e.from[i] + t, this.lastTo = e.to[i] + t, !0;
  }
  /**
  Finish the range set. Returns the new set. The builder can't be
  used anymore after this has been called.
  */
  finish() {
    return this.finishInner(L.empty);
  }
  /**
  @internal
  */
  finishInner(t) {
    if (this.from.length && this.finishChunk(!1), this.chunks.length == 0)
      return t;
    let e = L.create(this.chunkPos, this.chunks, this.nextLayer ? this.nextLayer.finishInner(t) : t, this.setMaxPoint);
    return this.from = null, e;
  }
}
function Xr(n, t, e) {
  let i = /* @__PURE__ */ new Map();
  for (let r of n)
    for (let o = 0; o < r.chunk.length; o++)
      r.chunk[o].maxPoint <= 0 && i.set(r.chunk[o], r.chunkPos[o]);
  let s = /* @__PURE__ */ new Set();
  for (let r of t)
    for (let o = 0; o < r.chunk.length; o++) {
      let l = i.get(r.chunk[o]);
      l != null && (e ? e.mapPos(l) : l) == r.chunkPos[o] && !(e != null && e.touchesRange(l, l + r.chunk[o].length)) && s.add(r.chunk[o]);
    }
  return s;
}
class Hl {
  constructor(t, e, i, s = 0) {
    this.layer = t, this.skip = e, this.minPoint = i, this.rank = s;
  }
  get startSide() {
    return this.value ? this.value.startSide : 0;
  }
  get endSide() {
    return this.value ? this.value.endSide : 0;
  }
  goto(t, e = -1e9) {
    return this.chunkIndex = this.rangeIndex = 0, this.gotoInner(t, e, !1), this;
  }
  gotoInner(t, e, i) {
    for (; this.chunkIndex < this.layer.chunk.length; ) {
      let s = this.layer.chunk[this.chunkIndex];
      if (!(this.skip && this.skip.has(s) || this.layer.chunkEnd(this.chunkIndex) < t || s.maxPoint < this.minPoint))
        break;
      this.chunkIndex++, i = !1;
    }
    if (this.chunkIndex < this.layer.chunk.length) {
      let s = this.layer.chunk[this.chunkIndex].findIndex(t - this.layer.chunkPos[this.chunkIndex], e, !0);
      (!i || this.rangeIndex < s) && this.setRangeIndex(s);
    }
    this.next();
  }
  forward(t, e) {
    (this.to - t || this.endSide - e) < 0 && this.gotoInner(t, e, !0);
  }
  next() {
    for (; ; )
      if (this.chunkIndex == this.layer.chunk.length) {
        this.from = this.to = 1e9, this.value = null;
        break;
      } else {
        let t = this.layer.chunkPos[this.chunkIndex], e = this.layer.chunk[this.chunkIndex], i = t + e.from[this.rangeIndex];
        if (this.from = i, this.to = t + e.to[this.rangeIndex], this.value = e.value[this.rangeIndex], this.setRangeIndex(this.rangeIndex + 1), this.minPoint < 0 || this.value.point && this.to - this.from >= this.minPoint)
          break;
      }
  }
  setRangeIndex(t) {
    if (t == this.layer.chunk[this.chunkIndex].value.length) {
      if (this.chunkIndex++, this.skip)
        for (; this.chunkIndex < this.layer.chunk.length && this.skip.has(this.layer.chunk[this.chunkIndex]); )
          this.chunkIndex++;
      this.rangeIndex = 0;
    } else
      this.rangeIndex = t;
  }
  nextChunk() {
    this.chunkIndex++, this.rangeIndex = 0, this.next();
  }
  compare(t) {
    return this.from - t.from || this.startSide - t.startSide || this.rank - t.rank || this.to - t.to || this.endSide - t.endSide;
  }
}
class ci {
  constructor(t) {
    this.heap = t;
  }
  static from(t, e = null, i = -1) {
    let s = [];
    for (let r = 0; r < t.length; r++)
      for (let o = t[r]; !o.isEmpty; o = o.nextLayer)
        o.maxPoint >= i && s.push(new Hl(o, e, i, r));
    return s.length == 1 ? s[0] : new ci(s);
  }
  get startSide() {
    return this.value ? this.value.startSide : 0;
  }
  goto(t, e = -1e9) {
    for (let i of this.heap)
      i.goto(t, e);
    for (let i = this.heap.length >> 1; i >= 0; i--)
      qs(this.heap, i);
    return this.next(), this;
  }
  forward(t, e) {
    for (let i of this.heap)
      i.forward(t, e);
    for (let i = this.heap.length >> 1; i >= 0; i--)
      qs(this.heap, i);
    (this.to - t || this.value.endSide - e) < 0 && this.next();
  }
  next() {
    if (this.heap.length == 0)
      this.from = this.to = 1e9, this.value = null, this.rank = -1;
    else {
      let t = this.heap[0];
      this.from = t.from, this.to = t.to, this.value = t.value, this.rank = t.rank, t.value && t.next(), qs(this.heap, 0);
    }
  }
}
function qs(n, t) {
  for (let e = n[t]; ; ) {
    let i = (t << 1) + 1;
    if (i >= n.length)
      break;
    let s = n[i];
    if (i + 1 < n.length && s.compare(n[i + 1]) >= 0 && (s = n[i + 1], i++), e.compare(s) < 0)
      break;
    n[i] = e, n[t] = s, t = i;
  }
}
class Ye {
  constructor(t, e, i) {
    this.minPoint = i, this.active = [], this.activeTo = [], this.activeRank = [], this.minActive = -1, this.point = null, this.pointFrom = 0, this.pointRank = 0, this.to = -1e9, this.endSide = 0, this.openStart = -1, this.cursor = ci.from(t, e, i);
  }
  goto(t, e = -1e9) {
    return this.cursor.goto(t, e), this.active.length = this.activeTo.length = this.activeRank.length = 0, this.minActive = -1, this.to = t, this.endSide = e, this.openStart = -1, this.next(), this;
  }
  forward(t, e) {
    for (; this.minActive > -1 && (this.activeTo[this.minActive] - t || this.active[this.minActive].endSide - e) < 0; )
      this.removeActive(this.minActive);
    this.cursor.forward(t, e);
  }
  removeActive(t) {
    Bi(this.active, t), Bi(this.activeTo, t), Bi(this.activeRank, t), this.minActive = Ur(this.active, this.activeTo);
  }
  addActive(t) {
    let e = 0, { value: i, to: s, rank: r } = this.cursor;
    for (; e < this.activeRank.length && (r - this.activeRank[e] || s - this.activeTo[e]) > 0; )
      e++;
    Ri(this.active, e, i), Ri(this.activeTo, e, s), Ri(this.activeRank, e, r), t && Ri(t, e, this.cursor.from), this.minActive = Ur(this.active, this.activeTo);
  }
  // After calling this, if `this.point` != null, the next range is a
  // point. Otherwise, it's a regular range, covered by `this.active`.
  next() {
    let t = this.to, e = this.point;
    this.point = null;
    let i = this.openStart < 0 ? [] : null;
    for (; ; ) {
      let s = this.minActive;
      if (s > -1 && (this.activeTo[s] - this.cursor.from || this.active[s].endSide - this.cursor.startSide) < 0) {
        if (this.activeTo[s] > t) {
          this.to = this.activeTo[s], this.endSide = this.active[s].endSide;
          break;
        }
        this.removeActive(s), i && Bi(i, s);
      } else if (this.cursor.value)
        if (this.cursor.from > t) {
          this.to = this.cursor.from, this.endSide = this.cursor.startSide;
          break;
        } else {
          let r = this.cursor.value;
          if (!r.point)
            this.addActive(i), this.cursor.next();
          else if (e && this.cursor.to == this.to && this.cursor.from < this.cursor.to)
            this.cursor.next();
          else {
            this.point = r, this.pointFrom = this.cursor.from, this.pointRank = this.cursor.rank, this.to = this.cursor.to, this.endSide = r.endSide, this.cursor.next(), this.forward(this.to, this.endSide);
            break;
          }
        }
      else {
        this.to = this.endSide = 1e9;
        break;
      }
    }
    if (i) {
      this.openStart = 0;
      for (let s = i.length - 1; s >= 0 && i[s] < t; s--)
        this.openStart++;
    }
  }
  activeForPoint(t) {
    if (!this.active.length)
      return this.active;
    let e = [];
    for (let i = this.active.length - 1; i >= 0 && !(this.activeRank[i] < this.pointRank); i--)
      (this.activeTo[i] > t || this.activeTo[i] == t && this.active[i].endSide >= this.point.endSide) && e.push(this.active[i]);
    return e.reverse();
  }
  openEnd(t) {
    let e = 0;
    for (let i = this.activeTo.length - 1; i >= 0 && this.activeTo[i] > t; i--)
      e++;
    return e;
  }
}
function qr(n, t, e, i, s, r) {
  n.goto(t), e.goto(i);
  let o = i + s, l = i, a = i - t, h = !!r.boundChange;
  for (let f = !1; ; ) {
    let c = n.to + a - e.to, u = c || n.endSide - e.endSide, d = u < 0 ? n.to + a : e.to, p = Math.min(d, o);
    if (n.point || e.point ? (n.point && e.point && lr(n.point, e.point) && Mn(n.activeForPoint(n.to), e.activeForPoint(e.to)) || r.comparePoint(l, p, n.point, e.point), f = !1) : (f && r.boundChange(l), p > l && !Mn(n.active, e.active) && r.compareRange(l, p, n.active, e.active), h && p < o && (c || n.openEnd(d) != e.openEnd(d)) && (f = !0)), d > o)
      break;
    l = d, u <= 0 && n.next(), u >= 0 && e.next();
  }
}
function Mn(n, t) {
  if (n.length != t.length)
    return !1;
  for (let e = 0; e < n.length; e++)
    if (n[e] != t[e] && !lr(n[e], t[e]))
      return !1;
  return !0;
}
function Bi(n, t) {
  for (let e = t, i = n.length - 1; e < i; e++)
    n[e] = n[e + 1];
  n.pop();
}
function Ri(n, t, e) {
  for (let i = n.length - 1; i >= t; i--)
    n[i + 1] = n[i];
  n[t] = e;
}
function Ur(n, t) {
  let e = -1, i = 1e9;
  for (let s = 0; s < t.length; s++)
    (t[s] - i || n[s].endSide - n[e].endSide) < 0 && (e = s, i = t[s]);
  return e;
}
function Rs(n, t, e = n.length) {
  let i = 0;
  for (let s = 0; s < e && s < n.length; )
    n.charCodeAt(s) == 9 ? (i += t - i % t, s++) : (i++, s = st(n, s));
  return i;
}
function rf(n, t, e, i) {
  for (let s = 0, r = 0; ; ) {
    if (r >= t)
      return s;
    if (s == n.length)
      break;
    r += n.charCodeAt(s) == 9 ? e - r % e : 1, s = st(n, s);
  }
  return n.length;
}
const Pn = "ͼ", Kr = typeof Symbol > "u" ? "__" + Pn : Symbol.for(Pn), Dn = typeof Symbol > "u" ? "__styleSet" + Math.floor(Math.random() * 1e8) : Symbol("styleSet"), _r = typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : {};
class ae {
  // :: (Object<Style>, ?{finish: ?(string) → string})
  // Create a style module from the given spec.
  //
  // When `finish` is given, it is called on regular (non-`@`)
  // selectors (after `&` expansion) to compute the final selector.
  constructor(t, e) {
    this.rules = [];
    let { finish: i } = e || {};
    function s(o) {
      return /^@/.test(o) ? [o] : o.split(/,\s*/);
    }
    function r(o, l, a, h) {
      let f = [], c = /^@(\w+)\b/.exec(o[0]), u = c && c[1] == "keyframes";
      if (c && l == null) return a.push(o[0] + ";");
      for (let d in l) {
        let p = l[d];
        if (/&/.test(d))
          r(
            d.split(/,\s*/).map((g) => o.map((m) => g.replace(/&/, m))).reduce((g, m) => g.concat(m)),
            p,
            a
          );
        else if (p && typeof p == "object") {
          if (!c) throw new RangeError("The value of a property (" + d + ") should be a primitive value.");
          r(s(d), p, f, u);
        } else p != null && f.push(d.replace(/_.*/, "").replace(/[A-Z]/g, (g) => "-" + g.toLowerCase()) + ": " + p + ";");
      }
      (f.length || u) && a.push((i && !c && !h ? o.map(i) : o).join(", ") + " {" + f.join(" ") + "}");
    }
    for (let o in t) r(s(o), t[o], this.rules);
  }
  // :: () → string
  // Returns a string containing the module's CSS rules.
  getRules() {
    return this.rules.join(`
`);
  }
  // :: () → string
  // Generate a new unique CSS class name.
  static newName() {
    let t = _r[Kr] || 1;
    return _r[Kr] = t + 1, Pn + t.toString(36);
  }
  // :: (union<Document, ShadowRoot>, union<[StyleModule], StyleModule>, ?{nonce: ?string})
  //
  // Mount the given set of modules in the given DOM root, which ensures
  // that the CSS rules defined by the module are available in that
  // context.
  //
  // Rules are only added to the document once per root.
  //
  // Rule order will follow the order of the modules, so that rules from
  // modules later in the array take precedence of those from earlier
  // modules. If you call this function multiple times for the same root
  // in a way that changes the order of already mounted modules, the old
  // order will be changed.
  //
  // If a Content Security Policy nonce is provided, it is added to
  // the `<style>` tag generated by the library.
  static mount(t, e, i) {
    let s = t[Dn], r = i && i.nonce;
    s ? r && s.setNonce(r) : s = new of(t, r), s.mount(Array.isArray(e) ? e : [e], t);
  }
}
let Gr = /* @__PURE__ */ new Map();
class of {
  constructor(t, e) {
    let i = t.ownerDocument || t, s = i.defaultView;
    if (!t.head && t.adoptedStyleSheets && s.CSSStyleSheet) {
      let r = Gr.get(i);
      if (r) return t[Dn] = r;
      this.sheet = new s.CSSStyleSheet(), Gr.set(i, this);
    } else
      this.styleTag = i.createElement("style"), e && this.styleTag.setAttribute("nonce", e);
    this.modules = [], t[Dn] = this;
  }
  mount(t, e) {
    let i = this.sheet, s = 0, r = 0;
    for (let o = 0; o < t.length; o++) {
      let l = t[o], a = this.modules.indexOf(l);
      if (a < r && a > -1 && (this.modules.splice(a, 1), r--, a = -1), a == -1) {
        if (this.modules.splice(r++, 0, l), i) for (let h = 0; h < l.rules.length; h++)
          i.insertRule(l.rules[h], s++);
      } else {
        for (; r < a; ) s += this.modules[r++].rules.length;
        s += l.rules.length, r++;
      }
    }
    if (i)
      e.adoptedStyleSheets.indexOf(this.sheet) < 0 && (e.adoptedStyleSheets = [this.sheet, ...e.adoptedStyleSheets]);
    else {
      let o = "";
      for (let a = 0; a < this.modules.length; a++)
        o += this.modules[a].getRules() + `
`;
      this.styleTag.textContent = o;
      let l = e.head || e;
      this.styleTag.parentNode != l && l.insertBefore(this.styleTag, l.firstChild);
    }
  }
  setNonce(t) {
    this.styleTag && this.styleTag.getAttribute("nonce") != t && this.styleTag.setAttribute("nonce", t);
  }
}
var he = {
  8: "Backspace",
  9: "Tab",
  10: "Enter",
  12: "NumLock",
  13: "Enter",
  16: "Shift",
  17: "Control",
  18: "Alt",
  20: "CapsLock",
  27: "Escape",
  32: " ",
  33: "PageUp",
  34: "PageDown",
  35: "End",
  36: "Home",
  37: "ArrowLeft",
  38: "ArrowUp",
  39: "ArrowRight",
  40: "ArrowDown",
  44: "PrintScreen",
  45: "Insert",
  46: "Delete",
  59: ";",
  61: "=",
  91: "Meta",
  92: "Meta",
  106: "*",
  107: "+",
  108: ",",
  109: "-",
  110: ".",
  111: "/",
  144: "NumLock",
  145: "ScrollLock",
  160: "Shift",
  161: "Shift",
  162: "Control",
  163: "Control",
  164: "Alt",
  165: "Alt",
  173: "-",
  186: ";",
  187: "=",
  188: ",",
  189: "-",
  190: ".",
  191: "/",
  192: "`",
  219: "[",
  220: "\\",
  221: "]",
  222: "'"
}, ui = {
  48: ")",
  49: "!",
  50: "@",
  51: "#",
  52: "$",
  53: "%",
  54: "^",
  55: "&",
  56: "*",
  57: "(",
  59: ":",
  61: "+",
  173: "_",
  186: ":",
  187: "+",
  188: "<",
  189: "_",
  190: ">",
  191: "?",
  192: "~",
  219: "{",
  220: "|",
  221: "}",
  222: '"'
}, lf = typeof navigator < "u" && /Mac/.test(navigator.platform), af = typeof navigator < "u" && /MSIE \d|Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(navigator.userAgent);
for (var et = 0; et < 10; et++) he[48 + et] = he[96 + et] = String(et);
for (var et = 1; et <= 24; et++) he[et + 111] = "F" + et;
for (var et = 65; et <= 90; et++)
  he[et] = String.fromCharCode(et + 32), ui[et] = String.fromCharCode(et);
for (var Us in he) ui.hasOwnProperty(Us) || (ui[Us] = he[Us]);
function hf(n) {
  var t = lf && n.metaKey && n.shiftKey && !n.ctrlKey && !n.altKey || af && n.shiftKey && n.key && n.key.length == 1 || n.key == "Unidentified", e = !t && n.key || (n.shiftKey ? ui : he)[n.keyCode] || n.key || "Unidentified";
  return e == "Esc" && (e = "Escape"), e == "Del" && (e = "Delete"), e == "Left" && (e = "ArrowLeft"), e == "Up" && (e = "ArrowUp"), e == "Right" && (e = "ArrowRight"), e == "Down" && (e = "ArrowDown"), e;
}
function Qt() {
  var n = arguments[0];
  typeof n == "string" && (n = document.createElement(n));
  var t = 1, e = arguments[1];
  if (e && typeof e == "object" && e.nodeType == null && !Array.isArray(e)) {
    for (var i in e) if (Object.prototype.hasOwnProperty.call(e, i)) {
      var s = e[i];
      typeof s == "string" ? n.setAttribute(i, s) : s != null && (n[i] = s);
    }
    t++;
  }
  for (; t < arguments.length; t++) Fl(n, arguments[t]);
  return n;
}
function Fl(n, t) {
  if (typeof t == "string")
    n.appendChild(document.createTextNode(t));
  else if (t != null) if (t.nodeType != null)
    n.appendChild(t);
  else if (Array.isArray(t))
    for (var e = 0; e < t.length; e++) Fl(n, t[e]);
  else
    throw new RangeError("Unsupported child node: " + t);
}
let rt = typeof navigator < "u" ? navigator : { userAgent: "", vendor: "", platform: "" }, Bn = typeof document < "u" ? document : { documentElement: { style: {} } };
const Rn = /* @__PURE__ */ /Edge\/(\d+)/.exec(rt.userAgent), zl = /* @__PURE__ */ /MSIE \d/.test(rt.userAgent), Ln = /* @__PURE__ */ /Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(rt.userAgent), Ls = !!(zl || Ln || Rn), Yr = !Ls && /* @__PURE__ */ /gecko\/(\d+)/i.test(rt.userAgent), Ks = !Ls && /* @__PURE__ */ /Chrome\/(\d+)/.exec(rt.userAgent), ff = "webkitFontSmoothing" in Bn.documentElement.style, En = !Ls && /* @__PURE__ */ /Apple Computer/.test(rt.vendor), Zr = En && (/* @__PURE__ */ /Mobile\/\w+/.test(rt.userAgent) || rt.maxTouchPoints > 2);
var A = {
  mac: Zr || /* @__PURE__ */ /Mac/.test(rt.platform),
  windows: /* @__PURE__ */ /Win/.test(rt.platform),
  linux: /* @__PURE__ */ /Linux|X11/.test(rt.platform),
  ie: Ls,
  ie_version: zl ? Bn.documentMode || 6 : Ln ? +Ln[1] : Rn ? +Rn[1] : 0,
  gecko: Yr,
  gecko_version: Yr ? +(/* @__PURE__ */ /Firefox\/(\d+)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  chrome: !!Ks,
  chrome_version: Ks ? +Ks[1] : 0,
  ios: Zr,
  android: /* @__PURE__ */ /Android\b/.test(rt.userAgent),
  webkit_version: ff ? +(/* @__PURE__ */ /\bAppleWebKit\/(\d+)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  safari: En,
  safari_version: En ? +(/* @__PURE__ */ /\bVersion\/(\d+(\.\d+)?)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  tabSize: Bn.documentElement.style.tabSize != null ? "tab-size" : "-moz-tab-size"
};
function hr(n, t) {
  for (let e in n)
    e == "class" && t.class ? t.class += " " + n.class : e == "style" && t.style ? t.style += ";" + n.style : t[e] = n[e];
  return t;
}
const hs = /* @__PURE__ */ Object.create(null);
function fr(n, t, e) {
  if (n == t)
    return !0;
  n || (n = hs), t || (t = hs);
  let i = Object.keys(n), s = Object.keys(t);
  if (i.length - 0 != s.length - 0)
    return !1;
  for (let r of i)
    if (r != e && (s.indexOf(r) == -1 || n[r] !== t[r]))
      return !1;
  return !0;
}
function cf(n, t) {
  for (let e = n.attributes.length - 1; e >= 0; e--) {
    let i = n.attributes[e].name;
    t[i] == null && n.removeAttribute(i);
  }
  for (let e in t) {
    let i = t[e];
    e == "style" ? n.style.cssText = i : n.getAttribute(e) != i && n.setAttribute(e, i);
  }
}
function Jr(n, t, e) {
  let i = !1;
  if (t)
    for (let s in t)
      e && s in e || (i = !0, s == "style" ? n.style.cssText = "" : n.removeAttribute(s));
  if (e)
    for (let s in e)
      t && t[s] == e[s] || (i = !0, s == "style" ? n.style.cssText = e[s] : n.setAttribute(s, e[s]));
  return i;
}
function uf(n) {
  let t = /* @__PURE__ */ Object.create(null);
  for (let e = 0; e < n.attributes.length; e++) {
    let i = n.attributes[e];
    t[i.name] = i.value;
  }
  return t;
}
class Si {
  /**
  Compare this instance to another instance of the same type.
  (TypeScript can't express this, but only instances of the same
  specific class will be passed to this method.) This is used to
  avoid redrawing widgets when they are replaced by a new
  decoration of the same type. The default implementation just
  returns `false`, which will cause new instances of the widget to
  always be redrawn.
  */
  eq(t) {
    return !1;
  }
  /**
  Update a DOM element created by a widget of the same type (but
  different, non-`eq` content) to reflect this widget. May return
  true to indicate that it could update, false to indicate it
  couldn't (in which case the widget will be redrawn). The default
  implementation just returns false.
  */
  updateDOM(t, e) {
    return !1;
  }
  /**
  @internal
  */
  compare(t) {
    return this == t || this.constructor == t.constructor && this.eq(t);
  }
  /**
  The estimated height this widget will have, to be used when
  estimating the height of content that hasn't been drawn. May
  return -1 to indicate you don't know. The default implementation
  returns -1.
  */
  get estimatedHeight() {
    return -1;
  }
  /**
  For inline widgets that are displayed inline (as opposed to
  `inline-block`) and introduce line breaks (through `<br>` tags
  or textual newlines), this must indicate the amount of line
  breaks they introduce. Defaults to 0.
  */
  get lineBreaks() {
    return 0;
  }
  /**
  Can be used to configure which kinds of events inside the widget
  should be ignored by the editor. The default is to ignore all
  events.
  */
  ignoreEvent(t) {
    return !0;
  }
  /**
  Override the way screen coordinates for positions at/in the
  widget are found. `pos` will be the offset into the widget, and
  `side` the side of the position that is being queried—less than
  zero for before, greater than zero for after, and zero for
  directly at that position.
  */
  coordsAt(t, e, i) {
    return null;
  }
  /**
  @internal
  */
  get isHidden() {
    return !1;
  }
  /**
  @internal
  */
  get editable() {
    return !1;
  }
  /**
  This is called when the an instance of the widget is removed
  from the editor view.
  */
  destroy(t) {
  }
}
var ut = /* @__PURE__ */ (function(n) {
  return n[n.Text = 0] = "Text", n[n.WidgetBefore = 1] = "WidgetBefore", n[n.WidgetAfter = 2] = "WidgetAfter", n[n.WidgetRange = 3] = "WidgetRange", n;
})(ut || (ut = {}));
class H extends le {
  constructor(t, e, i, s) {
    super(), this.startSide = t, this.endSide = e, this.widget = i, this.spec = s;
  }
  /**
  @internal
  */
  get heightRelevant() {
    return !1;
  }
  /**
  Create a mark decoration, which influences the styling of the
  content in its range. Nested mark decorations will cause nested
  DOM elements to be created. Nesting order is determined by
  precedence of the [facet](https://codemirror.net/6/docs/ref/#view.EditorView^decorations), with
  the higher-precedence decorations creating the inner DOM nodes.
  Such elements are split on line boundaries and on the boundaries
  of lower-precedence decorations.
  */
  static mark(t) {
    return new Oi(t);
  }
  /**
  Create a widget decoration, which displays a DOM element at the
  given position.
  */
  static widget(t) {
    let e = Math.max(-1e4, Math.min(1e4, t.side || 0)), i = !!t.block;
    return e += i && !t.inlineOrder ? e > 0 ? 3e8 : -4e8 : e > 0 ? 1e8 : -1e8, new Ce(t, e, e, i, t.widget || null, !1);
  }
  /**
  Create a replace decoration which replaces the given range with
  a widget, or simply hides it.
  */
  static replace(t) {
    let e = !!t.block, i, s;
    if (t.isBlockGap)
      i = -5e8, s = 4e8;
    else {
      let { start: r, end: o } = Ql(t, e);
      i = (r ? e ? -3e8 : -1 : 5e8) - 1, s = (o ? e ? 2e8 : 1 : -6e8) + 1;
    }
    return new Ce(t, i, s, e, t.widget || null, !0);
  }
  /**
  Create a line decoration, which can add DOM attributes to the
  line starting at the given position.
  */
  static line(t) {
    return new Ci(t);
  }
  /**
  Build a [`DecorationSet`](https://codemirror.net/6/docs/ref/#view.DecorationSet) from the given
  decorated range or ranges. If the ranges aren't already sorted,
  pass `true` for `sort` to make the library sort them for you.
  */
  static set(t, e = !1) {
    return L.of(t, e);
  }
  /**
  @internal
  */
  hasHeight() {
    return this.widget ? this.widget.estimatedHeight > -1 : !1;
  }
}
H.none = L.empty;
class Oi extends H {
  constructor(t) {
    let { start: e, end: i } = Ql(t);
    super(e ? -1 : 5e8, i ? 1 : -6e8, null, t), this.tagName = t.tagName || "span", this.attrs = t.class && t.attributes ? hr(t.attributes, { class: t.class }) : t.class ? { class: t.class } : t.attributes || hs;
  }
  eq(t) {
    return this == t || t instanceof Oi && this.tagName == t.tagName && fr(this.attrs, t.attrs);
  }
  range(t, e = t) {
    if (t >= e)
      throw new RangeError("Mark decorations may not be empty");
    return super.range(t, e);
  }
}
Oi.prototype.point = !1;
class Ci extends H {
  constructor(t) {
    super(-2e8, -2e8, null, t);
  }
  eq(t) {
    return t instanceof Ci && this.spec.class == t.spec.class && fr(this.spec.attributes, t.spec.attributes);
  }
  range(t, e = t) {
    if (e != t)
      throw new RangeError("Line decoration ranges must be zero-length");
    return super.range(t, e);
  }
}
Ci.prototype.mapMode = ot.TrackBefore;
Ci.prototype.point = !0;
class Ce extends H {
  constructor(t, e, i, s, r, o) {
    super(e, i, r, t), this.block = s, this.isReplace = o, this.mapMode = s ? e <= 0 ? ot.TrackBefore : ot.TrackAfter : ot.TrackDel;
  }
  // Only relevant when this.block == true
  get type() {
    return this.startSide != this.endSide ? ut.WidgetRange : this.startSide <= 0 ? ut.WidgetBefore : ut.WidgetAfter;
  }
  get heightRelevant() {
    return this.block || !!this.widget && (this.widget.estimatedHeight >= 5 || this.widget.lineBreaks > 0);
  }
  eq(t) {
    return t instanceof Ce && df(this.widget, t.widget) && this.block == t.block && this.startSide == t.startSide && this.endSide == t.endSide;
  }
  range(t, e = t) {
    if (this.isReplace && (t > e || t == e && this.startSide > 0 && this.endSide <= 0))
      throw new RangeError("Invalid range for replacement decoration");
    if (!this.isReplace && e != t)
      throw new RangeError("Widget decorations can only have zero-length ranges");
    return super.range(t, e);
  }
}
Ce.prototype.point = !0;
function Ql(n, t = !1) {
  let { inclusiveStart: e, inclusiveEnd: i } = n;
  return e == null && (e = n.inclusive), i == null && (i = n.inclusive), { start: e ?? t, end: i ?? t };
}
function df(n, t) {
  return n == t || !!(n && t && n.compare(t));
}
function Ie(n, t, e, i = 0) {
  let s = e.length - 1;
  s >= 0 && e[s] + i >= n ? e[s] = Math.max(e[s], t) : e.push(n, t);
}
class di extends le {
  constructor(t, e) {
    super(), this.tagName = t, this.attributes = e;
  }
  eq(t) {
    return t == this || t instanceof di && this.tagName == t.tagName && fr(this.attributes, t.attributes);
  }
  /**
  Create a block wrapper object with the given tag name and
  attributes.
  */
  static create(t) {
    return new di(t.tagName, t.attributes || hs);
  }
  /**
  Create a range set from the given block wrapper ranges.
  */
  static set(t, e = !1) {
    return L.of(t, e);
  }
}
di.prototype.startSide = di.prototype.endSide = -1;
function pi(n) {
  let t;
  return n.nodeType == 11 ? t = n.getSelection ? n : n.ownerDocument : t = n, t.getSelection();
}
function In(n, t) {
  return t ? n == t || n.contains(t.nodeType != 1 ? t.parentNode : t) : !1;
}
function oi(n, t) {
  if (!t.anchorNode)
    return !1;
  try {
    return In(n, t.anchorNode);
  } catch {
    return !1;
  }
}
function es(n) {
  return n.nodeType == 3 ? gi(n, 0, n.nodeValue.length).getClientRects() : n.nodeType == 1 ? n.getClientRects() : [];
}
function li(n, t, e, i) {
  return e ? to(n, t, e, i, -1) || to(n, t, e, i, 1) : !1;
}
function fe(n) {
  for (var t = 0; ; t++)
    if (n = n.previousSibling, !n)
      return t;
}
function fs(n) {
  return n.nodeType == 1 && /^(DIV|P|LI|UL|OL|BLOCKQUOTE|DD|DT|H\d|SECTION|PRE)$/.test(n.nodeName);
}
function to(n, t, e, i, s) {
  for (; ; ) {
    if (n == e && t == i)
      return !0;
    if (t == (s < 0 ? 0 : Zt(n))) {
      if (n.nodeName == "DIV")
        return !1;
      let r = n.parentNode;
      if (!r || r.nodeType != 1)
        return !1;
      t = fe(n) + (s < 0 ? 0 : 1), n = r;
    } else if (n.nodeType == 1) {
      if (n = n.childNodes[t + (s < 0 ? -1 : 0)], n.nodeType == 1 && n.contentEditable == "false")
        return !1;
      t = s < 0 ? Zt(n) : 0;
    } else
      return !1;
  }
}
function Zt(n) {
  return n.nodeType == 3 ? n.nodeValue.length : n.childNodes.length;
}
function cs(n, t) {
  let e = t ? n.left : n.right;
  return { left: e, right: e, top: n.top, bottom: n.bottom };
}
function pf(n) {
  let t = n.visualViewport;
  return t ? {
    left: 0,
    right: t.width,
    top: 0,
    bottom: t.height
  } : {
    left: 0,
    right: n.innerWidth,
    top: 0,
    bottom: n.innerHeight
  };
}
function $l(n, t) {
  let e = t.width / n.offsetWidth, i = t.height / n.offsetHeight;
  return (e > 0.995 && e < 1.005 || !isFinite(e) || Math.abs(t.width - n.offsetWidth) < 1) && (e = 1), (i > 0.995 && i < 1.005 || !isFinite(i) || Math.abs(t.height - n.offsetHeight) < 1) && (i = 1), { scaleX: e, scaleY: i };
}
function gf(n, t, e, i, s, r, o, l) {
  let a = n.ownerDocument, h = a.defaultView || window;
  for (let f = n, c = !1; f && !c; )
    if (f.nodeType == 1) {
      let u, d = f == a.body, p = 1, g = 1;
      if (d)
        u = pf(h);
      else {
        if (/^(fixed|sticky)$/.test(getComputedStyle(f).position) && (c = !0), f.scrollHeight <= f.clientHeight && f.scrollWidth <= f.clientWidth) {
          f = f.assignedSlot || f.parentNode;
          continue;
        }
        let b = f.getBoundingClientRect();
        ({ scaleX: p, scaleY: g } = $l(f, b)), u = {
          left: b.left,
          right: b.left + f.clientWidth * p,
          top: b.top,
          bottom: b.top + f.clientHeight * g
        };
      }
      let m = 0, y = 0;
      if (s == "nearest")
        t.top < u.top ? (y = t.top - (u.top + o), e > 0 && t.bottom > u.bottom + y && (y = t.bottom - u.bottom + o)) : t.bottom > u.bottom && (y = t.bottom - u.bottom + o, e < 0 && t.top - y < u.top && (y = t.top - (u.top + o)));
      else {
        let b = t.bottom - t.top, x = u.bottom - u.top;
        y = (s == "center" && b <= x ? t.top + b / 2 - x / 2 : s == "start" || s == "center" && e < 0 ? t.top - o : t.bottom - x + o) - u.top;
      }
      if (i == "nearest" ? t.left < u.left ? (m = t.left - (u.left + r), e > 0 && t.right > u.right + m && (m = t.right - u.right + r)) : t.right > u.right && (m = t.right - u.right + r, e < 0 && t.left < u.left + m && (m = t.left - (u.left + r))) : m = (i == "center" ? t.left + (t.right - t.left) / 2 - (u.right - u.left) / 2 : i == "start" == l ? t.left - r : t.right - (u.right - u.left) + r) - u.left, m || y)
        if (d)
          h.scrollBy(m, y);
        else {
          let b = 0, x = 0;
          if (y) {
            let O = f.scrollTop;
            f.scrollTop += y / g, x = (f.scrollTop - O) * g;
          }
          if (m) {
            let O = f.scrollLeft;
            f.scrollLeft += m / p, b = (f.scrollLeft - O) * p;
          }
          t = {
            left: t.left - b,
            top: t.top - x,
            right: t.right - b,
            bottom: t.bottom - x
          }, b && Math.abs(b - m) < 1 && (i = "nearest"), x && Math.abs(x - y) < 1 && (s = "nearest");
        }
      if (d)
        break;
      (t.top < u.top || t.bottom > u.bottom || t.left < u.left || t.right > u.right) && (t = {
        left: Math.max(t.left, u.left),
        right: Math.min(t.right, u.right),
        top: Math.max(t.top, u.top),
        bottom: Math.min(t.bottom, u.bottom)
      }), f = f.assignedSlot || f.parentNode;
    } else if (f.nodeType == 11)
      f = f.host;
    else
      break;
}
function jl(n, t = !0) {
  let e = n.ownerDocument, i = null, s = null;
  for (let r = n.parentNode; r && !(r == e.body || (!t || i) && s); )
    if (r.nodeType == 1)
      !s && r.scrollHeight > r.clientHeight && (s = r), t && !i && r.scrollWidth > r.clientWidth && (i = r), r = r.assignedSlot || r.parentNode;
    else if (r.nodeType == 11)
      r = r.host;
    else
      break;
  return { x: i, y: s };
}
class mf {
  constructor() {
    this.anchorNode = null, this.anchorOffset = 0, this.focusNode = null, this.focusOffset = 0;
  }
  eq(t) {
    return this.anchorNode == t.anchorNode && this.anchorOffset == t.anchorOffset && this.focusNode == t.focusNode && this.focusOffset == t.focusOffset;
  }
  setRange(t) {
    let { anchorNode: e, focusNode: i } = t;
    this.set(e, Math.min(t.anchorOffset, e ? Zt(e) : 0), i, Math.min(t.focusOffset, i ? Zt(i) : 0));
  }
  set(t, e, i, s) {
    this.anchorNode = t, this.anchorOffset = e, this.focusNode = i, this.focusOffset = s;
  }
}
let ye = null;
A.safari && A.safari_version >= 26 && (ye = !1);
function Xl(n) {
  if (n.setActive)
    return n.setActive();
  if (ye)
    return n.focus(ye);
  let t = [];
  for (let e = n; e && (t.push(e, e.scrollTop, e.scrollLeft), e != e.ownerDocument); e = e.parentNode)
    ;
  if (n.focus(ye == null ? {
    get preventScroll() {
      return ye = { preventScroll: !0 }, !0;
    }
  } : void 0), !ye) {
    ye = !1;
    for (let e = 0; e < t.length; ) {
      let i = t[e++], s = t[e++], r = t[e++];
      i.scrollTop != s && (i.scrollTop = s), i.scrollLeft != r && (i.scrollLeft = r);
    }
  }
}
let eo;
function gi(n, t, e = t) {
  let i = eo || (eo = document.createRange());
  return i.setEnd(n, e), i.setStart(n, t), i;
}
function Ne(n, t, e, i) {
  let s = { key: t, code: t, keyCode: e, which: e, cancelable: !0 };
  i && ({ altKey: s.altKey, ctrlKey: s.ctrlKey, shiftKey: s.shiftKey, metaKey: s.metaKey } = i);
  let r = new KeyboardEvent("keydown", s);
  r.synthetic = !0, n.dispatchEvent(r);
  let o = new KeyboardEvent("keyup", s);
  return o.synthetic = !0, n.dispatchEvent(o), r.defaultPrevented || o.defaultPrevented;
}
function yf(n) {
  for (; n; ) {
    if (n && (n.nodeType == 9 || n.nodeType == 11 && n.host))
      return n;
    n = n.assignedSlot || n.parentNode;
  }
  return null;
}
function bf(n, t) {
  let e = t.focusNode, i = t.focusOffset;
  if (!e || t.anchorNode != e || t.anchorOffset != i)
    return !1;
  for (i = Math.min(i, Zt(e)); ; )
    if (i) {
      if (e.nodeType != 1)
        return !1;
      let s = e.childNodes[i - 1];
      s.contentEditable == "false" ? i-- : (e = s, i = Zt(e));
    } else {
      if (e == n)
        return !0;
      i = fe(e), e = e.parentNode;
    }
}
function ql(n) {
  return n instanceof Window ? n.pageYOffset > Math.max(0, n.document.documentElement.scrollHeight - n.innerHeight - 4) : n.scrollTop > Math.max(1, n.scrollHeight - n.clientHeight - 4);
}
function Ul(n, t) {
  for (let e = n, i = t; ; ) {
    if (e.nodeType == 3 && i > 0)
      return { node: e, offset: i };
    if (e.nodeType == 1 && i > 0) {
      if (e.contentEditable == "false")
        return null;
      e = e.childNodes[i - 1], i = Zt(e);
    } else if (e.parentNode && !fs(e))
      i = fe(e), e = e.parentNode;
    else
      return null;
  }
}
function Kl(n, t) {
  for (let e = n, i = t; ; ) {
    if (e.nodeType == 3 && i < e.nodeValue.length)
      return { node: e, offset: i };
    if (e.nodeType == 1 && i < e.childNodes.length) {
      if (e.contentEditable == "false")
        return null;
      e = e.childNodes[i], i = 0;
    } else if (e.parentNode && !fs(e))
      i = fe(e) + 1, e = e.parentNode;
    else
      return null;
  }
}
class Pt {
  constructor(t, e, i = !0) {
    this.node = t, this.offset = e, this.precise = i;
  }
  static before(t, e) {
    return new Pt(t.parentNode, fe(t), e);
  }
  static after(t, e) {
    return new Pt(t.parentNode, fe(t) + 1, e);
  }
}
var $ = /* @__PURE__ */ (function(n) {
  return n[n.LTR = 0] = "LTR", n[n.RTL = 1] = "RTL", n;
})($ || ($ = {}));
const Ae = $.LTR, cr = $.RTL;
function _l(n) {
  let t = [];
  for (let e = 0; e < n.length; e++)
    t.push(1 << +n[e]);
  return t;
}
const xf = /* @__PURE__ */ _l("88888888888888888888888888888888888666888888787833333333337888888000000000000000000000000008888880000000000000000000000000088888888888888888888888888888888888887866668888088888663380888308888800000000000000000000000800000000000000000000000000000008"), wf = /* @__PURE__ */ _l("4444448826627288999999999992222222222222222222222222222222222222222222222229999999999999999999994444444444644222822222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222999999949999999229989999223333333333"), Nn = /* @__PURE__ */ Object.create(null), Nt = [];
for (let n of ["()", "[]", "{}"]) {
  let t = /* @__PURE__ */ n.charCodeAt(0), e = /* @__PURE__ */ n.charCodeAt(1);
  Nn[t] = e, Nn[e] = -t;
}
function Gl(n) {
  return n <= 247 ? xf[n] : 1424 <= n && n <= 1524 ? 2 : 1536 <= n && n <= 1785 ? wf[n - 1536] : 1774 <= n && n <= 2220 ? 4 : 8192 <= n && n <= 8204 ? 256 : 64336 <= n && n <= 65023 ? 4 : 1;
}
const kf = /[\u0590-\u05f4\u0600-\u06ff\u0700-\u08ac\ufb50-\ufdff]/;
class jt {
  /**
  The direction of this span.
  */
  get dir() {
    return this.level % 2 ? cr : Ae;
  }
  /**
  @internal
  */
  constructor(t, e, i) {
    this.from = t, this.to = e, this.level = i;
  }
  /**
  @internal
  */
  side(t, e) {
    return this.dir == e == t ? this.to : this.from;
  }
  /**
  @internal
  */
  forward(t, e) {
    return t == (this.dir == e);
  }
  /**
  @internal
  */
  static find(t, e, i, s) {
    let r = -1;
    for (let o = 0; o < t.length; o++) {
      let l = t[o];
      if (l.from <= e && l.to >= e) {
        if (l.level == i)
          return o;
        (r < 0 || (s != 0 ? s < 0 ? l.from < e : l.to > e : t[r].level > l.level)) && (r = o);
      }
    }
    if (r < 0)
      throw new RangeError("Index out of range");
    return r;
  }
}
function Yl(n, t) {
  if (n.length != t.length)
    return !1;
  for (let e = 0; e < n.length; e++) {
    let i = n[e], s = t[e];
    if (i.from != s.from || i.to != s.to || i.direction != s.direction || !Yl(i.inner, s.inner))
      return !1;
  }
  return !0;
}
const F = [];
function vf(n, t, e, i, s) {
  for (let r = 0; r <= i.length; r++) {
    let o = r ? i[r - 1].to : t, l = r < i.length ? i[r].from : e, a = r ? 256 : s;
    for (let h = o, f = a, c = a; h < l; h++) {
      let u = Gl(n.charCodeAt(h));
      u == 512 ? u = f : u == 8 && c == 4 && (u = 16), F[h] = u == 4 ? 2 : u, u & 7 && (c = u), f = u;
    }
    for (let h = o, f = a, c = a; h < l; h++) {
      let u = F[h];
      if (u == 128)
        h < l - 1 && f == F[h + 1] && f & 24 ? u = F[h] = f : F[h] = 256;
      else if (u == 64) {
        let d = h + 1;
        for (; d < l && F[d] == 64; )
          d++;
        let p = h && f == 8 || d < e && F[d] == 8 ? c == 1 ? 1 : 8 : 256;
        for (let g = h; g < d; g++)
          F[g] = p;
        h = d - 1;
      } else u == 8 && c == 1 && (F[h] = 1);
      f = u, u & 7 && (c = u);
    }
  }
}
function Sf(n, t, e, i, s) {
  let r = s == 1 ? 2 : 1;
  for (let o = 0, l = 0, a = 0; o <= i.length; o++) {
    let h = o ? i[o - 1].to : t, f = o < i.length ? i[o].from : e;
    for (let c = h, u, d, p; c < f; c++)
      if (d = Nn[u = n.charCodeAt(c)])
        if (d < 0) {
          for (let g = l - 3; g >= 0; g -= 3)
            if (Nt[g + 1] == -d) {
              let m = Nt[g + 2], y = m & 2 ? s : m & 4 ? m & 1 ? r : s : 0;
              y && (F[c] = F[Nt[g]] = y), l = g;
              break;
            }
        } else {
          if (Nt.length == 189)
            break;
          Nt[l++] = c, Nt[l++] = u, Nt[l++] = a;
        }
      else if ((p = F[c]) == 2 || p == 1) {
        let g = p == s;
        a = g ? 0 : 1;
        for (let m = l - 3; m >= 0; m -= 3) {
          let y = Nt[m + 2];
          if (y & 2)
            break;
          if (g)
            Nt[m + 2] |= 2;
          else {
            if (y & 4)
              break;
            Nt[m + 2] |= 4;
          }
        }
      }
  }
}
function Of(n, t, e, i) {
  for (let s = 0, r = i; s <= e.length; s++) {
    let o = s ? e[s - 1].to : n, l = s < e.length ? e[s].from : t;
    for (let a = o; a < l; ) {
      let h = F[a];
      if (h == 256) {
        let f = a + 1;
        for (; ; )
          if (f == l) {
            if (s == e.length)
              break;
            f = e[s++].to, l = s < e.length ? e[s].from : t;
          } else if (F[f] == 256)
            f++;
          else
            break;
        let c = r == 1, u = (f < t ? F[f] : i) == 1, d = c == u ? c ? 1 : 2 : i;
        for (let p = f, g = s, m = g ? e[g - 1].to : n; p > a; )
          p == m && (p = e[--g].from, m = g ? e[g - 1].to : n), F[--p] = d;
        a = f;
      } else
        r = h, a++;
    }
  }
}
function Vn(n, t, e, i, s, r, o) {
  let l = i % 2 ? 2 : 1;
  if (i % 2 == s % 2)
    for (let a = t, h = 0; a < e; ) {
      let f = !0, c = !1;
      if (h == r.length || a < r[h].from) {
        let g = F[a];
        g != l && (f = !1, c = g == 16);
      }
      let u = !f && l == 1 ? [] : null, d = f ? i : i + 1, p = a;
      t: for (; ; )
        if (h < r.length && p == r[h].from) {
          if (c)
            break t;
          let g = r[h];
          if (!f)
            for (let m = g.to, y = h + 1; ; ) {
              if (m == e)
                break t;
              if (y < r.length && r[y].from == m)
                m = r[y++].to;
              else {
                if (F[m] == l)
                  break t;
                break;
              }
            }
          if (h++, u)
            u.push(g);
          else {
            g.from > a && o.push(new jt(a, g.from, d));
            let m = g.direction == Ae != !(d % 2);
            Wn(n, m ? i + 1 : i, s, g.inner, g.from, g.to, o), a = g.to;
          }
          p = g.to;
        } else {
          if (p == e || (f ? F[p] != l : F[p] == l))
            break;
          p++;
        }
      u ? Vn(n, a, p, i + 1, s, u, o) : a < p && o.push(new jt(a, p, d)), a = p;
    }
  else
    for (let a = e, h = r.length; a > t; ) {
      let f = !0, c = !1;
      if (!h || a > r[h - 1].to) {
        let g = F[a - 1];
        g != l && (f = !1, c = g == 16);
      }
      let u = !f && l == 1 ? [] : null, d = f ? i : i + 1, p = a;
      t: for (; ; )
        if (h && p == r[h - 1].to) {
          if (c)
            break t;
          let g = r[--h];
          if (!f)
            for (let m = g.from, y = h; ; ) {
              if (m == t)
                break t;
              if (y && r[y - 1].to == m)
                m = r[--y].from;
              else {
                if (F[m - 1] == l)
                  break t;
                break;
              }
            }
          if (u)
            u.push(g);
          else {
            g.to < a && o.push(new jt(g.to, a, d));
            let m = g.direction == Ae != !(d % 2);
            Wn(n, m ? i + 1 : i, s, g.inner, g.from, g.to, o), a = g.from;
          }
          p = g.from;
        } else {
          if (p == t || (f ? F[p - 1] != l : F[p - 1] == l))
            break;
          p--;
        }
      u ? Vn(n, p, a, i + 1, s, u, o) : p < a && o.push(new jt(p, a, d)), a = p;
    }
}
function Wn(n, t, e, i, s, r, o) {
  let l = t % 2 ? 2 : 1;
  vf(n, s, r, i, l), Sf(n, s, r, i, l), Of(s, r, i, l), Vn(n, s, r, t, e, i, o);
}
function Cf(n, t, e) {
  if (!n)
    return [new jt(0, 0, t == cr ? 1 : 0)];
  if (t == Ae && !e.length && !kf.test(n))
    return Zl(n.length);
  if (e.length)
    for (; n.length > F.length; )
      F[F.length] = 256;
  let i = [], s = t == Ae ? 0 : 1;
  return Wn(n, s, s, e, 0, n.length, i), i;
}
function Zl(n) {
  return [new jt(0, n, 0)];
}
let Jl = "";
function Af(n, t, e, i, s) {
  var r;
  let o = i.head - n.from, l = jt.find(t, o, (r = i.bidiLevel) !== null && r !== void 0 ? r : -1, i.assoc), a = t[l], h = a.side(s, e);
  if (o == h) {
    let u = l += s ? 1 : -1;
    if (u < 0 || u >= t.length)
      return null;
    a = t[l = u], o = a.side(!s, e), h = a.side(s, e);
  }
  let f = st(n.text, o, a.forward(s, e));
  (f < a.from || f > a.to) && (f = h), Jl = n.text.slice(Math.min(o, f), Math.max(o, f));
  let c = l == (s ? t.length - 1 : 0) ? null : t[l + (s ? 1 : -1)];
  return c && f == h && c.level + (s ? 0 : 1) < a.level ? k.cursor(c.side(!s, e) + n.from, c.forward(s, e) ? 1 : -1, c.level) : k.cursor(f + n.from, a.forward(s, e) ? -1 : 1, a.level);
}
function Tf(n, t, e) {
  for (let i = t; i < e; i++) {
    let s = Gl(n.charCodeAt(i));
    if (s == 1)
      return Ae;
    if (s == 2 || s == 4)
      return cr;
  }
  return Ae;
}
const ta = /* @__PURE__ */ M.define(), ea = /* @__PURE__ */ M.define(), ia = /* @__PURE__ */ M.define(), sa = /* @__PURE__ */ M.define(), Hn = /* @__PURE__ */ M.define(), na = /* @__PURE__ */ M.define(), ra = /* @__PURE__ */ M.define(), ur = /* @__PURE__ */ M.define(), dr = /* @__PURE__ */ M.define(), oa = /* @__PURE__ */ M.define({
  combine: (n) => n.some((t) => t)
}), Mf = /* @__PURE__ */ M.define({
  combine: (n) => n.some((t) => t)
}), la = /* @__PURE__ */ M.define();
class Ve {
  constructor(t, e = "nearest", i = "nearest", s = 5, r = 5, o = !1) {
    this.range = t, this.y = e, this.x = i, this.yMargin = s, this.xMargin = r, this.isSnapshot = o;
  }
  map(t) {
    return t.empty ? this : new Ve(this.range.map(t), this.y, this.x, this.yMargin, this.xMargin, this.isSnapshot);
  }
  clip(t) {
    return this.range.to <= t.doc.length ? this : new Ve(k.cursor(t.doc.length), this.y, this.x, this.yMargin, this.xMargin, this.isSnapshot);
  }
}
const Li = /* @__PURE__ */ N.define({ map: (n, t) => n.map(t) }), aa = /* @__PURE__ */ N.define();
function ft(n, t, e) {
  let i = n.facet(sa);
  i.length ? i[0](t) : window.onerror && window.onerror(String(t), e, void 0, void 0, t) || (e ? console.error(e + ":", t) : console.error(t));
}
const Gt = /* @__PURE__ */ M.define({ combine: (n) => n.length ? n[0] : !0 });
let Pf = 0;
const De = /* @__PURE__ */ M.define({
  combine(n) {
    return n.filter((t, e) => {
      for (let i = 0; i < e; i++)
        if (n[i].plugin == t.plugin)
          return !1;
      return !0;
    });
  }
});
class dt {
  constructor(t, e, i, s, r) {
    this.id = t, this.create = e, this.domEventHandlers = i, this.domEventObservers = s, this.baseExtensions = r(this), this.extension = this.baseExtensions.concat(De.of({ plugin: this, arg: void 0 }));
  }
  /**
  Create an extension for this plugin with the given argument.
  */
  of(t) {
    return this.baseExtensions.concat(De.of({ plugin: this, arg: t }));
  }
  /**
  Define a plugin from a constructor function that creates the
  plugin's value, given an editor view.
  */
  static define(t, e) {
    const { eventHandlers: i, eventObservers: s, provide: r, decorations: o } = e || {};
    return new dt(Pf++, t, i, s, (l) => {
      let a = [];
      return o && a.push(Es.of((h) => {
        let f = h.plugin(l);
        return f ? o(f) : H.none;
      })), r && a.push(r(l)), a;
    });
  }
  /**
  Create a plugin for a class whose constructor takes a single
  editor view as argument.
  */
  static fromClass(t, e) {
    return dt.define((i, s) => new t(i, s), e);
  }
}
class _s {
  constructor(t) {
    this.spec = t, this.mustUpdate = null, this.value = null;
  }
  get plugin() {
    return this.spec && this.spec.plugin;
  }
  update(t) {
    if (this.value) {
      if (this.mustUpdate) {
        let e = this.mustUpdate;
        if (this.mustUpdate = null, this.value.update)
          try {
            this.value.update(e);
          } catch (i) {
            if (ft(e.state, i, "CodeMirror plugin crashed"), this.value.destroy)
              try {
                this.value.destroy();
              } catch {
              }
            this.deactivate();
          }
      }
    } else if (this.spec)
      try {
        this.value = this.spec.plugin.create(t, this.spec.arg);
      } catch (e) {
        ft(t.state, e, "CodeMirror plugin crashed"), this.deactivate();
      }
    return this;
  }
  destroy(t) {
    var e;
    if (!((e = this.value) === null || e === void 0) && e.destroy)
      try {
        this.value.destroy();
      } catch (i) {
        ft(t.state, i, "CodeMirror plugin crashed");
      }
  }
  deactivate() {
    this.spec = this.value = null;
  }
}
const ha = /* @__PURE__ */ M.define(), pr = /* @__PURE__ */ M.define(), Es = /* @__PURE__ */ M.define(), fa = /* @__PURE__ */ M.define(), gr = /* @__PURE__ */ M.define(), Ai = /* @__PURE__ */ M.define(), ca = /* @__PURE__ */ M.define();
function io(n, t) {
  let e = n.state.facet(ca);
  if (!e.length)
    return e;
  let i = e.map((r) => r instanceof Function ? r(n) : r), s = [];
  return L.spans(i, t.from, t.to, {
    point() {
    },
    span(r, o, l, a) {
      let h = r - t.from, f = o - t.from, c = s;
      for (let u = l.length - 1; u >= 0; u--, a--) {
        let d = l[u].spec.bidiIsolate, p;
        if (d == null && (d = Tf(t.text, h, f)), a > 0 && c.length && (p = c[c.length - 1]).to == h && p.direction == d)
          p.to = f, c = p.inner;
        else {
          let g = { from: h, to: f, direction: d, inner: [] };
          c.push(g), c = g.inner;
        }
      }
    }
  }), s;
}
const ua = /* @__PURE__ */ M.define();
function mr(n) {
  let t = 0, e = 0, i = 0, s = 0;
  for (let r of n.state.facet(ua)) {
    let o = r(n);
    o && (o.left != null && (t = Math.max(t, o.left)), o.right != null && (e = Math.max(e, o.right)), o.top != null && (i = Math.max(i, o.top)), o.bottom != null && (s = Math.max(s, o.bottom)));
  }
  return { left: t, right: e, top: i, bottom: s };
}
const ii = /* @__PURE__ */ M.define();
class vt {
  constructor(t, e, i, s) {
    this.fromA = t, this.toA = e, this.fromB = i, this.toB = s;
  }
  join(t) {
    return new vt(Math.min(this.fromA, t.fromA), Math.max(this.toA, t.toA), Math.min(this.fromB, t.fromB), Math.max(this.toB, t.toB));
  }
  addToSet(t) {
    let e = t.length, i = this;
    for (; e > 0; e--) {
      let s = t[e - 1];
      if (!(s.fromA > i.toA)) {
        if (s.toA < i.fromA)
          break;
        i = i.join(s), t.splice(e - 1, 1);
      }
    }
    return t.splice(e, 0, i), t;
  }
  // Extend a set to cover all the content in `ranges`, which is a
  // flat array with each pair of numbers representing fromB/toB
  // positions. These pairs are generated in unchanged ranges, so the
  // offset between doc A and doc B is the same for their start and
  // end points.
  static extendWithRanges(t, e) {
    if (e.length == 0)
      return t;
    let i = [];
    for (let s = 0, r = 0, o = 0; ; ) {
      let l = s < t.length ? t[s].fromB : 1e9, a = r < e.length ? e[r] : 1e9, h = Math.min(l, a);
      if (h == 1e9)
        break;
      let f = h + o, c = h, u = f;
      for (; ; )
        if (r < e.length && e[r] <= c) {
          let d = e[r + 1];
          r += 2, c = Math.max(c, d);
          for (let p = s; p < t.length && t[p].fromB <= c; p++)
            o = t[p].toA - t[p].toB;
          u = Math.max(u, d + o);
        } else if (s < t.length && t[s].fromB <= c) {
          let d = t[s++];
          c = Math.max(c, d.toB), u = Math.max(u, d.toA), o = d.toA - d.toB;
        } else
          break;
      i.push(new vt(f, u, h, c));
    }
    return i;
  }
}
class us {
  constructor(t, e, i) {
    this.view = t, this.state = e, this.transactions = i, this.flags = 0, this.startState = t.state, this.changes = G.empty(this.startState.doc.length);
    for (let r of i)
      this.changes = this.changes.compose(r.changes);
    let s = [];
    this.changes.iterChangedRanges((r, o, l, a) => s.push(new vt(r, o, l, a))), this.changedRanges = s;
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new us(t, e, i);
  }
  /**
  Tells you whether the [viewport](https://codemirror.net/6/docs/ref/#view.EditorView.viewport) or
  [visible ranges](https://codemirror.net/6/docs/ref/#view.EditorView.visibleRanges) changed in this
  update.
  */
  get viewportChanged() {
    return (this.flags & 4) > 0;
  }
  /**
  Returns true when
  [`viewportChanged`](https://codemirror.net/6/docs/ref/#view.ViewUpdate.viewportChanged) is true
  and the viewport change is not just the result of mapping it in
  response to document changes.
  */
  get viewportMoved() {
    return (this.flags & 8) > 0;
  }
  /**
  Indicates whether the height of a block element in the editor
  changed in this update.
  */
  get heightChanged() {
    return (this.flags & 2) > 0;
  }
  /**
  Returns true when the document was modified or the size of the
  editor, or elements within the editor, changed.
  */
  get geometryChanged() {
    return this.docChanged || (this.flags & 18) > 0;
  }
  /**
  True when this update indicates a focus change.
  */
  get focusChanged() {
    return (this.flags & 1) > 0;
  }
  /**
  Whether the document changed in this update.
  */
  get docChanged() {
    return !this.changes.empty;
  }
  /**
  Whether the selection was explicitly set in this update.
  */
  get selectionSet() {
    return this.transactions.some((t) => t.selection);
  }
  /**
  @internal
  */
  get empty() {
    return this.flags == 0 && this.transactions.length == 0;
  }
}
const Df = [];
class U {
  constructor(t, e, i = 0) {
    this.dom = t, this.length = e, this.flags = i, this.parent = null, t.cmTile = this;
  }
  get breakAfter() {
    return this.flags & 1;
  }
  get children() {
    return Df;
  }
  isWidget() {
    return !1;
  }
  get isHidden() {
    return !1;
  }
  isComposite() {
    return !1;
  }
  isLine() {
    return !1;
  }
  isText() {
    return !1;
  }
  isBlock() {
    return !1;
  }
  get domAttrs() {
    return null;
  }
  sync(t) {
    if (this.flags |= 2, this.flags & 4) {
      this.flags &= -5;
      let e = this.domAttrs;
      e && cf(this.dom, e);
    }
  }
  toString() {
    return this.constructor.name + (this.children.length ? `(${this.children})` : "") + (this.breakAfter ? "#" : "");
  }
  destroy() {
    this.parent = null;
  }
  setDOM(t) {
    this.dom = t, t.cmTile = this;
  }
  get posAtStart() {
    return this.parent ? this.parent.posBefore(this) : 0;
  }
  get posAtEnd() {
    return this.posAtStart + this.length;
  }
  posBefore(t, e = this.posAtStart) {
    let i = e;
    for (let s of this.children) {
      if (s == t)
        return i;
      i += s.length + s.breakAfter;
    }
    throw new RangeError("Invalid child in posBefore");
  }
  posAfter(t) {
    return this.posBefore(t) + t.length;
  }
  covers(t) {
    return !0;
  }
  coordsIn(t, e) {
    return null;
  }
  domPosFor(t, e) {
    let i = fe(this.dom), s = this.length ? t > 0 : e > 0;
    return new Pt(this.parent.dom, i + (s ? 1 : 0), t == 0 || t == this.length);
  }
  markDirty(t) {
    this.flags &= -3, t && (this.flags |= 4), this.parent && this.parent.flags & 2 && this.parent.markDirty(!1);
  }
  get overrideDOMText() {
    return null;
  }
  get root() {
    for (let t = this; t; t = t.parent)
      if (t instanceof Ns)
        return t;
    return null;
  }
  static get(t) {
    return t.cmTile;
  }
}
class Is extends U {
  constructor(t) {
    super(t, 0), this._children = [];
  }
  isComposite() {
    return !0;
  }
  get children() {
    return this._children;
  }
  get lastChild() {
    return this.children.length ? this.children[this.children.length - 1] : null;
  }
  append(t) {
    this.children.push(t), t.parent = this;
  }
  sync(t) {
    if (this.flags & 2)
      return;
    super.sync(t);
    let e = this.dom, i = null, s, r = (t == null ? void 0 : t.node) == e ? t : null, o = 0;
    for (let l of this.children) {
      if (l.sync(t), o += l.length + l.breakAfter, s = i ? i.nextSibling : e.firstChild, r && s != l.dom && (r.written = !0), l.dom.parentNode == e)
        for (; s && s != l.dom; )
          s = so(s);
      else
        e.insertBefore(l.dom, s);
      i = l.dom;
    }
    for (s = i ? i.nextSibling : e.firstChild, r && s && (r.written = !0); s; )
      s = so(s);
    this.length = o;
  }
}
function so(n) {
  let t = n.nextSibling;
  return n.parentNode.removeChild(n), t;
}
class Ns extends Is {
  constructor(t, e) {
    super(e), this.view = t;
  }
  owns(t) {
    for (; t; t = t.parent)
      if (t == this)
        return !0;
    return !1;
  }
  isBlock() {
    return !0;
  }
  nearest(t) {
    for (; ; ) {
      if (!t)
        return null;
      let e = U.get(t);
      if (e && this.owns(e))
        return e;
      t = t.parentNode;
    }
  }
  blockTiles(t) {
    for (let e = [], i = this, s = 0, r = 0; ; )
      if (s == i.children.length) {
        if (!e.length)
          return;
        i = i.parent, i.breakAfter && r++, s = e.pop();
      } else {
        let o = i.children[s++];
        if (o instanceof Yt)
          e.push(s), i = o, s = 0;
        else {
          let l = r + o.length, a = t(o, r);
          if (a !== void 0)
            return a;
          r = l + o.breakAfter;
        }
      }
  }
  // Find the block at the given position. If side < -1, make sure to
  // stay before block widgets at that position, if side > 1, after
  // such widgets (used for selection drawing, which needs to be able
  // to get coordinates for positions that aren't valid cursor positions).
  resolveBlock(t, e) {
    let i, s = -1, r, o = -1;
    if (this.blockTiles((l, a) => {
      let h = a + l.length;
      if (t >= a && t <= h) {
        if (l.isWidget() && e >= -1 && e <= 1) {
          if (l.flags & 32)
            return !0;
          l.flags & 16 && (i = void 0);
        }
        (a < t || t == h && (e < -1 ? l.length : l.covers(1))) && (!i || !l.isWidget() && i.isWidget()) && (i = l, s = t - a), (h > t || t == a && (e > 1 ? l.length : l.covers(-1))) && (!r || !l.isWidget() && r.isWidget()) && (r = l, o = t - a);
      }
    }), !i && !r)
      throw new Error("No tile at position " + t);
    return i && e < 0 || !r ? { tile: i, offset: s } : { tile: r, offset: o };
  }
}
class Yt extends Is {
  constructor(t, e) {
    super(t), this.wrapper = e;
  }
  isBlock() {
    return !0;
  }
  covers(t) {
    return this.children.length ? t < 0 ? this.children[0].covers(-1) : this.lastChild.covers(1) : !1;
  }
  get domAttrs() {
    return this.wrapper.attributes;
  }
  static of(t, e) {
    let i = new Yt(e || document.createElement(t.tagName), t);
    return e || (i.flags |= 4), i;
  }
}
class Qe extends Is {
  constructor(t, e) {
    super(t), this.attrs = e;
  }
  isLine() {
    return !0;
  }
  static start(t, e, i) {
    let s = new Qe(e || document.createElement("div"), t);
    return (!e || !i) && (s.flags |= 4), s;
  }
  get domAttrs() {
    return this.attrs;
  }
  // Find the tile associated with a given position in this line.
  resolveInline(t, e, i) {
    let s = null, r = -1, o = null, l = -1;
    function a(f, c) {
      for (let u = 0, d = 0; u < f.children.length && d <= c; u++) {
        let p = f.children[u], g = d + p.length;
        g >= c && (p.isComposite() ? a(p, c - d) : (!o || o.isHidden && (e > 0 || i && Rf(o, p))) && (g > c || p.flags & 32) ? (o = p, l = c - d) : (d < c || p.flags & 16 && !p.isHidden) && (s = p, r = c - d)), d = g;
      }
    }
    a(this, t);
    let h = (e < 0 ? s : o) || s || o;
    return h ? { tile: h, offset: h == s ? r : l } : null;
  }
  coordsIn(t, e) {
    let i = this.resolveInline(t, e, !0);
    return i ? i.tile.coordsIn(Math.max(0, i.offset), e) : Bf(this);
  }
  domIn(t, e) {
    let i = this.resolveInline(t, e);
    if (i) {
      let { tile: s, offset: r } = i;
      if (this.dom.contains(s.dom))
        return s.isText() ? new Pt(s.dom, Math.min(s.dom.nodeValue.length, r)) : s.domPosFor(r, s.flags & 16 ? 1 : s.flags & 32 ? -1 : e);
      let o = i.tile.parent, l = !1;
      for (let a of o.children) {
        if (l)
          return new Pt(a.dom, 0);
        a == i.tile && (l = !0);
      }
    }
    return new Pt(this.dom, 0);
  }
}
function Bf(n) {
  let t = n.dom.lastChild;
  if (!t)
    return n.dom.getBoundingClientRect();
  let e = es(t);
  return e[e.length - 1] || null;
}
function Rf(n, t) {
  let e = n.coordsIn(0, 1), i = t.coordsIn(0, 1);
  return e && i && i.top < e.bottom;
}
class ht extends Is {
  constructor(t, e) {
    super(t), this.mark = e;
  }
  get domAttrs() {
    return this.mark.attrs;
  }
  static of(t, e) {
    let i = new ht(e || document.createElement(t.tagName), t);
    return e || (i.flags |= 4), i;
  }
}
class ke extends U {
  constructor(t, e) {
    super(t, e.length), this.text = e;
  }
  sync(t) {
    this.flags & 2 || (super.sync(t), this.dom.nodeValue != this.text && (t && t.node == this.dom && (t.written = !0), this.dom.nodeValue = this.text));
  }
  isText() {
    return !0;
  }
  toString() {
    return JSON.stringify(this.text);
  }
  coordsIn(t, e) {
    let i = this.dom.nodeValue.length;
    t > i && (t = i);
    let s = t, r = t, o = 0;
    t == 0 && e < 0 || t == i && e >= 0 ? A.chrome || A.gecko || (t ? (s--, o = 1) : r < i && (r++, o = -1)) : e < 0 ? s-- : r < i && r++;
    let l = gi(this.dom, s, r).getClientRects();
    if (!l.length)
      return null;
    let a = l[(o ? o < 0 : e >= 0) ? 0 : l.length - 1];
    return A.safari && !o && a.width == 0 && (a = Array.prototype.find.call(l, (h) => h.width) || a), o ? cs(a, o < 0) : a || null;
  }
  static of(t, e) {
    let i = new ke(e || document.createTextNode(t), t);
    return e || (i.flags |= 2), i;
  }
}
class Te extends U {
  constructor(t, e, i, s) {
    super(t, e, s), this.widget = i;
  }
  isWidget() {
    return !0;
  }
  get isHidden() {
    return this.widget.isHidden;
  }
  covers(t) {
    return this.flags & 48 ? !1 : (this.flags & (t < 0 ? 64 : 128)) > 0;
  }
  coordsIn(t, e) {
    return this.coordsInWidget(t, e, !1);
  }
  coordsInWidget(t, e, i) {
    let s = this.widget.coordsAt(this.dom, t, e);
    if (s)
      return s;
    if (i)
      return cs(this.dom.getBoundingClientRect(), this.length ? t == 0 : e <= 0);
    {
      let r = this.dom.getClientRects(), o = null;
      if (!r.length)
        return null;
      let l = this.flags & 16 ? !0 : this.flags & 32 ? !1 : t > 0;
      for (let a = l ? r.length - 1 : 0; o = r[a], !(t > 0 ? a == 0 : a == r.length - 1 || o.top < o.bottom); a += l ? -1 : 1)
        ;
      return cs(o, !l);
    }
  }
  get overrideDOMText() {
    if (!this.length)
      return I.empty;
    let { root: t } = this;
    if (!t)
      return I.empty;
    let e = this.posAtStart;
    return t.view.state.doc.slice(e, e + this.length);
  }
  destroy() {
    super.destroy(), this.widget.destroy(this.dom);
  }
  static of(t, e, i, s, r) {
    return r || (r = t.toDOM(e), t.editable || (r.contentEditable = "false")), new Te(r, i, t, s);
  }
}
class ds extends U {
  constructor(t) {
    let e = document.createElement("img");
    e.className = "cm-widgetBuffer", e.setAttribute("aria-hidden", "true"), super(e, 0, t);
  }
  get isHidden() {
    return !0;
  }
  get overrideDOMText() {
    return I.empty;
  }
  coordsIn(t) {
    return this.dom.getBoundingClientRect();
  }
}
class Lf {
  constructor(t) {
    this.index = 0, this.beforeBreak = !1, this.parents = [], this.tile = t;
  }
  // Advance by the given distance. If side is -1, stop leaving or
  // entering tiles, or skipping zero-length tiles, once the distance
  // has been traversed. When side is 1, leave, enter, or skip
  // everything at the end position.
  advance(t, e, i) {
    let { tile: s, index: r, beforeBreak: o, parents: l } = this;
    for (; t || e > 0; )
      if (s.isComposite())
        if (o) {
          if (!t)
            break;
          i && i.break(), t--, o = !1;
        } else if (r == s.children.length) {
          if (!t && !l.length)
            break;
          i && i.leave(s), o = !!s.breakAfter, { tile: s, index: r } = l.pop(), r++;
        } else {
          let a = s.children[r], h = a.breakAfter;
          (e > 0 ? a.length <= t : a.length < t) && (!i || i.skip(a, 0, a.length) !== !1 || !a.isComposite) ? (o = !!h, r++, t -= a.length) : (l.push({ tile: s, index: r }), s = a, r = 0, i && a.isComposite() && i.enter(a));
        }
      else if (r == s.length)
        o = !!s.breakAfter, { tile: s, index: r } = l.pop(), r++;
      else if (t) {
        let a = Math.min(t, s.length - r);
        i && i.skip(s, r, r + a), t -= a, r += a;
      } else
        break;
    return this.tile = s, this.index = r, this.beforeBreak = o, this;
  }
  get root() {
    return this.parents.length ? this.parents[0].tile : this.tile;
  }
}
class Ef {
  constructor(t, e, i, s) {
    this.from = t, this.to = e, this.wrapper = i, this.rank = s;
  }
}
class If {
  constructor(t, e, i) {
    this.cache = t, this.root = e, this.blockWrappers = i, this.curLine = null, this.lastBlock = null, this.afterWidget = null, this.pos = 0, this.wrappers = [], this.wrapperPos = 0;
  }
  addText(t, e, i, s) {
    var r;
    this.flushBuffer();
    let o = this.ensureMarks(e, i), l = o.lastChild;
    if (l && l.isText() && !(l.flags & 8) && l.length + t.length < 512) {
      this.cache.reused.set(
        l,
        2
        /* Reused.DOM */
      );
      let a = o.children[o.children.length - 1] = new ke(l.dom, l.text + t);
      a.parent = o;
    } else
      o.append(s || ke.of(t, (r = this.cache.find(ke)) === null || r === void 0 ? void 0 : r.dom));
    this.pos += t.length, this.afterWidget = null;
  }
  addComposition(t, e) {
    let i = this.curLine;
    i.dom != e.line.dom && (i.setDOM(this.cache.reused.has(e.line) ? Gs(e.line.dom) : e.line.dom), this.cache.reused.set(
      e.line,
      2
      /* Reused.DOM */
    ));
    let s = i;
    for (let l = e.marks.length - 1; l >= 0; l--) {
      let a = e.marks[l], h = s.lastChild;
      if (h instanceof ht && h.mark.eq(a.mark))
        h.dom != a.dom && h.setDOM(Gs(a.dom)), s = h;
      else {
        if (this.cache.reused.get(a)) {
          let c = U.get(a.dom);
          c && c.setDOM(Gs(a.dom));
        }
        let f = ht.of(a.mark, a.dom);
        s.append(f), s = f;
      }
      this.cache.reused.set(
        a,
        2
        /* Reused.DOM */
      );
    }
    let r = U.get(t.text);
    r && this.cache.reused.set(
      r,
      2
      /* Reused.DOM */
    );
    let o = new ke(t.text, t.text.nodeValue);
    o.flags |= 8, s.append(o);
  }
  addInlineWidget(t, e, i) {
    let s = this.afterWidget && t.flags & 48 && (this.afterWidget.flags & 48) == (t.flags & 48);
    s || this.flushBuffer();
    let r = this.ensureMarks(e, i);
    !s && !(t.flags & 16) && r.append(this.getBuffer(1)), r.append(t), this.pos += t.length, this.afterWidget = t;
  }
  addMark(t, e, i) {
    this.flushBuffer(), this.ensureMarks(e, i).append(t), this.pos += t.length, this.afterWidget = null;
  }
  addBlockWidget(t) {
    this.getBlockPos().append(t), this.pos += t.length, this.lastBlock = t, this.endLine();
  }
  continueWidget(t) {
    let e = this.afterWidget || this.lastBlock;
    e.length += t, this.pos += t;
  }
  addLineStart(t, e) {
    var i;
    t || (t = da);
    let s = Qe.start(t, e || ((i = this.cache.find(Qe)) === null || i === void 0 ? void 0 : i.dom), !!e);
    this.getBlockPos().append(this.lastBlock = this.curLine = s);
  }
  addLine(t) {
    this.getBlockPos().append(t), this.pos += t.length, this.lastBlock = t, this.endLine();
  }
  addBreak() {
    this.lastBlock.flags |= 1, this.endLine(), this.pos++;
  }
  addLineStartIfNotCovered(t) {
    this.blockPosCovered() || this.addLineStart(t);
  }
  ensureLine(t) {
    this.curLine || this.addLineStart(t);
  }
  ensureMarks(t, e) {
    var i;
    let s = this.curLine;
    for (let r = t.length - 1; r >= 0; r--) {
      let o = t[r], l;
      if (e > 0 && (l = s.lastChild) && l instanceof ht && l.mark.eq(o))
        s = l, e--;
      else {
        let a = ht.of(o, (i = this.cache.find(ht, (h) => h.mark.eq(o))) === null || i === void 0 ? void 0 : i.dom);
        s.append(a), s = a, e = 0;
      }
    }
    return s;
  }
  endLine() {
    if (this.curLine) {
      this.flushBuffer();
      let t = this.curLine.lastChild;
      (!t || !no(this.curLine, !1) || t.dom.nodeName != "BR" && t.isWidget() && !(A.ios && no(this.curLine, !0))) && this.curLine.append(this.cache.findWidget(
        Ys,
        0,
        32
        /* TileFlag.After */
      ) || new Te(
        Ys.toDOM(),
        0,
        Ys,
        32
        /* TileFlag.After */
      )), this.curLine = this.afterWidget = null;
    }
  }
  updateBlockWrappers() {
    this.wrapperPos > this.pos + 1e4 && (this.blockWrappers.goto(this.pos), this.wrappers.length = 0);
    for (let t = this.wrappers.length - 1; t >= 0; t--)
      this.wrappers[t].to < this.pos && this.wrappers.splice(t, 1);
    for (let t = this.blockWrappers; t.value && t.from <= this.pos; t.next())
      if (t.to >= this.pos) {
        let e = new Ef(t.from, t.to, t.value, t.rank), i = this.wrappers.length;
        for (; i > 0 && (this.wrappers[i - 1].rank - e.rank || this.wrappers[i - 1].to - e.to) < 0; )
          i--;
        this.wrappers.splice(i, 0, e);
      }
    this.wrapperPos = this.pos;
  }
  getBlockPos() {
    var t;
    this.updateBlockWrappers();
    let e = this.root;
    for (let i of this.wrappers) {
      let s = e.lastChild;
      if (i.from < this.pos && s instanceof Yt && s.wrapper.eq(i.wrapper))
        e = s;
      else {
        let r = Yt.of(i.wrapper, (t = this.cache.find(Yt, (o) => o.wrapper.eq(i.wrapper))) === null || t === void 0 ? void 0 : t.dom);
        e.append(r), e = r;
      }
    }
    return e;
  }
  blockPosCovered() {
    let t = this.lastBlock;
    return t != null && !t.breakAfter && (!t.isWidget() || (t.flags & 160) > 0);
  }
  getBuffer(t) {
    let e = 2 | (t < 0 ? 16 : 32), i = this.cache.find(
      ds,
      void 0,
      1
      /* Reused.Full */
    );
    return i && (i.flags = e), i || new ds(e);
  }
  flushBuffer() {
    this.afterWidget && !(this.afterWidget.flags & 32) && (this.afterWidget.parent.append(this.getBuffer(-1)), this.afterWidget = null);
  }
}
class Nf {
  constructor(t) {
    this.skipCount = 0, this.text = "", this.textOff = 0, this.cursor = t.iter();
  }
  skip(t) {
    this.textOff + t <= this.text.length ? this.textOff += t : (this.skipCount += t - (this.text.length - this.textOff), this.text = "", this.textOff = 0);
  }
  next(t) {
    if (this.textOff == this.text.length) {
      let { value: s, lineBreak: r, done: o } = this.cursor.next(this.skipCount);
      if (this.skipCount = 0, o)
        throw new Error("Ran out of text content when drawing inline views");
      this.text = s;
      let l = this.textOff = Math.min(t, s.length);
      return r ? null : s.slice(0, l);
    }
    let e = Math.min(this.text.length, this.textOff + t), i = this.text.slice(this.textOff, e);
    return this.textOff = e, i;
  }
}
const ps = [Te, Qe, ke, ht, ds, Yt, Ns];
for (let n = 0; n < ps.length; n++)
  ps[n].bucket = n;
class Vf {
  constructor(t) {
    this.view = t, this.buckets = ps.map(() => []), this.index = ps.map(() => 0), this.reused = /* @__PURE__ */ new Map();
  }
  // Put a tile in the cache.
  add(t) {
    let e = t.constructor.bucket, i = this.buckets[e];
    i.length < 6 ? i.push(t) : i[
      this.index[e] = (this.index[e] + 1) % 6
      /* C.Bucket */
    ] = t;
  }
  find(t, e, i = 2) {
    let s = t.bucket, r = this.buckets[s], o = this.index[s];
    for (let l = r.length - 1; l >= 0; l--) {
      let a = (l + o) % r.length, h = r[a];
      if ((!e || e(h)) && !this.reused.has(h))
        return r.splice(a, 1), a < o && this.index[s]--, this.reused.set(h, i), h;
    }
    return null;
  }
  findWidget(t, e, i) {
    let s = this.buckets[0];
    if (s.length)
      for (let r = 0, o = 0; ; r++) {
        if (r == s.length) {
          if (o)
            return null;
          o = 1, r = 0;
        }
        let l = s[r];
        if (!this.reused.has(l) && (o == 0 ? l.widget.compare(t) : l.widget.constructor == t.constructor && t.updateDOM(l.dom, this.view)))
          return s.splice(r, 1), r < this.index[0] && this.index[0]--, l.widget == t && l.length == e && (l.flags & 497) == i ? (this.reused.set(
            l,
            1
            /* Reused.Full */
          ), l) : (this.reused.set(
            l,
            2
            /* Reused.DOM */
          ), new Te(l.dom, e, t, l.flags & -498 | i));
      }
  }
  reuse(t) {
    return this.reused.set(
      t,
      1
      /* Reused.Full */
    ), t;
  }
  maybeReuse(t, e = 2) {
    if (!this.reused.has(t))
      return this.reused.set(t, e), t.dom;
  }
  clear() {
    for (let t = 0; t < this.buckets.length; t++)
      this.buckets[t].length = this.index[t] = 0;
  }
}
class Wf {
  constructor(t, e, i, s, r) {
    this.view = t, this.decorations = s, this.disallowBlockEffectsFor = r, this.openWidget = !1, this.openMarks = 0, this.cache = new Vf(t), this.text = new Nf(t.state.doc), this.builder = new If(this.cache, new Ns(t, t.contentDOM), L.iter(i)), this.cache.reused.set(
      e,
      2
      /* Reused.DOM */
    ), this.old = new Lf(e), this.reuseWalker = {
      skip: (o, l, a) => {
        if (this.cache.add(o), o.isComposite())
          return !1;
      },
      enter: (o) => this.cache.add(o),
      leave: () => {
      },
      break: () => {
      }
    };
  }
  run(t, e) {
    let i = e && this.getCompositionContext(e.text);
    for (let s = 0, r = 0, o = 0; ; ) {
      let l = o < t.length ? t[o++] : null, a = l ? l.fromA : this.old.root.length;
      if (a > s) {
        let h = a - s;
        this.preserve(h, !o, !l), s = a, r += h;
      }
      if (!l)
        break;
      e && l.fromA <= e.range.fromA && l.toA >= e.range.toA ? (this.forward(l.fromA, e.range.fromA, e.range.fromA < e.range.toA ? 1 : -1), this.emit(r, e.range.fromB), this.cache.clear(), this.builder.addComposition(e, i), this.text.skip(e.range.toB - e.range.fromB), this.forward(e.range.fromA, l.toA), this.emit(e.range.toB, l.toB)) : (this.forward(l.fromA, l.toA), this.emit(r, l.toB)), r = l.toB, s = l.toA;
    }
    return this.builder.curLine && this.builder.endLine(), this.builder.root;
  }
  preserve(t, e, i) {
    let s = zf(this.old), r = this.openMarks;
    this.old.advance(t, i ? 1 : -1, {
      skip: (o, l, a) => {
        if (o.isWidget())
          if (this.openWidget)
            this.builder.continueWidget(a - l);
          else {
            let h = a > 0 || l < o.length ? Te.of(o.widget, this.view, a - l, o.flags & 496, this.cache.maybeReuse(o)) : this.cache.reuse(o);
            h.flags & 256 ? (h.flags &= -2, this.builder.addBlockWidget(h)) : (this.builder.ensureLine(null), this.builder.addInlineWidget(h, s, r), r = s.length);
          }
        else if (o.isText())
          this.builder.ensureLine(null), !l && a == o.length && !this.cache.reused.has(o) ? this.builder.addText(o.text, s, r, this.cache.reuse(o)) : (this.cache.add(o), this.builder.addText(o.text.slice(l, a), s, r)), r = s.length;
        else if (o.isLine())
          o.flags &= -2, this.cache.reused.set(
            o,
            1
            /* Reused.Full */
          ), this.builder.addLine(o);
        else if (o instanceof ds)
          this.cache.add(o);
        else if (o instanceof ht)
          this.builder.ensureLine(null), this.builder.addMark(o, s, r), this.cache.reused.set(
            o,
            1
            /* Reused.Full */
          ), r = s.length;
        else
          return !1;
        this.openWidget = !1;
      },
      enter: (o) => {
        o.isLine() ? this.builder.addLineStart(o.attrs, this.cache.maybeReuse(o)) : (this.cache.add(o), o instanceof ht && s.unshift(o.mark)), this.openWidget = !1;
      },
      leave: (o) => {
        o.isLine() ? s.length && (s.length = r = 0) : o instanceof ht && (s.shift(), r = Math.min(r, s.length));
      },
      break: () => {
        this.builder.addBreak(), this.openWidget = !1;
      }
    }), this.text.skip(t);
  }
  emit(t, e) {
    let i = null, s = this.builder, r = 0, o = L.spans(this.decorations, t, e, {
      point: (l, a, h, f, c, u) => {
        if (h instanceof Ce) {
          if (this.disallowBlockEffectsFor[u]) {
            if (h.block)
              throw new RangeError("Block decorations may not be specified via plugins");
            if (a > this.view.state.doc.lineAt(l).to)
              throw new RangeError("Decorations that replace line breaks may not be specified via plugins");
          }
          if (r = f.length, c > f.length)
            s.continueWidget(a - l);
          else {
            let d = h.widget || (h.block ? $e.block : $e.inline), p = Hf(h), g = this.cache.findWidget(d, a - l, p) || Te.of(d, this.view, a - l, p);
            h.block ? (h.startSide > 0 && s.addLineStartIfNotCovered(i), s.addBlockWidget(g)) : (s.ensureLine(i), s.addInlineWidget(g, f, c));
          }
          i = null;
        } else
          i = Ff(i, h);
        a > l && this.text.skip(a - l);
      },
      span: (l, a, h, f) => {
        for (let c = l; c < a; ) {
          let u = this.text.next(Math.min(512, a - c));
          u == null ? (s.addLineStartIfNotCovered(i), s.addBreak(), c++) : (s.ensureLine(i), s.addText(u, h, c == l ? f : h.length), c += u.length), i = null;
        }
      }
    });
    s.addLineStartIfNotCovered(i), this.openWidget = o > r, this.openMarks = o;
  }
  forward(t, e, i = 1) {
    e - t <= 10 ? this.old.advance(e - t, i, this.reuseWalker) : (this.old.advance(5, -1, this.reuseWalker), this.old.advance(e - t - 10, -1), this.old.advance(5, i, this.reuseWalker));
  }
  getCompositionContext(t) {
    let e = [], i = null;
    for (let s = t.parentNode; ; s = s.parentNode) {
      let r = U.get(s);
      if (s == this.view.contentDOM)
        break;
      r instanceof ht ? e.push(r) : r != null && r.isLine() ? i = r : r instanceof Yt || (s.nodeName == "DIV" && !i && s != this.view.contentDOM ? i = new Qe(s, da) : i || e.push(ht.of(new Oi({ tagName: s.nodeName.toLowerCase(), attributes: uf(s) }), s)));
    }
    return { line: i, marks: e };
  }
}
function no(n, t) {
  let e = (i) => {
    for (let s of i.children)
      if ((t ? s.isText() : s.length) || e(s))
        return !0;
    return !1;
  };
  return e(n);
}
function Hf(n) {
  let t = n.isReplace ? (n.startSide < 0 ? 64 : 0) | (n.endSide > 0 ? 128 : 0) : n.startSide > 0 ? 32 : 16;
  return n.block && (t |= 256), t;
}
const da = { class: "cm-line" };
function Ff(n, t) {
  let e = t.spec.attributes, i = t.spec.class;
  return !e && !i || (n || (n = { class: "cm-line" }), e && hr(e, n), i && (n.class += " " + i)), n;
}
function zf(n) {
  let t = [];
  for (let e = n.parents.length; e > 1; e--) {
    let i = e == n.parents.length ? n.tile : n.parents[e].tile;
    i instanceof ht && t.push(i.mark);
  }
  return t;
}
function Gs(n) {
  let t = U.get(n);
  return t && t.setDOM(n.cloneNode()), n;
}
class $e extends Si {
  constructor(t) {
    super(), this.tag = t;
  }
  eq(t) {
    return t.tag == this.tag;
  }
  toDOM() {
    return document.createElement(this.tag);
  }
  updateDOM(t) {
    return t.nodeName.toLowerCase() == this.tag;
  }
  get isHidden() {
    return !0;
  }
}
$e.inline = /* @__PURE__ */ new $e("span");
$e.block = /* @__PURE__ */ new $e("div");
const Ys = /* @__PURE__ */ new class extends Si {
  toDOM() {
    return document.createElement("br");
  }
  get isHidden() {
    return !0;
  }
  get editable() {
    return !0;
  }
}();
class ro {
  constructor(t) {
    this.view = t, this.decorations = [], this.blockWrappers = [], this.dynamicDecorationMap = [!1], this.domChanged = null, this.hasComposition = null, this.editContextFormatting = H.none, this.lastCompositionAfterCursor = !1, this.minWidth = 0, this.minWidthFrom = 0, this.minWidthTo = 0, this.impreciseAnchor = null, this.impreciseHead = null, this.forceSelection = !1, this.lastUpdate = Date.now(), this.updateDeco(), this.tile = new Ns(t, t.contentDOM), this.updateInner([new vt(0, 0, 0, t.state.doc.length)], null);
  }
  // Update the document view to a given state.
  update(t) {
    var e;
    let i = t.changedRanges;
    this.minWidth > 0 && i.length && (i.every(({ fromA: f, toA: c }) => c < this.minWidthFrom || f > this.minWidthTo) ? (this.minWidthFrom = t.changes.mapPos(this.minWidthFrom, 1), this.minWidthTo = t.changes.mapPos(this.minWidthTo, 1)) : this.minWidth = this.minWidthFrom = this.minWidthTo = 0), this.updateEditContextFormatting(t);
    let s = -1;
    this.view.inputState.composing >= 0 && !this.view.observer.editContext && (!((e = this.domChanged) === null || e === void 0) && e.newSel ? s = this.domChanged.newSel.head : !Gf(t.changes, this.hasComposition) && !t.selectionSet && (s = t.state.selection.main.head));
    let r = s > -1 ? $f(this.view, t.changes, s) : null;
    if (this.domChanged = null, this.hasComposition) {
      let { from: f, to: c } = this.hasComposition;
      i = new vt(f, c, t.changes.mapPos(f, -1), t.changes.mapPos(c, 1)).addToSet(i.slice());
    }
    this.hasComposition = r ? { from: r.range.fromB, to: r.range.toB } : null, (A.ie || A.chrome) && !r && t && t.state.doc.lines != t.startState.doc.lines && (this.forceSelection = !0);
    let o = this.decorations, l = this.blockWrappers;
    this.updateDeco();
    let a = qf(o, this.decorations, t.changes);
    a.length && (i = vt.extendWithRanges(i, a));
    let h = Kf(l, this.blockWrappers, t.changes);
    return h.length && (i = vt.extendWithRanges(i, h)), r && !i.some((f) => f.fromA <= r.range.fromA && f.toA >= r.range.toA) && (i = r.range.addToSet(i.slice())), this.tile.flags & 2 && i.length == 0 ? !1 : (this.updateInner(i, r), t.transactions.length && (this.lastUpdate = Date.now()), !0);
  }
  // Used by update and the constructor do perform the actual DOM
  // update
  updateInner(t, e) {
    this.view.viewState.mustMeasureContent = !0;
    let { observer: i } = this.view;
    i.ignore(() => {
      if (e || t.length) {
        let o = this.tile, l = new Wf(this.view, o, this.blockWrappers, this.decorations, this.dynamicDecorationMap);
        e && U.get(e.text) && l.cache.reused.set(
          U.get(e.text),
          2
          /* Reused.DOM */
        ), this.tile = l.run(t, e), Fn(o, l.cache.reused);
      }
      this.tile.dom.style.height = this.view.viewState.contentHeight / this.view.scaleY + "px", this.tile.dom.style.flexBasis = this.minWidth ? this.minWidth + "px" : "";
      let r = A.chrome || A.ios ? { node: i.selectionRange.focusNode, written: !1 } : void 0;
      this.tile.sync(r), r && (r.written || i.selectionRange.focusNode != r.node || !this.tile.dom.contains(r.node)) && (this.forceSelection = !0), this.tile.dom.style.height = "";
    });
    let s = [];
    if (this.view.viewport.from || this.view.viewport.to < this.view.state.doc.length)
      for (let r of this.tile.children)
        r.isWidget() && r.widget instanceof Zs && s.push(r.dom);
    i.updateGaps(s);
  }
  updateEditContextFormatting(t) {
    this.editContextFormatting = this.editContextFormatting.map(t.changes);
    for (let e of t.transactions)
      for (let i of e.effects)
        i.is(aa) && (this.editContextFormatting = i.value);
  }
  // Sync the DOM selection to this.state.selection
  updateSelection(t = !1, e = !1) {
    (t || !this.view.observer.selectionRange.focusNode) && this.view.observer.readSelectionRange();
    let { dom: i } = this.tile, s = this.view.root.activeElement, r = s == i, o = !r && !(this.view.state.facet(Gt) || i.tabIndex > -1) && oi(i, this.view.observer.selectionRange) && !(s && i.contains(s));
    if (!(r || e || o))
      return;
    let l = this.forceSelection;
    this.forceSelection = !1;
    let a = this.view.state.selection.main, h, f;
    if (a.empty ? f = h = this.inlineDOMNearPos(a.anchor, a.assoc || 1) : (f = this.inlineDOMNearPos(a.head, a.head == a.from ? 1 : -1), h = this.inlineDOMNearPos(a.anchor, a.anchor == a.from ? 1 : -1)), A.gecko && a.empty && !this.hasComposition && Qf(h)) {
      let u = document.createTextNode("");
      this.view.observer.ignore(() => h.node.insertBefore(u, h.node.childNodes[h.offset] || null)), h = f = new Pt(u, 0), l = !0;
    }
    let c = this.view.observer.selectionRange;
    (l || !c.focusNode || (!li(h.node, h.offset, c.anchorNode, c.anchorOffset) || !li(f.node, f.offset, c.focusNode, c.focusOffset)) && !this.suppressWidgetCursorChange(c, a)) && (this.view.observer.ignore(() => {
      A.android && A.chrome && i.contains(c.focusNode) && _f(c.focusNode, i) && (i.blur(), i.focus({ preventScroll: !0 }));
      let u = pi(this.view.root);
      if (u) if (a.empty) {
        if (A.gecko) {
          let d = jf(h.node, h.offset);
          if (d && d != 3) {
            let p = (d == 1 ? Ul : Kl)(h.node, h.offset);
            p && (h = new Pt(p.node, p.offset));
          }
        }
        u.collapse(h.node, h.offset), a.bidiLevel != null && u.caretBidiLevel !== void 0 && (u.caretBidiLevel = a.bidiLevel);
      } else if (u.extend) {
        u.collapse(h.node, h.offset);
        try {
          u.extend(f.node, f.offset);
        } catch {
        }
      } else {
        let d = document.createRange();
        a.anchor > a.head && ([h, f] = [f, h]), d.setEnd(f.node, f.offset), d.setStart(h.node, h.offset), u.removeAllRanges(), u.addRange(d);
      }
      o && this.view.root.activeElement == i && (i.blur(), s && s.focus());
    }), this.view.observer.setSelectionRange(h, f)), this.impreciseAnchor = h.precise ? null : new Pt(c.anchorNode, c.anchorOffset), this.impreciseHead = f.precise ? null : new Pt(c.focusNode, c.focusOffset);
  }
  // If a zero-length widget is inserted next to the cursor during
  // composition, avoid moving it across it and disrupting the
  // composition.
  suppressWidgetCursorChange(t, e) {
    return this.hasComposition && e.empty && li(t.focusNode, t.focusOffset, t.anchorNode, t.anchorOffset) && this.posFromDOM(t.focusNode, t.focusOffset) == e.head;
  }
  enforceCursorAssoc() {
    if (this.hasComposition)
      return;
    let { view: t } = this, e = t.state.selection.main, i = pi(t.root), { anchorNode: s, anchorOffset: r } = t.observer.selectionRange;
    if (!i || !e.empty || !e.assoc || !i.modify)
      return;
    let o = this.lineAt(e.head, e.assoc);
    if (!o)
      return;
    let l = o.posAtStart;
    if (e.head == l || e.head == l + o.length)
      return;
    let a = this.coordsAt(e.head, -1), h = this.coordsAt(e.head, 1);
    if (!a || !h || a.bottom > h.top)
      return;
    let f = this.domAtPos(e.head + e.assoc, e.assoc);
    i.collapse(f.node, f.offset), i.modify("move", e.assoc < 0 ? "forward" : "backward", "lineboundary"), t.observer.readSelectionRange();
    let c = t.observer.selectionRange;
    t.docView.posFromDOM(c.anchorNode, c.anchorOffset) != e.from && i.collapse(s, r);
  }
  posFromDOM(t, e) {
    let i = this.tile.nearest(t);
    if (!i)
      return this.tile.dom.compareDocumentPosition(t) & 2 ? 0 : this.view.state.doc.length;
    let s = i.posAtStart;
    if (i.isComposite()) {
      let r;
      if (t == i.dom)
        r = i.dom.childNodes[e];
      else {
        let o = Zt(t) == 0 ? 0 : e == 0 ? -1 : 1;
        for (; ; ) {
          let l = t.parentNode;
          if (l == i.dom)
            break;
          o == 0 && l.firstChild != l.lastChild && (t == l.firstChild ? o = -1 : o = 1), t = l;
        }
        o < 0 ? r = t : r = t.nextSibling;
      }
      if (r == i.dom.firstChild)
        return s;
      for (; r && !U.get(r); )
        r = r.nextSibling;
      if (!r)
        return s + i.length;
      for (let o = 0, l = s; ; o++) {
        let a = i.children[o];
        if (a.dom == r)
          return l;
        l += a.length + a.breakAfter;
      }
    } else return i.isText() ? t == i.dom ? s + e : s + (e ? i.length : 0) : s;
  }
  domAtPos(t, e) {
    let { tile: i, offset: s } = this.tile.resolveBlock(t, e);
    return i.isWidget() ? i.domPosFor(t, e) : i.domIn(s, e);
  }
  inlineDOMNearPos(t, e) {
    let i, s = -1, r = !1, o, l = -1, a = !1;
    return this.tile.blockTiles((h, f) => {
      if (h.isWidget()) {
        if (h.flags & 32 && f >= t)
          return !0;
        h.flags & 16 && (r = !0);
      } else {
        let c = f + h.length;
        if (f <= t && (i = h, s = t - f, r = c < t), c >= t && !o && (o = h, l = t - f, a = f > t), f > t && o)
          return !0;
      }
    }), !i && !o ? this.domAtPos(t, e) : (r && o ? i = null : a && i && (o = null), i && e < 0 || !o ? i.domIn(s, e) : o.domIn(l, e));
  }
  coordsAt(t, e) {
    let { tile: i, offset: s } = this.tile.resolveBlock(t, e);
    return i.isWidget() ? i.widget instanceof Zs ? null : i.coordsInWidget(s, e, !0) : i.coordsIn(s, e);
  }
  lineAt(t, e) {
    let { tile: i } = this.tile.resolveBlock(t, e);
    return i.isLine() ? i : null;
  }
  coordsForChar(t) {
    let { tile: e, offset: i } = this.tile.resolveBlock(t, 1);
    if (!e.isLine())
      return null;
    function s(r, o) {
      if (r.isComposite())
        for (let l of r.children) {
          if (l.length >= o) {
            let a = s(l, o);
            if (a)
              return a;
          }
          if (o -= l.length, o < 0)
            break;
        }
      else if (r.isText() && o < r.length) {
        let l = st(r.text, o);
        if (l == o)
          return null;
        let a = gi(r.dom, o, l).getClientRects();
        for (let h = 0; h < a.length; h++) {
          let f = a[h];
          if (h == a.length - 1 || f.top < f.bottom && f.left < f.right)
            return f;
        }
      }
      return null;
    }
    return s(e, i);
  }
  measureVisibleLineHeights(t) {
    let e = [], { from: i, to: s } = t, r = this.view.contentDOM.clientWidth, o = r > Math.max(this.view.scrollDOM.clientWidth, this.minWidth) + 1, l = -1, a = this.view.textDirection == $.LTR, h = 0, f = (c, u, d) => {
      for (let p = 0; p < c.children.length && !(u > s); p++) {
        let g = c.children[p], m = u + g.length, y = g.dom.getBoundingClientRect(), { height: b } = y;
        if (d && !p && (h += y.top - d.top), g instanceof Yt)
          m > i && f(g, u, y);
        else if (u >= i && (h > 0 && e.push(-h), e.push(b + h), h = 0, o)) {
          let x = g.dom.lastChild, O = x ? es(x) : [];
          if (O.length) {
            let S = O[O.length - 1], T = a ? S.right - y.left : y.right - S.left;
            T > l && (l = T, this.minWidth = r, this.minWidthFrom = u, this.minWidthTo = m);
          }
        }
        d && p == c.children.length - 1 && (h += d.bottom - y.bottom), u = m + g.breakAfter;
      }
    };
    return f(this.tile, 0, null), e;
  }
  textDirectionAt(t) {
    let { tile: e } = this.tile.resolveBlock(t, 1);
    return getComputedStyle(e.dom).direction == "rtl" ? $.RTL : $.LTR;
  }
  measureTextSize() {
    let t = this.tile.blockTiles((o) => {
      if (o.isLine() && o.children.length && o.length <= 20) {
        let l = 0, a;
        for (let h of o.children) {
          if (!h.isText() || /[^ -~]/.test(h.text))
            return;
          let f = es(h.dom);
          if (f.length != 1)
            return;
          l += f[0].width, a = f[0].height;
        }
        if (l)
          return {
            lineHeight: o.dom.getBoundingClientRect().height,
            charWidth: l / o.length,
            textHeight: a
          };
      }
    });
    if (t)
      return t;
    let e = document.createElement("div"), i, s, r;
    return e.className = "cm-line", e.style.width = "99999px", e.style.position = "absolute", e.textContent = "abc def ghi jkl mno pqr stu", this.view.observer.ignore(() => {
      this.tile.dom.appendChild(e);
      let o = es(e.firstChild)[0];
      i = e.getBoundingClientRect().height, s = o && o.width ? o.width / 27 : 7, r = o && o.height ? o.height : i, e.remove();
    }), { lineHeight: i, charWidth: s, textHeight: r };
  }
  computeBlockGapDeco() {
    let t = [], e = this.view.viewState;
    for (let i = 0, s = 0; ; s++) {
      let r = s == e.viewports.length ? null : e.viewports[s], o = r ? r.from - 1 : this.view.state.doc.length;
      if (o > i) {
        let l = (e.lineBlockAt(o).bottom - e.lineBlockAt(i).top) / this.view.scaleY;
        t.push(H.replace({
          widget: new Zs(l),
          block: !0,
          inclusive: !0,
          isBlockGap: !0
        }).range(i, o));
      }
      if (!r)
        break;
      i = r.to + 1;
    }
    return H.set(t);
  }
  updateDeco() {
    let t = 1, e = this.view.state.facet(Es).map((r) => (this.dynamicDecorationMap[t++] = typeof r == "function") ? r(this.view) : r), i = !1, s = this.view.state.facet(gr).map((r, o) => {
      let l = typeof r == "function";
      return l && (i = !0), l ? r(this.view) : r;
    });
    for (s.length && (this.dynamicDecorationMap[t++] = i, e.push(L.join(s))), this.decorations = [
      this.editContextFormatting,
      ...e,
      this.computeBlockGapDeco(),
      this.view.viewState.lineGapDeco
    ]; t < this.decorations.length; )
      this.dynamicDecorationMap[t++] = !1;
    this.blockWrappers = this.view.state.facet(fa).map((r) => typeof r == "function" ? r(this.view) : r);
  }
  scrollIntoView(t) {
    if (t.isSnapshot) {
      let h = this.view.viewState.lineBlockAt(t.range.head);
      this.view.scrollDOM.scrollTop = h.top - t.yMargin, this.view.scrollDOM.scrollLeft = t.xMargin;
      return;
    }
    for (let h of this.view.state.facet(la))
      try {
        if (h(this.view, t.range, t))
          return !0;
      } catch (f) {
        ft(this.view.state, f, "scroll handler");
      }
    let { range: e } = t, i = this.coordsAt(e.head, e.empty ? e.assoc : e.head > e.anchor ? -1 : 1), s;
    if (!i)
      return;
    !e.empty && (s = this.coordsAt(e.anchor, e.anchor > e.head ? -1 : 1)) && (i = {
      left: Math.min(i.left, s.left),
      top: Math.min(i.top, s.top),
      right: Math.max(i.right, s.right),
      bottom: Math.max(i.bottom, s.bottom)
    });
    let r = mr(this.view), o = {
      left: i.left - r.left,
      top: i.top - r.top,
      right: i.right + r.right,
      bottom: i.bottom + r.bottom
    }, { offsetWidth: l, offsetHeight: a } = this.view.scrollDOM;
    if (gf(this.view.scrollDOM, o, e.head < e.anchor ? -1 : 1, t.x, t.y, Math.max(Math.min(t.xMargin, l), -l), Math.max(Math.min(t.yMargin, a), -a), this.view.textDirection == $.LTR), window.visualViewport && window.innerHeight - window.visualViewport.height > 1 && (i.top > window.pageYOffset + window.visualViewport.offsetTop + window.visualViewport.height || i.bottom < window.pageYOffset + window.visualViewport.offsetTop)) {
      let h = this.view.docView.lineAt(e.head, 1);
      h && h.dom.scrollIntoView({ block: "nearest" });
    }
  }
  lineHasWidget(t) {
    let e = (i) => i.isWidget() || i.children.some(e);
    return e(this.tile.resolveBlock(t, 1).tile);
  }
  destroy() {
    Fn(this.tile);
  }
}
function Fn(n, t) {
  let e = t == null ? void 0 : t.get(n);
  if (e != 1) {
    e == null && n.destroy();
    for (let i of n.children)
      Fn(i, t);
  }
}
function Qf(n) {
  return n.node.nodeType == 1 && n.node.firstChild && (n.offset == 0 || n.node.childNodes[n.offset - 1].contentEditable == "false") && (n.offset == n.node.childNodes.length || n.node.childNodes[n.offset].contentEditable == "false");
}
function pa(n, t) {
  let e = n.observer.selectionRange;
  if (!e.focusNode)
    return null;
  let i = Ul(e.focusNode, e.focusOffset), s = Kl(e.focusNode, e.focusOffset), r = i || s;
  if (s && i && s.node != i.node) {
    let l = U.get(s.node);
    if (!l || l.isText() && l.text != s.node.nodeValue)
      r = s;
    else if (n.docView.lastCompositionAfterCursor) {
      let a = U.get(i.node);
      !a || a.isText() && a.text != i.node.nodeValue || (r = s);
    }
  }
  if (n.docView.lastCompositionAfterCursor = r != i, !r)
    return null;
  let o = t - r.offset;
  return { from: o, to: o + r.node.nodeValue.length, node: r.node };
}
function $f(n, t, e) {
  let i = pa(n, e);
  if (!i)
    return null;
  let { node: s, from: r, to: o } = i, l = s.nodeValue;
  if (/[\n\r]/.test(l) || n.state.doc.sliceString(i.from, i.to) != l)
    return null;
  let a = t.invertedDesc;
  return { range: new vt(a.mapPos(r), a.mapPos(o), r, o), text: s };
}
function jf(n, t) {
  return n.nodeType != 1 ? 0 : (t && n.childNodes[t - 1].contentEditable == "false" ? 1 : 0) | (t < n.childNodes.length && n.childNodes[t].contentEditable == "false" ? 2 : 0);
}
let Xf = class {
  constructor() {
    this.changes = [];
  }
  compareRange(t, e) {
    Ie(t, e, this.changes);
  }
  comparePoint(t, e) {
    Ie(t, e, this.changes);
  }
  boundChange(t) {
    Ie(t, t, this.changes);
  }
};
function qf(n, t, e) {
  let i = new Xf();
  return L.compare(n, t, e, i), i.changes;
}
class Uf {
  constructor() {
    this.changes = [];
  }
  compareRange(t, e) {
    Ie(t, e, this.changes);
  }
  comparePoint() {
  }
  boundChange(t) {
    Ie(t, t, this.changes);
  }
}
function Kf(n, t, e) {
  let i = new Uf();
  return L.compare(n, t, e, i), i.changes;
}
function _f(n, t) {
  for (let e = n; e && e != t; e = e.assignedSlot || e.parentNode)
    if (e.nodeType == 1 && e.contentEditable == "false")
      return !0;
  return !1;
}
function Gf(n, t) {
  let e = !1;
  return t && n.iterChangedRanges((i, s) => {
    i < t.to && s > t.from && (e = !0);
  }), e;
}
class Zs extends Si {
  constructor(t) {
    super(), this.height = t;
  }
  toDOM() {
    let t = document.createElement("div");
    return t.className = "cm-gap", this.updateDOM(t), t;
  }
  eq(t) {
    return t.height == this.height;
  }
  updateDOM(t) {
    return t.style.height = this.height + "px", !0;
  }
  get editable() {
    return !0;
  }
  get estimatedHeight() {
    return this.height;
  }
  ignoreEvent() {
    return !1;
  }
}
function Yf(n, t, e = 1) {
  let i = n.charCategorizer(t), s = n.doc.lineAt(t), r = t - s.from;
  if (s.length == 0)
    return k.cursor(t);
  r == 0 ? e = 1 : r == s.length && (e = -1);
  let o = r, l = r;
  e < 0 ? o = st(s.text, r, !1) : l = st(s.text, r);
  let a = i(s.text.slice(o, l));
  for (; o > 0; ) {
    let h = st(s.text, o, !1);
    if (i(s.text.slice(h, o)) != a)
      break;
    o = h;
  }
  for (; l < s.length; ) {
    let h = st(s.text, l);
    if (i(s.text.slice(l, h)) != a)
      break;
    l = h;
  }
  return k.range(o + s.from, l + s.from);
}
function Zf(n, t, e, i, s) {
  let r = Math.round((i - t.left) * n.defaultCharacterWidth);
  if (n.lineWrapping && e.height > n.defaultLineHeight * 1.5) {
    let l = n.viewState.heightOracle.textHeight, a = Math.floor((s - e.top - (n.defaultLineHeight - l) * 0.5) / l);
    r += a * n.viewState.heightOracle.lineLength;
  }
  let o = n.state.sliceDoc(e.from, e.to);
  return e.from + rf(o, r, n.state.tabSize);
}
function Jf(n, t, e) {
  let i = n.lineBlockAt(t);
  if (Array.isArray(i.type)) {
    let s;
    for (let r of i.type) {
      if (r.from > t)
        break;
      if (!(r.to < t)) {
        if (r.from < t && r.to > t)
          return r;
        (!s || r.type == ut.Text && (s.type != r.type || (e < 0 ? r.from < t : r.to > t))) && (s = r);
      }
    }
    return s || i;
  }
  return i;
}
function tc(n, t, e, i) {
  let s = Jf(n, t.head, t.assoc || -1), r = !i || s.type != ut.Text || !(n.lineWrapping || s.widgetLineBreaks) ? null : n.coordsAtPos(t.assoc < 0 && t.head > s.from ? t.head - 1 : t.head);
  if (r) {
    let o = n.dom.getBoundingClientRect(), l = n.textDirectionAt(s.from), a = n.posAtCoords({
      x: e == (l == $.LTR) ? o.right - 1 : o.left + 1,
      y: (r.top + r.bottom) / 2
    });
    if (a != null)
      return k.cursor(a, e ? -1 : 1);
  }
  return k.cursor(e ? s.to : s.from, e ? -1 : 1);
}
function oo(n, t, e, i) {
  let s = n.state.doc.lineAt(t.head), r = n.bidiSpans(s), o = n.textDirectionAt(s.from);
  for (let l = t, a = null; ; ) {
    let h = Af(s, r, o, l, e), f = Jl;
    if (!h) {
      if (s.number == (e ? n.state.doc.lines : 1))
        return l;
      f = `
`, s = n.state.doc.line(s.number + (e ? 1 : -1)), r = n.bidiSpans(s), h = n.visualLineSide(s, !e);
    }
    if (a) {
      if (!a(f))
        return l;
    } else {
      if (!i)
        return h;
      a = i(f);
    }
    l = h;
  }
}
function ec(n, t, e) {
  let i = n.state.charCategorizer(t), s = i(e);
  return (r) => {
    let o = i(r);
    return s == kt.Space && (s = o), s == o;
  };
}
function ic(n, t, e, i) {
  let s = t.head, r = e ? 1 : -1;
  if (s == (e ? n.state.doc.length : 0))
    return k.cursor(s, t.assoc);
  let o = t.goalColumn, l, a = n.contentDOM.getBoundingClientRect(), h = n.coordsAtPos(s, (t.empty ? t.assoc : 0) || (e ? 1 : -1)), f = n.documentTop;
  if (h)
    o == null && (o = h.left - a.left), l = r < 0 ? h.top : h.bottom;
  else {
    let p = n.viewState.lineBlockAt(s);
    o == null && (o = Math.min(a.right - a.left, n.defaultCharacterWidth * (s - p.from))), l = (r < 0 ? p.top : p.bottom) + f;
  }
  let c = a.left + o, u = i ?? n.viewState.heightOracle.textHeight >> 1, d = zn(n, { x: c, y: l + u * r }, !1, r);
  return k.cursor(d.pos, d.assoc, void 0, o);
}
function ai(n, t, e) {
  for (; ; ) {
    let i = 0;
    for (let s of n)
      s.between(t - 1, t + 1, (r, o, l) => {
        if (t > r && t < o) {
          let a = i || e || (t - r < o - t ? -1 : 1);
          t = a < 0 ? r : o, i = a;
        }
      });
    if (!i)
      return t;
  }
}
function ga(n, t) {
  let e = null;
  for (let i = 0; i < t.ranges.length; i++) {
    let s = t.ranges[i], r = null;
    if (s.empty) {
      let o = ai(n, s.from, 0);
      o != s.from && (r = k.cursor(o, -1));
    } else {
      let o = ai(n, s.from, -1), l = ai(n, s.to, 1);
      (o != s.from || l != s.to) && (r = k.range(s.from == s.anchor ? o : l, s.from == s.head ? o : l));
    }
    r && (e || (e = t.ranges.slice()), e[i] = r);
  }
  return e ? k.create(e, t.mainIndex) : t;
}
function Js(n, t, e) {
  let i = ai(n.state.facet(Ai).map((s) => s(n)), e.from, t.head > e.from ? -1 : 1);
  return i == e.from ? e : k.cursor(i, i < e.from ? 1 : -1);
}
class $t {
  constructor(t, e) {
    this.pos = t, this.assoc = e;
  }
}
function zn(n, t, e, i) {
  let s = n.contentDOM.getBoundingClientRect(), r = s.top + n.viewState.paddingTop, { x: o, y: l } = t, a = l - r, h;
  for (; ; ) {
    if (a < 0)
      return new $t(0, 1);
    if (a > n.viewState.docHeight)
      return new $t(n.state.doc.length, -1);
    if (h = n.elementAtHeight(a), i == null)
      break;
    if (h.type == ut.Text) {
      if (i < 0 ? h.to < n.viewport.from : h.from > n.viewport.to)
        break;
      let u = n.docView.coordsAt(i < 0 ? h.from : h.to, i > 0 ? -1 : 1);
      if (u && (i < 0 ? u.top <= a + r : u.bottom >= a + r))
        break;
    }
    let c = n.viewState.heightOracle.textHeight / 2;
    a = i > 0 ? h.bottom + c : h.top - c;
  }
  if (n.viewport.from >= h.to || n.viewport.to <= h.from) {
    if (e)
      return null;
    if (h.type == ut.Text) {
      let c = Zf(n, s, h, o, l);
      return new $t(c, c == h.from ? 1 : -1);
    }
  }
  if (h.type != ut.Text)
    return a < (h.top + h.bottom) / 2 ? new $t(h.from, 1) : new $t(h.to, -1);
  let f = n.docView.lineAt(h.from, 2);
  return (!f || f.length != h.length) && (f = n.docView.lineAt(h.from, -2)), new sc(n, o, l, n.textDirectionAt(h.from)).scanTile(f, h.from);
}
class sc {
  constructor(t, e, i, s) {
    this.view = t, this.x = e, this.y = i, this.baseDir = s, this.line = null, this.spans = null;
  }
  bidiSpansAt(t) {
    return (!this.line || this.line.from > t || this.line.to < t) && (this.line = this.view.state.doc.lineAt(t), this.spans = this.view.bidiSpans(this.line)), this;
  }
  baseDirAt(t, e) {
    let { line: i, spans: s } = this.bidiSpansAt(t);
    return s[jt.find(s, t - i.from, -1, e)].level == this.baseDir;
  }
  dirAt(t, e) {
    let { line: i, spans: s } = this.bidiSpansAt(t);
    return s[jt.find(s, t - i.from, -1, e)].dir;
  }
  // Used to short-circuit bidi tests for content with a uniform direction
  bidiIn(t, e) {
    let { spans: i, line: s } = this.bidiSpansAt(t);
    return i.length > 1 || i.length && (i[0].level != this.baseDir || i[0].to + s.from < e);
  }
  // Scan through the rectangles for the content of a tile with inline
  // content, looking for one that overlaps the queried position
  // vertically andis
  // closest horizontally. The caller is responsible for dividing its
  // content into N pieces, and pass an array with N+1 positions
  // (including the position after the last piece). For a text tile,
  // these will be character clusters, for a composite tile, these
  // will be child tiles.
  scan(t, e) {
    let i = 0, s = t.length - 1, r = /* @__PURE__ */ new Set(), o = this.bidiIn(t[0], t[s]), l, a, h = -1, f = 1e9, c;
    t: for (; i < s; ) {
      let d = s - i, p = i + s >> 1;
      e: if (r.has(p)) {
        let m = i + Math.floor(Math.random() * d);
        for (let y = 0; y < d; y++) {
          if (!r.has(m)) {
            p = m;
            break e;
          }
          m++, m == s && (m = i);
        }
        break t;
      }
      r.add(p);
      let g = e(p);
      if (g)
        for (let m = 0; m < g.length; m++) {
          let y = g[m], b = 0;
          if (!(y.width == 0 && g.length > 1)) {
            if (y.bottom < this.y)
              (!l || l.bottom < y.bottom) && (l = y), b = 1;
            else if (y.top > this.y)
              (!a || a.top > y.top) && (a = y), b = -1;
            else {
              let x = y.left > this.x ? this.x - y.left : y.right < this.x ? this.x - y.right : 0, O = Math.abs(x);
              O < f && (h = p, f = O, c = y), x && (b = x < 0 == (this.baseDir == $.LTR) ? -1 : 1);
            }
            b == -1 && (!o || this.baseDirAt(t[p], 1)) ? s = p : b == 1 && (!o || this.baseDirAt(t[p + 1], -1)) && (i = p + 1);
          }
        }
    }
    if (!c) {
      let d = l && (!a || this.y - l.bottom < a.top - this.y) ? l : a;
      return this.y = (d.top + d.bottom) / 2, this.scan(t, e);
    }
    let u = (o ? this.dirAt(t[h], 1) : this.baseDir) == $.LTR;
    return {
      i: h,
      // Test whether x is closes to the start or end of this element
      after: this.x > (c.left + c.right) / 2 == u
    };
  }
  scanText(t, e) {
    let i = [];
    for (let r = 0; r < t.length; r = st(t.text, r))
      i.push(e + r);
    i.push(e + t.length);
    let s = this.scan(i, (r) => {
      let o = i[r] - e, l = i[r + 1] - e;
      return gi(t.dom, o, l).getClientRects();
    });
    return s.after ? new $t(i[s.i + 1], -1) : new $t(i[s.i], 1);
  }
  scanTile(t, e) {
    if (!t.length)
      return new $t(e, 1);
    if (t.children.length == 1) {
      let l = t.children[0];
      if (l.isText())
        return this.scanText(l, e);
      if (l.isComposite())
        return this.scanTile(l, e);
    }
    let i = [e];
    for (let l = 0, a = e; l < t.children.length; l++)
      i.push(a += t.children[l].length);
    let s = this.scan(i, (l) => {
      let a = t.children[l];
      return a.flags & 48 ? null : (a.dom.nodeType == 1 ? a.dom : gi(a.dom, 0, a.length)).getClientRects();
    }), r = t.children[s.i], o = i[s.i];
    return r.isText() ? this.scanText(r, o) : r.isComposite() ? this.scanTile(r, o) : s.after ? new $t(i[s.i + 1], -1) : new $t(o, 1);
  }
}
const Pe = "￿";
class nc {
  constructor(t, e) {
    this.points = t, this.view = e, this.text = "", this.lineSeparator = e.state.facet(V.lineSeparator);
  }
  append(t) {
    this.text += t;
  }
  lineBreak() {
    this.text += Pe;
  }
  readRange(t, e) {
    if (!t)
      return this;
    let i = t.parentNode;
    for (let s = t; ; ) {
      this.findPointBefore(i, s);
      let r = this.text.length;
      this.readNode(s);
      let o = U.get(s), l = s.nextSibling;
      if (l == e) {
        o != null && o.breakAfter && !l && i != this.view.contentDOM && this.lineBreak();
        break;
      }
      let a = U.get(l);
      (o && a ? o.breakAfter : (o ? o.breakAfter : fs(s)) || fs(l) && (s.nodeName != "BR" || o != null && o.isWidget()) && this.text.length > r) && !oc(l, e) && this.lineBreak(), s = l;
    }
    return this.findPointBefore(i, e), this;
  }
  readTextNode(t) {
    let e = t.nodeValue;
    for (let i of this.points)
      i.node == t && (i.pos = this.text.length + Math.min(i.offset, e.length));
    for (let i = 0, s = this.lineSeparator ? null : /\r\n?|\n/g; ; ) {
      let r = -1, o = 1, l;
      if (this.lineSeparator ? (r = e.indexOf(this.lineSeparator, i), o = this.lineSeparator.length) : (l = s.exec(e)) && (r = l.index, o = l[0].length), this.append(e.slice(i, r < 0 ? e.length : r)), r < 0)
        break;
      if (this.lineBreak(), o > 1)
        for (let a of this.points)
          a.node == t && a.pos > this.text.length && (a.pos -= o - 1);
      i = r + o;
    }
  }
  readNode(t) {
    let e = U.get(t), i = e && e.overrideDOMText;
    if (i != null) {
      this.findPointInside(t, i.length);
      for (let s = i.iter(); !s.next().done; )
        s.lineBreak ? this.lineBreak() : this.append(s.value);
    } else t.nodeType == 3 ? this.readTextNode(t) : t.nodeName == "BR" ? t.nextSibling && this.lineBreak() : t.nodeType == 1 && this.readRange(t.firstChild, null);
  }
  findPointBefore(t, e) {
    for (let i of this.points)
      i.node == t && t.childNodes[i.offset] == e && (i.pos = this.text.length);
  }
  findPointInside(t, e) {
    for (let i of this.points)
      (t.nodeType == 3 ? i.node == t : t.contains(i.node)) && (i.pos = this.text.length + (rc(t, i.node, i.offset) ? e : 0));
  }
}
function rc(n, t, e) {
  for (; ; ) {
    if (!t || e < Zt(t))
      return !1;
    if (t == n)
      return !0;
    e = fe(t) + 1, t = t.parentNode;
  }
}
function oc(n, t) {
  let e;
  for (; !(n == t || !n); n = n.nextSibling) {
    let i = U.get(n);
    if (!(i != null && i.isWidget()))
      return !1;
    i && (e || (e = [])).push(i);
  }
  if (e)
    for (let i of e) {
      let s = i.overrideDOMText;
      if (s != null && s.length)
        return !1;
    }
  return !0;
}
class lo {
  constructor(t, e) {
    this.node = t, this.offset = e, this.pos = -1;
  }
}
class lc {
  constructor(t, e, i, s) {
    this.typeOver = s, this.bounds = null, this.text = "", this.domChanged = e > -1;
    let { impreciseHead: r, impreciseAnchor: o } = t.docView, l = t.state.selection;
    if (t.state.readOnly && e > -1)
      this.newSel = null;
    else if (e > -1 && (this.bounds = ma(t.docView.tile, e, i, 0))) {
      let a = r || o ? [] : hc(t), h = new nc(a, t);
      h.readRange(this.bounds.startDOM, this.bounds.endDOM), this.text = h.text, this.newSel = fc(a, this.bounds.from);
    } else {
      let a = t.observer.selectionRange, h = r && r.node == a.focusNode && r.offset == a.focusOffset || !In(t.contentDOM, a.focusNode) ? l.main.head : t.docView.posFromDOM(a.focusNode, a.focusOffset), f = o && o.node == a.anchorNode && o.offset == a.anchorOffset || !In(t.contentDOM, a.anchorNode) ? l.main.anchor : t.docView.posFromDOM(a.anchorNode, a.anchorOffset), c = t.viewport;
      if ((A.ios || A.chrome) && l.main.empty && h != f && (c.from > 0 || c.to < t.state.doc.length)) {
        let u = Math.min(h, f), d = Math.max(h, f), p = c.from - u, g = c.to - d;
        (p == 0 || p == 1 || u == 0) && (g == 0 || g == -1 || d == t.state.doc.length) && (h = 0, f = t.state.doc.length);
      }
      if (t.inputState.composing > -1 && l.ranges.length > 1)
        this.newSel = l.replaceRange(k.range(f, h));
      else if (t.lineWrapping && f == h && !(l.main.empty && l.main.head == h) && t.inputState.lastTouchTime > Date.now() - 100) {
        let u = t.coordsAtPos(h, -1), d = 0;
        u && (d = t.inputState.lastTouchY <= u.bottom ? -1 : 1), this.newSel = k.create([k.cursor(h, d)]);
      } else
        this.newSel = k.single(f, h);
    }
  }
}
function ma(n, t, e, i) {
  if (n.isComposite()) {
    let s = -1, r = -1, o = -1, l = -1;
    for (let a = 0, h = i, f = i; a < n.children.length; a++) {
      let c = n.children[a], u = h + c.length;
      if (h < t && u > e)
        return ma(c, t, e, h);
      if (u >= t && s == -1 && (s = a, r = h), h > e && c.dom.parentNode == n.dom) {
        o = a, l = f;
        break;
      }
      f = u, h = u + c.breakAfter;
    }
    return {
      from: r,
      to: l < 0 ? i + n.length : l,
      startDOM: (s ? n.children[s - 1].dom.nextSibling : null) || n.dom.firstChild,
      endDOM: o < n.children.length && o >= 0 ? n.children[o].dom : null
    };
  } else return n.isText() ? { from: i, to: i + n.length, startDOM: n.dom, endDOM: n.dom.nextSibling } : null;
}
function ya(n, t) {
  let e, { newSel: i } = t, { state: s } = n, r = s.selection.main, o = n.inputState.lastKeyTime > Date.now() - 100 ? n.inputState.lastKeyCode : -1;
  if (t.bounds) {
    let { from: l, to: a } = t.bounds, h = r.from, f = null;
    (o === 8 || A.android && t.text.length < a - l) && (h = r.to, f = "end");
    let c = s.doc.sliceString(l, a, Pe), u, d;
    !r.empty && r.from >= l && r.to <= a && (t.typeOver || c != t.text) && c.slice(0, r.from - l) == t.text.slice(0, r.from - l) && c.slice(r.to - l) == t.text.slice(u = t.text.length - (c.length - (r.to - l))) ? e = {
      from: r.from,
      to: r.to,
      insert: I.of(t.text.slice(r.from - l, u).split(Pe))
    } : (d = ba(c, t.text, h - l, f)) && (A.chrome && o == 13 && d.toB == d.from + 2 && t.text.slice(d.from, d.toB) == Pe + Pe && d.toB--, e = {
      from: l + d.from,
      to: l + d.toA,
      insert: I.of(t.text.slice(d.from, d.toB).split(Pe))
    });
  } else i && (!n.hasFocus && s.facet(Gt) || gs(i, r)) && (i = null);
  if (!e && !i)
    return !1;
  if ((A.mac || A.android) && e && e.from == e.to && e.from == r.head - 1 && /^\. ?$/.test(e.insert.toString()) && n.contentDOM.getAttribute("autocorrect") == "off" ? (i && e.insert.length == 2 && (i = k.single(i.main.anchor - 1, i.main.head - 1)), e = { from: e.from, to: e.to, insert: I.of([e.insert.toString().replace(".", " ")]) }) : s.doc.lineAt(r.from).to < r.to && n.docView.lineHasWidget(r.to) && n.inputState.insertingTextAt > Date.now() - 50 ? e = {
    from: r.from,
    to: r.to,
    insert: s.toText(n.inputState.insertingText)
  } : A.chrome && e && e.from == e.to && e.from == r.head && e.insert.toString() == `
 ` && n.lineWrapping && (i && (i = k.single(i.main.anchor - 1, i.main.head - 1)), e = { from: r.from, to: r.to, insert: I.of([" "]) }), e)
    return yr(n, e, i, o);
  if (i && !gs(i, r)) {
    let l = !1, a = "select";
    return n.inputState.lastSelectionTime > Date.now() - 50 && (n.inputState.lastSelectionOrigin == "select" && (l = !0), a = n.inputState.lastSelectionOrigin, a == "select.pointer" && (i = ga(s.facet(Ai).map((h) => h(n)), i))), n.dispatch({ selection: i, scrollIntoView: l, userEvent: a }), !0;
  } else
    return !1;
}
function yr(n, t, e, i = -1) {
  if (A.ios && n.inputState.flushIOSKey(t))
    return !0;
  let s = n.state.selection.main;
  if (A.android && (t.to == s.to && // GBoard will sometimes remove a space it just inserted
  // after a completion when you press enter
  (t.from == s.from || t.from == s.from - 1 && n.state.sliceDoc(t.from, s.from) == " ") && t.insert.length == 1 && t.insert.lines == 2 && Ne(n.contentDOM, "Enter", 13) || (t.from == s.from - 1 && t.to == s.to && t.insert.length == 0 || i == 8 && t.insert.length < t.to - t.from && t.to > s.head) && Ne(n.contentDOM, "Backspace", 8) || t.from == s.from && t.to == s.to + 1 && t.insert.length == 0 && Ne(n.contentDOM, "Delete", 46)))
    return !0;
  let r = t.insert.toString();
  n.inputState.composing >= 0 && n.inputState.composing++;
  let o, l = () => o || (o = ac(n, t, e));
  return n.state.facet(na).some((a) => a(n, t.from, t.to, r, l)) || n.dispatch(l()), !0;
}
function ac(n, t, e) {
  let i, s = n.state, r = s.selection.main, o = -1;
  if (t.from == t.to && t.from < r.from || t.from > r.to) {
    let a = t.from < r.from ? -1 : 1, h = a < 0 ? r.from : r.to, f = ai(s.facet(Ai).map((c) => c(n)), h, a);
    t.from == f && (o = f);
  }
  if (o > -1)
    i = {
      changes: t,
      selection: k.cursor(t.from + t.insert.length, -1)
    };
  else if (t.from >= r.from && t.to <= r.to && t.to - t.from >= (r.to - r.from) / 3 && (!e || e.main.empty && e.main.from == t.from + t.insert.length) && n.inputState.composing < 0) {
    let a = r.from < t.from ? s.sliceDoc(r.from, t.from) : "", h = r.to > t.to ? s.sliceDoc(t.to, r.to) : "";
    i = s.replaceSelection(n.state.toText(a + t.insert.sliceString(0, void 0, n.state.lineBreak) + h));
  } else {
    let a = s.changes(t), h = e && e.main.to <= a.newLength ? e.main : void 0;
    if (s.selection.ranges.length > 1 && (n.inputState.composing >= 0 || n.inputState.compositionPendingChange) && t.to <= r.to + 10 && t.to >= r.to - 10) {
      let f = n.state.sliceDoc(t.from, t.to), c, u = e && pa(n, e.main.head);
      if (u) {
        let p = t.insert.length - (t.to - t.from);
        c = { from: u.from, to: u.to - p };
      } else
        c = n.state.doc.lineAt(r.head);
      let d = r.to - t.to;
      i = s.changeByRange((p) => {
        if (p.from == r.from && p.to == r.to)
          return { changes: a, range: h || p.map(a) };
        let g = p.to - d, m = g - f.length;
        if (n.state.sliceDoc(m, g) != f || // Unfortunately, there's no way to make multiple
        // changes in the same node work without aborting
        // composition, so cursors in the composition range are
        // ignored.
        g >= c.from && m <= c.to)
          return { range: p };
        let y = s.changes({ from: m, to: g, insert: t.insert }), b = p.to - r.to;
        return {
          changes: y,
          range: h ? k.range(Math.max(0, h.anchor + b), Math.max(0, h.head + b)) : p.map(y)
        };
      });
    } else
      i = {
        changes: a,
        selection: h && s.selection.replaceRange(h)
      };
  }
  let l = "input.type";
  return (n.composing || n.inputState.compositionPendingChange && n.inputState.compositionEndedAt > Date.now() - 50) && (n.inputState.compositionPendingChange = !1, l += ".compose", n.inputState.compositionFirstChange && (l += ".start", n.inputState.compositionFirstChange = !1)), s.update(i, { userEvent: l, scrollIntoView: !0 });
}
function ba(n, t, e, i) {
  let s = Math.min(n.length, t.length), r = 0;
  for (; r < s && n.charCodeAt(r) == t.charCodeAt(r); )
    r++;
  if (r == s && n.length == t.length)
    return null;
  let o = n.length, l = t.length;
  for (; o > 0 && l > 0 && n.charCodeAt(o - 1) == t.charCodeAt(l - 1); )
    o--, l--;
  if (i == "end") {
    let a = Math.max(0, r - Math.min(o, l));
    e -= o + a - r;
  }
  if (o < r && n.length < t.length) {
    let a = e <= r && e >= o ? r - e : 0;
    r -= a, l = r + (l - o), o = r;
  } else if (l < r) {
    let a = e <= r && e >= l ? r - e : 0;
    r -= a, o = r + (o - l), l = r;
  }
  return { from: r, toA: o, toB: l };
}
function hc(n) {
  let t = [];
  if (n.root.activeElement != n.contentDOM)
    return t;
  let { anchorNode: e, anchorOffset: i, focusNode: s, focusOffset: r } = n.observer.selectionRange;
  return e && (t.push(new lo(e, i)), (s != e || r != i) && t.push(new lo(s, r))), t;
}
function fc(n, t) {
  if (n.length == 0)
    return null;
  let e = n[0].pos, i = n.length == 2 ? n[1].pos : e;
  return e > -1 && i > -1 ? k.single(e + t, i + t) : null;
}
function gs(n, t) {
  return t.head == n.main.head && t.anchor == n.main.anchor;
}
class cc {
  setSelectionOrigin(t) {
    this.lastSelectionOrigin = t, this.lastSelectionTime = Date.now();
  }
  constructor(t) {
    this.view = t, this.lastKeyCode = 0, this.lastKeyTime = 0, this.lastTouchTime = 0, this.lastTouchX = 0, this.lastTouchY = 0, this.lastFocusTime = 0, this.lastScrollTop = 0, this.lastScrollLeft = 0, this.lastWheelEvent = 0, this.pendingIOSKey = void 0, this.tabFocusMode = -1, this.lastSelectionOrigin = null, this.lastSelectionTime = 0, this.lastContextMenu = 0, this.scrollHandlers = [], this.handlers = /* @__PURE__ */ Object.create(null), this.composing = -1, this.compositionFirstChange = null, this.compositionEndedAt = 0, this.compositionPendingKey = !1, this.compositionPendingChange = !1, this.insertingText = "", this.insertingTextAt = 0, this.mouseSelection = null, this.draggedContent = null, this.handleEvent = this.handleEvent.bind(this), this.notifiedFocused = t.hasFocus, A.safari && t.contentDOM.addEventListener("input", () => null), A.gecko && Ac(t.contentDOM.ownerDocument);
  }
  handleEvent(t) {
    !xc(this.view, t) || this.ignoreDuringComposition(t) || t.type == "keydown" && this.keydown(t) || (this.view.updateState != 0 ? Promise.resolve().then(() => this.runHandlers(t.type, t)) : this.runHandlers(t.type, t));
  }
  runHandlers(t, e) {
    let i = this.handlers[t];
    if (i) {
      for (let s of i.observers)
        s(this.view, e);
      for (let s of i.handlers) {
        if (e.defaultPrevented)
          break;
        if (s(this.view, e)) {
          e.preventDefault();
          break;
        }
      }
    }
  }
  ensureHandlers(t) {
    let e = uc(t), i = this.handlers, s = this.view.contentDOM;
    for (let r in e)
      if (r != "scroll") {
        let o = !e[r].handlers.length, l = i[r];
        l && o != !l.handlers.length && (s.removeEventListener(r, this.handleEvent), l = null), l || s.addEventListener(r, this.handleEvent, { passive: o });
      }
    for (let r in i)
      r != "scroll" && !e[r] && s.removeEventListener(r, this.handleEvent);
    this.handlers = e;
  }
  keydown(t) {
    if (this.lastKeyCode = t.keyCode, this.lastKeyTime = Date.now(), t.keyCode == 9 && this.tabFocusMode > -1 && (!this.tabFocusMode || Date.now() <= this.tabFocusMode))
      return !0;
    if (this.tabFocusMode > 0 && t.keyCode != 27 && wa.indexOf(t.keyCode) < 0 && (this.tabFocusMode = -1), A.android && A.chrome && !t.synthetic && (t.keyCode == 13 || t.keyCode == 8))
      return this.view.observer.delayAndroidKey(t.key, t.keyCode), !0;
    let e;
    return A.ios && !t.synthetic && !t.altKey && !t.metaKey && ((e = xa.find((i) => i.keyCode == t.keyCode)) && !t.ctrlKey || dc.indexOf(t.key) > -1 && t.ctrlKey && !t.shiftKey) ? (this.pendingIOSKey = e || t, setTimeout(() => this.flushIOSKey(), 250), !0) : (t.keyCode != 229 && this.view.observer.forceFlush(), !1);
  }
  flushIOSKey(t) {
    let e = this.pendingIOSKey;
    return !e || e.key == "Enter" && t && t.from < t.to && /^\S+$/.test(t.insert.toString()) ? !1 : (this.pendingIOSKey = void 0, Ne(this.view.contentDOM, e.key, e.keyCode, e instanceof KeyboardEvent ? e : void 0));
  }
  ignoreDuringComposition(t) {
    return !/^key/.test(t.type) || t.synthetic ? !1 : this.composing > 0 ? !0 : A.safari && !A.ios && this.compositionPendingKey && Date.now() - this.compositionEndedAt < 100 ? (this.compositionPendingKey = !1, !0) : !1;
  }
  startMouseSelection(t) {
    this.mouseSelection && this.mouseSelection.destroy(), this.mouseSelection = t;
  }
  update(t) {
    this.view.observer.update(t), this.mouseSelection && this.mouseSelection.update(t), this.draggedContent && t.docChanged && (this.draggedContent = this.draggedContent.map(t.changes)), t.transactions.length && (this.lastKeyCode = this.lastSelectionTime = 0);
  }
  destroy() {
    this.mouseSelection && this.mouseSelection.destroy();
  }
}
function ao(n, t) {
  return (e, i) => {
    try {
      return t.call(n, i, e);
    } catch (s) {
      ft(e.state, s);
    }
  };
}
function uc(n) {
  let t = /* @__PURE__ */ Object.create(null);
  function e(i) {
    return t[i] || (t[i] = { observers: [], handlers: [] });
  }
  for (let i of n) {
    let s = i.spec, r = s && s.plugin.domEventHandlers, o = s && s.plugin.domEventObservers;
    if (r)
      for (let l in r) {
        let a = r[l];
        a && e(l).handlers.push(ao(i.value, a));
      }
    if (o)
      for (let l in o) {
        let a = o[l];
        a && e(l).observers.push(ao(i.value, a));
      }
  }
  for (let i in Rt)
    e(i).handlers.push(Rt[i]);
  for (let i in pt)
    e(i).observers.push(pt[i]);
  return t;
}
const xa = [
  { key: "Backspace", keyCode: 8, inputType: "deleteContentBackward" },
  { key: "Enter", keyCode: 13, inputType: "insertParagraph" },
  { key: "Enter", keyCode: 13, inputType: "insertLineBreak" },
  { key: "Delete", keyCode: 46, inputType: "deleteContentForward" }
], dc = "dthko", wa = [16, 17, 18, 20, 91, 92, 224, 225], Ei = 6;
function Ii(n) {
  return Math.max(0, n) * 0.7 + 8;
}
function pc(n, t) {
  return Math.max(Math.abs(n.clientX - t.clientX), Math.abs(n.clientY - t.clientY));
}
class gc {
  constructor(t, e, i, s) {
    this.view = t, this.startEvent = e, this.style = i, this.mustSelect = s, this.scrollSpeed = { x: 0, y: 0 }, this.scrolling = -1, this.lastEvent = e, this.scrollParents = jl(t.contentDOM), this.atoms = t.state.facet(Ai).map((o) => o(t));
    let r = t.contentDOM.ownerDocument;
    r.addEventListener("mousemove", this.move = this.move.bind(this)), r.addEventListener("mouseup", this.up = this.up.bind(this)), this.extend = e.shiftKey, this.multiple = t.state.facet(V.allowMultipleSelections) && mc(t, e), this.dragging = bc(t, e) && Sa(e) == 1 ? null : !1;
  }
  start(t) {
    this.dragging === !1 && this.select(t);
  }
  move(t) {
    if (t.buttons == 0)
      return this.destroy();
    if (this.dragging || this.dragging == null && pc(this.startEvent, t) < 10)
      return;
    this.select(this.lastEvent = t);
    let e = 0, i = 0, s = 0, r = 0, o = this.view.win.innerWidth, l = this.view.win.innerHeight;
    this.scrollParents.x && ({ left: s, right: o } = this.scrollParents.x.getBoundingClientRect()), this.scrollParents.y && ({ top: r, bottom: l } = this.scrollParents.y.getBoundingClientRect());
    let a = mr(this.view);
    t.clientX - a.left <= s + Ei ? e = -Ii(s - t.clientX) : t.clientX + a.right >= o - Ei && (e = Ii(t.clientX - o)), t.clientY - a.top <= r + Ei ? i = -Ii(r - t.clientY) : t.clientY + a.bottom >= l - Ei && (i = Ii(t.clientY - l)), this.setScrollSpeed(e, i);
  }
  up(t) {
    this.dragging == null && this.select(this.lastEvent), this.dragging || t.preventDefault(), this.destroy();
  }
  destroy() {
    this.setScrollSpeed(0, 0);
    let t = this.view.contentDOM.ownerDocument;
    t.removeEventListener("mousemove", this.move), t.removeEventListener("mouseup", this.up), this.view.inputState.mouseSelection = this.view.inputState.draggedContent = null;
  }
  setScrollSpeed(t, e) {
    this.scrollSpeed = { x: t, y: e }, t || e ? this.scrolling < 0 && (this.scrolling = setInterval(() => this.scroll(), 50)) : this.scrolling > -1 && (clearInterval(this.scrolling), this.scrolling = -1);
  }
  scroll() {
    let { x: t, y: e } = this.scrollSpeed;
    t && this.scrollParents.x && (this.scrollParents.x.scrollLeft += t, t = 0), e && this.scrollParents.y && (this.scrollParents.y.scrollTop += e, e = 0), (t || e) && this.view.win.scrollBy(t, e), this.dragging === !1 && this.select(this.lastEvent);
  }
  select(t) {
    let { view: e } = this, i = ga(this.atoms, this.style.get(t, this.extend, this.multiple));
    (this.mustSelect || !i.eq(e.state.selection, this.dragging === !1)) && this.view.dispatch({
      selection: i,
      userEvent: "select.pointer"
    }), this.mustSelect = !1;
  }
  update(t) {
    t.transactions.some((e) => e.isUserEvent("input.type")) ? this.destroy() : this.style.update(t) && setTimeout(() => this.select(this.lastEvent), 20);
  }
}
function mc(n, t) {
  let e = n.state.facet(ta);
  return e.length ? e[0](t) : A.mac ? t.metaKey : t.ctrlKey;
}
function yc(n, t) {
  let e = n.state.facet(ea);
  return e.length ? e[0](t) : A.mac ? !t.altKey : !t.ctrlKey;
}
function bc(n, t) {
  let { main: e } = n.state.selection;
  if (e.empty)
    return !1;
  let i = pi(n.root);
  if (!i || i.rangeCount == 0)
    return !0;
  let s = i.getRangeAt(0).getClientRects();
  for (let r = 0; r < s.length; r++) {
    let o = s[r];
    if (o.left <= t.clientX && o.right >= t.clientX && o.top <= t.clientY && o.bottom >= t.clientY)
      return !0;
  }
  return !1;
}
function xc(n, t) {
  if (!t.bubbles)
    return !0;
  if (t.defaultPrevented)
    return !1;
  for (let e = t.target, i; e != n.contentDOM; e = e.parentNode)
    if (!e || e.nodeType == 11 || (i = U.get(e)) && i.isWidget() && !i.isHidden && i.widget.ignoreEvent(t))
      return !1;
  return !0;
}
const Rt = /* @__PURE__ */ Object.create(null), pt = /* @__PURE__ */ Object.create(null), ka = A.ie && A.ie_version < 15 || A.ios && A.webkit_version < 604;
function wc(n) {
  let t = n.dom.parentNode;
  if (!t)
    return;
  let e = t.appendChild(document.createElement("textarea"));
  e.style.cssText = "position: fixed; left: -10000px; top: 10px", e.focus(), setTimeout(() => {
    n.focus(), e.remove(), va(n, e.value);
  }, 50);
}
function Vs(n, t, e) {
  for (let i of n.facet(t))
    e = i(e, n);
  return e;
}
function va(n, t) {
  t = Vs(n.state, ur, t);
  let { state: e } = n, i, s = 1, r = e.toText(t), o = r.lines == e.selection.ranges.length;
  if (Qn != null && e.selection.ranges.every((a) => a.empty) && Qn == r.toString()) {
    let a = -1;
    i = e.changeByRange((h) => {
      let f = e.doc.lineAt(h.from);
      if (f.from == a)
        return { range: h };
      a = f.from;
      let c = e.toText((o ? r.line(s++).text : t) + e.lineBreak);
      return {
        changes: { from: f.from, insert: c },
        range: k.cursor(h.from + c.length)
      };
    });
  } else o ? i = e.changeByRange((a) => {
    let h = r.line(s++);
    return {
      changes: { from: a.from, to: a.to, insert: h.text },
      range: k.cursor(a.from + h.length)
    };
  }) : i = e.replaceSelection(r);
  n.dispatch(i, {
    userEvent: "input.paste",
    scrollIntoView: !0
  });
}
pt.scroll = (n) => {
  n.inputState.lastScrollTop = n.scrollDOM.scrollTop, n.inputState.lastScrollLeft = n.scrollDOM.scrollLeft;
};
pt.wheel = pt.mousewheel = (n) => {
  n.inputState.lastWheelEvent = Date.now();
};
Rt.keydown = (n, t) => (n.inputState.setSelectionOrigin("select"), t.keyCode == 27 && n.inputState.tabFocusMode != 0 && (n.inputState.tabFocusMode = Date.now() + 2e3), !1);
pt.touchstart = (n, t) => {
  let e = n.inputState, i = t.targetTouches[0];
  e.lastTouchTime = Date.now(), i && (e.lastTouchX = i.clientX, e.lastTouchY = i.clientY), e.setSelectionOrigin("select.pointer");
};
pt.touchmove = (n) => {
  n.inputState.setSelectionOrigin("select.pointer");
};
Rt.mousedown = (n, t) => {
  if (n.observer.flush(), n.inputState.lastTouchTime > Date.now() - 2e3)
    return !1;
  let e = null;
  for (let i of n.state.facet(ia))
    if (e = i(n, t), e)
      break;
  if (!e && t.button == 0 && (e = vc(n, t)), e) {
    let i = !n.hasFocus;
    n.inputState.startMouseSelection(new gc(n, t, e, i)), i && n.observer.ignore(() => {
      Xl(n.contentDOM);
      let r = n.root.activeElement;
      r && !r.contains(n.contentDOM) && r.blur();
    });
    let s = n.inputState.mouseSelection;
    if (s)
      return s.start(t), s.dragging === !1;
  } else
    n.inputState.setSelectionOrigin("select.pointer");
  return !1;
};
function ho(n, t, e, i) {
  if (i == 1)
    return k.cursor(t, e);
  if (i == 2)
    return Yf(n.state, t, e);
  {
    let s = n.docView.lineAt(t, e), r = n.state.doc.lineAt(s ? s.posAtEnd : t), o = s ? s.posAtStart : r.from, l = s ? s.posAtEnd : r.to;
    return l < n.state.doc.length && l == r.to && l++, k.range(o, l);
  }
}
const kc = A.ie && A.ie_version <= 11;
let fo = null, co = 0, uo = 0;
function Sa(n) {
  if (!kc)
    return n.detail;
  let t = fo, e = uo;
  return fo = n, uo = Date.now(), co = !t || e > Date.now() - 400 && Math.abs(t.clientX - n.clientX) < 2 && Math.abs(t.clientY - n.clientY) < 2 ? (co + 1) % 3 : 1;
}
function vc(n, t) {
  let e = n.posAndSideAtCoords({ x: t.clientX, y: t.clientY }, !1), i = Sa(t), s = n.state.selection;
  return {
    update(r) {
      r.docChanged && (e.pos = r.changes.mapPos(e.pos), s = s.map(r.changes));
    },
    get(r, o, l) {
      let a = n.posAndSideAtCoords({ x: r.clientX, y: r.clientY }, !1), h, f = ho(n, a.pos, a.assoc, i);
      if (e.pos != a.pos && !o) {
        let c = ho(n, e.pos, e.assoc, i), u = Math.min(c.from, f.from), d = Math.max(c.to, f.to);
        f = u < f.from ? k.range(u, d) : k.range(d, u);
      }
      return o ? s.replaceRange(s.main.extend(f.from, f.to)) : l && i == 1 && s.ranges.length > 1 && (h = Sc(s, a.pos)) ? h : l ? s.addRange(f) : k.create([f]);
    }
  };
}
function Sc(n, t) {
  for (let e = 0; e < n.ranges.length; e++) {
    let { from: i, to: s } = n.ranges[e];
    if (i <= t && s >= t)
      return k.create(n.ranges.slice(0, e).concat(n.ranges.slice(e + 1)), n.mainIndex == e ? 0 : n.mainIndex - (n.mainIndex > e ? 1 : 0));
  }
  return null;
}
Rt.dragstart = (n, t) => {
  let { selection: { main: e } } = n.state;
  if (t.target.draggable) {
    let s = n.docView.tile.nearest(t.target);
    if (s && s.isWidget()) {
      let r = s.posAtStart, o = r + s.length;
      (r >= e.to || o <= e.from) && (e = k.range(r, o));
    }
  }
  let { inputState: i } = n;
  return i.mouseSelection && (i.mouseSelection.dragging = !0), i.draggedContent = e, t.dataTransfer && (t.dataTransfer.setData("Text", Vs(n.state, dr, n.state.sliceDoc(e.from, e.to))), t.dataTransfer.effectAllowed = "copyMove"), !1;
};
Rt.dragend = (n) => (n.inputState.draggedContent = null, !1);
function po(n, t, e, i) {
  if (e = Vs(n.state, ur, e), !e)
    return;
  let s = n.posAtCoords({ x: t.clientX, y: t.clientY }, !1), { draggedContent: r } = n.inputState, o = i && r && yc(n, t) ? { from: r.from, to: r.to } : null, l = { from: s, insert: e }, a = n.state.changes(o ? [o, l] : l);
  n.focus(), n.dispatch({
    changes: a,
    selection: { anchor: a.mapPos(s, -1), head: a.mapPos(s, 1) },
    userEvent: o ? "move.drop" : "input.drop"
  }), n.inputState.draggedContent = null;
}
Rt.drop = (n, t) => {
  if (!t.dataTransfer)
    return !1;
  if (n.state.readOnly)
    return !0;
  let e = t.dataTransfer.files;
  if (e && e.length) {
    let i = Array(e.length), s = 0, r = () => {
      ++s == e.length && po(n, t, i.filter((o) => o != null).join(n.state.lineBreak), !1);
    };
    for (let o = 0; o < e.length; o++) {
      let l = new FileReader();
      l.onerror = r, l.onload = () => {
        /[\x00-\x08\x0e-\x1f]{2}/.test(l.result) || (i[o] = l.result), r();
      }, l.readAsText(e[o]);
    }
    return !0;
  } else {
    let i = t.dataTransfer.getData("Text");
    if (i)
      return po(n, t, i, !0), !0;
  }
  return !1;
};
Rt.paste = (n, t) => {
  if (n.state.readOnly)
    return !0;
  n.observer.flush();
  let e = ka ? null : t.clipboardData;
  return e ? (va(n, e.getData("text/plain") || e.getData("text/uri-list")), !0) : (wc(n), !1);
};
function Oc(n, t) {
  let e = n.dom.parentNode;
  if (!e)
    return;
  let i = e.appendChild(document.createElement("textarea"));
  i.style.cssText = "position: fixed; left: -10000px; top: 10px", i.value = t, i.focus(), i.selectionEnd = t.length, i.selectionStart = 0, setTimeout(() => {
    i.remove(), n.focus();
  }, 50);
}
function Cc(n) {
  let t = [], e = [], i = !1;
  for (let s of n.selection.ranges)
    s.empty || (t.push(n.sliceDoc(s.from, s.to)), e.push(s));
  if (!t.length) {
    let s = -1;
    for (let { from: r } of n.selection.ranges) {
      let o = n.doc.lineAt(r);
      o.number > s && (t.push(o.text), e.push({ from: o.from, to: Math.min(n.doc.length, o.to + 1) })), s = o.number;
    }
    i = !0;
  }
  return { text: Vs(n, dr, t.join(n.lineBreak)), ranges: e, linewise: i };
}
let Qn = null;
Rt.copy = Rt.cut = (n, t) => {
  if (!oi(n.contentDOM, n.observer.selectionRange))
    return !1;
  let { text: e, ranges: i, linewise: s } = Cc(n.state);
  if (!e && !s)
    return !1;
  Qn = s ? e : null, t.type == "cut" && !n.state.readOnly && n.dispatch({
    changes: i,
    scrollIntoView: !0,
    userEvent: "delete.cut"
  });
  let r = ka ? null : t.clipboardData;
  return r ? (r.clearData(), r.setData("text/plain", e), !0) : (Oc(n, e), !1);
};
const Oa = /* @__PURE__ */ Jt.define();
function Ca(n, t) {
  let e = [];
  for (let i of n.facet(ra)) {
    let s = i(n, t);
    s && e.push(s);
  }
  return e.length ? n.update({ effects: e, annotations: Oa.of(!0) }) : null;
}
function Aa(n) {
  setTimeout(() => {
    let t = n.hasFocus;
    if (t != n.inputState.notifiedFocused) {
      let e = Ca(n.state, t);
      e ? n.dispatch(e) : n.update([]);
    }
  }, 10);
}
pt.focus = (n) => {
  n.inputState.lastFocusTime = Date.now(), !n.scrollDOM.scrollTop && (n.inputState.lastScrollTop || n.inputState.lastScrollLeft) && (n.scrollDOM.scrollTop = n.inputState.lastScrollTop, n.scrollDOM.scrollLeft = n.inputState.lastScrollLeft), Aa(n);
};
pt.blur = (n) => {
  n.observer.clearSelectionRange(), Aa(n);
};
pt.compositionstart = pt.compositionupdate = (n) => {
  n.observer.editContext || (n.inputState.compositionFirstChange == null && (n.inputState.compositionFirstChange = !0), n.inputState.composing < 0 && (n.inputState.composing = 0));
};
pt.compositionend = (n) => {
  n.observer.editContext || (n.inputState.composing = -1, n.inputState.compositionEndedAt = Date.now(), n.inputState.compositionPendingKey = !0, n.inputState.compositionPendingChange = n.observer.pendingRecords().length > 0, n.inputState.compositionFirstChange = null, A.chrome && A.android ? n.observer.flushSoon() : n.inputState.compositionPendingChange ? Promise.resolve().then(() => n.observer.flush()) : setTimeout(() => {
    n.inputState.composing < 0 && n.docView.hasComposition && n.update([]);
  }, 50));
};
pt.contextmenu = (n) => {
  n.inputState.lastContextMenu = Date.now();
};
Rt.beforeinput = (n, t) => {
  var e, i;
  if ((t.inputType == "insertText" || t.inputType == "insertCompositionText") && (n.inputState.insertingText = t.data, n.inputState.insertingTextAt = Date.now()), t.inputType == "insertReplacementText" && n.observer.editContext) {
    let r = (e = t.dataTransfer) === null || e === void 0 ? void 0 : e.getData("text/plain"), o = t.getTargetRanges();
    if (r && o.length) {
      let l = o[0], a = n.posAtDOM(l.startContainer, l.startOffset), h = n.posAtDOM(l.endContainer, l.endOffset);
      return yr(n, { from: a, to: h, insert: n.state.toText(r) }, null), !0;
    }
  }
  let s;
  if (A.chrome && A.android && (s = xa.find((r) => r.inputType == t.inputType)) && (n.observer.delayAndroidKey(s.key, s.keyCode), s.key == "Backspace" || s.key == "Delete")) {
    let r = ((i = window.visualViewport) === null || i === void 0 ? void 0 : i.height) || 0;
    setTimeout(() => {
      var o;
      (((o = window.visualViewport) === null || o === void 0 ? void 0 : o.height) || 0) > r + 10 && n.hasFocus && (n.contentDOM.blur(), n.focus());
    }, 100);
  }
  return A.ios && t.inputType == "deleteContentForward" && n.observer.flushSoon(), A.safari && t.inputType == "insertText" && n.inputState.composing >= 0 && setTimeout(() => pt.compositionend(n, t), 20), !1;
};
const go = /* @__PURE__ */ new Set();
function Ac(n) {
  go.has(n) || (go.add(n), n.addEventListener("copy", () => {
  }), n.addEventListener("cut", () => {
  }));
}
const mo = ["pre-wrap", "normal", "pre-line", "break-spaces"];
let je = !1;
function yo() {
  je = !1;
}
class Tc {
  constructor(t) {
    this.lineWrapping = t, this.doc = I.empty, this.heightSamples = {}, this.lineHeight = 14, this.charWidth = 7, this.textHeight = 14, this.lineLength = 30;
  }
  heightForGap(t, e) {
    let i = this.doc.lineAt(e).number - this.doc.lineAt(t).number + 1;
    return this.lineWrapping && (i += Math.max(0, Math.ceil((e - t - i * this.lineLength * 0.5) / this.lineLength))), this.lineHeight * i;
  }
  heightForLine(t) {
    return this.lineWrapping ? (1 + Math.max(0, Math.ceil((t - this.lineLength) / Math.max(1, this.lineLength - 5)))) * this.lineHeight : this.lineHeight;
  }
  setDoc(t) {
    return this.doc = t, this;
  }
  mustRefreshForWrapping(t) {
    return mo.indexOf(t) > -1 != this.lineWrapping;
  }
  mustRefreshForHeights(t) {
    let e = !1;
    for (let i = 0; i < t.length; i++) {
      let s = t[i];
      s < 0 ? i++ : this.heightSamples[Math.floor(s * 10)] || (e = !0, this.heightSamples[Math.floor(s * 10)] = !0);
    }
    return e;
  }
  refresh(t, e, i, s, r, o) {
    let l = mo.indexOf(t) > -1, a = Math.abs(e - this.lineHeight) > 0.3 || this.lineWrapping != l || Math.abs(i - this.charWidth) > 0.1;
    if (this.lineWrapping = l, this.lineHeight = e, this.charWidth = i, this.textHeight = s, this.lineLength = r, a) {
      this.heightSamples = {};
      for (let h = 0; h < o.length; h++) {
        let f = o[h];
        f < 0 ? h++ : this.heightSamples[Math.floor(f * 10)] = !0;
      }
    }
    return a;
  }
}
class Mc {
  constructor(t, e) {
    this.from = t, this.heights = e, this.index = 0;
  }
  get more() {
    return this.index < this.heights.length;
  }
}
class Mt {
  /**
  @internal
  */
  constructor(t, e, i, s, r) {
    this.from = t, this.length = e, this.top = i, this.height = s, this._content = r;
  }
  /**
  The type of element this is. When querying lines, this may be
  an array of all the blocks that make up the line.
  */
  get type() {
    return typeof this._content == "number" ? ut.Text : Array.isArray(this._content) ? this._content : this._content.type;
  }
  /**
  The end of the element as a document position.
  */
  get to() {
    return this.from + this.length;
  }
  /**
  The bottom position of the element.
  */
  get bottom() {
    return this.top + this.height;
  }
  /**
  If this is a widget block, this will return the widget
  associated with it.
  */
  get widget() {
    return this._content instanceof Ce ? this._content.widget : null;
  }
  /**
  If this is a textblock, this holds the number of line breaks
  that appear in widgets inside the block.
  */
  get widgetLineBreaks() {
    return typeof this._content == "number" ? this._content : 0;
  }
  /**
  @internal
  */
  join(t) {
    let e = (Array.isArray(this._content) ? this._content : [this]).concat(Array.isArray(t._content) ? t._content : [t]);
    return new Mt(this.from, this.length + t.length, this.top, this.height + t.height, e);
  }
}
var z = /* @__PURE__ */ (function(n) {
  return n[n.ByPos = 0] = "ByPos", n[n.ByHeight = 1] = "ByHeight", n[n.ByPosNoHeight = 2] = "ByPosNoHeight", n;
})(z || (z = {}));
const is = 1e-3;
class lt {
  constructor(t, e, i = 2) {
    this.length = t, this.height = e, this.flags = i;
  }
  get outdated() {
    return (this.flags & 2) > 0;
  }
  set outdated(t) {
    this.flags = (t ? 2 : 0) | this.flags & -3;
  }
  setHeight(t) {
    this.height != t && (Math.abs(this.height - t) > is && (je = !0), this.height = t);
  }
  // Base case is to replace a leaf node, which simply builds a tree
  // from the new nodes and returns that (HeightMapBranch and
  // HeightMapGap override this to actually use from/to)
  replace(t, e, i) {
    return lt.of(i);
  }
  // Again, these are base cases, and are overridden for branch and gap nodes.
  decomposeLeft(t, e) {
    e.push(this);
  }
  decomposeRight(t, e) {
    e.push(this);
  }
  applyChanges(t, e, i, s) {
    let r = this, o = i.doc;
    for (let l = s.length - 1; l >= 0; l--) {
      let { fromA: a, toA: h, fromB: f, toB: c } = s[l], u = r.lineAt(a, z.ByPosNoHeight, i.setDoc(e), 0, 0), d = u.to >= h ? u : r.lineAt(h, z.ByPosNoHeight, i, 0, 0);
      for (c += d.to - h, h = d.to; l > 0 && u.from <= s[l - 1].toA; )
        a = s[l - 1].fromA, f = s[l - 1].fromB, l--, a < u.from && (u = r.lineAt(a, z.ByPosNoHeight, i, 0, 0));
      f += u.from - a, a = u.from;
      let p = br.build(i.setDoc(o), t, f, c);
      r = ms(r, r.replace(a, h, p));
    }
    return r.updateHeight(i, 0);
  }
  static empty() {
    return new yt(0, 0, 0);
  }
  // nodes uses null values to indicate the position of line breaks.
  // There are never line breaks at the start or end of the array, or
  // two line breaks next to each other, and the array isn't allowed
  // to be empty (same restrictions as return value from the builder).
  static of(t) {
    if (t.length == 1)
      return t[0];
    let e = 0, i = t.length, s = 0, r = 0;
    for (; ; )
      if (e == i)
        if (s > r * 2) {
          let l = t[e - 1];
          l.break ? t.splice(--e, 1, l.left, null, l.right) : t.splice(--e, 1, l.left, l.right), i += 1 + l.break, s -= l.size;
        } else if (r > s * 2) {
          let l = t[i];
          l.break ? t.splice(i, 1, l.left, null, l.right) : t.splice(i, 1, l.left, l.right), i += 2 + l.break, r -= l.size;
        } else
          break;
      else if (s < r) {
        let l = t[e++];
        l && (s += l.size);
      } else {
        let l = t[--i];
        l && (r += l.size);
      }
    let o = 0;
    return t[e - 1] == null ? (o = 1, e--) : t[e] == null && (o = 1, i++), new Dc(lt.of(t.slice(0, e)), o, lt.of(t.slice(i)));
  }
}
function ms(n, t) {
  return n == t ? n : (n.constructor != t.constructor && (je = !0), t);
}
lt.prototype.size = 1;
const Pc = /* @__PURE__ */ H.replace({});
class Ta extends lt {
  constructor(t, e, i) {
    super(t, e), this.deco = i, this.spaceAbove = 0;
  }
  mainBlock(t, e) {
    return new Mt(e, this.length, t + this.spaceAbove, this.height - this.spaceAbove, this.deco || 0);
  }
  blockAt(t, e, i, s) {
    return this.spaceAbove && t < i + this.spaceAbove ? new Mt(s, 0, i, this.spaceAbove, Pc) : this.mainBlock(i, s);
  }
  lineAt(t, e, i, s, r) {
    let o = this.mainBlock(s, r);
    return this.spaceAbove ? this.blockAt(0, i, s, r).join(o) : o;
  }
  forEachLine(t, e, i, s, r, o) {
    t <= r + this.length && e >= r && o(this.lineAt(0, z.ByPos, i, s, r));
  }
  setMeasuredHeight(t) {
    let e = t.heights[t.index++];
    e < 0 ? (this.spaceAbove = -e, e = t.heights[t.index++]) : this.spaceAbove = 0, this.setHeight(e);
  }
  updateHeight(t, e = 0, i = !1, s) {
    return s && s.from <= e && s.more && this.setMeasuredHeight(s), this.outdated = !1, this;
  }
  toString() {
    return `block(${this.length})`;
  }
}
class yt extends Ta {
  constructor(t, e, i) {
    super(t, e, null), this.collapsed = 0, this.widgetHeight = 0, this.breaks = 0, this.spaceAbove = i;
  }
  mainBlock(t, e) {
    return new Mt(e, this.length, t + this.spaceAbove, this.height - this.spaceAbove, this.breaks);
  }
  replace(t, e, i) {
    let s = i[0];
    return i.length == 1 && (s instanceof yt || s instanceof tt && s.flags & 4) && Math.abs(this.length - s.length) < 10 ? (s instanceof tt ? s = new yt(s.length, this.height, this.spaceAbove) : s.height = this.height, this.outdated || (s.outdated = !1), s) : lt.of(i);
  }
  updateHeight(t, e = 0, i = !1, s) {
    return s && s.from <= e && s.more ? this.setMeasuredHeight(s) : (i || this.outdated) && (this.spaceAbove = 0, this.setHeight(Math.max(this.widgetHeight, t.heightForLine(this.length - this.collapsed)) + this.breaks * t.lineHeight)), this.outdated = !1, this;
  }
  toString() {
    return `line(${this.length}${this.collapsed ? -this.collapsed : ""}${this.widgetHeight ? ":" + this.widgetHeight : ""})`;
  }
}
class tt extends lt {
  constructor(t) {
    super(t, 0);
  }
  heightMetrics(t, e) {
    let i = t.doc.lineAt(e).number, s = t.doc.lineAt(e + this.length).number, r = s - i + 1, o, l = 0;
    if (t.lineWrapping) {
      let a = Math.min(this.height, t.lineHeight * r);
      o = a / r, this.length > r + 1 && (l = (this.height - a) / (this.length - r - 1));
    } else
      o = this.height / r;
    return { firstLine: i, lastLine: s, perLine: o, perChar: l };
  }
  blockAt(t, e, i, s) {
    let { firstLine: r, lastLine: o, perLine: l, perChar: a } = this.heightMetrics(e, s);
    if (e.lineWrapping) {
      let h = s + (t < e.lineHeight ? 0 : Math.round(Math.max(0, Math.min(1, (t - i) / this.height)) * this.length)), f = e.doc.lineAt(h), c = l + f.length * a, u = Math.max(i, t - c / 2);
      return new Mt(f.from, f.length, u, c, 0);
    } else {
      let h = Math.max(0, Math.min(o - r, Math.floor((t - i) / l))), { from: f, length: c } = e.doc.line(r + h);
      return new Mt(f, c, i + l * h, l, 0);
    }
  }
  lineAt(t, e, i, s, r) {
    if (e == z.ByHeight)
      return this.blockAt(t, i, s, r);
    if (e == z.ByPosNoHeight) {
      let { from: d, to: p } = i.doc.lineAt(t);
      return new Mt(d, p - d, 0, 0, 0);
    }
    let { firstLine: o, perLine: l, perChar: a } = this.heightMetrics(i, r), h = i.doc.lineAt(t), f = l + h.length * a, c = h.number - o, u = s + l * c + a * (h.from - r - c);
    return new Mt(h.from, h.length, Math.max(s, Math.min(u, s + this.height - f)), f, 0);
  }
  forEachLine(t, e, i, s, r, o) {
    t = Math.max(t, r), e = Math.min(e, r + this.length);
    let { firstLine: l, perLine: a, perChar: h } = this.heightMetrics(i, r);
    for (let f = t, c = s; f <= e; ) {
      let u = i.doc.lineAt(f);
      if (f == t) {
        let p = u.number - l;
        c += a * p + h * (t - r - p);
      }
      let d = a + h * u.length;
      o(new Mt(u.from, u.length, c, d, 0)), c += d, f = u.to + 1;
    }
  }
  replace(t, e, i) {
    let s = this.length - e;
    if (s > 0) {
      let r = i[i.length - 1];
      r instanceof tt ? i[i.length - 1] = new tt(r.length + s) : i.push(null, new tt(s - 1));
    }
    if (t > 0) {
      let r = i[0];
      r instanceof tt ? i[0] = new tt(t + r.length) : i.unshift(new tt(t - 1), null);
    }
    return lt.of(i);
  }
  decomposeLeft(t, e) {
    e.push(new tt(t - 1), null);
  }
  decomposeRight(t, e) {
    e.push(null, new tt(this.length - t - 1));
  }
  updateHeight(t, e = 0, i = !1, s) {
    let r = e + this.length;
    if (s && s.from <= e + this.length && s.more) {
      let o = [], l = Math.max(e, s.from), a = -1;
      for (s.from > e && o.push(new tt(s.from - e - 1).updateHeight(t, e)); l <= r && s.more; ) {
        let f = t.doc.lineAt(l).length;
        o.length && o.push(null);
        let c = s.heights[s.index++], u = 0;
        c < 0 && (u = -c, c = s.heights[s.index++]), a == -1 ? a = c : Math.abs(c - a) >= is && (a = -2);
        let d = new yt(f, c, u);
        d.outdated = !1, o.push(d), l += f + 1;
      }
      l <= r && o.push(null, new tt(r - l).updateHeight(t, l));
      let h = lt.of(o);
      return (a < 0 || Math.abs(h.height - this.height) >= is || Math.abs(a - this.heightMetrics(t, e).perLine) >= is) && (je = !0), ms(this, h);
    } else (i || this.outdated) && (this.setHeight(t.heightForGap(e, e + this.length)), this.outdated = !1);
    return this;
  }
  toString() {
    return `gap(${this.length})`;
  }
}
class Dc extends lt {
  constructor(t, e, i) {
    super(t.length + e + i.length, t.height + i.height, e | (t.outdated || i.outdated ? 2 : 0)), this.left = t, this.right = i, this.size = t.size + i.size;
  }
  get break() {
    return this.flags & 1;
  }
  blockAt(t, e, i, s) {
    let r = i + this.left.height;
    return t < r ? this.left.blockAt(t, e, i, s) : this.right.blockAt(t, e, r, s + this.left.length + this.break);
  }
  lineAt(t, e, i, s, r) {
    let o = s + this.left.height, l = r + this.left.length + this.break, a = e == z.ByHeight ? t < o : t < l, h = a ? this.left.lineAt(t, e, i, s, r) : this.right.lineAt(t, e, i, o, l);
    if (this.break || (a ? h.to < l : h.from > l))
      return h;
    let f = e == z.ByPosNoHeight ? z.ByPosNoHeight : z.ByPos;
    return a ? h.join(this.right.lineAt(l, f, i, o, l)) : this.left.lineAt(l, f, i, s, r).join(h);
  }
  forEachLine(t, e, i, s, r, o) {
    let l = s + this.left.height, a = r + this.left.length + this.break;
    if (this.break)
      t < a && this.left.forEachLine(t, e, i, s, r, o), e >= a && this.right.forEachLine(t, e, i, l, a, o);
    else {
      let h = this.lineAt(a, z.ByPos, i, s, r);
      t < h.from && this.left.forEachLine(t, h.from - 1, i, s, r, o), h.to >= t && h.from <= e && o(h), e > h.to && this.right.forEachLine(h.to + 1, e, i, l, a, o);
    }
  }
  replace(t, e, i) {
    let s = this.left.length + this.break;
    if (e < s)
      return this.balanced(this.left.replace(t, e, i), this.right);
    if (t > this.left.length)
      return this.balanced(this.left, this.right.replace(t - s, e - s, i));
    let r = [];
    t > 0 && this.decomposeLeft(t, r);
    let o = r.length;
    for (let l of i)
      r.push(l);
    if (t > 0 && bo(r, o - 1), e < this.length) {
      let l = r.length;
      this.decomposeRight(e, r), bo(r, l);
    }
    return lt.of(r);
  }
  decomposeLeft(t, e) {
    let i = this.left.length;
    if (t <= i)
      return this.left.decomposeLeft(t, e);
    e.push(this.left), this.break && (i++, t >= i && e.push(null)), t > i && this.right.decomposeLeft(t - i, e);
  }
  decomposeRight(t, e) {
    let i = this.left.length, s = i + this.break;
    if (t >= s)
      return this.right.decomposeRight(t - s, e);
    t < i && this.left.decomposeRight(t, e), this.break && t < s && e.push(null), e.push(this.right);
  }
  balanced(t, e) {
    return t.size > 2 * e.size || e.size > 2 * t.size ? lt.of(this.break ? [t, null, e] : [t, e]) : (this.left = ms(this.left, t), this.right = ms(this.right, e), this.setHeight(t.height + e.height), this.outdated = t.outdated || e.outdated, this.size = t.size + e.size, this.length = t.length + this.break + e.length, this);
  }
  updateHeight(t, e = 0, i = !1, s) {
    let { left: r, right: o } = this, l = e + r.length + this.break, a = null;
    return s && s.from <= e + r.length && s.more ? a = r = r.updateHeight(t, e, i, s) : r.updateHeight(t, e, i), s && s.from <= l + o.length && s.more ? a = o = o.updateHeight(t, l, i, s) : o.updateHeight(t, l, i), a ? this.balanced(r, o) : (this.height = this.left.height + this.right.height, this.outdated = !1, this);
  }
  toString() {
    return this.left + (this.break ? " " : "-") + this.right;
  }
}
function bo(n, t) {
  let e, i;
  n[t] == null && (e = n[t - 1]) instanceof tt && (i = n[t + 1]) instanceof tt && n.splice(t - 1, 3, new tt(e.length + 1 + i.length));
}
const Bc = 5;
class br {
  constructor(t, e) {
    this.pos = t, this.oracle = e, this.nodes = [], this.lineStart = -1, this.lineEnd = -1, this.covering = null, this.writtenTo = t;
  }
  get isCovered() {
    return this.covering && this.nodes[this.nodes.length - 1] == this.covering;
  }
  span(t, e) {
    if (this.lineStart > -1) {
      let i = Math.min(e, this.lineEnd), s = this.nodes[this.nodes.length - 1];
      s instanceof yt ? s.length += i - this.pos : (i > this.pos || !this.isCovered) && this.nodes.push(new yt(i - this.pos, -1, 0)), this.writtenTo = i, e > i && (this.nodes.push(null), this.writtenTo++, this.lineStart = -1);
    }
    this.pos = e;
  }
  point(t, e, i) {
    if (t < e || i.heightRelevant) {
      let s = i.widget ? i.widget.estimatedHeight : 0, r = i.widget ? i.widget.lineBreaks : 0;
      s < 0 && (s = this.oracle.lineHeight);
      let o = e - t;
      i.block ? this.addBlock(new Ta(o, s, i)) : (o || r || s >= Bc) && this.addLineDeco(s, r, o);
    } else e > t && this.span(t, e);
    this.lineEnd > -1 && this.lineEnd < this.pos && (this.lineEnd = this.oracle.doc.lineAt(this.pos).to);
  }
  enterLine() {
    if (this.lineStart > -1)
      return;
    let { from: t, to: e } = this.oracle.doc.lineAt(this.pos);
    this.lineStart = t, this.lineEnd = e, this.writtenTo < t && ((this.writtenTo < t - 1 || this.nodes[this.nodes.length - 1] == null) && this.nodes.push(this.blankContent(this.writtenTo, t - 1)), this.nodes.push(null)), this.pos > t && this.nodes.push(new yt(this.pos - t, -1, 0)), this.writtenTo = this.pos;
  }
  blankContent(t, e) {
    let i = new tt(e - t);
    return this.oracle.doc.lineAt(t).to == e && (i.flags |= 4), i;
  }
  ensureLine() {
    this.enterLine();
    let t = this.nodes.length ? this.nodes[this.nodes.length - 1] : null;
    if (t instanceof yt)
      return t;
    let e = new yt(0, -1, 0);
    return this.nodes.push(e), e;
  }
  addBlock(t) {
    this.enterLine();
    let e = t.deco;
    e && e.startSide > 0 && !this.isCovered && this.ensureLine(), this.nodes.push(t), this.writtenTo = this.pos = this.pos + t.length, e && e.endSide > 0 && (this.covering = t);
  }
  addLineDeco(t, e, i) {
    let s = this.ensureLine();
    s.length += i, s.collapsed += i, s.widgetHeight = Math.max(s.widgetHeight, t), s.breaks += e, this.writtenTo = this.pos = this.pos + i;
  }
  finish(t) {
    let e = this.nodes.length == 0 ? null : this.nodes[this.nodes.length - 1];
    this.lineStart > -1 && !(e instanceof yt) && !this.isCovered ? this.nodes.push(new yt(0, -1, 0)) : (this.writtenTo < this.pos || e == null) && this.nodes.push(this.blankContent(this.writtenTo, this.pos));
    let i = t;
    for (let s of this.nodes)
      s instanceof yt && s.updateHeight(this.oracle, i), i += s ? s.length : 1;
    return this.nodes;
  }
  // Always called with a region that on both sides either stretches
  // to a line break or the end of the document.
  // The returned array uses null to indicate line breaks, but never
  // starts or ends in a line break, or has multiple line breaks next
  // to each other.
  static build(t, e, i, s) {
    let r = new br(i, t);
    return L.spans(e, i, s, r, 0), r.finish(i);
  }
}
function Rc(n, t, e) {
  let i = new Lc();
  return L.compare(n, t, e, i, 0), i.changes;
}
class Lc {
  constructor() {
    this.changes = [];
  }
  compareRange() {
  }
  comparePoint(t, e, i, s) {
    (t < e || i && i.heightRelevant || s && s.heightRelevant) && Ie(t, e, this.changes, 5);
  }
}
function Ec(n, t) {
  let e = n.getBoundingClientRect(), i = n.ownerDocument, s = i.defaultView || window, r = Math.max(0, e.left), o = Math.min(s.innerWidth, e.right), l = Math.max(0, e.top), a = Math.min(s.innerHeight, e.bottom);
  for (let h = n.parentNode; h && h != i.body; )
    if (h.nodeType == 1) {
      let f = h, c = window.getComputedStyle(f);
      if ((f.scrollHeight > f.clientHeight || f.scrollWidth > f.clientWidth) && c.overflow != "visible") {
        let u = f.getBoundingClientRect();
        r = Math.max(r, u.left), o = Math.min(o, u.right), l = Math.max(l, u.top), a = Math.min(h == n.parentNode ? s.innerHeight : a, u.bottom);
      }
      h = c.position == "absolute" || c.position == "fixed" ? f.offsetParent : f.parentNode;
    } else if (h.nodeType == 11)
      h = h.host;
    else
      break;
  return {
    left: r - e.left,
    right: Math.max(r, o) - e.left,
    top: l - (e.top + t),
    bottom: Math.max(l, a) - (e.top + t)
  };
}
function Ic(n) {
  let t = n.getBoundingClientRect(), e = n.ownerDocument.defaultView || window;
  return t.left < e.innerWidth && t.right > 0 && t.top < e.innerHeight && t.bottom > 0;
}
function Nc(n, t) {
  let e = n.getBoundingClientRect();
  return {
    left: 0,
    right: e.right - e.left,
    top: t,
    bottom: e.bottom - (e.top + t)
  };
}
class tn {
  constructor(t, e, i, s) {
    this.from = t, this.to = e, this.size = i, this.displaySize = s;
  }
  static same(t, e) {
    if (t.length != e.length)
      return !1;
    for (let i = 0; i < t.length; i++) {
      let s = t[i], r = e[i];
      if (s.from != r.from || s.to != r.to || s.size != r.size)
        return !1;
    }
    return !0;
  }
  draw(t, e) {
    return H.replace({
      widget: new Vc(this.displaySize * (e ? t.scaleY : t.scaleX), e)
    }).range(this.from, this.to);
  }
}
class Vc extends Si {
  constructor(t, e) {
    super(), this.size = t, this.vertical = e;
  }
  eq(t) {
    return t.size == this.size && t.vertical == this.vertical;
  }
  toDOM() {
    let t = document.createElement("div");
    return this.vertical ? t.style.height = this.size + "px" : (t.style.width = this.size + "px", t.style.height = "2px", t.style.display = "inline-block"), t;
  }
  get estimatedHeight() {
    return this.vertical ? this.size : -1;
  }
}
class xo {
  constructor(t, e) {
    this.view = t, this.state = e, this.pixelViewport = { left: 0, right: window.innerWidth, top: 0, bottom: 0 }, this.inView = !0, this.paddingTop = 0, this.paddingBottom = 0, this.contentDOMWidth = 0, this.contentDOMHeight = 0, this.editorHeight = 0, this.editorWidth = 0, this.scaleX = 1, this.scaleY = 1, this.scrollOffset = 0, this.scrolledToBottom = !1, this.scrollAnchorPos = 0, this.scrollAnchorHeight = -1, this.scaler = wo, this.scrollTarget = null, this.printing = !1, this.mustMeasureContent = !0, this.defaultTextDirection = $.LTR, this.visibleRanges = [], this.mustEnforceCursorAssoc = !1;
    let i = e.facet(pr).some((s) => typeof s != "function" && s.class == "cm-lineWrapping");
    this.heightOracle = new Tc(i), this.stateDeco = ko(e), this.heightMap = lt.empty().applyChanges(this.stateDeco, I.empty, this.heightOracle.setDoc(e.doc), [new vt(0, 0, 0, e.doc.length)]);
    for (let s = 0; s < 2 && (this.viewport = this.getViewport(0, null), !!this.updateForViewport()); s++)
      ;
    this.updateViewportLines(), this.lineGaps = this.ensureLineGaps([]), this.lineGapDeco = H.set(this.lineGaps.map((s) => s.draw(this, !1))), this.scrollParent = t.scrollDOM, this.computeVisibleRanges();
  }
  updateForViewport() {
    let t = [this.viewport], { main: e } = this.state.selection;
    for (let i = 0; i <= 1; i++) {
      let s = i ? e.head : e.anchor;
      if (!t.some(({ from: r, to: o }) => s >= r && s <= o)) {
        let { from: r, to: o } = this.lineBlockAt(s);
        t.push(new Ni(r, o));
      }
    }
    return this.viewports = t.sort((i, s) => i.from - s.from), this.updateScaler();
  }
  updateScaler() {
    let t = this.scaler;
    return this.scaler = this.heightMap.height <= 7e6 ? wo : new xr(this.heightOracle, this.heightMap, this.viewports), t.eq(this.scaler) ? 0 : 2;
  }
  updateViewportLines() {
    this.viewportLines = [], this.heightMap.forEachLine(this.viewport.from, this.viewport.to, this.heightOracle.setDoc(this.state.doc), 0, 0, (t) => {
      this.viewportLines.push(si(t, this.scaler));
    });
  }
  update(t, e = null) {
    this.state = t.state;
    let i = this.stateDeco;
    this.stateDeco = ko(this.state);
    let s = t.changedRanges, r = vt.extendWithRanges(s, Rc(i, this.stateDeco, t ? t.changes : G.empty(this.state.doc.length))), o = this.heightMap.height, l = this.scrolledToBottom ? null : this.scrollAnchorAt(this.scrollOffset);
    yo(), this.heightMap = this.heightMap.applyChanges(this.stateDeco, t.startState.doc, this.heightOracle.setDoc(this.state.doc), r), (this.heightMap.height != o || je) && (t.flags |= 2), l ? (this.scrollAnchorPos = t.changes.mapPos(l.from, -1), this.scrollAnchorHeight = l.top) : (this.scrollAnchorPos = -1, this.scrollAnchorHeight = o);
    let a = r.length ? this.mapViewport(this.viewport, t.changes) : this.viewport;
    (e && (e.range.head < a.from || e.range.head > a.to) || !this.viewportIsAppropriate(a)) && (a = this.getViewport(0, e));
    let h = a.from != this.viewport.from || a.to != this.viewport.to;
    this.viewport = a, t.flags |= this.updateForViewport(), (h || !t.changes.empty || t.flags & 2) && this.updateViewportLines(), (this.lineGaps.length || this.viewport.to - this.viewport.from > 4e3) && this.updateLineGaps(this.ensureLineGaps(this.mapLineGaps(this.lineGaps, t.changes))), t.flags |= this.computeVisibleRanges(t.changes), e && (this.scrollTarget = e), !this.mustEnforceCursorAssoc && (t.selectionSet || t.focusChanged) && t.view.lineWrapping && t.state.selection.main.empty && t.state.selection.main.assoc && !t.state.facet(Mf) && (this.mustEnforceCursorAssoc = !0);
  }
  measure() {
    let { view: t } = this, e = t.contentDOM, i = window.getComputedStyle(e), s = this.heightOracle, r = i.whiteSpace;
    this.defaultTextDirection = i.direction == "rtl" ? $.RTL : $.LTR;
    let o = this.heightOracle.mustRefreshForWrapping(r) || this.mustMeasureContent === "refresh", l = e.getBoundingClientRect(), a = o || this.mustMeasureContent || this.contentDOMHeight != l.height;
    this.contentDOMHeight = l.height, this.mustMeasureContent = !1;
    let h = 0, f = 0;
    if (l.width && l.height) {
      let { scaleX: S, scaleY: T } = $l(e, l);
      (S > 5e-3 && Math.abs(this.scaleX - S) > 5e-3 || T > 5e-3 && Math.abs(this.scaleY - T) > 5e-3) && (this.scaleX = S, this.scaleY = T, h |= 16, o = a = !0);
    }
    let c = (parseInt(i.paddingTop) || 0) * this.scaleY, u = (parseInt(i.paddingBottom) || 0) * this.scaleY;
    (this.paddingTop != c || this.paddingBottom != u) && (this.paddingTop = c, this.paddingBottom = u, h |= 18), this.editorWidth != t.scrollDOM.clientWidth && (s.lineWrapping && (a = !0), this.editorWidth = t.scrollDOM.clientWidth, h |= 16);
    let d = jl(this.view.contentDOM, !1).y;
    d != this.scrollParent && (this.scrollParent = d, this.scrollAnchorHeight = -1, this.scrollOffset = 0);
    let p = this.getScrollOffset();
    this.scrollOffset != p && (this.scrollAnchorHeight = -1, this.scrollOffset = p), this.scrolledToBottom = ql(this.scrollParent || t.win);
    let g = (this.printing ? Nc : Ec)(e, this.paddingTop), m = g.top - this.pixelViewport.top, y = g.bottom - this.pixelViewport.bottom;
    this.pixelViewport = g;
    let b = this.pixelViewport.bottom > this.pixelViewport.top && this.pixelViewport.right > this.pixelViewport.left;
    if (b != this.inView && (this.inView = b, b && (a = !0)), !this.inView && !this.scrollTarget && !Ic(t.dom))
      return 0;
    let x = l.width;
    if ((this.contentDOMWidth != x || this.editorHeight != t.scrollDOM.clientHeight) && (this.contentDOMWidth = l.width, this.editorHeight = t.scrollDOM.clientHeight, h |= 16), a) {
      let S = t.docView.measureVisibleLineHeights(this.viewport);
      if (s.mustRefreshForHeights(S) && (o = !0), o || s.lineWrapping && Math.abs(x - this.contentDOMWidth) > s.charWidth) {
        let { lineHeight: T, charWidth: C, textHeight: R } = t.docView.measureTextSize();
        o = T > 0 && s.refresh(r, T, C, R, Math.max(5, x / C), S), o && (t.docView.minWidth = 0, h |= 16);
      }
      m > 0 && y > 0 ? f = Math.max(m, y) : m < 0 && y < 0 && (f = Math.min(m, y)), yo();
      for (let T of this.viewports) {
        let C = T.from == this.viewport.from ? S : t.docView.measureVisibleLineHeights(T);
        this.heightMap = (o ? lt.empty().applyChanges(this.stateDeco, I.empty, this.heightOracle, [new vt(0, 0, 0, t.state.doc.length)]) : this.heightMap).updateHeight(s, 0, o, new Mc(T.from, C));
      }
      je && (h |= 2);
    }
    let O = !this.viewportIsAppropriate(this.viewport, f) || this.scrollTarget && (this.scrollTarget.range.head < this.viewport.from || this.scrollTarget.range.head > this.viewport.to);
    return O && (h & 2 && (h |= this.updateScaler()), this.viewport = this.getViewport(f, this.scrollTarget), h |= this.updateForViewport()), (h & 2 || O) && this.updateViewportLines(), (this.lineGaps.length || this.viewport.to - this.viewport.from > 4e3) && this.updateLineGaps(this.ensureLineGaps(o ? [] : this.lineGaps, t)), h |= this.computeVisibleRanges(), this.mustEnforceCursorAssoc && (this.mustEnforceCursorAssoc = !1, t.docView.enforceCursorAssoc()), h;
  }
  get visibleTop() {
    return this.scaler.fromDOM(this.pixelViewport.top);
  }
  get visibleBottom() {
    return this.scaler.fromDOM(this.pixelViewport.bottom);
  }
  getViewport(t, e) {
    let i = 0.5 - Math.max(-0.5, Math.min(0.5, t / 1e3 / 2)), s = this.heightMap, r = this.heightOracle, { visibleTop: o, visibleBottom: l } = this, a = new Ni(s.lineAt(o - i * 1e3, z.ByHeight, r, 0, 0).from, s.lineAt(l + (1 - i) * 1e3, z.ByHeight, r, 0, 0).to);
    if (e) {
      let { head: h } = e.range;
      if (h < a.from || h > a.to) {
        let f = Math.min(this.editorHeight, this.pixelViewport.bottom - this.pixelViewport.top), c = s.lineAt(h, z.ByPos, r, 0, 0), u;
        e.y == "center" ? u = (c.top + c.bottom) / 2 - f / 2 : e.y == "start" || e.y == "nearest" && h < a.from ? u = c.top : u = c.bottom - f, a = new Ni(s.lineAt(u - 1e3 / 2, z.ByHeight, r, 0, 0).from, s.lineAt(u + f + 1e3 / 2, z.ByHeight, r, 0, 0).to);
      }
    }
    return a;
  }
  mapViewport(t, e) {
    let i = e.mapPos(t.from, -1), s = e.mapPos(t.to, 1);
    return new Ni(this.heightMap.lineAt(i, z.ByPos, this.heightOracle, 0, 0).from, this.heightMap.lineAt(s, z.ByPos, this.heightOracle, 0, 0).to);
  }
  // Checks if a given viewport covers the visible part of the
  // document and not too much beyond that.
  viewportIsAppropriate({ from: t, to: e }, i = 0) {
    if (!this.inView)
      return !0;
    let { top: s } = this.heightMap.lineAt(t, z.ByPos, this.heightOracle, 0, 0), { bottom: r } = this.heightMap.lineAt(e, z.ByPos, this.heightOracle, 0, 0), { visibleTop: o, visibleBottom: l } = this;
    return (t == 0 || s <= o - Math.max(10, Math.min(
      -i,
      250
      /* VP.MaxCoverMargin */
    ))) && (e == this.state.doc.length || r >= l + Math.max(10, Math.min(
      i,
      250
      /* VP.MaxCoverMargin */
    ))) && s > o - 2 * 1e3 && r < l + 2 * 1e3;
  }
  mapLineGaps(t, e) {
    if (!t.length || e.empty)
      return t;
    let i = [];
    for (let s of t)
      e.touchesRange(s.from, s.to) || i.push(new tn(e.mapPos(s.from), e.mapPos(s.to), s.size, s.displaySize));
    return i;
  }
  // Computes positions in the viewport where the start or end of a
  // line should be hidden, trying to reuse existing line gaps when
  // appropriate to avoid unneccesary redraws.
  // Uses crude character-counting for the positioning and sizing,
  // since actual DOM coordinates aren't always available and
  // predictable. Relies on generous margins (see LG.Margin) to hide
  // the artifacts this might produce from the user.
  ensureLineGaps(t, e) {
    let i = this.heightOracle.lineWrapping, s = i ? 1e4 : 2e3, r = s >> 1, o = s << 1;
    if (this.defaultTextDirection != $.LTR && !i)
      return [];
    let l = [], a = (f, c, u, d) => {
      if (c - f < r)
        return;
      let p = this.state.selection.main, g = [p.from];
      p.empty || g.push(p.to);
      for (let y of g)
        if (y > f && y < c) {
          a(f, y - 10, u, d), a(y + 10, c, u, d);
          return;
        }
      let m = Hc(t, (y) => y.from >= u.from && y.to <= u.to && Math.abs(y.from - f) < r && Math.abs(y.to - c) < r && !g.some((b) => y.from < b && y.to > b));
      if (!m) {
        if (c < u.to && e && i && e.visibleRanges.some((x) => x.from <= c && x.to >= c)) {
          let x = e.moveToLineBoundary(k.cursor(c), !1, !0).head;
          x > f && (c = x);
        }
        let y = this.gapSize(u, f, c, d), b = i || y < 2e6 ? y : 2e6;
        m = new tn(f, c, y, b);
      }
      l.push(m);
    }, h = (f) => {
      if (f.length < o || f.type != ut.Text)
        return;
      let c = Wc(f.from, f.to, this.stateDeco);
      if (c.total < o)
        return;
      let u = this.scrollTarget ? this.scrollTarget.range.head : null, d, p;
      if (i) {
        let g = s / this.heightOracle.lineLength * this.heightOracle.lineHeight, m, y;
        if (u != null) {
          let b = Wi(c, u), x = ((this.visibleBottom - this.visibleTop) / 2 + g) / f.height;
          m = b - x, y = b + x;
        } else
          m = (this.visibleTop - f.top - g) / f.height, y = (this.visibleBottom - f.top + g) / f.height;
        d = Vi(c, m), p = Vi(c, y);
      } else {
        let g = c.total * this.heightOracle.charWidth, m = s * this.heightOracle.charWidth, y = 0;
        if (g > 2e6)
          for (let T of t)
            T.from >= f.from && T.from < f.to && T.size != T.displaySize && T.from * this.heightOracle.charWidth + y < this.pixelViewport.left && (y = T.size - T.displaySize);
        let b = this.pixelViewport.left + y, x = this.pixelViewport.right + y, O, S;
        if (u != null) {
          let T = Wi(c, u), C = ((x - b) / 2 + m) / g;
          O = T - C, S = T + C;
        } else
          O = (b - m) / g, S = (x + m) / g;
        d = Vi(c, O), p = Vi(c, S);
      }
      d > f.from && a(f.from, d, f, c), p < f.to && a(p, f.to, f, c);
    };
    for (let f of this.viewportLines)
      Array.isArray(f.type) ? f.type.forEach(h) : h(f);
    return l;
  }
  gapSize(t, e, i, s) {
    let r = Wi(s, i) - Wi(s, e);
    return this.heightOracle.lineWrapping ? t.height * r : s.total * this.heightOracle.charWidth * r;
  }
  updateLineGaps(t) {
    tn.same(t, this.lineGaps) || (this.lineGaps = t, this.lineGapDeco = H.set(t.map((e) => e.draw(this, this.heightOracle.lineWrapping))));
  }
  computeVisibleRanges(t) {
    let e = this.stateDeco;
    this.lineGaps.length && (e = e.concat(this.lineGapDeco));
    let i = [];
    L.spans(e, this.viewport.from, this.viewport.to, {
      span(r, o) {
        i.push({ from: r, to: o });
      },
      point() {
      }
    }, 20);
    let s = 0;
    if (i.length != this.visibleRanges.length)
      s = 12;
    else
      for (let r = 0; r < i.length && !(s & 8); r++) {
        let o = this.visibleRanges[r], l = i[r];
        (o.from != l.from || o.to != l.to) && (s |= 4, t && t.mapPos(o.from, -1) == l.from && t.mapPos(o.to, 1) == l.to || (s |= 8));
      }
    return this.visibleRanges = i, s;
  }
  lineBlockAt(t) {
    return t >= this.viewport.from && t <= this.viewport.to && this.viewportLines.find((e) => e.from <= t && e.to >= t) || si(this.heightMap.lineAt(t, z.ByPos, this.heightOracle, 0, 0), this.scaler);
  }
  lineBlockAtHeight(t) {
    return t >= this.viewportLines[0].top && t <= this.viewportLines[this.viewportLines.length - 1].bottom && this.viewportLines.find((e) => e.top <= t && e.bottom >= t) || si(this.heightMap.lineAt(this.scaler.fromDOM(t), z.ByHeight, this.heightOracle, 0, 0), this.scaler);
  }
  getScrollOffset() {
    return (this.scrollParent == this.view.scrollDOM ? this.scrollParent.scrollTop : (this.scrollParent ? this.scrollParent.getBoundingClientRect().top : 0) - this.view.contentDOM.getBoundingClientRect().top) * this.scaleY;
  }
  scrollAnchorAt(t) {
    let e = this.lineBlockAtHeight(t + 8);
    return e.from >= this.viewport.from || this.viewportLines[0].top - t > 200 ? e : this.viewportLines[0];
  }
  elementAtHeight(t) {
    return si(this.heightMap.blockAt(this.scaler.fromDOM(t), this.heightOracle, 0, 0), this.scaler);
  }
  get docHeight() {
    return this.scaler.toDOM(this.heightMap.height);
  }
  get contentHeight() {
    return this.docHeight + this.paddingTop + this.paddingBottom;
  }
}
class Ni {
  constructor(t, e) {
    this.from = t, this.to = e;
  }
}
function Wc(n, t, e) {
  let i = [], s = n, r = 0;
  return L.spans(e, n, t, {
    span() {
    },
    point(o, l) {
      o > s && (i.push({ from: s, to: o }), r += o - s), s = l;
    }
  }, 20), s < t && (i.push({ from: s, to: t }), r += t - s), { total: r, ranges: i };
}
function Vi({ total: n, ranges: t }, e) {
  if (e <= 0)
    return t[0].from;
  if (e >= 1)
    return t[t.length - 1].to;
  let i = Math.floor(n * e);
  for (let s = 0; ; s++) {
    let { from: r, to: o } = t[s], l = o - r;
    if (i <= l)
      return r + i;
    i -= l;
  }
}
function Wi(n, t) {
  let e = 0;
  for (let { from: i, to: s } of n.ranges) {
    if (t <= s) {
      e += t - i;
      break;
    }
    e += s - i;
  }
  return e / n.total;
}
function Hc(n, t) {
  for (let e of n)
    if (t(e))
      return e;
}
const wo = {
  toDOM(n) {
    return n;
  },
  fromDOM(n) {
    return n;
  },
  scale: 1,
  eq(n) {
    return n == this;
  }
};
function ko(n) {
  let t = n.facet(Es).filter((i) => typeof i != "function"), e = n.facet(gr).filter((i) => typeof i != "function");
  return e.length && t.push(L.join(e)), t;
}
class xr {
  constructor(t, e, i) {
    let s = 0, r = 0, o = 0;
    this.viewports = i.map(({ from: l, to: a }) => {
      let h = e.lineAt(l, z.ByPos, t, 0, 0).top, f = e.lineAt(a, z.ByPos, t, 0, 0).bottom;
      return s += f - h, { from: l, to: a, top: h, bottom: f, domTop: 0, domBottom: 0 };
    }), this.scale = (7e6 - s) / (e.height - s);
    for (let l of this.viewports)
      l.domTop = o + (l.top - r) * this.scale, o = l.domBottom = l.domTop + (l.bottom - l.top), r = l.bottom;
  }
  toDOM(t) {
    for (let e = 0, i = 0, s = 0; ; e++) {
      let r = e < this.viewports.length ? this.viewports[e] : null;
      if (!r || t < r.top)
        return s + (t - i) * this.scale;
      if (t <= r.bottom)
        return r.domTop + (t - r.top);
      i = r.bottom, s = r.domBottom;
    }
  }
  fromDOM(t) {
    for (let e = 0, i = 0, s = 0; ; e++) {
      let r = e < this.viewports.length ? this.viewports[e] : null;
      if (!r || t < r.domTop)
        return i + (t - s) / this.scale;
      if (t <= r.domBottom)
        return r.top + (t - r.domTop);
      i = r.bottom, s = r.domBottom;
    }
  }
  eq(t) {
    return t instanceof xr ? this.scale == t.scale && this.viewports.length == t.viewports.length && this.viewports.every((e, i) => e.from == t.viewports[i].from && e.to == t.viewports[i].to) : !1;
  }
}
function si(n, t) {
  if (t.scale == 1)
    return n;
  let e = t.toDOM(n.top), i = t.toDOM(n.bottom);
  return new Mt(n.from, n.length, e, i - e, Array.isArray(n._content) ? n._content.map((s) => si(s, t)) : n._content);
}
const Hi = /* @__PURE__ */ M.define({ combine: (n) => n.join(" ") }), $n = /* @__PURE__ */ M.define({ combine: (n) => n.indexOf(!0) > -1 }), jn = /* @__PURE__ */ ae.newName(), Ma = /* @__PURE__ */ ae.newName(), Pa = /* @__PURE__ */ ae.newName(), Da = { "&light": "." + Ma, "&dark": "." + Pa };
function Xn(n, t, e) {
  return new ae(t, {
    finish(i) {
      return /&/.test(i) ? i.replace(/&\w*/, (s) => {
        if (s == "&")
          return n;
        if (!e || !e[s])
          throw new RangeError(`Unsupported selector: ${s}`);
        return e[s];
      }) : n + " " + i;
    }
  });
}
const Fc = /* @__PURE__ */ Xn("." + jn, {
  "&": {
    position: "relative !important",
    boxSizing: "border-box",
    "&.cm-focused": {
      // Provide a simple default outline to make sure a focused
      // editor is visually distinct. Can't leave the default behavior
      // because that will apply to the content element, which is
      // inside the scrollable container and doesn't include the
      // gutters. We also can't use an 'auto' outline, since those
      // are, for some reason, drawn behind the element content, which
      // will cause things like the active line background to cover
      // the outline (#297).
      outline: "1px dotted #212121"
    },
    display: "flex !important",
    flexDirection: "column"
  },
  ".cm-scroller": {
    display: "flex !important",
    alignItems: "flex-start !important",
    fontFamily: "monospace",
    lineHeight: 1.4,
    height: "100%",
    overflowX: "auto",
    position: "relative",
    zIndex: 0,
    overflowAnchor: "none"
  },
  ".cm-content": {
    margin: 0,
    flexGrow: 2,
    flexShrink: 0,
    display: "block",
    whiteSpace: "pre",
    wordWrap: "normal",
    // https://github.com/codemirror/dev/issues/456
    boxSizing: "border-box",
    minHeight: "100%",
    padding: "4px 0",
    outline: "none",
    "&[contenteditable=true]": {
      WebkitUserModify: "read-write-plaintext-only"
    }
  },
  ".cm-lineWrapping": {
    whiteSpace_fallback: "pre-wrap",
    // For IE
    whiteSpace: "break-spaces",
    wordBreak: "break-word",
    // For Safari, which doesn't support overflow-wrap: anywhere
    overflowWrap: "anywhere",
    flexShrink: 1
  },
  "&light .cm-content": { caretColor: "black" },
  "&dark .cm-content": { caretColor: "white" },
  ".cm-line": {
    display: "block",
    padding: "0 2px 0 6px"
  },
  ".cm-layer": {
    position: "absolute",
    left: 0,
    top: 0,
    contain: "size style",
    "& > *": {
      position: "absolute"
    }
  },
  "&light .cm-selectionBackground": {
    background: "#d9d9d9"
  },
  "&dark .cm-selectionBackground": {
    background: "#222"
  },
  "&light.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground": {
    background: "#d7d4f0"
  },
  "&dark.cm-focused > .cm-scroller > .cm-selectionLayer .cm-selectionBackground": {
    background: "#233"
  },
  ".cm-cursorLayer": {
    pointerEvents: "none"
  },
  "&.cm-focused > .cm-scroller > .cm-cursorLayer": {
    animation: "steps(1) cm-blink 1.2s infinite"
  },
  // Two animations defined so that we can switch between them to
  // restart the animation without forcing another style
  // recomputation.
  "@keyframes cm-blink": { "0%": {}, "50%": { opacity: 0 }, "100%": {} },
  "@keyframes cm-blink2": { "0%": {}, "50%": { opacity: 0 }, "100%": {} },
  ".cm-cursor, .cm-dropCursor": {
    borderLeft: "1.2px solid black",
    marginLeft: "-0.6px",
    pointerEvents: "none"
  },
  ".cm-cursor": {
    display: "none"
  },
  "&dark .cm-cursor": {
    borderLeftColor: "#ddd"
  },
  ".cm-selectionHandle": {
    backgroundColor: "currentColor",
    width: "1.5px"
  },
  ".cm-selectionHandle-start::before, .cm-selectionHandle-end::before": {
    content: '""',
    backgroundColor: "inherit",
    borderRadius: "50%",
    width: "8px",
    height: "8px",
    position: "absolute",
    left: "-3.25px"
  },
  ".cm-selectionHandle-start::before": { top: "-8px" },
  ".cm-selectionHandle-end::before": { bottom: "-8px" },
  ".cm-dropCursor": {
    position: "absolute"
  },
  "&.cm-focused > .cm-scroller > .cm-cursorLayer .cm-cursor": {
    display: "block"
  },
  ".cm-iso": {
    unicodeBidi: "isolate"
  },
  ".cm-announced": {
    position: "fixed",
    top: "-10000px"
  },
  "@media print": {
    ".cm-announced": { display: "none" }
  },
  "&light .cm-activeLine": { backgroundColor: "#cceeff44" },
  "&dark .cm-activeLine": { backgroundColor: "#99eeff33" },
  "&light .cm-specialChar": { color: "red" },
  "&dark .cm-specialChar": { color: "#f78" },
  ".cm-gutters": {
    flexShrink: 0,
    display: "flex",
    height: "100%",
    boxSizing: "border-box",
    zIndex: 200
  },
  ".cm-gutters-before": { insetInlineStart: 0 },
  ".cm-gutters-after": { insetInlineEnd: 0 },
  "&light .cm-gutters": {
    backgroundColor: "#f5f5f5",
    color: "#6c6c6c",
    border: "0px solid #ddd",
    "&.cm-gutters-before": { borderRightWidth: "1px" },
    "&.cm-gutters-after": { borderLeftWidth: "1px" }
  },
  "&dark .cm-gutters": {
    backgroundColor: "#333338",
    color: "#ccc"
  },
  ".cm-gutter": {
    display: "flex !important",
    // Necessary -- prevents margin collapsing
    flexDirection: "column",
    flexShrink: 0,
    boxSizing: "border-box",
    minHeight: "100%",
    overflow: "hidden"
  },
  ".cm-gutterElement": {
    boxSizing: "border-box"
  },
  ".cm-lineNumbers .cm-gutterElement": {
    padding: "0 3px 0 5px",
    minWidth: "20px",
    textAlign: "right",
    whiteSpace: "nowrap"
  },
  "&light .cm-activeLineGutter": {
    backgroundColor: "#e2f2ff"
  },
  "&dark .cm-activeLineGutter": {
    backgroundColor: "#222227"
  },
  ".cm-panels": {
    boxSizing: "border-box",
    position: "sticky",
    left: 0,
    right: 0,
    zIndex: 300
  },
  "&light .cm-panels": {
    backgroundColor: "#f5f5f5",
    color: "black"
  },
  "&light .cm-panels-top": {
    borderBottom: "1px solid #ddd"
  },
  "&light .cm-panels-bottom": {
    borderTop: "1px solid #ddd"
  },
  "&dark .cm-panels": {
    backgroundColor: "#333338",
    color: "white"
  },
  ".cm-dialog": {
    padding: "2px 19px 4px 6px",
    position: "relative",
    "& label": { fontSize: "80%" }
  },
  ".cm-dialog-close": {
    position: "absolute",
    top: "3px",
    right: "4px",
    backgroundColor: "inherit",
    border: "none",
    font: "inherit",
    fontSize: "14px",
    padding: "0"
  },
  ".cm-tab": {
    display: "inline-block",
    overflow: "hidden",
    verticalAlign: "bottom"
  },
  ".cm-widgetBuffer": {
    verticalAlign: "text-top",
    height: "1em",
    width: 0,
    display: "inline"
  },
  ".cm-placeholder": {
    color: "#888",
    display: "inline-block",
    verticalAlign: "top",
    userSelect: "none"
  },
  ".cm-highlightSpace": {
    backgroundImage: "radial-gradient(circle at 50% 55%, #aaa 20%, transparent 5%)",
    backgroundPosition: "center"
  },
  ".cm-highlightTab": {
    backgroundImage: `url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="200" height="20"><path stroke="%23888" stroke-width="1" fill="none" d="M1 10H196L190 5M190 15L196 10M197 4L197 16"/></svg>')`,
    backgroundSize: "auto 100%",
    backgroundPosition: "right 90%",
    backgroundRepeat: "no-repeat"
  },
  ".cm-trailingSpace": {
    backgroundColor: "#ff332255"
  },
  ".cm-button": {
    verticalAlign: "middle",
    color: "inherit",
    fontSize: "70%",
    padding: ".2em 1em",
    borderRadius: "1px"
  },
  "&light .cm-button": {
    backgroundImage: "linear-gradient(#eff1f5, #d9d9df)",
    border: "1px solid #888",
    "&:active": {
      backgroundImage: "linear-gradient(#b4b4b4, #d0d3d6)"
    }
  },
  "&dark .cm-button": {
    backgroundImage: "linear-gradient(#393939, #111)",
    border: "1px solid #888",
    "&:active": {
      backgroundImage: "linear-gradient(#111, #333)"
    }
  },
  ".cm-textfield": {
    verticalAlign: "middle",
    color: "inherit",
    fontSize: "70%",
    border: "1px solid silver",
    padding: ".2em .5em"
  },
  "&light .cm-textfield": {
    backgroundColor: "white"
  },
  "&dark .cm-textfield": {
    border: "1px solid #555",
    backgroundColor: "inherit"
  }
}, Da), zc = {
  childList: !0,
  characterData: !0,
  subtree: !0,
  attributes: !0,
  characterDataOldValue: !0
}, en = A.ie && A.ie_version <= 11;
class Qc {
  constructor(t) {
    this.view = t, this.active = !1, this.editContext = null, this.selectionRange = new mf(), this.selectionChanged = !1, this.delayedFlush = -1, this.resizeTimeout = -1, this.queue = [], this.delayedAndroidKey = null, this.flushingAndroidKey = -1, this.lastChange = 0, this.scrollTargets = [], this.intersection = null, this.resizeScroll = null, this.intersecting = !1, this.gapIntersection = null, this.gaps = [], this.printQuery = null, this.parentCheck = -1, this.dom = t.contentDOM, this.observer = new MutationObserver((e) => {
      for (let i of e)
        this.queue.push(i);
      (A.ie && A.ie_version <= 11 || A.ios && t.composing) && e.some((i) => i.type == "childList" && i.removedNodes.length || i.type == "characterData" && i.oldValue.length > i.target.nodeValue.length) ? this.flushSoon() : this.flush();
    }), window.EditContext && A.android && t.constructor.EDIT_CONTEXT !== !1 && // Chrome <126 doesn't support inverted selections in edit context (#1392)
    !(A.chrome && A.chrome_version < 126) && (this.editContext = new jc(t), t.state.facet(Gt) && (t.contentDOM.editContext = this.editContext.editContext)), en && (this.onCharData = (e) => {
      this.queue.push({
        target: e.target,
        type: "characterData",
        oldValue: e.prevValue
      }), this.flushSoon();
    }), this.onSelectionChange = this.onSelectionChange.bind(this), this.onResize = this.onResize.bind(this), this.onPrint = this.onPrint.bind(this), this.onScroll = this.onScroll.bind(this), window.matchMedia && (this.printQuery = window.matchMedia("print")), typeof ResizeObserver == "function" && (this.resizeScroll = new ResizeObserver(() => {
      var e;
      ((e = this.view.docView) === null || e === void 0 ? void 0 : e.lastUpdate) < Date.now() - 75 && this.onResize();
    }), this.resizeScroll.observe(t.scrollDOM)), this.addWindowListeners(this.win = t.win), this.start(), typeof IntersectionObserver == "function" && (this.intersection = new IntersectionObserver((e) => {
      this.parentCheck < 0 && (this.parentCheck = setTimeout(this.listenForScroll.bind(this), 1e3)), e.length > 0 && e[e.length - 1].intersectionRatio > 0 != this.intersecting && (this.intersecting = !this.intersecting, this.intersecting != this.view.inView && this.onScrollChanged(document.createEvent("Event")));
    }, { threshold: [0, 1e-3] }), this.intersection.observe(this.dom), this.gapIntersection = new IntersectionObserver((e) => {
      e.length > 0 && e[e.length - 1].intersectionRatio > 0 && this.onScrollChanged(document.createEvent("Event"));
    }, {})), this.listenForScroll(), this.readSelectionRange();
  }
  onScrollChanged(t) {
    this.view.inputState.runHandlers("scroll", t), this.intersecting && this.view.measure();
  }
  onScroll(t) {
    this.intersecting && this.flush(!1), this.editContext && this.view.requestMeasure(this.editContext.measureReq), this.onScrollChanged(t);
  }
  onResize() {
    this.resizeTimeout < 0 && (this.resizeTimeout = setTimeout(() => {
      this.resizeTimeout = -1, this.view.requestMeasure();
    }, 50));
  }
  onPrint(t) {
    (t.type == "change" || !t.type) && !t.matches || (this.view.viewState.printing = !0, this.view.measure(), setTimeout(() => {
      this.view.viewState.printing = !1, this.view.requestMeasure();
    }, 500));
  }
  updateGaps(t) {
    if (this.gapIntersection && (t.length != this.gaps.length || this.gaps.some((e, i) => e != t[i]))) {
      this.gapIntersection.disconnect();
      for (let e of t)
        this.gapIntersection.observe(e);
      this.gaps = t;
    }
  }
  onSelectionChange(t) {
    let e = this.selectionChanged;
    if (!this.readSelectionRange() || this.delayedAndroidKey)
      return;
    let { view: i } = this, s = this.selectionRange;
    if (i.state.facet(Gt) ? i.root.activeElement != this.dom : !oi(this.dom, s))
      return;
    let r = s.anchorNode && i.docView.tile.nearest(s.anchorNode);
    if (r && r.isWidget() && r.widget.ignoreEvent(t)) {
      e || (this.selectionChanged = !1);
      return;
    }
    (A.ie && A.ie_version <= 11 || A.android && A.chrome) && !i.state.selection.main.empty && // (Selection.isCollapsed isn't reliable on IE)
    s.focusNode && li(s.focusNode, s.focusOffset, s.anchorNode, s.anchorOffset) ? this.flushSoon() : this.flush(!1);
  }
  readSelectionRange() {
    let { view: t } = this, e = pi(t.root);
    if (!e)
      return !1;
    let i = A.safari && t.root.nodeType == 11 && t.root.activeElement == this.dom && $c(this.view, e) || e;
    if (!i || this.selectionRange.eq(i))
      return !1;
    let s = oi(this.dom, i);
    return s && !this.selectionChanged && t.inputState.lastFocusTime > Date.now() - 200 && t.inputState.lastTouchTime < Date.now() - 300 && bf(this.dom, i) ? (this.view.inputState.lastFocusTime = 0, t.docView.updateSelection(), !1) : (this.selectionRange.setRange(i), s && (this.selectionChanged = !0), !0);
  }
  setSelectionRange(t, e) {
    this.selectionRange.set(t.node, t.offset, e.node, e.offset), this.selectionChanged = !1;
  }
  clearSelectionRange() {
    this.selectionRange.set(null, 0, null, 0);
  }
  listenForScroll() {
    this.parentCheck = -1;
    let t = 0, e = null;
    for (let i = this.dom; i; )
      if (i.nodeType == 1)
        !e && t < this.scrollTargets.length && this.scrollTargets[t] == i ? t++ : e || (e = this.scrollTargets.slice(0, t)), e && e.push(i), i = i.assignedSlot || i.parentNode;
      else if (i.nodeType == 11)
        i = i.host;
      else
        break;
    if (t < this.scrollTargets.length && !e && (e = this.scrollTargets.slice(0, t)), e) {
      for (let i of this.scrollTargets)
        i.removeEventListener("scroll", this.onScroll);
      for (let i of this.scrollTargets = e)
        i.addEventListener("scroll", this.onScroll);
    }
  }
  ignore(t) {
    if (!this.active)
      return t();
    try {
      return this.stop(), t();
    } finally {
      this.start(), this.clear();
    }
  }
  start() {
    this.active || (this.observer.observe(this.dom, zc), en && this.dom.addEventListener("DOMCharacterDataModified", this.onCharData), this.active = !0);
  }
  stop() {
    this.active && (this.active = !1, this.observer.disconnect(), en && this.dom.removeEventListener("DOMCharacterDataModified", this.onCharData));
  }
  // Throw away any pending changes
  clear() {
    this.processRecords(), this.queue.length = 0, this.selectionChanged = !1;
  }
  // Chrome Android, especially in combination with GBoard, not only
  // doesn't reliably fire regular key events, but also often
  // surrounds the effect of enter or backspace with a bunch of
  // composition events that, when interrupted, cause text duplication
  // or other kinds of corruption. This hack makes the editor back off
  // from handling DOM changes for a moment when such a key is
  // detected (via beforeinput or keydown), and then tries to flush
  // them or, if that has no effect, dispatches the given key.
  delayAndroidKey(t, e) {
    var i;
    if (!this.delayedAndroidKey) {
      let s = () => {
        let r = this.delayedAndroidKey;
        r && (this.clearDelayedAndroidKey(), this.view.inputState.lastKeyCode = r.keyCode, this.view.inputState.lastKeyTime = Date.now(), !this.flush() && r.force && Ne(this.dom, r.key, r.keyCode));
      };
      this.flushingAndroidKey = this.view.win.requestAnimationFrame(s);
    }
    (!this.delayedAndroidKey || t == "Enter") && (this.delayedAndroidKey = {
      key: t,
      keyCode: e,
      // Only run the key handler when no changes are detected if
      // this isn't coming right after another change, in which case
      // it is probably part of a weird chain of updates, and should
      // be ignored if it returns the DOM to its previous state.
      force: this.lastChange < Date.now() - 50 || !!(!((i = this.delayedAndroidKey) === null || i === void 0) && i.force)
    });
  }
  clearDelayedAndroidKey() {
    this.win.cancelAnimationFrame(this.flushingAndroidKey), this.delayedAndroidKey = null, this.flushingAndroidKey = -1;
  }
  flushSoon() {
    this.delayedFlush < 0 && (this.delayedFlush = this.view.win.requestAnimationFrame(() => {
      this.delayedFlush = -1, this.flush();
    }));
  }
  forceFlush() {
    this.delayedFlush >= 0 && (this.view.win.cancelAnimationFrame(this.delayedFlush), this.delayedFlush = -1), this.flush();
  }
  pendingRecords() {
    for (let t of this.observer.takeRecords())
      this.queue.push(t);
    return this.queue;
  }
  processRecords() {
    let t = this.pendingRecords();
    t.length && (this.queue = []);
    let e = -1, i = -1, s = !1;
    for (let r of t) {
      let o = this.readMutation(r);
      o && (o.typeOver && (s = !0), e == -1 ? { from: e, to: i } = o : (e = Math.min(o.from, e), i = Math.max(o.to, i)));
    }
    return { from: e, to: i, typeOver: s };
  }
  readChange() {
    let { from: t, to: e, typeOver: i } = this.processRecords(), s = this.selectionChanged && oi(this.dom, this.selectionRange);
    if (t < 0 && !s)
      return null;
    t > -1 && (this.lastChange = Date.now()), this.view.inputState.lastFocusTime = 0, this.selectionChanged = !1;
    let r = new lc(this.view, t, e, i);
    return this.view.docView.domChanged = { newSel: r.newSel ? r.newSel.main : null }, r;
  }
  // Apply pending changes, if any
  flush(t = !0) {
    if (this.delayedFlush >= 0 || this.delayedAndroidKey)
      return !1;
    t && this.readSelectionRange();
    let e = this.readChange();
    if (!e)
      return this.view.requestMeasure(), !1;
    let i = this.view.state, s = ya(this.view, e);
    return this.view.state == i && (e.domChanged || e.newSel && !gs(this.view.state.selection, e.newSel.main)) && this.view.update([]), s;
  }
  readMutation(t) {
    let e = this.view.docView.tile.nearest(t.target);
    if (!e || e.isWidget())
      return null;
    if (e.markDirty(t.type == "attributes"), t.type == "childList") {
      let i = vo(e, t.previousSibling || t.target.previousSibling, -1), s = vo(e, t.nextSibling || t.target.nextSibling, 1);
      return {
        from: i ? e.posAfter(i) : e.posAtStart,
        to: s ? e.posBefore(s) : e.posAtEnd,
        typeOver: !1
      };
    } else return t.type == "characterData" ? { from: e.posAtStart, to: e.posAtEnd, typeOver: t.target.nodeValue == t.oldValue } : null;
  }
  setWindow(t) {
    t != this.win && (this.removeWindowListeners(this.win), this.win = t, this.addWindowListeners(this.win));
  }
  addWindowListeners(t) {
    t.addEventListener("resize", this.onResize), this.printQuery ? this.printQuery.addEventListener ? this.printQuery.addEventListener("change", this.onPrint) : this.printQuery.addListener(this.onPrint) : t.addEventListener("beforeprint", this.onPrint), t.addEventListener("scroll", this.onScroll), t.document.addEventListener("selectionchange", this.onSelectionChange);
  }
  removeWindowListeners(t) {
    t.removeEventListener("scroll", this.onScroll), t.removeEventListener("resize", this.onResize), this.printQuery ? this.printQuery.removeEventListener ? this.printQuery.removeEventListener("change", this.onPrint) : this.printQuery.removeListener(this.onPrint) : t.removeEventListener("beforeprint", this.onPrint), t.document.removeEventListener("selectionchange", this.onSelectionChange);
  }
  update(t) {
    this.editContext && (this.editContext.update(t), t.startState.facet(Gt) != t.state.facet(Gt) && (t.view.contentDOM.editContext = t.state.facet(Gt) ? this.editContext.editContext : null));
  }
  destroy() {
    var t, e, i;
    this.stop(), (t = this.intersection) === null || t === void 0 || t.disconnect(), (e = this.gapIntersection) === null || e === void 0 || e.disconnect(), (i = this.resizeScroll) === null || i === void 0 || i.disconnect();
    for (let s of this.scrollTargets)
      s.removeEventListener("scroll", this.onScroll);
    this.removeWindowListeners(this.win), clearTimeout(this.parentCheck), clearTimeout(this.resizeTimeout), this.win.cancelAnimationFrame(this.delayedFlush), this.win.cancelAnimationFrame(this.flushingAndroidKey), this.editContext && (this.view.contentDOM.editContext = null, this.editContext.destroy());
  }
}
function vo(n, t, e) {
  for (; t; ) {
    let i = U.get(t);
    if (i && i.parent == n)
      return i;
    let s = t.parentNode;
    t = s != n.dom ? s : e > 0 ? t.nextSibling : t.previousSibling;
  }
  return null;
}
function So(n, t) {
  let e = t.startContainer, i = t.startOffset, s = t.endContainer, r = t.endOffset, o = n.docView.domAtPos(n.state.selection.main.anchor, 1);
  return li(o.node, o.offset, s, r) && ([e, i, s, r] = [s, r, e, i]), { anchorNode: e, anchorOffset: i, focusNode: s, focusOffset: r };
}
function $c(n, t) {
  if (t.getComposedRanges) {
    let s = t.getComposedRanges(n.root)[0];
    if (s)
      return So(n, s);
  }
  let e = null;
  function i(s) {
    s.preventDefault(), s.stopImmediatePropagation(), e = s.getTargetRanges()[0];
  }
  return n.contentDOM.addEventListener("beforeinput", i, !0), n.dom.ownerDocument.execCommand("indent"), n.contentDOM.removeEventListener("beforeinput", i, !0), e ? So(n, e) : null;
}
class jc {
  constructor(t) {
    this.from = 0, this.to = 0, this.pendingContextChange = null, this.handlers = /* @__PURE__ */ Object.create(null), this.composing = null, this.resetRange(t.state);
    let e = this.editContext = new window.EditContext({
      text: t.state.doc.sliceString(this.from, this.to),
      selectionStart: this.toContextPos(Math.max(this.from, Math.min(this.to, t.state.selection.main.anchor))),
      selectionEnd: this.toContextPos(t.state.selection.main.head)
    });
    this.handlers.textupdate = (i) => {
      let s = t.state.selection.main, { anchor: r, head: o } = s, l = this.toEditorPos(i.updateRangeStart), a = this.toEditorPos(i.updateRangeEnd);
      t.inputState.composing >= 0 && !this.composing && (this.composing = { contextBase: i.updateRangeStart, editorBase: l, drifted: !1 });
      let h = a - l > i.text.length;
      l == this.from && r < this.from ? l = r : a == this.to && r > this.to && (a = r);
      let f = ba(t.state.sliceDoc(l, a), i.text, (h ? s.from : s.to) - l, h ? "end" : null);
      if (!f) {
        let u = k.single(this.toEditorPos(i.selectionStart), this.toEditorPos(i.selectionEnd));
        gs(u, s) || t.dispatch({ selection: u, userEvent: "select" });
        return;
      }
      let c = {
        from: f.from + l,
        to: f.toA + l,
        insert: I.of(i.text.slice(f.from, f.toB).split(`
`))
      };
      if ((A.mac || A.android) && c.from == o - 1 && /^\. ?$/.test(i.text) && t.contentDOM.getAttribute("autocorrect") == "off" && (c = { from: l, to: a, insert: I.of([i.text.replace(".", " ")]) }), this.pendingContextChange = c, !t.state.readOnly) {
        let u = this.to - this.from + (c.to - c.from + c.insert.length);
        yr(t, c, k.single(this.toEditorPos(i.selectionStart, u), this.toEditorPos(i.selectionEnd, u)));
      }
      this.pendingContextChange && (this.revertPending(t.state), this.setSelection(t.state)), c.from < c.to && !c.insert.length && t.inputState.composing >= 0 && !/[\\p{Alphabetic}\\p{Number}_]/.test(e.text.slice(Math.max(0, i.updateRangeStart - 1), Math.min(e.text.length, i.updateRangeStart + 1))) && this.handlers.compositionend(i);
    }, this.handlers.characterboundsupdate = (i) => {
      let s = [], r = null;
      for (let o = this.toEditorPos(i.rangeStart), l = this.toEditorPos(i.rangeEnd); o < l; o++) {
        let a = t.coordsForChar(o);
        r = a && new DOMRect(a.left, a.top, a.right - a.left, a.bottom - a.top) || r || new DOMRect(), s.push(r);
      }
      e.updateCharacterBounds(i.rangeStart, s);
    }, this.handlers.textformatupdate = (i) => {
      let s = [];
      for (let r of i.getTextFormats()) {
        let o = r.underlineStyle, l = r.underlineThickness;
        if (!/none/i.test(o) && !/none/i.test(l)) {
          let a = this.toEditorPos(r.rangeStart), h = this.toEditorPos(r.rangeEnd);
          if (a < h) {
            let f = `text-decoration: underline ${/^[a-z]/.test(o) ? o + " " : o == "Dashed" ? "dashed " : o == "Squiggle" ? "wavy " : ""}${/thin/i.test(l) ? 1 : 2}px`;
            s.push(H.mark({ attributes: { style: f } }).range(a, h));
          }
        }
      }
      t.dispatch({ effects: aa.of(H.set(s)) });
    }, this.handlers.compositionstart = () => {
      t.inputState.composing < 0 && (t.inputState.composing = 0, t.inputState.compositionFirstChange = !0);
    }, this.handlers.compositionend = () => {
      if (t.inputState.composing = -1, t.inputState.compositionFirstChange = null, this.composing) {
        let { drifted: i } = this.composing;
        this.composing = null, i && this.reset(t.state);
      }
    };
    for (let i in this.handlers)
      e.addEventListener(i, this.handlers[i]);
    this.measureReq = { read: (i) => {
      this.editContext.updateControlBounds(i.contentDOM.getBoundingClientRect());
      let s = pi(i.root);
      s && s.rangeCount && this.editContext.updateSelectionBounds(s.getRangeAt(0).getBoundingClientRect());
    } };
  }
  applyEdits(t) {
    let e = 0, i = !1, s = this.pendingContextChange;
    return t.changes.iterChanges((r, o, l, a, h) => {
      if (i)
        return;
      let f = h.length - (o - r);
      if (s && o >= s.to)
        if (s.from == r && s.to == o && s.insert.eq(h)) {
          s = this.pendingContextChange = null, e += f, this.to += f;
          return;
        } else
          s = null, this.revertPending(t.state);
      if (r += e, o += e, o <= this.from)
        this.from += f, this.to += f;
      else if (r < this.to) {
        if (r < this.from || o > this.to || this.to - this.from + h.length > 3e4) {
          i = !0;
          return;
        }
        this.editContext.updateText(this.toContextPos(r), this.toContextPos(o), h.toString()), this.to += f;
      }
      e += f;
    }), s && !i && this.revertPending(t.state), !i;
  }
  update(t) {
    let e = this.pendingContextChange, i = t.startState.selection.main;
    this.composing && (this.composing.drifted || !t.changes.touchesRange(i.from, i.to) && t.transactions.some((s) => !s.isUserEvent("input.type") && s.changes.touchesRange(this.from, this.to))) ? (this.composing.drifted = !0, this.composing.editorBase = t.changes.mapPos(this.composing.editorBase)) : !this.applyEdits(t) || !this.rangeIsValid(t.state) ? (this.pendingContextChange = null, this.reset(t.state)) : (t.docChanged || t.selectionSet || e) && this.setSelection(t.state), (t.geometryChanged || t.docChanged || t.selectionSet) && t.view.requestMeasure(this.measureReq);
  }
  resetRange(t) {
    let { head: e } = t.selection.main;
    this.from = Math.max(
      0,
      e - 1e4
      /* CxVp.Margin */
    ), this.to = Math.min(
      t.doc.length,
      e + 1e4
      /* CxVp.Margin */
    );
  }
  reset(t) {
    this.resetRange(t), this.editContext.updateText(0, this.editContext.text.length, t.doc.sliceString(this.from, this.to)), this.setSelection(t);
  }
  revertPending(t) {
    let e = this.pendingContextChange;
    this.pendingContextChange = null, this.editContext.updateText(this.toContextPos(e.from), this.toContextPos(e.from + e.insert.length), t.doc.sliceString(e.from, e.to));
  }
  setSelection(t) {
    let { main: e } = t.selection, i = this.toContextPos(Math.max(this.from, Math.min(this.to, e.anchor))), s = this.toContextPos(e.head);
    (this.editContext.selectionStart != i || this.editContext.selectionEnd != s) && this.editContext.updateSelection(i, s);
  }
  rangeIsValid(t) {
    let { head: e } = t.selection.main;
    return !(this.from > 0 && e - this.from < 500 || this.to < t.doc.length && this.to - e < 500 || this.to - this.from > 1e4 * 3);
  }
  toEditorPos(t, e = this.to - this.from) {
    t = Math.min(t, e);
    let i = this.composing;
    return i && i.drifted ? i.editorBase + (t - i.contextBase) : t + this.from;
  }
  toContextPos(t) {
    let e = this.composing;
    return e && e.drifted ? e.contextBase + (t - e.editorBase) : t - this.from;
  }
  destroy() {
    for (let t in this.handlers)
      this.editContext.removeEventListener(t, this.handlers[t]);
  }
}
class P {
  /**
  The current editor state.
  */
  get state() {
    return this.viewState.state;
  }
  /**
  To be able to display large documents without consuming too much
  memory or overloading the browser, CodeMirror only draws the
  code that is visible (plus a margin around it) to the DOM. This
  property tells you the extent of the current drawn viewport, in
  document positions.
  */
  get viewport() {
    return this.viewState.viewport;
  }
  /**
  When there are, for example, large collapsed ranges in the
  viewport, its size can be a lot bigger than the actual visible
  content. Thus, if you are doing something like styling the
  content in the viewport, it is preferable to only do so for
  these ranges, which are the subset of the viewport that is
  actually drawn.
  */
  get visibleRanges() {
    return this.viewState.visibleRanges;
  }
  /**
  Returns false when the editor is entirely scrolled out of view
  or otherwise hidden.
  */
  get inView() {
    return this.viewState.inView;
  }
  /**
  Indicates whether the user is currently composing text via
  [IME](https://en.wikipedia.org/wiki/Input_method), and at least
  one change has been made in the current composition.
  */
  get composing() {
    return !!this.inputState && this.inputState.composing > 0;
  }
  /**
  Indicates whether the user is currently in composing state. Note
  that on some platforms, like Android, this will be the case a
  lot, since just putting the cursor on a word starts a
  composition there.
  */
  get compositionStarted() {
    return !!this.inputState && this.inputState.composing >= 0;
  }
  /**
  The document or shadow root that the view lives in.
  */
  get root() {
    return this._root;
  }
  /**
  @internal
  */
  get win() {
    return this.dom.ownerDocument.defaultView || window;
  }
  /**
  Construct a new view. You'll want to either provide a `parent`
  option, or put `view.dom` into your document after creating a
  view, so that the user can see the editor.
  */
  constructor(t = {}) {
    var e;
    this.plugins = [], this.pluginMap = /* @__PURE__ */ new Map(), this.editorAttrs = {}, this.contentAttrs = {}, this.bidiCache = [], this.destroyed = !1, this.updateState = 2, this.measureScheduled = -1, this.measureRequests = [], this.contentDOM = document.createElement("div"), this.scrollDOM = document.createElement("div"), this.scrollDOM.tabIndex = -1, this.scrollDOM.className = "cm-scroller", this.scrollDOM.appendChild(this.contentDOM), this.announceDOM = document.createElement("div"), this.announceDOM.className = "cm-announced", this.announceDOM.setAttribute("aria-live", "polite"), this.dom = document.createElement("div"), this.dom.appendChild(this.announceDOM), this.dom.appendChild(this.scrollDOM), t.parent && t.parent.appendChild(this.dom);
    let { dispatch: i } = t;
    this.dispatchTransactions = t.dispatchTransactions || i && ((s) => s.forEach((r) => i(r, this))) || ((s) => this.update(s)), this.dispatch = this.dispatch.bind(this), this._root = t.root || yf(t.parent) || document, this.viewState = new xo(this, t.state || V.create(t)), t.scrollTo && t.scrollTo.is(Li) && (this.viewState.scrollTarget = t.scrollTo.value.clip(this.viewState.state)), this.plugins = this.state.facet(De).map((s) => new _s(s));
    for (let s of this.plugins)
      s.update(this);
    this.observer = new Qc(this), this.inputState = new cc(this), this.inputState.ensureHandlers(this.plugins), this.docView = new ro(this), this.mountStyles(), this.updateAttrs(), this.updateState = 0, this.requestMeasure(), !((e = document.fonts) === null || e === void 0) && e.ready && document.fonts.ready.then(() => {
      this.viewState.mustMeasureContent = "refresh", this.requestMeasure();
    });
  }
  dispatch(...t) {
    let e = t.length == 1 && t[0] instanceof Y ? t : t.length == 1 && Array.isArray(t[0]) ? t[0] : [this.state.update(...t)];
    this.dispatchTransactions(e, this);
  }
  /**
  Update the view for the given array of transactions. This will
  update the visible document and selection to match the state
  produced by the transactions, and notify view plugins of the
  change. You should usually call
  [`dispatch`](https://codemirror.net/6/docs/ref/#view.EditorView.dispatch) instead, which uses this
  as a primitive.
  */
  update(t) {
    if (this.updateState != 0)
      throw new Error("Calls to EditorView.update are not allowed while an update is in progress");
    let e = !1, i = !1, s, r = this.state;
    for (let u of t) {
      if (u.startState != r)
        throw new RangeError("Trying to update state with a transaction that doesn't start from the previous state.");
      r = u.state;
    }
    if (this.destroyed) {
      this.viewState.state = r;
      return;
    }
    let o = this.hasFocus, l = 0, a = null;
    t.some((u) => u.annotation(Oa)) ? (this.inputState.notifiedFocused = o, l = 1) : o != this.inputState.notifiedFocused && (this.inputState.notifiedFocused = o, a = Ca(r, o), a || (l = 1));
    let h = this.observer.delayedAndroidKey, f = null;
    if (h ? (this.observer.clearDelayedAndroidKey(), f = this.observer.readChange(), (f && !this.state.doc.eq(r.doc) || !this.state.selection.eq(r.selection)) && (f = null)) : this.observer.clear(), r.facet(V.phrases) != this.state.facet(V.phrases))
      return this.setState(r);
    s = us.create(this, r, t), s.flags |= l;
    let c = this.viewState.scrollTarget;
    try {
      this.updateState = 2;
      for (let u of t) {
        if (c && (c = c.map(u.changes)), u.scrollIntoView) {
          let { main: d } = u.state.selection;
          c = new Ve(d.empty ? d : k.cursor(d.head, d.head > d.anchor ? -1 : 1));
        }
        for (let d of u.effects)
          d.is(Li) && (c = d.value.clip(this.state));
      }
      this.viewState.update(s, c), this.bidiCache = ys.update(this.bidiCache, s.changes), s.empty || (this.updatePlugins(s), this.inputState.update(s)), e = this.docView.update(s), this.state.facet(ii) != this.styleModules && this.mountStyles(), i = this.updateAttrs(), this.showAnnouncements(t), this.docView.updateSelection(e, t.some((u) => u.isUserEvent("select.pointer")));
    } finally {
      this.updateState = 0;
    }
    if (s.startState.facet(Hi) != s.state.facet(Hi) && (this.viewState.mustMeasureContent = !0), (e || i || c || this.viewState.mustEnforceCursorAssoc || this.viewState.mustMeasureContent) && this.requestMeasure(), e && this.docViewUpdate(), !s.empty)
      for (let u of this.state.facet(Hn))
        try {
          u(s);
        } catch (d) {
          ft(this.state, d, "update listener");
        }
    (a || f) && Promise.resolve().then(() => {
      a && this.state == a.startState && this.dispatch(a), f && !ya(this, f) && h.force && Ne(this.contentDOM, h.key, h.keyCode);
    });
  }
  /**
  Reset the view to the given state. (This will cause the entire
  document to be redrawn and all view plugins to be reinitialized,
  so you should probably only use it when the new state isn't
  derived from the old state. Otherwise, use
  [`dispatch`](https://codemirror.net/6/docs/ref/#view.EditorView.dispatch) instead.)
  */
  setState(t) {
    if (this.updateState != 0)
      throw new Error("Calls to EditorView.setState are not allowed while an update is in progress");
    if (this.destroyed) {
      this.viewState.state = t;
      return;
    }
    this.updateState = 2;
    let e = this.hasFocus;
    try {
      for (let i of this.plugins)
        i.destroy(this);
      this.viewState = new xo(this, t), this.plugins = t.facet(De).map((i) => new _s(i)), this.pluginMap.clear();
      for (let i of this.plugins)
        i.update(this);
      this.docView.destroy(), this.docView = new ro(this), this.inputState.ensureHandlers(this.plugins), this.mountStyles(), this.updateAttrs(), this.bidiCache = [];
    } finally {
      this.updateState = 0;
    }
    e && this.focus(), this.requestMeasure();
  }
  updatePlugins(t) {
    let e = t.startState.facet(De), i = t.state.facet(De);
    if (e != i) {
      let s = [];
      for (let r of i) {
        let o = e.indexOf(r);
        if (o < 0)
          s.push(new _s(r));
        else {
          let l = this.plugins[o];
          l.mustUpdate = t, s.push(l);
        }
      }
      for (let r of this.plugins)
        r.mustUpdate != t && r.destroy(this);
      this.plugins = s, this.pluginMap.clear();
    } else
      for (let s of this.plugins)
        s.mustUpdate = t;
    for (let s = 0; s < this.plugins.length; s++)
      this.plugins[s].update(this);
    e != i && this.inputState.ensureHandlers(this.plugins);
  }
  docViewUpdate() {
    for (let t of this.plugins) {
      let e = t.value;
      if (e && e.docViewUpdate)
        try {
          e.docViewUpdate(this);
        } catch (i) {
          ft(this.state, i, "doc view update listener");
        }
    }
  }
  /**
  @internal
  */
  measure(t = !0) {
    if (this.destroyed)
      return;
    if (this.measureScheduled > -1 && this.win.cancelAnimationFrame(this.measureScheduled), this.observer.delayedAndroidKey) {
      this.measureScheduled = -1, this.requestMeasure();
      return;
    }
    this.measureScheduled = 0, t && this.observer.forceFlush();
    let e = null, i = this.viewState.scrollParent, s = this.viewState.getScrollOffset(), { scrollAnchorPos: r, scrollAnchorHeight: o } = this.viewState;
    Math.abs(s - this.viewState.scrollOffset) > 1 && (o = -1), this.viewState.scrollAnchorHeight = -1;
    try {
      for (let l = 0; ; l++) {
        if (o < 0)
          if (ql(i || this.win))
            r = -1, o = this.viewState.heightMap.height;
          else {
            let d = this.viewState.scrollAnchorAt(s);
            r = d.from, o = d.top;
          }
        this.updateState = 1;
        let a = this.viewState.measure();
        if (!a && !this.measureRequests.length && this.viewState.scrollTarget == null)
          break;
        if (l > 5) {
          console.warn(this.measureRequests.length ? "Measure loop restarted more than 5 times" : "Viewport failed to stabilize");
          break;
        }
        let h = [];
        a & 4 || ([this.measureRequests, h] = [h, this.measureRequests]);
        let f = h.map((d) => {
          try {
            return d.read(this);
          } catch (p) {
            return ft(this.state, p), Oo;
          }
        }), c = us.create(this, this.state, []), u = !1;
        c.flags |= a, e ? e.flags |= a : e = c, this.updateState = 2, c.empty || (this.updatePlugins(c), this.inputState.update(c), this.updateAttrs(), u = this.docView.update(c), u && this.docViewUpdate());
        for (let d = 0; d < h.length; d++)
          if (f[d] != Oo)
            try {
              let p = h[d];
              p.write && p.write(f[d], this);
            } catch (p) {
              ft(this.state, p);
            }
        if (u && this.docView.updateSelection(!0), !c.viewportChanged && this.measureRequests.length == 0) {
          if (this.viewState.editorHeight)
            if (this.viewState.scrollTarget) {
              this.docView.scrollIntoView(this.viewState.scrollTarget), this.viewState.scrollTarget = null, o = -1;
              continue;
            } else {
              let p = ((r < 0 ? this.viewState.heightMap.height : this.viewState.lineBlockAt(r).top) - o) / this.scaleY;
              if ((p > 1 || p < -1) && (i == this.scrollDOM || this.hasFocus || Math.max(this.inputState.lastWheelEvent, this.inputState.lastTouchTime) > Date.now() - 100)) {
                s = s + p, i ? i.scrollTop += p : this.win.scrollBy(0, p), o = -1;
                continue;
              }
            }
          break;
        }
      }
    } finally {
      this.updateState = 0, this.measureScheduled = -1;
    }
    if (e && !e.empty)
      for (let l of this.state.facet(Hn))
        l(e);
  }
  /**
  Get the CSS classes for the currently active editor themes.
  */
  get themeClasses() {
    return jn + " " + (this.state.facet($n) ? Pa : Ma) + " " + this.state.facet(Hi);
  }
  updateAttrs() {
    let t = Co(this, ha, {
      class: "cm-editor" + (this.hasFocus ? " cm-focused " : " ") + this.themeClasses
    }), e = {
      spellcheck: "false",
      autocorrect: "off",
      autocapitalize: "off",
      writingsuggestions: "false",
      translate: "no",
      contenteditable: this.state.facet(Gt) ? "true" : "false",
      class: "cm-content",
      style: `${A.tabSize}: ${this.state.tabSize}`,
      role: "textbox",
      "aria-multiline": "true"
    };
    this.state.readOnly && (e["aria-readonly"] = "true"), Co(this, pr, e);
    let i = this.observer.ignore(() => {
      let s = Jr(this.contentDOM, this.contentAttrs, e), r = Jr(this.dom, this.editorAttrs, t);
      return s || r;
    });
    return this.editorAttrs = t, this.contentAttrs = e, i;
  }
  showAnnouncements(t) {
    let e = !0;
    for (let i of t)
      for (let s of i.effects)
        if (s.is(P.announce)) {
          e && (this.announceDOM.textContent = ""), e = !1;
          let r = this.announceDOM.appendChild(document.createElement("div"));
          r.textContent = s.value;
        }
  }
  mountStyles() {
    this.styleModules = this.state.facet(ii);
    let t = this.state.facet(P.cspNonce);
    ae.mount(this.root, this.styleModules.concat(Fc).reverse(), t ? { nonce: t } : void 0);
  }
  readMeasured() {
    if (this.updateState == 2)
      throw new Error("Reading the editor layout isn't allowed during an update");
    this.updateState == 0 && this.measureScheduled > -1 && this.measure(!1);
  }
  /**
  Schedule a layout measurement, optionally providing callbacks to
  do custom DOM measuring followed by a DOM write phase. Using
  this is preferable reading DOM layout directly from, for
  example, an event handler, because it'll make sure measuring and
  drawing done by other components is synchronized, avoiding
  unnecessary DOM layout computations.
  */
  requestMeasure(t) {
    if (this.measureScheduled < 0 && (this.measureScheduled = this.win.requestAnimationFrame(() => this.measure())), t) {
      if (this.measureRequests.indexOf(t) > -1)
        return;
      if (t.key != null) {
        for (let e = 0; e < this.measureRequests.length; e++)
          if (this.measureRequests[e].key === t.key) {
            this.measureRequests[e] = t;
            return;
          }
      }
      this.measureRequests.push(t);
    }
  }
  /**
  Get the value of a specific plugin, if present. Note that
  plugins that crash can be dropped from a view, so even when you
  know you registered a given plugin, it is recommended to check
  the return value of this method.
  */
  plugin(t) {
    let e = this.pluginMap.get(t);
    return (e === void 0 || e && e.plugin != t) && this.pluginMap.set(t, e = this.plugins.find((i) => i.plugin == t) || null), e && e.update(this).value;
  }
  /**
  The top position of the document, in screen coordinates. This
  may be negative when the editor is scrolled down. Points
  directly to the top of the first line, not above the padding.
  */
  get documentTop() {
    return this.contentDOM.getBoundingClientRect().top + this.viewState.paddingTop;
  }
  /**
  Reports the padding above and below the document.
  */
  get documentPadding() {
    return { top: this.viewState.paddingTop, bottom: this.viewState.paddingBottom };
  }
  /**
  If the editor is transformed with CSS, this provides the scale
  along the X axis. Otherwise, it will just be 1. Note that
  transforms other than translation and scaling are not supported.
  */
  get scaleX() {
    return this.viewState.scaleX;
  }
  /**
  Provide the CSS transformed scale along the Y axis.
  */
  get scaleY() {
    return this.viewState.scaleY;
  }
  /**
  Find the text line or block widget at the given vertical
  position (which is interpreted as relative to the [top of the
  document](https://codemirror.net/6/docs/ref/#view.EditorView.documentTop)).
  */
  elementAtHeight(t) {
    return this.readMeasured(), this.viewState.elementAtHeight(t);
  }
  /**
  Find the line block (see
  [`lineBlockAt`](https://codemirror.net/6/docs/ref/#view.EditorView.lineBlockAt)) at the given
  height, again interpreted relative to the [top of the
  document](https://codemirror.net/6/docs/ref/#view.EditorView.documentTop).
  */
  lineBlockAtHeight(t) {
    return this.readMeasured(), this.viewState.lineBlockAtHeight(t);
  }
  /**
  Get the extent and vertical position of all [line
  blocks](https://codemirror.net/6/docs/ref/#view.EditorView.lineBlockAt) in the viewport. Positions
  are relative to the [top of the
  document](https://codemirror.net/6/docs/ref/#view.EditorView.documentTop);
  */
  get viewportLineBlocks() {
    return this.viewState.viewportLines;
  }
  /**
  Find the line block around the given document position. A line
  block is a range delimited on both sides by either a
  non-[hidden](https://codemirror.net/6/docs/ref/#view.Decoration^replace) line break, or the
  start/end of the document. It will usually just hold a line of
  text, but may be broken into multiple textblocks by block
  widgets.
  */
  lineBlockAt(t) {
    return this.viewState.lineBlockAt(t);
  }
  /**
  The editor's total content height.
  */
  get contentHeight() {
    return this.viewState.contentHeight;
  }
  /**
  Move a cursor position by [grapheme
  cluster](https://codemirror.net/6/docs/ref/#state.findClusterBreak). `forward` determines whether
  the motion is away from the line start, or towards it. In
  bidirectional text, the line is traversed in visual order, using
  the editor's [text direction](https://codemirror.net/6/docs/ref/#view.EditorView.textDirection).
  When the start position was the last one on the line, the
  returned position will be across the line break. If there is no
  further line, the original position is returned.
  
  By default, this method moves over a single cluster. The
  optional `by` argument can be used to move across more. It will
  be called with the first cluster as argument, and should return
  a predicate that determines, for each subsequent cluster,
  whether it should also be moved over.
  */
  moveByChar(t, e, i) {
    return Js(this, t, oo(this, t, e, i));
  }
  /**
  Move a cursor position across the next group of either
  [letters](https://codemirror.net/6/docs/ref/#state.EditorState.charCategorizer) or non-letter
  non-whitespace characters.
  */
  moveByGroup(t, e) {
    return Js(this, t, oo(this, t, e, (i) => ec(this, t.head, i)));
  }
  /**
  Get the cursor position visually at the start or end of a line.
  Note that this may differ from the _logical_ position at its
  start or end (which is simply at `line.from`/`line.to`) if text
  at the start or end goes against the line's base text direction.
  */
  visualLineSide(t, e) {
    let i = this.bidiSpans(t), s = this.textDirectionAt(t.from), r = i[e ? i.length - 1 : 0];
    return k.cursor(r.side(e, s) + t.from, r.forward(!e, s) ? 1 : -1);
  }
  /**
  Move to the next line boundary in the given direction. If
  `includeWrap` is true, line wrapping is on, and there is a
  further wrap point on the current line, the wrap point will be
  returned. Otherwise this function will return the start or end
  of the line.
  */
  moveToLineBoundary(t, e, i = !0) {
    return tc(this, t, e, i);
  }
  /**
  Move a cursor position vertically. When `distance` isn't given,
  it defaults to moving to the next line (including wrapped
  lines). Otherwise, `distance` should provide a positive distance
  in pixels.
  
  When `start` has a
  [`goalColumn`](https://codemirror.net/6/docs/ref/#state.SelectionRange.goalColumn), the vertical
  motion will use that as a target horizontal position. Otherwise,
  the cursor's own horizontal position is used. The returned
  cursor will have its goal column set to whichever column was
  used.
  */
  moveVertically(t, e, i) {
    return Js(this, t, ic(this, t, e, i));
  }
  /**
  Find the DOM parent node and offset (child offset if `node` is
  an element, character offset when it is a text node) at the
  given document position.
  
  Note that for positions that aren't currently in
  `visibleRanges`, the resulting DOM position isn't necessarily
  meaningful (it may just point before or after a placeholder
  element).
  */
  domAtPos(t, e = 1) {
    return this.docView.domAtPos(t, e);
  }
  /**
  Find the document position at the given DOM node. Can be useful
  for associating positions with DOM events. Will raise an error
  when `node` isn't part of the editor content.
  */
  posAtDOM(t, e = 0) {
    return this.docView.posFromDOM(t, e);
  }
  posAtCoords(t, e = !0) {
    this.readMeasured();
    let i = zn(this, t, e);
    return i && i.pos;
  }
  posAndSideAtCoords(t, e = !0) {
    return this.readMeasured(), zn(this, t, e);
  }
  /**
  Get the screen coordinates at the given document position.
  `side` determines whether the coordinates are based on the
  element before (-1) or after (1) the position (if no element is
  available on the given side, the method will transparently use
  another strategy to get reasonable coordinates).
  */
  coordsAtPos(t, e = 1) {
    this.readMeasured();
    let i = this.docView.coordsAt(t, e);
    if (!i || i.left == i.right)
      return i;
    let s = this.state.doc.lineAt(t), r = this.bidiSpans(s), o = r[jt.find(r, t - s.from, -1, e)];
    return cs(i, o.dir == $.LTR == e > 0);
  }
  /**
  Return the rectangle around a given character. If `pos` does not
  point in front of a character that is in the viewport and
  rendered (i.e. not replaced, not a line break), this will return
  null. For space characters that are a line wrap point, this will
  return the position before the line break.
  */
  coordsForChar(t) {
    return this.readMeasured(), this.docView.coordsForChar(t);
  }
  /**
  The default width of a character in the editor. May not
  accurately reflect the width of all characters (given variable
  width fonts or styling of invididual ranges).
  */
  get defaultCharacterWidth() {
    return this.viewState.heightOracle.charWidth;
  }
  /**
  The default height of a line in the editor. May not be accurate
  for all lines.
  */
  get defaultLineHeight() {
    return this.viewState.heightOracle.lineHeight;
  }
  /**
  The text direction
  ([`direction`](https://developer.mozilla.org/en-US/docs/Web/CSS/direction)
  CSS property) of the editor's content element.
  */
  get textDirection() {
    return this.viewState.defaultTextDirection;
  }
  /**
  Find the text direction of the block at the given position, as
  assigned by CSS. If
  [`perLineTextDirection`](https://codemirror.net/6/docs/ref/#view.EditorView^perLineTextDirection)
  isn't enabled, or the given position is outside of the viewport,
  this will always return the same as
  [`textDirection`](https://codemirror.net/6/docs/ref/#view.EditorView.textDirection). Note that
  this may trigger a DOM layout.
  */
  textDirectionAt(t) {
    return !this.state.facet(oa) || t < this.viewport.from || t > this.viewport.to ? this.textDirection : (this.readMeasured(), this.docView.textDirectionAt(t));
  }
  /**
  Whether this editor [wraps lines](https://codemirror.net/6/docs/ref/#view.EditorView.lineWrapping)
  (as determined by the
  [`white-space`](https://developer.mozilla.org/en-US/docs/Web/CSS/white-space)
  CSS property of its content element).
  */
  get lineWrapping() {
    return this.viewState.heightOracle.lineWrapping;
  }
  /**
  Returns the bidirectional text structure of the given line
  (which should be in the current document) as an array of span
  objects. The order of these spans matches the [text
  direction](https://codemirror.net/6/docs/ref/#view.EditorView.textDirection)—if that is
  left-to-right, the leftmost spans come first, otherwise the
  rightmost spans come first.
  */
  bidiSpans(t) {
    if (t.length > Xc)
      return Zl(t.length);
    let e = this.textDirectionAt(t.from), i;
    for (let r of this.bidiCache)
      if (r.from == t.from && r.dir == e && (r.fresh || Yl(r.isolates, i = io(this, t))))
        return r.order;
    i || (i = io(this, t));
    let s = Cf(t.text, e, i);
    return this.bidiCache.push(new ys(t.from, t.to, e, i, !0, s)), s;
  }
  /**
  Check whether the editor has focus.
  */
  get hasFocus() {
    var t;
    return (this.dom.ownerDocument.hasFocus() || A.safari && ((t = this.inputState) === null || t === void 0 ? void 0 : t.lastContextMenu) > Date.now() - 3e4) && this.root.activeElement == this.contentDOM;
  }
  /**
  Put focus on the editor.
  */
  focus() {
    this.observer.ignore(() => {
      Xl(this.contentDOM), this.docView.updateSelection();
    });
  }
  /**
  Update the [root](https://codemirror.net/6/docs/ref/##view.EditorViewConfig.root) in which the editor lives. This is only
  necessary when moving the editor's existing DOM to a new window or shadow root.
  */
  setRoot(t) {
    this._root != t && (this._root = t, this.observer.setWindow((t.nodeType == 9 ? t : t.ownerDocument).defaultView || window), this.mountStyles());
  }
  /**
  Clean up this editor view, removing its element from the
  document, unregistering event handlers, and notifying
  plugins. The view instance can no longer be used after
  calling this.
  */
  destroy() {
    this.root.activeElement == this.contentDOM && this.contentDOM.blur();
    for (let t of this.plugins)
      t.destroy(this);
    this.plugins = [], this.inputState.destroy(), this.docView.destroy(), this.dom.remove(), this.observer.destroy(), this.measureScheduled > -1 && this.win.cancelAnimationFrame(this.measureScheduled), this.destroyed = !0;
  }
  /**
  Returns an effect that can be
  [added](https://codemirror.net/6/docs/ref/#state.TransactionSpec.effects) to a transaction to
  cause it to scroll the given position or range into view.
  */
  static scrollIntoView(t, e = {}) {
    return Li.of(new Ve(typeof t == "number" ? k.cursor(t) : t, e.y, e.x, e.yMargin, e.xMargin));
  }
  /**
  Return an effect that resets the editor to its current (at the
  time this method was called) scroll position. Note that this
  only affects the editor's own scrollable element, not parents.
  See also
  [`EditorViewConfig.scrollTo`](https://codemirror.net/6/docs/ref/#view.EditorViewConfig.scrollTo).
  
  The effect should be used with a document identical to the one
  it was created for. Failing to do so is not an error, but may
  not scroll to the expected position. You can
  [map](https://codemirror.net/6/docs/ref/#state.StateEffect.map) the effect to account for changes.
  */
  scrollSnapshot() {
    let { scrollTop: t, scrollLeft: e } = this.scrollDOM, i = this.viewState.scrollAnchorAt(t);
    return Li.of(new Ve(k.cursor(i.from), "start", "start", i.top - t, e, !0));
  }
  /**
  Enable or disable tab-focus mode, which disables key bindings
  for Tab and Shift-Tab, letting the browser's default
  focus-changing behavior go through instead. This is useful to
  prevent trapping keyboard users in your editor.
  
  Without argument, this toggles the mode. With a boolean, it
  enables (true) or disables it (false). Given a number, it
  temporarily enables the mode until that number of milliseconds
  have passed or another non-Tab key is pressed.
  */
  setTabFocusMode(t) {
    t == null ? this.inputState.tabFocusMode = this.inputState.tabFocusMode < 0 ? 0 : -1 : typeof t == "boolean" ? this.inputState.tabFocusMode = t ? 0 : -1 : this.inputState.tabFocusMode != 0 && (this.inputState.tabFocusMode = Date.now() + t);
  }
  /**
  Returns an extension that can be used to add DOM event handlers.
  The value should be an object mapping event names to handler
  functions. For any given event, such functions are ordered by
  extension precedence, and the first handler to return true will
  be assumed to have handled that event, and no other handlers or
  built-in behavior will be activated for it. These are registered
  on the [content element](https://codemirror.net/6/docs/ref/#view.EditorView.contentDOM), except
  for `scroll` handlers, which will be called any time the
  editor's [scroll element](https://codemirror.net/6/docs/ref/#view.EditorView.scrollDOM) or one of
  its parent nodes is scrolled.
  */
  static domEventHandlers(t) {
    return dt.define(() => ({}), { eventHandlers: t });
  }
  /**
  Create an extension that registers DOM event observers. Contrary
  to event [handlers](https://codemirror.net/6/docs/ref/#view.EditorView^domEventHandlers),
  observers can't be prevented from running by a higher-precedence
  handler returning true. They also don't prevent other handlers
  and observers from running when they return true, and should not
  call `preventDefault`.
  */
  static domEventObservers(t) {
    return dt.define(() => ({}), { eventObservers: t });
  }
  /**
  Create a theme extension. The first argument can be a
  [`style-mod`](https://github.com/marijnh/style-mod#documentation)
  style spec providing the styles for the theme. These will be
  prefixed with a generated class for the style.
  
  Because the selectors will be prefixed with a scope class, rule
  that directly match the editor's [wrapper
  element](https://codemirror.net/6/docs/ref/#view.EditorView.dom)—to which the scope class will be
  added—need to be explicitly differentiated by adding an `&` to
  the selector for that element—for example
  `&.cm-focused`.
  
  When `dark` is set to true, the theme will be marked as dark,
  which will cause the `&dark` rules from [base
  themes](https://codemirror.net/6/docs/ref/#view.EditorView^baseTheme) to be used (as opposed to
  `&light` when a light theme is active).
  */
  static theme(t, e) {
    let i = ae.newName(), s = [Hi.of(i), ii.of(Xn(`.${i}`, t))];
    return e && e.dark && s.push($n.of(!0)), s;
  }
  /**
  Create an extension that adds styles to the base theme. Like
  with [`theme`](https://codemirror.net/6/docs/ref/#view.EditorView^theme), use `&` to indicate the
  place of the editor wrapper element when directly targeting
  that. You can also use `&dark` or `&light` instead to only
  target editors with a dark or light theme.
  */
  static baseTheme(t) {
    return wi.lowest(ii.of(Xn("." + jn, t, Da)));
  }
  /**
  Retrieve an editor view instance from the view's DOM
  representation.
  */
  static findFromDOM(t) {
    var e;
    let i = t.querySelector(".cm-content"), s = i && U.get(i) || U.get(t);
    return ((e = s == null ? void 0 : s.root) === null || e === void 0 ? void 0 : e.view) || null;
  }
}
P.styleModule = ii;
P.inputHandler = na;
P.clipboardInputFilter = ur;
P.clipboardOutputFilter = dr;
P.scrollHandler = la;
P.focusChangeEffect = ra;
P.perLineTextDirection = oa;
P.exceptionSink = sa;
P.updateListener = Hn;
P.editable = Gt;
P.mouseSelectionStyle = ia;
P.dragMovesSelection = ea;
P.clickAddsSelectionRange = ta;
P.decorations = Es;
P.blockWrappers = fa;
P.outerDecorations = gr;
P.atomicRanges = Ai;
P.bidiIsolatedRanges = ca;
P.scrollMargins = ua;
P.darkTheme = $n;
P.cspNonce = /* @__PURE__ */ M.define({ combine: (n) => n.length ? n[0] : "" });
P.contentAttributes = pr;
P.editorAttributes = ha;
P.lineWrapping = /* @__PURE__ */ P.contentAttributes.of({ class: "cm-lineWrapping" });
P.announce = /* @__PURE__ */ N.define();
const Xc = 4096, Oo = {};
class ys {
  constructor(t, e, i, s, r, o) {
    this.from = t, this.to = e, this.dir = i, this.isolates = s, this.fresh = r, this.order = o;
  }
  static update(t, e) {
    if (e.empty && !t.some((r) => r.fresh))
      return t;
    let i = [], s = t.length ? t[t.length - 1].dir : $.LTR;
    for (let r = Math.max(0, t.length - 10); r < t.length; r++) {
      let o = t[r];
      o.dir == s && !e.touchesRange(o.from, o.to) && i.push(new ys(e.mapPos(o.from, 1), e.mapPos(o.to, -1), o.dir, o.isolates, !1, o.order));
    }
    return i;
  }
}
function Co(n, t, e) {
  for (let i = n.state.facet(t), s = i.length - 1; s >= 0; s--) {
    let r = i[s], o = typeof r == "function" ? r(n) : r;
    o && hr(o, e);
  }
  return e;
}
const qc = A.mac ? "mac" : A.windows ? "win" : A.linux ? "linux" : "key";
function Uc(n, t) {
  const e = n.split(/-(?!$)/);
  let i = e[e.length - 1];
  i == "Space" && (i = " ");
  let s, r, o, l;
  for (let a = 0; a < e.length - 1; ++a) {
    const h = e[a];
    if (/^(cmd|meta|m)$/i.test(h))
      l = !0;
    else if (/^a(lt)?$/i.test(h))
      s = !0;
    else if (/^(c|ctrl|control)$/i.test(h))
      r = !0;
    else if (/^s(hift)?$/i.test(h))
      o = !0;
    else if (/^mod$/i.test(h))
      t == "mac" ? l = !0 : r = !0;
    else
      throw new Error("Unrecognized modifier name: " + h);
  }
  return s && (i = "Alt-" + i), r && (i = "Ctrl-" + i), l && (i = "Meta-" + i), o && (i = "Shift-" + i), i;
}
function Fi(n, t, e) {
  return t.altKey && (n = "Alt-" + n), t.ctrlKey && (n = "Ctrl-" + n), t.metaKey && (n = "Meta-" + n), e !== !1 && t.shiftKey && (n = "Shift-" + n), n;
}
const Kc = /* @__PURE__ */ wi.default(/* @__PURE__ */ P.domEventHandlers({
  keydown(n, t) {
    return Zc(_c(t.state), n, t, "editor");
  }
})), wr = /* @__PURE__ */ M.define({ enables: Kc }), Ao = /* @__PURE__ */ new WeakMap();
function _c(n) {
  let t = n.facet(wr), e = Ao.get(t);
  return e || Ao.set(t, e = Yc(t.reduce((i, s) => i.concat(s), []))), e;
}
let ne = null;
const Gc = 4e3;
function Yc(n, t = qc) {
  let e = /* @__PURE__ */ Object.create(null), i = /* @__PURE__ */ Object.create(null), s = (o, l) => {
    let a = i[o];
    if (a == null)
      i[o] = l;
    else if (a != l)
      throw new Error("Key binding " + o + " is used both as a regular binding and as a multi-stroke prefix");
  }, r = (o, l, a, h, f) => {
    var c, u;
    let d = e[o] || (e[o] = /* @__PURE__ */ Object.create(null)), p = l.split(/ (?!$)/).map((y) => Uc(y, t));
    for (let y = 1; y < p.length; y++) {
      let b = p.slice(0, y).join(" ");
      s(b, !0), d[b] || (d[b] = {
        preventDefault: !0,
        stopPropagation: !1,
        run: [(x) => {
          let O = ne = { view: x, prefix: b, scope: o };
          return setTimeout(() => {
            ne == O && (ne = null);
          }, Gc), !0;
        }]
      });
    }
    let g = p.join(" ");
    s(g, !1);
    let m = d[g] || (d[g] = {
      preventDefault: !1,
      stopPropagation: !1,
      run: ((u = (c = d._any) === null || c === void 0 ? void 0 : c.run) === null || u === void 0 ? void 0 : u.slice()) || []
    });
    a && m.run.push(a), h && (m.preventDefault = !0), f && (m.stopPropagation = !0);
  };
  for (let o of n) {
    let l = o.scope ? o.scope.split(" ") : ["editor"];
    if (o.any)
      for (let h of l) {
        let f = e[h] || (e[h] = /* @__PURE__ */ Object.create(null));
        f._any || (f._any = { preventDefault: !1, stopPropagation: !1, run: [] });
        let { any: c } = o;
        for (let u in f)
          f[u].run.push((d) => c(d, qn));
      }
    let a = o[t] || o.key;
    if (a)
      for (let h of l)
        r(h, a, o.run, o.preventDefault, o.stopPropagation), o.shift && r(h, "Shift-" + a, o.shift, o.preventDefault, o.stopPropagation);
  }
  return e;
}
let qn = null;
function Zc(n, t, e, i) {
  qn = t;
  let s = hf(t), r = Ft(s, 0), o = se(r) == s.length && s != " ", l = "", a = !1, h = !1, f = !1;
  ne && ne.view == e && ne.scope == i && (l = ne.prefix + " ", wa.indexOf(t.keyCode) < 0 && (h = !0, ne = null));
  let c = /* @__PURE__ */ new Set(), u = (m) => {
    if (m) {
      for (let y of m.run)
        if (!c.has(y) && (c.add(y), y(e)))
          return m.stopPropagation && (f = !0), !0;
      m.preventDefault && (m.stopPropagation && (f = !0), h = !0);
    }
    return !1;
  }, d = n[i], p, g;
  return d && (u(d[l + Fi(s, t, !o)]) ? a = !0 : o && (t.altKey || t.metaKey || t.ctrlKey) && // Ctrl-Alt may be used for AltGr on Windows
  !(A.windows && t.ctrlKey && t.altKey) && // Alt-combinations on macOS tend to be typed characters
  !(A.mac && t.altKey && !(t.ctrlKey || t.metaKey)) && (p = he[t.keyCode]) && p != s ? (u(d[l + Fi(p, t, !0)]) || t.shiftKey && (g = ui[t.keyCode]) != s && g != p && u(d[l + Fi(g, t, !1)])) && (a = !0) : o && t.shiftKey && u(d[l + Fi(s, t, !0)]) && (a = !0), !a && u(d._any) && (a = !0)), h && (a = !0), a && f && t.stopPropagation(), qn = null, a;
}
function Jc() {
  return eu;
}
const tu = /* @__PURE__ */ H.line({ class: "cm-activeLine" }), eu = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.decorations = this.getDeco(n);
  }
  update(n) {
    (n.docChanged || n.selectionSet) && (this.decorations = this.getDeco(n.view));
  }
  getDeco(n) {
    let t = -1, e = [];
    for (let i of n.state.selection.ranges) {
      let s = n.lineBlockAt(i.head);
      s.from > t && (e.push(tu.range(s.from)), t = s.from);
    }
    return H.set(e);
  }
}, {
  decorations: (n) => n.decorations
}), zi = "-10000px";
class Ba {
  constructor(t, e, i, s) {
    this.facet = e, this.createTooltipView = i, this.removeTooltipView = s, this.input = t.state.facet(e), this.tooltips = this.input.filter((o) => o);
    let r = null;
    this.tooltipViews = this.tooltips.map((o) => r = i(o, r));
  }
  update(t, e) {
    var i;
    let s = t.state.facet(this.facet), r = s.filter((a) => a);
    if (s === this.input) {
      for (let a of this.tooltipViews)
        a.update && a.update(t);
      return !1;
    }
    let o = [], l = e ? [] : null;
    for (let a = 0; a < r.length; a++) {
      let h = r[a], f = -1;
      if (h) {
        for (let c = 0; c < this.tooltips.length; c++) {
          let u = this.tooltips[c];
          u && u.create == h.create && (f = c);
        }
        if (f < 0)
          o[a] = this.createTooltipView(h, a ? o[a - 1] : null), l && (l[a] = !!h.above);
        else {
          let c = o[a] = this.tooltipViews[f];
          l && (l[a] = e[f]), c.update && c.update(t);
        }
      }
    }
    for (let a of this.tooltipViews)
      o.indexOf(a) < 0 && (this.removeTooltipView(a), (i = a.destroy) === null || i === void 0 || i.call(a));
    return e && (l.forEach((a, h) => e[h] = a), e.length = l.length), this.input = s, this.tooltips = r, this.tooltipViews = o, !0;
  }
}
function iu(n) {
  let t = n.dom.ownerDocument.documentElement;
  return { top: 0, left: 0, bottom: t.clientHeight, right: t.clientWidth };
}
const sn = /* @__PURE__ */ M.define({
  combine: (n) => {
    var t, e, i;
    return {
      position: A.ios ? "absolute" : ((t = n.find((s) => s.position)) === null || t === void 0 ? void 0 : t.position) || "fixed",
      parent: ((e = n.find((s) => s.parent)) === null || e === void 0 ? void 0 : e.parent) || null,
      tooltipSpace: ((i = n.find((s) => s.tooltipSpace)) === null || i === void 0 ? void 0 : i.tooltipSpace) || iu
    };
  }
}), To = /* @__PURE__ */ new WeakMap(), kr = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.view = n, this.above = [], this.inView = !0, this.madeAbsolute = !1, this.lastTransaction = 0, this.measureTimeout = -1;
    let t = n.state.facet(sn);
    this.position = t.position, this.parent = t.parent, this.classes = n.themeClasses, this.createContainer(), this.measureReq = { read: this.readMeasure.bind(this), write: this.writeMeasure.bind(this), key: this }, this.resizeObserver = typeof ResizeObserver == "function" ? new ResizeObserver(() => this.measureSoon()) : null, this.manager = new Ba(n, vr, (e, i) => this.createTooltip(e, i), (e) => {
      this.resizeObserver && this.resizeObserver.unobserve(e.dom), e.dom.remove();
    }), this.above = this.manager.tooltips.map((e) => !!e.above), this.intersectionObserver = typeof IntersectionObserver == "function" ? new IntersectionObserver((e) => {
      Date.now() > this.lastTransaction - 50 && e.length > 0 && e[e.length - 1].intersectionRatio < 1 && this.measureSoon();
    }, { threshold: [1] }) : null, this.observeIntersection(), n.win.addEventListener("resize", this.measureSoon = this.measureSoon.bind(this)), this.maybeMeasure();
  }
  createContainer() {
    this.parent ? (this.container = document.createElement("div"), this.container.style.position = "relative", this.container.className = this.view.themeClasses, this.parent.appendChild(this.container)) : this.container = this.view.dom;
  }
  observeIntersection() {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
      for (let n of this.manager.tooltipViews)
        this.intersectionObserver.observe(n.dom);
    }
  }
  measureSoon() {
    this.measureTimeout < 0 && (this.measureTimeout = setTimeout(() => {
      this.measureTimeout = -1, this.maybeMeasure();
    }, 50));
  }
  update(n) {
    n.transactions.length && (this.lastTransaction = Date.now());
    let t = this.manager.update(n, this.above);
    t && this.observeIntersection();
    let e = t || n.geometryChanged, i = n.state.facet(sn);
    if (i.position != this.position && !this.madeAbsolute) {
      this.position = i.position;
      for (let s of this.manager.tooltipViews)
        s.dom.style.position = this.position;
      e = !0;
    }
    if (i.parent != this.parent) {
      this.parent && this.container.remove(), this.parent = i.parent, this.createContainer();
      for (let s of this.manager.tooltipViews)
        this.container.appendChild(s.dom);
      e = !0;
    } else this.parent && this.view.themeClasses != this.classes && (this.classes = this.container.className = this.view.themeClasses);
    e && this.maybeMeasure();
  }
  createTooltip(n, t) {
    let e = n.create(this.view), i = t ? t.dom : null;
    if (e.dom.classList.add("cm-tooltip"), n.arrow && !e.dom.querySelector(".cm-tooltip > .cm-tooltip-arrow")) {
      let s = document.createElement("div");
      s.className = "cm-tooltip-arrow", e.dom.appendChild(s);
    }
    return e.dom.style.position = this.position, e.dom.style.top = zi, e.dom.style.left = "0px", this.container.insertBefore(e.dom, i), e.mount && e.mount(this.view), this.resizeObserver && this.resizeObserver.observe(e.dom), e;
  }
  destroy() {
    var n, t, e;
    this.view.win.removeEventListener("resize", this.measureSoon);
    for (let i of this.manager.tooltipViews)
      i.dom.remove(), (n = i.destroy) === null || n === void 0 || n.call(i);
    this.parent && this.container.remove(), (t = this.resizeObserver) === null || t === void 0 || t.disconnect(), (e = this.intersectionObserver) === null || e === void 0 || e.disconnect(), clearTimeout(this.measureTimeout);
  }
  readMeasure() {
    let n = 1, t = 1, e = !1;
    if (this.position == "fixed" && this.manager.tooltipViews.length) {
      let { dom: r } = this.manager.tooltipViews[0];
      if (A.safari) {
        let o = r.getBoundingClientRect();
        e = Math.abs(o.top + 1e4) > 1 || Math.abs(o.left) > 1;
      } else
        e = !!r.offsetParent && r.offsetParent != this.container.ownerDocument.body;
    }
    if (e || this.position == "absolute")
      if (this.parent) {
        let r = this.parent.getBoundingClientRect();
        r.width && r.height && (n = r.width / this.parent.offsetWidth, t = r.height / this.parent.offsetHeight);
      } else
        ({ scaleX: n, scaleY: t } = this.view.viewState);
    let i = this.view.scrollDOM.getBoundingClientRect(), s = mr(this.view);
    return {
      visible: {
        left: i.left + s.left,
        top: i.top + s.top,
        right: i.right - s.right,
        bottom: i.bottom - s.bottom
      },
      parent: this.parent ? this.container.getBoundingClientRect() : this.view.dom.getBoundingClientRect(),
      pos: this.manager.tooltips.map((r, o) => {
        let l = this.manager.tooltipViews[o];
        return l.getCoords ? l.getCoords(r.pos) : this.view.coordsAtPos(r.pos);
      }),
      size: this.manager.tooltipViews.map(({ dom: r }) => r.getBoundingClientRect()),
      space: this.view.state.facet(sn).tooltipSpace(this.view),
      scaleX: n,
      scaleY: t,
      makeAbsolute: e
    };
  }
  writeMeasure(n) {
    var t;
    if (n.makeAbsolute) {
      this.madeAbsolute = !0, this.position = "absolute";
      for (let l of this.manager.tooltipViews)
        l.dom.style.position = "absolute";
    }
    let { visible: e, space: i, scaleX: s, scaleY: r } = n, o = [];
    for (let l = 0; l < this.manager.tooltips.length; l++) {
      let a = this.manager.tooltips[l], h = this.manager.tooltipViews[l], { dom: f } = h, c = n.pos[l], u = n.size[l];
      if (!c || a.clip !== !1 && (c.bottom <= Math.max(e.top, i.top) || c.top >= Math.min(e.bottom, i.bottom) || c.right < Math.max(e.left, i.left) - 0.1 || c.left > Math.min(e.right, i.right) + 0.1)) {
        f.style.top = zi;
        continue;
      }
      let d = a.arrow ? h.dom.querySelector(".cm-tooltip-arrow") : null, p = d ? 7 : 0, g = u.right - u.left, m = (t = To.get(h)) !== null && t !== void 0 ? t : u.bottom - u.top, y = h.offset || nu, b = this.view.textDirection == $.LTR, x = u.width > i.right - i.left ? b ? i.left : i.right - u.width : b ? Math.max(i.left, Math.min(c.left - (d ? 14 : 0) + y.x, i.right - g)) : Math.min(Math.max(i.left, c.left - g + (d ? 14 : 0) - y.x), i.right - g), O = this.above[l];
      !a.strictSide && (O ? c.top - m - p - y.y < i.top : c.bottom + m + p + y.y > i.bottom) && O == i.bottom - c.bottom > c.top - i.top && (O = this.above[l] = !O);
      let S = (O ? c.top - i.top : i.bottom - c.bottom) - p;
      if (S < m && h.resize !== !1) {
        if (S < this.view.defaultLineHeight) {
          f.style.top = zi;
          continue;
        }
        To.set(h, m), f.style.height = (m = S) / r + "px";
      } else f.style.height && (f.style.height = "");
      let T = O ? c.top - m - p - y.y : c.bottom + p + y.y, C = x + g;
      if (h.overlap !== !0)
        for (let R of o)
          R.left < C && R.right > x && R.top < T + m && R.bottom > T && (T = O ? R.top - m - 2 - p : R.bottom + p + 2);
      if (this.position == "absolute" ? (f.style.top = (T - n.parent.top) / r + "px", Mo(f, (x - n.parent.left) / s)) : (f.style.top = T / r + "px", Mo(f, x / s)), d) {
        let R = c.left + (b ? y.x : -y.x) - (x + 14 - 7);
        d.style.left = R / s + "px";
      }
      h.overlap !== !0 && o.push({ left: x, top: T, right: C, bottom: T + m }), f.classList.toggle("cm-tooltip-above", O), f.classList.toggle("cm-tooltip-below", !O), h.positioned && h.positioned(n.space);
    }
  }
  maybeMeasure() {
    if (this.manager.tooltips.length && (this.view.inView && this.view.requestMeasure(this.measureReq), this.inView != this.view.inView && (this.inView = this.view.inView, !this.inView)))
      for (let n of this.manager.tooltipViews)
        n.dom.style.top = zi;
  }
}, {
  eventObservers: {
    scroll() {
      this.maybeMeasure();
    }
  }
});
function Mo(n, t) {
  let e = parseInt(n.style.left, 10);
  (isNaN(e) || Math.abs(t - e) > 1) && (n.style.left = t + "px");
}
const su = /* @__PURE__ */ P.baseTheme({
  ".cm-tooltip": {
    zIndex: 500,
    boxSizing: "border-box"
  },
  "&light .cm-tooltip": {
    border: "1px solid #bbb",
    backgroundColor: "#f5f5f5"
  },
  "&light .cm-tooltip-section:not(:first-child)": {
    borderTop: "1px solid #bbb"
  },
  "&dark .cm-tooltip": {
    backgroundColor: "#333338",
    color: "white"
  },
  ".cm-tooltip-arrow": {
    height: "7px",
    width: "14px",
    position: "absolute",
    zIndex: -1,
    overflow: "hidden",
    "&:before, &:after": {
      content: "''",
      position: "absolute",
      width: 0,
      height: 0,
      borderLeft: "7px solid transparent",
      borderRight: "7px solid transparent"
    },
    ".cm-tooltip-above &": {
      bottom: "-7px",
      "&:before": {
        borderTop: "7px solid #bbb"
      },
      "&:after": {
        borderTop: "7px solid #f5f5f5",
        bottom: "1px"
      }
    },
    ".cm-tooltip-below &": {
      top: "-7px",
      "&:before": {
        borderBottom: "7px solid #bbb"
      },
      "&:after": {
        borderBottom: "7px solid #f5f5f5",
        top: "1px"
      }
    }
  },
  "&dark .cm-tooltip .cm-tooltip-arrow": {
    "&:before": {
      borderTopColor: "#333338",
      borderBottomColor: "#333338"
    },
    "&:after": {
      borderTopColor: "transparent",
      borderBottomColor: "transparent"
    }
  }
}), nu = { x: 0, y: 0 }, vr = /* @__PURE__ */ M.define({
  enables: [kr, su]
}), bs = /* @__PURE__ */ M.define({
  combine: (n) => n.reduce((t, e) => t.concat(e), [])
});
class Ws {
  // Needs to be static so that host tooltip instances always match
  static create(t) {
    return new Ws(t);
  }
  constructor(t) {
    this.view = t, this.mounted = !1, this.dom = document.createElement("div"), this.dom.classList.add("cm-tooltip-hover"), this.manager = new Ba(t, bs, (e, i) => this.createHostedView(e, i), (e) => e.dom.remove());
  }
  createHostedView(t, e) {
    let i = t.create(this.view);
    return i.dom.classList.add("cm-tooltip-section"), this.dom.insertBefore(i.dom, e ? e.dom.nextSibling : this.dom.firstChild), this.mounted && i.mount && i.mount(this.view), i;
  }
  mount(t) {
    for (let e of this.manager.tooltipViews)
      e.mount && e.mount(t);
    this.mounted = !0;
  }
  positioned(t) {
    for (let e of this.manager.tooltipViews)
      e.positioned && e.positioned(t);
  }
  update(t) {
    this.manager.update(t);
  }
  destroy() {
    var t;
    for (let e of this.manager.tooltipViews)
      (t = e.destroy) === null || t === void 0 || t.call(e);
  }
  passProp(t) {
    let e;
    for (let i of this.manager.tooltipViews) {
      let s = i[t];
      if (s !== void 0) {
        if (e === void 0)
          e = s;
        else if (e !== s)
          return;
      }
    }
    return e;
  }
  get offset() {
    return this.passProp("offset");
  }
  get getCoords() {
    return this.passProp("getCoords");
  }
  get overlap() {
    return this.passProp("overlap");
  }
  get resize() {
    return this.passProp("resize");
  }
}
const ru = /* @__PURE__ */ vr.compute([bs], (n) => {
  let t = n.facet(bs);
  return t.length === 0 ? null : {
    pos: Math.min(...t.map((e) => e.pos)),
    end: Math.max(...t.map((e) => {
      var i;
      return (i = e.end) !== null && i !== void 0 ? i : e.pos;
    })),
    create: Ws.create,
    above: t[0].above,
    arrow: t.some((e) => e.arrow)
  };
});
class ou {
  constructor(t, e, i, s, r) {
    this.view = t, this.source = e, this.field = i, this.setHover = s, this.hoverTime = r, this.hoverTimeout = -1, this.restartTimeout = -1, this.pending = null, this.lastMove = { x: 0, y: 0, target: t.dom, time: 0 }, this.checkHover = this.checkHover.bind(this), t.dom.addEventListener("mouseleave", this.mouseleave = this.mouseleave.bind(this)), t.dom.addEventListener("mousemove", this.mousemove = this.mousemove.bind(this));
  }
  update() {
    this.pending && (this.pending = null, clearTimeout(this.restartTimeout), this.restartTimeout = setTimeout(() => this.startHover(), 20));
  }
  get active() {
    return this.view.state.field(this.field);
  }
  checkHover() {
    if (this.hoverTimeout = -1, this.active.length)
      return;
    let t = Date.now() - this.lastMove.time;
    t < this.hoverTime ? this.hoverTimeout = setTimeout(this.checkHover, this.hoverTime - t) : this.startHover();
  }
  startHover() {
    clearTimeout(this.restartTimeout);
    let { view: t, lastMove: e } = this, i = t.docView.tile.nearest(e.target);
    if (!i)
      return;
    let s, r = 1;
    if (i.isWidget())
      s = i.posAtStart;
    else {
      if (s = t.posAtCoords(e), s == null)
        return;
      let l = t.coordsAtPos(s);
      if (!l || e.y < l.top || e.y > l.bottom || e.x < l.left - t.defaultCharacterWidth || e.x > l.right + t.defaultCharacterWidth)
        return;
      let a = t.bidiSpans(t.state.doc.lineAt(s)).find((f) => f.from <= s && f.to >= s), h = a && a.dir == $.RTL ? -1 : 1;
      r = e.x < l.left ? -h : h;
    }
    let o = this.source(t, s, r);
    if (o != null && o.then) {
      let l = this.pending = { pos: s };
      o.then((a) => {
        this.pending == l && (this.pending = null, a && !(Array.isArray(a) && !a.length) && t.dispatch({ effects: this.setHover.of(Array.isArray(a) ? a : [a]) }));
      }, (a) => ft(t.state, a, "hover tooltip"));
    } else o && !(Array.isArray(o) && !o.length) && t.dispatch({ effects: this.setHover.of(Array.isArray(o) ? o : [o]) });
  }
  get tooltip() {
    let t = this.view.plugin(kr), e = t ? t.manager.tooltips.findIndex((i) => i.create == Ws.create) : -1;
    return e > -1 ? t.manager.tooltipViews[e] : null;
  }
  mousemove(t) {
    var e, i;
    this.lastMove = { x: t.clientX, y: t.clientY, target: t.target, time: Date.now() }, this.hoverTimeout < 0 && (this.hoverTimeout = setTimeout(this.checkHover, this.hoverTime));
    let { active: s, tooltip: r } = this;
    if (s.length && r && !lu(r.dom, t) || this.pending) {
      let { pos: o } = s[0] || this.pending, l = (i = (e = s[0]) === null || e === void 0 ? void 0 : e.end) !== null && i !== void 0 ? i : o;
      (o == l ? this.view.posAtCoords(this.lastMove) != o : !au(this.view, o, l, t.clientX, t.clientY)) && (this.view.dispatch({ effects: this.setHover.of([]) }), this.pending = null);
    }
  }
  mouseleave(t) {
    clearTimeout(this.hoverTimeout), this.hoverTimeout = -1;
    let { active: e } = this;
    if (e.length) {
      let { tooltip: i } = this;
      i && i.dom.contains(t.relatedTarget) ? this.watchTooltipLeave(i.dom) : this.view.dispatch({ effects: this.setHover.of([]) });
    }
  }
  watchTooltipLeave(t) {
    let e = (i) => {
      t.removeEventListener("mouseleave", e), this.active.length && !this.view.dom.contains(i.relatedTarget) && this.view.dispatch({ effects: this.setHover.of([]) });
    };
    t.addEventListener("mouseleave", e);
  }
  destroy() {
    clearTimeout(this.hoverTimeout), clearTimeout(this.restartTimeout), this.view.dom.removeEventListener("mouseleave", this.mouseleave), this.view.dom.removeEventListener("mousemove", this.mousemove);
  }
}
const Qi = 4;
function lu(n, t) {
  let { left: e, right: i, top: s, bottom: r } = n.getBoundingClientRect(), o;
  if (o = n.querySelector(".cm-tooltip-arrow")) {
    let l = o.getBoundingClientRect();
    s = Math.min(l.top, s), r = Math.max(l.bottom, r);
  }
  return t.clientX >= e - Qi && t.clientX <= i + Qi && t.clientY >= s - Qi && t.clientY <= r + Qi;
}
function au(n, t, e, i, s, r) {
  let o = n.scrollDOM.getBoundingClientRect(), l = n.documentTop + n.documentPadding.top + n.contentHeight;
  if (o.left > i || o.right < i || o.top > s || Math.min(o.bottom, l) < s)
    return !1;
  let a = n.posAtCoords({ x: i, y: s }, !1);
  return a >= t && a <= e;
}
function hu(n, t = {}) {
  let e = N.define(), i = At.define({
    create() {
      return [];
    },
    update(s, r) {
      if (s.length && (t.hideOnChange && (r.docChanged || r.selection) ? s = [] : t.hideOn && (s = s.filter((o) => !t.hideOn(r, o))), r.docChanged)) {
        let o = [];
        for (let l of s) {
          let a = r.changes.mapPos(l.pos, -1, ot.TrackDel);
          if (a != null) {
            let h = Object.assign(/* @__PURE__ */ Object.create(null), l);
            h.pos = a, h.end != null && (h.end = r.changes.mapPos(h.end)), o.push(h);
          }
        }
        s = o;
      }
      for (let o of r.effects)
        o.is(e) && (s = o.value), o.is(fu) && (s = []);
      return s;
    },
    provide: (s) => bs.from(s)
  });
  return {
    active: i,
    extension: [
      i,
      dt.define((s) => new ou(
        s,
        n,
        i,
        e,
        t.hoverTime || 300
        /* Hover.Time */
      )),
      ru
    ]
  };
}
function Ra(n, t) {
  let e = n.plugin(kr);
  if (!e)
    return null;
  let i = e.manager.tooltips.indexOf(t);
  return i < 0 ? null : e.manager.tooltipViews[i];
}
const fu = /* @__PURE__ */ N.define(), Po = /* @__PURE__ */ M.define({
  combine(n) {
    let t, e;
    for (let i of n)
      t = t || i.topContainer, e = e || i.bottomContainer;
    return { topContainer: t, bottomContainer: e };
  }
}), cu = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.input = n.state.facet(Un), this.specs = this.input.filter((e) => e), this.panels = this.specs.map((e) => e(n));
    let t = n.state.facet(Po);
    this.top = new $i(n, !0, t.topContainer), this.bottom = new $i(n, !1, t.bottomContainer), this.top.sync(this.panels.filter((e) => e.top)), this.bottom.sync(this.panels.filter((e) => !e.top));
    for (let e of this.panels)
      e.dom.classList.add("cm-panel"), e.mount && e.mount();
  }
  update(n) {
    let t = n.state.facet(Po);
    this.top.container != t.topContainer && (this.top.sync([]), this.top = new $i(n.view, !0, t.topContainer)), this.bottom.container != t.bottomContainer && (this.bottom.sync([]), this.bottom = new $i(n.view, !1, t.bottomContainer)), this.top.syncClasses(), this.bottom.syncClasses();
    let e = n.state.facet(Un);
    if (e != this.input) {
      let i = e.filter((a) => a), s = [], r = [], o = [], l = [];
      for (let a of i) {
        let h = this.specs.indexOf(a), f;
        h < 0 ? (f = a(n.view), l.push(f)) : (f = this.panels[h], f.update && f.update(n)), s.push(f), (f.top ? r : o).push(f);
      }
      this.specs = i, this.panels = s, this.top.sync(r), this.bottom.sync(o);
      for (let a of l)
        a.dom.classList.add("cm-panel"), a.mount && a.mount();
    } else
      for (let i of this.panels)
        i.update && i.update(n);
  }
  destroy() {
    this.top.sync([]), this.bottom.sync([]);
  }
}, {
  provide: (n) => P.scrollMargins.of((t) => {
    let e = t.plugin(n);
    return e && { top: e.top.scrollMargin(), bottom: e.bottom.scrollMargin() };
  })
});
class $i {
  constructor(t, e, i) {
    this.view = t, this.top = e, this.container = i, this.dom = void 0, this.classes = "", this.panels = [], this.syncClasses();
  }
  sync(t) {
    for (let e of this.panels)
      e.destroy && t.indexOf(e) < 0 && e.destroy();
    this.panels = t, this.syncDOM();
  }
  syncDOM() {
    if (this.panels.length == 0) {
      this.dom && (this.dom.remove(), this.dom = void 0);
      return;
    }
    if (!this.dom) {
      this.dom = document.createElement("div"), this.dom.className = this.top ? "cm-panels cm-panels-top" : "cm-panels cm-panels-bottom", this.dom.style[this.top ? "top" : "bottom"] = "0";
      let e = this.container || this.view.dom;
      e.insertBefore(this.dom, this.top ? e.firstChild : null);
    }
    let t = this.dom.firstChild;
    for (let e of this.panels)
      if (e.dom.parentNode == this.dom) {
        for (; t != e.dom; )
          t = Do(t);
        t = t.nextSibling;
      } else
        this.dom.insertBefore(e.dom, t);
    for (; t; )
      t = Do(t);
  }
  scrollMargin() {
    return !this.dom || this.container ? 0 : Math.max(0, this.top ? this.dom.getBoundingClientRect().bottom - Math.max(0, this.view.scrollDOM.getBoundingClientRect().top) : Math.min(innerHeight, this.view.scrollDOM.getBoundingClientRect().bottom) - this.dom.getBoundingClientRect().top);
  }
  syncClasses() {
    if (!(!this.container || this.classes == this.view.themeClasses)) {
      for (let t of this.classes.split(" "))
        t && this.container.classList.remove(t);
      for (let t of (this.classes = this.view.themeClasses).split(" "))
        t && this.container.classList.add(t);
    }
  }
}
function Do(n) {
  let t = n.nextSibling;
  return n.remove(), t;
}
const Un = /* @__PURE__ */ M.define({
  enables: cu
});
class ce extends le {
  /**
  @internal
  */
  compare(t) {
    return this == t || this.constructor == t.constructor && this.eq(t);
  }
  /**
  Compare this marker to another marker of the same type.
  */
  eq(t) {
    return !1;
  }
  /**
  Called if the marker has a `toDOM` method and its representation
  was removed from a gutter.
  */
  destroy(t) {
  }
}
ce.prototype.elementClass = "";
ce.prototype.toDOM = void 0;
ce.prototype.mapMode = ot.TrackBefore;
ce.prototype.startSide = ce.prototype.endSide = -1;
ce.prototype.point = !0;
const ss = /* @__PURE__ */ M.define(), uu = /* @__PURE__ */ M.define(), ns = /* @__PURE__ */ M.define(), Bo = /* @__PURE__ */ M.define({
  combine: (n) => n.some((t) => t)
});
function du(n) {
  return [
    pu
  ];
}
const pu = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.view = n, this.domAfter = null, this.prevViewport = n.viewport, this.dom = document.createElement("div"), this.dom.className = "cm-gutters cm-gutters-before", this.dom.setAttribute("aria-hidden", "true"), this.dom.style.minHeight = this.view.contentHeight / this.view.scaleY + "px", this.gutters = n.state.facet(ns).map((t) => new Lo(n, t)), this.fixed = !n.state.facet(Bo);
    for (let t of this.gutters)
      t.config.side == "after" ? this.getDOMAfter().appendChild(t.dom) : this.dom.appendChild(t.dom);
    this.fixed && (this.dom.style.position = "sticky"), this.syncGutters(!1), n.scrollDOM.insertBefore(this.dom, n.contentDOM);
  }
  getDOMAfter() {
    return this.domAfter || (this.domAfter = document.createElement("div"), this.domAfter.className = "cm-gutters cm-gutters-after", this.domAfter.setAttribute("aria-hidden", "true"), this.domAfter.style.minHeight = this.view.contentHeight / this.view.scaleY + "px", this.domAfter.style.position = this.fixed ? "sticky" : "", this.view.scrollDOM.appendChild(this.domAfter)), this.domAfter;
  }
  update(n) {
    if (this.updateGutters(n)) {
      let t = this.prevViewport, e = n.view.viewport, i = Math.min(t.to, e.to) - Math.max(t.from, e.from);
      this.syncGutters(i < (e.to - e.from) * 0.8);
    }
    if (n.geometryChanged) {
      let t = this.view.contentHeight / this.view.scaleY + "px";
      this.dom.style.minHeight = t, this.domAfter && (this.domAfter.style.minHeight = t);
    }
    this.view.state.facet(Bo) != !this.fixed && (this.fixed = !this.fixed, this.dom.style.position = this.fixed ? "sticky" : "", this.domAfter && (this.domAfter.style.position = this.fixed ? "sticky" : "")), this.prevViewport = n.view.viewport;
  }
  syncGutters(n) {
    let t = this.dom.nextSibling;
    n && (this.dom.remove(), this.domAfter && this.domAfter.remove());
    let e = L.iter(this.view.state.facet(ss), this.view.viewport.from), i = [], s = this.gutters.map((r) => new gu(r, this.view.viewport, -this.view.documentPadding.top));
    for (let r of this.view.viewportLineBlocks)
      if (i.length && (i = []), Array.isArray(r.type)) {
        let o = !0;
        for (let l of r.type)
          if (l.type == ut.Text && o) {
            Kn(e, i, l.from);
            for (let a of s)
              a.line(this.view, l, i);
            o = !1;
          } else if (l.widget)
            for (let a of s)
              a.widget(this.view, l);
      } else if (r.type == ut.Text) {
        Kn(e, i, r.from);
        for (let o of s)
          o.line(this.view, r, i);
      } else if (r.widget)
        for (let o of s)
          o.widget(this.view, r);
    for (let r of s)
      r.finish();
    n && (this.view.scrollDOM.insertBefore(this.dom, t), this.domAfter && this.view.scrollDOM.appendChild(this.domAfter));
  }
  updateGutters(n) {
    let t = n.startState.facet(ns), e = n.state.facet(ns), i = n.docChanged || n.heightChanged || n.viewportChanged || !L.eq(n.startState.facet(ss), n.state.facet(ss), n.view.viewport.from, n.view.viewport.to);
    if (t == e)
      for (let s of this.gutters)
        s.update(n) && (i = !0);
    else {
      i = !0;
      let s = [];
      for (let r of e) {
        let o = t.indexOf(r);
        o < 0 ? s.push(new Lo(this.view, r)) : (this.gutters[o].update(n), s.push(this.gutters[o]));
      }
      for (let r of this.gutters)
        r.dom.remove(), s.indexOf(r) < 0 && r.destroy();
      for (let r of s)
        r.config.side == "after" ? this.getDOMAfter().appendChild(r.dom) : this.dom.appendChild(r.dom);
      this.gutters = s;
    }
    return i;
  }
  destroy() {
    for (let n of this.gutters)
      n.destroy();
    this.dom.remove(), this.domAfter && this.domAfter.remove();
  }
}, {
  provide: (n) => P.scrollMargins.of((t) => {
    let e = t.plugin(n);
    if (!e || e.gutters.length == 0 || !e.fixed)
      return null;
    let i = e.dom.offsetWidth * t.scaleX, s = e.domAfter ? e.domAfter.offsetWidth * t.scaleX : 0;
    return t.textDirection == $.LTR ? { left: i, right: s } : { right: i, left: s };
  })
});
function Ro(n) {
  return Array.isArray(n) ? n : [n];
}
function Kn(n, t, e) {
  for (; n.value && n.from <= e; )
    n.from == e && t.push(n.value), n.next();
}
class gu {
  constructor(t, e, i) {
    this.gutter = t, this.height = i, this.i = 0, this.cursor = L.iter(t.markers, e.from);
  }
  addElement(t, e, i) {
    let { gutter: s } = this, r = (e.top - this.height) / t.scaleY, o = e.height / t.scaleY;
    if (this.i == s.elements.length) {
      let l = new La(t, o, r, i);
      s.elements.push(l), s.dom.appendChild(l.dom);
    } else
      s.elements[this.i].update(t, o, r, i);
    this.height = e.bottom, this.i++;
  }
  line(t, e, i) {
    let s = [];
    Kn(this.cursor, s, e.from), i.length && (s = s.concat(i));
    let r = this.gutter.config.lineMarker(t, e, s);
    r && s.unshift(r);
    let o = this.gutter;
    s.length == 0 && !o.config.renderEmptyElements || this.addElement(t, e, s);
  }
  widget(t, e) {
    let i = this.gutter.config.widgetMarker(t, e.widget, e), s = i ? [i] : null;
    for (let r of t.state.facet(uu)) {
      let o = r(t, e.widget, e);
      o && (s || (s = [])).push(o);
    }
    s && this.addElement(t, e, s);
  }
  finish() {
    let t = this.gutter;
    for (; t.elements.length > this.i; ) {
      let e = t.elements.pop();
      t.dom.removeChild(e.dom), e.destroy();
    }
  }
}
class Lo {
  constructor(t, e) {
    this.view = t, this.config = e, this.elements = [], this.spacer = null, this.dom = document.createElement("div"), this.dom.className = "cm-gutter" + (this.config.class ? " " + this.config.class : "");
    for (let i in e.domEventHandlers)
      this.dom.addEventListener(i, (s) => {
        let r = s.target, o;
        if (r != this.dom && this.dom.contains(r)) {
          for (; r.parentNode != this.dom; )
            r = r.parentNode;
          let a = r.getBoundingClientRect();
          o = (a.top + a.bottom) / 2;
        } else
          o = s.clientY;
        let l = t.lineBlockAtHeight(o - t.documentTop);
        e.domEventHandlers[i](t, l, s) && s.preventDefault();
      });
    this.markers = Ro(e.markers(t)), e.initialSpacer && (this.spacer = new La(t, 0, 0, [e.initialSpacer(t)]), this.dom.appendChild(this.spacer.dom), this.spacer.dom.style.cssText += "visibility: hidden; pointer-events: none");
  }
  update(t) {
    let e = this.markers;
    if (this.markers = Ro(this.config.markers(t.view)), this.spacer && this.config.updateSpacer) {
      let s = this.config.updateSpacer(this.spacer.markers[0], t);
      s != this.spacer.markers[0] && this.spacer.update(t.view, 0, 0, [s]);
    }
    let i = t.view.viewport;
    return !L.eq(this.markers, e, i.from, i.to) || (this.config.lineMarkerChange ? this.config.lineMarkerChange(t) : !1);
  }
  destroy() {
    for (let t of this.elements)
      t.destroy();
  }
}
class La {
  constructor(t, e, i, s) {
    this.height = -1, this.above = 0, this.markers = [], this.dom = document.createElement("div"), this.dom.className = "cm-gutterElement", this.update(t, e, i, s);
  }
  update(t, e, i, s) {
    this.height != e && (this.height = e, this.dom.style.height = e + "px"), this.above != i && (this.dom.style.marginTop = (this.above = i) ? i + "px" : ""), mu(this.markers, s) || this.setMarkers(t, s);
  }
  setMarkers(t, e) {
    let i = "cm-gutterElement", s = this.dom.firstChild;
    for (let r = 0, o = 0; ; ) {
      let l = o, a = r < e.length ? e[r++] : null, h = !1;
      if (a) {
        let f = a.elementClass;
        f && (i += " " + f);
        for (let c = o; c < this.markers.length; c++)
          if (this.markers[c].compare(a)) {
            l = c, h = !0;
            break;
          }
      } else
        l = this.markers.length;
      for (; o < l; ) {
        let f = this.markers[o++];
        if (f.toDOM) {
          f.destroy(s);
          let c = s.nextSibling;
          s.remove(), s = c;
        }
      }
      if (!a)
        break;
      a.toDOM && (h ? s = s.nextSibling : this.dom.insertBefore(a.toDOM(t), s)), h && o++;
    }
    this.dom.className = i, this.markers = e;
  }
  destroy() {
    this.setMarkers(null, []);
  }
}
function mu(n, t) {
  if (n.length != t.length)
    return !1;
  for (let e = 0; e < n.length; e++)
    if (!n[e].compare(t[e]))
      return !1;
  return !0;
}
const yu = /* @__PURE__ */ M.define(), bu = /* @__PURE__ */ M.define(), Be = /* @__PURE__ */ M.define({
  combine(n) {
    return vi(n, { formatNumber: String, domEventHandlers: {} }, {
      domEventHandlers(t, e) {
        let i = Object.assign({}, t);
        for (let s in e) {
          let r = i[s], o = e[s];
          i[s] = r ? (l, a, h) => r(l, a, h) || o(l, a, h) : o;
        }
        return i;
      }
    });
  }
});
class nn extends ce {
  constructor(t) {
    super(), this.number = t;
  }
  eq(t) {
    return this.number == t.number;
  }
  toDOM() {
    return document.createTextNode(this.number);
  }
}
function rn(n, t) {
  return n.state.facet(Be).formatNumber(t, n.state);
}
const xu = /* @__PURE__ */ ns.compute([Be], (n) => ({
  class: "cm-lineNumbers",
  renderEmptyElements: !1,
  markers(t) {
    return t.state.facet(yu);
  },
  lineMarker(t, e, i) {
    return i.some((s) => s.toDOM) ? null : new nn(rn(t, t.state.doc.lineAt(e.from).number));
  },
  widgetMarker: (t, e, i) => {
    for (let s of t.state.facet(bu)) {
      let r = s(t, e, i);
      if (r)
        return r;
    }
    return null;
  },
  lineMarkerChange: (t) => t.startState.facet(Be) != t.state.facet(Be),
  initialSpacer(t) {
    return new nn(rn(t, Eo(t.state.doc.lines)));
  },
  updateSpacer(t, e) {
    let i = rn(e.view, Eo(e.view.state.doc.lines));
    return i == t.number ? t : new nn(i);
  },
  domEventHandlers: n.facet(Be).domEventHandlers,
  side: "before"
}));
function wu(n = {}) {
  return [
    Be.of(n),
    du(),
    xu
  ];
}
function Eo(n) {
  let t = 9;
  for (; t < n; )
    t = t * 10 + 9;
  return t;
}
const ku = /* @__PURE__ */ new class extends ce {
  constructor() {
    super(...arguments), this.elementClass = "cm-activeLineGutter";
  }
}(), vu = /* @__PURE__ */ ss.compute(["selection"], (n) => {
  let t = [], e = -1;
  for (let i of n.selection.ranges) {
    let s = n.doc.lineAt(i.head).from;
    s > e && (e = s, t.push(ku.range(s)));
  }
  return L.of(t);
});
function Su() {
  return vu;
}
const Ea = 1024;
let Ou = 0;
class on {
  constructor(t, e) {
    this.from = t, this.to = e;
  }
}
class B {
  /**
  Create a new node prop type.
  */
  constructor(t = {}) {
    this.id = Ou++, this.perNode = !!t.perNode, this.deserialize = t.deserialize || (() => {
      throw new Error("This node type doesn't define a deserialize function");
    }), this.combine = t.combine || null;
  }
  /**
  This is meant to be used with
  [`NodeSet.extend`](#common.NodeSet.extend) or
  [`LRParser.configure`](#lr.ParserConfig.props) to compute
  prop values for each node type in the set. Takes a [match
  object](#common.NodeType^match) or function that returns undefined
  if the node type doesn't get this prop, and the prop's value if
  it does.
  */
  add(t) {
    if (this.perNode)
      throw new RangeError("Can't add per-node props to node types");
    return typeof t != "function" && (t = gt.match(t)), (e) => {
      let i = t(e);
      return i === void 0 ? null : [this, i];
    };
  }
}
B.closedBy = new B({ deserialize: (n) => n.split(" ") });
B.openedBy = new B({ deserialize: (n) => n.split(" ") });
B.group = new B({ deserialize: (n) => n.split(" ") });
B.isolate = new B({ deserialize: (n) => {
  if (n && n != "rtl" && n != "ltr" && n != "auto")
    throw new RangeError("Invalid value for isolate: " + n);
  return n || "auto";
} });
B.contextHash = new B({ perNode: !0 });
B.lookAhead = new B({ perNode: !0 });
B.mounted = new B({ perNode: !0 });
class hi {
  constructor(t, e, i, s = !1) {
    this.tree = t, this.overlay = e, this.parser = i, this.bracketed = s;
  }
  /**
  @internal
  */
  static get(t) {
    return t && t.props && t.props[B.mounted.id];
  }
}
const Cu = /* @__PURE__ */ Object.create(null);
class gt {
  /**
  @internal
  */
  constructor(t, e, i, s = 0) {
    this.name = t, this.props = e, this.id = i, this.flags = s;
  }
  /**
  Define a node type.
  */
  static define(t) {
    let e = t.props && t.props.length ? /* @__PURE__ */ Object.create(null) : Cu, i = (t.top ? 1 : 0) | (t.skipped ? 2 : 0) | (t.error ? 4 : 0) | (t.name == null ? 8 : 0), s = new gt(t.name || "", e, t.id, i);
    if (t.props) {
      for (let r of t.props)
        if (Array.isArray(r) || (r = r(s)), r) {
          if (r[0].perNode)
            throw new RangeError("Can't store a per-node prop on a node type");
          e[r[0].id] = r[1];
        }
    }
    return s;
  }
  /**
  Retrieves a node prop for this type. Will return `undefined` if
  the prop isn't present on this node.
  */
  prop(t) {
    return this.props[t.id];
  }
  /**
  True when this is the top node of a grammar.
  */
  get isTop() {
    return (this.flags & 1) > 0;
  }
  /**
  True when this node is produced by a skip rule.
  */
  get isSkipped() {
    return (this.flags & 2) > 0;
  }
  /**
  Indicates whether this is an error node.
  */
  get isError() {
    return (this.flags & 4) > 0;
  }
  /**
  When true, this node type doesn't correspond to a user-declared
  named node, for example because it is used to cache repetition.
  */
  get isAnonymous() {
    return (this.flags & 8) > 0;
  }
  /**
  Returns true when this node's name or one of its
  [groups](#common.NodeProp^group) matches the given string.
  */
  is(t) {
    if (typeof t == "string") {
      if (this.name == t)
        return !0;
      let e = this.prop(B.group);
      return e ? e.indexOf(t) > -1 : !1;
    }
    return this.id == t;
  }
  /**
  Create a function from node types to arbitrary values by
  specifying an object whose property names are node or
  [group](#common.NodeProp^group) names. Often useful with
  [`NodeProp.add`](#common.NodeProp.add). You can put multiple
  names, separated by spaces, in a single property name to map
  multiple node names to a single value.
  */
  static match(t) {
    let e = /* @__PURE__ */ Object.create(null);
    for (let i in t)
      for (let s of i.split(" "))
        e[s] = t[i];
    return (i) => {
      for (let s = i.prop(B.group), r = -1; r < (s ? s.length : 0); r++) {
        let o = e[r < 0 ? i.name : s[r]];
        if (o)
          return o;
      }
    };
  }
}
gt.none = new gt(
  "",
  /* @__PURE__ */ Object.create(null),
  0,
  8
  /* NodeFlag.Anonymous */
);
class Sr {
  /**
  Create a set with the given types. The `id` property of each
  type should correspond to its position within the array.
  */
  constructor(t) {
    this.types = t;
    for (let e = 0; e < t.length; e++)
      if (t[e].id != e)
        throw new RangeError("Node type ids should correspond to array positions when creating a node set");
  }
  /**
  Create a copy of this set with some node properties added. The
  arguments to this method can be created with
  [`NodeProp.add`](#common.NodeProp.add).
  */
  extend(...t) {
    let e = [];
    for (let i of this.types) {
      let s = null;
      for (let r of t) {
        let o = r(i);
        if (o) {
          s || (s = Object.assign({}, i.props));
          let l = o[1], a = o[0];
          a.combine && a.id in s && (l = a.combine(s[a.id], l)), s[a.id] = l;
        }
      }
      e.push(s ? new gt(i.name, s, i.id, i.flags) : i);
    }
    return new Sr(e);
  }
}
const ji = /* @__PURE__ */ new WeakMap(), Io = /* @__PURE__ */ new WeakMap();
var q;
(function(n) {
  n[n.ExcludeBuffers = 1] = "ExcludeBuffers", n[n.IncludeAnonymous = 2] = "IncludeAnonymous", n[n.IgnoreMounts = 4] = "IgnoreMounts", n[n.IgnoreOverlays = 8] = "IgnoreOverlays", n[n.EnterBracketed = 16] = "EnterBracketed";
})(q || (q = {}));
class _ {
  /**
  Construct a new tree. See also [`Tree.build`](#common.Tree^build).
  */
  constructor(t, e, i, s, r) {
    if (this.type = t, this.children = e, this.positions = i, this.length = s, this.props = null, r && r.length) {
      this.props = /* @__PURE__ */ Object.create(null);
      for (let [o, l] of r)
        this.props[typeof o == "number" ? o : o.id] = l;
    }
  }
  /**
  @internal
  */
  toString() {
    let t = hi.get(this);
    if (t && !t.overlay)
      return t.tree.toString();
    let e = "";
    for (let i of this.children) {
      let s = i.toString();
      s && (e && (e += ","), e += s);
    }
    return this.type.name ? (/\W/.test(this.type.name) && !this.type.isError ? JSON.stringify(this.type.name) : this.type.name) + (e.length ? "(" + e + ")" : "") : e;
  }
  /**
  Get a [tree cursor](#common.TreeCursor) positioned at the top of
  the tree. Mode can be used to [control](#common.IterMode) which
  nodes the cursor visits.
  */
  cursor(t = 0) {
    return new Gn(this.topNode, t);
  }
  /**
  Get a [tree cursor](#common.TreeCursor) pointing into this tree
  at the given position and side (see
  [`moveTo`](#common.TreeCursor.moveTo).
  */
  cursorAt(t, e = 0, i = 0) {
    let s = ji.get(this) || this.topNode, r = new Gn(s);
    return r.moveTo(t, e), ji.set(this, r._tree), r;
  }
  /**
  Get a [syntax node](#common.SyntaxNode) object for the top of the
  tree.
  */
  get topNode() {
    return new Ct(this, 0, 0, null);
  }
  /**
  Get the [syntax node](#common.SyntaxNode) at the given position.
  If `side` is -1, this will move into nodes that end at the
  position. If 1, it'll move into nodes that start at the
  position. With 0, it'll only enter nodes that cover the position
  from both sides.
  
  Note that this will not enter
  [overlays](#common.MountedTree.overlay), and you often want
  [`resolveInner`](#common.Tree.resolveInner) instead.
  */
  resolve(t, e = 0) {
    let i = mi(ji.get(this) || this.topNode, t, e, !1);
    return ji.set(this, i), i;
  }
  /**
  Like [`resolve`](#common.Tree.resolve), but will enter
  [overlaid](#common.MountedTree.overlay) nodes, producing a syntax node
  pointing into the innermost overlaid tree at the given position
  (with parent links going through all parent structure, including
  the host trees).
  */
  resolveInner(t, e = 0) {
    let i = mi(Io.get(this) || this.topNode, t, e, !0);
    return Io.set(this, i), i;
  }
  /**
  In some situations, it can be useful to iterate through all
  nodes around a position, including those in overlays that don't
  directly cover the position. This method gives you an iterator
  that will produce all nodes, from small to big, around the given
  position.
  */
  resolveStack(t, e = 0) {
    return Mu(this, t, e);
  }
  /**
  Iterate over the tree and its children, calling `enter` for any
  node that touches the `from`/`to` region (if given) before
  running over such a node's children, and `leave` (if given) when
  leaving the node. When `enter` returns `false`, that node will
  not have its children iterated over (or `leave` called).
  */
  iterate(t) {
    let { enter: e, leave: i, from: s = 0, to: r = this.length } = t, o = t.mode || 0, l = (o & q.IncludeAnonymous) > 0;
    for (let a = this.cursor(o | q.IncludeAnonymous); ; ) {
      let h = !1;
      if (a.from <= r && a.to >= s && (!l && a.type.isAnonymous || e(a) !== !1)) {
        if (a.firstChild())
          continue;
        h = !0;
      }
      for (; h && i && (l || !a.type.isAnonymous) && i(a), !a.nextSibling(); ) {
        if (!a.parent())
          return;
        h = !0;
      }
    }
  }
  /**
  Get the value of the given [node prop](#common.NodeProp) for this
  node. Works with both per-node and per-type props.
  */
  prop(t) {
    return t.perNode ? this.props ? this.props[t.id] : void 0 : this.type.prop(t);
  }
  /**
  Returns the node's [per-node props](#common.NodeProp.perNode) in a
  format that can be passed to the [`Tree`](#common.Tree)
  constructor.
  */
  get propValues() {
    let t = [];
    if (this.props)
      for (let e in this.props)
        t.push([+e, this.props[e]]);
    return t;
  }
  /**
  Balance the direct children of this tree, producing a copy of
  which may have children grouped into subtrees with type
  [`NodeType.none`](#common.NodeType^none).
  */
  balance(t = {}) {
    return this.children.length <= 8 ? this : Ar(gt.none, this.children, this.positions, 0, this.children.length, 0, this.length, (e, i, s) => new _(this.type, e, i, s, this.propValues), t.makeTree || ((e, i, s) => new _(gt.none, e, i, s)));
  }
  /**
  Build a tree from a postfix-ordered buffer of node information,
  or a cursor over such a buffer.
  */
  static build(t) {
    return Pu(t);
  }
}
_.empty = new _(gt.none, [], [], 0);
class Or {
  constructor(t, e) {
    this.buffer = t, this.index = e;
  }
  get id() {
    return this.buffer[this.index - 4];
  }
  get start() {
    return this.buffer[this.index - 3];
  }
  get end() {
    return this.buffer[this.index - 2];
  }
  get size() {
    return this.buffer[this.index - 1];
  }
  get pos() {
    return this.index;
  }
  next() {
    this.index -= 4;
  }
  fork() {
    return new Or(this.buffer, this.index);
  }
}
class ue {
  /**
  Create a tree buffer.
  */
  constructor(t, e, i) {
    this.buffer = t, this.length = e, this.set = i;
  }
  /**
  @internal
  */
  get type() {
    return gt.none;
  }
  /**
  @internal
  */
  toString() {
    let t = [];
    for (let e = 0; e < this.buffer.length; )
      t.push(this.childString(e)), e = this.buffer[e + 3];
    return t.join(",");
  }
  /**
  @internal
  */
  childString(t) {
    let e = this.buffer[t], i = this.buffer[t + 3], s = this.set.types[e], r = s.name;
    if (/\W/.test(r) && !s.isError && (r = JSON.stringify(r)), t += 4, i == t)
      return r;
    let o = [];
    for (; t < i; )
      o.push(this.childString(t)), t = this.buffer[t + 3];
    return r + "(" + o.join(",") + ")";
  }
  /**
  @internal
  */
  findChild(t, e, i, s, r) {
    let { buffer: o } = this, l = -1;
    for (let a = t; a != e && !(Ia(r, s, o[a + 1], o[a + 2]) && (l = a, i > 0)); a = o[a + 3])
      ;
    return l;
  }
  /**
  @internal
  */
  slice(t, e, i) {
    let s = this.buffer, r = new Uint16Array(e - t), o = 0;
    for (let l = t, a = 0; l < e; ) {
      r[a++] = s[l++], r[a++] = s[l++] - i;
      let h = r[a++] = s[l++] - i;
      r[a++] = s[l++] - t, o = Math.max(o, h);
    }
    return new ue(r, o, this.set);
  }
}
function Ia(n, t, e, i) {
  switch (n) {
    case -2:
      return e < t;
    case -1:
      return i >= t && e < t;
    case 0:
      return e < t && i > t;
    case 1:
      return e <= t && i > t;
    case 2:
      return i > t;
    case 4:
      return !0;
  }
}
function mi(n, t, e, i) {
  for (var s; n.from == n.to || (e < 1 ? n.from >= t : n.from > t) || (e > -1 ? n.to <= t : n.to < t); ) {
    let o = !i && n instanceof Ct && n.index < 0 ? null : n.parent;
    if (!o)
      return n;
    n = o;
  }
  let r = i ? 0 : q.IgnoreOverlays;
  if (i)
    for (let o = n, l = o.parent; l; o = l, l = o.parent)
      o instanceof Ct && o.index < 0 && ((s = l.enter(t, e, r)) === null || s === void 0 ? void 0 : s.from) != o.from && (n = l);
  for (; ; ) {
    let o = n.enter(t, e, r);
    if (!o)
      return n;
    n = o;
  }
}
class Na {
  cursor(t = 0) {
    return new Gn(this, t);
  }
  getChild(t, e = null, i = null) {
    let s = No(this, t, e, i);
    return s.length ? s[0] : null;
  }
  getChildren(t, e = null, i = null) {
    return No(this, t, e, i);
  }
  resolve(t, e = 0) {
    return mi(this, t, e, !1);
  }
  resolveInner(t, e = 0) {
    return mi(this, t, e, !0);
  }
  matchContext(t) {
    return _n(this.parent, t);
  }
  enterUnfinishedNodesBefore(t) {
    let e = this.childBefore(t), i = this;
    for (; e; ) {
      let s = e.lastChild;
      if (!s || s.to != e.to)
        break;
      s.type.isError && s.from == s.to ? (i = e, e = s.prevSibling) : e = s;
    }
    return i;
  }
  get node() {
    return this;
  }
  get next() {
    return this.parent;
  }
}
class Ct extends Na {
  constructor(t, e, i, s) {
    super(), this._tree = t, this.from = e, this.index = i, this._parent = s;
  }
  get type() {
    return this._tree.type;
  }
  get name() {
    return this._tree.type.name;
  }
  get to() {
    return this.from + this._tree.length;
  }
  nextChild(t, e, i, s, r = 0) {
    for (let o = this; ; ) {
      for (let { children: l, positions: a } = o._tree, h = e > 0 ? l.length : -1; t != h; t += e) {
        let f = l[t], c = a[t] + o.from, u;
        if (!(!(r & q.EnterBracketed && f instanceof _ && (u = hi.get(f)) && !u.overlay && u.bracketed && i >= c && i <= c + f.length) && !Ia(s, i, c, c + f.length))) {
          if (f instanceof ue) {
            if (r & q.ExcludeBuffers)
              continue;
            let d = f.findChild(0, f.buffer.length, e, i - c, s);
            if (d > -1)
              return new oe(new Au(o, f, t, c), null, d);
          } else if (r & q.IncludeAnonymous || !f.type.isAnonymous || Cr(f)) {
            let d;
            if (!(r & q.IgnoreMounts) && (d = hi.get(f)) && !d.overlay)
              return new Ct(d.tree, c, t, o);
            let p = new Ct(f, c, t, o);
            return r & q.IncludeAnonymous || !p.type.isAnonymous ? p : p.nextChild(e < 0 ? f.children.length - 1 : 0, e, i, s, r);
          }
        }
      }
      if (r & q.IncludeAnonymous || !o.type.isAnonymous || (o.index >= 0 ? t = o.index + e : t = e < 0 ? -1 : o._parent._tree.children.length, o = o._parent, !o))
        return null;
    }
  }
  get firstChild() {
    return this.nextChild(
      0,
      1,
      0,
      4
      /* Side.DontCare */
    );
  }
  get lastChild() {
    return this.nextChild(
      this._tree.children.length - 1,
      -1,
      0,
      4
      /* Side.DontCare */
    );
  }
  childAfter(t) {
    return this.nextChild(
      0,
      1,
      t,
      2
      /* Side.After */
    );
  }
  childBefore(t) {
    return this.nextChild(
      this._tree.children.length - 1,
      -1,
      t,
      -2
      /* Side.Before */
    );
  }
  prop(t) {
    return this._tree.prop(t);
  }
  enter(t, e, i = 0) {
    let s;
    if (!(i & q.IgnoreOverlays) && (s = hi.get(this._tree)) && s.overlay) {
      let r = t - this.from, o = i & q.EnterBracketed && s.bracketed;
      for (let { from: l, to: a } of s.overlay)
        if ((e > 0 || o ? l <= r : l < r) && (e < 0 || o ? a >= r : a > r))
          return new Ct(s.tree, s.overlay[0].from + this.from, -1, this);
    }
    return this.nextChild(0, 1, t, e, i);
  }
  nextSignificantParent() {
    let t = this;
    for (; t.type.isAnonymous && t._parent; )
      t = t._parent;
    return t;
  }
  get parent() {
    return this._parent ? this._parent.nextSignificantParent() : null;
  }
  get nextSibling() {
    return this._parent && this.index >= 0 ? this._parent.nextChild(
      this.index + 1,
      1,
      0,
      4
      /* Side.DontCare */
    ) : null;
  }
  get prevSibling() {
    return this._parent && this.index >= 0 ? this._parent.nextChild(
      this.index - 1,
      -1,
      0,
      4
      /* Side.DontCare */
    ) : null;
  }
  get tree() {
    return this._tree;
  }
  toTree() {
    return this._tree;
  }
  /**
  @internal
  */
  toString() {
    return this._tree.toString();
  }
}
function No(n, t, e, i) {
  let s = n.cursor(), r = [];
  if (!s.firstChild())
    return r;
  if (e != null) {
    for (let o = !1; !o; )
      if (o = s.type.is(e), !s.nextSibling())
        return r;
  }
  for (; ; ) {
    if (i != null && s.type.is(i))
      return r;
    if (s.type.is(t) && r.push(s.node), !s.nextSibling())
      return i == null ? r : [];
  }
}
function _n(n, t, e = t.length - 1) {
  for (let i = n; e >= 0; i = i.parent) {
    if (!i)
      return !1;
    if (!i.type.isAnonymous) {
      if (t[e] && t[e] != i.name)
        return !1;
      e--;
    }
  }
  return !0;
}
class Au {
  constructor(t, e, i, s) {
    this.parent = t, this.buffer = e, this.index = i, this.start = s;
  }
}
class oe extends Na {
  get name() {
    return this.type.name;
  }
  get from() {
    return this.context.start + this.context.buffer.buffer[this.index + 1];
  }
  get to() {
    return this.context.start + this.context.buffer.buffer[this.index + 2];
  }
  constructor(t, e, i) {
    super(), this.context = t, this._parent = e, this.index = i, this.type = t.buffer.set.types[t.buffer.buffer[i]];
  }
  child(t, e, i) {
    let { buffer: s } = this.context, r = s.findChild(this.index + 4, s.buffer[this.index + 3], t, e - this.context.start, i);
    return r < 0 ? null : new oe(this.context, this, r);
  }
  get firstChild() {
    return this.child(
      1,
      0,
      4
      /* Side.DontCare */
    );
  }
  get lastChild() {
    return this.child(
      -1,
      0,
      4
      /* Side.DontCare */
    );
  }
  childAfter(t) {
    return this.child(
      1,
      t,
      2
      /* Side.After */
    );
  }
  childBefore(t) {
    return this.child(
      -1,
      t,
      -2
      /* Side.Before */
    );
  }
  prop(t) {
    return this.type.prop(t);
  }
  enter(t, e, i = 0) {
    if (i & q.ExcludeBuffers)
      return null;
    let { buffer: s } = this.context, r = s.findChild(this.index + 4, s.buffer[this.index + 3], e > 0 ? 1 : -1, t - this.context.start, e);
    return r < 0 ? null : new oe(this.context, this, r);
  }
  get parent() {
    return this._parent || this.context.parent.nextSignificantParent();
  }
  externalSibling(t) {
    return this._parent ? null : this.context.parent.nextChild(
      this.context.index + t,
      t,
      0,
      4
      /* Side.DontCare */
    );
  }
  get nextSibling() {
    let { buffer: t } = this.context, e = t.buffer[this.index + 3];
    return e < (this._parent ? t.buffer[this._parent.index + 3] : t.buffer.length) ? new oe(this.context, this._parent, e) : this.externalSibling(1);
  }
  get prevSibling() {
    let { buffer: t } = this.context, e = this._parent ? this._parent.index + 4 : 0;
    return this.index == e ? this.externalSibling(-1) : new oe(this.context, this._parent, t.findChild(
      e,
      this.index,
      -1,
      0,
      4
      /* Side.DontCare */
    ));
  }
  get tree() {
    return null;
  }
  toTree() {
    let t = [], e = [], { buffer: i } = this.context, s = this.index + 4, r = i.buffer[this.index + 3];
    if (r > s) {
      let o = i.buffer[this.index + 1];
      t.push(i.slice(s, r, o)), e.push(0);
    }
    return new _(this.type, t, e, this.to - this.from);
  }
  /**
  @internal
  */
  toString() {
    return this.context.buffer.childString(this.index);
  }
}
function Va(n) {
  if (!n.length)
    return null;
  let t = 0, e = n[0];
  for (let r = 1; r < n.length; r++) {
    let o = n[r];
    (o.from > e.from || o.to < e.to) && (e = o, t = r);
  }
  let i = e instanceof Ct && e.index < 0 ? null : e.parent, s = n.slice();
  return i ? s[t] = i : s.splice(t, 1), new Tu(s, e);
}
class Tu {
  constructor(t, e) {
    this.heads = t, this.node = e;
  }
  get next() {
    return Va(this.heads);
  }
}
function Mu(n, t, e) {
  let i = n.resolveInner(t, e), s = null;
  for (let r = i instanceof Ct ? i : i.context.parent; r; r = r.parent)
    if (r.index < 0) {
      let o = r.parent;
      (s || (s = [i])).push(o.resolve(t, e)), r = o;
    } else {
      let o = hi.get(r.tree);
      if (o && o.overlay && o.overlay[0].from <= t && o.overlay[o.overlay.length - 1].to >= t) {
        let l = new Ct(o.tree, o.overlay[0].from + r.from, -1, r);
        (s || (s = [i])).push(mi(l, t, e, !1));
      }
    }
  return s ? Va(s) : i;
}
class Gn {
  /**
  Shorthand for `.type.name`.
  */
  get name() {
    return this.type.name;
  }
  /**
  @internal
  */
  constructor(t, e = 0) {
    if (this.buffer = null, this.stack = [], this.index = 0, this.bufferNode = null, this.mode = e & ~q.EnterBracketed, t instanceof Ct)
      this.yieldNode(t);
    else {
      this._tree = t.context.parent, this.buffer = t.context;
      for (let i = t._parent; i; i = i._parent)
        this.stack.unshift(i.index);
      this.bufferNode = t, this.yieldBuf(t.index);
    }
  }
  yieldNode(t) {
    return t ? (this._tree = t, this.type = t.type, this.from = t.from, this.to = t.to, !0) : !1;
  }
  yieldBuf(t, e) {
    this.index = t;
    let { start: i, buffer: s } = this.buffer;
    return this.type = e || s.set.types[s.buffer[t]], this.from = i + s.buffer[t + 1], this.to = i + s.buffer[t + 2], !0;
  }
  /**
  @internal
  */
  yield(t) {
    return t ? t instanceof Ct ? (this.buffer = null, this.yieldNode(t)) : (this.buffer = t.context, this.yieldBuf(t.index, t.type)) : !1;
  }
  /**
  @internal
  */
  toString() {
    return this.buffer ? this.buffer.buffer.childString(this.index) : this._tree.toString();
  }
  /**
  @internal
  */
  enterChild(t, e, i) {
    if (!this.buffer)
      return this.yield(this._tree.nextChild(t < 0 ? this._tree._tree.children.length - 1 : 0, t, e, i, this.mode));
    let { buffer: s } = this.buffer, r = s.findChild(this.index + 4, s.buffer[this.index + 3], t, e - this.buffer.start, i);
    return r < 0 ? !1 : (this.stack.push(this.index), this.yieldBuf(r));
  }
  /**
  Move the cursor to this node's first child. When this returns
  false, the node has no child, and the cursor has not been moved.
  */
  firstChild() {
    return this.enterChild(
      1,
      0,
      4
      /* Side.DontCare */
    );
  }
  /**
  Move the cursor to this node's last child.
  */
  lastChild() {
    return this.enterChild(
      -1,
      0,
      4
      /* Side.DontCare */
    );
  }
  /**
  Move the cursor to the first child that ends after `pos`.
  */
  childAfter(t) {
    return this.enterChild(
      1,
      t,
      2
      /* Side.After */
    );
  }
  /**
  Move to the last child that starts before `pos`.
  */
  childBefore(t) {
    return this.enterChild(
      -1,
      t,
      -2
      /* Side.Before */
    );
  }
  /**
  Move the cursor to the child around `pos`. If side is -1 the
  child may end at that position, when 1 it may start there. This
  will also enter [overlaid](#common.MountedTree.overlay)
  [mounted](#common.NodeProp^mounted) trees unless `overlays` is
  set to false.
  */
  enter(t, e, i = this.mode) {
    return this.buffer ? i & q.ExcludeBuffers ? !1 : this.enterChild(1, t, e) : this.yield(this._tree.enter(t, e, i));
  }
  /**
  Move to the node's parent node, if this isn't the top node.
  */
  parent() {
    if (!this.buffer)
      return this.yieldNode(this.mode & q.IncludeAnonymous ? this._tree._parent : this._tree.parent);
    if (this.stack.length)
      return this.yieldBuf(this.stack.pop());
    let t = this.mode & q.IncludeAnonymous ? this.buffer.parent : this.buffer.parent.nextSignificantParent();
    return this.buffer = null, this.yieldNode(t);
  }
  /**
  @internal
  */
  sibling(t) {
    if (!this.buffer)
      return this._tree._parent ? this.yield(this._tree.index < 0 ? null : this._tree._parent.nextChild(this._tree.index + t, t, 0, 4, this.mode)) : !1;
    let { buffer: e } = this.buffer, i = this.stack.length - 1;
    if (t < 0) {
      let s = i < 0 ? 0 : this.stack[i] + 4;
      if (this.index != s)
        return this.yieldBuf(e.findChild(
          s,
          this.index,
          -1,
          0,
          4
          /* Side.DontCare */
        ));
    } else {
      let s = e.buffer[this.index + 3];
      if (s < (i < 0 ? e.buffer.length : e.buffer[this.stack[i] + 3]))
        return this.yieldBuf(s);
    }
    return i < 0 ? this.yield(this.buffer.parent.nextChild(this.buffer.index + t, t, 0, 4, this.mode)) : !1;
  }
  /**
  Move to this node's next sibling, if any.
  */
  nextSibling() {
    return this.sibling(1);
  }
  /**
  Move to this node's previous sibling, if any.
  */
  prevSibling() {
    return this.sibling(-1);
  }
  atLastNode(t) {
    let e, i, { buffer: s } = this;
    if (s) {
      if (t > 0) {
        if (this.index < s.buffer.buffer.length)
          return !1;
      } else
        for (let r = 0; r < this.index; r++)
          if (s.buffer.buffer[r + 3] < this.index)
            return !1;
      ({ index: e, parent: i } = s);
    } else
      ({ index: e, _parent: i } = this._tree);
    for (; i; { index: e, _parent: i } = i)
      if (e > -1)
        for (let r = e + t, o = t < 0 ? -1 : i._tree.children.length; r != o; r += t) {
          let l = i._tree.children[r];
          if (this.mode & q.IncludeAnonymous || l instanceof ue || !l.type.isAnonymous || Cr(l))
            return !1;
        }
    return !0;
  }
  move(t, e) {
    if (e && this.enterChild(
      t,
      0,
      4
      /* Side.DontCare */
    ))
      return !0;
    for (; ; ) {
      if (this.sibling(t))
        return !0;
      if (this.atLastNode(t) || !this.parent())
        return !1;
    }
  }
  /**
  Move to the next node in a
  [pre-order](https://en.wikipedia.org/wiki/Tree_traversal#Pre-order,_NLR)
  traversal, going from a node to its first child or, if the
  current node is empty or `enter` is false, its next sibling or
  the next sibling of the first parent node that has one.
  */
  next(t = !0) {
    return this.move(1, t);
  }
  /**
  Move to the next node in a last-to-first pre-order traversal. A
  node is followed by its last child or, if it has none, its
  previous sibling or the previous sibling of the first parent
  node that has one.
  */
  prev(t = !0) {
    return this.move(-1, t);
  }
  /**
  Move the cursor to the innermost node that covers `pos`. If
  `side` is -1, it will enter nodes that end at `pos`. If it is 1,
  it will enter nodes that start at `pos`.
  */
  moveTo(t, e = 0) {
    for (; (this.from == this.to || (e < 1 ? this.from >= t : this.from > t) || (e > -1 ? this.to <= t : this.to < t)) && this.parent(); )
      ;
    for (; this.enterChild(1, t, e); )
      ;
    return this;
  }
  /**
  Get a [syntax node](#common.SyntaxNode) at the cursor's current
  position.
  */
  get node() {
    if (!this.buffer)
      return this._tree;
    let t = this.bufferNode, e = null, i = 0;
    if (t && t.context == this.buffer)
      t: for (let s = this.index, r = this.stack.length; r >= 0; ) {
        for (let o = t; o; o = o._parent)
          if (o.index == s) {
            if (s == this.index)
              return o;
            e = o, i = r + 1;
            break t;
          }
        s = this.stack[--r];
      }
    for (let s = i; s < this.stack.length; s++)
      e = new oe(this.buffer, e, this.stack[s]);
    return this.bufferNode = new oe(this.buffer, e, this.index);
  }
  /**
  Get the [tree](#common.Tree) that represents the current node, if
  any. Will return null when the node is in a [tree
  buffer](#common.TreeBuffer).
  */
  get tree() {
    return this.buffer ? null : this._tree._tree;
  }
  /**
  Iterate over the current node and all its descendants, calling
  `enter` when entering a node and `leave`, if given, when leaving
  one. When `enter` returns `false`, any children of that node are
  skipped, and `leave` isn't called for it.
  */
  iterate(t, e) {
    for (let i = 0; ; ) {
      let s = !1;
      if (this.type.isAnonymous || t(this) !== !1) {
        if (this.firstChild()) {
          i++;
          continue;
        }
        this.type.isAnonymous || (s = !0);
      }
      for (; ; ) {
        if (s && e && e(this), s = this.type.isAnonymous, !i)
          return;
        if (this.nextSibling())
          break;
        this.parent(), i--, s = !0;
      }
    }
  }
  /**
  Test whether the current node matches a given context—a sequence
  of direct parent node names. Empty strings in the context array
  are treated as wildcards.
  */
  matchContext(t) {
    if (!this.buffer)
      return _n(this.node.parent, t);
    let { buffer: e } = this.buffer, { types: i } = e.set;
    for (let s = t.length - 1, r = this.stack.length - 1; s >= 0; r--) {
      if (r < 0)
        return _n(this._tree, t, s);
      let o = i[e.buffer[this.stack[r]]];
      if (!o.isAnonymous) {
        if (t[s] && t[s] != o.name)
          return !1;
        s--;
      }
    }
    return !0;
  }
}
function Cr(n) {
  return n.children.some((t) => t instanceof ue || !t.type.isAnonymous || Cr(t));
}
function Pu(n) {
  var t;
  let { buffer: e, nodeSet: i, maxBufferLength: s = Ea, reused: r = [], minRepeatType: o = i.types.length } = n, l = Array.isArray(e) ? new Or(e, e.length) : e, a = i.types, h = 0, f = 0;
  function c(S, T, C, R, W, X) {
    let { id: E, start: D, end: Q, size: j } = l, J = f, te = h;
    if (j < 0)
      if (l.next(), j == -1) {
        let Kt = r[E];
        C.push(Kt), R.push(D - S);
        return;
      } else if (j == -3) {
        h = E;
        return;
      } else if (j == -4) {
        f = E;
        return;
      } else
        throw new RangeError(`Unrecognized record size: ${j}`);
    let _e = a[E], Mi, pe, Wr = D - S;
    if (Q - D <= s && (pe = m(l.pos - T, W))) {
      let Kt = new Uint16Array(pe.size - pe.skip), xt = l.pos - pe.size, It = Kt.length;
      for (; l.pos > xt; )
        It = y(pe.start, Kt, It);
      Mi = new ue(Kt, Q - pe.start, i), Wr = pe.start - S;
    } else {
      let Kt = l.pos - j;
      l.next();
      let xt = [], It = [], ge = E >= o ? E : -1, Me = 0, Pi = Q;
      for (; l.pos > Kt; )
        ge >= 0 && l.id == ge && l.size >= 0 ? (l.end <= Pi - s && (p(xt, It, D, Me, l.end, Pi, ge, J, te), Me = xt.length, Pi = l.end), l.next()) : X > 2500 ? u(D, Kt, xt, It) : c(D, Kt, xt, It, ge, X + 1);
      if (ge >= 0 && Me > 0 && Me < xt.length && p(xt, It, D, Me, D, Pi, ge, J, te), xt.reverse(), It.reverse(), ge > -1 && Me > 0) {
        let Hr = d(_e, te);
        Mi = Ar(_e, xt, It, 0, xt.length, 0, Q - D, Hr, Hr);
      } else
        Mi = g(_e, xt, It, Q - D, J - Q, te);
    }
    C.push(Mi), R.push(Wr);
  }
  function u(S, T, C, R) {
    let W = [], X = 0, E = -1;
    for (; l.pos > T; ) {
      let { id: D, start: Q, end: j, size: J } = l;
      if (J > 4)
        l.next();
      else {
        if (E > -1 && Q < E)
          break;
        E < 0 && (E = j - s), W.push(D, Q, j), X++, l.next();
      }
    }
    if (X) {
      let D = new Uint16Array(X * 4), Q = W[W.length - 2];
      for (let j = W.length - 3, J = 0; j >= 0; j -= 3)
        D[J++] = W[j], D[J++] = W[j + 1] - Q, D[J++] = W[j + 2] - Q, D[J++] = J;
      C.push(new ue(D, W[2] - Q, i)), R.push(Q - S);
    }
  }
  function d(S, T) {
    return (C, R, W) => {
      let X = 0, E = C.length - 1, D, Q;
      if (E >= 0 && (D = C[E]) instanceof _) {
        if (!E && D.type == S && D.length == W)
          return D;
        (Q = D.prop(B.lookAhead)) && (X = R[E] + D.length + Q);
      }
      return g(S, C, R, W, X, T);
    };
  }
  function p(S, T, C, R, W, X, E, D, Q) {
    let j = [], J = [];
    for (; S.length > R; )
      j.push(S.pop()), J.push(T.pop() + C - W);
    S.push(g(i.types[E], j, J, X - W, D - X, Q)), T.push(W - C);
  }
  function g(S, T, C, R, W, X, E) {
    if (X) {
      let D = [B.contextHash, X];
      E = E ? [D].concat(E) : [D];
    }
    if (W > 25) {
      let D = [B.lookAhead, W];
      E = E ? [D].concat(E) : [D];
    }
    return new _(S, T, C, R, E);
  }
  function m(S, T) {
    let C = l.fork(), R = 0, W = 0, X = 0, E = C.end - s, D = { size: 0, start: 0, skip: 0 };
    t: for (let Q = C.pos - S; C.pos > Q; ) {
      let j = C.size;
      if (C.id == T && j >= 0) {
        D.size = R, D.start = W, D.skip = X, X += 4, R += 4, C.next();
        continue;
      }
      let J = C.pos - j;
      if (j < 0 || J < Q || C.start < E)
        break;
      let te = C.id >= o ? 4 : 0, _e = C.start;
      for (C.next(); C.pos > J; ) {
        if (C.size < 0)
          if (C.size == -3 || C.size == -4)
            te += 4;
          else
            break t;
        else C.id >= o && (te += 4);
        C.next();
      }
      W = _e, R += j, X += te;
    }
    return (T < 0 || R == S) && (D.size = R, D.start = W, D.skip = X), D.size > 4 ? D : void 0;
  }
  function y(S, T, C) {
    let { id: R, start: W, end: X, size: E } = l;
    if (l.next(), E >= 0 && R < o) {
      let D = C;
      if (E > 4) {
        let Q = l.pos - (E - 4);
        for (; l.pos > Q; )
          C = y(S, T, C);
      }
      T[--C] = D, T[--C] = X - S, T[--C] = W - S, T[--C] = R;
    } else E == -3 ? h = R : E == -4 && (f = R);
    return C;
  }
  let b = [], x = [];
  for (; l.pos > 0; )
    c(n.start || 0, n.bufferStart || 0, b, x, -1, 0);
  let O = (t = n.length) !== null && t !== void 0 ? t : b.length ? x[0] + b[0].length : 0;
  return new _(a[n.topID], b.reverse(), x.reverse(), O);
}
const Vo = /* @__PURE__ */ new WeakMap();
function rs(n, t) {
  if (!n.isAnonymous || t instanceof ue || t.type != n)
    return 1;
  let e = Vo.get(t);
  if (e == null) {
    e = 1;
    for (let i of t.children) {
      if (i.type != n || !(i instanceof _)) {
        e = 1;
        break;
      }
      e += rs(n, i);
    }
    Vo.set(t, e);
  }
  return e;
}
function Ar(n, t, e, i, s, r, o, l, a) {
  let h = 0;
  for (let p = i; p < s; p++)
    h += rs(n, t[p]);
  let f = Math.ceil(
    h * 1.5 / 8
    /* Balance.BranchFactor */
  ), c = [], u = [];
  function d(p, g, m, y, b) {
    for (let x = m; x < y; ) {
      let O = x, S = g[x], T = rs(n, p[x]);
      for (x++; x < y; x++) {
        let C = rs(n, p[x]);
        if (T + C >= f)
          break;
        T += C;
      }
      if (x == O + 1) {
        if (T > f) {
          let C = p[O];
          d(C.children, C.positions, 0, C.children.length, g[O] + b);
          continue;
        }
        c.push(p[O]);
      } else {
        let C = g[x - 1] + p[x - 1].length - S;
        c.push(Ar(n, p, g, O, x, S, C, null, a));
      }
      u.push(S + b - r);
    }
  }
  return d(t, e, i, s, 0), (l || a)(c, u, o);
}
class Se {
  /**
  Construct a tree fragment. You'll usually want to use
  [`addTree`](#common.TreeFragment^addTree) and
  [`applyChanges`](#common.TreeFragment^applyChanges) instead of
  calling this directly.
  */
  constructor(t, e, i, s, r = !1, o = !1) {
    this.from = t, this.to = e, this.tree = i, this.offset = s, this.open = (r ? 1 : 0) | (o ? 2 : 0);
  }
  /**
  Whether the start of the fragment represents the start of a
  parse, or the end of a change. (In the second case, it may not
  be safe to reuse some nodes at the start, depending on the
  parsing algorithm.)
  */
  get openStart() {
    return (this.open & 1) > 0;
  }
  /**
  Whether the end of the fragment represents the end of a
  full-document parse, or the start of a change.
  */
  get openEnd() {
    return (this.open & 2) > 0;
  }
  /**
  Create a set of fragments from a freshly parsed tree, or update
  an existing set of fragments by replacing the ones that overlap
  with a tree with content from the new tree. When `partial` is
  true, the parse is treated as incomplete, and the resulting
  fragment has [`openEnd`](#common.TreeFragment.openEnd) set to
  true.
  */
  static addTree(t, e = [], i = !1) {
    let s = [new Se(0, t.length, t, 0, !1, i)];
    for (let r of e)
      r.to > t.length && s.push(r);
    return s;
  }
  /**
  Apply a set of edits to an array of fragments, removing or
  splitting fragments as necessary to remove edited ranges, and
  adjusting offsets for fragments that moved.
  */
  static applyChanges(t, e, i = 128) {
    if (!e.length)
      return t;
    let s = [], r = 1, o = t.length ? t[0] : null;
    for (let l = 0, a = 0, h = 0; ; l++) {
      let f = l < e.length ? e[l] : null, c = f ? f.fromA : 1e9;
      if (c - a >= i)
        for (; o && o.from < c; ) {
          let u = o;
          if (a >= u.from || c <= u.to || h) {
            let d = Math.max(u.from, a) - h, p = Math.min(u.to, c) - h;
            u = d >= p ? null : new Se(d, p, u.tree, u.offset + h, l > 0, !!f);
          }
          if (u && s.push(u), o.to > c)
            break;
          o = r < t.length ? t[r++] : null;
        }
      if (!f)
        break;
      a = f.toA, h = f.toA - f.toB;
    }
    return s;
  }
}
class Wa {
  /**
  Start a parse, returning a [partial parse](#common.PartialParse)
  object. [`fragments`](#common.TreeFragment) can be passed in to
  make the parse incremental.
  
  By default, the entire input is parsed. You can pass `ranges`,
  which should be a sorted array of non-empty, non-overlapping
  ranges, to parse only those ranges. The tree returned in that
  case will start at `ranges[0].from`.
  */
  startParse(t, e, i) {
    return typeof t == "string" && (t = new Du(t)), i = i ? i.length ? i.map((s) => new on(s.from, s.to)) : [new on(0, 0)] : [new on(0, t.length)], this.createParse(t, e || [], i);
  }
  /**
  Run a full parse, returning the resulting tree.
  */
  parse(t, e, i) {
    let s = this.startParse(t, e, i);
    for (; ; ) {
      let r = s.advance();
      if (r)
        return r;
    }
  }
}
class Du {
  constructor(t) {
    this.string = t;
  }
  get length() {
    return this.string.length;
  }
  chunk(t) {
    return this.string.slice(t);
  }
  get lineChunks() {
    return !1;
  }
  read(t, e) {
    return this.string.slice(t, e);
  }
}
new B({ perNode: !0 });
let Bu = 0;
class wt {
  /**
  @internal
  */
  constructor(t, e, i, s) {
    this.name = t, this.set = e, this.base = i, this.modified = s, this.id = Bu++;
  }
  toString() {
    let { name: t } = this;
    for (let e of this.modified)
      e.name && (t = `${e.name}(${t})`);
    return t;
  }
  static define(t, e) {
    let i = typeof t == "string" ? t : "?";
    if (t instanceof wt && (e = t), e != null && e.base)
      throw new Error("Can not derive from a modified tag");
    let s = new wt(i, [], null, []);
    if (s.set.push(s), e)
      for (let r of e.set)
        s.set.push(r);
    return s;
  }
  /**
  Define a tag _modifier_, which is a function that, given a tag,
  will return a tag that is a subtag of the original. Applying the
  same modifier to a twice tag will return the same value (`m1(t1)
  == m1(t1)`) and applying multiple modifiers will, regardless or
  order, produce the same tag (`m1(m2(t1)) == m2(m1(t1))`).
  
  When multiple modifiers are applied to a given base tag, each
  smaller set of modifiers is registered as a parent, so that for
  example `m1(m2(m3(t1)))` is a subtype of `m1(m2(t1))`,
  `m1(m3(t1)`, and so on.
  */
  static defineModifier(t) {
    let e = new xs(t);
    return (i) => i.modified.indexOf(e) > -1 ? i : xs.get(i.base || i, i.modified.concat(e).sort((s, r) => s.id - r.id));
  }
}
let Ru = 0;
class xs {
  constructor(t) {
    this.name = t, this.instances = [], this.id = Ru++;
  }
  static get(t, e) {
    if (!e.length)
      return t;
    let i = e[0].instances.find((l) => l.base == t && Lu(e, l.modified));
    if (i)
      return i;
    let s = [], r = new wt(t.name, s, t, e);
    for (let l of e)
      l.instances.push(r);
    let o = Eu(e);
    for (let l of t.set)
      if (!l.modified.length)
        for (let a of o)
          s.push(xs.get(l, a));
    return r;
  }
}
function Lu(n, t) {
  return n.length == t.length && n.every((e, i) => e == t[i]);
}
function Eu(n) {
  let t = [[]];
  for (let e = 0; e < n.length; e++)
    for (let i = 0, s = t.length; i < s; i++)
      t.push(t[i].concat(n[e]));
  return t.sort((e, i) => i.length - e.length);
}
function Ha(n) {
  let t = /* @__PURE__ */ Object.create(null);
  for (let e in n) {
    let i = n[e];
    Array.isArray(i) || (i = [i]);
    for (let s of e.split(" "))
      if (s) {
        let r = [], o = 2, l = s;
        for (let c = 0; ; ) {
          if (l == "..." && c > 0 && c + 3 == s.length) {
            o = 1;
            break;
          }
          let u = /^"(?:[^"\\]|\\.)*?"|[^\/!]+/.exec(l);
          if (!u)
            throw new RangeError("Invalid path: " + s);
          if (r.push(u[0] == "*" ? "" : u[0][0] == '"' ? JSON.parse(u[0]) : u[0]), c += u[0].length, c == s.length)
            break;
          let d = s[c++];
          if (c == s.length && d == "!") {
            o = 0;
            break;
          }
          if (d != "/")
            throw new RangeError("Invalid path: " + s);
          l = s.slice(c);
        }
        let a = r.length - 1, h = r[a];
        if (!h)
          throw new RangeError("Invalid path: " + s);
        let f = new yi(i, o, a > 0 ? r.slice(0, a) : null);
        t[h] = f.sort(t[h]);
      }
  }
  return Fa.add(t);
}
const Fa = new B({
  combine(n, t) {
    let e, i, s;
    for (; n || t; ) {
      if (!n || t && n.depth >= t.depth ? (s = t, t = t.next) : (s = n, n = n.next), e && e.mode == s.mode && !s.context && !e.context)
        continue;
      let r = new yi(s.tags, s.mode, s.context);
      e ? e.next = r : i = r, e = r;
    }
    return i;
  }
});
class yi {
  constructor(t, e, i, s) {
    this.tags = t, this.mode = e, this.context = i, this.next = s;
  }
  get opaque() {
    return this.mode == 0;
  }
  get inherit() {
    return this.mode == 1;
  }
  sort(t) {
    return !t || t.depth < this.depth ? (this.next = t, this) : (t.next = this.sort(t.next), t);
  }
  get depth() {
    return this.context ? this.context.length : 0;
  }
}
yi.empty = new yi([], 2, null);
function za(n, t) {
  let e = /* @__PURE__ */ Object.create(null);
  for (let r of n)
    if (!Array.isArray(r.tag))
      e[r.tag.id] = r.class;
    else
      for (let o of r.tag)
        e[o.id] = r.class;
  let { scope: i, all: s = null } = t || {};
  return {
    style: (r) => {
      let o = s;
      for (let l of r)
        for (let a of l.set) {
          let h = e[a.id];
          if (h) {
            o = o ? o + " " + h : h;
            break;
          }
        }
      return o;
    },
    scope: i
  };
}
function Iu(n, t) {
  let e = null;
  for (let i of n) {
    let s = i.style(t);
    s && (e = e ? e + " " + s : s);
  }
  return e;
}
function Nu(n, t, e, i = 0, s = n.length) {
  let r = new Vu(i, Array.isArray(t) ? t : [t], e);
  r.highlightRange(n.cursor(), i, s, "", r.highlighters), r.flush(s);
}
class Vu {
  constructor(t, e, i) {
    this.at = t, this.highlighters = e, this.span = i, this.class = "";
  }
  startSpan(t, e) {
    e != this.class && (this.flush(t), t > this.at && (this.at = t), this.class = e);
  }
  flush(t) {
    t > this.at && this.class && this.span(this.at, t, this.class);
  }
  highlightRange(t, e, i, s, r) {
    let { type: o, from: l, to: a } = t;
    if (l >= i || a <= e)
      return;
    o.isTop && (r = this.highlighters.filter((d) => !d.scope || d.scope(o)));
    let h = s, f = Wu(t) || yi.empty, c = Iu(r, f.tags);
    if (c && (h && (h += " "), h += c, f.mode == 1 && (s += (s ? " " : "") + c)), this.startSpan(Math.max(e, l), h), f.opaque)
      return;
    let u = t.tree && t.tree.prop(B.mounted);
    if (u && u.overlay) {
      let d = t.node.enter(u.overlay[0].from + l, 1), p = this.highlighters.filter((m) => !m.scope || m.scope(u.tree.type)), g = t.firstChild();
      for (let m = 0, y = l; ; m++) {
        let b = m < u.overlay.length ? u.overlay[m] : null, x = b ? b.from + l : a, O = Math.max(e, y), S = Math.min(i, x);
        if (O < S && g)
          for (; t.from < S && (this.highlightRange(t, O, S, s, r), this.startSpan(Math.min(S, t.to), h), !(t.to >= x || !t.nextSibling())); )
            ;
        if (!b || x > i)
          break;
        y = b.to + l, y > e && (this.highlightRange(d.cursor(), Math.max(e, b.from + l), Math.min(i, y), "", p), this.startSpan(Math.min(i, y), h));
      }
      g && t.parent();
    } else if (t.firstChild()) {
      u && (s = "");
      do
        if (!(t.to <= e)) {
          if (t.from >= i)
            break;
          this.highlightRange(t, e, i, s, r), this.startSpan(Math.min(i, t.to), h);
        }
      while (t.nextSibling());
      t.parent();
    }
  }
}
function Wu(n) {
  let t = n.type.prop(Fa);
  for (; t && t.context && !n.matchContext(t.context); )
    t = t.next;
  return t || null;
}
const v = wt.define, Xi = v(), ee = v(), Wo = v(ee), Ho = v(ee), ie = v(), qi = v(ie), ln = v(ie), Ht = v(), me = v(Ht), Vt = v(), Wt = v(), Yn = v(), Ze = v(Yn), Ui = v(), w = {
  /**
  A comment.
  */
  comment: Xi,
  /**
  A line [comment](#highlight.tags.comment).
  */
  lineComment: v(Xi),
  /**
  A block [comment](#highlight.tags.comment).
  */
  blockComment: v(Xi),
  /**
  A documentation [comment](#highlight.tags.comment).
  */
  docComment: v(Xi),
  /**
  Any kind of identifier.
  */
  name: ee,
  /**
  The [name](#highlight.tags.name) of a variable.
  */
  variableName: v(ee),
  /**
  A type [name](#highlight.tags.name).
  */
  typeName: Wo,
  /**
  A tag name (subtag of [`typeName`](#highlight.tags.typeName)).
  */
  tagName: v(Wo),
  /**
  A property or field [name](#highlight.tags.name).
  */
  propertyName: Ho,
  /**
  An attribute name (subtag of [`propertyName`](#highlight.tags.propertyName)).
  */
  attributeName: v(Ho),
  /**
  The [name](#highlight.tags.name) of a class.
  */
  className: v(ee),
  /**
  A label [name](#highlight.tags.name).
  */
  labelName: v(ee),
  /**
  A namespace [name](#highlight.tags.name).
  */
  namespace: v(ee),
  /**
  The [name](#highlight.tags.name) of a macro.
  */
  macroName: v(ee),
  /**
  A literal value.
  */
  literal: ie,
  /**
  A string [literal](#highlight.tags.literal).
  */
  string: qi,
  /**
  A documentation [string](#highlight.tags.string).
  */
  docString: v(qi),
  /**
  A character literal (subtag of [string](#highlight.tags.string)).
  */
  character: v(qi),
  /**
  An attribute value (subtag of [string](#highlight.tags.string)).
  */
  attributeValue: v(qi),
  /**
  A number [literal](#highlight.tags.literal).
  */
  number: ln,
  /**
  An integer [number](#highlight.tags.number) literal.
  */
  integer: v(ln),
  /**
  A floating-point [number](#highlight.tags.number) literal.
  */
  float: v(ln),
  /**
  A boolean [literal](#highlight.tags.literal).
  */
  bool: v(ie),
  /**
  Regular expression [literal](#highlight.tags.literal).
  */
  regexp: v(ie),
  /**
  An escape [literal](#highlight.tags.literal), for example a
  backslash escape in a string.
  */
  escape: v(ie),
  /**
  A color [literal](#highlight.tags.literal).
  */
  color: v(ie),
  /**
  A URL [literal](#highlight.tags.literal).
  */
  url: v(ie),
  /**
  A language keyword.
  */
  keyword: Vt,
  /**
  The [keyword](#highlight.tags.keyword) for the self or this
  object.
  */
  self: v(Vt),
  /**
  The [keyword](#highlight.tags.keyword) for null.
  */
  null: v(Vt),
  /**
  A [keyword](#highlight.tags.keyword) denoting some atomic value.
  */
  atom: v(Vt),
  /**
  A [keyword](#highlight.tags.keyword) that represents a unit.
  */
  unit: v(Vt),
  /**
  A modifier [keyword](#highlight.tags.keyword).
  */
  modifier: v(Vt),
  /**
  A [keyword](#highlight.tags.keyword) that acts as an operator.
  */
  operatorKeyword: v(Vt),
  /**
  A control-flow related [keyword](#highlight.tags.keyword).
  */
  controlKeyword: v(Vt),
  /**
  A [keyword](#highlight.tags.keyword) that defines something.
  */
  definitionKeyword: v(Vt),
  /**
  A [keyword](#highlight.tags.keyword) related to defining or
  interfacing with modules.
  */
  moduleKeyword: v(Vt),
  /**
  An operator.
  */
  operator: Wt,
  /**
  An [operator](#highlight.tags.operator) that dereferences something.
  */
  derefOperator: v(Wt),
  /**
  Arithmetic-related [operator](#highlight.tags.operator).
  */
  arithmeticOperator: v(Wt),
  /**
  Logical [operator](#highlight.tags.operator).
  */
  logicOperator: v(Wt),
  /**
  Bit [operator](#highlight.tags.operator).
  */
  bitwiseOperator: v(Wt),
  /**
  Comparison [operator](#highlight.tags.operator).
  */
  compareOperator: v(Wt),
  /**
  [Operator](#highlight.tags.operator) that updates its operand.
  */
  updateOperator: v(Wt),
  /**
  [Operator](#highlight.tags.operator) that defines something.
  */
  definitionOperator: v(Wt),
  /**
  Type-related [operator](#highlight.tags.operator).
  */
  typeOperator: v(Wt),
  /**
  Control-flow [operator](#highlight.tags.operator).
  */
  controlOperator: v(Wt),
  /**
  Program or markup punctuation.
  */
  punctuation: Yn,
  /**
  [Punctuation](#highlight.tags.punctuation) that separates
  things.
  */
  separator: v(Yn),
  /**
  Bracket-style [punctuation](#highlight.tags.punctuation).
  */
  bracket: Ze,
  /**
  Angle [brackets](#highlight.tags.bracket) (usually `<` and `>`
  tokens).
  */
  angleBracket: v(Ze),
  /**
  Square [brackets](#highlight.tags.bracket) (usually `[` and `]`
  tokens).
  */
  squareBracket: v(Ze),
  /**
  Parentheses (usually `(` and `)` tokens). Subtag of
  [bracket](#highlight.tags.bracket).
  */
  paren: v(Ze),
  /**
  Braces (usually `{` and `}` tokens). Subtag of
  [bracket](#highlight.tags.bracket).
  */
  brace: v(Ze),
  /**
  Content, for example plain text in XML or markup documents.
  */
  content: Ht,
  /**
  [Content](#highlight.tags.content) that represents a heading.
  */
  heading: me,
  /**
  A level 1 [heading](#highlight.tags.heading).
  */
  heading1: v(me),
  /**
  A level 2 [heading](#highlight.tags.heading).
  */
  heading2: v(me),
  /**
  A level 3 [heading](#highlight.tags.heading).
  */
  heading3: v(me),
  /**
  A level 4 [heading](#highlight.tags.heading).
  */
  heading4: v(me),
  /**
  A level 5 [heading](#highlight.tags.heading).
  */
  heading5: v(me),
  /**
  A level 6 [heading](#highlight.tags.heading).
  */
  heading6: v(me),
  /**
  A prose [content](#highlight.tags.content) separator (such as a horizontal rule).
  */
  contentSeparator: v(Ht),
  /**
  [Content](#highlight.tags.content) that represents a list.
  */
  list: v(Ht),
  /**
  [Content](#highlight.tags.content) that represents a quote.
  */
  quote: v(Ht),
  /**
  [Content](#highlight.tags.content) that is emphasized.
  */
  emphasis: v(Ht),
  /**
  [Content](#highlight.tags.content) that is styled strong.
  */
  strong: v(Ht),
  /**
  [Content](#highlight.tags.content) that is part of a link.
  */
  link: v(Ht),
  /**
  [Content](#highlight.tags.content) that is styled as code or
  monospace.
  */
  monospace: v(Ht),
  /**
  [Content](#highlight.tags.content) that has a strike-through
  style.
  */
  strikethrough: v(Ht),
  /**
  Inserted text in a change-tracking format.
  */
  inserted: v(),
  /**
  Deleted text.
  */
  deleted: v(),
  /**
  Changed text.
  */
  changed: v(),
  /**
  An invalid or unsyntactic element.
  */
  invalid: v(),
  /**
  Metadata or meta-instruction.
  */
  meta: Ui,
  /**
  [Metadata](#highlight.tags.meta) that applies to the entire
  document.
  */
  documentMeta: v(Ui),
  /**
  [Metadata](#highlight.tags.meta) that annotates or adds
  attributes to a given syntactic element.
  */
  annotation: v(Ui),
  /**
  Processing instruction or preprocessor directive. Subtag of
  [meta](#highlight.tags.meta).
  */
  processingInstruction: v(Ui),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates that a
  given element is being defined. Expected to be used with the
  various [name](#highlight.tags.name) tags.
  */
  definition: wt.defineModifier("definition"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates that
  something is constant. Mostly expected to be used with
  [variable names](#highlight.tags.variableName).
  */
  constant: wt.defineModifier("constant"),
  /**
  [Modifier](#highlight.Tag^defineModifier) used to indicate that
  a [variable](#highlight.tags.variableName) or [property
  name](#highlight.tags.propertyName) is being called or defined
  as a function.
  */
  function: wt.defineModifier("function"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that can be applied to
  [names](#highlight.tags.name) to indicate that they belong to
  the language's standard environment.
  */
  standard: wt.defineModifier("standard"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates a given
  [names](#highlight.tags.name) is local to some scope.
  */
  local: wt.defineModifier("local"),
  /**
  A generic variant [modifier](#highlight.Tag^defineModifier) that
  can be used to tag language-specific alternative variants of
  some common tag. It is recommended for themes to define special
  forms of at least the [string](#highlight.tags.string) and
  [variable name](#highlight.tags.variableName) tags, since those
  come up a lot.
  */
  special: wt.defineModifier("special")
};
for (let n in w) {
  let t = w[n];
  t instanceof wt && (t.name = n);
}
za([
  { tag: w.link, class: "tok-link" },
  { tag: w.heading, class: "tok-heading" },
  { tag: w.emphasis, class: "tok-emphasis" },
  { tag: w.strong, class: "tok-strong" },
  { tag: w.keyword, class: "tok-keyword" },
  { tag: w.atom, class: "tok-atom" },
  { tag: w.bool, class: "tok-bool" },
  { tag: w.url, class: "tok-url" },
  { tag: w.labelName, class: "tok-labelName" },
  { tag: w.inserted, class: "tok-inserted" },
  { tag: w.deleted, class: "tok-deleted" },
  { tag: w.literal, class: "tok-literal" },
  { tag: w.string, class: "tok-string" },
  { tag: w.number, class: "tok-number" },
  { tag: [w.regexp, w.escape, w.special(w.string)], class: "tok-string2" },
  { tag: w.variableName, class: "tok-variableName" },
  { tag: w.local(w.variableName), class: "tok-variableName tok-local" },
  { tag: w.definition(w.variableName), class: "tok-variableName tok-definition" },
  { tag: w.special(w.variableName), class: "tok-variableName2" },
  { tag: w.definition(w.propertyName), class: "tok-propertyName tok-definition" },
  { tag: w.typeName, class: "tok-typeName" },
  { tag: w.namespace, class: "tok-namespace" },
  { tag: w.className, class: "tok-className" },
  { tag: w.macroName, class: "tok-macroName" },
  { tag: w.propertyName, class: "tok-propertyName" },
  { tag: w.operator, class: "tok-operator" },
  { tag: w.comment, class: "tok-comment" },
  { tag: w.meta, class: "tok-meta" },
  { tag: w.invalid, class: "tok-invalid" },
  { tag: w.punctuation, class: "tok-punctuation" }
]);
var an;
const Re = /* @__PURE__ */ new B();
function Hu(n) {
  return M.define({
    combine: n ? (t) => t.concat(n) : void 0
  });
}
const Fu = /* @__PURE__ */ new B();
class Dt {
  /**
  Construct a language object. If you need to invoke this
  directly, first define a data facet with
  [`defineLanguageFacet`](https://codemirror.net/6/docs/ref/#language.defineLanguageFacet), and then
  configure your parser to [attach](https://codemirror.net/6/docs/ref/#language.languageDataProp) it
  to the language's outer syntax node.
  */
  constructor(t, e, i = [], s = "") {
    this.data = t, this.name = s, V.prototype.hasOwnProperty("tree") || Object.defineProperty(V.prototype, "tree", { get() {
      return bt(this);
    } }), this.parser = e, this.extension = [
      qe.of(this),
      V.languageData.of((r, o, l) => {
        let a = Fo(r, o, l), h = a.type.prop(Re);
        if (!h)
          return [];
        let f = r.facet(h), c = a.type.prop(Fu);
        if (c) {
          let u = a.resolve(o - a.from, l);
          for (let d of c)
            if (d.test(u, r)) {
              let p = r.facet(d.facet);
              return d.type == "replace" ? p : p.concat(f);
            }
        }
        return f;
      })
    ].concat(i);
  }
  /**
  Query whether this language is active at the given position.
  */
  isActiveAt(t, e, i = -1) {
    return Fo(t, e, i).type.prop(Re) == this.data;
  }
  /**
  Find the document regions that were parsed using this language.
  The returned regions will _include_ any nested languages rooted
  in this language, when those exist.
  */
  findRegions(t) {
    let e = t.facet(qe);
    if ((e == null ? void 0 : e.data) == this.data)
      return [{ from: 0, to: t.doc.length }];
    if (!e || !e.allowsNesting)
      return [];
    let i = [], s = (r, o) => {
      if (r.prop(Re) == this.data) {
        i.push({ from: o, to: o + r.length });
        return;
      }
      let l = r.prop(B.mounted);
      if (l) {
        if (l.tree.prop(Re) == this.data) {
          if (l.overlay)
            for (let a of l.overlay)
              i.push({ from: a.from + o, to: a.to + o });
          else
            i.push({ from: o, to: o + r.length });
          return;
        } else if (l.overlay) {
          let a = i.length;
          if (s(l.tree, l.overlay[0].from + o), i.length > a)
            return;
        }
      }
      for (let a = 0; a < r.children.length; a++) {
        let h = r.children[a];
        h instanceof _ && s(h, r.positions[a] + o);
      }
    };
    return s(bt(t), 0), i;
  }
  /**
  Indicates whether this language allows nested languages. The
  default implementation returns true.
  */
  get allowsNesting() {
    return !0;
  }
}
Dt.setState = /* @__PURE__ */ N.define();
function Fo(n, t, e) {
  let i = n.facet(qe), s = bt(n).topNode;
  if (!i || i.allowsNesting)
    for (let r = s; r; r = r.enter(t, e, q.ExcludeBuffers | q.EnterBracketed))
      r.type.isTop && (s = r);
  return s;
}
class ws extends Dt {
  constructor(t, e, i) {
    super(t, e, [], i), this.parser = e;
  }
  /**
  Define a language from a parser.
  */
  static define(t) {
    let e = Hu(t.languageData);
    return new ws(e, t.parser.configure({
      props: [Re.add((i) => i.isTop ? e : void 0)]
    }), t.name);
  }
  /**
  Create a new instance of this language with a reconfigured
  version of its parser and optionally a new name.
  */
  configure(t, e) {
    return new ws(this.data, this.parser.configure(t), e || this.name);
  }
  get allowsNesting() {
    return this.parser.hasWrappers();
  }
}
function bt(n) {
  let t = n.field(Dt.state, !1);
  return t ? t.tree : _.empty;
}
class zu {
  /**
  Create an input object for the given document.
  */
  constructor(t) {
    this.doc = t, this.cursorPos = 0, this.string = "", this.cursor = t.iter();
  }
  get length() {
    return this.doc.length;
  }
  syncTo(t) {
    return this.string = this.cursor.next(t - this.cursorPos).value, this.cursorPos = t + this.string.length, this.cursorPos - this.string.length;
  }
  chunk(t) {
    return this.syncTo(t), this.string;
  }
  get lineChunks() {
    return !0;
  }
  read(t, e) {
    let i = this.cursorPos - this.string.length;
    return t < i || e >= this.cursorPos ? this.doc.sliceString(t, e) : this.string.slice(t - i, e - i);
  }
}
let Je = null;
class ks {
  constructor(t, e, i = [], s, r, o, l, a) {
    this.parser = t, this.state = e, this.fragments = i, this.tree = s, this.treeLen = r, this.viewport = o, this.skipped = l, this.scheduleOn = a, this.parse = null, this.tempSkipped = [];
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new ks(t, e, [], _.empty, 0, i, [], null);
  }
  startParse() {
    return this.parser.startParse(new zu(this.state.doc), this.fragments);
  }
  /**
  @internal
  */
  work(t, e) {
    return e != null && e >= this.state.doc.length && (e = void 0), this.tree != _.empty && this.isDone(e ?? this.state.doc.length) ? (this.takeTree(), !0) : this.withContext(() => {
      var i;
      if (typeof t == "number") {
        let s = Date.now() + t;
        t = () => Date.now() > s;
      }
      for (this.parse || (this.parse = this.startParse()), e != null && (this.parse.stoppedAt == null || this.parse.stoppedAt > e) && e < this.state.doc.length && this.parse.stopAt(e); ; ) {
        let s = this.parse.advance();
        if (s)
          if (this.fragments = this.withoutTempSkipped(Se.addTree(s, this.fragments, this.parse.stoppedAt != null)), this.treeLen = (i = this.parse.stoppedAt) !== null && i !== void 0 ? i : this.state.doc.length, this.tree = s, this.parse = null, this.treeLen < (e ?? this.state.doc.length))
            this.parse = this.startParse();
          else
            return !0;
        if (t())
          return !1;
      }
    });
  }
  /**
  @internal
  */
  takeTree() {
    let t, e;
    this.parse && (t = this.parse.parsedPos) >= this.treeLen && ((this.parse.stoppedAt == null || this.parse.stoppedAt > t) && this.parse.stopAt(t), this.withContext(() => {
      for (; !(e = this.parse.advance()); )
        ;
    }), this.treeLen = t, this.tree = e, this.fragments = this.withoutTempSkipped(Se.addTree(this.tree, this.fragments, !0)), this.parse = null);
  }
  withContext(t) {
    let e = Je;
    Je = this;
    try {
      return t();
    } finally {
      Je = e;
    }
  }
  withoutTempSkipped(t) {
    for (let e; e = this.tempSkipped.pop(); )
      t = zo(t, e.from, e.to);
    return t;
  }
  /**
  @internal
  */
  changes(t, e) {
    let { fragments: i, tree: s, treeLen: r, viewport: o, skipped: l } = this;
    if (this.takeTree(), !t.empty) {
      let a = [];
      if (t.iterChangedRanges((h, f, c, u) => a.push({ fromA: h, toA: f, fromB: c, toB: u })), i = Se.applyChanges(i, a), s = _.empty, r = 0, o = { from: t.mapPos(o.from, -1), to: t.mapPos(o.to, 1) }, this.skipped.length) {
        l = [];
        for (let h of this.skipped) {
          let f = t.mapPos(h.from, 1), c = t.mapPos(h.to, -1);
          f < c && l.push({ from: f, to: c });
        }
      }
    }
    return new ks(this.parser, e, i, s, r, o, l, this.scheduleOn);
  }
  /**
  @internal
  */
  updateViewport(t) {
    if (this.viewport.from == t.from && this.viewport.to == t.to)
      return !1;
    this.viewport = t;
    let e = this.skipped.length;
    for (let i = 0; i < this.skipped.length; i++) {
      let { from: s, to: r } = this.skipped[i];
      s < t.to && r > t.from && (this.fragments = zo(this.fragments, s, r), this.skipped.splice(i--, 1));
    }
    return this.skipped.length >= e ? !1 : (this.reset(), !0);
  }
  /**
  @internal
  */
  reset() {
    this.parse && (this.takeTree(), this.parse = null);
  }
  /**
  Notify the parse scheduler that the given region was skipped
  because it wasn't in view, and the parse should be restarted
  when it comes into view.
  */
  skipUntilInView(t, e) {
    this.skipped.push({ from: t, to: e });
  }
  /**
  Returns a parser intended to be used as placeholder when
  asynchronously loading a nested parser. It'll skip its input and
  mark it as not-really-parsed, so that the next update will parse
  it again.
  
  When `until` is given, a reparse will be scheduled when that
  promise resolves.
  */
  static getSkippingParser(t) {
    return new class extends Wa {
      createParse(e, i, s) {
        let r = s[0].from, o = s[s.length - 1].to;
        return {
          parsedPos: r,
          advance() {
            let a = Je;
            if (a) {
              for (let h of s)
                a.tempSkipped.push(h);
              t && (a.scheduleOn = a.scheduleOn ? Promise.all([a.scheduleOn, t]) : t);
            }
            return this.parsedPos = o, new _(gt.none, [], [], o - r);
          },
          stoppedAt: null,
          stopAt() {
          }
        };
      }
    }();
  }
  /**
  @internal
  */
  isDone(t) {
    t = Math.min(t, this.state.doc.length);
    let e = this.fragments;
    return this.treeLen >= t && e.length && e[0].from == 0 && e[0].to >= t;
  }
  /**
  Get the context for the current parse, or `null` if no editor
  parse is in progress.
  */
  static get() {
    return Je;
  }
}
function zo(n, t, e) {
  return Se.applyChanges(n, [{ fromA: t, toA: e, fromB: t, toB: e }]);
}
class Xe {
  constructor(t) {
    this.context = t, this.tree = t.tree;
  }
  apply(t) {
    if (!t.docChanged && this.tree == this.context.tree)
      return this;
    let e = this.context.changes(t.changes, t.state), i = this.context.treeLen == t.startState.doc.length ? void 0 : Math.max(t.changes.mapPos(this.context.treeLen), e.viewport.to);
    return e.work(20, i) || e.takeTree(), new Xe(e);
  }
  static init(t) {
    let e = Math.min(3e3, t.doc.length), i = ks.create(t.facet(qe).parser, t, { from: 0, to: e });
    return i.work(20, e) || i.takeTree(), new Xe(i);
  }
}
Dt.state = /* @__PURE__ */ At.define({
  create: Xe.init,
  update(n, t) {
    for (let e of t.effects)
      if (e.is(Dt.setState))
        return e.value;
    return t.startState.facet(qe) != t.state.facet(qe) ? Xe.init(t.state) : n.apply(t);
  }
});
let Qa = (n) => {
  let t = setTimeout(
    () => n(),
    500
    /* Work.MaxPause */
  );
  return () => clearTimeout(t);
};
typeof requestIdleCallback < "u" && (Qa = (n) => {
  let t = -1, e = setTimeout(
    () => {
      t = requestIdleCallback(n, {
        timeout: 400
        /* Work.MinPause */
      });
    },
    100
    /* Work.MinPause */
  );
  return () => t < 0 ? clearTimeout(e) : cancelIdleCallback(t);
});
const hn = typeof navigator < "u" && (!((an = navigator.scheduling) === null || an === void 0) && an.isInputPending) ? () => navigator.scheduling.isInputPending() : null, Qu = /* @__PURE__ */ dt.fromClass(class {
  constructor(t) {
    this.view = t, this.working = null, this.workScheduled = 0, this.chunkEnd = -1, this.chunkBudget = -1, this.work = this.work.bind(this), this.scheduleWork();
  }
  update(t) {
    let e = this.view.state.field(Dt.state).context;
    (e.updateViewport(t.view.viewport) || this.view.viewport.to > e.treeLen) && this.scheduleWork(), (t.docChanged || t.selectionSet) && (this.view.hasFocus && (this.chunkBudget += 50), this.scheduleWork()), this.checkAsyncSchedule(e);
  }
  scheduleWork() {
    if (this.working)
      return;
    let { state: t } = this.view, e = t.field(Dt.state);
    (e.tree != e.context.tree || !e.context.isDone(t.doc.length)) && (this.working = Qa(this.work));
  }
  work(t) {
    this.working = null;
    let e = Date.now();
    if (this.chunkEnd < e && (this.chunkEnd < 0 || this.view.hasFocus) && (this.chunkEnd = e + 3e4, this.chunkBudget = 3e3), this.chunkBudget <= 0)
      return;
    let { state: i, viewport: { to: s } } = this.view, r = i.field(Dt.state);
    if (r.tree == r.context.tree && r.context.isDone(
      s + 1e5
      /* Work.MaxParseAhead */
    ))
      return;
    let o = Date.now() + Math.min(this.chunkBudget, 100, t && !hn ? Math.max(25, t.timeRemaining() - 5) : 1e9), l = r.context.treeLen < s && i.doc.length > s + 1e3, a = r.context.work(() => hn && hn() || Date.now() > o, s + (l ? 0 : 1e5));
    this.chunkBudget -= Date.now() - e, (a || this.chunkBudget <= 0) && (r.context.takeTree(), this.view.dispatch({ effects: Dt.setState.of(new Xe(r.context)) })), this.chunkBudget > 0 && !(a && !l) && this.scheduleWork(), this.checkAsyncSchedule(r.context);
  }
  checkAsyncSchedule(t) {
    t.scheduleOn && (this.workScheduled++, t.scheduleOn.then(() => this.scheduleWork()).catch((e) => ft(this.view.state, e)).then(() => this.workScheduled--), t.scheduleOn = null);
  }
  destroy() {
    this.working && this.working();
  }
  isWorking() {
    return !!(this.working || this.workScheduled > 0);
  }
}, {
  eventHandlers: { focus() {
    this.scheduleWork();
  } }
}), qe = /* @__PURE__ */ M.define({
  combine(n) {
    return n.length ? n[0] : null;
  },
  enables: (n) => [
    Dt.state,
    Qu,
    P.contentAttributes.compute([n], (t) => {
      let e = t.facet(n);
      return e && e.name ? { "data-language": e.name } : {};
    })
  ]
});
class $u {
  /**
  Create a language support object.
  */
  constructor(t, e = []) {
    this.language = t, this.support = e, this.extension = [t, e];
  }
}
const ju = /* @__PURE__ */ M.define(), Tr = /* @__PURE__ */ M.define({
  combine: (n) => {
    if (!n.length)
      return "  ";
    let t = n[0];
    if (!t || /\S/.test(t) || Array.from(t).some((e) => e != t[0]))
      throw new Error("Invalid indent unit: " + JSON.stringify(n[0]));
    return t;
  }
});
function vs(n) {
  let t = n.facet(Tr);
  return t.charCodeAt(0) == 9 ? n.tabSize * t.length : t.length;
}
function Ss(n, t) {
  let e = "", i = n.tabSize, s = n.facet(Tr)[0];
  if (s == "	") {
    for (; t >= i; )
      e += "	", t -= i;
    s = " ";
  }
  for (let r = 0; r < t; r++)
    e += s;
  return e;
}
function $a(n, t) {
  n instanceof V && (n = new Hs(n));
  for (let i of n.state.facet(ju)) {
    let s = i(n, t);
    if (s !== void 0)
      return s;
  }
  let e = bt(n.state);
  return e.length >= t ? qu(n, e, t) : null;
}
class Hs {
  /**
  Create an indent context.
  */
  constructor(t, e = {}) {
    this.state = t, this.options = e, this.unit = vs(t);
  }
  /**
  Get a description of the line at the given position, taking
  [simulated line
  breaks](https://codemirror.net/6/docs/ref/#language.IndentContext.constructor^options.simulateBreak)
  into account. If there is such a break at `pos`, the `bias`
  argument determines whether the part of the line line before or
  after the break is used.
  */
  lineAt(t, e = 1) {
    let i = this.state.doc.lineAt(t), { simulateBreak: s, simulateDoubleBreak: r } = this.options;
    return s != null && s >= i.from && s <= i.to ? r && s == t ? { text: "", from: t } : (e < 0 ? s < t : s <= t) ? { text: i.text.slice(s - i.from), from: s } : { text: i.text.slice(0, s - i.from), from: i.from } : i;
  }
  /**
  Get the text directly after `pos`, either the entire line
  or the next 100 characters, whichever is shorter.
  */
  textAfterPos(t, e = 1) {
    if (this.options.simulateDoubleBreak && t == this.options.simulateBreak)
      return "";
    let { text: i, from: s } = this.lineAt(t, e);
    return i.slice(t - s, Math.min(i.length, t + 100 - s));
  }
  /**
  Find the column for the given position.
  */
  column(t, e = 1) {
    let { text: i, from: s } = this.lineAt(t, e), r = this.countColumn(i, t - s), o = this.options.overrideIndentation ? this.options.overrideIndentation(s) : -1;
    return o > -1 && (r += o - this.countColumn(i, i.search(/\S|$/))), r;
  }
  /**
  Find the column position (taking tabs into account) of the given
  position in the given string.
  */
  countColumn(t, e = t.length) {
    return Rs(t, this.state.tabSize, e);
  }
  /**
  Find the indentation column of the line at the given point.
  */
  lineIndent(t, e = 1) {
    let { text: i, from: s } = this.lineAt(t, e), r = this.options.overrideIndentation;
    if (r) {
      let o = r(s);
      if (o > -1)
        return o;
    }
    return this.countColumn(i, i.search(/\S|$/));
  }
  /**
  Returns the [simulated line
  break](https://codemirror.net/6/docs/ref/#language.IndentContext.constructor^options.simulateBreak)
  for this context, if any.
  */
  get simulatedBreak() {
    return this.options.simulateBreak || null;
  }
}
const Xu = /* @__PURE__ */ new B();
function qu(n, t, e) {
  let i = t.resolveStack(e), s = t.resolveInner(e, -1).resolve(e, 0).enterUnfinishedNodesBefore(e);
  if (s != i.node) {
    let r = [];
    for (let o = s; o && !(o.from < i.node.from || o.to > i.node.to || o.from == i.node.from && o.type == i.node.type); o = o.parent)
      r.push(o);
    for (let o = r.length - 1; o >= 0; o--)
      i = { node: r[o], next: i };
  }
  return ja(i, n, e);
}
function ja(n, t, e) {
  for (let i = n; i; i = i.next) {
    let s = Ku(i.node);
    if (s)
      return s(Mr.create(t, e, i));
  }
  return 0;
}
function Uu(n) {
  return n.pos == n.options.simulateBreak && n.options.simulateDoubleBreak;
}
function Ku(n) {
  let t = n.type.prop(Xu);
  if (t)
    return t;
  let e = n.firstChild, i;
  if (e && (i = e.type.prop(B.closedBy))) {
    let s = n.lastChild, r = s && i.indexOf(s.name) > -1;
    return (o) => Zu(o, !0, 1, void 0, r && !Uu(o) ? s.from : void 0);
  }
  return n.parent == null ? _u : null;
}
function _u() {
  return 0;
}
class Mr extends Hs {
  constructor(t, e, i) {
    super(t.state, t.options), this.base = t, this.pos = e, this.context = i;
  }
  /**
  The syntax tree node to which the indentation strategy
  applies.
  */
  get node() {
    return this.context.node;
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new Mr(t, e, i);
  }
  /**
  Get the text directly after `this.pos`, either the entire line
  or the next 100 characters, whichever is shorter.
  */
  get textAfter() {
    return this.textAfterPos(this.pos);
  }
  /**
  Get the indentation at the reference line for `this.node`, which
  is the line on which it starts, unless there is a node that is
  _not_ a parent of this node covering the start of that line. If
  so, the line at the start of that node is tried, again skipping
  on if it is covered by another such node.
  */
  get baseIndent() {
    return this.baseIndentFor(this.node);
  }
  /**
  Get the indentation for the reference line of the given node
  (see [`baseIndent`](https://codemirror.net/6/docs/ref/#language.TreeIndentContext.baseIndent)).
  */
  baseIndentFor(t) {
    let e = this.state.doc.lineAt(t.from);
    for (; ; ) {
      let i = t.resolve(e.from);
      for (; i.parent && i.parent.from == i.from; )
        i = i.parent;
      if (Gu(i, t))
        break;
      e = this.state.doc.lineAt(i.from);
    }
    return this.lineIndent(e.from);
  }
  /**
  Continue looking for indentations in the node's parent nodes,
  and return the result of that.
  */
  continue() {
    return ja(this.context.next, this.base, this.pos);
  }
}
function Gu(n, t) {
  for (let e = t; e; e = e.parent)
    if (n == e)
      return !0;
  return !1;
}
function Yu(n) {
  let t = n.node, e = t.childAfter(t.from), i = t.lastChild;
  if (!e)
    return null;
  let s = n.options.simulateBreak, r = n.state.doc.lineAt(e.from), o = s == null || s <= r.from ? r.to : Math.min(r.to, s);
  for (let l = e.to; ; ) {
    let a = t.childAfter(l);
    if (!a || a == i)
      return null;
    if (!a.type.isSkipped) {
      if (a.from >= o)
        return null;
      let h = /^ */.exec(r.text.slice(e.to - r.from))[0].length;
      return { from: e.from, to: e.to + h };
    }
    l = a.to;
  }
}
function Zu(n, t, e, i, s) {
  let r = n.textAfter, o = r.match(/^\s*/)[0].length, l = i && r.slice(o, o + i.length) == i || s == n.pos + o, a = Yu(n);
  return a ? l ? n.column(a.from) : n.column(a.to) : n.baseIndent + (l ? 0 : n.unit * e);
}
class Fs {
  constructor(t, e) {
    this.specs = t;
    let i;
    function s(l) {
      let a = ae.newName();
      return (i || (i = /* @__PURE__ */ Object.create(null)))["." + a] = l, a;
    }
    const r = typeof e.all == "string" ? e.all : e.all ? s(e.all) : void 0, o = e.scope;
    this.scope = o instanceof Dt ? (l) => l.prop(Re) == o.data : o ? (l) => l == o : void 0, this.style = za(t.map((l) => ({
      tag: l.tag,
      class: l.class || s(Object.assign({}, l, { tag: null }))
    })), {
      all: r
    }).style, this.module = i ? new ae(i) : null, this.themeType = e.themeType;
  }
  /**
  Create a highlighter style that associates the given styles to
  the given tags. The specs must be objects that hold a style tag
  or array of tags in their `tag` property, and either a single
  `class` property providing a static CSS class (for highlighter
  that rely on external styling), or a
  [`style-mod`](https://github.com/marijnh/style-mod#documentation)-style
  set of CSS properties (which define the styling for those tags).
  
  The CSS rules created for a highlighter will be emitted in the
  order of the spec's properties. That means that for elements that
  have multiple tags associated with them, styles defined further
  down in the list will have a higher CSS precedence than styles
  defined earlier.
  */
  static define(t, e) {
    return new Fs(t, e || {});
  }
}
const Zn = /* @__PURE__ */ M.define(), Ju = /* @__PURE__ */ M.define({
  combine(n) {
    return n.length ? [n[0]] : null;
  }
});
function fn(n) {
  let t = n.facet(Zn);
  return t.length ? t : n.facet(Ju);
}
function td(n, t) {
  let e = [id], i;
  return n instanceof Fs && (n.module && e.push(P.styleModule.of(n.module)), i = n.themeType), i ? e.push(Zn.computeN([P.darkTheme], (s) => s.facet(P.darkTheme) == (i == "dark") ? [n] : [])) : e.push(Zn.of(n)), e;
}
class ed {
  constructor(t) {
    this.markCache = /* @__PURE__ */ Object.create(null), this.tree = bt(t.state), this.decorations = this.buildDeco(t, fn(t.state)), this.decoratedTo = t.viewport.to;
  }
  update(t) {
    let e = bt(t.state), i = fn(t.state), s = i != fn(t.startState), { viewport: r } = t.view, o = t.changes.mapPos(this.decoratedTo, 1);
    e.length < r.to && !s && e.type == this.tree.type && o >= r.to ? (this.decorations = this.decorations.map(t.changes), this.decoratedTo = o) : (e != this.tree || t.viewportChanged || s) && (this.tree = e, this.decorations = this.buildDeco(t.view, i), this.decoratedTo = r.to);
  }
  buildDeco(t, e) {
    if (!e || !this.tree.length)
      return H.none;
    let i = new ze();
    for (let { from: s, to: r } of t.visibleRanges)
      Nu(this.tree, e, (o, l, a) => {
        i.add(o, l, this.markCache[a] || (this.markCache[a] = H.mark({ class: a })));
      }, s, r);
    return i.finish();
  }
}
const id = /* @__PURE__ */ wi.high(/* @__PURE__ */ dt.fromClass(ed, {
  decorations: (n) => n.decorations
})), sd = /* @__PURE__ */ Fs.define([
  {
    tag: w.meta,
    color: "#404740"
  },
  {
    tag: w.link,
    textDecoration: "underline"
  },
  {
    tag: w.heading,
    textDecoration: "underline",
    fontWeight: "bold"
  },
  {
    tag: w.emphasis,
    fontStyle: "italic"
  },
  {
    tag: w.strong,
    fontWeight: "bold"
  },
  {
    tag: w.strikethrough,
    textDecoration: "line-through"
  },
  {
    tag: w.keyword,
    color: "#708"
  },
  {
    tag: [w.atom, w.bool, w.url, w.contentSeparator, w.labelName],
    color: "#219"
  },
  {
    tag: [w.literal, w.inserted],
    color: "#164"
  },
  {
    tag: [w.string, w.deleted],
    color: "#a11"
  },
  {
    tag: [w.regexp, w.escape, /* @__PURE__ */ w.special(w.string)],
    color: "#e40"
  },
  {
    tag: /* @__PURE__ */ w.definition(w.variableName),
    color: "#00f"
  },
  {
    tag: /* @__PURE__ */ w.local(w.variableName),
    color: "#30a"
  },
  {
    tag: [w.typeName, w.namespace],
    color: "#085"
  },
  {
    tag: w.className,
    color: "#167"
  },
  {
    tag: [/* @__PURE__ */ w.special(w.variableName), w.macroName],
    color: "#256"
  },
  {
    tag: /* @__PURE__ */ w.definition(w.propertyName),
    color: "#00c"
  },
  {
    tag: w.comment,
    color: "#940"
  },
  {
    tag: w.invalid,
    color: "#f00"
  }
]), nd = /* @__PURE__ */ P.baseTheme({
  "&.cm-focused .cm-matchingBracket": { backgroundColor: "#328c8252" },
  "&.cm-focused .cm-nonmatchingBracket": { backgroundColor: "#bb555544" }
}), Xa = 1e4, qa = "()[]{}", Ua = /* @__PURE__ */ M.define({
  combine(n) {
    return vi(n, {
      afterCursor: !0,
      brackets: qa,
      maxScanDistance: Xa,
      renderMatch: ld
    });
  }
}), rd = /* @__PURE__ */ H.mark({ class: "cm-matchingBracket" }), od = /* @__PURE__ */ H.mark({ class: "cm-nonmatchingBracket" });
function ld(n) {
  let t = [], e = n.matched ? rd : od;
  return t.push(e.range(n.start.from, n.start.to)), n.end && t.push(e.range(n.end.from, n.end.to)), t;
}
function Qo(n) {
  let t = [], e = n.facet(Ua);
  for (let i of n.selection.ranges) {
    if (!i.empty)
      continue;
    let s = Xt(n, i.head, -1, e) || i.head > 0 && Xt(n, i.head - 1, 1, e) || e.afterCursor && (Xt(n, i.head, 1, e) || i.head < n.doc.length && Xt(n, i.head + 1, -1, e));
    s && (t = t.concat(e.renderMatch(s, n)));
  }
  return H.set(t, !0);
}
const ad = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.paused = !1, this.decorations = Qo(n.state);
  }
  update(n) {
    (n.docChanged || n.selectionSet || this.paused) && (n.view.composing ? (this.decorations = this.decorations.map(n.changes), this.paused = !0) : (this.decorations = Qo(n.state), this.paused = !1));
  }
}, {
  decorations: (n) => n.decorations
}), hd = [
  ad,
  nd
];
function fd(n = {}) {
  return [Ua.of(n), hd];
}
const cd = /* @__PURE__ */ new B();
function Jn(n, t, e) {
  let i = n.prop(t < 0 ? B.openedBy : B.closedBy);
  if (i)
    return i;
  if (n.name.length == 1) {
    let s = e.indexOf(n.name);
    if (s > -1 && s % 2 == (t < 0 ? 1 : 0))
      return [e[s + t]];
  }
  return null;
}
function tr(n) {
  let t = n.type.prop(cd);
  return t ? t(n.node) : n;
}
function Xt(n, t, e, i = {}) {
  let s = i.maxScanDistance || Xa, r = i.brackets || qa, o = bt(n), l = o.resolveInner(t, e);
  for (let a = l; a; a = a.parent) {
    let h = Jn(a.type, e, r);
    if (h && a.from < a.to) {
      let f = tr(a);
      if (f && (e > 0 ? t >= f.from && t < f.to : t > f.from && t <= f.to))
        return ud(n, t, e, a, f, h, r);
    }
  }
  return dd(n, t, e, o, l.type, s, r);
}
function ud(n, t, e, i, s, r, o) {
  let l = i.parent, a = { from: s.from, to: s.to }, h = 0, f = l == null ? void 0 : l.cursor();
  if (f && (e < 0 ? f.childBefore(i.from) : f.childAfter(i.to)))
    do
      if (e < 0 ? f.to <= i.from : f.from >= i.to) {
        if (h == 0 && r.indexOf(f.type.name) > -1 && f.from < f.to) {
          let c = tr(f);
          return { start: a, end: c ? { from: c.from, to: c.to } : void 0, matched: !0 };
        } else if (Jn(f.type, e, o))
          h++;
        else if (Jn(f.type, -e, o)) {
          if (h == 0) {
            let c = tr(f);
            return {
              start: a,
              end: c && c.from < c.to ? { from: c.from, to: c.to } : void 0,
              matched: !1
            };
          }
          h--;
        }
      }
    while (e < 0 ? f.prevSibling() : f.nextSibling());
  return { start: a, matched: !1 };
}
function dd(n, t, e, i, s, r, o) {
  let l = e < 0 ? n.sliceDoc(t - 1, t) : n.sliceDoc(t, t + 1), a = o.indexOf(l);
  if (a < 0 || a % 2 == 0 != e > 0)
    return null;
  let h = { from: e < 0 ? t - 1 : t, to: e > 0 ? t + 1 : t }, f = n.doc.iterRange(t, e > 0 ? n.doc.length : 0), c = 0;
  for (let u = 0; !f.next().done && u <= r; ) {
    let d = f.value;
    e < 0 && (u += d.length);
    let p = t + u * e;
    for (let g = e > 0 ? 0 : d.length - 1, m = e > 0 ? d.length : -1; g != m; g += e) {
      let y = o.indexOf(d[g]);
      if (!(y < 0 || i.resolveInner(p + g, 1).type != s))
        if (y % 2 == 0 == e > 0)
          c++;
        else {
          if (c == 1)
            return { start: h, end: { from: p + g, to: p + g + 1 }, matched: y >> 1 == a >> 1 };
          c--;
        }
    }
    e > 0 && (u += d.length);
  }
  return f.done ? { start: h, matched: !1 } : null;
}
const pd = /* @__PURE__ */ Object.create(null), $o = [gt.none], jo = [], Xo = /* @__PURE__ */ Object.create(null), gd = /* @__PURE__ */ Object.create(null);
for (let [n, t] of [
  ["variable", "variableName"],
  ["variable-2", "variableName.special"],
  ["string-2", "string.special"],
  ["def", "variableName.definition"],
  ["tag", "tagName"],
  ["attribute", "attributeName"],
  ["type", "typeName"],
  ["builtin", "variableName.standard"],
  ["qualifier", "modifier"],
  ["error", "invalid"],
  ["header", "heading"],
  ["property", "propertyName"]
])
  gd[n] = /* @__PURE__ */ md(pd, t);
function cn(n, t) {
  jo.indexOf(n) > -1 || (jo.push(n), console.warn(t));
}
function md(n, t) {
  let e = [];
  for (let l of t.split(" ")) {
    let a = [];
    for (let h of l.split(".")) {
      let f = n[h] || w[h];
      f ? typeof f == "function" ? a.length ? a = a.map(f) : cn(h, `Modifier ${h} used at start of tag`) : a.length ? cn(h, `Tag ${h} used as modifier`) : a = Array.isArray(f) ? f : [f] : cn(h, `Unknown highlighting tag ${h}`);
    }
    for (let h of a)
      e.push(h);
  }
  if (!e.length)
    return 0;
  let i = t.replace(/ /g, "_"), s = i + " " + e.map((l) => l.id), r = Xo[s];
  if (r)
    return r.id;
  let o = Xo[s] = gt.define({
    id: $o.length,
    name: i,
    props: [Ha({ [i]: e })]
  });
  return $o.push(o), o.id;
}
$.RTL, $.LTR;
const yd = (n) => {
  let { state: t } = n, e = t.doc.lineAt(t.selection.main.from), i = Dr(n.state, e.from);
  return i.line ? bd(n) : i.block ? wd(n) : !1;
};
function Pr(n, t) {
  return ({ state: e, dispatch: i }) => {
    if (e.readOnly)
      return !1;
    let s = n(t, e);
    return s ? (i(e.update(s)), !0) : !1;
  };
}
const bd = /* @__PURE__ */ Pr(
  Sd,
  0
  /* CommentOption.Toggle */
), xd = /* @__PURE__ */ Pr(
  Ka,
  0
  /* CommentOption.Toggle */
), wd = /* @__PURE__ */ Pr(
  (n, t) => Ka(n, t, vd(t)),
  0
  /* CommentOption.Toggle */
);
function Dr(n, t) {
  let e = n.languageDataAt("commentTokens", t, 1);
  return e.length ? e[0] : {};
}
const ti = 50;
function kd(n, { open: t, close: e }, i, s) {
  let r = n.sliceDoc(i - ti, i), o = n.sliceDoc(s, s + ti), l = /\s*$/.exec(r)[0].length, a = /^\s*/.exec(o)[0].length, h = r.length - l;
  if (r.slice(h - t.length, h) == t && o.slice(a, a + e.length) == e)
    return {
      open: { pos: i - l, margin: l && 1 },
      close: { pos: s + a, margin: a && 1 }
    };
  let f, c;
  s - i <= 2 * ti ? f = c = n.sliceDoc(i, s) : (f = n.sliceDoc(i, i + ti), c = n.sliceDoc(s - ti, s));
  let u = /^\s*/.exec(f)[0].length, d = /\s*$/.exec(c)[0].length, p = c.length - d - e.length;
  return f.slice(u, u + t.length) == t && c.slice(p, p + e.length) == e ? {
    open: {
      pos: i + u + t.length,
      margin: /\s/.test(f.charAt(u + t.length)) ? 1 : 0
    },
    close: {
      pos: s - d - e.length,
      margin: /\s/.test(c.charAt(p - 1)) ? 1 : 0
    }
  } : null;
}
function vd(n) {
  let t = [];
  for (let e of n.selection.ranges) {
    let i = n.doc.lineAt(e.from), s = e.to <= i.to ? i : n.doc.lineAt(e.to);
    s.from > i.from && s.from == e.to && (s = e.to == i.to + 1 ? i : n.doc.lineAt(e.to - 1));
    let r = t.length - 1;
    r >= 0 && t[r].to > i.from ? t[r].to = s.to : t.push({ from: i.from + /^\s*/.exec(i.text)[0].length, to: s.to });
  }
  return t;
}
function Ka(n, t, e = t.selection.ranges) {
  let i = e.map((r) => Dr(t, r.from).block);
  if (!i.every((r) => r))
    return null;
  let s = e.map((r, o) => kd(t, i[o], r.from, r.to));
  if (n != 2 && !s.every((r) => r))
    return { changes: t.changes(e.map((r, o) => s[o] ? [] : [{ from: r.from, insert: i[o].open + " " }, { from: r.to, insert: " " + i[o].close }])) };
  if (n != 1 && s.some((r) => r)) {
    let r = [];
    for (let o = 0, l; o < s.length; o++)
      if (l = s[o]) {
        let a = i[o], { open: h, close: f } = l;
        r.push({ from: h.pos - a.open.length, to: h.pos + h.margin }, { from: f.pos - f.margin, to: f.pos + a.close.length });
      }
    return { changes: r };
  }
  return null;
}
function Sd(n, t, e = t.selection.ranges) {
  let i = [], s = -1;
  t: for (let { from: r, to: o } of e) {
    let l = i.length, a = 1e9, h;
    for (let f = r; f <= o; ) {
      let c = t.doc.lineAt(f);
      if (h == null && (h = Dr(t, c.from).line, !h))
        continue t;
      if (c.from > s && (r == o || o > c.from)) {
        s = c.from;
        let u = /^\s*/.exec(c.text)[0].length, d = u == c.length, p = c.text.slice(u, u + h.length) == h ? u : -1;
        u < c.text.length && u < a && (a = u), i.push({ line: c, comment: p, token: h, indent: u, empty: d, single: !1 });
      }
      f = c.to + 1;
    }
    if (a < 1e9)
      for (let f = l; f < i.length; f++)
        i[f].indent < i[f].line.text.length && (i[f].indent = a);
    i.length == l + 1 && (i[l].single = !0);
  }
  if (n != 2 && i.some((r) => r.comment < 0 && (!r.empty || r.single))) {
    let r = [];
    for (let { line: l, token: a, indent: h, empty: f, single: c } of i)
      (c || !f) && r.push({ from: l.from + h, insert: a + " " });
    let o = t.changes(r);
    return { changes: o, selection: t.selection.map(o, 1) };
  } else if (n != 1 && i.some((r) => r.comment >= 0)) {
    let r = [];
    for (let { line: o, comment: l, token: a } of i)
      if (l >= 0) {
        let h = o.from + l, f = h + a.length;
        o.text[f - o.from] == " " && f++, r.push({ from: h, to: f });
      }
    return { changes: r };
  }
  return null;
}
const er = /* @__PURE__ */ Jt.define(), Od = /* @__PURE__ */ Jt.define(), Cd = /* @__PURE__ */ M.define(), _a = /* @__PURE__ */ M.define({
  combine(n) {
    return vi(n, {
      minDepth: 100,
      newGroupDelay: 500,
      joinToEvent: (t, e) => e
    }, {
      minDepth: Math.max,
      newGroupDelay: Math.min,
      joinToEvent: (t, e) => (i, s) => t(i, s) || e(i, s)
    });
  }
}), Ga = /* @__PURE__ */ At.define({
  create() {
    return qt.empty;
  },
  update(n, t) {
    let e = t.state.facet(_a), i = t.annotation(er);
    if (i) {
      let a = ct.fromTransaction(t, i.selection), h = i.side, f = h == 0 ? n.undone : n.done;
      return a ? f = Os(f, f.length, e.minDepth, a) : f = Ja(f, t.startState.selection), new qt(h == 0 ? i.rest : f, h == 0 ? f : i.rest);
    }
    let s = t.annotation(Od);
    if ((s == "full" || s == "before") && (n = n.isolate()), t.annotation(Y.addToHistory) === !1)
      return t.changes.empty ? n : n.addMapping(t.changes.desc);
    let r = ct.fromTransaction(t), o = t.annotation(Y.time), l = t.annotation(Y.userEvent);
    return r ? n = n.addChanges(r, o, l, e, t) : t.selection && (n = n.addSelection(t.startState.selection, o, l, e.newGroupDelay)), (s == "full" || s == "after") && (n = n.isolate()), n;
  },
  toJSON(n) {
    return { done: n.done.map((t) => t.toJSON()), undone: n.undone.map((t) => t.toJSON()) };
  },
  fromJSON(n) {
    return new qt(n.done.map(ct.fromJSON), n.undone.map(ct.fromJSON));
  }
});
function Ad(n = {}) {
  return [
    Ga,
    _a.of(n),
    P.domEventHandlers({
      beforeinput(t, e) {
        let i = t.inputType == "historyUndo" ? Ya : t.inputType == "historyRedo" ? ir : null;
        return i ? (t.preventDefault(), i(e)) : !1;
      }
    })
  ];
}
function zs(n, t) {
  return function({ state: e, dispatch: i }) {
    if (!t && e.readOnly)
      return !1;
    let s = e.field(Ga, !1);
    if (!s)
      return !1;
    let r = s.pop(n, e, t);
    return r ? (i(r), !0) : !1;
  };
}
const Ya = /* @__PURE__ */ zs(0, !1), ir = /* @__PURE__ */ zs(1, !1), Td = /* @__PURE__ */ zs(0, !0), Md = /* @__PURE__ */ zs(1, !0);
class ct {
  constructor(t, e, i, s, r) {
    this.changes = t, this.effects = e, this.mapped = i, this.startSelection = s, this.selectionsAfter = r;
  }
  setSelAfter(t) {
    return new ct(this.changes, this.effects, this.mapped, this.startSelection, t);
  }
  toJSON() {
    var t, e, i;
    return {
      changes: (t = this.changes) === null || t === void 0 ? void 0 : t.toJSON(),
      mapped: (e = this.mapped) === null || e === void 0 ? void 0 : e.toJSON(),
      startSelection: (i = this.startSelection) === null || i === void 0 ? void 0 : i.toJSON(),
      selectionsAfter: this.selectionsAfter.map((s) => s.toJSON())
    };
  }
  static fromJSON(t) {
    return new ct(t.changes && G.fromJSON(t.changes), [], t.mapped && Ut.fromJSON(t.mapped), t.startSelection && k.fromJSON(t.startSelection), t.selectionsAfter.map(k.fromJSON));
  }
  // This does not check `addToHistory` and such, it assumes the
  // transaction needs to be converted to an item. Returns null when
  // there are no changes or effects in the transaction.
  static fromTransaction(t, e) {
    let i = St;
    for (let s of t.startState.facet(Cd)) {
      let r = s(t);
      r.length && (i = i.concat(r));
    }
    return !i.length && t.changes.empty ? null : new ct(t.changes.invert(t.startState.doc), i, void 0, e || t.startState.selection, St);
  }
  static selection(t) {
    return new ct(void 0, St, void 0, void 0, t);
  }
}
function Os(n, t, e, i) {
  let s = t + 1 > e + 20 ? t - e - 1 : 0, r = n.slice(s, t);
  return r.push(i), r;
}
function Pd(n, t) {
  let e = [], i = !1;
  return n.iterChangedRanges((s, r) => e.push(s, r)), t.iterChangedRanges((s, r, o, l) => {
    for (let a = 0; a < e.length; ) {
      let h = e[a++], f = e[a++];
      l >= h && o <= f && (i = !0);
    }
  }), i;
}
function Dd(n, t) {
  return n.ranges.length == t.ranges.length && n.ranges.filter((e, i) => e.empty != t.ranges[i].empty).length === 0;
}
function Za(n, t) {
  return n.length ? t.length ? n.concat(t) : n : t;
}
const St = [], Bd = 200;
function Ja(n, t) {
  if (n.length) {
    let e = n[n.length - 1], i = e.selectionsAfter.slice(Math.max(0, e.selectionsAfter.length - Bd));
    return i.length && i[i.length - 1].eq(t) ? n : (i.push(t), Os(n, n.length - 1, 1e9, e.setSelAfter(i)));
  } else
    return [ct.selection([t])];
}
function Rd(n) {
  let t = n[n.length - 1], e = n.slice();
  return e[n.length - 1] = t.setSelAfter(t.selectionsAfter.slice(0, t.selectionsAfter.length - 1)), e;
}
function un(n, t) {
  if (!n.length)
    return n;
  let e = n.length, i = St;
  for (; e; ) {
    let s = Ld(n[e - 1], t, i);
    if (s.changes && !s.changes.empty || s.effects.length) {
      let r = n.slice(0, e);
      return r[e - 1] = s, r;
    } else
      t = s.mapped, e--, i = s.selectionsAfter;
  }
  return i.length ? [ct.selection(i)] : St;
}
function Ld(n, t, e) {
  let i = Za(n.selectionsAfter.length ? n.selectionsAfter.map((l) => l.map(t)) : St, e);
  if (!n.changes)
    return ct.selection(i);
  let s = n.changes.map(t), r = t.mapDesc(n.changes, !0), o = n.mapped ? n.mapped.composeDesc(r) : r;
  return new ct(s, N.mapEffects(n.effects, t), o, n.startSelection.map(r), i);
}
const Ed = /^(input\.type|delete)($|\.)/;
class qt {
  constructor(t, e, i = 0, s = void 0) {
    this.done = t, this.undone = e, this.prevTime = i, this.prevUserEvent = s;
  }
  isolate() {
    return this.prevTime ? new qt(this.done, this.undone) : this;
  }
  addChanges(t, e, i, s, r) {
    let o = this.done, l = o[o.length - 1];
    return l && l.changes && !l.changes.empty && t.changes && (!i || Ed.test(i)) && (!l.selectionsAfter.length && e - this.prevTime < s.newGroupDelay && s.joinToEvent(r, Pd(l.changes, t.changes)) || // For compose (but not compose.start) events, always join with previous event
    i == "input.type.compose") ? o = Os(o, o.length - 1, s.minDepth, new ct(t.changes.compose(l.changes), Za(N.mapEffects(t.effects, l.changes), l.effects), l.mapped, l.startSelection, St)) : o = Os(o, o.length, s.minDepth, t), new qt(o, St, e, i);
  }
  addSelection(t, e, i, s) {
    let r = this.done.length ? this.done[this.done.length - 1].selectionsAfter : St;
    return r.length > 0 && e - this.prevTime < s && i == this.prevUserEvent && i && /^select($|\.)/.test(i) && Dd(r[r.length - 1], t) ? this : new qt(Ja(this.done, t), this.undone, e, i);
  }
  addMapping(t) {
    return new qt(un(this.done, t), un(this.undone, t), this.prevTime, this.prevUserEvent);
  }
  pop(t, e, i) {
    let s = t == 0 ? this.done : this.undone;
    if (s.length == 0)
      return null;
    let r = s[s.length - 1], o = r.selectionsAfter[0] || (r.startSelection ? r.startSelection.map(r.changes.invertedDesc, 1) : e.selection);
    if (i && r.selectionsAfter.length)
      return e.update({
        selection: r.selectionsAfter[r.selectionsAfter.length - 1],
        annotations: er.of({ side: t, rest: Rd(s), selection: o }),
        userEvent: t == 0 ? "select.undo" : "select.redo",
        scrollIntoView: !0
      });
    if (r.changes) {
      let l = s.length == 1 ? St : s.slice(0, s.length - 1);
      return r.mapped && (l = un(l, r.mapped)), e.update({
        changes: r.changes,
        selection: r.startSelection,
        effects: r.effects,
        annotations: er.of({ side: t, rest: l, selection: o }),
        filter: !1,
        userEvent: t == 0 ? "undo" : "redo",
        scrollIntoView: !0
      });
    } else
      return null;
  }
}
qt.empty = /* @__PURE__ */ new qt(St, St);
const Id = [
  { key: "Mod-z", run: Ya, preventDefault: !0 },
  { key: "Mod-y", mac: "Mod-Shift-z", run: ir, preventDefault: !0 },
  { linux: "Ctrl-Shift-z", run: ir, preventDefault: !0 },
  { key: "Mod-u", run: Td, preventDefault: !0 },
  { key: "Alt-u", mac: "Mod-Shift-u", run: Md, preventDefault: !0 }
];
function Ke(n, t) {
  return k.create(n.ranges.map(t), n.mainIndex);
}
function Lt(n, t) {
  return n.update({ selection: t, scrollIntoView: !0, userEvent: "select" });
}
function Et({ state: n, dispatch: t }, e) {
  let i = Ke(n.selection, e);
  return i.eq(n.selection, !0) ? !1 : (t(Lt(n, i)), !0);
}
function Qs(n, t) {
  return k.cursor(t ? n.to : n.from);
}
function th(n, t) {
  return Et(n, (e) => e.empty ? n.moveByChar(e, t) : Qs(e, t));
}
function nt(n) {
  return n.textDirectionAt(n.state.selection.main.head) == $.LTR;
}
const eh = (n) => th(n, !nt(n)), ih = (n) => th(n, nt(n));
function sh(n, t) {
  return Et(n, (e) => e.empty ? n.moveByGroup(e, t) : Qs(e, t));
}
const Nd = (n) => sh(n, !nt(n)), Vd = (n) => sh(n, nt(n));
function Wd(n, t, e) {
  if (t.type.prop(e))
    return !0;
  let i = t.to - t.from;
  return i && (i > 2 || /[^\s,.;:]/.test(n.sliceDoc(t.from, t.to))) || t.firstChild;
}
function $s(n, t, e) {
  let i = bt(n).resolveInner(t.head), s = e ? B.closedBy : B.openedBy;
  for (let a = t.head; ; ) {
    let h = e ? i.childAfter(a) : i.childBefore(a);
    if (!h)
      break;
    Wd(n, h, s) ? i = h : a = e ? h.to : h.from;
  }
  let r = i.type.prop(s), o, l;
  return r && (o = e ? Xt(n, i.from, 1) : Xt(n, i.to, -1)) && o.matched ? l = e ? o.end.to : o.end.from : l = e ? i.to : i.from, k.cursor(l, e ? -1 : 1);
}
const Hd = (n) => Et(n, (t) => $s(n.state, t, !nt(n))), Fd = (n) => Et(n, (t) => $s(n.state, t, nt(n)));
function nh(n, t) {
  return Et(n, (e) => {
    if (!e.empty)
      return Qs(e, t);
    let i = n.moveVertically(e, t);
    return i.head != e.head ? i : n.moveToLineBoundary(e, t);
  });
}
const rh = (n) => nh(n, !1), oh = (n) => nh(n, !0);
function lh(n) {
  let t = n.scrollDOM.clientHeight < n.scrollDOM.scrollHeight - 2, e = 0, i = 0, s;
  if (t) {
    for (let r of n.state.facet(P.scrollMargins)) {
      let o = r(n);
      o != null && o.top && (e = Math.max(o == null ? void 0 : o.top, e)), o != null && o.bottom && (i = Math.max(o == null ? void 0 : o.bottom, i));
    }
    s = n.scrollDOM.clientHeight - e - i;
  } else
    s = (n.dom.ownerDocument.defaultView || window).innerHeight;
  return {
    marginTop: e,
    marginBottom: i,
    selfScroll: t,
    height: Math.max(n.defaultLineHeight, s - 5)
  };
}
function ah(n, t) {
  let e = lh(n), { state: i } = n, s = Ke(i.selection, (o) => o.empty ? n.moveVertically(o, t, e.height) : Qs(o, t));
  if (s.eq(i.selection))
    return !1;
  let r;
  if (e.selfScroll) {
    let o = n.coordsAtPos(i.selection.main.head), l = n.scrollDOM.getBoundingClientRect(), a = l.top + e.marginTop, h = l.bottom - e.marginBottom;
    o && o.top > a && o.bottom < h && (r = P.scrollIntoView(s.main.head, { y: "start", yMargin: o.top - a }));
  }
  return n.dispatch(Lt(i, s), { effects: r }), !0;
}
const qo = (n) => ah(n, !1), sr = (n) => ah(n, !0);
function de(n, t, e) {
  let i = n.lineBlockAt(t.head), s = n.moveToLineBoundary(t, e);
  if (s.head == t.head && s.head != (e ? i.to : i.from) && (s = n.moveToLineBoundary(t, e, !1)), !e && s.head == i.from && i.length) {
    let r = /^\s*/.exec(n.state.sliceDoc(i.from, Math.min(i.from + 100, i.to)))[0].length;
    r && t.head != i.from + r && (s = k.cursor(i.from + r));
  }
  return s;
}
const zd = (n) => Et(n, (t) => de(n, t, !0)), Qd = (n) => Et(n, (t) => de(n, t, !1)), $d = (n) => Et(n, (t) => de(n, t, !nt(n))), jd = (n) => Et(n, (t) => de(n, t, nt(n))), Xd = (n) => Et(n, (t) => k.cursor(n.lineBlockAt(t.head).from, 1)), qd = (n) => Et(n, (t) => k.cursor(n.lineBlockAt(t.head).to, -1));
function Ud(n, t, e) {
  let i = !1, s = Ke(n.selection, (r) => {
    let o = Xt(n, r.head, -1) || Xt(n, r.head, 1) || r.head > 0 && Xt(n, r.head - 1, 1) || r.head < n.doc.length && Xt(n, r.head + 1, -1);
    if (!o || !o.end)
      return r;
    i = !0;
    let l = o.start.from == r.head ? o.end.to : o.end.from;
    return k.cursor(l);
  });
  return i ? (t(Lt(n, s)), !0) : !1;
}
const Kd = ({ state: n, dispatch: t }) => Ud(n, t);
function Tt(n, t) {
  let e = Ke(n.state.selection, (i) => {
    let s = t(i);
    return k.range(i.anchor, s.head, s.goalColumn, s.bidiLevel || void 0);
  });
  return e.eq(n.state.selection) ? !1 : (n.dispatch(Lt(n.state, e)), !0);
}
function hh(n, t) {
  return Tt(n, (e) => n.moveByChar(e, t));
}
const fh = (n) => hh(n, !nt(n)), ch = (n) => hh(n, nt(n));
function uh(n, t) {
  return Tt(n, (e) => n.moveByGroup(e, t));
}
const _d = (n) => uh(n, !nt(n)), Gd = (n) => uh(n, nt(n)), Yd = (n) => Tt(n, (t) => $s(n.state, t, !nt(n))), Zd = (n) => Tt(n, (t) => $s(n.state, t, nt(n)));
function dh(n, t) {
  return Tt(n, (e) => n.moveVertically(e, t));
}
const ph = (n) => dh(n, !1), gh = (n) => dh(n, !0);
function mh(n, t) {
  return Tt(n, (e) => n.moveVertically(e, t, lh(n).height));
}
const Uo = (n) => mh(n, !1), Ko = (n) => mh(n, !0), Jd = (n) => Tt(n, (t) => de(n, t, !0)), tp = (n) => Tt(n, (t) => de(n, t, !1)), ep = (n) => Tt(n, (t) => de(n, t, !nt(n))), ip = (n) => Tt(n, (t) => de(n, t, nt(n))), sp = (n) => Tt(n, (t) => k.cursor(n.lineBlockAt(t.head).from)), np = (n) => Tt(n, (t) => k.cursor(n.lineBlockAt(t.head).to)), _o = ({ state: n, dispatch: t }) => (t(Lt(n, { anchor: 0 })), !0), Go = ({ state: n, dispatch: t }) => (t(Lt(n, { anchor: n.doc.length })), !0), Yo = ({ state: n, dispatch: t }) => (t(Lt(n, { anchor: n.selection.main.anchor, head: 0 })), !0), Zo = ({ state: n, dispatch: t }) => (t(Lt(n, { anchor: n.selection.main.anchor, head: n.doc.length })), !0), rp = ({ state: n, dispatch: t }) => (t(n.update({ selection: { anchor: 0, head: n.doc.length }, userEvent: "select" })), !0), op = ({ state: n, dispatch: t }) => {
  let e = js(n).map(({ from: i, to: s }) => k.range(i, Math.min(s + 1, n.doc.length)));
  return t(n.update({ selection: k.create(e), userEvent: "select" })), !0;
}, lp = ({ state: n, dispatch: t }) => {
  let e = Ke(n.selection, (i) => {
    let s = bt(n), r = s.resolveStack(i.from, 1);
    if (i.empty) {
      let o = s.resolveStack(i.from, -1);
      o.node.from >= r.node.from && o.node.to <= r.node.to && (r = o);
    }
    for (let o = r; o; o = o.next) {
      let { node: l } = o;
      if ((l.from < i.from && l.to >= i.to || l.to > i.to && l.from <= i.from) && o.next)
        return k.range(l.to, l.from);
    }
    return i;
  });
  return e.eq(n.selection) ? !1 : (t(Lt(n, e)), !0);
};
function yh(n, t) {
  let { state: e } = n, i = e.selection, s = e.selection.ranges.slice();
  for (let r of e.selection.ranges) {
    let o = e.doc.lineAt(r.head);
    if (t ? o.to < n.state.doc.length : o.from > 0)
      for (let l = r; ; ) {
        let a = n.moveVertically(l, t);
        if (a.head < o.from || a.head > o.to) {
          s.some((h) => h.head == a.head) || s.push(a);
          break;
        } else {
          if (a.head == l.head)
            break;
          l = a;
        }
      }
  }
  return s.length == i.ranges.length ? !1 : (n.dispatch(Lt(e, k.create(s, s.length - 1))), !0);
}
const ap = (n) => yh(n, !1), hp = (n) => yh(n, !0), fp = ({ state: n, dispatch: t }) => {
  let e = n.selection, i = null;
  return e.ranges.length > 1 ? i = k.create([e.main]) : e.main.empty || (i = k.create([k.cursor(e.main.head)])), i ? (t(Lt(n, i)), !0) : !1;
};
function Ti(n, t) {
  if (n.state.readOnly)
    return !1;
  let e = "delete.selection", { state: i } = n, s = i.changeByRange((r) => {
    let { from: o, to: l } = r;
    if (o == l) {
      let a = t(r);
      a < o ? (e = "delete.backward", a = Ki(n, a, !1)) : a > o && (e = "delete.forward", a = Ki(n, a, !0)), o = Math.min(o, a), l = Math.max(l, a);
    } else
      o = Ki(n, o, !1), l = Ki(n, l, !0);
    return o == l ? { range: r } : { changes: { from: o, to: l }, range: k.cursor(o, o < r.head ? -1 : 1) };
  });
  return s.changes.empty ? !1 : (n.dispatch(i.update(s, {
    scrollIntoView: !0,
    userEvent: e,
    effects: e == "delete.selection" ? P.announce.of(i.phrase("Selection deleted")) : void 0
  })), !0);
}
function Ki(n, t, e) {
  if (n instanceof P)
    for (let i of n.state.facet(P.atomicRanges).map((s) => s(n)))
      i.between(t, t, (s, r) => {
        s < t && r > t && (t = e ? r : s);
      });
  return t;
}
const bh = (n, t, e) => Ti(n, (i) => {
  let s = i.from, { state: r } = n, o = r.doc.lineAt(s), l, a;
  if (e && !t && s > o.from && s < o.from + 200 && !/[^ \t]/.test(l = o.text.slice(0, s - o.from))) {
    if (l[l.length - 1] == "	")
      return s - 1;
    let h = Rs(l, r.tabSize), f = h % vs(r) || vs(r);
    for (let c = 0; c < f && l[l.length - 1 - c] == " "; c++)
      s--;
    a = s;
  } else
    a = st(o.text, s - o.from, t, t) + o.from, a == s && o.number != (t ? r.doc.lines : 1) ? a += t ? 1 : -1 : !t && /[\ufe00-\ufe0f]/.test(o.text.slice(a - o.from, s - o.from)) && (a = st(o.text, a - o.from, !1, !1) + o.from);
  return a;
}), nr = (n) => bh(n, !1, !0), xh = (n) => bh(n, !0, !1), wh = (n, t) => Ti(n, (e) => {
  let i = e.head, { state: s } = n, r = s.doc.lineAt(i), o = s.charCategorizer(i);
  for (let l = null; ; ) {
    if (i == (t ? r.to : r.from)) {
      i == e.head && r.number != (t ? s.doc.lines : 1) && (i += t ? 1 : -1);
      break;
    }
    let a = st(r.text, i - r.from, t) + r.from, h = r.text.slice(Math.min(i, a) - r.from, Math.max(i, a) - r.from), f = o(h);
    if (l != null && f != l)
      break;
    (h != " " || i != e.head) && (l = f), i = a;
  }
  return i;
}), kh = (n) => wh(n, !1), cp = (n) => wh(n, !0), up = (n) => Ti(n, (t) => {
  let e = n.lineBlockAt(t.head).to;
  return t.head < e ? e : Math.min(n.state.doc.length, t.head + 1);
}), dp = (n) => Ti(n, (t) => {
  let e = n.moveToLineBoundary(t, !1).head;
  return t.head > e ? e : Math.max(0, t.head - 1);
}), pp = (n) => Ti(n, (t) => {
  let e = n.moveToLineBoundary(t, !0).head;
  return t.head < e ? e : Math.min(n.state.doc.length, t.head + 1);
}), gp = ({ state: n, dispatch: t }) => {
  if (n.readOnly)
    return !1;
  let e = n.changeByRange((i) => ({
    changes: { from: i.from, to: i.to, insert: I.of(["", ""]) },
    range: k.cursor(i.from)
  }));
  return t(n.update(e, { scrollIntoView: !0, userEvent: "input" })), !0;
}, mp = ({ state: n, dispatch: t }) => {
  if (n.readOnly)
    return !1;
  let e = n.changeByRange((i) => {
    if (!i.empty || i.from == 0 || i.from == n.doc.length)
      return { range: i };
    let s = i.from, r = n.doc.lineAt(s), o = s == r.from ? s - 1 : st(r.text, s - r.from, !1) + r.from, l = s == r.to ? s + 1 : st(r.text, s - r.from, !0) + r.from;
    return {
      changes: { from: o, to: l, insert: n.doc.slice(s, l).append(n.doc.slice(o, s)) },
      range: k.cursor(l)
    };
  });
  return e.changes.empty ? !1 : (t(n.update(e, { scrollIntoView: !0, userEvent: "move.character" })), !0);
};
function js(n) {
  let t = [], e = -1;
  for (let i of n.selection.ranges) {
    let s = n.doc.lineAt(i.from), r = n.doc.lineAt(i.to);
    if (!i.empty && i.to == r.from && (r = n.doc.lineAt(i.to - 1)), e >= s.number) {
      let o = t[t.length - 1];
      o.to = r.to, o.ranges.push(i);
    } else
      t.push({ from: s.from, to: r.to, ranges: [i] });
    e = r.number + 1;
  }
  return t;
}
function vh(n, t, e) {
  if (n.readOnly)
    return !1;
  let i = [], s = [];
  for (let r of js(n)) {
    if (e ? r.to == n.doc.length : r.from == 0)
      continue;
    let o = n.doc.lineAt(e ? r.to + 1 : r.from - 1), l = o.length + 1;
    if (e) {
      i.push({ from: r.to, to: o.to }, { from: r.from, insert: o.text + n.lineBreak });
      for (let a of r.ranges)
        s.push(k.range(Math.min(n.doc.length, a.anchor + l), Math.min(n.doc.length, a.head + l)));
    } else {
      i.push({ from: o.from, to: r.from }, { from: r.to, insert: n.lineBreak + o.text });
      for (let a of r.ranges)
        s.push(k.range(a.anchor - l, a.head - l));
    }
  }
  return i.length ? (t(n.update({
    changes: i,
    scrollIntoView: !0,
    selection: k.create(s, n.selection.mainIndex),
    userEvent: "move.line"
  })), !0) : !1;
}
const yp = ({ state: n, dispatch: t }) => vh(n, t, !1), bp = ({ state: n, dispatch: t }) => vh(n, t, !0);
function Sh(n, t, e) {
  if (n.readOnly)
    return !1;
  let i = [];
  for (let r of js(n))
    e ? i.push({ from: r.from, insert: n.doc.slice(r.from, r.to) + n.lineBreak }) : i.push({ from: r.to, insert: n.lineBreak + n.doc.slice(r.from, r.to) });
  let s = n.changes(i);
  return t(n.update({
    changes: s,
    selection: n.selection.map(s, e ? 1 : -1),
    scrollIntoView: !0,
    userEvent: "input.copyline"
  })), !0;
}
const xp = ({ state: n, dispatch: t }) => Sh(n, t, !1), wp = ({ state: n, dispatch: t }) => Sh(n, t, !0), kp = (n) => {
  if (n.state.readOnly)
    return !1;
  let { state: t } = n, e = t.changes(js(t).map(({ from: s, to: r }) => (s > 0 ? s-- : r < t.doc.length && r++, { from: s, to: r }))), i = Ke(t.selection, (s) => {
    let r;
    if (n.lineWrapping) {
      let o = n.lineBlockAt(s.head), l = n.coordsAtPos(s.head, s.assoc || 1);
      l && (r = o.bottom + n.documentTop - l.bottom + n.defaultLineHeight / 2);
    }
    return n.moveVertically(s, !0, r);
  }).map(e);
  return n.dispatch({ changes: e, selection: i, scrollIntoView: !0, userEvent: "delete.line" }), !0;
};
function vp(n, t) {
  if (/\(\)|\[\]|\{\}/.test(n.sliceDoc(t - 1, t + 1)))
    return { from: t, to: t };
  let e = bt(n).resolveInner(t), i = e.childBefore(t), s = e.childAfter(t), r;
  return i && s && i.to <= t && s.from >= t && (r = i.type.prop(B.closedBy)) && r.indexOf(s.name) > -1 && n.doc.lineAt(i.to).from == n.doc.lineAt(s.from).from && !/\S/.test(n.sliceDoc(i.to, s.from)) ? { from: i.to, to: s.from } : null;
}
const Jo = /* @__PURE__ */ Oh(!1), Sp = /* @__PURE__ */ Oh(!0);
function Oh(n) {
  return ({ state: t, dispatch: e }) => {
    if (t.readOnly)
      return !1;
    let i = t.changeByRange((s) => {
      let { from: r, to: o } = s, l = t.doc.lineAt(r), a = !n && r == o && vp(t, r);
      n && (r = o = (o <= l.to ? l : t.doc.lineAt(o)).to);
      let h = new Hs(t, { simulateBreak: r, simulateDoubleBreak: !!a }), f = $a(h, r);
      for (f == null && (f = Rs(/^\s*/.exec(t.doc.lineAt(r).text)[0], t.tabSize)); o < l.to && /\s/.test(l.text[o - l.from]); )
        o++;
      a ? { from: r, to: o } = a : r > l.from && r < l.from + 100 && !/\S/.test(l.text.slice(0, r)) && (r = l.from);
      let c = ["", Ss(t, f)];
      return a && c.push(Ss(t, h.lineIndent(l.from, -1))), {
        changes: { from: r, to: o, insert: I.of(c) },
        range: k.cursor(r + 1 + c[1].length)
      };
    });
    return e(t.update(i, { scrollIntoView: !0, userEvent: "input" })), !0;
  };
}
function Br(n, t) {
  let e = -1;
  return n.changeByRange((i) => {
    let s = [];
    for (let o = i.from; o <= i.to; ) {
      let l = n.doc.lineAt(o);
      l.number > e && (i.empty || i.to > l.from) && (t(l, s, i), e = l.number), o = l.to + 1;
    }
    let r = n.changes(s);
    return {
      changes: s,
      range: k.range(r.mapPos(i.anchor, 1), r.mapPos(i.head, 1))
    };
  });
}
const Op = ({ state: n, dispatch: t }) => {
  if (n.readOnly)
    return !1;
  let e = /* @__PURE__ */ Object.create(null), i = new Hs(n, { overrideIndentation: (r) => {
    let o = e[r];
    return o ?? -1;
  } }), s = Br(n, (r, o, l) => {
    let a = $a(i, r.from);
    if (a == null)
      return;
    /\S/.test(r.text) || (a = 0);
    let h = /^\s*/.exec(r.text)[0], f = Ss(n, a);
    (h != f || l.from < r.from + h.length) && (e[r.from] = a, o.push({ from: r.from, to: r.from + h.length, insert: f }));
  });
  return s.changes.empty || t(n.update(s, { userEvent: "indent" })), !0;
}, Cp = ({ state: n, dispatch: t }) => n.readOnly ? !1 : (t(n.update(Br(n, (e, i) => {
  i.push({ from: e.from, insert: n.facet(Tr) });
}), { userEvent: "input.indent" })), !0), Ap = ({ state: n, dispatch: t }) => n.readOnly ? !1 : (t(n.update(Br(n, (e, i) => {
  let s = /^\s*/.exec(e.text)[0];
  if (!s)
    return;
  let r = Rs(s, n.tabSize), o = 0, l = Ss(n, Math.max(0, r - vs(n)));
  for (; o < s.length && o < l.length && s.charCodeAt(o) == l.charCodeAt(o); )
    o++;
  i.push({ from: e.from + o, to: e.from + s.length, insert: l.slice(o) });
}), { userEvent: "delete.dedent" })), !0), Tp = (n) => (n.setTabFocusMode(), !0), Mp = [
  { key: "Ctrl-b", run: eh, shift: fh, preventDefault: !0 },
  { key: "Ctrl-f", run: ih, shift: ch },
  { key: "Ctrl-p", run: rh, shift: ph },
  { key: "Ctrl-n", run: oh, shift: gh },
  { key: "Ctrl-a", run: Xd, shift: sp },
  { key: "Ctrl-e", run: qd, shift: np },
  { key: "Ctrl-d", run: xh },
  { key: "Ctrl-h", run: nr },
  { key: "Ctrl-k", run: up },
  { key: "Ctrl-Alt-h", run: kh },
  { key: "Ctrl-o", run: gp },
  { key: "Ctrl-t", run: mp },
  { key: "Ctrl-v", run: sr }
], Pp = /* @__PURE__ */ [
  { key: "ArrowLeft", run: eh, shift: fh, preventDefault: !0 },
  { key: "Mod-ArrowLeft", mac: "Alt-ArrowLeft", run: Nd, shift: _d, preventDefault: !0 },
  { mac: "Cmd-ArrowLeft", run: $d, shift: ep, preventDefault: !0 },
  { key: "ArrowRight", run: ih, shift: ch, preventDefault: !0 },
  { key: "Mod-ArrowRight", mac: "Alt-ArrowRight", run: Vd, shift: Gd, preventDefault: !0 },
  { mac: "Cmd-ArrowRight", run: jd, shift: ip, preventDefault: !0 },
  { key: "ArrowUp", run: rh, shift: ph, preventDefault: !0 },
  { mac: "Cmd-ArrowUp", run: _o, shift: Yo },
  { mac: "Ctrl-ArrowUp", run: qo, shift: Uo },
  { key: "ArrowDown", run: oh, shift: gh, preventDefault: !0 },
  { mac: "Cmd-ArrowDown", run: Go, shift: Zo },
  { mac: "Ctrl-ArrowDown", run: sr, shift: Ko },
  { key: "PageUp", run: qo, shift: Uo },
  { key: "PageDown", run: sr, shift: Ko },
  { key: "Home", run: Qd, shift: tp, preventDefault: !0 },
  { key: "Mod-Home", run: _o, shift: Yo },
  { key: "End", run: zd, shift: Jd, preventDefault: !0 },
  { key: "Mod-End", run: Go, shift: Zo },
  { key: "Enter", run: Jo, shift: Jo },
  { key: "Mod-a", run: rp },
  { key: "Backspace", run: nr, shift: nr, preventDefault: !0 },
  { key: "Delete", run: xh, preventDefault: !0 },
  { key: "Mod-Backspace", mac: "Alt-Backspace", run: kh, preventDefault: !0 },
  { key: "Mod-Delete", mac: "Alt-Delete", run: cp, preventDefault: !0 },
  { mac: "Mod-Backspace", run: dp, preventDefault: !0 },
  { mac: "Mod-Delete", run: pp, preventDefault: !0 }
].concat(/* @__PURE__ */ Mp.map((n) => ({ mac: n.key, run: n.run, shift: n.shift }))), Dp = /* @__PURE__ */ [
  { key: "Alt-ArrowLeft", mac: "Ctrl-ArrowLeft", run: Hd, shift: Yd },
  { key: "Alt-ArrowRight", mac: "Ctrl-ArrowRight", run: Fd, shift: Zd },
  { key: "Alt-ArrowUp", run: yp },
  { key: "Shift-Alt-ArrowUp", run: xp },
  { key: "Alt-ArrowDown", run: bp },
  { key: "Shift-Alt-ArrowDown", run: wp },
  { key: "Mod-Alt-ArrowUp", run: ap },
  { key: "Mod-Alt-ArrowDown", run: hp },
  { key: "Escape", run: fp },
  { key: "Mod-Enter", run: Sp },
  { key: "Alt-l", mac: "Ctrl-l", run: op },
  { key: "Mod-i", run: lp, preventDefault: !0 },
  { key: "Mod-[", run: Ap },
  { key: "Mod-]", run: Cp },
  { key: "Mod-Alt-\\", run: Op },
  { key: "Shift-Mod-k", run: kp },
  { key: "Shift-Mod-\\", run: Kd },
  { key: "Mod-/", run: yd },
  { key: "Alt-A", run: xd },
  { key: "Ctrl-m", mac: "Shift-Alt-m", run: Tp }
].concat(Pp);
class Ch {
  /**
  Create a new completion context. (Mostly useful for testing
  completion sources—in the editor, the extension will create
  these for you.)
  */
  constructor(t, e, i, s) {
    this.state = t, this.pos = e, this.explicit = i, this.view = s, this.abortListeners = [], this.abortOnDocChange = !1;
  }
  /**
  Get the extent, content, and (if there is a token) type of the
  token before `this.pos`.
  */
  tokenBefore(t) {
    let e = bt(this.state).resolveInner(this.pos, -1);
    for (; e && t.indexOf(e.name) < 0; )
      e = e.parent;
    return e ? {
      from: e.from,
      to: this.pos,
      text: this.state.sliceDoc(e.from, this.pos),
      type: e.type
    } : null;
  }
  /**
  Get the match of the given expression directly before the
  cursor.
  */
  matchBefore(t) {
    let e = this.state.doc.lineAt(this.pos), i = Math.max(e.from, this.pos - 250), s = e.text.slice(i - e.from, this.pos - e.from), r = s.search(Ah(t, !1));
    return r < 0 ? null : { from: i + r, to: this.pos, text: s.slice(r) };
  }
  /**
  Yields true when the query has been aborted. Can be useful in
  asynchronous queries to avoid doing work that will be ignored.
  */
  get aborted() {
    return this.abortListeners == null;
  }
  /**
  Allows you to register abort handlers, which will be called when
  the query is
  [aborted](https://codemirror.net/6/docs/ref/#autocomplete.CompletionContext.aborted).
  
  By default, running queries will not be aborted for regular
  typing or backspacing, on the assumption that they are likely to
  return a result with a
  [`validFor`](https://codemirror.net/6/docs/ref/#autocomplete.CompletionResult.validFor) field that
  allows the result to be used after all. Passing `onDocChange:
  true` will cause this query to be aborted for any document
  change.
  */
  addEventListener(t, e, i) {
    t == "abort" && this.abortListeners && (this.abortListeners.push(e), i && i.onDocChange && (this.abortOnDocChange = !0));
  }
}
function tl(n) {
  let t = Object.keys(n).join(""), e = /\w/.test(t);
  return e && (t = t.replace(/\w/g, "")), `[${e ? "\\w" : ""}${t.replace(/[^\w\s]/g, "\\$&")}]`;
}
function Bp(n) {
  let t = /* @__PURE__ */ Object.create(null), e = /* @__PURE__ */ Object.create(null);
  for (let { label: s } of n) {
    t[s[0]] = !0;
    for (let r = 1; r < s.length; r++)
      e[s[r]] = !0;
  }
  let i = tl(t) + tl(e) + "*$";
  return [new RegExp("^" + i), new RegExp(i)];
}
function Rp(n) {
  let t = n.map((s) => typeof s == "string" ? { label: s } : s), [e, i] = t.every((s) => /^\w+$/.test(s.label)) ? [/\w*$/, /\w+$/] : Bp(t);
  return (s) => {
    let r = s.matchBefore(i);
    return r || s.explicit ? { from: r ? r.from : s.pos, options: t, validFor: e } : null;
  };
}
class el {
  constructor(t, e, i, s) {
    this.completion = t, this.source = e, this.match = i, this.score = s;
  }
}
function Oe(n) {
  return n.selection.main.from;
}
function Ah(n, t) {
  var e;
  let { source: i } = n, s = t && i[0] != "^", r = i[i.length - 1] != "$";
  return !s && !r ? n : new RegExp(`${s ? "^" : ""}(?:${i})${r ? "$" : ""}`, (e = n.flags) !== null && e !== void 0 ? e : n.ignoreCase ? "i" : "");
}
const Th = /* @__PURE__ */ Jt.define();
function Lp(n, t, e, i) {
  let { main: s } = n.selection, r = e - s.from, o = i - s.from;
  return {
    ...n.changeByRange((l) => {
      if (l != s && e != i && n.sliceDoc(l.from + r, l.from + o) != n.sliceDoc(e, i))
        return { range: l };
      let a = n.toText(t);
      return {
        changes: { from: l.from + r, to: i == s.from ? l.to : l.from + o, insert: a },
        range: k.cursor(l.from + r + a.length)
      };
    }),
    scrollIntoView: !0,
    userEvent: "input.complete"
  };
}
const il = /* @__PURE__ */ new WeakMap();
function Ep(n) {
  if (!Array.isArray(n))
    return n;
  let t = il.get(n);
  return t || il.set(n, t = Rp(n)), t;
}
const Cs = /* @__PURE__ */ N.define(), bi = /* @__PURE__ */ N.define();
class Ip {
  constructor(t) {
    this.pattern = t, this.chars = [], this.folded = [], this.any = [], this.precise = [], this.byWord = [], this.score = 0, this.matched = [];
    for (let e = 0; e < t.length; ) {
      let i = Ft(t, e), s = se(i);
      this.chars.push(i);
      let r = t.slice(e, e + s), o = r.toUpperCase();
      this.folded.push(Ft(o == r ? r.toLowerCase() : o, 0)), e += s;
    }
    this.astral = t.length != this.chars.length;
  }
  ret(t, e) {
    return this.score = t, this.matched = e, this;
  }
  // Matches a given word (completion) against the pattern (input).
  // Will return a boolean indicating whether there was a match and,
  // on success, set `this.score` to the score, `this.matched` to an
  // array of `from, to` pairs indicating the matched parts of `word`.
  //
  // The score is a number that is more negative the worse the match
  // is. See `Penalty` above.
  match(t) {
    if (this.pattern.length == 0)
      return this.ret(-100, []);
    if (t.length < this.pattern.length)
      return null;
    let { chars: e, folded: i, any: s, precise: r, byWord: o } = this;
    if (e.length == 1) {
      let b = Ft(t, 0), x = se(b), O = x == t.length ? 0 : -100;
      if (b != e[0]) if (b == i[0])
        O += -200;
      else
        return null;
      return this.ret(O, [0, x]);
    }
    let l = t.indexOf(this.pattern);
    if (l == 0)
      return this.ret(t.length == this.pattern.length ? 0 : -100, [0, this.pattern.length]);
    let a = e.length, h = 0;
    if (l < 0) {
      for (let b = 0, x = Math.min(t.length, 200); b < x && h < a; ) {
        let O = Ft(t, b);
        (O == e[h] || O == i[h]) && (s[h++] = b), b += se(O);
      }
      if (h < a)
        return null;
    }
    let f = 0, c = 0, u = !1, d = 0, p = -1, g = -1, m = /[a-z]/.test(t), y = !0;
    for (let b = 0, x = Math.min(t.length, 200), O = 0; b < x && c < a; ) {
      let S = Ft(t, b);
      l < 0 && (f < a && S == e[f] && (r[f++] = b), d < a && (S == e[d] || S == i[d] ? (d == 0 && (p = b), g = b + 1, d++) : d = 0));
      let T, C = S < 255 ? S >= 48 && S <= 57 || S >= 97 && S <= 122 ? 2 : S >= 65 && S <= 90 ? 1 : 0 : (T = Al(S)) != T.toLowerCase() ? 1 : T != T.toUpperCase() ? 2 : 0;
      (!b || C == 1 && m || O == 0 && C != 0) && (e[c] == S || i[c] == S && (u = !0) ? o[c++] = b : o.length && (y = !1)), O = C, b += se(S);
    }
    return c == a && o[0] == 0 && y ? this.result(-100 + (u ? -200 : 0), o, t) : d == a && p == 0 ? this.ret(-200 - t.length + (g == t.length ? 0 : -100), [0, g]) : l > -1 ? this.ret(-700 - t.length, [l, l + this.pattern.length]) : d == a ? this.ret(-900 - t.length, [p, g]) : c == a ? this.result(-100 + (u ? -200 : 0) + -700 + (y ? 0 : -1100), o, t) : e.length == 2 ? null : this.result((s[0] ? -700 : 0) + -200 + -1100, s, t);
  }
  result(t, e, i) {
    let s = [], r = 0;
    for (let o of e) {
      let l = o + (this.astral ? se(Ft(i, o)) : 1);
      r && s[r - 1] == o ? s[r - 1] = l : (s[r++] = o, s[r++] = l);
    }
    return this.ret(t - i.length, s);
  }
}
class Np {
  constructor(t) {
    this.pattern = t, this.matched = [], this.score = 0, this.folded = t.toLowerCase();
  }
  match(t) {
    if (t.length < this.pattern.length)
      return null;
    let e = t.slice(0, this.pattern.length), i = e == this.pattern ? 0 : e.toLowerCase() == this.folded ? -200 : null;
    return i == null ? null : (this.matched = [0, e.length], this.score = i + (t.length == this.pattern.length ? 0 : -100), this);
  }
}
const Z = /* @__PURE__ */ M.define({
  combine(n) {
    return vi(n, {
      activateOnTyping: !0,
      activateOnCompletion: () => !1,
      activateOnTypingDelay: 100,
      selectOnOpen: !0,
      override: null,
      closeOnBlur: !0,
      maxRenderedOptions: 100,
      defaultKeymap: !0,
      tooltipClass: () => "",
      optionClass: () => "",
      aboveCursor: !1,
      icons: !0,
      addToOptions: [],
      positionInfo: Vp,
      filterStrict: !1,
      compareCompletions: (t, e) => (t.sortText || t.label).localeCompare(e.sortText || e.label),
      interactionDelay: 75,
      updateSyncTime: 100
    }, {
      defaultKeymap: (t, e) => t && e,
      closeOnBlur: (t, e) => t && e,
      icons: (t, e) => t && e,
      tooltipClass: (t, e) => (i) => sl(t(i), e(i)),
      optionClass: (t, e) => (i) => sl(t(i), e(i)),
      addToOptions: (t, e) => t.concat(e),
      filterStrict: (t, e) => t || e
    });
  }
});
function sl(n, t) {
  return n ? t ? n + " " + t : n : t;
}
function Vp(n, t, e, i, s, r) {
  let o = n.textDirection == $.RTL, l = o, a = !1, h = "top", f, c, u = t.left - s.left, d = s.right - t.right, p = i.right - i.left, g = i.bottom - i.top;
  if (l && u < Math.min(p, d) ? l = !1 : !l && d < Math.min(p, u) && (l = !0), p <= (l ? u : d))
    f = Math.max(s.top, Math.min(e.top, s.bottom - g)) - t.top, c = Math.min(400, l ? u : d);
  else {
    a = !0, c = Math.min(
      400,
      (o ? t.right : s.right - t.left) - 30
      /* Info.Margin */
    );
    let b = s.bottom - t.bottom;
    b >= g || b > t.top ? f = e.bottom - t.top : (h = "bottom", f = t.bottom - e.top);
  }
  let m = (t.bottom - t.top) / r.offsetHeight, y = (t.right - t.left) / r.offsetWidth;
  return {
    style: `${h}: ${f / m}px; max-width: ${c / y}px`,
    class: "cm-completionInfo-" + (a ? o ? "left-narrow" : "right-narrow" : l ? "left" : "right")
  };
}
const Rr = /* @__PURE__ */ N.define();
function Wp(n) {
  let t = n.addToOptions.slice();
  return n.icons && t.push({
    render(e) {
      let i = document.createElement("div");
      return i.classList.add("cm-completionIcon"), e.type && i.classList.add(...e.type.split(/\s+/g).map((s) => "cm-completionIcon-" + s)), i.setAttribute("aria-hidden", "true"), i;
    },
    position: 20
  }), t.push({
    render(e, i, s, r) {
      let o = document.createElement("span");
      o.className = "cm-completionLabel";
      let l = e.displayLabel || e.label, a = 0;
      for (let h = 0; h < r.length; ) {
        let f = r[h++], c = r[h++];
        f > a && o.appendChild(document.createTextNode(l.slice(a, f)));
        let u = o.appendChild(document.createElement("span"));
        u.appendChild(document.createTextNode(l.slice(f, c))), u.className = "cm-completionMatchedText", a = c;
      }
      return a < l.length && o.appendChild(document.createTextNode(l.slice(a))), o;
    },
    position: 50
  }, {
    render(e) {
      if (!e.detail)
        return null;
      let i = document.createElement("span");
      return i.className = "cm-completionDetail", i.textContent = e.detail, i;
    },
    position: 80
  }), t.sort((e, i) => e.position - i.position).map((e) => e.render);
}
function dn(n, t, e) {
  if (n <= e)
    return { from: 0, to: n };
  if (t < 0 && (t = 0), t <= n >> 1) {
    let s = Math.floor(t / e);
    return { from: s * e, to: (s + 1) * e };
  }
  let i = Math.floor((n - t) / e);
  return { from: n - (i + 1) * e, to: n - i * e };
}
class Hp {
  constructor(t, e, i) {
    this.view = t, this.stateField = e, this.applyCompletion = i, this.info = null, this.infoDestroy = null, this.placeInfoReq = {
      read: () => this.measureInfo(),
      write: (a) => this.placeInfo(a),
      key: this
    }, this.space = null, this.currentClass = "";
    let s = t.state.field(e), { options: r, selected: o } = s.open, l = t.state.facet(Z);
    this.optionContent = Wp(l), this.optionClass = l.optionClass, this.tooltipClass = l.tooltipClass, this.range = dn(r.length, o, l.maxRenderedOptions), this.dom = document.createElement("div"), this.dom.className = "cm-tooltip-autocomplete", this.updateTooltipClass(t.state), this.dom.addEventListener("mousedown", (a) => {
      let { options: h } = t.state.field(e).open;
      for (let f = a.target, c; f && f != this.dom; f = f.parentNode)
        if (f.nodeName == "LI" && (c = /-(\d+)$/.exec(f.id)) && +c[1] < h.length) {
          this.applyCompletion(t, h[+c[1]]), a.preventDefault();
          return;
        }
      if (a.target == this.list) {
        let f = this.list.classList.contains("cm-completionListIncompleteTop") && a.clientY < this.list.firstChild.getBoundingClientRect().top ? this.range.from - 1 : this.list.classList.contains("cm-completionListIncompleteBottom") && a.clientY > this.list.lastChild.getBoundingClientRect().bottom ? this.range.to : null;
        f != null && (t.dispatch({ effects: Rr.of(f) }), a.preventDefault());
      }
    }), this.dom.addEventListener("focusout", (a) => {
      let h = t.state.field(this.stateField, !1);
      h && h.tooltip && t.state.facet(Z).closeOnBlur && a.relatedTarget != t.contentDOM && t.dispatch({ effects: bi.of(null) });
    }), this.showOptions(r, s.id);
  }
  mount() {
    this.updateSel();
  }
  showOptions(t, e) {
    this.list && this.list.remove(), this.list = this.dom.appendChild(this.createListBox(t, e, this.range)), this.list.addEventListener("scroll", () => {
      this.info && this.view.requestMeasure(this.placeInfoReq);
    });
  }
  update(t) {
    var e;
    let i = t.state.field(this.stateField), s = t.startState.field(this.stateField);
    if (this.updateTooltipClass(t.state), i != s) {
      let { options: r, selected: o, disabled: l } = i.open;
      (!s.open || s.open.options != r) && (this.range = dn(r.length, o, t.state.facet(Z).maxRenderedOptions), this.showOptions(r, i.id)), this.updateSel(), l != ((e = s.open) === null || e === void 0 ? void 0 : e.disabled) && this.dom.classList.toggle("cm-tooltip-autocomplete-disabled", !!l);
    }
  }
  updateTooltipClass(t) {
    let e = this.tooltipClass(t);
    if (e != this.currentClass) {
      for (let i of this.currentClass.split(" "))
        i && this.dom.classList.remove(i);
      for (let i of e.split(" "))
        i && this.dom.classList.add(i);
      this.currentClass = e;
    }
  }
  positioned(t) {
    this.space = t, this.info && this.view.requestMeasure(this.placeInfoReq);
  }
  updateSel() {
    let t = this.view.state.field(this.stateField), e = t.open;
    (e.selected > -1 && e.selected < this.range.from || e.selected >= this.range.to) && (this.range = dn(e.options.length, e.selected, this.view.state.facet(Z).maxRenderedOptions), this.showOptions(e.options, t.id));
    let i = this.updateSelectedOption(e.selected);
    if (i) {
      this.destroyInfo();
      let { completion: s } = e.options[e.selected], { info: r } = s;
      if (!r)
        return;
      let o = typeof r == "string" ? document.createTextNode(r) : r(s);
      if (!o)
        return;
      "then" in o ? o.then((l) => {
        l && this.view.state.field(this.stateField, !1) == t && this.addInfoPane(l, s);
      }).catch((l) => ft(this.view.state, l, "completion info")) : (this.addInfoPane(o, s), i.setAttribute("aria-describedby", this.info.id));
    }
  }
  addInfoPane(t, e) {
    this.destroyInfo();
    let i = this.info = document.createElement("div");
    if (i.className = "cm-tooltip cm-completionInfo", i.id = "cm-completionInfo-" + Math.floor(Math.random() * 65535).toString(16), t.nodeType != null)
      i.appendChild(t), this.infoDestroy = null;
    else {
      let { dom: s, destroy: r } = t;
      i.appendChild(s), this.infoDestroy = r || null;
    }
    this.dom.appendChild(i), this.view.requestMeasure(this.placeInfoReq);
  }
  updateSelectedOption(t) {
    let e = null;
    for (let i = this.list.firstChild, s = this.range.from; i; i = i.nextSibling, s++)
      i.nodeName != "LI" || !i.id ? s-- : s == t ? i.hasAttribute("aria-selected") || (i.setAttribute("aria-selected", "true"), e = i) : i.hasAttribute("aria-selected") && (i.removeAttribute("aria-selected"), i.removeAttribute("aria-describedby"));
    return e && zp(this.list, e), e;
  }
  measureInfo() {
    let t = this.dom.querySelector("[aria-selected]");
    if (!t || !this.info)
      return null;
    let e = this.dom.getBoundingClientRect(), i = this.info.getBoundingClientRect(), s = t.getBoundingClientRect(), r = this.space;
    if (!r) {
      let o = this.dom.ownerDocument.documentElement;
      r = { left: 0, top: 0, right: o.clientWidth, bottom: o.clientHeight };
    }
    return s.top > Math.min(r.bottom, e.bottom) - 10 || s.bottom < Math.max(r.top, e.top) + 10 ? null : this.view.state.facet(Z).positionInfo(this.view, e, s, i, r, this.dom);
  }
  placeInfo(t) {
    this.info && (t ? (t.style && (this.info.style.cssText = t.style), this.info.className = "cm-tooltip cm-completionInfo " + (t.class || "")) : this.info.style.cssText = "top: -1e6px");
  }
  createListBox(t, e, i) {
    const s = document.createElement("ul");
    s.id = e, s.setAttribute("role", "listbox"), s.setAttribute("aria-expanded", "true"), s.setAttribute("aria-label", this.view.state.phrase("Completions")), s.addEventListener("mousedown", (o) => {
      o.target == s && o.preventDefault();
    });
    let r = null;
    for (let o = i.from; o < i.to; o++) {
      let { completion: l, match: a } = t[o], { section: h } = l;
      if (h) {
        let u = typeof h == "string" ? h : h.name;
        if (u != r && (o > i.from || i.from == 0))
          if (r = u, typeof h != "string" && h.header)
            s.appendChild(h.header(h));
          else {
            let d = s.appendChild(document.createElement("completion-section"));
            d.textContent = u;
          }
      }
      const f = s.appendChild(document.createElement("li"));
      f.id = e + "-" + o, f.setAttribute("role", "option");
      let c = this.optionClass(l);
      c && (f.className = c);
      for (let u of this.optionContent) {
        let d = u(l, this.view.state, this.view, a);
        d && f.appendChild(d);
      }
    }
    return i.from && s.classList.add("cm-completionListIncompleteTop"), i.to < t.length && s.classList.add("cm-completionListIncompleteBottom"), s;
  }
  destroyInfo() {
    this.info && (this.infoDestroy && this.infoDestroy(), this.info.remove(), this.info = null);
  }
  destroy() {
    this.destroyInfo();
  }
}
function Fp(n, t) {
  return (e) => new Hp(e, n, t);
}
function zp(n, t) {
  let e = n.getBoundingClientRect(), i = t.getBoundingClientRect(), s = e.height / n.offsetHeight;
  i.top < e.top ? n.scrollTop -= (e.top - i.top) / s : i.bottom > e.bottom && (n.scrollTop += (i.bottom - e.bottom) / s);
}
function nl(n) {
  return (n.boost || 0) * 100 + (n.apply ? 10 : 0) + (n.info ? 5 : 0) + (n.type ? 1 : 0);
}
function Qp(n, t) {
  let e = [], i = null, s = null, r = (f) => {
    e.push(f);
    let { section: c } = f.completion;
    if (c) {
      i || (i = []);
      let u = typeof c == "string" ? c : c.name;
      i.some((d) => d.name == u) || i.push(typeof c == "string" ? { name: u } : c);
    }
  }, o = t.facet(Z);
  for (let f of n)
    if (f.hasResult()) {
      let c = f.result.getMatch;
      if (f.result.filter === !1)
        for (let u of f.result.options)
          r(new el(u, f.source, c ? c(u) : [], 1e9 - e.length));
      else {
        let u = t.sliceDoc(f.from, f.to), d, p = o.filterStrict ? new Np(u) : new Ip(u);
        for (let g of f.result.options)
          if (d = p.match(g.label)) {
            let m = g.displayLabel ? c ? c(g, d.matched) : [] : d.matched, y = d.score + (g.boost || 0);
            if (r(new el(g, f.source, m, y)), typeof g.section == "object" && g.section.rank === "dynamic") {
              let { name: b } = g.section;
              s || (s = /* @__PURE__ */ Object.create(null)), s[b] = Math.max(y, s[b] || -1e9);
            }
          }
      }
    }
  if (i) {
    let f = /* @__PURE__ */ Object.create(null), c = 0, u = (d, p) => (d.rank === "dynamic" && p.rank === "dynamic" ? s[p.name] - s[d.name] : 0) || (typeof d.rank == "number" ? d.rank : 1e9) - (typeof p.rank == "number" ? p.rank : 1e9) || (d.name < p.name ? -1 : 1);
    for (let d of i.sort(u))
      c -= 1e5, f[d.name] = c;
    for (let d of e) {
      let { section: p } = d.completion;
      p && (d.score += f[typeof p == "string" ? p : p.name]);
    }
  }
  let l = [], a = null, h = o.compareCompletions;
  for (let f of e.sort((c, u) => u.score - c.score || h(c.completion, u.completion))) {
    let c = f.completion;
    !a || a.label != c.label || a.detail != c.detail || a.type != null && c.type != null && a.type != c.type || a.apply != c.apply || a.boost != c.boost ? l.push(f) : nl(f.completion) > nl(a) && (l[l.length - 1] = f), a = f.completion;
  }
  return l;
}
class Le {
  constructor(t, e, i, s, r, o) {
    this.options = t, this.attrs = e, this.tooltip = i, this.timestamp = s, this.selected = r, this.disabled = o;
  }
  setSelected(t, e) {
    return t == this.selected || t >= this.options.length ? this : new Le(this.options, rl(e, t), this.tooltip, this.timestamp, t, this.disabled);
  }
  static build(t, e, i, s, r, o) {
    if (s && !o && t.some((h) => h.isPending))
      return s.setDisabled();
    let l = Qp(t, e);
    if (!l.length)
      return s && t.some((h) => h.isPending) ? s.setDisabled() : null;
    let a = e.facet(Z).selectOnOpen ? 0 : -1;
    if (s && s.selected != a && s.selected != -1) {
      let h = s.options[s.selected].completion;
      for (let f = 0; f < l.length; f++)
        if (l[f].completion == h) {
          a = f;
          break;
        }
    }
    return new Le(l, rl(i, a), {
      pos: t.reduce((h, f) => f.hasResult() ? Math.min(h, f.from) : h, 1e8),
      create: Kp,
      above: r.aboveCursor
    }, s ? s.timestamp : Date.now(), a, !1);
  }
  map(t) {
    return new Le(this.options, this.attrs, { ...this.tooltip, pos: t.mapPos(this.tooltip.pos) }, this.timestamp, this.selected, this.disabled);
  }
  setDisabled() {
    return new Le(this.options, this.attrs, this.tooltip, this.timestamp, this.selected, !0);
  }
}
class As {
  constructor(t, e, i) {
    this.active = t, this.id = e, this.open = i;
  }
  static start() {
    return new As(qp, "cm-ac-" + Math.floor(Math.random() * 2e6).toString(36), null);
  }
  update(t) {
    let { state: e } = t, i = e.facet(Z), r = (i.override || e.languageDataAt("autocomplete", Oe(e)).map(Ep)).map((a) => (this.active.find((f) => f.source == a) || new Ot(
      a,
      this.active.some(
        (f) => f.state != 0
        /* State.Inactive */
      ) ? 1 : 0
      /* State.Inactive */
    )).update(t, i));
    r.length == this.active.length && r.every((a, h) => a == this.active[h]) && (r = this.active);
    let o = this.open, l = t.effects.some((a) => a.is(Lr));
    o && t.docChanged && (o = o.map(t.changes)), t.selection || r.some((a) => a.hasResult() && t.changes.touchesRange(a.from, a.to)) || !$p(r, this.active) || l ? o = Le.build(r, e, this.id, o, i, l) : o && o.disabled && !r.some((a) => a.isPending) && (o = null), !o && r.every((a) => !a.isPending) && r.some((a) => a.hasResult()) && (r = r.map((a) => a.hasResult() ? new Ot(
      a.source,
      0
      /* State.Inactive */
    ) : a));
    for (let a of t.effects)
      a.is(Rr) && (o = o && o.setSelected(a.value, this.id));
    return r == this.active && o == this.open ? this : new As(r, this.id, o);
  }
  get tooltip() {
    return this.open ? this.open.tooltip : null;
  }
  get attrs() {
    return this.open ? this.open.attrs : this.active.length ? jp : Xp;
  }
}
function $p(n, t) {
  if (n == t)
    return !0;
  for (let e = 0, i = 0; ; ) {
    for (; e < n.length && !n[e].hasResult(); )
      e++;
    for (; i < t.length && !t[i].hasResult(); )
      i++;
    let s = e == n.length, r = i == t.length;
    if (s || r)
      return s == r;
    if (n[e++].result != t[i++].result)
      return !1;
  }
}
const jp = {
  "aria-autocomplete": "list"
}, Xp = {};
function rl(n, t) {
  let e = {
    "aria-autocomplete": "list",
    "aria-haspopup": "listbox",
    "aria-controls": n
  };
  return t > -1 && (e["aria-activedescendant"] = n + "-" + t), e;
}
const qp = [];
function Mh(n, t) {
  if (n.isUserEvent("input.complete")) {
    let i = n.annotation(Th);
    if (i && t.activateOnCompletion(i))
      return 12;
  }
  let e = n.isUserEvent("input.type");
  return e && t.activateOnTyping ? 5 : e ? 1 : n.isUserEvent("delete.backward") ? 2 : n.selection ? 8 : n.docChanged ? 16 : 0;
}
class Ot {
  constructor(t, e, i = !1) {
    this.source = t, this.state = e, this.explicit = i;
  }
  hasResult() {
    return !1;
  }
  get isPending() {
    return this.state == 1;
  }
  update(t, e) {
    let i = Mh(t, e), s = this;
    (i & 8 || i & 16 && this.touches(t)) && (s = new Ot(
      s.source,
      0
      /* State.Inactive */
    )), i & 4 && s.state == 0 && (s = new Ot(
      this.source,
      1
      /* State.Pending */
    )), s = s.updateFor(t, i);
    for (let r of t.effects)
      if (r.is(Cs))
        s = new Ot(s.source, 1, r.value);
      else if (r.is(bi))
        s = new Ot(
          s.source,
          0
          /* State.Inactive */
        );
      else if (r.is(Lr))
        for (let o of r.value)
          o.source == s.source && (s = o);
    return s;
  }
  updateFor(t, e) {
    return this.map(t.changes);
  }
  map(t) {
    return this;
  }
  touches(t) {
    return t.changes.touchesRange(Oe(t.state));
  }
}
class We extends Ot {
  constructor(t, e, i, s, r, o) {
    super(t, 3, e), this.limit = i, this.result = s, this.from = r, this.to = o;
  }
  hasResult() {
    return !0;
  }
  updateFor(t, e) {
    var i;
    if (!(e & 3))
      return this.map(t.changes);
    let s = this.result;
    s.map && !t.changes.empty && (s = s.map(s, t.changes));
    let r = t.changes.mapPos(this.from), o = t.changes.mapPos(this.to, 1), l = Oe(t.state);
    if (l > o || !s || e & 2 && (Oe(t.startState) == this.from || l < this.limit))
      return new Ot(
        this.source,
        e & 4 ? 1 : 0
        /* State.Inactive */
      );
    let a = t.changes.mapPos(this.limit);
    return Up(s.validFor, t.state, r, o) ? new We(this.source, this.explicit, a, s, r, o) : s.update && (s = s.update(s, r, o, new Ch(t.state, l, !1))) ? new We(this.source, this.explicit, a, s, s.from, (i = s.to) !== null && i !== void 0 ? i : Oe(t.state)) : new Ot(this.source, 1, this.explicit);
  }
  map(t) {
    return t.empty ? this : (this.result.map ? this.result.map(this.result, t) : this.result) ? new We(this.source, this.explicit, t.mapPos(this.limit), this.result, t.mapPos(this.from), t.mapPos(this.to, 1)) : new Ot(
      this.source,
      0
      /* State.Inactive */
    );
  }
  touches(t) {
    return t.changes.touchesRange(this.from, this.to);
  }
}
function Up(n, t, e, i) {
  if (!n)
    return !1;
  let s = t.sliceDoc(e, i);
  return typeof n == "function" ? n(s, e, i, t) : Ah(n, !0).test(s);
}
const Lr = /* @__PURE__ */ N.define({
  map(n, t) {
    return n.map((e) => e.map(t));
  }
}), at = /* @__PURE__ */ At.define({
  create() {
    return As.start();
  },
  update(n, t) {
    return n.update(t);
  },
  provide: (n) => [
    vr.from(n, (t) => t.tooltip),
    P.contentAttributes.from(n, (t) => t.attrs)
  ]
});
function Er(n, t) {
  const e = t.completion.apply || t.completion.label;
  let i = n.state.field(at).active.find((s) => s.source == t.source);
  return i instanceof We ? (typeof e == "string" ? n.dispatch({
    ...Lp(n.state, e, i.from, i.to),
    annotations: Th.of(t.completion)
  }) : e(n, t.completion, i.from, i.to), !0) : !1;
}
const Kp = /* @__PURE__ */ Fp(at, Er);
function _i(n, t = "option") {
  return (e) => {
    let i = e.state.field(at, !1);
    if (!i || !i.open || i.open.disabled || Date.now() - i.open.timestamp < e.state.facet(Z).interactionDelay)
      return !1;
    let s = 1, r;
    t == "page" && (r = Ra(e, i.open.tooltip)) && (s = Math.max(2, Math.floor(r.dom.offsetHeight / r.dom.querySelector("li").offsetHeight) - 1));
    let { length: o } = i.open.options, l = i.open.selected > -1 ? i.open.selected + s * (n ? 1 : -1) : n ? 0 : o - 1;
    return l < 0 ? l = t == "page" ? 0 : o - 1 : l >= o && (l = t == "page" ? o - 1 : 0), e.dispatch({ effects: Rr.of(l) }), !0;
  };
}
const _p = (n) => {
  let t = n.state.field(at, !1);
  return n.state.readOnly || !t || !t.open || t.open.selected < 0 || t.open.disabled || Date.now() - t.open.timestamp < n.state.facet(Z).interactionDelay ? !1 : Er(n, t.open.options[t.open.selected]);
}, pn = (n) => n.state.field(at, !1) ? (n.dispatch({ effects: Cs.of(!0) }), !0) : !1, Gp = (n) => {
  let t = n.state.field(at, !1);
  return !t || !t.active.some(
    (e) => e.state != 0
    /* State.Inactive */
  ) ? !1 : (n.dispatch({ effects: bi.of(null) }), !0);
};
class Yp {
  constructor(t, e) {
    this.active = t, this.context = e, this.time = Date.now(), this.updates = [], this.done = void 0;
  }
}
const Zp = 50, Jp = 1e3, tg = /* @__PURE__ */ dt.fromClass(class {
  constructor(n) {
    this.view = n, this.debounceUpdate = -1, this.running = [], this.debounceAccept = -1, this.pendingStart = !1, this.composing = 0;
    for (let t of n.state.field(at).active)
      t.isPending && this.startQuery(t);
  }
  update(n) {
    let t = n.state.field(at), e = n.state.facet(Z);
    if (!n.selectionSet && !n.docChanged && n.startState.field(at) == t)
      return;
    let i = n.transactions.some((r) => {
      let o = Mh(r, e);
      return o & 8 || (r.selection || r.docChanged) && !(o & 3);
    });
    for (let r = 0; r < this.running.length; r++) {
      let o = this.running[r];
      if (i || o.context.abortOnDocChange && n.docChanged || o.updates.length + n.transactions.length > Zp && Date.now() - o.time > Jp) {
        for (let l of o.context.abortListeners)
          try {
            l();
          } catch (a) {
            ft(this.view.state, a);
          }
        o.context.abortListeners = null, this.running.splice(r--, 1);
      } else
        o.updates.push(...n.transactions);
    }
    this.debounceUpdate > -1 && clearTimeout(this.debounceUpdate), n.transactions.some((r) => r.effects.some((o) => o.is(Cs))) && (this.pendingStart = !0);
    let s = this.pendingStart ? 50 : e.activateOnTypingDelay;
    if (this.debounceUpdate = t.active.some((r) => r.isPending && !this.running.some((o) => o.active.source == r.source)) ? setTimeout(() => this.startUpdate(), s) : -1, this.composing != 0)
      for (let r of n.transactions)
        r.isUserEvent("input.type") ? this.composing = 2 : this.composing == 2 && r.selection && (this.composing = 3);
  }
  startUpdate() {
    this.debounceUpdate = -1, this.pendingStart = !1;
    let { state: n } = this.view, t = n.field(at);
    for (let e of t.active)
      e.isPending && !this.running.some((i) => i.active.source == e.source) && this.startQuery(e);
    this.running.length && t.open && t.open.disabled && (this.debounceAccept = setTimeout(() => this.accept(), this.view.state.facet(Z).updateSyncTime));
  }
  startQuery(n) {
    let { state: t } = this.view, e = Oe(t), i = new Ch(t, e, n.explicit, this.view), s = new Yp(n, i);
    this.running.push(s), Promise.resolve(n.source(i)).then((r) => {
      s.context.aborted || (s.done = r || null, this.scheduleAccept());
    }, (r) => {
      this.view.dispatch({ effects: bi.of(null) }), ft(this.view.state, r);
    });
  }
  scheduleAccept() {
    this.running.every((n) => n.done !== void 0) ? this.accept() : this.debounceAccept < 0 && (this.debounceAccept = setTimeout(() => this.accept(), this.view.state.facet(Z).updateSyncTime));
  }
  // For each finished query in this.running, try to create a result
  // or, if appropriate, restart the query.
  accept() {
    var n;
    this.debounceAccept > -1 && clearTimeout(this.debounceAccept), this.debounceAccept = -1;
    let t = [], e = this.view.state.facet(Z), i = this.view.state.field(at);
    for (let s = 0; s < this.running.length; s++) {
      let r = this.running[s];
      if (r.done === void 0)
        continue;
      if (this.running.splice(s--, 1), r.done) {
        let l = Oe(r.updates.length ? r.updates[0].startState : this.view.state), a = Math.min(l, r.done.from + (r.active.explicit ? 0 : 1)), h = new We(r.active.source, r.active.explicit, a, r.done, r.done.from, (n = r.done.to) !== null && n !== void 0 ? n : l);
        for (let f of r.updates)
          h = h.update(f, e);
        if (h.hasResult()) {
          t.push(h);
          continue;
        }
      }
      let o = i.active.find((l) => l.source == r.active.source);
      if (o && o.isPending)
        if (r.done == null) {
          let l = new Ot(
            r.active.source,
            0
            /* State.Inactive */
          );
          for (let a of r.updates)
            l = l.update(a, e);
          l.isPending || t.push(l);
        } else
          this.startQuery(o);
    }
    (t.length || i.open && i.open.disabled) && this.view.dispatch({ effects: Lr.of(t) });
  }
}, {
  eventHandlers: {
    blur(n) {
      let t = this.view.state.field(at, !1);
      if (t && t.tooltip && this.view.state.facet(Z).closeOnBlur) {
        let e = t.open && Ra(this.view, t.open.tooltip);
        (!e || !e.dom.contains(n.relatedTarget)) && setTimeout(() => this.view.dispatch({ effects: bi.of(null) }), 10);
      }
    },
    compositionstart() {
      this.composing = 1;
    },
    compositionend() {
      this.composing == 3 && setTimeout(() => this.view.dispatch({ effects: Cs.of(!1) }), 20), this.composing = 0;
    }
  }
}), eg = typeof navigator == "object" && /* @__PURE__ */ /Win/.test(navigator.platform), ig = /* @__PURE__ */ wi.highest(/* @__PURE__ */ P.domEventHandlers({
  keydown(n, t) {
    let e = t.state.field(at, !1);
    if (!e || !e.open || e.open.disabled || e.open.selected < 0 || n.key.length > 1 || n.ctrlKey && !(eg && n.altKey) || n.metaKey)
      return !1;
    let i = e.open.options[e.open.selected], s = e.active.find((o) => o.source == i.source), r = i.completion.commitCharacters || s.result.commitCharacters;
    return r && r.indexOf(n.key) > -1 && Er(t, i), !1;
  }
})), sg = /* @__PURE__ */ P.baseTheme({
  ".cm-tooltip.cm-tooltip-autocomplete": {
    "& > ul": {
      fontFamily: "monospace",
      whiteSpace: "nowrap",
      overflow: "hidden auto",
      maxWidth_fallback: "700px",
      maxWidth: "min(700px, 95vw)",
      minWidth: "250px",
      maxHeight: "10em",
      height: "100%",
      listStyle: "none",
      margin: 0,
      padding: 0,
      "& > li, & > completion-section": {
        padding: "1px 3px",
        lineHeight: 1.2
      },
      "& > li": {
        overflowX: "hidden",
        textOverflow: "ellipsis",
        cursor: "pointer"
      },
      "& > completion-section": {
        display: "list-item",
        borderBottom: "1px solid silver",
        paddingLeft: "0.5em",
        opacity: 0.7
      }
    }
  },
  "&light .cm-tooltip-autocomplete ul li[aria-selected]": {
    background: "#17c",
    color: "white"
  },
  "&light .cm-tooltip-autocomplete-disabled ul li[aria-selected]": {
    background: "#777"
  },
  "&dark .cm-tooltip-autocomplete ul li[aria-selected]": {
    background: "#347",
    color: "white"
  },
  "&dark .cm-tooltip-autocomplete-disabled ul li[aria-selected]": {
    background: "#444"
  },
  ".cm-completionListIncompleteTop:before, .cm-completionListIncompleteBottom:after": {
    content: '"···"',
    opacity: 0.5,
    display: "block",
    textAlign: "center"
  },
  ".cm-tooltip.cm-completionInfo": {
    position: "absolute",
    padding: "3px 9px",
    width: "max-content",
    maxWidth: "400px",
    boxSizing: "border-box",
    whiteSpace: "pre-line"
  },
  ".cm-completionInfo.cm-completionInfo-left": { right: "100%" },
  ".cm-completionInfo.cm-completionInfo-right": { left: "100%" },
  ".cm-completionInfo.cm-completionInfo-left-narrow": { right: "30px" },
  ".cm-completionInfo.cm-completionInfo-right-narrow": { left: "30px" },
  "&light .cm-snippetField": { backgroundColor: "#00000022" },
  "&dark .cm-snippetField": { backgroundColor: "#ffffff22" },
  ".cm-snippetFieldPosition": {
    verticalAlign: "text-top",
    width: 0,
    height: "1.15em",
    display: "inline-block",
    margin: "0 -0.7px -.7em",
    borderLeft: "1.4px dotted #888"
  },
  ".cm-completionMatchedText": {
    textDecoration: "underline"
  },
  ".cm-completionDetail": {
    marginLeft: "0.5em",
    fontStyle: "italic"
  },
  ".cm-completionIcon": {
    fontSize: "90%",
    width: ".8em",
    display: "inline-block",
    textAlign: "center",
    paddingRight: ".6em",
    opacity: "0.6",
    boxSizing: "content-box"
  },
  ".cm-completionIcon-function, .cm-completionIcon-method": {
    "&:after": { content: "'ƒ'" }
  },
  ".cm-completionIcon-class": {
    "&:after": { content: "'○'" }
  },
  ".cm-completionIcon-interface": {
    "&:after": { content: "'◌'" }
  },
  ".cm-completionIcon-variable": {
    "&:after": { content: "'𝑥'" }
  },
  ".cm-completionIcon-constant": {
    "&:after": { content: "'𝐶'" }
  },
  ".cm-completionIcon-type": {
    "&:after": { content: "'𝑡'" }
  },
  ".cm-completionIcon-enum": {
    "&:after": { content: "'∪'" }
  },
  ".cm-completionIcon-property": {
    "&:after": { content: "'□'" }
  },
  ".cm-completionIcon-keyword": {
    "&:after": { content: "'🔑︎'" }
    // Disable emoji rendering
  },
  ".cm-completionIcon-namespace": {
    "&:after": { content: "'▢'" }
  },
  ".cm-completionIcon-text": {
    "&:after": { content: "'abc'", fontSize: "50%", verticalAlign: "middle" }
  }
}), Ts = {
  brackets: ["(", "[", "{", "'", '"'],
  before: ")]}:;>",
  stringPrefixes: []
}, ve = /* @__PURE__ */ N.define({
  map(n, t) {
    let e = t.mapPos(n, -1, ot.TrackAfter);
    return e ?? void 0;
  }
}), Ir = /* @__PURE__ */ new class extends le {
}();
Ir.startSide = 1;
Ir.endSide = -1;
const Ph = /* @__PURE__ */ At.define({
  create() {
    return L.empty;
  },
  update(n, t) {
    if (n = n.map(t.changes), t.selection) {
      let e = t.state.doc.lineAt(t.selection.main.head);
      n = n.update({ filter: (i) => i >= e.from && i <= e.to });
    }
    for (let e of t.effects)
      e.is(ve) && (n = n.update({ add: [Ir.range(e.value, e.value + 1)] }));
    return n;
  }
});
function ng() {
  return [ag, Ph];
}
const gn = "()[]{}<>«»»«［］｛｝";
function rg(n) {
  for (let t = 0; t < gn.length; t += 2)
    if (gn.charCodeAt(t) == n)
      return gn.charAt(t + 1);
  return Al(n < 128 ? n : n + 1);
}
function og(n, t) {
  return n.languageDataAt("closeBrackets", t)[0] || Ts;
}
const lg = typeof navigator == "object" && /* @__PURE__ */ /Android\b/.test(navigator.userAgent), ag = /* @__PURE__ */ P.inputHandler.of((n, t, e, i) => {
  if ((lg ? n.composing : n.compositionStarted) || n.state.readOnly)
    return !1;
  let s = n.state.selection.main;
  if (i.length > 2 || i.length == 2 && se(Ft(i, 0)) == 1 || t != s.from || e != s.to)
    return !1;
  let r = hg(n.state, i);
  return r ? (n.dispatch(r), !0) : !1;
});
function hg(n, t) {
  let e = og(n, n.selection.main.head), i = e.brackets || Ts.brackets;
  for (let s of i) {
    let r = rg(Ft(s, 0));
    if (t == s)
      return r == s ? ug(n, s, i.indexOf(s + s + s) > -1, e) : fg(n, s, r, e.before || Ts.before);
    if (t == r && Dh(n, n.selection.main.from))
      return cg(n, s, r);
  }
  return null;
}
function Dh(n, t) {
  let e = !1;
  return n.field(Ph).between(0, n.doc.length, (i) => {
    i == t && (e = !0);
  }), e;
}
function Nr(n, t) {
  let e = n.sliceString(t, t + 2);
  return e.slice(0, se(Ft(e, 0)));
}
function fg(n, t, e, i) {
  let s = null, r = n.changeByRange((o) => {
    if (!o.empty)
      return {
        changes: [{ insert: t, from: o.from }, { insert: e, from: o.to }],
        effects: ve.of(o.to + t.length),
        range: k.range(o.anchor + t.length, o.head + t.length)
      };
    let l = Nr(n.doc, o.head);
    return !l || /\s/.test(l) || i.indexOf(l) > -1 ? {
      changes: { insert: t + e, from: o.head },
      effects: ve.of(o.head + t.length),
      range: k.cursor(o.head + t.length)
    } : { range: s = o };
  });
  return s ? null : n.update(r, {
    scrollIntoView: !0,
    userEvent: "input.type"
  });
}
function cg(n, t, e) {
  let i = null, s = n.changeByRange((r) => r.empty && Nr(n.doc, r.head) == e ? {
    changes: { from: r.head, to: r.head + e.length, insert: e },
    range: k.cursor(r.head + e.length)
  } : i = { range: r });
  return i ? null : n.update(s, {
    scrollIntoView: !0,
    userEvent: "input.type"
  });
}
function ug(n, t, e, i) {
  let s = i.stringPrefixes || Ts.stringPrefixes, r = null, o = n.changeByRange((l) => {
    if (!l.empty)
      return {
        changes: [{ insert: t, from: l.from }, { insert: t, from: l.to }],
        effects: ve.of(l.to + t.length),
        range: k.range(l.anchor + t.length, l.head + t.length)
      };
    let a = l.head, h = Nr(n.doc, a), f;
    if (h == t) {
      if (ol(n, a))
        return {
          changes: { insert: t + t, from: a },
          effects: ve.of(a + t.length),
          range: k.cursor(a + t.length)
        };
      if (Dh(n, a)) {
        let u = e && n.sliceDoc(a, a + t.length * 3) == t + t + t ? t + t + t : t;
        return {
          changes: { from: a, to: a + u.length, insert: u },
          range: k.cursor(a + u.length)
        };
      }
    } else {
      if (e && n.sliceDoc(a - 2 * t.length, a) == t + t && (f = ll(n, a - 2 * t.length, s)) > -1 && ol(n, f))
        return {
          changes: { insert: t + t + t + t, from: a },
          effects: ve.of(a + t.length),
          range: k.cursor(a + t.length)
        };
      if (n.charCategorizer(a)(h) != kt.Word && ll(n, a, s) > -1 && !dg(n, a, t, s))
        return {
          changes: { insert: t + t, from: a },
          effects: ve.of(a + t.length),
          range: k.cursor(a + t.length)
        };
    }
    return { range: r = l };
  });
  return r ? null : n.update(o, {
    scrollIntoView: !0,
    userEvent: "input.type"
  });
}
function ol(n, t) {
  let e = bt(n).resolveInner(t + 1);
  return e.parent && e.from == t;
}
function dg(n, t, e, i) {
  let s = bt(n).resolveInner(t, -1), r = i.reduce((o, l) => Math.max(o, l.length), 0);
  for (let o = 0; o < 5; o++) {
    let l = n.sliceDoc(s.from, Math.min(s.to, s.from + e.length + r)), a = l.indexOf(e);
    if (!a || a > -1 && i.indexOf(l.slice(0, a)) > -1) {
      let f = s.firstChild;
      for (; f && f.from == s.from && f.to - f.from > e.length + a; ) {
        if (n.sliceDoc(f.to - e.length, f.to) == e)
          return !1;
        f = f.firstChild;
      }
      return !0;
    }
    let h = s.to == t && s.parent;
    if (!h)
      break;
    s = h;
  }
  return !1;
}
function ll(n, t, e) {
  let i = n.charCategorizer(t);
  if (i(n.sliceDoc(t - 1, t)) != kt.Word)
    return t;
  for (let s of e) {
    let r = t - s.length;
    if (n.sliceDoc(r, t) == s && i(n.sliceDoc(r - 1, r)) != kt.Word)
      return r;
  }
  return -1;
}
function pg(n = {}) {
  return [
    ig,
    at,
    Z.of(n),
    tg,
    mg,
    sg
  ];
}
const gg = [
  { key: "Ctrl-Space", run: pn },
  { mac: "Alt-`", run: pn },
  { mac: "Alt-i", run: pn },
  { key: "Escape", run: Gp },
  { key: "ArrowDown", run: /* @__PURE__ */ _i(!0) },
  { key: "ArrowUp", run: /* @__PURE__ */ _i(!1) },
  { key: "PageDown", run: /* @__PURE__ */ _i(!0, "page") },
  { key: "PageUp", run: /* @__PURE__ */ _i(!1, "page") },
  { key: "Enter", run: _p }
], mg = /* @__PURE__ */ wi.highest(/* @__PURE__ */ wr.computeN([Z], (n) => n.facet(Z).defaultKeymap ? [gg] : []));
class al {
  constructor(t, e, i) {
    this.from = t, this.to = e, this.diagnostic = i;
  }
}
class xe {
  constructor(t, e, i) {
    this.diagnostics = t, this.panel = e, this.selected = i;
  }
  static init(t, e, i) {
    let s = i.facet(xi).markerFilter;
    s && (t = s(t, i));
    let r = t.slice().sort((d, p) => d.from - p.from || d.to - p.to), o = new ze(), l = [], a = 0, h = i.doc.iter(), f = 0, c = i.doc.length;
    for (let d = 0; ; ) {
      let p = d == r.length ? null : r[d];
      if (!p && !l.length)
        break;
      let g, m;
      if (l.length)
        g = a, m = l.reduce((x, O) => Math.min(x, O.to), p && p.from > g ? p.from : 1e8);
      else {
        if (g = p.from, g > c)
          break;
        m = p.to, l.push(p), d++;
      }
      for (; d < r.length; ) {
        let x = r[d];
        if (x.from == g && (x.to > x.from || x.to == g))
          l.push(x), d++, m = Math.min(x.to, m);
        else {
          m = Math.min(x.from, m);
          break;
        }
      }
      m = Math.min(m, c);
      let y = !1;
      if (l.some((x) => x.from == g && (x.to == m || m == c)) && (y = g == m, !y && m - g < 10)) {
        let x = g - (f + h.value.length);
        x > 0 && (h.next(x), f = g);
        for (let O = g; ; ) {
          if (O >= m) {
            y = !0;
            break;
          }
          if (!h.lineBreak && f + h.value.length > O)
            break;
          O = f + h.value.length, f += h.value.length, h.next();
        }
      }
      let b = Tg(l);
      if (y)
        o.add(g, g, H.widget({
          widget: new Sg(b),
          diagnostics: l.slice()
        }));
      else {
        let x = l.reduce((O, S) => S.markClass ? O + " " + S.markClass : O, "");
        o.add(g, m, H.mark({
          class: "cm-lintRange cm-lintRange-" + b + x,
          diagnostics: l.slice(),
          inclusiveEnd: l.some((O) => O.to > m)
        }));
      }
      if (a = m, a == c)
        break;
      for (let x = 0; x < l.length; x++)
        l[x].to <= a && l.splice(x--, 1);
    }
    let u = o.finish();
    return new xe(u, e, Ue(u));
  }
}
function Ue(n, t = null, e = 0) {
  let i = null;
  return n.between(e, 1e9, (s, r, { spec: o }) => {
    if (!(t && o.diagnostics.indexOf(t) < 0))
      if (!i)
        i = new al(s, r, t || o.diagnostics[0]);
      else {
        if (o.diagnostics.indexOf(i.diagnostic) < 0)
          return !1;
        i = new al(i.from, r, i.diagnostic);
      }
  }), i;
}
function yg(n, t) {
  let e = t.pos, i = t.end || e, s = n.state.facet(xi).hideOn(n, e, i);
  if (s != null)
    return s;
  let r = n.startState.doc.lineAt(t.pos);
  return !!(n.effects.some((o) => o.is(Vr)) || n.changes.touchesRange(r.from, Math.max(r.to, i)));
}
function bg(n, t) {
  return n.field(Bt, !1) ? t : t.concat(N.appendConfig.of(Mg));
}
function xg(n, t) {
  return {
    effects: bg(n, [Vr.of(t)])
  };
}
const Vr = /* @__PURE__ */ N.define(), Bh = /* @__PURE__ */ N.define(), Rh = /* @__PURE__ */ N.define(), Bt = /* @__PURE__ */ At.define({
  create() {
    return new xe(H.none, null, null);
  },
  update(n, t) {
    if (t.docChanged && n.diagnostics.size) {
      let e = n.diagnostics.map(t.changes), i = null, s = n.panel;
      if (n.selected) {
        let r = t.changes.mapPos(n.selected.from, 1);
        i = Ue(e, n.selected.diagnostic, r) || Ue(e, null, r);
      }
      !e.size && s && t.state.facet(xi).autoPanel && (s = null), n = new xe(e, s, i);
    }
    for (let e of t.effects)
      if (e.is(Vr)) {
        let i = t.state.facet(xi).autoPanel ? e.value.length ? Ms.open : null : n.panel;
        n = xe.init(e.value, i, t.state);
      } else e.is(Bh) ? n = new xe(n.diagnostics, e.value ? Ms.open : null, n.selected) : e.is(Rh) && (n = new xe(n.diagnostics, n.panel, e.value));
    return n;
  },
  provide: (n) => [
    Un.from(n, (t) => t.panel),
    P.decorations.from(n, (t) => t.diagnostics)
  ]
}), wg = /* @__PURE__ */ H.mark({ class: "cm-lintRange cm-lintRange-active" });
function kg(n, t, e) {
  let { diagnostics: i } = n.state.field(Bt), s, r = -1, o = -1;
  i.between(t - (e < 0 ? 1 : 0), t + (e > 0 ? 1 : 0), (a, h, { spec: f }) => {
    if (t >= a && t <= h && (a == h || (t > a || e > 0) && (t < h || e < 0)))
      return s = f.diagnostics, r = a, o = h, !1;
  });
  let l = n.state.facet(xi).tooltipFilter;
  return s && l && (s = l(s, n.state)), s ? {
    pos: r,
    end: o,
    above: n.state.doc.lineAt(r).to < o,
    create() {
      return { dom: vg(n, s) };
    }
  } : null;
}
function vg(n, t) {
  return Qt("ul", { class: "cm-tooltip-lint" }, t.map((e) => Eh(n, e, !1)));
}
const hl = (n) => {
  let t = n.state.field(Bt, !1);
  return !t || !t.panel ? !1 : (n.dispatch({ effects: Bh.of(!1) }), !0);
}, xi = /* @__PURE__ */ M.define({
  combine(n) {
    return {
      sources: n.map((t) => t.source).filter((t) => t != null),
      ...vi(n.map((t) => t.config), {
        delay: 750,
        markerFilter: null,
        tooltipFilter: null,
        needsRefresh: null,
        hideOn: () => null
      }, {
        delay: Math.max,
        markerFilter: fl,
        tooltipFilter: fl,
        needsRefresh: (t, e) => t ? e ? (i) => t(i) || e(i) : t : e,
        hideOn: (t, e) => t ? e ? (i, s, r) => t(i, s, r) || e(i, s, r) : t : e,
        autoPanel: (t, e) => t || e
      })
    };
  }
});
function fl(n, t) {
  return n ? t ? (e, i) => t(n(e, i), i) : n : t;
}
function Lh(n) {
  let t = [];
  if (n)
    t: for (let { name: e } of n) {
      for (let i = 0; i < e.length; i++) {
        let s = e[i];
        if (/[a-zA-Z]/.test(s) && !t.some((r) => r.toLowerCase() == s.toLowerCase())) {
          t.push(s);
          continue t;
        }
      }
      t.push("");
    }
  return t;
}
function Eh(n, t, e) {
  var i;
  let s = e ? Lh(t.actions) : [];
  return Qt("li", { class: "cm-diagnostic cm-diagnostic-" + t.severity }, Qt("span", { class: "cm-diagnosticText" }, t.renderMessage ? t.renderMessage(n) : t.message), (i = t.actions) === null || i === void 0 ? void 0 : i.map((r, o) => {
    let l = !1, a = (d) => {
      if (d.preventDefault(), l)
        return;
      l = !0;
      let p = Ue(n.state.field(Bt).diagnostics, t);
      p && r.apply(n, p.from, p.to);
    }, { name: h } = r, f = s[o] ? h.indexOf(s[o]) : -1, c = f < 0 ? h : [
      h.slice(0, f),
      Qt("u", h.slice(f, f + 1)),
      h.slice(f + 1)
    ], u = r.markClass ? " " + r.markClass : "";
    return Qt("button", {
      type: "button",
      class: "cm-diagnosticAction" + u,
      onclick: a,
      onmousedown: a,
      "aria-label": ` Action: ${h}${f < 0 ? "" : ` (access key "${s[o]})"`}.`
    }, c);
  }), t.source && Qt("div", { class: "cm-diagnosticSource" }, t.source));
}
class Sg extends Si {
  constructor(t) {
    super(), this.sev = t;
  }
  eq(t) {
    return t.sev == this.sev;
  }
  toDOM() {
    return Qt("span", { class: "cm-lintPoint cm-lintPoint-" + this.sev });
  }
}
class cl {
  constructor(t, e) {
    this.diagnostic = e, this.id = "item_" + Math.floor(Math.random() * 4294967295).toString(16), this.dom = Eh(t, e, !0), this.dom.id = this.id, this.dom.setAttribute("role", "option");
  }
}
class Ms {
  constructor(t) {
    this.view = t, this.items = [];
    let e = (s) => {
      if (!(s.ctrlKey || s.altKey || s.metaKey)) {
        if (s.keyCode == 27)
          hl(this.view), this.view.focus();
        else if (s.keyCode == 38 || s.keyCode == 33)
          this.moveSelection((this.selectedIndex - 1 + this.items.length) % this.items.length);
        else if (s.keyCode == 40 || s.keyCode == 34)
          this.moveSelection((this.selectedIndex + 1) % this.items.length);
        else if (s.keyCode == 36)
          this.moveSelection(0);
        else if (s.keyCode == 35)
          this.moveSelection(this.items.length - 1);
        else if (s.keyCode == 13)
          this.view.focus();
        else if (s.keyCode >= 65 && s.keyCode <= 90 && this.selectedIndex >= 0) {
          let { diagnostic: r } = this.items[this.selectedIndex], o = Lh(r.actions);
          for (let l = 0; l < o.length; l++)
            if (o[l].toUpperCase().charCodeAt(0) == s.keyCode) {
              let a = Ue(this.view.state.field(Bt).diagnostics, r);
              a && r.actions[l].apply(t, a.from, a.to);
            }
        } else
          return;
        s.preventDefault();
      }
    }, i = (s) => {
      for (let r = 0; r < this.items.length; r++)
        this.items[r].dom.contains(s.target) && this.moveSelection(r);
    };
    this.list = Qt("ul", {
      tabIndex: 0,
      role: "listbox",
      "aria-label": this.view.state.phrase("Diagnostics"),
      onkeydown: e,
      onclick: i
    }), this.dom = Qt("div", { class: "cm-panel-lint" }, this.list, Qt("button", {
      type: "button",
      name: "close",
      "aria-label": this.view.state.phrase("close"),
      onclick: () => hl(this.view)
    }, "×")), this.update();
  }
  get selectedIndex() {
    let t = this.view.state.field(Bt).selected;
    if (!t)
      return -1;
    for (let e = 0; e < this.items.length; e++)
      if (this.items[e].diagnostic == t.diagnostic)
        return e;
    return -1;
  }
  update() {
    let { diagnostics: t, selected: e } = this.view.state.field(Bt), i = 0, s = !1, r = null, o = /* @__PURE__ */ new Set();
    for (t.between(0, this.view.state.doc.length, (l, a, { spec: h }) => {
      for (let f of h.diagnostics) {
        if (o.has(f))
          continue;
        o.add(f);
        let c = -1, u;
        for (let d = i; d < this.items.length; d++)
          if (this.items[d].diagnostic == f) {
            c = d;
            break;
          }
        c < 0 ? (u = new cl(this.view, f), this.items.splice(i, 0, u), s = !0) : (u = this.items[c], c > i && (this.items.splice(i, c - i), s = !0)), e && u.diagnostic == e.diagnostic ? u.dom.hasAttribute("aria-selected") || (u.dom.setAttribute("aria-selected", "true"), r = u) : u.dom.hasAttribute("aria-selected") && u.dom.removeAttribute("aria-selected"), i++;
      }
    }); i < this.items.length && !(this.items.length == 1 && this.items[0].diagnostic.from < 0); )
      s = !0, this.items.pop();
    this.items.length == 0 && (this.items.push(new cl(this.view, {
      from: -1,
      to: -1,
      severity: "info",
      message: this.view.state.phrase("No diagnostics")
    })), s = !0), r ? (this.list.setAttribute("aria-activedescendant", r.id), this.view.requestMeasure({
      key: this,
      read: () => ({ sel: r.dom.getBoundingClientRect(), panel: this.list.getBoundingClientRect() }),
      write: ({ sel: l, panel: a }) => {
        let h = a.height / this.list.offsetHeight;
        l.top < a.top ? this.list.scrollTop -= (a.top - l.top) / h : l.bottom > a.bottom && (this.list.scrollTop += (l.bottom - a.bottom) / h);
      }
    })) : this.selectedIndex < 0 && this.list.removeAttribute("aria-activedescendant"), s && this.sync();
  }
  sync() {
    let t = this.list.firstChild;
    function e() {
      let i = t;
      t = i.nextSibling, i.remove();
    }
    for (let i of this.items)
      if (i.dom.parentNode == this.list) {
        for (; t != i.dom; )
          e();
        t = i.dom.nextSibling;
      } else
        this.list.insertBefore(i.dom, t);
    for (; t; )
      e();
  }
  moveSelection(t) {
    if (this.selectedIndex < 0)
      return;
    let e = this.view.state.field(Bt), i = Ue(e.diagnostics, this.items[t].diagnostic);
    i && this.view.dispatch({
      selection: { anchor: i.from, head: i.to },
      scrollIntoView: !0,
      effects: Rh.of(i)
    });
  }
  static open(t) {
    return new Ms(t);
  }
}
function Og(n, t = 'viewBox="0 0 40 40"') {
  return `url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" ${t}>${encodeURIComponent(n)}</svg>')`;
}
function Gi(n) {
  return Og(`<path d="m0 2.5 l2 -1.5 l1 0 l2 1.5 l1 0" stroke="${n}" fill="none" stroke-width=".7"/>`, 'width="6" height="3"');
}
const Cg = /* @__PURE__ */ P.baseTheme({
  ".cm-diagnostic": {
    padding: "3px 6px 3px 8px",
    marginLeft: "-1px",
    display: "block",
    whiteSpace: "pre-wrap"
  },
  ".cm-diagnostic-error": { borderLeft: "5px solid #d11" },
  ".cm-diagnostic-warning": { borderLeft: "5px solid orange" },
  ".cm-diagnostic-info": { borderLeft: "5px solid #999" },
  ".cm-diagnostic-hint": { borderLeft: "5px solid #66d" },
  ".cm-diagnosticAction": {
    font: "inherit",
    border: "none",
    padding: "2px 4px",
    backgroundColor: "#444",
    color: "white",
    borderRadius: "3px",
    marginLeft: "8px",
    cursor: "pointer"
  },
  ".cm-diagnosticSource": {
    fontSize: "70%",
    opacity: 0.7
  },
  ".cm-lintRange": {
    backgroundPosition: "left bottom",
    backgroundRepeat: "repeat-x",
    paddingBottom: "0.7px"
  },
  ".cm-lintRange-error": { backgroundImage: /* @__PURE__ */ Gi("#d11") },
  ".cm-lintRange-warning": { backgroundImage: /* @__PURE__ */ Gi("orange") },
  ".cm-lintRange-info": { backgroundImage: /* @__PURE__ */ Gi("#999") },
  ".cm-lintRange-hint": { backgroundImage: /* @__PURE__ */ Gi("#66d") },
  ".cm-lintRange-active": { backgroundColor: "#ffdd9980" },
  ".cm-tooltip-lint": {
    padding: 0,
    margin: 0
  },
  ".cm-lintPoint": {
    position: "relative",
    "&:after": {
      content: '""',
      position: "absolute",
      bottom: 0,
      left: "-2px",
      borderLeft: "3px solid transparent",
      borderRight: "3px solid transparent",
      borderBottom: "4px solid #d11"
    }
  },
  ".cm-lintPoint-warning": {
    "&:after": { borderBottomColor: "orange" }
  },
  ".cm-lintPoint-info": {
    "&:after": { borderBottomColor: "#999" }
  },
  ".cm-lintPoint-hint": {
    "&:after": { borderBottomColor: "#66d" }
  },
  ".cm-panel.cm-panel-lint": {
    position: "relative",
    "& ul": {
      maxHeight: "100px",
      overflowY: "auto",
      "& [aria-selected]": {
        backgroundColor: "#ddd",
        "& u": { textDecoration: "underline" }
      },
      "&:focus [aria-selected]": {
        background_fallback: "#bdf",
        backgroundColor: "Highlight",
        color_fallback: "white",
        color: "HighlightText"
      },
      "& u": { textDecoration: "none" },
      padding: 0,
      margin: 0
    },
    "& [name=close]": {
      position: "absolute",
      top: "0",
      right: "2px",
      background: "inherit",
      border: "none",
      font: "inherit",
      padding: 0,
      margin: 0
    }
  },
  "&dark .cm-lintRange-active": { backgroundColor: "#86714a80" },
  "&dark .cm-panel.cm-panel-lint ul": {
    "& [aria-selected]": {
      backgroundColor: "#2e343e"
    }
  }
});
function Ag(n) {
  return n == "error" ? 4 : n == "warning" ? 3 : n == "info" ? 2 : 1;
}
function Tg(n) {
  let t = "hint", e = 1;
  for (let i of n) {
    let s = Ag(i.severity);
    s > e && (e = s, t = i.severity);
  }
  return t;
}
const Mg = [
  Bt,
  /* @__PURE__ */ P.decorations.compute([Bt], (n) => {
    let { selected: t, panel: e } = n.field(Bt);
    return !t || !e || t.from == t.to ? H.none : H.set([
      wg.range(t.from, t.to)
    ]);
  }),
  /* @__PURE__ */ hu(kg, { hideOn: yg }),
  Cg
];
class Ps {
  /**
  @internal
  */
  constructor(t, e, i, s, r, o, l, a, h, f = 0, c) {
    this.p = t, this.stack = e, this.state = i, this.reducePos = s, this.pos = r, this.score = o, this.buffer = l, this.bufferBase = a, this.curContext = h, this.lookAhead = f, this.parent = c;
  }
  /**
  @internal
  */
  toString() {
    return `[${this.stack.filter((t, e) => e % 3 == 0).concat(this.state)}]@${this.pos}${this.score ? "!" + this.score : ""}`;
  }
  // Start an empty stack
  /**
  @internal
  */
  static start(t, e, i = 0) {
    let s = t.parser.context;
    return new Ps(t, [], e, i, i, 0, [], 0, s ? new ul(s, s.start) : null, 0, null);
  }
  /**
  The stack's current [context](#lr.ContextTracker) value, if
  any. Its type will depend on the context tracker's type
  parameter, or it will be `null` if there is no context
  tracker.
  */
  get context() {
    return this.curContext ? this.curContext.context : null;
  }
  // Push a state onto the stack, tracking its start position as well
  // as the buffer base at that point.
  /**
  @internal
  */
  pushState(t, e) {
    this.stack.push(this.state, e, this.bufferBase + this.buffer.length), this.state = t;
  }
  // Apply a reduce action
  /**
  @internal
  */
  reduce(t) {
    var e;
    let i = t >> 19, s = t & 65535, { parser: r } = this.p, o = this.reducePos < this.pos - 25 && this.setLookAhead(this.pos), l = r.dynamicPrecedence(s);
    if (l && (this.score += l), i == 0) {
      this.pushState(r.getGoto(this.state, s, !0), this.reducePos), s < r.minRepeatTerm && this.storeNode(s, this.reducePos, this.reducePos, o ? 8 : 4, !0), this.reduceContext(s, this.reducePos);
      return;
    }
    let a = this.stack.length - (i - 1) * 3 - (t & 262144 ? 6 : 0), h = a ? this.stack[a - 2] : this.p.ranges[0].from, f = this.reducePos - h;
    f >= 2e3 && !(!((e = this.p.parser.nodeSet.types[s]) === null || e === void 0) && e.isAnonymous) && (h == this.p.lastBigReductionStart ? (this.p.bigReductionCount++, this.p.lastBigReductionSize = f) : this.p.lastBigReductionSize < f && (this.p.bigReductionCount = 1, this.p.lastBigReductionStart = h, this.p.lastBigReductionSize = f));
    let c = a ? this.stack[a - 1] : 0, u = this.bufferBase + this.buffer.length - c;
    if (s < r.minRepeatTerm || t & 131072) {
      let d = r.stateFlag(
        this.state,
        1
        /* StateFlag.Skipped */
      ) ? this.pos : this.reducePos;
      this.storeNode(s, h, d, u + 4, !0);
    }
    if (t & 262144)
      this.state = this.stack[a];
    else {
      let d = this.stack[a - 3];
      this.state = r.getGoto(d, s, !0);
    }
    for (; this.stack.length > a; )
      this.stack.pop();
    this.reduceContext(s, h);
  }
  // Shift a value into the buffer
  /**
  @internal
  */
  storeNode(t, e, i, s = 4, r = !1) {
    if (t == 0 && (!this.stack.length || this.stack[this.stack.length - 1] < this.buffer.length + this.bufferBase)) {
      let o = this, l = this.buffer.length;
      if (l == 0 && o.parent && (l = o.bufferBase - o.parent.bufferBase, o = o.parent), l > 0 && o.buffer[l - 4] == 0 && o.buffer[l - 1] > -1) {
        if (e == i)
          return;
        if (o.buffer[l - 2] >= e) {
          o.buffer[l - 2] = i;
          return;
        }
      }
    }
    if (!r || this.pos == i)
      this.buffer.push(t, e, i, s);
    else {
      let o = this.buffer.length;
      if (o > 0 && (this.buffer[o - 4] != 0 || this.buffer[o - 1] < 0)) {
        let l = !1;
        for (let a = o; a > 0 && this.buffer[a - 2] > i; a -= 4)
          if (this.buffer[a - 1] >= 0) {
            l = !0;
            break;
          }
        if (l)
          for (; o > 0 && this.buffer[o - 2] > i; )
            this.buffer[o] = this.buffer[o - 4], this.buffer[o + 1] = this.buffer[o - 3], this.buffer[o + 2] = this.buffer[o - 2], this.buffer[o + 3] = this.buffer[o - 1], o -= 4, s > 4 && (s -= 4);
      }
      this.buffer[o] = t, this.buffer[o + 1] = e, this.buffer[o + 2] = i, this.buffer[o + 3] = s;
    }
  }
  // Apply a shift action
  /**
  @internal
  */
  shift(t, e, i, s) {
    if (t & 131072)
      this.pushState(t & 65535, this.pos);
    else if ((t & 262144) == 0) {
      let r = t, { parser: o } = this.p;
      this.pos = s;
      let l = o.stateFlag(
        r,
        1
        /* StateFlag.Skipped */
      );
      !l && (s > i || e <= o.maxNode) && (this.reducePos = s), this.pushState(r, l ? i : Math.min(i, this.reducePos)), this.shiftContext(e, i), e <= o.maxNode && this.buffer.push(e, i, s, 4);
    } else
      this.pos = s, this.shiftContext(e, i), e <= this.p.parser.maxNode && this.buffer.push(e, i, s, 4);
  }
  // Apply an action
  /**
  @internal
  */
  apply(t, e, i, s) {
    t & 65536 ? this.reduce(t) : this.shift(t, e, i, s);
  }
  // Add a prebuilt (reused) node into the buffer.
  /**
  @internal
  */
  useNode(t, e) {
    let i = this.p.reused.length - 1;
    (i < 0 || this.p.reused[i] != t) && (this.p.reused.push(t), i++);
    let s = this.pos;
    this.reducePos = this.pos = s + t.length, this.pushState(e, s), this.buffer.push(
      i,
      s,
      this.reducePos,
      -1
      /* size == -1 means this is a reused value */
    ), this.curContext && this.updateContext(this.curContext.tracker.reuse(this.curContext.context, t, this, this.p.stream.reset(this.pos - t.length)));
  }
  // Split the stack. Due to the buffer sharing and the fact
  // that `this.stack` tends to stay quite shallow, this isn't very
  // expensive.
  /**
  @internal
  */
  split() {
    let t = this, e = t.buffer.length;
    for (; e > 0 && t.buffer[e - 2] > t.reducePos; )
      e -= 4;
    let i = t.buffer.slice(e), s = t.bufferBase + e;
    for (; t && s == t.bufferBase; )
      t = t.parent;
    return new Ps(this.p, this.stack.slice(), this.state, this.reducePos, this.pos, this.score, i, s, this.curContext, this.lookAhead, t);
  }
  // Try to recover from an error by 'deleting' (ignoring) one token.
  /**
  @internal
  */
  recoverByDelete(t, e) {
    let i = t <= this.p.parser.maxNode;
    i && this.storeNode(t, this.pos, e, 4), this.storeNode(0, this.pos, e, i ? 8 : 4), this.pos = this.reducePos = e, this.score -= 190;
  }
  /**
  Check if the given term would be able to be shifted (optionally
  after some reductions) on this stack. This can be useful for
  external tokenizers that want to make sure they only provide a
  given token when it applies.
  */
  canShift(t) {
    for (let e = new Pg(this); ; ) {
      let i = this.p.parser.stateSlot(
        e.state,
        4
        /* ParseState.DefaultReduce */
      ) || this.p.parser.hasAction(e.state, t);
      if (i == 0)
        return !1;
      if ((i & 65536) == 0)
        return !0;
      e.reduce(i);
    }
  }
  // Apply up to Recover.MaxNext recovery actions that conceptually
  // inserts some missing token or rule.
  /**
  @internal
  */
  recoverByInsert(t) {
    if (this.stack.length >= 300)
      return [];
    let e = this.p.parser.nextStates(this.state);
    if (e.length > 8 || this.stack.length >= 120) {
      let s = [];
      for (let r = 0, o; r < e.length; r += 2)
        (o = e[r + 1]) != this.state && this.p.parser.hasAction(o, t) && s.push(e[r], o);
      if (this.stack.length < 120)
        for (let r = 0; s.length < 8 && r < e.length; r += 2) {
          let o = e[r + 1];
          s.some((l, a) => a & 1 && l == o) || s.push(e[r], o);
        }
      e = s;
    }
    let i = [];
    for (let s = 0; s < e.length && i.length < 4; s += 2) {
      let r = e[s + 1];
      if (r == this.state)
        continue;
      let o = this.split();
      o.pushState(r, this.pos), o.storeNode(0, o.pos, o.pos, 4, !0), o.shiftContext(e[s], this.pos), o.reducePos = this.pos, o.score -= 200, i.push(o);
    }
    return i;
  }
  // Force a reduce, if possible. Return false if that can't
  // be done.
  /**
  @internal
  */
  forceReduce() {
    let { parser: t } = this.p, e = t.stateSlot(
      this.state,
      5
      /* ParseState.ForcedReduce */
    );
    if ((e & 65536) == 0)
      return !1;
    if (!t.validAction(this.state, e)) {
      let i = e >> 19, s = e & 65535, r = this.stack.length - i * 3;
      if (r < 0 || t.getGoto(this.stack[r], s, !1) < 0) {
        let o = this.findForcedReduction();
        if (o == null)
          return !1;
        e = o;
      }
      this.storeNode(0, this.pos, this.pos, 4, !0), this.score -= 100;
    }
    return this.reducePos = this.pos, this.reduce(e), !0;
  }
  /**
  Try to scan through the automaton to find some kind of reduction
  that can be applied. Used when the regular ForcedReduce field
  isn't a valid action. @internal
  */
  findForcedReduction() {
    let { parser: t } = this.p, e = [], i = (s, r) => {
      if (!e.includes(s))
        return e.push(s), t.allActions(s, (o) => {
          if (!(o & 393216)) if (o & 65536) {
            let l = (o >> 19) - r;
            if (l > 1) {
              let a = o & 65535, h = this.stack.length - l * 3;
              if (h >= 0 && t.getGoto(this.stack[h], a, !1) >= 0)
                return l << 19 | 65536 | a;
            }
          } else {
            let l = i(o, r + 1);
            if (l != null)
              return l;
          }
        });
    };
    return i(this.state, 0);
  }
  /**
  @internal
  */
  forceAll() {
    for (; !this.p.parser.stateFlag(
      this.state,
      2
      /* StateFlag.Accepting */
    ); )
      if (!this.forceReduce()) {
        this.storeNode(0, this.pos, this.pos, 4, !0);
        break;
      }
    return this;
  }
  /**
  Check whether this state has no further actions (assumed to be a direct descendant of the
  top state, since any other states must be able to continue
  somehow). @internal
  */
  get deadEnd() {
    if (this.stack.length != 3)
      return !1;
    let { parser: t } = this.p;
    return t.data[t.stateSlot(
      this.state,
      1
      /* ParseState.Actions */
    )] == 65535 && !t.stateSlot(
      this.state,
      4
      /* ParseState.DefaultReduce */
    );
  }
  /**
  Restart the stack (put it back in its start state). Only safe
  when this.stack.length == 3 (state is directly below the top
  state). @internal
  */
  restart() {
    this.storeNode(0, this.pos, this.pos, 4, !0), this.state = this.stack[0], this.stack.length = 0;
  }
  /**
  @internal
  */
  sameState(t) {
    if (this.state != t.state || this.stack.length != t.stack.length)
      return !1;
    for (let e = 0; e < this.stack.length; e += 3)
      if (this.stack[e] != t.stack[e])
        return !1;
    return !0;
  }
  /**
  Get the parser used by this stack.
  */
  get parser() {
    return this.p.parser;
  }
  /**
  Test whether a given dialect (by numeric ID, as exported from
  the terms file) is enabled.
  */
  dialectEnabled(t) {
    return this.p.parser.dialect.flags[t];
  }
  shiftContext(t, e) {
    this.curContext && this.updateContext(this.curContext.tracker.shift(this.curContext.context, t, this, this.p.stream.reset(e)));
  }
  reduceContext(t, e) {
    this.curContext && this.updateContext(this.curContext.tracker.reduce(this.curContext.context, t, this, this.p.stream.reset(e)));
  }
  /**
  @internal
  */
  emitContext() {
    let t = this.buffer.length - 1;
    (t < 0 || this.buffer[t] != -3) && this.buffer.push(this.curContext.hash, this.pos, this.pos, -3);
  }
  /**
  @internal
  */
  emitLookAhead() {
    let t = this.buffer.length - 1;
    (t < 0 || this.buffer[t] != -4) && this.buffer.push(this.lookAhead, this.pos, this.pos, -4);
  }
  updateContext(t) {
    if (t != this.curContext.context) {
      let e = new ul(this.curContext.tracker, t);
      e.hash != this.curContext.hash && this.emitContext(), this.curContext = e;
    }
  }
  /**
  @internal
  */
  setLookAhead(t) {
    return t <= this.lookAhead ? !1 : (this.emitLookAhead(), this.lookAhead = t, !0);
  }
  /**
  @internal
  */
  close() {
    this.curContext && this.curContext.tracker.strict && this.emitContext(), this.lookAhead > 0 && this.emitLookAhead();
  }
}
class ul {
  constructor(t, e) {
    this.tracker = t, this.context = e, this.hash = t.strict ? t.hash(e) : 0;
  }
}
class Pg {
  constructor(t) {
    this.start = t, this.state = t.state, this.stack = t.stack, this.base = this.stack.length;
  }
  reduce(t) {
    let e = t & 65535, i = t >> 19;
    i == 0 ? (this.stack == this.start.stack && (this.stack = this.stack.slice()), this.stack.push(this.state, 0, 0), this.base += 3) : this.base -= (i - 1) * 3;
    let s = this.start.p.parser.getGoto(this.stack[this.base - 3], e, !0);
    this.state = s;
  }
}
class Ds {
  constructor(t, e, i) {
    this.stack = t, this.pos = e, this.index = i, this.buffer = t.buffer, this.index == 0 && this.maybeNext();
  }
  static create(t, e = t.bufferBase + t.buffer.length) {
    return new Ds(t, e, e - t.bufferBase);
  }
  maybeNext() {
    let t = this.stack.parent;
    t != null && (this.index = this.stack.bufferBase - t.bufferBase, this.stack = t, this.buffer = t.buffer);
  }
  get id() {
    return this.buffer[this.index - 4];
  }
  get start() {
    return this.buffer[this.index - 3];
  }
  get end() {
    return this.buffer[this.index - 2];
  }
  get size() {
    return this.buffer[this.index - 1];
  }
  next() {
    this.index -= 4, this.pos -= 4, this.index == 0 && this.maybeNext();
  }
  fork() {
    return new Ds(this.stack, this.pos, this.index);
  }
}
function Yi(n, t = Uint16Array) {
  if (typeof n != "string")
    return n;
  let e = null;
  for (let i = 0, s = 0; i < n.length; ) {
    let r = 0;
    for (; ; ) {
      let o = n.charCodeAt(i++), l = !1;
      if (o == 126) {
        r = 65535;
        break;
      }
      o >= 92 && o--, o >= 34 && o--;
      let a = o - 32;
      if (a >= 46 && (a -= 46, l = !0), r += a, l)
        break;
      r *= 46;
    }
    e ? e[s++] = r : e = new t(r);
  }
  return e;
}
class os {
  constructor() {
    this.start = -1, this.value = -1, this.end = -1, this.extended = -1, this.lookAhead = 0, this.mask = 0, this.context = 0;
  }
}
const dl = new os();
class Dg {
  /**
  @internal
  */
  constructor(t, e) {
    this.input = t, this.ranges = e, this.chunk = "", this.chunkOff = 0, this.chunk2 = "", this.chunk2Pos = 0, this.next = -1, this.token = dl, this.rangeIndex = 0, this.pos = this.chunkPos = e[0].from, this.range = e[0], this.end = e[e.length - 1].to, this.readNext();
  }
  /**
  @internal
  */
  resolveOffset(t, e) {
    let i = this.range, s = this.rangeIndex, r = this.pos + t;
    for (; r < i.from; ) {
      if (!s)
        return null;
      let o = this.ranges[--s];
      r -= i.from - o.to, i = o;
    }
    for (; e < 0 ? r > i.to : r >= i.to; ) {
      if (s == this.ranges.length - 1)
        return null;
      let o = this.ranges[++s];
      r += o.from - i.to, i = o;
    }
    return r;
  }
  /**
  @internal
  */
  clipPos(t) {
    if (t >= this.range.from && t < this.range.to)
      return t;
    for (let e of this.ranges)
      if (e.to > t)
        return Math.max(t, e.from);
    return this.end;
  }
  /**
  Look at a code unit near the stream position. `.peek(0)` equals
  `.next`, `.peek(-1)` gives you the previous character, and so
  on.
  
  Note that looking around during tokenizing creates dependencies
  on potentially far-away content, which may reduce the
  effectiveness incremental parsing—when looking forward—or even
  cause invalid reparses when looking backward more than 25 code
  units, since the library does not track lookbehind.
  */
  peek(t) {
    let e = this.chunkOff + t, i, s;
    if (e >= 0 && e < this.chunk.length)
      i = this.pos + t, s = this.chunk.charCodeAt(e);
    else {
      let r = this.resolveOffset(t, 1);
      if (r == null)
        return -1;
      if (i = r, i >= this.chunk2Pos && i < this.chunk2Pos + this.chunk2.length)
        s = this.chunk2.charCodeAt(i - this.chunk2Pos);
      else {
        let o = this.rangeIndex, l = this.range;
        for (; l.to <= i; )
          l = this.ranges[++o];
        this.chunk2 = this.input.chunk(this.chunk2Pos = i), i + this.chunk2.length > l.to && (this.chunk2 = this.chunk2.slice(0, l.to - i)), s = this.chunk2.charCodeAt(0);
      }
    }
    return i >= this.token.lookAhead && (this.token.lookAhead = i + 1), s;
  }
  /**
  Accept a token. By default, the end of the token is set to the
  current stream position, but you can pass an offset (relative to
  the stream position) to change that.
  */
  acceptToken(t, e = 0) {
    let i = e ? this.resolveOffset(e, -1) : this.pos;
    if (i == null || i < this.token.start)
      throw new RangeError("Token end out of bounds");
    this.token.value = t, this.token.end = i;
  }
  /**
  Accept a token ending at a specific given position.
  */
  acceptTokenTo(t, e) {
    this.token.value = t, this.token.end = e;
  }
  getChunk() {
    if (this.pos >= this.chunk2Pos && this.pos < this.chunk2Pos + this.chunk2.length) {
      let { chunk: t, chunkPos: e } = this;
      this.chunk = this.chunk2, this.chunkPos = this.chunk2Pos, this.chunk2 = t, this.chunk2Pos = e, this.chunkOff = this.pos - this.chunkPos;
    } else {
      this.chunk2 = this.chunk, this.chunk2Pos = this.chunkPos;
      let t = this.input.chunk(this.pos), e = this.pos + t.length;
      this.chunk = e > this.range.to ? t.slice(0, this.range.to - this.pos) : t, this.chunkPos = this.pos, this.chunkOff = 0;
    }
  }
  readNext() {
    return this.chunkOff >= this.chunk.length && (this.getChunk(), this.chunkOff == this.chunk.length) ? this.next = -1 : this.next = this.chunk.charCodeAt(this.chunkOff);
  }
  /**
  Move the stream forward N (defaults to 1) code units. Returns
  the new value of [`next`](#lr.InputStream.next).
  */
  advance(t = 1) {
    for (this.chunkOff += t; this.pos + t >= this.range.to; ) {
      if (this.rangeIndex == this.ranges.length - 1)
        return this.setDone();
      t -= this.range.to - this.pos, this.range = this.ranges[++this.rangeIndex], this.pos = this.range.from;
    }
    return this.pos += t, this.pos >= this.token.lookAhead && (this.token.lookAhead = this.pos + 1), this.readNext();
  }
  setDone() {
    return this.pos = this.chunkPos = this.end, this.range = this.ranges[this.rangeIndex = this.ranges.length - 1], this.chunk = "", this.next = -1;
  }
  /**
  @internal
  */
  reset(t, e) {
    if (e ? (this.token = e, e.start = t, e.lookAhead = t + 1, e.value = e.extended = -1) : this.token = dl, this.pos != t) {
      if (this.pos = t, t == this.end)
        return this.setDone(), this;
      for (; t < this.range.from; )
        this.range = this.ranges[--this.rangeIndex];
      for (; t >= this.range.to; )
        this.range = this.ranges[++this.rangeIndex];
      t >= this.chunkPos && t < this.chunkPos + this.chunk.length ? this.chunkOff = t - this.chunkPos : (this.chunk = "", this.chunkOff = 0), this.readNext();
    }
    return this;
  }
  /**
  @internal
  */
  read(t, e) {
    if (t >= this.chunkPos && e <= this.chunkPos + this.chunk.length)
      return this.chunk.slice(t - this.chunkPos, e - this.chunkPos);
    if (t >= this.chunk2Pos && e <= this.chunk2Pos + this.chunk2.length)
      return this.chunk2.slice(t - this.chunk2Pos, e - this.chunk2Pos);
    if (t >= this.range.from && e <= this.range.to)
      return this.input.read(t, e);
    let i = "";
    for (let s of this.ranges) {
      if (s.from >= e)
        break;
      s.to > t && (i += this.input.read(Math.max(s.from, t), Math.min(s.to, e)));
    }
    return i;
  }
}
class He {
  constructor(t, e) {
    this.data = t, this.id = e;
  }
  token(t, e) {
    let { parser: i } = e.p;
    Bg(this.data, t, e, this.id, i.data, i.tokenPrecTable);
  }
}
He.prototype.contextual = He.prototype.fallback = He.prototype.extend = !1;
He.prototype.fallback = He.prototype.extend = !1;
function Bg(n, t, e, i, s, r) {
  let o = 0, l = 1 << i, { dialect: a } = e.p.parser;
  t: for (; (l & n[o]) != 0; ) {
    let h = n[o + 1];
    for (let d = o + 3; d < h; d += 2)
      if ((n[d + 1] & l) > 0) {
        let p = n[d];
        if (a.allows(p) && (t.token.value == -1 || t.token.value == p || Rg(p, t.token.value, s, r))) {
          t.acceptToken(p);
          break;
        }
      }
    let f = t.next, c = 0, u = n[o + 2];
    if (t.next < 0 && u > c && n[h + u * 3 - 3] == 65535) {
      o = n[h + u * 3 - 1];
      continue t;
    }
    for (; c < u; ) {
      let d = c + u >> 1, p = h + d + (d << 1), g = n[p], m = n[p + 1] || 65536;
      if (f < g)
        u = d;
      else if (f >= m)
        c = d + 1;
      else {
        o = n[p + 2], t.advance();
        continue t;
      }
    }
    break;
  }
}
function pl(n, t, e) {
  for (let i = t, s; (s = n[i]) != 65535; i++)
    if (s == e)
      return i - t;
  return -1;
}
function Rg(n, t, e, i) {
  let s = pl(e, i, t);
  return s < 0 || pl(e, i, n) < s;
}
const mt = typeof process < "u" && process.env && /\bparse\b/.test(process.env.LOG);
let mn = null;
function gl(n, t, e) {
  let i = n.cursor(q.IncludeAnonymous);
  for (i.moveTo(t); ; )
    if (!(e < 0 ? i.childBefore(t) : i.childAfter(t)))
      for (; ; ) {
        if ((e < 0 ? i.to < t : i.from > t) && !i.type.isError)
          return e < 0 ? Math.max(0, Math.min(
            i.to - 1,
            t - 25
            /* Lookahead.Margin */
          )) : Math.min(n.length, Math.max(
            i.from + 1,
            t + 25
            /* Lookahead.Margin */
          ));
        if (e < 0 ? i.prevSibling() : i.nextSibling())
          break;
        if (!i.parent())
          return e < 0 ? 0 : n.length;
      }
}
class Lg {
  constructor(t, e) {
    this.fragments = t, this.nodeSet = e, this.i = 0, this.fragment = null, this.safeFrom = -1, this.safeTo = -1, this.trees = [], this.start = [], this.index = [], this.nextFragment();
  }
  nextFragment() {
    let t = this.fragment = this.i == this.fragments.length ? null : this.fragments[this.i++];
    if (t) {
      for (this.safeFrom = t.openStart ? gl(t.tree, t.from + t.offset, 1) - t.offset : t.from, this.safeTo = t.openEnd ? gl(t.tree, t.to + t.offset, -1) - t.offset : t.to; this.trees.length; )
        this.trees.pop(), this.start.pop(), this.index.pop();
      this.trees.push(t.tree), this.start.push(-t.offset), this.index.push(0), this.nextStart = this.safeFrom;
    } else
      this.nextStart = 1e9;
  }
  // `pos` must be >= any previously given `pos` for this cursor
  nodeAt(t) {
    if (t < this.nextStart)
      return null;
    for (; this.fragment && this.safeTo <= t; )
      this.nextFragment();
    if (!this.fragment)
      return null;
    for (; ; ) {
      let e = this.trees.length - 1;
      if (e < 0)
        return this.nextFragment(), null;
      let i = this.trees[e], s = this.index[e];
      if (s == i.children.length) {
        this.trees.pop(), this.start.pop(), this.index.pop();
        continue;
      }
      let r = i.children[s], o = this.start[e] + i.positions[s];
      if (o > t)
        return this.nextStart = o, null;
      if (r instanceof _) {
        if (o == t) {
          if (o < this.safeFrom)
            return null;
          let l = o + r.length;
          if (l <= this.safeTo) {
            let a = r.prop(B.lookAhead);
            if (!a || l + a < this.fragment.to)
              return r;
          }
        }
        this.index[e]++, o + r.length >= Math.max(this.safeFrom, t) && (this.trees.push(r), this.start.push(o), this.index.push(0));
      } else
        this.index[e]++, this.nextStart = o + r.length;
    }
  }
}
class Eg {
  constructor(t, e) {
    this.stream = e, this.tokens = [], this.mainToken = null, this.actions = [], this.tokens = t.tokenizers.map((i) => new os());
  }
  getActions(t) {
    let e = 0, i = null, { parser: s } = t.p, { tokenizers: r } = s, o = s.stateSlot(
      t.state,
      3
      /* ParseState.TokenizerMask */
    ), l = t.curContext ? t.curContext.hash : 0, a = 0;
    for (let h = 0; h < r.length; h++) {
      if ((1 << h & o) == 0)
        continue;
      let f = r[h], c = this.tokens[h];
      if (!(i && !f.fallback) && ((f.contextual || c.start != t.pos || c.mask != o || c.context != l) && (this.updateCachedToken(c, f, t), c.mask = o, c.context = l), c.lookAhead > c.end + 25 && (a = Math.max(c.lookAhead, a)), c.value != 0)) {
        let u = e;
        if (c.extended > -1 && (e = this.addActions(t, c.extended, c.end, e)), e = this.addActions(t, c.value, c.end, e), !f.extend && (i = c, e > u))
          break;
      }
    }
    for (; this.actions.length > e; )
      this.actions.pop();
    return a && t.setLookAhead(a), !i && t.pos == this.stream.end && (i = new os(), i.value = t.p.parser.eofTerm, i.start = i.end = t.pos, e = this.addActions(t, i.value, i.end, e)), this.mainToken = i, this.actions;
  }
  getMainToken(t) {
    if (this.mainToken)
      return this.mainToken;
    let e = new os(), { pos: i, p: s } = t;
    return e.start = i, e.end = Math.min(i + 1, s.stream.end), e.value = i == s.stream.end ? s.parser.eofTerm : 0, e;
  }
  updateCachedToken(t, e, i) {
    let s = this.stream.clipPos(i.pos);
    if (e.token(this.stream.reset(s, t), i), t.value > -1) {
      let { parser: r } = i.p;
      for (let o = 0; o < r.specialized.length; o++)
        if (r.specialized[o] == t.value) {
          let l = r.specializers[o](this.stream.read(t.start, t.end), i);
          if (l >= 0 && i.p.parser.dialect.allows(l >> 1)) {
            (l & 1) == 0 ? t.value = l >> 1 : t.extended = l >> 1;
            break;
          }
        }
    } else
      t.value = 0, t.end = this.stream.clipPos(s + 1);
  }
  putAction(t, e, i, s) {
    for (let r = 0; r < s; r += 3)
      if (this.actions[r] == t)
        return s;
    return this.actions[s++] = t, this.actions[s++] = e, this.actions[s++] = i, s;
  }
  addActions(t, e, i, s) {
    let { state: r } = t, { parser: o } = t.p, { data: l } = o;
    for (let a = 0; a < 2; a++)
      for (let h = o.stateSlot(
        r,
        a ? 2 : 1
        /* ParseState.Actions */
      ); ; h += 3) {
        if (l[h] == 65535)
          if (l[h + 1] == 1)
            h = _t(l, h + 2);
          else {
            s == 0 && l[h + 1] == 2 && (s = this.putAction(_t(l, h + 2), e, i, s));
            break;
          }
        l[h] == e && (s = this.putAction(_t(l, h + 1), e, i, s));
      }
    return s;
  }
}
class Ig {
  constructor(t, e, i, s) {
    this.parser = t, this.input = e, this.ranges = s, this.recovering = 0, this.nextStackID = 9812, this.minStackPos = 0, this.reused = [], this.stoppedAt = null, this.lastBigReductionStart = -1, this.lastBigReductionSize = 0, this.bigReductionCount = 0, this.stream = new Dg(e, s), this.tokens = new Eg(t, this.stream), this.topTerm = t.top[1];
    let { from: r } = s[0];
    this.stacks = [Ps.start(this, t.top[0], r)], this.fragments = i.length && this.stream.end - r > t.bufferLength * 4 ? new Lg(i, t.nodeSet) : null;
  }
  get parsedPos() {
    return this.minStackPos;
  }
  // Move the parser forward. This will process all parse stacks at
  // `this.pos` and try to advance them to a further position. If no
  // stack for such a position is found, it'll start error-recovery.
  //
  // When the parse is finished, this will return a syntax tree. When
  // not, it returns `null`.
  advance() {
    let t = this.stacks, e = this.minStackPos, i = this.stacks = [], s, r;
    if (this.bigReductionCount > 300 && t.length == 1) {
      let [o] = t;
      for (; o.forceReduce() && o.stack.length && o.stack[o.stack.length - 2] >= this.lastBigReductionStart; )
        ;
      this.bigReductionCount = this.lastBigReductionSize = 0;
    }
    for (let o = 0; o < t.length; o++) {
      let l = t[o];
      for (; ; ) {
        if (this.tokens.mainToken = null, l.pos > e)
          i.push(l);
        else {
          if (this.advanceStack(l, i, t))
            continue;
          {
            s || (s = [], r = []), s.push(l);
            let a = this.tokens.getMainToken(l);
            r.push(a.value, a.end);
          }
        }
        break;
      }
    }
    if (!i.length) {
      let o = s && Vg(s);
      if (o)
        return mt && console.log("Finish with " + this.stackID(o)), this.stackToTree(o);
      if (this.parser.strict)
        throw mt && s && console.log("Stuck with token " + (this.tokens.mainToken ? this.parser.getName(this.tokens.mainToken.value) : "none")), new SyntaxError("No parse at " + e);
      this.recovering || (this.recovering = 5);
    }
    if (this.recovering && s) {
      let o = this.stoppedAt != null && s[0].pos > this.stoppedAt ? s[0] : this.runRecovery(s, r, i);
      if (o)
        return mt && console.log("Force-finish " + this.stackID(o)), this.stackToTree(o.forceAll());
    }
    if (this.recovering) {
      let o = this.recovering == 1 ? 1 : this.recovering * 3;
      if (i.length > o)
        for (i.sort((l, a) => a.score - l.score); i.length > o; )
          i.pop();
      i.some((l) => l.reducePos > e) && this.recovering--;
    } else if (i.length > 1) {
      t: for (let o = 0; o < i.length - 1; o++) {
        let l = i[o];
        for (let a = o + 1; a < i.length; a++) {
          let h = i[a];
          if (l.sameState(h) || l.buffer.length > 500 && h.buffer.length > 500)
            if ((l.score - h.score || l.buffer.length - h.buffer.length) > 0)
              i.splice(a--, 1);
            else {
              i.splice(o--, 1);
              continue t;
            }
        }
      }
      i.length > 12 && (i.sort((o, l) => l.score - o.score), i.splice(
        12,
        i.length - 12
        /* Rec.MaxStackCount */
      ));
    }
    this.minStackPos = i[0].pos;
    for (let o = 1; o < i.length; o++)
      i[o].pos < this.minStackPos && (this.minStackPos = i[o].pos);
    return null;
  }
  stopAt(t) {
    if (this.stoppedAt != null && this.stoppedAt < t)
      throw new RangeError("Can't move stoppedAt forward");
    this.stoppedAt = t;
  }
  // Returns an updated version of the given stack, or null if the
  // stack can't advance normally. When `split` and `stacks` are
  // given, stacks split off by ambiguous operations will be pushed to
  // `split`, or added to `stacks` if they move `pos` forward.
  advanceStack(t, e, i) {
    let s = t.pos, { parser: r } = this, o = mt ? this.stackID(t) + " -> " : "";
    if (this.stoppedAt != null && s > this.stoppedAt)
      return t.forceReduce() ? t : null;
    if (this.fragments) {
      let h = t.curContext && t.curContext.tracker.strict, f = h ? t.curContext.hash : 0;
      for (let c = this.fragments.nodeAt(s); c; ) {
        let u = this.parser.nodeSet.types[c.type.id] == c.type ? r.getGoto(t.state, c.type.id) : -1;
        if (u > -1 && c.length && (!h || (c.prop(B.contextHash) || 0) == f))
          return t.useNode(c, u), mt && console.log(o + this.stackID(t) + ` (via reuse of ${r.getName(c.type.id)})`), !0;
        if (!(c instanceof _) || c.children.length == 0 || c.positions[0] > 0)
          break;
        let d = c.children[0];
        if (d instanceof _ && c.positions[0] == 0)
          c = d;
        else
          break;
      }
    }
    let l = r.stateSlot(
      t.state,
      4
      /* ParseState.DefaultReduce */
    );
    if (l > 0)
      return t.reduce(l), mt && console.log(o + this.stackID(t) + ` (via always-reduce ${r.getName(
        l & 65535
        /* Action.ValueMask */
      )})`), !0;
    if (t.stack.length >= 8400)
      for (; t.stack.length > 6e3 && t.forceReduce(); )
        ;
    let a = this.tokens.getActions(t);
    for (let h = 0; h < a.length; ) {
      let f = a[h++], c = a[h++], u = a[h++], d = h == a.length || !i, p = d ? t : t.split(), g = this.tokens.mainToken;
      if (p.apply(f, c, g ? g.start : p.pos, u), mt && console.log(o + this.stackID(p) + ` (via ${(f & 65536) == 0 ? "shift" : `reduce of ${r.getName(
        f & 65535
        /* Action.ValueMask */
      )}`} for ${r.getName(c)} @ ${s}${p == t ? "" : ", split"})`), d)
        return !0;
      p.pos > s ? e.push(p) : i.push(p);
    }
    return !1;
  }
  // Advance a given stack forward as far as it will go. Returns the
  // (possibly updated) stack if it got stuck, or null if it moved
  // forward and was given to `pushStackDedup`.
  advanceFully(t, e) {
    let i = t.pos;
    for (; ; ) {
      if (!this.advanceStack(t, null, null))
        return !1;
      if (t.pos > i)
        return ml(t, e), !0;
    }
  }
  runRecovery(t, e, i) {
    let s = null, r = !1;
    for (let o = 0; o < t.length; o++) {
      let l = t[o], a = e[o << 1], h = e[(o << 1) + 1], f = mt ? this.stackID(l) + " -> " : "";
      if (l.deadEnd && (r || (r = !0, l.restart(), mt && console.log(f + this.stackID(l) + " (restarted)"), this.advanceFully(l, i))))
        continue;
      let c = l.split(), u = f;
      for (let d = 0; d < 10 && c.forceReduce() && (mt && console.log(u + this.stackID(c) + " (via force-reduce)"), !this.advanceFully(c, i)); d++)
        mt && (u = this.stackID(c) + " -> ");
      for (let d of l.recoverByInsert(a))
        mt && console.log(f + this.stackID(d) + " (via recover-insert)"), this.advanceFully(d, i);
      this.stream.end > l.pos ? (h == l.pos && (h++, a = 0), l.recoverByDelete(a, h), mt && console.log(f + this.stackID(l) + ` (via recover-delete ${this.parser.getName(a)})`), ml(l, i)) : (!s || s.score < c.score) && (s = c);
    }
    return s;
  }
  // Convert the stack's buffer to a syntax tree.
  stackToTree(t) {
    return t.close(), _.build({
      buffer: Ds.create(t),
      nodeSet: this.parser.nodeSet,
      topID: this.topTerm,
      maxBufferLength: this.parser.bufferLength,
      reused: this.reused,
      start: this.ranges[0].from,
      length: t.pos - this.ranges[0].from,
      minRepeatType: this.parser.minRepeatTerm
    });
  }
  stackID(t) {
    let e = (mn || (mn = /* @__PURE__ */ new WeakMap())).get(t);
    return e || mn.set(t, e = String.fromCodePoint(this.nextStackID++)), e + t;
  }
}
function ml(n, t) {
  for (let e = 0; e < t.length; e++) {
    let i = t[e];
    if (i.pos == n.pos && i.sameState(n)) {
      t[e].score < n.score && (t[e] = n);
      return;
    }
  }
  t.push(n);
}
class Ng {
  constructor(t, e, i) {
    this.source = t, this.flags = e, this.disabled = i;
  }
  allows(t) {
    return !this.disabled || this.disabled[t] == 0;
  }
}
class Bs extends Wa {
  /**
  @internal
  */
  constructor(t) {
    if (super(), this.wrappers = [], t.version != 14)
      throw new RangeError(`Parser version (${t.version}) doesn't match runtime version (14)`);
    let e = t.nodeNames.split(" ");
    this.minRepeatTerm = e.length;
    for (let l = 0; l < t.repeatNodeCount; l++)
      e.push("");
    let i = Object.keys(t.topRules).map((l) => t.topRules[l][1]), s = [];
    for (let l = 0; l < e.length; l++)
      s.push([]);
    function r(l, a, h) {
      s[l].push([a, a.deserialize(String(h))]);
    }
    if (t.nodeProps)
      for (let l of t.nodeProps) {
        let a = l[0];
        typeof a == "string" && (a = B[a]);
        for (let h = 1; h < l.length; ) {
          let f = l[h++];
          if (f >= 0)
            r(f, a, l[h++]);
          else {
            let c = l[h + -f];
            for (let u = -f; u > 0; u--)
              r(l[h++], a, c);
            h++;
          }
        }
      }
    this.nodeSet = new Sr(e.map((l, a) => gt.define({
      name: a >= this.minRepeatTerm ? void 0 : l,
      id: a,
      props: s[a],
      top: i.indexOf(a) > -1,
      error: a == 0,
      skipped: t.skippedNodes && t.skippedNodes.indexOf(a) > -1
    }))), t.propSources && (this.nodeSet = this.nodeSet.extend(...t.propSources)), this.strict = !1, this.bufferLength = Ea;
    let o = Yi(t.tokenData);
    this.context = t.context, this.specializerSpecs = t.specialized || [], this.specialized = new Uint16Array(this.specializerSpecs.length);
    for (let l = 0; l < this.specializerSpecs.length; l++)
      this.specialized[l] = this.specializerSpecs[l].term;
    this.specializers = this.specializerSpecs.map(yl), this.states = Yi(t.states, Uint32Array), this.data = Yi(t.stateData), this.goto = Yi(t.goto), this.maxTerm = t.maxTerm, this.tokenizers = t.tokenizers.map((l) => typeof l == "number" ? new He(o, l) : l), this.topRules = t.topRules, this.dialects = t.dialects || {}, this.dynamicPrecedences = t.dynamicPrecedences || null, this.tokenPrecTable = t.tokenPrec, this.termNames = t.termNames || null, this.maxNode = this.nodeSet.types.length - 1, this.dialect = this.parseDialect(), this.top = this.topRules[Object.keys(this.topRules)[0]];
  }
  createParse(t, e, i) {
    let s = new Ig(this, t, e, i);
    for (let r of this.wrappers)
      s = r(s, t, e, i);
    return s;
  }
  /**
  Get a goto table entry @internal
  */
  getGoto(t, e, i = !1) {
    let s = this.goto;
    if (e >= s[0])
      return -1;
    for (let r = s[e + 1]; ; ) {
      let o = s[r++], l = o & 1, a = s[r++];
      if (l && i)
        return a;
      for (let h = r + (o >> 1); r < h; r++)
        if (s[r] == t)
          return a;
      if (l)
        return -1;
    }
  }
  /**
  Check if this state has an action for a given terminal @internal
  */
  hasAction(t, e) {
    let i = this.data;
    for (let s = 0; s < 2; s++)
      for (let r = this.stateSlot(
        t,
        s ? 2 : 1
        /* ParseState.Actions */
      ), o; ; r += 3) {
        if ((o = i[r]) == 65535)
          if (i[r + 1] == 1)
            o = i[r = _t(i, r + 2)];
          else {
            if (i[r + 1] == 2)
              return _t(i, r + 2);
            break;
          }
        if (o == e || o == 0)
          return _t(i, r + 1);
      }
    return 0;
  }
  /**
  @internal
  */
  stateSlot(t, e) {
    return this.states[t * 6 + e];
  }
  /**
  @internal
  */
  stateFlag(t, e) {
    return (this.stateSlot(
      t,
      0
      /* ParseState.Flags */
    ) & e) > 0;
  }
  /**
  @internal
  */
  validAction(t, e) {
    return !!this.allActions(t, (i) => i == e ? !0 : null);
  }
  /**
  @internal
  */
  allActions(t, e) {
    let i = this.stateSlot(
      t,
      4
      /* ParseState.DefaultReduce */
    ), s = i ? e(i) : void 0;
    for (let r = this.stateSlot(
      t,
      1
      /* ParseState.Actions */
    ); s == null; r += 3) {
      if (this.data[r] == 65535)
        if (this.data[r + 1] == 1)
          r = _t(this.data, r + 2);
        else
          break;
      s = e(_t(this.data, r + 1));
    }
    return s;
  }
  /**
  Get the states that can follow this one through shift actions or
  goto jumps. @internal
  */
  nextStates(t) {
    let e = [];
    for (let i = this.stateSlot(
      t,
      1
      /* ParseState.Actions */
    ); ; i += 3) {
      if (this.data[i] == 65535)
        if (this.data[i + 1] == 1)
          i = _t(this.data, i + 2);
        else
          break;
      if ((this.data[i + 2] & 1) == 0) {
        let s = this.data[i + 1];
        e.some((r, o) => o & 1 && r == s) || e.push(this.data[i], s);
      }
    }
    return e;
  }
  /**
  Configure the parser. Returns a new parser instance that has the
  given settings modified. Settings not provided in `config` are
  kept from the original parser.
  */
  configure(t) {
    let e = Object.assign(Object.create(Bs.prototype), this);
    if (t.props && (e.nodeSet = this.nodeSet.extend(...t.props)), t.top) {
      let i = this.topRules[t.top];
      if (!i)
        throw new RangeError(`Invalid top rule name ${t.top}`);
      e.top = i;
    }
    return t.tokenizers && (e.tokenizers = this.tokenizers.map((i) => {
      let s = t.tokenizers.find((r) => r.from == i);
      return s ? s.to : i;
    })), t.specializers && (e.specializers = this.specializers.slice(), e.specializerSpecs = this.specializerSpecs.map((i, s) => {
      let r = t.specializers.find((l) => l.from == i.external);
      if (!r)
        return i;
      let o = Object.assign(Object.assign({}, i), { external: r.to });
      return e.specializers[s] = yl(o), o;
    })), t.contextTracker && (e.context = t.contextTracker), t.dialect && (e.dialect = this.parseDialect(t.dialect)), t.strict != null && (e.strict = t.strict), t.wrap && (e.wrappers = e.wrappers.concat(t.wrap)), t.bufferLength != null && (e.bufferLength = t.bufferLength), e;
  }
  /**
  Tells you whether any [parse wrappers](#lr.ParserConfig.wrap)
  are registered for this parser.
  */
  hasWrappers() {
    return this.wrappers.length > 0;
  }
  /**
  Returns the name associated with a given term. This will only
  work for all terms when the parser was generated with the
  `--names` option. By default, only the names of tagged terms are
  stored.
  */
  getName(t) {
    return this.termNames ? this.termNames[t] : String(t <= this.maxNode && this.nodeSet.types[t].name || t);
  }
  /**
  The eof term id is always allocated directly after the node
  types. @internal
  */
  get eofTerm() {
    return this.maxNode + 1;
  }
  /**
  The type of top node produced by the parser.
  */
  get topNode() {
    return this.nodeSet.types[this.top[1]];
  }
  /**
  @internal
  */
  dynamicPrecedence(t) {
    let e = this.dynamicPrecedences;
    return e == null ? 0 : e[t] || 0;
  }
  /**
  @internal
  */
  parseDialect(t) {
    let e = Object.keys(this.dialects), i = e.map(() => !1);
    if (t)
      for (let r of t.split(" ")) {
        let o = e.indexOf(r);
        o >= 0 && (i[o] = !0);
      }
    let s = null;
    for (let r = 0; r < e.length; r++)
      if (!i[r])
        for (let o = this.dialects[e[r]], l; (l = this.data[o++]) != 65535; )
          (s || (s = new Uint8Array(this.maxTerm + 1)))[l] = 1;
    return new Ng(t, i, s);
  }
  /**
  Used by the output of the parser generator. Not available to
  user code. @hide
  */
  static deserialize(t) {
    return new Bs(t);
  }
}
function _t(n, t) {
  return n[t] | n[t + 1] << 16;
}
function Vg(n) {
  let t = null;
  for (let e of n) {
    let i = e.p.stoppedAt;
    (e.pos == e.p.stream.end || i != null && e.pos > i) && e.p.parser.stateFlag(
      e.state,
      2
      /* StateFlag.Accepting */
    ) && (!t || t.score < e.score) && (t = e);
  }
  return t;
}
function yl(n) {
  if (n.external) {
    let t = n.extend ? 1 : 0;
    return (e, i) => n.external(e, i) << 1 | t;
  }
  return n.get;
}
const Ih = Ha({
  Number: w.number,
  String: w.string,
  Boolean: w.bool,
  Null: w.null,
  Variable: w.variableName,
  ModelConstant: w.typeName,
  ResourceKey: w.special(w.string),
  LanguageTag: w.annotation,
  LineComment: w.lineComment,
  BlockComment: w.blockComment,
  identifier: w.variableName,
  "FunctionCall/identifier": w.function(w.variableName),
  "MethodAccess/identifier": w.function(w.propertyName),
  Arrow: w.punctuation,
  '"(" ")" "[" "]" "{" "}"': w.paren,
  '"." ".."': w.derefOperator,
  "orOp andOp": w.logicOperator,
  compareOp: w.compareOperator,
  "Plus Minus multiplicativeOp": w.arithmeticOperator,
  NotOp: w.logicOperator,
  '"switch" "default" "tuple"': w.keyword,
  '"?" ":"': w.punctuation
}), Wg = { __proto__: null, true: 40, false: 42, null: 46, tuple: 62, switch: 68, default: 74 }, Hg = Bs.deserialize({
  version: 14,
  states: "3OO]QPOOO!aQQO'#DWO!iQPO'#CwO!nQPO'#DSO]QPO'#DVOOQO'#Cl'#ClO$yQSO'#CkO%QQPO'#ChOOQO'#Ch'#ChO&|QSO'#CgO'xQSO'#CfO(VQQO'#CeO(zQPO'#CdO)lQPO'#CcO*ZQPO'#CbOOQO'#Dk'#DkQOQPOOOOQO'#Co'#CoOOQO'#Cr'#CrO*uQPO'#CzO*zQPO'#C}O]QPO,58zO+SQPO'#DXOOQO,59r,59rO+ZQPO'#CxO+`QPO'#DnO+hQPO,59cO+mQWO'#DWOOQO'#DU'#DUO+xQPO'#DTO,QQPO,59nO,VQPO,59qO,[QPO'#DYO,aQPO'#DZO]QPO'#D[OOQO'#Dc'#DcO-mQSO,59VO!dQPO'#DWOOQO,59S,59SO%QQPO'#DdO.lQSO,59RO%QQPO'#DeO/hQSO,59QO/uQPO,59PO%QQPO'#DfO0yQPO,59OO%QQPO'#DgO1kQPO,58}O]QPO,58|O2YQPO,59fO2_QPO,59iO]QPO,59iOOQO1G.f1G.fO2iQPO'#DxOOQO,59s,59sO2qQPO,59sOOQO,59d,59dO!iQPO'#D^O2vQPO,5:YOOQO1G.}1G.}O]QPO,59pO3OQPO,59oO3VQPO,59oOOQO1G/Y1G/YOOQO1G/]1G/]OOQO,59t,59tOOQO,59u,59uO3_QPO,59vOOQO-E7a-E7aOOQO,5:O,5:OOOQO-E7b-E7bOOQO,5:P,5:POOQO-E7c-E7cO#rQPO'#CkO3dQPO'#CgO3kQPO'#CfOOQO1G.k1G.kOOQO,5:Q,5:QOOQO-E7d-E7dOOQO,5:R,5:ROOQO-E7e-E7eO3uQPO1G.hO3zQQO'#C|O4PQPO'#DrO4XQPO1G/QO4^QPO'#DPOOQO'#D`'#D`O4cQPO1G/TO4mQPO'#DQOOQO1G/T1G/TO4rQPO1G/TO4wQPO1G/TO]QPO'#DbO4|QPO,5:dOOQO1G/_1G/_OOQO,59x,59xOOQO-E7[-E7[OOQO1G/[1G/[OOQO,59{,59{O5UQPO1G/ZOOQO-E7_-E7_OOQO1G/b1G/bO,fQPO,59VO5]QPO,59RO5dQPO,59QO]QPO7+$SO]QPO,59hO2YQPO'#D_O5nQPO,5:^OOQO7+$l7+$lO]QPO,59kOOQO-E7^-E7^OOQO7+$o7+$oO5vQPO7+$oO]QPO,59lO5{QPO7+$oOOQO,59|,59|OOQO-E7`-E7`P!nQPO'#DaOOQO<<Gn<<GnOOQO1G/S1G/SOOQO,59y,59yOOQO-E7]-E7]O6QQPO1G/VOOQO<<HZ<<HZO6VQPO1G/WO6_QPO<<HZOOQO7+$q7+$qOOQO7+$r7+$rO6iQPOAN=uOOQOAN=uAN=uO6sQPOAN=uOOQOG23aG23aO6xQPOG23aOOQOLD({LD({O/uQPO'#ChO/uQPO'#DdO/uQPO'#De",
  stateData: "7X~O!^OSPOSQOS~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`PO!aQO!eSO!gRO~OTeO!efO~ObhO~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`kO!aQO!eSO!gRO~O!efO!mpO!nqO!orO^_X!P_X![_X!q_X!s_X!t_X!u_X!i_X!j_X!d_X!c_X!p_X!h_X~O!r_X~P#rO]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O^ZX!PZX![ZX!sZX!tZX!uZX!iZX!jZX!dZX!cZX!pZX!hZX~O!qwO!rZX~P&UO![YX!sYX!tYX!uYX!iYX!jYX!dYX!cYX!pYX!hYX~O^yO!PyO!rYX~P'WO!r{O![XX!sXX!tXX!uXX!iXX!jXX!dXX!cXX!pXX!hXX~O!s|O![WX!tWX!uWX!iWX!jWX!dWX!cWX!pWX!hWX~O!t!OO![VX!uVX!iVX!jVX!dVX!cVX!pVX!hVX~O!u!QO![UX!iUX!jUX!dUX!cUX!pUX!hUX~O!e!RO~O!e!TO!g!SO~O!d!WO~P]Om!YO~O!c!ZO!d!bX~O!d!]O~OTeO!efO!k!^O~O!i!_O!jwX~O!j!aO~O!d!bO~O!`!cO~O!`!dO~O!efO!mpO!nqO!orO^_a!P_a![_a!q_a!s_a!t_a!u_a!i_a!j_a!d_a!c_a!p_a!h_a~O!r_a~P,fO^Za!PZa![Za!sZa!tZa!uZa!iZa!jZa!dZa!cZa!pZa!hZa~O!qwO!rZa~P-tO![Ya!sYa!tYa!uYa!iYa!jYa!dYa!cYa!pYa!hYa~O^yO!PyO!rYa~P.vO]#{O^#{OaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O!s|O![Wa!tWa!uWa!iWa!jWa!dWa!cWa!pWa!hWa~O!t!OO![Va!uVa!iVa!jVa!dVa!cVa!pVa!hVa~O!`!tO~Ou!zO!j!{O~P]O!c#OO!d!lX~O!d#QO~O!c!ZO!d!ba~O!jwa~P!nO!i#VO!jwa~O!p#XO~O!q#|O~P&UO^#}O!P#}O~P'WO!h#]O~OT#^O~O!c#_O!d!fX~O!d#aO~O!h#bO~Ou!zO!j#dO~P]O!h#fO~O!j#dO~O!d#gO~O!c#OO!d!la~O!jwi~P!nO!q#|O~P-tO^#}O!P#}O~P.vO!c#_O!d!fa~O!j#pO~O!g#rO~O!i#sO~O!i#tO!jti~Ou!zO!j#vO~P]Ou!zO!j#xO~P]O!j#xO~O!j#zO~OQP!qaT!`!t!sT~",
  goto: "-V!mPPPP!nP!n#V#n$Y$u%c&TPP&{'mPP(aPP(aPPPP(a)RP(aP)X(aP)_)eP(a)q)t(a(a)|*R*R*RP*X*_*e*o*u*{+V+a+k+qPPP+wPP,|PPP-PPPPPP-S{_ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u{^ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uz]ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!q!O|[ORSefr!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!o|!OZORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!n{!OYORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uS!iy#}R!m{!QXORSefry|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uSvV#{S!gw#|T!l{#}!_WORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}!UUORSVefrwy|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uX!k{#{#|#}!_TORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}QiQR#R!ZQ!u!RR#m#_X!x!S!y#r#uQ!|!SQ#e!yQ#w#rR#y#uRnRQmRV#U!_#V#jUgPkuXsUt!k#YQ![iR#S![Q#`!uR#n#`Q!y!SS#c!y#uR#u#rQ!`mR#W!`Q#P!VR#i#PQtUS!ft#YR#Y!kQxXS!hx#ZR#Z!lQzYS!jz#[R#[!mQ}[R!p}Q!P]R!r!PQ`OWlR!_#V#jQoSQ!UeQ!VfQ!erQ!s!QW!w!S!y#r#uQ!}!TQ#T!^Q#h#OQ#k#]Q#l#^Q#o#bR#q#fRjQR!v!RR!Xf",
  nodeNames: "⚠ LineComment BlockComment Script Lambda Arrow TernaryExpr OrExpr AndExpr CompareExpr AdditiveExpr MultiplicativeExpr UnaryExpr NotOp Minus AccessExpr AtomicExpr Number String Boolean true false Null null Variable ModelConstant ResourceKey I18NLiteral I18NEntry LanguageTag TupleExpr tuple TupleEntry SwitchExpr switch SwitchCase SwitchDefault default Block BlockContent Statement ParenExpr FunctionCall CallArgs MethodAccess DescendAccess ArrayAccess Plus",
  maxTerm: 83,
  propSources: [Ih],
  skippedNodes: [0, 1, 2],
  repeatNodeCount: 10,
  tokenData: ":S~RxXY#oYZ#o]^#opq#oqr$Qrs$_st(ptu,Wuv,uvw,zwx-Vxy.nyz.sz{,u{|.x|}.}}!O/S!O!P/a!P!Q/n!Q!R1c!R![2q![!]3S!]!^3X!^!_3^!_!`3f!`!a3^!a!b3n!b!c3s!c!}4[!}#O4m#P#Q4r#R#S4[#S#T4w#T#U7^#U#c4[#c#d8q#d#o4[#o#p9m#p#q9r#q#r9}~#tS!^~XY#oYZ#o]^#opq#oV$VP]P!_!`$YU$_O!rU~$bVOr$wrs&es#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~$zVOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~%fOb~~%iRO;'S$w;'S;=`%r;=`O$w~%uWOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_;=`<%l$w<%lO$w~&bP;=`<%l$w~&jPb~rs&m~&pTOr&mrs'Ps;'S&m;'S;=`(Z;=`O&m~'STOr&mrs'cs;'S&m;'S;=`(Z;=`O&m~'fTOr&mrs'us;'S&m;'S;=`(Z;=`O&m~'zTb~Or&mrs'us;'S&m;'S;=`(Z;=`O&m~(^UOr&mrs'Ps;'S&m;'S;=`(Z;=`<%l&m<%lO&m~(sRrs(|wx*jxy,R~)PVOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d<%lO(|~)kOj~~)nRO;'S(|;'S;=`)w;=`O(|~)zWOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d;=`<%l(|<%lO(|~*gP;=`<%l(|~*mVOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{<%lO*j~+VRO;'S*j;'S;=`+`;=`O*j~+cWOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{;=`<%l*j<%lO*j~,OP;=`<%l*j~,WO!a~~,ZR!c!},d#R#S,d#T#o,d~,iSh~!Q![,d!c!},d#R#S,d#T#o,d~,zO!q~~,}Pvw-Q~-VO!s~~-YVOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h<%lO-V~-rRO;'S-V;'S;=`-{;=`O-V~.OWOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h;=`<%l-V<%lO-V~.kP;=`<%l-V~.sO!e~~.xO!d~~.}O!P~~/SO!c~_/XP^T!`!a/[Y/aOTY~/fP!m~!O!P/i~/nO!n~~/sQ!q~z{/y!P!Q0z~/|TOz/yz{0]{;'S/y;'S;=`0t<%lO/y~0`TO!P/y!P!Q0o!Q;'S/y;'S;=`0t<%lO/y~0tOQ~~0wP;=`<%l/y~1PSP~OY0zZ;'S0z;'S;=`1]<%lO0z~1`P;=`<%l0z~1hRa~!O!P1q!g!h2V#X#Y2V~1tP!Q![1w~1|Ra~!Q![1w!g!h2V#X#Y2V~2YR{|2c}!O2c!Q![2i~2fP!Q![2i~2nPa~!Q![2i~2vSa~!O!P1q!Q![2q!g!h2V#X#Y2V~3XO!h~~3^O!i~U3cP!rU!_!`$Y^3kP!kW!_!`$Y~3sO!u~~3vQ!c!}3|#T#o3|~4RRm~}!O3|!c!}3|#T#o3|~4aS!`~!Q![4[!c!}4[#R#S4[#T#o4[~4rO!o~~4wO!p~~4zR!c!}5T#R#S5T#T#o5T~5WWst5p!O!P5T!Q![5T![!]6h!c!}5T#R#S5T#S#T6c#T#o5T~5sR!c!}5|#R#S5|#T#o5|~6PU!O!P5|!Q![5|!c!}5|#R#S5|#S#T6c#T#o5|~6hOi~~6kR!c!}6t#R#S6t#T#o6t~6wVst5p!O!P6t!Q![6t!c!}6t#R#S6t#S#T6c#T#o6t~7cU!`~!Q![4[!c!}4[#R#S4[#T#b4[#b#c7u#c#o4[~7zU!`~!Q![4[!c!}4[#R#S4[#T#W4[#W#X8^#X#o4[~8eS!`~!s~!Q![4[!c!}4[#R#S4[#T#o4[~8vU!`~!Q![4[!c!}4[#R#S4[#T#f4[#f#g9Y#g#o4[~9aS!`~!t~!Q![4[!c!}4[#R#S4[#T#o4[~9rO!g~~9uP#p#q9x~9}O!t~~:SO!j~",
  tokenizers: [0, 1, 2, 3],
  topRules: { Script: [0, 3] },
  specialized: [{ term: 62, get: (n) => Wg[n] || -1 }],
  tokenPrec: 1011
}), Fg = ws.define({
  parser: Hg.configure({
    props: [Ih]
  }),
  languageData: {
    commentTokens: { line: "//", block: { open: "/*", close: "*/" } },
    closeBrackets: { brackets: ["(", "[", "{", "'", '"', "`"] }
  }
});
function zg() {
  return new $u(Fg);
}
const { useRef: ei, useEffect: Zi, useCallback: bl } = xl, Qg = 300, $g = 3e3, jg = ({ controlId: n, state: t }) => {
  const e = Nh(), i = Vh(), s = ei(null), r = ei(null), o = ei(new ki()), l = ei(null), a = ei(null), h = t.value ?? "", f = t.readOnly === !0, c = bl(
    (g) => {
      const m = g.pos, y = g.state.doc.lineAt(m);
      if (!g.matchBefore(/[\w$`.:]+/) && !g.explicit) return Promise.resolve(null);
      const x = g.matchBefore(/[\w]+/), O = (x == null ? void 0 : x.text) ?? "", S = (x == null ? void 0 : x.from) ?? m, T = y.text.substring(0, m - y.from), C = String(Date.now()) + Math.random();
      return i("complete", { line: T, prefix: O, requestId: C }), new Promise((R) => {
        a.current = { requestId: C, resolve: R, from: S }, setTimeout(() => {
          var W;
          ((W = a.current) == null ? void 0 : W.requestId) === C && (a.current = null, R(null));
        }, $g);
      });
    },
    [i]
  );
  Zi(() => {
    if (!s.current) return;
    const g = new P({
      state: V.create({
        doc: h,
        extensions: [
          zg(),
          td(sd),
          wu(),
          Jc(),
          Su(),
          fd(),
          ng(),
          Ad(),
          wr.of([...Dp, ...Id]),
          o.current.of(P.editable.of(!f)),
          pg({ override: [c] }),
          P.updateListener.of((m) => {
            m.docChanged && (l.current && clearTimeout(l.current), l.current = setTimeout(() => {
              const y = m.state.doc.toString();
              i("valueChanged", { value: y }), i("validate", { text: y });
            }, Qg));
          })
        ]
      }),
      parent: s.current
    });
    return r.current = g, () => {
      l.current && clearTimeout(l.current), g.destroy(), r.current = null;
    };
  }, []), Zi(() => {
    const g = r.current;
    g && g.dispatch({
      effects: o.current.reconfigure(P.editable.of(!f))
    });
  }, [f]);
  const u = e.diagnostics ?? [];
  Zi(() => {
    const g = r.current;
    if (!g) return;
    const m = g.state.doc, y = u.map((b) => {
      const x = Math.max(1, Math.min(b.line, m.lines)), O = m.line(x), S = O.from + Math.max(0, Math.min(b.col - 1, O.length)), T = b.endCol != null && b.endLine != null ? m.line(Math.max(1, Math.min(b.endLine, m.lines))).from + Math.max(0, Math.min(b.endCol - 1, O.length)) : Math.min(S + 1, O.to);
      return { from: S, to: T, severity: b.severity, message: b.message };
    }).filter((b) => b.from <= m.length && b.to <= m.length);
    g.dispatch(xg(g.state, y));
  }, [u]);
  const d = e.completionResponse;
  Zi(() => {
    const g = a.current;
    if (!g || !d || d.requestId !== g.requestId)
      return;
    const m = d.completions ?? [];
    if (a.current = null, m.length === 0) {
      g.resolve(null);
      return;
    }
    g.resolve({
      from: g.from,
      options: m.map((y) => ({
        label: y.name,
        apply: y.replacement ?? y.name,
        detail: y.value !== y.name ? y.value : void 0,
        info: y.docHTML ? () => {
          const b = document.createElement("div");
          return b.innerHTML = y.docHTML, b;
        } : void 0,
        boost: y.score ?? 0
      }))
    });
  }, [d]);
  const p = bl((g) => {
    const m = r.current;
    m && !m.dom.contains(g.target) && m.focus();
  }, []);
  return /* @__PURE__ */ xl.createElement("div", { ref: s, id: n, className: "tlScriptEditor", onClick: p });
};
Wh("TLScriptEditor", jg);
