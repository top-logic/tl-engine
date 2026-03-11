import { React as Go, useTLState as oh, useTLCommand as lh, register as ah } from "tl-react-bridge";
let os = [], Zo = [];
(() => {
  let s = "lc,34,7n,7,7b,19,,,,2,,2,,,20,b,1c,l,g,,2t,7,2,6,2,2,,4,z,,u,r,2j,b,1m,9,9,,o,4,,9,,3,,5,17,3,3b,f,,w,1j,,,,4,8,4,,3,7,a,2,t,,1m,,,,2,4,8,,9,,a,2,q,,2,2,1l,,4,2,4,2,2,3,3,,u,2,3,,b,2,1l,,4,5,,2,4,,k,2,m,6,,,1m,,,2,,4,8,,7,3,a,2,u,,1n,,,,c,,9,,14,,3,,1l,3,5,3,,4,7,2,b,2,t,,1m,,2,,2,,3,,5,2,7,2,b,2,s,2,1l,2,,,2,4,8,,9,,a,2,t,,20,,4,,2,3,,,8,,29,,2,7,c,8,2q,,2,9,b,6,22,2,r,,,,,,1j,e,,5,,2,5,b,,10,9,,2u,4,,6,,2,2,2,p,2,4,3,g,4,d,,2,2,6,,f,,jj,3,qa,3,t,3,t,2,u,2,1s,2,,7,8,,2,b,9,,19,3,3b,2,y,,3a,3,4,2,9,,6,3,63,2,2,,1m,,,7,,,,,2,8,6,a,2,,1c,h,1r,4,1c,7,,,5,,14,9,c,2,w,4,2,2,,3,1k,,,2,3,,,3,1m,8,2,2,48,3,,d,,7,4,,6,,3,2,5i,1m,,5,ek,,5f,x,2da,3,3x,,2o,w,fe,6,2x,2,n9w,4,,a,w,2,28,2,7k,,3,,4,,p,2,5,,47,2,q,i,d,,12,8,p,b,1a,3,1c,,2,4,2,2,13,,1v,6,2,2,2,2,c,,8,,1b,,1f,,,3,2,2,5,2,,,16,2,8,,6m,,2,,4,,fn4,,kh,g,g,g,a6,2,gt,,6a,,45,5,1ae,3,,2,5,4,14,3,4,,4l,2,fx,4,ar,2,49,b,4w,,1i,f,1k,3,1d,4,2,2,1x,3,10,5,,8,1q,,c,2,1g,9,a,4,2,,2n,3,2,,,2,6,,4g,,3,8,l,2,1l,2,,,,,m,,e,7,3,5,5f,8,2,3,,,n,,29,,2,6,,,2,,,2,,2,6j,,2,4,6,2,,2,r,2,2d,8,2,,,2,2y,,,,2,6,,,2t,3,2,4,,5,77,9,,2,6t,,a,2,,,4,,40,4,2,2,4,,w,a,14,6,2,4,8,,9,6,2,3,1a,d,,2,ba,7,,6,,,2a,m,2,7,,2,,2,3e,6,3,,,2,,7,,,20,2,3,,,,9n,2,f0b,5,1n,7,t4,,1r,4,29,,f5k,2,43q,,,3,4,5,8,8,2,7,u,4,44,3,1iz,1j,4,1e,8,,e,,m,5,,f,11s,7,,h,2,7,,2,,5,79,7,c5,4,15s,7,31,7,240,5,gx7k,2o,3k,6o".split(",").map((t) => t ? parseInt(t, 36) : 1);
  for (let t = 0, e = 0; t < s.length; t++)
    (t % 2 ? Zo : os).push(e = e + s[t]);
})();
function hh(s) {
  if (s < 768) return !1;
  for (let t = 0, e = os.length; ; ) {
    let i = t + e >> 1;
    if (s < os[i]) e = i;
    else if (s >= Zo[i]) t = i + 1;
    else return !0;
    if (t == e) return !1;
  }
}
function Ar(s) {
  return s >= 127462 && s <= 127487;
}
const Tr = 8205;
function fh(s, t, e = !0, i = !0) {
  return (e ? Jo : ch)(s, t, i);
}
function Jo(s, t, e) {
  if (t == s.length) return t;
  t && tl(s.charCodeAt(t)) && el(s.charCodeAt(t - 1)) && t--;
  let i = Hn(s, t);
  for (t += Pr(i); t < s.length; ) {
    let n = Hn(s, t);
    if (i == Tr || n == Tr || e && hh(n))
      t += Pr(n), i = n;
    else if (Ar(n)) {
      let r = 0, o = t - 2;
      for (; o >= 0 && Ar(Hn(s, o)); )
        r++, o -= 2;
      if (r % 2 == 0) break;
      t += 2;
    } else
      break;
  }
  return t;
}
function ch(s, t, e) {
  for (; t > 0; ) {
    let i = Jo(s, t - 2, e);
    if (i < t) return i;
    t--;
  }
  return 0;
}
function Hn(s, t) {
  let e = s.charCodeAt(t);
  if (!el(e) || t + 1 == s.length) return e;
  let i = s.charCodeAt(t + 1);
  return tl(i) ? (e - 55296 << 10) + (i - 56320) + 65536 : e;
}
function tl(s) {
  return s >= 56320 && s < 57344;
}
function el(s) {
  return s >= 55296 && s < 56320;
}
function Pr(s) {
  return s < 65536 ? 1 : 2;
}
class E {
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
    [t, e] = Ie(this, t, e);
    let n = [];
    return this.decompose(
      0,
      t,
      n,
      2
      /* Open.To */
    ), i.length && i.decompose(
      0,
      i.length,
      n,
      3
      /* Open.To */
    ), this.decompose(
      e,
      this.length,
      n,
      1
      /* Open.From */
    ), Nt.from(n, this.length - (e - t) + i.length);
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
    [t, e] = Ie(this, t, e);
    let i = [];
    return this.decompose(t, e, i, 0), Nt.from(i, e - t);
  }
  /**
  Test whether this text is equal to another instance.
  */
  eq(t) {
    if (t == this)
      return !0;
    if (t.length != this.length || t.lines != this.lines)
      return !1;
    let e = this.scanIdentical(t, 1), i = this.length - this.scanIdentical(t, -1), n = new ei(this), r = new ei(t);
    for (let o = e, l = e; ; ) {
      if (n.next(o), r.next(o), o = 0, n.lineBreak != r.lineBreak || n.done != r.done || n.value != r.value)
        return !1;
      if (l += n.value.length, n.done || l >= i)
        return !0;
    }
  }
  /**
  Iterate over the text. When `dir` is `-1`, iteration happens
  from end to start. This will return lines and the breaks between
  them as separate strings.
  */
  iter(t = 1) {
    return new ei(this, t);
  }
  /**
  Iterate over a range of the text. When `from` > `to`, the
  iterator will run in reverse.
  */
  iterRange(t, e = this.length) {
    return new il(this, t, e);
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
      let n = this.line(t).from;
      i = this.iterRange(n, Math.max(n, e == this.lines + 1 ? this.length : e <= 1 ? 0 : this.line(e - 1).to));
    }
    return new nl(i);
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
    return t.length == 1 && !t[0] ? E.empty : t.length <= 32 ? new K(t) : Nt.from(K.split(t, []));
  }
}
class K extends E {
  constructor(t, e = uh(t)) {
    super(), this.text = t, this.length = e;
  }
  get lines() {
    return this.text.length;
  }
  get children() {
    return null;
  }
  lineInner(t, e, i, n) {
    for (let r = 0; ; r++) {
      let o = this.text[r], l = n + o.length;
      if ((e ? i : l) >= t)
        return new dh(n, l, i, o);
      n = l + 1, i++;
    }
  }
  decompose(t, e, i, n) {
    let r = t <= 0 && e >= this.length ? this : new K(Mr(this.text, t, e), Math.min(e, this.length) - Math.max(0, t));
    if (n & 1) {
      let o = i.pop(), l = Ki(r.text, o.text.slice(), 0, r.length);
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
    [t, e] = Ie(this, t, e);
    let n = Ki(this.text, Ki(i.text, Mr(this.text, 0, t)), e), r = this.length + i.length - (e - t);
    return n.length <= 32 ? new K(n, r) : Nt.from(K.split(n, []), r);
  }
  sliceString(t, e = this.length, i = `
`) {
    [t, e] = Ie(this, t, e);
    let n = "";
    for (let r = 0, o = 0; r <= e && o < this.text.length; o++) {
      let l = this.text[o], a = r + l.length;
      r > t && o && (n += i), t < a && e > r && (n += l.slice(Math.max(0, t - r), e - r)), r = a + 1;
    }
    return n;
  }
  flatten(t) {
    for (let e of this.text)
      t.push(e);
  }
  scanIdentical() {
    return 0;
  }
  static split(t, e) {
    let i = [], n = -1;
    for (let r of t)
      i.push(r), n += r.length + 1, i.length == 32 && (e.push(new K(i, n)), i = [], n = -1);
    return n > -1 && e.push(new K(i, n)), e;
  }
}
class Nt extends E {
  constructor(t, e) {
    super(), this.children = t, this.length = e, this.lines = 0;
    for (let i of t)
      this.lines += i.lines;
  }
  lineInner(t, e, i, n) {
    for (let r = 0; ; r++) {
      let o = this.children[r], l = n + o.length, a = i + o.lines - 1;
      if ((e ? a : l) >= t)
        return o.lineInner(t, e, i, n);
      n = l + 1, i = a + 1;
    }
  }
  decompose(t, e, i, n) {
    for (let r = 0, o = 0; o <= e && r < this.children.length; r++) {
      let l = this.children[r], a = o + l.length;
      if (t <= a && e >= o) {
        let h = n & ((o <= t ? 1 : 0) | (a >= e ? 2 : 0));
        o >= t && a <= e && !h ? i.push(l) : l.decompose(t - o, e - o, i, h);
      }
      o = a + 1;
    }
  }
  replace(t, e, i) {
    if ([t, e] = Ie(this, t, e), i.lines < this.lines)
      for (let n = 0, r = 0; n < this.children.length; n++) {
        let o = this.children[n], l = r + o.length;
        if (t >= r && e <= l) {
          let a = o.replace(t - r, e - r, i), h = this.lines - o.lines + a.lines;
          if (a.lines < h >> 4 && a.lines > h >> 6) {
            let f = this.children.slice();
            return f[n] = a, new Nt(f, this.length - (e - t) + i.length);
          }
          return super.replace(r, l, a);
        }
        r = l + 1;
      }
    return super.replace(t, e, i);
  }
  sliceString(t, e = this.length, i = `
`) {
    [t, e] = Ie(this, t, e);
    let n = "";
    for (let r = 0, o = 0; r < this.children.length && o <= e; r++) {
      let l = this.children[r], a = o + l.length;
      o > t && r && (n += i), t < a && e > o && (n += l.sliceString(t - o, e - o, i)), o = a + 1;
    }
    return n;
  }
  flatten(t) {
    for (let e of this.children)
      e.flatten(t);
  }
  scanIdentical(t, e) {
    if (!(t instanceof Nt))
      return 0;
    let i = 0, [n, r, o, l] = e > 0 ? [0, 0, this.children.length, t.children.length] : [this.children.length - 1, t.children.length - 1, -1, -1];
    for (; ; n += e, r += e) {
      if (n == o || r == l)
        return i;
      let a = this.children[n], h = t.children[r];
      if (a != h)
        return i + a.scanIdentical(h, e);
      i += a.length + 1;
    }
  }
  static from(t, e = t.reduce((i, n) => i + n.length + 1, -1)) {
    let i = 0;
    for (let d of t)
      i += d.lines;
    if (i < 32) {
      let d = [];
      for (let p of t)
        p.flatten(d);
      return new K(d, e);
    }
    let n = Math.max(
      32,
      i >> 5
      /* Tree.BranchShift */
    ), r = n << 1, o = n >> 1, l = [], a = 0, h = -1, f = [];
    function c(d) {
      let p;
      if (d.lines > r && d instanceof Nt)
        for (let g of d.children)
          c(g);
      else d.lines > o && (a > o || !a) ? (u(), l.push(d)) : d instanceof K && a && (p = f[f.length - 1]) instanceof K && d.lines + p.lines <= 32 ? (a += d.lines, h += d.length + 1, f[f.length - 1] = new K(p.text.concat(d.text), p.length + 1 + d.length)) : (a + d.lines > n && u(), a += d.lines, h += d.length + 1, f.push(d));
    }
    function u() {
      a != 0 && (l.push(f.length == 1 ? f[0] : Nt.from(f, h)), h = -1, a = f.length = 0);
    }
    for (let d of t)
      c(d);
    return u(), l.length == 1 ? l[0] : new Nt(l, e);
  }
}
E.empty = /* @__PURE__ */ new K([""], 0);
function uh(s) {
  let t = -1;
  for (let e of s)
    t += e.length + 1;
  return t;
}
function Ki(s, t, e = 0, i = 1e9) {
  for (let n = 0, r = 0, o = !0; r < s.length && n <= i; r++) {
    let l = s[r], a = n + l.length;
    a >= e && (a > i && (l = l.slice(0, i - n)), n < e && (l = l.slice(e - n)), o ? (t[t.length - 1] += l, o = !1) : t.push(l)), n = a + 1;
  }
  return t;
}
function Mr(s, t, e) {
  return Ki(s, [""], t, e);
}
class ei {
  constructor(t, e = 1) {
    this.dir = e, this.done = !1, this.lineBreak = !1, this.value = "", this.nodes = [t], this.offsets = [e > 0 ? 1 : (t instanceof K ? t.text.length : t.children.length) << 1];
  }
  nextInner(t, e) {
    for (this.done = this.lineBreak = !1; ; ) {
      let i = this.nodes.length - 1, n = this.nodes[i], r = this.offsets[i], o = r >> 1, l = n instanceof K ? n.text.length : n.children.length;
      if (o == (e > 0 ? l : 0)) {
        if (i == 0)
          return this.done = !0, this.value = "", this;
        e > 0 && this.offsets[i - 1]++, this.nodes.pop(), this.offsets.pop();
      } else if ((r & 1) == (e > 0 ? 0 : 1)) {
        if (this.offsets[i] += e, t == 0)
          return this.lineBreak = !0, this.value = `
`, this;
        t--;
      } else if (n instanceof K) {
        let a = n.text[o + (e < 0 ? -1 : 0)];
        if (this.offsets[i] += e, a.length > Math.max(0, t))
          return this.value = t == 0 ? a : e > 0 ? a.slice(t) : a.slice(0, a.length - t), this;
        t -= a.length;
      } else {
        let a = n.children[o + (e < 0 ? -1 : 0)];
        t > a.length ? (t -= a.length, this.offsets[i] += e) : (e < 0 && this.offsets[i]--, this.nodes.push(a), this.offsets.push(e > 0 ? 1 : (a instanceof K ? a.text.length : a.children.length) << 1));
      }
    }
  }
  next(t = 0) {
    return t < 0 && (this.nextInner(-t, -this.dir), t = this.value.length), this.nextInner(t, this.dir);
  }
}
class il {
  constructor(t, e, i) {
    this.value = "", this.done = !1, this.cursor = new ei(t, e > i ? -1 : 1), this.pos = e > i ? t.length : 0, this.from = Math.min(e, i), this.to = Math.max(e, i);
  }
  nextInner(t, e) {
    if (e < 0 ? this.pos <= this.from : this.pos >= this.to)
      return this.value = "", this.done = !0, this;
    t += Math.max(0, e < 0 ? this.pos - this.to : this.from - this.pos);
    let i = e < 0 ? this.pos - this.from : this.to - this.pos;
    t > i && (t = i), i -= t;
    let { value: n } = this.cursor.next(t);
    return this.pos += (n.length + t) * e, this.value = n.length <= i ? n : e < 0 ? n.slice(n.length - i) : n.slice(0, i), this.done = !this.value, this;
  }
  next(t = 0) {
    return t < 0 ? t = Math.max(t, this.from - this.pos) : t > 0 && (t = Math.min(t, this.to - this.pos)), this.nextInner(t, this.cursor.dir);
  }
  get lineBreak() {
    return this.cursor.lineBreak && this.value != "";
  }
}
class nl {
  constructor(t) {
    this.inner = t, this.afterBreak = !0, this.value = "", this.done = !1;
  }
  next(t = 0) {
    let { done: e, lineBreak: i, value: n } = this.inner.next(t);
    return e && this.afterBreak ? (this.value = "", this.afterBreak = !1) : e ? (this.done = !0, this.value = "") : i ? this.afterBreak ? this.value = "" : (this.afterBreak = !0, this.next()) : (this.value = n, this.afterBreak = !1), this;
  }
  get lineBreak() {
    return !1;
  }
}
typeof Symbol < "u" && (E.prototype[Symbol.iterator] = function() {
  return this.iter();
}, ei.prototype[Symbol.iterator] = il.prototype[Symbol.iterator] = nl.prototype[Symbol.iterator] = function() {
  return this;
});
class dh {
  /**
  @internal
  */
  constructor(t, e, i, n) {
    this.from = t, this.to = e, this.number = i, this.text = n;
  }
  /**
  The length of the line (not including any line break after it).
  */
  get length() {
    return this.to - this.from;
  }
}
function Ie(s, t, e) {
  return t = Math.max(0, Math.min(s.length, t)), [t, Math.max(t, Math.min(s.length, e))];
}
function nt(s, t, e = !0, i = !0) {
  return fh(s, t, e, i);
}
function ph(s) {
  return s >= 56320 && s < 57344;
}
function gh(s) {
  return s >= 55296 && s < 56320;
}
function ue(s, t) {
  let e = s.charCodeAt(t);
  if (!gh(e) || t + 1 == s.length)
    return e;
  let i = s.charCodeAt(t + 1);
  return ph(i) ? (e - 55296 << 10) + (i - 56320) + 65536 : e;
}
function mh(s) {
  return s <= 65535 ? String.fromCharCode(s) : (s -= 65536, String.fromCharCode((s >> 10) + 55296, (s & 1023) + 56320));
}
function Oe(s) {
  return s < 65536 ? 1 : 2;
}
const ls = /\r\n?|\n/;
var at = /* @__PURE__ */ (function(s) {
  return s[s.Simple = 0] = "Simple", s[s.TrackDel = 1] = "TrackDel", s[s.TrackBefore = 2] = "TrackBefore", s[s.TrackAfter = 3] = "TrackAfter", s;
})(at || (at = {}));
class Qt {
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
    for (let e = 0, i = 0, n = 0; e < this.sections.length; ) {
      let r = this.sections[e++], o = this.sections[e++];
      o < 0 ? (t(i, n, r), n += r) : n += o, i += r;
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
    as(this, t, e);
  }
  /**
  Get a description of the inverted form of these changes.
  */
  get invertedDesc() {
    let t = [];
    for (let e = 0; e < this.sections.length; ) {
      let i = this.sections[e++], n = this.sections[e++];
      n < 0 ? t.push(i, n) : t.push(n, i);
    }
    return new Qt(t);
  }
  /**
  Compute the combined effect of applying another set of changes
  after this one. The length of the document after this set should
  match the length before `other`.
  */
  composeDesc(t) {
    return this.empty ? t : t.empty ? this : sl(this, t);
  }
  /**
  Map this description, which should start with the same document
  as `other`, over another set of changes, so that it can be
  applied after it. When `before` is true, map as if the changes
  in `this` happened before the ones in `other`.
  */
  mapDesc(t, e = !1) {
    return t.empty ? this : hs(this, t, e);
  }
  mapPos(t, e = -1, i = at.Simple) {
    let n = 0, r = 0;
    for (let o = 0; o < this.sections.length; ) {
      let l = this.sections[o++], a = this.sections[o++], h = n + l;
      if (a < 0) {
        if (h > t)
          return r + (t - n);
        r += l;
      } else {
        if (i != at.Simple && h >= t && (i == at.TrackDel && n < t && h > t || i == at.TrackBefore && n < t || i == at.TrackAfter && h > t))
          return null;
        if (h > t || h == t && e < 0 && !l)
          return t == n || e < 0 ? r : r + a;
        r += a;
      }
      n = h;
    }
    if (t > n)
      throw new RangeError(`Position ${t} is out of range for changeset of length ${n}`);
    return r;
  }
  /**
  Check whether these changes touch a given range. When one of the
  changes entirely covers the range, the string `"cover"` is
  returned.
  */
  touchesRange(t, e = t) {
    for (let i = 0, n = 0; i < this.sections.length && n <= e; ) {
      let r = this.sections[i++], o = this.sections[i++], l = n + r;
      if (o >= 0 && n <= e && l >= t)
        return n < t && l > e ? "cover" : !0;
      n = l;
    }
    return !1;
  }
  /**
  @internal
  */
  toString() {
    let t = "";
    for (let e = 0; e < this.sections.length; ) {
      let i = this.sections[e++], n = this.sections[e++];
      t += (t ? " " : "") + i + (n >= 0 ? ":" + n : "");
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
    return new Qt(t);
  }
  /**
  @internal
  */
  static create(t) {
    return new Qt(t);
  }
}
class Y extends Qt {
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
    return as(this, (e, i, n, r, o) => t = t.replace(n, n + (i - e), o), !1), t;
  }
  mapDesc(t, e = !1) {
    return hs(this, t, e, !0);
  }
  /**
  Given the document as it existed _before_ the changes, return a
  change set that represents the inverse of this set, which could
  be used to go from the document created by the changes back to
  the document as it existed before the changes.
  */
  invert(t) {
    let e = this.sections.slice(), i = [];
    for (let n = 0, r = 0; n < e.length; n += 2) {
      let o = e[n], l = e[n + 1];
      if (l >= 0) {
        e[n] = l, e[n + 1] = o;
        let a = n >> 1;
        for (; i.length < a; )
          i.push(E.empty);
        i.push(o ? t.slice(r, r + o) : E.empty);
      }
      r += o;
    }
    return new Y(e, i);
  }
  /**
  Combine two subsequent change sets into a single set. `other`
  must start in the document produced by `this`. If `this` goes
  `docA` → `docB` and `other` represents `docB` → `docC`, the
  returned value will represent the change `docA` → `docC`.
  */
  compose(t) {
    return this.empty ? t : t.empty ? this : sl(this, t, !0);
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
    return t.empty ? this : hs(this, t, e, !0);
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
    as(this, t, e);
  }
  /**
  Get a [change description](https://codemirror.net/6/docs/ref/#state.ChangeDesc) for this change
  set.
  */
  get desc() {
    return Qt.create(this.sections);
  }
  /**
  @internal
  */
  filter(t) {
    let e = [], i = [], n = [], r = new li(this);
    t: for (let o = 0, l = 0; ; ) {
      let a = o == t.length ? 1e9 : t[o++];
      for (; l < a || l == a && r.len == 0; ) {
        if (r.done)
          break t;
        let f = Math.min(r.len, a - l);
        it(n, f, -1);
        let c = r.ins == -1 ? -1 : r.off == 0 ? r.ins : 0;
        it(e, f, c), c > 0 && ie(i, e, r.text), r.forward(f), l += f;
      }
      let h = t[o++];
      for (; l < h; ) {
        if (r.done)
          break t;
        let f = Math.min(r.len, h - l);
        it(e, f, -1), it(n, f, r.ins == -1 ? -1 : r.off == 0 ? r.ins : 0), r.forward(f), l += f;
      }
    }
    return {
      changes: new Y(e, i),
      filtered: Qt.create(n)
    };
  }
  /**
  Serialize this change set to a JSON-representable value.
  */
  toJSON() {
    let t = [];
    for (let e = 0; e < this.sections.length; e += 2) {
      let i = this.sections[e], n = this.sections[e + 1];
      n < 0 ? t.push(i) : n == 0 ? t.push([i]) : t.push([i].concat(this.inserted[e >> 1].toJSON()));
    }
    return t;
  }
  /**
  Create a change set for the given changes, for a document of the
  given length, using `lineSep` as line separator.
  */
  static of(t, e, i) {
    let n = [], r = [], o = 0, l = null;
    function a(f = !1) {
      if (!f && !n.length)
        return;
      o < e && it(n, e - o, -1);
      let c = new Y(n, r);
      l = l ? l.compose(c.map(l)) : c, n = [], r = [], o = 0;
    }
    function h(f) {
      if (Array.isArray(f))
        for (let c of f)
          h(c);
      else if (f instanceof Y) {
        if (f.length != e)
          throw new RangeError(`Mismatched change set length (got ${f.length}, expected ${e})`);
        a(), l = l ? l.compose(f.map(l)) : f;
      } else {
        let { from: c, to: u = c, insert: d } = f;
        if (c > u || c < 0 || u > e)
          throw new RangeError(`Invalid change range ${c} to ${u} (in doc of length ${e})`);
        let p = d ? typeof d == "string" ? E.of(d.split(i || ls)) : d : E.empty, g = p.length;
        if (c == u && g == 0)
          return;
        c < o && a(), c > o && it(n, c - o, -1), it(n, u - c, g), ie(r, n, p), o = u;
      }
    }
    return h(t), a(!l), l;
  }
  /**
  Create an empty changeset of the given length.
  */
  static empty(t) {
    return new Y(t ? [t, -1] : [], []);
  }
  /**
  Create a changeset from its JSON representation (as produced by
  [`toJSON`](https://codemirror.net/6/docs/ref/#state.ChangeSet.toJSON).
  */
  static fromJSON(t) {
    if (!Array.isArray(t))
      throw new RangeError("Invalid JSON representation of ChangeSet");
    let e = [], i = [];
    for (let n = 0; n < t.length; n++) {
      let r = t[n];
      if (typeof r == "number")
        e.push(r, -1);
      else {
        if (!Array.isArray(r) || typeof r[0] != "number" || r.some((o, l) => l && typeof o != "string"))
          throw new RangeError("Invalid JSON representation of ChangeSet");
        if (r.length == 1)
          e.push(r[0], 0);
        else {
          for (; i.length < n; )
            i.push(E.empty);
          i[n] = E.of(r.slice(1)), e.push(r[0], i[n].length);
        }
      }
    }
    return new Y(e, i);
  }
  /**
  @internal
  */
  static createSet(t, e) {
    return new Y(t, e);
  }
}
function it(s, t, e, i = !1) {
  if (t == 0 && e <= 0)
    return;
  let n = s.length - 2;
  n >= 0 && e <= 0 && e == s[n + 1] ? s[n] += t : n >= 0 && t == 0 && s[n] == 0 ? s[n + 1] += e : i ? (s[n] += t, s[n + 1] += e) : s.push(t, e);
}
function ie(s, t, e) {
  if (e.length == 0)
    return;
  let i = t.length - 2 >> 1;
  if (i < s.length)
    s[s.length - 1] = s[s.length - 1].append(e);
  else {
    for (; s.length < i; )
      s.push(E.empty);
    s.push(e);
  }
}
function as(s, t, e) {
  let i = s.inserted;
  for (let n = 0, r = 0, o = 0; o < s.sections.length; ) {
    let l = s.sections[o++], a = s.sections[o++];
    if (a < 0)
      n += l, r += l;
    else {
      let h = n, f = r, c = E.empty;
      for (; h += l, f += a, a && i && (c = c.append(i[o - 2 >> 1])), !(e || o == s.sections.length || s.sections[o + 1] < 0); )
        l = s.sections[o++], a = s.sections[o++];
      t(n, h, r, f, c), n = h, r = f;
    }
  }
}
function hs(s, t, e, i = !1) {
  let n = [], r = i ? [] : null, o = new li(s), l = new li(t);
  for (let a = -1; ; ) {
    if (o.done && l.len || l.done && o.len)
      throw new Error("Mismatched change set lengths");
    if (o.ins == -1 && l.ins == -1) {
      let h = Math.min(o.len, l.len);
      it(n, h, -1), o.forward(h), l.forward(h);
    } else if (l.ins >= 0 && (o.ins < 0 || a == o.i || o.off == 0 && (l.len < o.len || l.len == o.len && !e))) {
      let h = l.len;
      for (it(n, l.ins, -1); h; ) {
        let f = Math.min(o.len, h);
        o.ins >= 0 && a < o.i && o.len <= f && (it(n, 0, o.ins), r && ie(r, n, o.text), a = o.i), o.forward(f), h -= f;
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
      it(n, h, a < o.i ? o.ins : 0), r && a < o.i && ie(r, n, o.text), a = o.i, o.forward(o.len - f);
    } else {
      if (o.done && l.done)
        return r ? Y.createSet(n, r) : Qt.create(n);
      throw new Error("Mismatched change set lengths");
    }
  }
}
function sl(s, t, e = !1) {
  let i = [], n = e ? [] : null, r = new li(s), o = new li(t);
  for (let l = !1; ; ) {
    if (r.done && o.done)
      return n ? Y.createSet(i, n) : Qt.create(i);
    if (r.ins == 0)
      it(i, r.len, 0, l), r.next();
    else if (o.len == 0 && !o.done)
      it(i, 0, o.ins, l), n && ie(n, i, o.text), o.next();
    else {
      if (r.done || o.done)
        throw new Error("Mismatched change set lengths");
      {
        let a = Math.min(r.len2, o.len), h = i.length;
        if (r.ins == -1) {
          let f = o.ins == -1 ? -1 : o.off ? 0 : o.ins;
          it(i, a, f, l), n && f && ie(n, i, o.text);
        } else o.ins == -1 ? (it(i, r.off ? 0 : r.len, a, l), n && ie(n, i, r.textBit(a))) : (it(i, r.off ? 0 : r.len, o.off ? 0 : o.ins, l), n && !o.off && ie(n, i, o.text));
        l = (r.ins > a || o.ins >= 0 && o.len > a) && (l || i.length > h), r.forward2(a), o.forward(a);
      }
    }
  }
}
class li {
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
    return e >= t.length ? E.empty : t[e];
  }
  textBit(t) {
    let { inserted: e } = this.set, i = this.i - 2 >> 1;
    return i >= e.length && !t ? E.empty : e[i].slice(this.off, t == null ? void 0 : this.off + t);
  }
  forward(t) {
    t == this.len ? this.next() : (this.len -= t, this.off += t);
  }
  forward2(t) {
    this.ins == -1 ? this.forward(t) : t == this.ins ? this.next() : (this.ins -= t, this.off += t);
  }
}
class me {
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
    let i, n;
    return this.empty ? i = n = t.mapPos(this.from, e) : (i = t.mapPos(this.from, 1), n = t.mapPos(this.to, -1)), i == this.from && n == this.to ? this : new me(i, n, this.flags);
  }
  /**
  Extend this range to cover at least `from` to `to`.
  */
  extend(t, e = t) {
    if (t <= this.anchor && e >= this.anchor)
      return x.range(t, e);
    let i = Math.abs(t - this.anchor) > Math.abs(e - this.anchor) ? t : e;
    return x.range(this.anchor, i);
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
    return x.range(t.anchor, t.head);
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new me(t, e, i);
  }
}
class x {
  constructor(t, e) {
    this.ranges = t, this.mainIndex = e;
  }
  /**
  Map a selection through a change. Used to adjust the selection
  position for changes.
  */
  map(t, e = -1) {
    return t.empty ? this : x.create(this.ranges.map((i) => i.map(t, e)), this.mainIndex);
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
    return this.ranges.length == 1 ? this : new x([this.main], 0);
  }
  /**
  Extend this selection with an extra range.
  */
  addRange(t, e = !0) {
    return x.create([t].concat(this.ranges), e ? 0 : this.mainIndex + 1);
  }
  /**
  Replace a given range with another range, and then normalize the
  selection to merge and sort ranges if necessary.
  */
  replaceRange(t, e = this.mainIndex) {
    let i = this.ranges.slice();
    return i[e] = t, x.create(i, this.mainIndex);
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
    return new x(t.ranges.map((e) => me.fromJSON(e)), t.main);
  }
  /**
  Create a selection holding a single range.
  */
  static single(t, e = t) {
    return new x([x.range(t, e)], 0);
  }
  /**
  Sort and merge the given set of ranges, creating a valid
  selection.
  */
  static create(t, e = 0) {
    if (t.length == 0)
      throw new RangeError("A selection needs at least one range");
    for (let i = 0, n = 0; n < t.length; n++) {
      let r = t[n];
      if (r.empty ? r.from <= i : r.from < i)
        return x.normalized(t.slice(), e);
      i = r.to;
    }
    return new x(t, e);
  }
  /**
  Create a cursor selection range at the given position. You can
  safely ignore the optional arguments in most situations.
  */
  static cursor(t, e = 0, i, n) {
    return me.create(t, t, (e == 0 ? 0 : e < 0 ? 8 : 16) | (i == null ? 7 : Math.min(6, i)) | (n ?? 16777215) << 6);
  }
  /**
  Create a selection range.
  */
  static range(t, e, i, n) {
    let r = (i ?? 16777215) << 6 | (n == null ? 7 : Math.min(6, n));
    return e < t ? me.create(e, t, 48 | r) : me.create(t, e, (e > t ? 8 : 0) | r);
  }
  /**
  @internal
  */
  static normalized(t, e = 0) {
    let i = t[e];
    t.sort((n, r) => n.from - r.from), e = t.indexOf(i);
    for (let n = 1; n < t.length; n++) {
      let r = t[n], o = t[n - 1];
      if (r.empty ? r.from <= o.to : r.from < o.to) {
        let l = o.from, a = Math.max(r.to, o.to);
        n <= e && e--, t.splice(--n, 2, r.anchor > r.head ? x.range(a, l) : x.range(l, a));
      }
    }
    return new x(t, e);
  }
}
function rl(s, t) {
  for (let e of s.ranges)
    if (e.to > t)
      throw new RangeError("Selection points outside of document");
}
let qs = 0;
class P {
  constructor(t, e, i, n, r) {
    this.combine = t, this.compareInput = e, this.compare = i, this.isStatic = n, this.id = qs++, this.default = t([]), this.extensions = typeof r == "function" ? r(this) : r;
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
    return new P(t.combine || ((e) => e), t.compareInput || ((e, i) => e === i), t.compare || (t.combine ? (e, i) => e === i : Us), !!t.static, t.enables);
  }
  /**
  Returns an extension that adds the given value to this facet.
  */
  of(t) {
    return new _i([], this, 0, t);
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
    return new _i(t, this, 1, e);
  }
  /**
  Create an extension that computes zero or more values for this
  facet from a state.
  */
  computeN(t, e) {
    if (this.isStatic)
      throw new Error("Can't compute a static facet");
    return new _i(t, this, 2, e);
  }
  from(t, e) {
    return e || (e = (i) => i), this.compute([t], (i) => e(i.field(t)));
  }
}
function Us(s, t) {
  return s == t || s.length == t.length && s.every((e, i) => e === t[i]);
}
class _i {
  constructor(t, e, i, n) {
    this.dependencies = t, this.facet = e, this.type = i, this.value = n, this.id = qs++;
  }
  dynamicSlot(t) {
    var e;
    let i = this.value, n = this.facet.compareInput, r = this.id, o = t[r] >> 1, l = this.type == 2, a = !1, h = !1, f = [];
    for (let c of this.dependencies)
      c == "doc" ? a = !0 : c == "selection" ? h = !0 : (((e = t[c.id]) !== null && e !== void 0 ? e : 1) & 1) == 0 && f.push(t[c.id]);
    return {
      create(c) {
        return c.values[o] = i(c), 1;
      },
      update(c, u) {
        if (a && u.docChanged || h && (u.docChanged || u.selection) || fs(c, f)) {
          let d = i(c);
          if (l ? !Dr(d, c.values[o], n) : !n(d, c.values[o]))
            return c.values[o] = d, 1;
        }
        return 0;
      },
      reconfigure: (c, u) => {
        let d, p = u.config.address[r];
        if (p != null) {
          let g = en(u, p);
          if (this.dependencies.every((m) => m instanceof P ? u.facet(m) === c.facet(m) : m instanceof Pt ? u.field(m, !1) == c.field(m, !1) : !0) || (l ? Dr(d = i(c), g, n) : n(d = i(c), g)))
            return c.values[o] = g, 0;
        } else
          d = i(c);
        return c.values[o] = d, 1;
      }
    };
  }
}
function Dr(s, t, e) {
  if (s.length != t.length)
    return !1;
  for (let i = 0; i < s.length; i++)
    if (!e(s[i], t[i]))
      return !1;
  return !0;
}
function fs(s, t) {
  let e = !1;
  for (let i of t)
    ii(s, i) & 1 && (e = !0);
  return e;
}
function yh(s, t, e) {
  let i = e.map((a) => s[a.id]), n = e.map((a) => a.type), r = i.filter((a) => !(a & 1)), o = s[t.id] >> 1;
  function l(a) {
    let h = [];
    for (let f = 0; f < i.length; f++) {
      let c = en(a, i[f]);
      if (n[f] == 2)
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
        ii(a, h);
      return a.values[o] = l(a), 1;
    },
    update(a, h) {
      if (!fs(a, r))
        return 0;
      let f = l(a);
      return t.compare(f, a.values[o]) ? 0 : (a.values[o] = f, 1);
    },
    reconfigure(a, h) {
      let f = fs(a, i), c = h.config.facets[t.id], u = h.facet(t);
      if (c && !f && Us(e, c))
        return a.values[o] = u, 0;
      let d = l(a);
      return t.compare(d, u) ? (a.values[o] = u, 0) : (a.values[o] = d, 1);
    }
  };
}
const Ci = /* @__PURE__ */ P.define({ static: !0 });
class Pt {
  constructor(t, e, i, n, r) {
    this.id = t, this.createF = e, this.updateF = i, this.compareF = n, this.spec = r, this.provides = void 0;
  }
  /**
  Define a state field.
  */
  static define(t) {
    let e = new Pt(qs++, t.create, t.update, t.compare || ((i, n) => i === n), t);
    return t.provide && (e.provides = t.provide(e)), e;
  }
  create(t) {
    let e = t.facet(Ci).find((i) => i.field == this);
    return ((e == null ? void 0 : e.create) || this.createF)(t);
  }
  /**
  @internal
  */
  slot(t) {
    let e = t[this.id] >> 1;
    return {
      create: (i) => (i.values[e] = this.create(i), 1),
      update: (i, n) => {
        let r = i.values[e], o = this.updateF(r, n);
        return this.compareF(r, o) ? 0 : (i.values[e] = o, 1);
      },
      reconfigure: (i, n) => {
        let r = i.facet(Ci), o = n.facet(Ci), l;
        return (l = r.find((a) => a.field == this)) && l != o.find((a) => a.field == this) ? (i.values[e] = l.create(i), 1) : n.config.address[this.id] != null ? (i.values[e] = n.field(this), 0) : (i.values[e] = this.create(i), 1);
      }
    };
  }
  /**
  Returns an extension that enables this field and overrides the
  way it is initialized. Can be useful when you need to provide a
  non-default starting value for the field.
  */
  init(t) {
    return [this, Ci.of({ field: this, create: t })];
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
const pe = { lowest: 4, low: 3, default: 2, high: 1, highest: 0 };
function qe(s) {
  return (t) => new ol(t, s);
}
const An = {
  /**
  The highest precedence level, for extensions that should end up
  near the start of the precedence ordering.
  */
  highest: /* @__PURE__ */ qe(pe.highest),
  /**
  A higher-than-default precedence, for extensions that should
  come before those with default precedence.
  */
  high: /* @__PURE__ */ qe(pe.high),
  /**
  The default precedence, which is also used for extensions
  without an explicit precedence.
  */
  default: /* @__PURE__ */ qe(pe.default),
  /**
  A lower-than-default precedence.
  */
  low: /* @__PURE__ */ qe(pe.low),
  /**
  The lowest precedence level. Meant for things that should end up
  near the end of the extension order.
  */
  lowest: /* @__PURE__ */ qe(pe.lowest)
};
class ol {
  constructor(t, e) {
    this.inner = t, this.prec = e;
  }
}
class yi {
  /**
  Create an instance of this compartment to add to your [state
  configuration](https://codemirror.net/6/docs/ref/#state.EditorStateConfig.extensions).
  */
  of(t) {
    return new cs(this, t);
  }
  /**
  Create an [effect](https://codemirror.net/6/docs/ref/#state.TransactionSpec.effects) that
  reconfigures this compartment.
  */
  reconfigure(t) {
    return yi.reconfigure.of({ compartment: this, extension: t });
  }
  /**
  Get the current content of the compartment in the state, or
  `undefined` if it isn't present.
  */
  get(t) {
    return t.config.compartments.get(this);
  }
}
class cs {
  constructor(t, e) {
    this.compartment = t, this.inner = e;
  }
}
class tn {
  constructor(t, e, i, n, r, o) {
    for (this.base = t, this.compartments = e, this.dynamicSlots = i, this.address = n, this.staticValues = r, this.facets = o, this.statusTemplate = []; this.statusTemplate.length < i.length; )
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
    let n = [], r = /* @__PURE__ */ Object.create(null), o = /* @__PURE__ */ new Map();
    for (let u of bh(t, e, o))
      u instanceof Pt ? n.push(u) : (r[u.facet.id] || (r[u.facet.id] = [])).push(u);
    let l = /* @__PURE__ */ Object.create(null), a = [], h = [];
    for (let u of n)
      l[u.id] = h.length << 1, h.push((d) => u.slot(d));
    let f = i == null ? void 0 : i.config.facets;
    for (let u in r) {
      let d = r[u], p = d[0].facet, g = f && f[u] || [];
      if (d.every(
        (m) => m.type == 0
        /* Provider.Static */
      ))
        if (l[p.id] = a.length << 1 | 1, Us(g, d))
          a.push(i.facet(p));
        else {
          let m = p.combine(d.map((y) => y.value));
          a.push(i && p.compare(m, i.facet(p)) ? i.facet(p) : m);
        }
      else {
        for (let m of d)
          m.type == 0 ? (l[m.id] = a.length << 1 | 1, a.push(m.value)) : (l[m.id] = h.length << 1, h.push((y) => m.dynamicSlot(y)));
        l[p.id] = h.length << 1, h.push((m) => yh(m, p, d));
      }
    }
    let c = h.map((u) => u(l));
    return new tn(t, o, c, l, a, r);
  }
}
function bh(s, t, e) {
  let i = [[], [], [], [], []], n = /* @__PURE__ */ new Map();
  function r(o, l) {
    let a = n.get(o);
    if (a != null) {
      if (a <= l)
        return;
      let h = i[a].indexOf(o);
      h > -1 && i[a].splice(h, 1), o instanceof cs && e.delete(o.compartment);
    }
    if (n.set(o, l), Array.isArray(o))
      for (let h of o)
        r(h, l);
    else if (o instanceof cs) {
      if (e.has(o.compartment))
        throw new RangeError("Duplicate use of compartment in extensions");
      let h = t.get(o.compartment) || o.inner;
      e.set(o.compartment, h), r(h, l);
    } else if (o instanceof ol)
      r(o.inner, o.prec);
    else if (o instanceof Pt)
      i[l].push(o), o.provides && r(o.provides, l);
    else if (o instanceof _i)
      i[l].push(o), o.facet.extensions && r(o.facet.extensions, pe.default);
    else {
      let h = o.extension;
      if (!h)
        throw new Error(`Unrecognized extension value in extension set (${o}). This sometimes happens because multiple instances of @codemirror/state are loaded, breaking instanceof checks.`);
      r(h, l);
    }
  }
  return r(s, pe.default), i.reduce((o, l) => o.concat(l));
}
function ii(s, t) {
  if (t & 1)
    return 2;
  let e = t >> 1, i = s.status[e];
  if (i == 4)
    throw new Error("Cyclic dependency between fields and/or facets");
  if (i & 2)
    return i;
  s.status[e] = 4;
  let n = s.computeSlot(s, s.config.dynamicSlots[e]);
  return s.status[e] = 2 | n;
}
function en(s, t) {
  return t & 1 ? s.config.staticValues[t >> 1] : s.values[t >> 1];
}
const ll = /* @__PURE__ */ P.define(), us = /* @__PURE__ */ P.define({
  combine: (s) => s.some((t) => t),
  static: !0
}), al = /* @__PURE__ */ P.define({
  combine: (s) => s.length ? s[0] : void 0,
  static: !0
}), hl = /* @__PURE__ */ P.define(), fl = /* @__PURE__ */ P.define(), cl = /* @__PURE__ */ P.define(), ul = /* @__PURE__ */ P.define({
  combine: (s) => s.length ? s[0] : !1
});
class Gt {
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
    return new wh();
  }
}
class wh {
  /**
  Create an instance of this annotation.
  */
  of(t) {
    return new Gt(this, t);
  }
}
class xh {
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
    return new I(this, t);
  }
}
class I {
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
    return e === void 0 ? void 0 : e == this.value ? this : new I(this.type, e);
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
    return new xh(t.map || ((e) => e));
  }
  /**
  Map an array of effects through a change set.
  */
  static mapEffects(t, e) {
    if (!t.length)
      return t;
    let i = [];
    for (let n of t) {
      let r = n.map(e);
      r && i.push(r);
    }
    return i;
  }
}
I.reconfigure = /* @__PURE__ */ I.define();
I.appendConfig = /* @__PURE__ */ I.define();
class G {
  constructor(t, e, i, n, r, o) {
    this.startState = t, this.changes = e, this.selection = i, this.effects = n, this.annotations = r, this.scrollIntoView = o, this._doc = null, this._state = null, i && rl(i, e.newLength), r.some((l) => l.type == G.time) || (this.annotations = r.concat(G.time.of(Date.now())));
  }
  /**
  @internal
  */
  static create(t, e, i, n, r, o) {
    return new G(t, e, i, n, r, o);
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
    let e = this.annotation(G.userEvent);
    return !!(e && (e == t || e.length > t.length && e.slice(0, t.length) == t && e[t.length] == "."));
  }
}
G.time = /* @__PURE__ */ Gt.define();
G.userEvent = /* @__PURE__ */ Gt.define();
G.addToHistory = /* @__PURE__ */ Gt.define();
G.remote = /* @__PURE__ */ Gt.define();
function kh(s, t) {
  let e = [];
  for (let i = 0, n = 0; ; ) {
    let r, o;
    if (i < s.length && (n == t.length || t[n] >= s[i]))
      r = s[i++], o = s[i++];
    else if (n < t.length)
      r = t[n++], o = t[n++];
    else
      return e;
    !e.length || e[e.length - 1] < r ? e.push(r, o) : e[e.length - 1] < o && (e[e.length - 1] = o);
  }
}
function dl(s, t, e) {
  var i;
  let n, r, o;
  return e ? (n = t.changes, r = Y.empty(t.changes.length), o = s.changes.compose(t.changes)) : (n = t.changes.map(s.changes), r = s.changes.mapDesc(t.changes, !0), o = s.changes.compose(n)), {
    changes: o,
    selection: t.selection ? t.selection.map(r) : (i = s.selection) === null || i === void 0 ? void 0 : i.map(n),
    effects: I.mapEffects(s.effects, n).concat(I.mapEffects(t.effects, r)),
    annotations: s.annotations.length ? s.annotations.concat(t.annotations) : t.annotations,
    scrollIntoView: s.scrollIntoView || t.scrollIntoView
  };
}
function ds(s, t, e) {
  let i = t.selection, n = Me(t.annotations);
  return t.userEvent && (n = n.concat(G.userEvent.of(t.userEvent))), {
    changes: t.changes instanceof Y ? t.changes : Y.of(t.changes || [], e, s.facet(al)),
    selection: i && (i instanceof x ? i : x.single(i.anchor, i.head)),
    effects: Me(t.effects),
    annotations: n,
    scrollIntoView: !!t.scrollIntoView
  };
}
function pl(s, t, e) {
  let i = ds(s, t.length ? t[0] : {}, s.doc.length);
  t.length && t[0].filter === !1 && (e = !1);
  for (let r = 1; r < t.length; r++) {
    t[r].filter === !1 && (e = !1);
    let o = !!t[r].sequential;
    i = dl(i, ds(s, t[r], o ? i.changes.newLength : s.doc.length), o);
  }
  let n = G.create(s, i.changes, i.selection, i.effects, i.annotations, i.scrollIntoView);
  return vh(e ? Sh(n) : n);
}
function Sh(s) {
  let t = s.startState, e = !0;
  for (let n of t.facet(hl)) {
    let r = n(s);
    if (r === !1) {
      e = !1;
      break;
    }
    Array.isArray(r) && (e = e === !0 ? r : kh(e, r));
  }
  if (e !== !0) {
    let n, r;
    if (e === !1)
      r = s.changes.invertedDesc, n = Y.empty(t.doc.length);
    else {
      let o = s.changes.filter(e);
      n = o.changes, r = o.filtered.mapDesc(o.changes).invertedDesc;
    }
    s = G.create(t, n, s.selection && s.selection.map(r), I.mapEffects(s.effects, r), s.annotations, s.scrollIntoView);
  }
  let i = t.facet(fl);
  for (let n = i.length - 1; n >= 0; n--) {
    let r = i[n](s);
    r instanceof G ? s = r : Array.isArray(r) && r.length == 1 && r[0] instanceof G ? s = r[0] : s = pl(t, Me(r), !1);
  }
  return s;
}
function vh(s) {
  let t = s.startState, e = t.facet(cl), i = s;
  for (let n = e.length - 1; n >= 0; n--) {
    let r = e[n](s);
    r && Object.keys(r).length && (i = dl(i, ds(t, r, s.changes.newLength), !0));
  }
  return i == s ? s : G.create(t, s.changes, s.selection, i.effects, i.annotations, i.scrollIntoView);
}
const Oh = [];
function Me(s) {
  return s == null ? Oh : Array.isArray(s) ? s : [s];
}
var Ut = /* @__PURE__ */ (function(s) {
  return s[s.Word = 0] = "Word", s[s.Space = 1] = "Space", s[s.Other = 2] = "Other", s;
})(Ut || (Ut = {}));
const Ch = /[\u00df\u0587\u0590-\u05f4\u0600-\u06ff\u3040-\u309f\u30a0-\u30ff\u3400-\u4db5\u4e00-\u9fcc\uac00-\ud7af]/;
let ps;
try {
  ps = /* @__PURE__ */ new RegExp("[\\p{Alphabetic}\\p{Number}_]", "u");
} catch {
}
function Ah(s) {
  if (ps)
    return ps.test(s);
  for (let t = 0; t < s.length; t++) {
    let e = s[t];
    if (/\w/.test(e) || e > "" && (e.toUpperCase() != e.toLowerCase() || Ch.test(e)))
      return !0;
  }
  return !1;
}
function Th(s) {
  return (t) => {
    if (!/\S/.test(t))
      return Ut.Space;
    if (Ah(t))
      return Ut.Word;
    for (let e = 0; e < s.length; e++)
      if (t.indexOf(s[e]) > -1)
        return Ut.Word;
    return Ut.Other;
  };
}
class N {
  constructor(t, e, i, n, r, o) {
    this.config = t, this.doc = e, this.selection = i, this.values = n, this.status = t.statusTemplate.slice(), this.computeSlot = r, o && (o._state = this);
    for (let l = 0; l < this.config.dynamicSlots.length; l++)
      ii(this, l << 1);
    this.computeSlot = null;
  }
  field(t, e = !0) {
    let i = this.config.address[t.id];
    if (i == null) {
      if (e)
        throw new RangeError("Field is not present in this state");
      return;
    }
    return ii(this, i), en(this, i);
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
    return pl(this, t, !0);
  }
  /**
  @internal
  */
  applyTransaction(t) {
    let e = this.config, { base: i, compartments: n } = e;
    for (let l of t.effects)
      l.is(yi.reconfigure) ? (e && (n = /* @__PURE__ */ new Map(), e.compartments.forEach((a, h) => n.set(h, a)), e = null), n.set(l.value.compartment, l.value.extension)) : l.is(I.reconfigure) ? (e = null, i = l.value) : l.is(I.appendConfig) && (e = null, i = Me(i).concat(l.value));
    let r;
    e ? r = t.startState.values.slice() : (e = tn.resolve(i, n, this), r = new N(e, this.doc, this.selection, e.dynamicSlots.map(() => null), (a, h) => h.reconfigure(a, this), null).values);
    let o = t.startState.facet(us) ? t.newSelection : t.newSelection.asSingle();
    new N(e, t.newDoc, o, r, (l, a) => a.update(l, t), t);
  }
  /**
  Create a [transaction spec](https://codemirror.net/6/docs/ref/#state.TransactionSpec) that
  replaces every selection range with the given content.
  */
  replaceSelection(t) {
    return typeof t == "string" && (t = this.toText(t)), this.changeByRange((e) => ({
      changes: { from: e.from, to: e.to, insert: t },
      range: x.cursor(e.from + t.length)
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
    let e = this.selection, i = t(e.ranges[0]), n = this.changes(i.changes), r = [i.range], o = Me(i.effects);
    for (let l = 1; l < e.ranges.length; l++) {
      let a = t(e.ranges[l]), h = this.changes(a.changes), f = h.map(n);
      for (let u = 0; u < l; u++)
        r[u] = r[u].map(f);
      let c = n.mapDesc(h, !0);
      r.push(a.range.map(c)), n = n.compose(f), o = I.mapEffects(o, f).concat(I.mapEffects(Me(a.effects), c));
    }
    return {
      changes: n,
      selection: x.create(r, e.mainIndex),
      effects: o
    };
  }
  /**
  Create a [change set](https://codemirror.net/6/docs/ref/#state.ChangeSet) from the given change
  description, taking the state's document length and line
  separator into account.
  */
  changes(t = []) {
    return t instanceof Y ? t : Y.of(t, this.doc.length, this.facet(N.lineSeparator));
  }
  /**
  Using the state's [line
  separator](https://codemirror.net/6/docs/ref/#state.EditorState^lineSeparator), create a
  [`Text`](https://codemirror.net/6/docs/ref/#state.Text) instance from the given string.
  */
  toText(t) {
    return E.of(t.split(this.facet(N.lineSeparator) || ls));
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
    return e == null ? t.default : (ii(this, e), en(this, e));
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
        let n = t[i];
        n instanceof Pt && this.config.address[n.id] != null && (e[i] = n.spec.toJSON(this.field(t[i]), this));
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
    let n = [];
    if (i) {
      for (let r in i)
        if (Object.prototype.hasOwnProperty.call(t, r)) {
          let o = i[r], l = t[r];
          n.push(o.init((a) => o.spec.fromJSON(l, a)));
        }
    }
    return N.create({
      doc: t.doc,
      selection: x.fromJSON(t.selection),
      extensions: e.extensions ? n.concat([e.extensions]) : n
    });
  }
  /**
  Create a new state. You'll usually only need this when
  initializing an editor—updated states are created by applying
  transactions.
  */
  static create(t = {}) {
    let e = tn.resolve(t.extensions || [], /* @__PURE__ */ new Map()), i = t.doc instanceof E ? t.doc : E.of((t.doc || "").split(e.staticFacet(N.lineSeparator) || ls)), n = t.selection ? t.selection instanceof x ? t.selection : x.single(t.selection.anchor, t.selection.head) : x.single(0);
    return rl(n, i.length), e.staticFacet(us) || (n = n.asSingle()), new N(e, i, n, e.dynamicSlots.map(() => null), (r, o) => o.create(r), null);
  }
  /**
  The size (in columns) of a tab in the document, determined by
  the [`tabSize`](https://codemirror.net/6/docs/ref/#state.EditorState^tabSize) facet.
  */
  get tabSize() {
    return this.facet(N.tabSize);
  }
  /**
  Get the proper [line-break](https://codemirror.net/6/docs/ref/#state.EditorState^lineSeparator)
  string for this state.
  */
  get lineBreak() {
    return this.facet(N.lineSeparator) || `
`;
  }
  /**
  Returns true when the editor is
  [configured](https://codemirror.net/6/docs/ref/#state.EditorState^readOnly) to be read-only.
  */
  get readOnly() {
    return this.facet(ul);
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
    for (let i of this.facet(N.phrases))
      if (Object.prototype.hasOwnProperty.call(i, t)) {
        t = i[t];
        break;
      }
    return e.length && (t = t.replace(/\$(\$|\d*)/g, (i, n) => {
      if (n == "$")
        return "$";
      let r = +(n || 1);
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
    let n = [];
    for (let r of this.facet(ll))
      for (let o of r(this, e, i))
        Object.prototype.hasOwnProperty.call(o, t) && n.push(o[t]);
    return n;
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
    return Th(e.length ? e[0] : "");
  }
  /**
  Find the word at the given position, meaning the range
  containing all [word](https://codemirror.net/6/docs/ref/#state.CharCategory.Word) characters
  around it. If no word characters are adjacent to the position,
  this returns null.
  */
  wordAt(t) {
    let { text: e, from: i, length: n } = this.doc.lineAt(t), r = this.charCategorizer(t), o = t - i, l = t - i;
    for (; o > 0; ) {
      let a = nt(e, o, !1);
      if (r(e.slice(a, o)) != Ut.Word)
        break;
      o = a;
    }
    for (; l < n; ) {
      let a = nt(e, l);
      if (r(e.slice(l, a)) != Ut.Word)
        break;
      l = a;
    }
    return o == l ? null : x.range(o + i, l + i);
  }
}
N.allowMultipleSelections = us;
N.tabSize = /* @__PURE__ */ P.define({
  combine: (s) => s.length ? s[0] : 4
});
N.lineSeparator = al;
N.readOnly = ul;
N.phrases = /* @__PURE__ */ P.define({
  compare(s, t) {
    let e = Object.keys(s), i = Object.keys(t);
    return e.length == i.length && e.every((n) => s[n] == t[n]);
  }
});
N.languageData = ll;
N.changeFilter = hl;
N.transactionFilter = fl;
N.transactionExtender = cl;
yi.reconfigure = /* @__PURE__ */ I.define();
function Ks(s, t, e = {}) {
  let i = {};
  for (let n of s)
    for (let r of Object.keys(n)) {
      let o = n[r], l = i[r];
      if (l === void 0)
        i[r] = o;
      else if (!(l === o || o === void 0)) if (Object.hasOwnProperty.call(e, r))
        i[r] = e[r](l, o);
      else
        throw new Error("Config merge conflict for field " + r);
    }
  for (let n in t)
    i[n] === void 0 && (i[n] = t[n]);
  return i;
}
class se {
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
    return gs.create(t, e, this);
  }
}
se.prototype.startSide = se.prototype.endSide = 0;
se.prototype.point = !1;
se.prototype.mapMode = at.TrackDel;
function _s(s, t) {
  return s == t || s.constructor == t.constructor && s.eq(t);
}
let gs = class gl {
  constructor(t, e, i) {
    this.from = t, this.to = e, this.value = i;
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new gl(t, e, i);
  }
};
function ms(s, t) {
  return s.from - t.from || s.value.startSide - t.value.startSide;
}
class Ys {
  constructor(t, e, i, n) {
    this.from = t, this.to = e, this.value = i, this.maxPoint = n;
  }
  get length() {
    return this.to[this.to.length - 1];
  }
  // Find the index of the given position and side. Use the ranges'
  // `from` pos when `end == false`, `to` when `end == true`.
  findIndex(t, e, i, n = 0) {
    let r = i ? this.to : this.from;
    for (let o = n, l = r.length; ; ) {
      if (o == l)
        return o;
      let a = o + l >> 1, h = r[a] - t || (i ? this.value[a].endSide : this.value[a].startSide) - e;
      if (a == o)
        return h >= 0 ? o : l;
      h >= 0 ? l = a : o = a + 1;
    }
  }
  between(t, e, i, n) {
    for (let r = this.findIndex(e, -1e9, !0), o = this.findIndex(i, 1e9, !1, r); r < o; r++)
      if (n(this.from[r] + t, this.to[r] + t, this.value[r]) === !1)
        return !1;
  }
  map(t, e) {
    let i = [], n = [], r = [], o = -1, l = -1;
    for (let a = 0; a < this.value.length; a++) {
      let h = this.value[a], f = this.from[a] + t, c = this.to[a] + t, u, d;
      if (f == c) {
        let p = e.mapPos(f, h.startSide, h.mapMode);
        if (p == null || (u = d = p, h.startSide != h.endSide && (d = e.mapPos(f, h.endSide), d < u)))
          continue;
      } else if (u = e.mapPos(f, h.startSide), d = e.mapPos(c, h.endSide), u > d || u == d && h.startSide > 0 && h.endSide <= 0)
        continue;
      (d - u || h.endSide - h.startSide) < 0 || (o < 0 && (o = u), h.point && (l = Math.max(l, d - u)), i.push(h), n.push(u - o), r.push(d - o));
    }
    return { mapped: i.length ? new Ys(n, r, i, l) : null, pos: o };
  }
}
class V {
  constructor(t, e, i, n) {
    this.chunkPos = t, this.chunk = e, this.nextLayer = i, this.maxPoint = n;
  }
  /**
  @internal
  */
  static create(t, e, i, n) {
    return new V(t, e, i, n);
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
    let { add: e = [], sort: i = !1, filterFrom: n = 0, filterTo: r = this.length } = t, o = t.filter;
    if (e.length == 0 && !o)
      return this;
    if (i && (e = e.slice().sort(ms)), this.isEmpty)
      return e.length ? V.of(e) : this;
    let l = new ml(this, null, -1).goto(0), a = 0, h = [], f = new ai();
    for (; l.value || a < e.length; )
      if (a < e.length && (l.from - e[a].from || l.startSide - e[a].value.startSide) >= 0) {
        let c = e[a++];
        f.addInner(c.from, c.to, c.value) || h.push(c);
      } else l.rangeIndex == 1 && l.chunkIndex < this.chunk.length && (a == e.length || this.chunkEnd(l.chunkIndex) < e[a].from) && (!o || n > this.chunkEnd(l.chunkIndex) || r < this.chunkPos[l.chunkIndex]) && f.addChunk(this.chunkPos[l.chunkIndex], this.chunk[l.chunkIndex]) ? l.nextChunk() : ((!o || n > l.to || r < l.from || o(l.from, l.to, l.value)) && (f.addInner(l.from, l.to, l.value) || h.push(gs.create(l.from, l.to, l.value))), l.next());
    return f.finishInner(this.nextLayer.isEmpty && !h.length ? V.empty : this.nextLayer.update({ add: h, filter: o, filterFrom: n, filterTo: r }));
  }
  /**
  Map this range set through a set of changes, return the new set.
  */
  map(t) {
    if (t.empty || this.isEmpty)
      return this;
    let e = [], i = [], n = -1;
    for (let o = 0; o < this.chunk.length; o++) {
      let l = this.chunkPos[o], a = this.chunk[o], h = t.touchesRange(l, l + a.length);
      if (h === !1)
        n = Math.max(n, a.maxPoint), e.push(a), i.push(t.mapPos(l));
      else if (h === !0) {
        let { mapped: f, pos: c } = a.map(l, t);
        f && (n = Math.max(n, f.maxPoint), e.push(f), i.push(c));
      }
    }
    let r = this.nextLayer.map(t);
    return e.length == 0 ? r : new V(i, e, r || V.empty, n);
  }
  /**
  Iterate over the ranges that touch the region `from` to `to`,
  calling `f` for each. There is no guarantee that the ranges will
  be reported in any specific order. When the callback returns
  `false`, iteration stops.
  */
  between(t, e, i) {
    if (!this.isEmpty) {
      for (let n = 0; n < this.chunk.length; n++) {
        let r = this.chunkPos[n], o = this.chunk[n];
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
    return hi.from([this]).goto(t);
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
    return hi.from(t).goto(e);
  }
  /**
  Iterate over two groups of sets, calling methods on `comparator`
  to notify it of possible differences.
  */
  static compare(t, e, i, n, r = -1) {
    let o = t.filter((c) => c.maxPoint > 0 || !c.isEmpty && c.maxPoint >= r), l = e.filter((c) => c.maxPoint > 0 || !c.isEmpty && c.maxPoint >= r), a = Rr(o, l, i), h = new Ue(o, a, r), f = new Ue(l, a, r);
    i.iterGaps((c, u, d) => Br(h, c, f, u, d, n)), i.empty && i.length == 0 && Br(h, 0, f, 0, 0, n);
  }
  /**
  Compare the contents of two groups of range sets, returning true
  if they are equivalent in the given range.
  */
  static eq(t, e, i = 0, n) {
    n == null && (n = 999999999);
    let r = t.filter((f) => !f.isEmpty && e.indexOf(f) < 0), o = e.filter((f) => !f.isEmpty && t.indexOf(f) < 0);
    if (r.length != o.length)
      return !1;
    if (!r.length)
      return !0;
    let l = Rr(r, o), a = new Ue(r, l, 0).goto(i), h = new Ue(o, l, 0).goto(i);
    for (; ; ) {
      if (a.to != h.to || !ys(a.active, h.active) || a.point && (!h.point || !_s(a.point, h.point)))
        return !1;
      if (a.to > n)
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
  static spans(t, e, i, n, r = -1) {
    let o = new Ue(t, null, r).goto(e), l = e, a = o.openStart;
    for (; ; ) {
      let h = Math.min(o.to, i);
      if (o.point) {
        let f = o.activeForPoint(o.to), c = o.pointFrom < e ? f.length + 1 : o.point.startSide < 0 ? f.length : Math.min(f.length, a);
        n.point(l, h, o.point, f, c, o.pointRank), a = Math.min(o.openEnd(h), f.length);
      } else h > l && (n.span(l, h, o.active, a), a = o.openEnd(h));
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
    let i = new ai();
    for (let n of t instanceof gs ? [t] : e ? Ph(t) : t)
      i.add(n.from, n.to, n.value);
    return i.finish();
  }
  /**
  Join an array of range sets into a single set.
  */
  static join(t) {
    if (!t.length)
      return V.empty;
    let e = t[t.length - 1];
    for (let i = t.length - 2; i >= 0; i--)
      for (let n = t[i]; n != V.empty; n = n.nextLayer)
        e = new V(n.chunkPos, n.chunk, e, Math.max(n.maxPoint, e.maxPoint));
    return e;
  }
}
V.empty = /* @__PURE__ */ new V([], [], null, -1);
function Ph(s) {
  if (s.length > 1)
    for (let t = s[0], e = 1; e < s.length; e++) {
      let i = s[e];
      if (ms(t, i) > 0)
        return s.slice().sort(ms);
      t = i;
    }
  return s;
}
V.empty.nextLayer = V.empty;
class ai {
  finishChunk(t) {
    this.chunks.push(new Ys(this.from, this.to, this.value, this.maxPoint)), this.chunkPos.push(this.chunkStart), this.chunkStart = -1, this.setMaxPoint = Math.max(this.setMaxPoint, this.maxPoint), this.maxPoint = -1, t && (this.from = [], this.to = [], this.value = []);
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
    this.addInner(t, e, i) || (this.nextLayer || (this.nextLayer = new ai())).add(t, e, i);
  }
  /**
  @internal
  */
  addInner(t, e, i) {
    let n = t - this.lastTo || i.startSide - this.last.endSide;
    if (n <= 0 && (t - this.lastFrom || i.startSide - this.last.startSide) < 0)
      throw new Error("Ranges must be added sorted by `from` position and `startSide`");
    return n < 0 ? !1 : (this.from.length == 250 && this.finishChunk(!0), this.chunkStart < 0 && (this.chunkStart = t), this.from.push(t - this.chunkStart), this.to.push(e - this.chunkStart), this.last = i, this.lastFrom = t, this.lastTo = e, this.value.push(i), i.point && (this.maxPoint = Math.max(this.maxPoint, e - t)), !0);
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
    return this.finishInner(V.empty);
  }
  /**
  @internal
  */
  finishInner(t) {
    if (this.from.length && this.finishChunk(!1), this.chunks.length == 0)
      return t;
    let e = V.create(this.chunkPos, this.chunks, this.nextLayer ? this.nextLayer.finishInner(t) : t, this.setMaxPoint);
    return this.from = null, e;
  }
}
function Rr(s, t, e) {
  let i = /* @__PURE__ */ new Map();
  for (let r of s)
    for (let o = 0; o < r.chunk.length; o++)
      r.chunk[o].maxPoint <= 0 && i.set(r.chunk[o], r.chunkPos[o]);
  let n = /* @__PURE__ */ new Set();
  for (let r of t)
    for (let o = 0; o < r.chunk.length; o++) {
      let l = i.get(r.chunk[o]);
      l != null && (e ? e.mapPos(l) : l) == r.chunkPos[o] && !(e != null && e.touchesRange(l, l + r.chunk[o].length)) && n.add(r.chunk[o]);
    }
  return n;
}
class ml {
  constructor(t, e, i, n = 0) {
    this.layer = t, this.skip = e, this.minPoint = i, this.rank = n;
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
      let n = this.layer.chunk[this.chunkIndex];
      if (!(this.skip && this.skip.has(n) || this.layer.chunkEnd(this.chunkIndex) < t || n.maxPoint < this.minPoint))
        break;
      this.chunkIndex++, i = !1;
    }
    if (this.chunkIndex < this.layer.chunk.length) {
      let n = this.layer.chunk[this.chunkIndex].findIndex(t - this.layer.chunkPos[this.chunkIndex], e, !0);
      (!i || this.rangeIndex < n) && this.setRangeIndex(n);
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
class hi {
  constructor(t) {
    this.heap = t;
  }
  static from(t, e = null, i = -1) {
    let n = [];
    for (let r = 0; r < t.length; r++)
      for (let o = t[r]; !o.isEmpty; o = o.nextLayer)
        o.maxPoint >= i && n.push(new ml(o, e, i, r));
    return n.length == 1 ? n[0] : new hi(n);
  }
  get startSide() {
    return this.value ? this.value.startSide : 0;
  }
  goto(t, e = -1e9) {
    for (let i of this.heap)
      i.goto(t, e);
    for (let i = this.heap.length >> 1; i >= 0; i--)
      Fn(this.heap, i);
    return this.next(), this;
  }
  forward(t, e) {
    for (let i of this.heap)
      i.forward(t, e);
    for (let i = this.heap.length >> 1; i >= 0; i--)
      Fn(this.heap, i);
    (this.to - t || this.value.endSide - e) < 0 && this.next();
  }
  next() {
    if (this.heap.length == 0)
      this.from = this.to = 1e9, this.value = null, this.rank = -1;
    else {
      let t = this.heap[0];
      this.from = t.from, this.to = t.to, this.value = t.value, this.rank = t.rank, t.value && t.next(), Fn(this.heap, 0);
    }
  }
}
function Fn(s, t) {
  for (let e = s[t]; ; ) {
    let i = (t << 1) + 1;
    if (i >= s.length)
      break;
    let n = s[i];
    if (i + 1 < s.length && n.compare(s[i + 1]) >= 0 && (n = s[i + 1], i++), e.compare(n) < 0)
      break;
    s[i] = e, s[t] = n, t = i;
  }
}
class Ue {
  constructor(t, e, i) {
    this.minPoint = i, this.active = [], this.activeTo = [], this.activeRank = [], this.minActive = -1, this.point = null, this.pointFrom = 0, this.pointRank = 0, this.to = -1e9, this.endSide = 0, this.openStart = -1, this.cursor = hi.from(t, e, i);
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
    Ai(this.active, t), Ai(this.activeTo, t), Ai(this.activeRank, t), this.minActive = Lr(this.active, this.activeTo);
  }
  addActive(t) {
    let e = 0, { value: i, to: n, rank: r } = this.cursor;
    for (; e < this.activeRank.length && (r - this.activeRank[e] || n - this.activeTo[e]) > 0; )
      e++;
    Ti(this.active, e, i), Ti(this.activeTo, e, n), Ti(this.activeRank, e, r), t && Ti(t, e, this.cursor.from), this.minActive = Lr(this.active, this.activeTo);
  }
  // After calling this, if `this.point` != null, the next range is a
  // point. Otherwise, it's a regular range, covered by `this.active`.
  next() {
    let t = this.to, e = this.point;
    this.point = null;
    let i = this.openStart < 0 ? [] : null;
    for (; ; ) {
      let n = this.minActive;
      if (n > -1 && (this.activeTo[n] - this.cursor.from || this.active[n].endSide - this.cursor.startSide) < 0) {
        if (this.activeTo[n] > t) {
          this.to = this.activeTo[n], this.endSide = this.active[n].endSide;
          break;
        }
        this.removeActive(n), i && Ai(i, n);
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
      for (let n = i.length - 1; n >= 0 && i[n] < t; n--)
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
function Br(s, t, e, i, n, r) {
  s.goto(t), e.goto(i);
  let o = i + n, l = i, a = i - t, h = !!r.boundChange;
  for (let f = !1; ; ) {
    let c = s.to + a - e.to, u = c || s.endSide - e.endSide, d = u < 0 ? s.to + a : e.to, p = Math.min(d, o);
    if (s.point || e.point ? (s.point && e.point && _s(s.point, e.point) && ys(s.activeForPoint(s.to), e.activeForPoint(e.to)) || r.comparePoint(l, p, s.point, e.point), f = !1) : (f && r.boundChange(l), p > l && !ys(s.active, e.active) && r.compareRange(l, p, s.active, e.active), h && p < o && (c || s.openEnd(d) != e.openEnd(d)) && (f = !0)), d > o)
      break;
    l = d, u <= 0 && s.next(), u >= 0 && e.next();
  }
}
function ys(s, t) {
  if (s.length != t.length)
    return !1;
  for (let e = 0; e < s.length; e++)
    if (s[e] != t[e] && !_s(s[e], t[e]))
      return !1;
  return !0;
}
function Ai(s, t) {
  for (let e = t, i = s.length - 1; e < i; e++)
    s[e] = s[e + 1];
  s.pop();
}
function Ti(s, t, e) {
  for (let i = s.length - 1; i >= t; i--)
    s[i + 1] = s[i];
  s[t] = e;
}
function Lr(s, t) {
  let e = -1, i = 1e9;
  for (let n = 0; n < t.length; n++)
    (t[n] - i || s[n].endSide - s[e].endSide) < 0 && (e = n, i = t[n]);
  return e;
}
function Tn(s, t, e = s.length) {
  let i = 0;
  for (let n = 0; n < e && n < s.length; )
    s.charCodeAt(n) == 9 ? (i += t - i % t, n++) : (i++, n = nt(s, n));
  return i;
}
function Mh(s, t, e, i) {
  for (let n = 0, r = 0; ; ) {
    if (r >= t)
      return n;
    if (n == s.length)
      break;
    r += s.charCodeAt(n) == 9 ? e - r % e : 1, n = nt(s, n);
  }
  return s.length;
}
const bs = "ͼ", Er = typeof Symbol > "u" ? "__" + bs : Symbol.for(bs), ws = typeof Symbol > "u" ? "__styleSet" + Math.floor(Math.random() * 1e8) : Symbol("styleSet"), Ir = typeof globalThis < "u" ? globalThis : typeof window < "u" ? window : {};
class Ne {
  // :: (Object<Style>, ?{finish: ?(string) → string})
  // Create a style module from the given spec.
  //
  // When `finish` is given, it is called on regular (non-`@`)
  // selectors (after `&` expansion) to compute the final selector.
  constructor(t, e) {
    this.rules = [];
    let { finish: i } = e || {};
    function n(o) {
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
          r(n(d), p, f, u);
        } else p != null && f.push(d.replace(/_.*/, "").replace(/[A-Z]/g, (g) => "-" + g.toLowerCase()) + ": " + p + ";");
      }
      (f.length || u) && a.push((i && !c && !h ? o.map(i) : o).join(", ") + " {" + f.join(" ") + "}");
    }
    for (let o in t) r(n(o), t[o], this.rules);
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
    let t = Ir[Er] || 1;
    return Ir[Er] = t + 1, bs + t.toString(36);
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
    let n = t[ws], r = i && i.nonce;
    n ? r && n.setNonce(r) : n = new Dh(t, r), n.mount(Array.isArray(e) ? e : [e], t);
  }
}
let Nr = /* @__PURE__ */ new Map();
class Dh {
  constructor(t, e) {
    let i = t.ownerDocument || t, n = i.defaultView;
    if (!t.head && t.adoptedStyleSheets && n.CSSStyleSheet) {
      let r = Nr.get(i);
      if (r) return t[ws] = r;
      this.sheet = new n.CSSStyleSheet(), Nr.set(i, this);
    } else
      this.styleTag = i.createElement("style"), e && this.styleTag.setAttribute("nonce", e);
    this.modules = [], t[ws] = this;
  }
  mount(t, e) {
    let i = this.sheet, n = 0, r = 0;
    for (let o = 0; o < t.length; o++) {
      let l = t[o], a = this.modules.indexOf(l);
      if (a < r && a > -1 && (this.modules.splice(a, 1), r--, a = -1), a == -1) {
        if (this.modules.splice(r++, 0, l), i) for (let h = 0; h < l.rules.length; h++)
          i.insertRule(l.rules[h], n++);
      } else {
        for (; r < a; ) n += this.modules[r++].rules.length;
        n += l.rules.length, r++;
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
var re = {
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
}, fi = {
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
}, Rh = typeof navigator < "u" && /Mac/.test(navigator.platform), Bh = typeof navigator < "u" && /MSIE \d|Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(navigator.userAgent);
for (var et = 0; et < 10; et++) re[48 + et] = re[96 + et] = String(et);
for (var et = 1; et <= 24; et++) re[et + 111] = "F" + et;
for (var et = 65; et <= 90; et++)
  re[et] = String.fromCharCode(et + 32), fi[et] = String.fromCharCode(et);
for (var zn in re) fi.hasOwnProperty(zn) || (fi[zn] = re[zn]);
function Lh(s) {
  var t = Rh && s.metaKey && s.shiftKey && !s.ctrlKey && !s.altKey || Bh && s.shiftKey && s.key && s.key.length == 1 || s.key == "Unidentified", e = !t && s.key || (s.shiftKey ? fi : re)[s.keyCode] || s.key || "Unidentified";
  return e == "Esc" && (e = "Escape"), e == "Del" && (e = "Delete"), e == "Left" && (e = "ArrowLeft"), e == "Up" && (e = "ArrowUp"), e == "Right" && (e = "ArrowRight"), e == "Down" && (e = "ArrowDown"), e;
}
function Vt() {
  var s = arguments[0];
  typeof s == "string" && (s = document.createElement(s));
  var t = 1, e = arguments[1];
  if (e && typeof e == "object" && e.nodeType == null && !Array.isArray(e)) {
    for (var i in e) if (Object.prototype.hasOwnProperty.call(e, i)) {
      var n = e[i];
      typeof n == "string" ? s.setAttribute(i, n) : n != null && (s[i] = n);
    }
    t++;
  }
  for (; t < arguments.length; t++) yl(s, arguments[t]);
  return s;
}
function yl(s, t) {
  if (typeof t == "string")
    s.appendChild(document.createTextNode(t));
  else if (t != null) if (t.nodeType != null)
    s.appendChild(t);
  else if (Array.isArray(t))
    for (var e = 0; e < t.length; e++) yl(s, t[e]);
  else
    throw new RangeError("Unsupported child node: " + t);
}
let rt = typeof navigator < "u" ? navigator : { userAgent: "", vendor: "", platform: "" }, xs = typeof document < "u" ? document : { documentElement: { style: {} } };
const ks = /* @__PURE__ */ /Edge\/(\d+)/.exec(rt.userAgent), bl = /* @__PURE__ */ /MSIE \d/.test(rt.userAgent), Ss = /* @__PURE__ */ /Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(rt.userAgent), Pn = !!(bl || Ss || ks), Vr = !Pn && /* @__PURE__ */ /gecko\/(\d+)/i.test(rt.userAgent), Qn = !Pn && /* @__PURE__ */ /Chrome\/(\d+)/.exec(rt.userAgent), Eh = "webkitFontSmoothing" in xs.documentElement.style, vs = !Pn && /* @__PURE__ */ /Apple Computer/.test(rt.vendor), Wr = vs && (/* @__PURE__ */ /Mobile\/\w+/.test(rt.userAgent) || rt.maxTouchPoints > 2);
var S = {
  mac: Wr || /* @__PURE__ */ /Mac/.test(rt.platform),
  windows: /* @__PURE__ */ /Win/.test(rt.platform),
  linux: /* @__PURE__ */ /Linux|X11/.test(rt.platform),
  ie: Pn,
  ie_version: bl ? xs.documentMode || 6 : Ss ? +Ss[1] : ks ? +ks[1] : 0,
  gecko: Vr,
  gecko_version: Vr ? +(/* @__PURE__ */ /Firefox\/(\d+)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  chrome: !!Qn,
  chrome_version: Qn ? +Qn[1] : 0,
  ios: Wr,
  android: /* @__PURE__ */ /Android\b/.test(rt.userAgent),
  webkit_version: Eh ? +(/* @__PURE__ */ /\bAppleWebKit\/(\d+)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  safari: vs,
  safari_version: vs ? +(/* @__PURE__ */ /\bVersion\/(\d+(\.\d+)?)/.exec(rt.userAgent) || [0, 0])[1] : 0,
  tabSize: xs.documentElement.style.tabSize != null ? "tab-size" : "-moz-tab-size"
};
function Gs(s, t) {
  for (let e in s)
    e == "class" && t.class ? t.class += " " + s.class : e == "style" && t.style ? t.style += ";" + s.style : t[e] = s[e];
  return t;
}
const nn = /* @__PURE__ */ Object.create(null);
function Zs(s, t, e) {
  if (s == t)
    return !0;
  s || (s = nn), t || (t = nn);
  let i = Object.keys(s), n = Object.keys(t);
  if (i.length - 0 != n.length - 0)
    return !1;
  for (let r of i)
    if (r != e && (n.indexOf(r) == -1 || s[r] !== t[r]))
      return !1;
  return !0;
}
function Ih(s, t) {
  for (let e = s.attributes.length - 1; e >= 0; e--) {
    let i = s.attributes[e].name;
    t[i] == null && s.removeAttribute(i);
  }
  for (let e in t) {
    let i = t[e];
    e == "style" ? s.style.cssText = i : s.getAttribute(e) != i && s.setAttribute(e, i);
  }
}
function Hr(s, t, e) {
  let i = !1;
  if (t)
    for (let n in t)
      e && n in e || (i = !0, n == "style" ? s.style.cssText = "" : s.removeAttribute(n));
  if (e)
    for (let n in e)
      t && t[n] == e[n] || (i = !0, n == "style" ? s.style.cssText = e[n] : s.setAttribute(n, e[n]));
  return i;
}
function Nh(s) {
  let t = /* @__PURE__ */ Object.create(null);
  for (let e = 0; e < s.attributes.length; e++) {
    let i = s.attributes[e];
    t[i.name] = i.value;
  }
  return t;
}
class bi {
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
var kt = /* @__PURE__ */ (function(s) {
  return s[s.Text = 0] = "Text", s[s.WidgetBefore = 1] = "WidgetBefore", s[s.WidgetAfter = 2] = "WidgetAfter", s[s.WidgetRange = 3] = "WidgetRange", s;
})(kt || (kt = {}));
class q extends se {
  constructor(t, e, i, n) {
    super(), this.startSide = t, this.endSide = e, this.widget = i, this.spec = n;
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
    return new wi(t);
  }
  /**
  Create a widget decoration, which displays a DOM element at the
  given position.
  */
  static widget(t) {
    let e = Math.max(-1e4, Math.min(1e4, t.side || 0)), i = !!t.block;
    return e += i && !t.inlineOrder ? e > 0 ? 3e8 : -4e8 : e > 0 ? 1e8 : -1e8, new xe(t, e, e, i, t.widget || null, !1);
  }
  /**
  Create a replace decoration which replaces the given range with
  a widget, or simply hides it.
  */
  static replace(t) {
    let e = !!t.block, i, n;
    if (t.isBlockGap)
      i = -5e8, n = 4e8;
    else {
      let { start: r, end: o } = wl(t, e);
      i = (r ? e ? -3e8 : -1 : 5e8) - 1, n = (o ? e ? 2e8 : 1 : -6e8) + 1;
    }
    return new xe(t, i, n, e, t.widget || null, !0);
  }
  /**
  Create a line decoration, which can add DOM attributes to the
  line starting at the given position.
  */
  static line(t) {
    return new xi(t);
  }
  /**
  Build a [`DecorationSet`](https://codemirror.net/6/docs/ref/#view.DecorationSet) from the given
  decorated range or ranges. If the ranges aren't already sorted,
  pass `true` for `sort` to make the library sort them for you.
  */
  static set(t, e = !1) {
    return V.of(t, e);
  }
  /**
  @internal
  */
  hasHeight() {
    return this.widget ? this.widget.estimatedHeight > -1 : !1;
  }
}
q.none = V.empty;
class wi extends q {
  constructor(t) {
    let { start: e, end: i } = wl(t);
    super(e ? -1 : 5e8, i ? 1 : -6e8, null, t), this.tagName = t.tagName || "span", this.attrs = t.class && t.attributes ? Gs(t.attributes, { class: t.class }) : t.class ? { class: t.class } : t.attributes || nn;
  }
  eq(t) {
    return this == t || t instanceof wi && this.tagName == t.tagName && Zs(this.attrs, t.attrs);
  }
  range(t, e = t) {
    if (t >= e)
      throw new RangeError("Mark decorations may not be empty");
    return super.range(t, e);
  }
}
wi.prototype.point = !1;
class xi extends q {
  constructor(t) {
    super(-2e8, -2e8, null, t);
  }
  eq(t) {
    return t instanceof xi && this.spec.class == t.spec.class && Zs(this.spec.attributes, t.spec.attributes);
  }
  range(t, e = t) {
    if (e != t)
      throw new RangeError("Line decoration ranges must be zero-length");
    return super.range(t, e);
  }
}
xi.prototype.mapMode = at.TrackBefore;
xi.prototype.point = !0;
class xe extends q {
  constructor(t, e, i, n, r, o) {
    super(e, i, r, t), this.block = n, this.isReplace = o, this.mapMode = n ? e <= 0 ? at.TrackBefore : at.TrackAfter : at.TrackDel;
  }
  // Only relevant when this.block == true
  get type() {
    return this.startSide != this.endSide ? kt.WidgetRange : this.startSide <= 0 ? kt.WidgetBefore : kt.WidgetAfter;
  }
  get heightRelevant() {
    return this.block || !!this.widget && (this.widget.estimatedHeight >= 5 || this.widget.lineBreaks > 0);
  }
  eq(t) {
    return t instanceof xe && Vh(this.widget, t.widget) && this.block == t.block && this.startSide == t.startSide && this.endSide == t.endSide;
  }
  range(t, e = t) {
    if (this.isReplace && (t > e || t == e && this.startSide > 0 && this.endSide <= 0))
      throw new RangeError("Invalid range for replacement decoration");
    if (!this.isReplace && e != t)
      throw new RangeError("Widget decorations can only have zero-length ranges");
    return super.range(t, e);
  }
}
xe.prototype.point = !0;
function wl(s, t = !1) {
  let { inclusiveStart: e, inclusiveEnd: i } = s;
  return e == null && (e = s.inclusive), i == null && (i = s.inclusive), { start: e ?? t, end: i ?? t };
}
function Vh(s, t) {
  return s == t || !!(s && t && s.compare(t));
}
function De(s, t, e, i = 0) {
  let n = e.length - 1;
  n >= 0 && e[n] + i >= s ? e[n] = Math.max(e[n], t) : e.push(s, t);
}
class ci extends se {
  constructor(t, e) {
    super(), this.tagName = t, this.attributes = e;
  }
  eq(t) {
    return t == this || t instanceof ci && this.tagName == t.tagName && Zs(this.attributes, t.attributes);
  }
  /**
  Create a block wrapper object with the given tag name and
  attributes.
  */
  static create(t) {
    return new ci(t.tagName, t.attributes || nn);
  }
  /**
  Create a range set from the given block wrapper ranges.
  */
  static set(t, e = !1) {
    return V.of(t, e);
  }
}
ci.prototype.startSide = ci.prototype.endSide = -1;
function ui(s) {
  let t;
  return s.nodeType == 11 ? t = s.getSelection ? s : s.ownerDocument : t = s, t.getSelection();
}
function Os(s, t) {
  return t ? s == t || s.contains(t.nodeType != 1 ? t.parentNode : t) : !1;
}
function ni(s, t) {
  if (!t.anchorNode)
    return !1;
  try {
    return Os(s, t.anchorNode);
  } catch {
    return !1;
  }
}
function Yi(s) {
  return s.nodeType == 3 ? di(s, 0, s.nodeValue.length).getClientRects() : s.nodeType == 1 ? s.getClientRects() : [];
}
function si(s, t, e, i) {
  return e ? Fr(s, t, e, i, -1) || Fr(s, t, e, i, 1) : !1;
}
function oe(s) {
  for (var t = 0; ; t++)
    if (s = s.previousSibling, !s)
      return t;
}
function sn(s) {
  return s.nodeType == 1 && /^(DIV|P|LI|UL|OL|BLOCKQUOTE|DD|DT|H\d|SECTION|PRE)$/.test(s.nodeName);
}
function Fr(s, t, e, i, n) {
  for (; ; ) {
    if (s == e && t == i)
      return !0;
    if (t == (n < 0 ? 0 : _t(s))) {
      if (s.nodeName == "DIV")
        return !1;
      let r = s.parentNode;
      if (!r || r.nodeType != 1)
        return !1;
      t = oe(s) + (n < 0 ? 0 : 1), s = r;
    } else if (s.nodeType == 1) {
      if (s = s.childNodes[t + (n < 0 ? -1 : 0)], s.nodeType == 1 && s.contentEditable == "false")
        return !1;
      t = n < 0 ? _t(s) : 0;
    } else
      return !1;
  }
}
function _t(s) {
  return s.nodeType == 3 ? s.nodeValue.length : s.childNodes.length;
}
function rn(s, t) {
  let e = t ? s.left : s.right;
  return { left: e, right: e, top: s.top, bottom: s.bottom };
}
function Wh(s) {
  let t = s.visualViewport;
  return t ? {
    left: 0,
    right: t.width,
    top: 0,
    bottom: t.height
  } : {
    left: 0,
    right: s.innerWidth,
    top: 0,
    bottom: s.innerHeight
  };
}
function xl(s, t) {
  let e = t.width / s.offsetWidth, i = t.height / s.offsetHeight;
  return (e > 0.995 && e < 1.005 || !isFinite(e) || Math.abs(t.width - s.offsetWidth) < 1) && (e = 1), (i > 0.995 && i < 1.005 || !isFinite(i) || Math.abs(t.height - s.offsetHeight) < 1) && (i = 1), { scaleX: e, scaleY: i };
}
function Hh(s, t, e, i, n, r, o, l) {
  let a = s.ownerDocument, h = a.defaultView || window;
  for (let f = s, c = !1; f && !c; )
    if (f.nodeType == 1) {
      let u, d = f == a.body, p = 1, g = 1;
      if (d)
        u = Wh(h);
      else {
        if (/^(fixed|sticky)$/.test(getComputedStyle(f).position) && (c = !0), f.scrollHeight <= f.clientHeight && f.scrollWidth <= f.clientWidth) {
          f = f.assignedSlot || f.parentNode;
          continue;
        }
        let w = f.getBoundingClientRect();
        ({ scaleX: p, scaleY: g } = xl(f, w)), u = {
          left: w.left,
          right: w.left + f.clientWidth * p,
          top: w.top,
          bottom: w.top + f.clientHeight * g
        };
      }
      let m = 0, y = 0;
      if (n == "nearest")
        t.top < u.top ? (y = t.top - (u.top + o), e > 0 && t.bottom > u.bottom + y && (y = t.bottom - u.bottom + o)) : t.bottom > u.bottom && (y = t.bottom - u.bottom + o, e < 0 && t.top - y < u.top && (y = t.top - (u.top + o)));
      else {
        let w = t.bottom - t.top, b = u.bottom - u.top;
        y = (n == "center" && w <= b ? t.top + w / 2 - b / 2 : n == "start" || n == "center" && e < 0 ? t.top - o : t.bottom - b + o) - u.top;
      }
      if (i == "nearest" ? t.left < u.left ? (m = t.left - (u.left + r), e > 0 && t.right > u.right + m && (m = t.right - u.right + r)) : t.right > u.right && (m = t.right - u.right + r, e < 0 && t.left < u.left + m && (m = t.left - (u.left + r))) : m = (i == "center" ? t.left + (t.right - t.left) / 2 - (u.right - u.left) / 2 : i == "start" == l ? t.left - r : t.right - (u.right - u.left) + r) - u.left, m || y)
        if (d)
          h.scrollBy(m, y);
        else {
          let w = 0, b = 0;
          if (y) {
            let O = f.scrollTop;
            f.scrollTop += y / g, b = (f.scrollTop - O) * g;
          }
          if (m) {
            let O = f.scrollLeft;
            f.scrollLeft += m / p, w = (f.scrollLeft - O) * p;
          }
          t = {
            left: t.left - w,
            top: t.top - b,
            right: t.right - w,
            bottom: t.bottom - b
          }, w && Math.abs(w - m) < 1 && (i = "nearest"), b && Math.abs(b - y) < 1 && (n = "nearest");
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
function kl(s, t = !0) {
  let e = s.ownerDocument, i = null, n = null;
  for (let r = s.parentNode; r && !(r == e.body || (!t || i) && n); )
    if (r.nodeType == 1)
      !n && r.scrollHeight > r.clientHeight && (n = r), t && !i && r.scrollWidth > r.clientWidth && (i = r), r = r.assignedSlot || r.parentNode;
    else if (r.nodeType == 11)
      r = r.host;
    else
      break;
  return { x: i, y: n };
}
class Fh {
  constructor() {
    this.anchorNode = null, this.anchorOffset = 0, this.focusNode = null, this.focusOffset = 0;
  }
  eq(t) {
    return this.anchorNode == t.anchorNode && this.anchorOffset == t.anchorOffset && this.focusNode == t.focusNode && this.focusOffset == t.focusOffset;
  }
  setRange(t) {
    let { anchorNode: e, focusNode: i } = t;
    this.set(e, Math.min(t.anchorOffset, e ? _t(e) : 0), i, Math.min(t.focusOffset, i ? _t(i) : 0));
  }
  set(t, e, i, n) {
    this.anchorNode = t, this.anchorOffset = e, this.focusNode = i, this.focusOffset = n;
  }
}
let de = null;
S.safari && S.safari_version >= 26 && (de = !1);
function Sl(s) {
  if (s.setActive)
    return s.setActive();
  if (de)
    return s.focus(de);
  let t = [];
  for (let e = s; e && (t.push(e, e.scrollTop, e.scrollLeft), e != e.ownerDocument); e = e.parentNode)
    ;
  if (s.focus(de == null ? {
    get preventScroll() {
      return de = { preventScroll: !0 }, !0;
    }
  } : void 0), !de) {
    de = !1;
    for (let e = 0; e < t.length; ) {
      let i = t[e++], n = t[e++], r = t[e++];
      i.scrollTop != n && (i.scrollTop = n), i.scrollLeft != r && (i.scrollLeft = r);
    }
  }
}
let zr;
function di(s, t, e = t) {
  let i = zr || (zr = document.createRange());
  return i.setEnd(s, e), i.setStart(s, t), i;
}
function Re(s, t, e, i) {
  let n = { key: t, code: t, keyCode: e, which: e, cancelable: !0 };
  i && ({ altKey: n.altKey, ctrlKey: n.ctrlKey, shiftKey: n.shiftKey, metaKey: n.metaKey } = i);
  let r = new KeyboardEvent("keydown", n);
  r.synthetic = !0, s.dispatchEvent(r);
  let o = new KeyboardEvent("keyup", n);
  return o.synthetic = !0, s.dispatchEvent(o), r.defaultPrevented || o.defaultPrevented;
}
function zh(s) {
  for (; s; ) {
    if (s && (s.nodeType == 9 || s.nodeType == 11 && s.host))
      return s;
    s = s.assignedSlot || s.parentNode;
  }
  return null;
}
function Qh(s, t) {
  let e = t.focusNode, i = t.focusOffset;
  if (!e || t.anchorNode != e || t.anchorOffset != i)
    return !1;
  for (i = Math.min(i, _t(e)); ; )
    if (i) {
      if (e.nodeType != 1)
        return !1;
      let n = e.childNodes[i - 1];
      n.contentEditable == "false" ? i-- : (e = n, i = _t(e));
    } else {
      if (e == s)
        return !0;
      i = oe(e), e = e.parentNode;
    }
}
function vl(s) {
  return s instanceof Window ? s.pageYOffset > Math.max(0, s.document.documentElement.scrollHeight - s.innerHeight - 4) : s.scrollTop > Math.max(1, s.scrollHeight - s.clientHeight - 4);
}
function Ol(s, t) {
  for (let e = s, i = t; ; ) {
    if (e.nodeType == 3 && i > 0)
      return { node: e, offset: i };
    if (e.nodeType == 1 && i > 0) {
      if (e.contentEditable == "false")
        return null;
      e = e.childNodes[i - 1], i = _t(e);
    } else if (e.parentNode && !sn(e))
      i = oe(e), e = e.parentNode;
    else
      return null;
  }
}
function Cl(s, t) {
  for (let e = s, i = t; ; ) {
    if (e.nodeType == 3 && i < e.nodeValue.length)
      return { node: e, offset: i };
    if (e.nodeType == 1 && i < e.childNodes.length) {
      if (e.contentEditable == "false")
        return null;
      e = e.childNodes[i], i = 0;
    } else if (e.parentNode && !sn(e))
      i = oe(e) + 1, e = e.parentNode;
    else
      return null;
  }
}
class Ct {
  constructor(t, e, i = !0) {
    this.node = t, this.offset = e, this.precise = i;
  }
  static before(t, e) {
    return new Ct(t.parentNode, oe(t), e);
  }
  static after(t, e) {
    return new Ct(t.parentNode, oe(t) + 1, e);
  }
}
var $ = /* @__PURE__ */ (function(s) {
  return s[s.LTR = 0] = "LTR", s[s.RTL = 1] = "RTL", s;
})($ || ($ = {}));
const ke = $.LTR, Js = $.RTL;
function Al(s) {
  let t = [];
  for (let e = 0; e < s.length; e++)
    t.push(1 << +s[e]);
  return t;
}
const $h = /* @__PURE__ */ Al("88888888888888888888888888888888888666888888787833333333337888888000000000000000000000000008888880000000000000000000000000088888888888888888888888888888888888887866668888088888663380888308888800000000000000000000000800000000000000000000000000000008"), jh = /* @__PURE__ */ Al("4444448826627288999999999992222222222222222222222222222222222222222222222229999999999999999999994444444444644222822222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222999999949999999229989999223333333333"), Cs = /* @__PURE__ */ Object.create(null), Bt = [];
for (let s of ["()", "[]", "{}"]) {
  let t = /* @__PURE__ */ s.charCodeAt(0), e = /* @__PURE__ */ s.charCodeAt(1);
  Cs[t] = e, Cs[e] = -t;
}
function Tl(s) {
  return s <= 247 ? $h[s] : 1424 <= s && s <= 1524 ? 2 : 1536 <= s && s <= 1785 ? jh[s - 1536] : 1774 <= s && s <= 2220 ? 4 : 8192 <= s && s <= 8204 ? 256 : 64336 <= s && s <= 65023 ? 4 : 1;
}
const Xh = /[\u0590-\u05f4\u0600-\u06ff\u0700-\u08ac\ufb50-\ufdff]/;
class Ht {
  /**
  The direction of this span.
  */
  get dir() {
    return this.level % 2 ? Js : ke;
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
  static find(t, e, i, n) {
    let r = -1;
    for (let o = 0; o < t.length; o++) {
      let l = t[o];
      if (l.from <= e && l.to >= e) {
        if (l.level == i)
          return o;
        (r < 0 || (n != 0 ? n < 0 ? l.from < e : l.to > e : t[r].level > l.level)) && (r = o);
      }
    }
    if (r < 0)
      throw new RangeError("Index out of range");
    return r;
  }
}
function Pl(s, t) {
  if (s.length != t.length)
    return !1;
  for (let e = 0; e < s.length; e++) {
    let i = s[e], n = t[e];
    if (i.from != n.from || i.to != n.to || i.direction != n.direction || !Pl(i.inner, n.inner))
      return !1;
  }
  return !0;
}
const W = [];
function qh(s, t, e, i, n) {
  for (let r = 0; r <= i.length; r++) {
    let o = r ? i[r - 1].to : t, l = r < i.length ? i[r].from : e, a = r ? 256 : n;
    for (let h = o, f = a, c = a; h < l; h++) {
      let u = Tl(s.charCodeAt(h));
      u == 512 ? u = f : u == 8 && c == 4 && (u = 16), W[h] = u == 4 ? 2 : u, u & 7 && (c = u), f = u;
    }
    for (let h = o, f = a, c = a; h < l; h++) {
      let u = W[h];
      if (u == 128)
        h < l - 1 && f == W[h + 1] && f & 24 ? u = W[h] = f : W[h] = 256;
      else if (u == 64) {
        let d = h + 1;
        for (; d < l && W[d] == 64; )
          d++;
        let p = h && f == 8 || d < e && W[d] == 8 ? c == 1 ? 1 : 8 : 256;
        for (let g = h; g < d; g++)
          W[g] = p;
        h = d - 1;
      } else u == 8 && c == 1 && (W[h] = 1);
      f = u, u & 7 && (c = u);
    }
  }
}
function Uh(s, t, e, i, n) {
  let r = n == 1 ? 2 : 1;
  for (let o = 0, l = 0, a = 0; o <= i.length; o++) {
    let h = o ? i[o - 1].to : t, f = o < i.length ? i[o].from : e;
    for (let c = h, u, d, p; c < f; c++)
      if (d = Cs[u = s.charCodeAt(c)])
        if (d < 0) {
          for (let g = l - 3; g >= 0; g -= 3)
            if (Bt[g + 1] == -d) {
              let m = Bt[g + 2], y = m & 2 ? n : m & 4 ? m & 1 ? r : n : 0;
              y && (W[c] = W[Bt[g]] = y), l = g;
              break;
            }
        } else {
          if (Bt.length == 189)
            break;
          Bt[l++] = c, Bt[l++] = u, Bt[l++] = a;
        }
      else if ((p = W[c]) == 2 || p == 1) {
        let g = p == n;
        a = g ? 0 : 1;
        for (let m = l - 3; m >= 0; m -= 3) {
          let y = Bt[m + 2];
          if (y & 2)
            break;
          if (g)
            Bt[m + 2] |= 2;
          else {
            if (y & 4)
              break;
            Bt[m + 2] |= 4;
          }
        }
      }
  }
}
function Kh(s, t, e, i) {
  for (let n = 0, r = i; n <= e.length; n++) {
    let o = n ? e[n - 1].to : s, l = n < e.length ? e[n].from : t;
    for (let a = o; a < l; ) {
      let h = W[a];
      if (h == 256) {
        let f = a + 1;
        for (; ; )
          if (f == l) {
            if (n == e.length)
              break;
            f = e[n++].to, l = n < e.length ? e[n].from : t;
          } else if (W[f] == 256)
            f++;
          else
            break;
        let c = r == 1, u = (f < t ? W[f] : i) == 1, d = c == u ? c ? 1 : 2 : i;
        for (let p = f, g = n, m = g ? e[g - 1].to : s; p > a; )
          p == m && (p = e[--g].from, m = g ? e[g - 1].to : s), W[--p] = d;
        a = f;
      } else
        r = h, a++;
    }
  }
}
function As(s, t, e, i, n, r, o) {
  let l = i % 2 ? 2 : 1;
  if (i % 2 == n % 2)
    for (let a = t, h = 0; a < e; ) {
      let f = !0, c = !1;
      if (h == r.length || a < r[h].from) {
        let g = W[a];
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
                if (W[m] == l)
                  break t;
                break;
              }
            }
          if (h++, u)
            u.push(g);
          else {
            g.from > a && o.push(new Ht(a, g.from, d));
            let m = g.direction == ke != !(d % 2);
            Ts(s, m ? i + 1 : i, n, g.inner, g.from, g.to, o), a = g.to;
          }
          p = g.to;
        } else {
          if (p == e || (f ? W[p] != l : W[p] == l))
            break;
          p++;
        }
      u ? As(s, a, p, i + 1, n, u, o) : a < p && o.push(new Ht(a, p, d)), a = p;
    }
  else
    for (let a = e, h = r.length; a > t; ) {
      let f = !0, c = !1;
      if (!h || a > r[h - 1].to) {
        let g = W[a - 1];
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
                if (W[m - 1] == l)
                  break t;
                break;
              }
            }
          if (u)
            u.push(g);
          else {
            g.to < a && o.push(new Ht(g.to, a, d));
            let m = g.direction == ke != !(d % 2);
            Ts(s, m ? i + 1 : i, n, g.inner, g.from, g.to, o), a = g.from;
          }
          p = g.from;
        } else {
          if (p == t || (f ? W[p - 1] != l : W[p - 1] == l))
            break;
          p--;
        }
      u ? As(s, p, a, i + 1, n, u, o) : p < a && o.push(new Ht(p, a, d)), a = p;
    }
}
function Ts(s, t, e, i, n, r, o) {
  let l = t % 2 ? 2 : 1;
  qh(s, n, r, i, l), Uh(s, n, r, i, l), Kh(n, r, i, l), As(s, n, r, t, e, i, o);
}
function _h(s, t, e) {
  if (!s)
    return [new Ht(0, 0, t == Js ? 1 : 0)];
  if (t == ke && !e.length && !Xh.test(s))
    return Ml(s.length);
  if (e.length)
    for (; s.length > W.length; )
      W[W.length] = 256;
  let i = [], n = t == ke ? 0 : 1;
  return Ts(s, n, n, e, 0, s.length, i), i;
}
function Ml(s) {
  return [new Ht(0, s, 0)];
}
let Dl = "";
function Yh(s, t, e, i, n) {
  var r;
  let o = i.head - s.from, l = Ht.find(t, o, (r = i.bidiLevel) !== null && r !== void 0 ? r : -1, i.assoc), a = t[l], h = a.side(n, e);
  if (o == h) {
    let u = l += n ? 1 : -1;
    if (u < 0 || u >= t.length)
      return null;
    a = t[l = u], o = a.side(!n, e), h = a.side(n, e);
  }
  let f = nt(s.text, o, a.forward(n, e));
  (f < a.from || f > a.to) && (f = h), Dl = s.text.slice(Math.min(o, f), Math.max(o, f));
  let c = l == (n ? t.length - 1 : 0) ? null : t[l + (n ? 1 : -1)];
  return c && f == h && c.level + (n ? 0 : 1) < a.level ? x.cursor(c.side(!n, e) + s.from, c.forward(n, e) ? 1 : -1, c.level) : x.cursor(f + s.from, a.forward(n, e) ? -1 : 1, a.level);
}
function Gh(s, t, e) {
  for (let i = t; i < e; i++) {
    let n = Tl(s.charCodeAt(i));
    if (n == 1)
      return ke;
    if (n == 2 || n == 4)
      return Js;
  }
  return ke;
}
const Rl = /* @__PURE__ */ P.define(), Bl = /* @__PURE__ */ P.define(), Ll = /* @__PURE__ */ P.define(), El = /* @__PURE__ */ P.define(), Ps = /* @__PURE__ */ P.define(), Il = /* @__PURE__ */ P.define(), Nl = /* @__PURE__ */ P.define(), tr = /* @__PURE__ */ P.define(), er = /* @__PURE__ */ P.define(), Vl = /* @__PURE__ */ P.define({
  combine: (s) => s.some((t) => t)
}), Zh = /* @__PURE__ */ P.define({
  combine: (s) => s.some((t) => t)
}), Wl = /* @__PURE__ */ P.define();
class Be {
  constructor(t, e = "nearest", i = "nearest", n = 5, r = 5, o = !1) {
    this.range = t, this.y = e, this.x = i, this.yMargin = n, this.xMargin = r, this.isSnapshot = o;
  }
  map(t) {
    return t.empty ? this : new Be(this.range.map(t), this.y, this.x, this.yMargin, this.xMargin, this.isSnapshot);
  }
  clip(t) {
    return this.range.to <= t.doc.length ? this : new Be(x.cursor(t.doc.length), this.y, this.x, this.yMargin, this.xMargin, this.isSnapshot);
  }
}
const Pi = /* @__PURE__ */ I.define({ map: (s, t) => s.map(t) }), Hl = /* @__PURE__ */ I.define();
function ft(s, t, e) {
  let i = s.facet(El);
  i.length ? i[0](t) : window.onerror && window.onerror(String(t), e, void 0, void 0, t) || (e ? console.error(e + ":", t) : console.error(t));
}
const qt = /* @__PURE__ */ P.define({ combine: (s) => s.length ? s[0] : !0 });
let Jh = 0;
const Ae = /* @__PURE__ */ P.define({
  combine(s) {
    return s.filter((t, e) => {
      for (let i = 0; i < e; i++)
        if (s[i].plugin == t.plugin)
          return !1;
      return !0;
    });
  }
});
class $t {
  constructor(t, e, i, n, r) {
    this.id = t, this.create = e, this.domEventHandlers = i, this.domEventObservers = n, this.baseExtensions = r(this), this.extension = this.baseExtensions.concat(Ae.of({ plugin: this, arg: void 0 }));
  }
  /**
  Create an extension for this plugin with the given argument.
  */
  of(t) {
    return this.baseExtensions.concat(Ae.of({ plugin: this, arg: t }));
  }
  /**
  Define a plugin from a constructor function that creates the
  plugin's value, given an editor view.
  */
  static define(t, e) {
    const { eventHandlers: i, eventObservers: n, provide: r, decorations: o } = e || {};
    return new $t(Jh++, t, i, n, (l) => {
      let a = [];
      return o && a.push(Mn.of((h) => {
        let f = h.plugin(l);
        return f ? o(f) : q.none;
      })), r && a.push(r(l)), a;
    });
  }
  /**
  Create a plugin for a class whose constructor takes a single
  editor view as argument.
  */
  static fromClass(t, e) {
    return $t.define((i, n) => new t(i, n), e);
  }
}
class $n {
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
const Fl = /* @__PURE__ */ P.define(), ir = /* @__PURE__ */ P.define(), Mn = /* @__PURE__ */ P.define(), zl = /* @__PURE__ */ P.define(), nr = /* @__PURE__ */ P.define(), ki = /* @__PURE__ */ P.define(), Ql = /* @__PURE__ */ P.define();
function Qr(s, t) {
  let e = s.state.facet(Ql);
  if (!e.length)
    return e;
  let i = e.map((r) => r instanceof Function ? r(s) : r), n = [];
  return V.spans(i, t.from, t.to, {
    point() {
    },
    span(r, o, l, a) {
      let h = r - t.from, f = o - t.from, c = n;
      for (let u = l.length - 1; u >= 0; u--, a--) {
        let d = l[u].spec.bidiIsolate, p;
        if (d == null && (d = Gh(t.text, h, f)), a > 0 && c.length && (p = c[c.length - 1]).to == h && p.direction == d)
          p.to = f, c = p.inner;
        else {
          let g = { from: h, to: f, direction: d, inner: [] };
          c.push(g), c = g.inner;
        }
      }
    }
  }), n;
}
const $l = /* @__PURE__ */ P.define();
function sr(s) {
  let t = 0, e = 0, i = 0, n = 0;
  for (let r of s.state.facet($l)) {
    let o = r(s);
    o && (o.left != null && (t = Math.max(t, o.left)), o.right != null && (e = Math.max(e, o.right)), o.top != null && (i = Math.max(i, o.top)), o.bottom != null && (n = Math.max(n, o.bottom)));
  }
  return { left: t, right: e, top: i, bottom: n };
}
const Ze = /* @__PURE__ */ P.define();
class bt {
  constructor(t, e, i, n) {
    this.fromA = t, this.toA = e, this.fromB = i, this.toB = n;
  }
  join(t) {
    return new bt(Math.min(this.fromA, t.fromA), Math.max(this.toA, t.toA), Math.min(this.fromB, t.fromB), Math.max(this.toB, t.toB));
  }
  addToSet(t) {
    let e = t.length, i = this;
    for (; e > 0; e--) {
      let n = t[e - 1];
      if (!(n.fromA > i.toA)) {
        if (n.toA < i.fromA)
          break;
        i = i.join(n), t.splice(e - 1, 1);
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
    for (let n = 0, r = 0, o = 0; ; ) {
      let l = n < t.length ? t[n].fromB : 1e9, a = r < e.length ? e[r] : 1e9, h = Math.min(l, a);
      if (h == 1e9)
        break;
      let f = h + o, c = h, u = f;
      for (; ; )
        if (r < e.length && e[r] <= c) {
          let d = e[r + 1];
          r += 2, c = Math.max(c, d);
          for (let p = n; p < t.length && t[p].fromB <= c; p++)
            o = t[p].toA - t[p].toB;
          u = Math.max(u, d + o);
        } else if (n < t.length && t[n].fromB <= c) {
          let d = t[n++];
          c = Math.max(c, d.toB), u = Math.max(u, d.toA), o = d.toA - d.toB;
        } else
          break;
      i.push(new bt(f, u, h, c));
    }
    return i;
  }
}
class on {
  constructor(t, e, i) {
    this.view = t, this.state = e, this.transactions = i, this.flags = 0, this.startState = t.state, this.changes = Y.empty(this.startState.doc.length);
    for (let r of i)
      this.changes = this.changes.compose(r.changes);
    let n = [];
    this.changes.iterChangedRanges((r, o, l, a) => n.push(new bt(r, o, l, a))), this.changedRanges = n;
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new on(t, e, i);
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
const tf = [];
class U {
  constructor(t, e, i = 0) {
    this.dom = t, this.length = e, this.flags = i, this.parent = null, t.cmTile = this;
  }
  get breakAfter() {
    return this.flags & 1;
  }
  get children() {
    return tf;
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
      e && Ih(this.dom, e);
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
    for (let n of this.children) {
      if (n == t)
        return i;
      i += n.length + n.breakAfter;
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
    let i = oe(this.dom), n = this.length ? t > 0 : e > 0;
    return new Ct(this.parent.dom, i + (n ? 1 : 0), t == 0 || t == this.length);
  }
  markDirty(t) {
    this.flags &= -3, t && (this.flags |= 4), this.parent && this.parent.flags & 2 && this.parent.markDirty(!1);
  }
  get overrideDOMText() {
    return null;
  }
  get root() {
    for (let t = this; t; t = t.parent)
      if (t instanceof Rn)
        return t;
    return null;
  }
  static get(t) {
    return t.cmTile;
  }
}
class Dn extends U {
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
    let e = this.dom, i = null, n, r = (t == null ? void 0 : t.node) == e ? t : null, o = 0;
    for (let l of this.children) {
      if (l.sync(t), o += l.length + l.breakAfter, n = i ? i.nextSibling : e.firstChild, r && n != l.dom && (r.written = !0), l.dom.parentNode == e)
        for (; n && n != l.dom; )
          n = $r(n);
      else
        e.insertBefore(l.dom, n);
      i = l.dom;
    }
    for (n = i ? i.nextSibling : e.firstChild, r && n && (r.written = !0); n; )
      n = $r(n);
    this.length = o;
  }
}
function $r(s) {
  let t = s.nextSibling;
  return s.parentNode.removeChild(s), t;
}
class Rn extends Dn {
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
    for (let e = [], i = this, n = 0, r = 0; ; )
      if (n == i.children.length) {
        if (!e.length)
          return;
        i = i.parent, i.breakAfter && r++, n = e.pop();
      } else {
        let o = i.children[n++];
        if (o instanceof Kt)
          e.push(n), i = o, n = 0;
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
    let i, n = -1, r, o = -1;
    if (this.blockTiles((l, a) => {
      let h = a + l.length;
      if (t >= a && t <= h) {
        if (l.isWidget() && e >= -1 && e <= 1) {
          if (l.flags & 32)
            return !0;
          l.flags & 16 && (i = void 0);
        }
        (a < t || t == h && (e < -1 ? l.length : l.covers(1))) && (!i || !l.isWidget() && i.isWidget()) && (i = l, n = t - a), (h > t || t == a && (e > 1 ? l.length : l.covers(-1))) && (!r || !l.isWidget() && r.isWidget()) && (r = l, o = t - a);
      }
    }), !i && !r)
      throw new Error("No tile at position " + t);
    return i && e < 0 || !r ? { tile: i, offset: n } : { tile: r, offset: o };
  }
}
class Kt extends Dn {
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
    let i = new Kt(e || document.createElement(t.tagName), t);
    return e || (i.flags |= 4), i;
  }
}
class Ve extends Dn {
  constructor(t, e) {
    super(t), this.attrs = e;
  }
  isLine() {
    return !0;
  }
  static start(t, e, i) {
    let n = new Ve(e || document.createElement("div"), t);
    return (!e || !i) && (n.flags |= 4), n;
  }
  get domAttrs() {
    return this.attrs;
  }
  // Find the tile associated with a given position in this line.
  resolveInline(t, e, i) {
    let n = null, r = -1, o = null, l = -1;
    function a(f, c) {
      for (let u = 0, d = 0; u < f.children.length && d <= c; u++) {
        let p = f.children[u], g = d + p.length;
        g >= c && (p.isComposite() ? a(p, c - d) : (!o || o.isHidden && (e > 0 || i && nf(o, p))) && (g > c || p.flags & 32) ? (o = p, l = c - d) : (d < c || p.flags & 16 && !p.isHidden) && (n = p, r = c - d)), d = g;
      }
    }
    a(this, t);
    let h = (e < 0 ? n : o) || n || o;
    return h ? { tile: h, offset: h == n ? r : l } : null;
  }
  coordsIn(t, e) {
    let i = this.resolveInline(t, e, !0);
    return i ? i.tile.coordsIn(Math.max(0, i.offset), e) : ef(this);
  }
  domIn(t, e) {
    let i = this.resolveInline(t, e);
    if (i) {
      let { tile: n, offset: r } = i;
      if (this.dom.contains(n.dom))
        return n.isText() ? new Ct(n.dom, Math.min(n.dom.nodeValue.length, r)) : n.domPosFor(r, n.flags & 16 ? 1 : n.flags & 32 ? -1 : e);
      let o = i.tile.parent, l = !1;
      for (let a of o.children) {
        if (l)
          return new Ct(a.dom, 0);
        a == i.tile && (l = !0);
      }
    }
    return new Ct(this.dom, 0);
  }
}
function ef(s) {
  let t = s.dom.lastChild;
  if (!t)
    return s.dom.getBoundingClientRect();
  let e = Yi(t);
  return e[e.length - 1] || null;
}
function nf(s, t) {
  let e = s.coordsIn(0, 1), i = t.coordsIn(0, 1);
  return e && i && i.top < e.bottom;
}
class ht extends Dn {
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
class ye extends U {
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
    let n = t, r = t, o = 0;
    t == 0 && e < 0 || t == i && e >= 0 ? S.chrome || S.gecko || (t ? (n--, o = 1) : r < i && (r++, o = -1)) : e < 0 ? n-- : r < i && r++;
    let l = di(this.dom, n, r).getClientRects();
    if (!l.length)
      return null;
    let a = l[(o ? o < 0 : e >= 0) ? 0 : l.length - 1];
    return S.safari && !o && a.width == 0 && (a = Array.prototype.find.call(l, (h) => h.width) || a), o ? rn(a, o < 0) : a || null;
  }
  static of(t, e) {
    let i = new ye(e || document.createTextNode(t), t);
    return e || (i.flags |= 2), i;
  }
}
class Se extends U {
  constructor(t, e, i, n) {
    super(t, e, n), this.widget = i;
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
    let n = this.widget.coordsAt(this.dom, t, e);
    if (n)
      return n;
    if (i)
      return rn(this.dom.getBoundingClientRect(), this.length ? t == 0 : e <= 0);
    {
      let r = this.dom.getClientRects(), o = null;
      if (!r.length)
        return null;
      let l = this.flags & 16 ? !0 : this.flags & 32 ? !1 : t > 0;
      for (let a = l ? r.length - 1 : 0; o = r[a], !(t > 0 ? a == 0 : a == r.length - 1 || o.top < o.bottom); a += l ? -1 : 1)
        ;
      return rn(o, !l);
    }
  }
  get overrideDOMText() {
    if (!this.length)
      return E.empty;
    let { root: t } = this;
    if (!t)
      return E.empty;
    let e = this.posAtStart;
    return t.view.state.doc.slice(e, e + this.length);
  }
  destroy() {
    super.destroy(), this.widget.destroy(this.dom);
  }
  static of(t, e, i, n, r) {
    return r || (r = t.toDOM(e), t.editable || (r.contentEditable = "false")), new Se(r, i, t, n);
  }
}
class ln extends U {
  constructor(t) {
    let e = document.createElement("img");
    e.className = "cm-widgetBuffer", e.setAttribute("aria-hidden", "true"), super(e, 0, t);
  }
  get isHidden() {
    return !0;
  }
  get overrideDOMText() {
    return E.empty;
  }
  coordsIn(t) {
    return this.dom.getBoundingClientRect();
  }
}
class sf {
  constructor(t) {
    this.index = 0, this.beforeBreak = !1, this.parents = [], this.tile = t;
  }
  // Advance by the given distance. If side is -1, stop leaving or
  // entering tiles, or skipping zero-length tiles, once the distance
  // has been traversed. When side is 1, leave, enter, or skip
  // everything at the end position.
  advance(t, e, i) {
    let { tile: n, index: r, beforeBreak: o, parents: l } = this;
    for (; t || e > 0; )
      if (n.isComposite())
        if (o) {
          if (!t)
            break;
          i && i.break(), t--, o = !1;
        } else if (r == n.children.length) {
          if (!t && !l.length)
            break;
          i && i.leave(n), o = !!n.breakAfter, { tile: n, index: r } = l.pop(), r++;
        } else {
          let a = n.children[r], h = a.breakAfter;
          (e > 0 ? a.length <= t : a.length < t) && (!i || i.skip(a, 0, a.length) !== !1 || !a.isComposite) ? (o = !!h, r++, t -= a.length) : (l.push({ tile: n, index: r }), n = a, r = 0, i && a.isComposite() && i.enter(a));
        }
      else if (r == n.length)
        o = !!n.breakAfter, { tile: n, index: r } = l.pop(), r++;
      else if (t) {
        let a = Math.min(t, n.length - r);
        i && i.skip(n, r, r + a), t -= a, r += a;
      } else
        break;
    return this.tile = n, this.index = r, this.beforeBreak = o, this;
  }
  get root() {
    return this.parents.length ? this.parents[0].tile : this.tile;
  }
}
class rf {
  constructor(t, e, i, n) {
    this.from = t, this.to = e, this.wrapper = i, this.rank = n;
  }
}
class of {
  constructor(t, e, i) {
    this.cache = t, this.root = e, this.blockWrappers = i, this.curLine = null, this.lastBlock = null, this.afterWidget = null, this.pos = 0, this.wrappers = [], this.wrapperPos = 0;
  }
  addText(t, e, i, n) {
    var r;
    this.flushBuffer();
    let o = this.ensureMarks(e, i), l = o.lastChild;
    if (l && l.isText() && !(l.flags & 8) && l.length + t.length < 512) {
      this.cache.reused.set(
        l,
        2
        /* Reused.DOM */
      );
      let a = o.children[o.children.length - 1] = new ye(l.dom, l.text + t);
      a.parent = o;
    } else
      o.append(n || ye.of(t, (r = this.cache.find(ye)) === null || r === void 0 ? void 0 : r.dom));
    this.pos += t.length, this.afterWidget = null;
  }
  addComposition(t, e) {
    let i = this.curLine;
    i.dom != e.line.dom && (i.setDOM(this.cache.reused.has(e.line) ? jn(e.line.dom) : e.line.dom), this.cache.reused.set(
      e.line,
      2
      /* Reused.DOM */
    ));
    let n = i;
    for (let l = e.marks.length - 1; l >= 0; l--) {
      let a = e.marks[l], h = n.lastChild;
      if (h instanceof ht && h.mark.eq(a.mark))
        h.dom != a.dom && h.setDOM(jn(a.dom)), n = h;
      else {
        if (this.cache.reused.get(a)) {
          let c = U.get(a.dom);
          c && c.setDOM(jn(a.dom));
        }
        let f = ht.of(a.mark, a.dom);
        n.append(f), n = f;
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
    let o = new ye(t.text, t.text.nodeValue);
    o.flags |= 8, n.append(o);
  }
  addInlineWidget(t, e, i) {
    let n = this.afterWidget && t.flags & 48 && (this.afterWidget.flags & 48) == (t.flags & 48);
    n || this.flushBuffer();
    let r = this.ensureMarks(e, i);
    !n && !(t.flags & 16) && r.append(this.getBuffer(1)), r.append(t), this.pos += t.length, this.afterWidget = t;
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
    t || (t = jl);
    let n = Ve.start(t, e || ((i = this.cache.find(Ve)) === null || i === void 0 ? void 0 : i.dom), !!e);
    this.getBlockPos().append(this.lastBlock = this.curLine = n);
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
    let n = this.curLine;
    for (let r = t.length - 1; r >= 0; r--) {
      let o = t[r], l;
      if (e > 0 && (l = n.lastChild) && l instanceof ht && l.mark.eq(o))
        n = l, e--;
      else {
        let a = ht.of(o, (i = this.cache.find(ht, (h) => h.mark.eq(o))) === null || i === void 0 ? void 0 : i.dom);
        n.append(a), n = a, e = 0;
      }
    }
    return n;
  }
  endLine() {
    if (this.curLine) {
      this.flushBuffer();
      let t = this.curLine.lastChild;
      (!t || !jr(this.curLine, !1) || t.dom.nodeName != "BR" && t.isWidget() && !(S.ios && jr(this.curLine, !0))) && this.curLine.append(this.cache.findWidget(
        Xn,
        0,
        32
        /* TileFlag.After */
      ) || new Se(
        Xn.toDOM(),
        0,
        Xn,
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
        let e = new rf(t.from, t.to, t.value, t.rank), i = this.wrappers.length;
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
      let n = e.lastChild;
      if (i.from < this.pos && n instanceof Kt && n.wrapper.eq(i.wrapper))
        e = n;
      else {
        let r = Kt.of(i.wrapper, (t = this.cache.find(Kt, (o) => o.wrapper.eq(i.wrapper))) === null || t === void 0 ? void 0 : t.dom);
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
      ln,
      void 0,
      1
      /* Reused.Full */
    );
    return i && (i.flags = e), i || new ln(e);
  }
  flushBuffer() {
    this.afterWidget && !(this.afterWidget.flags & 32) && (this.afterWidget.parent.append(this.getBuffer(-1)), this.afterWidget = null);
  }
}
class lf {
  constructor(t) {
    this.skipCount = 0, this.text = "", this.textOff = 0, this.cursor = t.iter();
  }
  skip(t) {
    this.textOff + t <= this.text.length ? this.textOff += t : (this.skipCount += t - (this.text.length - this.textOff), this.text = "", this.textOff = 0);
  }
  next(t) {
    if (this.textOff == this.text.length) {
      let { value: n, lineBreak: r, done: o } = this.cursor.next(this.skipCount);
      if (this.skipCount = 0, o)
        throw new Error("Ran out of text content when drawing inline views");
      this.text = n;
      let l = this.textOff = Math.min(t, n.length);
      return r ? null : n.slice(0, l);
    }
    let e = Math.min(this.text.length, this.textOff + t), i = this.text.slice(this.textOff, e);
    return this.textOff = e, i;
  }
}
const an = [Se, Ve, ye, ht, ln, Kt, Rn];
for (let s = 0; s < an.length; s++)
  an[s].bucket = s;
class af {
  constructor(t) {
    this.view = t, this.buckets = an.map(() => []), this.index = an.map(() => 0), this.reused = /* @__PURE__ */ new Map();
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
    let n = t.bucket, r = this.buckets[n], o = this.index[n];
    for (let l = r.length - 1; l >= 0; l--) {
      let a = (l + o) % r.length, h = r[a];
      if ((!e || e(h)) && !this.reused.has(h))
        return r.splice(a, 1), a < o && this.index[n]--, this.reused.set(h, i), h;
    }
    return null;
  }
  findWidget(t, e, i) {
    let n = this.buckets[0];
    if (n.length)
      for (let r = 0, o = 0; ; r++) {
        if (r == n.length) {
          if (o)
            return null;
          o = 1, r = 0;
        }
        let l = n[r];
        if (!this.reused.has(l) && (o == 0 ? l.widget.compare(t) : l.widget.constructor == t.constructor && t.updateDOM(l.dom, this.view)))
          return n.splice(r, 1), r < this.index[0] && this.index[0]--, l.widget == t && l.length == e && (l.flags & 497) == i ? (this.reused.set(
            l,
            1
            /* Reused.Full */
          ), l) : (this.reused.set(
            l,
            2
            /* Reused.DOM */
          ), new Se(l.dom, e, t, l.flags & -498 | i));
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
class hf {
  constructor(t, e, i, n, r) {
    this.view = t, this.decorations = n, this.disallowBlockEffectsFor = r, this.openWidget = !1, this.openMarks = 0, this.cache = new af(t), this.text = new lf(t.state.doc), this.builder = new of(this.cache, new Rn(t, t.contentDOM), V.iter(i)), this.cache.reused.set(
      e,
      2
      /* Reused.DOM */
    ), this.old = new sf(e), this.reuseWalker = {
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
    for (let n = 0, r = 0, o = 0; ; ) {
      let l = o < t.length ? t[o++] : null, a = l ? l.fromA : this.old.root.length;
      if (a > n) {
        let h = a - n;
        this.preserve(h, !o, !l), n = a, r += h;
      }
      if (!l)
        break;
      e && l.fromA <= e.range.fromA && l.toA >= e.range.toA ? (this.forward(l.fromA, e.range.fromA, e.range.fromA < e.range.toA ? 1 : -1), this.emit(r, e.range.fromB), this.cache.clear(), this.builder.addComposition(e, i), this.text.skip(e.range.toB - e.range.fromB), this.forward(e.range.fromA, l.toA), this.emit(e.range.toB, l.toB)) : (this.forward(l.fromA, l.toA), this.emit(r, l.toB)), r = l.toB, n = l.toA;
    }
    return this.builder.curLine && this.builder.endLine(), this.builder.root;
  }
  preserve(t, e, i) {
    let n = uf(this.old), r = this.openMarks;
    this.old.advance(t, i ? 1 : -1, {
      skip: (o, l, a) => {
        if (o.isWidget())
          if (this.openWidget)
            this.builder.continueWidget(a - l);
          else {
            let h = a > 0 || l < o.length ? Se.of(o.widget, this.view, a - l, o.flags & 496, this.cache.maybeReuse(o)) : this.cache.reuse(o);
            h.flags & 256 ? (h.flags &= -2, this.builder.addBlockWidget(h)) : (this.builder.ensureLine(null), this.builder.addInlineWidget(h, n, r), r = n.length);
          }
        else if (o.isText())
          this.builder.ensureLine(null), !l && a == o.length && !this.cache.reused.has(o) ? this.builder.addText(o.text, n, r, this.cache.reuse(o)) : (this.cache.add(o), this.builder.addText(o.text.slice(l, a), n, r)), r = n.length;
        else if (o.isLine())
          o.flags &= -2, this.cache.reused.set(
            o,
            1
            /* Reused.Full */
          ), this.builder.addLine(o);
        else if (o instanceof ln)
          this.cache.add(o);
        else if (o instanceof ht)
          this.builder.ensureLine(null), this.builder.addMark(o, n, r), this.cache.reused.set(
            o,
            1
            /* Reused.Full */
          ), r = n.length;
        else
          return !1;
        this.openWidget = !1;
      },
      enter: (o) => {
        o.isLine() ? this.builder.addLineStart(o.attrs, this.cache.maybeReuse(o)) : (this.cache.add(o), o instanceof ht && n.unshift(o.mark)), this.openWidget = !1;
      },
      leave: (o) => {
        o.isLine() ? n.length && (n.length = r = 0) : o instanceof ht && (n.shift(), r = Math.min(r, n.length));
      },
      break: () => {
        this.builder.addBreak(), this.openWidget = !1;
      }
    }), this.text.skip(t);
  }
  emit(t, e) {
    let i = null, n = this.builder, r = 0, o = V.spans(this.decorations, t, e, {
      point: (l, a, h, f, c, u) => {
        if (h instanceof xe) {
          if (this.disallowBlockEffectsFor[u]) {
            if (h.block)
              throw new RangeError("Block decorations may not be specified via plugins");
            if (a > this.view.state.doc.lineAt(l).to)
              throw new RangeError("Decorations that replace line breaks may not be specified via plugins");
          }
          if (r = f.length, c > f.length)
            n.continueWidget(a - l);
          else {
            let d = h.widget || (h.block ? We.block : We.inline), p = ff(h), g = this.cache.findWidget(d, a - l, p) || Se.of(d, this.view, a - l, p);
            h.block ? (h.startSide > 0 && n.addLineStartIfNotCovered(i), n.addBlockWidget(g)) : (n.ensureLine(i), n.addInlineWidget(g, f, c));
          }
          i = null;
        } else
          i = cf(i, h);
        a > l && this.text.skip(a - l);
      },
      span: (l, a, h, f) => {
        for (let c = l; c < a; ) {
          let u = this.text.next(Math.min(512, a - c));
          u == null ? (n.addLineStartIfNotCovered(i), n.addBreak(), c++) : (n.ensureLine(i), n.addText(u, h, c == l ? f : h.length), c += u.length), i = null;
        }
      }
    });
    n.addLineStartIfNotCovered(i), this.openWidget = o > r, this.openMarks = o;
  }
  forward(t, e, i = 1) {
    e - t <= 10 ? this.old.advance(e - t, i, this.reuseWalker) : (this.old.advance(5, -1, this.reuseWalker), this.old.advance(e - t - 10, -1), this.old.advance(5, i, this.reuseWalker));
  }
  getCompositionContext(t) {
    let e = [], i = null;
    for (let n = t.parentNode; ; n = n.parentNode) {
      let r = U.get(n);
      if (n == this.view.contentDOM)
        break;
      r instanceof ht ? e.push(r) : r != null && r.isLine() ? i = r : r instanceof Kt || (n.nodeName == "DIV" && !i && n != this.view.contentDOM ? i = new Ve(n, jl) : i || e.push(ht.of(new wi({ tagName: n.nodeName.toLowerCase(), attributes: Nh(n) }), n)));
    }
    return { line: i, marks: e };
  }
}
function jr(s, t) {
  let e = (i) => {
    for (let n of i.children)
      if ((t ? n.isText() : n.length) || e(n))
        return !0;
    return !1;
  };
  return e(s);
}
function ff(s) {
  let t = s.isReplace ? (s.startSide < 0 ? 64 : 0) | (s.endSide > 0 ? 128 : 0) : s.startSide > 0 ? 32 : 16;
  return s.block && (t |= 256), t;
}
const jl = { class: "cm-line" };
function cf(s, t) {
  let e = t.spec.attributes, i = t.spec.class;
  return !e && !i || (s || (s = { class: "cm-line" }), e && Gs(e, s), i && (s.class += " " + i)), s;
}
function uf(s) {
  let t = [];
  for (let e = s.parents.length; e > 1; e--) {
    let i = e == s.parents.length ? s.tile : s.parents[e].tile;
    i instanceof ht && t.push(i.mark);
  }
  return t;
}
function jn(s) {
  let t = U.get(s);
  return t && t.setDOM(s.cloneNode()), s;
}
class We extends bi {
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
We.inline = /* @__PURE__ */ new We("span");
We.block = /* @__PURE__ */ new We("div");
const Xn = /* @__PURE__ */ new class extends bi {
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
class Xr {
  constructor(t) {
    this.view = t, this.decorations = [], this.blockWrappers = [], this.dynamicDecorationMap = [!1], this.domChanged = null, this.hasComposition = null, this.editContextFormatting = q.none, this.lastCompositionAfterCursor = !1, this.minWidth = 0, this.minWidthFrom = 0, this.minWidthTo = 0, this.impreciseAnchor = null, this.impreciseHead = null, this.forceSelection = !1, this.lastUpdate = Date.now(), this.updateDeco(), this.tile = new Rn(t, t.contentDOM), this.updateInner([new bt(0, 0, 0, t.state.doc.length)], null);
  }
  // Update the document view to a given state.
  update(t) {
    var e;
    let i = t.changedRanges;
    this.minWidth > 0 && i.length && (i.every(({ fromA: f, toA: c }) => c < this.minWidthFrom || f > this.minWidthTo) ? (this.minWidthFrom = t.changes.mapPos(this.minWidthFrom, 1), this.minWidthTo = t.changes.mapPos(this.minWidthTo, 1)) : this.minWidth = this.minWidthFrom = this.minWidthTo = 0), this.updateEditContextFormatting(t);
    let n = -1;
    this.view.inputState.composing >= 0 && !this.view.observer.editContext && (!((e = this.domChanged) === null || e === void 0) && e.newSel ? n = this.domChanged.newSel.head : !kf(t.changes, this.hasComposition) && !t.selectionSet && (n = t.state.selection.main.head));
    let r = n > -1 ? pf(this.view, t.changes, n) : null;
    if (this.domChanged = null, this.hasComposition) {
      let { from: f, to: c } = this.hasComposition;
      i = new bt(f, c, t.changes.mapPos(f, -1), t.changes.mapPos(c, 1)).addToSet(i.slice());
    }
    this.hasComposition = r ? { from: r.range.fromB, to: r.range.toB } : null, (S.ie || S.chrome) && !r && t && t.state.doc.lines != t.startState.doc.lines && (this.forceSelection = !0);
    let o = this.decorations, l = this.blockWrappers;
    this.updateDeco();
    let a = yf(o, this.decorations, t.changes);
    a.length && (i = bt.extendWithRanges(i, a));
    let h = wf(l, this.blockWrappers, t.changes);
    return h.length && (i = bt.extendWithRanges(i, h)), r && !i.some((f) => f.fromA <= r.range.fromA && f.toA >= r.range.toA) && (i = r.range.addToSet(i.slice())), this.tile.flags & 2 && i.length == 0 ? !1 : (this.updateInner(i, r), t.transactions.length && (this.lastUpdate = Date.now()), !0);
  }
  // Used by update and the constructor do perform the actual DOM
  // update
  updateInner(t, e) {
    this.view.viewState.mustMeasureContent = !0;
    let { observer: i } = this.view;
    i.ignore(() => {
      if (e || t.length) {
        let o = this.tile, l = new hf(this.view, o, this.blockWrappers, this.decorations, this.dynamicDecorationMap);
        e && U.get(e.text) && l.cache.reused.set(
          U.get(e.text),
          2
          /* Reused.DOM */
        ), this.tile = l.run(t, e), Ms(o, l.cache.reused);
      }
      this.tile.dom.style.height = this.view.viewState.contentHeight / this.view.scaleY + "px", this.tile.dom.style.flexBasis = this.minWidth ? this.minWidth + "px" : "";
      let r = S.chrome || S.ios ? { node: i.selectionRange.focusNode, written: !1 } : void 0;
      this.tile.sync(r), r && (r.written || i.selectionRange.focusNode != r.node || !this.tile.dom.contains(r.node)) && (this.forceSelection = !0), this.tile.dom.style.height = "";
    });
    let n = [];
    if (this.view.viewport.from || this.view.viewport.to < this.view.state.doc.length)
      for (let r of this.tile.children)
        r.isWidget() && r.widget instanceof qn && n.push(r.dom);
    i.updateGaps(n);
  }
  updateEditContextFormatting(t) {
    this.editContextFormatting = this.editContextFormatting.map(t.changes);
    for (let e of t.transactions)
      for (let i of e.effects)
        i.is(Hl) && (this.editContextFormatting = i.value);
  }
  // Sync the DOM selection to this.state.selection
  updateSelection(t = !1, e = !1) {
    (t || !this.view.observer.selectionRange.focusNode) && this.view.observer.readSelectionRange();
    let { dom: i } = this.tile, n = this.view.root.activeElement, r = n == i, o = !r && !(this.view.state.facet(qt) || i.tabIndex > -1) && ni(i, this.view.observer.selectionRange) && !(n && i.contains(n));
    if (!(r || e || o))
      return;
    let l = this.forceSelection;
    this.forceSelection = !1;
    let a = this.view.state.selection.main, h, f;
    if (a.empty ? f = h = this.inlineDOMNearPos(a.anchor, a.assoc || 1) : (f = this.inlineDOMNearPos(a.head, a.head == a.from ? 1 : -1), h = this.inlineDOMNearPos(a.anchor, a.anchor == a.from ? 1 : -1)), S.gecko && a.empty && !this.hasComposition && df(h)) {
      let u = document.createTextNode("");
      this.view.observer.ignore(() => h.node.insertBefore(u, h.node.childNodes[h.offset] || null)), h = f = new Ct(u, 0), l = !0;
    }
    let c = this.view.observer.selectionRange;
    (l || !c.focusNode || (!si(h.node, h.offset, c.anchorNode, c.anchorOffset) || !si(f.node, f.offset, c.focusNode, c.focusOffset)) && !this.suppressWidgetCursorChange(c, a)) && (this.view.observer.ignore(() => {
      S.android && S.chrome && i.contains(c.focusNode) && xf(c.focusNode, i) && (i.blur(), i.focus({ preventScroll: !0 }));
      let u = ui(this.view.root);
      if (u) if (a.empty) {
        if (S.gecko) {
          let d = gf(h.node, h.offset);
          if (d && d != 3) {
            let p = (d == 1 ? Ol : Cl)(h.node, h.offset);
            p && (h = new Ct(p.node, p.offset));
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
      o && this.view.root.activeElement == i && (i.blur(), n && n.focus());
    }), this.view.observer.setSelectionRange(h, f)), this.impreciseAnchor = h.precise ? null : new Ct(c.anchorNode, c.anchorOffset), this.impreciseHead = f.precise ? null : new Ct(c.focusNode, c.focusOffset);
  }
  // If a zero-length widget is inserted next to the cursor during
  // composition, avoid moving it across it and disrupting the
  // composition.
  suppressWidgetCursorChange(t, e) {
    return this.hasComposition && e.empty && si(t.focusNode, t.focusOffset, t.anchorNode, t.anchorOffset) && this.posFromDOM(t.focusNode, t.focusOffset) == e.head;
  }
  enforceCursorAssoc() {
    if (this.hasComposition)
      return;
    let { view: t } = this, e = t.state.selection.main, i = ui(t.root), { anchorNode: n, anchorOffset: r } = t.observer.selectionRange;
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
    t.docView.posFromDOM(c.anchorNode, c.anchorOffset) != e.from && i.collapse(n, r);
  }
  posFromDOM(t, e) {
    let i = this.tile.nearest(t);
    if (!i)
      return this.tile.dom.compareDocumentPosition(t) & 2 ? 0 : this.view.state.doc.length;
    let n = i.posAtStart;
    if (i.isComposite()) {
      let r;
      if (t == i.dom)
        r = i.dom.childNodes[e];
      else {
        let o = _t(t) == 0 ? 0 : e == 0 ? -1 : 1;
        for (; ; ) {
          let l = t.parentNode;
          if (l == i.dom)
            break;
          o == 0 && l.firstChild != l.lastChild && (t == l.firstChild ? o = -1 : o = 1), t = l;
        }
        o < 0 ? r = t : r = t.nextSibling;
      }
      if (r == i.dom.firstChild)
        return n;
      for (; r && !U.get(r); )
        r = r.nextSibling;
      if (!r)
        return n + i.length;
      for (let o = 0, l = n; ; o++) {
        let a = i.children[o];
        if (a.dom == r)
          return l;
        l += a.length + a.breakAfter;
      }
    } else return i.isText() ? t == i.dom ? n + e : n + (e ? i.length : 0) : n;
  }
  domAtPos(t, e) {
    let { tile: i, offset: n } = this.tile.resolveBlock(t, e);
    return i.isWidget() ? i.domPosFor(t, e) : i.domIn(n, e);
  }
  inlineDOMNearPos(t, e) {
    let i, n = -1, r = !1, o, l = -1, a = !1;
    return this.tile.blockTiles((h, f) => {
      if (h.isWidget()) {
        if (h.flags & 32 && f >= t)
          return !0;
        h.flags & 16 && (r = !0);
      } else {
        let c = f + h.length;
        if (f <= t && (i = h, n = t - f, r = c < t), c >= t && !o && (o = h, l = t - f, a = f > t), f > t && o)
          return !0;
      }
    }), !i && !o ? this.domAtPos(t, e) : (r && o ? i = null : a && i && (o = null), i && e < 0 || !o ? i.domIn(n, e) : o.domIn(l, e));
  }
  coordsAt(t, e) {
    let { tile: i, offset: n } = this.tile.resolveBlock(t, e);
    return i.isWidget() ? i.widget instanceof qn ? null : i.coordsInWidget(n, e, !0) : i.coordsIn(n, e);
  }
  lineAt(t, e) {
    let { tile: i } = this.tile.resolveBlock(t, e);
    return i.isLine() ? i : null;
  }
  coordsForChar(t) {
    let { tile: e, offset: i } = this.tile.resolveBlock(t, 1);
    if (!e.isLine())
      return null;
    function n(r, o) {
      if (r.isComposite())
        for (let l of r.children) {
          if (l.length >= o) {
            let a = n(l, o);
            if (a)
              return a;
          }
          if (o -= l.length, o < 0)
            break;
        }
      else if (r.isText() && o < r.length) {
        let l = nt(r.text, o);
        if (l == o)
          return null;
        let a = di(r.dom, o, l).getClientRects();
        for (let h = 0; h < a.length; h++) {
          let f = a[h];
          if (h == a.length - 1 || f.top < f.bottom && f.left < f.right)
            return f;
        }
      }
      return null;
    }
    return n(e, i);
  }
  measureVisibleLineHeights(t) {
    let e = [], { from: i, to: n } = t, r = this.view.contentDOM.clientWidth, o = r > Math.max(this.view.scrollDOM.clientWidth, this.minWidth) + 1, l = -1, a = this.view.textDirection == $.LTR, h = 0, f = (c, u, d) => {
      for (let p = 0; p < c.children.length && !(u > n); p++) {
        let g = c.children[p], m = u + g.length, y = g.dom.getBoundingClientRect(), { height: w } = y;
        if (d && !p && (h += y.top - d.top), g instanceof Kt)
          m > i && f(g, u, y);
        else if (u >= i && (h > 0 && e.push(-h), e.push(w + h), h = 0, o)) {
          let b = g.dom.lastChild, O = b ? Yi(b) : [];
          if (O.length) {
            let v = O[O.length - 1], T = a ? v.right - y.left : y.right - v.left;
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
          let f = Yi(h.dom);
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
    let e = document.createElement("div"), i, n, r;
    return e.className = "cm-line", e.style.width = "99999px", e.style.position = "absolute", e.textContent = "abc def ghi jkl mno pqr stu", this.view.observer.ignore(() => {
      this.tile.dom.appendChild(e);
      let o = Yi(e.firstChild)[0];
      i = e.getBoundingClientRect().height, n = o && o.width ? o.width / 27 : 7, r = o && o.height ? o.height : i, e.remove();
    }), { lineHeight: i, charWidth: n, textHeight: r };
  }
  computeBlockGapDeco() {
    let t = [], e = this.view.viewState;
    for (let i = 0, n = 0; ; n++) {
      let r = n == e.viewports.length ? null : e.viewports[n], o = r ? r.from - 1 : this.view.state.doc.length;
      if (o > i) {
        let l = (e.lineBlockAt(o).bottom - e.lineBlockAt(i).top) / this.view.scaleY;
        t.push(q.replace({
          widget: new qn(l),
          block: !0,
          inclusive: !0,
          isBlockGap: !0
        }).range(i, o));
      }
      if (!r)
        break;
      i = r.to + 1;
    }
    return q.set(t);
  }
  updateDeco() {
    let t = 1, e = this.view.state.facet(Mn).map((r) => (this.dynamicDecorationMap[t++] = typeof r == "function") ? r(this.view) : r), i = !1, n = this.view.state.facet(nr).map((r, o) => {
      let l = typeof r == "function";
      return l && (i = !0), l ? r(this.view) : r;
    });
    for (n.length && (this.dynamicDecorationMap[t++] = i, e.push(V.join(n))), this.decorations = [
      this.editContextFormatting,
      ...e,
      this.computeBlockGapDeco(),
      this.view.viewState.lineGapDeco
    ]; t < this.decorations.length; )
      this.dynamicDecorationMap[t++] = !1;
    this.blockWrappers = this.view.state.facet(zl).map((r) => typeof r == "function" ? r(this.view) : r);
  }
  scrollIntoView(t) {
    if (t.isSnapshot) {
      let h = this.view.viewState.lineBlockAt(t.range.head);
      this.view.scrollDOM.scrollTop = h.top - t.yMargin, this.view.scrollDOM.scrollLeft = t.xMargin;
      return;
    }
    for (let h of this.view.state.facet(Wl))
      try {
        if (h(this.view, t.range, t))
          return !0;
      } catch (f) {
        ft(this.view.state, f, "scroll handler");
      }
    let { range: e } = t, i = this.coordsAt(e.head, e.empty ? e.assoc : e.head > e.anchor ? -1 : 1), n;
    if (!i)
      return;
    !e.empty && (n = this.coordsAt(e.anchor, e.anchor > e.head ? -1 : 1)) && (i = {
      left: Math.min(i.left, n.left),
      top: Math.min(i.top, n.top),
      right: Math.max(i.right, n.right),
      bottom: Math.max(i.bottom, n.bottom)
    });
    let r = sr(this.view), o = {
      left: i.left - r.left,
      top: i.top - r.top,
      right: i.right + r.right,
      bottom: i.bottom + r.bottom
    }, { offsetWidth: l, offsetHeight: a } = this.view.scrollDOM;
    if (Hh(this.view.scrollDOM, o, e.head < e.anchor ? -1 : 1, t.x, t.y, Math.max(Math.min(t.xMargin, l), -l), Math.max(Math.min(t.yMargin, a), -a), this.view.textDirection == $.LTR), window.visualViewport && window.innerHeight - window.visualViewport.height > 1 && (i.top > window.pageYOffset + window.visualViewport.offsetTop + window.visualViewport.height || i.bottom < window.pageYOffset + window.visualViewport.offsetTop)) {
      let h = this.view.docView.lineAt(e.head, 1);
      h && h.dom.scrollIntoView({ block: "nearest" });
    }
  }
  lineHasWidget(t) {
    let e = (i) => i.isWidget() || i.children.some(e);
    return e(this.tile.resolveBlock(t, 1).tile);
  }
  destroy() {
    Ms(this.tile);
  }
}
function Ms(s, t) {
  let e = t == null ? void 0 : t.get(s);
  if (e != 1) {
    e == null && s.destroy();
    for (let i of s.children)
      Ms(i, t);
  }
}
function df(s) {
  return s.node.nodeType == 1 && s.node.firstChild && (s.offset == 0 || s.node.childNodes[s.offset - 1].contentEditable == "false") && (s.offset == s.node.childNodes.length || s.node.childNodes[s.offset].contentEditable == "false");
}
function Xl(s, t) {
  let e = s.observer.selectionRange;
  if (!e.focusNode)
    return null;
  let i = Ol(e.focusNode, e.focusOffset), n = Cl(e.focusNode, e.focusOffset), r = i || n;
  if (n && i && n.node != i.node) {
    let l = U.get(n.node);
    if (!l || l.isText() && l.text != n.node.nodeValue)
      r = n;
    else if (s.docView.lastCompositionAfterCursor) {
      let a = U.get(i.node);
      !a || a.isText() && a.text != i.node.nodeValue || (r = n);
    }
  }
  if (s.docView.lastCompositionAfterCursor = r != i, !r)
    return null;
  let o = t - r.offset;
  return { from: o, to: o + r.node.nodeValue.length, node: r.node };
}
function pf(s, t, e) {
  let i = Xl(s, e);
  if (!i)
    return null;
  let { node: n, from: r, to: o } = i, l = n.nodeValue;
  if (/[\n\r]/.test(l) || s.state.doc.sliceString(i.from, i.to) != l)
    return null;
  let a = t.invertedDesc;
  return { range: new bt(a.mapPos(r), a.mapPos(o), r, o), text: n };
}
function gf(s, t) {
  return s.nodeType != 1 ? 0 : (t && s.childNodes[t - 1].contentEditable == "false" ? 1 : 0) | (t < s.childNodes.length && s.childNodes[t].contentEditable == "false" ? 2 : 0);
}
let mf = class {
  constructor() {
    this.changes = [];
  }
  compareRange(t, e) {
    De(t, e, this.changes);
  }
  comparePoint(t, e) {
    De(t, e, this.changes);
  }
  boundChange(t) {
    De(t, t, this.changes);
  }
};
function yf(s, t, e) {
  let i = new mf();
  return V.compare(s, t, e, i), i.changes;
}
class bf {
  constructor() {
    this.changes = [];
  }
  compareRange(t, e) {
    De(t, e, this.changes);
  }
  comparePoint() {
  }
  boundChange(t) {
    De(t, t, this.changes);
  }
}
function wf(s, t, e) {
  let i = new bf();
  return V.compare(s, t, e, i), i.changes;
}
function xf(s, t) {
  for (let e = s; e && e != t; e = e.assignedSlot || e.parentNode)
    if (e.nodeType == 1 && e.contentEditable == "false")
      return !0;
  return !1;
}
function kf(s, t) {
  let e = !1;
  return t && s.iterChangedRanges((i, n) => {
    i < t.to && n > t.from && (e = !0);
  }), e;
}
class qn extends bi {
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
function Sf(s, t, e = 1) {
  let i = s.charCategorizer(t), n = s.doc.lineAt(t), r = t - n.from;
  if (n.length == 0)
    return x.cursor(t);
  r == 0 ? e = 1 : r == n.length && (e = -1);
  let o = r, l = r;
  e < 0 ? o = nt(n.text, r, !1) : l = nt(n.text, r);
  let a = i(n.text.slice(o, l));
  for (; o > 0; ) {
    let h = nt(n.text, o, !1);
    if (i(n.text.slice(h, o)) != a)
      break;
    o = h;
  }
  for (; l < n.length; ) {
    let h = nt(n.text, l);
    if (i(n.text.slice(l, h)) != a)
      break;
    l = h;
  }
  return x.range(o + n.from, l + n.from);
}
function vf(s, t, e, i, n) {
  let r = Math.round((i - t.left) * s.defaultCharacterWidth);
  if (s.lineWrapping && e.height > s.defaultLineHeight * 1.5) {
    let l = s.viewState.heightOracle.textHeight, a = Math.floor((n - e.top - (s.defaultLineHeight - l) * 0.5) / l);
    r += a * s.viewState.heightOracle.lineLength;
  }
  let o = s.state.sliceDoc(e.from, e.to);
  return e.from + Mh(o, r, s.state.tabSize);
}
function Of(s, t, e) {
  let i = s.lineBlockAt(t);
  if (Array.isArray(i.type)) {
    let n;
    for (let r of i.type) {
      if (r.from > t)
        break;
      if (!(r.to < t)) {
        if (r.from < t && r.to > t)
          return r;
        (!n || r.type == kt.Text && (n.type != r.type || (e < 0 ? r.from < t : r.to > t))) && (n = r);
      }
    }
    return n || i;
  }
  return i;
}
function Cf(s, t, e, i) {
  let n = Of(s, t.head, t.assoc || -1), r = !i || n.type != kt.Text || !(s.lineWrapping || n.widgetLineBreaks) ? null : s.coordsAtPos(t.assoc < 0 && t.head > n.from ? t.head - 1 : t.head);
  if (r) {
    let o = s.dom.getBoundingClientRect(), l = s.textDirectionAt(n.from), a = s.posAtCoords({
      x: e == (l == $.LTR) ? o.right - 1 : o.left + 1,
      y: (r.top + r.bottom) / 2
    });
    if (a != null)
      return x.cursor(a, e ? -1 : 1);
  }
  return x.cursor(e ? n.to : n.from, e ? -1 : 1);
}
function qr(s, t, e, i) {
  let n = s.state.doc.lineAt(t.head), r = s.bidiSpans(n), o = s.textDirectionAt(n.from);
  for (let l = t, a = null; ; ) {
    let h = Yh(n, r, o, l, e), f = Dl;
    if (!h) {
      if (n.number == (e ? s.state.doc.lines : 1))
        return l;
      f = `
`, n = s.state.doc.line(n.number + (e ? 1 : -1)), r = s.bidiSpans(n), h = s.visualLineSide(n, !e);
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
function Af(s, t, e) {
  let i = s.state.charCategorizer(t), n = i(e);
  return (r) => {
    let o = i(r);
    return n == Ut.Space && (n = o), n == o;
  };
}
function Tf(s, t, e, i) {
  let n = t.head, r = e ? 1 : -1;
  if (n == (e ? s.state.doc.length : 0))
    return x.cursor(n, t.assoc);
  let o = t.goalColumn, l, a = s.contentDOM.getBoundingClientRect(), h = s.coordsAtPos(n, (t.empty ? t.assoc : 0) || (e ? 1 : -1)), f = s.documentTop;
  if (h)
    o == null && (o = h.left - a.left), l = r < 0 ? h.top : h.bottom;
  else {
    let p = s.viewState.lineBlockAt(n);
    o == null && (o = Math.min(a.right - a.left, s.defaultCharacterWidth * (n - p.from))), l = (r < 0 ? p.top : p.bottom) + f;
  }
  let c = a.left + o, u = i ?? s.viewState.heightOracle.textHeight >> 1, d = Ds(s, { x: c, y: l + u * r }, !1, r);
  return x.cursor(d.pos, d.assoc, void 0, o);
}
function ri(s, t, e) {
  for (; ; ) {
    let i = 0;
    for (let n of s)
      n.between(t - 1, t + 1, (r, o, l) => {
        if (t > r && t < o) {
          let a = i || e || (t - r < o - t ? -1 : 1);
          t = a < 0 ? r : o, i = a;
        }
      });
    if (!i)
      return t;
  }
}
function ql(s, t) {
  let e = null;
  for (let i = 0; i < t.ranges.length; i++) {
    let n = t.ranges[i], r = null;
    if (n.empty) {
      let o = ri(s, n.from, 0);
      o != n.from && (r = x.cursor(o, -1));
    } else {
      let o = ri(s, n.from, -1), l = ri(s, n.to, 1);
      (o != n.from || l != n.to) && (r = x.range(n.from == n.anchor ? o : l, n.from == n.head ? o : l));
    }
    r && (e || (e = t.ranges.slice()), e[i] = r);
  }
  return e ? x.create(e, t.mainIndex) : t;
}
function Un(s, t, e) {
  let i = ri(s.state.facet(ki).map((n) => n(s)), e.from, t.head > e.from ? -1 : 1);
  return i == e.from ? e : x.cursor(i, i < e.from ? 1 : -1);
}
class Wt {
  constructor(t, e) {
    this.pos = t, this.assoc = e;
  }
}
function Ds(s, t, e, i) {
  let n = s.contentDOM.getBoundingClientRect(), r = n.top + s.viewState.paddingTop, { x: o, y: l } = t, a = l - r, h;
  for (; ; ) {
    if (a < 0)
      return new Wt(0, 1);
    if (a > s.viewState.docHeight)
      return new Wt(s.state.doc.length, -1);
    if (h = s.elementAtHeight(a), i == null)
      break;
    if (h.type == kt.Text) {
      if (i < 0 ? h.to < s.viewport.from : h.from > s.viewport.to)
        break;
      let u = s.docView.coordsAt(i < 0 ? h.from : h.to, i > 0 ? -1 : 1);
      if (u && (i < 0 ? u.top <= a + r : u.bottom >= a + r))
        break;
    }
    let c = s.viewState.heightOracle.textHeight / 2;
    a = i > 0 ? h.bottom + c : h.top - c;
  }
  if (s.viewport.from >= h.to || s.viewport.to <= h.from) {
    if (e)
      return null;
    if (h.type == kt.Text) {
      let c = vf(s, n, h, o, l);
      return new Wt(c, c == h.from ? 1 : -1);
    }
  }
  if (h.type != kt.Text)
    return a < (h.top + h.bottom) / 2 ? new Wt(h.from, 1) : new Wt(h.to, -1);
  let f = s.docView.lineAt(h.from, 2);
  return (!f || f.length != h.length) && (f = s.docView.lineAt(h.from, -2)), new Pf(s, o, l, s.textDirectionAt(h.from)).scanTile(f, h.from);
}
class Pf {
  constructor(t, e, i, n) {
    this.view = t, this.x = e, this.y = i, this.baseDir = n, this.line = null, this.spans = null;
  }
  bidiSpansAt(t) {
    return (!this.line || this.line.from > t || this.line.to < t) && (this.line = this.view.state.doc.lineAt(t), this.spans = this.view.bidiSpans(this.line)), this;
  }
  baseDirAt(t, e) {
    let { line: i, spans: n } = this.bidiSpansAt(t);
    return n[Ht.find(n, t - i.from, -1, e)].level == this.baseDir;
  }
  dirAt(t, e) {
    let { line: i, spans: n } = this.bidiSpansAt(t);
    return n[Ht.find(n, t - i.from, -1, e)].dir;
  }
  // Used to short-circuit bidi tests for content with a uniform direction
  bidiIn(t, e) {
    let { spans: i, line: n } = this.bidiSpansAt(t);
    return i.length > 1 || i.length && (i[0].level != this.baseDir || i[0].to + n.from < e);
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
    let i = 0, n = t.length - 1, r = /* @__PURE__ */ new Set(), o = this.bidiIn(t[0], t[n]), l, a, h = -1, f = 1e9, c;
    t: for (; i < n; ) {
      let d = n - i, p = i + n >> 1;
      e: if (r.has(p)) {
        let m = i + Math.floor(Math.random() * d);
        for (let y = 0; y < d; y++) {
          if (!r.has(m)) {
            p = m;
            break e;
          }
          m++, m == n && (m = i);
        }
        break t;
      }
      r.add(p);
      let g = e(p);
      if (g)
        for (let m = 0; m < g.length; m++) {
          let y = g[m], w = 0;
          if (!(y.width == 0 && g.length > 1)) {
            if (y.bottom < this.y)
              (!l || l.bottom < y.bottom) && (l = y), w = 1;
            else if (y.top > this.y)
              (!a || a.top > y.top) && (a = y), w = -1;
            else {
              let b = y.left > this.x ? this.x - y.left : y.right < this.x ? this.x - y.right : 0, O = Math.abs(b);
              O < f && (h = p, f = O, c = y), b && (w = b < 0 == (this.baseDir == $.LTR) ? -1 : 1);
            }
            w == -1 && (!o || this.baseDirAt(t[p], 1)) ? n = p : w == 1 && (!o || this.baseDirAt(t[p + 1], -1)) && (i = p + 1);
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
    for (let r = 0; r < t.length; r = nt(t.text, r))
      i.push(e + r);
    i.push(e + t.length);
    let n = this.scan(i, (r) => {
      let o = i[r] - e, l = i[r + 1] - e;
      return di(t.dom, o, l).getClientRects();
    });
    return n.after ? new Wt(i[n.i + 1], -1) : new Wt(i[n.i], 1);
  }
  scanTile(t, e) {
    if (!t.length)
      return new Wt(e, 1);
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
    let n = this.scan(i, (l) => {
      let a = t.children[l];
      return a.flags & 48 ? null : (a.dom.nodeType == 1 ? a.dom : di(a.dom, 0, a.length)).getClientRects();
    }), r = t.children[n.i], o = i[n.i];
    return r.isText() ? this.scanText(r, o) : r.isComposite() ? this.scanTile(r, o) : n.after ? new Wt(i[n.i + 1], -1) : new Wt(o, 1);
  }
}
const Ce = "￿";
class Mf {
  constructor(t, e) {
    this.points = t, this.view = e, this.text = "", this.lineSeparator = e.state.facet(N.lineSeparator);
  }
  append(t) {
    this.text += t;
  }
  lineBreak() {
    this.text += Ce;
  }
  readRange(t, e) {
    if (!t)
      return this;
    let i = t.parentNode;
    for (let n = t; ; ) {
      this.findPointBefore(i, n);
      let r = this.text.length;
      this.readNode(n);
      let o = U.get(n), l = n.nextSibling;
      if (l == e) {
        o != null && o.breakAfter && !l && i != this.view.contentDOM && this.lineBreak();
        break;
      }
      let a = U.get(l);
      (o && a ? o.breakAfter : (o ? o.breakAfter : sn(n)) || sn(l) && (n.nodeName != "BR" || o != null && o.isWidget()) && this.text.length > r) && !Rf(l, e) && this.lineBreak(), n = l;
    }
    return this.findPointBefore(i, e), this;
  }
  readTextNode(t) {
    let e = t.nodeValue;
    for (let i of this.points)
      i.node == t && (i.pos = this.text.length + Math.min(i.offset, e.length));
    for (let i = 0, n = this.lineSeparator ? null : /\r\n?|\n/g; ; ) {
      let r = -1, o = 1, l;
      if (this.lineSeparator ? (r = e.indexOf(this.lineSeparator, i), o = this.lineSeparator.length) : (l = n.exec(e)) && (r = l.index, o = l[0].length), this.append(e.slice(i, r < 0 ? e.length : r)), r < 0)
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
      for (let n = i.iter(); !n.next().done; )
        n.lineBreak ? this.lineBreak() : this.append(n.value);
    } else t.nodeType == 3 ? this.readTextNode(t) : t.nodeName == "BR" ? t.nextSibling && this.lineBreak() : t.nodeType == 1 && this.readRange(t.firstChild, null);
  }
  findPointBefore(t, e) {
    for (let i of this.points)
      i.node == t && t.childNodes[i.offset] == e && (i.pos = this.text.length);
  }
  findPointInside(t, e) {
    for (let i of this.points)
      (t.nodeType == 3 ? i.node == t : t.contains(i.node)) && (i.pos = this.text.length + (Df(t, i.node, i.offset) ? e : 0));
  }
}
function Df(s, t, e) {
  for (; ; ) {
    if (!t || e < _t(t))
      return !1;
    if (t == s)
      return !0;
    e = oe(t) + 1, t = t.parentNode;
  }
}
function Rf(s, t) {
  let e;
  for (; !(s == t || !s); s = s.nextSibling) {
    let i = U.get(s);
    if (!(i != null && i.isWidget()))
      return !1;
    i && (e || (e = [])).push(i);
  }
  if (e)
    for (let i of e) {
      let n = i.overrideDOMText;
      if (n != null && n.length)
        return !1;
    }
  return !0;
}
class Ur {
  constructor(t, e) {
    this.node = t, this.offset = e, this.pos = -1;
  }
}
class Bf {
  constructor(t, e, i, n) {
    this.typeOver = n, this.bounds = null, this.text = "", this.domChanged = e > -1;
    let { impreciseHead: r, impreciseAnchor: o } = t.docView, l = t.state.selection;
    if (t.state.readOnly && e > -1)
      this.newSel = null;
    else if (e > -1 && (this.bounds = Ul(t.docView.tile, e, i, 0))) {
      let a = r || o ? [] : Ef(t), h = new Mf(a, t);
      h.readRange(this.bounds.startDOM, this.bounds.endDOM), this.text = h.text, this.newSel = If(a, this.bounds.from);
    } else {
      let a = t.observer.selectionRange, h = r && r.node == a.focusNode && r.offset == a.focusOffset || !Os(t.contentDOM, a.focusNode) ? l.main.head : t.docView.posFromDOM(a.focusNode, a.focusOffset), f = o && o.node == a.anchorNode && o.offset == a.anchorOffset || !Os(t.contentDOM, a.anchorNode) ? l.main.anchor : t.docView.posFromDOM(a.anchorNode, a.anchorOffset), c = t.viewport;
      if ((S.ios || S.chrome) && l.main.empty && h != f && (c.from > 0 || c.to < t.state.doc.length)) {
        let u = Math.min(h, f), d = Math.max(h, f), p = c.from - u, g = c.to - d;
        (p == 0 || p == 1 || u == 0) && (g == 0 || g == -1 || d == t.state.doc.length) && (h = 0, f = t.state.doc.length);
      }
      if (t.inputState.composing > -1 && l.ranges.length > 1)
        this.newSel = l.replaceRange(x.range(f, h));
      else if (t.lineWrapping && f == h && !(l.main.empty && l.main.head == h) && t.inputState.lastTouchTime > Date.now() - 100) {
        let u = t.coordsAtPos(h, -1), d = 0;
        u && (d = t.inputState.lastTouchY <= u.bottom ? -1 : 1), this.newSel = x.create([x.cursor(h, d)]);
      } else
        this.newSel = x.single(f, h);
    }
  }
}
function Ul(s, t, e, i) {
  if (s.isComposite()) {
    let n = -1, r = -1, o = -1, l = -1;
    for (let a = 0, h = i, f = i; a < s.children.length; a++) {
      let c = s.children[a], u = h + c.length;
      if (h < t && u > e)
        return Ul(c, t, e, h);
      if (u >= t && n == -1 && (n = a, r = h), h > e && c.dom.parentNode == s.dom) {
        o = a, l = f;
        break;
      }
      f = u, h = u + c.breakAfter;
    }
    return {
      from: r,
      to: l < 0 ? i + s.length : l,
      startDOM: (n ? s.children[n - 1].dom.nextSibling : null) || s.dom.firstChild,
      endDOM: o < s.children.length && o >= 0 ? s.children[o].dom : null
    };
  } else return s.isText() ? { from: i, to: i + s.length, startDOM: s.dom, endDOM: s.dom.nextSibling } : null;
}
function Kl(s, t) {
  let e, { newSel: i } = t, { state: n } = s, r = n.selection.main, o = s.inputState.lastKeyTime > Date.now() - 100 ? s.inputState.lastKeyCode : -1;
  if (t.bounds) {
    let { from: l, to: a } = t.bounds, h = r.from, f = null;
    (o === 8 || S.android && t.text.length < a - l) && (h = r.to, f = "end");
    let c = n.doc.sliceString(l, a, Ce), u, d;
    !r.empty && r.from >= l && r.to <= a && (t.typeOver || c != t.text) && c.slice(0, r.from - l) == t.text.slice(0, r.from - l) && c.slice(r.to - l) == t.text.slice(u = t.text.length - (c.length - (r.to - l))) ? e = {
      from: r.from,
      to: r.to,
      insert: E.of(t.text.slice(r.from - l, u).split(Ce))
    } : (d = _l(c, t.text, h - l, f)) && (S.chrome && o == 13 && d.toB == d.from + 2 && t.text.slice(d.from, d.toB) == Ce + Ce && d.toB--, e = {
      from: l + d.from,
      to: l + d.toA,
      insert: E.of(t.text.slice(d.from, d.toB).split(Ce))
    });
  } else i && (!s.hasFocus && n.facet(qt) || hn(i, r)) && (i = null);
  if (!e && !i)
    return !1;
  if ((S.mac || S.android) && e && e.from == e.to && e.from == r.head - 1 && /^\. ?$/.test(e.insert.toString()) && s.contentDOM.getAttribute("autocorrect") == "off" ? (i && e.insert.length == 2 && (i = x.single(i.main.anchor - 1, i.main.head - 1)), e = { from: e.from, to: e.to, insert: E.of([e.insert.toString().replace(".", " ")]) }) : n.doc.lineAt(r.from).to < r.to && s.docView.lineHasWidget(r.to) && s.inputState.insertingTextAt > Date.now() - 50 ? e = {
    from: r.from,
    to: r.to,
    insert: n.toText(s.inputState.insertingText)
  } : S.chrome && e && e.from == e.to && e.from == r.head && e.insert.toString() == `
 ` && s.lineWrapping && (i && (i = x.single(i.main.anchor - 1, i.main.head - 1)), e = { from: r.from, to: r.to, insert: E.of([" "]) }), e)
    return rr(s, e, i, o);
  if (i && !hn(i, r)) {
    let l = !1, a = "select";
    return s.inputState.lastSelectionTime > Date.now() - 50 && (s.inputState.lastSelectionOrigin == "select" && (l = !0), a = s.inputState.lastSelectionOrigin, a == "select.pointer" && (i = ql(n.facet(ki).map((h) => h(s)), i))), s.dispatch({ selection: i, scrollIntoView: l, userEvent: a }), !0;
  } else
    return !1;
}
function rr(s, t, e, i = -1) {
  if (S.ios && s.inputState.flushIOSKey(t))
    return !0;
  let n = s.state.selection.main;
  if (S.android && (t.to == n.to && // GBoard will sometimes remove a space it just inserted
  // after a completion when you press enter
  (t.from == n.from || t.from == n.from - 1 && s.state.sliceDoc(t.from, n.from) == " ") && t.insert.length == 1 && t.insert.lines == 2 && Re(s.contentDOM, "Enter", 13) || (t.from == n.from - 1 && t.to == n.to && t.insert.length == 0 || i == 8 && t.insert.length < t.to - t.from && t.to > n.head) && Re(s.contentDOM, "Backspace", 8) || t.from == n.from && t.to == n.to + 1 && t.insert.length == 0 && Re(s.contentDOM, "Delete", 46)))
    return !0;
  let r = t.insert.toString();
  s.inputState.composing >= 0 && s.inputState.composing++;
  let o, l = () => o || (o = Lf(s, t, e));
  return s.state.facet(Il).some((a) => a(s, t.from, t.to, r, l)) || s.dispatch(l()), !0;
}
function Lf(s, t, e) {
  let i, n = s.state, r = n.selection.main, o = -1;
  if (t.from == t.to && t.from < r.from || t.from > r.to) {
    let a = t.from < r.from ? -1 : 1, h = a < 0 ? r.from : r.to, f = ri(n.facet(ki).map((c) => c(s)), h, a);
    t.from == f && (o = f);
  }
  if (o > -1)
    i = {
      changes: t,
      selection: x.cursor(t.from + t.insert.length, -1)
    };
  else if (t.from >= r.from && t.to <= r.to && t.to - t.from >= (r.to - r.from) / 3 && (!e || e.main.empty && e.main.from == t.from + t.insert.length) && s.inputState.composing < 0) {
    let a = r.from < t.from ? n.sliceDoc(r.from, t.from) : "", h = r.to > t.to ? n.sliceDoc(t.to, r.to) : "";
    i = n.replaceSelection(s.state.toText(a + t.insert.sliceString(0, void 0, s.state.lineBreak) + h));
  } else {
    let a = n.changes(t), h = e && e.main.to <= a.newLength ? e.main : void 0;
    if (n.selection.ranges.length > 1 && (s.inputState.composing >= 0 || s.inputState.compositionPendingChange) && t.to <= r.to + 10 && t.to >= r.to - 10) {
      let f = s.state.sliceDoc(t.from, t.to), c, u = e && Xl(s, e.main.head);
      if (u) {
        let p = t.insert.length - (t.to - t.from);
        c = { from: u.from, to: u.to - p };
      } else
        c = s.state.doc.lineAt(r.head);
      let d = r.to - t.to;
      i = n.changeByRange((p) => {
        if (p.from == r.from && p.to == r.to)
          return { changes: a, range: h || p.map(a) };
        let g = p.to - d, m = g - f.length;
        if (s.state.sliceDoc(m, g) != f || // Unfortunately, there's no way to make multiple
        // changes in the same node work without aborting
        // composition, so cursors in the composition range are
        // ignored.
        g >= c.from && m <= c.to)
          return { range: p };
        let y = n.changes({ from: m, to: g, insert: t.insert }), w = p.to - r.to;
        return {
          changes: y,
          range: h ? x.range(Math.max(0, h.anchor + w), Math.max(0, h.head + w)) : p.map(y)
        };
      });
    } else
      i = {
        changes: a,
        selection: h && n.selection.replaceRange(h)
      };
  }
  let l = "input.type";
  return (s.composing || s.inputState.compositionPendingChange && s.inputState.compositionEndedAt > Date.now() - 50) && (s.inputState.compositionPendingChange = !1, l += ".compose", s.inputState.compositionFirstChange && (l += ".start", s.inputState.compositionFirstChange = !1)), n.update(i, { userEvent: l, scrollIntoView: !0 });
}
function _l(s, t, e, i) {
  let n = Math.min(s.length, t.length), r = 0;
  for (; r < n && s.charCodeAt(r) == t.charCodeAt(r); )
    r++;
  if (r == n && s.length == t.length)
    return null;
  let o = s.length, l = t.length;
  for (; o > 0 && l > 0 && s.charCodeAt(o - 1) == t.charCodeAt(l - 1); )
    o--, l--;
  if (i == "end") {
    let a = Math.max(0, r - Math.min(o, l));
    e -= o + a - r;
  }
  if (o < r && s.length < t.length) {
    let a = e <= r && e >= o ? r - e : 0;
    r -= a, l = r + (l - o), o = r;
  } else if (l < r) {
    let a = e <= r && e >= l ? r - e : 0;
    r -= a, o = r + (o - l), l = r;
  }
  return { from: r, toA: o, toB: l };
}
function Ef(s) {
  let t = [];
  if (s.root.activeElement != s.contentDOM)
    return t;
  let { anchorNode: e, anchorOffset: i, focusNode: n, focusOffset: r } = s.observer.selectionRange;
  return e && (t.push(new Ur(e, i)), (n != e || r != i) && t.push(new Ur(n, r))), t;
}
function If(s, t) {
  if (s.length == 0)
    return null;
  let e = s[0].pos, i = s.length == 2 ? s[1].pos : e;
  return e > -1 && i > -1 ? x.single(e + t, i + t) : null;
}
function hn(s, t) {
  return t.head == s.main.head && t.anchor == s.main.anchor;
}
class Nf {
  setSelectionOrigin(t) {
    this.lastSelectionOrigin = t, this.lastSelectionTime = Date.now();
  }
  constructor(t) {
    this.view = t, this.lastKeyCode = 0, this.lastKeyTime = 0, this.lastTouchTime = 0, this.lastTouchX = 0, this.lastTouchY = 0, this.lastFocusTime = 0, this.lastScrollTop = 0, this.lastScrollLeft = 0, this.lastWheelEvent = 0, this.pendingIOSKey = void 0, this.tabFocusMode = -1, this.lastSelectionOrigin = null, this.lastSelectionTime = 0, this.lastContextMenu = 0, this.scrollHandlers = [], this.handlers = /* @__PURE__ */ Object.create(null), this.composing = -1, this.compositionFirstChange = null, this.compositionEndedAt = 0, this.compositionPendingKey = !1, this.compositionPendingChange = !1, this.insertingText = "", this.insertingTextAt = 0, this.mouseSelection = null, this.draggedContent = null, this.handleEvent = this.handleEvent.bind(this), this.notifiedFocused = t.hasFocus, S.safari && t.contentDOM.addEventListener("input", () => null), S.gecko && Gf(t.contentDOM.ownerDocument);
  }
  handleEvent(t) {
    !jf(this.view, t) || this.ignoreDuringComposition(t) || t.type == "keydown" && this.keydown(t) || (this.view.updateState != 0 ? Promise.resolve().then(() => this.runHandlers(t.type, t)) : this.runHandlers(t.type, t));
  }
  runHandlers(t, e) {
    let i = this.handlers[t];
    if (i) {
      for (let n of i.observers)
        n(this.view, e);
      for (let n of i.handlers) {
        if (e.defaultPrevented)
          break;
        if (n(this.view, e)) {
          e.preventDefault();
          break;
        }
      }
    }
  }
  ensureHandlers(t) {
    let e = Vf(t), i = this.handlers, n = this.view.contentDOM;
    for (let r in e)
      if (r != "scroll") {
        let o = !e[r].handlers.length, l = i[r];
        l && o != !l.handlers.length && (n.removeEventListener(r, this.handleEvent), l = null), l || n.addEventListener(r, this.handleEvent, { passive: o });
      }
    for (let r in i)
      r != "scroll" && !e[r] && n.removeEventListener(r, this.handleEvent);
    this.handlers = e;
  }
  keydown(t) {
    if (this.lastKeyCode = t.keyCode, this.lastKeyTime = Date.now(), t.keyCode == 9 && this.tabFocusMode > -1 && (!this.tabFocusMode || Date.now() <= this.tabFocusMode))
      return !0;
    if (this.tabFocusMode > 0 && t.keyCode != 27 && Gl.indexOf(t.keyCode) < 0 && (this.tabFocusMode = -1), S.android && S.chrome && !t.synthetic && (t.keyCode == 13 || t.keyCode == 8))
      return this.view.observer.delayAndroidKey(t.key, t.keyCode), !0;
    let e;
    return S.ios && !t.synthetic && !t.altKey && !t.metaKey && ((e = Yl.find((i) => i.keyCode == t.keyCode)) && !t.ctrlKey || Wf.indexOf(t.key) > -1 && t.ctrlKey && !t.shiftKey) ? (this.pendingIOSKey = e || t, setTimeout(() => this.flushIOSKey(), 250), !0) : (t.keyCode != 229 && this.view.observer.forceFlush(), !1);
  }
  flushIOSKey(t) {
    let e = this.pendingIOSKey;
    return !e || e.key == "Enter" && t && t.from < t.to && /^\S+$/.test(t.insert.toString()) ? !1 : (this.pendingIOSKey = void 0, Re(this.view.contentDOM, e.key, e.keyCode, e instanceof KeyboardEvent ? e : void 0));
  }
  ignoreDuringComposition(t) {
    return !/^key/.test(t.type) || t.synthetic ? !1 : this.composing > 0 ? !0 : S.safari && !S.ios && this.compositionPendingKey && Date.now() - this.compositionEndedAt < 100 ? (this.compositionPendingKey = !1, !0) : !1;
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
function Kr(s, t) {
  return (e, i) => {
    try {
      return t.call(s, i, e);
    } catch (n) {
      ft(e.state, n);
    }
  };
}
function Vf(s) {
  let t = /* @__PURE__ */ Object.create(null);
  function e(i) {
    return t[i] || (t[i] = { observers: [], handlers: [] });
  }
  for (let i of s) {
    let n = i.spec, r = n && n.plugin.domEventHandlers, o = n && n.plugin.domEventObservers;
    if (r)
      for (let l in r) {
        let a = r[l];
        a && e(l).handlers.push(Kr(i.value, a));
      }
    if (o)
      for (let l in o) {
        let a = o[l];
        a && e(l).observers.push(Kr(i.value, a));
      }
  }
  for (let i in Tt)
    e(i).handlers.push(Tt[i]);
  for (let i in ut)
    e(i).observers.push(ut[i]);
  return t;
}
const Yl = [
  { key: "Backspace", keyCode: 8, inputType: "deleteContentBackward" },
  { key: "Enter", keyCode: 13, inputType: "insertParagraph" },
  { key: "Enter", keyCode: 13, inputType: "insertLineBreak" },
  { key: "Delete", keyCode: 46, inputType: "deleteContentForward" }
], Wf = "dthko", Gl = [16, 17, 18, 20, 91, 92, 224, 225], Mi = 6;
function Di(s) {
  return Math.max(0, s) * 0.7 + 8;
}
function Hf(s, t) {
  return Math.max(Math.abs(s.clientX - t.clientX), Math.abs(s.clientY - t.clientY));
}
class Ff {
  constructor(t, e, i, n) {
    this.view = t, this.startEvent = e, this.style = i, this.mustSelect = n, this.scrollSpeed = { x: 0, y: 0 }, this.scrolling = -1, this.lastEvent = e, this.scrollParents = kl(t.contentDOM), this.atoms = t.state.facet(ki).map((o) => o(t));
    let r = t.contentDOM.ownerDocument;
    r.addEventListener("mousemove", this.move = this.move.bind(this)), r.addEventListener("mouseup", this.up = this.up.bind(this)), this.extend = e.shiftKey, this.multiple = t.state.facet(N.allowMultipleSelections) && zf(t, e), this.dragging = $f(t, e) && ta(e) == 1 ? null : !1;
  }
  start(t) {
    this.dragging === !1 && this.select(t);
  }
  move(t) {
    if (t.buttons == 0)
      return this.destroy();
    if (this.dragging || this.dragging == null && Hf(this.startEvent, t) < 10)
      return;
    this.select(this.lastEvent = t);
    let e = 0, i = 0, n = 0, r = 0, o = this.view.win.innerWidth, l = this.view.win.innerHeight;
    this.scrollParents.x && ({ left: n, right: o } = this.scrollParents.x.getBoundingClientRect()), this.scrollParents.y && ({ top: r, bottom: l } = this.scrollParents.y.getBoundingClientRect());
    let a = sr(this.view);
    t.clientX - a.left <= n + Mi ? e = -Di(n - t.clientX) : t.clientX + a.right >= o - Mi && (e = Di(t.clientX - o)), t.clientY - a.top <= r + Mi ? i = -Di(r - t.clientY) : t.clientY + a.bottom >= l - Mi && (i = Di(t.clientY - l)), this.setScrollSpeed(e, i);
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
    let { view: e } = this, i = ql(this.atoms, this.style.get(t, this.extend, this.multiple));
    (this.mustSelect || !i.eq(e.state.selection, this.dragging === !1)) && this.view.dispatch({
      selection: i,
      userEvent: "select.pointer"
    }), this.mustSelect = !1;
  }
  update(t) {
    t.transactions.some((e) => e.isUserEvent("input.type")) ? this.destroy() : this.style.update(t) && setTimeout(() => this.select(this.lastEvent), 20);
  }
}
function zf(s, t) {
  let e = s.state.facet(Rl);
  return e.length ? e[0](t) : S.mac ? t.metaKey : t.ctrlKey;
}
function Qf(s, t) {
  let e = s.state.facet(Bl);
  return e.length ? e[0](t) : S.mac ? !t.altKey : !t.ctrlKey;
}
function $f(s, t) {
  let { main: e } = s.state.selection;
  if (e.empty)
    return !1;
  let i = ui(s.root);
  if (!i || i.rangeCount == 0)
    return !0;
  let n = i.getRangeAt(0).getClientRects();
  for (let r = 0; r < n.length; r++) {
    let o = n[r];
    if (o.left <= t.clientX && o.right >= t.clientX && o.top <= t.clientY && o.bottom >= t.clientY)
      return !0;
  }
  return !1;
}
function jf(s, t) {
  if (!t.bubbles)
    return !0;
  if (t.defaultPrevented)
    return !1;
  for (let e = t.target, i; e != s.contentDOM; e = e.parentNode)
    if (!e || e.nodeType == 11 || (i = U.get(e)) && i.isWidget() && !i.isHidden && i.widget.ignoreEvent(t))
      return !1;
  return !0;
}
const Tt = /* @__PURE__ */ Object.create(null), ut = /* @__PURE__ */ Object.create(null), Zl = S.ie && S.ie_version < 15 || S.ios && S.webkit_version < 604;
function Xf(s) {
  let t = s.dom.parentNode;
  if (!t)
    return;
  let e = t.appendChild(document.createElement("textarea"));
  e.style.cssText = "position: fixed; left: -10000px; top: 10px", e.focus(), setTimeout(() => {
    s.focus(), e.remove(), Jl(s, e.value);
  }, 50);
}
function Bn(s, t, e) {
  for (let i of s.facet(t))
    e = i(e, s);
  return e;
}
function Jl(s, t) {
  t = Bn(s.state, tr, t);
  let { state: e } = s, i, n = 1, r = e.toText(t), o = r.lines == e.selection.ranges.length;
  if (Rs != null && e.selection.ranges.every((a) => a.empty) && Rs == r.toString()) {
    let a = -1;
    i = e.changeByRange((h) => {
      let f = e.doc.lineAt(h.from);
      if (f.from == a)
        return { range: h };
      a = f.from;
      let c = e.toText((o ? r.line(n++).text : t) + e.lineBreak);
      return {
        changes: { from: f.from, insert: c },
        range: x.cursor(h.from + c.length)
      };
    });
  } else o ? i = e.changeByRange((a) => {
    let h = r.line(n++);
    return {
      changes: { from: a.from, to: a.to, insert: h.text },
      range: x.cursor(a.from + h.length)
    };
  }) : i = e.replaceSelection(r);
  s.dispatch(i, {
    userEvent: "input.paste",
    scrollIntoView: !0
  });
}
ut.scroll = (s) => {
  s.inputState.lastScrollTop = s.scrollDOM.scrollTop, s.inputState.lastScrollLeft = s.scrollDOM.scrollLeft;
};
ut.wheel = ut.mousewheel = (s) => {
  s.inputState.lastWheelEvent = Date.now();
};
Tt.keydown = (s, t) => (s.inputState.setSelectionOrigin("select"), t.keyCode == 27 && s.inputState.tabFocusMode != 0 && (s.inputState.tabFocusMode = Date.now() + 2e3), !1);
ut.touchstart = (s, t) => {
  let e = s.inputState, i = t.targetTouches[0];
  e.lastTouchTime = Date.now(), i && (e.lastTouchX = i.clientX, e.lastTouchY = i.clientY), e.setSelectionOrigin("select.pointer");
};
ut.touchmove = (s) => {
  s.inputState.setSelectionOrigin("select.pointer");
};
Tt.mousedown = (s, t) => {
  if (s.observer.flush(), s.inputState.lastTouchTime > Date.now() - 2e3)
    return !1;
  let e = null;
  for (let i of s.state.facet(Ll))
    if (e = i(s, t), e)
      break;
  if (!e && t.button == 0 && (e = Uf(s, t)), e) {
    let i = !s.hasFocus;
    s.inputState.startMouseSelection(new Ff(s, t, e, i)), i && s.observer.ignore(() => {
      Sl(s.contentDOM);
      let r = s.root.activeElement;
      r && !r.contains(s.contentDOM) && r.blur();
    });
    let n = s.inputState.mouseSelection;
    if (n)
      return n.start(t), n.dragging === !1;
  } else
    s.inputState.setSelectionOrigin("select.pointer");
  return !1;
};
function _r(s, t, e, i) {
  if (i == 1)
    return x.cursor(t, e);
  if (i == 2)
    return Sf(s.state, t, e);
  {
    let n = s.docView.lineAt(t, e), r = s.state.doc.lineAt(n ? n.posAtEnd : t), o = n ? n.posAtStart : r.from, l = n ? n.posAtEnd : r.to;
    return l < s.state.doc.length && l == r.to && l++, x.range(o, l);
  }
}
const qf = S.ie && S.ie_version <= 11;
let Yr = null, Gr = 0, Zr = 0;
function ta(s) {
  if (!qf)
    return s.detail;
  let t = Yr, e = Zr;
  return Yr = s, Zr = Date.now(), Gr = !t || e > Date.now() - 400 && Math.abs(t.clientX - s.clientX) < 2 && Math.abs(t.clientY - s.clientY) < 2 ? (Gr + 1) % 3 : 1;
}
function Uf(s, t) {
  let e = s.posAndSideAtCoords({ x: t.clientX, y: t.clientY }, !1), i = ta(t), n = s.state.selection;
  return {
    update(r) {
      r.docChanged && (e.pos = r.changes.mapPos(e.pos), n = n.map(r.changes));
    },
    get(r, o, l) {
      let a = s.posAndSideAtCoords({ x: r.clientX, y: r.clientY }, !1), h, f = _r(s, a.pos, a.assoc, i);
      if (e.pos != a.pos && !o) {
        let c = _r(s, e.pos, e.assoc, i), u = Math.min(c.from, f.from), d = Math.max(c.to, f.to);
        f = u < f.from ? x.range(u, d) : x.range(d, u);
      }
      return o ? n.replaceRange(n.main.extend(f.from, f.to)) : l && i == 1 && n.ranges.length > 1 && (h = Kf(n, a.pos)) ? h : l ? n.addRange(f) : x.create([f]);
    }
  };
}
function Kf(s, t) {
  for (let e = 0; e < s.ranges.length; e++) {
    let { from: i, to: n } = s.ranges[e];
    if (i <= t && n >= t)
      return x.create(s.ranges.slice(0, e).concat(s.ranges.slice(e + 1)), s.mainIndex == e ? 0 : s.mainIndex - (s.mainIndex > e ? 1 : 0));
  }
  return null;
}
Tt.dragstart = (s, t) => {
  let { selection: { main: e } } = s.state;
  if (t.target.draggable) {
    let n = s.docView.tile.nearest(t.target);
    if (n && n.isWidget()) {
      let r = n.posAtStart, o = r + n.length;
      (r >= e.to || o <= e.from) && (e = x.range(r, o));
    }
  }
  let { inputState: i } = s;
  return i.mouseSelection && (i.mouseSelection.dragging = !0), i.draggedContent = e, t.dataTransfer && (t.dataTransfer.setData("Text", Bn(s.state, er, s.state.sliceDoc(e.from, e.to))), t.dataTransfer.effectAllowed = "copyMove"), !1;
};
Tt.dragend = (s) => (s.inputState.draggedContent = null, !1);
function Jr(s, t, e, i) {
  if (e = Bn(s.state, tr, e), !e)
    return;
  let n = s.posAtCoords({ x: t.clientX, y: t.clientY }, !1), { draggedContent: r } = s.inputState, o = i && r && Qf(s, t) ? { from: r.from, to: r.to } : null, l = { from: n, insert: e }, a = s.state.changes(o ? [o, l] : l);
  s.focus(), s.dispatch({
    changes: a,
    selection: { anchor: a.mapPos(n, -1), head: a.mapPos(n, 1) },
    userEvent: o ? "move.drop" : "input.drop"
  }), s.inputState.draggedContent = null;
}
Tt.drop = (s, t) => {
  if (!t.dataTransfer)
    return !1;
  if (s.state.readOnly)
    return !0;
  let e = t.dataTransfer.files;
  if (e && e.length) {
    let i = Array(e.length), n = 0, r = () => {
      ++n == e.length && Jr(s, t, i.filter((o) => o != null).join(s.state.lineBreak), !1);
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
      return Jr(s, t, i, !0), !0;
  }
  return !1;
};
Tt.paste = (s, t) => {
  if (s.state.readOnly)
    return !0;
  s.observer.flush();
  let e = Zl ? null : t.clipboardData;
  return e ? (Jl(s, e.getData("text/plain") || e.getData("text/uri-list")), !0) : (Xf(s), !1);
};
function _f(s, t) {
  let e = s.dom.parentNode;
  if (!e)
    return;
  let i = e.appendChild(document.createElement("textarea"));
  i.style.cssText = "position: fixed; left: -10000px; top: 10px", i.value = t, i.focus(), i.selectionEnd = t.length, i.selectionStart = 0, setTimeout(() => {
    i.remove(), s.focus();
  }, 50);
}
function Yf(s) {
  let t = [], e = [], i = !1;
  for (let n of s.selection.ranges)
    n.empty || (t.push(s.sliceDoc(n.from, n.to)), e.push(n));
  if (!t.length) {
    let n = -1;
    for (let { from: r } of s.selection.ranges) {
      let o = s.doc.lineAt(r);
      o.number > n && (t.push(o.text), e.push({ from: o.from, to: Math.min(s.doc.length, o.to + 1) })), n = o.number;
    }
    i = !0;
  }
  return { text: Bn(s, er, t.join(s.lineBreak)), ranges: e, linewise: i };
}
let Rs = null;
Tt.copy = Tt.cut = (s, t) => {
  if (!ni(s.contentDOM, s.observer.selectionRange))
    return !1;
  let { text: e, ranges: i, linewise: n } = Yf(s.state);
  if (!e && !n)
    return !1;
  Rs = n ? e : null, t.type == "cut" && !s.state.readOnly && s.dispatch({
    changes: i,
    scrollIntoView: !0,
    userEvent: "delete.cut"
  });
  let r = Zl ? null : t.clipboardData;
  return r ? (r.clearData(), r.setData("text/plain", e), !0) : (_f(s, e), !1);
};
const ea = /* @__PURE__ */ Gt.define();
function ia(s, t) {
  let e = [];
  for (let i of s.facet(Nl)) {
    let n = i(s, t);
    n && e.push(n);
  }
  return e.length ? s.update({ effects: e, annotations: ea.of(!0) }) : null;
}
function na(s) {
  setTimeout(() => {
    let t = s.hasFocus;
    if (t != s.inputState.notifiedFocused) {
      let e = ia(s.state, t);
      e ? s.dispatch(e) : s.update([]);
    }
  }, 10);
}
ut.focus = (s) => {
  s.inputState.lastFocusTime = Date.now(), !s.scrollDOM.scrollTop && (s.inputState.lastScrollTop || s.inputState.lastScrollLeft) && (s.scrollDOM.scrollTop = s.inputState.lastScrollTop, s.scrollDOM.scrollLeft = s.inputState.lastScrollLeft), na(s);
};
ut.blur = (s) => {
  s.observer.clearSelectionRange(), na(s);
};
ut.compositionstart = ut.compositionupdate = (s) => {
  s.observer.editContext || (s.inputState.compositionFirstChange == null && (s.inputState.compositionFirstChange = !0), s.inputState.composing < 0 && (s.inputState.composing = 0));
};
ut.compositionend = (s) => {
  s.observer.editContext || (s.inputState.composing = -1, s.inputState.compositionEndedAt = Date.now(), s.inputState.compositionPendingKey = !0, s.inputState.compositionPendingChange = s.observer.pendingRecords().length > 0, s.inputState.compositionFirstChange = null, S.chrome && S.android ? s.observer.flushSoon() : s.inputState.compositionPendingChange ? Promise.resolve().then(() => s.observer.flush()) : setTimeout(() => {
    s.inputState.composing < 0 && s.docView.hasComposition && s.update([]);
  }, 50));
};
ut.contextmenu = (s) => {
  s.inputState.lastContextMenu = Date.now();
};
Tt.beforeinput = (s, t) => {
  var e, i;
  if ((t.inputType == "insertText" || t.inputType == "insertCompositionText") && (s.inputState.insertingText = t.data, s.inputState.insertingTextAt = Date.now()), t.inputType == "insertReplacementText" && s.observer.editContext) {
    let r = (e = t.dataTransfer) === null || e === void 0 ? void 0 : e.getData("text/plain"), o = t.getTargetRanges();
    if (r && o.length) {
      let l = o[0], a = s.posAtDOM(l.startContainer, l.startOffset), h = s.posAtDOM(l.endContainer, l.endOffset);
      return rr(s, { from: a, to: h, insert: s.state.toText(r) }, null), !0;
    }
  }
  let n;
  if (S.chrome && S.android && (n = Yl.find((r) => r.inputType == t.inputType)) && (s.observer.delayAndroidKey(n.key, n.keyCode), n.key == "Backspace" || n.key == "Delete")) {
    let r = ((i = window.visualViewport) === null || i === void 0 ? void 0 : i.height) || 0;
    setTimeout(() => {
      var o;
      (((o = window.visualViewport) === null || o === void 0 ? void 0 : o.height) || 0) > r + 10 && s.hasFocus && (s.contentDOM.blur(), s.focus());
    }, 100);
  }
  return S.ios && t.inputType == "deleteContentForward" && s.observer.flushSoon(), S.safari && t.inputType == "insertText" && s.inputState.composing >= 0 && setTimeout(() => ut.compositionend(s, t), 20), !1;
};
const to = /* @__PURE__ */ new Set();
function Gf(s) {
  to.has(s) || (to.add(s), s.addEventListener("copy", () => {
  }), s.addEventListener("cut", () => {
  }));
}
const eo = ["pre-wrap", "normal", "pre-line", "break-spaces"];
let He = !1;
function io() {
  He = !1;
}
class Zf {
  constructor(t) {
    this.lineWrapping = t, this.doc = E.empty, this.heightSamples = {}, this.lineHeight = 14, this.charWidth = 7, this.textHeight = 14, this.lineLength = 30;
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
    return eo.indexOf(t) > -1 != this.lineWrapping;
  }
  mustRefreshForHeights(t) {
    let e = !1;
    for (let i = 0; i < t.length; i++) {
      let n = t[i];
      n < 0 ? i++ : this.heightSamples[Math.floor(n * 10)] || (e = !0, this.heightSamples[Math.floor(n * 10)] = !0);
    }
    return e;
  }
  refresh(t, e, i, n, r, o) {
    let l = eo.indexOf(t) > -1, a = Math.abs(e - this.lineHeight) > 0.3 || this.lineWrapping != l || Math.abs(i - this.charWidth) > 0.1;
    if (this.lineWrapping = l, this.lineHeight = e, this.charWidth = i, this.textHeight = n, this.lineLength = r, a) {
      this.heightSamples = {};
      for (let h = 0; h < o.length; h++) {
        let f = o[h];
        f < 0 ? h++ : this.heightSamples[Math.floor(f * 10)] = !0;
      }
    }
    return a;
  }
}
class Jf {
  constructor(t, e) {
    this.from = t, this.heights = e, this.index = 0;
  }
  get more() {
    return this.index < this.heights.length;
  }
}
class Ot {
  /**
  @internal
  */
  constructor(t, e, i, n, r) {
    this.from = t, this.length = e, this.top = i, this.height = n, this._content = r;
  }
  /**
  The type of element this is. When querying lines, this may be
  an array of all the blocks that make up the line.
  */
  get type() {
    return typeof this._content == "number" ? kt.Text : Array.isArray(this._content) ? this._content : this._content.type;
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
    return this._content instanceof xe ? this._content.widget : null;
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
    return new Ot(this.from, this.length + t.length, this.top, this.height + t.height, e);
  }
}
var F = /* @__PURE__ */ (function(s) {
  return s[s.ByPos = 0] = "ByPos", s[s.ByHeight = 1] = "ByHeight", s[s.ByPosNoHeight = 2] = "ByPosNoHeight", s;
})(F || (F = {}));
const Gi = 1e-3;
class ot {
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
    this.height != t && (Math.abs(this.height - t) > Gi && (He = !0), this.height = t);
  }
  // Base case is to replace a leaf node, which simply builds a tree
  // from the new nodes and returns that (HeightMapBranch and
  // HeightMapGap override this to actually use from/to)
  replace(t, e, i) {
    return ot.of(i);
  }
  // Again, these are base cases, and are overridden for branch and gap nodes.
  decomposeLeft(t, e) {
    e.push(this);
  }
  decomposeRight(t, e) {
    e.push(this);
  }
  applyChanges(t, e, i, n) {
    let r = this, o = i.doc;
    for (let l = n.length - 1; l >= 0; l--) {
      let { fromA: a, toA: h, fromB: f, toB: c } = n[l], u = r.lineAt(a, F.ByPosNoHeight, i.setDoc(e), 0, 0), d = u.to >= h ? u : r.lineAt(h, F.ByPosNoHeight, i, 0, 0);
      for (c += d.to - h, h = d.to; l > 0 && u.from <= n[l - 1].toA; )
        a = n[l - 1].fromA, f = n[l - 1].fromB, l--, a < u.from && (u = r.lineAt(a, F.ByPosNoHeight, i, 0, 0));
      f += u.from - a, a = u.from;
      let p = or.build(i.setDoc(o), t, f, c);
      r = fn(r, r.replace(a, h, p));
    }
    return r.updateHeight(i, 0);
  }
  static empty() {
    return new gt(0, 0, 0);
  }
  // nodes uses null values to indicate the position of line breaks.
  // There are never line breaks at the start or end of the array, or
  // two line breaks next to each other, and the array isn't allowed
  // to be empty (same restrictions as return value from the builder).
  static of(t) {
    if (t.length == 1)
      return t[0];
    let e = 0, i = t.length, n = 0, r = 0;
    for (; ; )
      if (e == i)
        if (n > r * 2) {
          let l = t[e - 1];
          l.break ? t.splice(--e, 1, l.left, null, l.right) : t.splice(--e, 1, l.left, l.right), i += 1 + l.break, n -= l.size;
        } else if (r > n * 2) {
          let l = t[i];
          l.break ? t.splice(i, 1, l.left, null, l.right) : t.splice(i, 1, l.left, l.right), i += 2 + l.break, r -= l.size;
        } else
          break;
      else if (n < r) {
        let l = t[e++];
        l && (n += l.size);
      } else {
        let l = t[--i];
        l && (r += l.size);
      }
    let o = 0;
    return t[e - 1] == null ? (o = 1, e--) : t[e] == null && (o = 1, i++), new ec(ot.of(t.slice(0, e)), o, ot.of(t.slice(i)));
  }
}
function fn(s, t) {
  return s == t ? s : (s.constructor != t.constructor && (He = !0), t);
}
ot.prototype.size = 1;
const tc = /* @__PURE__ */ q.replace({});
class sa extends ot {
  constructor(t, e, i) {
    super(t, e), this.deco = i, this.spaceAbove = 0;
  }
  mainBlock(t, e) {
    return new Ot(e, this.length, t + this.spaceAbove, this.height - this.spaceAbove, this.deco || 0);
  }
  blockAt(t, e, i, n) {
    return this.spaceAbove && t < i + this.spaceAbove ? new Ot(n, 0, i, this.spaceAbove, tc) : this.mainBlock(i, n);
  }
  lineAt(t, e, i, n, r) {
    let o = this.mainBlock(n, r);
    return this.spaceAbove ? this.blockAt(0, i, n, r).join(o) : o;
  }
  forEachLine(t, e, i, n, r, o) {
    t <= r + this.length && e >= r && o(this.lineAt(0, F.ByPos, i, n, r));
  }
  setMeasuredHeight(t) {
    let e = t.heights[t.index++];
    e < 0 ? (this.spaceAbove = -e, e = t.heights[t.index++]) : this.spaceAbove = 0, this.setHeight(e);
  }
  updateHeight(t, e = 0, i = !1, n) {
    return n && n.from <= e && n.more && this.setMeasuredHeight(n), this.outdated = !1, this;
  }
  toString() {
    return `block(${this.length})`;
  }
}
class gt extends sa {
  constructor(t, e, i) {
    super(t, e, null), this.collapsed = 0, this.widgetHeight = 0, this.breaks = 0, this.spaceAbove = i;
  }
  mainBlock(t, e) {
    return new Ot(e, this.length, t + this.spaceAbove, this.height - this.spaceAbove, this.breaks);
  }
  replace(t, e, i) {
    let n = i[0];
    return i.length == 1 && (n instanceof gt || n instanceof tt && n.flags & 4) && Math.abs(this.length - n.length) < 10 ? (n instanceof tt ? n = new gt(n.length, this.height, this.spaceAbove) : n.height = this.height, this.outdated || (n.outdated = !1), n) : ot.of(i);
  }
  updateHeight(t, e = 0, i = !1, n) {
    return n && n.from <= e && n.more ? this.setMeasuredHeight(n) : (i || this.outdated) && (this.spaceAbove = 0, this.setHeight(Math.max(this.widgetHeight, t.heightForLine(this.length - this.collapsed)) + this.breaks * t.lineHeight)), this.outdated = !1, this;
  }
  toString() {
    return `line(${this.length}${this.collapsed ? -this.collapsed : ""}${this.widgetHeight ? ":" + this.widgetHeight : ""})`;
  }
}
class tt extends ot {
  constructor(t) {
    super(t, 0);
  }
  heightMetrics(t, e) {
    let i = t.doc.lineAt(e).number, n = t.doc.lineAt(e + this.length).number, r = n - i + 1, o, l = 0;
    if (t.lineWrapping) {
      let a = Math.min(this.height, t.lineHeight * r);
      o = a / r, this.length > r + 1 && (l = (this.height - a) / (this.length - r - 1));
    } else
      o = this.height / r;
    return { firstLine: i, lastLine: n, perLine: o, perChar: l };
  }
  blockAt(t, e, i, n) {
    let { firstLine: r, lastLine: o, perLine: l, perChar: a } = this.heightMetrics(e, n);
    if (e.lineWrapping) {
      let h = n + (t < e.lineHeight ? 0 : Math.round(Math.max(0, Math.min(1, (t - i) / this.height)) * this.length)), f = e.doc.lineAt(h), c = l + f.length * a, u = Math.max(i, t - c / 2);
      return new Ot(f.from, f.length, u, c, 0);
    } else {
      let h = Math.max(0, Math.min(o - r, Math.floor((t - i) / l))), { from: f, length: c } = e.doc.line(r + h);
      return new Ot(f, c, i + l * h, l, 0);
    }
  }
  lineAt(t, e, i, n, r) {
    if (e == F.ByHeight)
      return this.blockAt(t, i, n, r);
    if (e == F.ByPosNoHeight) {
      let { from: d, to: p } = i.doc.lineAt(t);
      return new Ot(d, p - d, 0, 0, 0);
    }
    let { firstLine: o, perLine: l, perChar: a } = this.heightMetrics(i, r), h = i.doc.lineAt(t), f = l + h.length * a, c = h.number - o, u = n + l * c + a * (h.from - r - c);
    return new Ot(h.from, h.length, Math.max(n, Math.min(u, n + this.height - f)), f, 0);
  }
  forEachLine(t, e, i, n, r, o) {
    t = Math.max(t, r), e = Math.min(e, r + this.length);
    let { firstLine: l, perLine: a, perChar: h } = this.heightMetrics(i, r);
    for (let f = t, c = n; f <= e; ) {
      let u = i.doc.lineAt(f);
      if (f == t) {
        let p = u.number - l;
        c += a * p + h * (t - r - p);
      }
      let d = a + h * u.length;
      o(new Ot(u.from, u.length, c, d, 0)), c += d, f = u.to + 1;
    }
  }
  replace(t, e, i) {
    let n = this.length - e;
    if (n > 0) {
      let r = i[i.length - 1];
      r instanceof tt ? i[i.length - 1] = new tt(r.length + n) : i.push(null, new tt(n - 1));
    }
    if (t > 0) {
      let r = i[0];
      r instanceof tt ? i[0] = new tt(t + r.length) : i.unshift(new tt(t - 1), null);
    }
    return ot.of(i);
  }
  decomposeLeft(t, e) {
    e.push(new tt(t - 1), null);
  }
  decomposeRight(t, e) {
    e.push(null, new tt(this.length - t - 1));
  }
  updateHeight(t, e = 0, i = !1, n) {
    let r = e + this.length;
    if (n && n.from <= e + this.length && n.more) {
      let o = [], l = Math.max(e, n.from), a = -1;
      for (n.from > e && o.push(new tt(n.from - e - 1).updateHeight(t, e)); l <= r && n.more; ) {
        let f = t.doc.lineAt(l).length;
        o.length && o.push(null);
        let c = n.heights[n.index++], u = 0;
        c < 0 && (u = -c, c = n.heights[n.index++]), a == -1 ? a = c : Math.abs(c - a) >= Gi && (a = -2);
        let d = new gt(f, c, u);
        d.outdated = !1, o.push(d), l += f + 1;
      }
      l <= r && o.push(null, new tt(r - l).updateHeight(t, l));
      let h = ot.of(o);
      return (a < 0 || Math.abs(h.height - this.height) >= Gi || Math.abs(a - this.heightMetrics(t, e).perLine) >= Gi) && (He = !0), fn(this, h);
    } else (i || this.outdated) && (this.setHeight(t.heightForGap(e, e + this.length)), this.outdated = !1);
    return this;
  }
  toString() {
    return `gap(${this.length})`;
  }
}
class ec extends ot {
  constructor(t, e, i) {
    super(t.length + e + i.length, t.height + i.height, e | (t.outdated || i.outdated ? 2 : 0)), this.left = t, this.right = i, this.size = t.size + i.size;
  }
  get break() {
    return this.flags & 1;
  }
  blockAt(t, e, i, n) {
    let r = i + this.left.height;
    return t < r ? this.left.blockAt(t, e, i, n) : this.right.blockAt(t, e, r, n + this.left.length + this.break);
  }
  lineAt(t, e, i, n, r) {
    let o = n + this.left.height, l = r + this.left.length + this.break, a = e == F.ByHeight ? t < o : t < l, h = a ? this.left.lineAt(t, e, i, n, r) : this.right.lineAt(t, e, i, o, l);
    if (this.break || (a ? h.to < l : h.from > l))
      return h;
    let f = e == F.ByPosNoHeight ? F.ByPosNoHeight : F.ByPos;
    return a ? h.join(this.right.lineAt(l, f, i, o, l)) : this.left.lineAt(l, f, i, n, r).join(h);
  }
  forEachLine(t, e, i, n, r, o) {
    let l = n + this.left.height, a = r + this.left.length + this.break;
    if (this.break)
      t < a && this.left.forEachLine(t, e, i, n, r, o), e >= a && this.right.forEachLine(t, e, i, l, a, o);
    else {
      let h = this.lineAt(a, F.ByPos, i, n, r);
      t < h.from && this.left.forEachLine(t, h.from - 1, i, n, r, o), h.to >= t && h.from <= e && o(h), e > h.to && this.right.forEachLine(h.to + 1, e, i, l, a, o);
    }
  }
  replace(t, e, i) {
    let n = this.left.length + this.break;
    if (e < n)
      return this.balanced(this.left.replace(t, e, i), this.right);
    if (t > this.left.length)
      return this.balanced(this.left, this.right.replace(t - n, e - n, i));
    let r = [];
    t > 0 && this.decomposeLeft(t, r);
    let o = r.length;
    for (let l of i)
      r.push(l);
    if (t > 0 && no(r, o - 1), e < this.length) {
      let l = r.length;
      this.decomposeRight(e, r), no(r, l);
    }
    return ot.of(r);
  }
  decomposeLeft(t, e) {
    let i = this.left.length;
    if (t <= i)
      return this.left.decomposeLeft(t, e);
    e.push(this.left), this.break && (i++, t >= i && e.push(null)), t > i && this.right.decomposeLeft(t - i, e);
  }
  decomposeRight(t, e) {
    let i = this.left.length, n = i + this.break;
    if (t >= n)
      return this.right.decomposeRight(t - n, e);
    t < i && this.left.decomposeRight(t, e), this.break && t < n && e.push(null), e.push(this.right);
  }
  balanced(t, e) {
    return t.size > 2 * e.size || e.size > 2 * t.size ? ot.of(this.break ? [t, null, e] : [t, e]) : (this.left = fn(this.left, t), this.right = fn(this.right, e), this.setHeight(t.height + e.height), this.outdated = t.outdated || e.outdated, this.size = t.size + e.size, this.length = t.length + this.break + e.length, this);
  }
  updateHeight(t, e = 0, i = !1, n) {
    let { left: r, right: o } = this, l = e + r.length + this.break, a = null;
    return n && n.from <= e + r.length && n.more ? a = r = r.updateHeight(t, e, i, n) : r.updateHeight(t, e, i), n && n.from <= l + o.length && n.more ? a = o = o.updateHeight(t, l, i, n) : o.updateHeight(t, l, i), a ? this.balanced(r, o) : (this.height = this.left.height + this.right.height, this.outdated = !1, this);
  }
  toString() {
    return this.left + (this.break ? " " : "-") + this.right;
  }
}
function no(s, t) {
  let e, i;
  s[t] == null && (e = s[t - 1]) instanceof tt && (i = s[t + 1]) instanceof tt && s.splice(t - 1, 3, new tt(e.length + 1 + i.length));
}
const ic = 5;
class or {
  constructor(t, e) {
    this.pos = t, this.oracle = e, this.nodes = [], this.lineStart = -1, this.lineEnd = -1, this.covering = null, this.writtenTo = t;
  }
  get isCovered() {
    return this.covering && this.nodes[this.nodes.length - 1] == this.covering;
  }
  span(t, e) {
    if (this.lineStart > -1) {
      let i = Math.min(e, this.lineEnd), n = this.nodes[this.nodes.length - 1];
      n instanceof gt ? n.length += i - this.pos : (i > this.pos || !this.isCovered) && this.nodes.push(new gt(i - this.pos, -1, 0)), this.writtenTo = i, e > i && (this.nodes.push(null), this.writtenTo++, this.lineStart = -1);
    }
    this.pos = e;
  }
  point(t, e, i) {
    if (t < e || i.heightRelevant) {
      let n = i.widget ? i.widget.estimatedHeight : 0, r = i.widget ? i.widget.lineBreaks : 0;
      n < 0 && (n = this.oracle.lineHeight);
      let o = e - t;
      i.block ? this.addBlock(new sa(o, n, i)) : (o || r || n >= ic) && this.addLineDeco(n, r, o);
    } else e > t && this.span(t, e);
    this.lineEnd > -1 && this.lineEnd < this.pos && (this.lineEnd = this.oracle.doc.lineAt(this.pos).to);
  }
  enterLine() {
    if (this.lineStart > -1)
      return;
    let { from: t, to: e } = this.oracle.doc.lineAt(this.pos);
    this.lineStart = t, this.lineEnd = e, this.writtenTo < t && ((this.writtenTo < t - 1 || this.nodes[this.nodes.length - 1] == null) && this.nodes.push(this.blankContent(this.writtenTo, t - 1)), this.nodes.push(null)), this.pos > t && this.nodes.push(new gt(this.pos - t, -1, 0)), this.writtenTo = this.pos;
  }
  blankContent(t, e) {
    let i = new tt(e - t);
    return this.oracle.doc.lineAt(t).to == e && (i.flags |= 4), i;
  }
  ensureLine() {
    this.enterLine();
    let t = this.nodes.length ? this.nodes[this.nodes.length - 1] : null;
    if (t instanceof gt)
      return t;
    let e = new gt(0, -1, 0);
    return this.nodes.push(e), e;
  }
  addBlock(t) {
    this.enterLine();
    let e = t.deco;
    e && e.startSide > 0 && !this.isCovered && this.ensureLine(), this.nodes.push(t), this.writtenTo = this.pos = this.pos + t.length, e && e.endSide > 0 && (this.covering = t);
  }
  addLineDeco(t, e, i) {
    let n = this.ensureLine();
    n.length += i, n.collapsed += i, n.widgetHeight = Math.max(n.widgetHeight, t), n.breaks += e, this.writtenTo = this.pos = this.pos + i;
  }
  finish(t) {
    let e = this.nodes.length == 0 ? null : this.nodes[this.nodes.length - 1];
    this.lineStart > -1 && !(e instanceof gt) && !this.isCovered ? this.nodes.push(new gt(0, -1, 0)) : (this.writtenTo < this.pos || e == null) && this.nodes.push(this.blankContent(this.writtenTo, this.pos));
    let i = t;
    for (let n of this.nodes)
      n instanceof gt && n.updateHeight(this.oracle, i), i += n ? n.length : 1;
    return this.nodes;
  }
  // Always called with a region that on both sides either stretches
  // to a line break or the end of the document.
  // The returned array uses null to indicate line breaks, but never
  // starts or ends in a line break, or has multiple line breaks next
  // to each other.
  static build(t, e, i, n) {
    let r = new or(i, t);
    return V.spans(e, i, n, r, 0), r.finish(i);
  }
}
function nc(s, t, e) {
  let i = new sc();
  return V.compare(s, t, e, i, 0), i.changes;
}
class sc {
  constructor() {
    this.changes = [];
  }
  compareRange() {
  }
  comparePoint(t, e, i, n) {
    (t < e || i && i.heightRelevant || n && n.heightRelevant) && De(t, e, this.changes, 5);
  }
}
function rc(s, t) {
  let e = s.getBoundingClientRect(), i = s.ownerDocument, n = i.defaultView || window, r = Math.max(0, e.left), o = Math.min(n.innerWidth, e.right), l = Math.max(0, e.top), a = Math.min(n.innerHeight, e.bottom);
  for (let h = s.parentNode; h && h != i.body; )
    if (h.nodeType == 1) {
      let f = h, c = window.getComputedStyle(f);
      if ((f.scrollHeight > f.clientHeight || f.scrollWidth > f.clientWidth) && c.overflow != "visible") {
        let u = f.getBoundingClientRect();
        r = Math.max(r, u.left), o = Math.min(o, u.right), l = Math.max(l, u.top), a = Math.min(h == s.parentNode ? n.innerHeight : a, u.bottom);
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
function oc(s) {
  let t = s.getBoundingClientRect(), e = s.ownerDocument.defaultView || window;
  return t.left < e.innerWidth && t.right > 0 && t.top < e.innerHeight && t.bottom > 0;
}
function lc(s, t) {
  let e = s.getBoundingClientRect();
  return {
    left: 0,
    right: e.right - e.left,
    top: t,
    bottom: e.bottom - (e.top + t)
  };
}
class Kn {
  constructor(t, e, i, n) {
    this.from = t, this.to = e, this.size = i, this.displaySize = n;
  }
  static same(t, e) {
    if (t.length != e.length)
      return !1;
    for (let i = 0; i < t.length; i++) {
      let n = t[i], r = e[i];
      if (n.from != r.from || n.to != r.to || n.size != r.size)
        return !1;
    }
    return !0;
  }
  draw(t, e) {
    return q.replace({
      widget: new ac(this.displaySize * (e ? t.scaleY : t.scaleX), e)
    }).range(this.from, this.to);
  }
}
class ac extends bi {
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
class so {
  constructor(t, e) {
    this.view = t, this.state = e, this.pixelViewport = { left: 0, right: window.innerWidth, top: 0, bottom: 0 }, this.inView = !0, this.paddingTop = 0, this.paddingBottom = 0, this.contentDOMWidth = 0, this.contentDOMHeight = 0, this.editorHeight = 0, this.editorWidth = 0, this.scaleX = 1, this.scaleY = 1, this.scrollOffset = 0, this.scrolledToBottom = !1, this.scrollAnchorPos = 0, this.scrollAnchorHeight = -1, this.scaler = ro, this.scrollTarget = null, this.printing = !1, this.mustMeasureContent = !0, this.defaultTextDirection = $.LTR, this.visibleRanges = [], this.mustEnforceCursorAssoc = !1;
    let i = e.facet(ir).some((n) => typeof n != "function" && n.class == "cm-lineWrapping");
    this.heightOracle = new Zf(i), this.stateDeco = oo(e), this.heightMap = ot.empty().applyChanges(this.stateDeco, E.empty, this.heightOracle.setDoc(e.doc), [new bt(0, 0, 0, e.doc.length)]);
    for (let n = 0; n < 2 && (this.viewport = this.getViewport(0, null), !!this.updateForViewport()); n++)
      ;
    this.updateViewportLines(), this.lineGaps = this.ensureLineGaps([]), this.lineGapDeco = q.set(this.lineGaps.map((n) => n.draw(this, !1))), this.scrollParent = t.scrollDOM, this.computeVisibleRanges();
  }
  updateForViewport() {
    let t = [this.viewport], { main: e } = this.state.selection;
    for (let i = 0; i <= 1; i++) {
      let n = i ? e.head : e.anchor;
      if (!t.some(({ from: r, to: o }) => n >= r && n <= o)) {
        let { from: r, to: o } = this.lineBlockAt(n);
        t.push(new Ri(r, o));
      }
    }
    return this.viewports = t.sort((i, n) => i.from - n.from), this.updateScaler();
  }
  updateScaler() {
    let t = this.scaler;
    return this.scaler = this.heightMap.height <= 7e6 ? ro : new lr(this.heightOracle, this.heightMap, this.viewports), t.eq(this.scaler) ? 0 : 2;
  }
  updateViewportLines() {
    this.viewportLines = [], this.heightMap.forEachLine(this.viewport.from, this.viewport.to, this.heightOracle.setDoc(this.state.doc), 0, 0, (t) => {
      this.viewportLines.push(Je(t, this.scaler));
    });
  }
  update(t, e = null) {
    this.state = t.state;
    let i = this.stateDeco;
    this.stateDeco = oo(this.state);
    let n = t.changedRanges, r = bt.extendWithRanges(n, nc(i, this.stateDeco, t ? t.changes : Y.empty(this.state.doc.length))), o = this.heightMap.height, l = this.scrolledToBottom ? null : this.scrollAnchorAt(this.scrollOffset);
    io(), this.heightMap = this.heightMap.applyChanges(this.stateDeco, t.startState.doc, this.heightOracle.setDoc(this.state.doc), r), (this.heightMap.height != o || He) && (t.flags |= 2), l ? (this.scrollAnchorPos = t.changes.mapPos(l.from, -1), this.scrollAnchorHeight = l.top) : (this.scrollAnchorPos = -1, this.scrollAnchorHeight = o);
    let a = r.length ? this.mapViewport(this.viewport, t.changes) : this.viewport;
    (e && (e.range.head < a.from || e.range.head > a.to) || !this.viewportIsAppropriate(a)) && (a = this.getViewport(0, e));
    let h = a.from != this.viewport.from || a.to != this.viewport.to;
    this.viewport = a, t.flags |= this.updateForViewport(), (h || !t.changes.empty || t.flags & 2) && this.updateViewportLines(), (this.lineGaps.length || this.viewport.to - this.viewport.from > 4e3) && this.updateLineGaps(this.ensureLineGaps(this.mapLineGaps(this.lineGaps, t.changes))), t.flags |= this.computeVisibleRanges(t.changes), e && (this.scrollTarget = e), !this.mustEnforceCursorAssoc && (t.selectionSet || t.focusChanged) && t.view.lineWrapping && t.state.selection.main.empty && t.state.selection.main.assoc && !t.state.facet(Zh) && (this.mustEnforceCursorAssoc = !0);
  }
  measure() {
    let { view: t } = this, e = t.contentDOM, i = window.getComputedStyle(e), n = this.heightOracle, r = i.whiteSpace;
    this.defaultTextDirection = i.direction == "rtl" ? $.RTL : $.LTR;
    let o = this.heightOracle.mustRefreshForWrapping(r) || this.mustMeasureContent === "refresh", l = e.getBoundingClientRect(), a = o || this.mustMeasureContent || this.contentDOMHeight != l.height;
    this.contentDOMHeight = l.height, this.mustMeasureContent = !1;
    let h = 0, f = 0;
    if (l.width && l.height) {
      let { scaleX: v, scaleY: T } = xl(e, l);
      (v > 5e-3 && Math.abs(this.scaleX - v) > 5e-3 || T > 5e-3 && Math.abs(this.scaleY - T) > 5e-3) && (this.scaleX = v, this.scaleY = T, h |= 16, o = a = !0);
    }
    let c = (parseInt(i.paddingTop) || 0) * this.scaleY, u = (parseInt(i.paddingBottom) || 0) * this.scaleY;
    (this.paddingTop != c || this.paddingBottom != u) && (this.paddingTop = c, this.paddingBottom = u, h |= 18), this.editorWidth != t.scrollDOM.clientWidth && (n.lineWrapping && (a = !0), this.editorWidth = t.scrollDOM.clientWidth, h |= 16);
    let d = kl(this.view.contentDOM, !1).y;
    d != this.scrollParent && (this.scrollParent = d, this.scrollAnchorHeight = -1, this.scrollOffset = 0);
    let p = this.getScrollOffset();
    this.scrollOffset != p && (this.scrollAnchorHeight = -1, this.scrollOffset = p), this.scrolledToBottom = vl(this.scrollParent || t.win);
    let g = (this.printing ? lc : rc)(e, this.paddingTop), m = g.top - this.pixelViewport.top, y = g.bottom - this.pixelViewport.bottom;
    this.pixelViewport = g;
    let w = this.pixelViewport.bottom > this.pixelViewport.top && this.pixelViewport.right > this.pixelViewport.left;
    if (w != this.inView && (this.inView = w, w && (a = !0)), !this.inView && !this.scrollTarget && !oc(t.dom))
      return 0;
    let b = l.width;
    if ((this.contentDOMWidth != b || this.editorHeight != t.scrollDOM.clientHeight) && (this.contentDOMWidth = l.width, this.editorHeight = t.scrollDOM.clientHeight, h |= 16), a) {
      let v = t.docView.measureVisibleLineHeights(this.viewport);
      if (n.mustRefreshForHeights(v) && (o = !0), o || n.lineWrapping && Math.abs(b - this.contentDOMWidth) > n.charWidth) {
        let { lineHeight: T, charWidth: A, textHeight: L } = t.docView.measureTextSize();
        o = T > 0 && n.refresh(r, T, A, L, Math.max(5, b / A), v), o && (t.docView.minWidth = 0, h |= 16);
      }
      m > 0 && y > 0 ? f = Math.max(m, y) : m < 0 && y < 0 && (f = Math.min(m, y)), io();
      for (let T of this.viewports) {
        let A = T.from == this.viewport.from ? v : t.docView.measureVisibleLineHeights(T);
        this.heightMap = (o ? ot.empty().applyChanges(this.stateDeco, E.empty, this.heightOracle, [new bt(0, 0, 0, t.state.doc.length)]) : this.heightMap).updateHeight(n, 0, o, new Jf(T.from, A));
      }
      He && (h |= 2);
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
    let i = 0.5 - Math.max(-0.5, Math.min(0.5, t / 1e3 / 2)), n = this.heightMap, r = this.heightOracle, { visibleTop: o, visibleBottom: l } = this, a = new Ri(n.lineAt(o - i * 1e3, F.ByHeight, r, 0, 0).from, n.lineAt(l + (1 - i) * 1e3, F.ByHeight, r, 0, 0).to);
    if (e) {
      let { head: h } = e.range;
      if (h < a.from || h > a.to) {
        let f = Math.min(this.editorHeight, this.pixelViewport.bottom - this.pixelViewport.top), c = n.lineAt(h, F.ByPos, r, 0, 0), u;
        e.y == "center" ? u = (c.top + c.bottom) / 2 - f / 2 : e.y == "start" || e.y == "nearest" && h < a.from ? u = c.top : u = c.bottom - f, a = new Ri(n.lineAt(u - 1e3 / 2, F.ByHeight, r, 0, 0).from, n.lineAt(u + f + 1e3 / 2, F.ByHeight, r, 0, 0).to);
      }
    }
    return a;
  }
  mapViewport(t, e) {
    let i = e.mapPos(t.from, -1), n = e.mapPos(t.to, 1);
    return new Ri(this.heightMap.lineAt(i, F.ByPos, this.heightOracle, 0, 0).from, this.heightMap.lineAt(n, F.ByPos, this.heightOracle, 0, 0).to);
  }
  // Checks if a given viewport covers the visible part of the
  // document and not too much beyond that.
  viewportIsAppropriate({ from: t, to: e }, i = 0) {
    if (!this.inView)
      return !0;
    let { top: n } = this.heightMap.lineAt(t, F.ByPos, this.heightOracle, 0, 0), { bottom: r } = this.heightMap.lineAt(e, F.ByPos, this.heightOracle, 0, 0), { visibleTop: o, visibleBottom: l } = this;
    return (t == 0 || n <= o - Math.max(10, Math.min(
      -i,
      250
      /* VP.MaxCoverMargin */
    ))) && (e == this.state.doc.length || r >= l + Math.max(10, Math.min(
      i,
      250
      /* VP.MaxCoverMargin */
    ))) && n > o - 2 * 1e3 && r < l + 2 * 1e3;
  }
  mapLineGaps(t, e) {
    if (!t.length || e.empty)
      return t;
    let i = [];
    for (let n of t)
      e.touchesRange(n.from, n.to) || i.push(new Kn(e.mapPos(n.from), e.mapPos(n.to), n.size, n.displaySize));
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
    let i = this.heightOracle.lineWrapping, n = i ? 1e4 : 2e3, r = n >> 1, o = n << 1;
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
      let m = fc(t, (y) => y.from >= u.from && y.to <= u.to && Math.abs(y.from - f) < r && Math.abs(y.to - c) < r && !g.some((w) => y.from < w && y.to > w));
      if (!m) {
        if (c < u.to && e && i && e.visibleRanges.some((b) => b.from <= c && b.to >= c)) {
          let b = e.moveToLineBoundary(x.cursor(c), !1, !0).head;
          b > f && (c = b);
        }
        let y = this.gapSize(u, f, c, d), w = i || y < 2e6 ? y : 2e6;
        m = new Kn(f, c, y, w);
      }
      l.push(m);
    }, h = (f) => {
      if (f.length < o || f.type != kt.Text)
        return;
      let c = hc(f.from, f.to, this.stateDeco);
      if (c.total < o)
        return;
      let u = this.scrollTarget ? this.scrollTarget.range.head : null, d, p;
      if (i) {
        let g = n / this.heightOracle.lineLength * this.heightOracle.lineHeight, m, y;
        if (u != null) {
          let w = Li(c, u), b = ((this.visibleBottom - this.visibleTop) / 2 + g) / f.height;
          m = w - b, y = w + b;
        } else
          m = (this.visibleTop - f.top - g) / f.height, y = (this.visibleBottom - f.top + g) / f.height;
        d = Bi(c, m), p = Bi(c, y);
      } else {
        let g = c.total * this.heightOracle.charWidth, m = n * this.heightOracle.charWidth, y = 0;
        if (g > 2e6)
          for (let T of t)
            T.from >= f.from && T.from < f.to && T.size != T.displaySize && T.from * this.heightOracle.charWidth + y < this.pixelViewport.left && (y = T.size - T.displaySize);
        let w = this.pixelViewport.left + y, b = this.pixelViewport.right + y, O, v;
        if (u != null) {
          let T = Li(c, u), A = ((b - w) / 2 + m) / g;
          O = T - A, v = T + A;
        } else
          O = (w - m) / g, v = (b + m) / g;
        d = Bi(c, O), p = Bi(c, v);
      }
      d > f.from && a(f.from, d, f, c), p < f.to && a(p, f.to, f, c);
    };
    for (let f of this.viewportLines)
      Array.isArray(f.type) ? f.type.forEach(h) : h(f);
    return l;
  }
  gapSize(t, e, i, n) {
    let r = Li(n, i) - Li(n, e);
    return this.heightOracle.lineWrapping ? t.height * r : n.total * this.heightOracle.charWidth * r;
  }
  updateLineGaps(t) {
    Kn.same(t, this.lineGaps) || (this.lineGaps = t, this.lineGapDeco = q.set(t.map((e) => e.draw(this, this.heightOracle.lineWrapping))));
  }
  computeVisibleRanges(t) {
    let e = this.stateDeco;
    this.lineGaps.length && (e = e.concat(this.lineGapDeco));
    let i = [];
    V.spans(e, this.viewport.from, this.viewport.to, {
      span(r, o) {
        i.push({ from: r, to: o });
      },
      point() {
      }
    }, 20);
    let n = 0;
    if (i.length != this.visibleRanges.length)
      n = 12;
    else
      for (let r = 0; r < i.length && !(n & 8); r++) {
        let o = this.visibleRanges[r], l = i[r];
        (o.from != l.from || o.to != l.to) && (n |= 4, t && t.mapPos(o.from, -1) == l.from && t.mapPos(o.to, 1) == l.to || (n |= 8));
      }
    return this.visibleRanges = i, n;
  }
  lineBlockAt(t) {
    return t >= this.viewport.from && t <= this.viewport.to && this.viewportLines.find((e) => e.from <= t && e.to >= t) || Je(this.heightMap.lineAt(t, F.ByPos, this.heightOracle, 0, 0), this.scaler);
  }
  lineBlockAtHeight(t) {
    return t >= this.viewportLines[0].top && t <= this.viewportLines[this.viewportLines.length - 1].bottom && this.viewportLines.find((e) => e.top <= t && e.bottom >= t) || Je(this.heightMap.lineAt(this.scaler.fromDOM(t), F.ByHeight, this.heightOracle, 0, 0), this.scaler);
  }
  getScrollOffset() {
    return (this.scrollParent == this.view.scrollDOM ? this.scrollParent.scrollTop : (this.scrollParent ? this.scrollParent.getBoundingClientRect().top : 0) - this.view.contentDOM.getBoundingClientRect().top) * this.scaleY;
  }
  scrollAnchorAt(t) {
    let e = this.lineBlockAtHeight(t + 8);
    return e.from >= this.viewport.from || this.viewportLines[0].top - t > 200 ? e : this.viewportLines[0];
  }
  elementAtHeight(t) {
    return Je(this.heightMap.blockAt(this.scaler.fromDOM(t), this.heightOracle, 0, 0), this.scaler);
  }
  get docHeight() {
    return this.scaler.toDOM(this.heightMap.height);
  }
  get contentHeight() {
    return this.docHeight + this.paddingTop + this.paddingBottom;
  }
}
class Ri {
  constructor(t, e) {
    this.from = t, this.to = e;
  }
}
function hc(s, t, e) {
  let i = [], n = s, r = 0;
  return V.spans(e, s, t, {
    span() {
    },
    point(o, l) {
      o > n && (i.push({ from: n, to: o }), r += o - n), n = l;
    }
  }, 20), n < t && (i.push({ from: n, to: t }), r += t - n), { total: r, ranges: i };
}
function Bi({ total: s, ranges: t }, e) {
  if (e <= 0)
    return t[0].from;
  if (e >= 1)
    return t[t.length - 1].to;
  let i = Math.floor(s * e);
  for (let n = 0; ; n++) {
    let { from: r, to: o } = t[n], l = o - r;
    if (i <= l)
      return r + i;
    i -= l;
  }
}
function Li(s, t) {
  let e = 0;
  for (let { from: i, to: n } of s.ranges) {
    if (t <= n) {
      e += t - i;
      break;
    }
    e += n - i;
  }
  return e / s.total;
}
function fc(s, t) {
  for (let e of s)
    if (t(e))
      return e;
}
const ro = {
  toDOM(s) {
    return s;
  },
  fromDOM(s) {
    return s;
  },
  scale: 1,
  eq(s) {
    return s == this;
  }
};
function oo(s) {
  let t = s.facet(Mn).filter((i) => typeof i != "function"), e = s.facet(nr).filter((i) => typeof i != "function");
  return e.length && t.push(V.join(e)), t;
}
class lr {
  constructor(t, e, i) {
    let n = 0, r = 0, o = 0;
    this.viewports = i.map(({ from: l, to: a }) => {
      let h = e.lineAt(l, F.ByPos, t, 0, 0).top, f = e.lineAt(a, F.ByPos, t, 0, 0).bottom;
      return n += f - h, { from: l, to: a, top: h, bottom: f, domTop: 0, domBottom: 0 };
    }), this.scale = (7e6 - n) / (e.height - n);
    for (let l of this.viewports)
      l.domTop = o + (l.top - r) * this.scale, o = l.domBottom = l.domTop + (l.bottom - l.top), r = l.bottom;
  }
  toDOM(t) {
    for (let e = 0, i = 0, n = 0; ; e++) {
      let r = e < this.viewports.length ? this.viewports[e] : null;
      if (!r || t < r.top)
        return n + (t - i) * this.scale;
      if (t <= r.bottom)
        return r.domTop + (t - r.top);
      i = r.bottom, n = r.domBottom;
    }
  }
  fromDOM(t) {
    for (let e = 0, i = 0, n = 0; ; e++) {
      let r = e < this.viewports.length ? this.viewports[e] : null;
      if (!r || t < r.domTop)
        return i + (t - n) / this.scale;
      if (t <= r.domBottom)
        return r.top + (t - r.domTop);
      i = r.bottom, n = r.domBottom;
    }
  }
  eq(t) {
    return t instanceof lr ? this.scale == t.scale && this.viewports.length == t.viewports.length && this.viewports.every((e, i) => e.from == t.viewports[i].from && e.to == t.viewports[i].to) : !1;
  }
}
function Je(s, t) {
  if (t.scale == 1)
    return s;
  let e = t.toDOM(s.top), i = t.toDOM(s.bottom);
  return new Ot(s.from, s.length, e, i - e, Array.isArray(s._content) ? s._content.map((n) => Je(n, t)) : s._content);
}
const Ei = /* @__PURE__ */ P.define({ combine: (s) => s.join(" ") }), Bs = /* @__PURE__ */ P.define({ combine: (s) => s.indexOf(!0) > -1 }), Ls = /* @__PURE__ */ Ne.newName(), ra = /* @__PURE__ */ Ne.newName(), oa = /* @__PURE__ */ Ne.newName(), la = { "&light": "." + ra, "&dark": "." + oa };
function Es(s, t, e) {
  return new Ne(t, {
    finish(i) {
      return /&/.test(i) ? i.replace(/&\w*/, (n) => {
        if (n == "&")
          return s;
        if (!e || !e[n])
          throw new RangeError(`Unsupported selector: ${n}`);
        return e[n];
      }) : s + " " + i;
    }
  });
}
const cc = /* @__PURE__ */ Es("." + Ls, {
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
}, la), uc = {
  childList: !0,
  characterData: !0,
  subtree: !0,
  attributes: !0,
  characterDataOldValue: !0
}, _n = S.ie && S.ie_version <= 11;
class dc {
  constructor(t) {
    this.view = t, this.active = !1, this.editContext = null, this.selectionRange = new Fh(), this.selectionChanged = !1, this.delayedFlush = -1, this.resizeTimeout = -1, this.queue = [], this.delayedAndroidKey = null, this.flushingAndroidKey = -1, this.lastChange = 0, this.scrollTargets = [], this.intersection = null, this.resizeScroll = null, this.intersecting = !1, this.gapIntersection = null, this.gaps = [], this.printQuery = null, this.parentCheck = -1, this.dom = t.contentDOM, this.observer = new MutationObserver((e) => {
      for (let i of e)
        this.queue.push(i);
      (S.ie && S.ie_version <= 11 || S.ios && t.composing) && e.some((i) => i.type == "childList" && i.removedNodes.length || i.type == "characterData" && i.oldValue.length > i.target.nodeValue.length) ? this.flushSoon() : this.flush();
    }), window.EditContext && S.android && t.constructor.EDIT_CONTEXT !== !1 && // Chrome <126 doesn't support inverted selections in edit context (#1392)
    !(S.chrome && S.chrome_version < 126) && (this.editContext = new gc(t), t.state.facet(qt) && (t.contentDOM.editContext = this.editContext.editContext)), _n && (this.onCharData = (e) => {
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
    let { view: i } = this, n = this.selectionRange;
    if (i.state.facet(qt) ? i.root.activeElement != this.dom : !ni(this.dom, n))
      return;
    let r = n.anchorNode && i.docView.tile.nearest(n.anchorNode);
    if (r && r.isWidget() && r.widget.ignoreEvent(t)) {
      e || (this.selectionChanged = !1);
      return;
    }
    (S.ie && S.ie_version <= 11 || S.android && S.chrome) && !i.state.selection.main.empty && // (Selection.isCollapsed isn't reliable on IE)
    n.focusNode && si(n.focusNode, n.focusOffset, n.anchorNode, n.anchorOffset) ? this.flushSoon() : this.flush(!1);
  }
  readSelectionRange() {
    let { view: t } = this, e = ui(t.root);
    if (!e)
      return !1;
    let i = S.safari && t.root.nodeType == 11 && t.root.activeElement == this.dom && pc(this.view, e) || e;
    if (!i || this.selectionRange.eq(i))
      return !1;
    let n = ni(this.dom, i);
    return n && !this.selectionChanged && t.inputState.lastFocusTime > Date.now() - 200 && t.inputState.lastTouchTime < Date.now() - 300 && Qh(this.dom, i) ? (this.view.inputState.lastFocusTime = 0, t.docView.updateSelection(), !1) : (this.selectionRange.setRange(i), n && (this.selectionChanged = !0), !0);
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
    this.active || (this.observer.observe(this.dom, uc), _n && this.dom.addEventListener("DOMCharacterDataModified", this.onCharData), this.active = !0);
  }
  stop() {
    this.active && (this.active = !1, this.observer.disconnect(), _n && this.dom.removeEventListener("DOMCharacterDataModified", this.onCharData));
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
      let n = () => {
        let r = this.delayedAndroidKey;
        r && (this.clearDelayedAndroidKey(), this.view.inputState.lastKeyCode = r.keyCode, this.view.inputState.lastKeyTime = Date.now(), !this.flush() && r.force && Re(this.dom, r.key, r.keyCode));
      };
      this.flushingAndroidKey = this.view.win.requestAnimationFrame(n);
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
    let e = -1, i = -1, n = !1;
    for (let r of t) {
      let o = this.readMutation(r);
      o && (o.typeOver && (n = !0), e == -1 ? { from: e, to: i } = o : (e = Math.min(o.from, e), i = Math.max(o.to, i)));
    }
    return { from: e, to: i, typeOver: n };
  }
  readChange() {
    let { from: t, to: e, typeOver: i } = this.processRecords(), n = this.selectionChanged && ni(this.dom, this.selectionRange);
    if (t < 0 && !n)
      return null;
    t > -1 && (this.lastChange = Date.now()), this.view.inputState.lastFocusTime = 0, this.selectionChanged = !1;
    let r = new Bf(this.view, t, e, i);
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
    let i = this.view.state, n = Kl(this.view, e);
    return this.view.state == i && (e.domChanged || e.newSel && !hn(this.view.state.selection, e.newSel.main)) && this.view.update([]), n;
  }
  readMutation(t) {
    let e = this.view.docView.tile.nearest(t.target);
    if (!e || e.isWidget())
      return null;
    if (e.markDirty(t.type == "attributes"), t.type == "childList") {
      let i = lo(e, t.previousSibling || t.target.previousSibling, -1), n = lo(e, t.nextSibling || t.target.nextSibling, 1);
      return {
        from: i ? e.posAfter(i) : e.posAtStart,
        to: n ? e.posBefore(n) : e.posAtEnd,
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
    this.editContext && (this.editContext.update(t), t.startState.facet(qt) != t.state.facet(qt) && (t.view.contentDOM.editContext = t.state.facet(qt) ? this.editContext.editContext : null));
  }
  destroy() {
    var t, e, i;
    this.stop(), (t = this.intersection) === null || t === void 0 || t.disconnect(), (e = this.gapIntersection) === null || e === void 0 || e.disconnect(), (i = this.resizeScroll) === null || i === void 0 || i.disconnect();
    for (let n of this.scrollTargets)
      n.removeEventListener("scroll", this.onScroll);
    this.removeWindowListeners(this.win), clearTimeout(this.parentCheck), clearTimeout(this.resizeTimeout), this.win.cancelAnimationFrame(this.delayedFlush), this.win.cancelAnimationFrame(this.flushingAndroidKey), this.editContext && (this.view.contentDOM.editContext = null, this.editContext.destroy());
  }
}
function lo(s, t, e) {
  for (; t; ) {
    let i = U.get(t);
    if (i && i.parent == s)
      return i;
    let n = t.parentNode;
    t = n != s.dom ? n : e > 0 ? t.nextSibling : t.previousSibling;
  }
  return null;
}
function ao(s, t) {
  let e = t.startContainer, i = t.startOffset, n = t.endContainer, r = t.endOffset, o = s.docView.domAtPos(s.state.selection.main.anchor, 1);
  return si(o.node, o.offset, n, r) && ([e, i, n, r] = [n, r, e, i]), { anchorNode: e, anchorOffset: i, focusNode: n, focusOffset: r };
}
function pc(s, t) {
  if (t.getComposedRanges) {
    let n = t.getComposedRanges(s.root)[0];
    if (n)
      return ao(s, n);
  }
  let e = null;
  function i(n) {
    n.preventDefault(), n.stopImmediatePropagation(), e = n.getTargetRanges()[0];
  }
  return s.contentDOM.addEventListener("beforeinput", i, !0), s.dom.ownerDocument.execCommand("indent"), s.contentDOM.removeEventListener("beforeinput", i, !0), e ? ao(s, e) : null;
}
class gc {
  constructor(t) {
    this.from = 0, this.to = 0, this.pendingContextChange = null, this.handlers = /* @__PURE__ */ Object.create(null), this.composing = null, this.resetRange(t.state);
    let e = this.editContext = new window.EditContext({
      text: t.state.doc.sliceString(this.from, this.to),
      selectionStart: this.toContextPos(Math.max(this.from, Math.min(this.to, t.state.selection.main.anchor))),
      selectionEnd: this.toContextPos(t.state.selection.main.head)
    });
    this.handlers.textupdate = (i) => {
      let n = t.state.selection.main, { anchor: r, head: o } = n, l = this.toEditorPos(i.updateRangeStart), a = this.toEditorPos(i.updateRangeEnd);
      t.inputState.composing >= 0 && !this.composing && (this.composing = { contextBase: i.updateRangeStart, editorBase: l, drifted: !1 });
      let h = a - l > i.text.length;
      l == this.from && r < this.from ? l = r : a == this.to && r > this.to && (a = r);
      let f = _l(t.state.sliceDoc(l, a), i.text, (h ? n.from : n.to) - l, h ? "end" : null);
      if (!f) {
        let u = x.single(this.toEditorPos(i.selectionStart), this.toEditorPos(i.selectionEnd));
        hn(u, n) || t.dispatch({ selection: u, userEvent: "select" });
        return;
      }
      let c = {
        from: f.from + l,
        to: f.toA + l,
        insert: E.of(i.text.slice(f.from, f.toB).split(`
`))
      };
      if ((S.mac || S.android) && c.from == o - 1 && /^\. ?$/.test(i.text) && t.contentDOM.getAttribute("autocorrect") == "off" && (c = { from: l, to: a, insert: E.of([i.text.replace(".", " ")]) }), this.pendingContextChange = c, !t.state.readOnly) {
        let u = this.to - this.from + (c.to - c.from + c.insert.length);
        rr(t, c, x.single(this.toEditorPos(i.selectionStart, u), this.toEditorPos(i.selectionEnd, u)));
      }
      this.pendingContextChange && (this.revertPending(t.state), this.setSelection(t.state)), c.from < c.to && !c.insert.length && t.inputState.composing >= 0 && !/[\\p{Alphabetic}\\p{Number}_]/.test(e.text.slice(Math.max(0, i.updateRangeStart - 1), Math.min(e.text.length, i.updateRangeStart + 1))) && this.handlers.compositionend(i);
    }, this.handlers.characterboundsupdate = (i) => {
      let n = [], r = null;
      for (let o = this.toEditorPos(i.rangeStart), l = this.toEditorPos(i.rangeEnd); o < l; o++) {
        let a = t.coordsForChar(o);
        r = a && new DOMRect(a.left, a.top, a.right - a.left, a.bottom - a.top) || r || new DOMRect(), n.push(r);
      }
      e.updateCharacterBounds(i.rangeStart, n);
    }, this.handlers.textformatupdate = (i) => {
      let n = [];
      for (let r of i.getTextFormats()) {
        let o = r.underlineStyle, l = r.underlineThickness;
        if (!/none/i.test(o) && !/none/i.test(l)) {
          let a = this.toEditorPos(r.rangeStart), h = this.toEditorPos(r.rangeEnd);
          if (a < h) {
            let f = `text-decoration: underline ${/^[a-z]/.test(o) ? o + " " : o == "Dashed" ? "dashed " : o == "Squiggle" ? "wavy " : ""}${/thin/i.test(l) ? 1 : 2}px`;
            n.push(q.mark({ attributes: { style: f } }).range(a, h));
          }
        }
      }
      t.dispatch({ effects: Hl.of(q.set(n)) });
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
      let n = ui(i.root);
      n && n.rangeCount && this.editContext.updateSelectionBounds(n.getRangeAt(0).getBoundingClientRect());
    } };
  }
  applyEdits(t) {
    let e = 0, i = !1, n = this.pendingContextChange;
    return t.changes.iterChanges((r, o, l, a, h) => {
      if (i)
        return;
      let f = h.length - (o - r);
      if (n && o >= n.to)
        if (n.from == r && n.to == o && n.insert.eq(h)) {
          n = this.pendingContextChange = null, e += f, this.to += f;
          return;
        } else
          n = null, this.revertPending(t.state);
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
    }), n && !i && this.revertPending(t.state), !i;
  }
  update(t) {
    let e = this.pendingContextChange, i = t.startState.selection.main;
    this.composing && (this.composing.drifted || !t.changes.touchesRange(i.from, i.to) && t.transactions.some((n) => !n.isUserEvent("input.type") && n.changes.touchesRange(this.from, this.to))) ? (this.composing.drifted = !0, this.composing.editorBase = t.changes.mapPos(this.composing.editorBase)) : !this.applyEdits(t) || !this.rangeIsValid(t.state) ? (this.pendingContextChange = null, this.reset(t.state)) : (t.docChanged || t.selectionSet || e) && this.setSelection(t.state), (t.geometryChanged || t.docChanged || t.selectionSet) && t.view.requestMeasure(this.measureReq);
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
    let { main: e } = t.selection, i = this.toContextPos(Math.max(this.from, Math.min(this.to, e.anchor))), n = this.toContextPos(e.head);
    (this.editContext.selectionStart != i || this.editContext.selectionEnd != n) && this.editContext.updateSelection(i, n);
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
class M {
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
    this.dispatchTransactions = t.dispatchTransactions || i && ((n) => n.forEach((r) => i(r, this))) || ((n) => this.update(n)), this.dispatch = this.dispatch.bind(this), this._root = t.root || zh(t.parent) || document, this.viewState = new so(this, t.state || N.create(t)), t.scrollTo && t.scrollTo.is(Pi) && (this.viewState.scrollTarget = t.scrollTo.value.clip(this.viewState.state)), this.plugins = this.state.facet(Ae).map((n) => new $n(n));
    for (let n of this.plugins)
      n.update(this);
    this.observer = new dc(this), this.inputState = new Nf(this), this.inputState.ensureHandlers(this.plugins), this.docView = new Xr(this), this.mountStyles(), this.updateAttrs(), this.updateState = 0, this.requestMeasure(), !((e = document.fonts) === null || e === void 0) && e.ready && document.fonts.ready.then(() => {
      this.viewState.mustMeasureContent = "refresh", this.requestMeasure();
    });
  }
  dispatch(...t) {
    let e = t.length == 1 && t[0] instanceof G ? t : t.length == 1 && Array.isArray(t[0]) ? t[0] : [this.state.update(...t)];
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
    let e = !1, i = !1, n, r = this.state;
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
    t.some((u) => u.annotation(ea)) ? (this.inputState.notifiedFocused = o, l = 1) : o != this.inputState.notifiedFocused && (this.inputState.notifiedFocused = o, a = ia(r, o), a || (l = 1));
    let h = this.observer.delayedAndroidKey, f = null;
    if (h ? (this.observer.clearDelayedAndroidKey(), f = this.observer.readChange(), (f && !this.state.doc.eq(r.doc) || !this.state.selection.eq(r.selection)) && (f = null)) : this.observer.clear(), r.facet(N.phrases) != this.state.facet(N.phrases))
      return this.setState(r);
    n = on.create(this, r, t), n.flags |= l;
    let c = this.viewState.scrollTarget;
    try {
      this.updateState = 2;
      for (let u of t) {
        if (c && (c = c.map(u.changes)), u.scrollIntoView) {
          let { main: d } = u.state.selection;
          c = new Be(d.empty ? d : x.cursor(d.head, d.head > d.anchor ? -1 : 1));
        }
        for (let d of u.effects)
          d.is(Pi) && (c = d.value.clip(this.state));
      }
      this.viewState.update(n, c), this.bidiCache = cn.update(this.bidiCache, n.changes), n.empty || (this.updatePlugins(n), this.inputState.update(n)), e = this.docView.update(n), this.state.facet(Ze) != this.styleModules && this.mountStyles(), i = this.updateAttrs(), this.showAnnouncements(t), this.docView.updateSelection(e, t.some((u) => u.isUserEvent("select.pointer")));
    } finally {
      this.updateState = 0;
    }
    if (n.startState.facet(Ei) != n.state.facet(Ei) && (this.viewState.mustMeasureContent = !0), (e || i || c || this.viewState.mustEnforceCursorAssoc || this.viewState.mustMeasureContent) && this.requestMeasure(), e && this.docViewUpdate(), !n.empty)
      for (let u of this.state.facet(Ps))
        try {
          u(n);
        } catch (d) {
          ft(this.state, d, "update listener");
        }
    (a || f) && Promise.resolve().then(() => {
      a && this.state == a.startState && this.dispatch(a), f && !Kl(this, f) && h.force && Re(this.contentDOM, h.key, h.keyCode);
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
      this.viewState = new so(this, t), this.plugins = t.facet(Ae).map((i) => new $n(i)), this.pluginMap.clear();
      for (let i of this.plugins)
        i.update(this);
      this.docView.destroy(), this.docView = new Xr(this), this.inputState.ensureHandlers(this.plugins), this.mountStyles(), this.updateAttrs(), this.bidiCache = [];
    } finally {
      this.updateState = 0;
    }
    e && this.focus(), this.requestMeasure();
  }
  updatePlugins(t) {
    let e = t.startState.facet(Ae), i = t.state.facet(Ae);
    if (e != i) {
      let n = [];
      for (let r of i) {
        let o = e.indexOf(r);
        if (o < 0)
          n.push(new $n(r));
        else {
          let l = this.plugins[o];
          l.mustUpdate = t, n.push(l);
        }
      }
      for (let r of this.plugins)
        r.mustUpdate != t && r.destroy(this);
      this.plugins = n, this.pluginMap.clear();
    } else
      for (let n of this.plugins)
        n.mustUpdate = t;
    for (let n = 0; n < this.plugins.length; n++)
      this.plugins[n].update(this);
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
    let e = null, i = this.viewState.scrollParent, n = this.viewState.getScrollOffset(), { scrollAnchorPos: r, scrollAnchorHeight: o } = this.viewState;
    Math.abs(n - this.viewState.scrollOffset) > 1 && (o = -1), this.viewState.scrollAnchorHeight = -1;
    try {
      for (let l = 0; ; l++) {
        if (o < 0)
          if (vl(i || this.win))
            r = -1, o = this.viewState.heightMap.height;
          else {
            let d = this.viewState.scrollAnchorAt(n);
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
            return ft(this.state, p), ho;
          }
        }), c = on.create(this, this.state, []), u = !1;
        c.flags |= a, e ? e.flags |= a : e = c, this.updateState = 2, c.empty || (this.updatePlugins(c), this.inputState.update(c), this.updateAttrs(), u = this.docView.update(c), u && this.docViewUpdate());
        for (let d = 0; d < h.length; d++)
          if (f[d] != ho)
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
                n = n + p, i ? i.scrollTop += p : this.win.scrollBy(0, p), o = -1;
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
      for (let l of this.state.facet(Ps))
        l(e);
  }
  /**
  Get the CSS classes for the currently active editor themes.
  */
  get themeClasses() {
    return Ls + " " + (this.state.facet(Bs) ? oa : ra) + " " + this.state.facet(Ei);
  }
  updateAttrs() {
    let t = fo(this, Fl, {
      class: "cm-editor" + (this.hasFocus ? " cm-focused " : " ") + this.themeClasses
    }), e = {
      spellcheck: "false",
      autocorrect: "off",
      autocapitalize: "off",
      writingsuggestions: "false",
      translate: "no",
      contenteditable: this.state.facet(qt) ? "true" : "false",
      class: "cm-content",
      style: `${S.tabSize}: ${this.state.tabSize}`,
      role: "textbox",
      "aria-multiline": "true"
    };
    this.state.readOnly && (e["aria-readonly"] = "true"), fo(this, ir, e);
    let i = this.observer.ignore(() => {
      let n = Hr(this.contentDOM, this.contentAttrs, e), r = Hr(this.dom, this.editorAttrs, t);
      return n || r;
    });
    return this.editorAttrs = t, this.contentAttrs = e, i;
  }
  showAnnouncements(t) {
    let e = !0;
    for (let i of t)
      for (let n of i.effects)
        if (n.is(M.announce)) {
          e && (this.announceDOM.textContent = ""), e = !1;
          let r = this.announceDOM.appendChild(document.createElement("div"));
          r.textContent = n.value;
        }
  }
  mountStyles() {
    this.styleModules = this.state.facet(Ze);
    let t = this.state.facet(M.cspNonce);
    Ne.mount(this.root, this.styleModules.concat(cc).reverse(), t ? { nonce: t } : void 0);
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
    return Un(this, t, qr(this, t, e, i));
  }
  /**
  Move a cursor position across the next group of either
  [letters](https://codemirror.net/6/docs/ref/#state.EditorState.charCategorizer) or non-letter
  non-whitespace characters.
  */
  moveByGroup(t, e) {
    return Un(this, t, qr(this, t, e, (i) => Af(this, t.head, i)));
  }
  /**
  Get the cursor position visually at the start or end of a line.
  Note that this may differ from the _logical_ position at its
  start or end (which is simply at `line.from`/`line.to`) if text
  at the start or end goes against the line's base text direction.
  */
  visualLineSide(t, e) {
    let i = this.bidiSpans(t), n = this.textDirectionAt(t.from), r = i[e ? i.length - 1 : 0];
    return x.cursor(r.side(e, n) + t.from, r.forward(!e, n) ? 1 : -1);
  }
  /**
  Move to the next line boundary in the given direction. If
  `includeWrap` is true, line wrapping is on, and there is a
  further wrap point on the current line, the wrap point will be
  returned. Otherwise this function will return the start or end
  of the line.
  */
  moveToLineBoundary(t, e, i = !0) {
    return Cf(this, t, e, i);
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
    return Un(this, t, Tf(this, t, e, i));
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
    let i = Ds(this, t, e);
    return i && i.pos;
  }
  posAndSideAtCoords(t, e = !0) {
    return this.readMeasured(), Ds(this, t, e);
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
    let n = this.state.doc.lineAt(t), r = this.bidiSpans(n), o = r[Ht.find(r, t - n.from, -1, e)];
    return rn(i, o.dir == $.LTR == e > 0);
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
    return !this.state.facet(Vl) || t < this.viewport.from || t > this.viewport.to ? this.textDirection : (this.readMeasured(), this.docView.textDirectionAt(t));
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
    if (t.length > mc)
      return Ml(t.length);
    let e = this.textDirectionAt(t.from), i;
    for (let r of this.bidiCache)
      if (r.from == t.from && r.dir == e && (r.fresh || Pl(r.isolates, i = Qr(this, t))))
        return r.order;
    i || (i = Qr(this, t));
    let n = _h(t.text, e, i);
    return this.bidiCache.push(new cn(t.from, t.to, e, i, !0, n)), n;
  }
  /**
  Check whether the editor has focus.
  */
  get hasFocus() {
    var t;
    return (this.dom.ownerDocument.hasFocus() || S.safari && ((t = this.inputState) === null || t === void 0 ? void 0 : t.lastContextMenu) > Date.now() - 3e4) && this.root.activeElement == this.contentDOM;
  }
  /**
  Put focus on the editor.
  */
  focus() {
    this.observer.ignore(() => {
      Sl(this.contentDOM), this.docView.updateSelection();
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
    return Pi.of(new Be(typeof t == "number" ? x.cursor(t) : t, e.y, e.x, e.yMargin, e.xMargin));
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
    return Pi.of(new Be(x.cursor(i.from), "start", "start", i.top - t, e, !0));
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
    return $t.define(() => ({}), { eventHandlers: t });
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
    return $t.define(() => ({}), { eventObservers: t });
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
    let i = Ne.newName(), n = [Ei.of(i), Ze.of(Es(`.${i}`, t))];
    return e && e.dark && n.push(Bs.of(!0)), n;
  }
  /**
  Create an extension that adds styles to the base theme. Like
  with [`theme`](https://codemirror.net/6/docs/ref/#view.EditorView^theme), use `&` to indicate the
  place of the editor wrapper element when directly targeting
  that. You can also use `&dark` or `&light` instead to only
  target editors with a dark or light theme.
  */
  static baseTheme(t) {
    return An.lowest(Ze.of(Es("." + Ls, t, la)));
  }
  /**
  Retrieve an editor view instance from the view's DOM
  representation.
  */
  static findFromDOM(t) {
    var e;
    let i = t.querySelector(".cm-content"), n = i && U.get(i) || U.get(t);
    return ((e = n == null ? void 0 : n.root) === null || e === void 0 ? void 0 : e.view) || null;
  }
}
M.styleModule = Ze;
M.inputHandler = Il;
M.clipboardInputFilter = tr;
M.clipboardOutputFilter = er;
M.scrollHandler = Wl;
M.focusChangeEffect = Nl;
M.perLineTextDirection = Vl;
M.exceptionSink = El;
M.updateListener = Ps;
M.editable = qt;
M.mouseSelectionStyle = Ll;
M.dragMovesSelection = Bl;
M.clickAddsSelectionRange = Rl;
M.decorations = Mn;
M.blockWrappers = zl;
M.outerDecorations = nr;
M.atomicRanges = ki;
M.bidiIsolatedRanges = Ql;
M.scrollMargins = $l;
M.darkTheme = Bs;
M.cspNonce = /* @__PURE__ */ P.define({ combine: (s) => s.length ? s[0] : "" });
M.contentAttributes = ir;
M.editorAttributes = Fl;
M.lineWrapping = /* @__PURE__ */ M.contentAttributes.of({ class: "cm-lineWrapping" });
M.announce = /* @__PURE__ */ I.define();
const mc = 4096, ho = {};
class cn {
  constructor(t, e, i, n, r, o) {
    this.from = t, this.to = e, this.dir = i, this.isolates = n, this.fresh = r, this.order = o;
  }
  static update(t, e) {
    if (e.empty && !t.some((r) => r.fresh))
      return t;
    let i = [], n = t.length ? t[t.length - 1].dir : $.LTR;
    for (let r = Math.max(0, t.length - 10); r < t.length; r++) {
      let o = t[r];
      o.dir == n && !e.touchesRange(o.from, o.to) && i.push(new cn(e.mapPos(o.from, 1), e.mapPos(o.to, -1), o.dir, o.isolates, !1, o.order));
    }
    return i;
  }
}
function fo(s, t, e) {
  for (let i = s.state.facet(t), n = i.length - 1; n >= 0; n--) {
    let r = i[n], o = typeof r == "function" ? r(s) : r;
    o && Gs(o, e);
  }
  return e;
}
const yc = S.mac ? "mac" : S.windows ? "win" : S.linux ? "linux" : "key";
function bc(s, t) {
  const e = s.split(/-(?!$)/);
  let i = e[e.length - 1];
  i == "Space" && (i = " ");
  let n, r, o, l;
  for (let a = 0; a < e.length - 1; ++a) {
    const h = e[a];
    if (/^(cmd|meta|m)$/i.test(h))
      l = !0;
    else if (/^a(lt)?$/i.test(h))
      n = !0;
    else if (/^(c|ctrl|control)$/i.test(h))
      r = !0;
    else if (/^s(hift)?$/i.test(h))
      o = !0;
    else if (/^mod$/i.test(h))
      t == "mac" ? l = !0 : r = !0;
    else
      throw new Error("Unrecognized modifier name: " + h);
  }
  return n && (i = "Alt-" + i), r && (i = "Ctrl-" + i), l && (i = "Meta-" + i), o && (i = "Shift-" + i), i;
}
function Ii(s, t, e) {
  return t.altKey && (s = "Alt-" + s), t.ctrlKey && (s = "Ctrl-" + s), t.metaKey && (s = "Meta-" + s), e !== !1 && t.shiftKey && (s = "Shift-" + s), s;
}
const wc = /* @__PURE__ */ An.default(/* @__PURE__ */ M.domEventHandlers({
  keydown(s, t) {
    return vc(xc(t.state), s, t, "editor");
  }
})), ar = /* @__PURE__ */ P.define({ enables: wc }), co = /* @__PURE__ */ new WeakMap();
function xc(s) {
  let t = s.facet(ar), e = co.get(t);
  return e || co.set(t, e = Sc(t.reduce((i, n) => i.concat(n), []))), e;
}
let ee = null;
const kc = 4e3;
function Sc(s, t = yc) {
  let e = /* @__PURE__ */ Object.create(null), i = /* @__PURE__ */ Object.create(null), n = (o, l) => {
    let a = i[o];
    if (a == null)
      i[o] = l;
    else if (a != l)
      throw new Error("Key binding " + o + " is used both as a regular binding and as a multi-stroke prefix");
  }, r = (o, l, a, h, f) => {
    var c, u;
    let d = e[o] || (e[o] = /* @__PURE__ */ Object.create(null)), p = l.split(/ (?!$)/).map((y) => bc(y, t));
    for (let y = 1; y < p.length; y++) {
      let w = p.slice(0, y).join(" ");
      n(w, !0), d[w] || (d[w] = {
        preventDefault: !0,
        stopPropagation: !1,
        run: [(b) => {
          let O = ee = { view: b, prefix: w, scope: o };
          return setTimeout(() => {
            ee == O && (ee = null);
          }, kc), !0;
        }]
      });
    }
    let g = p.join(" ");
    n(g, !1);
    let m = d[g] || (d[g] = {
      preventDefault: !1,
      stopPropagation: !1,
      run: ((u = (c = d._any) === null || c === void 0 ? void 0 : c.run) === null || u === void 0 ? void 0 : u.slice()) || []
    });
    a && m.run.push(a), h && (m.preventDefault = !0), f && (m.stopPropagation = !0);
  };
  for (let o of s) {
    let l = o.scope ? o.scope.split(" ") : ["editor"];
    if (o.any)
      for (let h of l) {
        let f = e[h] || (e[h] = /* @__PURE__ */ Object.create(null));
        f._any || (f._any = { preventDefault: !1, stopPropagation: !1, run: [] });
        let { any: c } = o;
        for (let u in f)
          f[u].run.push((d) => c(d, Is));
      }
    let a = o[t] || o.key;
    if (a)
      for (let h of l)
        r(h, a, o.run, o.preventDefault, o.stopPropagation), o.shift && r(h, "Shift-" + a, o.shift, o.preventDefault, o.stopPropagation);
  }
  return e;
}
let Is = null;
function vc(s, t, e, i) {
  Is = t;
  let n = Lh(t), r = ue(n, 0), o = Oe(r) == n.length && n != " ", l = "", a = !1, h = !1, f = !1;
  ee && ee.view == e && ee.scope == i && (l = ee.prefix + " ", Gl.indexOf(t.keyCode) < 0 && (h = !0, ee = null));
  let c = /* @__PURE__ */ new Set(), u = (m) => {
    if (m) {
      for (let y of m.run)
        if (!c.has(y) && (c.add(y), y(e)))
          return m.stopPropagation && (f = !0), !0;
      m.preventDefault && (m.stopPropagation && (f = !0), h = !0);
    }
    return !1;
  }, d = s[i], p, g;
  return d && (u(d[l + Ii(n, t, !o)]) ? a = !0 : o && (t.altKey || t.metaKey || t.ctrlKey) && // Ctrl-Alt may be used for AltGr on Windows
  !(S.windows && t.ctrlKey && t.altKey) && // Alt-combinations on macOS tend to be typed characters
  !(S.mac && t.altKey && !(t.ctrlKey || t.metaKey)) && (p = re[t.keyCode]) && p != n ? (u(d[l + Ii(p, t, !0)]) || t.shiftKey && (g = fi[t.keyCode]) != n && g != p && u(d[l + Ii(g, t, !1)])) && (a = !0) : o && t.shiftKey && u(d[l + Ii(n, t, !0)]) && (a = !0), !a && u(d._any) && (a = !0)), h && (a = !0), a && f && t.stopPropagation(), Is = null, a;
}
const Ni = "-10000px";
class aa {
  constructor(t, e, i, n) {
    this.facet = e, this.createTooltipView = i, this.removeTooltipView = n, this.input = t.state.facet(e), this.tooltips = this.input.filter((o) => o);
    let r = null;
    this.tooltipViews = this.tooltips.map((o) => r = i(o, r));
  }
  update(t, e) {
    var i;
    let n = t.state.facet(this.facet), r = n.filter((a) => a);
    if (n === this.input) {
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
    return e && (l.forEach((a, h) => e[h] = a), e.length = l.length), this.input = n, this.tooltips = r, this.tooltipViews = o, !0;
  }
}
function Oc(s) {
  let t = s.dom.ownerDocument.documentElement;
  return { top: 0, left: 0, bottom: t.clientHeight, right: t.clientWidth };
}
const Yn = /* @__PURE__ */ P.define({
  combine: (s) => {
    var t, e, i;
    return {
      position: S.ios ? "absolute" : ((t = s.find((n) => n.position)) === null || t === void 0 ? void 0 : t.position) || "fixed",
      parent: ((e = s.find((n) => n.parent)) === null || e === void 0 ? void 0 : e.parent) || null,
      tooltipSpace: ((i = s.find((n) => n.tooltipSpace)) === null || i === void 0 ? void 0 : i.tooltipSpace) || Oc
    };
  }
}), uo = /* @__PURE__ */ new WeakMap(), hr = /* @__PURE__ */ $t.fromClass(class {
  constructor(s) {
    this.view = s, this.above = [], this.inView = !0, this.madeAbsolute = !1, this.lastTransaction = 0, this.measureTimeout = -1;
    let t = s.state.facet(Yn);
    this.position = t.position, this.parent = t.parent, this.classes = s.themeClasses, this.createContainer(), this.measureReq = { read: this.readMeasure.bind(this), write: this.writeMeasure.bind(this), key: this }, this.resizeObserver = typeof ResizeObserver == "function" ? new ResizeObserver(() => this.measureSoon()) : null, this.manager = new aa(s, fr, (e, i) => this.createTooltip(e, i), (e) => {
      this.resizeObserver && this.resizeObserver.unobserve(e.dom), e.dom.remove();
    }), this.above = this.manager.tooltips.map((e) => !!e.above), this.intersectionObserver = typeof IntersectionObserver == "function" ? new IntersectionObserver((e) => {
      Date.now() > this.lastTransaction - 50 && e.length > 0 && e[e.length - 1].intersectionRatio < 1 && this.measureSoon();
    }, { threshold: [1] }) : null, this.observeIntersection(), s.win.addEventListener("resize", this.measureSoon = this.measureSoon.bind(this)), this.maybeMeasure();
  }
  createContainer() {
    this.parent ? (this.container = document.createElement("div"), this.container.style.position = "relative", this.container.className = this.view.themeClasses, this.parent.appendChild(this.container)) : this.container = this.view.dom;
  }
  observeIntersection() {
    if (this.intersectionObserver) {
      this.intersectionObserver.disconnect();
      for (let s of this.manager.tooltipViews)
        this.intersectionObserver.observe(s.dom);
    }
  }
  measureSoon() {
    this.measureTimeout < 0 && (this.measureTimeout = setTimeout(() => {
      this.measureTimeout = -1, this.maybeMeasure();
    }, 50));
  }
  update(s) {
    s.transactions.length && (this.lastTransaction = Date.now());
    let t = this.manager.update(s, this.above);
    t && this.observeIntersection();
    let e = t || s.geometryChanged, i = s.state.facet(Yn);
    if (i.position != this.position && !this.madeAbsolute) {
      this.position = i.position;
      for (let n of this.manager.tooltipViews)
        n.dom.style.position = this.position;
      e = !0;
    }
    if (i.parent != this.parent) {
      this.parent && this.container.remove(), this.parent = i.parent, this.createContainer();
      for (let n of this.manager.tooltipViews)
        this.container.appendChild(n.dom);
      e = !0;
    } else this.parent && this.view.themeClasses != this.classes && (this.classes = this.container.className = this.view.themeClasses);
    e && this.maybeMeasure();
  }
  createTooltip(s, t) {
    let e = s.create(this.view), i = t ? t.dom : null;
    if (e.dom.classList.add("cm-tooltip"), s.arrow && !e.dom.querySelector(".cm-tooltip > .cm-tooltip-arrow")) {
      let n = document.createElement("div");
      n.className = "cm-tooltip-arrow", e.dom.appendChild(n);
    }
    return e.dom.style.position = this.position, e.dom.style.top = Ni, e.dom.style.left = "0px", this.container.insertBefore(e.dom, i), e.mount && e.mount(this.view), this.resizeObserver && this.resizeObserver.observe(e.dom), e;
  }
  destroy() {
    var s, t, e;
    this.view.win.removeEventListener("resize", this.measureSoon);
    for (let i of this.manager.tooltipViews)
      i.dom.remove(), (s = i.destroy) === null || s === void 0 || s.call(i);
    this.parent && this.container.remove(), (t = this.resizeObserver) === null || t === void 0 || t.disconnect(), (e = this.intersectionObserver) === null || e === void 0 || e.disconnect(), clearTimeout(this.measureTimeout);
  }
  readMeasure() {
    let s = 1, t = 1, e = !1;
    if (this.position == "fixed" && this.manager.tooltipViews.length) {
      let { dom: r } = this.manager.tooltipViews[0];
      if (S.safari) {
        let o = r.getBoundingClientRect();
        e = Math.abs(o.top + 1e4) > 1 || Math.abs(o.left) > 1;
      } else
        e = !!r.offsetParent && r.offsetParent != this.container.ownerDocument.body;
    }
    if (e || this.position == "absolute")
      if (this.parent) {
        let r = this.parent.getBoundingClientRect();
        r.width && r.height && (s = r.width / this.parent.offsetWidth, t = r.height / this.parent.offsetHeight);
      } else
        ({ scaleX: s, scaleY: t } = this.view.viewState);
    let i = this.view.scrollDOM.getBoundingClientRect(), n = sr(this.view);
    return {
      visible: {
        left: i.left + n.left,
        top: i.top + n.top,
        right: i.right - n.right,
        bottom: i.bottom - n.bottom
      },
      parent: this.parent ? this.container.getBoundingClientRect() : this.view.dom.getBoundingClientRect(),
      pos: this.manager.tooltips.map((r, o) => {
        let l = this.manager.tooltipViews[o];
        return l.getCoords ? l.getCoords(r.pos) : this.view.coordsAtPos(r.pos);
      }),
      size: this.manager.tooltipViews.map(({ dom: r }) => r.getBoundingClientRect()),
      space: this.view.state.facet(Yn).tooltipSpace(this.view),
      scaleX: s,
      scaleY: t,
      makeAbsolute: e
    };
  }
  writeMeasure(s) {
    var t;
    if (s.makeAbsolute) {
      this.madeAbsolute = !0, this.position = "absolute";
      for (let l of this.manager.tooltipViews)
        l.dom.style.position = "absolute";
    }
    let { visible: e, space: i, scaleX: n, scaleY: r } = s, o = [];
    for (let l = 0; l < this.manager.tooltips.length; l++) {
      let a = this.manager.tooltips[l], h = this.manager.tooltipViews[l], { dom: f } = h, c = s.pos[l], u = s.size[l];
      if (!c || a.clip !== !1 && (c.bottom <= Math.max(e.top, i.top) || c.top >= Math.min(e.bottom, i.bottom) || c.right < Math.max(e.left, i.left) - 0.1 || c.left > Math.min(e.right, i.right) + 0.1)) {
        f.style.top = Ni;
        continue;
      }
      let d = a.arrow ? h.dom.querySelector(".cm-tooltip-arrow") : null, p = d ? 7 : 0, g = u.right - u.left, m = (t = uo.get(h)) !== null && t !== void 0 ? t : u.bottom - u.top, y = h.offset || Ac, w = this.view.textDirection == $.LTR, b = u.width > i.right - i.left ? w ? i.left : i.right - u.width : w ? Math.max(i.left, Math.min(c.left - (d ? 14 : 0) + y.x, i.right - g)) : Math.min(Math.max(i.left, c.left - g + (d ? 14 : 0) - y.x), i.right - g), O = this.above[l];
      !a.strictSide && (O ? c.top - m - p - y.y < i.top : c.bottom + m + p + y.y > i.bottom) && O == i.bottom - c.bottom > c.top - i.top && (O = this.above[l] = !O);
      let v = (O ? c.top - i.top : i.bottom - c.bottom) - p;
      if (v < m && h.resize !== !1) {
        if (v < this.view.defaultLineHeight) {
          f.style.top = Ni;
          continue;
        }
        uo.set(h, m), f.style.height = (m = v) / r + "px";
      } else f.style.height && (f.style.height = "");
      let T = O ? c.top - m - p - y.y : c.bottom + p + y.y, A = b + g;
      if (h.overlap !== !0)
        for (let L of o)
          L.left < A && L.right > b && L.top < T + m && L.bottom > T && (T = O ? L.top - m - 2 - p : L.bottom + p + 2);
      if (this.position == "absolute" ? (f.style.top = (T - s.parent.top) / r + "px", po(f, (b - s.parent.left) / n)) : (f.style.top = T / r + "px", po(f, b / n)), d) {
        let L = c.left + (w ? y.x : -y.x) - (b + 14 - 7);
        d.style.left = L / n + "px";
      }
      h.overlap !== !0 && o.push({ left: b, top: T, right: A, bottom: T + m }), f.classList.toggle("cm-tooltip-above", O), f.classList.toggle("cm-tooltip-below", !O), h.positioned && h.positioned(s.space);
    }
  }
  maybeMeasure() {
    if (this.manager.tooltips.length && (this.view.inView && this.view.requestMeasure(this.measureReq), this.inView != this.view.inView && (this.inView = this.view.inView, !this.inView)))
      for (let s of this.manager.tooltipViews)
        s.dom.style.top = Ni;
  }
}, {
  eventObservers: {
    scroll() {
      this.maybeMeasure();
    }
  }
});
function po(s, t) {
  let e = parseInt(s.style.left, 10);
  (isNaN(e) || Math.abs(t - e) > 1) && (s.style.left = t + "px");
}
const Cc = /* @__PURE__ */ M.baseTheme({
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
}), Ac = { x: 0, y: 0 }, fr = /* @__PURE__ */ P.define({
  enables: [hr, Cc]
}), un = /* @__PURE__ */ P.define({
  combine: (s) => s.reduce((t, e) => t.concat(e), [])
});
class Ln {
  // Needs to be static so that host tooltip instances always match
  static create(t) {
    return new Ln(t);
  }
  constructor(t) {
    this.view = t, this.mounted = !1, this.dom = document.createElement("div"), this.dom.classList.add("cm-tooltip-hover"), this.manager = new aa(t, un, (e, i) => this.createHostedView(e, i), (e) => e.dom.remove());
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
      let n = i[t];
      if (n !== void 0) {
        if (e === void 0)
          e = n;
        else if (e !== n)
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
const Tc = /* @__PURE__ */ fr.compute([un], (s) => {
  let t = s.facet(un);
  return t.length === 0 ? null : {
    pos: Math.min(...t.map((e) => e.pos)),
    end: Math.max(...t.map((e) => {
      var i;
      return (i = e.end) !== null && i !== void 0 ? i : e.pos;
    })),
    create: Ln.create,
    above: t[0].above,
    arrow: t.some((e) => e.arrow)
  };
});
class Pc {
  constructor(t, e, i, n, r) {
    this.view = t, this.source = e, this.field = i, this.setHover = n, this.hoverTime = r, this.hoverTimeout = -1, this.restartTimeout = -1, this.pending = null, this.lastMove = { x: 0, y: 0, target: t.dom, time: 0 }, this.checkHover = this.checkHover.bind(this), t.dom.addEventListener("mouseleave", this.mouseleave = this.mouseleave.bind(this)), t.dom.addEventListener("mousemove", this.mousemove = this.mousemove.bind(this));
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
    let n, r = 1;
    if (i.isWidget())
      n = i.posAtStart;
    else {
      if (n = t.posAtCoords(e), n == null)
        return;
      let l = t.coordsAtPos(n);
      if (!l || e.y < l.top || e.y > l.bottom || e.x < l.left - t.defaultCharacterWidth || e.x > l.right + t.defaultCharacterWidth)
        return;
      let a = t.bidiSpans(t.state.doc.lineAt(n)).find((f) => f.from <= n && f.to >= n), h = a && a.dir == $.RTL ? -1 : 1;
      r = e.x < l.left ? -h : h;
    }
    let o = this.source(t, n, r);
    if (o != null && o.then) {
      let l = this.pending = { pos: n };
      o.then((a) => {
        this.pending == l && (this.pending = null, a && !(Array.isArray(a) && !a.length) && t.dispatch({ effects: this.setHover.of(Array.isArray(a) ? a : [a]) }));
      }, (a) => ft(t.state, a, "hover tooltip"));
    } else o && !(Array.isArray(o) && !o.length) && t.dispatch({ effects: this.setHover.of(Array.isArray(o) ? o : [o]) });
  }
  get tooltip() {
    let t = this.view.plugin(hr), e = t ? t.manager.tooltips.findIndex((i) => i.create == Ln.create) : -1;
    return e > -1 ? t.manager.tooltipViews[e] : null;
  }
  mousemove(t) {
    var e, i;
    this.lastMove = { x: t.clientX, y: t.clientY, target: t.target, time: Date.now() }, this.hoverTimeout < 0 && (this.hoverTimeout = setTimeout(this.checkHover, this.hoverTime));
    let { active: n, tooltip: r } = this;
    if (n.length && r && !Mc(r.dom, t) || this.pending) {
      let { pos: o } = n[0] || this.pending, l = (i = (e = n[0]) === null || e === void 0 ? void 0 : e.end) !== null && i !== void 0 ? i : o;
      (o == l ? this.view.posAtCoords(this.lastMove) != o : !Dc(this.view, o, l, t.clientX, t.clientY)) && (this.view.dispatch({ effects: this.setHover.of([]) }), this.pending = null);
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
const Vi = 4;
function Mc(s, t) {
  let { left: e, right: i, top: n, bottom: r } = s.getBoundingClientRect(), o;
  if (o = s.querySelector(".cm-tooltip-arrow")) {
    let l = o.getBoundingClientRect();
    n = Math.min(l.top, n), r = Math.max(l.bottom, r);
  }
  return t.clientX >= e - Vi && t.clientX <= i + Vi && t.clientY >= n - Vi && t.clientY <= r + Vi;
}
function Dc(s, t, e, i, n, r) {
  let o = s.scrollDOM.getBoundingClientRect(), l = s.documentTop + s.documentPadding.top + s.contentHeight;
  if (o.left > i || o.right < i || o.top > n || Math.min(o.bottom, l) < n)
    return !1;
  let a = s.posAtCoords({ x: i, y: n }, !1);
  return a >= t && a <= e;
}
function Rc(s, t = {}) {
  let e = I.define(), i = Pt.define({
    create() {
      return [];
    },
    update(n, r) {
      if (n.length && (t.hideOnChange && (r.docChanged || r.selection) ? n = [] : t.hideOn && (n = n.filter((o) => !t.hideOn(r, o))), r.docChanged)) {
        let o = [];
        for (let l of n) {
          let a = r.changes.mapPos(l.pos, -1, at.TrackDel);
          if (a != null) {
            let h = Object.assign(/* @__PURE__ */ Object.create(null), l);
            h.pos = a, h.end != null && (h.end = r.changes.mapPos(h.end)), o.push(h);
          }
        }
        n = o;
      }
      for (let o of r.effects)
        o.is(e) && (n = o.value), o.is(Bc) && (n = []);
      return n;
    },
    provide: (n) => un.from(n)
  });
  return {
    active: i,
    extension: [
      i,
      $t.define((n) => new Pc(
        n,
        s,
        i,
        e,
        t.hoverTime || 300
        /* Hover.Time */
      )),
      Tc
    ]
  };
}
function ha(s, t) {
  let e = s.plugin(hr);
  if (!e)
    return null;
  let i = e.manager.tooltips.indexOf(t);
  return i < 0 ? null : e.manager.tooltipViews[i];
}
const Bc = /* @__PURE__ */ I.define(), go = /* @__PURE__ */ P.define({
  combine(s) {
    let t, e;
    for (let i of s)
      t = t || i.topContainer, e = e || i.bottomContainer;
    return { topContainer: t, bottomContainer: e };
  }
}), Lc = /* @__PURE__ */ $t.fromClass(class {
  constructor(s) {
    this.input = s.state.facet(Ns), this.specs = this.input.filter((e) => e), this.panels = this.specs.map((e) => e(s));
    let t = s.state.facet(go);
    this.top = new Wi(s, !0, t.topContainer), this.bottom = new Wi(s, !1, t.bottomContainer), this.top.sync(this.panels.filter((e) => e.top)), this.bottom.sync(this.panels.filter((e) => !e.top));
    for (let e of this.panels)
      e.dom.classList.add("cm-panel"), e.mount && e.mount();
  }
  update(s) {
    let t = s.state.facet(go);
    this.top.container != t.topContainer && (this.top.sync([]), this.top = new Wi(s.view, !0, t.topContainer)), this.bottom.container != t.bottomContainer && (this.bottom.sync([]), this.bottom = new Wi(s.view, !1, t.bottomContainer)), this.top.syncClasses(), this.bottom.syncClasses();
    let e = s.state.facet(Ns);
    if (e != this.input) {
      let i = e.filter((a) => a), n = [], r = [], o = [], l = [];
      for (let a of i) {
        let h = this.specs.indexOf(a), f;
        h < 0 ? (f = a(s.view), l.push(f)) : (f = this.panels[h], f.update && f.update(s)), n.push(f), (f.top ? r : o).push(f);
      }
      this.specs = i, this.panels = n, this.top.sync(r), this.bottom.sync(o);
      for (let a of l)
        a.dom.classList.add("cm-panel"), a.mount && a.mount();
    } else
      for (let i of this.panels)
        i.update && i.update(s);
  }
  destroy() {
    this.top.sync([]), this.bottom.sync([]);
  }
}, {
  provide: (s) => M.scrollMargins.of((t) => {
    let e = t.plugin(s);
    return e && { top: e.top.scrollMargin(), bottom: e.bottom.scrollMargin() };
  })
});
class Wi {
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
          t = mo(t);
        t = t.nextSibling;
      } else
        this.dom.insertBefore(e.dom, t);
    for (; t; )
      t = mo(t);
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
function mo(s) {
  let t = s.nextSibling;
  return s.remove(), t;
}
const Ns = /* @__PURE__ */ P.define({
  enables: Lc
});
class Fe extends se {
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
Fe.prototype.elementClass = "";
Fe.prototype.toDOM = void 0;
Fe.prototype.mapMode = at.TrackBefore;
Fe.prototype.startSide = Fe.prototype.endSide = -1;
Fe.prototype.point = !0;
const fa = 1024;
let Ec = 0;
class Gn {
  constructor(t, e) {
    this.from = t, this.to = e;
  }
}
class R {
  /**
  Create a new node prop type.
  */
  constructor(t = {}) {
    this.id = Ec++, this.perNode = !!t.perNode, this.deserialize = t.deserialize || (() => {
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
    return typeof t != "function" && (t = dt.match(t)), (e) => {
      let i = t(e);
      return i === void 0 ? null : [this, i];
    };
  }
}
R.closedBy = new R({ deserialize: (s) => s.split(" ") });
R.openedBy = new R({ deserialize: (s) => s.split(" ") });
R.group = new R({ deserialize: (s) => s.split(" ") });
R.isolate = new R({ deserialize: (s) => {
  if (s && s != "rtl" && s != "ltr" && s != "auto")
    throw new RangeError("Invalid value for isolate: " + s);
  return s || "auto";
} });
R.contextHash = new R({ perNode: !0 });
R.lookAhead = new R({ perNode: !0 });
R.mounted = new R({ perNode: !0 });
class oi {
  constructor(t, e, i, n = !1) {
    this.tree = t, this.overlay = e, this.parser = i, this.bracketed = n;
  }
  /**
  @internal
  */
  static get(t) {
    return t && t.props && t.props[R.mounted.id];
  }
}
const Ic = /* @__PURE__ */ Object.create(null);
class dt {
  /**
  @internal
  */
  constructor(t, e, i, n = 0) {
    this.name = t, this.props = e, this.id = i, this.flags = n;
  }
  /**
  Define a node type.
  */
  static define(t) {
    let e = t.props && t.props.length ? /* @__PURE__ */ Object.create(null) : Ic, i = (t.top ? 1 : 0) | (t.skipped ? 2 : 0) | (t.error ? 4 : 0) | (t.name == null ? 8 : 0), n = new dt(t.name || "", e, t.id, i);
    if (t.props) {
      for (let r of t.props)
        if (Array.isArray(r) || (r = r(n)), r) {
          if (r[0].perNode)
            throw new RangeError("Can't store a per-node prop on a node type");
          e[r[0].id] = r[1];
        }
    }
    return n;
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
      let e = this.prop(R.group);
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
      for (let n of i.split(" "))
        e[n] = t[i];
    return (i) => {
      for (let n = i.prop(R.group), r = -1; r < (n ? n.length : 0); r++) {
        let o = e[r < 0 ? i.name : n[r]];
        if (o)
          return o;
      }
    };
  }
}
dt.none = new dt(
  "",
  /* @__PURE__ */ Object.create(null),
  0,
  8
  /* NodeFlag.Anonymous */
);
class cr {
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
      let n = null;
      for (let r of t) {
        let o = r(i);
        if (o) {
          n || (n = Object.assign({}, i.props));
          let l = o[1], a = o[0];
          a.combine && a.id in n && (l = a.combine(n[a.id], l)), n[a.id] = l;
        }
      }
      e.push(n ? new dt(i.name, n, i.id, i.flags) : i);
    }
    return new cr(e);
  }
}
const Hi = /* @__PURE__ */ new WeakMap(), yo = /* @__PURE__ */ new WeakMap();
var X;
(function(s) {
  s[s.ExcludeBuffers = 1] = "ExcludeBuffers", s[s.IncludeAnonymous = 2] = "IncludeAnonymous", s[s.IgnoreMounts = 4] = "IgnoreMounts", s[s.IgnoreOverlays = 8] = "IgnoreOverlays", s[s.EnterBracketed = 16] = "EnterBracketed";
})(X || (X = {}));
class _ {
  /**
  Construct a new tree. See also [`Tree.build`](#common.Tree^build).
  */
  constructor(t, e, i, n, r) {
    if (this.type = t, this.children = e, this.positions = i, this.length = n, this.props = null, r && r.length) {
      this.props = /* @__PURE__ */ Object.create(null);
      for (let [o, l] of r)
        this.props[typeof o == "number" ? o : o.id] = l;
    }
  }
  /**
  @internal
  */
  toString() {
    let t = oi.get(this);
    if (t && !t.overlay)
      return t.tree.toString();
    let e = "";
    for (let i of this.children) {
      let n = i.toString();
      n && (e && (e += ","), e += n);
    }
    return this.type.name ? (/\W/.test(this.type.name) && !this.type.isError ? JSON.stringify(this.type.name) : this.type.name) + (e.length ? "(" + e + ")" : "") : e;
  }
  /**
  Get a [tree cursor](#common.TreeCursor) positioned at the top of
  the tree. Mode can be used to [control](#common.IterMode) which
  nodes the cursor visits.
  */
  cursor(t = 0) {
    return new Ws(this.topNode, t);
  }
  /**
  Get a [tree cursor](#common.TreeCursor) pointing into this tree
  at the given position and side (see
  [`moveTo`](#common.TreeCursor.moveTo).
  */
  cursorAt(t, e = 0, i = 0) {
    let n = Hi.get(this) || this.topNode, r = new Ws(n);
    return r.moveTo(t, e), Hi.set(this, r._tree), r;
  }
  /**
  Get a [syntax node](#common.SyntaxNode) object for the top of the
  tree.
  */
  get topNode() {
    return new St(this, 0, 0, null);
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
    let i = pi(Hi.get(this) || this.topNode, t, e, !1);
    return Hi.set(this, i), i;
  }
  /**
  Like [`resolve`](#common.Tree.resolve), but will enter
  [overlaid](#common.MountedTree.overlay) nodes, producing a syntax node
  pointing into the innermost overlaid tree at the given position
  (with parent links going through all parent structure, including
  the host trees).
  */
  resolveInner(t, e = 0) {
    let i = pi(yo.get(this) || this.topNode, t, e, !0);
    return yo.set(this, i), i;
  }
  /**
  In some situations, it can be useful to iterate through all
  nodes around a position, including those in overlays that don't
  directly cover the position. This method gives you an iterator
  that will produce all nodes, from small to big, around the given
  position.
  */
  resolveStack(t, e = 0) {
    return Wc(this, t, e);
  }
  /**
  Iterate over the tree and its children, calling `enter` for any
  node that touches the `from`/`to` region (if given) before
  running over such a node's children, and `leave` (if given) when
  leaving the node. When `enter` returns `false`, that node will
  not have its children iterated over (or `leave` called).
  */
  iterate(t) {
    let { enter: e, leave: i, from: n = 0, to: r = this.length } = t, o = t.mode || 0, l = (o & X.IncludeAnonymous) > 0;
    for (let a = this.cursor(o | X.IncludeAnonymous); ; ) {
      let h = !1;
      if (a.from <= r && a.to >= n && (!l && a.type.isAnonymous || e(a) !== !1)) {
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
    return this.children.length <= 8 ? this : pr(dt.none, this.children, this.positions, 0, this.children.length, 0, this.length, (e, i, n) => new _(this.type, e, i, n, this.propValues), t.makeTree || ((e, i, n) => new _(dt.none, e, i, n)));
  }
  /**
  Build a tree from a postfix-ordered buffer of node information,
  or a cursor over such a buffer.
  */
  static build(t) {
    return Hc(t);
  }
}
_.empty = new _(dt.none, [], [], 0);
class ur {
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
    return new ur(this.buffer, this.index);
  }
}
class le {
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
    return dt.none;
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
    let e = this.buffer[t], i = this.buffer[t + 3], n = this.set.types[e], r = n.name;
    if (/\W/.test(r) && !n.isError && (r = JSON.stringify(r)), t += 4, i == t)
      return r;
    let o = [];
    for (; t < i; )
      o.push(this.childString(t)), t = this.buffer[t + 3];
    return r + "(" + o.join(",") + ")";
  }
  /**
  @internal
  */
  findChild(t, e, i, n, r) {
    let { buffer: o } = this, l = -1;
    for (let a = t; a != e && !(ca(r, n, o[a + 1], o[a + 2]) && (l = a, i > 0)); a = o[a + 3])
      ;
    return l;
  }
  /**
  @internal
  */
  slice(t, e, i) {
    let n = this.buffer, r = new Uint16Array(e - t), o = 0;
    for (let l = t, a = 0; l < e; ) {
      r[a++] = n[l++], r[a++] = n[l++] - i;
      let h = r[a++] = n[l++] - i;
      r[a++] = n[l++] - t, o = Math.max(o, h);
    }
    return new le(r, o, this.set);
  }
}
function ca(s, t, e, i) {
  switch (s) {
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
function pi(s, t, e, i) {
  for (var n; s.from == s.to || (e < 1 ? s.from >= t : s.from > t) || (e > -1 ? s.to <= t : s.to < t); ) {
    let o = !i && s instanceof St && s.index < 0 ? null : s.parent;
    if (!o)
      return s;
    s = o;
  }
  let r = i ? 0 : X.IgnoreOverlays;
  if (i)
    for (let o = s, l = o.parent; l; o = l, l = o.parent)
      o instanceof St && o.index < 0 && ((n = l.enter(t, e, r)) === null || n === void 0 ? void 0 : n.from) != o.from && (s = l);
  for (; ; ) {
    let o = s.enter(t, e, r);
    if (!o)
      return s;
    s = o;
  }
}
class ua {
  cursor(t = 0) {
    return new Ws(this, t);
  }
  getChild(t, e = null, i = null) {
    let n = bo(this, t, e, i);
    return n.length ? n[0] : null;
  }
  getChildren(t, e = null, i = null) {
    return bo(this, t, e, i);
  }
  resolve(t, e = 0) {
    return pi(this, t, e, !1);
  }
  resolveInner(t, e = 0) {
    return pi(this, t, e, !0);
  }
  matchContext(t) {
    return Vs(this.parent, t);
  }
  enterUnfinishedNodesBefore(t) {
    let e = this.childBefore(t), i = this;
    for (; e; ) {
      let n = e.lastChild;
      if (!n || n.to != e.to)
        break;
      n.type.isError && n.from == n.to ? (i = e, e = n.prevSibling) : e = n;
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
class St extends ua {
  constructor(t, e, i, n) {
    super(), this._tree = t, this.from = e, this.index = i, this._parent = n;
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
  nextChild(t, e, i, n, r = 0) {
    for (let o = this; ; ) {
      for (let { children: l, positions: a } = o._tree, h = e > 0 ? l.length : -1; t != h; t += e) {
        let f = l[t], c = a[t] + o.from, u;
        if (!(!(r & X.EnterBracketed && f instanceof _ && (u = oi.get(f)) && !u.overlay && u.bracketed && i >= c && i <= c + f.length) && !ca(n, i, c, c + f.length))) {
          if (f instanceof le) {
            if (r & X.ExcludeBuffers)
              continue;
            let d = f.findChild(0, f.buffer.length, e, i - c, n);
            if (d > -1)
              return new ne(new Nc(o, f, t, c), null, d);
          } else if (r & X.IncludeAnonymous || !f.type.isAnonymous || dr(f)) {
            let d;
            if (!(r & X.IgnoreMounts) && (d = oi.get(f)) && !d.overlay)
              return new St(d.tree, c, t, o);
            let p = new St(f, c, t, o);
            return r & X.IncludeAnonymous || !p.type.isAnonymous ? p : p.nextChild(e < 0 ? f.children.length - 1 : 0, e, i, n, r);
          }
        }
      }
      if (r & X.IncludeAnonymous || !o.type.isAnonymous || (o.index >= 0 ? t = o.index + e : t = e < 0 ? -1 : o._parent._tree.children.length, o = o._parent, !o))
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
    let n;
    if (!(i & X.IgnoreOverlays) && (n = oi.get(this._tree)) && n.overlay) {
      let r = t - this.from, o = i & X.EnterBracketed && n.bracketed;
      for (let { from: l, to: a } of n.overlay)
        if ((e > 0 || o ? l <= r : l < r) && (e < 0 || o ? a >= r : a > r))
          return new St(n.tree, n.overlay[0].from + this.from, -1, this);
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
function bo(s, t, e, i) {
  let n = s.cursor(), r = [];
  if (!n.firstChild())
    return r;
  if (e != null) {
    for (let o = !1; !o; )
      if (o = n.type.is(e), !n.nextSibling())
        return r;
  }
  for (; ; ) {
    if (i != null && n.type.is(i))
      return r;
    if (n.type.is(t) && r.push(n.node), !n.nextSibling())
      return i == null ? r : [];
  }
}
function Vs(s, t, e = t.length - 1) {
  for (let i = s; e >= 0; i = i.parent) {
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
class Nc {
  constructor(t, e, i, n) {
    this.parent = t, this.buffer = e, this.index = i, this.start = n;
  }
}
class ne extends ua {
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
    let { buffer: n } = this.context, r = n.findChild(this.index + 4, n.buffer[this.index + 3], t, e - this.context.start, i);
    return r < 0 ? null : new ne(this.context, this, r);
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
    if (i & X.ExcludeBuffers)
      return null;
    let { buffer: n } = this.context, r = n.findChild(this.index + 4, n.buffer[this.index + 3], e > 0 ? 1 : -1, t - this.context.start, e);
    return r < 0 ? null : new ne(this.context, this, r);
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
    return e < (this._parent ? t.buffer[this._parent.index + 3] : t.buffer.length) ? new ne(this.context, this._parent, e) : this.externalSibling(1);
  }
  get prevSibling() {
    let { buffer: t } = this.context, e = this._parent ? this._parent.index + 4 : 0;
    return this.index == e ? this.externalSibling(-1) : new ne(this.context, this._parent, t.findChild(
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
    let t = [], e = [], { buffer: i } = this.context, n = this.index + 4, r = i.buffer[this.index + 3];
    if (r > n) {
      let o = i.buffer[this.index + 1];
      t.push(i.slice(n, r, o)), e.push(0);
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
function da(s) {
  if (!s.length)
    return null;
  let t = 0, e = s[0];
  for (let r = 1; r < s.length; r++) {
    let o = s[r];
    (o.from > e.from || o.to < e.to) && (e = o, t = r);
  }
  let i = e instanceof St && e.index < 0 ? null : e.parent, n = s.slice();
  return i ? n[t] = i : n.splice(t, 1), new Vc(n, e);
}
class Vc {
  constructor(t, e) {
    this.heads = t, this.node = e;
  }
  get next() {
    return da(this.heads);
  }
}
function Wc(s, t, e) {
  let i = s.resolveInner(t, e), n = null;
  for (let r = i instanceof St ? i : i.context.parent; r; r = r.parent)
    if (r.index < 0) {
      let o = r.parent;
      (n || (n = [i])).push(o.resolve(t, e)), r = o;
    } else {
      let o = oi.get(r.tree);
      if (o && o.overlay && o.overlay[0].from <= t && o.overlay[o.overlay.length - 1].to >= t) {
        let l = new St(o.tree, o.overlay[0].from + r.from, -1, r);
        (n || (n = [i])).push(pi(l, t, e, !1));
      }
    }
  return n ? da(n) : i;
}
class Ws {
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
    if (this.buffer = null, this.stack = [], this.index = 0, this.bufferNode = null, this.mode = e & ~X.EnterBracketed, t instanceof St)
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
    let { start: i, buffer: n } = this.buffer;
    return this.type = e || n.set.types[n.buffer[t]], this.from = i + n.buffer[t + 1], this.to = i + n.buffer[t + 2], !0;
  }
  /**
  @internal
  */
  yield(t) {
    return t ? t instanceof St ? (this.buffer = null, this.yieldNode(t)) : (this.buffer = t.context, this.yieldBuf(t.index, t.type)) : !1;
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
    let { buffer: n } = this.buffer, r = n.findChild(this.index + 4, n.buffer[this.index + 3], t, e - this.buffer.start, i);
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
    return this.buffer ? i & X.ExcludeBuffers ? !1 : this.enterChild(1, t, e) : this.yield(this._tree.enter(t, e, i));
  }
  /**
  Move to the node's parent node, if this isn't the top node.
  */
  parent() {
    if (!this.buffer)
      return this.yieldNode(this.mode & X.IncludeAnonymous ? this._tree._parent : this._tree.parent);
    if (this.stack.length)
      return this.yieldBuf(this.stack.pop());
    let t = this.mode & X.IncludeAnonymous ? this.buffer.parent : this.buffer.parent.nextSignificantParent();
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
      let n = i < 0 ? 0 : this.stack[i] + 4;
      if (this.index != n)
        return this.yieldBuf(e.findChild(
          n,
          this.index,
          -1,
          0,
          4
          /* Side.DontCare */
        ));
    } else {
      let n = e.buffer[this.index + 3];
      if (n < (i < 0 ? e.buffer.length : e.buffer[this.stack[i] + 3]))
        return this.yieldBuf(n);
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
    let e, i, { buffer: n } = this;
    if (n) {
      if (t > 0) {
        if (this.index < n.buffer.buffer.length)
          return !1;
      } else
        for (let r = 0; r < this.index; r++)
          if (n.buffer.buffer[r + 3] < this.index)
            return !1;
      ({ index: e, parent: i } = n);
    } else
      ({ index: e, _parent: i } = this._tree);
    for (; i; { index: e, _parent: i } = i)
      if (e > -1)
        for (let r = e + t, o = t < 0 ? -1 : i._tree.children.length; r != o; r += t) {
          let l = i._tree.children[r];
          if (this.mode & X.IncludeAnonymous || l instanceof le || !l.type.isAnonymous || dr(l))
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
      t: for (let n = this.index, r = this.stack.length; r >= 0; ) {
        for (let o = t; o; o = o._parent)
          if (o.index == n) {
            if (n == this.index)
              return o;
            e = o, i = r + 1;
            break t;
          }
        n = this.stack[--r];
      }
    for (let n = i; n < this.stack.length; n++)
      e = new ne(this.buffer, e, this.stack[n]);
    return this.bufferNode = new ne(this.buffer, e, this.index);
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
      let n = !1;
      if (this.type.isAnonymous || t(this) !== !1) {
        if (this.firstChild()) {
          i++;
          continue;
        }
        this.type.isAnonymous || (n = !0);
      }
      for (; ; ) {
        if (n && e && e(this), n = this.type.isAnonymous, !i)
          return;
        if (this.nextSibling())
          break;
        this.parent(), i--, n = !0;
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
      return Vs(this.node.parent, t);
    let { buffer: e } = this.buffer, { types: i } = e.set;
    for (let n = t.length - 1, r = this.stack.length - 1; n >= 0; r--) {
      if (r < 0)
        return Vs(this._tree, t, n);
      let o = i[e.buffer[this.stack[r]]];
      if (!o.isAnonymous) {
        if (t[n] && t[n] != o.name)
          return !1;
        n--;
      }
    }
    return !0;
  }
}
function dr(s) {
  return s.children.some((t) => t instanceof le || !t.type.isAnonymous || dr(t));
}
function Hc(s) {
  var t;
  let { buffer: e, nodeSet: i, maxBufferLength: n = fa, reused: r = [], minRepeatType: o = i.types.length } = s, l = Array.isArray(e) ? new ur(e, e.length) : e, a = i.types, h = 0, f = 0;
  function c(v, T, A, L, H, j) {
    let { id: B, start: D, end: z, size: Q } = l, J = f, Zt = h;
    if (Q < 0)
      if (l.next(), Q == -1) {
        let jt = r[B];
        A.push(jt), L.push(D - v);
        return;
      } else if (Q == -3) {
        h = B;
        return;
      } else if (Q == -4) {
        f = B;
        return;
      } else
        throw new RangeError(`Unrecognized record size: ${Q}`);
    let Xe = a[B], vi, he, Or = D - v;
    if (z - D <= n && (he = m(l.pos - T, H))) {
      let jt = new Uint16Array(he.size - he.skip), mt = l.pos - he.size, Rt = jt.length;
      for (; l.pos > mt; )
        Rt = y(he.start, jt, Rt);
      vi = new le(jt, z - he.start, i), Or = he.start - v;
    } else {
      let jt = l.pos - Q;
      l.next();
      let mt = [], Rt = [], fe = B >= o ? B : -1, ve = 0, Oi = z;
      for (; l.pos > jt; )
        fe >= 0 && l.id == fe && l.size >= 0 ? (l.end <= Oi - n && (p(mt, Rt, D, ve, l.end, Oi, fe, J, Zt), ve = mt.length, Oi = l.end), l.next()) : j > 2500 ? u(D, jt, mt, Rt) : c(D, jt, mt, Rt, fe, j + 1);
      if (fe >= 0 && ve > 0 && ve < mt.length && p(mt, Rt, D, ve, D, Oi, fe, J, Zt), mt.reverse(), Rt.reverse(), fe > -1 && ve > 0) {
        let Cr = d(Xe, Zt);
        vi = pr(Xe, mt, Rt, 0, mt.length, 0, z - D, Cr, Cr);
      } else
        vi = g(Xe, mt, Rt, z - D, J - z, Zt);
    }
    A.push(vi), L.push(Or);
  }
  function u(v, T, A, L) {
    let H = [], j = 0, B = -1;
    for (; l.pos > T; ) {
      let { id: D, start: z, end: Q, size: J } = l;
      if (J > 4)
        l.next();
      else {
        if (B > -1 && z < B)
          break;
        B < 0 && (B = Q - n), H.push(D, z, Q), j++, l.next();
      }
    }
    if (j) {
      let D = new Uint16Array(j * 4), z = H[H.length - 2];
      for (let Q = H.length - 3, J = 0; Q >= 0; Q -= 3)
        D[J++] = H[Q], D[J++] = H[Q + 1] - z, D[J++] = H[Q + 2] - z, D[J++] = J;
      A.push(new le(D, H[2] - z, i)), L.push(z - v);
    }
  }
  function d(v, T) {
    return (A, L, H) => {
      let j = 0, B = A.length - 1, D, z;
      if (B >= 0 && (D = A[B]) instanceof _) {
        if (!B && D.type == v && D.length == H)
          return D;
        (z = D.prop(R.lookAhead)) && (j = L[B] + D.length + z);
      }
      return g(v, A, L, H, j, T);
    };
  }
  function p(v, T, A, L, H, j, B, D, z) {
    let Q = [], J = [];
    for (; v.length > L; )
      Q.push(v.pop()), J.push(T.pop() + A - H);
    v.push(g(i.types[B], Q, J, j - H, D - j, z)), T.push(H - A);
  }
  function g(v, T, A, L, H, j, B) {
    if (j) {
      let D = [R.contextHash, j];
      B = B ? [D].concat(B) : [D];
    }
    if (H > 25) {
      let D = [R.lookAhead, H];
      B = B ? [D].concat(B) : [D];
    }
    return new _(v, T, A, L, B);
  }
  function m(v, T) {
    let A = l.fork(), L = 0, H = 0, j = 0, B = A.end - n, D = { size: 0, start: 0, skip: 0 };
    t: for (let z = A.pos - v; A.pos > z; ) {
      let Q = A.size;
      if (A.id == T && Q >= 0) {
        D.size = L, D.start = H, D.skip = j, j += 4, L += 4, A.next();
        continue;
      }
      let J = A.pos - Q;
      if (Q < 0 || J < z || A.start < B)
        break;
      let Zt = A.id >= o ? 4 : 0, Xe = A.start;
      for (A.next(); A.pos > J; ) {
        if (A.size < 0)
          if (A.size == -3 || A.size == -4)
            Zt += 4;
          else
            break t;
        else A.id >= o && (Zt += 4);
        A.next();
      }
      H = Xe, L += Q, j += Zt;
    }
    return (T < 0 || L == v) && (D.size = L, D.start = H, D.skip = j), D.size > 4 ? D : void 0;
  }
  function y(v, T, A) {
    let { id: L, start: H, end: j, size: B } = l;
    if (l.next(), B >= 0 && L < o) {
      let D = A;
      if (B > 4) {
        let z = l.pos - (B - 4);
        for (; l.pos > z; )
          A = y(v, T, A);
      }
      T[--A] = D, T[--A] = j - v, T[--A] = H - v, T[--A] = L;
    } else B == -3 ? h = L : B == -4 && (f = L);
    return A;
  }
  let w = [], b = [];
  for (; l.pos > 0; )
    c(s.start || 0, s.bufferStart || 0, w, b, -1, 0);
  let O = (t = s.length) !== null && t !== void 0 ? t : w.length ? b[0] + w[0].length : 0;
  return new _(a[s.topID], w.reverse(), b.reverse(), O);
}
const wo = /* @__PURE__ */ new WeakMap();
function Zi(s, t) {
  if (!s.isAnonymous || t instanceof le || t.type != s)
    return 1;
  let e = wo.get(t);
  if (e == null) {
    e = 1;
    for (let i of t.children) {
      if (i.type != s || !(i instanceof _)) {
        e = 1;
        break;
      }
      e += Zi(s, i);
    }
    wo.set(t, e);
  }
  return e;
}
function pr(s, t, e, i, n, r, o, l, a) {
  let h = 0;
  for (let p = i; p < n; p++)
    h += Zi(s, t[p]);
  let f = Math.ceil(
    h * 1.5 / 8
    /* Balance.BranchFactor */
  ), c = [], u = [];
  function d(p, g, m, y, w) {
    for (let b = m; b < y; ) {
      let O = b, v = g[b], T = Zi(s, p[b]);
      for (b++; b < y; b++) {
        let A = Zi(s, p[b]);
        if (T + A >= f)
          break;
        T += A;
      }
      if (b == O + 1) {
        if (T > f) {
          let A = p[O];
          d(A.children, A.positions, 0, A.children.length, g[O] + w);
          continue;
        }
        c.push(p[O]);
      } else {
        let A = g[b - 1] + p[b - 1].length - v;
        c.push(pr(s, p, g, O, b, v, A, null, a));
      }
      u.push(v + w - r);
    }
  }
  return d(t, e, i, n, 0), (l || a)(c, u, o);
}
class be {
  /**
  Construct a tree fragment. You'll usually want to use
  [`addTree`](#common.TreeFragment^addTree) and
  [`applyChanges`](#common.TreeFragment^applyChanges) instead of
  calling this directly.
  */
  constructor(t, e, i, n, r = !1, o = !1) {
    this.from = t, this.to = e, this.tree = i, this.offset = n, this.open = (r ? 1 : 0) | (o ? 2 : 0);
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
    let n = [new be(0, t.length, t, 0, !1, i)];
    for (let r of e)
      r.to > t.length && n.push(r);
    return n;
  }
  /**
  Apply a set of edits to an array of fragments, removing or
  splitting fragments as necessary to remove edited ranges, and
  adjusting offsets for fragments that moved.
  */
  static applyChanges(t, e, i = 128) {
    if (!e.length)
      return t;
    let n = [], r = 1, o = t.length ? t[0] : null;
    for (let l = 0, a = 0, h = 0; ; l++) {
      let f = l < e.length ? e[l] : null, c = f ? f.fromA : 1e9;
      if (c - a >= i)
        for (; o && o.from < c; ) {
          let u = o;
          if (a >= u.from || c <= u.to || h) {
            let d = Math.max(u.from, a) - h, p = Math.min(u.to, c) - h;
            u = d >= p ? null : new be(d, p, u.tree, u.offset + h, l > 0, !!f);
          }
          if (u && n.push(u), o.to > c)
            break;
          o = r < t.length ? t[r++] : null;
        }
      if (!f)
        break;
      a = f.toA, h = f.toA - f.toB;
    }
    return n;
  }
}
class pa {
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
    return typeof t == "string" && (t = new Fc(t)), i = i ? i.length ? i.map((n) => new Gn(n.from, n.to)) : [new Gn(0, 0)] : [new Gn(0, t.length)], this.createParse(t, e || [], i);
  }
  /**
  Run a full parse, returning the resulting tree.
  */
  parse(t, e, i) {
    let n = this.startParse(t, e, i);
    for (; ; ) {
      let r = n.advance();
      if (r)
        return r;
    }
  }
}
class Fc {
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
new R({ perNode: !0 });
let zc = 0;
class yt {
  /**
  @internal
  */
  constructor(t, e, i, n) {
    this.name = t, this.set = e, this.base = i, this.modified = n, this.id = zc++;
  }
  toString() {
    let { name: t } = this;
    for (let e of this.modified)
      e.name && (t = `${e.name}(${t})`);
    return t;
  }
  static define(t, e) {
    let i = typeof t == "string" ? t : "?";
    if (t instanceof yt && (e = t), e != null && e.base)
      throw new Error("Can not derive from a modified tag");
    let n = new yt(i, [], null, []);
    if (n.set.push(n), e)
      for (let r of e.set)
        n.set.push(r);
    return n;
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
    let e = new dn(t);
    return (i) => i.modified.indexOf(e) > -1 ? i : dn.get(i.base || i, i.modified.concat(e).sort((n, r) => n.id - r.id));
  }
}
let Qc = 0;
class dn {
  constructor(t) {
    this.name = t, this.instances = [], this.id = Qc++;
  }
  static get(t, e) {
    if (!e.length)
      return t;
    let i = e[0].instances.find((l) => l.base == t && $c(e, l.modified));
    if (i)
      return i;
    let n = [], r = new yt(t.name, n, t, e);
    for (let l of e)
      l.instances.push(r);
    let o = jc(e);
    for (let l of t.set)
      if (!l.modified.length)
        for (let a of o)
          n.push(dn.get(l, a));
    return r;
  }
}
function $c(s, t) {
  return s.length == t.length && s.every((e, i) => e == t[i]);
}
function jc(s) {
  let t = [[]];
  for (let e = 0; e < s.length; e++)
    for (let i = 0, n = t.length; i < n; i++)
      t.push(t[i].concat(s[e]));
  return t.sort((e, i) => i.length - e.length);
}
function ga(s) {
  let t = /* @__PURE__ */ Object.create(null);
  for (let e in s) {
    let i = s[e];
    Array.isArray(i) || (i = [i]);
    for (let n of e.split(" "))
      if (n) {
        let r = [], o = 2, l = n;
        for (let c = 0; ; ) {
          if (l == "..." && c > 0 && c + 3 == n.length) {
            o = 1;
            break;
          }
          let u = /^"(?:[^"\\]|\\.)*?"|[^\/!]+/.exec(l);
          if (!u)
            throw new RangeError("Invalid path: " + n);
          if (r.push(u[0] == "*" ? "" : u[0][0] == '"' ? JSON.parse(u[0]) : u[0]), c += u[0].length, c == n.length)
            break;
          let d = n[c++];
          if (c == n.length && d == "!") {
            o = 0;
            break;
          }
          if (d != "/")
            throw new RangeError("Invalid path: " + n);
          l = n.slice(c);
        }
        let a = r.length - 1, h = r[a];
        if (!h)
          throw new RangeError("Invalid path: " + n);
        let f = new pn(i, o, a > 0 ? r.slice(0, a) : null);
        t[h] = f.sort(t[h]);
      }
  }
  return Xc.add(t);
}
const Xc = new R({
  combine(s, t) {
    let e, i, n;
    for (; s || t; ) {
      if (!s || t && s.depth >= t.depth ? (n = t, t = t.next) : (n = s, s = s.next), e && e.mode == n.mode && !n.context && !e.context)
        continue;
      let r = new pn(n.tags, n.mode, n.context);
      e ? e.next = r : i = r, e = r;
    }
    return i;
  }
});
class pn {
  constructor(t, e, i, n) {
    this.tags = t, this.mode = e, this.context = i, this.next = n;
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
pn.empty = new pn([], 2, null);
function qc(s, t) {
  let e = /* @__PURE__ */ Object.create(null);
  for (let r of s)
    if (!Array.isArray(r.tag))
      e[r.tag.id] = r.class;
    else
      for (let o of r.tag)
        e[o.id] = r.class;
  let { scope: i, all: n = null } = {};
  return {
    style: (r) => {
      let o = n;
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
const k = yt.define, Fi = k(), Jt = k(), xo = k(Jt), ko = k(Jt), te = k(), zi = k(te), Zn = k(te), It = k(), ce = k(It), Lt = k(), Et = k(), Hs = k(), Ke = k(Hs), Qi = k(), C = {
  /**
  A comment.
  */
  comment: Fi,
  /**
  A line [comment](#highlight.tags.comment).
  */
  lineComment: k(Fi),
  /**
  A block [comment](#highlight.tags.comment).
  */
  blockComment: k(Fi),
  /**
  A documentation [comment](#highlight.tags.comment).
  */
  docComment: k(Fi),
  /**
  Any kind of identifier.
  */
  name: Jt,
  /**
  The [name](#highlight.tags.name) of a variable.
  */
  variableName: k(Jt),
  /**
  A type [name](#highlight.tags.name).
  */
  typeName: xo,
  /**
  A tag name (subtag of [`typeName`](#highlight.tags.typeName)).
  */
  tagName: k(xo),
  /**
  A property or field [name](#highlight.tags.name).
  */
  propertyName: ko,
  /**
  An attribute name (subtag of [`propertyName`](#highlight.tags.propertyName)).
  */
  attributeName: k(ko),
  /**
  The [name](#highlight.tags.name) of a class.
  */
  className: k(Jt),
  /**
  A label [name](#highlight.tags.name).
  */
  labelName: k(Jt),
  /**
  A namespace [name](#highlight.tags.name).
  */
  namespace: k(Jt),
  /**
  The [name](#highlight.tags.name) of a macro.
  */
  macroName: k(Jt),
  /**
  A literal value.
  */
  literal: te,
  /**
  A string [literal](#highlight.tags.literal).
  */
  string: zi,
  /**
  A documentation [string](#highlight.tags.string).
  */
  docString: k(zi),
  /**
  A character literal (subtag of [string](#highlight.tags.string)).
  */
  character: k(zi),
  /**
  An attribute value (subtag of [string](#highlight.tags.string)).
  */
  attributeValue: k(zi),
  /**
  A number [literal](#highlight.tags.literal).
  */
  number: Zn,
  /**
  An integer [number](#highlight.tags.number) literal.
  */
  integer: k(Zn),
  /**
  A floating-point [number](#highlight.tags.number) literal.
  */
  float: k(Zn),
  /**
  A boolean [literal](#highlight.tags.literal).
  */
  bool: k(te),
  /**
  Regular expression [literal](#highlight.tags.literal).
  */
  regexp: k(te),
  /**
  An escape [literal](#highlight.tags.literal), for example a
  backslash escape in a string.
  */
  escape: k(te),
  /**
  A color [literal](#highlight.tags.literal).
  */
  color: k(te),
  /**
  A URL [literal](#highlight.tags.literal).
  */
  url: k(te),
  /**
  A language keyword.
  */
  keyword: Lt,
  /**
  The [keyword](#highlight.tags.keyword) for the self or this
  object.
  */
  self: k(Lt),
  /**
  The [keyword](#highlight.tags.keyword) for null.
  */
  null: k(Lt),
  /**
  A [keyword](#highlight.tags.keyword) denoting some atomic value.
  */
  atom: k(Lt),
  /**
  A [keyword](#highlight.tags.keyword) that represents a unit.
  */
  unit: k(Lt),
  /**
  A modifier [keyword](#highlight.tags.keyword).
  */
  modifier: k(Lt),
  /**
  A [keyword](#highlight.tags.keyword) that acts as an operator.
  */
  operatorKeyword: k(Lt),
  /**
  A control-flow related [keyword](#highlight.tags.keyword).
  */
  controlKeyword: k(Lt),
  /**
  A [keyword](#highlight.tags.keyword) that defines something.
  */
  definitionKeyword: k(Lt),
  /**
  A [keyword](#highlight.tags.keyword) related to defining or
  interfacing with modules.
  */
  moduleKeyword: k(Lt),
  /**
  An operator.
  */
  operator: Et,
  /**
  An [operator](#highlight.tags.operator) that dereferences something.
  */
  derefOperator: k(Et),
  /**
  Arithmetic-related [operator](#highlight.tags.operator).
  */
  arithmeticOperator: k(Et),
  /**
  Logical [operator](#highlight.tags.operator).
  */
  logicOperator: k(Et),
  /**
  Bit [operator](#highlight.tags.operator).
  */
  bitwiseOperator: k(Et),
  /**
  Comparison [operator](#highlight.tags.operator).
  */
  compareOperator: k(Et),
  /**
  [Operator](#highlight.tags.operator) that updates its operand.
  */
  updateOperator: k(Et),
  /**
  [Operator](#highlight.tags.operator) that defines something.
  */
  definitionOperator: k(Et),
  /**
  Type-related [operator](#highlight.tags.operator).
  */
  typeOperator: k(Et),
  /**
  Control-flow [operator](#highlight.tags.operator).
  */
  controlOperator: k(Et),
  /**
  Program or markup punctuation.
  */
  punctuation: Hs,
  /**
  [Punctuation](#highlight.tags.punctuation) that separates
  things.
  */
  separator: k(Hs),
  /**
  Bracket-style [punctuation](#highlight.tags.punctuation).
  */
  bracket: Ke,
  /**
  Angle [brackets](#highlight.tags.bracket) (usually `<` and `>`
  tokens).
  */
  angleBracket: k(Ke),
  /**
  Square [brackets](#highlight.tags.bracket) (usually `[` and `]`
  tokens).
  */
  squareBracket: k(Ke),
  /**
  Parentheses (usually `(` and `)` tokens). Subtag of
  [bracket](#highlight.tags.bracket).
  */
  paren: k(Ke),
  /**
  Braces (usually `{` and `}` tokens). Subtag of
  [bracket](#highlight.tags.bracket).
  */
  brace: k(Ke),
  /**
  Content, for example plain text in XML or markup documents.
  */
  content: It,
  /**
  [Content](#highlight.tags.content) that represents a heading.
  */
  heading: ce,
  /**
  A level 1 [heading](#highlight.tags.heading).
  */
  heading1: k(ce),
  /**
  A level 2 [heading](#highlight.tags.heading).
  */
  heading2: k(ce),
  /**
  A level 3 [heading](#highlight.tags.heading).
  */
  heading3: k(ce),
  /**
  A level 4 [heading](#highlight.tags.heading).
  */
  heading4: k(ce),
  /**
  A level 5 [heading](#highlight.tags.heading).
  */
  heading5: k(ce),
  /**
  A level 6 [heading](#highlight.tags.heading).
  */
  heading6: k(ce),
  /**
  A prose [content](#highlight.tags.content) separator (such as a horizontal rule).
  */
  contentSeparator: k(It),
  /**
  [Content](#highlight.tags.content) that represents a list.
  */
  list: k(It),
  /**
  [Content](#highlight.tags.content) that represents a quote.
  */
  quote: k(It),
  /**
  [Content](#highlight.tags.content) that is emphasized.
  */
  emphasis: k(It),
  /**
  [Content](#highlight.tags.content) that is styled strong.
  */
  strong: k(It),
  /**
  [Content](#highlight.tags.content) that is part of a link.
  */
  link: k(It),
  /**
  [Content](#highlight.tags.content) that is styled as code or
  monospace.
  */
  monospace: k(It),
  /**
  [Content](#highlight.tags.content) that has a strike-through
  style.
  */
  strikethrough: k(It),
  /**
  Inserted text in a change-tracking format.
  */
  inserted: k(),
  /**
  Deleted text.
  */
  deleted: k(),
  /**
  Changed text.
  */
  changed: k(),
  /**
  An invalid or unsyntactic element.
  */
  invalid: k(),
  /**
  Metadata or meta-instruction.
  */
  meta: Qi,
  /**
  [Metadata](#highlight.tags.meta) that applies to the entire
  document.
  */
  documentMeta: k(Qi),
  /**
  [Metadata](#highlight.tags.meta) that annotates or adds
  attributes to a given syntactic element.
  */
  annotation: k(Qi),
  /**
  Processing instruction or preprocessor directive. Subtag of
  [meta](#highlight.tags.meta).
  */
  processingInstruction: k(Qi),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates that a
  given element is being defined. Expected to be used with the
  various [name](#highlight.tags.name) tags.
  */
  definition: yt.defineModifier("definition"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates that
  something is constant. Mostly expected to be used with
  [variable names](#highlight.tags.variableName).
  */
  constant: yt.defineModifier("constant"),
  /**
  [Modifier](#highlight.Tag^defineModifier) used to indicate that
  a [variable](#highlight.tags.variableName) or [property
  name](#highlight.tags.propertyName) is being called or defined
  as a function.
  */
  function: yt.defineModifier("function"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that can be applied to
  [names](#highlight.tags.name) to indicate that they belong to
  the language's standard environment.
  */
  standard: yt.defineModifier("standard"),
  /**
  [Modifier](#highlight.Tag^defineModifier) that indicates a given
  [names](#highlight.tags.name) is local to some scope.
  */
  local: yt.defineModifier("local"),
  /**
  A generic variant [modifier](#highlight.Tag^defineModifier) that
  can be used to tag language-specific alternative variants of
  some common tag. It is recommended for themes to define special
  forms of at least the [string](#highlight.tags.string) and
  [variable name](#highlight.tags.variableName) tags, since those
  come up a lot.
  */
  special: yt.defineModifier("special")
};
for (let s in C) {
  let t = C[s];
  t instanceof yt && (t.name = s);
}
qc([
  { tag: C.link, class: "tok-link" },
  { tag: C.heading, class: "tok-heading" },
  { tag: C.emphasis, class: "tok-emphasis" },
  { tag: C.strong, class: "tok-strong" },
  { tag: C.keyword, class: "tok-keyword" },
  { tag: C.atom, class: "tok-atom" },
  { tag: C.bool, class: "tok-bool" },
  { tag: C.url, class: "tok-url" },
  { tag: C.labelName, class: "tok-labelName" },
  { tag: C.inserted, class: "tok-inserted" },
  { tag: C.deleted, class: "tok-deleted" },
  { tag: C.literal, class: "tok-literal" },
  { tag: C.string, class: "tok-string" },
  { tag: C.number, class: "tok-number" },
  { tag: [C.regexp, C.escape, C.special(C.string)], class: "tok-string2" },
  { tag: C.variableName, class: "tok-variableName" },
  { tag: C.local(C.variableName), class: "tok-variableName tok-local" },
  { tag: C.definition(C.variableName), class: "tok-variableName tok-definition" },
  { tag: C.special(C.variableName), class: "tok-variableName2" },
  { tag: C.definition(C.propertyName), class: "tok-propertyName tok-definition" },
  { tag: C.typeName, class: "tok-typeName" },
  { tag: C.namespace, class: "tok-namespace" },
  { tag: C.className, class: "tok-className" },
  { tag: C.macroName, class: "tok-macroName" },
  { tag: C.propertyName, class: "tok-propertyName" },
  { tag: C.operator, class: "tok-operator" },
  { tag: C.comment, class: "tok-comment" },
  { tag: C.meta, class: "tok-meta" },
  { tag: C.invalid, class: "tok-invalid" },
  { tag: C.punctuation, class: "tok-punctuation" }
]);
var Jn;
const ti = /* @__PURE__ */ new R();
function Uc(s) {
  return P.define({
    combine: s ? (t) => t.concat(s) : void 0
  });
}
const Kc = /* @__PURE__ */ new R();
class Ft {
  /**
  Construct a language object. If you need to invoke this
  directly, first define a data facet with
  [`defineLanguageFacet`](https://codemirror.net/6/docs/ref/#language.defineLanguageFacet), and then
  configure your parser to [attach](https://codemirror.net/6/docs/ref/#language.languageDataProp) it
  to the language's outer syntax node.
  */
  constructor(t, e, i = [], n = "") {
    this.data = t, this.name = n, N.prototype.hasOwnProperty("tree") || Object.defineProperty(N.prototype, "tree", { get() {
      return Yt(this);
    } }), this.parser = e, this.extension = [
      Qe.of(this),
      N.languageData.of((r, o, l) => {
        let a = So(r, o, l), h = a.type.prop(ti);
        if (!h)
          return [];
        let f = r.facet(h), c = a.type.prop(Kc);
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
    return So(t, e, i).type.prop(ti) == this.data;
  }
  /**
  Find the document regions that were parsed using this language.
  The returned regions will _include_ any nested languages rooted
  in this language, when those exist.
  */
  findRegions(t) {
    let e = t.facet(Qe);
    if ((e == null ? void 0 : e.data) == this.data)
      return [{ from: 0, to: t.doc.length }];
    if (!e || !e.allowsNesting)
      return [];
    let i = [], n = (r, o) => {
      if (r.prop(ti) == this.data) {
        i.push({ from: o, to: o + r.length });
        return;
      }
      let l = r.prop(R.mounted);
      if (l) {
        if (l.tree.prop(ti) == this.data) {
          if (l.overlay)
            for (let a of l.overlay)
              i.push({ from: a.from + o, to: a.to + o });
          else
            i.push({ from: o, to: o + r.length });
          return;
        } else if (l.overlay) {
          let a = i.length;
          if (n(l.tree, l.overlay[0].from + o), i.length > a)
            return;
        }
      }
      for (let a = 0; a < r.children.length; a++) {
        let h = r.children[a];
        h instanceof _ && n(h, r.positions[a] + o);
      }
    };
    return n(Yt(t), 0), i;
  }
  /**
  Indicates whether this language allows nested languages. The
  default implementation returns true.
  */
  get allowsNesting() {
    return !0;
  }
}
Ft.setState = /* @__PURE__ */ I.define();
function So(s, t, e) {
  let i = s.facet(Qe), n = Yt(s).topNode;
  if (!i || i.allowsNesting)
    for (let r = n; r; r = r.enter(t, e, X.ExcludeBuffers | X.EnterBracketed))
      r.type.isTop && (n = r);
  return n;
}
class gn extends Ft {
  constructor(t, e, i) {
    super(t, e, [], i), this.parser = e;
  }
  /**
  Define a language from a parser.
  */
  static define(t) {
    let e = Uc(t.languageData);
    return new gn(e, t.parser.configure({
      props: [ti.add((i) => i.isTop ? e : void 0)]
    }), t.name);
  }
  /**
  Create a new instance of this language with a reconfigured
  version of its parser and optionally a new name.
  */
  configure(t, e) {
    return new gn(this.data, this.parser.configure(t), e || this.name);
  }
  get allowsNesting() {
    return this.parser.hasWrappers();
  }
}
function Yt(s) {
  let t = s.field(Ft.state, !1);
  return t ? t.tree : _.empty;
}
class _c {
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
let _e = null;
class mn {
  constructor(t, e, i = [], n, r, o, l, a) {
    this.parser = t, this.state = e, this.fragments = i, this.tree = n, this.treeLen = r, this.viewport = o, this.skipped = l, this.scheduleOn = a, this.parse = null, this.tempSkipped = [];
  }
  /**
  @internal
  */
  static create(t, e, i) {
    return new mn(t, e, [], _.empty, 0, i, [], null);
  }
  startParse() {
    return this.parser.startParse(new _c(this.state.doc), this.fragments);
  }
  /**
  @internal
  */
  work(t, e) {
    return e != null && e >= this.state.doc.length && (e = void 0), this.tree != _.empty && this.isDone(e ?? this.state.doc.length) ? (this.takeTree(), !0) : this.withContext(() => {
      var i;
      if (typeof t == "number") {
        let n = Date.now() + t;
        t = () => Date.now() > n;
      }
      for (this.parse || (this.parse = this.startParse()), e != null && (this.parse.stoppedAt == null || this.parse.stoppedAt > e) && e < this.state.doc.length && this.parse.stopAt(e); ; ) {
        let n = this.parse.advance();
        if (n)
          if (this.fragments = this.withoutTempSkipped(be.addTree(n, this.fragments, this.parse.stoppedAt != null)), this.treeLen = (i = this.parse.stoppedAt) !== null && i !== void 0 ? i : this.state.doc.length, this.tree = n, this.parse = null, this.treeLen < (e ?? this.state.doc.length))
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
    }), this.treeLen = t, this.tree = e, this.fragments = this.withoutTempSkipped(be.addTree(this.tree, this.fragments, !0)), this.parse = null);
  }
  withContext(t) {
    let e = _e;
    _e = this;
    try {
      return t();
    } finally {
      _e = e;
    }
  }
  withoutTempSkipped(t) {
    for (let e; e = this.tempSkipped.pop(); )
      t = vo(t, e.from, e.to);
    return t;
  }
  /**
  @internal
  */
  changes(t, e) {
    let { fragments: i, tree: n, treeLen: r, viewport: o, skipped: l } = this;
    if (this.takeTree(), !t.empty) {
      let a = [];
      if (t.iterChangedRanges((h, f, c, u) => a.push({ fromA: h, toA: f, fromB: c, toB: u })), i = be.applyChanges(i, a), n = _.empty, r = 0, o = { from: t.mapPos(o.from, -1), to: t.mapPos(o.to, 1) }, this.skipped.length) {
        l = [];
        for (let h of this.skipped) {
          let f = t.mapPos(h.from, 1), c = t.mapPos(h.to, -1);
          f < c && l.push({ from: f, to: c });
        }
      }
    }
    return new mn(this.parser, e, i, n, r, o, l, this.scheduleOn);
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
      let { from: n, to: r } = this.skipped[i];
      n < t.to && r > t.from && (this.fragments = vo(this.fragments, n, r), this.skipped.splice(i--, 1));
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
    return new class extends pa {
      createParse(e, i, n) {
        let r = n[0].from, o = n[n.length - 1].to;
        return {
          parsedPos: r,
          advance() {
            let a = _e;
            if (a) {
              for (let h of n)
                a.tempSkipped.push(h);
              t && (a.scheduleOn = a.scheduleOn ? Promise.all([a.scheduleOn, t]) : t);
            }
            return this.parsedPos = o, new _(dt.none, [], [], o - r);
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
    return _e;
  }
}
function vo(s, t, e) {
  return be.applyChanges(s, [{ fromA: t, toA: e, fromB: t, toB: e }]);
}
class ze {
  constructor(t) {
    this.context = t, this.tree = t.tree;
  }
  apply(t) {
    if (!t.docChanged && this.tree == this.context.tree)
      return this;
    let e = this.context.changes(t.changes, t.state), i = this.context.treeLen == t.startState.doc.length ? void 0 : Math.max(t.changes.mapPos(this.context.treeLen), e.viewport.to);
    return e.work(20, i) || e.takeTree(), new ze(e);
  }
  static init(t) {
    let e = Math.min(3e3, t.doc.length), i = mn.create(t.facet(Qe).parser, t, { from: 0, to: e });
    return i.work(20, e) || i.takeTree(), new ze(i);
  }
}
Ft.state = /* @__PURE__ */ Pt.define({
  create: ze.init,
  update(s, t) {
    for (let e of t.effects)
      if (e.is(Ft.setState))
        return e.value;
    return t.startState.facet(Qe) != t.state.facet(Qe) ? ze.init(t.state) : s.apply(t);
  }
});
let ma = (s) => {
  let t = setTimeout(
    () => s(),
    500
    /* Work.MaxPause */
  );
  return () => clearTimeout(t);
};
typeof requestIdleCallback < "u" && (ma = (s) => {
  let t = -1, e = setTimeout(
    () => {
      t = requestIdleCallback(s, {
        timeout: 400
        /* Work.MinPause */
      });
    },
    100
    /* Work.MinPause */
  );
  return () => t < 0 ? clearTimeout(e) : cancelIdleCallback(t);
});
const ts = typeof navigator < "u" && (!((Jn = navigator.scheduling) === null || Jn === void 0) && Jn.isInputPending) ? () => navigator.scheduling.isInputPending() : null, Yc = /* @__PURE__ */ $t.fromClass(class {
  constructor(t) {
    this.view = t, this.working = null, this.workScheduled = 0, this.chunkEnd = -1, this.chunkBudget = -1, this.work = this.work.bind(this), this.scheduleWork();
  }
  update(t) {
    let e = this.view.state.field(Ft.state).context;
    (e.updateViewport(t.view.viewport) || this.view.viewport.to > e.treeLen) && this.scheduleWork(), (t.docChanged || t.selectionSet) && (this.view.hasFocus && (this.chunkBudget += 50), this.scheduleWork()), this.checkAsyncSchedule(e);
  }
  scheduleWork() {
    if (this.working)
      return;
    let { state: t } = this.view, e = t.field(Ft.state);
    (e.tree != e.context.tree || !e.context.isDone(t.doc.length)) && (this.working = ma(this.work));
  }
  work(t) {
    this.working = null;
    let e = Date.now();
    if (this.chunkEnd < e && (this.chunkEnd < 0 || this.view.hasFocus) && (this.chunkEnd = e + 3e4, this.chunkBudget = 3e3), this.chunkBudget <= 0)
      return;
    let { state: i, viewport: { to: n } } = this.view, r = i.field(Ft.state);
    if (r.tree == r.context.tree && r.context.isDone(
      n + 1e5
      /* Work.MaxParseAhead */
    ))
      return;
    let o = Date.now() + Math.min(this.chunkBudget, 100, t && !ts ? Math.max(25, t.timeRemaining() - 5) : 1e9), l = r.context.treeLen < n && i.doc.length > n + 1e3, a = r.context.work(() => ts && ts() || Date.now() > o, n + (l ? 0 : 1e5));
    this.chunkBudget -= Date.now() - e, (a || this.chunkBudget <= 0) && (r.context.takeTree(), this.view.dispatch({ effects: Ft.setState.of(new ze(r.context)) })), this.chunkBudget > 0 && !(a && !l) && this.scheduleWork(), this.checkAsyncSchedule(r.context);
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
}), Qe = /* @__PURE__ */ P.define({
  combine(s) {
    return s.length ? s[0] : null;
  },
  enables: (s) => [
    Ft.state,
    Yc,
    M.contentAttributes.compute([s], (t) => {
      let e = t.facet(s);
      return e && e.name ? { "data-language": e.name } : {};
    })
  ]
});
class Gc {
  /**
  Create a language support object.
  */
  constructor(t, e = []) {
    this.language = t, this.support = e, this.extension = [t, e];
  }
}
const Zc = /* @__PURE__ */ P.define(), gr = /* @__PURE__ */ P.define({
  combine: (s) => {
    if (!s.length)
      return "  ";
    let t = s[0];
    if (!t || /\S/.test(t) || Array.from(t).some((e) => e != t[0]))
      throw new Error("Invalid indent unit: " + JSON.stringify(s[0]));
    return t;
  }
});
function yn(s) {
  let t = s.facet(gr);
  return t.charCodeAt(0) == 9 ? s.tabSize * t.length : t.length;
}
function bn(s, t) {
  let e = "", i = s.tabSize, n = s.facet(gr)[0];
  if (n == "	") {
    for (; t >= i; )
      e += "	", t -= i;
    n = " ";
  }
  for (let r = 0; r < t; r++)
    e += n;
  return e;
}
function ya(s, t) {
  s instanceof N && (s = new En(s));
  for (let i of s.state.facet(Zc)) {
    let n = i(s, t);
    if (n !== void 0)
      return n;
  }
  let e = Yt(s.state);
  return e.length >= t ? tu(s, e, t) : null;
}
class En {
  /**
  Create an indent context.
  */
  constructor(t, e = {}) {
    this.state = t, this.options = e, this.unit = yn(t);
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
    let i = this.state.doc.lineAt(t), { simulateBreak: n, simulateDoubleBreak: r } = this.options;
    return n != null && n >= i.from && n <= i.to ? r && n == t ? { text: "", from: t } : (e < 0 ? n < t : n <= t) ? { text: i.text.slice(n - i.from), from: n } : { text: i.text.slice(0, n - i.from), from: i.from } : i;
  }
  /**
  Get the text directly after `pos`, either the entire line
  or the next 100 characters, whichever is shorter.
  */
  textAfterPos(t, e = 1) {
    if (this.options.simulateDoubleBreak && t == this.options.simulateBreak)
      return "";
    let { text: i, from: n } = this.lineAt(t, e);
    return i.slice(t - n, Math.min(i.length, t + 100 - n));
  }
  /**
  Find the column for the given position.
  */
  column(t, e = 1) {
    let { text: i, from: n } = this.lineAt(t, e), r = this.countColumn(i, t - n), o = this.options.overrideIndentation ? this.options.overrideIndentation(n) : -1;
    return o > -1 && (r += o - this.countColumn(i, i.search(/\S|$/))), r;
  }
  /**
  Find the column position (taking tabs into account) of the given
  position in the given string.
  */
  countColumn(t, e = t.length) {
    return Tn(t, this.state.tabSize, e);
  }
  /**
  Find the indentation column of the line at the given point.
  */
  lineIndent(t, e = 1) {
    let { text: i, from: n } = this.lineAt(t, e), r = this.options.overrideIndentation;
    if (r) {
      let o = r(n);
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
const Jc = /* @__PURE__ */ new R();
function tu(s, t, e) {
  let i = t.resolveStack(e), n = t.resolveInner(e, -1).resolve(e, 0).enterUnfinishedNodesBefore(e);
  if (n != i.node) {
    let r = [];
    for (let o = n; o && !(o.from < i.node.from || o.to > i.node.to || o.from == i.node.from && o.type == i.node.type); o = o.parent)
      r.push(o);
    for (let o = r.length - 1; o >= 0; o--)
      i = { node: r[o], next: i };
  }
  return ba(i, s, e);
}
function ba(s, t, e) {
  for (let i = s; i; i = i.next) {
    let n = iu(i.node);
    if (n)
      return n(mr.create(t, e, i));
  }
  return 0;
}
function eu(s) {
  return s.pos == s.options.simulateBreak && s.options.simulateDoubleBreak;
}
function iu(s) {
  let t = s.type.prop(Jc);
  if (t)
    return t;
  let e = s.firstChild, i;
  if (e && (i = e.type.prop(R.closedBy))) {
    let n = s.lastChild, r = n && i.indexOf(n.name) > -1;
    return (o) => ou(o, !0, 1, void 0, r && !eu(o) ? n.from : void 0);
  }
  return s.parent == null ? nu : null;
}
function nu() {
  return 0;
}
class mr extends En {
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
    return new mr(t, e, i);
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
      if (su(i, t))
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
    return ba(this.context.next, this.base, this.pos);
  }
}
function su(s, t) {
  for (let e = t; e; e = e.parent)
    if (s == e)
      return !0;
  return !1;
}
function ru(s) {
  let t = s.node, e = t.childAfter(t.from), i = t.lastChild;
  if (!e)
    return null;
  let n = s.options.simulateBreak, r = s.state.doc.lineAt(e.from), o = n == null || n <= r.from ? r.to : Math.min(r.to, n);
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
function ou(s, t, e, i, n) {
  let r = s.textAfter, o = r.match(/^\s*/)[0].length, l = i && r.slice(o, o + i.length) == i || n == s.pos + o, a = ru(s);
  return a ? l ? s.column(a.from) : s.column(a.to) : s.baseIndent + (l ? 0 : s.unit * e);
}
const lu = 1e4, au = "()[]{}", hu = /* @__PURE__ */ new R();
function Fs(s, t, e) {
  let i = s.prop(t < 0 ? R.openedBy : R.closedBy);
  if (i)
    return i;
  if (s.name.length == 1) {
    let n = e.indexOf(s.name);
    if (n > -1 && n % 2 == (t < 0 ? 1 : 0))
      return [e[n + t]];
  }
  return null;
}
function zs(s) {
  let t = s.type.prop(hu);
  return t ? t(s.node) : s;
}
function Te(s, t, e, i = {}) {
  let n = i.maxScanDistance || lu, r = i.brackets || au, o = Yt(s), l = o.resolveInner(t, e);
  for (let a = l; a; a = a.parent) {
    let h = Fs(a.type, e, r);
    if (h && a.from < a.to) {
      let f = zs(a);
      if (f && (e > 0 ? t >= f.from && t < f.to : t > f.from && t <= f.to))
        return fu(s, t, e, a, f, h, r);
    }
  }
  return cu(s, t, e, o, l.type, n, r);
}
function fu(s, t, e, i, n, r, o) {
  let l = i.parent, a = { from: n.from, to: n.to }, h = 0, f = l == null ? void 0 : l.cursor();
  if (f && (e < 0 ? f.childBefore(i.from) : f.childAfter(i.to)))
    do
      if (e < 0 ? f.to <= i.from : f.from >= i.to) {
        if (h == 0 && r.indexOf(f.type.name) > -1 && f.from < f.to) {
          let c = zs(f);
          return { start: a, end: c ? { from: c.from, to: c.to } : void 0, matched: !0 };
        } else if (Fs(f.type, e, o))
          h++;
        else if (Fs(f.type, -e, o)) {
          if (h == 0) {
            let c = zs(f);
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
function cu(s, t, e, i, n, r, o) {
  let l = e < 0 ? s.sliceDoc(t - 1, t) : s.sliceDoc(t, t + 1), a = o.indexOf(l);
  if (a < 0 || a % 2 == 0 != e > 0)
    return null;
  let h = { from: e < 0 ? t - 1 : t, to: e > 0 ? t + 1 : t }, f = s.doc.iterRange(t, e > 0 ? s.doc.length : 0), c = 0;
  for (let u = 0; !f.next().done && u <= r; ) {
    let d = f.value;
    e < 0 && (u += d.length);
    let p = t + u * e;
    for (let g = e > 0 ? 0 : d.length - 1, m = e > 0 ? d.length : -1; g != m; g += e) {
      let y = o.indexOf(d[g]);
      if (!(y < 0 || i.resolveInner(p + g, 1).type != n))
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
const uu = /* @__PURE__ */ Object.create(null), Oo = [dt.none], Co = [], Ao = /* @__PURE__ */ Object.create(null), du = /* @__PURE__ */ Object.create(null);
for (let [s, t] of [
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
  du[s] = /* @__PURE__ */ pu(uu, t);
function es(s, t) {
  Co.indexOf(s) > -1 || (Co.push(s), console.warn(t));
}
function pu(s, t) {
  let e = [];
  for (let l of t.split(" ")) {
    let a = [];
    for (let h of l.split(".")) {
      let f = s[h] || C[h];
      f ? typeof f == "function" ? a.length ? a = a.map(f) : es(h, `Modifier ${h} used at start of tag`) : a.length ? es(h, `Tag ${h} used as modifier`) : a = Array.isArray(f) ? f : [f] : es(h, `Unknown highlighting tag ${h}`);
    }
    for (let h of a)
      e.push(h);
  }
  if (!e.length)
    return 0;
  let i = t.replace(/ /g, "_"), n = i + " " + e.map((l) => l.id), r = Ao[n];
  if (r)
    return r.id;
  let o = Ao[n] = dt.define({
    id: Oo.length,
    name: i,
    props: [ga({ [i]: e })]
  });
  return Oo.push(o), o.id;
}
$.RTL, $.LTR;
const gu = (s) => {
  let { state: t } = s, e = t.doc.lineAt(t.selection.main.from), i = br(s.state, e.from);
  return i.line ? mu(s) : i.block ? bu(s) : !1;
};
function yr(s, t) {
  return ({ state: e, dispatch: i }) => {
    if (e.readOnly)
      return !1;
    let n = s(t, e);
    return n ? (i(e.update(n)), !0) : !1;
  };
}
const mu = /* @__PURE__ */ yr(
  ku,
  0
  /* CommentOption.Toggle */
), yu = /* @__PURE__ */ yr(
  wa,
  0
  /* CommentOption.Toggle */
), bu = /* @__PURE__ */ yr(
  (s, t) => wa(s, t, xu(t)),
  0
  /* CommentOption.Toggle */
);
function br(s, t) {
  let e = s.languageDataAt("commentTokens", t, 1);
  return e.length ? e[0] : {};
}
const Ye = 50;
function wu(s, { open: t, close: e }, i, n) {
  let r = s.sliceDoc(i - Ye, i), o = s.sliceDoc(n, n + Ye), l = /\s*$/.exec(r)[0].length, a = /^\s*/.exec(o)[0].length, h = r.length - l;
  if (r.slice(h - t.length, h) == t && o.slice(a, a + e.length) == e)
    return {
      open: { pos: i - l, margin: l && 1 },
      close: { pos: n + a, margin: a && 1 }
    };
  let f, c;
  n - i <= 2 * Ye ? f = c = s.sliceDoc(i, n) : (f = s.sliceDoc(i, i + Ye), c = s.sliceDoc(n - Ye, n));
  let u = /^\s*/.exec(f)[0].length, d = /\s*$/.exec(c)[0].length, p = c.length - d - e.length;
  return f.slice(u, u + t.length) == t && c.slice(p, p + e.length) == e ? {
    open: {
      pos: i + u + t.length,
      margin: /\s/.test(f.charAt(u + t.length)) ? 1 : 0
    },
    close: {
      pos: n - d - e.length,
      margin: /\s/.test(c.charAt(p - 1)) ? 1 : 0
    }
  } : null;
}
function xu(s) {
  let t = [];
  for (let e of s.selection.ranges) {
    let i = s.doc.lineAt(e.from), n = e.to <= i.to ? i : s.doc.lineAt(e.to);
    n.from > i.from && n.from == e.to && (n = e.to == i.to + 1 ? i : s.doc.lineAt(e.to - 1));
    let r = t.length - 1;
    r >= 0 && t[r].to > i.from ? t[r].to = n.to : t.push({ from: i.from + /^\s*/.exec(i.text)[0].length, to: n.to });
  }
  return t;
}
function wa(s, t, e = t.selection.ranges) {
  let i = e.map((r) => br(t, r.from).block);
  if (!i.every((r) => r))
    return null;
  let n = e.map((r, o) => wu(t, i[o], r.from, r.to));
  if (s != 2 && !n.every((r) => r))
    return { changes: t.changes(e.map((r, o) => n[o] ? [] : [{ from: r.from, insert: i[o].open + " " }, { from: r.to, insert: " " + i[o].close }])) };
  if (s != 1 && n.some((r) => r)) {
    let r = [];
    for (let o = 0, l; o < n.length; o++)
      if (l = n[o]) {
        let a = i[o], { open: h, close: f } = l;
        r.push({ from: h.pos - a.open.length, to: h.pos + h.margin }, { from: f.pos - f.margin, to: f.pos + a.close.length });
      }
    return { changes: r };
  }
  return null;
}
function ku(s, t, e = t.selection.ranges) {
  let i = [], n = -1;
  t: for (let { from: r, to: o } of e) {
    let l = i.length, a = 1e9, h;
    for (let f = r; f <= o; ) {
      let c = t.doc.lineAt(f);
      if (h == null && (h = br(t, c.from).line, !h))
        continue t;
      if (c.from > n && (r == o || o > c.from)) {
        n = c.from;
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
  if (s != 2 && i.some((r) => r.comment < 0 && (!r.empty || r.single))) {
    let r = [];
    for (let { line: l, token: a, indent: h, empty: f, single: c } of i)
      (c || !f) && r.push({ from: l.from + h, insert: a + " " });
    let o = t.changes(r);
    return { changes: o, selection: t.selection.map(o, 1) };
  } else if (s != 1 && i.some((r) => r.comment >= 0)) {
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
const Qs = /* @__PURE__ */ Gt.define(), Su = /* @__PURE__ */ Gt.define(), vu = /* @__PURE__ */ P.define(), xa = /* @__PURE__ */ P.define({
  combine(s) {
    return Ks(s, {
      minDepth: 100,
      newGroupDelay: 500,
      joinToEvent: (t, e) => e
    }, {
      minDepth: Math.max,
      newGroupDelay: Math.min,
      joinToEvent: (t, e) => (i, n) => t(i, n) || e(i, n)
    });
  }
}), ka = /* @__PURE__ */ Pt.define({
  create() {
    return zt.empty;
  },
  update(s, t) {
    let e = t.state.facet(xa), i = t.annotation(Qs);
    if (i) {
      let a = ct.fromTransaction(t, i.selection), h = i.side, f = h == 0 ? s.undone : s.done;
      return a ? f = wn(f, f.length, e.minDepth, a) : f = Oa(f, t.startState.selection), new zt(h == 0 ? i.rest : f, h == 0 ? f : i.rest);
    }
    let n = t.annotation(Su);
    if ((n == "full" || n == "before") && (s = s.isolate()), t.annotation(G.addToHistory) === !1)
      return t.changes.empty ? s : s.addMapping(t.changes.desc);
    let r = ct.fromTransaction(t), o = t.annotation(G.time), l = t.annotation(G.userEvent);
    return r ? s = s.addChanges(r, o, l, e, t) : t.selection && (s = s.addSelection(t.startState.selection, o, l, e.newGroupDelay)), (n == "full" || n == "after") && (s = s.isolate()), s;
  },
  toJSON(s) {
    return { done: s.done.map((t) => t.toJSON()), undone: s.undone.map((t) => t.toJSON()) };
  },
  fromJSON(s) {
    return new zt(s.done.map(ct.fromJSON), s.undone.map(ct.fromJSON));
  }
});
function Ou(s = {}) {
  return [
    ka,
    xa.of(s),
    M.domEventHandlers({
      beforeinput(t, e) {
        let i = t.inputType == "historyUndo" ? Sa : t.inputType == "historyRedo" ? $s : null;
        return i ? (t.preventDefault(), i(e)) : !1;
      }
    })
  ];
}
function In(s, t) {
  return function({ state: e, dispatch: i }) {
    if (!t && e.readOnly)
      return !1;
    let n = e.field(ka, !1);
    if (!n)
      return !1;
    let r = n.pop(s, e, t);
    return r ? (i(r), !0) : !1;
  };
}
const Sa = /* @__PURE__ */ In(0, !1), $s = /* @__PURE__ */ In(1, !1), Cu = /* @__PURE__ */ In(0, !0), Au = /* @__PURE__ */ In(1, !0);
class ct {
  constructor(t, e, i, n, r) {
    this.changes = t, this.effects = e, this.mapped = i, this.startSelection = n, this.selectionsAfter = r;
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
      selectionsAfter: this.selectionsAfter.map((n) => n.toJSON())
    };
  }
  static fromJSON(t) {
    return new ct(t.changes && Y.fromJSON(t.changes), [], t.mapped && Qt.fromJSON(t.mapped), t.startSelection && x.fromJSON(t.startSelection), t.selectionsAfter.map(x.fromJSON));
  }
  // This does not check `addToHistory` and such, it assumes the
  // transaction needs to be converted to an item. Returns null when
  // there are no changes or effects in the transaction.
  static fromTransaction(t, e) {
    let i = wt;
    for (let n of t.startState.facet(vu)) {
      let r = n(t);
      r.length && (i = i.concat(r));
    }
    return !i.length && t.changes.empty ? null : new ct(t.changes.invert(t.startState.doc), i, void 0, e || t.startState.selection, wt);
  }
  static selection(t) {
    return new ct(void 0, wt, void 0, void 0, t);
  }
}
function wn(s, t, e, i) {
  let n = t + 1 > e + 20 ? t - e - 1 : 0, r = s.slice(n, t);
  return r.push(i), r;
}
function Tu(s, t) {
  let e = [], i = !1;
  return s.iterChangedRanges((n, r) => e.push(n, r)), t.iterChangedRanges((n, r, o, l) => {
    for (let a = 0; a < e.length; ) {
      let h = e[a++], f = e[a++];
      l >= h && o <= f && (i = !0);
    }
  }), i;
}
function Pu(s, t) {
  return s.ranges.length == t.ranges.length && s.ranges.filter((e, i) => e.empty != t.ranges[i].empty).length === 0;
}
function va(s, t) {
  return s.length ? t.length ? s.concat(t) : s : t;
}
const wt = [], Mu = 200;
function Oa(s, t) {
  if (s.length) {
    let e = s[s.length - 1], i = e.selectionsAfter.slice(Math.max(0, e.selectionsAfter.length - Mu));
    return i.length && i[i.length - 1].eq(t) ? s : (i.push(t), wn(s, s.length - 1, 1e9, e.setSelAfter(i)));
  } else
    return [ct.selection([t])];
}
function Du(s) {
  let t = s[s.length - 1], e = s.slice();
  return e[s.length - 1] = t.setSelAfter(t.selectionsAfter.slice(0, t.selectionsAfter.length - 1)), e;
}
function is(s, t) {
  if (!s.length)
    return s;
  let e = s.length, i = wt;
  for (; e; ) {
    let n = Ru(s[e - 1], t, i);
    if (n.changes && !n.changes.empty || n.effects.length) {
      let r = s.slice(0, e);
      return r[e - 1] = n, r;
    } else
      t = n.mapped, e--, i = n.selectionsAfter;
  }
  return i.length ? [ct.selection(i)] : wt;
}
function Ru(s, t, e) {
  let i = va(s.selectionsAfter.length ? s.selectionsAfter.map((l) => l.map(t)) : wt, e);
  if (!s.changes)
    return ct.selection(i);
  let n = s.changes.map(t), r = t.mapDesc(s.changes, !0), o = s.mapped ? s.mapped.composeDesc(r) : r;
  return new ct(n, I.mapEffects(s.effects, t), o, s.startSelection.map(r), i);
}
const Bu = /^(input\.type|delete)($|\.)/;
class zt {
  constructor(t, e, i = 0, n = void 0) {
    this.done = t, this.undone = e, this.prevTime = i, this.prevUserEvent = n;
  }
  isolate() {
    return this.prevTime ? new zt(this.done, this.undone) : this;
  }
  addChanges(t, e, i, n, r) {
    let o = this.done, l = o[o.length - 1];
    return l && l.changes && !l.changes.empty && t.changes && (!i || Bu.test(i)) && (!l.selectionsAfter.length && e - this.prevTime < n.newGroupDelay && n.joinToEvent(r, Tu(l.changes, t.changes)) || // For compose (but not compose.start) events, always join with previous event
    i == "input.type.compose") ? o = wn(o, o.length - 1, n.minDepth, new ct(t.changes.compose(l.changes), va(I.mapEffects(t.effects, l.changes), l.effects), l.mapped, l.startSelection, wt)) : o = wn(o, o.length, n.minDepth, t), new zt(o, wt, e, i);
  }
  addSelection(t, e, i, n) {
    let r = this.done.length ? this.done[this.done.length - 1].selectionsAfter : wt;
    return r.length > 0 && e - this.prevTime < n && i == this.prevUserEvent && i && /^select($|\.)/.test(i) && Pu(r[r.length - 1], t) ? this : new zt(Oa(this.done, t), this.undone, e, i);
  }
  addMapping(t) {
    return new zt(is(this.done, t), is(this.undone, t), this.prevTime, this.prevUserEvent);
  }
  pop(t, e, i) {
    let n = t == 0 ? this.done : this.undone;
    if (n.length == 0)
      return null;
    let r = n[n.length - 1], o = r.selectionsAfter[0] || (r.startSelection ? r.startSelection.map(r.changes.invertedDesc, 1) : e.selection);
    if (i && r.selectionsAfter.length)
      return e.update({
        selection: r.selectionsAfter[r.selectionsAfter.length - 1],
        annotations: Qs.of({ side: t, rest: Du(n), selection: o }),
        userEvent: t == 0 ? "select.undo" : "select.redo",
        scrollIntoView: !0
      });
    if (r.changes) {
      let l = n.length == 1 ? wt : n.slice(0, n.length - 1);
      return r.mapped && (l = is(l, r.mapped)), e.update({
        changes: r.changes,
        selection: r.startSelection,
        effects: r.effects,
        annotations: Qs.of({ side: t, rest: l, selection: o }),
        filter: !1,
        userEvent: t == 0 ? "undo" : "redo",
        scrollIntoView: !0
      });
    } else
      return null;
  }
}
zt.empty = /* @__PURE__ */ new zt(wt, wt);
const Lu = [
  { key: "Mod-z", run: Sa, preventDefault: !0 },
  { key: "Mod-y", mac: "Mod-Shift-z", run: $s, preventDefault: !0 },
  { linux: "Ctrl-Shift-z", run: $s, preventDefault: !0 },
  { key: "Mod-u", run: Cu, preventDefault: !0 },
  { key: "Alt-u", mac: "Mod-Shift-u", run: Au, preventDefault: !0 }
];
function je(s, t) {
  return x.create(s.ranges.map(t), s.mainIndex);
}
function Mt(s, t) {
  return s.update({ selection: t, scrollIntoView: !0, userEvent: "select" });
}
function Dt({ state: s, dispatch: t }, e) {
  let i = je(s.selection, e);
  return i.eq(s.selection, !0) ? !1 : (t(Mt(s, i)), !0);
}
function Nn(s, t) {
  return x.cursor(t ? s.to : s.from);
}
function Ca(s, t) {
  return Dt(s, (e) => e.empty ? s.moveByChar(e, t) : Nn(e, t));
}
function st(s) {
  return s.textDirectionAt(s.state.selection.main.head) == $.LTR;
}
const Aa = (s) => Ca(s, !st(s)), Ta = (s) => Ca(s, st(s));
function Pa(s, t) {
  return Dt(s, (e) => e.empty ? s.moveByGroup(e, t) : Nn(e, t));
}
const Eu = (s) => Pa(s, !st(s)), Iu = (s) => Pa(s, st(s));
function Nu(s, t, e) {
  if (t.type.prop(e))
    return !0;
  let i = t.to - t.from;
  return i && (i > 2 || /[^\s,.;:]/.test(s.sliceDoc(t.from, t.to))) || t.firstChild;
}
function Vn(s, t, e) {
  let i = Yt(s).resolveInner(t.head), n = e ? R.closedBy : R.openedBy;
  for (let a = t.head; ; ) {
    let h = e ? i.childAfter(a) : i.childBefore(a);
    if (!h)
      break;
    Nu(s, h, n) ? i = h : a = e ? h.to : h.from;
  }
  let r = i.type.prop(n), o, l;
  return r && (o = e ? Te(s, i.from, 1) : Te(s, i.to, -1)) && o.matched ? l = e ? o.end.to : o.end.from : l = e ? i.to : i.from, x.cursor(l, e ? -1 : 1);
}
const Vu = (s) => Dt(s, (t) => Vn(s.state, t, !st(s))), Wu = (s) => Dt(s, (t) => Vn(s.state, t, st(s)));
function Ma(s, t) {
  return Dt(s, (e) => {
    if (!e.empty)
      return Nn(e, t);
    let i = s.moveVertically(e, t);
    return i.head != e.head ? i : s.moveToLineBoundary(e, t);
  });
}
const Da = (s) => Ma(s, !1), Ra = (s) => Ma(s, !0);
function Ba(s) {
  let t = s.scrollDOM.clientHeight < s.scrollDOM.scrollHeight - 2, e = 0, i = 0, n;
  if (t) {
    for (let r of s.state.facet(M.scrollMargins)) {
      let o = r(s);
      o != null && o.top && (e = Math.max(o == null ? void 0 : o.top, e)), o != null && o.bottom && (i = Math.max(o == null ? void 0 : o.bottom, i));
    }
    n = s.scrollDOM.clientHeight - e - i;
  } else
    n = (s.dom.ownerDocument.defaultView || window).innerHeight;
  return {
    marginTop: e,
    marginBottom: i,
    selfScroll: t,
    height: Math.max(s.defaultLineHeight, n - 5)
  };
}
function La(s, t) {
  let e = Ba(s), { state: i } = s, n = je(i.selection, (o) => o.empty ? s.moveVertically(o, t, e.height) : Nn(o, t));
  if (n.eq(i.selection))
    return !1;
  let r;
  if (e.selfScroll) {
    let o = s.coordsAtPos(i.selection.main.head), l = s.scrollDOM.getBoundingClientRect(), a = l.top + e.marginTop, h = l.bottom - e.marginBottom;
    o && o.top > a && o.bottom < h && (r = M.scrollIntoView(n.main.head, { y: "start", yMargin: o.top - a }));
  }
  return s.dispatch(Mt(i, n), { effects: r }), !0;
}
const To = (s) => La(s, !1), js = (s) => La(s, !0);
function ae(s, t, e) {
  let i = s.lineBlockAt(t.head), n = s.moveToLineBoundary(t, e);
  if (n.head == t.head && n.head != (e ? i.to : i.from) && (n = s.moveToLineBoundary(t, e, !1)), !e && n.head == i.from && i.length) {
    let r = /^\s*/.exec(s.state.sliceDoc(i.from, Math.min(i.from + 100, i.to)))[0].length;
    r && t.head != i.from + r && (n = x.cursor(i.from + r));
  }
  return n;
}
const Hu = (s) => Dt(s, (t) => ae(s, t, !0)), Fu = (s) => Dt(s, (t) => ae(s, t, !1)), zu = (s) => Dt(s, (t) => ae(s, t, !st(s))), Qu = (s) => Dt(s, (t) => ae(s, t, st(s))), $u = (s) => Dt(s, (t) => x.cursor(s.lineBlockAt(t.head).from, 1)), ju = (s) => Dt(s, (t) => x.cursor(s.lineBlockAt(t.head).to, -1));
function Xu(s, t, e) {
  let i = !1, n = je(s.selection, (r) => {
    let o = Te(s, r.head, -1) || Te(s, r.head, 1) || r.head > 0 && Te(s, r.head - 1, 1) || r.head < s.doc.length && Te(s, r.head + 1, -1);
    if (!o || !o.end)
      return r;
    i = !0;
    let l = o.start.from == r.head ? o.end.to : o.end.from;
    return x.cursor(l);
  });
  return i ? (t(Mt(s, n)), !0) : !1;
}
const qu = ({ state: s, dispatch: t }) => Xu(s, t);
function vt(s, t) {
  let e = je(s.state.selection, (i) => {
    let n = t(i);
    return x.range(i.anchor, n.head, n.goalColumn, n.bidiLevel || void 0);
  });
  return e.eq(s.state.selection) ? !1 : (s.dispatch(Mt(s.state, e)), !0);
}
function Ea(s, t) {
  return vt(s, (e) => s.moveByChar(e, t));
}
const Ia = (s) => Ea(s, !st(s)), Na = (s) => Ea(s, st(s));
function Va(s, t) {
  return vt(s, (e) => s.moveByGroup(e, t));
}
const Uu = (s) => Va(s, !st(s)), Ku = (s) => Va(s, st(s)), _u = (s) => vt(s, (t) => Vn(s.state, t, !st(s))), Yu = (s) => vt(s, (t) => Vn(s.state, t, st(s)));
function Wa(s, t) {
  return vt(s, (e) => s.moveVertically(e, t));
}
const Ha = (s) => Wa(s, !1), Fa = (s) => Wa(s, !0);
function za(s, t) {
  return vt(s, (e) => s.moveVertically(e, t, Ba(s).height));
}
const Po = (s) => za(s, !1), Mo = (s) => za(s, !0), Gu = (s) => vt(s, (t) => ae(s, t, !0)), Zu = (s) => vt(s, (t) => ae(s, t, !1)), Ju = (s) => vt(s, (t) => ae(s, t, !st(s))), td = (s) => vt(s, (t) => ae(s, t, st(s))), ed = (s) => vt(s, (t) => x.cursor(s.lineBlockAt(t.head).from)), id = (s) => vt(s, (t) => x.cursor(s.lineBlockAt(t.head).to)), Do = ({ state: s, dispatch: t }) => (t(Mt(s, { anchor: 0 })), !0), Ro = ({ state: s, dispatch: t }) => (t(Mt(s, { anchor: s.doc.length })), !0), Bo = ({ state: s, dispatch: t }) => (t(Mt(s, { anchor: s.selection.main.anchor, head: 0 })), !0), Lo = ({ state: s, dispatch: t }) => (t(Mt(s, { anchor: s.selection.main.anchor, head: s.doc.length })), !0), nd = ({ state: s, dispatch: t }) => (t(s.update({ selection: { anchor: 0, head: s.doc.length }, userEvent: "select" })), !0), sd = ({ state: s, dispatch: t }) => {
  let e = Wn(s).map(({ from: i, to: n }) => x.range(i, Math.min(n + 1, s.doc.length)));
  return t(s.update({ selection: x.create(e), userEvent: "select" })), !0;
}, rd = ({ state: s, dispatch: t }) => {
  let e = je(s.selection, (i) => {
    let n = Yt(s), r = n.resolveStack(i.from, 1);
    if (i.empty) {
      let o = n.resolveStack(i.from, -1);
      o.node.from >= r.node.from && o.node.to <= r.node.to && (r = o);
    }
    for (let o = r; o; o = o.next) {
      let { node: l } = o;
      if ((l.from < i.from && l.to >= i.to || l.to > i.to && l.from <= i.from) && o.next)
        return x.range(l.to, l.from);
    }
    return i;
  });
  return e.eq(s.selection) ? !1 : (t(Mt(s, e)), !0);
};
function Qa(s, t) {
  let { state: e } = s, i = e.selection, n = e.selection.ranges.slice();
  for (let r of e.selection.ranges) {
    let o = e.doc.lineAt(r.head);
    if (t ? o.to < s.state.doc.length : o.from > 0)
      for (let l = r; ; ) {
        let a = s.moveVertically(l, t);
        if (a.head < o.from || a.head > o.to) {
          n.some((h) => h.head == a.head) || n.push(a);
          break;
        } else {
          if (a.head == l.head)
            break;
          l = a;
        }
      }
  }
  return n.length == i.ranges.length ? !1 : (s.dispatch(Mt(e, x.create(n, n.length - 1))), !0);
}
const od = (s) => Qa(s, !1), ld = (s) => Qa(s, !0), ad = ({ state: s, dispatch: t }) => {
  let e = s.selection, i = null;
  return e.ranges.length > 1 ? i = x.create([e.main]) : e.main.empty || (i = x.create([x.cursor(e.main.head)])), i ? (t(Mt(s, i)), !0) : !1;
};
function Si(s, t) {
  if (s.state.readOnly)
    return !1;
  let e = "delete.selection", { state: i } = s, n = i.changeByRange((r) => {
    let { from: o, to: l } = r;
    if (o == l) {
      let a = t(r);
      a < o ? (e = "delete.backward", a = $i(s, a, !1)) : a > o && (e = "delete.forward", a = $i(s, a, !0)), o = Math.min(o, a), l = Math.max(l, a);
    } else
      o = $i(s, o, !1), l = $i(s, l, !0);
    return o == l ? { range: r } : { changes: { from: o, to: l }, range: x.cursor(o, o < r.head ? -1 : 1) };
  });
  return n.changes.empty ? !1 : (s.dispatch(i.update(n, {
    scrollIntoView: !0,
    userEvent: e,
    effects: e == "delete.selection" ? M.announce.of(i.phrase("Selection deleted")) : void 0
  })), !0);
}
function $i(s, t, e) {
  if (s instanceof M)
    for (let i of s.state.facet(M.atomicRanges).map((n) => n(s)))
      i.between(t, t, (n, r) => {
        n < t && r > t && (t = e ? r : n);
      });
  return t;
}
const $a = (s, t, e) => Si(s, (i) => {
  let n = i.from, { state: r } = s, o = r.doc.lineAt(n), l, a;
  if (e && !t && n > o.from && n < o.from + 200 && !/[^ \t]/.test(l = o.text.slice(0, n - o.from))) {
    if (l[l.length - 1] == "	")
      return n - 1;
    let h = Tn(l, r.tabSize), f = h % yn(r) || yn(r);
    for (let c = 0; c < f && l[l.length - 1 - c] == " "; c++)
      n--;
    a = n;
  } else
    a = nt(o.text, n - o.from, t, t) + o.from, a == n && o.number != (t ? r.doc.lines : 1) ? a += t ? 1 : -1 : !t && /[\ufe00-\ufe0f]/.test(o.text.slice(a - o.from, n - o.from)) && (a = nt(o.text, a - o.from, !1, !1) + o.from);
  return a;
}), Xs = (s) => $a(s, !1, !0), ja = (s) => $a(s, !0, !1), Xa = (s, t) => Si(s, (e) => {
  let i = e.head, { state: n } = s, r = n.doc.lineAt(i), o = n.charCategorizer(i);
  for (let l = null; ; ) {
    if (i == (t ? r.to : r.from)) {
      i == e.head && r.number != (t ? n.doc.lines : 1) && (i += t ? 1 : -1);
      break;
    }
    let a = nt(r.text, i - r.from, t) + r.from, h = r.text.slice(Math.min(i, a) - r.from, Math.max(i, a) - r.from), f = o(h);
    if (l != null && f != l)
      break;
    (h != " " || i != e.head) && (l = f), i = a;
  }
  return i;
}), qa = (s) => Xa(s, !1), hd = (s) => Xa(s, !0), fd = (s) => Si(s, (t) => {
  let e = s.lineBlockAt(t.head).to;
  return t.head < e ? e : Math.min(s.state.doc.length, t.head + 1);
}), cd = (s) => Si(s, (t) => {
  let e = s.moveToLineBoundary(t, !1).head;
  return t.head > e ? e : Math.max(0, t.head - 1);
}), ud = (s) => Si(s, (t) => {
  let e = s.moveToLineBoundary(t, !0).head;
  return t.head < e ? e : Math.min(s.state.doc.length, t.head + 1);
}), dd = ({ state: s, dispatch: t }) => {
  if (s.readOnly)
    return !1;
  let e = s.changeByRange((i) => ({
    changes: { from: i.from, to: i.to, insert: E.of(["", ""]) },
    range: x.cursor(i.from)
  }));
  return t(s.update(e, { scrollIntoView: !0, userEvent: "input" })), !0;
}, pd = ({ state: s, dispatch: t }) => {
  if (s.readOnly)
    return !1;
  let e = s.changeByRange((i) => {
    if (!i.empty || i.from == 0 || i.from == s.doc.length)
      return { range: i };
    let n = i.from, r = s.doc.lineAt(n), o = n == r.from ? n - 1 : nt(r.text, n - r.from, !1) + r.from, l = n == r.to ? n + 1 : nt(r.text, n - r.from, !0) + r.from;
    return {
      changes: { from: o, to: l, insert: s.doc.slice(n, l).append(s.doc.slice(o, n)) },
      range: x.cursor(l)
    };
  });
  return e.changes.empty ? !1 : (t(s.update(e, { scrollIntoView: !0, userEvent: "move.character" })), !0);
};
function Wn(s) {
  let t = [], e = -1;
  for (let i of s.selection.ranges) {
    let n = s.doc.lineAt(i.from), r = s.doc.lineAt(i.to);
    if (!i.empty && i.to == r.from && (r = s.doc.lineAt(i.to - 1)), e >= n.number) {
      let o = t[t.length - 1];
      o.to = r.to, o.ranges.push(i);
    } else
      t.push({ from: n.from, to: r.to, ranges: [i] });
    e = r.number + 1;
  }
  return t;
}
function Ua(s, t, e) {
  if (s.readOnly)
    return !1;
  let i = [], n = [];
  for (let r of Wn(s)) {
    if (e ? r.to == s.doc.length : r.from == 0)
      continue;
    let o = s.doc.lineAt(e ? r.to + 1 : r.from - 1), l = o.length + 1;
    if (e) {
      i.push({ from: r.to, to: o.to }, { from: r.from, insert: o.text + s.lineBreak });
      for (let a of r.ranges)
        n.push(x.range(Math.min(s.doc.length, a.anchor + l), Math.min(s.doc.length, a.head + l)));
    } else {
      i.push({ from: o.from, to: r.from }, { from: r.to, insert: s.lineBreak + o.text });
      for (let a of r.ranges)
        n.push(x.range(a.anchor - l, a.head - l));
    }
  }
  return i.length ? (t(s.update({
    changes: i,
    scrollIntoView: !0,
    selection: x.create(n, s.selection.mainIndex),
    userEvent: "move.line"
  })), !0) : !1;
}
const gd = ({ state: s, dispatch: t }) => Ua(s, t, !1), md = ({ state: s, dispatch: t }) => Ua(s, t, !0);
function Ka(s, t, e) {
  if (s.readOnly)
    return !1;
  let i = [];
  for (let r of Wn(s))
    e ? i.push({ from: r.from, insert: s.doc.slice(r.from, r.to) + s.lineBreak }) : i.push({ from: r.to, insert: s.lineBreak + s.doc.slice(r.from, r.to) });
  let n = s.changes(i);
  return t(s.update({
    changes: n,
    selection: s.selection.map(n, e ? 1 : -1),
    scrollIntoView: !0,
    userEvent: "input.copyline"
  })), !0;
}
const yd = ({ state: s, dispatch: t }) => Ka(s, t, !1), bd = ({ state: s, dispatch: t }) => Ka(s, t, !0), wd = (s) => {
  if (s.state.readOnly)
    return !1;
  let { state: t } = s, e = t.changes(Wn(t).map(({ from: n, to: r }) => (n > 0 ? n-- : r < t.doc.length && r++, { from: n, to: r }))), i = je(t.selection, (n) => {
    let r;
    if (s.lineWrapping) {
      let o = s.lineBlockAt(n.head), l = s.coordsAtPos(n.head, n.assoc || 1);
      l && (r = o.bottom + s.documentTop - l.bottom + s.defaultLineHeight / 2);
    }
    return s.moveVertically(n, !0, r);
  }).map(e);
  return s.dispatch({ changes: e, selection: i, scrollIntoView: !0, userEvent: "delete.line" }), !0;
};
function xd(s, t) {
  if (/\(\)|\[\]|\{\}/.test(s.sliceDoc(t - 1, t + 1)))
    return { from: t, to: t };
  let e = Yt(s).resolveInner(t), i = e.childBefore(t), n = e.childAfter(t), r;
  return i && n && i.to <= t && n.from >= t && (r = i.type.prop(R.closedBy)) && r.indexOf(n.name) > -1 && s.doc.lineAt(i.to).from == s.doc.lineAt(n.from).from && !/\S/.test(s.sliceDoc(i.to, n.from)) ? { from: i.to, to: n.from } : null;
}
const Eo = /* @__PURE__ */ _a(!1), kd = /* @__PURE__ */ _a(!0);
function _a(s) {
  return ({ state: t, dispatch: e }) => {
    if (t.readOnly)
      return !1;
    let i = t.changeByRange((n) => {
      let { from: r, to: o } = n, l = t.doc.lineAt(r), a = !s && r == o && xd(t, r);
      s && (r = o = (o <= l.to ? l : t.doc.lineAt(o)).to);
      let h = new En(t, { simulateBreak: r, simulateDoubleBreak: !!a }), f = ya(h, r);
      for (f == null && (f = Tn(/^\s*/.exec(t.doc.lineAt(r).text)[0], t.tabSize)); o < l.to && /\s/.test(l.text[o - l.from]); )
        o++;
      a ? { from: r, to: o } = a : r > l.from && r < l.from + 100 && !/\S/.test(l.text.slice(0, r)) && (r = l.from);
      let c = ["", bn(t, f)];
      return a && c.push(bn(t, h.lineIndent(l.from, -1))), {
        changes: { from: r, to: o, insert: E.of(c) },
        range: x.cursor(r + 1 + c[1].length)
      };
    });
    return e(t.update(i, { scrollIntoView: !0, userEvent: "input" })), !0;
  };
}
function wr(s, t) {
  let e = -1;
  return s.changeByRange((i) => {
    let n = [];
    for (let o = i.from; o <= i.to; ) {
      let l = s.doc.lineAt(o);
      l.number > e && (i.empty || i.to > l.from) && (t(l, n, i), e = l.number), o = l.to + 1;
    }
    let r = s.changes(n);
    return {
      changes: n,
      range: x.range(r.mapPos(i.anchor, 1), r.mapPos(i.head, 1))
    };
  });
}
const Sd = ({ state: s, dispatch: t }) => {
  if (s.readOnly)
    return !1;
  let e = /* @__PURE__ */ Object.create(null), i = new En(s, { overrideIndentation: (r) => {
    let o = e[r];
    return o ?? -1;
  } }), n = wr(s, (r, o, l) => {
    let a = ya(i, r.from);
    if (a == null)
      return;
    /\S/.test(r.text) || (a = 0);
    let h = /^\s*/.exec(r.text)[0], f = bn(s, a);
    (h != f || l.from < r.from + h.length) && (e[r.from] = a, o.push({ from: r.from, to: r.from + h.length, insert: f }));
  });
  return n.changes.empty || t(s.update(n, { userEvent: "indent" })), !0;
}, vd = ({ state: s, dispatch: t }) => s.readOnly ? !1 : (t(s.update(wr(s, (e, i) => {
  i.push({ from: e.from, insert: s.facet(gr) });
}), { userEvent: "input.indent" })), !0), Od = ({ state: s, dispatch: t }) => s.readOnly ? !1 : (t(s.update(wr(s, (e, i) => {
  let n = /^\s*/.exec(e.text)[0];
  if (!n)
    return;
  let r = Tn(n, s.tabSize), o = 0, l = bn(s, Math.max(0, r - yn(s)));
  for (; o < n.length && o < l.length && n.charCodeAt(o) == l.charCodeAt(o); )
    o++;
  i.push({ from: e.from + o, to: e.from + n.length, insert: l.slice(o) });
}), { userEvent: "delete.dedent" })), !0), Cd = (s) => (s.setTabFocusMode(), !0), Ad = [
  { key: "Ctrl-b", run: Aa, shift: Ia, preventDefault: !0 },
  { key: "Ctrl-f", run: Ta, shift: Na },
  { key: "Ctrl-p", run: Da, shift: Ha },
  { key: "Ctrl-n", run: Ra, shift: Fa },
  { key: "Ctrl-a", run: $u, shift: ed },
  { key: "Ctrl-e", run: ju, shift: id },
  { key: "Ctrl-d", run: ja },
  { key: "Ctrl-h", run: Xs },
  { key: "Ctrl-k", run: fd },
  { key: "Ctrl-Alt-h", run: qa },
  { key: "Ctrl-o", run: dd },
  { key: "Ctrl-t", run: pd },
  { key: "Ctrl-v", run: js }
], Td = /* @__PURE__ */ [
  { key: "ArrowLeft", run: Aa, shift: Ia, preventDefault: !0 },
  { key: "Mod-ArrowLeft", mac: "Alt-ArrowLeft", run: Eu, shift: Uu, preventDefault: !0 },
  { mac: "Cmd-ArrowLeft", run: zu, shift: Ju, preventDefault: !0 },
  { key: "ArrowRight", run: Ta, shift: Na, preventDefault: !0 },
  { key: "Mod-ArrowRight", mac: "Alt-ArrowRight", run: Iu, shift: Ku, preventDefault: !0 },
  { mac: "Cmd-ArrowRight", run: Qu, shift: td, preventDefault: !0 },
  { key: "ArrowUp", run: Da, shift: Ha, preventDefault: !0 },
  { mac: "Cmd-ArrowUp", run: Do, shift: Bo },
  { mac: "Ctrl-ArrowUp", run: To, shift: Po },
  { key: "ArrowDown", run: Ra, shift: Fa, preventDefault: !0 },
  { mac: "Cmd-ArrowDown", run: Ro, shift: Lo },
  { mac: "Ctrl-ArrowDown", run: js, shift: Mo },
  { key: "PageUp", run: To, shift: Po },
  { key: "PageDown", run: js, shift: Mo },
  { key: "Home", run: Fu, shift: Zu, preventDefault: !0 },
  { key: "Mod-Home", run: Do, shift: Bo },
  { key: "End", run: Hu, shift: Gu, preventDefault: !0 },
  { key: "Mod-End", run: Ro, shift: Lo },
  { key: "Enter", run: Eo, shift: Eo },
  { key: "Mod-a", run: nd },
  { key: "Backspace", run: Xs, shift: Xs, preventDefault: !0 },
  { key: "Delete", run: ja, preventDefault: !0 },
  { key: "Mod-Backspace", mac: "Alt-Backspace", run: qa, preventDefault: !0 },
  { key: "Mod-Delete", mac: "Alt-Delete", run: hd, preventDefault: !0 },
  { mac: "Mod-Backspace", run: cd, preventDefault: !0 },
  { mac: "Mod-Delete", run: ud, preventDefault: !0 }
].concat(/* @__PURE__ */ Ad.map((s) => ({ mac: s.key, run: s.run, shift: s.shift }))), Pd = /* @__PURE__ */ [
  { key: "Alt-ArrowLeft", mac: "Ctrl-ArrowLeft", run: Vu, shift: _u },
  { key: "Alt-ArrowRight", mac: "Ctrl-ArrowRight", run: Wu, shift: Yu },
  { key: "Alt-ArrowUp", run: gd },
  { key: "Shift-Alt-ArrowUp", run: yd },
  { key: "Alt-ArrowDown", run: md },
  { key: "Shift-Alt-ArrowDown", run: bd },
  { key: "Mod-Alt-ArrowUp", run: od },
  { key: "Mod-Alt-ArrowDown", run: ld },
  { key: "Escape", run: ad },
  { key: "Mod-Enter", run: kd },
  { key: "Alt-l", mac: "Ctrl-l", run: sd },
  { key: "Mod-i", run: rd, preventDefault: !0 },
  { key: "Mod-[", run: Od },
  { key: "Mod-]", run: vd },
  { key: "Mod-Alt-\\", run: Sd },
  { key: "Shift-Mod-k", run: wd },
  { key: "Shift-Mod-\\", run: qu },
  { key: "Mod-/", run: gu },
  { key: "Alt-A", run: yu },
  { key: "Ctrl-m", mac: "Shift-Alt-m", run: Cd }
].concat(Td);
class Ya {
  /**
  Create a new completion context. (Mostly useful for testing
  completion sources—in the editor, the extension will create
  these for you.)
  */
  constructor(t, e, i, n) {
    this.state = t, this.pos = e, this.explicit = i, this.view = n, this.abortListeners = [], this.abortOnDocChange = !1;
  }
  /**
  Get the extent, content, and (if there is a token) type of the
  token before `this.pos`.
  */
  tokenBefore(t) {
    let e = Yt(this.state).resolveInner(this.pos, -1);
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
    let e = this.state.doc.lineAt(this.pos), i = Math.max(e.from, this.pos - 250), n = e.text.slice(i - e.from, this.pos - e.from), r = n.search(Ga(t, !1));
    return r < 0 ? null : { from: i + r, to: this.pos, text: n.slice(r) };
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
function Io(s) {
  let t = Object.keys(s).join(""), e = /\w/.test(t);
  return e && (t = t.replace(/\w/g, "")), `[${e ? "\\w" : ""}${t.replace(/[^\w\s]/g, "\\$&")}]`;
}
function Md(s) {
  let t = /* @__PURE__ */ Object.create(null), e = /* @__PURE__ */ Object.create(null);
  for (let { label: n } of s) {
    t[n[0]] = !0;
    for (let r = 1; r < n.length; r++)
      e[n[r]] = !0;
  }
  let i = Io(t) + Io(e) + "*$";
  return [new RegExp("^" + i), new RegExp(i)];
}
function Dd(s) {
  let t = s.map((n) => typeof n == "string" ? { label: n } : n), [e, i] = t.every((n) => /^\w+$/.test(n.label)) ? [/\w*$/, /\w+$/] : Md(t);
  return (n) => {
    let r = n.matchBefore(i);
    return r || n.explicit ? { from: r ? r.from : n.pos, options: t, validFor: e } : null;
  };
}
class No {
  constructor(t, e, i, n) {
    this.completion = t, this.source = e, this.match = i, this.score = n;
  }
}
function we(s) {
  return s.selection.main.from;
}
function Ga(s, t) {
  var e;
  let { source: i } = s, n = t && i[0] != "^", r = i[i.length - 1] != "$";
  return !n && !r ? s : new RegExp(`${n ? "^" : ""}(?:${i})${r ? "$" : ""}`, (e = s.flags) !== null && e !== void 0 ? e : s.ignoreCase ? "i" : "");
}
const Za = /* @__PURE__ */ Gt.define();
function Rd(s, t, e, i) {
  let { main: n } = s.selection, r = e - n.from, o = i - n.from;
  return {
    ...s.changeByRange((l) => {
      if (l != n && e != i && s.sliceDoc(l.from + r, l.from + o) != s.sliceDoc(e, i))
        return { range: l };
      let a = s.toText(t);
      return {
        changes: { from: l.from + r, to: i == n.from ? l.to : l.from + o, insert: a },
        range: x.cursor(l.from + r + a.length)
      };
    }),
    scrollIntoView: !0,
    userEvent: "input.complete"
  };
}
const Vo = /* @__PURE__ */ new WeakMap();
function Bd(s) {
  if (!Array.isArray(s))
    return s;
  let t = Vo.get(s);
  return t || Vo.set(s, t = Dd(s)), t;
}
const xn = /* @__PURE__ */ I.define(), gi = /* @__PURE__ */ I.define();
class Ld {
  constructor(t) {
    this.pattern = t, this.chars = [], this.folded = [], this.any = [], this.precise = [], this.byWord = [], this.score = 0, this.matched = [];
    for (let e = 0; e < t.length; ) {
      let i = ue(t, e), n = Oe(i);
      this.chars.push(i);
      let r = t.slice(e, e + n), o = r.toUpperCase();
      this.folded.push(ue(o == r ? r.toLowerCase() : o, 0)), e += n;
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
    let { chars: e, folded: i, any: n, precise: r, byWord: o } = this;
    if (e.length == 1) {
      let w = ue(t, 0), b = Oe(w), O = b == t.length ? 0 : -100;
      if (w != e[0]) if (w == i[0])
        O += -200;
      else
        return null;
      return this.ret(O, [0, b]);
    }
    let l = t.indexOf(this.pattern);
    if (l == 0)
      return this.ret(t.length == this.pattern.length ? 0 : -100, [0, this.pattern.length]);
    let a = e.length, h = 0;
    if (l < 0) {
      for (let w = 0, b = Math.min(t.length, 200); w < b && h < a; ) {
        let O = ue(t, w);
        (O == e[h] || O == i[h]) && (n[h++] = w), w += Oe(O);
      }
      if (h < a)
        return null;
    }
    let f = 0, c = 0, u = !1, d = 0, p = -1, g = -1, m = /[a-z]/.test(t), y = !0;
    for (let w = 0, b = Math.min(t.length, 200), O = 0; w < b && c < a; ) {
      let v = ue(t, w);
      l < 0 && (f < a && v == e[f] && (r[f++] = w), d < a && (v == e[d] || v == i[d] ? (d == 0 && (p = w), g = w + 1, d++) : d = 0));
      let T, A = v < 255 ? v >= 48 && v <= 57 || v >= 97 && v <= 122 ? 2 : v >= 65 && v <= 90 ? 1 : 0 : (T = mh(v)) != T.toLowerCase() ? 1 : T != T.toUpperCase() ? 2 : 0;
      (!w || A == 1 && m || O == 0 && A != 0) && (e[c] == v || i[c] == v && (u = !0) ? o[c++] = w : o.length && (y = !1)), O = A, w += Oe(v);
    }
    return c == a && o[0] == 0 && y ? this.result(-100 + (u ? -200 : 0), o, t) : d == a && p == 0 ? this.ret(-200 - t.length + (g == t.length ? 0 : -100), [0, g]) : l > -1 ? this.ret(-700 - t.length, [l, l + this.pattern.length]) : d == a ? this.ret(-900 - t.length, [p, g]) : c == a ? this.result(-100 + (u ? -200 : 0) + -700 + (y ? 0 : -1100), o, t) : e.length == 2 ? null : this.result((n[0] ? -700 : 0) + -200 + -1100, n, t);
  }
  result(t, e, i) {
    let n = [], r = 0;
    for (let o of e) {
      let l = o + (this.astral ? Oe(ue(i, o)) : 1);
      r && n[r - 1] == o ? n[r - 1] = l : (n[r++] = o, n[r++] = l);
    }
    return this.ret(t - i.length, n);
  }
}
class Ed {
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
const Z = /* @__PURE__ */ P.define({
  combine(s) {
    return Ks(s, {
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
      positionInfo: Id,
      filterStrict: !1,
      compareCompletions: (t, e) => (t.sortText || t.label).localeCompare(e.sortText || e.label),
      interactionDelay: 75,
      updateSyncTime: 100
    }, {
      defaultKeymap: (t, e) => t && e,
      closeOnBlur: (t, e) => t && e,
      icons: (t, e) => t && e,
      tooltipClass: (t, e) => (i) => Wo(t(i), e(i)),
      optionClass: (t, e) => (i) => Wo(t(i), e(i)),
      addToOptions: (t, e) => t.concat(e),
      filterStrict: (t, e) => t || e
    });
  }
});
function Wo(s, t) {
  return s ? t ? s + " " + t : s : t;
}
function Id(s, t, e, i, n, r) {
  let o = s.textDirection == $.RTL, l = o, a = !1, h = "top", f, c, u = t.left - n.left, d = n.right - t.right, p = i.right - i.left, g = i.bottom - i.top;
  if (l && u < Math.min(p, d) ? l = !1 : !l && d < Math.min(p, u) && (l = !0), p <= (l ? u : d))
    f = Math.max(n.top, Math.min(e.top, n.bottom - g)) - t.top, c = Math.min(400, l ? u : d);
  else {
    a = !0, c = Math.min(
      400,
      (o ? t.right : n.right - t.left) - 30
      /* Info.Margin */
    );
    let w = n.bottom - t.bottom;
    w >= g || w > t.top ? f = e.bottom - t.top : (h = "bottom", f = t.bottom - e.top);
  }
  let m = (t.bottom - t.top) / r.offsetHeight, y = (t.right - t.left) / r.offsetWidth;
  return {
    style: `${h}: ${f / m}px; max-width: ${c / y}px`,
    class: "cm-completionInfo-" + (a ? o ? "left-narrow" : "right-narrow" : l ? "left" : "right")
  };
}
const xr = /* @__PURE__ */ I.define();
function Nd(s) {
  let t = s.addToOptions.slice();
  return s.icons && t.push({
    render(e) {
      let i = document.createElement("div");
      return i.classList.add("cm-completionIcon"), e.type && i.classList.add(...e.type.split(/\s+/g).map((n) => "cm-completionIcon-" + n)), i.setAttribute("aria-hidden", "true"), i;
    },
    position: 20
  }), t.push({
    render(e, i, n, r) {
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
function ns(s, t, e) {
  if (s <= e)
    return { from: 0, to: s };
  if (t < 0 && (t = 0), t <= s >> 1) {
    let n = Math.floor(t / e);
    return { from: n * e, to: (n + 1) * e };
  }
  let i = Math.floor((s - t) / e);
  return { from: s - (i + 1) * e, to: s - i * e };
}
class Vd {
  constructor(t, e, i) {
    this.view = t, this.stateField = e, this.applyCompletion = i, this.info = null, this.infoDestroy = null, this.placeInfoReq = {
      read: () => this.measureInfo(),
      write: (a) => this.placeInfo(a),
      key: this
    }, this.space = null, this.currentClass = "";
    let n = t.state.field(e), { options: r, selected: o } = n.open, l = t.state.facet(Z);
    this.optionContent = Nd(l), this.optionClass = l.optionClass, this.tooltipClass = l.tooltipClass, this.range = ns(r.length, o, l.maxRenderedOptions), this.dom = document.createElement("div"), this.dom.className = "cm-tooltip-autocomplete", this.updateTooltipClass(t.state), this.dom.addEventListener("mousedown", (a) => {
      let { options: h } = t.state.field(e).open;
      for (let f = a.target, c; f && f != this.dom; f = f.parentNode)
        if (f.nodeName == "LI" && (c = /-(\d+)$/.exec(f.id)) && +c[1] < h.length) {
          this.applyCompletion(t, h[+c[1]]), a.preventDefault();
          return;
        }
      if (a.target == this.list) {
        let f = this.list.classList.contains("cm-completionListIncompleteTop") && a.clientY < this.list.firstChild.getBoundingClientRect().top ? this.range.from - 1 : this.list.classList.contains("cm-completionListIncompleteBottom") && a.clientY > this.list.lastChild.getBoundingClientRect().bottom ? this.range.to : null;
        f != null && (t.dispatch({ effects: xr.of(f) }), a.preventDefault());
      }
    }), this.dom.addEventListener("focusout", (a) => {
      let h = t.state.field(this.stateField, !1);
      h && h.tooltip && t.state.facet(Z).closeOnBlur && a.relatedTarget != t.contentDOM && t.dispatch({ effects: gi.of(null) });
    }), this.showOptions(r, n.id);
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
    let i = t.state.field(this.stateField), n = t.startState.field(this.stateField);
    if (this.updateTooltipClass(t.state), i != n) {
      let { options: r, selected: o, disabled: l } = i.open;
      (!n.open || n.open.options != r) && (this.range = ns(r.length, o, t.state.facet(Z).maxRenderedOptions), this.showOptions(r, i.id)), this.updateSel(), l != ((e = n.open) === null || e === void 0 ? void 0 : e.disabled) && this.dom.classList.toggle("cm-tooltip-autocomplete-disabled", !!l);
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
    (e.selected > -1 && e.selected < this.range.from || e.selected >= this.range.to) && (this.range = ns(e.options.length, e.selected, this.view.state.facet(Z).maxRenderedOptions), this.showOptions(e.options, t.id));
    let i = this.updateSelectedOption(e.selected);
    if (i) {
      this.destroyInfo();
      let { completion: n } = e.options[e.selected], { info: r } = n;
      if (!r)
        return;
      let o = typeof r == "string" ? document.createTextNode(r) : r(n);
      if (!o)
        return;
      "then" in o ? o.then((l) => {
        l && this.view.state.field(this.stateField, !1) == t && this.addInfoPane(l, n);
      }).catch((l) => ft(this.view.state, l, "completion info")) : (this.addInfoPane(o, n), i.setAttribute("aria-describedby", this.info.id));
    }
  }
  addInfoPane(t, e) {
    this.destroyInfo();
    let i = this.info = document.createElement("div");
    if (i.className = "cm-tooltip cm-completionInfo", i.id = "cm-completionInfo-" + Math.floor(Math.random() * 65535).toString(16), t.nodeType != null)
      i.appendChild(t), this.infoDestroy = null;
    else {
      let { dom: n, destroy: r } = t;
      i.appendChild(n), this.infoDestroy = r || null;
    }
    this.dom.appendChild(i), this.view.requestMeasure(this.placeInfoReq);
  }
  updateSelectedOption(t) {
    let e = null;
    for (let i = this.list.firstChild, n = this.range.from; i; i = i.nextSibling, n++)
      i.nodeName != "LI" || !i.id ? n-- : n == t ? i.hasAttribute("aria-selected") || (i.setAttribute("aria-selected", "true"), e = i) : i.hasAttribute("aria-selected") && (i.removeAttribute("aria-selected"), i.removeAttribute("aria-describedby"));
    return e && Hd(this.list, e), e;
  }
  measureInfo() {
    let t = this.dom.querySelector("[aria-selected]");
    if (!t || !this.info)
      return null;
    let e = this.dom.getBoundingClientRect(), i = this.info.getBoundingClientRect(), n = t.getBoundingClientRect(), r = this.space;
    if (!r) {
      let o = this.dom.ownerDocument.documentElement;
      r = { left: 0, top: 0, right: o.clientWidth, bottom: o.clientHeight };
    }
    return n.top > Math.min(r.bottom, e.bottom) - 10 || n.bottom < Math.max(r.top, e.top) + 10 ? null : this.view.state.facet(Z).positionInfo(this.view, e, n, i, r, this.dom);
  }
  placeInfo(t) {
    this.info && (t ? (t.style && (this.info.style.cssText = t.style), this.info.className = "cm-tooltip cm-completionInfo " + (t.class || "")) : this.info.style.cssText = "top: -1e6px");
  }
  createListBox(t, e, i) {
    const n = document.createElement("ul");
    n.id = e, n.setAttribute("role", "listbox"), n.setAttribute("aria-expanded", "true"), n.setAttribute("aria-label", this.view.state.phrase("Completions")), n.addEventListener("mousedown", (o) => {
      o.target == n && o.preventDefault();
    });
    let r = null;
    for (let o = i.from; o < i.to; o++) {
      let { completion: l, match: a } = t[o], { section: h } = l;
      if (h) {
        let u = typeof h == "string" ? h : h.name;
        if (u != r && (o > i.from || i.from == 0))
          if (r = u, typeof h != "string" && h.header)
            n.appendChild(h.header(h));
          else {
            let d = n.appendChild(document.createElement("completion-section"));
            d.textContent = u;
          }
      }
      const f = n.appendChild(document.createElement("li"));
      f.id = e + "-" + o, f.setAttribute("role", "option");
      let c = this.optionClass(l);
      c && (f.className = c);
      for (let u of this.optionContent) {
        let d = u(l, this.view.state, this.view, a);
        d && f.appendChild(d);
      }
    }
    return i.from && n.classList.add("cm-completionListIncompleteTop"), i.to < t.length && n.classList.add("cm-completionListIncompleteBottom"), n;
  }
  destroyInfo() {
    this.info && (this.infoDestroy && this.infoDestroy(), this.info.remove(), this.info = null);
  }
  destroy() {
    this.destroyInfo();
  }
}
function Wd(s, t) {
  return (e) => new Vd(e, s, t);
}
function Hd(s, t) {
  let e = s.getBoundingClientRect(), i = t.getBoundingClientRect(), n = e.height / s.offsetHeight;
  i.top < e.top ? s.scrollTop -= (e.top - i.top) / n : i.bottom > e.bottom && (s.scrollTop += (i.bottom - e.bottom) / n);
}
function Ho(s) {
  return (s.boost || 0) * 100 + (s.apply ? 10 : 0) + (s.info ? 5 : 0) + (s.type ? 1 : 0);
}
function Fd(s, t) {
  let e = [], i = null, n = null, r = (f) => {
    e.push(f);
    let { section: c } = f.completion;
    if (c) {
      i || (i = []);
      let u = typeof c == "string" ? c : c.name;
      i.some((d) => d.name == u) || i.push(typeof c == "string" ? { name: u } : c);
    }
  }, o = t.facet(Z);
  for (let f of s)
    if (f.hasResult()) {
      let c = f.result.getMatch;
      if (f.result.filter === !1)
        for (let u of f.result.options)
          r(new No(u, f.source, c ? c(u) : [], 1e9 - e.length));
      else {
        let u = t.sliceDoc(f.from, f.to), d, p = o.filterStrict ? new Ed(u) : new Ld(u);
        for (let g of f.result.options)
          if (d = p.match(g.label)) {
            let m = g.displayLabel ? c ? c(g, d.matched) : [] : d.matched, y = d.score + (g.boost || 0);
            if (r(new No(g, f.source, m, y)), typeof g.section == "object" && g.section.rank === "dynamic") {
              let { name: w } = g.section;
              n || (n = /* @__PURE__ */ Object.create(null)), n[w] = Math.max(y, n[w] || -1e9);
            }
          }
      }
    }
  if (i) {
    let f = /* @__PURE__ */ Object.create(null), c = 0, u = (d, p) => (d.rank === "dynamic" && p.rank === "dynamic" ? n[p.name] - n[d.name] : 0) || (typeof d.rank == "number" ? d.rank : 1e9) - (typeof p.rank == "number" ? p.rank : 1e9) || (d.name < p.name ? -1 : 1);
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
    !a || a.label != c.label || a.detail != c.detail || a.type != null && c.type != null && a.type != c.type || a.apply != c.apply || a.boost != c.boost ? l.push(f) : Ho(f.completion) > Ho(a) && (l[l.length - 1] = f), a = f.completion;
  }
  return l;
}
class Pe {
  constructor(t, e, i, n, r, o) {
    this.options = t, this.attrs = e, this.tooltip = i, this.timestamp = n, this.selected = r, this.disabled = o;
  }
  setSelected(t, e) {
    return t == this.selected || t >= this.options.length ? this : new Pe(this.options, Fo(e, t), this.tooltip, this.timestamp, t, this.disabled);
  }
  static build(t, e, i, n, r, o) {
    if (n && !o && t.some((h) => h.isPending))
      return n.setDisabled();
    let l = Fd(t, e);
    if (!l.length)
      return n && t.some((h) => h.isPending) ? n.setDisabled() : null;
    let a = e.facet(Z).selectOnOpen ? 0 : -1;
    if (n && n.selected != a && n.selected != -1) {
      let h = n.options[n.selected].completion;
      for (let f = 0; f < l.length; f++)
        if (l[f].completion == h) {
          a = f;
          break;
        }
    }
    return new Pe(l, Fo(i, a), {
      pos: t.reduce((h, f) => f.hasResult() ? Math.min(h, f.from) : h, 1e8),
      create: qd,
      above: r.aboveCursor
    }, n ? n.timestamp : Date.now(), a, !1);
  }
  map(t) {
    return new Pe(this.options, this.attrs, { ...this.tooltip, pos: t.mapPos(this.tooltip.pos) }, this.timestamp, this.selected, this.disabled);
  }
  setDisabled() {
    return new Pe(this.options, this.attrs, this.tooltip, this.timestamp, this.selected, !0);
  }
}
class kn {
  constructor(t, e, i) {
    this.active = t, this.id = e, this.open = i;
  }
  static start() {
    return new kn(jd, "cm-ac-" + Math.floor(Math.random() * 2e6).toString(36), null);
  }
  update(t) {
    let { state: e } = t, i = e.facet(Z), r = (i.override || e.languageDataAt("autocomplete", we(e)).map(Bd)).map((a) => (this.active.find((f) => f.source == a) || new xt(
      a,
      this.active.some(
        (f) => f.state != 0
        /* State.Inactive */
      ) ? 1 : 0
      /* State.Inactive */
    )).update(t, i));
    r.length == this.active.length && r.every((a, h) => a == this.active[h]) && (r = this.active);
    let o = this.open, l = t.effects.some((a) => a.is(kr));
    o && t.docChanged && (o = o.map(t.changes)), t.selection || r.some((a) => a.hasResult() && t.changes.touchesRange(a.from, a.to)) || !zd(r, this.active) || l ? o = Pe.build(r, e, this.id, o, i, l) : o && o.disabled && !r.some((a) => a.isPending) && (o = null), !o && r.every((a) => !a.isPending) && r.some((a) => a.hasResult()) && (r = r.map((a) => a.hasResult() ? new xt(
      a.source,
      0
      /* State.Inactive */
    ) : a));
    for (let a of t.effects)
      a.is(xr) && (o = o && o.setSelected(a.value, this.id));
    return r == this.active && o == this.open ? this : new kn(r, this.id, o);
  }
  get tooltip() {
    return this.open ? this.open.tooltip : null;
  }
  get attrs() {
    return this.open ? this.open.attrs : this.active.length ? Qd : $d;
  }
}
function zd(s, t) {
  if (s == t)
    return !0;
  for (let e = 0, i = 0; ; ) {
    for (; e < s.length && !s[e].hasResult(); )
      e++;
    for (; i < t.length && !t[i].hasResult(); )
      i++;
    let n = e == s.length, r = i == t.length;
    if (n || r)
      return n == r;
    if (s[e++].result != t[i++].result)
      return !1;
  }
}
const Qd = {
  "aria-autocomplete": "list"
}, $d = {};
function Fo(s, t) {
  let e = {
    "aria-autocomplete": "list",
    "aria-haspopup": "listbox",
    "aria-controls": s
  };
  return t > -1 && (e["aria-activedescendant"] = s + "-" + t), e;
}
const jd = [];
function Ja(s, t) {
  if (s.isUserEvent("input.complete")) {
    let i = s.annotation(Za);
    if (i && t.activateOnCompletion(i))
      return 12;
  }
  let e = s.isUserEvent("input.type");
  return e && t.activateOnTyping ? 5 : e ? 1 : s.isUserEvent("delete.backward") ? 2 : s.selection ? 8 : s.docChanged ? 16 : 0;
}
class xt {
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
    let i = Ja(t, e), n = this;
    (i & 8 || i & 16 && this.touches(t)) && (n = new xt(
      n.source,
      0
      /* State.Inactive */
    )), i & 4 && n.state == 0 && (n = new xt(
      this.source,
      1
      /* State.Pending */
    )), n = n.updateFor(t, i);
    for (let r of t.effects)
      if (r.is(xn))
        n = new xt(n.source, 1, r.value);
      else if (r.is(gi))
        n = new xt(
          n.source,
          0
          /* State.Inactive */
        );
      else if (r.is(kr))
        for (let o of r.value)
          o.source == n.source && (n = o);
    return n;
  }
  updateFor(t, e) {
    return this.map(t.changes);
  }
  map(t) {
    return this;
  }
  touches(t) {
    return t.changes.touchesRange(we(t.state));
  }
}
class Le extends xt {
  constructor(t, e, i, n, r, o) {
    super(t, 3, e), this.limit = i, this.result = n, this.from = r, this.to = o;
  }
  hasResult() {
    return !0;
  }
  updateFor(t, e) {
    var i;
    if (!(e & 3))
      return this.map(t.changes);
    let n = this.result;
    n.map && !t.changes.empty && (n = n.map(n, t.changes));
    let r = t.changes.mapPos(this.from), o = t.changes.mapPos(this.to, 1), l = we(t.state);
    if (l > o || !n || e & 2 && (we(t.startState) == this.from || l < this.limit))
      return new xt(
        this.source,
        e & 4 ? 1 : 0
        /* State.Inactive */
      );
    let a = t.changes.mapPos(this.limit);
    return Xd(n.validFor, t.state, r, o) ? new Le(this.source, this.explicit, a, n, r, o) : n.update && (n = n.update(n, r, o, new Ya(t.state, l, !1))) ? new Le(this.source, this.explicit, a, n, n.from, (i = n.to) !== null && i !== void 0 ? i : we(t.state)) : new xt(this.source, 1, this.explicit);
  }
  map(t) {
    return t.empty ? this : (this.result.map ? this.result.map(this.result, t) : this.result) ? new Le(this.source, this.explicit, t.mapPos(this.limit), this.result, t.mapPos(this.from), t.mapPos(this.to, 1)) : new xt(
      this.source,
      0
      /* State.Inactive */
    );
  }
  touches(t) {
    return t.changes.touchesRange(this.from, this.to);
  }
}
function Xd(s, t, e, i) {
  if (!s)
    return !1;
  let n = t.sliceDoc(e, i);
  return typeof s == "function" ? s(n, e, i, t) : Ga(s, !0).test(n);
}
const kr = /* @__PURE__ */ I.define({
  map(s, t) {
    return s.map((e) => e.map(t));
  }
}), lt = /* @__PURE__ */ Pt.define({
  create() {
    return kn.start();
  },
  update(s, t) {
    return s.update(t);
  },
  provide: (s) => [
    fr.from(s, (t) => t.tooltip),
    M.contentAttributes.from(s, (t) => t.attrs)
  ]
});
function Sr(s, t) {
  const e = t.completion.apply || t.completion.label;
  let i = s.state.field(lt).active.find((n) => n.source == t.source);
  return i instanceof Le ? (typeof e == "string" ? s.dispatch({
    ...Rd(s.state, e, i.from, i.to),
    annotations: Za.of(t.completion)
  }) : e(s, t.completion, i.from, i.to), !0) : !1;
}
const qd = /* @__PURE__ */ Wd(lt, Sr);
function ji(s, t = "option") {
  return (e) => {
    let i = e.state.field(lt, !1);
    if (!i || !i.open || i.open.disabled || Date.now() - i.open.timestamp < e.state.facet(Z).interactionDelay)
      return !1;
    let n = 1, r;
    t == "page" && (r = ha(e, i.open.tooltip)) && (n = Math.max(2, Math.floor(r.dom.offsetHeight / r.dom.querySelector("li").offsetHeight) - 1));
    let { length: o } = i.open.options, l = i.open.selected > -1 ? i.open.selected + n * (s ? 1 : -1) : s ? 0 : o - 1;
    return l < 0 ? l = t == "page" ? 0 : o - 1 : l >= o && (l = t == "page" ? o - 1 : 0), e.dispatch({ effects: xr.of(l) }), !0;
  };
}
const Ud = (s) => {
  let t = s.state.field(lt, !1);
  return s.state.readOnly || !t || !t.open || t.open.selected < 0 || t.open.disabled || Date.now() - t.open.timestamp < s.state.facet(Z).interactionDelay ? !1 : Sr(s, t.open.options[t.open.selected]);
}, ss = (s) => s.state.field(lt, !1) ? (s.dispatch({ effects: xn.of(!0) }), !0) : !1, Kd = (s) => {
  let t = s.state.field(lt, !1);
  return !t || !t.active.some(
    (e) => e.state != 0
    /* State.Inactive */
  ) ? !1 : (s.dispatch({ effects: gi.of(null) }), !0);
};
class _d {
  constructor(t, e) {
    this.active = t, this.context = e, this.time = Date.now(), this.updates = [], this.done = void 0;
  }
}
const Yd = 50, Gd = 1e3, Zd = /* @__PURE__ */ $t.fromClass(class {
  constructor(s) {
    this.view = s, this.debounceUpdate = -1, this.running = [], this.debounceAccept = -1, this.pendingStart = !1, this.composing = 0;
    for (let t of s.state.field(lt).active)
      t.isPending && this.startQuery(t);
  }
  update(s) {
    let t = s.state.field(lt), e = s.state.facet(Z);
    if (!s.selectionSet && !s.docChanged && s.startState.field(lt) == t)
      return;
    let i = s.transactions.some((r) => {
      let o = Ja(r, e);
      return o & 8 || (r.selection || r.docChanged) && !(o & 3);
    });
    for (let r = 0; r < this.running.length; r++) {
      let o = this.running[r];
      if (i || o.context.abortOnDocChange && s.docChanged || o.updates.length + s.transactions.length > Yd && Date.now() - o.time > Gd) {
        for (let l of o.context.abortListeners)
          try {
            l();
          } catch (a) {
            ft(this.view.state, a);
          }
        o.context.abortListeners = null, this.running.splice(r--, 1);
      } else
        o.updates.push(...s.transactions);
    }
    this.debounceUpdate > -1 && clearTimeout(this.debounceUpdate), s.transactions.some((r) => r.effects.some((o) => o.is(xn))) && (this.pendingStart = !0);
    let n = this.pendingStart ? 50 : e.activateOnTypingDelay;
    if (this.debounceUpdate = t.active.some((r) => r.isPending && !this.running.some((o) => o.active.source == r.source)) ? setTimeout(() => this.startUpdate(), n) : -1, this.composing != 0)
      for (let r of s.transactions)
        r.isUserEvent("input.type") ? this.composing = 2 : this.composing == 2 && r.selection && (this.composing = 3);
  }
  startUpdate() {
    this.debounceUpdate = -1, this.pendingStart = !1;
    let { state: s } = this.view, t = s.field(lt);
    for (let e of t.active)
      e.isPending && !this.running.some((i) => i.active.source == e.source) && this.startQuery(e);
    this.running.length && t.open && t.open.disabled && (this.debounceAccept = setTimeout(() => this.accept(), this.view.state.facet(Z).updateSyncTime));
  }
  startQuery(s) {
    let { state: t } = this.view, e = we(t), i = new Ya(t, e, s.explicit, this.view), n = new _d(s, i);
    this.running.push(n), Promise.resolve(s.source(i)).then((r) => {
      n.context.aborted || (n.done = r || null, this.scheduleAccept());
    }, (r) => {
      this.view.dispatch({ effects: gi.of(null) }), ft(this.view.state, r);
    });
  }
  scheduleAccept() {
    this.running.every((s) => s.done !== void 0) ? this.accept() : this.debounceAccept < 0 && (this.debounceAccept = setTimeout(() => this.accept(), this.view.state.facet(Z).updateSyncTime));
  }
  // For each finished query in this.running, try to create a result
  // or, if appropriate, restart the query.
  accept() {
    var s;
    this.debounceAccept > -1 && clearTimeout(this.debounceAccept), this.debounceAccept = -1;
    let t = [], e = this.view.state.facet(Z), i = this.view.state.field(lt);
    for (let n = 0; n < this.running.length; n++) {
      let r = this.running[n];
      if (r.done === void 0)
        continue;
      if (this.running.splice(n--, 1), r.done) {
        let l = we(r.updates.length ? r.updates[0].startState : this.view.state), a = Math.min(l, r.done.from + (r.active.explicit ? 0 : 1)), h = new Le(r.active.source, r.active.explicit, a, r.done, r.done.from, (s = r.done.to) !== null && s !== void 0 ? s : l);
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
          let l = new xt(
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
    (t.length || i.open && i.open.disabled) && this.view.dispatch({ effects: kr.of(t) });
  }
}, {
  eventHandlers: {
    blur(s) {
      let t = this.view.state.field(lt, !1);
      if (t && t.tooltip && this.view.state.facet(Z).closeOnBlur) {
        let e = t.open && ha(this.view, t.open.tooltip);
        (!e || !e.dom.contains(s.relatedTarget)) && setTimeout(() => this.view.dispatch({ effects: gi.of(null) }), 10);
      }
    },
    compositionstart() {
      this.composing = 1;
    },
    compositionend() {
      this.composing == 3 && setTimeout(() => this.view.dispatch({ effects: xn.of(!1) }), 20), this.composing = 0;
    }
  }
}), Jd = typeof navigator == "object" && /* @__PURE__ */ /Win/.test(navigator.platform), tp = /* @__PURE__ */ An.highest(/* @__PURE__ */ M.domEventHandlers({
  keydown(s, t) {
    let e = t.state.field(lt, !1);
    if (!e || !e.open || e.open.disabled || e.open.selected < 0 || s.key.length > 1 || s.ctrlKey && !(Jd && s.altKey) || s.metaKey)
      return !1;
    let i = e.open.options[e.open.selected], n = e.active.find((o) => o.source == i.source), r = i.completion.commitCharacters || n.result.commitCharacters;
    return r && r.indexOf(s.key) > -1 && Sr(t, i), !1;
  }
})), ep = /* @__PURE__ */ M.baseTheme({
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
}), th = /* @__PURE__ */ new class extends se {
}();
th.startSide = 1;
th.endSide = -1;
function ip(s = {}) {
  return [
    tp,
    lt,
    Z.of(s),
    Zd,
    sp,
    ep
  ];
}
const np = [
  { key: "Ctrl-Space", run: ss },
  { mac: "Alt-`", run: ss },
  { mac: "Alt-i", run: ss },
  { key: "Escape", run: Kd },
  { key: "ArrowDown", run: /* @__PURE__ */ ji(!0) },
  { key: "ArrowUp", run: /* @__PURE__ */ ji(!1) },
  { key: "PageDown", run: /* @__PURE__ */ ji(!0, "page") },
  { key: "PageUp", run: /* @__PURE__ */ ji(!1, "page") },
  { key: "Enter", run: Ud }
], sp = /* @__PURE__ */ An.highest(/* @__PURE__ */ ar.computeN([Z], (s) => s.facet(Z).defaultKeymap ? [np] : []));
class zo {
  constructor(t, e, i) {
    this.from = t, this.to = e, this.diagnostic = i;
  }
}
class ge {
  constructor(t, e, i) {
    this.diagnostics = t, this.panel = e, this.selected = i;
  }
  static init(t, e, i) {
    let n = i.facet(mi).markerFilter;
    n && (t = n(t, i));
    let r = t.slice().sort((d, p) => d.from - p.from || d.to - p.to), o = new ai(), l = [], a = 0, h = i.doc.iter(), f = 0, c = i.doc.length;
    for (let d = 0; ; ) {
      let p = d == r.length ? null : r[d];
      if (!p && !l.length)
        break;
      let g, m;
      if (l.length)
        g = a, m = l.reduce((b, O) => Math.min(b, O.to), p && p.from > g ? p.from : 1e8);
      else {
        if (g = p.from, g > c)
          break;
        m = p.to, l.push(p), d++;
      }
      for (; d < r.length; ) {
        let b = r[d];
        if (b.from == g && (b.to > b.from || b.to == g))
          l.push(b), d++, m = Math.min(b.to, m);
        else {
          m = Math.min(b.from, m);
          break;
        }
      }
      m = Math.min(m, c);
      let y = !1;
      if (l.some((b) => b.from == g && (b.to == m || m == c)) && (y = g == m, !y && m - g < 10)) {
        let b = g - (f + h.value.length);
        b > 0 && (h.next(b), f = g);
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
      let w = gp(l);
      if (y)
        o.add(g, g, q.widget({
          widget: new cp(w),
          diagnostics: l.slice()
        }));
      else {
        let b = l.reduce((O, v) => v.markClass ? O + " " + v.markClass : O, "");
        o.add(g, m, q.mark({
          class: "cm-lintRange cm-lintRange-" + w + b,
          diagnostics: l.slice(),
          inclusiveEnd: l.some((O) => O.to > m)
        }));
      }
      if (a = m, a == c)
        break;
      for (let b = 0; b < l.length; b++)
        l[b].to <= a && l.splice(b--, 1);
    }
    let u = o.finish();
    return new ge(u, e, $e(u));
  }
}
function $e(s, t = null, e = 0) {
  let i = null;
  return s.between(e, 1e9, (n, r, { spec: o }) => {
    if (!(t && o.diagnostics.indexOf(t) < 0))
      if (!i)
        i = new zo(n, r, t || o.diagnostics[0]);
      else {
        if (o.diagnostics.indexOf(i.diagnostic) < 0)
          return !1;
        i = new zo(i.from, r, i.diagnostic);
      }
  }), i;
}
function rp(s, t) {
  let e = t.pos, i = t.end || e, n = s.state.facet(mi).hideOn(s, e, i);
  if (n != null)
    return n;
  let r = s.startState.doc.lineAt(t.pos);
  return !!(s.effects.some((o) => o.is(vr)) || s.changes.touchesRange(r.from, Math.max(r.to, i)));
}
function op(s, t) {
  return s.field(At, !1) ? t : t.concat(I.appendConfig.of(mp));
}
function lp(s, t) {
  return {
    effects: op(s, [vr.of(t)])
  };
}
const vr = /* @__PURE__ */ I.define(), eh = /* @__PURE__ */ I.define(), ih = /* @__PURE__ */ I.define(), At = /* @__PURE__ */ Pt.define({
  create() {
    return new ge(q.none, null, null);
  },
  update(s, t) {
    if (t.docChanged && s.diagnostics.size) {
      let e = s.diagnostics.map(t.changes), i = null, n = s.panel;
      if (s.selected) {
        let r = t.changes.mapPos(s.selected.from, 1);
        i = $e(e, s.selected.diagnostic, r) || $e(e, null, r);
      }
      !e.size && n && t.state.facet(mi).autoPanel && (n = null), s = new ge(e, n, i);
    }
    for (let e of t.effects)
      if (e.is(vr)) {
        let i = t.state.facet(mi).autoPanel ? e.value.length ? Sn.open : null : s.panel;
        s = ge.init(e.value, i, t.state);
      } else e.is(eh) ? s = new ge(s.diagnostics, e.value ? Sn.open : null, s.selected) : e.is(ih) && (s = new ge(s.diagnostics, s.panel, e.value));
    return s;
  },
  provide: (s) => [
    Ns.from(s, (t) => t.panel),
    M.decorations.from(s, (t) => t.diagnostics)
  ]
}), ap = /* @__PURE__ */ q.mark({ class: "cm-lintRange cm-lintRange-active" });
function hp(s, t, e) {
  let { diagnostics: i } = s.state.field(At), n, r = -1, o = -1;
  i.between(t - (e < 0 ? 1 : 0), t + (e > 0 ? 1 : 0), (a, h, { spec: f }) => {
    if (t >= a && t <= h && (a == h || (t > a || e > 0) && (t < h || e < 0)))
      return n = f.diagnostics, r = a, o = h, !1;
  });
  let l = s.state.facet(mi).tooltipFilter;
  return n && l && (n = l(n, s.state)), n ? {
    pos: r,
    end: o,
    above: s.state.doc.lineAt(r).to < o,
    create() {
      return { dom: fp(s, n) };
    }
  } : null;
}
function fp(s, t) {
  return Vt("ul", { class: "cm-tooltip-lint" }, t.map((e) => sh(s, e, !1)));
}
const Qo = (s) => {
  let t = s.state.field(At, !1);
  return !t || !t.panel ? !1 : (s.dispatch({ effects: eh.of(!1) }), !0);
}, mi = /* @__PURE__ */ P.define({
  combine(s) {
    return {
      sources: s.map((t) => t.source).filter((t) => t != null),
      ...Ks(s.map((t) => t.config), {
        delay: 750,
        markerFilter: null,
        tooltipFilter: null,
        needsRefresh: null,
        hideOn: () => null
      }, {
        delay: Math.max,
        markerFilter: $o,
        tooltipFilter: $o,
        needsRefresh: (t, e) => t ? e ? (i) => t(i) || e(i) : t : e,
        hideOn: (t, e) => t ? e ? (i, n, r) => t(i, n, r) || e(i, n, r) : t : e,
        autoPanel: (t, e) => t || e
      })
    };
  }
});
function $o(s, t) {
  return s ? t ? (e, i) => t(s(e, i), i) : s : t;
}
function nh(s) {
  let t = [];
  if (s)
    t: for (let { name: e } of s) {
      for (let i = 0; i < e.length; i++) {
        let n = e[i];
        if (/[a-zA-Z]/.test(n) && !t.some((r) => r.toLowerCase() == n.toLowerCase())) {
          t.push(n);
          continue t;
        }
      }
      t.push("");
    }
  return t;
}
function sh(s, t, e) {
  var i;
  let n = e ? nh(t.actions) : [];
  return Vt("li", { class: "cm-diagnostic cm-diagnostic-" + t.severity }, Vt("span", { class: "cm-diagnosticText" }, t.renderMessage ? t.renderMessage(s) : t.message), (i = t.actions) === null || i === void 0 ? void 0 : i.map((r, o) => {
    let l = !1, a = (d) => {
      if (d.preventDefault(), l)
        return;
      l = !0;
      let p = $e(s.state.field(At).diagnostics, t);
      p && r.apply(s, p.from, p.to);
    }, { name: h } = r, f = n[o] ? h.indexOf(n[o]) : -1, c = f < 0 ? h : [
      h.slice(0, f),
      Vt("u", h.slice(f, f + 1)),
      h.slice(f + 1)
    ], u = r.markClass ? " " + r.markClass : "";
    return Vt("button", {
      type: "button",
      class: "cm-diagnosticAction" + u,
      onclick: a,
      onmousedown: a,
      "aria-label": ` Action: ${h}${f < 0 ? "" : ` (access key "${n[o]})"`}.`
    }, c);
  }), t.source && Vt("div", { class: "cm-diagnosticSource" }, t.source));
}
class cp extends bi {
  constructor(t) {
    super(), this.sev = t;
  }
  eq(t) {
    return t.sev == this.sev;
  }
  toDOM() {
    return Vt("span", { class: "cm-lintPoint cm-lintPoint-" + this.sev });
  }
}
class jo {
  constructor(t, e) {
    this.diagnostic = e, this.id = "item_" + Math.floor(Math.random() * 4294967295).toString(16), this.dom = sh(t, e, !0), this.dom.id = this.id, this.dom.setAttribute("role", "option");
  }
}
class Sn {
  constructor(t) {
    this.view = t, this.items = [];
    let e = (n) => {
      if (!(n.ctrlKey || n.altKey || n.metaKey)) {
        if (n.keyCode == 27)
          Qo(this.view), this.view.focus();
        else if (n.keyCode == 38 || n.keyCode == 33)
          this.moveSelection((this.selectedIndex - 1 + this.items.length) % this.items.length);
        else if (n.keyCode == 40 || n.keyCode == 34)
          this.moveSelection((this.selectedIndex + 1) % this.items.length);
        else if (n.keyCode == 36)
          this.moveSelection(0);
        else if (n.keyCode == 35)
          this.moveSelection(this.items.length - 1);
        else if (n.keyCode == 13)
          this.view.focus();
        else if (n.keyCode >= 65 && n.keyCode <= 90 && this.selectedIndex >= 0) {
          let { diagnostic: r } = this.items[this.selectedIndex], o = nh(r.actions);
          for (let l = 0; l < o.length; l++)
            if (o[l].toUpperCase().charCodeAt(0) == n.keyCode) {
              let a = $e(this.view.state.field(At).diagnostics, r);
              a && r.actions[l].apply(t, a.from, a.to);
            }
        } else
          return;
        n.preventDefault();
      }
    }, i = (n) => {
      for (let r = 0; r < this.items.length; r++)
        this.items[r].dom.contains(n.target) && this.moveSelection(r);
    };
    this.list = Vt("ul", {
      tabIndex: 0,
      role: "listbox",
      "aria-label": this.view.state.phrase("Diagnostics"),
      onkeydown: e,
      onclick: i
    }), this.dom = Vt("div", { class: "cm-panel-lint" }, this.list, Vt("button", {
      type: "button",
      name: "close",
      "aria-label": this.view.state.phrase("close"),
      onclick: () => Qo(this.view)
    }, "×")), this.update();
  }
  get selectedIndex() {
    let t = this.view.state.field(At).selected;
    if (!t)
      return -1;
    for (let e = 0; e < this.items.length; e++)
      if (this.items[e].diagnostic == t.diagnostic)
        return e;
    return -1;
  }
  update() {
    let { diagnostics: t, selected: e } = this.view.state.field(At), i = 0, n = !1, r = null, o = /* @__PURE__ */ new Set();
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
        c < 0 ? (u = new jo(this.view, f), this.items.splice(i, 0, u), n = !0) : (u = this.items[c], c > i && (this.items.splice(i, c - i), n = !0)), e && u.diagnostic == e.diagnostic ? u.dom.hasAttribute("aria-selected") || (u.dom.setAttribute("aria-selected", "true"), r = u) : u.dom.hasAttribute("aria-selected") && u.dom.removeAttribute("aria-selected"), i++;
      }
    }); i < this.items.length && !(this.items.length == 1 && this.items[0].diagnostic.from < 0); )
      n = !0, this.items.pop();
    this.items.length == 0 && (this.items.push(new jo(this.view, {
      from: -1,
      to: -1,
      severity: "info",
      message: this.view.state.phrase("No diagnostics")
    })), n = !0), r ? (this.list.setAttribute("aria-activedescendant", r.id), this.view.requestMeasure({
      key: this,
      read: () => ({ sel: r.dom.getBoundingClientRect(), panel: this.list.getBoundingClientRect() }),
      write: ({ sel: l, panel: a }) => {
        let h = a.height / this.list.offsetHeight;
        l.top < a.top ? this.list.scrollTop -= (a.top - l.top) / h : l.bottom > a.bottom && (this.list.scrollTop += (l.bottom - a.bottom) / h);
      }
    })) : this.selectedIndex < 0 && this.list.removeAttribute("aria-activedescendant"), n && this.sync();
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
    let e = this.view.state.field(At), i = $e(e.diagnostics, this.items[t].diagnostic);
    i && this.view.dispatch({
      selection: { anchor: i.from, head: i.to },
      scrollIntoView: !0,
      effects: ih.of(i)
    });
  }
  static open(t) {
    return new Sn(t);
  }
}
function up(s, t = 'viewBox="0 0 40 40"') {
  return `url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" ${t}>${encodeURIComponent(s)}</svg>')`;
}
function Xi(s) {
  return up(`<path d="m0 2.5 l2 -1.5 l1 0 l2 1.5 l1 0" stroke="${s}" fill="none" stroke-width=".7"/>`, 'width="6" height="3"');
}
const dp = /* @__PURE__ */ M.baseTheme({
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
  ".cm-lintRange-error": { backgroundImage: /* @__PURE__ */ Xi("#d11") },
  ".cm-lintRange-warning": { backgroundImage: /* @__PURE__ */ Xi("orange") },
  ".cm-lintRange-info": { backgroundImage: /* @__PURE__ */ Xi("#999") },
  ".cm-lintRange-hint": { backgroundImage: /* @__PURE__ */ Xi("#66d") },
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
function pp(s) {
  return s == "error" ? 4 : s == "warning" ? 3 : s == "info" ? 2 : 1;
}
function gp(s) {
  let t = "hint", e = 1;
  for (let i of s) {
    let n = pp(i.severity);
    n > e && (e = n, t = i.severity);
  }
  return t;
}
const mp = [
  At,
  /* @__PURE__ */ M.decorations.compute([At], (s) => {
    let { selected: t, panel: e } = s.field(At);
    return !t || !e || t.from == t.to ? q.none : q.set([
      ap.range(t.from, t.to)
    ]);
  }),
  /* @__PURE__ */ Rc(hp, { hideOn: rp }),
  dp
];
class vn {
  /**
  @internal
  */
  constructor(t, e, i, n, r, o, l, a, h, f = 0, c) {
    this.p = t, this.stack = e, this.state = i, this.reducePos = n, this.pos = r, this.score = o, this.buffer = l, this.bufferBase = a, this.curContext = h, this.lookAhead = f, this.parent = c;
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
    let n = t.parser.context;
    return new vn(t, [], e, i, i, 0, [], 0, n ? new Xo(n, n.start) : null, 0, null);
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
    let i = t >> 19, n = t & 65535, { parser: r } = this.p, o = this.reducePos < this.pos - 25 && this.setLookAhead(this.pos), l = r.dynamicPrecedence(n);
    if (l && (this.score += l), i == 0) {
      this.pushState(r.getGoto(this.state, n, !0), this.reducePos), n < r.minRepeatTerm && this.storeNode(n, this.reducePos, this.reducePos, o ? 8 : 4, !0), this.reduceContext(n, this.reducePos);
      return;
    }
    let a = this.stack.length - (i - 1) * 3 - (t & 262144 ? 6 : 0), h = a ? this.stack[a - 2] : this.p.ranges[0].from, f = this.reducePos - h;
    f >= 2e3 && !(!((e = this.p.parser.nodeSet.types[n]) === null || e === void 0) && e.isAnonymous) && (h == this.p.lastBigReductionStart ? (this.p.bigReductionCount++, this.p.lastBigReductionSize = f) : this.p.lastBigReductionSize < f && (this.p.bigReductionCount = 1, this.p.lastBigReductionStart = h, this.p.lastBigReductionSize = f));
    let c = a ? this.stack[a - 1] : 0, u = this.bufferBase + this.buffer.length - c;
    if (n < r.minRepeatTerm || t & 131072) {
      let d = r.stateFlag(
        this.state,
        1
        /* StateFlag.Skipped */
      ) ? this.pos : this.reducePos;
      this.storeNode(n, h, d, u + 4, !0);
    }
    if (t & 262144)
      this.state = this.stack[a];
    else {
      let d = this.stack[a - 3];
      this.state = r.getGoto(d, n, !0);
    }
    for (; this.stack.length > a; )
      this.stack.pop();
    this.reduceContext(n, h);
  }
  // Shift a value into the buffer
  /**
  @internal
  */
  storeNode(t, e, i, n = 4, r = !1) {
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
      this.buffer.push(t, e, i, n);
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
            this.buffer[o] = this.buffer[o - 4], this.buffer[o + 1] = this.buffer[o - 3], this.buffer[o + 2] = this.buffer[o - 2], this.buffer[o + 3] = this.buffer[o - 1], o -= 4, n > 4 && (n -= 4);
      }
      this.buffer[o] = t, this.buffer[o + 1] = e, this.buffer[o + 2] = i, this.buffer[o + 3] = n;
    }
  }
  // Apply a shift action
  /**
  @internal
  */
  shift(t, e, i, n) {
    if (t & 131072)
      this.pushState(t & 65535, this.pos);
    else if ((t & 262144) == 0) {
      let r = t, { parser: o } = this.p;
      this.pos = n;
      let l = o.stateFlag(
        r,
        1
        /* StateFlag.Skipped */
      );
      !l && (n > i || e <= o.maxNode) && (this.reducePos = n), this.pushState(r, l ? i : Math.min(i, this.reducePos)), this.shiftContext(e, i), e <= o.maxNode && this.buffer.push(e, i, n, 4);
    } else
      this.pos = n, this.shiftContext(e, i), e <= this.p.parser.maxNode && this.buffer.push(e, i, n, 4);
  }
  // Apply an action
  /**
  @internal
  */
  apply(t, e, i, n) {
    t & 65536 ? this.reduce(t) : this.shift(t, e, i, n);
  }
  // Add a prebuilt (reused) node into the buffer.
  /**
  @internal
  */
  useNode(t, e) {
    let i = this.p.reused.length - 1;
    (i < 0 || this.p.reused[i] != t) && (this.p.reused.push(t), i++);
    let n = this.pos;
    this.reducePos = this.pos = n + t.length, this.pushState(e, n), this.buffer.push(
      i,
      n,
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
    let i = t.buffer.slice(e), n = t.bufferBase + e;
    for (; t && n == t.bufferBase; )
      t = t.parent;
    return new vn(this.p, this.stack.slice(), this.state, this.reducePos, this.pos, this.score, i, n, this.curContext, this.lookAhead, t);
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
    for (let e = new yp(this); ; ) {
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
      let n = [];
      for (let r = 0, o; r < e.length; r += 2)
        (o = e[r + 1]) != this.state && this.p.parser.hasAction(o, t) && n.push(e[r], o);
      if (this.stack.length < 120)
        for (let r = 0; n.length < 8 && r < e.length; r += 2) {
          let o = e[r + 1];
          n.some((l, a) => a & 1 && l == o) || n.push(e[r], o);
        }
      e = n;
    }
    let i = [];
    for (let n = 0; n < e.length && i.length < 4; n += 2) {
      let r = e[n + 1];
      if (r == this.state)
        continue;
      let o = this.split();
      o.pushState(r, this.pos), o.storeNode(0, o.pos, o.pos, 4, !0), o.shiftContext(e[n], this.pos), o.reducePos = this.pos, o.score -= 200, i.push(o);
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
      let i = e >> 19, n = e & 65535, r = this.stack.length - i * 3;
      if (r < 0 || t.getGoto(this.stack[r], n, !1) < 0) {
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
    let { parser: t } = this.p, e = [], i = (n, r) => {
      if (!e.includes(n))
        return e.push(n), t.allActions(n, (o) => {
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
      let e = new Xo(this.curContext.tracker, t);
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
class Xo {
  constructor(t, e) {
    this.tracker = t, this.context = e, this.hash = t.strict ? t.hash(e) : 0;
  }
}
class yp {
  constructor(t) {
    this.start = t, this.state = t.state, this.stack = t.stack, this.base = this.stack.length;
  }
  reduce(t) {
    let e = t & 65535, i = t >> 19;
    i == 0 ? (this.stack == this.start.stack && (this.stack = this.stack.slice()), this.stack.push(this.state, 0, 0), this.base += 3) : this.base -= (i - 1) * 3;
    let n = this.start.p.parser.getGoto(this.stack[this.base - 3], e, !0);
    this.state = n;
  }
}
class On {
  constructor(t, e, i) {
    this.stack = t, this.pos = e, this.index = i, this.buffer = t.buffer, this.index == 0 && this.maybeNext();
  }
  static create(t, e = t.bufferBase + t.buffer.length) {
    return new On(t, e, e - t.bufferBase);
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
    return new On(this.stack, this.pos, this.index);
  }
}
function qi(s, t = Uint16Array) {
  if (typeof s != "string")
    return s;
  let e = null;
  for (let i = 0, n = 0; i < s.length; ) {
    let r = 0;
    for (; ; ) {
      let o = s.charCodeAt(i++), l = !1;
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
    e ? e[n++] = r : e = new t(r);
  }
  return e;
}
class Ji {
  constructor() {
    this.start = -1, this.value = -1, this.end = -1, this.extended = -1, this.lookAhead = 0, this.mask = 0, this.context = 0;
  }
}
const qo = new Ji();
class bp {
  /**
  @internal
  */
  constructor(t, e) {
    this.input = t, this.ranges = e, this.chunk = "", this.chunkOff = 0, this.chunk2 = "", this.chunk2Pos = 0, this.next = -1, this.token = qo, this.rangeIndex = 0, this.pos = this.chunkPos = e[0].from, this.range = e[0], this.end = e[e.length - 1].to, this.readNext();
  }
  /**
  @internal
  */
  resolveOffset(t, e) {
    let i = this.range, n = this.rangeIndex, r = this.pos + t;
    for (; r < i.from; ) {
      if (!n)
        return null;
      let o = this.ranges[--n];
      r -= i.from - o.to, i = o;
    }
    for (; e < 0 ? r > i.to : r >= i.to; ) {
      if (n == this.ranges.length - 1)
        return null;
      let o = this.ranges[++n];
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
    let e = this.chunkOff + t, i, n;
    if (e >= 0 && e < this.chunk.length)
      i = this.pos + t, n = this.chunk.charCodeAt(e);
    else {
      let r = this.resolveOffset(t, 1);
      if (r == null)
        return -1;
      if (i = r, i >= this.chunk2Pos && i < this.chunk2Pos + this.chunk2.length)
        n = this.chunk2.charCodeAt(i - this.chunk2Pos);
      else {
        let o = this.rangeIndex, l = this.range;
        for (; l.to <= i; )
          l = this.ranges[++o];
        this.chunk2 = this.input.chunk(this.chunk2Pos = i), i + this.chunk2.length > l.to && (this.chunk2 = this.chunk2.slice(0, l.to - i)), n = this.chunk2.charCodeAt(0);
      }
    }
    return i >= this.token.lookAhead && (this.token.lookAhead = i + 1), n;
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
    if (e ? (this.token = e, e.start = t, e.lookAhead = t + 1, e.value = e.extended = -1) : this.token = qo, this.pos != t) {
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
    for (let n of this.ranges) {
      if (n.from >= e)
        break;
      n.to > t && (i += this.input.read(Math.max(n.from, t), Math.min(n.to, e)));
    }
    return i;
  }
}
class Ee {
  constructor(t, e) {
    this.data = t, this.id = e;
  }
  token(t, e) {
    let { parser: i } = e.p;
    wp(this.data, t, e, this.id, i.data, i.tokenPrecTable);
  }
}
Ee.prototype.contextual = Ee.prototype.fallback = Ee.prototype.extend = !1;
Ee.prototype.fallback = Ee.prototype.extend = !1;
function wp(s, t, e, i, n, r) {
  let o = 0, l = 1 << i, { dialect: a } = e.p.parser;
  t: for (; (l & s[o]) != 0; ) {
    let h = s[o + 1];
    for (let d = o + 3; d < h; d += 2)
      if ((s[d + 1] & l) > 0) {
        let p = s[d];
        if (a.allows(p) && (t.token.value == -1 || t.token.value == p || xp(p, t.token.value, n, r))) {
          t.acceptToken(p);
          break;
        }
      }
    let f = t.next, c = 0, u = s[o + 2];
    if (t.next < 0 && u > c && s[h + u * 3 - 3] == 65535) {
      o = s[h + u * 3 - 1];
      continue t;
    }
    for (; c < u; ) {
      let d = c + u >> 1, p = h + d + (d << 1), g = s[p], m = s[p + 1] || 65536;
      if (f < g)
        u = d;
      else if (f >= m)
        c = d + 1;
      else {
        o = s[p + 2], t.advance();
        continue t;
      }
    }
    break;
  }
}
function Uo(s, t, e) {
  for (let i = t, n; (n = s[i]) != 65535; i++)
    if (n == e)
      return i - t;
  return -1;
}
function xp(s, t, e, i) {
  let n = Uo(e, i, t);
  return n < 0 || Uo(e, i, s) < n;
}
const pt = typeof process < "u" && process.env && /\bparse\b/.test(process.env.LOG);
let rs = null;
function Ko(s, t, e) {
  let i = s.cursor(X.IncludeAnonymous);
  for (i.moveTo(t); ; )
    if (!(e < 0 ? i.childBefore(t) : i.childAfter(t)))
      for (; ; ) {
        if ((e < 0 ? i.to < t : i.from > t) && !i.type.isError)
          return e < 0 ? Math.max(0, Math.min(
            i.to - 1,
            t - 25
            /* Lookahead.Margin */
          )) : Math.min(s.length, Math.max(
            i.from + 1,
            t + 25
            /* Lookahead.Margin */
          ));
        if (e < 0 ? i.prevSibling() : i.nextSibling())
          break;
        if (!i.parent())
          return e < 0 ? 0 : s.length;
      }
}
class kp {
  constructor(t, e) {
    this.fragments = t, this.nodeSet = e, this.i = 0, this.fragment = null, this.safeFrom = -1, this.safeTo = -1, this.trees = [], this.start = [], this.index = [], this.nextFragment();
  }
  nextFragment() {
    let t = this.fragment = this.i == this.fragments.length ? null : this.fragments[this.i++];
    if (t) {
      for (this.safeFrom = t.openStart ? Ko(t.tree, t.from + t.offset, 1) - t.offset : t.from, this.safeTo = t.openEnd ? Ko(t.tree, t.to + t.offset, -1) - t.offset : t.to; this.trees.length; )
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
      let i = this.trees[e], n = this.index[e];
      if (n == i.children.length) {
        this.trees.pop(), this.start.pop(), this.index.pop();
        continue;
      }
      let r = i.children[n], o = this.start[e] + i.positions[n];
      if (o > t)
        return this.nextStart = o, null;
      if (r instanceof _) {
        if (o == t) {
          if (o < this.safeFrom)
            return null;
          let l = o + r.length;
          if (l <= this.safeTo) {
            let a = r.prop(R.lookAhead);
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
class Sp {
  constructor(t, e) {
    this.stream = e, this.tokens = [], this.mainToken = null, this.actions = [], this.tokens = t.tokenizers.map((i) => new Ji());
  }
  getActions(t) {
    let e = 0, i = null, { parser: n } = t.p, { tokenizers: r } = n, o = n.stateSlot(
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
    return a && t.setLookAhead(a), !i && t.pos == this.stream.end && (i = new Ji(), i.value = t.p.parser.eofTerm, i.start = i.end = t.pos, e = this.addActions(t, i.value, i.end, e)), this.mainToken = i, this.actions;
  }
  getMainToken(t) {
    if (this.mainToken)
      return this.mainToken;
    let e = new Ji(), { pos: i, p: n } = t;
    return e.start = i, e.end = Math.min(i + 1, n.stream.end), e.value = i == n.stream.end ? n.parser.eofTerm : 0, e;
  }
  updateCachedToken(t, e, i) {
    let n = this.stream.clipPos(i.pos);
    if (e.token(this.stream.reset(n, t), i), t.value > -1) {
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
      t.value = 0, t.end = this.stream.clipPos(n + 1);
  }
  putAction(t, e, i, n) {
    for (let r = 0; r < n; r += 3)
      if (this.actions[r] == t)
        return n;
    return this.actions[n++] = t, this.actions[n++] = e, this.actions[n++] = i, n;
  }
  addActions(t, e, i, n) {
    let { state: r } = t, { parser: o } = t.p, { data: l } = o;
    for (let a = 0; a < 2; a++)
      for (let h = o.stateSlot(
        r,
        a ? 2 : 1
        /* ParseState.Actions */
      ); ; h += 3) {
        if (l[h] == 65535)
          if (l[h + 1] == 1)
            h = Xt(l, h + 2);
          else {
            n == 0 && l[h + 1] == 2 && (n = this.putAction(Xt(l, h + 2), e, i, n));
            break;
          }
        l[h] == e && (n = this.putAction(Xt(l, h + 1), e, i, n));
      }
    return n;
  }
}
class vp {
  constructor(t, e, i, n) {
    this.parser = t, this.input = e, this.ranges = n, this.recovering = 0, this.nextStackID = 9812, this.minStackPos = 0, this.reused = [], this.stoppedAt = null, this.lastBigReductionStart = -1, this.lastBigReductionSize = 0, this.bigReductionCount = 0, this.stream = new bp(e, n), this.tokens = new Sp(t, this.stream), this.topTerm = t.top[1];
    let { from: r } = n[0];
    this.stacks = [vn.start(this, t.top[0], r)], this.fragments = i.length && this.stream.end - r > t.bufferLength * 4 ? new kp(i, t.nodeSet) : null;
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
    let t = this.stacks, e = this.minStackPos, i = this.stacks = [], n, r;
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
            n || (n = [], r = []), n.push(l);
            let a = this.tokens.getMainToken(l);
            r.push(a.value, a.end);
          }
        }
        break;
      }
    }
    if (!i.length) {
      let o = n && Cp(n);
      if (o)
        return pt && console.log("Finish with " + this.stackID(o)), this.stackToTree(o);
      if (this.parser.strict)
        throw pt && n && console.log("Stuck with token " + (this.tokens.mainToken ? this.parser.getName(this.tokens.mainToken.value) : "none")), new SyntaxError("No parse at " + e);
      this.recovering || (this.recovering = 5);
    }
    if (this.recovering && n) {
      let o = this.stoppedAt != null && n[0].pos > this.stoppedAt ? n[0] : this.runRecovery(n, r, i);
      if (o)
        return pt && console.log("Force-finish " + this.stackID(o)), this.stackToTree(o.forceAll());
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
    let n = t.pos, { parser: r } = this, o = pt ? this.stackID(t) + " -> " : "";
    if (this.stoppedAt != null && n > this.stoppedAt)
      return t.forceReduce() ? t : null;
    if (this.fragments) {
      let h = t.curContext && t.curContext.tracker.strict, f = h ? t.curContext.hash : 0;
      for (let c = this.fragments.nodeAt(n); c; ) {
        let u = this.parser.nodeSet.types[c.type.id] == c.type ? r.getGoto(t.state, c.type.id) : -1;
        if (u > -1 && c.length && (!h || (c.prop(R.contextHash) || 0) == f))
          return t.useNode(c, u), pt && console.log(o + this.stackID(t) + ` (via reuse of ${r.getName(c.type.id)})`), !0;
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
      return t.reduce(l), pt && console.log(o + this.stackID(t) + ` (via always-reduce ${r.getName(
        l & 65535
        /* Action.ValueMask */
      )})`), !0;
    if (t.stack.length >= 8400)
      for (; t.stack.length > 6e3 && t.forceReduce(); )
        ;
    let a = this.tokens.getActions(t);
    for (let h = 0; h < a.length; ) {
      let f = a[h++], c = a[h++], u = a[h++], d = h == a.length || !i, p = d ? t : t.split(), g = this.tokens.mainToken;
      if (p.apply(f, c, g ? g.start : p.pos, u), pt && console.log(o + this.stackID(p) + ` (via ${(f & 65536) == 0 ? "shift" : `reduce of ${r.getName(
        f & 65535
        /* Action.ValueMask */
      )}`} for ${r.getName(c)} @ ${n}${p == t ? "" : ", split"})`), d)
        return !0;
      p.pos > n ? e.push(p) : i.push(p);
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
        return _o(t, e), !0;
    }
  }
  runRecovery(t, e, i) {
    let n = null, r = !1;
    for (let o = 0; o < t.length; o++) {
      let l = t[o], a = e[o << 1], h = e[(o << 1) + 1], f = pt ? this.stackID(l) + " -> " : "";
      if (l.deadEnd && (r || (r = !0, l.restart(), pt && console.log(f + this.stackID(l) + " (restarted)"), this.advanceFully(l, i))))
        continue;
      let c = l.split(), u = f;
      for (let d = 0; d < 10 && c.forceReduce() && (pt && console.log(u + this.stackID(c) + " (via force-reduce)"), !this.advanceFully(c, i)); d++)
        pt && (u = this.stackID(c) + " -> ");
      for (let d of l.recoverByInsert(a))
        pt && console.log(f + this.stackID(d) + " (via recover-insert)"), this.advanceFully(d, i);
      this.stream.end > l.pos ? (h == l.pos && (h++, a = 0), l.recoverByDelete(a, h), pt && console.log(f + this.stackID(l) + ` (via recover-delete ${this.parser.getName(a)})`), _o(l, i)) : (!n || n.score < c.score) && (n = c);
    }
    return n;
  }
  // Convert the stack's buffer to a syntax tree.
  stackToTree(t) {
    return t.close(), _.build({
      buffer: On.create(t),
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
    let e = (rs || (rs = /* @__PURE__ */ new WeakMap())).get(t);
    return e || rs.set(t, e = String.fromCodePoint(this.nextStackID++)), e + t;
  }
}
function _o(s, t) {
  for (let e = 0; e < t.length; e++) {
    let i = t[e];
    if (i.pos == s.pos && i.sameState(s)) {
      t[e].score < s.score && (t[e] = s);
      return;
    }
  }
  t.push(s);
}
class Op {
  constructor(t, e, i) {
    this.source = t, this.flags = e, this.disabled = i;
  }
  allows(t) {
    return !this.disabled || this.disabled[t] == 0;
  }
}
class Cn extends pa {
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
    let i = Object.keys(t.topRules).map((l) => t.topRules[l][1]), n = [];
    for (let l = 0; l < e.length; l++)
      n.push([]);
    function r(l, a, h) {
      n[l].push([a, a.deserialize(String(h))]);
    }
    if (t.nodeProps)
      for (let l of t.nodeProps) {
        let a = l[0];
        typeof a == "string" && (a = R[a]);
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
    this.nodeSet = new cr(e.map((l, a) => dt.define({
      name: a >= this.minRepeatTerm ? void 0 : l,
      id: a,
      props: n[a],
      top: i.indexOf(a) > -1,
      error: a == 0,
      skipped: t.skippedNodes && t.skippedNodes.indexOf(a) > -1
    }))), t.propSources && (this.nodeSet = this.nodeSet.extend(...t.propSources)), this.strict = !1, this.bufferLength = fa;
    let o = qi(t.tokenData);
    this.context = t.context, this.specializerSpecs = t.specialized || [], this.specialized = new Uint16Array(this.specializerSpecs.length);
    for (let l = 0; l < this.specializerSpecs.length; l++)
      this.specialized[l] = this.specializerSpecs[l].term;
    this.specializers = this.specializerSpecs.map(Yo), this.states = qi(t.states, Uint32Array), this.data = qi(t.stateData), this.goto = qi(t.goto), this.maxTerm = t.maxTerm, this.tokenizers = t.tokenizers.map((l) => typeof l == "number" ? new Ee(o, l) : l), this.topRules = t.topRules, this.dialects = t.dialects || {}, this.dynamicPrecedences = t.dynamicPrecedences || null, this.tokenPrecTable = t.tokenPrec, this.termNames = t.termNames || null, this.maxNode = this.nodeSet.types.length - 1, this.dialect = this.parseDialect(), this.top = this.topRules[Object.keys(this.topRules)[0]];
  }
  createParse(t, e, i) {
    let n = new vp(this, t, e, i);
    for (let r of this.wrappers)
      n = r(n, t, e, i);
    return n;
  }
  /**
  Get a goto table entry @internal
  */
  getGoto(t, e, i = !1) {
    let n = this.goto;
    if (e >= n[0])
      return -1;
    for (let r = n[e + 1]; ; ) {
      let o = n[r++], l = o & 1, a = n[r++];
      if (l && i)
        return a;
      for (let h = r + (o >> 1); r < h; r++)
        if (n[r] == t)
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
    for (let n = 0; n < 2; n++)
      for (let r = this.stateSlot(
        t,
        n ? 2 : 1
        /* ParseState.Actions */
      ), o; ; r += 3) {
        if ((o = i[r]) == 65535)
          if (i[r + 1] == 1)
            o = i[r = Xt(i, r + 2)];
          else {
            if (i[r + 1] == 2)
              return Xt(i, r + 2);
            break;
          }
        if (o == e || o == 0)
          return Xt(i, r + 1);
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
    ), n = i ? e(i) : void 0;
    for (let r = this.stateSlot(
      t,
      1
      /* ParseState.Actions */
    ); n == null; r += 3) {
      if (this.data[r] == 65535)
        if (this.data[r + 1] == 1)
          r = Xt(this.data, r + 2);
        else
          break;
      n = e(Xt(this.data, r + 1));
    }
    return n;
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
          i = Xt(this.data, i + 2);
        else
          break;
      if ((this.data[i + 2] & 1) == 0) {
        let n = this.data[i + 1];
        e.some((r, o) => o & 1 && r == n) || e.push(this.data[i], n);
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
    let e = Object.assign(Object.create(Cn.prototype), this);
    if (t.props && (e.nodeSet = this.nodeSet.extend(...t.props)), t.top) {
      let i = this.topRules[t.top];
      if (!i)
        throw new RangeError(`Invalid top rule name ${t.top}`);
      e.top = i;
    }
    return t.tokenizers && (e.tokenizers = this.tokenizers.map((i) => {
      let n = t.tokenizers.find((r) => r.from == i);
      return n ? n.to : i;
    })), t.specializers && (e.specializers = this.specializers.slice(), e.specializerSpecs = this.specializerSpecs.map((i, n) => {
      let r = t.specializers.find((l) => l.from == i.external);
      if (!r)
        return i;
      let o = Object.assign(Object.assign({}, i), { external: r.to });
      return e.specializers[n] = Yo(o), o;
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
    let n = null;
    for (let r = 0; r < e.length; r++)
      if (!i[r])
        for (let o = this.dialects[e[r]], l; (l = this.data[o++]) != 65535; )
          (n || (n = new Uint8Array(this.maxTerm + 1)))[l] = 1;
    return new Op(t, i, n);
  }
  /**
  Used by the output of the parser generator. Not available to
  user code. @hide
  */
  static deserialize(t) {
    return new Cn(t);
  }
}
function Xt(s, t) {
  return s[t] | s[t + 1] << 16;
}
function Cp(s) {
  let t = null;
  for (let e of s) {
    let i = e.p.stoppedAt;
    (e.pos == e.p.stream.end || i != null && e.pos > i) && e.p.parser.stateFlag(
      e.state,
      2
      /* StateFlag.Accepting */
    ) && (!t || t.score < e.score) && (t = e);
  }
  return t;
}
function Yo(s) {
  if (s.external) {
    let t = s.extend ? 1 : 0;
    return (e, i) => s.external(e, i) << 1 | t;
  }
  return s.get;
}
const rh = ga({
  Number: C.number,
  String: C.string,
  Boolean: C.bool,
  Null: C.null,
  Variable: C.variableName,
  ModelConstant: C.typeName,
  ResourceKey: C.special(C.string),
  LanguageTag: C.annotation,
  LineComment: C.lineComment,
  BlockComment: C.blockComment,
  identifier: C.variableName,
  "FunctionCall/identifier": C.function(C.variableName),
  "MethodAccess/identifier": C.function(C.propertyName),
  Arrow: C.punctuation,
  '"(" ")" "[" "]" "{" "}"': C.paren,
  '"." ".."': C.derefOperator,
  "orOp andOp": C.logicOperator,
  compareOp: C.compareOperator,
  "Plus Minus multiplicativeOp": C.arithmeticOperator,
  NotOp: C.logicOperator,
  '"switch" "default" "tuple"': C.keyword,
  '"?" ":"': C.punctuation
}), Ap = { __proto__: null, true: 40, false: 42, null: 46, tuple: 62, switch: 68, default: 74 }, Tp = Cn.deserialize({
  version: 14,
  states: "3OO]QPOOO!aQQO'#DWO!iQPO'#CwO!nQPO'#DSO]QPO'#DVOOQO'#Cl'#ClO$yQSO'#CkO%QQPO'#ChOOQO'#Ch'#ChO&|QSO'#CgO'xQSO'#CfO(VQQO'#CeO(zQPO'#CdO)lQPO'#CcO*ZQPO'#CbOOQO'#Dk'#DkQOQPOOOOQO'#Co'#CoOOQO'#Cr'#CrO*uQPO'#CzO*zQPO'#C}O]QPO,58zO+SQPO'#DXOOQO,59r,59rO+ZQPO'#CxO+`QPO'#DnO+hQPO,59cO+mQWO'#DWOOQO'#DU'#DUO+xQPO'#DTO,QQPO,59nO,VQPO,59qO,[QPO'#DYO,aQPO'#DZO]QPO'#D[OOQO'#Dc'#DcO-mQSO,59VO!dQPO'#DWOOQO,59S,59SO%QQPO'#DdO.lQSO,59RO%QQPO'#DeO/hQSO,59QO/uQPO,59PO%QQPO'#DfO0yQPO,59OO%QQPO'#DgO1kQPO,58}O]QPO,58|O2YQPO,59fO2_QPO,59iO]QPO,59iOOQO1G.f1G.fO2iQPO'#DxOOQO,59s,59sO2qQPO,59sOOQO,59d,59dO!iQPO'#D^O2vQPO,5:YOOQO1G.}1G.}O]QPO,59pO3OQPO,59oO3VQPO,59oOOQO1G/Y1G/YOOQO1G/]1G/]OOQO,59t,59tOOQO,59u,59uO3_QPO,59vOOQO-E7a-E7aOOQO,5:O,5:OOOQO-E7b-E7bOOQO,5:P,5:POOQO-E7c-E7cO#rQPO'#CkO3dQPO'#CgO3kQPO'#CfOOQO1G.k1G.kOOQO,5:Q,5:QOOQO-E7d-E7dOOQO,5:R,5:ROOQO-E7e-E7eO3uQPO1G.hO3zQQO'#C|O4PQPO'#DrO4XQPO1G/QO4^QPO'#DPOOQO'#D`'#D`O4cQPO1G/TO4mQPO'#DQOOQO1G/T1G/TO4rQPO1G/TO4wQPO1G/TO]QPO'#DbO4|QPO,5:dOOQO1G/_1G/_OOQO,59x,59xOOQO-E7[-E7[OOQO1G/[1G/[OOQO,59{,59{O5UQPO1G/ZOOQO-E7_-E7_OOQO1G/b1G/bO,fQPO,59VO5]QPO,59RO5dQPO,59QO]QPO7+$SO]QPO,59hO2YQPO'#D_O5nQPO,5:^OOQO7+$l7+$lO]QPO,59kOOQO-E7^-E7^OOQO7+$o7+$oO5vQPO7+$oO]QPO,59lO5{QPO7+$oOOQO,59|,59|OOQO-E7`-E7`P!nQPO'#DaOOQO<<Gn<<GnOOQO1G/S1G/SOOQO,59y,59yOOQO-E7]-E7]O6QQPO1G/VOOQO<<HZ<<HZO6VQPO1G/WO6_QPO<<HZOOQO7+$q7+$qOOQO7+$r7+$rO6iQPOAN=uOOQOAN=uAN=uO6sQPOAN=uOOQOG23aG23aO6xQPOG23aOOQOLD({LD({O/uQPO'#ChO/uQPO'#DdO/uQPO'#De",
  stateData: "7X~O!^OSPOSQOS~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`PO!aQO!eSO!gRO~OTeO!efO~ObhO~O]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`kO!aQO!eSO!gRO~O!efO!mpO!nqO!orO^_X!P_X![_X!q_X!s_X!t_X!u_X!i_X!j_X!d_X!c_X!p_X!h_X~O!r_X~P#rO]VO^VOaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O^ZX!PZX![ZX!sZX!tZX!uZX!iZX!jZX!dZX!cZX!pZX!hZX~O!qwO!rZX~P&UO![YX!sYX!tYX!uYX!iYX!jYX!dYX!cYX!pYX!hYX~O^yO!PyO!rYX~P'WO!r{O![XX!sXX!tXX!uXX!iXX!jXX!dXX!cXX!pXX!hXX~O!s|O![WX!tWX!uWX!iWX!jWX!dWX!cWX!pWX!hWX~O!t!OO![VX!uVX!iVX!jVX!dVX!cVX!pVX!hVX~O!u!QO![UX!iUX!jUX!dUX!cUX!pUX!hUX~O!e!RO~O!e!TO!g!SO~O!d!WO~P]Om!YO~O!c!ZO!d!bX~O!d!]O~OTeO!efO!k!^O~O!i!_O!jwX~O!j!aO~O!d!bO~O!`!cO~O!`!dO~O!efO!mpO!nqO!orO^_a!P_a![_a!q_a!s_a!t_a!u_a!i_a!j_a!d_a!c_a!p_a!h_a~O!r_a~P,fO^Za!PZa![Za!sZa!tZa!uZa!iZa!jZa!dZa!cZa!pZa!hZa~O!qwO!rZa~P-tO![Ya!sYa!tYa!uYa!iYa!jYa!dYa!cYa!pYa!hYa~O^yO!PyO!rYa~P.vO]#{O^#{OaTObTOdaOeaOgbOhTOiTOjTOocOrdO!`uO!aQO!eSO!gRO~O!s|O![Wa!tWa!uWa!iWa!jWa!dWa!cWa!pWa!hWa~O!t!OO![Va!uVa!iVa!jVa!dVa!cVa!pVa!hVa~O!`!tO~Ou!zO!j!{O~P]O!c#OO!d!lX~O!d#QO~O!c!ZO!d!ba~O!jwa~P!nO!i#VO!jwa~O!p#XO~O!q#|O~P&UO^#}O!P#}O~P'WO!h#]O~OT#^O~O!c#_O!d!fX~O!d#aO~O!h#bO~Ou!zO!j#dO~P]O!h#fO~O!j#dO~O!d#gO~O!c#OO!d!la~O!jwi~P!nO!q#|O~P-tO^#}O!P#}O~P.vO!c#_O!d!fa~O!j#pO~O!g#rO~O!i#sO~O!i#tO!jti~Ou!zO!j#vO~P]Ou!zO!j#xO~P]O!j#xO~O!j#zO~OQP!qaT!`!t!sT~",
  goto: "-V!mPPPP!nP!n#V#n$Y$u%c&TPP&{'mPP(aPP(aPPPP(a)RP(aP)X(aP)_)eP(a)q)t(a(a)|*R*R*RP*X*_*e*o*u*{+V+a+k+qPPP+wPP,|PPP-PPPPPP-S{_ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u{^ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uz]ORSefr!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!q!O|[ORSefr!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!o|!OZORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uR!n{!OYORSefr|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uS!iy#}R!m{!QXORSefry|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uSvV#{S!gw#|T!l{#}!_WORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}!UUORSVefrwy|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#uX!k{#{#|#}!_TORSVefrwy{|!O!Q!S!T!^!_!y#O#V#]#^#b#f#j#r#u#{#|#}QiQR#R!ZQ!u!RR#m#_X!x!S!y#r#uQ!|!SQ#e!yQ#w#rR#y#uRnRQmRV#U!_#V#jUgPkuXsUt!k#YQ![iR#S![Q#`!uR#n#`Q!y!SS#c!y#uR#u#rQ!`mR#W!`Q#P!VR#i#PQtUS!ft#YR#Y!kQxXS!hx#ZR#Z!lQzYS!jz#[R#[!mQ}[R!p}Q!P]R!r!PQ`OWlR!_#V#jQoSQ!UeQ!VfQ!erQ!s!QW!w!S!y#r#uQ!}!TQ#T!^Q#h#OQ#k#]Q#l#^Q#o#bR#q#fRjQR!v!RR!Xf",
  nodeNames: "⚠ LineComment BlockComment Script Lambda Arrow TernaryExpr OrExpr AndExpr CompareExpr AdditiveExpr MultiplicativeExpr UnaryExpr NotOp Minus AccessExpr AtomicExpr Number String Boolean true false Null null Variable ModelConstant ResourceKey I18NLiteral I18NEntry LanguageTag TupleExpr tuple TupleEntry SwitchExpr switch SwitchCase SwitchDefault default Block BlockContent Statement ParenExpr FunctionCall CallArgs MethodAccess DescendAccess ArrayAccess Plus",
  maxTerm: 83,
  propSources: [rh],
  skippedNodes: [0, 1, 2],
  repeatNodeCount: 10,
  tokenData: ":S~RxXY#oYZ#o]^#opq#oqr$Qrs$_st(ptu,Wuv,uvw,zwx-Vxy.nyz.sz{,u{|.x|}.}}!O/S!O!P/a!P!Q/n!Q!R1c!R![2q![!]3S!]!^3X!^!_3^!_!`3f!`!a3^!a!b3n!b!c3s!c!}4[!}#O4m#P#Q4r#R#S4[#S#T4w#T#U7^#U#c4[#c#d8q#d#o4[#o#p9m#p#q9r#q#r9}~#tS!^~XY#oYZ#o]^#opq#oV$VP]P!_!`$YU$_O!rU~$bVOr$wrs&es#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~$zVOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_<%lO$w~%fOb~~%iRO;'S$w;'S;=`%r;=`O$w~%uWOr$wrs%as#O$w#O#P%f#P;'S$w;'S;=`&_;=`<%l$w<%lO$w~&bP;=`<%l$w~&jPb~rs&m~&pTOr&mrs'Ps;'S&m;'S;=`(Z;=`O&m~'STOr&mrs'cs;'S&m;'S;=`(Z;=`O&m~'fTOr&mrs'us;'S&m;'S;=`(Z;=`O&m~'zTb~Or&mrs'us;'S&m;'S;=`(Z;=`O&m~(^UOr&mrs'Ps;'S&m;'S;=`(Z;=`<%l&m<%lO&m~(sRrs(|wx*jxy,R~)PVOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d<%lO(|~)kOj~~)nRO;'S(|;'S;=`)w;=`O(|~)zWOr(|rs)fs#O(|#O#P)k#P;'S(|;'S;=`*d;=`<%l(|<%lO(|~*gP;=`<%l(|~*mVOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{<%lO*j~+VRO;'S*j;'S;=`+`;=`O*j~+cWOw*jwx)fx#O*j#O#P+S#P;'S*j;'S;=`+{;=`<%l*j<%lO*j~,OP;=`<%l*j~,WO!a~~,ZR!c!},d#R#S,d#T#o,d~,iSh~!Q![,d!c!},d#R#S,d#T#o,d~,zO!q~~,}Pvw-Q~-VO!s~~-YVOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h<%lO-V~-rRO;'S-V;'S;=`-{;=`O-V~.OWOw-Vwx%ax#O-V#O#P-o#P;'S-V;'S;=`.h;=`<%l-V<%lO-V~.kP;=`<%l-V~.sO!e~~.xO!d~~.}O!P~~/SO!c~_/XP^T!`!a/[Y/aOTY~/fP!m~!O!P/i~/nO!n~~/sQ!q~z{/y!P!Q0z~/|TOz/yz{0]{;'S/y;'S;=`0t<%lO/y~0`TO!P/y!P!Q0o!Q;'S/y;'S;=`0t<%lO/y~0tOQ~~0wP;=`<%l/y~1PSP~OY0zZ;'S0z;'S;=`1]<%lO0z~1`P;=`<%l0z~1hRa~!O!P1q!g!h2V#X#Y2V~1tP!Q![1w~1|Ra~!Q![1w!g!h2V#X#Y2V~2YR{|2c}!O2c!Q![2i~2fP!Q![2i~2nPa~!Q![2i~2vSa~!O!P1q!Q![2q!g!h2V#X#Y2V~3XO!h~~3^O!i~U3cP!rU!_!`$Y^3kP!kW!_!`$Y~3sO!u~~3vQ!c!}3|#T#o3|~4RRm~}!O3|!c!}3|#T#o3|~4aS!`~!Q![4[!c!}4[#R#S4[#T#o4[~4rO!o~~4wO!p~~4zR!c!}5T#R#S5T#T#o5T~5WWst5p!O!P5T!Q![5T![!]6h!c!}5T#R#S5T#S#T6c#T#o5T~5sR!c!}5|#R#S5|#T#o5|~6PU!O!P5|!Q![5|!c!}5|#R#S5|#S#T6c#T#o5|~6hOi~~6kR!c!}6t#R#S6t#T#o6t~6wVst5p!O!P6t!Q![6t!c!}6t#R#S6t#S#T6c#T#o6t~7cU!`~!Q![4[!c!}4[#R#S4[#T#b4[#b#c7u#c#o4[~7zU!`~!Q![4[!c!}4[#R#S4[#T#W4[#W#X8^#X#o4[~8eS!`~!s~!Q![4[!c!}4[#R#S4[#T#o4[~8vU!`~!Q![4[!c!}4[#R#S4[#T#f4[#f#g9Y#g#o4[~9aS!`~!t~!Q![4[!c!}4[#R#S4[#T#o4[~9rO!g~~9uP#p#q9x~9}O!t~~:SO!j~",
  tokenizers: [0, 1, 2, 3],
  topRules: { Script: [0, 3] },
  specialized: [{ term: 62, get: (s) => Ap[s] || -1 }],
  tokenPrec: 1011
}), Pp = gn.define({
  parser: Tp.configure({
    props: [rh]
  }),
  languageData: {
    commentTokens: { line: "//", block: { open: "/*", close: "*/" } },
    closeBrackets: { brackets: ["(", "[", "{", "'", '"', "`"] }
  }
});
function Mp() {
  return new Gc(Pp);
}
const { useRef: Ge, useEffect: Ui, useCallback: Dp } = Go, Rp = 300, Bp = 3e3, Lp = ({ controlId: s, state: t }) => {
  const e = oh(), i = lh(), n = Ge(null), r = Ge(null), o = Ge(new yi()), l = Ge(null), a = Ge(null), h = t.value ?? "", f = t.readOnly === !0, c = Dp(
    (p) => {
      var b;
      const g = p.pos, m = p.state.doc.lineAt(g), y = ((b = p.matchBefore(/[\w$`.:]+/)) == null ? void 0 : b.text) ?? "";
      if (!y && !p.explicit) return Promise.resolve(null);
      const w = String(Date.now()) + Math.random();
      return i("complete", { line: m.text, prefix: y, requestId: w }), new Promise((O) => {
        a.current = { requestId: w, resolve: O, from: g - y.length }, setTimeout(() => {
          var v;
          ((v = a.current) == null ? void 0 : v.requestId) === w && (a.current = null, O(null));
        }, Bp);
      });
    },
    [i]
  );
  Ui(() => {
    if (!n.current) return;
    const p = new M({
      state: N.create({
        doc: h,
        extensions: [
          Mp(),
          Ou(),
          ar.of([...Pd, ...Lu]),
          o.current.of(M.editable.of(!f)),
          ip({ override: [c] }),
          M.updateListener.of((g) => {
            g.docChanged && (l.current && clearTimeout(l.current), l.current = setTimeout(() => {
              const m = g.state.doc.toString();
              i("valueChanged", { value: m }), i("validate", { text: m });
            }, Rp));
          })
        ]
      }),
      parent: n.current
    });
    return r.current = p, () => {
      l.current && clearTimeout(l.current), p.destroy(), r.current = null;
    };
  }, []), Ui(() => {
    const p = r.current;
    p && p.dispatch({
      effects: o.current.reconfigure(M.editable.of(!f))
    });
  }, [f]);
  const u = e.diagnostics ?? [];
  Ui(() => {
    const p = r.current;
    if (!p) return;
    const g = p.state.doc, m = u.map((y) => {
      const w = Math.max(1, Math.min(y.line, g.lines)), b = g.line(w), O = b.from + Math.max(0, Math.min(y.col - 1, b.length)), v = y.endCol != null && y.endLine != null ? g.line(Math.max(1, Math.min(y.endLine, g.lines))).from + Math.max(0, Math.min(y.endCol - 1, b.length)) : Math.min(O + 1, b.to);
      return { from: O, to: v, severity: y.severity, message: y.message };
    }).filter((y) => y.from <= g.length && y.to <= g.length);
    p.dispatch(lp(p.state, m));
  }, [u]);
  const d = e.completionResponse;
  return Ui(() => {
    const p = a.current;
    if (!p || !d || d.requestId !== p.requestId)
      return;
    const g = d.completions ?? [];
    if (a.current = null, g.length === 0) {
      p.resolve(null);
      return;
    }
    p.resolve({
      from: p.from,
      options: g.map((m) => ({
        label: m.name,
        detail: m.value !== m.name ? m.value : void 0,
        info: m.docHTML ? () => {
          const y = document.createElement("div");
          return y.innerHTML = m.docHTML, y;
        } : void 0,
        boost: m.score ?? 0
      }))
    });
  }, [d]), /* @__PURE__ */ Go.createElement("div", { ref: n, id: s, className: "tlScriptEditor" });
};
ah("TLScriptEditor", Lp);
