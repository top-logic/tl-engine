import { React as x, ReactDOM as Pi, useI18N as Om, useTLState as Nm, useTLCommand as Rm, useTLUpload as Dm, useTLDataUrl as Pm, register as Im } from "tl-react-bridge";
function ye(n) {
  this.content = n;
}
ye.prototype = {
  constructor: ye,
  find: function(n) {
    for (var e = 0; e < this.content.length; e += 2)
      if (this.content[e] === n) return e;
    return -1;
  },
  // :: (string) → ?any
  // Retrieve the value stored under `key`, or return undefined when
  // no such key exists.
  get: function(n) {
    var e = this.find(n);
    return e == -1 ? void 0 : this.content[e + 1];
  },
  // :: (string, any, ?string) → OrderedMap
  // Create a new map by replacing the value of `key` with a new
  // value, or adding a binding to the end of the map. If `newKey` is
  // given, the key of the binding will be replaced with that key.
  update: function(n, e, t) {
    var r = t && t != n ? this.remove(t) : this, o = r.find(n), i = r.content.slice();
    return o == -1 ? i.push(t || n, e) : (i[o + 1] = e, t && (i[o] = t)), new ye(i);
  },
  // :: (string) → OrderedMap
  // Return a map with the given key removed, if it existed.
  remove: function(n) {
    var e = this.find(n);
    if (e == -1) return this;
    var t = this.content.slice();
    return t.splice(e, 2), new ye(t);
  },
  // :: (string, any) → OrderedMap
  // Add a new key to the start of the map.
  addToStart: function(n, e) {
    return new ye([n, e].concat(this.remove(n).content));
  },
  // :: (string, any) → OrderedMap
  // Add a new key to the end of the map.
  addToEnd: function(n, e) {
    var t = this.remove(n).content.slice();
    return t.push(n, e), new ye(t);
  },
  // :: (string, string, any) → OrderedMap
  // Add a key after the given key. If `place` is not found, the new
  // key is added to the end.
  addBefore: function(n, e, t) {
    var r = this.remove(e), o = r.content.slice(), i = r.find(n);
    return o.splice(i == -1 ? o.length : i, 0, e, t), new ye(o);
  },
  // :: ((key: string, value: any))
  // Call the given function for each key/value pair in the map, in
  // order.
  forEach: function(n) {
    for (var e = 0; e < this.content.length; e += 2)
      n(this.content[e], this.content[e + 1]);
  },
  // :: (union<Object, OrderedMap>) → OrderedMap
  // Create a new map by prepending the keys in this map that don't
  // appear in `map` before the keys in `map`.
  prepend: function(n) {
    return n = ye.from(n), n.size ? new ye(n.content.concat(this.subtract(n).content)) : this;
  },
  // :: (union<Object, OrderedMap>) → OrderedMap
  // Create a new map by appending the keys in this map that don't
  // appear in `map` after the keys in `map`.
  append: function(n) {
    return n = ye.from(n), n.size ? new ye(this.subtract(n).content.concat(n.content)) : this;
  },
  // :: (union<Object, OrderedMap>) → OrderedMap
  // Create a map containing all the keys in this map that don't
  // appear in `map`.
  subtract: function(n) {
    var e = this;
    n = ye.from(n);
    for (var t = 0; t < n.content.length; t += 2)
      e = e.remove(n.content[t]);
    return e;
  },
  // :: () → Object
  // Turn ordered map into a plain object.
  toObject: function() {
    var n = {};
    return this.forEach(function(e, t) {
      n[e] = t;
    }), n;
  },
  // :: number
  // The amount of keys in this map.
  get size() {
    return this.content.length >> 1;
  }
};
ye.from = function(n) {
  if (n instanceof ye) return n;
  var e = [];
  if (n) for (var t in n) e.push(t, n[t]);
  return new ye(e);
};
function md(n, e, t) {
  for (let r = 0; ; r++) {
    if (r == n.childCount || r == e.childCount)
      return n.childCount == e.childCount ? null : t;
    let o = n.child(r), i = e.child(r);
    if (o == i) {
      t += o.nodeSize;
      continue;
    }
    if (!o.sameMarkup(i))
      return t;
    if (o.isText && o.text != i.text) {
      for (let s = 0; o.text[s] == i.text[s]; s++)
        t++;
      return t;
    }
    if (o.content.size || i.content.size) {
      let s = md(o.content, i.content, t + 1);
      if (s != null)
        return s;
    }
    t += o.nodeSize;
  }
}
function gd(n, e, t, r) {
  for (let o = n.childCount, i = e.childCount; ; ) {
    if (o == 0 || i == 0)
      return o == i ? null : { a: t, b: r };
    let s = n.child(--o), l = e.child(--i), a = s.nodeSize;
    if (s == l) {
      t -= a, r -= a;
      continue;
    }
    if (!s.sameMarkup(l))
      return { a: t, b: r };
    if (s.isText && s.text != l.text) {
      let c = 0, u = Math.min(s.text.length, l.text.length);
      for (; c < u && s.text[s.text.length - c - 1] == l.text[l.text.length - c - 1]; )
        c++, t--, r--;
      return { a: t, b: r };
    }
    if (s.content.size || l.content.size) {
      let c = gd(s.content, l.content, t - 1, r - 1);
      if (c)
        return c;
    }
    t -= a, r -= a;
  }
}
let M = class Oe {
  /**
  @internal
  */
  constructor(e, t) {
    if (this.content = e, this.size = t || 0, t == null)
      for (let r = 0; r < e.length; r++)
        this.size += e[r].nodeSize;
  }
  /**
  Invoke a callback for all descendant nodes between the given two
  positions (relative to start of this fragment). Doesn't descend
  into a node when the callback returns `false`.
  */
  nodesBetween(e, t, r, o = 0, i) {
    for (let s = 0, l = 0; l < t; s++) {
      let a = this.content[s], c = l + a.nodeSize;
      if (c > e && r(a, o + l, i || null, s) !== !1 && a.content.size) {
        let u = l + 1;
        a.nodesBetween(Math.max(0, e - u), Math.min(a.content.size, t - u), r, o + u);
      }
      l = c;
    }
  }
  /**
  Call the given callback for every descendant node. `pos` will be
  relative to the start of the fragment. The callback may return
  `false` to prevent traversal of a given node's children.
  */
  descendants(e) {
    this.nodesBetween(0, this.size, e);
  }
  /**
  Extract the text between `from` and `to`. See the same method on
  [`Node`](https://prosemirror.net/docs/ref/#model.Node.textBetween).
  */
  textBetween(e, t, r, o) {
    let i = "", s = !0;
    return this.nodesBetween(e, t, (l, a) => {
      let c = l.isText ? l.text.slice(Math.max(e, a) - a, t - a) : l.isLeaf ? o ? typeof o == "function" ? o(l) : o : l.type.spec.leafText ? l.type.spec.leafText(l) : "" : "";
      l.isBlock && (l.isLeaf && c || l.isTextblock) && r && (s ? s = !1 : i += r), i += c;
    }, 0), i;
  }
  /**
  Create a new fragment containing the combined content of this
  fragment and the other.
  */
  append(e) {
    if (!e.size)
      return this;
    if (!this.size)
      return e;
    let t = this.lastChild, r = e.firstChild, o = this.content.slice(), i = 0;
    for (t.isText && t.sameMarkup(r) && (o[o.length - 1] = t.withText(t.text + r.text), i = 1); i < e.content.length; i++)
      o.push(e.content[i]);
    return new Oe(o, this.size + e.size);
  }
  /**
  Cut out the sub-fragment between the two given positions.
  */
  cut(e, t = this.size) {
    if (e == 0 && t == this.size)
      return this;
    let r = [], o = 0;
    if (t > e)
      for (let i = 0, s = 0; s < t; i++) {
        let l = this.content[i], a = s + l.nodeSize;
        a > e && ((s < e || a > t) && (l.isText ? l = l.cut(Math.max(0, e - s), Math.min(l.text.length, t - s)) : l = l.cut(Math.max(0, e - s - 1), Math.min(l.content.size, t - s - 1))), r.push(l), o += l.nodeSize), s = a;
      }
    return new Oe(r, o);
  }
  /**
  @internal
  */
  cutByIndex(e, t) {
    return e == t ? Oe.empty : e == 0 && t == this.content.length ? this : new Oe(this.content.slice(e, t));
  }
  /**
  Create a new fragment in which the node at the given index is
  replaced by the given node.
  */
  replaceChild(e, t) {
    let r = this.content[e];
    if (r == t)
      return this;
    let o = this.content.slice(), i = this.size + t.nodeSize - r.nodeSize;
    return o[e] = t, new Oe(o, i);
  }
  /**
  Create a new fragment by prepending the given node to this
  fragment.
  */
  addToStart(e) {
    return new Oe([e].concat(this.content), this.size + e.nodeSize);
  }
  /**
  Create a new fragment by appending the given node to this
  fragment.
  */
  addToEnd(e) {
    return new Oe(this.content.concat(e), this.size + e.nodeSize);
  }
  /**
  Compare this fragment to another one.
  */
  eq(e) {
    if (this.content.length != e.content.length)
      return !1;
    for (let t = 0; t < this.content.length; t++)
      if (!this.content[t].eq(e.content[t]))
        return !1;
    return !0;
  }
  /**
  The first child of the fragment, or `null` if it is empty.
  */
  get firstChild() {
    return this.content.length ? this.content[0] : null;
  }
  /**
  The last child of the fragment, or `null` if it is empty.
  */
  get lastChild() {
    return this.content.length ? this.content[this.content.length - 1] : null;
  }
  /**
  The number of child nodes in this fragment.
  */
  get childCount() {
    return this.content.length;
  }
  /**
  Get the child node at the given index. Raise an error when the
  index is out of range.
  */
  child(e) {
    let t = this.content[e];
    if (!t)
      throw new RangeError("Index " + e + " out of range for " + this);
    return t;
  }
  /**
  Get the child node at the given index, if it exists.
  */
  maybeChild(e) {
    return this.content[e] || null;
  }
  /**
  Call `f` for every child node, passing the node, its offset
  into this parent node, and its index.
  */
  forEach(e) {
    for (let t = 0, r = 0; t < this.content.length; t++) {
      let o = this.content[t];
      e(o, r, t), r += o.nodeSize;
    }
  }
  /**
  Find the first position at which this fragment and another
  fragment differ, or `null` if they are the same.
  */
  findDiffStart(e, t = 0) {
    return md(this, e, t);
  }
  /**
  Find the first position, searching from the end, at which this
  fragment and the given fragment differ, or `null` if they are
  the same. Since this position will not be the same in both
  nodes, an object with two separate positions is returned.
  */
  findDiffEnd(e, t = this.size, r = e.size) {
    return gd(this, e, t, r);
  }
  /**
  Find the index and inner offset corresponding to a given relative
  position in this fragment. The result object will be reused
  (overwritten) the next time the function is called. @internal
  */
  findIndex(e) {
    if (e == 0)
      return so(0, e);
    if (e == this.size)
      return so(this.content.length, e);
    if (e > this.size || e < 0)
      throw new RangeError(`Position ${e} outside of fragment (${this})`);
    for (let t = 0, r = 0; ; t++) {
      let o = this.child(t), i = r + o.nodeSize;
      if (i >= e)
        return i == e ? so(t + 1, i) : so(t, r);
      r = i;
    }
  }
  /**
  Return a debugging string that describes this fragment.
  */
  toString() {
    return "<" + this.toStringInner() + ">";
  }
  /**
  @internal
  */
  toStringInner() {
    return this.content.join(", ");
  }
  /**
  Create a JSON-serializeable representation of this fragment.
  */
  toJSON() {
    return this.content.length ? this.content.map((e) => e.toJSON()) : null;
  }
  /**
  Deserialize a fragment from its JSON representation.
  */
  static fromJSON(e, t) {
    if (!t)
      return Oe.empty;
    if (!Array.isArray(t))
      throw new RangeError("Invalid input for Fragment.fromJSON");
    return new Oe(t.map(e.nodeFromJSON));
  }
  /**
  Build a fragment from an array of nodes. Ensures that adjacent
  text nodes with the same marks are joined together.
  */
  static fromArray(e) {
    if (!e.length)
      return Oe.empty;
    let t, r = 0;
    for (let o = 0; o < e.length; o++) {
      let i = e[o];
      r += i.nodeSize, o && i.isText && e[o - 1].sameMarkup(i) ? (t || (t = e.slice(0, o)), t[t.length - 1] = i.withText(t[t.length - 1].text + i.text)) : t && t.push(i);
    }
    return new Oe(t || e, r);
  }
  /**
  Create a fragment from something that can be interpreted as a
  set of nodes. For `null`, it returns the empty fragment. For a
  fragment, the fragment itself. For a node or array of nodes, a
  fragment containing those nodes.
  */
  static from(e) {
    if (!e)
      return Oe.empty;
    if (e instanceof Oe)
      return e;
    if (Array.isArray(e))
      return this.fromArray(e);
    if (e.attrs)
      return new Oe([e], e.nodeSize);
    throw new RangeError("Can not convert " + e + " to a Fragment" + (e.nodesBetween ? " (looks like multiple versions of prosemirror-model were loaded)" : ""));
  }
};
M.empty = new M([], 0);
const gs = { index: 0, offset: 0 };
function so(n, e) {
  return gs.index = n, gs.offset = e, gs;
}
function zo(n, e) {
  if (n === e)
    return !0;
  if (!(n && typeof n == "object") || !(e && typeof e == "object"))
    return !1;
  let t = Array.isArray(n);
  if (Array.isArray(e) != t)
    return !1;
  if (t) {
    if (n.length != e.length)
      return !1;
    for (let r = 0; r < n.length; r++)
      if (!zo(n[r], e[r]))
        return !1;
  } else {
    for (let r in n)
      if (!(r in e) || !zo(n[r], e[r]))
        return !1;
    for (let r in e)
      if (!(r in n))
        return !1;
  }
  return !0;
}
let Z = class nl {
  /**
  @internal
  */
  constructor(e, t) {
    this.type = e, this.attrs = t;
  }
  /**
  Given a set of marks, create a new set which contains this one as
  well, in the right position. If this mark is already in the set,
  the set itself is returned. If any marks that are set to be
  [exclusive](https://prosemirror.net/docs/ref/#model.MarkSpec.excludes) with this mark are present,
  those are replaced by this one.
  */
  addToSet(e) {
    let t, r = !1;
    for (let o = 0; o < e.length; o++) {
      let i = e[o];
      if (this.eq(i))
        return e;
      if (this.type.excludes(i.type))
        t || (t = e.slice(0, o));
      else {
        if (i.type.excludes(this.type))
          return e;
        !r && i.type.rank > this.type.rank && (t || (t = e.slice(0, o)), t.push(this), r = !0), t && t.push(i);
      }
    }
    return t || (t = e.slice()), r || t.push(this), t;
  }
  /**
  Remove this mark from the given set, returning a new set. If this
  mark is not in the set, the set itself is returned.
  */
  removeFromSet(e) {
    for (let t = 0; t < e.length; t++)
      if (this.eq(e[t]))
        return e.slice(0, t).concat(e.slice(t + 1));
    return e;
  }
  /**
  Test whether this mark is in the given set of marks.
  */
  isInSet(e) {
    for (let t = 0; t < e.length; t++)
      if (this.eq(e[t]))
        return !0;
    return !1;
  }
  /**
  Test whether this mark has the same type and attributes as
  another mark.
  */
  eq(e) {
    return this == e || this.type == e.type && zo(this.attrs, e.attrs);
  }
  /**
  Convert this mark to a JSON-serializeable representation.
  */
  toJSON() {
    let e = { type: this.type.name };
    for (let t in this.attrs) {
      e.attrs = this.attrs;
      break;
    }
    return e;
  }
  /**
  Deserialize a mark from JSON.
  */
  static fromJSON(e, t) {
    if (!t)
      throw new RangeError("Invalid input for Mark.fromJSON");
    let r = e.marks[t.type];
    if (!r)
      throw new RangeError(`There is no mark type ${t.type} in this schema`);
    let o = r.create(t.attrs);
    return r.checkAttrs(o.attrs), o;
  }
  /**
  Test whether two sets of marks are identical.
  */
  static sameSet(e, t) {
    if (e == t)
      return !0;
    if (e.length != t.length)
      return !1;
    for (let r = 0; r < e.length; r++)
      if (!e[r].eq(t[r]))
        return !1;
    return !0;
  }
  /**
  Create a properly sorted mark set from null, a single mark, or an
  unsorted array of marks.
  */
  static setFrom(e) {
    if (!e || Array.isArray(e) && e.length == 0)
      return nl.none;
    if (e instanceof nl)
      return [e];
    let t = e.slice();
    return t.sort((r, o) => r.type.rank - o.type.rank), t;
  }
};
Z.none = [];
class $o extends Error {
}
class O {
  /**
  Create a slice. When specifying a non-zero open depth, you must
  make sure that there are nodes of at least that depth at the
  appropriate side of the fragment—i.e. if the fragment is an
  empty paragraph node, `openStart` and `openEnd` can't be greater
  than 1.
  
  It is not necessary for the content of open nodes to conform to
  the schema's content constraints, though it should be a valid
  start/end/middle for such a node, depending on which sides are
  open.
  */
  constructor(e, t, r) {
    this.content = e, this.openStart = t, this.openEnd = r;
  }
  /**
  The size this slice would add when inserted into a document.
  */
  get size() {
    return this.content.size - this.openStart - this.openEnd;
  }
  /**
  @internal
  */
  insertAt(e, t) {
    let r = bd(this.content, e + this.openStart, t);
    return r && new O(r, this.openStart, this.openEnd);
  }
  /**
  @internal
  */
  removeBetween(e, t) {
    return new O(yd(this.content, e + this.openStart, t + this.openStart), this.openStart, this.openEnd);
  }
  /**
  Tests whether this slice is equal to another slice.
  */
  eq(e) {
    return this.content.eq(e.content) && this.openStart == e.openStart && this.openEnd == e.openEnd;
  }
  /**
  @internal
  */
  toString() {
    return this.content + "(" + this.openStart + "," + this.openEnd + ")";
  }
  /**
  Convert a slice to a JSON-serializable representation.
  */
  toJSON() {
    if (!this.content.size)
      return null;
    let e = { content: this.content.toJSON() };
    return this.openStart > 0 && (e.openStart = this.openStart), this.openEnd > 0 && (e.openEnd = this.openEnd), e;
  }
  /**
  Deserialize a slice from its JSON representation.
  */
  static fromJSON(e, t) {
    if (!t)
      return O.empty;
    let r = t.openStart || 0, o = t.openEnd || 0;
    if (typeof r != "number" || typeof o != "number")
      throw new RangeError("Invalid input for Slice.fromJSON");
    return new O(M.fromJSON(e, t.content), r, o);
  }
  /**
  Create a slice from a fragment by taking the maximum possible
  open value on both side of the fragment.
  */
  static maxOpen(e, t = !0) {
    let r = 0, o = 0;
    for (let i = e.firstChild; i && !i.isLeaf && (t || !i.type.spec.isolating); i = i.firstChild)
      r++;
    for (let i = e.lastChild; i && !i.isLeaf && (t || !i.type.spec.isolating); i = i.lastChild)
      o++;
    return new O(e, r, o);
  }
}
O.empty = new O(M.empty, 0, 0);
function yd(n, e, t) {
  let { index: r, offset: o } = n.findIndex(e), i = n.maybeChild(r), { index: s, offset: l } = n.findIndex(t);
  if (o == e || i.isText) {
    if (l != t && !n.child(s).isText)
      throw new RangeError("Removing non-flat range");
    return n.cut(0, e).append(n.cut(t));
  }
  if (r != s)
    throw new RangeError("Removing non-flat range");
  return n.replaceChild(r, i.copy(yd(i.content, e - o - 1, t - o - 1)));
}
function bd(n, e, t, r) {
  let { index: o, offset: i } = n.findIndex(e), s = n.maybeChild(o);
  if (i == e || s.isText)
    return r && !r.canReplace(o, o, t) ? null : n.cut(0, e).append(t).append(n.cut(e));
  let l = bd(s.content, e - i - 1, t, s);
  return l && n.replaceChild(o, s.copy(l));
}
function Lm(n, e, t) {
  if (t.openStart > n.depth)
    throw new $o("Inserted content deeper than insertion position");
  if (n.depth - t.openStart != e.depth - t.openEnd)
    throw new $o("Inconsistent open depths");
  return wd(n, e, t, 0);
}
function wd(n, e, t, r) {
  let o = n.index(r), i = n.node(r);
  if (o == e.index(r) && r < n.depth - t.openStart) {
    let s = wd(n, e, t, r + 1);
    return i.copy(i.content.replaceChild(o, s));
  } else if (t.content.size)
    if (!t.openStart && !t.openEnd && n.depth == r && e.depth == r) {
      let s = n.parent, l = s.content;
      return pn(s, l.cut(0, n.parentOffset).append(t.content).append(l.cut(e.parentOffset)));
    } else {
      let { start: s, end: l } = _m(t, n);
      return pn(i, Cd(n, s, l, e, r));
    }
  else return pn(i, Ho(n, e, r));
}
function vd(n, e) {
  if (!e.type.compatibleContent(n.type))
    throw new $o("Cannot join " + e.type.name + " onto " + n.type.name);
}
function rl(n, e, t) {
  let r = n.node(t);
  return vd(r, e.node(t)), r;
}
function hn(n, e) {
  let t = e.length - 1;
  t >= 0 && n.isText && n.sameMarkup(e[t]) ? e[t] = n.withText(e[t].text + n.text) : e.push(n);
}
function br(n, e, t, r) {
  let o = (e || n).node(t), i = 0, s = e ? e.index(t) : o.childCount;
  n && (i = n.index(t), n.depth > t ? i++ : n.textOffset && (hn(n.nodeAfter, r), i++));
  for (let l = i; l < s; l++)
    hn(o.child(l), r);
  e && e.depth == t && e.textOffset && hn(e.nodeBefore, r);
}
function pn(n, e) {
  return n.type.checkContent(e), n.copy(e);
}
function Cd(n, e, t, r, o) {
  let i = n.depth > o && rl(n, e, o + 1), s = r.depth > o && rl(t, r, o + 1), l = [];
  return br(null, n, o, l), i && s && e.index(o) == t.index(o) ? (vd(i, s), hn(pn(i, Cd(n, e, t, r, o + 1)), l)) : (i && hn(pn(i, Ho(n, e, o + 1)), l), br(e, t, o, l), s && hn(pn(s, Ho(t, r, o + 1)), l)), br(r, null, o, l), new M(l);
}
function Ho(n, e, t) {
  let r = [];
  if (br(null, n, t, r), n.depth > t) {
    let o = rl(n, e, t + 1);
    hn(pn(o, Ho(n, e, t + 1)), r);
  }
  return br(e, null, t, r), new M(r);
}
function _m(n, e) {
  let t = e.depth - n.openStart, o = e.node(t).copy(n.content);
  for (let i = t - 1; i >= 0; i--)
    o = e.node(i).copy(M.from(o));
  return {
    start: o.resolveNoCache(n.openStart + t),
    end: o.resolveNoCache(o.content.size - n.openEnd - t)
  };
}
class Ar {
  /**
  @internal
  */
  constructor(e, t, r) {
    this.pos = e, this.path = t, this.parentOffset = r, this.depth = t.length / 3 - 1;
  }
  /**
  @internal
  */
  resolveDepth(e) {
    return e == null ? this.depth : e < 0 ? this.depth + e : e;
  }
  /**
  The parent node that the position points into. Note that even if
  a position points into a text node, that node is not considered
  the parent—text nodes are ‘flat’ in this model, and have no content.
  */
  get parent() {
    return this.node(this.depth);
  }
  /**
  The root node in which the position was resolved.
  */
  get doc() {
    return this.node(0);
  }
  /**
  The ancestor node at the given level. `p.node(p.depth)` is the
  same as `p.parent`.
  */
  node(e) {
    return this.path[this.resolveDepth(e) * 3];
  }
  /**
  The index into the ancestor at the given level. If this points
  at the 3rd node in the 2nd paragraph on the top level, for
  example, `p.index(0)` is 1 and `p.index(1)` is 2.
  */
  index(e) {
    return this.path[this.resolveDepth(e) * 3 + 1];
  }
  /**
  The index pointing after this position into the ancestor at the
  given level.
  */
  indexAfter(e) {
    return e = this.resolveDepth(e), this.index(e) + (e == this.depth && !this.textOffset ? 0 : 1);
  }
  /**
  The (absolute) position at the start of the node at the given
  level.
  */
  start(e) {
    return e = this.resolveDepth(e), e == 0 ? 0 : this.path[e * 3 - 1] + 1;
  }
  /**
  The (absolute) position at the end of the node at the given
  level.
  */
  end(e) {
    return e = this.resolveDepth(e), this.start(e) + this.node(e).content.size;
  }
  /**
  The (absolute) position directly before the wrapping node at the
  given level, or, when `depth` is `this.depth + 1`, the original
  position.
  */
  before(e) {
    if (e = this.resolveDepth(e), !e)
      throw new RangeError("There is no position before the top-level node");
    return e == this.depth + 1 ? this.pos : this.path[e * 3 - 1];
  }
  /**
  The (absolute) position directly after the wrapping node at the
  given level, or the original position when `depth` is `this.depth + 1`.
  */
  after(e) {
    if (e = this.resolveDepth(e), !e)
      throw new RangeError("There is no position after the top-level node");
    return e == this.depth + 1 ? this.pos : this.path[e * 3 - 1] + this.path[e * 3].nodeSize;
  }
  /**
  When this position points into a text node, this returns the
  distance between the position and the start of the text node.
  Will be zero for positions that point between nodes.
  */
  get textOffset() {
    return this.pos - this.path[this.path.length - 1];
  }
  /**
  Get the node directly after the position, if any. If the position
  points into a text node, only the part of that node after the
  position is returned.
  */
  get nodeAfter() {
    let e = this.parent, t = this.index(this.depth);
    if (t == e.childCount)
      return null;
    let r = this.pos - this.path[this.path.length - 1], o = e.child(t);
    return r ? e.child(t).cut(r) : o;
  }
  /**
  Get the node directly before the position, if any. If the
  position points into a text node, only the part of that node
  before the position is returned.
  */
  get nodeBefore() {
    let e = this.index(this.depth), t = this.pos - this.path[this.path.length - 1];
    return t ? this.parent.child(e).cut(0, t) : e == 0 ? null : this.parent.child(e - 1);
  }
  /**
  Get the position at the given index in the parent node at the
  given depth (which defaults to `this.depth`).
  */
  posAtIndex(e, t) {
    t = this.resolveDepth(t);
    let r = this.path[t * 3], o = t == 0 ? 0 : this.path[t * 3 - 1] + 1;
    for (let i = 0; i < e; i++)
      o += r.child(i).nodeSize;
    return o;
  }
  /**
  Get the marks at this position, factoring in the surrounding
  marks' [`inclusive`](https://prosemirror.net/docs/ref/#model.MarkSpec.inclusive) property. If the
  position is at the start of a non-empty node, the marks of the
  node after it (if any) are returned.
  */
  marks() {
    let e = this.parent, t = this.index();
    if (e.content.size == 0)
      return Z.none;
    if (this.textOffset)
      return e.child(t).marks;
    let r = e.maybeChild(t - 1), o = e.maybeChild(t);
    if (!r) {
      let l = r;
      r = o, o = l;
    }
    let i = r.marks;
    for (var s = 0; s < i.length; s++)
      i[s].type.spec.inclusive === !1 && (!o || !i[s].isInSet(o.marks)) && (i = i[s--].removeFromSet(i));
    return i;
  }
  /**
  Get the marks after the current position, if any, except those
  that are non-inclusive and not present at position `$end`. This
  is mostly useful for getting the set of marks to preserve after a
  deletion. Will return `null` if this position is at the end of
  its parent node or its parent node isn't a textblock (in which
  case no marks should be preserved).
  */
  marksAcross(e) {
    let t = this.parent.maybeChild(this.index());
    if (!t || !t.isInline)
      return null;
    let r = t.marks, o = e.parent.maybeChild(e.index());
    for (var i = 0; i < r.length; i++)
      r[i].type.spec.inclusive === !1 && (!o || !r[i].isInSet(o.marks)) && (r = r[i--].removeFromSet(r));
    return r;
  }
  /**
  The depth up to which this position and the given (non-resolved)
  position share the same parent nodes.
  */
  sharedDepth(e) {
    for (let t = this.depth; t > 0; t--)
      if (this.start(t) <= e && this.end(t) >= e)
        return t;
    return 0;
  }
  /**
  Returns a range based on the place where this position and the
  given position diverge around block content. If both point into
  the same textblock, for example, a range around that textblock
  will be returned. If they point into different blocks, the range
  around those blocks in their shared ancestor is returned. You can
  pass in an optional predicate that will be called with a parent
  node to see if a range into that parent is acceptable.
  */
  blockRange(e = this, t) {
    if (e.pos < this.pos)
      return e.blockRange(this);
    for (let r = this.depth - (this.parent.inlineContent || this.pos == e.pos ? 1 : 0); r >= 0; r--)
      if (e.pos <= this.end(r) && (!t || t(this.node(r))))
        return new Wo(this, e, r);
    return null;
  }
  /**
  Query whether the given position shares the same parent node.
  */
  sameParent(e) {
    return this.pos - this.parentOffset == e.pos - e.parentOffset;
  }
  /**
  Return the greater of this and the given position.
  */
  max(e) {
    return e.pos > this.pos ? e : this;
  }
  /**
  Return the smaller of this and the given position.
  */
  min(e) {
    return e.pos < this.pos ? e : this;
  }
  /**
  @internal
  */
  toString() {
    let e = "";
    for (let t = 1; t <= this.depth; t++)
      e += (e ? "/" : "") + this.node(t).type.name + "_" + this.index(t - 1);
    return e + ":" + this.parentOffset;
  }
  /**
  @internal
  */
  static resolve(e, t) {
    if (!(t >= 0 && t <= e.content.size))
      throw new RangeError("Position " + t + " out of range");
    let r = [], o = 0, i = t;
    for (let s = e; ; ) {
      let { index: l, offset: a } = s.content.findIndex(i), c = i - a;
      if (r.push(s, l, o + a), !c || (s = s.child(l), s.isText))
        break;
      i = c - 1, o += a + 1;
    }
    return new Ar(t, r, i);
  }
  /**
  @internal
  */
  static resolveCached(e, t) {
    let r = lc.get(e);
    if (r)
      for (let i = 0; i < r.elts.length; i++) {
        let s = r.elts[i];
        if (s.pos == t)
          return s;
      }
    else
      lc.set(e, r = new Bm());
    let o = r.elts[r.i] = Ar.resolve(e, t);
    return r.i = (r.i + 1) % Fm, o;
  }
}
class Bm {
  constructor() {
    this.elts = [], this.i = 0;
  }
}
const Fm = 12, lc = /* @__PURE__ */ new WeakMap();
class Wo {
  /**
  Construct a node range. `$from` and `$to` should point into the
  same node until at least the given `depth`, since a node range
  denotes an adjacent set of nodes in a single parent node.
  */
  constructor(e, t, r) {
    this.$from = e, this.$to = t, this.depth = r;
  }
  /**
  The position at the start of the range.
  */
  get start() {
    return this.$from.before(this.depth + 1);
  }
  /**
  The position at the end of the range.
  */
  get end() {
    return this.$to.after(this.depth + 1);
  }
  /**
  The parent node that the range points into.
  */
  get parent() {
    return this.$from.node(this.depth);
  }
  /**
  The start index of the range in the parent node.
  */
  get startIndex() {
    return this.$from.index(this.depth);
  }
  /**
  The end index of the range in the parent node.
  */
  get endIndex() {
    return this.$to.indexAfter(this.depth);
  }
}
const zm = /* @__PURE__ */ Object.create(null);
let jt = class ol {
  /**
  @internal
  */
  constructor(e, t, r, o = Z.none) {
    this.type = e, this.attrs = t, this.marks = o, this.content = r || M.empty;
  }
  /**
  The array of this node's child nodes.
  */
  get children() {
    return this.content.content;
  }
  /**
  The size of this node, as defined by the integer-based [indexing
  scheme](https://prosemirror.net/docs/guide/#doc.indexing). For text nodes, this is the
  amount of characters. For other leaf nodes, it is one. For
  non-leaf nodes, it is the size of the content plus two (the
  start and end token).
  */
  get nodeSize() {
    return this.isLeaf ? 1 : 2 + this.content.size;
  }
  /**
  The number of children that the node has.
  */
  get childCount() {
    return this.content.childCount;
  }
  /**
  Get the child node at the given index. Raises an error when the
  index is out of range.
  */
  child(e) {
    return this.content.child(e);
  }
  /**
  Get the child node at the given index, if it exists.
  */
  maybeChild(e) {
    return this.content.maybeChild(e);
  }
  /**
  Call `f` for every child node, passing the node, its offset
  into this parent node, and its index.
  */
  forEach(e) {
    this.content.forEach(e);
  }
  /**
  Invoke a callback for all descendant nodes recursively between
  the given two positions that are relative to start of this
  node's content. The callback is invoked with the node, its
  position relative to the original node (method receiver),
  its parent node, and its child index. When the callback returns
  false for a given node, that node's children will not be
  recursed over. The last parameter can be used to specify a
  starting position to count from.
  */
  nodesBetween(e, t, r, o = 0) {
    this.content.nodesBetween(e, t, r, o, this);
  }
  /**
  Call the given callback for every descendant node. Doesn't
  descend into a node when the callback returns `false`.
  */
  descendants(e) {
    this.nodesBetween(0, this.content.size, e);
  }
  /**
  Concatenates all the text nodes found in this fragment and its
  children.
  */
  get textContent() {
    return this.isLeaf && this.type.spec.leafText ? this.type.spec.leafText(this) : this.textBetween(0, this.content.size, "");
  }
  /**
  Get all text between positions `from` and `to`. When
  `blockSeparator` is given, it will be inserted to separate text
  from different block nodes. If `leafText` is given, it'll be
  inserted for every non-text leaf node encountered, otherwise
  [`leafText`](https://prosemirror.net/docs/ref/#model.NodeSpec.leafText) will be used.
  */
  textBetween(e, t, r, o) {
    return this.content.textBetween(e, t, r, o);
  }
  /**
  Returns this node's first child, or `null` if there are no
  children.
  */
  get firstChild() {
    return this.content.firstChild;
  }
  /**
  Returns this node's last child, or `null` if there are no
  children.
  */
  get lastChild() {
    return this.content.lastChild;
  }
  /**
  Test whether two nodes represent the same piece of document.
  */
  eq(e) {
    return this == e || this.sameMarkup(e) && this.content.eq(e.content);
  }
  /**
  Compare the markup (type, attributes, and marks) of this node to
  those of another. Returns `true` if both have the same markup.
  */
  sameMarkup(e) {
    return this.hasMarkup(e.type, e.attrs, e.marks);
  }
  /**
  Check whether this node's markup correspond to the given type,
  attributes, and marks.
  */
  hasMarkup(e, t, r) {
    return this.type == e && zo(this.attrs, t || e.defaultAttrs || zm) && Z.sameSet(this.marks, r || Z.none);
  }
  /**
  Create a new node with the same markup as this node, containing
  the given content (or empty, if no content is given).
  */
  copy(e = null) {
    return e == this.content ? this : new ol(this.type, this.attrs, e, this.marks);
  }
  /**
  Create a copy of this node, with the given set of marks instead
  of the node's own marks.
  */
  mark(e) {
    return e == this.marks ? this : new ol(this.type, this.attrs, this.content, e);
  }
  /**
  Create a copy of this node with only the content between the
  given positions. If `to` is not given, it defaults to the end of
  the node.
  */
  cut(e, t = this.content.size) {
    return e == 0 && t == this.content.size ? this : this.copy(this.content.cut(e, t));
  }
  /**
  Cut out the part of the document between the given positions, and
  return it as a `Slice` object.
  */
  slice(e, t = this.content.size, r = !1) {
    if (e == t)
      return O.empty;
    let o = this.resolve(e), i = this.resolve(t), s = r ? 0 : o.sharedDepth(t), l = o.start(s), c = o.node(s).content.cut(o.pos - l, i.pos - l);
    return new O(c, o.depth - s, i.depth - s);
  }
  /**
  Replace the part of the document between the given positions with
  the given slice. The slice must 'fit', meaning its open sides
  must be able to connect to the surrounding content, and its
  content nodes must be valid children for the node they are placed
  into. If any of this is violated, an error of type
  [`ReplaceError`](https://prosemirror.net/docs/ref/#model.ReplaceError) is thrown.
  */
  replace(e, t, r) {
    return Lm(this.resolve(e), this.resolve(t), r);
  }
  /**
  Find the node directly after the given position.
  */
  nodeAt(e) {
    for (let t = this; ; ) {
      let { index: r, offset: o } = t.content.findIndex(e);
      if (t = t.maybeChild(r), !t)
        return null;
      if (o == e || t.isText)
        return t;
      e -= o + 1;
    }
  }
  /**
  Find the (direct) child node after the given offset, if any,
  and return it along with its index and offset relative to this
  node.
  */
  childAfter(e) {
    let { index: t, offset: r } = this.content.findIndex(e);
    return { node: this.content.maybeChild(t), index: t, offset: r };
  }
  /**
  Find the (direct) child node before the given offset, if any,
  and return it along with its index and offset relative to this
  node.
  */
  childBefore(e) {
    if (e == 0)
      return { node: null, index: 0, offset: 0 };
    let { index: t, offset: r } = this.content.findIndex(e);
    if (r < e)
      return { node: this.content.child(t), index: t, offset: r };
    let o = this.content.child(t - 1);
    return { node: o, index: t - 1, offset: r - o.nodeSize };
  }
  /**
  Resolve the given position in the document, returning an
  [object](https://prosemirror.net/docs/ref/#model.ResolvedPos) with information about its context.
  */
  resolve(e) {
    return Ar.resolveCached(this, e);
  }
  /**
  @internal
  */
  resolveNoCache(e) {
    return Ar.resolve(this, e);
  }
  /**
  Test whether a given mark or mark type occurs in this document
  between the two given positions.
  */
  rangeHasMark(e, t, r) {
    let o = !1;
    return t > e && this.nodesBetween(e, t, (i) => (r.isInSet(i.marks) && (o = !0), !o)), o;
  }
  /**
  True when this is a block (non-inline node)
  */
  get isBlock() {
    return this.type.isBlock;
  }
  /**
  True when this is a textblock node, a block node with inline
  content.
  */
  get isTextblock() {
    return this.type.isTextblock;
  }
  /**
  True when this node allows inline content.
  */
  get inlineContent() {
    return this.type.inlineContent;
  }
  /**
  True when this is an inline node (a text node or a node that can
  appear among text).
  */
  get isInline() {
    return this.type.isInline;
  }
  /**
  True when this is a text node.
  */
  get isText() {
    return this.type.isText;
  }
  /**
  True when this is a leaf node.
  */
  get isLeaf() {
    return this.type.isLeaf;
  }
  /**
  True when this is an atom, i.e. when it does not have directly
  editable content. This is usually the same as `isLeaf`, but can
  be configured with the [`atom` property](https://prosemirror.net/docs/ref/#model.NodeSpec.atom)
  on a node's spec (typically used when the node is displayed as
  an uneditable [node view](https://prosemirror.net/docs/ref/#view.NodeView)).
  */
  get isAtom() {
    return this.type.isAtom;
  }
  /**
  Return a string representation of this node for debugging
  purposes.
  */
  toString() {
    if (this.type.spec.toDebugString)
      return this.type.spec.toDebugString(this);
    let e = this.type.name;
    return this.content.size && (e += "(" + this.content.toStringInner() + ")"), Sd(this.marks, e);
  }
  /**
  Get the content match in this node at the given index.
  */
  contentMatchAt(e) {
    let t = this.type.contentMatch.matchFragment(this.content, 0, e);
    if (!t)
      throw new Error("Called contentMatchAt on a node with invalid content");
    return t;
  }
  /**
  Test whether replacing the range between `from` and `to` (by
  child index) with the given replacement fragment (which defaults
  to the empty fragment) would leave the node's content valid. You
  can optionally pass `start` and `end` indices into the
  replacement fragment.
  */
  canReplace(e, t, r = M.empty, o = 0, i = r.childCount) {
    let s = this.contentMatchAt(e).matchFragment(r, o, i), l = s && s.matchFragment(this.content, t);
    if (!l || !l.validEnd)
      return !1;
    for (let a = o; a < i; a++)
      if (!this.type.allowsMarks(r.child(a).marks))
        return !1;
    return !0;
  }
  /**
  Test whether replacing the range `from` to `to` (by index) with
  a node of the given type would leave the node's content valid.
  */
  canReplaceWith(e, t, r, o) {
    if (o && !this.type.allowsMarks(o))
      return !1;
    let i = this.contentMatchAt(e).matchType(r), s = i && i.matchFragment(this.content, t);
    return s ? s.validEnd : !1;
  }
  /**
  Test whether the given node's content could be appended to this
  node. If that node is empty, this will only return true if there
  is at least one node type that can appear in both nodes (to avoid
  merging completely incompatible nodes).
  */
  canAppend(e) {
    return e.content.size ? this.canReplace(this.childCount, this.childCount, e.content) : this.type.compatibleContent(e.type);
  }
  /**
  Check whether this node and its descendants conform to the
  schema, and raise an exception when they do not.
  */
  check() {
    this.type.checkContent(this.content), this.type.checkAttrs(this.attrs);
    let e = Z.none;
    for (let t = 0; t < this.marks.length; t++) {
      let r = this.marks[t];
      r.type.checkAttrs(r.attrs), e = r.addToSet(e);
    }
    if (!Z.sameSet(e, this.marks))
      throw new RangeError(`Invalid collection of marks for node ${this.type.name}: ${this.marks.map((t) => t.type.name)}`);
    this.content.forEach((t) => t.check());
  }
  /**
  Return a JSON-serializeable representation of this node.
  */
  toJSON() {
    let e = { type: this.type.name };
    for (let t in this.attrs) {
      e.attrs = this.attrs;
      break;
    }
    return this.content.size && (e.content = this.content.toJSON()), this.marks.length && (e.marks = this.marks.map((t) => t.toJSON())), e;
  }
  /**
  Deserialize a node from its JSON representation.
  */
  static fromJSON(e, t) {
    if (!t)
      throw new RangeError("Invalid input for Node.fromJSON");
    let r;
    if (t.marks) {
      if (!Array.isArray(t.marks))
        throw new RangeError("Invalid mark data for Node.fromJSON");
      r = t.marks.map(e.markFromJSON);
    }
    if (t.type == "text") {
      if (typeof t.text != "string")
        throw new RangeError("Invalid text node in JSON");
      return e.text(t.text, r);
    }
    let o = M.fromJSON(e, t.content), i = e.nodeType(t.type).create(t.attrs, o, r);
    return i.type.checkAttrs(i.attrs), i;
  }
};
jt.prototype.text = void 0;
class Vo extends jt {
  /**
  @internal
  */
  constructor(e, t, r, o) {
    if (super(e, t, null, o), !r)
      throw new RangeError("Empty text nodes are not allowed");
    this.text = r;
  }
  toString() {
    return this.type.spec.toDebugString ? this.type.spec.toDebugString(this) : Sd(this.marks, JSON.stringify(this.text));
  }
  get textContent() {
    return this.text;
  }
  textBetween(e, t) {
    return this.text.slice(e, t);
  }
  get nodeSize() {
    return this.text.length;
  }
  mark(e) {
    return e == this.marks ? this : new Vo(this.type, this.attrs, this.text, e);
  }
  withText(e) {
    return e == this.text ? this : new Vo(this.type, this.attrs, e, this.marks);
  }
  cut(e = 0, t = this.text.length) {
    return e == 0 && t == this.text.length ? this : this.withText(this.text.slice(e, t));
  }
  eq(e) {
    return this.sameMarkup(e) && this.text == e.text;
  }
  toJSON() {
    let e = super.toJSON();
    return e.text = this.text, e;
  }
}
function Sd(n, e) {
  for (let t = n.length - 1; t >= 0; t--)
    e = n[t].type.name + "(" + e + ")";
  return e;
}
class bn {
  /**
  @internal
  */
  constructor(e) {
    this.validEnd = e, this.next = [], this.wrapCache = [];
  }
  /**
  @internal
  */
  static parse(e, t) {
    let r = new $m(e, t);
    if (r.next == null)
      return bn.empty;
    let o = xd(r);
    r.next && r.err("Unexpected trailing text");
    let i = qm(Km(o));
    return Gm(i, r), i;
  }
  /**
  Match a node type, returning a match after that node if
  successful.
  */
  matchType(e) {
    for (let t = 0; t < this.next.length; t++)
      if (this.next[t].type == e)
        return this.next[t].next;
    return null;
  }
  /**
  Try to match a fragment. Returns the resulting match when
  successful.
  */
  matchFragment(e, t = 0, r = e.childCount) {
    let o = this;
    for (let i = t; o && i < r; i++)
      o = o.matchType(e.child(i).type);
    return o;
  }
  /**
  @internal
  */
  get inlineContent() {
    return this.next.length != 0 && this.next[0].type.isInline;
  }
  /**
  Get the first matching node type at this match position that can
  be generated.
  */
  get defaultType() {
    for (let e = 0; e < this.next.length; e++) {
      let { type: t } = this.next[e];
      if (!(t.isText || t.hasRequiredAttrs()))
        return t;
    }
    return null;
  }
  /**
  @internal
  */
  compatible(e) {
    for (let t = 0; t < this.next.length; t++)
      for (let r = 0; r < e.next.length; r++)
        if (this.next[t].type == e.next[r].type)
          return !0;
    return !1;
  }
  /**
  Try to match the given fragment, and if that fails, see if it can
  be made to match by inserting nodes in front of it. When
  successful, return a fragment of inserted nodes (which may be
  empty if nothing had to be inserted). When `toEnd` is true, only
  return a fragment if the resulting match goes to the end of the
  content expression.
  */
  fillBefore(e, t = !1, r = 0) {
    let o = [this];
    function i(s, l) {
      let a = s.matchFragment(e, r);
      if (a && (!t || a.validEnd))
        return M.from(l.map((c) => c.createAndFill()));
      for (let c = 0; c < s.next.length; c++) {
        let { type: u, next: d } = s.next[c];
        if (!(u.isText || u.hasRequiredAttrs()) && o.indexOf(d) == -1) {
          o.push(d);
          let f = i(d, l.concat(u));
          if (f)
            return f;
        }
      }
      return null;
    }
    return i(this, []);
  }
  /**
  Find a set of wrapping node types that would allow a node of the
  given type to appear at this position. The result may be empty
  (when it fits directly) and will be null when no such wrapping
  exists.
  */
  findWrapping(e) {
    for (let r = 0; r < this.wrapCache.length; r += 2)
      if (this.wrapCache[r] == e)
        return this.wrapCache[r + 1];
    let t = this.computeWrapping(e);
    return this.wrapCache.push(e, t), t;
  }
  /**
  @internal
  */
  computeWrapping(e) {
    let t = /* @__PURE__ */ Object.create(null), r = [{ match: this, type: null, via: null }];
    for (; r.length; ) {
      let o = r.shift(), i = o.match;
      if (i.matchType(e)) {
        let s = [];
        for (let l = o; l.type; l = l.via)
          s.push(l.type);
        return s.reverse();
      }
      for (let s = 0; s < i.next.length; s++) {
        let { type: l, next: a } = i.next[s];
        !l.isLeaf && !l.hasRequiredAttrs() && !(l.name in t) && (!o.type || a.validEnd) && (r.push({ match: l.contentMatch, type: l, via: o }), t[l.name] = !0);
      }
    }
    return null;
  }
  /**
  The number of outgoing edges this node has in the finite
  automaton that describes the content expression.
  */
  get edgeCount() {
    return this.next.length;
  }
  /**
  Get the _n_​th outgoing edge from this node in the finite
  automaton that describes the content expression.
  */
  edge(e) {
    if (e >= this.next.length)
      throw new RangeError(`There's no ${e}th edge in this content match`);
    return this.next[e];
  }
  /**
  @internal
  */
  toString() {
    let e = [];
    function t(r) {
      e.push(r);
      for (let o = 0; o < r.next.length; o++)
        e.indexOf(r.next[o].next) == -1 && t(r.next[o].next);
    }
    return t(this), e.map((r, o) => {
      let i = o + (r.validEnd ? "*" : " ") + " ";
      for (let s = 0; s < r.next.length; s++)
        i += (s ? ", " : "") + r.next[s].type.name + "->" + e.indexOf(r.next[s].next);
      return i;
    }).join(`
`);
  }
}
bn.empty = new bn(!0);
class $m {
  constructor(e, t) {
    this.string = e, this.nodeTypes = t, this.inline = null, this.pos = 0, this.tokens = e.split(/\s*(?=\b|\W|$)/), this.tokens[this.tokens.length - 1] == "" && this.tokens.pop(), this.tokens[0] == "" && this.tokens.shift();
  }
  get next() {
    return this.tokens[this.pos];
  }
  eat(e) {
    return this.next == e && (this.pos++ || !0);
  }
  err(e) {
    throw new SyntaxError(e + " (in content expression '" + this.string + "')");
  }
}
function xd(n) {
  let e = [];
  do
    e.push(Hm(n));
  while (n.eat("|"));
  return e.length == 1 ? e[0] : { type: "choice", exprs: e };
}
function Hm(n) {
  let e = [];
  do
    e.push(Wm(n));
  while (n.next && n.next != ")" && n.next != "|");
  return e.length == 1 ? e[0] : { type: "seq", exprs: e };
}
function Wm(n) {
  let e = Um(n);
  for (; ; )
    if (n.eat("+"))
      e = { type: "plus", expr: e };
    else if (n.eat("*"))
      e = { type: "star", expr: e };
    else if (n.eat("?"))
      e = { type: "opt", expr: e };
    else if (n.eat("{"))
      e = Vm(n, e);
    else
      break;
  return e;
}
function ac(n) {
  /\D/.test(n.next) && n.err("Expected number, got '" + n.next + "'");
  let e = Number(n.next);
  return n.pos++, e;
}
function Vm(n, e) {
  let t = ac(n), r = t;
  return n.eat(",") && (n.next != "}" ? r = ac(n) : r = -1), n.eat("}") || n.err("Unclosed braced range"), { type: "range", min: t, max: r, expr: e };
}
function jm(n, e) {
  let t = n.nodeTypes, r = t[e];
  if (r)
    return [r];
  let o = [];
  for (let i in t) {
    let s = t[i];
    s.isInGroup(e) && o.push(s);
  }
  return o.length == 0 && n.err("No node type or group '" + e + "' found"), o;
}
function Um(n) {
  if (n.eat("(")) {
    let e = xd(n);
    return n.eat(")") || n.err("Missing closing paren"), e;
  } else if (/\W/.test(n.next))
    n.err("Unexpected token '" + n.next + "'");
  else {
    let e = jm(n, n.next).map((t) => (n.inline == null ? n.inline = t.isInline : n.inline != t.isInline && n.err("Mixing inline and block content"), { type: "name", value: t }));
    return n.pos++, e.length == 1 ? e[0] : { type: "choice", exprs: e };
  }
}
function Km(n) {
  let e = [[]];
  return o(i(n, 0), t()), e;
  function t() {
    return e.push([]) - 1;
  }
  function r(s, l, a) {
    let c = { term: a, to: l };
    return e[s].push(c), c;
  }
  function o(s, l) {
    s.forEach((a) => a.to = l);
  }
  function i(s, l) {
    if (s.type == "choice")
      return s.exprs.reduce((a, c) => a.concat(i(c, l)), []);
    if (s.type == "seq")
      for (let a = 0; ; a++) {
        let c = i(s.exprs[a], l);
        if (a == s.exprs.length - 1)
          return c;
        o(c, l = t());
      }
    else if (s.type == "star") {
      let a = t();
      return r(l, a), o(i(s.expr, a), a), [r(a)];
    } else if (s.type == "plus") {
      let a = t();
      return o(i(s.expr, l), a), o(i(s.expr, a), a), [r(a)];
    } else {
      if (s.type == "opt")
        return [r(l)].concat(i(s.expr, l));
      if (s.type == "range") {
        let a = l;
        for (let c = 0; c < s.min; c++) {
          let u = t();
          o(i(s.expr, a), u), a = u;
        }
        if (s.max == -1)
          o(i(s.expr, a), a);
        else
          for (let c = s.min; c < s.max; c++) {
            let u = t();
            r(a, u), o(i(s.expr, a), u), a = u;
          }
        return [r(a)];
      } else {
        if (s.type == "name")
          return [r(l, void 0, s.value)];
        throw new Error("Unknown expr type");
      }
    }
  }
}
function kd(n, e) {
  return e - n;
}
function cc(n, e) {
  let t = [];
  return r(e), t.sort(kd);
  function r(o) {
    let i = n[o];
    if (i.length == 1 && !i[0].term)
      return r(i[0].to);
    t.push(o);
    for (let s = 0; s < i.length; s++) {
      let { term: l, to: a } = i[s];
      !l && t.indexOf(a) == -1 && r(a);
    }
  }
}
function qm(n) {
  let e = /* @__PURE__ */ Object.create(null);
  return t(cc(n, 0));
  function t(r) {
    let o = [];
    r.forEach((s) => {
      n[s].forEach(({ term: l, to: a }) => {
        if (!l)
          return;
        let c;
        for (let u = 0; u < o.length; u++)
          o[u][0] == l && (c = o[u][1]);
        cc(n, a).forEach((u) => {
          c || o.push([l, c = []]), c.indexOf(u) == -1 && c.push(u);
        });
      });
    });
    let i = e[r.join(",")] = new bn(r.indexOf(n.length - 1) > -1);
    for (let s = 0; s < o.length; s++) {
      let l = o[s][1].sort(kd);
      i.next.push({ type: o[s][0], next: e[l.join(",")] || t(l) });
    }
    return i;
  }
}
function Gm(n, e) {
  for (let t = 0, r = [n]; t < r.length; t++) {
    let o = r[t], i = !o.validEnd, s = [];
    for (let l = 0; l < o.next.length; l++) {
      let { type: a, next: c } = o.next[l];
      s.push(a.name), i && !(a.isText || a.hasRequiredAttrs()) && (i = !1), r.indexOf(c) == -1 && r.push(c);
    }
    i && e.err("Only non-generatable nodes (" + s.join(", ") + ") in a required position (see https://prosemirror.net/docs/guide/#generatable)");
  }
}
function Ed(n) {
  let e = /* @__PURE__ */ Object.create(null);
  for (let t in n) {
    let r = n[t];
    if (!r.hasDefault)
      return null;
    e[t] = r.default;
  }
  return e;
}
function Md(n, e) {
  let t = /* @__PURE__ */ Object.create(null);
  for (let r in n) {
    let o = e && e[r];
    if (o === void 0) {
      let i = n[r];
      if (i.hasDefault)
        o = i.default;
      else
        throw new RangeError("No value supplied for attribute " + r);
    }
    t[r] = o;
  }
  return t;
}
function Td(n, e, t, r) {
  for (let o in e)
    if (!(o in n))
      throw new RangeError(`Unsupported attribute ${o} for ${t} of type ${o}`);
  for (let o in n) {
    let i = n[o];
    i.validate && i.validate(e[o]);
  }
}
function Ad(n, e) {
  let t = /* @__PURE__ */ Object.create(null);
  if (e)
    for (let r in e)
      t[r] = new Ym(n, r, e[r]);
  return t;
}
let uc = class Od {
  /**
  @internal
  */
  constructor(e, t, r) {
    this.name = e, this.schema = t, this.spec = r, this.markSet = null, this.groups = r.group ? r.group.split(" ") : [], this.attrs = Ad(e, r.attrs), this.defaultAttrs = Ed(this.attrs), this.contentMatch = null, this.inlineContent = null, this.isBlock = !(r.inline || e == "text"), this.isText = e == "text";
  }
  /**
  True if this is an inline type.
  */
  get isInline() {
    return !this.isBlock;
  }
  /**
  True if this is a textblock type, a block that contains inline
  content.
  */
  get isTextblock() {
    return this.isBlock && this.inlineContent;
  }
  /**
  True for node types that allow no content.
  */
  get isLeaf() {
    return this.contentMatch == bn.empty;
  }
  /**
  True when this node is an atom, i.e. when it does not have
  directly editable content.
  */
  get isAtom() {
    return this.isLeaf || !!this.spec.atom;
  }
  /**
  Return true when this node type is part of the given
  [group](https://prosemirror.net/docs/ref/#model.NodeSpec.group).
  */
  isInGroup(e) {
    return this.groups.indexOf(e) > -1;
  }
  /**
  The node type's [whitespace](https://prosemirror.net/docs/ref/#model.NodeSpec.whitespace) option.
  */
  get whitespace() {
    return this.spec.whitespace || (this.spec.code ? "pre" : "normal");
  }
  /**
  Tells you whether this node type has any required attributes.
  */
  hasRequiredAttrs() {
    for (let e in this.attrs)
      if (this.attrs[e].isRequired)
        return !0;
    return !1;
  }
  /**
  Indicates whether this node allows some of the same content as
  the given node type.
  */
  compatibleContent(e) {
    return this == e || this.contentMatch.compatible(e.contentMatch);
  }
  /**
  @internal
  */
  computeAttrs(e) {
    return !e && this.defaultAttrs ? this.defaultAttrs : Md(this.attrs, e);
  }
  /**
  Create a `Node` of this type. The given attributes are
  checked and defaulted (you can pass `null` to use the type's
  defaults entirely, if no required attributes exist). `content`
  may be a `Fragment`, a node, an array of nodes, or
  `null`. Similarly `marks` may be `null` to default to the empty
  set of marks.
  */
  create(e = null, t, r) {
    if (this.isText)
      throw new Error("NodeType.create can't construct text nodes");
    return new jt(this, this.computeAttrs(e), M.from(t), Z.setFrom(r));
  }
  /**
  Like [`create`](https://prosemirror.net/docs/ref/#model.NodeType.create), but check the given content
  against the node type's content restrictions, and throw an error
  if it doesn't match.
  */
  createChecked(e = null, t, r) {
    return t = M.from(t), this.checkContent(t), new jt(this, this.computeAttrs(e), t, Z.setFrom(r));
  }
  /**
  Like [`create`](https://prosemirror.net/docs/ref/#model.NodeType.create), but see if it is
  necessary to add nodes to the start or end of the given fragment
  to make it fit the node. If no fitting wrapping can be found,
  return null. Note that, due to the fact that required nodes can
  always be created, this will always succeed if you pass null or
  `Fragment.empty` as content.
  */
  createAndFill(e = null, t, r) {
    if (e = this.computeAttrs(e), t = M.from(t), t.size) {
      let s = this.contentMatch.fillBefore(t);
      if (!s)
        return null;
      t = s.append(t);
    }
    let o = this.contentMatch.matchFragment(t), i = o && o.fillBefore(M.empty, !0);
    return i ? new jt(this, e, t.append(i), Z.setFrom(r)) : null;
  }
  /**
  Returns true if the given fragment is valid content for this node
  type.
  */
  validContent(e) {
    let t = this.contentMatch.matchFragment(e);
    if (!t || !t.validEnd)
      return !1;
    for (let r = 0; r < e.childCount; r++)
      if (!this.allowsMarks(e.child(r).marks))
        return !1;
    return !0;
  }
  /**
  Throws a RangeError if the given fragment is not valid content for this
  node type.
  @internal
  */
  checkContent(e) {
    if (!this.validContent(e))
      throw new RangeError(`Invalid content for node ${this.name}: ${e.toString().slice(0, 50)}`);
  }
  /**
  @internal
  */
  checkAttrs(e) {
    Td(this.attrs, e, "node", this.name);
  }
  /**
  Check whether the given mark type is allowed in this node.
  */
  allowsMarkType(e) {
    return this.markSet == null || this.markSet.indexOf(e) > -1;
  }
  /**
  Test whether the given set of marks are allowed in this node.
  */
  allowsMarks(e) {
    if (this.markSet == null)
      return !0;
    for (let t = 0; t < e.length; t++)
      if (!this.allowsMarkType(e[t].type))
        return !1;
    return !0;
  }
  /**
  Removes the marks that are not allowed in this node from the given set.
  */
  allowedMarks(e) {
    if (this.markSet == null)
      return e;
    let t;
    for (let r = 0; r < e.length; r++)
      this.allowsMarkType(e[r].type) ? t && t.push(e[r]) : t || (t = e.slice(0, r));
    return t ? t.length ? t : Z.none : e;
  }
  /**
  @internal
  */
  static compile(e, t) {
    let r = /* @__PURE__ */ Object.create(null);
    e.forEach((i, s) => r[i] = new Od(i, t, s));
    let o = t.spec.topNode || "doc";
    if (!r[o])
      throw new RangeError("Schema is missing its top node type ('" + o + "')");
    if (!r.text)
      throw new RangeError("Every schema needs a 'text' type");
    for (let i in r.text.attrs)
      throw new RangeError("The text node type should not have attributes");
    return r;
  }
};
function Jm(n, e, t) {
  let r = t.split("|");
  return (o) => {
    let i = o === null ? "null" : typeof o;
    if (r.indexOf(i) < 0)
      throw new RangeError(`Expected value of type ${r} for attribute ${e} on type ${n}, got ${i}`);
  };
}
class Ym {
  constructor(e, t, r) {
    this.hasDefault = Object.prototype.hasOwnProperty.call(r, "default"), this.default = r.default, this.validate = typeof r.validate == "string" ? Jm(e, t, r.validate) : r.validate;
  }
  get isRequired() {
    return !this.hasDefault;
  }
}
class Ii {
  /**
  @internal
  */
  constructor(e, t, r, o) {
    this.name = e, this.rank = t, this.schema = r, this.spec = o, this.attrs = Ad(e, o.attrs), this.excluded = null;
    let i = Ed(this.attrs);
    this.instance = i ? new Z(this, i) : null;
  }
  /**
  Create a mark of this type. `attrs` may be `null` or an object
  containing only some of the mark's attributes. The others, if
  they have defaults, will be added.
  */
  create(e = null) {
    return !e && this.instance ? this.instance : new Z(this, Md(this.attrs, e));
  }
  /**
  @internal
  */
  static compile(e, t) {
    let r = /* @__PURE__ */ Object.create(null), o = 0;
    return e.forEach((i, s) => r[i] = new Ii(i, o++, t, s)), r;
  }
  /**
  When there is a mark of this type in the given set, a new set
  without it is returned. Otherwise, the input set is returned.
  */
  removeFromSet(e) {
    for (var t = 0; t < e.length; t++)
      e[t].type == this && (e = e.slice(0, t).concat(e.slice(t + 1)), t--);
    return e;
  }
  /**
  Tests whether there is a mark of this type in the given set.
  */
  isInSet(e) {
    for (let t = 0; t < e.length; t++)
      if (e[t].type == this)
        return e[t];
  }
  /**
  @internal
  */
  checkAttrs(e) {
    Td(this.attrs, e, "mark", this.name);
  }
  /**
  Queries whether a given mark type is
  [excluded](https://prosemirror.net/docs/ref/#model.MarkSpec.excludes) by this one.
  */
  excludes(e) {
    return this.excluded.indexOf(e) > -1;
  }
}
class Nd {
  /**
  Construct a schema from a schema [specification](https://prosemirror.net/docs/ref/#model.SchemaSpec).
  */
  constructor(e) {
    this.linebreakReplacement = null, this.cached = /* @__PURE__ */ Object.create(null);
    let t = this.spec = {};
    for (let o in e)
      t[o] = e[o];
    t.nodes = ye.from(e.nodes), t.marks = ye.from(e.marks || {}), this.nodes = uc.compile(this.spec.nodes, this), this.marks = Ii.compile(this.spec.marks, this);
    let r = /* @__PURE__ */ Object.create(null);
    for (let o in this.nodes) {
      if (o in this.marks)
        throw new RangeError(o + " can not be both a node and a mark");
      let i = this.nodes[o], s = i.spec.content || "", l = i.spec.marks;
      if (i.contentMatch = r[s] || (r[s] = bn.parse(s, this.nodes)), i.inlineContent = i.contentMatch.inlineContent, i.spec.linebreakReplacement) {
        if (this.linebreakReplacement)
          throw new RangeError("Multiple linebreak nodes defined");
        if (!i.isInline || !i.isLeaf)
          throw new RangeError("Linebreak replacement nodes must be inline leaf nodes");
        this.linebreakReplacement = i;
      }
      i.markSet = l == "_" ? null : l ? dc(this, l.split(" ")) : l == "" || !i.inlineContent ? [] : null;
    }
    for (let o in this.marks) {
      let i = this.marks[o], s = i.spec.excludes;
      i.excluded = s == null ? [i] : s == "" ? [] : dc(this, s.split(" "));
    }
    this.nodeFromJSON = (o) => jt.fromJSON(this, o), this.markFromJSON = (o) => Z.fromJSON(this, o), this.topNodeType = this.nodes[this.spec.topNode || "doc"], this.cached.wrappings = /* @__PURE__ */ Object.create(null);
  }
  /**
  Create a node in this schema. The `type` may be a string or a
  `NodeType` instance. Attributes will be extended with defaults,
  `content` may be a `Fragment`, `null`, a `Node`, or an array of
  nodes.
  */
  node(e, t = null, r, o) {
    if (typeof e == "string")
      e = this.nodeType(e);
    else if (e instanceof uc) {
      if (e.schema != this)
        throw new RangeError("Node type from different schema used (" + e.name + ")");
    } else throw new RangeError("Invalid node type: " + e);
    return e.createChecked(t, r, o);
  }
  /**
  Create a text node in the schema. Empty text nodes are not
  allowed.
  */
  text(e, t) {
    let r = this.nodes.text;
    return new Vo(r, r.defaultAttrs, e, Z.setFrom(t));
  }
  /**
  Create a mark with the given type and attributes.
  */
  mark(e, t) {
    return typeof e == "string" && (e = this.marks[e]), e.create(t);
  }
  /**
  @internal
  */
  nodeType(e) {
    let t = this.nodes[e];
    if (!t)
      throw new RangeError("Unknown node type: " + e);
    return t;
  }
}
function dc(n, e) {
  let t = [];
  for (let r = 0; r < e.length; r++) {
    let o = e[r], i = n.marks[o], s = i;
    if (i)
      t.push(i);
    else
      for (let l in n.marks) {
        let a = n.marks[l];
        (o == "_" || a.spec.group && a.spec.group.split(" ").indexOf(o) > -1) && t.push(s = a);
      }
    if (!s)
      throw new SyntaxError("Unknown mark type: '" + e[r] + "'");
  }
  return t;
}
function Xm(n) {
  return n.tag != null;
}
function Qm(n) {
  return n.style != null;
}
class Ut {
  /**
  Create a parser that targets the given schema, using the given
  parsing rules.
  */
  constructor(e, t) {
    this.schema = e, this.rules = t, this.tags = [], this.styles = [];
    let r = this.matchedStyles = [];
    t.forEach((o) => {
      if (Xm(o))
        this.tags.push(o);
      else if (Qm(o)) {
        let i = /[^=]*/.exec(o.style)[0];
        r.indexOf(i) < 0 && r.push(i), this.styles.push(o);
      }
    }), this.normalizeLists = !this.tags.some((o) => {
      if (!/^(ul|ol)\b/.test(o.tag) || !o.node)
        return !1;
      let i = e.nodes[o.node];
      return i.contentMatch.matchType(i);
    });
  }
  /**
  Parse a document from the content of a DOM node.
  */
  parse(e, t = {}) {
    let r = new hc(this, t, !1);
    return r.addAll(e, Z.none, t.from, t.to), r.finish();
  }
  /**
  Parses the content of the given DOM node, like
  [`parse`](https://prosemirror.net/docs/ref/#model.DOMParser.parse), and takes the same set of
  options. But unlike that method, which produces a whole node,
  this one returns a slice that is open at the sides, meaning that
  the schema constraints aren't applied to the start of nodes to
  the left of the input and the end of nodes at the end.
  */
  parseSlice(e, t = {}) {
    let r = new hc(this, t, !0);
    return r.addAll(e, Z.none, t.from, t.to), O.maxOpen(r.finish());
  }
  /**
  @internal
  */
  matchTag(e, t, r) {
    for (let o = r ? this.tags.indexOf(r) + 1 : 0; o < this.tags.length; o++) {
      let i = this.tags[o];
      if (tg(e, i.tag) && (i.namespace === void 0 || e.namespaceURI == i.namespace) && (!i.context || t.matchesContext(i.context))) {
        if (i.getAttrs) {
          let s = i.getAttrs(e);
          if (s === !1)
            continue;
          i.attrs = s || void 0;
        }
        return i;
      }
    }
  }
  /**
  @internal
  */
  matchStyle(e, t, r, o) {
    for (let i = o ? this.styles.indexOf(o) + 1 : 0; i < this.styles.length; i++) {
      let s = this.styles[i], l = s.style;
      if (!(l.indexOf(e) != 0 || s.context && !r.matchesContext(s.context) || // Test that the style string either precisely matches the prop,
      // or has an '=' sign after the prop, followed by the given
      // value.
      l.length > e.length && (l.charCodeAt(e.length) != 61 || l.slice(e.length + 1) != t))) {
        if (s.getAttrs) {
          let a = s.getAttrs(t);
          if (a === !1)
            continue;
          s.attrs = a || void 0;
        }
        return s;
      }
    }
  }
  /**
  @internal
  */
  static schemaRules(e) {
    let t = [];
    function r(o) {
      let i = o.priority == null ? 50 : o.priority, s = 0;
      for (; s < t.length; s++) {
        let l = t[s];
        if ((l.priority == null ? 50 : l.priority) < i)
          break;
      }
      t.splice(s, 0, o);
    }
    for (let o in e.marks) {
      let i = e.marks[o].spec.parseDOM;
      i && i.forEach((s) => {
        r(s = pc(s)), s.mark || s.ignore || s.clearMark || (s.mark = o);
      });
    }
    for (let o in e.nodes) {
      let i = e.nodes[o].spec.parseDOM;
      i && i.forEach((s) => {
        r(s = pc(s)), s.node || s.ignore || s.mark || (s.node = o);
      });
    }
    return t;
  }
  /**
  Construct a DOM parser using the parsing rules listed in a
  schema's [node specs](https://prosemirror.net/docs/ref/#model.NodeSpec.parseDOM), reordered by
  [priority](https://prosemirror.net/docs/ref/#model.GenericParseRule.priority).
  */
  static fromSchema(e) {
    return e.cached.domParser || (e.cached.domParser = new Ut(e, Ut.schemaRules(e)));
  }
}
const Rd = {
  address: !0,
  article: !0,
  aside: !0,
  blockquote: !0,
  canvas: !0,
  dd: !0,
  div: !0,
  dl: !0,
  fieldset: !0,
  figcaption: !0,
  figure: !0,
  footer: !0,
  form: !0,
  h1: !0,
  h2: !0,
  h3: !0,
  h4: !0,
  h5: !0,
  h6: !0,
  header: !0,
  hgroup: !0,
  hr: !0,
  li: !0,
  noscript: !0,
  ol: !0,
  output: !0,
  p: !0,
  pre: !0,
  section: !0,
  table: !0,
  tfoot: !0,
  ul: !0
}, Zm = {
  head: !0,
  noscript: !0,
  object: !0,
  script: !0,
  style: !0,
  title: !0
}, Dd = { ol: !0, ul: !0 }, Or = 1, il = 2, wr = 4;
function fc(n, e, t) {
  return e != null ? (e ? Or : 0) | (e === "full" ? il : 0) : n && n.whitespace == "pre" ? Or | il : t & ~wr;
}
class lo {
  constructor(e, t, r, o, i, s) {
    this.type = e, this.attrs = t, this.marks = r, this.solid = o, this.options = s, this.content = [], this.activeMarks = Z.none, this.match = i || (s & wr ? null : e.contentMatch);
  }
  findWrapping(e) {
    if (!this.match) {
      if (!this.type)
        return [];
      let t = this.type.contentMatch.fillBefore(M.from(e));
      if (t)
        this.match = this.type.contentMatch.matchFragment(t);
      else {
        let r = this.type.contentMatch, o;
        return (o = r.findWrapping(e.type)) ? (this.match = r, o) : null;
      }
    }
    return this.match.findWrapping(e.type);
  }
  finish(e) {
    if (!(this.options & Or)) {
      let r = this.content[this.content.length - 1], o;
      if (r && r.isText && (o = /[ \t\r\n\u000c]+$/.exec(r.text))) {
        let i = r;
        r.text.length == o[0].length ? this.content.pop() : this.content[this.content.length - 1] = i.withText(i.text.slice(0, i.text.length - o[0].length));
      }
    }
    let t = M.from(this.content);
    return !e && this.match && (t = t.append(this.match.fillBefore(M.empty, !0))), this.type ? this.type.create(this.attrs, t, this.marks) : t;
  }
  inlineContext(e) {
    return this.type ? this.type.inlineContent : this.content.length ? this.content[0].isInline : e.parentNode && !Rd.hasOwnProperty(e.parentNode.nodeName.toLowerCase());
  }
}
class hc {
  constructor(e, t, r) {
    this.parser = e, this.options = t, this.isOpen = r, this.open = 0, this.localPreserveWS = !1;
    let o = t.topNode, i, s = fc(null, t.preserveWhitespace, 0) | (r ? wr : 0);
    o ? i = new lo(o.type, o.attrs, Z.none, !0, t.topMatch || o.type.contentMatch, s) : r ? i = new lo(null, null, Z.none, !0, null, s) : i = new lo(e.schema.topNodeType, null, Z.none, !0, null, s), this.nodes = [i], this.find = t.findPositions, this.needsBlock = !1;
  }
  get top() {
    return this.nodes[this.open];
  }
  // Add a DOM node to the content. Text is inserted as text node,
  // otherwise, the node is passed to `addElement` or, if it has a
  // `style` attribute, `addElementWithStyles`.
  addDOM(e, t) {
    e.nodeType == 3 ? this.addTextNode(e, t) : e.nodeType == 1 && this.addElement(e, t);
  }
  addTextNode(e, t) {
    let r = e.nodeValue, o = this.top, i = o.options & il ? "full" : this.localPreserveWS || (o.options & Or) > 0, { schema: s } = this.parser;
    if (i === "full" || o.inlineContext(e) || /[^ \t\r\n\u000c]/.test(r)) {
      if (i)
        if (i === "full")
          r = r.replace(/\r\n?/g, `
`);
        else if (s.linebreakReplacement && /[\r\n]/.test(r) && this.top.findWrapping(s.linebreakReplacement.create())) {
          let l = r.split(/\r?\n|\r/);
          for (let a = 0; a < l.length; a++)
            a && this.insertNode(s.linebreakReplacement.create(), t, !0), l[a] && this.insertNode(s.text(l[a]), t, !/\S/.test(l[a]));
          r = "";
        } else
          r = r.replace(/\r?\n|\r/g, " ");
      else if (r = r.replace(/[ \t\r\n\u000c]+/g, " "), /^[ \t\r\n\u000c]/.test(r) && this.open == this.nodes.length - 1) {
        let l = o.content[o.content.length - 1], a = e.previousSibling;
        (!l || a && a.nodeName == "BR" || l.isText && /[ \t\r\n\u000c]$/.test(l.text)) && (r = r.slice(1));
      }
      r && this.insertNode(s.text(r), t, !/\S/.test(r)), this.findInText(e);
    } else
      this.findInside(e);
  }
  // Try to find a handler for the given tag and use that to parse. If
  // none is found, the element's content nodes are added directly.
  addElement(e, t, r) {
    let o = this.localPreserveWS, i = this.top;
    (e.tagName == "PRE" || /pre/.test(e.style && e.style.whiteSpace)) && (this.localPreserveWS = !0);
    let s = e.nodeName.toLowerCase(), l;
    Dd.hasOwnProperty(s) && this.parser.normalizeLists && eg(e);
    let a = this.options.ruleFromNode && this.options.ruleFromNode(e) || (l = this.parser.matchTag(e, this, r));
    e: if (a ? a.ignore : Zm.hasOwnProperty(s))
      this.findInside(e), this.ignoreFallback(e, t);
    else if (!a || a.skip || a.closeParent) {
      a && a.closeParent ? this.open = Math.max(0, this.open - 1) : a && a.skip.nodeType && (e = a.skip);
      let c, u = this.needsBlock;
      if (Rd.hasOwnProperty(s))
        i.content.length && i.content[0].isInline && this.open && (this.open--, i = this.top), c = !0, i.type || (this.needsBlock = !0);
      else if (!e.firstChild) {
        this.leafFallback(e, t);
        break e;
      }
      let d = a && a.skip ? t : this.readStyles(e, t);
      d && this.addAll(e, d), c && this.sync(i), this.needsBlock = u;
    } else {
      let c = this.readStyles(e, t);
      c && this.addElementByRule(e, a, c, a.consuming === !1 ? l : void 0);
    }
    this.localPreserveWS = o;
  }
  // Called for leaf DOM nodes that would otherwise be ignored
  leafFallback(e, t) {
    e.nodeName == "BR" && this.top.type && this.top.type.inlineContent && this.addTextNode(e.ownerDocument.createTextNode(`
`), t);
  }
  // Called for ignored nodes
  ignoreFallback(e, t) {
    e.nodeName == "BR" && (!this.top.type || !this.top.type.inlineContent) && this.findPlace(this.parser.schema.text("-"), t, !0);
  }
  // Run any style parser associated with the node's styles. Either
  // return an updated array of marks, or null to indicate some of the
  // styles had a rule with `ignore` set.
  readStyles(e, t) {
    let r = e.style;
    if (r && r.length)
      for (let o = 0; o < this.parser.matchedStyles.length; o++) {
        let i = this.parser.matchedStyles[o], s = r.getPropertyValue(i);
        if (s)
          for (let l = void 0; ; ) {
            let a = this.parser.matchStyle(i, s, this, l);
            if (!a)
              break;
            if (a.ignore)
              return null;
            if (a.clearMark ? t = t.filter((c) => !a.clearMark(c)) : t = t.concat(this.parser.schema.marks[a.mark].create(a.attrs)), a.consuming === !1)
              l = a;
            else
              break;
          }
      }
    return t;
  }
  // Look up a handler for the given node. If none are found, return
  // false. Otherwise, apply it, use its return value to drive the way
  // the node's content is wrapped, and return true.
  addElementByRule(e, t, r, o) {
    let i, s;
    if (t.node)
      if (s = this.parser.schema.nodes[t.node], s.isLeaf)
        this.insertNode(s.create(t.attrs), r, e.nodeName == "BR") || this.leafFallback(e, r);
      else {
        let a = this.enter(s, t.attrs || null, r, t.preserveWhitespace);
        a && (i = !0, r = a);
      }
    else {
      let a = this.parser.schema.marks[t.mark];
      r = r.concat(a.create(t.attrs));
    }
    let l = this.top;
    if (s && s.isLeaf)
      this.findInside(e);
    else if (o)
      this.addElement(e, r, o);
    else if (t.getContent)
      this.findInside(e), t.getContent(e, this.parser.schema).forEach((a) => this.insertNode(a, r, !1));
    else {
      let a = e;
      typeof t.contentElement == "string" ? a = e.querySelector(t.contentElement) : typeof t.contentElement == "function" ? a = t.contentElement(e) : t.contentElement && (a = t.contentElement), this.findAround(e, a, !0), this.addAll(a, r), this.findAround(e, a, !1);
    }
    i && this.sync(l) && this.open--;
  }
  // Add all child nodes between `startIndex` and `endIndex` (or the
  // whole node, if not given). If `sync` is passed, use it to
  // synchronize after every block element.
  addAll(e, t, r, o) {
    let i = r || 0;
    for (let s = r ? e.childNodes[r] : e.firstChild, l = o == null ? null : e.childNodes[o]; s != l; s = s.nextSibling, ++i)
      this.findAtPoint(e, i), this.addDOM(s, t);
    this.findAtPoint(e, i);
  }
  // Try to find a way to fit the given node type into the current
  // context. May add intermediate wrappers and/or leave non-solid
  // nodes that we're in.
  findPlace(e, t, r) {
    let o, i;
    for (let s = this.open, l = 0; s >= 0; s--) {
      let a = this.nodes[s], c = a.findWrapping(e);
      if (c && (!o || o.length > c.length + l) && (o = c, i = a, !c.length))
        break;
      if (a.solid) {
        if (r)
          break;
        l += 2;
      }
    }
    if (!o)
      return null;
    this.sync(i);
    for (let s = 0; s < o.length; s++)
      t = this.enterInner(o[s], null, t, !1);
    return t;
  }
  // Try to insert the given node, adjusting the context when needed.
  insertNode(e, t, r) {
    if (e.isInline && this.needsBlock && !this.top.type) {
      let i = this.textblockFromContext();
      i && (t = this.enterInner(i, null, t));
    }
    let o = this.findPlace(e, t, r);
    if (o) {
      this.closeExtra();
      let i = this.top;
      i.match && (i.match = i.match.matchType(e.type));
      let s = Z.none;
      for (let l of o.concat(e.marks))
        (i.type ? i.type.allowsMarkType(l.type) : mc(l.type, e.type)) && (s = l.addToSet(s));
      return i.content.push(e.mark(s)), !0;
    }
    return !1;
  }
  // Try to start a node of the given type, adjusting the context when
  // necessary.
  enter(e, t, r, o) {
    let i = this.findPlace(e.create(t), r, !1);
    return i && (i = this.enterInner(e, t, r, !0, o)), i;
  }
  // Open a node of the given type
  enterInner(e, t, r, o = !1, i) {
    this.closeExtra();
    let s = this.top;
    s.match = s.match && s.match.matchType(e);
    let l = fc(e, i, s.options);
    s.options & wr && s.content.length == 0 && (l |= wr);
    let a = Z.none;
    return r = r.filter((c) => (s.type ? s.type.allowsMarkType(c.type) : mc(c.type, e)) ? (a = c.addToSet(a), !1) : !0), this.nodes.push(new lo(e, t, a, o, null, l)), this.open++, r;
  }
  // Make sure all nodes above this.open are finished and added to
  // their parents
  closeExtra(e = !1) {
    let t = this.nodes.length - 1;
    if (t > this.open) {
      for (; t > this.open; t--)
        this.nodes[t - 1].content.push(this.nodes[t].finish(e));
      this.nodes.length = this.open + 1;
    }
  }
  finish() {
    return this.open = 0, this.closeExtra(this.isOpen), this.nodes[0].finish(!!(this.isOpen || this.options.topOpen));
  }
  sync(e) {
    for (let t = this.open; t >= 0; t--) {
      if (this.nodes[t] == e)
        return this.open = t, !0;
      this.localPreserveWS && (this.nodes[t].options |= Or);
    }
    return !1;
  }
  get currentPos() {
    this.closeExtra();
    let e = 0;
    for (let t = this.open; t >= 0; t--) {
      let r = this.nodes[t].content;
      for (let o = r.length - 1; o >= 0; o--)
        e += r[o].nodeSize;
      t && e++;
    }
    return e;
  }
  findAtPoint(e, t) {
    if (this.find)
      for (let r = 0; r < this.find.length; r++)
        this.find[r].node == e && this.find[r].offset == t && (this.find[r].pos = this.currentPos);
  }
  findInside(e) {
    if (this.find)
      for (let t = 0; t < this.find.length; t++)
        this.find[t].pos == null && e.nodeType == 1 && e.contains(this.find[t].node) && (this.find[t].pos = this.currentPos);
  }
  findAround(e, t, r) {
    if (e != t && this.find)
      for (let o = 0; o < this.find.length; o++)
        this.find[o].pos == null && e.nodeType == 1 && e.contains(this.find[o].node) && t.compareDocumentPosition(this.find[o].node) & (r ? 2 : 4) && (this.find[o].pos = this.currentPos);
  }
  findInText(e) {
    if (this.find)
      for (let t = 0; t < this.find.length; t++)
        this.find[t].node == e && (this.find[t].pos = this.currentPos - (e.nodeValue.length - this.find[t].offset));
  }
  // Determines whether the given context string matches this context.
  matchesContext(e) {
    if (e.indexOf("|") > -1)
      return e.split(/\s*\|\s*/).some(this.matchesContext, this);
    let t = e.split("/"), r = this.options.context, o = !this.isOpen && (!r || r.parent.type == this.nodes[0].type), i = -(r ? r.depth + 1 : 0) + (o ? 0 : 1), s = (l, a) => {
      for (; l >= 0; l--) {
        let c = t[l];
        if (c == "") {
          if (l == t.length - 1 || l == 0)
            continue;
          for (; a >= i; a--)
            if (s(l - 1, a))
              return !0;
          return !1;
        } else {
          let u = a > 0 || a == 0 && o ? this.nodes[a].type : r && a >= i ? r.node(a - i).type : null;
          if (!u || u.name != c && !u.isInGroup(c))
            return !1;
          a--;
        }
      }
      return !0;
    };
    return s(t.length - 1, this.open);
  }
  textblockFromContext() {
    let e = this.options.context;
    if (e)
      for (let t = e.depth; t >= 0; t--) {
        let r = e.node(t).contentMatchAt(e.indexAfter(t)).defaultType;
        if (r && r.isTextblock && r.defaultAttrs)
          return r;
      }
    for (let t in this.parser.schema.nodes) {
      let r = this.parser.schema.nodes[t];
      if (r.isTextblock && r.defaultAttrs)
        return r;
    }
  }
}
function eg(n) {
  for (let e = n.firstChild, t = null; e; e = e.nextSibling) {
    let r = e.nodeType == 1 ? e.nodeName.toLowerCase() : null;
    r && Dd.hasOwnProperty(r) && t ? (t.appendChild(e), e = t) : r == "li" ? t = e : r && (t = null);
  }
}
function tg(n, e) {
  return (n.matches || n.msMatchesSelector || n.webkitMatchesSelector || n.mozMatchesSelector).call(n, e);
}
function pc(n) {
  let e = {};
  for (let t in n)
    e[t] = n[t];
  return e;
}
function mc(n, e) {
  let t = e.schema.nodes;
  for (let r in t) {
    let o = t[r];
    if (!o.allowsMarkType(n))
      continue;
    let i = [], s = (l) => {
      i.push(l);
      for (let a = 0; a < l.edgeCount; a++) {
        let { type: c, next: u } = l.edge(a);
        if (c == e || i.indexOf(u) < 0 && s(u))
          return !0;
      }
    };
    if (s(o.contentMatch))
      return !0;
  }
}
class Tn {
  /**
  Create a serializer. `nodes` should map node names to functions
  that take a node and return a description of the corresponding
  DOM. `marks` does the same for mark names, but also gets an
  argument that tells it whether the mark's content is block or
  inline content (for typical use, it'll always be inline). A mark
  serializer may be `null` to indicate that marks of that type
  should not be serialized.
  */
  constructor(e, t) {
    this.nodes = e, this.marks = t;
  }
  /**
  Serialize the content of this fragment to a DOM fragment. When
  not in the browser, the `document` option, containing a DOM
  document, should be passed so that the serializer can create
  nodes.
  */
  serializeFragment(e, t = {}, r) {
    r || (r = ys(t).createDocumentFragment());
    let o = r, i = [];
    return e.forEach((s) => {
      if (i.length || s.marks.length) {
        let l = 0, a = 0;
        for (; l < i.length && a < s.marks.length; ) {
          let c = s.marks[a];
          if (!this.marks[c.type.name]) {
            a++;
            continue;
          }
          if (!c.eq(i[l][0]) || c.type.spec.spanning === !1)
            break;
          l++, a++;
        }
        for (; l < i.length; )
          o = i.pop()[1];
        for (; a < s.marks.length; ) {
          let c = s.marks[a++], u = this.serializeMark(c, s.isInline, t);
          u && (i.push([c, o]), o.appendChild(u.dom), o = u.contentDOM || u.dom);
        }
      }
      o.appendChild(this.serializeNodeInner(s, t));
    }), r;
  }
  /**
  @internal
  */
  serializeNodeInner(e, t) {
    let { dom: r, contentDOM: o } = Oo(ys(t), this.nodes[e.type.name](e), null, e.attrs);
    if (o) {
      if (e.isLeaf)
        throw new RangeError("Content hole not allowed in a leaf node spec");
      this.serializeFragment(e.content, t, o);
    }
    return r;
  }
  /**
  Serialize this node to a DOM node. This can be useful when you
  need to serialize a part of a document, as opposed to the whole
  document. To serialize a whole document, use
  [`serializeFragment`](https://prosemirror.net/docs/ref/#model.DOMSerializer.serializeFragment) on
  its [content](https://prosemirror.net/docs/ref/#model.Node.content).
  */
  serializeNode(e, t = {}) {
    let r = this.serializeNodeInner(e, t);
    for (let o = e.marks.length - 1; o >= 0; o--) {
      let i = this.serializeMark(e.marks[o], e.isInline, t);
      i && ((i.contentDOM || i.dom).appendChild(r), r = i.dom);
    }
    return r;
  }
  /**
  @internal
  */
  serializeMark(e, t, r = {}) {
    let o = this.marks[e.type.name];
    return o && Oo(ys(r), o(e, t), null, e.attrs);
  }
  static renderSpec(e, t, r = null, o) {
    return Oo(e, t, r, o);
  }
  /**
  Build a serializer using the [`toDOM`](https://prosemirror.net/docs/ref/#model.NodeSpec.toDOM)
  properties in a schema's node and mark specs.
  */
  static fromSchema(e) {
    return e.cached.domSerializer || (e.cached.domSerializer = new Tn(this.nodesFromSchema(e), this.marksFromSchema(e)));
  }
  /**
  Gather the serializers in a schema's node specs into an object.
  This can be useful as a base to build a custom serializer from.
  */
  static nodesFromSchema(e) {
    let t = gc(e.nodes);
    return t.text || (t.text = (r) => r.text), t;
  }
  /**
  Gather the serializers in a schema's mark specs into an object.
  */
  static marksFromSchema(e) {
    return gc(e.marks);
  }
}
function gc(n) {
  let e = {};
  for (let t in n) {
    let r = n[t].spec.toDOM;
    r && (e[t] = r);
  }
  return e;
}
function ys(n) {
  return n.document || window.document;
}
const yc = /* @__PURE__ */ new WeakMap();
function ng(n) {
  let e = yc.get(n);
  return e === void 0 && yc.set(n, e = rg(n)), e;
}
function rg(n) {
  let e = null;
  function t(r) {
    if (r && typeof r == "object")
      if (Array.isArray(r))
        if (typeof r[0] == "string")
          e || (e = []), e.push(r);
        else
          for (let o = 0; o < r.length; o++)
            t(r[o]);
      else
        for (let o in r)
          t(r[o]);
  }
  return t(n), e;
}
function Oo(n, e, t, r) {
  if (typeof e == "string")
    return { dom: n.createTextNode(e) };
  if (e.nodeType != null)
    return { dom: e };
  if (e.dom && e.dom.nodeType != null)
    return e;
  let o = e[0], i;
  if (typeof o != "string")
    throw new RangeError("Invalid array passed to renderSpec");
  if (r && (i = ng(r)) && i.indexOf(e) > -1)
    throw new RangeError("Using an array from an attribute object as a DOM spec. This may be an attempted cross site scripting attack.");
  let s = o.indexOf(" ");
  s > 0 && (t = o.slice(0, s), o = o.slice(s + 1));
  let l, a = t ? n.createElementNS(t, o) : n.createElement(o), c = e[1], u = 1;
  if (c && typeof c == "object" && c.nodeType == null && !Array.isArray(c)) {
    u = 2;
    for (let d in c)
      if (c[d] != null) {
        let f = d.indexOf(" ");
        f > 0 ? a.setAttributeNS(d.slice(0, f), d.slice(f + 1), c[d]) : d == "style" && a.style ? a.style.cssText = c[d] : a.setAttribute(d, c[d]);
      }
  }
  for (let d = u; d < e.length; d++) {
    let f = e[d];
    if (f === 0) {
      if (d < e.length - 1 || d > u)
        throw new RangeError("Content hole must be the only child of its parent node");
      return { dom: a, contentDOM: a };
    } else {
      let { dom: h, contentDOM: p } = Oo(n, f, t, r);
      if (a.appendChild(h), p) {
        if (l)
          throw new RangeError("Multiple content holes");
        l = p;
      }
    }
  }
  return { dom: a, contentDOM: l };
}
const Pd = 65535, Id = Math.pow(2, 16);
function og(n, e) {
  return n + e * Id;
}
function bc(n) {
  return n & Pd;
}
function ig(n) {
  return (n - (n & Pd)) / Id;
}
const Ld = 1, _d = 2, No = 4, Bd = 8;
class sl {
  /**
  @internal
  */
  constructor(e, t, r) {
    this.pos = e, this.delInfo = t, this.recover = r;
  }
  /**
  Tells you whether the position was deleted, that is, whether the
  step removed the token on the side queried (via the `assoc`)
  argument from the document.
  */
  get deleted() {
    return (this.delInfo & Bd) > 0;
  }
  /**
  Tells you whether the token before the mapped position was deleted.
  */
  get deletedBefore() {
    return (this.delInfo & (Ld | No)) > 0;
  }
  /**
  True when the token after the mapped position was deleted.
  */
  get deletedAfter() {
    return (this.delInfo & (_d | No)) > 0;
  }
  /**
  Tells whether any of the steps mapped through deletes across the
  position (including both the token before and after the
  position).
  */
  get deletedAcross() {
    return (this.delInfo & No) > 0;
  }
}
class Be {
  /**
  Create a position map. The modifications to the document are
  represented as an array of numbers, in which each group of three
  represents a modified chunk as `[start, oldSize, newSize]`.
  */
  constructor(e, t = !1) {
    if (this.ranges = e, this.inverted = t, !e.length && Be.empty)
      return Be.empty;
  }
  /**
  @internal
  */
  recover(e) {
    let t = 0, r = bc(e);
    if (!this.inverted)
      for (let o = 0; o < r; o++)
        t += this.ranges[o * 3 + 2] - this.ranges[o * 3 + 1];
    return this.ranges[r * 3] + t + ig(e);
  }
  mapResult(e, t = 1) {
    return this._map(e, t, !1);
  }
  map(e, t = 1) {
    return this._map(e, t, !0);
  }
  /**
  @internal
  */
  _map(e, t, r) {
    let o = 0, i = this.inverted ? 2 : 1, s = this.inverted ? 1 : 2;
    for (let l = 0; l < this.ranges.length; l += 3) {
      let a = this.ranges[l] - (this.inverted ? o : 0);
      if (a > e)
        break;
      let c = this.ranges[l + i], u = this.ranges[l + s], d = a + c;
      if (e <= d) {
        let f = c ? e == a ? -1 : e == d ? 1 : t : t, h = a + o + (f < 0 ? 0 : u);
        if (r)
          return h;
        let p = e == (t < 0 ? a : d) ? null : og(l / 3, e - a), m = e == a ? _d : e == d ? Ld : No;
        return (t < 0 ? e != a : e != d) && (m |= Bd), new sl(h, m, p);
      }
      o += u - c;
    }
    return r ? e + o : new sl(e + o, 0, null);
  }
  /**
  @internal
  */
  touches(e, t) {
    let r = 0, o = bc(t), i = this.inverted ? 2 : 1, s = this.inverted ? 1 : 2;
    for (let l = 0; l < this.ranges.length; l += 3) {
      let a = this.ranges[l] - (this.inverted ? r : 0);
      if (a > e)
        break;
      let c = this.ranges[l + i], u = a + c;
      if (e <= u && l == o * 3)
        return !0;
      r += this.ranges[l + s] - c;
    }
    return !1;
  }
  /**
  Calls the given function on each of the changed ranges included in
  this map.
  */
  forEach(e) {
    let t = this.inverted ? 2 : 1, r = this.inverted ? 1 : 2;
    for (let o = 0, i = 0; o < this.ranges.length; o += 3) {
      let s = this.ranges[o], l = s - (this.inverted ? i : 0), a = s + (this.inverted ? 0 : i), c = this.ranges[o + t], u = this.ranges[o + r];
      e(l, l + c, a, a + u), i += u - c;
    }
  }
  /**
  Create an inverted version of this map. The result can be used to
  map positions in the post-step document to the pre-step document.
  */
  invert() {
    return new Be(this.ranges, !this.inverted);
  }
  /**
  @internal
  */
  toString() {
    return (this.inverted ? "-" : "") + JSON.stringify(this.ranges);
  }
  /**
  Create a map that moves all positions by offset `n` (which may be
  negative). This can be useful when applying steps meant for a
  sub-document to a larger document, or vice-versa.
  */
  static offset(e) {
    return e == 0 ? Be.empty : new Be(e < 0 ? [0, -e, 0] : [0, 0, e]);
  }
}
Be.empty = new Be([]);
class Nr {
  /**
  Create a new mapping with the given position maps.
  */
  constructor(e, t, r = 0, o = e ? e.length : 0) {
    this.mirror = t, this.from = r, this.to = o, this._maps = e || [], this.ownData = !(e || t);
  }
  /**
  The step maps in this mapping.
  */
  get maps() {
    return this._maps;
  }
  /**
  Create a mapping that maps only through a part of this one.
  */
  slice(e = 0, t = this.maps.length) {
    return new Nr(this._maps, this.mirror, e, t);
  }
  /**
  Add a step map to the end of this mapping. If `mirrors` is
  given, it should be the index of the step map that is the mirror
  image of this one.
  */
  appendMap(e, t) {
    this.ownData || (this._maps = this._maps.slice(), this.mirror = this.mirror && this.mirror.slice(), this.ownData = !0), this.to = this._maps.push(e), t != null && this.setMirror(this._maps.length - 1, t);
  }
  /**
  Add all the step maps in a given mapping to this one (preserving
  mirroring information).
  */
  appendMapping(e) {
    for (let t = 0, r = this._maps.length; t < e._maps.length; t++) {
      let o = e.getMirror(t);
      this.appendMap(e._maps[t], o != null && o < t ? r + o : void 0);
    }
  }
  /**
  Finds the offset of the step map that mirrors the map at the
  given offset, in this mapping (as per the second argument to
  `appendMap`).
  */
  getMirror(e) {
    if (this.mirror) {
      for (let t = 0; t < this.mirror.length; t++)
        if (this.mirror[t] == e)
          return this.mirror[t + (t % 2 ? -1 : 1)];
    }
  }
  /**
  @internal
  */
  setMirror(e, t) {
    this.mirror || (this.mirror = []), this.mirror.push(e, t);
  }
  /**
  Append the inverse of the given mapping to this one.
  */
  appendMappingInverted(e) {
    for (let t = e.maps.length - 1, r = this._maps.length + e._maps.length; t >= 0; t--) {
      let o = e.getMirror(t);
      this.appendMap(e._maps[t].invert(), o != null && o > t ? r - o - 1 : void 0);
    }
  }
  /**
  Create an inverted version of this mapping.
  */
  invert() {
    let e = new Nr();
    return e.appendMappingInverted(this), e;
  }
  /**
  Map a position through this mapping.
  */
  map(e, t = 1) {
    if (this.mirror)
      return this._map(e, t, !0);
    for (let r = this.from; r < this.to; r++)
      e = this._maps[r].map(e, t);
    return e;
  }
  /**
  Map a position through this mapping, returning a mapping
  result.
  */
  mapResult(e, t = 1) {
    return this._map(e, t, !1);
  }
  /**
  @internal
  */
  _map(e, t, r) {
    let o = 0;
    for (let i = this.from; i < this.to; i++) {
      let s = this._maps[i], l = s.mapResult(e, t);
      if (l.recover != null) {
        let a = this.getMirror(i);
        if (a != null && a > i && a < this.to) {
          i = a, e = this._maps[a].recover(l.recover);
          continue;
        }
      }
      o |= l.delInfo, e = l.pos;
    }
    return r ? e : new sl(e, o, null);
  }
}
const bs = /* @__PURE__ */ Object.create(null);
class Ee {
  /**
  Get the step map that represents the changes made by this step,
  and which can be used to transform between positions in the old
  and the new document.
  */
  getMap() {
    return Be.empty;
  }
  /**
  Try to merge this step with another one, to be applied directly
  after it. Returns the merged step when possible, null if the
  steps can't be merged.
  */
  merge(e) {
    return null;
  }
  /**
  Deserialize a step from its JSON representation. Will call
  through to the step class' own implementation of this method.
  */
  static fromJSON(e, t) {
    if (!t || !t.stepType)
      throw new RangeError("Invalid input for Step.fromJSON");
    let r = bs[t.stepType];
    if (!r)
      throw new RangeError(`No step type ${t.stepType} defined`);
    return r.fromJSON(e, t);
  }
  /**
  To be able to serialize steps to JSON, each step needs a string
  ID to attach to its JSON representation. Use this method to
  register an ID for your step classes. Try to pick something
  that's unlikely to clash with steps from other modules.
  */
  static jsonID(e, t) {
    if (e in bs)
      throw new RangeError("Duplicate use of step JSON ID " + e);
    return bs[e] = t, t.prototype.jsonID = e, t;
  }
}
class ce {
  /**
  @internal
  */
  constructor(e, t) {
    this.doc = e, this.failed = t;
  }
  /**
  Create a successful step result.
  */
  static ok(e) {
    return new ce(e, null);
  }
  /**
  Create a failed step result.
  */
  static fail(e) {
    return new ce(null, e);
  }
  /**
  Call [`Node.replace`](https://prosemirror.net/docs/ref/#model.Node.replace) with the given
  arguments. Create a successful result if it succeeds, and a
  failed one if it throws a `ReplaceError`.
  */
  static fromReplace(e, t, r, o) {
    try {
      return ce.ok(e.replace(t, r, o));
    } catch (i) {
      if (i instanceof $o)
        return ce.fail(i.message);
      throw i;
    }
  }
}
function Gl(n, e, t) {
  let r = [];
  for (let o = 0; o < n.childCount; o++) {
    let i = n.child(o);
    i.content.size && (i = i.copy(Gl(i.content, e, i))), i.isInline && (i = e(i, t, o)), r.push(i);
  }
  return M.fromArray(r);
}
class Ht extends Ee {
  /**
  Create a mark step.
  */
  constructor(e, t, r) {
    super(), this.from = e, this.to = t, this.mark = r;
  }
  apply(e) {
    let t = e.slice(this.from, this.to), r = e.resolve(this.from), o = r.node(r.sharedDepth(this.to)), i = new O(Gl(t.content, (s, l) => !s.isAtom || !l.type.allowsMarkType(this.mark.type) ? s : s.mark(this.mark.addToSet(s.marks)), o), t.openStart, t.openEnd);
    return ce.fromReplace(e, this.from, this.to, i);
  }
  invert() {
    return new lt(this.from, this.to, this.mark);
  }
  map(e) {
    let t = e.mapResult(this.from, 1), r = e.mapResult(this.to, -1);
    return t.deleted && r.deleted || t.pos >= r.pos ? null : new Ht(t.pos, r.pos, this.mark);
  }
  merge(e) {
    return e instanceof Ht && e.mark.eq(this.mark) && this.from <= e.to && this.to >= e.from ? new Ht(Math.min(this.from, e.from), Math.max(this.to, e.to), this.mark) : null;
  }
  toJSON() {
    return {
      stepType: "addMark",
      mark: this.mark.toJSON(),
      from: this.from,
      to: this.to
    };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.from != "number" || typeof t.to != "number")
      throw new RangeError("Invalid input for AddMarkStep.fromJSON");
    return new Ht(t.from, t.to, e.markFromJSON(t.mark));
  }
}
Ee.jsonID("addMark", Ht);
class lt extends Ee {
  /**
  Create a mark-removing step.
  */
  constructor(e, t, r) {
    super(), this.from = e, this.to = t, this.mark = r;
  }
  apply(e) {
    let t = e.slice(this.from, this.to), r = new O(Gl(t.content, (o) => o.mark(this.mark.removeFromSet(o.marks)), e), t.openStart, t.openEnd);
    return ce.fromReplace(e, this.from, this.to, r);
  }
  invert() {
    return new Ht(this.from, this.to, this.mark);
  }
  map(e) {
    let t = e.mapResult(this.from, 1), r = e.mapResult(this.to, -1);
    return t.deleted && r.deleted || t.pos >= r.pos ? null : new lt(t.pos, r.pos, this.mark);
  }
  merge(e) {
    return e instanceof lt && e.mark.eq(this.mark) && this.from <= e.to && this.to >= e.from ? new lt(Math.min(this.from, e.from), Math.max(this.to, e.to), this.mark) : null;
  }
  toJSON() {
    return {
      stepType: "removeMark",
      mark: this.mark.toJSON(),
      from: this.from,
      to: this.to
    };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.from != "number" || typeof t.to != "number")
      throw new RangeError("Invalid input for RemoveMarkStep.fromJSON");
    return new lt(t.from, t.to, e.markFromJSON(t.mark));
  }
}
Ee.jsonID("removeMark", lt);
class Wt extends Ee {
  /**
  Create a node mark step.
  */
  constructor(e, t) {
    super(), this.pos = e, this.mark = t;
  }
  apply(e) {
    let t = e.nodeAt(this.pos);
    if (!t)
      return ce.fail("No node at mark step's position");
    let r = t.type.create(t.attrs, null, this.mark.addToSet(t.marks));
    return ce.fromReplace(e, this.pos, this.pos + 1, new O(M.from(r), 0, t.isLeaf ? 0 : 1));
  }
  invert(e) {
    let t = e.nodeAt(this.pos);
    if (t) {
      let r = this.mark.addToSet(t.marks);
      if (r.length == t.marks.length) {
        for (let o = 0; o < t.marks.length; o++)
          if (!t.marks[o].isInSet(r))
            return new Wt(this.pos, t.marks[o]);
        return new Wt(this.pos, this.mark);
      }
    }
    return new wn(this.pos, this.mark);
  }
  map(e) {
    let t = e.mapResult(this.pos, 1);
    return t.deletedAfter ? null : new Wt(t.pos, this.mark);
  }
  toJSON() {
    return { stepType: "addNodeMark", pos: this.pos, mark: this.mark.toJSON() };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.pos != "number")
      throw new RangeError("Invalid input for AddNodeMarkStep.fromJSON");
    return new Wt(t.pos, e.markFromJSON(t.mark));
  }
}
Ee.jsonID("addNodeMark", Wt);
class wn extends Ee {
  /**
  Create a mark-removing step.
  */
  constructor(e, t) {
    super(), this.pos = e, this.mark = t;
  }
  apply(e) {
    let t = e.nodeAt(this.pos);
    if (!t)
      return ce.fail("No node at mark step's position");
    let r = t.type.create(t.attrs, null, this.mark.removeFromSet(t.marks));
    return ce.fromReplace(e, this.pos, this.pos + 1, new O(M.from(r), 0, t.isLeaf ? 0 : 1));
  }
  invert(e) {
    let t = e.nodeAt(this.pos);
    return !t || !this.mark.isInSet(t.marks) ? this : new Wt(this.pos, this.mark);
  }
  map(e) {
    let t = e.mapResult(this.pos, 1);
    return t.deletedAfter ? null : new wn(t.pos, this.mark);
  }
  toJSON() {
    return { stepType: "removeNodeMark", pos: this.pos, mark: this.mark.toJSON() };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.pos != "number")
      throw new RangeError("Invalid input for RemoveNodeMarkStep.fromJSON");
    return new wn(t.pos, e.markFromJSON(t.mark));
  }
}
Ee.jsonID("removeNodeMark", wn);
class fe extends Ee {
  /**
  The given `slice` should fit the 'gap' between `from` and
  `to`—the depths must line up, and the surrounding nodes must be
  able to be joined with the open sides of the slice. When
  `structure` is true, the step will fail if the content between
  from and to is not just a sequence of closing and then opening
  tokens (this is to guard against rebased replace steps
  overwriting something they weren't supposed to).
  */
  constructor(e, t, r, o = !1) {
    super(), this.from = e, this.to = t, this.slice = r, this.structure = o;
  }
  apply(e) {
    return this.structure && ll(e, this.from, this.to) ? ce.fail("Structure replace would overwrite content") : ce.fromReplace(e, this.from, this.to, this.slice);
  }
  getMap() {
    return new Be([this.from, this.to - this.from, this.slice.size]);
  }
  invert(e) {
    return new fe(this.from, this.from + this.slice.size, e.slice(this.from, this.to));
  }
  map(e) {
    let t = e.mapResult(this.from, 1), r = e.mapResult(this.to, -1);
    return t.deletedAcross && r.deletedAcross ? null : new fe(t.pos, Math.max(t.pos, r.pos), this.slice, this.structure);
  }
  merge(e) {
    if (!(e instanceof fe) || e.structure || this.structure)
      return null;
    if (this.from + this.slice.size == e.from && !this.slice.openEnd && !e.slice.openStart) {
      let t = this.slice.size + e.slice.size == 0 ? O.empty : new O(this.slice.content.append(e.slice.content), this.slice.openStart, e.slice.openEnd);
      return new fe(this.from, this.to + (e.to - e.from), t, this.structure);
    } else if (e.to == this.from && !this.slice.openStart && !e.slice.openEnd) {
      let t = this.slice.size + e.slice.size == 0 ? O.empty : new O(e.slice.content.append(this.slice.content), e.slice.openStart, this.slice.openEnd);
      return new fe(e.from, this.to, t, this.structure);
    } else
      return null;
  }
  toJSON() {
    let e = { stepType: "replace", from: this.from, to: this.to };
    return this.slice.size && (e.slice = this.slice.toJSON()), this.structure && (e.structure = !0), e;
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.from != "number" || typeof t.to != "number")
      throw new RangeError("Invalid input for ReplaceStep.fromJSON");
    return new fe(t.from, t.to, O.fromJSON(e, t.slice), !!t.structure);
  }
}
Ee.jsonID("replace", fe);
class he extends Ee {
  /**
  Create a replace-around step with the given range and gap.
  `insert` should be the point in the slice into which the content
  of the gap should be moved. `structure` has the same meaning as
  it has in the [`ReplaceStep`](https://prosemirror.net/docs/ref/#transform.ReplaceStep) class.
  */
  constructor(e, t, r, o, i, s, l = !1) {
    super(), this.from = e, this.to = t, this.gapFrom = r, this.gapTo = o, this.slice = i, this.insert = s, this.structure = l;
  }
  apply(e) {
    if (this.structure && (ll(e, this.from, this.gapFrom) || ll(e, this.gapTo, this.to)))
      return ce.fail("Structure gap-replace would overwrite content");
    let t = e.slice(this.gapFrom, this.gapTo);
    if (t.openStart || t.openEnd)
      return ce.fail("Gap is not a flat range");
    let r = this.slice.insertAt(this.insert, t.content);
    return r ? ce.fromReplace(e, this.from, this.to, r) : ce.fail("Content does not fit in gap");
  }
  getMap() {
    return new Be([
      this.from,
      this.gapFrom - this.from,
      this.insert,
      this.gapTo,
      this.to - this.gapTo,
      this.slice.size - this.insert
    ]);
  }
  invert(e) {
    let t = this.gapTo - this.gapFrom;
    return new he(this.from, this.from + this.slice.size + t, this.from + this.insert, this.from + this.insert + t, e.slice(this.from, this.to).removeBetween(this.gapFrom - this.from, this.gapTo - this.from), this.gapFrom - this.from, this.structure);
  }
  map(e) {
    let t = e.mapResult(this.from, 1), r = e.mapResult(this.to, -1), o = this.from == this.gapFrom ? t.pos : e.map(this.gapFrom, -1), i = this.to == this.gapTo ? r.pos : e.map(this.gapTo, 1);
    return t.deletedAcross && r.deletedAcross || o < t.pos || i > r.pos ? null : new he(t.pos, r.pos, o, i, this.slice, this.insert, this.structure);
  }
  toJSON() {
    let e = {
      stepType: "replaceAround",
      from: this.from,
      to: this.to,
      gapFrom: this.gapFrom,
      gapTo: this.gapTo,
      insert: this.insert
    };
    return this.slice.size && (e.slice = this.slice.toJSON()), this.structure && (e.structure = !0), e;
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.from != "number" || typeof t.to != "number" || typeof t.gapFrom != "number" || typeof t.gapTo != "number" || typeof t.insert != "number")
      throw new RangeError("Invalid input for ReplaceAroundStep.fromJSON");
    return new he(t.from, t.to, t.gapFrom, t.gapTo, O.fromJSON(e, t.slice), t.insert, !!t.structure);
  }
}
Ee.jsonID("replaceAround", he);
function ll(n, e, t) {
  let r = n.resolve(e), o = t - e, i = r.depth;
  for (; o > 0 && i > 0 && r.indexAfter(i) == r.node(i).childCount; )
    i--, o--;
  if (o > 0) {
    let s = r.node(i).maybeChild(r.indexAfter(i));
    for (; o > 0; ) {
      if (!s || s.isLeaf)
        return !0;
      s = s.firstChild, o--;
    }
  }
  return !1;
}
function sg(n, e, t, r) {
  let o = [], i = [], s, l;
  n.doc.nodesBetween(e, t, (a, c, u) => {
    if (!a.isInline)
      return;
    let d = a.marks;
    if (!r.isInSet(d) && u.type.allowsMarkType(r.type)) {
      let f = Math.max(c, e), h = Math.min(c + a.nodeSize, t), p = r.addToSet(d);
      for (let m = 0; m < d.length; m++)
        d[m].isInSet(p) || (s && s.to == f && s.mark.eq(d[m]) ? s.to = h : o.push(s = new lt(f, h, d[m])));
      l && l.to == f ? l.to = h : i.push(l = new Ht(f, h, r));
    }
  }), o.forEach((a) => n.step(a)), i.forEach((a) => n.step(a));
}
function lg(n, e, t, r) {
  let o = [], i = 0;
  n.doc.nodesBetween(e, t, (s, l) => {
    if (!s.isInline)
      return;
    i++;
    let a = null;
    if (r instanceof Ii) {
      let c = s.marks, u;
      for (; u = r.isInSet(c); )
        (a || (a = [])).push(u), c = u.removeFromSet(c);
    } else r ? r.isInSet(s.marks) && (a = [r]) : a = s.marks;
    if (a && a.length) {
      let c = Math.min(l + s.nodeSize, t);
      for (let u = 0; u < a.length; u++) {
        let d = a[u], f;
        for (let h = 0; h < o.length; h++) {
          let p = o[h];
          p.step == i - 1 && d.eq(o[h].style) && (f = p);
        }
        f ? (f.to = c, f.step = i) : o.push({ style: d, from: Math.max(l, e), to: c, step: i });
      }
    }
  }), o.forEach((s) => n.step(new lt(s.from, s.to, s.style)));
}
function Jl(n, e, t, r = t.contentMatch, o = !0) {
  let i = n.doc.nodeAt(e), s = [], l = e + 1;
  for (let a = 0; a < i.childCount; a++) {
    let c = i.child(a), u = l + c.nodeSize, d = r.matchType(c.type);
    if (!d)
      s.push(new fe(l, u, O.empty));
    else {
      r = d;
      for (let f = 0; f < c.marks.length; f++)
        t.allowsMarkType(c.marks[f].type) || n.step(new lt(l, u, c.marks[f]));
      if (o && c.isText && t.whitespace != "pre") {
        let f, h = /\r?\n|\r/g, p;
        for (; f = h.exec(c.text); )
          p || (p = new O(M.from(t.schema.text(" ", t.allowedMarks(c.marks))), 0, 0)), s.push(new fe(l + f.index, l + f.index + f[0].length, p));
      }
    }
    l = u;
  }
  if (!r.validEnd) {
    let a = r.fillBefore(M.empty, !0);
    n.replace(l, l, new O(a, 0, 0));
  }
  for (let a = s.length - 1; a >= 0; a--)
    n.step(s[a]);
}
function ag(n, e, t) {
  return (e == 0 || n.canReplace(e, n.childCount)) && (t == n.childCount || n.canReplace(0, t));
}
function er(n) {
  let t = n.parent.content.cutByIndex(n.startIndex, n.endIndex);
  for (let r = n.depth, o = 0, i = 0; ; --r) {
    let s = n.$from.node(r), l = n.$from.index(r) + o, a = n.$to.indexAfter(r) - i;
    if (r < n.depth && s.canReplace(l, a, t))
      return r;
    if (r == 0 || s.type.spec.isolating || !ag(s, l, a))
      break;
    l && (o = 1), a < s.childCount && (i = 1);
  }
  return null;
}
function cg(n, e, t) {
  let { $from: r, $to: o, depth: i } = e, s = r.before(i + 1), l = o.after(i + 1), a = s, c = l, u = M.empty, d = 0;
  for (let p = i, m = !1; p > t; p--)
    m || r.index(p) > 0 ? (m = !0, u = M.from(r.node(p).copy(u)), d++) : a--;
  let f = M.empty, h = 0;
  for (let p = i, m = !1; p > t; p--)
    m || o.after(p + 1) < o.end(p) ? (m = !0, f = M.from(o.node(p).copy(f)), h++) : c++;
  n.step(new he(a, c, s, l, new O(u.append(f), d, h), u.size - d, !0));
}
function Yl(n, e, t = null, r = n) {
  let o = ug(n, e), i = o && dg(r, e);
  return i ? o.map(wc).concat({ type: e, attrs: t }).concat(i.map(wc)) : null;
}
function wc(n) {
  return { type: n, attrs: null };
}
function ug(n, e) {
  let { parent: t, startIndex: r, endIndex: o } = n, i = t.contentMatchAt(r).findWrapping(e);
  if (!i)
    return null;
  let s = i.length ? i[0] : e;
  return t.canReplaceWith(r, o, s) ? i : null;
}
function dg(n, e) {
  let { parent: t, startIndex: r, endIndex: o } = n, i = t.child(r), s = e.contentMatch.findWrapping(i.type);
  if (!s)
    return null;
  let a = (s.length ? s[s.length - 1] : e).contentMatch;
  for (let c = r; a && c < o; c++)
    a = a.matchType(t.child(c).type);
  return !a || !a.validEnd ? null : s;
}
function fg(n, e, t) {
  let r = M.empty;
  for (let s = t.length - 1; s >= 0; s--) {
    if (r.size) {
      let l = t[s].type.contentMatch.matchFragment(r);
      if (!l || !l.validEnd)
        throw new RangeError("Wrapper type given to Transform.wrap does not form valid content of its parent wrapper");
    }
    r = M.from(t[s].type.create(t[s].attrs, r));
  }
  let o = e.start, i = e.end;
  n.step(new he(o, i, o, i, new O(r, 0, 0), t.length, !0));
}
function hg(n, e, t, r, o) {
  if (!r.isTextblock)
    throw new RangeError("Type given to setBlockType should be a textblock");
  let i = n.steps.length;
  n.doc.nodesBetween(e, t, (s, l) => {
    let a = typeof o == "function" ? o(s) : o;
    if (s.isTextblock && !s.hasMarkup(r, a) && pg(n.doc, n.mapping.slice(i).map(l), r)) {
      let c = null;
      if (r.schema.linebreakReplacement) {
        let h = r.whitespace == "pre", p = !!r.contentMatch.matchType(r.schema.linebreakReplacement);
        h && !p ? c = !1 : !h && p && (c = !0);
      }
      c === !1 && zd(n, s, l, i), Jl(n, n.mapping.slice(i).map(l, 1), r, void 0, c === null);
      let u = n.mapping.slice(i), d = u.map(l, 1), f = u.map(l + s.nodeSize, 1);
      return n.step(new he(d, f, d + 1, f - 1, new O(M.from(r.create(a, null, s.marks)), 0, 0), 1, !0)), c === !0 && Fd(n, s, l, i), !1;
    }
  });
}
function Fd(n, e, t, r) {
  e.forEach((o, i) => {
    if (o.isText) {
      let s, l = /\r?\n|\r/g;
      for (; s = l.exec(o.text); ) {
        let a = n.mapping.slice(r).map(t + 1 + i + s.index);
        n.replaceWith(a, a + 1, e.type.schema.linebreakReplacement.create());
      }
    }
  });
}
function zd(n, e, t, r) {
  e.forEach((o, i) => {
    if (o.type == o.type.schema.linebreakReplacement) {
      let s = n.mapping.slice(r).map(t + 1 + i);
      n.replaceWith(s, s + 1, e.type.schema.text(`
`));
    }
  });
}
function pg(n, e, t) {
  let r = n.resolve(e), o = r.index();
  return r.parent.canReplaceWith(o, o + 1, t);
}
function mg(n, e, t, r, o) {
  let i = n.doc.nodeAt(e);
  if (!i)
    throw new RangeError("No node at given position");
  t || (t = i.type);
  let s = t.create(r, null, o || i.marks);
  if (i.isLeaf)
    return n.replaceWith(e, e + i.nodeSize, s);
  if (!t.validContent(i.content))
    throw new RangeError("Invalid content for node type " + t.name);
  n.step(new he(e, e + i.nodeSize, e + 1, e + i.nodeSize - 1, new O(M.from(s), 0, 0), 1, !0));
}
function xt(n, e, t = 1, r) {
  let o = n.resolve(e), i = o.depth - t, s = r && r[r.length - 1] || o.parent;
  if (i < 0 || o.parent.type.spec.isolating || !o.parent.canReplace(o.index(), o.parent.childCount) || !s.type.validContent(o.parent.content.cutByIndex(o.index(), o.parent.childCount)))
    return !1;
  for (let c = o.depth - 1, u = t - 2; c > i; c--, u--) {
    let d = o.node(c), f = o.index(c);
    if (d.type.spec.isolating)
      return !1;
    let h = d.content.cutByIndex(f, d.childCount), p = r && r[u + 1];
    p && (h = h.replaceChild(0, p.type.create(p.attrs)));
    let m = r && r[u] || d;
    if (!d.canReplace(f + 1, d.childCount) || !m.type.validContent(h))
      return !1;
  }
  let l = o.indexAfter(i), a = r && r[0];
  return o.node(i).canReplaceWith(l, l, a ? a.type : o.node(i + 1).type);
}
function gg(n, e, t = 1, r) {
  let o = n.doc.resolve(e), i = M.empty, s = M.empty;
  for (let l = o.depth, a = o.depth - t, c = t - 1; l > a; l--, c--) {
    i = M.from(o.node(l).copy(i));
    let u = r && r[c];
    s = M.from(u ? u.type.create(u.attrs, s) : o.node(l).copy(s));
  }
  n.step(new fe(e, e, new O(i.append(s), t, t), !0));
}
function Zt(n, e) {
  let t = n.resolve(e), r = t.index();
  return $d(t.nodeBefore, t.nodeAfter) && t.parent.canReplace(r, r + 1);
}
function yg(n, e) {
  e.content.size || n.type.compatibleContent(e.type);
  let t = n.contentMatchAt(n.childCount), { linebreakReplacement: r } = n.type.schema;
  for (let o = 0; o < e.childCount; o++) {
    let i = e.child(o), s = i.type == r ? n.type.schema.nodes.text : i.type;
    if (t = t.matchType(s), !t || !n.type.allowsMarks(i.marks))
      return !1;
  }
  return t.validEnd;
}
function $d(n, e) {
  return !!(n && e && !n.isLeaf && yg(n, e));
}
function Li(n, e, t = -1) {
  let r = n.resolve(e);
  for (let o = r.depth; ; o--) {
    let i, s, l = r.index(o);
    if (o == r.depth ? (i = r.nodeBefore, s = r.nodeAfter) : t > 0 ? (i = r.node(o + 1), l++, s = r.node(o).maybeChild(l)) : (i = r.node(o).maybeChild(l - 1), s = r.node(o + 1)), i && !i.isTextblock && $d(i, s) && r.node(o).canReplace(l, l + 1))
      return e;
    if (o == 0)
      break;
    e = t < 0 ? r.before(o) : r.after(o);
  }
}
function bg(n, e, t) {
  let r = null, { linebreakReplacement: o } = n.doc.type.schema, i = n.doc.resolve(e - t), s = i.node().type;
  if (o && s.inlineContent) {
    let u = s.whitespace == "pre", d = !!s.contentMatch.matchType(o);
    u && !d ? r = !1 : !u && d && (r = !0);
  }
  let l = n.steps.length;
  if (r === !1) {
    let u = n.doc.resolve(e + t);
    zd(n, u.node(), u.before(), l);
  }
  s.inlineContent && Jl(n, e + t - 1, s, i.node().contentMatchAt(i.index()), r == null);
  let a = n.mapping.slice(l), c = a.map(e - t);
  if (n.step(new fe(c, a.map(e + t, -1), O.empty, !0)), r === !0) {
    let u = n.doc.resolve(c);
    Fd(n, u.node(), u.before(), n.steps.length);
  }
  return n;
}
function wg(n, e, t) {
  let r = n.resolve(e);
  if (r.parent.canReplaceWith(r.index(), r.index(), t))
    return e;
  if (r.parentOffset == 0)
    for (let o = r.depth - 1; o >= 0; o--) {
      let i = r.index(o);
      if (r.node(o).canReplaceWith(i, i, t))
        return r.before(o + 1);
      if (i > 0)
        return null;
    }
  if (r.parentOffset == r.parent.content.size)
    for (let o = r.depth - 1; o >= 0; o--) {
      let i = r.indexAfter(o);
      if (r.node(o).canReplaceWith(i, i, t))
        return r.after(o + 1);
      if (i < r.node(o).childCount)
        return null;
    }
  return null;
}
function Hd(n, e, t) {
  let r = n.resolve(e);
  if (!t.content.size)
    return e;
  let o = t.content;
  for (let i = 0; i < t.openStart; i++)
    o = o.firstChild.content;
  for (let i = 1; i <= (t.openStart == 0 && t.size ? 2 : 1); i++)
    for (let s = r.depth; s >= 0; s--) {
      let l = s == r.depth ? 0 : r.pos <= (r.start(s + 1) + r.end(s + 1)) / 2 ? -1 : 1, a = r.index(s) + (l > 0 ? 1 : 0), c = r.node(s), u = !1;
      if (i == 1)
        u = c.canReplace(a, a, o);
      else {
        let d = c.contentMatchAt(a).findWrapping(o.firstChild.type);
        u = d && c.canReplaceWith(a, a, d[0]);
      }
      if (u)
        return l == 0 ? r.pos : l < 0 ? r.before(s + 1) : r.after(s + 1);
    }
  return null;
}
function _i(n, e, t = e, r = O.empty) {
  if (e == t && !r.size)
    return null;
  let o = n.resolve(e), i = n.resolve(t);
  return Wd(o, i, r) ? new fe(e, t, r) : new vg(o, i, r).fit();
}
function Wd(n, e, t) {
  return !t.openStart && !t.openEnd && n.start() == e.start() && n.parent.canReplace(n.index(), e.index(), t.content);
}
class vg {
  constructor(e, t, r) {
    this.$from = e, this.$to = t, this.unplaced = r, this.frontier = [], this.placed = M.empty;
    for (let o = 0; o <= e.depth; o++) {
      let i = e.node(o);
      this.frontier.push({
        type: i.type,
        match: i.contentMatchAt(e.indexAfter(o))
      });
    }
    for (let o = e.depth; o > 0; o--)
      this.placed = M.from(e.node(o).copy(this.placed));
  }
  get depth() {
    return this.frontier.length - 1;
  }
  fit() {
    for (; this.unplaced.size; ) {
      let c = this.findFittable();
      c ? this.placeNodes(c) : this.openMore() || this.dropNode();
    }
    let e = this.mustMoveInline(), t = this.placed.size - this.depth - this.$from.depth, r = this.$from, o = this.close(e < 0 ? this.$to : r.doc.resolve(e));
    if (!o)
      return null;
    let i = this.placed, s = r.depth, l = o.depth;
    for (; s && l && i.childCount == 1; )
      i = i.firstChild.content, s--, l--;
    let a = new O(i, s, l);
    return e > -1 ? new he(r.pos, e, this.$to.pos, this.$to.end(), a, t) : a.size || r.pos != this.$to.pos ? new fe(r.pos, o.pos, a) : null;
  }
  // Find a position on the start spine of `this.unplaced` that has
  // content that can be moved somewhere on the frontier. Returns two
  // depths, one for the slice and one for the frontier.
  findFittable() {
    let e = this.unplaced.openStart;
    for (let t = this.unplaced.content, r = 0, o = this.unplaced.openEnd; r < e; r++) {
      let i = t.firstChild;
      if (t.childCount > 1 && (o = 0), i.type.spec.isolating && o <= r) {
        e = r;
        break;
      }
      t = i.content;
    }
    for (let t = 1; t <= 2; t++)
      for (let r = t == 1 ? e : this.unplaced.openStart; r >= 0; r--) {
        let o, i = null;
        r ? (i = ws(this.unplaced.content, r - 1).firstChild, o = i.content) : o = this.unplaced.content;
        let s = o.firstChild;
        for (let l = this.depth; l >= 0; l--) {
          let { type: a, match: c } = this.frontier[l], u, d = null;
          if (t == 1 && (s ? c.matchType(s.type) || (d = c.fillBefore(M.from(s), !1)) : i && a.compatibleContent(i.type)))
            return { sliceDepth: r, frontierDepth: l, parent: i, inject: d };
          if (t == 2 && s && (u = c.findWrapping(s.type)))
            return { sliceDepth: r, frontierDepth: l, parent: i, wrap: u };
          if (i && c.matchType(i.type))
            break;
        }
      }
  }
  openMore() {
    let { content: e, openStart: t, openEnd: r } = this.unplaced, o = ws(e, t);
    return !o.childCount || o.firstChild.isLeaf ? !1 : (this.unplaced = new O(e, t + 1, Math.max(r, o.size + t >= e.size - r ? t + 1 : 0)), !0);
  }
  dropNode() {
    let { content: e, openStart: t, openEnd: r } = this.unplaced, o = ws(e, t);
    if (o.childCount <= 1 && t > 0) {
      let i = e.size - t <= t + o.size;
      this.unplaced = new O(hr(e, t - 1, 1), t - 1, i ? t - 1 : r);
    } else
      this.unplaced = new O(hr(e, t, 1), t, r);
  }
  // Move content from the unplaced slice at `sliceDepth` to the
  // frontier node at `frontierDepth`. Close that frontier node when
  // applicable.
  placeNodes({ sliceDepth: e, frontierDepth: t, parent: r, inject: o, wrap: i }) {
    for (; this.depth > t; )
      this.closeFrontierNode();
    if (i)
      for (let m = 0; m < i.length; m++)
        this.openFrontierNode(i[m]);
    let s = this.unplaced, l = r ? r.content : s.content, a = s.openStart - e, c = 0, u = [], { match: d, type: f } = this.frontier[t];
    if (o) {
      for (let m = 0; m < o.childCount; m++)
        u.push(o.child(m));
      d = d.matchFragment(o);
    }
    let h = l.size + e - (s.content.size - s.openEnd);
    for (; c < l.childCount; ) {
      let m = l.child(c), g = d.matchType(m.type);
      if (!g)
        break;
      c++, (c > 1 || a == 0 || m.content.size) && (d = g, u.push(Vd(m.mark(f.allowedMarks(m.marks)), c == 1 ? a : 0, c == l.childCount ? h : -1)));
    }
    let p = c == l.childCount;
    p || (h = -1), this.placed = pr(this.placed, t, M.from(u)), this.frontier[t].match = d, p && h < 0 && r && r.type == this.frontier[this.depth].type && this.frontier.length > 1 && this.closeFrontierNode();
    for (let m = 0, g = l; m < h; m++) {
      let y = g.lastChild;
      this.frontier.push({ type: y.type, match: y.contentMatchAt(y.childCount) }), g = y.content;
    }
    this.unplaced = p ? e == 0 ? O.empty : new O(hr(s.content, e - 1, 1), e - 1, h < 0 ? s.openEnd : e - 1) : new O(hr(s.content, e, c), s.openStart, s.openEnd);
  }
  mustMoveInline() {
    if (!this.$to.parent.isTextblock)
      return -1;
    let e = this.frontier[this.depth], t;
    if (!e.type.isTextblock || !vs(this.$to, this.$to.depth, e.type, e.match, !1) || this.$to.depth == this.depth && (t = this.findCloseLevel(this.$to)) && t.depth == this.depth)
      return -1;
    let { depth: r } = this.$to, o = this.$to.after(r);
    for (; r > 1 && o == this.$to.end(--r); )
      ++o;
    return o;
  }
  findCloseLevel(e) {
    e: for (let t = Math.min(this.depth, e.depth); t >= 0; t--) {
      let { match: r, type: o } = this.frontier[t], i = t < e.depth && e.end(t + 1) == e.pos + (e.depth - (t + 1)), s = vs(e, t, o, r, i);
      if (s) {
        for (let l = t - 1; l >= 0; l--) {
          let { match: a, type: c } = this.frontier[l], u = vs(e, l, c, a, !0);
          if (!u || u.childCount)
            continue e;
        }
        return { depth: t, fit: s, move: i ? e.doc.resolve(e.after(t + 1)) : e };
      }
    }
  }
  close(e) {
    let t = this.findCloseLevel(e);
    if (!t)
      return null;
    for (; this.depth > t.depth; )
      this.closeFrontierNode();
    t.fit.childCount && (this.placed = pr(this.placed, t.depth, t.fit)), e = t.move;
    for (let r = t.depth + 1; r <= e.depth; r++) {
      let o = e.node(r), i = o.type.contentMatch.fillBefore(o.content, !0, e.index(r));
      this.openFrontierNode(o.type, o.attrs, i);
    }
    return e;
  }
  openFrontierNode(e, t = null, r) {
    let o = this.frontier[this.depth];
    o.match = o.match.matchType(e), this.placed = pr(this.placed, this.depth, M.from(e.create(t, r))), this.frontier.push({ type: e, match: e.contentMatch });
  }
  closeFrontierNode() {
    let t = this.frontier.pop().match.fillBefore(M.empty, !0);
    t.childCount && (this.placed = pr(this.placed, this.frontier.length, t));
  }
}
function hr(n, e, t) {
  return e == 0 ? n.cutByIndex(t, n.childCount) : n.replaceChild(0, n.firstChild.copy(hr(n.firstChild.content, e - 1, t)));
}
function pr(n, e, t) {
  return e == 0 ? n.append(t) : n.replaceChild(n.childCount - 1, n.lastChild.copy(pr(n.lastChild.content, e - 1, t)));
}
function ws(n, e) {
  for (let t = 0; t < e; t++)
    n = n.firstChild.content;
  return n;
}
function Vd(n, e, t) {
  if (e <= 0)
    return n;
  let r = n.content;
  return e > 1 && (r = r.replaceChild(0, Vd(r.firstChild, e - 1, r.childCount == 1 ? t - 1 : 0))), e > 0 && (r = n.type.contentMatch.fillBefore(r).append(r), t <= 0 && (r = r.append(n.type.contentMatch.matchFragment(r).fillBefore(M.empty, !0)))), n.copy(r);
}
function vs(n, e, t, r, o) {
  let i = n.node(e), s = o ? n.indexAfter(e) : n.index(e);
  if (s == i.childCount && !t.compatibleContent(i.type))
    return null;
  let l = r.fillBefore(i.content, !0, s);
  return l && !Cg(t, i.content, s) ? l : null;
}
function Cg(n, e, t) {
  for (let r = t; r < e.childCount; r++)
    if (!n.allowsMarks(e.child(r).marks))
      return !0;
  return !1;
}
function Sg(n) {
  return n.spec.defining || n.spec.definingForContent;
}
function xg(n, e, t, r) {
  if (!r.size)
    return n.deleteRange(e, t);
  let o = n.doc.resolve(e), i = n.doc.resolve(t);
  if (Wd(o, i, r))
    return n.step(new fe(e, t, r));
  let s = Ud(o, i);
  s[s.length - 1] == 0 && s.pop();
  let l = -(o.depth + 1);
  s.unshift(l);
  for (let f = o.depth, h = o.pos - 1; f > 0; f--, h--) {
    let p = o.node(f).type.spec;
    if (p.defining || p.definingAsContext || p.isolating)
      break;
    s.indexOf(f) > -1 ? l = f : o.before(f) == h && s.splice(1, 0, -f);
  }
  let a = s.indexOf(l), c = [], u = r.openStart;
  for (let f = r.content, h = 0; ; h++) {
    let p = f.firstChild;
    if (c.push(p), h == r.openStart)
      break;
    f = p.content;
  }
  for (let f = u - 1; f >= 0; f--) {
    let h = c[f], p = Sg(h.type);
    if (p && !h.sameMarkup(o.node(Math.abs(l) - 1)))
      u = f;
    else if (p || !h.type.isTextblock)
      break;
  }
  for (let f = r.openStart; f >= 0; f--) {
    let h = (f + u + 1) % (r.openStart + 1), p = c[h];
    if (p)
      for (let m = 0; m < s.length; m++) {
        let g = s[(m + a) % s.length], y = !0;
        g < 0 && (y = !1, g = -g);
        let b = o.node(g - 1), w = o.index(g - 1);
        if (b.canReplaceWith(w, w, p.type, p.marks))
          return n.replace(o.before(g), y ? i.after(g) : t, new O(jd(r.content, 0, r.openStart, h), h, r.openEnd));
      }
  }
  let d = n.steps.length;
  for (let f = s.length - 1; f >= 0 && (n.replace(e, t, r), !(n.steps.length > d)); f--) {
    let h = s[f];
    h < 0 || (e = o.before(h), t = i.after(h));
  }
}
function jd(n, e, t, r, o) {
  if (e < t) {
    let i = n.firstChild;
    n = n.replaceChild(0, i.copy(jd(i.content, e + 1, t, r, i)));
  }
  if (e > r) {
    let i = o.contentMatchAt(0), s = i.fillBefore(n).append(n);
    n = s.append(i.matchFragment(s).fillBefore(M.empty, !0));
  }
  return n;
}
function kg(n, e, t, r) {
  if (!r.isInline && e == t && n.doc.resolve(e).parent.content.size) {
    let o = wg(n.doc, e, r.type);
    o != null && (e = t = o);
  }
  n.replaceRange(e, t, new O(M.from(r), 0, 0));
}
function Eg(n, e, t) {
  let r = n.doc.resolve(e), o = n.doc.resolve(t), i = Ud(r, o);
  for (let s = 0; s < i.length; s++) {
    let l = i[s], a = s == i.length - 1;
    if (a && l == 0 || r.node(l).type.contentMatch.validEnd)
      return n.delete(r.start(l), o.end(l));
    if (l > 0 && (a || r.node(l - 1).canReplace(r.index(l - 1), o.indexAfter(l - 1))))
      return n.delete(r.before(l), o.after(l));
  }
  for (let s = 1; s <= r.depth && s <= o.depth; s++)
    if (e - r.start(s) == r.depth - s && t > r.end(s) && o.end(s) - t != o.depth - s && r.start(s - 1) == o.start(s - 1) && r.node(s - 1).canReplace(r.index(s - 1), o.index(s - 1)))
      return n.delete(r.before(s), t);
  n.delete(e, t);
}
function Ud(n, e) {
  let t = [], r = Math.min(n.depth, e.depth);
  for (let o = r; o >= 0; o--) {
    let i = n.start(o);
    if (i < n.pos - (n.depth - o) || e.end(o) > e.pos + (e.depth - o) || n.node(o).type.spec.isolating || e.node(o).type.spec.isolating)
      break;
    (i == e.start(o) || o == n.depth && o == e.depth && n.parent.inlineContent && e.parent.inlineContent && o && e.start(o - 1) == i - 1) && t.push(o);
  }
  return t;
}
class Wn extends Ee {
  /**
  Construct an attribute step.
  */
  constructor(e, t, r) {
    super(), this.pos = e, this.attr = t, this.value = r;
  }
  apply(e) {
    let t = e.nodeAt(this.pos);
    if (!t)
      return ce.fail("No node at attribute step's position");
    let r = /* @__PURE__ */ Object.create(null);
    for (let i in t.attrs)
      r[i] = t.attrs[i];
    r[this.attr] = this.value;
    let o = t.type.create(r, null, t.marks);
    return ce.fromReplace(e, this.pos, this.pos + 1, new O(M.from(o), 0, t.isLeaf ? 0 : 1));
  }
  getMap() {
    return Be.empty;
  }
  invert(e) {
    return new Wn(this.pos, this.attr, e.nodeAt(this.pos).attrs[this.attr]);
  }
  map(e) {
    let t = e.mapResult(this.pos, 1);
    return t.deletedAfter ? null : new Wn(t.pos, this.attr, this.value);
  }
  toJSON() {
    return { stepType: "attr", pos: this.pos, attr: this.attr, value: this.value };
  }
  static fromJSON(e, t) {
    if (typeof t.pos != "number" || typeof t.attr != "string")
      throw new RangeError("Invalid input for AttrStep.fromJSON");
    return new Wn(t.pos, t.attr, t.value);
  }
}
Ee.jsonID("attr", Wn);
class Rr extends Ee {
  /**
  Construct an attribute step.
  */
  constructor(e, t) {
    super(), this.attr = e, this.value = t;
  }
  apply(e) {
    let t = /* @__PURE__ */ Object.create(null);
    for (let o in e.attrs)
      t[o] = e.attrs[o];
    t[this.attr] = this.value;
    let r = e.type.create(t, e.content, e.marks);
    return ce.ok(r);
  }
  getMap() {
    return Be.empty;
  }
  invert(e) {
    return new Rr(this.attr, e.attrs[this.attr]);
  }
  map(e) {
    return this;
  }
  toJSON() {
    return { stepType: "docAttr", attr: this.attr, value: this.value };
  }
  static fromJSON(e, t) {
    if (typeof t.attr != "string")
      throw new RangeError("Invalid input for DocAttrStep.fromJSON");
    return new Rr(t.attr, t.value);
  }
}
Ee.jsonID("docAttr", Rr);
let Kn = class extends Error {
};
Kn = function n(e) {
  let t = Error.call(this, e);
  return t.__proto__ = n.prototype, t;
};
Kn.prototype = Object.create(Error.prototype);
Kn.prototype.constructor = Kn;
Kn.prototype.name = "TransformError";
class Xl {
  /**
  Create a transform that starts with the given document.
  */
  constructor(e) {
    this.doc = e, this.steps = [], this.docs = [], this.mapping = new Nr();
  }
  /**
  The starting document.
  */
  get before() {
    return this.docs.length ? this.docs[0] : this.doc;
  }
  /**
  Apply a new step in this transform, saving the result. Throws an
  error when the step fails.
  */
  step(e) {
    let t = this.maybeStep(e);
    if (t.failed)
      throw new Kn(t.failed);
    return this;
  }
  /**
  Try to apply a step in this transformation, ignoring it if it
  fails. Returns the step result.
  */
  maybeStep(e) {
    let t = e.apply(this.doc);
    return t.failed || this.addStep(e, t.doc), t;
  }
  /**
  True when the document has been changed (when there are any
  steps).
  */
  get docChanged() {
    return this.steps.length > 0;
  }
  /**
  Return a single range, in post-transform document positions,
  that covers all content changed by this transform. Returns null
  if no replacements are made. Note that this will ignore changes
  that add/remove marks without replacing the underlying content.
  */
  changedRange() {
    let e = 1e9, t = -1e9;
    for (let r = 0; r < this.mapping.maps.length; r++) {
      let o = this.mapping.maps[r];
      r && (e = o.map(e, 1), t = o.map(t, -1)), o.forEach((i, s, l, a) => {
        e = Math.min(e, l), t = Math.max(t, a);
      });
    }
    return e == 1e9 ? null : { from: e, to: t };
  }
  /**
  @internal
  */
  addStep(e, t) {
    this.docs.push(this.doc), this.steps.push(e), this.mapping.appendMap(e.getMap()), this.doc = t;
  }
  /**
  Replace the part of the document between `from` and `to` with the
  given `slice`.
  */
  replace(e, t = e, r = O.empty) {
    let o = _i(this.doc, e, t, r);
    return o && this.step(o), this;
  }
  /**
  Replace the given range with the given content, which may be a
  fragment, node, or array of nodes.
  */
  replaceWith(e, t, r) {
    return this.replace(e, t, new O(M.from(r), 0, 0));
  }
  /**
  Delete the content between the given positions.
  */
  delete(e, t) {
    return this.replace(e, t, O.empty);
  }
  /**
  Insert the given content at the given position.
  */
  insert(e, t) {
    return this.replaceWith(e, e, t);
  }
  /**
  Replace a range of the document with a given slice, using
  `from`, `to`, and the slice's
  [`openStart`](https://prosemirror.net/docs/ref/#model.Slice.openStart) property as hints, rather
  than fixed start and end points. This method may grow the
  replaced area or close open nodes in the slice in order to get a
  fit that is more in line with WYSIWYG expectations, by dropping
  fully covered parent nodes of the replaced region when they are
  marked [non-defining as
  context](https://prosemirror.net/docs/ref/#model.NodeSpec.definingAsContext), or including an
  open parent node from the slice that _is_ marked as [defining
  its content](https://prosemirror.net/docs/ref/#model.NodeSpec.definingForContent).
  
  This is the method, for example, to handle paste. The similar
  [`replace`](https://prosemirror.net/docs/ref/#transform.Transform.replace) method is a more
  primitive tool which will _not_ move the start and end of its given
  range, and is useful in situations where you need more precise
  control over what happens.
  */
  replaceRange(e, t, r) {
    return xg(this, e, t, r), this;
  }
  /**
  Replace the given range with a node, but use `from` and `to` as
  hints, rather than precise positions. When from and to are the same
  and are at the start or end of a parent node in which the given
  node doesn't fit, this method may _move_ them out towards a parent
  that does allow the given node to be placed. When the given range
  completely covers a parent node, this method may completely replace
  that parent node.
  */
  replaceRangeWith(e, t, r) {
    return kg(this, e, t, r), this;
  }
  /**
  Delete the given range, expanding it to cover fully covered
  parent nodes until a valid replace is found.
  */
  deleteRange(e, t) {
    return Eg(this, e, t), this;
  }
  /**
  Split the content in the given range off from its parent, if there
  is sibling content before or after it, and move it up the tree to
  the depth specified by `target`. You'll probably want to use
  [`liftTarget`](https://prosemirror.net/docs/ref/#transform.liftTarget) to compute `target`, to make
  sure the lift is valid.
  */
  lift(e, t) {
    return cg(this, e, t), this;
  }
  /**
  Join the blocks around the given position. If depth is 2, their
  last and first siblings are also joined, and so on.
  */
  join(e, t = 1) {
    return bg(this, e, t), this;
  }
  /**
  Wrap the given [range](https://prosemirror.net/docs/ref/#model.NodeRange) in the given set of wrappers.
  The wrappers are assumed to be valid in this position, and should
  probably be computed with [`findWrapping`](https://prosemirror.net/docs/ref/#transform.findWrapping).
  */
  wrap(e, t) {
    return fg(this, e, t), this;
  }
  /**
  Set the type of all textblocks (partly) between `from` and `to` to
  the given node type with the given attributes.
  */
  setBlockType(e, t = e, r, o = null) {
    return hg(this, e, t, r, o), this;
  }
  /**
  Change the type, attributes, and/or marks of the node at `pos`.
  When `type` isn't given, the existing node type is preserved,
  */
  setNodeMarkup(e, t, r = null, o) {
    return mg(this, e, t, r, o), this;
  }
  /**
  Set a single attribute on a given node to a new value.
  The `pos` addresses the document content. Use `setDocAttribute`
  to set attributes on the document itself.
  */
  setNodeAttribute(e, t, r) {
    return this.step(new Wn(e, t, r)), this;
  }
  /**
  Set a single attribute on the document to a new value.
  */
  setDocAttribute(e, t) {
    return this.step(new Rr(e, t)), this;
  }
  /**
  Add a mark to the node at position `pos`.
  */
  addNodeMark(e, t) {
    return this.step(new Wt(e, t)), this;
  }
  /**
  Remove a mark (or all marks of the given type) from the node at
  position `pos`.
  */
  removeNodeMark(e, t) {
    let r = this.doc.nodeAt(e);
    if (!r)
      throw new RangeError("No node at position " + e);
    if (t instanceof Z)
      t.isInSet(r.marks) && this.step(new wn(e, t));
    else {
      let o = r.marks, i, s = [];
      for (; i = t.isInSet(o); )
        s.push(new wn(e, i)), o = i.removeFromSet(o);
      for (let l = s.length - 1; l >= 0; l--)
        this.step(s[l]);
    }
    return this;
  }
  /**
  Split the node at the given position, and optionally, if `depth` is
  greater than one, any number of nodes above that. By default, the
  parts split off will inherit the node type of the original node.
  This can be changed by passing an array of types and attributes to
  use after the split (with the outermost nodes coming first).
  */
  split(e, t = 1, r) {
    return gg(this, e, t, r), this;
  }
  /**
  Add the given mark to the inline content between `from` and `to`.
  */
  addMark(e, t, r) {
    return sg(this, e, t, r), this;
  }
  /**
  Remove marks from inline nodes between `from` and `to`. When
  `mark` is a single mark, remove precisely that mark. When it is
  a mark type, remove all marks of that type. When it is null,
  remove all marks of any type.
  */
  removeMark(e, t, r) {
    return lg(this, e, t, r), this;
  }
  /**
  Removes all marks and nodes from the content of the node at
  `pos` that don't match the given new parent node type. Accepts
  an optional starting [content match](https://prosemirror.net/docs/ref/#model.ContentMatch) as
  third argument.
  */
  clearIncompatible(e, t, r) {
    return Jl(this, e, t, r), this;
  }
}
const Cs = /* @__PURE__ */ Object.create(null);
class _ {
  /**
  Initialize a selection with the head and anchor and ranges. If no
  ranges are given, constructs a single range across `$anchor` and
  `$head`.
  */
  constructor(e, t, r) {
    this.$anchor = e, this.$head = t, this.ranges = r || [new Kd(e.min(t), e.max(t))];
  }
  /**
  The selection's anchor, as an unresolved position.
  */
  get anchor() {
    return this.$anchor.pos;
  }
  /**
  The selection's head.
  */
  get head() {
    return this.$head.pos;
  }
  /**
  The lower bound of the selection's main range.
  */
  get from() {
    return this.$from.pos;
  }
  /**
  The upper bound of the selection's main range.
  */
  get to() {
    return this.$to.pos;
  }
  /**
  The resolved lower  bound of the selection's main range.
  */
  get $from() {
    return this.ranges[0].$from;
  }
  /**
  The resolved upper bound of the selection's main range.
  */
  get $to() {
    return this.ranges[0].$to;
  }
  /**
  Indicates whether the selection contains any content.
  */
  get empty() {
    let e = this.ranges;
    for (let t = 0; t < e.length; t++)
      if (e[t].$from.pos != e[t].$to.pos)
        return !1;
    return !0;
  }
  /**
  Get the content of this selection as a slice.
  */
  content() {
    return this.$from.doc.slice(this.from, this.to, !0);
  }
  /**
  Replace the selection with a slice or, if no slice is given,
  delete the selection. Will append to the given transaction.
  */
  replace(e, t = O.empty) {
    let r = t.content.lastChild, o = null;
    for (let l = 0; l < t.openEnd; l++)
      o = r, r = r.lastChild;
    let i = e.steps.length, s = this.ranges;
    for (let l = 0; l < s.length; l++) {
      let { $from: a, $to: c } = s[l], u = e.mapping.slice(i);
      e.replaceRange(u.map(a.pos), u.map(c.pos), l ? O.empty : t), l == 0 && Sc(e, i, (r ? r.isInline : o && o.isTextblock) ? -1 : 1);
    }
  }
  /**
  Replace the selection with the given node, appending the changes
  to the given transaction.
  */
  replaceWith(e, t) {
    let r = e.steps.length, o = this.ranges;
    for (let i = 0; i < o.length; i++) {
      let { $from: s, $to: l } = o[i], a = e.mapping.slice(r), c = a.map(s.pos), u = a.map(l.pos);
      i ? e.deleteRange(c, u) : (e.replaceRangeWith(c, u, t), Sc(e, r, t.isInline ? -1 : 1));
    }
  }
  /**
  Find a valid cursor or leaf node selection starting at the given
  position and searching back if `dir` is negative, and forward if
  positive. When `textOnly` is true, only consider cursor
  selections. Will return null when no valid selection position is
  found.
  */
  static findFrom(e, t, r = !1) {
    let o = e.parent.inlineContent ? new L(e) : Bn(e.node(0), e.parent, e.pos, e.index(), t, r);
    if (o)
      return o;
    for (let i = e.depth - 1; i >= 0; i--) {
      let s = t < 0 ? Bn(e.node(0), e.node(i), e.before(i + 1), e.index(i), t, r) : Bn(e.node(0), e.node(i), e.after(i + 1), e.index(i) + 1, t, r);
      if (s)
        return s;
    }
    return null;
  }
  /**
  Find a valid cursor or leaf node selection near the given
  position. Searches forward first by default, but if `bias` is
  negative, it will search backwards first.
  */
  static near(e, t = 1) {
    return this.findFrom(e, t) || this.findFrom(e, -t) || new ze(e.node(0));
  }
  /**
  Find the cursor or leaf node selection closest to the start of
  the given document. Will return an
  [`AllSelection`](https://prosemirror.net/docs/ref/#state.AllSelection) if no valid position
  exists.
  */
  static atStart(e) {
    return Bn(e, e, 0, 0, 1) || new ze(e);
  }
  /**
  Find the cursor or leaf node selection closest to the end of the
  given document.
  */
  static atEnd(e) {
    return Bn(e, e, e.content.size, e.childCount, -1) || new ze(e);
  }
  /**
  Deserialize the JSON representation of a selection. Must be
  implemented for custom classes (as a static class method).
  */
  static fromJSON(e, t) {
    if (!t || !t.type)
      throw new RangeError("Invalid input for Selection.fromJSON");
    let r = Cs[t.type];
    if (!r)
      throw new RangeError(`No selection type ${t.type} defined`);
    return r.fromJSON(e, t);
  }
  /**
  To be able to deserialize selections from JSON, custom selection
  classes must register themselves with an ID string, so that they
  can be disambiguated. Try to pick something that's unlikely to
  clash with classes from other modules.
  */
  static jsonID(e, t) {
    if (e in Cs)
      throw new RangeError("Duplicate use of selection JSON ID " + e);
    return Cs[e] = t, t.prototype.jsonID = e, t;
  }
  /**
  Get a [bookmark](https://prosemirror.net/docs/ref/#state.SelectionBookmark) for this selection,
  which is a value that can be mapped without having access to a
  current document, and later resolved to a real selection for a
  given document again. (This is used mostly by the history to
  track and restore old selections.) The default implementation of
  this method just converts the selection to a text selection and
  returns the bookmark for that.
  */
  getBookmark() {
    return L.between(this.$anchor, this.$head).getBookmark();
  }
}
_.prototype.visible = !0;
class Kd {
  /**
  Create a range.
  */
  constructor(e, t) {
    this.$from = e, this.$to = t;
  }
}
let vc = !1;
function Cc(n) {
  !vc && !n.parent.inlineContent && (vc = !0, console.warn("TextSelection endpoint not pointing into a node with inline content (" + n.parent.type.name + ")"));
}
class L extends _ {
  /**
  Construct a text selection between the given points.
  */
  constructor(e, t = e) {
    Cc(e), Cc(t), super(e, t);
  }
  /**
  Returns a resolved position if this is a cursor selection (an
  empty text selection), and null otherwise.
  */
  get $cursor() {
    return this.$anchor.pos == this.$head.pos ? this.$head : null;
  }
  map(e, t) {
    let r = e.resolve(t.map(this.head));
    if (!r.parent.inlineContent)
      return _.near(r);
    let o = e.resolve(t.map(this.anchor));
    return new L(o.parent.inlineContent ? o : r, r);
  }
  replace(e, t = O.empty) {
    if (super.replace(e, t), t == O.empty) {
      let r = this.$from.marksAcross(this.$to);
      r && e.ensureMarks(r);
    }
  }
  eq(e) {
    return e instanceof L && e.anchor == this.anchor && e.head == this.head;
  }
  getBookmark() {
    return new Bi(this.anchor, this.head);
  }
  toJSON() {
    return { type: "text", anchor: this.anchor, head: this.head };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.anchor != "number" || typeof t.head != "number")
      throw new RangeError("Invalid input for TextSelection.fromJSON");
    return new L(e.resolve(t.anchor), e.resolve(t.head));
  }
  /**
  Create a text selection from non-resolved positions.
  */
  static create(e, t, r = t) {
    let o = e.resolve(t);
    return new this(o, r == t ? o : e.resolve(r));
  }
  /**
  Return a text selection that spans the given positions or, if
  they aren't text positions, find a text selection near them.
  `bias` determines whether the method searches forward (default)
  or backwards (negative number) first. Will fall back to calling
  [`Selection.near`](https://prosemirror.net/docs/ref/#state.Selection^near) when the document
  doesn't contain a valid text position.
  */
  static between(e, t, r) {
    let o = e.pos - t.pos;
    if ((!r || o) && (r = o >= 0 ? 1 : -1), !t.parent.inlineContent) {
      let i = _.findFrom(t, r, !0) || _.findFrom(t, -r, !0);
      if (i)
        t = i.$head;
      else
        return _.near(t, r);
    }
    return e.parent.inlineContent || (o == 0 ? e = t : (e = (_.findFrom(e, -r, !0) || _.findFrom(e, r, !0)).$anchor, e.pos < t.pos != o < 0 && (e = t))), new L(e, t);
  }
}
_.jsonID("text", L);
class Bi {
  constructor(e, t) {
    this.anchor = e, this.head = t;
  }
  map(e) {
    return new Bi(e.map(this.anchor), e.map(this.head));
  }
  resolve(e) {
    return L.between(e.resolve(this.anchor), e.resolve(this.head));
  }
}
class I extends _ {
  /**
  Create a node selection. Does not verify the validity of its
  argument.
  */
  constructor(e) {
    let t = e.nodeAfter, r = e.node(0).resolve(e.pos + t.nodeSize);
    super(e, r), this.node = t;
  }
  map(e, t) {
    let { deleted: r, pos: o } = t.mapResult(this.anchor), i = e.resolve(o);
    return r ? _.near(i) : new I(i);
  }
  content() {
    return new O(M.from(this.node), 0, 0);
  }
  eq(e) {
    return e instanceof I && e.anchor == this.anchor;
  }
  toJSON() {
    return { type: "node", anchor: this.anchor };
  }
  getBookmark() {
    return new Ql(this.anchor);
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.anchor != "number")
      throw new RangeError("Invalid input for NodeSelection.fromJSON");
    return new I(e.resolve(t.anchor));
  }
  /**
  Create a node selection from non-resolved positions.
  */
  static create(e, t) {
    return new I(e.resolve(t));
  }
  /**
  Determines whether the given node may be selected as a node
  selection.
  */
  static isSelectable(e) {
    return !e.isText && e.type.spec.selectable !== !1;
  }
}
I.prototype.visible = !1;
_.jsonID("node", I);
class Ql {
  constructor(e) {
    this.anchor = e;
  }
  map(e) {
    let { deleted: t, pos: r } = e.mapResult(this.anchor);
    return t ? new Bi(r, r) : new Ql(r);
  }
  resolve(e) {
    let t = e.resolve(this.anchor), r = t.nodeAfter;
    return r && I.isSelectable(r) ? new I(t) : _.near(t);
  }
}
class ze extends _ {
  /**
  Create an all-selection over the given document.
  */
  constructor(e) {
    super(e.resolve(0), e.resolve(e.content.size));
  }
  replace(e, t = O.empty) {
    if (t == O.empty) {
      e.delete(0, e.doc.content.size);
      let r = _.atStart(e.doc);
      r.eq(e.selection) || e.setSelection(r);
    } else
      super.replace(e, t);
  }
  toJSON() {
    return { type: "all" };
  }
  /**
  @internal
  */
  static fromJSON(e) {
    return new ze(e);
  }
  map(e) {
    return new ze(e);
  }
  eq(e) {
    return e instanceof ze;
  }
  getBookmark() {
    return Mg;
  }
}
_.jsonID("all", ze);
const Mg = {
  map() {
    return this;
  },
  resolve(n) {
    return new ze(n);
  }
};
function Bn(n, e, t, r, o, i = !1) {
  if (e.inlineContent)
    return L.create(n, t);
  for (let s = r - (o > 0 ? 0 : 1); o > 0 ? s < e.childCount : s >= 0; s += o) {
    let l = e.child(s);
    if (l.isAtom) {
      if (!i && I.isSelectable(l))
        return I.create(n, t - (o < 0 ? l.nodeSize : 0));
    } else {
      let a = Bn(n, l, t + o, o < 0 ? l.childCount : 0, o, i);
      if (a)
        return a;
    }
    t += l.nodeSize * o;
  }
  return null;
}
function Sc(n, e, t) {
  let r = n.steps.length - 1;
  if (r < e)
    return;
  let o = n.steps[r];
  if (!(o instanceof fe || o instanceof he))
    return;
  let i = n.mapping.maps[r], s;
  i.forEach((l, a, c, u) => {
    s == null && (s = u);
  }), n.setSelection(_.near(n.doc.resolve(s), t));
}
const xc = 1, ao = 2, kc = 4;
class Tg extends Xl {
  /**
  @internal
  */
  constructor(e) {
    super(e.doc), this.curSelectionFor = 0, this.updated = 0, this.meta = /* @__PURE__ */ Object.create(null), this.time = Date.now(), this.curSelection = e.selection, this.storedMarks = e.storedMarks;
  }
  /**
  The transaction's current selection. This defaults to the editor
  selection [mapped](https://prosemirror.net/docs/ref/#state.Selection.map) through the steps in the
  transaction, but can be overwritten with
  [`setSelection`](https://prosemirror.net/docs/ref/#state.Transaction.setSelection).
  */
  get selection() {
    return this.curSelectionFor < this.steps.length && (this.curSelection = this.curSelection.map(this.doc, this.mapping.slice(this.curSelectionFor)), this.curSelectionFor = this.steps.length), this.curSelection;
  }
  /**
  Update the transaction's current selection. Will determine the
  selection that the editor gets when the transaction is applied.
  */
  setSelection(e) {
    if (e.$from.doc != this.doc)
      throw new RangeError("Selection passed to setSelection must point at the current document");
    return this.curSelection = e, this.curSelectionFor = this.steps.length, this.updated = (this.updated | xc) & ~ao, this.storedMarks = null, this;
  }
  /**
  Whether the selection was explicitly updated by this transaction.
  */
  get selectionSet() {
    return (this.updated & xc) > 0;
  }
  /**
  Set the current stored marks.
  */
  setStoredMarks(e) {
    return this.storedMarks = e, this.updated |= ao, this;
  }
  /**
  Make sure the current stored marks or, if that is null, the marks
  at the selection, match the given set of marks. Does nothing if
  this is already the case.
  */
  ensureMarks(e) {
    return Z.sameSet(this.storedMarks || this.selection.$from.marks(), e) || this.setStoredMarks(e), this;
  }
  /**
  Add a mark to the set of stored marks.
  */
  addStoredMark(e) {
    return this.ensureMarks(e.addToSet(this.storedMarks || this.selection.$head.marks()));
  }
  /**
  Remove a mark or mark type from the set of stored marks.
  */
  removeStoredMark(e) {
    return this.ensureMarks(e.removeFromSet(this.storedMarks || this.selection.$head.marks()));
  }
  /**
  Whether the stored marks were explicitly set for this transaction.
  */
  get storedMarksSet() {
    return (this.updated & ao) > 0;
  }
  /**
  @internal
  */
  addStep(e, t) {
    super.addStep(e, t), this.updated = this.updated & ~ao, this.storedMarks = null;
  }
  /**
  Update the timestamp for the transaction.
  */
  setTime(e) {
    return this.time = e, this;
  }
  /**
  Replace the current selection with the given slice.
  */
  replaceSelection(e) {
    return this.selection.replace(this, e), this;
  }
  /**
  Replace the selection with the given node. When `inheritMarks` is
  true and the content is inline, it inherits the marks from the
  place where it is inserted.
  */
  replaceSelectionWith(e, t = !0) {
    let r = this.selection;
    return t && (e = e.mark(this.storedMarks || (r.empty ? r.$from.marks() : r.$from.marksAcross(r.$to) || Z.none))), r.replaceWith(this, e), this;
  }
  /**
  Delete the selection.
  */
  deleteSelection() {
    return this.selection.replace(this), this;
  }
  /**
  Replace the given range, or the selection if no range is given,
  with a text node containing the given string.
  */
  insertText(e, t, r) {
    let o = this.doc.type.schema;
    if (t == null)
      return e ? this.replaceSelectionWith(o.text(e), !0) : this.deleteSelection();
    {
      if (r == null && (r = t), !e)
        return this.deleteRange(t, r);
      let i = this.storedMarks;
      if (!i) {
        let s = this.doc.resolve(t);
        i = r == t ? s.marks() : s.marksAcross(this.doc.resolve(r));
      }
      return this.replaceRangeWith(t, r, o.text(e, i)), !this.selection.empty && this.selection.to == t + e.length && this.setSelection(_.near(this.selection.$to)), this;
    }
  }
  /**
  Store a metadata property in this transaction, keyed either by
  name or by plugin.
  */
  setMeta(e, t) {
    return this.meta[typeof e == "string" ? e : e.key] = t, this;
  }
  /**
  Retrieve a metadata property for a given name or plugin.
  */
  getMeta(e) {
    return this.meta[typeof e == "string" ? e : e.key];
  }
  /**
  Returns true if this transaction doesn't contain any metadata,
  and can thus safely be extended.
  */
  get isGeneric() {
    for (let e in this.meta)
      return !1;
    return !0;
  }
  /**
  Indicate that the editor should scroll the selection into view
  when updated to the state produced by this transaction.
  */
  scrollIntoView() {
    return this.updated |= kc, this;
  }
  /**
  True when this transaction has had `scrollIntoView` called on it.
  */
  get scrolledIntoView() {
    return (this.updated & kc) > 0;
  }
}
function Ec(n, e) {
  return !e || !n ? n : n.bind(e);
}
class mr {
  constructor(e, t, r) {
    this.name = e, this.init = Ec(t.init, r), this.apply = Ec(t.apply, r);
  }
}
const Ag = [
  new mr("doc", {
    init(n) {
      return n.doc || n.schema.topNodeType.createAndFill();
    },
    apply(n) {
      return n.doc;
    }
  }),
  new mr("selection", {
    init(n, e) {
      return n.selection || _.atStart(e.doc);
    },
    apply(n) {
      return n.selection;
    }
  }),
  new mr("storedMarks", {
    init(n) {
      return n.storedMarks || null;
    },
    apply(n, e, t, r) {
      return r.selection.$cursor ? n.storedMarks : null;
    }
  }),
  new mr("scrollToSelection", {
    init() {
      return 0;
    },
    apply(n, e) {
      return n.scrolledIntoView ? e + 1 : e;
    }
  })
];
class Ss {
  constructor(e, t) {
    this.schema = e, this.plugins = [], this.pluginsByKey = /* @__PURE__ */ Object.create(null), this.fields = Ag.slice(), t && t.forEach((r) => {
      if (this.pluginsByKey[r.key])
        throw new RangeError("Adding different instances of a keyed plugin (" + r.key + ")");
      this.plugins.push(r), this.pluginsByKey[r.key] = r, r.spec.state && this.fields.push(new mr(r.key, r.spec.state, r));
    });
  }
}
class $n {
  /**
  @internal
  */
  constructor(e) {
    this.config = e;
  }
  /**
  The schema of the state's document.
  */
  get schema() {
    return this.config.schema;
  }
  /**
  The plugins that are active in this state.
  */
  get plugins() {
    return this.config.plugins;
  }
  /**
  Apply the given transaction to produce a new state.
  */
  apply(e) {
    return this.applyTransaction(e).state;
  }
  /**
  @internal
  */
  filterTransaction(e, t = -1) {
    for (let r = 0; r < this.config.plugins.length; r++)
      if (r != t) {
        let o = this.config.plugins[r];
        if (o.spec.filterTransaction && !o.spec.filterTransaction.call(o, e, this))
          return !1;
      }
    return !0;
  }
  /**
  Verbose variant of [`apply`](https://prosemirror.net/docs/ref/#state.EditorState.apply) that
  returns the precise transactions that were applied (which might
  be influenced by the [transaction
  hooks](https://prosemirror.net/docs/ref/#state.PluginSpec.filterTransaction) of
  plugins) along with the new state.
  */
  applyTransaction(e) {
    if (!this.filterTransaction(e))
      return { state: this, transactions: [] };
    let t = [e], r = this.applyInner(e), o = null;
    for (; ; ) {
      let i = !1;
      for (let s = 0; s < this.config.plugins.length; s++) {
        let l = this.config.plugins[s];
        if (l.spec.appendTransaction) {
          let a = o ? o[s].n : 0, c = o ? o[s].state : this, u = a < t.length && l.spec.appendTransaction.call(l, a ? t.slice(a) : t, c, r);
          if (u && r.filterTransaction(u, s)) {
            if (u.setMeta("appendedTransaction", e), !o) {
              o = [];
              for (let d = 0; d < this.config.plugins.length; d++)
                o.push(d < s ? { state: r, n: t.length } : { state: this, n: 0 });
            }
            t.push(u), r = r.applyInner(u), i = !0;
          }
          o && (o[s] = { state: r, n: t.length });
        }
      }
      if (!i)
        return { state: r, transactions: t };
    }
  }
  /**
  @internal
  */
  applyInner(e) {
    if (!e.before.eq(this.doc))
      throw new RangeError("Applying a mismatched transaction");
    let t = new $n(this.config), r = this.config.fields;
    for (let o = 0; o < r.length; o++) {
      let i = r[o];
      t[i.name] = i.apply(e, this[i.name], this, t);
    }
    return t;
  }
  /**
  Accessor that constructs and returns a new [transaction](https://prosemirror.net/docs/ref/#state.Transaction) from this state.
  */
  get tr() {
    return new Tg(this);
  }
  /**
  Create a new state.
  */
  static create(e) {
    let t = new Ss(e.doc ? e.doc.type.schema : e.schema, e.plugins), r = new $n(t);
    for (let o = 0; o < t.fields.length; o++)
      r[t.fields[o].name] = t.fields[o].init(e, r);
    return r;
  }
  /**
  Create a new state based on this one, but with an adjusted set
  of active plugins. State fields that exist in both sets of
  plugins are kept unchanged. Those that no longer exist are
  dropped, and those that are new are initialized using their
  [`init`](https://prosemirror.net/docs/ref/#state.StateField.init) method, passing in the new
  configuration object..
  */
  reconfigure(e) {
    let t = new Ss(this.schema, e.plugins), r = t.fields, o = new $n(t);
    for (let i = 0; i < r.length; i++) {
      let s = r[i].name;
      o[s] = this.hasOwnProperty(s) ? this[s] : r[i].init(e, o);
    }
    return o;
  }
  /**
  Serialize this state to JSON. If you want to serialize the state
  of plugins, pass an object mapping property names to use in the
  resulting JSON object to plugin objects. The argument may also be
  a string or number, in which case it is ignored, to support the
  way `JSON.stringify` calls `toString` methods.
  */
  toJSON(e) {
    let t = { doc: this.doc.toJSON(), selection: this.selection.toJSON() };
    if (this.storedMarks && (t.storedMarks = this.storedMarks.map((r) => r.toJSON())), e && typeof e == "object")
      for (let r in e) {
        if (r == "doc" || r == "selection")
          throw new RangeError("The JSON fields `doc` and `selection` are reserved");
        let o = e[r], i = o.spec.state;
        i && i.toJSON && (t[r] = i.toJSON.call(o, this[o.key]));
      }
    return t;
  }
  /**
  Deserialize a JSON representation of a state. `config` should
  have at least a `schema` field, and should contain array of
  plugins to initialize the state with. `pluginFields` can be used
  to deserialize the state of plugins, by associating plugin
  instances with the property names they use in the JSON object.
  */
  static fromJSON(e, t, r) {
    if (!t)
      throw new RangeError("Invalid input for EditorState.fromJSON");
    if (!e.schema)
      throw new RangeError("Required config field 'schema' missing");
    let o = new Ss(e.schema, e.plugins), i = new $n(o);
    return o.fields.forEach((s) => {
      if (s.name == "doc")
        i.doc = jt.fromJSON(e.schema, t.doc);
      else if (s.name == "selection")
        i.selection = _.fromJSON(i.doc, t.selection);
      else if (s.name == "storedMarks")
        t.storedMarks && (i.storedMarks = t.storedMarks.map(e.schema.markFromJSON));
      else {
        if (r)
          for (let l in r) {
            let a = r[l], c = a.spec.state;
            if (a.key == s.name && c && c.fromJSON && Object.prototype.hasOwnProperty.call(t, l)) {
              i[s.name] = c.fromJSON.call(a, e, t[l], i);
              return;
            }
          }
        i[s.name] = s.init(e, i);
      }
    }), i;
  }
}
function qd(n, e, t) {
  for (let r in n) {
    let o = n[r];
    o instanceof Function ? o = o.bind(e) : r == "handleDOMEvents" && (o = qd(o, e, {})), t[r] = o;
  }
  return t;
}
class ue {
  /**
  Create a plugin.
  */
  constructor(e) {
    this.spec = e, this.props = {}, e.props && qd(e.props, this, this.props), this.key = e.key ? e.key.key : Gd("plugin");
  }
  /**
  Extract the plugin's state field from an editor state.
  */
  getState(e) {
    return e[this.key];
  }
}
const xs = /* @__PURE__ */ Object.create(null);
function Gd(n) {
  return n in xs ? n + "$" + ++xs[n] : (xs[n] = 0, n + "$");
}
class Me {
  /**
  Create a plugin key.
  */
  constructor(e = "key") {
    this.key = Gd(e);
  }
  /**
  Get the active plugin with this key, if any, from an editor
  state.
  */
  get(e) {
    return e.config.pluginsByKey[this.key];
  }
  /**
  Get the plugin's state from an editor state.
  */
  getState(e) {
    return e[this.key];
  }
}
const be = function(n) {
  for (var e = 0; ; e++)
    if (n = n.previousSibling, !n)
      return e;
}, qn = function(n) {
  let e = n.assignedSlot || n.parentNode;
  return e && e.nodeType == 11 ? e.host : e;
};
let al = null;
const Ct = function(n, e, t) {
  let r = al || (al = document.createRange());
  return r.setEnd(n, t ?? n.nodeValue.length), r.setStart(n, e || 0), r;
}, Og = function() {
  al = null;
}, vn = function(n, e, t, r) {
  return t && (Mc(n, e, t, r, -1) || Mc(n, e, t, r, 1));
}, Ng = /^(img|br|input|textarea|hr)$/i;
function Mc(n, e, t, r, o) {
  for (var i; ; ) {
    if (n == t && e == r)
      return !0;
    if (e == (o < 0 ? 0 : Ke(n))) {
      let s = n.parentNode;
      if (!s || s.nodeType != 1 || qr(n) || Ng.test(n.nodeName) || n.contentEditable == "false")
        return !1;
      e = be(n) + (o < 0 ? 0 : 1), n = s;
    } else if (n.nodeType == 1) {
      let s = n.childNodes[e + (o < 0 ? -1 : 0)];
      if (s.nodeType == 1 && s.contentEditable == "false")
        if (!((i = s.pmViewDesc) === null || i === void 0) && i.ignoreForSelection)
          e += o;
        else
          return !1;
      else
        n = s, e = o < 0 ? Ke(n) : 0;
    } else
      return !1;
  }
}
function Ke(n) {
  return n.nodeType == 3 ? n.nodeValue.length : n.childNodes.length;
}
function Rg(n, e) {
  for (; ; ) {
    if (n.nodeType == 3 && e)
      return n;
    if (n.nodeType == 1 && e > 0) {
      if (n.contentEditable == "false")
        return null;
      n = n.childNodes[e - 1], e = Ke(n);
    } else if (n.parentNode && !qr(n))
      e = be(n), n = n.parentNode;
    else
      return null;
  }
}
function Dg(n, e) {
  for (; ; ) {
    if (n.nodeType == 3 && e < n.nodeValue.length)
      return n;
    if (n.nodeType == 1 && e < n.childNodes.length) {
      if (n.contentEditable == "false")
        return null;
      n = n.childNodes[e], e = 0;
    } else if (n.parentNode && !qr(n))
      e = be(n) + 1, n = n.parentNode;
    else
      return null;
  }
}
function Pg(n, e, t) {
  for (let r = e == 0, o = e == Ke(n); r || o; ) {
    if (n == t)
      return !0;
    let i = be(n);
    if (n = n.parentNode, !n)
      return !1;
    r = r && i == 0, o = o && i == Ke(n);
  }
}
function qr(n) {
  let e;
  for (let t = n; t && !(e = t.pmViewDesc); t = t.parentNode)
    ;
  return e && e.node && e.node.isBlock && (e.dom == n || e.contentDOM == n);
}
const Fi = function(n) {
  return n.focusNode && vn(n.focusNode, n.focusOffset, n.anchorNode, n.anchorOffset);
};
function ln(n, e) {
  let t = document.createEvent("Event");
  return t.initEvent("keydown", !0, !0), t.keyCode = n, t.key = t.code = e, t;
}
function Ig(n) {
  let e = n.activeElement;
  for (; e && e.shadowRoot; )
    e = e.shadowRoot.activeElement;
  return e;
}
function Lg(n, e, t) {
  if (n.caretPositionFromPoint)
    try {
      let r = n.caretPositionFromPoint(e, t);
      if (r)
        return { node: r.offsetNode, offset: Math.min(Ke(r.offsetNode), r.offset) };
    } catch {
    }
  if (n.caretRangeFromPoint) {
    let r = n.caretRangeFromPoint(e, t);
    if (r)
      return { node: r.startContainer, offset: Math.min(Ke(r.startContainer), r.startOffset) };
  }
}
const dt = typeof navigator < "u" ? navigator : null, Tc = typeof document < "u" ? document : null, en = dt && dt.userAgent || "", cl = /Edge\/(\d+)/.exec(en), Jd = /MSIE \d/.exec(en), ul = /Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(en), Le = !!(Jd || ul || cl), Kt = Jd ? document.documentMode : ul ? +ul[1] : cl ? +cl[1] : 0, qe = !Le && /gecko\/(\d+)/i.test(en);
qe && +(/Firefox\/(\d+)/.exec(en) || [0, 0])[1];
const dl = !Le && /Chrome\/(\d+)/.exec(en), ve = !!dl, Yd = dl ? +dl[1] : 0, Se = !Le && !!dt && /Apple Computer/.test(dt.vendor), Gn = Se && (/Mobile\/\w+/.test(en) || !!dt && dt.maxTouchPoints > 2), Ue = Gn || (dt ? /Mac/.test(dt.platform) : !1), Xd = dt ? /Win/.test(dt.platform) : !1, St = /Android \d/.test(en), Gr = !!Tc && "webkitFontSmoothing" in Tc.documentElement.style, _g = Gr ? +(/\bAppleWebKit\/(\d+)/.exec(navigator.userAgent) || [0, 0])[1] : 0;
function Bg(n) {
  let e = n.defaultView && n.defaultView.visualViewport;
  return e ? {
    left: 0,
    right: e.width,
    top: 0,
    bottom: e.height
  } : {
    left: 0,
    right: n.documentElement.clientWidth,
    top: 0,
    bottom: n.documentElement.clientHeight
  };
}
function gt(n, e) {
  return typeof n == "number" ? n : n[e];
}
function Fg(n) {
  let e = n.getBoundingClientRect(), t = e.width / n.offsetWidth || 1, r = e.height / n.offsetHeight || 1;
  return {
    left: e.left,
    right: e.left + n.clientWidth * t,
    top: e.top,
    bottom: e.top + n.clientHeight * r
  };
}
function Ac(n, e, t) {
  let r = n.someProp("scrollThreshold") || 0, o = n.someProp("scrollMargin") || 5, i = n.dom.ownerDocument;
  for (let s = t || n.dom; s; ) {
    if (s.nodeType != 1) {
      s = qn(s);
      continue;
    }
    let l = s, a = l == i.body, c = a ? Bg(i) : Fg(l), u = 0, d = 0;
    if (e.top < c.top + gt(r, "top") ? d = -(c.top - e.top + gt(o, "top")) : e.bottom > c.bottom - gt(r, "bottom") && (d = e.bottom - e.top > c.bottom - c.top ? e.top + gt(o, "top") - c.top : e.bottom - c.bottom + gt(o, "bottom")), e.left < c.left + gt(r, "left") ? u = -(c.left - e.left + gt(o, "left")) : e.right > c.right - gt(r, "right") && (u = e.right - c.right + gt(o, "right")), u || d)
      if (a)
        i.defaultView.scrollBy(u, d);
      else {
        let h = l.scrollLeft, p = l.scrollTop;
        d && (l.scrollTop += d), u && (l.scrollLeft += u);
        let m = l.scrollLeft - h, g = l.scrollTop - p;
        e = { left: e.left - m, top: e.top - g, right: e.right - m, bottom: e.bottom - g };
      }
    let f = a ? "fixed" : getComputedStyle(s).position;
    if (/^(fixed|sticky)$/.test(f))
      break;
    s = f == "absolute" ? s.offsetParent : qn(s);
  }
}
function zg(n) {
  let e = n.dom.getBoundingClientRect(), t = Math.max(0, e.top), r, o;
  for (let i = (e.left + e.right) / 2, s = t + 1; s < Math.min(innerHeight, e.bottom); s += 5) {
    let l = n.root.elementFromPoint(i, s);
    if (!l || l == n.dom || !n.dom.contains(l))
      continue;
    let a = l.getBoundingClientRect();
    if (a.top >= t - 20) {
      r = l, o = a.top;
      break;
    }
  }
  return { refDOM: r, refTop: o, stack: Qd(n.dom) };
}
function Qd(n) {
  let e = [], t = n.ownerDocument;
  for (let r = n; r && (e.push({ dom: r, top: r.scrollTop, left: r.scrollLeft }), n != t); r = qn(r))
    ;
  return e;
}
function $g({ refDOM: n, refTop: e, stack: t }) {
  let r = n ? n.getBoundingClientRect().top : 0;
  Zd(t, r == 0 ? 0 : r - e);
}
function Zd(n, e) {
  for (let t = 0; t < n.length; t++) {
    let { dom: r, top: o, left: i } = n[t];
    r.scrollTop != o + e && (r.scrollTop = o + e), r.scrollLeft != i && (r.scrollLeft = i);
  }
}
let Dn = null;
function Hg(n) {
  if (n.setActive)
    return n.setActive();
  if (Dn)
    return n.focus(Dn);
  let e = Qd(n);
  n.focus(Dn == null ? {
    get preventScroll() {
      return Dn = { preventScroll: !0 }, !0;
    }
  } : void 0), Dn || (Dn = !1, Zd(e, 0));
}
function ef(n, e) {
  let t, r = 2e8, o, i = 0, s = e.top, l = e.top, a, c;
  for (let u = n.firstChild, d = 0; u; u = u.nextSibling, d++) {
    let f;
    if (u.nodeType == 1)
      f = u.getClientRects();
    else if (u.nodeType == 3)
      f = Ct(u).getClientRects();
    else
      continue;
    for (let h = 0; h < f.length; h++) {
      let p = f[h];
      if (p.top <= s && p.bottom >= l) {
        s = Math.max(p.bottom, s), l = Math.min(p.top, l);
        let m = p.left > e.left ? p.left - e.left : p.right < e.left ? e.left - p.right : 0;
        if (m < r) {
          t = u, r = m, o = m && t.nodeType == 3 ? {
            left: p.right < e.left ? p.right : p.left,
            top: e.top
          } : e, u.nodeType == 1 && m && (i = d + (e.left >= (p.left + p.right) / 2 ? 1 : 0));
          continue;
        }
      } else p.top > e.top && !a && p.left <= e.left && p.right >= e.left && (a = u, c = { left: Math.max(p.left, Math.min(p.right, e.left)), top: p.top });
      !t && (e.left >= p.right && e.top >= p.top || e.left >= p.left && e.top >= p.bottom) && (i = d + 1);
    }
  }
  return !t && a && (t = a, o = c, r = 0), t && t.nodeType == 3 ? Wg(t, o) : !t || r && t.nodeType == 1 ? { node: n, offset: i } : ef(t, o);
}
function Wg(n, e) {
  let t = n.nodeValue.length, r = document.createRange(), o;
  for (let i = 0; i < t; i++) {
    r.setEnd(n, i + 1), r.setStart(n, i);
    let s = Pt(r, 1);
    if (s.top != s.bottom && Zl(e, s)) {
      o = { node: n, offset: i + (e.left >= (s.left + s.right) / 2 ? 1 : 0) };
      break;
    }
  }
  return r.detach(), o || { node: n, offset: 0 };
}
function Zl(n, e) {
  return n.left >= e.left - 1 && n.left <= e.right + 1 && n.top >= e.top - 1 && n.top <= e.bottom + 1;
}
function Vg(n, e) {
  let t = n.parentNode;
  return t && /^li$/i.test(t.nodeName) && e.left < n.getBoundingClientRect().left ? t : n;
}
function jg(n, e, t) {
  let { node: r, offset: o } = ef(e, t), i = -1;
  if (r.nodeType == 1 && !r.firstChild) {
    let s = r.getBoundingClientRect();
    i = s.left != s.right && t.left > (s.left + s.right) / 2 ? 1 : -1;
  }
  return n.docView.posFromDOM(r, o, i);
}
function Ug(n, e, t, r) {
  let o = -1;
  for (let i = e, s = !1; i != n.dom; ) {
    let l = n.docView.nearestDesc(i, !0), a;
    if (!l)
      return null;
    if (l.dom.nodeType == 1 && (l.node.isBlock && l.parent || !l.contentDOM) && // Ignore elements with zero-size bounding rectangles
    ((a = l.dom.getBoundingClientRect()).width || a.height) && (l.node.isBlock && l.parent && !/^T(R|BODY|HEAD|FOOT)$/.test(l.dom.nodeName) && (!s && a.left > r.left || a.top > r.top ? o = l.posBefore : (!s && a.right < r.left || a.bottom < r.top) && (o = l.posAfter), s = !0), !l.contentDOM && o < 0 && !l.node.isText))
      return (l.node.isBlock ? r.top < (a.top + a.bottom) / 2 : r.left < (a.left + a.right) / 2) ? l.posBefore : l.posAfter;
    i = l.dom.parentNode;
  }
  return o > -1 ? o : n.docView.posFromDOM(e, t, -1);
}
function tf(n, e, t) {
  let r = n.childNodes.length;
  if (r && t.top < t.bottom)
    for (let o = Math.max(0, Math.min(r - 1, Math.floor(r * (e.top - t.top) / (t.bottom - t.top)) - 2)), i = o; ; ) {
      let s = n.childNodes[i];
      if (s.nodeType == 1) {
        let l = s.getClientRects();
        for (let a = 0; a < l.length; a++) {
          let c = l[a];
          if (Zl(e, c))
            return tf(s, e, c);
        }
      }
      if ((i = (i + 1) % r) == o)
        break;
    }
  return n;
}
function Kg(n, e) {
  let t = n.dom.ownerDocument, r, o = 0, i = Lg(t, e.left, e.top);
  i && ({ node: r, offset: o } = i);
  let s = (n.root.elementFromPoint ? n.root : t).elementFromPoint(e.left, e.top), l;
  if (!s || !n.dom.contains(s.nodeType != 1 ? s.parentNode : s)) {
    let c = n.dom.getBoundingClientRect();
    if (!Zl(e, c) || (s = tf(n.dom, e, c), !s))
      return null;
  }
  if (Se)
    for (let c = s; r && c; c = qn(c))
      c.draggable && (r = void 0);
  if (s = Vg(s, e), r) {
    if (qe && r.nodeType == 1 && (o = Math.min(o, r.childNodes.length), o < r.childNodes.length)) {
      let u = r.childNodes[o], d;
      u.nodeName == "IMG" && (d = u.getBoundingClientRect()).right <= e.left && d.bottom > e.top && o++;
    }
    let c;
    Gr && o && r.nodeType == 1 && (c = r.childNodes[o - 1]).nodeType == 1 && c.contentEditable == "false" && c.getBoundingClientRect().top >= e.top && o--, r == n.dom && o == r.childNodes.length - 1 && r.lastChild.nodeType == 1 && e.top > r.lastChild.getBoundingClientRect().bottom ? l = n.state.doc.content.size : (o == 0 || r.nodeType != 1 || r.childNodes[o - 1].nodeName != "BR") && (l = Ug(n, r, o, e));
  }
  l == null && (l = jg(n, s, e));
  let a = n.docView.nearestDesc(s, !0);
  return { pos: l, inside: a ? a.posAtStart - a.border : -1 };
}
function Oc(n) {
  return n.top < n.bottom || n.left < n.right;
}
function Pt(n, e) {
  let t = n.getClientRects();
  if (t.length) {
    let r = t[e < 0 ? 0 : t.length - 1];
    if (Oc(r))
      return r;
  }
  return Array.prototype.find.call(t, Oc) || n.getBoundingClientRect();
}
const qg = /[\u0590-\u05f4\u0600-\u06ff\u0700-\u08ac]/;
function nf(n, e, t) {
  let { node: r, offset: o, atom: i } = n.docView.domFromPos(e, t < 0 ? -1 : 1), s = Gr || qe;
  if (r.nodeType == 3)
    if (s && (qg.test(r.nodeValue) || (t < 0 ? !o : o == r.nodeValue.length))) {
      let a = Pt(Ct(r, o, o), t);
      if (qe && o && /\s/.test(r.nodeValue[o - 1]) && o < r.nodeValue.length) {
        let c = Pt(Ct(r, o - 1, o - 1), -1);
        if (c.top == a.top) {
          let u = Pt(Ct(r, o, o + 1), -1);
          if (u.top != a.top)
            return dr(u, u.left < c.left);
        }
      }
      return a;
    } else {
      let a = o, c = o, u = t < 0 ? 1 : -1;
      return t < 0 && !o ? (c++, u = -1) : t >= 0 && o == r.nodeValue.length ? (a--, u = 1) : t < 0 ? a-- : c++, dr(Pt(Ct(r, a, c), u), u < 0);
    }
  if (!n.state.doc.resolve(e - (i || 0)).parent.inlineContent) {
    if (i == null && o && (t < 0 || o == Ke(r))) {
      let a = r.childNodes[o - 1];
      if (a.nodeType == 1)
        return ks(a.getBoundingClientRect(), !1);
    }
    if (i == null && o < Ke(r)) {
      let a = r.childNodes[o];
      if (a.nodeType == 1)
        return ks(a.getBoundingClientRect(), !0);
    }
    return ks(r.getBoundingClientRect(), t >= 0);
  }
  if (i == null && o && (t < 0 || o == Ke(r))) {
    let a = r.childNodes[o - 1], c = a.nodeType == 3 ? Ct(a, Ke(a) - (s ? 0 : 1)) : a.nodeType == 1 && (a.nodeName != "BR" || !a.nextSibling) ? a : null;
    if (c)
      return dr(Pt(c, 1), !1);
  }
  if (i == null && o < Ke(r)) {
    let a = r.childNodes[o];
    for (; a.pmViewDesc && a.pmViewDesc.ignoreForCoords; )
      a = a.nextSibling;
    let c = a ? a.nodeType == 3 ? Ct(a, 0, s ? 0 : 1) : a.nodeType == 1 ? a : null : null;
    if (c)
      return dr(Pt(c, -1), !0);
  }
  return dr(Pt(r.nodeType == 3 ? Ct(r) : r, -t), t >= 0);
}
function dr(n, e) {
  if (n.width == 0)
    return n;
  let t = e ? n.left : n.right;
  return { top: n.top, bottom: n.bottom, left: t, right: t };
}
function ks(n, e) {
  if (n.height == 0)
    return n;
  let t = e ? n.top : n.bottom;
  return { top: t, bottom: t, left: n.left, right: n.right };
}
function rf(n, e, t) {
  let r = n.state, o = n.root.activeElement;
  r != e && n.updateState(e), o != n.dom && n.focus();
  try {
    return t();
  } finally {
    r != e && n.updateState(r), o != n.dom && o && o.focus();
  }
}
function Gg(n, e, t) {
  let r = e.selection, o = t == "up" ? r.$from : r.$to;
  return rf(n, e, () => {
    let { node: i } = n.docView.domFromPos(o.pos, t == "up" ? -1 : 1);
    for (; ; ) {
      let l = n.docView.nearestDesc(i, !0);
      if (!l)
        break;
      if (l.node.isBlock) {
        i = l.contentDOM || l.dom;
        break;
      }
      i = l.dom.parentNode;
    }
    let s = nf(n, o.pos, 1);
    for (let l = i.firstChild; l; l = l.nextSibling) {
      let a;
      if (l.nodeType == 1)
        a = l.getClientRects();
      else if (l.nodeType == 3)
        a = Ct(l, 0, l.nodeValue.length).getClientRects();
      else
        continue;
      for (let c = 0; c < a.length; c++) {
        let u = a[c];
        if (u.bottom > u.top + 1 && (t == "up" ? s.top - u.top > (u.bottom - s.top) * 2 : u.bottom - s.bottom > (s.bottom - u.top) * 2))
          return !1;
      }
    }
    return !0;
  });
}
const Jg = /[\u0590-\u08ac]/;
function Yg(n, e, t) {
  let { $head: r } = e.selection;
  if (!r.parent.isTextblock)
    return !1;
  let o = r.parentOffset, i = !o, s = o == r.parent.content.size, l = n.domSelection();
  return l ? !Jg.test(r.parent.textContent) || !l.modify ? t == "left" || t == "backward" ? i : s : rf(n, e, () => {
    let { focusNode: a, focusOffset: c, anchorNode: u, anchorOffset: d } = n.domSelectionRange(), f = l.caretBidiLevel;
    l.modify("move", t, "character");
    let h = r.depth ? n.docView.domAfterPos(r.before()) : n.dom, { focusNode: p, focusOffset: m } = n.domSelectionRange(), g = p && !h.contains(p.nodeType == 1 ? p : p.parentNode) || a == p && c == m;
    try {
      l.collapse(u, d), a && (a != u || c != d) && l.extend && l.extend(a, c);
    } catch {
    }
    return f != null && (l.caretBidiLevel = f), g;
  }) : r.pos == r.start() || r.pos == r.end();
}
let Nc = null, Rc = null, Dc = !1;
function Xg(n, e, t) {
  return Nc == e && Rc == t ? Dc : (Nc = e, Rc = t, Dc = t == "up" || t == "down" ? Gg(n, e, t) : Yg(n, e, t));
}
const Ge = 0, Pc = 1, cn = 2, ft = 3;
class Jr {
  constructor(e, t, r, o) {
    this.parent = e, this.children = t, this.dom = r, this.contentDOM = o, this.dirty = Ge, r.pmViewDesc = this;
  }
  // Used to check whether a given description corresponds to a
  // widget/mark/node.
  matchesWidget(e) {
    return !1;
  }
  matchesMark(e) {
    return !1;
  }
  matchesNode(e, t, r) {
    return !1;
  }
  matchesHack(e) {
    return !1;
  }
  // When parsing in-editor content (in domchange.js), we allow
  // descriptions to determine the parse rules that should be used to
  // parse them.
  parseRule() {
    return null;
  }
  // Used by the editor's event handler to ignore events that come
  // from certain descs.
  stopEvent(e) {
    return !1;
  }
  // The size of the content represented by this desc.
  get size() {
    let e = 0;
    for (let t = 0; t < this.children.length; t++)
      e += this.children[t].size;
    return e;
  }
  // For block nodes, this represents the space taken up by their
  // start/end tokens.
  get border() {
    return 0;
  }
  destroy() {
    this.parent = void 0, this.dom.pmViewDesc == this && (this.dom.pmViewDesc = void 0);
    for (let e = 0; e < this.children.length; e++)
      this.children[e].destroy();
  }
  posBeforeChild(e) {
    for (let t = 0, r = this.posAtStart; ; t++) {
      let o = this.children[t];
      if (o == e)
        return r;
      r += o.size;
    }
  }
  get posBefore() {
    return this.parent.posBeforeChild(this);
  }
  get posAtStart() {
    return this.parent ? this.parent.posBeforeChild(this) + this.border : 0;
  }
  get posAfter() {
    return this.posBefore + this.size;
  }
  get posAtEnd() {
    return this.posAtStart + this.size - 2 * this.border;
  }
  localPosFromDOM(e, t, r) {
    if (this.contentDOM && this.contentDOM.contains(e.nodeType == 1 ? e : e.parentNode))
      if (r < 0) {
        let i, s;
        if (e == this.contentDOM)
          i = e.childNodes[t - 1];
        else {
          for (; e.parentNode != this.contentDOM; )
            e = e.parentNode;
          i = e.previousSibling;
        }
        for (; i && !((s = i.pmViewDesc) && s.parent == this); )
          i = i.previousSibling;
        return i ? this.posBeforeChild(s) + s.size : this.posAtStart;
      } else {
        let i, s;
        if (e == this.contentDOM)
          i = e.childNodes[t];
        else {
          for (; e.parentNode != this.contentDOM; )
            e = e.parentNode;
          i = e.nextSibling;
        }
        for (; i && !((s = i.pmViewDesc) && s.parent == this); )
          i = i.nextSibling;
        return i ? this.posBeforeChild(s) : this.posAtEnd;
      }
    let o;
    if (e == this.dom && this.contentDOM)
      o = t > be(this.contentDOM);
    else if (this.contentDOM && this.contentDOM != this.dom && this.dom.contains(this.contentDOM))
      o = e.compareDocumentPosition(this.contentDOM) & 2;
    else if (this.dom.firstChild) {
      if (t == 0)
        for (let i = e; ; i = i.parentNode) {
          if (i == this.dom) {
            o = !1;
            break;
          }
          if (i.previousSibling)
            break;
        }
      if (o == null && t == e.childNodes.length)
        for (let i = e; ; i = i.parentNode) {
          if (i == this.dom) {
            o = !0;
            break;
          }
          if (i.nextSibling)
            break;
        }
    }
    return o ?? r > 0 ? this.posAtEnd : this.posAtStart;
  }
  nearestDesc(e, t = !1) {
    for (let r = !0, o = e; o; o = o.parentNode) {
      let i = this.getDesc(o), s;
      if (i && (!t || i.node))
        if (r && (s = i.nodeDOM) && !(s.nodeType == 1 ? s.contains(e.nodeType == 1 ? e : e.parentNode) : s == e))
          r = !1;
        else
          return i;
    }
  }
  getDesc(e) {
    let t = e.pmViewDesc;
    for (let r = t; r; r = r.parent)
      if (r == this)
        return t;
  }
  posFromDOM(e, t, r) {
    for (let o = e; o; o = o.parentNode) {
      let i = this.getDesc(o);
      if (i)
        return i.localPosFromDOM(e, t, r);
    }
    return -1;
  }
  // Find the desc for the node after the given pos, if any. (When a
  // parent node overrode rendering, there might not be one.)
  descAt(e) {
    for (let t = 0, r = 0; t < this.children.length; t++) {
      let o = this.children[t], i = r + o.size;
      if (r == e && i != r) {
        for (; !o.border && o.children.length; )
          for (let s = 0; s < o.children.length; s++) {
            let l = o.children[s];
            if (l.size) {
              o = l;
              break;
            }
          }
        return o;
      }
      if (e < i)
        return o.descAt(e - r - o.border);
      r = i;
    }
  }
  domFromPos(e, t) {
    if (!this.contentDOM)
      return { node: this.dom, offset: 0, atom: e + 1 };
    let r = 0, o = 0;
    for (let i = 0; r < this.children.length; r++) {
      let s = this.children[r], l = i + s.size;
      if (l > e || s instanceof sf) {
        o = e - i;
        break;
      }
      i = l;
    }
    if (o)
      return this.children[r].domFromPos(o - this.children[r].border, t);
    for (let i; r && !(i = this.children[r - 1]).size && i instanceof of && i.side >= 0; r--)
      ;
    if (t <= 0) {
      let i, s = !0;
      for (; i = r ? this.children[r - 1] : null, !(!i || i.dom.parentNode == this.contentDOM); r--, s = !1)
        ;
      return i && t && s && !i.border && !i.domAtom ? i.domFromPos(i.size, t) : { node: this.contentDOM, offset: i ? be(i.dom) + 1 : 0 };
    } else {
      let i, s = !0;
      for (; i = r < this.children.length ? this.children[r] : null, !(!i || i.dom.parentNode == this.contentDOM); r++, s = !1)
        ;
      return i && s && !i.border && !i.domAtom ? i.domFromPos(0, t) : { node: this.contentDOM, offset: i ? be(i.dom) : this.contentDOM.childNodes.length };
    }
  }
  // Used to find a DOM range in a single parent for a given changed
  // range.
  parseRange(e, t, r = 0) {
    if (this.children.length == 0)
      return { node: this.contentDOM, from: e, to: t, fromOffset: 0, toOffset: this.contentDOM.childNodes.length };
    let o = -1, i = -1;
    for (let s = r, l = 0; ; l++) {
      let a = this.children[l], c = s + a.size;
      if (o == -1 && e <= c) {
        let u = s + a.border;
        if (e >= u && t <= c - a.border && a.node && a.contentDOM && this.contentDOM.contains(a.contentDOM))
          return a.parseRange(e, t, u);
        e = s;
        for (let d = l; d > 0; d--) {
          let f = this.children[d - 1];
          if (f.size && f.dom.parentNode == this.contentDOM && !f.emptyChildAt(1)) {
            o = be(f.dom) + 1;
            break;
          }
          e -= f.size;
        }
        o == -1 && (o = 0);
      }
      if (o > -1 && (c > t || l == this.children.length - 1)) {
        t = c;
        for (let u = l + 1; u < this.children.length; u++) {
          let d = this.children[u];
          if (d.size && d.dom.parentNode == this.contentDOM && !d.emptyChildAt(-1)) {
            i = be(d.dom);
            break;
          }
          t += d.size;
        }
        i == -1 && (i = this.contentDOM.childNodes.length);
        break;
      }
      s = c;
    }
    return { node: this.contentDOM, from: e, to: t, fromOffset: o, toOffset: i };
  }
  emptyChildAt(e) {
    if (this.border || !this.contentDOM || !this.children.length)
      return !1;
    let t = this.children[e < 0 ? 0 : this.children.length - 1];
    return t.size == 0 || t.emptyChildAt(e);
  }
  domAfterPos(e) {
    let { node: t, offset: r } = this.domFromPos(e, 0);
    if (t.nodeType != 1 || r == t.childNodes.length)
      throw new RangeError("No node after pos " + e);
    return t.childNodes[r];
  }
  // View descs are responsible for setting any selection that falls
  // entirely inside of them, so that custom implementations can do
  // custom things with the selection. Note that this falls apart when
  // a selection starts in such a node and ends in another, in which
  // case we just use whatever domFromPos produces as a best effort.
  setSelection(e, t, r, o = !1) {
    let i = Math.min(e, t), s = Math.max(e, t);
    for (let h = 0, p = 0; h < this.children.length; h++) {
      let m = this.children[h], g = p + m.size;
      if (i > p && s < g)
        return m.setSelection(e - p - m.border, t - p - m.border, r, o);
      p = g;
    }
    let l = this.domFromPos(e, e ? -1 : 1), a = t == e ? l : this.domFromPos(t, t ? -1 : 1), c = r.root.getSelection(), u = r.domSelectionRange(), d = !1;
    if ((qe || Se) && e == t) {
      let { node: h, offset: p } = l;
      if (h.nodeType == 3) {
        if (d = !!(p && h.nodeValue[p - 1] == `
`), d && p == h.nodeValue.length)
          for (let m = h, g; m; m = m.parentNode) {
            if (g = m.nextSibling) {
              g.nodeName == "BR" && (l = a = { node: g.parentNode, offset: be(g) + 1 });
              break;
            }
            let y = m.pmViewDesc;
            if (y && y.node && y.node.isBlock)
              break;
          }
      } else {
        let m = h.childNodes[p - 1];
        d = m && (m.nodeName == "BR" || m.contentEditable == "false");
      }
    }
    if (qe && u.focusNode && u.focusNode != a.node && u.focusNode.nodeType == 1) {
      let h = u.focusNode.childNodes[u.focusOffset];
      h && h.contentEditable == "false" && (o = !0);
    }
    if (!(o || d && Se) && vn(l.node, l.offset, u.anchorNode, u.anchorOffset) && vn(a.node, a.offset, u.focusNode, u.focusOffset))
      return;
    let f = !1;
    if ((c.extend || e == t) && !(d && qe)) {
      c.collapse(l.node, l.offset);
      try {
        e != t && c.extend(a.node, a.offset), f = !0;
      } catch {
      }
    }
    if (!f) {
      if (e > t) {
        let p = l;
        l = a, a = p;
      }
      let h = document.createRange();
      h.setEnd(a.node, a.offset), h.setStart(l.node, l.offset), c.removeAllRanges(), c.addRange(h);
    }
  }
  ignoreMutation(e) {
    return !this.contentDOM && e.type != "selection";
  }
  get contentLost() {
    return this.contentDOM && this.contentDOM != this.dom && !this.dom.contains(this.contentDOM);
  }
  // Remove a subtree of the element tree that has been touched
  // by a DOM change, so that the next update will redraw it.
  markDirty(e, t) {
    for (let r = 0, o = 0; o < this.children.length; o++) {
      let i = this.children[o], s = r + i.size;
      if (r == s ? e <= s && t >= r : e < s && t > r) {
        let l = r + i.border, a = s - i.border;
        if (e >= l && t <= a) {
          this.dirty = e == r || t == s ? cn : Pc, e == l && t == a && (i.contentLost || i.dom.parentNode != this.contentDOM) ? i.dirty = ft : i.markDirty(e - l, t - l);
          return;
        } else
          i.dirty = i.dom == i.contentDOM && i.dom.parentNode == this.contentDOM && !i.children.length ? cn : ft;
      }
      r = s;
    }
    this.dirty = cn;
  }
  markParentsDirty() {
    let e = 1;
    for (let t = this.parent; t; t = t.parent, e++) {
      let r = e == 1 ? cn : Pc;
      t.dirty < r && (t.dirty = r);
    }
  }
  get domAtom() {
    return !1;
  }
  get ignoreForCoords() {
    return !1;
  }
  get ignoreForSelection() {
    return !1;
  }
  isText(e) {
    return !1;
  }
}
class of extends Jr {
  constructor(e, t, r, o) {
    let i, s = t.type.toDOM;
    if (typeof s == "function" && (s = s(r, () => {
      if (!i)
        return o;
      if (i.parent)
        return i.parent.posBeforeChild(i);
    })), !t.type.spec.raw) {
      if (s.nodeType != 1) {
        let l = document.createElement("span");
        l.appendChild(s), s = l;
      }
      s.contentEditable = "false", s.classList.add("ProseMirror-widget");
    }
    super(e, [], s, null), this.widget = t, this.widget = t, i = this;
  }
  matchesWidget(e) {
    return this.dirty == Ge && e.type.eq(this.widget.type);
  }
  parseRule() {
    return { ignore: !0 };
  }
  stopEvent(e) {
    let t = this.widget.spec.stopEvent;
    return t ? t(e) : !1;
  }
  ignoreMutation(e) {
    return e.type != "selection" || this.widget.spec.ignoreSelection;
  }
  destroy() {
    this.widget.type.destroy(this.dom), super.destroy();
  }
  get domAtom() {
    return !0;
  }
  get ignoreForSelection() {
    return !!this.widget.type.spec.relaxedSide;
  }
  get side() {
    return this.widget.type.side;
  }
}
class Qg extends Jr {
  constructor(e, t, r, o) {
    super(e, [], t, null), this.textDOM = r, this.text = o;
  }
  get size() {
    return this.text.length;
  }
  localPosFromDOM(e, t) {
    return e != this.textDOM ? this.posAtStart + (t ? this.size : 0) : this.posAtStart + t;
  }
  domFromPos(e) {
    return { node: this.textDOM, offset: e };
  }
  ignoreMutation(e) {
    return e.type === "characterData" && e.target.nodeValue == e.oldValue;
  }
}
class Cn extends Jr {
  constructor(e, t, r, o, i) {
    super(e, [], r, o), this.mark = t, this.spec = i;
  }
  static create(e, t, r, o) {
    let i = o.nodeViews[t.type.name], s = i && i(t, o, r);
    return (!s || !s.dom) && (s = Tn.renderSpec(document, t.type.spec.toDOM(t, r), null, t.attrs)), new Cn(e, t, s.dom, s.contentDOM || s.dom, s);
  }
  parseRule() {
    return this.dirty & ft || this.mark.type.spec.reparseInView ? null : { mark: this.mark.type.name, attrs: this.mark.attrs, contentElement: this.contentDOM };
  }
  matchesMark(e) {
    return this.dirty != ft && this.mark.eq(e);
  }
  markDirty(e, t) {
    if (super.markDirty(e, t), this.dirty != Ge) {
      let r = this.parent;
      for (; !r.node; )
        r = r.parent;
      r.dirty < this.dirty && (r.dirty = this.dirty), this.dirty = Ge;
    }
  }
  slice(e, t, r) {
    let o = Cn.create(this.parent, this.mark, !0, r), i = this.children, s = this.size;
    t < s && (i = hl(i, t, s, r)), e > 0 && (i = hl(i, 0, e, r));
    for (let l = 0; l < i.length; l++)
      i[l].parent = o;
    return o.children = i, o;
  }
  ignoreMutation(e) {
    return this.spec.ignoreMutation ? this.spec.ignoreMutation(e) : super.ignoreMutation(e);
  }
  destroy() {
    this.spec.destroy && this.spec.destroy(), super.destroy();
  }
}
class qt extends Jr {
  constructor(e, t, r, o, i, s, l, a, c) {
    super(e, [], i, s), this.node = t, this.outerDeco = r, this.innerDeco = o, this.nodeDOM = l;
  }
  // By default, a node is rendered using the `toDOM` method from the
  // node type spec. But client code can use the `nodeViews` spec to
  // supply a custom node view, which can influence various aspects of
  // the way the node works.
  //
  // (Using subclassing for this was intentionally decided against,
  // since it'd require exposing a whole slew of finicky
  // implementation details to the user code that they probably will
  // never need.)
  static create(e, t, r, o, i, s) {
    let l = i.nodeViews[t.type.name], a, c = l && l(t, i, () => {
      if (!a)
        return s;
      if (a.parent)
        return a.parent.posBeforeChild(a);
    }, r, o), u = c && c.dom, d = c && c.contentDOM;
    if (t.isText) {
      if (!u)
        u = document.createTextNode(t.text);
      else if (u.nodeType != 3)
        throw new RangeError("Text must be rendered as a DOM text node");
    } else u || ({ dom: u, contentDOM: d } = Tn.renderSpec(document, t.type.spec.toDOM(t), null, t.attrs));
    !d && !t.isText && u.nodeName != "BR" && (u.hasAttribute("contenteditable") || (u.contentEditable = "false"), t.type.spec.draggable && (u.draggable = !0));
    let f = u;
    return u = cf(u, r, t), c ? a = new Zg(e, t, r, o, u, d || null, f, c, i, s + 1) : t.isText ? new zi(e, t, r, o, u, f, i) : new qt(e, t, r, o, u, d || null, f, i, s + 1);
  }
  parseRule() {
    if (this.node.type.spec.reparseInView)
      return null;
    let e = { node: this.node.type.name, attrs: this.node.attrs };
    if (this.node.type.whitespace == "pre" && (e.preserveWhitespace = "full"), !this.contentDOM)
      e.getContent = () => this.node.content;
    else if (!this.contentLost)
      e.contentElement = this.contentDOM;
    else {
      for (let t = this.children.length - 1; t >= 0; t--) {
        let r = this.children[t];
        if (this.dom.contains(r.dom.parentNode)) {
          e.contentElement = r.dom.parentNode;
          break;
        }
      }
      e.contentElement || (e.getContent = () => M.empty);
    }
    return e;
  }
  matchesNode(e, t, r) {
    return this.dirty == Ge && e.eq(this.node) && jo(t, this.outerDeco) && r.eq(this.innerDeco);
  }
  get size() {
    return this.node.nodeSize;
  }
  get border() {
    return this.node.isLeaf ? 0 : 1;
  }
  // Syncs `this.children` to match `this.node.content` and the local
  // decorations, possibly introducing nesting for marks. Then, in a
  // separate step, syncs the DOM inside `this.contentDOM` to
  // `this.children`.
  updateChildren(e, t) {
    let r = this.node.inlineContent, o = t, i = e.composing ? this.localCompositionInfo(e, t) : null, s = i && i.pos > -1 ? i : null, l = i && i.pos < 0, a = new ty(this, s && s.node, e);
    oy(this.node, this.innerDeco, (c, u, d) => {
      c.spec.marks ? a.syncToMarks(c.spec.marks, r, e, u) : c.type.side >= 0 && !d && a.syncToMarks(u == this.node.childCount ? Z.none : this.node.child(u).marks, r, e, u), a.placeWidget(c, e, o);
    }, (c, u, d, f) => {
      a.syncToMarks(c.marks, r, e, f);
      let h;
      a.findNodeMatch(c, u, d, f) || l && e.state.selection.from > o && e.state.selection.to < o + c.nodeSize && (h = a.findIndexWithChild(i.node)) > -1 && a.updateNodeAt(c, u, d, h, e) || a.updateNextNode(c, u, d, e, f, o) || a.addNode(c, u, d, e, o), o += c.nodeSize;
    }), a.syncToMarks([], r, e, 0), this.node.isTextblock && a.addTextblockHacks(), a.destroyRest(), (a.changed || this.dirty == cn) && (s && this.protectLocalComposition(e, s), lf(this.contentDOM, this.children, e), Gn && iy(this.dom));
  }
  localCompositionInfo(e, t) {
    let { from: r, to: o } = e.state.selection;
    if (!(e.state.selection instanceof L) || r < t || o > t + this.node.content.size)
      return null;
    let i = e.input.compositionNode;
    if (!i || !this.dom.contains(i.parentNode))
      return null;
    if (this.node.inlineContent) {
      let s = i.nodeValue, l = sy(this.node.content, s, r - t, o - t);
      return l < 0 ? null : { node: i, pos: l, text: s };
    } else
      return { node: i, pos: -1, text: "" };
  }
  protectLocalComposition(e, { node: t, pos: r, text: o }) {
    if (this.getDesc(t))
      return;
    let i = t;
    for (; i.parentNode != this.contentDOM; i = i.parentNode) {
      for (; i.previousSibling; )
        i.parentNode.removeChild(i.previousSibling);
      for (; i.nextSibling; )
        i.parentNode.removeChild(i.nextSibling);
      i.pmViewDesc && (i.pmViewDesc = void 0);
    }
    let s = new Qg(this, i, t, o);
    e.input.compositionNodes.push(s), this.children = hl(this.children, r, r + o.length, e, s);
  }
  // If this desc must be updated to match the given node decoration,
  // do so and return true.
  update(e, t, r, o) {
    return this.dirty == ft || !e.sameMarkup(this.node) ? !1 : (this.updateInner(e, t, r, o), !0);
  }
  updateInner(e, t, r, o) {
    this.updateOuterDeco(t), this.node = e, this.innerDeco = r, this.contentDOM && this.updateChildren(o, this.posAtStart), this.dirty = Ge;
  }
  updateOuterDeco(e) {
    if (jo(e, this.outerDeco))
      return;
    let t = this.nodeDOM.nodeType != 1, r = this.dom;
    this.dom = af(this.dom, this.nodeDOM, fl(this.outerDeco, this.node, t), fl(e, this.node, t)), this.dom != r && (r.pmViewDesc = void 0, this.dom.pmViewDesc = this), this.outerDeco = e;
  }
  // Mark this node as being the selected node.
  selectNode() {
    this.nodeDOM.nodeType == 1 && (this.nodeDOM.classList.add("ProseMirror-selectednode"), (this.contentDOM || !this.node.type.spec.draggable) && (this.nodeDOM.draggable = !0));
  }
  // Remove selected node marking from this node.
  deselectNode() {
    this.nodeDOM.nodeType == 1 && (this.nodeDOM.classList.remove("ProseMirror-selectednode"), (this.contentDOM || !this.node.type.spec.draggable) && this.nodeDOM.removeAttribute("draggable"));
  }
  get domAtom() {
    return this.node.isAtom;
  }
}
function Ic(n, e, t, r, o) {
  cf(r, e, n);
  let i = new qt(void 0, n, e, t, r, r, r, o, 0);
  return i.contentDOM && i.updateChildren(o, 0), i;
}
class zi extends qt {
  constructor(e, t, r, o, i, s, l) {
    super(e, t, r, o, i, null, s, l, 0);
  }
  parseRule() {
    let e = this.nodeDOM.parentNode;
    for (; e && e != this.dom && !e.pmIsDeco; )
      e = e.parentNode;
    return { skip: e || !0 };
  }
  update(e, t, r, o) {
    return this.dirty == ft || this.dirty != Ge && !this.inParent() || !e.sameMarkup(this.node) ? !1 : (this.updateOuterDeco(t), (this.dirty != Ge || e.text != this.node.text) && e.text != this.nodeDOM.nodeValue && (this.nodeDOM.nodeValue = e.text, o.trackWrites == this.nodeDOM && (o.trackWrites = null)), this.node = e, this.dirty = Ge, !0);
  }
  inParent() {
    let e = this.parent.contentDOM;
    for (let t = this.nodeDOM; t; t = t.parentNode)
      if (t == e)
        return !0;
    return !1;
  }
  domFromPos(e) {
    return { node: this.nodeDOM, offset: e };
  }
  localPosFromDOM(e, t, r) {
    return e == this.nodeDOM ? this.posAtStart + Math.min(t, this.node.text.length) : super.localPosFromDOM(e, t, r);
  }
  ignoreMutation(e) {
    return e.type != "characterData" && e.type != "selection";
  }
  slice(e, t, r) {
    let o = this.node.cut(e, t), i = document.createTextNode(o.text);
    return new zi(this.parent, o, this.outerDeco, this.innerDeco, i, i, r);
  }
  markDirty(e, t) {
    super.markDirty(e, t), this.dom != this.nodeDOM && (e == 0 || t == this.nodeDOM.nodeValue.length) && (this.dirty = ft);
  }
  get domAtom() {
    return !1;
  }
  isText(e) {
    return this.node.text == e;
  }
}
class sf extends Jr {
  parseRule() {
    return { ignore: !0 };
  }
  matchesHack(e) {
    return this.dirty == Ge && this.dom.nodeName == e;
  }
  get domAtom() {
    return !0;
  }
  get ignoreForCoords() {
    return this.dom.nodeName == "IMG";
  }
}
class Zg extends qt {
  constructor(e, t, r, o, i, s, l, a, c, u) {
    super(e, t, r, o, i, s, l, c, u), this.spec = a;
  }
  // A custom `update` method gets to decide whether the update goes
  // through. If it does, and there's a `contentDOM` node, our logic
  // updates the children.
  update(e, t, r, o) {
    if (this.dirty == ft)
      return !1;
    if (this.spec.update && (this.node.type == e.type || this.spec.multiType)) {
      let i = this.spec.update(e, t, r);
      return i && this.updateInner(e, t, r, o), i;
    } else return !this.contentDOM && !e.isLeaf ? !1 : super.update(e, t, r, o);
  }
  selectNode() {
    this.spec.selectNode ? this.spec.selectNode() : super.selectNode();
  }
  deselectNode() {
    this.spec.deselectNode ? this.spec.deselectNode() : super.deselectNode();
  }
  setSelection(e, t, r, o) {
    this.spec.setSelection ? this.spec.setSelection(e, t, r.root) : super.setSelection(e, t, r, o);
  }
  destroy() {
    this.spec.destroy && this.spec.destroy(), super.destroy();
  }
  stopEvent(e) {
    return this.spec.stopEvent ? this.spec.stopEvent(e) : !1;
  }
  ignoreMutation(e) {
    return this.spec.ignoreMutation ? this.spec.ignoreMutation(e) : super.ignoreMutation(e);
  }
}
function lf(n, e, t) {
  let r = n.firstChild, o = !1;
  for (let i = 0; i < e.length; i++) {
    let s = e[i], l = s.dom;
    if (l.parentNode == n) {
      for (; l != r; )
        r = Lc(r), o = !0;
      r = r.nextSibling;
    } else
      o = !0, n.insertBefore(l, r);
    if (s instanceof Cn) {
      let a = r ? r.previousSibling : n.lastChild;
      lf(s.contentDOM, s.children, t), r = a ? a.nextSibling : n.firstChild;
    }
  }
  for (; r; )
    r = Lc(r), o = !0;
  o && t.trackWrites == n && (t.trackWrites = null);
}
const vr = function(n) {
  n && (this.nodeName = n);
};
vr.prototype = /* @__PURE__ */ Object.create(null);
const un = [new vr()];
function fl(n, e, t) {
  if (n.length == 0)
    return un;
  let r = t ? un[0] : new vr(), o = [r];
  for (let i = 0; i < n.length; i++) {
    let s = n[i].type.attrs;
    if (s) {
      s.nodeName && o.push(r = new vr(s.nodeName));
      for (let l in s) {
        let a = s[l];
        a != null && (t && o.length == 1 && o.push(r = new vr(e.isInline ? "span" : "div")), l == "class" ? r.class = (r.class ? r.class + " " : "") + a : l == "style" ? r.style = (r.style ? r.style + ";" : "") + a : l != "nodeName" && (r[l] = a));
      }
    }
  }
  return o;
}
function af(n, e, t, r) {
  if (t == un && r == un)
    return e;
  let o = e;
  for (let i = 0; i < r.length; i++) {
    let s = r[i], l = t[i];
    if (i) {
      let a;
      l && l.nodeName == s.nodeName && o != n && (a = o.parentNode) && a.nodeName.toLowerCase() == s.nodeName || (a = document.createElement(s.nodeName), a.pmIsDeco = !0, a.appendChild(o), l = un[0]), o = a;
    }
    ey(o, l || un[0], s);
  }
  return o;
}
function ey(n, e, t) {
  for (let r in e)
    r != "class" && r != "style" && r != "nodeName" && !(r in t) && n.removeAttribute(r);
  for (let r in t)
    r != "class" && r != "style" && r != "nodeName" && t[r] != e[r] && n.setAttribute(r, t[r]);
  if (e.class != t.class) {
    let r = e.class ? e.class.split(" ").filter(Boolean) : [], o = t.class ? t.class.split(" ").filter(Boolean) : [];
    for (let i = 0; i < r.length; i++)
      o.indexOf(r[i]) == -1 && n.classList.remove(r[i]);
    for (let i = 0; i < o.length; i++)
      r.indexOf(o[i]) == -1 && n.classList.add(o[i]);
    n.classList.length == 0 && n.removeAttribute("class");
  }
  if (e.style != t.style) {
    if (e.style) {
      let r = /\s*([\w\-\xa1-\uffff]+)\s*:(?:"(?:\\.|[^"])*"|'(?:\\.|[^'])*'|\(.*?\)|[^;])*/g, o;
      for (; o = r.exec(e.style); )
        n.style.removeProperty(o[1]);
    }
    t.style && (n.style.cssText += t.style);
  }
}
function cf(n, e, t) {
  return af(n, n, un, fl(e, t, n.nodeType != 1));
}
function jo(n, e) {
  if (n.length != e.length)
    return !1;
  for (let t = 0; t < n.length; t++)
    if (!n[t].type.eq(e[t].type))
      return !1;
  return !0;
}
function Lc(n) {
  let e = n.nextSibling;
  return n.parentNode.removeChild(n), e;
}
class ty {
  constructor(e, t, r) {
    this.lock = t, this.view = r, this.index = 0, this.stack = [], this.changed = !1, this.top = e, this.preMatch = ny(e.node.content, e);
  }
  // Destroy and remove the children between the given indices in
  // `this.top`.
  destroyBetween(e, t) {
    if (e != t) {
      for (let r = e; r < t; r++)
        this.top.children[r].destroy();
      this.top.children.splice(e, t - e), this.changed = !0;
    }
  }
  // Destroy all remaining children in `this.top`.
  destroyRest() {
    this.destroyBetween(this.index, this.top.children.length);
  }
  // Sync the current stack of mark descs with the given array of
  // marks, reusing existing mark descs when possible.
  syncToMarks(e, t, r, o) {
    let i = 0, s = this.stack.length >> 1, l = Math.min(s, e.length);
    for (; i < l && (i == s - 1 ? this.top : this.stack[i + 1 << 1]).matchesMark(e[i]) && e[i].type.spec.spanning !== !1; )
      i++;
    for (; i < s; )
      this.destroyRest(), this.top.dirty = Ge, this.index = this.stack.pop(), this.top = this.stack.pop(), s--;
    for (; s < e.length; ) {
      this.stack.push(this.top, this.index + 1);
      let a = -1, c = this.top.children.length;
      o < this.preMatch.index && (c = Math.min(this.index + 3, c));
      for (let u = this.index; u < c; u++) {
        let d = this.top.children[u];
        if (d.matchesMark(e[s]) && !this.isLocked(d.dom)) {
          a = u;
          break;
        }
      }
      if (a > -1)
        a > this.index && (this.changed = !0, this.destroyBetween(this.index, a)), this.top = this.top.children[this.index];
      else {
        let u = Cn.create(this.top, e[s], t, r);
        this.top.children.splice(this.index, 0, u), this.top = u, this.changed = !0;
      }
      this.index = 0, s++;
    }
  }
  // Try to find a node desc matching the given data. Skip over it and
  // return true when successful.
  findNodeMatch(e, t, r, o) {
    let i = -1, s;
    if (o >= this.preMatch.index && (s = this.preMatch.matches[o - this.preMatch.index]).parent == this.top && s.matchesNode(e, t, r))
      i = this.top.children.indexOf(s, this.index);
    else
      for (let l = this.index, a = Math.min(this.top.children.length, l + 5); l < a; l++) {
        let c = this.top.children[l];
        if (c.matchesNode(e, t, r) && !this.preMatch.matched.has(c)) {
          i = l;
          break;
        }
      }
    return i < 0 ? !1 : (this.destroyBetween(this.index, i), this.index++, !0);
  }
  updateNodeAt(e, t, r, o, i) {
    let s = this.top.children[o];
    return s.dirty == ft && s.dom == s.contentDOM && (s.dirty = cn), s.update(e, t, r, i) ? (this.destroyBetween(this.index, o), this.index++, !0) : !1;
  }
  findIndexWithChild(e) {
    for (; ; ) {
      let t = e.parentNode;
      if (!t)
        return -1;
      if (t == this.top.contentDOM) {
        let r = e.pmViewDesc;
        if (r) {
          for (let o = this.index; o < this.top.children.length; o++)
            if (this.top.children[o] == r)
              return o;
        }
        return -1;
      }
      e = t;
    }
  }
  // Try to update the next node, if any, to the given data. Checks
  // pre-matches to avoid overwriting nodes that could still be used.
  updateNextNode(e, t, r, o, i, s) {
    for (let l = this.index; l < this.top.children.length; l++) {
      let a = this.top.children[l];
      if (a instanceof qt) {
        let c = this.preMatch.matched.get(a);
        if (c != null && c != i)
          return !1;
        let u = a.dom, d, f = this.isLocked(u) && !(e.isText && a.node && a.node.isText && a.nodeDOM.nodeValue == e.text && a.dirty != ft && jo(t, a.outerDeco));
        if (!f && a.update(e, t, r, o))
          return this.destroyBetween(this.index, l), a.dom != u && (this.changed = !0), this.index++, !0;
        if (!f && (d = this.recreateWrapper(a, e, t, r, o, s)))
          return this.destroyBetween(this.index, l), this.top.children[this.index] = d, d.contentDOM && (d.dirty = cn, d.updateChildren(o, s + 1), d.dirty = Ge), this.changed = !0, this.index++, !0;
        break;
      }
    }
    return !1;
  }
  // When a node with content is replaced by a different node with
  // identical content, move over its children.
  recreateWrapper(e, t, r, o, i, s) {
    if (e.dirty || t.isAtom || !e.children.length || !e.node.content.eq(t.content) || !jo(r, e.outerDeco) || !o.eq(e.innerDeco))
      return null;
    let l = qt.create(this.top, t, r, o, i, s);
    if (l.contentDOM) {
      l.children = e.children, e.children = [];
      for (let a of l.children)
        a.parent = l;
    }
    return e.destroy(), l;
  }
  // Insert the node as a newly created node desc.
  addNode(e, t, r, o, i) {
    let s = qt.create(this.top, e, t, r, o, i);
    s.contentDOM && s.updateChildren(o, i + 1), this.top.children.splice(this.index++, 0, s), this.changed = !0;
  }
  placeWidget(e, t, r) {
    let o = this.index < this.top.children.length ? this.top.children[this.index] : null;
    if (o && o.matchesWidget(e) && (e == o.widget || !o.widget.type.toDOM.parentNode))
      this.index++;
    else {
      let i = new of(this.top, e, t, r);
      this.top.children.splice(this.index++, 0, i), this.changed = !0;
    }
  }
  // Make sure a textblock looks and behaves correctly in
  // contentEditable.
  addTextblockHacks() {
    let e = this.top.children[this.index - 1], t = this.top;
    for (; e instanceof Cn; )
      t = e, e = t.children[t.children.length - 1];
    (!e || // Empty textblock
    !(e instanceof zi) || /\n$/.test(e.node.text) || this.view.requiresGeckoHackNode && /\s$/.test(e.node.text)) && ((Se || ve) && e && e.dom.contentEditable == "false" && this.addHackNode("IMG", t), this.addHackNode("BR", this.top));
  }
  addHackNode(e, t) {
    if (t == this.top && this.index < t.children.length && t.children[this.index].matchesHack(e))
      this.index++;
    else {
      let r = document.createElement(e);
      e == "IMG" && (r.className = "ProseMirror-separator", r.alt = ""), e == "BR" && (r.className = "ProseMirror-trailingBreak");
      let o = new sf(this.top, [], r, null);
      t != this.top ? t.children.push(o) : t.children.splice(this.index++, 0, o), this.changed = !0;
    }
  }
  isLocked(e) {
    return this.lock && (e == this.lock || e.nodeType == 1 && e.contains(this.lock.parentNode));
  }
}
function ny(n, e) {
  let t = e, r = t.children.length, o = n.childCount, i = /* @__PURE__ */ new Map(), s = [];
  e: for (; o > 0; ) {
    let l;
    for (; ; )
      if (r) {
        let c = t.children[r - 1];
        if (c instanceof Cn)
          t = c, r = c.children.length;
        else {
          l = c, r--;
          break;
        }
      } else {
        if (t == e)
          break e;
        r = t.parent.children.indexOf(t), t = t.parent;
      }
    let a = l.node;
    if (a) {
      if (a != n.child(o - 1))
        break;
      --o, i.set(l, o), s.push(l);
    }
  }
  return { index: o, matched: i, matches: s.reverse() };
}
function ry(n, e) {
  return n.type.side - e.type.side;
}
function oy(n, e, t, r) {
  let o = e.locals(n), i = 0;
  if (o.length == 0) {
    for (let c = 0; c < n.childCount; c++) {
      let u = n.child(c);
      r(u, o, e.forChild(i, u), c), i += u.nodeSize;
    }
    return;
  }
  let s = 0, l = [], a = null;
  for (let c = 0; ; ) {
    let u, d;
    for (; s < o.length && o[s].to == i; ) {
      let g = o[s++];
      g.widget && (u ? (d || (d = [u])).push(g) : u = g);
    }
    if (u)
      if (d) {
        d.sort(ry);
        for (let g = 0; g < d.length; g++)
          t(d[g], c, !!a);
      } else
        t(u, c, !!a);
    let f, h;
    if (a)
      h = -1, f = a, a = null;
    else if (c < n.childCount)
      h = c, f = n.child(c++);
    else
      break;
    for (let g = 0; g < l.length; g++)
      l[g].to <= i && l.splice(g--, 1);
    for (; s < o.length && o[s].from <= i && o[s].to > i; )
      l.push(o[s++]);
    let p = i + f.nodeSize;
    if (f.isText) {
      let g = p;
      s < o.length && o[s].from < g && (g = o[s].from);
      for (let y = 0; y < l.length; y++)
        l[y].to < g && (g = l[y].to);
      g < p && (a = f.cut(g - i), f = f.cut(0, g - i), p = g, h = -1);
    } else
      for (; s < o.length && o[s].to < p; )
        s++;
    let m = f.isInline && !f.isLeaf ? l.filter((g) => !g.inline) : l.slice();
    r(f, m, e.forChild(i, f), h), i = p;
  }
}
function iy(n) {
  if (n.nodeName == "UL" || n.nodeName == "OL") {
    let e = n.style.cssText;
    n.style.cssText = e + "; list-style: square !important", window.getComputedStyle(n).listStyle, n.style.cssText = e;
  }
}
function sy(n, e, t, r) {
  for (let o = 0, i = 0; o < n.childCount && i <= r; ) {
    let s = n.child(o++), l = i;
    if (i += s.nodeSize, !s.isText)
      continue;
    let a = s.text;
    for (; o < n.childCount; ) {
      let c = n.child(o++);
      if (i += c.nodeSize, !c.isText)
        break;
      a += c.text;
    }
    if (i >= t) {
      if (i >= r && a.slice(r - e.length - l, r - l) == e)
        return r - e.length;
      let c = l < r ? a.lastIndexOf(e, r - l - 1) : -1;
      if (c >= 0 && c + e.length + l >= t)
        return l + c;
      if (t == r && a.length >= r + e.length - l && a.slice(r - l, r - l + e.length) == e)
        return r;
    }
  }
  return -1;
}
function hl(n, e, t, r, o) {
  let i = [];
  for (let s = 0, l = 0; s < n.length; s++) {
    let a = n[s], c = l, u = l += a.size;
    c >= t || u <= e ? i.push(a) : (c < e && i.push(a.slice(0, e - c, r)), o && (i.push(o), o = void 0), u > t && i.push(a.slice(t - c, a.size, r)));
  }
  return i;
}
function ea(n, e = null) {
  let t = n.domSelectionRange(), r = n.state.doc;
  if (!t.focusNode)
    return null;
  let o = n.docView.nearestDesc(t.focusNode), i = o && o.size == 0, s = n.docView.posFromDOM(t.focusNode, t.focusOffset, 1);
  if (s < 0)
    return null;
  let l = r.resolve(s), a, c;
  if (Fi(t)) {
    for (a = s; o && !o.node; )
      o = o.parent;
    let d = o.node;
    if (o && d.isAtom && I.isSelectable(d) && o.parent && !(d.isInline && Pg(t.focusNode, t.focusOffset, o.dom))) {
      let f = o.posBefore;
      c = new I(s == f ? l : r.resolve(f));
    }
  } else {
    if (t instanceof n.dom.ownerDocument.defaultView.Selection && t.rangeCount > 1) {
      let d = s, f = s;
      for (let h = 0; h < t.rangeCount; h++) {
        let p = t.getRangeAt(h);
        d = Math.min(d, n.docView.posFromDOM(p.startContainer, p.startOffset, 1)), f = Math.max(f, n.docView.posFromDOM(p.endContainer, p.endOffset, -1));
      }
      if (d < 0)
        return null;
      [a, s] = f == n.state.selection.anchor ? [f, d] : [d, f], l = r.resolve(s);
    } else
      a = n.docView.posFromDOM(t.anchorNode, t.anchorOffset, 1);
    if (a < 0)
      return null;
  }
  let u = r.resolve(a);
  if (!c) {
    let d = e == "pointer" || n.state.selection.head < l.pos && !i ? 1 : -1;
    c = ta(n, u, l, d);
  }
  return c;
}
function uf(n) {
  return n.editable ? n.hasFocus() : ff(n) && document.activeElement && document.activeElement.contains(n.dom);
}
function kt(n, e = !1) {
  let t = n.state.selection;
  if (df(n, t), !!uf(n)) {
    if (!e && n.input.mouseDown && n.input.mouseDown.allowDefault && ve) {
      let r = n.domSelectionRange(), o = n.domObserver.currentSelection;
      if (r.anchorNode && o.anchorNode && vn(r.anchorNode, r.anchorOffset, o.anchorNode, o.anchorOffset)) {
        n.input.mouseDown.delayedSelectionSync = !0, n.domObserver.setCurSelection();
        return;
      }
    }
    if (n.domObserver.disconnectSelection(), n.cursorWrapper)
      ay(n);
    else {
      let { anchor: r, head: o } = t, i, s;
      _c && !(t instanceof L) && (t.$from.parent.inlineContent || (i = Bc(n, t.from)), !t.empty && !t.$from.parent.inlineContent && (s = Bc(n, t.to))), n.docView.setSelection(r, o, n, e), _c && (i && Fc(i), s && Fc(s)), t.visible ? n.dom.classList.remove("ProseMirror-hideselection") : (n.dom.classList.add("ProseMirror-hideselection"), "onselectionchange" in document && ly(n));
    }
    n.domObserver.setCurSelection(), n.domObserver.connectSelection();
  }
}
const _c = Se || ve && Yd < 63;
function Bc(n, e) {
  let { node: t, offset: r } = n.docView.domFromPos(e, 0), o = r < t.childNodes.length ? t.childNodes[r] : null, i = r ? t.childNodes[r - 1] : null;
  if (Se && o && o.contentEditable == "false")
    return Es(o);
  if ((!o || o.contentEditable == "false") && (!i || i.contentEditable == "false")) {
    if (o)
      return Es(o);
    if (i)
      return Es(i);
  }
}
function Es(n) {
  return n.contentEditable = "true", Se && n.draggable && (n.draggable = !1, n.wasDraggable = !0), n;
}
function Fc(n) {
  n.contentEditable = "false", n.wasDraggable && (n.draggable = !0, n.wasDraggable = null);
}
function ly(n) {
  let e = n.dom.ownerDocument;
  e.removeEventListener("selectionchange", n.input.hideSelectionGuard);
  let t = n.domSelectionRange(), r = t.anchorNode, o = t.anchorOffset;
  e.addEventListener("selectionchange", n.input.hideSelectionGuard = () => {
    (t.anchorNode != r || t.anchorOffset != o) && (e.removeEventListener("selectionchange", n.input.hideSelectionGuard), setTimeout(() => {
      (!uf(n) || n.state.selection.visible) && n.dom.classList.remove("ProseMirror-hideselection");
    }, 20));
  });
}
function ay(n) {
  let e = n.domSelection();
  if (!e)
    return;
  let t = n.cursorWrapper.dom, r = t.nodeName == "IMG";
  r ? e.collapse(t.parentNode, be(t) + 1) : e.collapse(t, 0), !r && !n.state.selection.visible && Le && Kt <= 11 && (t.disabled = !0, t.disabled = !1);
}
function df(n, e) {
  if (e instanceof I) {
    let t = n.docView.descAt(e.from);
    t != n.lastSelectedViewDesc && (zc(n), t && t.selectNode(), n.lastSelectedViewDesc = t);
  } else
    zc(n);
}
function zc(n) {
  n.lastSelectedViewDesc && (n.lastSelectedViewDesc.parent && n.lastSelectedViewDesc.deselectNode(), n.lastSelectedViewDesc = void 0);
}
function ta(n, e, t, r) {
  return n.someProp("createSelectionBetween", (o) => o(n, e, t)) || L.between(e, t, r);
}
function $c(n) {
  return n.editable && !n.hasFocus() ? !1 : ff(n);
}
function ff(n) {
  let e = n.domSelectionRange();
  if (!e.anchorNode)
    return !1;
  try {
    return n.dom.contains(e.anchorNode.nodeType == 3 ? e.anchorNode.parentNode : e.anchorNode) && (n.editable || n.dom.contains(e.focusNode.nodeType == 3 ? e.focusNode.parentNode : e.focusNode));
  } catch {
    return !1;
  }
}
function cy(n) {
  let e = n.docView.domFromPos(n.state.selection.anchor, 0), t = n.domSelectionRange();
  return vn(e.node, e.offset, t.anchorNode, t.anchorOffset);
}
function pl(n, e) {
  let { $anchor: t, $head: r } = n.selection, o = e > 0 ? t.max(r) : t.min(r), i = o.parent.inlineContent ? o.depth ? n.doc.resolve(e > 0 ? o.after() : o.before()) : null : o;
  return i && _.findFrom(i, e);
}
function Lt(n, e) {
  return n.dispatch(n.state.tr.setSelection(e).scrollIntoView()), !0;
}
function Hc(n, e, t) {
  let r = n.state.selection;
  if (r instanceof L)
    if (t.indexOf("s") > -1) {
      let { $head: o } = r, i = o.textOffset ? null : e < 0 ? o.nodeBefore : o.nodeAfter;
      if (!i || i.isText || !i.isLeaf)
        return !1;
      let s = n.state.doc.resolve(o.pos + i.nodeSize * (e < 0 ? -1 : 1));
      return Lt(n, new L(r.$anchor, s));
    } else if (r.empty) {
      if (n.endOfTextblock(e > 0 ? "forward" : "backward")) {
        let o = pl(n.state, e);
        return o && o instanceof I ? Lt(n, o) : !1;
      } else if (!(Ue && t.indexOf("m") > -1)) {
        let o = r.$head, i = o.textOffset ? null : e < 0 ? o.nodeBefore : o.nodeAfter, s;
        if (!i || i.isText)
          return !1;
        let l = e < 0 ? o.pos - i.nodeSize : o.pos;
        return i.isAtom || (s = n.docView.descAt(l)) && !s.contentDOM ? I.isSelectable(i) ? Lt(n, new I(e < 0 ? n.state.doc.resolve(o.pos - i.nodeSize) : o)) : Gr ? Lt(n, new L(n.state.doc.resolve(e < 0 ? l : l + i.nodeSize))) : !1 : !1;
      }
    } else return !1;
  else {
    if (r instanceof I && r.node.isInline)
      return Lt(n, new L(e > 0 ? r.$to : r.$from));
    {
      let o = pl(n.state, e);
      return o ? Lt(n, o) : !1;
    }
  }
}
function Uo(n) {
  return n.nodeType == 3 ? n.nodeValue.length : n.childNodes.length;
}
function Cr(n, e) {
  let t = n.pmViewDesc;
  return t && t.size == 0 && (e < 0 || n.nextSibling || n.nodeName != "BR");
}
function Pn(n, e) {
  return e < 0 ? uy(n) : dy(n);
}
function uy(n) {
  let e = n.domSelectionRange(), t = e.focusNode, r = e.focusOffset;
  if (!t)
    return;
  let o, i, s = !1;
  for (qe && t.nodeType == 1 && r < Uo(t) && Cr(t.childNodes[r], -1) && (s = !0); ; )
    if (r > 0) {
      if (t.nodeType != 1)
        break;
      {
        let l = t.childNodes[r - 1];
        if (Cr(l, -1))
          o = t, i = --r;
        else if (l.nodeType == 3)
          t = l, r = t.nodeValue.length;
        else
          break;
      }
    } else {
      if (hf(t))
        break;
      {
        let l = t.previousSibling;
        for (; l && Cr(l, -1); )
          o = t.parentNode, i = be(l), l = l.previousSibling;
        if (l)
          t = l, r = Uo(t);
        else {
          if (t = t.parentNode, t == n.dom)
            break;
          r = 0;
        }
      }
    }
  s ? ml(n, t, r) : o && ml(n, o, i);
}
function dy(n) {
  let e = n.domSelectionRange(), t = e.focusNode, r = e.focusOffset;
  if (!t)
    return;
  let o = Uo(t), i, s;
  for (; ; )
    if (r < o) {
      if (t.nodeType != 1)
        break;
      let l = t.childNodes[r];
      if (Cr(l, 1))
        i = t, s = ++r;
      else
        break;
    } else {
      if (hf(t))
        break;
      {
        let l = t.nextSibling;
        for (; l && Cr(l, 1); )
          i = l.parentNode, s = be(l) + 1, l = l.nextSibling;
        if (l)
          t = l, r = 0, o = Uo(t);
        else {
          if (t = t.parentNode, t == n.dom)
            break;
          r = o = 0;
        }
      }
    }
  i && ml(n, i, s);
}
function hf(n) {
  let e = n.pmViewDesc;
  return e && e.node && e.node.isBlock;
}
function fy(n, e) {
  for (; n && e == n.childNodes.length && !qr(n); )
    e = be(n) + 1, n = n.parentNode;
  for (; n && e < n.childNodes.length; ) {
    let t = n.childNodes[e];
    if (t.nodeType == 3)
      return t;
    if (t.nodeType == 1 && t.contentEditable == "false")
      break;
    n = t, e = 0;
  }
}
function hy(n, e) {
  for (; n && !e && !qr(n); )
    e = be(n), n = n.parentNode;
  for (; n && e; ) {
    let t = n.childNodes[e - 1];
    if (t.nodeType == 3)
      return t;
    if (t.nodeType == 1 && t.contentEditable == "false")
      break;
    n = t, e = n.childNodes.length;
  }
}
function ml(n, e, t) {
  if (e.nodeType != 3) {
    let i, s;
    (s = fy(e, t)) ? (e = s, t = 0) : (i = hy(e, t)) && (e = i, t = i.nodeValue.length);
  }
  let r = n.domSelection();
  if (!r)
    return;
  if (Fi(r)) {
    let i = document.createRange();
    i.setEnd(e, t), i.setStart(e, t), r.removeAllRanges(), r.addRange(i);
  } else r.extend && r.extend(e, t);
  n.domObserver.setCurSelection();
  let { state: o } = n;
  setTimeout(() => {
    n.state == o && kt(n);
  }, 50);
}
function Wc(n, e) {
  let t = n.state.doc.resolve(e);
  if (!(ve || Xd) && t.parent.inlineContent) {
    let o = n.coordsAtPos(e);
    if (e > t.start()) {
      let i = n.coordsAtPos(e - 1), s = (i.top + i.bottom) / 2;
      if (s > o.top && s < o.bottom && Math.abs(i.left - o.left) > 1)
        return i.left < o.left ? "ltr" : "rtl";
    }
    if (e < t.end()) {
      let i = n.coordsAtPos(e + 1), s = (i.top + i.bottom) / 2;
      if (s > o.top && s < o.bottom && Math.abs(i.left - o.left) > 1)
        return i.left > o.left ? "ltr" : "rtl";
    }
  }
  return getComputedStyle(n.dom).direction == "rtl" ? "rtl" : "ltr";
}
function Vc(n, e, t) {
  let r = n.state.selection;
  if (r instanceof L && !r.empty || t.indexOf("s") > -1 || Ue && t.indexOf("m") > -1)
    return !1;
  let { $from: o, $to: i } = r;
  if (!o.parent.inlineContent || n.endOfTextblock(e < 0 ? "up" : "down")) {
    let s = pl(n.state, e);
    if (s && s instanceof I)
      return Lt(n, s);
  }
  if (!o.parent.inlineContent) {
    let s = e < 0 ? o : i, l = r instanceof ze ? _.near(s, e) : _.findFrom(s, e);
    return l ? Lt(n, l) : !1;
  }
  return !1;
}
function jc(n, e) {
  if (!(n.state.selection instanceof L))
    return !0;
  let { $head: t, $anchor: r, empty: o } = n.state.selection;
  if (!t.sameParent(r))
    return !0;
  if (!o)
    return !1;
  if (n.endOfTextblock(e > 0 ? "forward" : "backward"))
    return !0;
  let i = !t.textOffset && (e < 0 ? t.nodeBefore : t.nodeAfter);
  if (i && !i.isText) {
    let s = n.state.tr;
    return e < 0 ? s.delete(t.pos - i.nodeSize, t.pos) : s.delete(t.pos, t.pos + i.nodeSize), n.dispatch(s), !0;
  }
  return !1;
}
function Uc(n, e, t) {
  n.domObserver.stop(), e.contentEditable = t, n.domObserver.start();
}
function py(n) {
  if (!Se || n.state.selection.$head.parentOffset > 0)
    return !1;
  let { focusNode: e, focusOffset: t } = n.domSelectionRange();
  if (e && e.nodeType == 1 && t == 0 && e.firstChild && e.firstChild.contentEditable == "false") {
    let r = e.firstChild;
    Uc(n, r, "true"), setTimeout(() => Uc(n, r, "false"), 20);
  }
  return !1;
}
function my(n) {
  let e = "";
  return n.ctrlKey && (e += "c"), n.metaKey && (e += "m"), n.altKey && (e += "a"), n.shiftKey && (e += "s"), e;
}
function gy(n, e) {
  let t = e.keyCode, r = my(e);
  if (t == 8 || Ue && t == 72 && r == "c")
    return jc(n, -1) || Pn(n, -1);
  if (t == 46 && !e.shiftKey || Ue && t == 68 && r == "c")
    return jc(n, 1) || Pn(n, 1);
  if (t == 13 || t == 27)
    return !0;
  if (t == 37 || Ue && t == 66 && r == "c") {
    let o = t == 37 ? Wc(n, n.state.selection.from) == "ltr" ? -1 : 1 : -1;
    return Hc(n, o, r) || Pn(n, o);
  } else if (t == 39 || Ue && t == 70 && r == "c") {
    let o = t == 39 ? Wc(n, n.state.selection.from) == "ltr" ? 1 : -1 : 1;
    return Hc(n, o, r) || Pn(n, o);
  } else {
    if (t == 38 || Ue && t == 80 && r == "c")
      return Vc(n, -1, r) || Pn(n, -1);
    if (t == 40 || Ue && t == 78 && r == "c")
      return py(n) || Vc(n, 1, r) || Pn(n, 1);
    if (r == (Ue ? "m" : "c") && (t == 66 || t == 73 || t == 89 || t == 90))
      return !0;
  }
  return !1;
}
function na(n, e) {
  n.someProp("transformCopied", (h) => {
    e = h(e, n);
  });
  let t = [], { content: r, openStart: o, openEnd: i } = e;
  for (; o > 1 && i > 1 && r.childCount == 1 && r.firstChild.childCount == 1; ) {
    o--, i--;
    let h = r.firstChild;
    t.push(h.type.name, h.attrs != h.type.defaultAttrs ? h.attrs : null), r = h.content;
  }
  let s = n.someProp("clipboardSerializer") || Tn.fromSchema(n.state.schema), l = wf(), a = l.createElement("div");
  a.appendChild(s.serializeFragment(r, { document: l }));
  let c = a.firstChild, u, d = 0;
  for (; c && c.nodeType == 1 && (u = bf[c.nodeName.toLowerCase()]); ) {
    for (let h = u.length - 1; h >= 0; h--) {
      let p = l.createElement(u[h]);
      for (; a.firstChild; )
        p.appendChild(a.firstChild);
      a.appendChild(p), d++;
    }
    c = a.firstChild;
  }
  c && c.nodeType == 1 && c.setAttribute("data-pm-slice", `${o} ${i}${d ? ` -${d}` : ""} ${JSON.stringify(t)}`);
  let f = n.someProp("clipboardTextSerializer", (h) => h(e, n)) || e.content.textBetween(0, e.content.size, `

`);
  return { dom: a, text: f, slice: e };
}
function pf(n, e, t, r, o) {
  let i = o.parent.type.spec.code, s, l;
  if (!t && !e)
    return null;
  let a = !!e && (r || i || !t);
  if (a) {
    if (n.someProp("transformPastedText", (f) => {
      e = f(e, i || r, n);
    }), i)
      return l = new O(M.from(n.state.schema.text(e.replace(/\r\n?/g, `
`))), 0, 0), n.someProp("transformPasted", (f) => {
        l = f(l, n, !0);
      }), l;
    let d = n.someProp("clipboardTextParser", (f) => f(e, o, r, n));
    if (d)
      l = d;
    else {
      let f = o.marks(), { schema: h } = n.state, p = Tn.fromSchema(h);
      s = document.createElement("div"), e.split(/(?:\r\n?|\n)+/).forEach((m) => {
        let g = s.appendChild(document.createElement("p"));
        m && g.appendChild(p.serializeNode(h.text(m, f)));
      });
    }
  } else
    n.someProp("transformPastedHTML", (d) => {
      t = d(t, n);
    }), s = vy(t), Gr && Cy(s);
  let c = s && s.querySelector("[data-pm-slice]"), u = c && /^(\d+) (\d+)(?: -(\d+))? (.*)/.exec(c.getAttribute("data-pm-slice") || "");
  if (u && u[3])
    for (let d = +u[3]; d > 0; d--) {
      let f = s.firstChild;
      for (; f && f.nodeType != 1; )
        f = f.nextSibling;
      if (!f)
        break;
      s = f;
    }
  if (l || (l = (n.someProp("clipboardParser") || n.someProp("domParser") || Ut.fromSchema(n.state.schema)).parseSlice(s, {
    preserveWhitespace: !!(a || u),
    context: o,
    ruleFromNode(f) {
      return f.nodeName == "BR" && !f.nextSibling && f.parentNode && !yy.test(f.parentNode.nodeName) ? { ignore: !0 } : null;
    }
  })), u)
    l = Sy(Kc(l, +u[1], +u[2]), u[4]);
  else if (l = O.maxOpen(by(l.content, o), !0), l.openStart || l.openEnd) {
    let d = 0, f = 0;
    for (let h = l.content.firstChild; d < l.openStart && !h.type.spec.isolating; d++, h = h.firstChild)
      ;
    for (let h = l.content.lastChild; f < l.openEnd && !h.type.spec.isolating; f++, h = h.lastChild)
      ;
    l = Kc(l, d, f);
  }
  return n.someProp("transformPasted", (d) => {
    l = d(l, n, a);
  }), l;
}
const yy = /^(a|abbr|acronym|b|cite|code|del|em|i|ins|kbd|label|output|q|ruby|s|samp|span|strong|sub|sup|time|u|tt|var)$/i;
function by(n, e) {
  if (n.childCount < 2)
    return n;
  for (let t = e.depth; t >= 0; t--) {
    let o = e.node(t).contentMatchAt(e.index(t)), i, s = [];
    if (n.forEach((l) => {
      if (!s)
        return;
      let a = o.findWrapping(l.type), c;
      if (!a)
        return s = null;
      if (c = s.length && i.length && gf(a, i, l, s[s.length - 1], 0))
        s[s.length - 1] = c;
      else {
        s.length && (s[s.length - 1] = yf(s[s.length - 1], i.length));
        let u = mf(l, a);
        s.push(u), o = o.matchType(u.type), i = a;
      }
    }), s)
      return M.from(s);
  }
  return n;
}
function mf(n, e, t = 0) {
  for (let r = e.length - 1; r >= t; r--)
    n = e[r].create(null, M.from(n));
  return n;
}
function gf(n, e, t, r, o) {
  if (o < n.length && o < e.length && n[o] == e[o]) {
    let i = gf(n, e, t, r.lastChild, o + 1);
    if (i)
      return r.copy(r.content.replaceChild(r.childCount - 1, i));
    if (r.contentMatchAt(r.childCount).matchType(o == n.length - 1 ? t.type : n[o + 1]))
      return r.copy(r.content.append(M.from(mf(t, n, o + 1))));
  }
}
function yf(n, e) {
  if (e == 0)
    return n;
  let t = n.content.replaceChild(n.childCount - 1, yf(n.lastChild, e - 1)), r = n.contentMatchAt(n.childCount).fillBefore(M.empty, !0);
  return n.copy(t.append(r));
}
function gl(n, e, t, r, o, i) {
  let s = e < 0 ? n.firstChild : n.lastChild, l = s.content;
  return n.childCount > 1 && (i = 0), o < r - 1 && (l = gl(l, e, t, r, o + 1, i)), o >= t && (l = e < 0 ? s.contentMatchAt(0).fillBefore(l, i <= o).append(l) : l.append(s.contentMatchAt(s.childCount).fillBefore(M.empty, !0))), n.replaceChild(e < 0 ? 0 : n.childCount - 1, s.copy(l));
}
function Kc(n, e, t) {
  return e < n.openStart && (n = new O(gl(n.content, -1, e, n.openStart, 0, n.openEnd), e, n.openEnd)), t < n.openEnd && (n = new O(gl(n.content, 1, t, n.openEnd, 0, 0), n.openStart, t)), n;
}
const bf = {
  thead: ["table"],
  tbody: ["table"],
  tfoot: ["table"],
  caption: ["table"],
  colgroup: ["table"],
  col: ["table", "colgroup"],
  tr: ["table", "tbody"],
  td: ["table", "tbody", "tr"],
  th: ["table", "tbody", "tr"]
};
let qc = null;
function wf() {
  return qc || (qc = document.implementation.createHTMLDocument("title"));
}
let Ms = null;
function wy(n) {
  let e = window.trustedTypes;
  return e ? (Ms || (Ms = e.defaultPolicy || e.createPolicy("ProseMirrorClipboard", { createHTML: (t) => t })), Ms.createHTML(n)) : n;
}
function vy(n) {
  let e = /^(\s*<meta [^>]*>)*/.exec(n);
  e && (n = n.slice(e[0].length));
  let t = wf().createElement("div"), r = /<([a-z][^>\s]+)/i.exec(n), o;
  if ((o = r && bf[r[1].toLowerCase()]) && (n = o.map((i) => "<" + i + ">").join("") + n + o.map((i) => "</" + i + ">").reverse().join("")), t.innerHTML = wy(n), o)
    for (let i = 0; i < o.length; i++)
      t = t.querySelector(o[i]) || t;
  return t;
}
function Cy(n) {
  let e = n.querySelectorAll(ve ? "span:not([class]):not([style])" : "span.Apple-converted-space");
  for (let t = 0; t < e.length; t++) {
    let r = e[t];
    r.childNodes.length == 1 && r.textContent == " " && r.parentNode && r.parentNode.replaceChild(n.ownerDocument.createTextNode(" "), r);
  }
}
function Sy(n, e) {
  if (!n.size)
    return n;
  let t = n.content.firstChild.type.schema, r;
  try {
    r = JSON.parse(e);
  } catch {
    return n;
  }
  let { content: o, openStart: i, openEnd: s } = n;
  for (let l = r.length - 2; l >= 0; l -= 2) {
    let a = t.nodes[r[l]];
    if (!a || a.hasRequiredAttrs())
      break;
    o = M.from(a.create(r[l + 1], o)), i++, s++;
  }
  return new O(o, i, s);
}
const Re = {}, De = {}, xy = { touchstart: !0, touchmove: !0 };
class ky {
  constructor() {
    this.shiftKey = !1, this.mouseDown = null, this.lastKeyCode = null, this.lastKeyCodeTime = 0, this.lastClick = { time: 0, x: 0, y: 0, type: "", button: 0 }, this.lastSelectionOrigin = null, this.lastSelectionTime = 0, this.lastIOSEnter = 0, this.lastIOSEnterFallbackTimeout = -1, this.lastFocus = 0, this.lastTouch = 0, this.lastChromeDelete = 0, this.composing = !1, this.compositionNode = null, this.composingTimeout = -1, this.compositionNodes = [], this.compositionEndedAt = -2e8, this.compositionID = 1, this.badSafariComposition = !1, this.compositionPendingChanges = 0, this.domChangeCount = 0, this.eventHandlers = /* @__PURE__ */ Object.create(null), this.hideSelectionGuard = null;
  }
}
function Ey(n) {
  for (let e in Re) {
    let t = Re[e];
    n.dom.addEventListener(e, n.input.eventHandlers[e] = (r) => {
      Ty(n, r) && !ra(n, r) && (n.editable || !(r.type in De)) && t(n, r);
    }, xy[e] ? { passive: !0 } : void 0);
  }
  Se && n.dom.addEventListener("input", () => null), yl(n);
}
function Vt(n, e) {
  n.input.lastSelectionOrigin = e, n.input.lastSelectionTime = Date.now();
}
function My(n) {
  n.domObserver.stop();
  for (let e in n.input.eventHandlers)
    n.dom.removeEventListener(e, n.input.eventHandlers[e]);
  clearTimeout(n.input.composingTimeout), clearTimeout(n.input.lastIOSEnterFallbackTimeout);
}
function yl(n) {
  n.someProp("handleDOMEvents", (e) => {
    for (let t in e)
      n.input.eventHandlers[t] || n.dom.addEventListener(t, n.input.eventHandlers[t] = (r) => ra(n, r));
  });
}
function ra(n, e) {
  return n.someProp("handleDOMEvents", (t) => {
    let r = t[e.type];
    return r ? r(n, e) || e.defaultPrevented : !1;
  });
}
function Ty(n, e) {
  if (!e.bubbles)
    return !0;
  if (e.defaultPrevented)
    return !1;
  for (let t = e.target; t != n.dom; t = t.parentNode)
    if (!t || t.nodeType == 11 || t.pmViewDesc && t.pmViewDesc.stopEvent(e))
      return !1;
  return !0;
}
function Ay(n, e) {
  !ra(n, e) && Re[e.type] && (n.editable || !(e.type in De)) && Re[e.type](n, e);
}
De.keydown = (n, e) => {
  let t = e;
  if (n.input.shiftKey = t.keyCode == 16 || t.shiftKey, !Cf(n, t) && (n.input.lastKeyCode = t.keyCode, n.input.lastKeyCodeTime = Date.now(), !(St && ve && t.keyCode == 13)))
    if (t.keyCode != 229 && n.domObserver.forceFlush(), Gn && t.keyCode == 13 && !t.ctrlKey && !t.altKey && !t.metaKey) {
      let r = Date.now();
      n.input.lastIOSEnter = r, n.input.lastIOSEnterFallbackTimeout = setTimeout(() => {
        n.input.lastIOSEnter == r && (n.someProp("handleKeyDown", (o) => o(n, ln(13, "Enter"))), n.input.lastIOSEnter = 0);
      }, 200);
    } else n.someProp("handleKeyDown", (r) => r(n, t)) || gy(n, t) ? t.preventDefault() : Vt(n, "key");
};
De.keyup = (n, e) => {
  e.keyCode == 16 && (n.input.shiftKey = !1);
};
De.keypress = (n, e) => {
  let t = e;
  if (Cf(n, t) || !t.charCode || t.ctrlKey && !t.altKey || Ue && t.metaKey)
    return;
  if (n.someProp("handleKeyPress", (o) => o(n, t))) {
    t.preventDefault();
    return;
  }
  let r = n.state.selection;
  if (!(r instanceof L) || !r.$from.sameParent(r.$to)) {
    let o = String.fromCharCode(t.charCode), i = () => n.state.tr.insertText(o).scrollIntoView();
    !/[\r\n]/.test(o) && !n.someProp("handleTextInput", (s) => s(n, r.$from.pos, r.$to.pos, o, i)) && n.dispatch(i()), t.preventDefault();
  }
};
function $i(n) {
  return { left: n.clientX, top: n.clientY };
}
function Oy(n, e) {
  let t = e.x - n.clientX, r = e.y - n.clientY;
  return t * t + r * r < 100;
}
function oa(n, e, t, r, o) {
  if (r == -1)
    return !1;
  let i = n.state.doc.resolve(r);
  for (let s = i.depth + 1; s > 0; s--)
    if (n.someProp(e, (l) => s > i.depth ? l(n, t, i.nodeAfter, i.before(s), o, !0) : l(n, t, i.node(s), i.before(s), o, !1)))
      return !0;
  return !1;
}
function Vn(n, e, t) {
  if (n.focused || n.focus(), n.state.selection.eq(e))
    return;
  let r = n.state.tr.setSelection(e);
  r.setMeta("pointer", !0), n.dispatch(r);
}
function Ny(n, e) {
  if (e == -1)
    return !1;
  let t = n.state.doc.resolve(e), r = t.nodeAfter;
  return r && r.isAtom && I.isSelectable(r) ? (Vn(n, new I(t)), !0) : !1;
}
function Ry(n, e) {
  if (e == -1)
    return !1;
  let t = n.state.selection, r, o;
  t instanceof I && (r = t.node);
  let i = n.state.doc.resolve(e);
  for (let s = i.depth + 1; s > 0; s--) {
    let l = s > i.depth ? i.nodeAfter : i.node(s);
    if (I.isSelectable(l)) {
      r && t.$from.depth > 0 && s >= t.$from.depth && i.before(t.$from.depth + 1) == t.$from.pos ? o = i.before(t.$from.depth) : o = i.before(s);
      break;
    }
  }
  return o != null ? (Vn(n, I.create(n.state.doc, o)), !0) : !1;
}
function Dy(n, e, t, r, o) {
  return oa(n, "handleClickOn", e, t, r) || n.someProp("handleClick", (i) => i(n, e, r)) || (o ? Ry(n, t) : Ny(n, t));
}
function Py(n, e, t, r) {
  return oa(n, "handleDoubleClickOn", e, t, r) || n.someProp("handleDoubleClick", (o) => o(n, e, r));
}
function Iy(n, e, t, r) {
  return oa(n, "handleTripleClickOn", e, t, r) || n.someProp("handleTripleClick", (o) => o(n, e, r)) || Ly(n, t, r);
}
function Ly(n, e, t) {
  if (t.button != 0)
    return !1;
  let r = n.state.doc;
  if (e == -1)
    return r.inlineContent ? (Vn(n, L.create(r, 0, r.content.size)), !0) : !1;
  let o = r.resolve(e);
  for (let i = o.depth + 1; i > 0; i--) {
    let s = i > o.depth ? o.nodeAfter : o.node(i), l = o.before(i);
    if (s.inlineContent)
      Vn(n, L.create(r, l + 1, l + 1 + s.content.size));
    else if (I.isSelectable(s))
      Vn(n, I.create(r, l));
    else
      continue;
    return !0;
  }
}
function ia(n) {
  return Ko(n);
}
const vf = Ue ? "metaKey" : "ctrlKey";
Re.mousedown = (n, e) => {
  let t = e;
  n.input.shiftKey = t.shiftKey;
  let r = ia(n), o = Date.now(), i = "singleClick";
  o - n.input.lastClick.time < 500 && Oy(t, n.input.lastClick) && !t[vf] && n.input.lastClick.button == t.button && (n.input.lastClick.type == "singleClick" ? i = "doubleClick" : n.input.lastClick.type == "doubleClick" && (i = "tripleClick")), n.input.lastClick = { time: o, x: t.clientX, y: t.clientY, type: i, button: t.button };
  let s = n.posAtCoords($i(t));
  s && (i == "singleClick" ? (n.input.mouseDown && n.input.mouseDown.done(), n.input.mouseDown = new _y(n, s, t, !!r)) : (i == "doubleClick" ? Py : Iy)(n, s.pos, s.inside, t) ? t.preventDefault() : Vt(n, "pointer"));
};
class _y {
  constructor(e, t, r, o) {
    this.view = e, this.pos = t, this.event = r, this.flushed = o, this.delayedSelectionSync = !1, this.mightDrag = null, this.startDoc = e.state.doc, this.selectNode = !!r[vf], this.allowDefault = r.shiftKey;
    let i, s;
    if (t.inside > -1)
      i = e.state.doc.nodeAt(t.inside), s = t.inside;
    else {
      let u = e.state.doc.resolve(t.pos);
      i = u.parent, s = u.depth ? u.before() : 0;
    }
    const l = o ? null : r.target, a = l ? e.docView.nearestDesc(l, !0) : null;
    this.target = a && a.nodeDOM.nodeType == 1 ? a.nodeDOM : null;
    let { selection: c } = e.state;
    (r.button == 0 && i.type.spec.draggable && i.type.spec.selectable !== !1 || c instanceof I && c.from <= s && c.to > s) && (this.mightDrag = {
      node: i,
      pos: s,
      addAttr: !!(this.target && !this.target.draggable),
      setUneditable: !!(this.target && qe && !this.target.hasAttribute("contentEditable"))
    }), this.target && this.mightDrag && (this.mightDrag.addAttr || this.mightDrag.setUneditable) && (this.view.domObserver.stop(), this.mightDrag.addAttr && (this.target.draggable = !0), this.mightDrag.setUneditable && setTimeout(() => {
      this.view.input.mouseDown == this && this.target.setAttribute("contentEditable", "false");
    }, 20), this.view.domObserver.start()), e.root.addEventListener("mouseup", this.up = this.up.bind(this)), e.root.addEventListener("mousemove", this.move = this.move.bind(this)), Vt(e, "pointer");
  }
  done() {
    this.view.root.removeEventListener("mouseup", this.up), this.view.root.removeEventListener("mousemove", this.move), this.mightDrag && this.target && (this.view.domObserver.stop(), this.mightDrag.addAttr && this.target.removeAttribute("draggable"), this.mightDrag.setUneditable && this.target.removeAttribute("contentEditable"), this.view.domObserver.start()), this.delayedSelectionSync && setTimeout(() => kt(this.view)), this.view.input.mouseDown = null;
  }
  up(e) {
    if (this.done(), !this.view.dom.contains(e.target))
      return;
    let t = this.pos;
    this.view.state.doc != this.startDoc && (t = this.view.posAtCoords($i(e))), this.updateAllowDefault(e), this.allowDefault || !t ? Vt(this.view, "pointer") : Dy(this.view, t.pos, t.inside, e, this.selectNode) ? e.preventDefault() : e.button == 0 && (this.flushed || // Safari ignores clicks on draggable elements
    Se && this.mightDrag && !this.mightDrag.node.isAtom || // Chrome will sometimes treat a node selection as a
    // cursor, but still report that the node is selected
    // when asked through getSelection. You'll then get a
    // situation where clicking at the point where that
    // (hidden) cursor is doesn't change the selection, and
    // thus doesn't get a reaction from ProseMirror. This
    // works around that.
    ve && !this.view.state.selection.visible && Math.min(Math.abs(t.pos - this.view.state.selection.from), Math.abs(t.pos - this.view.state.selection.to)) <= 2) ? (Vn(this.view, _.near(this.view.state.doc.resolve(t.pos))), e.preventDefault()) : Vt(this.view, "pointer");
  }
  move(e) {
    this.updateAllowDefault(e), Vt(this.view, "pointer"), e.buttons == 0 && this.done();
  }
  updateAllowDefault(e) {
    !this.allowDefault && (Math.abs(this.event.x - e.clientX) > 4 || Math.abs(this.event.y - e.clientY) > 4) && (this.allowDefault = !0);
  }
}
Re.touchstart = (n) => {
  n.input.lastTouch = Date.now(), ia(n), Vt(n, "pointer");
};
Re.touchmove = (n) => {
  n.input.lastTouch = Date.now(), Vt(n, "pointer");
};
Re.contextmenu = (n) => ia(n);
function Cf(n, e) {
  return n.composing ? !0 : Se && Math.abs(e.timeStamp - n.input.compositionEndedAt) < 500 ? (n.input.compositionEndedAt = -2e8, !0) : !1;
}
const By = St ? 5e3 : -1;
De.compositionstart = De.compositionupdate = (n) => {
  if (!n.composing) {
    n.domObserver.flush();
    let { state: e } = n, t = e.selection.$to;
    if (e.selection instanceof L && (e.storedMarks || !t.textOffset && t.parentOffset && t.nodeBefore.marks.some((r) => r.type.spec.inclusive === !1) || ve && Xd && Fy(n)))
      n.markCursor = n.state.storedMarks || t.marks(), Ko(n, !0), n.markCursor = null;
    else if (Ko(n, !e.selection.empty), qe && e.selection.empty && t.parentOffset && !t.textOffset && t.nodeBefore.marks.length) {
      let r = n.domSelectionRange();
      for (let o = r.focusNode, i = r.focusOffset; o && o.nodeType == 1 && i != 0; ) {
        let s = i < 0 ? o.lastChild : o.childNodes[i - 1];
        if (!s)
          break;
        if (s.nodeType == 3) {
          let l = n.domSelection();
          l && l.collapse(s, s.nodeValue.length);
          break;
        } else
          o = s, i = -1;
      }
    }
    n.input.composing = !0;
  }
  Sf(n, By);
};
function Fy(n) {
  let { focusNode: e, focusOffset: t } = n.domSelectionRange();
  if (!e || e.nodeType != 1 || t >= e.childNodes.length)
    return !1;
  let r = e.childNodes[t];
  return r.nodeType == 1 && r.contentEditable == "false";
}
De.compositionend = (n, e) => {
  n.composing && (n.input.composing = !1, n.input.compositionEndedAt = e.timeStamp, n.input.compositionPendingChanges = n.domObserver.pendingRecords().length ? n.input.compositionID : 0, n.input.compositionNode = null, n.input.badSafariComposition ? n.domObserver.forceFlush() : n.input.compositionPendingChanges && Promise.resolve().then(() => n.domObserver.flush()), n.input.compositionID++, Sf(n, 20));
};
function Sf(n, e) {
  clearTimeout(n.input.composingTimeout), e > -1 && (n.input.composingTimeout = setTimeout(() => Ko(n), e));
}
function xf(n) {
  for (n.composing && (n.input.composing = !1, n.input.compositionEndedAt = $y()); n.input.compositionNodes.length > 0; )
    n.input.compositionNodes.pop().markParentsDirty();
}
function zy(n) {
  let e = n.domSelectionRange();
  if (!e.focusNode)
    return null;
  let t = Rg(e.focusNode, e.focusOffset), r = Dg(e.focusNode, e.focusOffset);
  if (t && r && t != r) {
    let o = r.pmViewDesc, i = n.domObserver.lastChangedTextNode;
    if (t == i || r == i)
      return i;
    if (!o || !o.isText(r.nodeValue))
      return r;
    if (n.input.compositionNode == r) {
      let s = t.pmViewDesc;
      if (!(!s || !s.isText(t.nodeValue)))
        return r;
    }
  }
  return t || r;
}
function $y() {
  let n = document.createEvent("Event");
  return n.initEvent("event", !0, !0), n.timeStamp;
}
function Ko(n, e = !1) {
  if (!(St && n.domObserver.flushingSoon >= 0)) {
    if (n.domObserver.forceFlush(), xf(n), e || n.docView && n.docView.dirty) {
      let t = ea(n), r = n.state.selection;
      return t && !t.eq(r) ? n.dispatch(n.state.tr.setSelection(t)) : (n.markCursor || e) && !r.$from.node(r.$from.sharedDepth(r.to)).inlineContent ? n.dispatch(n.state.tr.deleteSelection()) : n.updateState(n.state), !0;
    }
    return !1;
  }
}
function Hy(n, e) {
  if (!n.dom.parentNode)
    return;
  let t = n.dom.parentNode.appendChild(document.createElement("div"));
  t.appendChild(e), t.style.cssText = "position: fixed; left: -10000px; top: 10px";
  let r = getSelection(), o = document.createRange();
  o.selectNodeContents(e), n.dom.blur(), r.removeAllRanges(), r.addRange(o), setTimeout(() => {
    t.parentNode && t.parentNode.removeChild(t), n.focus();
  }, 50);
}
const Dr = Le && Kt < 15 || Gn && _g < 604;
Re.copy = De.cut = (n, e) => {
  let t = e, r = n.state.selection, o = t.type == "cut";
  if (r.empty)
    return;
  let i = Dr ? null : t.clipboardData, s = r.content(), { dom: l, text: a } = na(n, s);
  i ? (t.preventDefault(), i.clearData(), i.setData("text/html", l.innerHTML), i.setData("text/plain", a)) : Hy(n, l), o && n.dispatch(n.state.tr.deleteSelection().scrollIntoView().setMeta("uiEvent", "cut"));
};
function Wy(n) {
  return n.openStart == 0 && n.openEnd == 0 && n.content.childCount == 1 ? n.content.firstChild : null;
}
function Vy(n, e) {
  if (!n.dom.parentNode)
    return;
  let t = n.input.shiftKey || n.state.selection.$from.parent.type.spec.code, r = n.dom.parentNode.appendChild(document.createElement(t ? "textarea" : "div"));
  t || (r.contentEditable = "true"), r.style.cssText = "position: fixed; left: -10000px; top: 10px", r.focus();
  let o = n.input.shiftKey && n.input.lastKeyCode != 45;
  setTimeout(() => {
    n.focus(), r.parentNode && r.parentNode.removeChild(r), t ? Pr(n, r.value, null, o, e) : Pr(n, r.textContent, r.innerHTML, o, e);
  }, 50);
}
function Pr(n, e, t, r, o) {
  let i = pf(n, e, t, r, n.state.selection.$from);
  if (n.someProp("handlePaste", (a) => a(n, o, i || O.empty)))
    return !0;
  if (!i)
    return !1;
  let s = Wy(i), l = s ? n.state.tr.replaceSelectionWith(s, r) : n.state.tr.replaceSelection(i);
  return n.dispatch(l.scrollIntoView().setMeta("paste", !0).setMeta("uiEvent", "paste")), !0;
}
function kf(n) {
  let e = n.getData("text/plain") || n.getData("Text");
  if (e)
    return e;
  let t = n.getData("text/uri-list");
  return t ? t.replace(/\r?\n/g, " ") : "";
}
De.paste = (n, e) => {
  let t = e;
  if (n.composing && !St)
    return;
  let r = Dr ? null : t.clipboardData, o = n.input.shiftKey && n.input.lastKeyCode != 45;
  r && Pr(n, kf(r), r.getData("text/html"), o, t) ? t.preventDefault() : Vy(n, t);
};
class Ef {
  constructor(e, t, r) {
    this.slice = e, this.move = t, this.node = r;
  }
}
const jy = Ue ? "altKey" : "ctrlKey";
function Mf(n, e) {
  let t = n.someProp("dragCopies", (r) => !r(e));
  return t ?? !e[jy];
}
Re.dragstart = (n, e) => {
  let t = e, r = n.input.mouseDown;
  if (r && r.done(), !t.dataTransfer)
    return;
  let o = n.state.selection, i = o.empty ? null : n.posAtCoords($i(t)), s;
  if (!(i && i.pos >= o.from && i.pos <= (o instanceof I ? o.to - 1 : o.to))) {
    if (r && r.mightDrag)
      s = I.create(n.state.doc, r.mightDrag.pos);
    else if (t.target && t.target.nodeType == 1) {
      let d = n.docView.nearestDesc(t.target, !0);
      d && d.node.type.spec.draggable && d != n.docView && (s = I.create(n.state.doc, d.posBefore));
    }
  }
  let l = (s || n.state.selection).content(), { dom: a, text: c, slice: u } = na(n, l);
  (!t.dataTransfer.files.length || !ve || Yd > 120) && t.dataTransfer.clearData(), t.dataTransfer.setData(Dr ? "Text" : "text/html", a.innerHTML), t.dataTransfer.effectAllowed = "copyMove", Dr || t.dataTransfer.setData("text/plain", c), n.dragging = new Ef(u, Mf(n, t), s);
};
Re.dragend = (n) => {
  let e = n.dragging;
  window.setTimeout(() => {
    n.dragging == e && (n.dragging = null);
  }, 50);
};
De.dragover = De.dragenter = (n, e) => e.preventDefault();
De.drop = (n, e) => {
  try {
    Uy(n, e, n.dragging);
  } finally {
    n.dragging = null;
  }
};
function Uy(n, e, t) {
  if (!e.dataTransfer)
    return;
  let r = n.posAtCoords($i(e));
  if (!r)
    return;
  let o = n.state.doc.resolve(r.pos), i = t && t.slice;
  i ? n.someProp("transformPasted", (h) => {
    i = h(i, n, !1);
  }) : i = pf(n, kf(e.dataTransfer), Dr ? null : e.dataTransfer.getData("text/html"), !1, o);
  let s = !!(t && Mf(n, e));
  if (n.someProp("handleDrop", (h) => h(n, e, i || O.empty, s))) {
    e.preventDefault();
    return;
  }
  if (!i)
    return;
  e.preventDefault();
  let l = i ? Hd(n.state.doc, o.pos, i) : o.pos;
  l == null && (l = o.pos);
  let a = n.state.tr;
  if (s) {
    let { node: h } = t;
    h ? h.replace(a) : a.deleteSelection();
  }
  let c = a.mapping.map(l), u = i.openStart == 0 && i.openEnd == 0 && i.content.childCount == 1, d = a.doc;
  if (u ? a.replaceRangeWith(c, c, i.content.firstChild) : a.replaceRange(c, c, i), a.doc.eq(d))
    return;
  let f = a.doc.resolve(c);
  if (u && I.isSelectable(i.content.firstChild) && f.nodeAfter && f.nodeAfter.sameMarkup(i.content.firstChild))
    a.setSelection(new I(f));
  else {
    let h = a.mapping.map(l);
    a.mapping.maps[a.mapping.maps.length - 1].forEach((p, m, g, y) => h = y), a.setSelection(ta(n, f, a.doc.resolve(h)));
  }
  n.focus(), n.dispatch(a.setMeta("uiEvent", "drop"));
}
Re.focus = (n) => {
  n.input.lastFocus = Date.now(), n.focused || (n.domObserver.stop(), n.dom.classList.add("ProseMirror-focused"), n.domObserver.start(), n.focused = !0, setTimeout(() => {
    n.docView && n.hasFocus() && !n.domObserver.currentSelection.eq(n.domSelectionRange()) && kt(n);
  }, 20));
};
Re.blur = (n, e) => {
  let t = e;
  n.focused && (n.domObserver.stop(), n.dom.classList.remove("ProseMirror-focused"), n.domObserver.start(), t.relatedTarget && n.dom.contains(t.relatedTarget) && n.domObserver.currentSelection.clear(), n.focused = !1);
};
Re.beforeinput = (n, e) => {
  if (ve && St && e.inputType == "deleteContentBackward") {
    n.domObserver.flushSoon();
    let { domChangeCount: r } = n.input;
    setTimeout(() => {
      if (n.input.domChangeCount != r || (n.dom.blur(), n.focus(), n.someProp("handleKeyDown", (i) => i(n, ln(8, "Backspace")))))
        return;
      let { $cursor: o } = n.state.selection;
      o && o.pos > 0 && n.dispatch(n.state.tr.delete(o.pos - 1, o.pos).scrollIntoView());
    }, 50);
  }
};
for (let n in De)
  Re[n] = De[n];
function Ir(n, e) {
  if (n == e)
    return !0;
  for (let t in n)
    if (n[t] !== e[t])
      return !1;
  for (let t in e)
    if (!(t in n))
      return !1;
  return !0;
}
class qo {
  constructor(e, t) {
    this.toDOM = e, this.spec = t || mn, this.side = this.spec.side || 0;
  }
  map(e, t, r, o) {
    let { pos: i, deleted: s } = e.mapResult(t.from + o, this.side < 0 ? -1 : 1);
    return s ? null : new Ne(i - r, i - r, this);
  }
  valid() {
    return !0;
  }
  eq(e) {
    return this == e || e instanceof qo && (this.spec.key && this.spec.key == e.spec.key || this.toDOM == e.toDOM && Ir(this.spec, e.spec));
  }
  destroy(e) {
    this.spec.destroy && this.spec.destroy(e);
  }
}
class Gt {
  constructor(e, t) {
    this.attrs = e, this.spec = t || mn;
  }
  map(e, t, r, o) {
    let i = e.map(t.from + o, this.spec.inclusiveStart ? -1 : 1) - r, s = e.map(t.to + o, this.spec.inclusiveEnd ? 1 : -1) - r;
    return i >= s ? null : new Ne(i, s, this);
  }
  valid(e, t) {
    return t.from < t.to;
  }
  eq(e) {
    return this == e || e instanceof Gt && Ir(this.attrs, e.attrs) && Ir(this.spec, e.spec);
  }
  static is(e) {
    return e.type instanceof Gt;
  }
  destroy() {
  }
}
class sa {
  constructor(e, t) {
    this.attrs = e, this.spec = t || mn;
  }
  map(e, t, r, o) {
    let i = e.mapResult(t.from + o, 1);
    if (i.deleted)
      return null;
    let s = e.mapResult(t.to + o, -1);
    return s.deleted || s.pos <= i.pos ? null : new Ne(i.pos - r, s.pos - r, this);
  }
  valid(e, t) {
    let { index: r, offset: o } = e.content.findIndex(t.from), i;
    return o == t.from && !(i = e.child(r)).isText && o + i.nodeSize == t.to;
  }
  eq(e) {
    return this == e || e instanceof sa && Ir(this.attrs, e.attrs) && Ir(this.spec, e.spec);
  }
  destroy() {
  }
}
class Ne {
  /**
  @internal
  */
  constructor(e, t, r) {
    this.from = e, this.to = t, this.type = r;
  }
  /**
  @internal
  */
  copy(e, t) {
    return new Ne(e, t, this.type);
  }
  /**
  @internal
  */
  eq(e, t = 0) {
    return this.type.eq(e.type) && this.from + t == e.from && this.to + t == e.to;
  }
  /**
  @internal
  */
  map(e, t, r) {
    return this.type.map(e, this, t, r);
  }
  /**
  Creates a widget decoration, which is a DOM node that's shown in
  the document at the given position. It is recommended that you
  delay rendering the widget by passing a function that will be
  called when the widget is actually drawn in a view, but you can
  also directly pass a DOM node. `getPos` can be used to find the
  widget's current document position.
  */
  static widget(e, t, r) {
    return new Ne(e, e, new qo(t, r));
  }
  /**
  Creates an inline decoration, which adds the given attributes to
  each inline node between `from` and `to`.
  */
  static inline(e, t, r, o) {
    return new Ne(e, t, new Gt(r, o));
  }
  /**
  Creates a node decoration. `from` and `to` should point precisely
  before and after a node in the document. That node, and only that
  node, will receive the given attributes.
  */
  static node(e, t, r, o) {
    return new Ne(e, t, new sa(r, o));
  }
  /**
  The spec provided when creating this decoration. Can be useful
  if you've stored extra information in that object.
  */
  get spec() {
    return this.type.spec;
  }
  /**
  @internal
  */
  get inline() {
    return this.type instanceof Gt;
  }
  /**
  @internal
  */
  get widget() {
    return this.type instanceof qo;
  }
}
const Fn = [], mn = {};
class se {
  /**
  @internal
  */
  constructor(e, t) {
    this.local = e.length ? e : Fn, this.children = t.length ? t : Fn;
  }
  /**
  Create a set of decorations, using the structure of the given
  document. This will consume (modify) the `decorations` array, so
  you must make a copy if you want need to preserve that.
  */
  static create(e, t) {
    return t.length ? Go(t, e, 0, mn) : Ce;
  }
  /**
  Find all decorations in this set which touch the given range
  (including decorations that start or end directly at the
  boundaries) and match the given predicate on their spec. When
  `start` and `end` are omitted, all decorations in the set are
  considered. When `predicate` isn't given, all decorations are
  assumed to match.
  */
  find(e, t, r) {
    let o = [];
    return this.findInner(e ?? 0, t ?? 1e9, o, 0, r), o;
  }
  findInner(e, t, r, o, i) {
    for (let s = 0; s < this.local.length; s++) {
      let l = this.local[s];
      l.from <= t && l.to >= e && (!i || i(l.spec)) && r.push(l.copy(l.from + o, l.to + o));
    }
    for (let s = 0; s < this.children.length; s += 3)
      if (this.children[s] < t && this.children[s + 1] > e) {
        let l = this.children[s] + 1;
        this.children[s + 2].findInner(e - l, t - l, r, o + l, i);
      }
  }
  /**
  Map the set of decorations in response to a change in the
  document.
  */
  map(e, t, r) {
    return this == Ce || e.maps.length == 0 ? this : this.mapInner(e, t, 0, 0, r || mn);
  }
  /**
  @internal
  */
  mapInner(e, t, r, o, i) {
    let s;
    for (let l = 0; l < this.local.length; l++) {
      let a = this.local[l].map(e, r, o);
      a && a.type.valid(t, a) ? (s || (s = [])).push(a) : i.onRemove && i.onRemove(this.local[l].spec);
    }
    return this.children.length ? Ky(this.children, s || [], e, t, r, o, i) : s ? new se(s.sort(gn), Fn) : Ce;
  }
  /**
  Add the given array of decorations to the ones in the set,
  producing a new set. Consumes the `decorations` array. Needs
  access to the current document to create the appropriate tree
  structure.
  */
  add(e, t) {
    return t.length ? this == Ce ? se.create(e, t) : this.addInner(e, t, 0) : this;
  }
  addInner(e, t, r) {
    let o, i = 0;
    e.forEach((l, a) => {
      let c = a + r, u;
      if (u = Af(t, l, c)) {
        for (o || (o = this.children.slice()); i < o.length && o[i] < a; )
          i += 3;
        o[i] == a ? o[i + 2] = o[i + 2].addInner(l, u, c + 1) : o.splice(i, 0, a, a + l.nodeSize, Go(u, l, c + 1, mn)), i += 3;
      }
    });
    let s = Tf(i ? Of(t) : t, -r);
    for (let l = 0; l < s.length; l++)
      s[l].type.valid(e, s[l]) || s.splice(l--, 1);
    return new se(s.length ? this.local.concat(s).sort(gn) : this.local, o || this.children);
  }
  /**
  Create a new set that contains the decorations in this set, minus
  the ones in the given array.
  */
  remove(e) {
    return e.length == 0 || this == Ce ? this : this.removeInner(e, 0);
  }
  removeInner(e, t) {
    let r = this.children, o = this.local;
    for (let i = 0; i < r.length; i += 3) {
      let s, l = r[i] + t, a = r[i + 1] + t;
      for (let u = 0, d; u < e.length; u++)
        (d = e[u]) && d.from > l && d.to < a && (e[u] = null, (s || (s = [])).push(d));
      if (!s)
        continue;
      r == this.children && (r = this.children.slice());
      let c = r[i + 2].removeInner(s, l + 1);
      c != Ce ? r[i + 2] = c : (r.splice(i, 3), i -= 3);
    }
    if (o.length) {
      for (let i = 0, s; i < e.length; i++)
        if (s = e[i])
          for (let l = 0; l < o.length; l++)
            o[l].eq(s, t) && (o == this.local && (o = this.local.slice()), o.splice(l--, 1));
    }
    return r == this.children && o == this.local ? this : o.length || r.length ? new se(o, r) : Ce;
  }
  forChild(e, t) {
    if (this == Ce)
      return this;
    if (t.isLeaf)
      return se.empty;
    let r, o;
    for (let l = 0; l < this.children.length; l += 3)
      if (this.children[l] >= e) {
        this.children[l] == e && (r = this.children[l + 2]);
        break;
      }
    let i = e + 1, s = i + t.content.size;
    for (let l = 0; l < this.local.length; l++) {
      let a = this.local[l];
      if (a.from < s && a.to > i && a.type instanceof Gt) {
        let c = Math.max(i, a.from) - i, u = Math.min(s, a.to) - i;
        c < u && (o || (o = [])).push(a.copy(c, u));
      }
    }
    if (o) {
      let l = new se(o.sort(gn), Fn);
      return r ? new Ft([l, r]) : l;
    }
    return r || Ce;
  }
  /**
  @internal
  */
  eq(e) {
    if (this == e)
      return !0;
    if (!(e instanceof se) || this.local.length != e.local.length || this.children.length != e.children.length)
      return !1;
    for (let t = 0; t < this.local.length; t++)
      if (!this.local[t].eq(e.local[t]))
        return !1;
    for (let t = 0; t < this.children.length; t += 3)
      if (this.children[t] != e.children[t] || this.children[t + 1] != e.children[t + 1] || !this.children[t + 2].eq(e.children[t + 2]))
        return !1;
    return !0;
  }
  /**
  @internal
  */
  locals(e) {
    return la(this.localsInner(e));
  }
  /**
  @internal
  */
  localsInner(e) {
    if (this == Ce)
      return Fn;
    if (e.inlineContent || !this.local.some(Gt.is))
      return this.local;
    let t = [];
    for (let r = 0; r < this.local.length; r++)
      this.local[r].type instanceof Gt || t.push(this.local[r]);
    return t;
  }
  forEachSet(e) {
    e(this);
  }
}
se.empty = new se([], []);
se.removeOverlap = la;
const Ce = se.empty;
class Ft {
  constructor(e) {
    this.members = e;
  }
  map(e, t) {
    const r = this.members.map((o) => o.map(e, t, mn));
    return Ft.from(r);
  }
  forChild(e, t) {
    if (t.isLeaf)
      return se.empty;
    let r = [];
    for (let o = 0; o < this.members.length; o++) {
      let i = this.members[o].forChild(e, t);
      i != Ce && (i instanceof Ft ? r = r.concat(i.members) : r.push(i));
    }
    return Ft.from(r);
  }
  eq(e) {
    if (!(e instanceof Ft) || e.members.length != this.members.length)
      return !1;
    for (let t = 0; t < this.members.length; t++)
      if (!this.members[t].eq(e.members[t]))
        return !1;
    return !0;
  }
  locals(e) {
    let t, r = !0;
    for (let o = 0; o < this.members.length; o++) {
      let i = this.members[o].localsInner(e);
      if (i.length)
        if (!t)
          t = i;
        else {
          r && (t = t.slice(), r = !1);
          for (let s = 0; s < i.length; s++)
            t.push(i[s]);
        }
    }
    return t ? la(r ? t : t.sort(gn)) : Fn;
  }
  // Create a group for the given array of decoration sets, or return
  // a single set when possible.
  static from(e) {
    switch (e.length) {
      case 0:
        return Ce;
      case 1:
        return e[0];
      default:
        return new Ft(e.every((t) => t instanceof se) ? e : e.reduce((t, r) => t.concat(r instanceof se ? r : r.members), []));
    }
  }
  forEachSet(e) {
    for (let t = 0; t < this.members.length; t++)
      this.members[t].forEachSet(e);
  }
}
function Ky(n, e, t, r, o, i, s) {
  let l = n.slice();
  for (let c = 0, u = i; c < t.maps.length; c++) {
    let d = 0;
    t.maps[c].forEach((f, h, p, m) => {
      let g = m - p - (h - f);
      for (let y = 0; y < l.length; y += 3) {
        let b = l[y + 1];
        if (b < 0 || f > b + u - d)
          continue;
        let w = l[y] + u - d;
        h >= w ? l[y + 1] = f <= w ? -2 : -1 : f >= u && g && (l[y] += g, l[y + 1] += g);
      }
      d += g;
    }), u = t.maps[c].map(u, -1);
  }
  let a = !1;
  for (let c = 0; c < l.length; c += 3)
    if (l[c + 1] < 0) {
      if (l[c + 1] == -2) {
        a = !0, l[c + 1] = -1;
        continue;
      }
      let u = t.map(n[c] + i), d = u - o;
      if (d < 0 || d >= r.content.size) {
        a = !0;
        continue;
      }
      let f = t.map(n[c + 1] + i, -1), h = f - o, { index: p, offset: m } = r.content.findIndex(d), g = r.maybeChild(p);
      if (g && m == d && m + g.nodeSize == h) {
        let y = l[c + 2].mapInner(t, g, u + 1, n[c] + i + 1, s);
        y != Ce ? (l[c] = d, l[c + 1] = h, l[c + 2] = y) : (l[c + 1] = -2, a = !0);
      } else
        a = !0;
    }
  if (a) {
    let c = qy(l, n, e, t, o, i, s), u = Go(c, r, 0, s);
    e = u.local;
    for (let d = 0; d < l.length; d += 3)
      l[d + 1] < 0 && (l.splice(d, 3), d -= 3);
    for (let d = 0, f = 0; d < u.children.length; d += 3) {
      let h = u.children[d];
      for (; f < l.length && l[f] < h; )
        f += 3;
      l.splice(f, 0, u.children[d], u.children[d + 1], u.children[d + 2]);
    }
  }
  return new se(e.sort(gn), l);
}
function Tf(n, e) {
  if (!e || !n.length)
    return n;
  let t = [];
  for (let r = 0; r < n.length; r++) {
    let o = n[r];
    t.push(new Ne(o.from + e, o.to + e, o.type));
  }
  return t;
}
function qy(n, e, t, r, o, i, s) {
  function l(a, c) {
    for (let u = 0; u < a.local.length; u++) {
      let d = a.local[u].map(r, o, c);
      d ? t.push(d) : s.onRemove && s.onRemove(a.local[u].spec);
    }
    for (let u = 0; u < a.children.length; u += 3)
      l(a.children[u + 2], a.children[u] + c + 1);
  }
  for (let a = 0; a < n.length; a += 3)
    n[a + 1] == -1 && l(n[a + 2], e[a] + i + 1);
  return t;
}
function Af(n, e, t) {
  if (e.isLeaf)
    return null;
  let r = t + e.nodeSize, o = null;
  for (let i = 0, s; i < n.length; i++)
    (s = n[i]) && s.from > t && s.to < r && ((o || (o = [])).push(s), n[i] = null);
  return o;
}
function Of(n) {
  let e = [];
  for (let t = 0; t < n.length; t++)
    n[t] != null && e.push(n[t]);
  return e;
}
function Go(n, e, t, r) {
  let o = [], i = !1;
  e.forEach((l, a) => {
    let c = Af(n, l, a + t);
    if (c) {
      i = !0;
      let u = Go(c, l, t + a + 1, r);
      u != Ce && o.push(a, a + l.nodeSize, u);
    }
  });
  let s = Tf(i ? Of(n) : n, -t).sort(gn);
  for (let l = 0; l < s.length; l++)
    s[l].type.valid(e, s[l]) || (r.onRemove && r.onRemove(s[l].spec), s.splice(l--, 1));
  return s.length || o.length ? new se(s, o) : Ce;
}
function gn(n, e) {
  return n.from - e.from || n.to - e.to;
}
function la(n) {
  let e = n;
  for (let t = 0; t < e.length - 1; t++) {
    let r = e[t];
    if (r.from != r.to)
      for (let o = t + 1; o < e.length; o++) {
        let i = e[o];
        if (i.from == r.from) {
          i.to != r.to && (e == n && (e = n.slice()), e[o] = i.copy(i.from, r.to), Gc(e, o + 1, i.copy(r.to, i.to)));
          continue;
        } else {
          i.from < r.to && (e == n && (e = n.slice()), e[t] = r.copy(r.from, i.from), Gc(e, o, r.copy(i.from, r.to)));
          break;
        }
      }
  }
  return e;
}
function Gc(n, e, t) {
  for (; e < n.length && gn(t, n[e]) > 0; )
    e++;
  n.splice(e, 0, t);
}
function Ts(n) {
  let e = [];
  return n.someProp("decorations", (t) => {
    let r = t(n.state);
    r && r != Ce && e.push(r);
  }), n.cursorWrapper && e.push(se.create(n.state.doc, [n.cursorWrapper.deco])), Ft.from(e);
}
const Gy = {
  childList: !0,
  characterData: !0,
  characterDataOldValue: !0,
  attributes: !0,
  attributeOldValue: !0,
  subtree: !0
}, Jy = Le && Kt <= 11;
class Yy {
  constructor() {
    this.anchorNode = null, this.anchorOffset = 0, this.focusNode = null, this.focusOffset = 0;
  }
  set(e) {
    this.anchorNode = e.anchorNode, this.anchorOffset = e.anchorOffset, this.focusNode = e.focusNode, this.focusOffset = e.focusOffset;
  }
  clear() {
    this.anchorNode = this.focusNode = null;
  }
  eq(e) {
    return e.anchorNode == this.anchorNode && e.anchorOffset == this.anchorOffset && e.focusNode == this.focusNode && e.focusOffset == this.focusOffset;
  }
}
class Xy {
  constructor(e, t) {
    this.view = e, this.handleDOMChange = t, this.queue = [], this.flushingSoon = -1, this.observer = null, this.currentSelection = new Yy(), this.onCharData = null, this.suppressingSelectionUpdates = !1, this.lastChangedTextNode = null, this.observer = window.MutationObserver && new window.MutationObserver((r) => {
      for (let o = 0; o < r.length; o++)
        this.queue.push(r[o]);
      Le && Kt <= 11 && r.some((o) => o.type == "childList" && o.removedNodes.length || o.type == "characterData" && o.oldValue.length > o.target.nodeValue.length) ? this.flushSoon() : Se && e.composing && r.some((o) => o.type == "childList" && o.target.nodeName == "TR") ? (e.input.badSafariComposition = !0, this.flushSoon()) : this.flush();
    }), Jy && (this.onCharData = (r) => {
      this.queue.push({ target: r.target, type: "characterData", oldValue: r.prevValue }), this.flushSoon();
    }), this.onSelectionChange = this.onSelectionChange.bind(this);
  }
  flushSoon() {
    this.flushingSoon < 0 && (this.flushingSoon = window.setTimeout(() => {
      this.flushingSoon = -1, this.flush();
    }, 20));
  }
  forceFlush() {
    this.flushingSoon > -1 && (window.clearTimeout(this.flushingSoon), this.flushingSoon = -1, this.flush());
  }
  start() {
    this.observer && (this.observer.takeRecords(), this.observer.observe(this.view.dom, Gy)), this.onCharData && this.view.dom.addEventListener("DOMCharacterDataModified", this.onCharData), this.connectSelection();
  }
  stop() {
    if (this.observer) {
      let e = this.observer.takeRecords();
      if (e.length) {
        for (let t = 0; t < e.length; t++)
          this.queue.push(e[t]);
        window.setTimeout(() => this.flush(), 20);
      }
      this.observer.disconnect();
    }
    this.onCharData && this.view.dom.removeEventListener("DOMCharacterDataModified", this.onCharData), this.disconnectSelection();
  }
  connectSelection() {
    this.view.dom.ownerDocument.addEventListener("selectionchange", this.onSelectionChange);
  }
  disconnectSelection() {
    this.view.dom.ownerDocument.removeEventListener("selectionchange", this.onSelectionChange);
  }
  suppressSelectionUpdates() {
    this.suppressingSelectionUpdates = !0, setTimeout(() => this.suppressingSelectionUpdates = !1, 50);
  }
  onSelectionChange() {
    if ($c(this.view)) {
      if (this.suppressingSelectionUpdates)
        return kt(this.view);
      if (Le && Kt <= 11 && !this.view.state.selection.empty) {
        let e = this.view.domSelectionRange();
        if (e.focusNode && vn(e.focusNode, e.focusOffset, e.anchorNode, e.anchorOffset))
          return this.flushSoon();
      }
      this.flush();
    }
  }
  setCurSelection() {
    this.currentSelection.set(this.view.domSelectionRange());
  }
  ignoreSelectionChange(e) {
    if (!e.focusNode)
      return !0;
    let t = /* @__PURE__ */ new Set(), r;
    for (let i = e.focusNode; i; i = qn(i))
      t.add(i);
    for (let i = e.anchorNode; i; i = qn(i))
      if (t.has(i)) {
        r = i;
        break;
      }
    let o = r && this.view.docView.nearestDesc(r);
    if (o && o.ignoreMutation({
      type: "selection",
      target: r.nodeType == 3 ? r.parentNode : r
    }))
      return this.setCurSelection(), !0;
  }
  pendingRecords() {
    if (this.observer)
      for (let e of this.observer.takeRecords())
        this.queue.push(e);
    return this.queue;
  }
  flush() {
    let { view: e } = this;
    if (!e.docView || this.flushingSoon > -1)
      return;
    let t = this.pendingRecords();
    t.length && (this.queue = []);
    let r = e.domSelectionRange(), o = !this.suppressingSelectionUpdates && !this.currentSelection.eq(r) && $c(e) && !this.ignoreSelectionChange(r), i = -1, s = -1, l = !1, a = [];
    if (e.editable)
      for (let u = 0; u < t.length; u++) {
        let d = this.registerMutation(t[u], a);
        d && (i = i < 0 ? d.from : Math.min(d.from, i), s = s < 0 ? d.to : Math.max(d.to, s), d.typeOver && (l = !0));
      }
    if (a.some((u) => u.nodeName == "BR") && (e.input.lastKeyCode == 8 || e.input.lastKeyCode == 46)) {
      for (let u of a)
        if (u.nodeName == "BR" && u.parentNode) {
          let d = u.nextSibling;
          for (; d && d.nodeType == 1; ) {
            if (d.contentEditable == "false") {
              u.parentNode.removeChild(u);
              break;
            }
            d = d.firstChild;
          }
        }
    } else if (qe && a.length) {
      let u = a.filter((d) => d.nodeName == "BR");
      if (u.length == 2) {
        let [d, f] = u;
        d.parentNode && d.parentNode.parentNode == f.parentNode ? f.remove() : d.remove();
      } else {
        let { focusNode: d } = this.currentSelection;
        for (let f of u) {
          let h = f.parentNode;
          h && h.nodeName == "LI" && (!d || eb(e, d) != h) && f.remove();
        }
      }
    }
    let c = null;
    i < 0 && o && e.input.lastFocus > Date.now() - 200 && Math.max(e.input.lastTouch, e.input.lastClick.time) < Date.now() - 300 && Fi(r) && (c = ea(e)) && c.eq(_.near(e.state.doc.resolve(0), 1)) ? (e.input.lastFocus = 0, kt(e), this.currentSelection.set(r), e.scrollToSelection()) : (i > -1 || o) && (i > -1 && (e.docView.markDirty(i, s), Qy(e)), e.input.badSafariComposition && (e.input.badSafariComposition = !1, tb(e, a)), this.handleDOMChange(i, s, l, a), e.docView && e.docView.dirty ? e.updateState(e.state) : this.currentSelection.eq(r) || kt(e), this.currentSelection.set(r));
  }
  registerMutation(e, t) {
    if (t.indexOf(e.target) > -1)
      return null;
    let r = this.view.docView.nearestDesc(e.target);
    if (e.type == "attributes" && (r == this.view.docView || e.attributeName == "contenteditable" || // Firefox sometimes fires spurious events for null/empty styles
    e.attributeName == "style" && !e.oldValue && !e.target.getAttribute("style")) || !r || r.ignoreMutation(e))
      return null;
    if (e.type == "childList") {
      for (let u = 0; u < e.addedNodes.length; u++) {
        let d = e.addedNodes[u];
        t.push(d), d.nodeType == 3 && (this.lastChangedTextNode = d);
      }
      if (r.contentDOM && r.contentDOM != r.dom && !r.contentDOM.contains(e.target))
        return { from: r.posBefore, to: r.posAfter };
      let o = e.previousSibling, i = e.nextSibling;
      if (Le && Kt <= 11 && e.addedNodes.length)
        for (let u = 0; u < e.addedNodes.length; u++) {
          let { previousSibling: d, nextSibling: f } = e.addedNodes[u];
          (!d || Array.prototype.indexOf.call(e.addedNodes, d) < 0) && (o = d), (!f || Array.prototype.indexOf.call(e.addedNodes, f) < 0) && (i = f);
        }
      let s = o && o.parentNode == e.target ? be(o) + 1 : 0, l = r.localPosFromDOM(e.target, s, -1), a = i && i.parentNode == e.target ? be(i) : e.target.childNodes.length, c = r.localPosFromDOM(e.target, a, 1);
      return { from: l, to: c };
    } else return e.type == "attributes" ? { from: r.posAtStart - r.border, to: r.posAtEnd + r.border } : (this.lastChangedTextNode = e.target, {
      from: r.posAtStart,
      to: r.posAtEnd,
      // An event was generated for a text change that didn't change
      // any text. Mark the dom change to fall back to assuming the
      // selection was typed over with an identical value if it can't
      // find another change.
      typeOver: e.target.nodeValue == e.oldValue
    });
  }
}
let Jc = /* @__PURE__ */ new WeakMap(), Yc = !1;
function Qy(n) {
  if (!Jc.has(n) && (Jc.set(n, null), ["normal", "nowrap", "pre-line"].indexOf(getComputedStyle(n.dom).whiteSpace) !== -1)) {
    if (n.requiresGeckoHackNode = qe, Yc)
      return;
    console.warn("ProseMirror expects the CSS white-space property to be set, preferably to 'pre-wrap'. It is recommended to load style/prosemirror.css from the prosemirror-view package."), Yc = !0;
  }
}
function Xc(n, e) {
  let t = e.startContainer, r = e.startOffset, o = e.endContainer, i = e.endOffset, s = n.domAtPos(n.state.selection.anchor);
  return vn(s.node, s.offset, o, i) && ([t, r, o, i] = [o, i, t, r]), { anchorNode: t, anchorOffset: r, focusNode: o, focusOffset: i };
}
function Zy(n, e) {
  if (e.getComposedRanges) {
    let o = e.getComposedRanges(n.root)[0];
    if (o)
      return Xc(n, o);
  }
  let t;
  function r(o) {
    o.preventDefault(), o.stopImmediatePropagation(), t = o.getTargetRanges()[0];
  }
  return n.dom.addEventListener("beforeinput", r, !0), document.execCommand("indent"), n.dom.removeEventListener("beforeinput", r, !0), t ? Xc(n, t) : null;
}
function eb(n, e) {
  for (let t = e.parentNode; t && t != n.dom; t = t.parentNode) {
    let r = n.docView.nearestDesc(t, !0);
    if (r && r.node.isBlock)
      return t;
  }
  return null;
}
function tb(n, e) {
  var t;
  let { focusNode: r, focusOffset: o } = n.domSelectionRange();
  for (let i of e)
    if (((t = i.parentNode) === null || t === void 0 ? void 0 : t.nodeName) == "TR") {
      let s = i.nextSibling;
      for (; s && s.nodeName != "TD" && s.nodeName != "TH"; )
        s = s.nextSibling;
      if (s) {
        let l = s;
        for (; ; ) {
          let a = l.firstChild;
          if (!a || a.nodeType != 1 || a.contentEditable == "false" || /^(BR|IMG)$/.test(a.nodeName))
            break;
          l = a;
        }
        l.insertBefore(i, l.firstChild), r == i && n.domSelection().collapse(i, o);
      } else
        i.parentNode.removeChild(i);
    }
}
function nb(n, e, t) {
  let { node: r, fromOffset: o, toOffset: i, from: s, to: l } = n.docView.parseRange(e, t), a = n.domSelectionRange(), c, u = a.anchorNode;
  if (u && n.dom.contains(u.nodeType == 1 ? u : u.parentNode) && (c = [{ node: u, offset: a.anchorOffset }], Fi(a) || c.push({ node: a.focusNode, offset: a.focusOffset })), ve && n.input.lastKeyCode === 8)
    for (let g = i; g > o; g--) {
      let y = r.childNodes[g - 1], b = y.pmViewDesc;
      if (y.nodeName == "BR" && !b) {
        i = g;
        break;
      }
      if (!b || b.size)
        break;
    }
  let d = n.state.doc, f = n.someProp("domParser") || Ut.fromSchema(n.state.schema), h = d.resolve(s), p = null, m = f.parse(r, {
    topNode: h.parent,
    topMatch: h.parent.contentMatchAt(h.index()),
    topOpen: !0,
    from: o,
    to: i,
    preserveWhitespace: h.parent.type.whitespace == "pre" ? "full" : !0,
    findPositions: c,
    ruleFromNode: rb,
    context: h
  });
  if (c && c[0].pos != null) {
    let g = c[0].pos, y = c[1] && c[1].pos;
    y == null && (y = g), p = { anchor: g + s, head: y + s };
  }
  return { doc: m, sel: p, from: s, to: l };
}
function rb(n) {
  let e = n.pmViewDesc;
  if (e)
    return e.parseRule();
  if (n.nodeName == "BR" && n.parentNode) {
    if (Se && /^(ul|ol)$/i.test(n.parentNode.nodeName)) {
      let t = document.createElement("div");
      return t.appendChild(document.createElement("li")), { skip: t };
    } else if (n.parentNode.lastChild == n || Se && /^(tr|table)$/i.test(n.parentNode.nodeName))
      return { ignore: !0 };
  } else if (n.nodeName == "IMG" && n.getAttribute("mark-placeholder"))
    return { ignore: !0 };
  return null;
}
const ob = /^(a|abbr|acronym|b|bd[io]|big|br|button|cite|code|data(list)?|del|dfn|em|i|img|ins|kbd|label|map|mark|meter|output|q|ruby|s|samp|small|span|strong|su[bp]|time|u|tt|var)$/i;
function ib(n, e, t, r, o) {
  let i = n.input.compositionPendingChanges || (n.composing ? n.input.compositionID : 0);
  if (n.input.compositionPendingChanges = 0, e < 0) {
    let C = n.input.lastSelectionTime > Date.now() - 50 ? n.input.lastSelectionOrigin : null, A = ea(n, C);
    if (A && !n.state.selection.eq(A)) {
      if (ve && St && n.input.lastKeyCode === 13 && Date.now() - 100 < n.input.lastKeyCodeTime && n.someProp("handleKeyDown", (j) => j(n, ln(13, "Enter"))))
        return;
      let B = n.state.tr.setSelection(A);
      C == "pointer" ? B.setMeta("pointer", !0) : C == "key" && B.scrollIntoView(), i && B.setMeta("composition", i), n.dispatch(B);
    }
    return;
  }
  let s = n.state.doc.resolve(e), l = s.sharedDepth(t);
  e = s.before(l + 1), t = n.state.doc.resolve(t).after(l + 1);
  let a = n.state.selection, c = nb(n, e, t), u = n.state.doc, d = u.slice(c.from, c.to), f, h;
  n.input.lastKeyCode === 8 && Date.now() - 100 < n.input.lastKeyCodeTime ? (f = n.state.selection.to, h = "end") : (f = n.state.selection.from, h = "start"), n.input.lastKeyCode = null;
  let p = ab(d.content, c.doc.content, c.from, f, h);
  if (p && n.input.domChangeCount++, (Gn && n.input.lastIOSEnter > Date.now() - 225 || St) && o.some((C) => C.nodeType == 1 && !ob.test(C.nodeName)) && (!p || p.endA >= p.endB) && n.someProp("handleKeyDown", (C) => C(n, ln(13, "Enter")))) {
    n.input.lastIOSEnter = 0;
    return;
  }
  if (!p)
    if (r && a instanceof L && !a.empty && a.$head.sameParent(a.$anchor) && !n.composing && !(c.sel && c.sel.anchor != c.sel.head))
      p = { start: a.from, endA: a.to, endB: a.to };
    else {
      if (c.sel) {
        let C = Qc(n, n.state.doc, c.sel);
        if (C && !C.eq(n.state.selection)) {
          let A = n.state.tr.setSelection(C);
          i && A.setMeta("composition", i), n.dispatch(A);
        }
      }
      return;
    }
  n.state.selection.from < n.state.selection.to && p.start == p.endB && n.state.selection instanceof L && (p.start > n.state.selection.from && p.start <= n.state.selection.from + 2 && n.state.selection.from >= c.from ? p.start = n.state.selection.from : p.endA < n.state.selection.to && p.endA >= n.state.selection.to - 2 && n.state.selection.to <= c.to && (p.endB += n.state.selection.to - p.endA, p.endA = n.state.selection.to)), Le && Kt <= 11 && p.endB == p.start + 1 && p.endA == p.start && p.start > c.from && c.doc.textBetween(p.start - c.from - 1, p.start - c.from + 1) == "  " && (p.start--, p.endA--, p.endB--);
  let m = c.doc.resolveNoCache(p.start - c.from), g = c.doc.resolveNoCache(p.endB - c.from), y = u.resolve(p.start), b = m.sameParent(g) && m.parent.inlineContent && y.end() >= p.endA;
  if ((Gn && n.input.lastIOSEnter > Date.now() - 225 && (!b || o.some((C) => C.nodeName == "DIV" || C.nodeName == "P")) || !b && m.pos < c.doc.content.size && (!m.sameParent(g) || !m.parent.inlineContent) && m.pos < g.pos && !/\S/.test(c.doc.textBetween(m.pos, g.pos, "", ""))) && n.someProp("handleKeyDown", (C) => C(n, ln(13, "Enter")))) {
    n.input.lastIOSEnter = 0;
    return;
  }
  if (n.state.selection.anchor > p.start && lb(u, p.start, p.endA, m, g) && n.someProp("handleKeyDown", (C) => C(n, ln(8, "Backspace")))) {
    St && ve && n.domObserver.suppressSelectionUpdates();
    return;
  }
  ve && p.endB == p.start && (n.input.lastChromeDelete = Date.now()), St && !b && m.start() != g.start() && g.parentOffset == 0 && m.depth == g.depth && c.sel && c.sel.anchor == c.sel.head && c.sel.head == p.endA && (p.endB -= 2, g = c.doc.resolveNoCache(p.endB - c.from), setTimeout(() => {
    n.someProp("handleKeyDown", function(C) {
      return C(n, ln(13, "Enter"));
    });
  }, 20));
  let w = p.start, v = p.endA, k = (C) => {
    let A = C || n.state.tr.replace(w, v, c.doc.slice(p.start - c.from, p.endB - c.from));
    if (c.sel) {
      let B = Qc(n, A.doc, c.sel);
      B && !(ve && n.composing && B.empty && (p.start != p.endB || n.input.lastChromeDelete < Date.now() - 100) && (B.head == w || B.head == A.mapping.map(v) - 1) || Le && B.empty && B.head == w) && A.setSelection(B);
    }
    return i && A.setMeta("composition", i), A.scrollIntoView();
  }, E;
  if (b)
    if (m.pos == g.pos) {
      Le && Kt <= 11 && m.parentOffset == 0 && (n.domObserver.suppressSelectionUpdates(), setTimeout(() => kt(n), 20));
      let C = k(n.state.tr.delete(w, v)), A = u.resolve(p.start).marksAcross(u.resolve(p.endA));
      A && C.ensureMarks(A), n.dispatch(C);
    } else if (
      // Adding or removing a mark
      p.endA == p.endB && (E = sb(m.parent.content.cut(m.parentOffset, g.parentOffset), y.parent.content.cut(y.parentOffset, p.endA - y.start())))
    ) {
      let C = k(n.state.tr);
      E.type == "add" ? C.addMark(w, v, E.mark) : C.removeMark(w, v, E.mark), n.dispatch(C);
    } else if (m.parent.child(m.index()).isText && m.index() == g.index() - (g.textOffset ? 0 : 1)) {
      let C = m.parent.textBetween(m.parentOffset, g.parentOffset), A = () => k(n.state.tr.insertText(C, w, v));
      n.someProp("handleTextInput", (B) => B(n, w, v, C, A)) || n.dispatch(A());
    } else
      n.dispatch(k());
  else
    n.dispatch(k());
}
function Qc(n, e, t) {
  return Math.max(t.anchor, t.head) > e.content.size ? null : ta(n, e.resolve(t.anchor), e.resolve(t.head));
}
function sb(n, e) {
  let t = n.firstChild.marks, r = e.firstChild.marks, o = t, i = r, s, l, a;
  for (let u = 0; u < r.length; u++)
    o = r[u].removeFromSet(o);
  for (let u = 0; u < t.length; u++)
    i = t[u].removeFromSet(i);
  if (o.length == 1 && i.length == 0)
    l = o[0], s = "add", a = (u) => u.mark(l.addToSet(u.marks));
  else if (o.length == 0 && i.length == 1)
    l = i[0], s = "remove", a = (u) => u.mark(l.removeFromSet(u.marks));
  else
    return null;
  let c = [];
  for (let u = 0; u < e.childCount; u++)
    c.push(a(e.child(u)));
  if (M.from(c).eq(n))
    return { mark: l, type: s };
}
function lb(n, e, t, r, o) {
  if (
    // The content must have shrunk
    t - e <= o.pos - r.pos || // newEnd must point directly at or after the end of the block that newStart points into
    As(r, !0, !1) < o.pos
  )
    return !1;
  let i = n.resolve(e);
  if (!r.parent.isTextblock) {
    let l = i.nodeAfter;
    return l != null && t == e + l.nodeSize;
  }
  if (i.parentOffset < i.parent.content.size || !i.parent.isTextblock)
    return !1;
  let s = n.resolve(As(i, !0, !0));
  return !s.parent.isTextblock || s.pos > t || As(s, !0, !1) < t ? !1 : r.parent.content.cut(r.parentOffset).eq(s.parent.content);
}
function As(n, e, t) {
  let r = n.depth, o = e ? n.end() : n.pos;
  for (; r > 0 && (e || n.indexAfter(r) == n.node(r).childCount); )
    r--, o++, e = !1;
  if (t) {
    let i = n.node(r).maybeChild(n.indexAfter(r));
    for (; i && !i.isLeaf; )
      i = i.firstChild, o++;
  }
  return o;
}
function ab(n, e, t, r, o) {
  let i = n.findDiffStart(e, t);
  if (i == null)
    return null;
  let { a: s, b: l } = n.findDiffEnd(e, t + n.size, t + e.size);
  if (o == "end") {
    let a = Math.max(0, i - Math.min(s, l));
    r -= s + a - i;
  }
  if (s < i && n.size < e.size) {
    let a = r <= i && r >= s ? i - r : 0;
    i -= a, i && i < e.size && Zc(e.textBetween(i - 1, i + 1)) && (i += a ? 1 : -1), l = i + (l - s), s = i;
  } else if (l < i) {
    let a = r <= i && r >= l ? i - r : 0;
    i -= a, i && i < n.size && Zc(n.textBetween(i - 1, i + 1)) && (i += a ? 1 : -1), s = i + (s - l), l = i;
  }
  return { start: i, endA: s, endB: l };
}
function Zc(n) {
  if (n.length != 2)
    return !1;
  let e = n.charCodeAt(0), t = n.charCodeAt(1);
  return e >= 56320 && e <= 57343 && t >= 55296 && t <= 56319;
}
class Nf {
  /**
  Create a view. `place` may be a DOM node that the editor should
  be appended to, a function that will place it into the document,
  or an object whose `mount` property holds the node to use as the
  document container. If it is `null`, the editor will not be
  added to the document.
  */
  constructor(e, t) {
    this._root = null, this.focused = !1, this.trackWrites = null, this.mounted = !1, this.markCursor = null, this.cursorWrapper = null, this.lastSelectedViewDesc = void 0, this.input = new ky(), this.prevDirectPlugins = [], this.pluginViews = [], this.requiresGeckoHackNode = !1, this.dragging = null, this._props = t, this.state = t.state, this.directPlugins = t.plugins || [], this.directPlugins.forEach(ou), this.dispatch = this.dispatch.bind(this), this.dom = e && e.mount || document.createElement("div"), e && (e.appendChild ? e.appendChild(this.dom) : typeof e == "function" ? e(this.dom) : e.mount && (this.mounted = !0)), this.editable = nu(this), tu(this), this.nodeViews = ru(this), this.docView = Ic(this.state.doc, eu(this), Ts(this), this.dom, this), this.domObserver = new Xy(this, (r, o, i, s) => ib(this, r, o, i, s)), this.domObserver.start(), Ey(this), this.updatePluginViews();
  }
  /**
  Holds `true` when a
  [composition](https://w3c.github.io/uievents/#events-compositionevents)
  is active.
  */
  get composing() {
    return this.input.composing;
  }
  /**
  The view's current [props](https://prosemirror.net/docs/ref/#view.EditorProps).
  */
  get props() {
    if (this._props.state != this.state) {
      let e = this._props;
      this._props = {};
      for (let t in e)
        this._props[t] = e[t];
      this._props.state = this.state;
    }
    return this._props;
  }
  /**
  Update the view's props. Will immediately cause an update to
  the DOM.
  */
  update(e) {
    e.handleDOMEvents != this._props.handleDOMEvents && yl(this);
    let t = this._props;
    this._props = e, e.plugins && (e.plugins.forEach(ou), this.directPlugins = e.plugins), this.updateStateInner(e.state, t);
  }
  /**
  Update the view by updating existing props object with the object
  given as argument. Equivalent to `view.update(Object.assign({},
  view.props, props))`.
  */
  setProps(e) {
    let t = {};
    for (let r in this._props)
      t[r] = this._props[r];
    t.state = this.state;
    for (let r in e)
      t[r] = e[r];
    this.update(t);
  }
  /**
  Update the editor's `state` prop, without touching any of the
  other props.
  */
  updateState(e) {
    this.updateStateInner(e, this._props);
  }
  updateStateInner(e, t) {
    var r;
    let o = this.state, i = !1, s = !1;
    e.storedMarks && this.composing && (xf(this), s = !0), this.state = e;
    let l = o.plugins != e.plugins || this._props.plugins != t.plugins;
    if (l || this._props.plugins != t.plugins || this._props.nodeViews != t.nodeViews) {
      let h = ru(this);
      ub(h, this.nodeViews) && (this.nodeViews = h, i = !0);
    }
    (l || t.handleDOMEvents != this._props.handleDOMEvents) && yl(this), this.editable = nu(this), tu(this);
    let a = Ts(this), c = eu(this), u = o.plugins != e.plugins && !o.doc.eq(e.doc) ? "reset" : e.scrollToSelection > o.scrollToSelection ? "to selection" : "preserve", d = i || !this.docView.matchesNode(e.doc, c, a);
    (d || !e.selection.eq(o.selection)) && (s = !0);
    let f = u == "preserve" && s && this.dom.style.overflowAnchor == null && zg(this);
    if (s) {
      this.domObserver.stop();
      let h = d && (Le || ve) && !this.composing && !o.selection.empty && !e.selection.empty && cb(o.selection, e.selection);
      if (d) {
        let p = ve ? this.trackWrites = this.domSelectionRange().focusNode : null;
        this.composing && (this.input.compositionNode = zy(this)), (i || !this.docView.update(e.doc, c, a, this)) && (this.docView.updateOuterDeco(c), this.docView.destroy(), this.docView = Ic(e.doc, c, a, this.dom, this)), p && (!this.trackWrites || !this.dom.contains(this.trackWrites)) && (h = !0);
      }
      h || !(this.input.mouseDown && this.domObserver.currentSelection.eq(this.domSelectionRange()) && cy(this)) ? kt(this, h) : (df(this, e.selection), this.domObserver.setCurSelection()), this.domObserver.start();
    }
    this.updatePluginViews(o), !((r = this.dragging) === null || r === void 0) && r.node && !o.doc.eq(e.doc) && this.updateDraggedNode(this.dragging, o), u == "reset" ? this.dom.scrollTop = 0 : u == "to selection" ? this.scrollToSelection() : f && $g(f);
  }
  /**
  @internal
  */
  scrollToSelection() {
    let e = this.domSelectionRange().focusNode;
    if (!(!e || !this.dom.contains(e.nodeType == 1 ? e : e.parentNode))) {
      if (!this.someProp("handleScrollToSelection", (t) => t(this))) if (this.state.selection instanceof I) {
        let t = this.docView.domAfterPos(this.state.selection.from);
        t.nodeType == 1 && Ac(this, t.getBoundingClientRect(), e);
      } else
        Ac(this, this.coordsAtPos(this.state.selection.head, 1), e);
    }
  }
  destroyPluginViews() {
    let e;
    for (; e = this.pluginViews.pop(); )
      e.destroy && e.destroy();
  }
  updatePluginViews(e) {
    if (!e || e.plugins != this.state.plugins || this.directPlugins != this.prevDirectPlugins) {
      this.prevDirectPlugins = this.directPlugins, this.destroyPluginViews();
      for (let t = 0; t < this.directPlugins.length; t++) {
        let r = this.directPlugins[t];
        r.spec.view && this.pluginViews.push(r.spec.view(this));
      }
      for (let t = 0; t < this.state.plugins.length; t++) {
        let r = this.state.plugins[t];
        r.spec.view && this.pluginViews.push(r.spec.view(this));
      }
    } else
      for (let t = 0; t < this.pluginViews.length; t++) {
        let r = this.pluginViews[t];
        r.update && r.update(this, e);
      }
  }
  updateDraggedNode(e, t) {
    let r = e.node, o = -1;
    if (r.from < this.state.doc.content.size && this.state.doc.nodeAt(r.from) == r.node)
      o = r.from;
    else {
      let i = r.from + (this.state.doc.content.size - t.doc.content.size);
      (i > 0 && i < this.state.doc.content.size && this.state.doc.nodeAt(i)) == r.node && (o = i);
    }
    this.dragging = new Ef(e.slice, e.move, o < 0 ? void 0 : I.create(this.state.doc, o));
  }
  someProp(e, t) {
    let r = this._props && this._props[e], o;
    if (r != null && (o = t ? t(r) : r))
      return o;
    for (let s = 0; s < this.directPlugins.length; s++) {
      let l = this.directPlugins[s].props[e];
      if (l != null && (o = t ? t(l) : l))
        return o;
    }
    let i = this.state.plugins;
    if (i)
      for (let s = 0; s < i.length; s++) {
        let l = i[s].props[e];
        if (l != null && (o = t ? t(l) : l))
          return o;
      }
  }
  /**
  Query whether the view has focus.
  */
  hasFocus() {
    if (Le) {
      let e = this.root.activeElement;
      if (e == this.dom)
        return !0;
      if (!e || !this.dom.contains(e))
        return !1;
      for (; e && this.dom != e && this.dom.contains(e); ) {
        if (e.contentEditable == "false")
          return !1;
        e = e.parentElement;
      }
      return !0;
    }
    return this.root.activeElement == this.dom;
  }
  /**
  Focus the editor.
  */
  focus() {
    this.domObserver.stop(), this.editable && Hg(this.dom), kt(this), this.domObserver.start();
  }
  /**
  Get the document root in which the editor exists. This will
  usually be the top-level `document`, but might be a [shadow
  DOM](https://developer.mozilla.org/en-US/docs/Web/Web_Components/Shadow_DOM)
  root if the editor is inside one.
  */
  get root() {
    let e = this._root;
    if (e == null) {
      for (let t = this.dom.parentNode; t; t = t.parentNode)
        if (t.nodeType == 9 || t.nodeType == 11 && t.host)
          return t.getSelection || (Object.getPrototypeOf(t).getSelection = () => t.ownerDocument.getSelection()), this._root = t;
    }
    return e || document;
  }
  /**
  When an existing editor view is moved to a new document or
  shadow tree, call this to make it recompute its root.
  */
  updateRoot() {
    this._root = null;
  }
  /**
  Given a pair of viewport coordinates, return the document
  position that corresponds to them. May return null if the given
  coordinates aren't inside of the editor. When an object is
  returned, its `pos` property is the position nearest to the
  coordinates, and its `inside` property holds the position of the
  inner node that the position falls inside of, or -1 if it is at
  the top level, not in any node.
  */
  posAtCoords(e) {
    return Kg(this, e);
  }
  /**
  Returns the viewport rectangle at a given document position.
  `left` and `right` will be the same number, as this returns a
  flat cursor-ish rectangle. If the position is between two things
  that aren't directly adjacent, `side` determines which element
  is used. When < 0, the element before the position is used,
  otherwise the element after.
  */
  coordsAtPos(e, t = 1) {
    return nf(this, e, t);
  }
  /**
  Find the DOM position that corresponds to the given document
  position. When `side` is negative, find the position as close as
  possible to the content before the position. When positive,
  prefer positions close to the content after the position. When
  zero, prefer as shallow a position as possible.
  
  Note that you should **not** mutate the editor's internal DOM,
  only inspect it (and even that is usually not necessary).
  */
  domAtPos(e, t = 0) {
    return this.docView.domFromPos(e, t);
  }
  /**
  Find the DOM node that represents the document node after the
  given position. May return `null` when the position doesn't point
  in front of a node or if the node is inside an opaque node view.
  
  This is intended to be able to call things like
  `getBoundingClientRect` on that DOM node. Do **not** mutate the
  editor DOM directly, or add styling this way, since that will be
  immediately overriden by the editor as it redraws the node.
  */
  nodeDOM(e) {
    let t = this.docView.descAt(e);
    return t ? t.nodeDOM : null;
  }
  /**
  Find the document position that corresponds to a given DOM
  position. (Whenever possible, it is preferable to inspect the
  document structure directly, rather than poking around in the
  DOM, but sometimes—for example when interpreting an event
  target—you don't have a choice.)
  
  The `bias` parameter can be used to influence which side of a DOM
  node to use when the position is inside a leaf node.
  */
  posAtDOM(e, t, r = -1) {
    let o = this.docView.posFromDOM(e, t, r);
    if (o == null)
      throw new RangeError("DOM position not inside the editor");
    return o;
  }
  /**
  Find out whether the selection is at the end of a textblock when
  moving in a given direction. When, for example, given `"left"`,
  it will return true if moving left from the current cursor
  position would leave that position's parent textblock. Will apply
  to the view's current state by default, but it is possible to
  pass a different state.
  */
  endOfTextblock(e, t) {
    return Xg(this, t || this.state, e);
  }
  /**
  Run the editor's paste logic with the given HTML string. The
  `event`, if given, will be passed to the
  [`handlePaste`](https://prosemirror.net/docs/ref/#view.EditorProps.handlePaste) hook.
  */
  pasteHTML(e, t) {
    return Pr(this, "", e, !1, t || new ClipboardEvent("paste"));
  }
  /**
  Run the editor's paste logic with the given plain-text input.
  */
  pasteText(e, t) {
    return Pr(this, e, null, !0, t || new ClipboardEvent("paste"));
  }
  /**
  Serialize the given slice as it would be if it was copied from
  this editor. Returns a DOM element that contains a
  representation of the slice as its children, a textual
  representation, and the transformed slice (which can be
  different from the given input due to hooks like
  [`transformCopied`](https://prosemirror.net/docs/ref/#view.EditorProps.transformCopied)).
  */
  serializeForClipboard(e) {
    return na(this, e);
  }
  /**
  Removes the editor from the DOM and destroys all [node
  views](https://prosemirror.net/docs/ref/#view.NodeView).
  */
  destroy() {
    this.docView && (My(this), this.destroyPluginViews(), this.mounted ? (this.docView.update(this.state.doc, [], Ts(this), this), this.dom.textContent = "") : this.dom.parentNode && this.dom.parentNode.removeChild(this.dom), this.docView.destroy(), this.docView = null, Og());
  }
  /**
  This is true when the view has been
  [destroyed](https://prosemirror.net/docs/ref/#view.EditorView.destroy) (and thus should not be
  used anymore).
  */
  get isDestroyed() {
    return this.docView == null;
  }
  /**
  Used for testing.
  */
  dispatchEvent(e) {
    return Ay(this, e);
  }
  /**
  @internal
  */
  domSelectionRange() {
    let e = this.domSelection();
    return e ? Se && this.root.nodeType === 11 && Ig(this.dom.ownerDocument) == this.dom && Zy(this, e) || e : { focusNode: null, focusOffset: 0, anchorNode: null, anchorOffset: 0 };
  }
  /**
  @internal
  */
  domSelection() {
    return this.root.getSelection();
  }
}
Nf.prototype.dispatch = function(n) {
  let e = this._props.dispatchTransaction;
  e ? e.call(this, n) : this.updateState(this.state.apply(n));
};
function eu(n) {
  let e = /* @__PURE__ */ Object.create(null);
  return e.class = "ProseMirror", e.contenteditable = String(n.editable), n.someProp("attributes", (t) => {
    if (typeof t == "function" && (t = t(n.state)), t)
      for (let r in t)
        r == "class" ? e.class += " " + t[r] : r == "style" ? e.style = (e.style ? e.style + ";" : "") + t[r] : !e[r] && r != "contenteditable" && r != "nodeName" && (e[r] = String(t[r]));
  }), e.translate || (e.translate = "no"), [Ne.node(0, n.state.doc.content.size, e)];
}
function tu(n) {
  if (n.markCursor) {
    let e = document.createElement("img");
    e.className = "ProseMirror-separator", e.setAttribute("mark-placeholder", "true"), e.setAttribute("alt", ""), n.cursorWrapper = { dom: e, deco: Ne.widget(n.state.selection.from, e, { raw: !0, marks: n.markCursor }) };
  } else
    n.cursorWrapper = null;
}
function nu(n) {
  return !n.someProp("editable", (e) => e(n.state) === !1);
}
function cb(n, e) {
  let t = Math.min(n.$anchor.sharedDepth(n.head), e.$anchor.sharedDepth(e.head));
  return n.$anchor.start(t) != e.$anchor.start(t);
}
function ru(n) {
  let e = /* @__PURE__ */ Object.create(null);
  function t(r) {
    for (let o in r)
      Object.prototype.hasOwnProperty.call(e, o) || (e[o] = r[o]);
  }
  return n.someProp("nodeViews", t), n.someProp("markViews", t), e;
}
function ub(n, e) {
  let t = 0, r = 0;
  for (let o in n) {
    if (n[o] != e[o])
      return !0;
    t++;
  }
  for (let o in e)
    r++;
  return t != r;
}
function ou(n) {
  if (n.spec.state || n.spec.filterTransaction || n.spec.appendTransaction)
    throw new RangeError("Plugins passed directly to the view must not have a state component");
}
var Jt = {
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
}, Jo = {
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
}, db = typeof navigator < "u" && /Mac/.test(navigator.platform), fb = typeof navigator < "u" && /MSIE \d|Trident\/(?:[7-9]|\d{2,})\..*rv:(\d+)/.exec(navigator.userAgent);
for (var we = 0; we < 10; we++) Jt[48 + we] = Jt[96 + we] = String(we);
for (var we = 1; we <= 24; we++) Jt[we + 111] = "F" + we;
for (var we = 65; we <= 90; we++)
  Jt[we] = String.fromCharCode(we + 32), Jo[we] = String.fromCharCode(we);
for (var Os in Jt) Jo.hasOwnProperty(Os) || (Jo[Os] = Jt[Os]);
function hb(n) {
  var e = db && n.metaKey && n.shiftKey && !n.ctrlKey && !n.altKey || fb && n.shiftKey && n.key && n.key.length == 1 || n.key == "Unidentified", t = !e && n.key || (n.shiftKey ? Jo : Jt)[n.keyCode] || n.key || "Unidentified";
  return t == "Esc" && (t = "Escape"), t == "Del" && (t = "Delete"), t == "Left" && (t = "ArrowLeft"), t == "Up" && (t = "ArrowUp"), t == "Right" && (t = "ArrowRight"), t == "Down" && (t = "ArrowDown"), t;
}
const pb = typeof navigator < "u" && /Mac|iP(hone|[oa]d)/.test(navigator.platform), mb = typeof navigator < "u" && /Win/.test(navigator.platform);
function gb(n) {
  let e = n.split(/-(?!$)/), t = e[e.length - 1];
  t == "Space" && (t = " ");
  let r, o, i, s;
  for (let l = 0; l < e.length - 1; l++) {
    let a = e[l];
    if (/^(cmd|meta|m)$/i.test(a))
      s = !0;
    else if (/^a(lt)?$/i.test(a))
      r = !0;
    else if (/^(c|ctrl|control)$/i.test(a))
      o = !0;
    else if (/^s(hift)?$/i.test(a))
      i = !0;
    else if (/^mod$/i.test(a))
      pb ? s = !0 : o = !0;
    else
      throw new Error("Unrecognized modifier name: " + a);
  }
  return r && (t = "Alt-" + t), o && (t = "Ctrl-" + t), s && (t = "Meta-" + t), i && (t = "Shift-" + t), t;
}
function yb(n) {
  let e = /* @__PURE__ */ Object.create(null);
  for (let t in n)
    e[gb(t)] = n[t];
  return e;
}
function Ns(n, e, t = !0) {
  return e.altKey && (n = "Alt-" + n), e.ctrlKey && (n = "Ctrl-" + n), e.metaKey && (n = "Meta-" + n), t && e.shiftKey && (n = "Shift-" + n), n;
}
function bb(n) {
  return new ue({ props: { handleKeyDown: aa(n) } });
}
function aa(n) {
  let e = yb(n);
  return function(t, r) {
    let o = hb(r), i, s = e[Ns(o, r)];
    if (s && s(t.state, t.dispatch, t))
      return !0;
    if (o.length == 1 && o != " ") {
      if (r.shiftKey) {
        let l = e[Ns(o, r, !1)];
        if (l && l(t.state, t.dispatch, t))
          return !0;
      }
      if ((r.altKey || r.metaKey || r.ctrlKey) && // Ctrl-Alt may be used for AltGr on Windows
      !(mb && r.ctrlKey && r.altKey) && (i = Jt[r.keyCode]) && i != o) {
        let l = e[Ns(i, r)];
        if (l && l(t.state, t.dispatch, t))
          return !0;
      }
    }
    return !1;
  };
}
const ca = (n, e) => n.selection.empty ? !1 : (e && e(n.tr.deleteSelection().scrollIntoView()), !0);
function Rf(n, e) {
  let { $cursor: t } = n.selection;
  return !t || (e ? !e.endOfTextblock("backward", n) : t.parentOffset > 0) ? null : t;
}
const Df = (n, e, t) => {
  let r = Rf(n, t);
  if (!r)
    return !1;
  let o = ua(r);
  if (!o) {
    let s = r.blockRange(), l = s && er(s);
    return l == null ? !1 : (e && e(n.tr.lift(s, l).scrollIntoView()), !0);
  }
  let i = o.nodeBefore;
  if (Hf(n, o, e, -1))
    return !0;
  if (r.parent.content.size == 0 && (Jn(i, "end") || I.isSelectable(i)))
    for (let s = r.depth; ; s--) {
      let l = _i(n.doc, r.before(s), r.after(s), O.empty);
      if (l && l.slice.size < l.to - l.from) {
        if (e) {
          let a = n.tr.step(l);
          a.setSelection(Jn(i, "end") ? _.findFrom(a.doc.resolve(a.mapping.map(o.pos, -1)), -1) : I.create(a.doc, o.pos - i.nodeSize)), e(a.scrollIntoView());
        }
        return !0;
      }
      if (s == 1 || r.node(s - 1).childCount > 1)
        break;
    }
  return i.isAtom && o.depth == r.depth - 1 ? (e && e(n.tr.delete(o.pos - i.nodeSize, o.pos).scrollIntoView()), !0) : !1;
}, wb = (n, e, t) => {
  let r = Rf(n, t);
  if (!r)
    return !1;
  let o = ua(r);
  return o ? Pf(n, o, e) : !1;
}, vb = (n, e, t) => {
  let r = Lf(n, t);
  if (!r)
    return !1;
  let o = da(r);
  return o ? Pf(n, o, e) : !1;
};
function Pf(n, e, t) {
  let r = e.nodeBefore, o = r, i = e.pos - 1;
  for (; !o.isTextblock; i--) {
    if (o.type.spec.isolating)
      return !1;
    let u = o.lastChild;
    if (!u)
      return !1;
    o = u;
  }
  let s = e.nodeAfter, l = s, a = e.pos + 1;
  for (; !l.isTextblock; a++) {
    if (l.type.spec.isolating)
      return !1;
    let u = l.firstChild;
    if (!u)
      return !1;
    l = u;
  }
  let c = _i(n.doc, i, a, O.empty);
  if (!c || c.from != i || c instanceof fe && c.slice.size >= a - i)
    return !1;
  if (t) {
    let u = n.tr.step(c);
    u.setSelection(L.create(u.doc, i)), t(u.scrollIntoView());
  }
  return !0;
}
function Jn(n, e, t = !1) {
  for (let r = n; r; r = e == "start" ? r.firstChild : r.lastChild) {
    if (r.isTextblock)
      return !0;
    if (t && r.childCount != 1)
      return !1;
  }
  return !1;
}
const If = (n, e, t) => {
  let { $head: r, empty: o } = n.selection, i = r;
  if (!o)
    return !1;
  if (r.parent.isTextblock) {
    if (t ? !t.endOfTextblock("backward", n) : r.parentOffset > 0)
      return !1;
    i = ua(r);
  }
  let s = i && i.nodeBefore;
  return !s || !I.isSelectable(s) ? !1 : (e && e(n.tr.setSelection(I.create(n.doc, i.pos - s.nodeSize)).scrollIntoView()), !0);
};
function ua(n) {
  if (!n.parent.type.spec.isolating)
    for (let e = n.depth - 1; e >= 0; e--) {
      if (n.index(e) > 0)
        return n.doc.resolve(n.before(e + 1));
      if (n.node(e).type.spec.isolating)
        break;
    }
  return null;
}
function Lf(n, e) {
  let { $cursor: t } = n.selection;
  return !t || (e ? !e.endOfTextblock("forward", n) : t.parentOffset < t.parent.content.size) ? null : t;
}
const _f = (n, e, t) => {
  let r = Lf(n, t);
  if (!r)
    return !1;
  let o = da(r);
  if (!o)
    return !1;
  let i = o.nodeAfter;
  if (Hf(n, o, e, 1))
    return !0;
  if (r.parent.content.size == 0 && (Jn(i, "start") || I.isSelectable(i))) {
    let s = _i(n.doc, r.before(), r.after(), O.empty);
    if (s && s.slice.size < s.to - s.from) {
      if (e) {
        let l = n.tr.step(s);
        l.setSelection(Jn(i, "start") ? _.findFrom(l.doc.resolve(l.mapping.map(o.pos)), 1) : I.create(l.doc, l.mapping.map(o.pos))), e(l.scrollIntoView());
      }
      return !0;
    }
  }
  return i.isAtom && o.depth == r.depth - 1 ? (e && e(n.tr.delete(o.pos, o.pos + i.nodeSize).scrollIntoView()), !0) : !1;
}, Bf = (n, e, t) => {
  let { $head: r, empty: o } = n.selection, i = r;
  if (!o)
    return !1;
  if (r.parent.isTextblock) {
    if (t ? !t.endOfTextblock("forward", n) : r.parentOffset < r.parent.content.size)
      return !1;
    i = da(r);
  }
  let s = i && i.nodeAfter;
  return !s || !I.isSelectable(s) ? !1 : (e && e(n.tr.setSelection(I.create(n.doc, i.pos)).scrollIntoView()), !0);
};
function da(n) {
  if (!n.parent.type.spec.isolating)
    for (let e = n.depth - 1; e >= 0; e--) {
      let t = n.node(e);
      if (n.index(e) + 1 < t.childCount)
        return n.doc.resolve(n.after(e + 1));
      if (t.type.spec.isolating)
        break;
    }
  return null;
}
const Cb = (n, e) => {
  let t = n.selection, r = t instanceof I, o;
  if (r) {
    if (t.node.isTextblock || !Zt(n.doc, t.from))
      return !1;
    o = t.from;
  } else if (o = Li(n.doc, t.from, -1), o == null)
    return !1;
  if (e) {
    let i = n.tr.join(o);
    r && i.setSelection(I.create(i.doc, o - n.doc.resolve(o).nodeBefore.nodeSize)), e(i.scrollIntoView());
  }
  return !0;
}, Sb = (n, e) => {
  let t = n.selection, r;
  if (t instanceof I) {
    if (t.node.isTextblock || !Zt(n.doc, t.to))
      return !1;
    r = t.to;
  } else if (r = Li(n.doc, t.to, 1), r == null)
    return !1;
  return e && e(n.tr.join(r).scrollIntoView()), !0;
}, xb = (n, e) => {
  let { $from: t, $to: r } = n.selection, o = t.blockRange(r), i = o && er(o);
  return i == null ? !1 : (e && e(n.tr.lift(o, i).scrollIntoView()), !0);
}, Ff = (n, e) => {
  let { $head: t, $anchor: r } = n.selection;
  return !t.parent.type.spec.code || !t.sameParent(r) ? !1 : (e && e(n.tr.insertText(`
`).scrollIntoView()), !0);
};
function fa(n) {
  for (let e = 0; e < n.edgeCount; e++) {
    let { type: t } = n.edge(e);
    if (t.isTextblock && !t.hasRequiredAttrs())
      return t;
  }
  return null;
}
const kb = (n, e) => {
  let { $head: t, $anchor: r } = n.selection;
  if (!t.parent.type.spec.code || !t.sameParent(r))
    return !1;
  let o = t.node(-1), i = t.indexAfter(-1), s = fa(o.contentMatchAt(i));
  if (!s || !o.canReplaceWith(i, i, s))
    return !1;
  if (e) {
    let l = t.after(), a = n.tr.replaceWith(l, l, s.createAndFill());
    a.setSelection(_.near(a.doc.resolve(l), 1)), e(a.scrollIntoView());
  }
  return !0;
}, zf = (n, e) => {
  let t = n.selection, { $from: r, $to: o } = t;
  if (t instanceof ze || r.parent.inlineContent || o.parent.inlineContent)
    return !1;
  let i = fa(o.parent.contentMatchAt(o.indexAfter()));
  if (!i || !i.isTextblock)
    return !1;
  if (e) {
    let s = (!r.parentOffset && o.index() < o.parent.childCount ? r : o).pos, l = n.tr.insert(s, i.createAndFill());
    l.setSelection(L.create(l.doc, s + 1)), e(l.scrollIntoView());
  }
  return !0;
}, $f = (n, e) => {
  let { $cursor: t } = n.selection;
  if (!t || t.parent.content.size)
    return !1;
  if (t.depth > 1 && t.after() != t.end(-1)) {
    let i = t.before();
    if (xt(n.doc, i))
      return e && e(n.tr.split(i).scrollIntoView()), !0;
  }
  let r = t.blockRange(), o = r && er(r);
  return o == null ? !1 : (e && e(n.tr.lift(r, o).scrollIntoView()), !0);
};
function Eb(n) {
  return (e, t) => {
    let { $from: r, $to: o } = e.selection;
    if (e.selection instanceof I && e.selection.node.isBlock)
      return !r.parentOffset || !xt(e.doc, r.pos) ? !1 : (t && t(e.tr.split(r.pos).scrollIntoView()), !0);
    if (!r.depth)
      return !1;
    let i = [], s, l, a = !1, c = !1;
    for (let h = r.depth; ; h--)
      if (r.node(h).isBlock) {
        a = r.end(h) == r.pos + (r.depth - h), c = r.start(h) == r.pos - (r.depth - h), l = fa(r.node(h - 1).contentMatchAt(r.indexAfter(h - 1))), i.unshift(a && l ? { type: l } : null), s = h;
        break;
      } else {
        if (h == 1)
          return !1;
        i.unshift(null);
      }
    let u = e.tr;
    (e.selection instanceof L || e.selection instanceof ze) && u.deleteSelection();
    let d = u.mapping.map(r.pos), f = xt(u.doc, d, i.length, i);
    if (f || (i[0] = l ? { type: l } : null, f = xt(u.doc, d, i.length, i)), !f)
      return !1;
    if (u.split(d, i.length, i), !a && c && r.node(s).type != l) {
      let h = u.mapping.map(r.before(s)), p = u.doc.resolve(h);
      l && r.node(s - 1).canReplaceWith(p.index(), p.index() + 1, l) && u.setNodeMarkup(u.mapping.map(r.before(s)), l);
    }
    return t && t(u.scrollIntoView()), !0;
  };
}
const Mb = Eb(), Tb = (n, e) => {
  let { $from: t, to: r } = n.selection, o, i = t.sharedDepth(r);
  return i == 0 ? !1 : (o = t.before(i), e && e(n.tr.setSelection(I.create(n.doc, o))), !0);
};
function Ab(n, e, t) {
  let r = e.nodeBefore, o = e.nodeAfter, i = e.index();
  return !r || !o || !r.type.compatibleContent(o.type) ? !1 : !r.content.size && e.parent.canReplace(i - 1, i) ? (t && t(n.tr.delete(e.pos - r.nodeSize, e.pos).scrollIntoView()), !0) : !e.parent.canReplace(i, i + 1) || !(o.isTextblock || Zt(n.doc, e.pos)) ? !1 : (t && t(n.tr.join(e.pos).scrollIntoView()), !0);
}
function Hf(n, e, t, r) {
  let o = e.nodeBefore, i = e.nodeAfter, s, l, a = o.type.spec.isolating || i.type.spec.isolating;
  if (!a && Ab(n, e, t))
    return !0;
  let c = !a && e.parent.canReplace(e.index(), e.index() + 1);
  if (c && (s = (l = o.contentMatchAt(o.childCount)).findWrapping(i.type)) && l.matchType(s[0] || i.type).validEnd) {
    if (t) {
      let h = e.pos + i.nodeSize, p = M.empty;
      for (let y = s.length - 1; y >= 0; y--)
        p = M.from(s[y].create(null, p));
      p = M.from(o.copy(p));
      let m = n.tr.step(new he(e.pos - 1, h, e.pos, h, new O(p, 1, 0), s.length, !0)), g = m.doc.resolve(h + 2 * s.length);
      g.nodeAfter && g.nodeAfter.type == o.type && Zt(m.doc, g.pos) && m.join(g.pos), t(m.scrollIntoView());
    }
    return !0;
  }
  let u = i.type.spec.isolating || r > 0 && a ? null : _.findFrom(e, 1), d = u && u.$from.blockRange(u.$to), f = d && er(d);
  if (f != null && f >= e.depth)
    return t && t(n.tr.lift(d, f).scrollIntoView()), !0;
  if (c && Jn(i, "start", !0) && Jn(o, "end")) {
    let h = o, p = [];
    for (; p.push(h), !h.isTextblock; )
      h = h.lastChild;
    let m = i, g = 1;
    for (; !m.isTextblock; m = m.firstChild)
      g++;
    if (h.canReplace(h.childCount, h.childCount, m.content)) {
      if (t) {
        let y = M.empty;
        for (let w = p.length - 1; w >= 0; w--)
          y = M.from(p[w].copy(y));
        let b = n.tr.step(new he(e.pos - p.length, e.pos + i.nodeSize, e.pos + g, e.pos + i.nodeSize - g, new O(y, p.length, 0), 0, !0));
        t(b.scrollIntoView());
      }
      return !0;
    }
  }
  return !1;
}
function Wf(n) {
  return function(e, t) {
    let r = e.selection, o = n < 0 ? r.$from : r.$to, i = o.depth;
    for (; o.node(i).isInline; ) {
      if (!i)
        return !1;
      i--;
    }
    return o.node(i).isTextblock ? (t && t(e.tr.setSelection(L.create(e.doc, n < 0 ? o.start(i) : o.end(i)))), !0) : !1;
  };
}
const Ob = Wf(-1), Nb = Wf(1);
function Rb(n, e = null) {
  return function(t, r) {
    let { $from: o, $to: i } = t.selection, s = o.blockRange(i), l = s && Yl(s, n, e);
    return l ? (r && r(t.tr.wrap(s, l).scrollIntoView()), !0) : !1;
  };
}
function iu(n, e = null) {
  return function(t, r) {
    let o = !1;
    for (let i = 0; i < t.selection.ranges.length && !o; i++) {
      let { $from: { pos: s }, $to: { pos: l } } = t.selection.ranges[i];
      t.doc.nodesBetween(s, l, (a, c) => {
        if (o)
          return !1;
        if (!(!a.isTextblock || a.hasMarkup(n, e)))
          if (a.type == n)
            o = !0;
          else {
            let u = t.doc.resolve(c), d = u.index();
            o = u.parent.canReplaceWith(d, d + 1, n);
          }
      });
    }
    if (!o)
      return !1;
    if (r) {
      let i = t.tr;
      for (let s = 0; s < t.selection.ranges.length; s++) {
        let { $from: { pos: l }, $to: { pos: a } } = t.selection.ranges[s];
        i.setBlockType(l, a, n, e);
      }
      r(i.scrollIntoView());
    }
    return !0;
  };
}
function ha(...n) {
  return function(e, t, r) {
    for (let o = 0; o < n.length; o++)
      if (n[o](e, t, r))
        return !0;
    return !1;
  };
}
ha(ca, Df, If);
ha(ca, _f, Bf);
ha(Ff, zf, $f, Mb);
typeof navigator < "u" ? /Mac|iP(hone|[oa]d)/.test(navigator.platform) : typeof os < "u" && os.platform && os.platform() == "darwin";
function Db(n, e = null) {
  return function(t, r) {
    let { $from: o, $to: i } = t.selection, s = o.blockRange(i);
    if (!s)
      return !1;
    let l = r ? t.tr : null;
    return Pb(l, s, n, e) ? (r && r(l.scrollIntoView()), !0) : !1;
  };
}
function Pb(n, e, t, r = null) {
  let o = !1, i = e, s = e.$from.doc;
  if (e.depth >= 2 && e.$from.node(e.depth - 1).type.compatibleContent(t) && e.startIndex == 0) {
    if (e.$from.index(e.depth - 1) == 0)
      return !1;
    let a = s.resolve(e.start - 2);
    i = new Wo(a, a, e.depth), e.endIndex < e.parent.childCount && (e = new Wo(e.$from, s.resolve(e.$to.end(e.depth)), e.depth)), o = !0;
  }
  let l = Yl(i, t, r, e);
  return l ? (n && Ib(n, e, l, o, t), !0) : !1;
}
function Ib(n, e, t, r, o) {
  let i = M.empty;
  for (let u = t.length - 1; u >= 0; u--)
    i = M.from(t[u].type.create(t[u].attrs, i));
  n.step(new he(e.start - (r ? 2 : 0), e.end, e.start, e.end, new O(i, 0, 0), t.length, !0));
  let s = 0;
  for (let u = 0; u < t.length; u++)
    t[u].type == o && (s = u + 1);
  let l = t.length - s, a = e.start + t.length - (r ? 2 : 0), c = e.parent;
  for (let u = e.startIndex, d = e.endIndex, f = !0; u < d; u++, f = !1)
    !f && xt(n.doc, a, l) && (n.split(a, l), a += 2 * l), a += c.child(u).nodeSize;
  return n;
}
function Lb(n) {
  return function(e, t) {
    let { $from: r, $to: o } = e.selection, i = r.blockRange(o, (s) => s.childCount > 0 && s.firstChild.type == n);
    return i ? t ? r.node(i.depth - 1).type == n ? _b(e, t, n, i) : Bb(e, t, i) : !0 : !1;
  };
}
function _b(n, e, t, r) {
  let o = n.tr, i = r.end, s = r.$to.end(r.depth);
  i < s && (o.step(new he(i - 1, s, i, s, new O(M.from(t.create(null, r.parent.copy())), 1, 0), 1, !0)), r = new Wo(o.doc.resolve(r.$from.pos), o.doc.resolve(s), r.depth));
  const l = er(r);
  if (l == null)
    return !1;
  o.lift(r, l);
  let a = o.doc.resolve(o.mapping.map(i, -1) - 1);
  return Zt(o.doc, a.pos) && a.nodeBefore.type == a.nodeAfter.type && o.join(a.pos), e(o.scrollIntoView()), !0;
}
function Bb(n, e, t) {
  let r = n.tr, o = t.parent;
  for (let h = t.end, p = t.endIndex - 1, m = t.startIndex; p > m; p--)
    h -= o.child(p).nodeSize, r.delete(h - 1, h + 1);
  let i = r.doc.resolve(t.start), s = i.nodeAfter;
  if (r.mapping.map(t.end) != t.start + i.nodeAfter.nodeSize)
    return !1;
  let l = t.startIndex == 0, a = t.endIndex == o.childCount, c = i.node(-1), u = i.index(-1);
  if (!c.canReplace(u + (l ? 0 : 1), u + 1, s.content.append(a ? M.empty : M.from(o))))
    return !1;
  let d = i.pos, f = d + s.nodeSize;
  return r.step(new he(d - (l ? 1 : 0), f + (a ? 1 : 0), d + 1, f - 1, new O((l ? M.empty : M.from(o.copy(M.empty))).append(a ? M.empty : M.from(o.copy(M.empty))), l ? 0 : 1, a ? 0 : 1), l ? 0 : 1)), e(r.scrollIntoView()), !0;
}
function Fb(n) {
  return function(e, t) {
    let { $from: r, $to: o } = e.selection, i = r.blockRange(o, (c) => c.childCount > 0 && c.firstChild.type == n);
    if (!i)
      return !1;
    let s = i.startIndex;
    if (s == 0)
      return !1;
    let l = i.parent, a = l.child(s - 1);
    if (a.type != n)
      return !1;
    if (t) {
      let c = a.lastChild && a.lastChild.type == l.type, u = M.from(c ? n.create() : null), d = new O(M.from(n.create(null, M.from(l.type.create(null, u)))), c ? 3 : 1, 0), f = i.start, h = i.end;
      t(e.tr.step(new he(f - (c ? 3 : 1), h, f, h, d, 1, !0)).scrollIntoView());
    }
    return !0;
  };
}
function Hi(n) {
  const { state: e, transaction: t } = n;
  let { selection: r } = t, { doc: o } = t, { storedMarks: i } = t;
  return {
    ...e,
    apply: e.apply.bind(e),
    applyTransaction: e.applyTransaction.bind(e),
    plugins: e.plugins,
    schema: e.schema,
    reconfigure: e.reconfigure.bind(e),
    toJSON: e.toJSON.bind(e),
    get storedMarks() {
      return i;
    },
    get selection() {
      return r;
    },
    get doc() {
      return o;
    },
    get tr() {
      return r = t.selection, o = t.doc, i = t.storedMarks, t;
    }
  };
}
class Wi {
  constructor(e) {
    this.editor = e.editor, this.rawCommands = this.editor.extensionManager.commands, this.customState = e.state;
  }
  get hasCustomState() {
    return !!this.customState;
  }
  get state() {
    return this.customState || this.editor.state;
  }
  get commands() {
    const { rawCommands: e, editor: t, state: r } = this, { view: o } = t, { tr: i } = r, s = this.buildProps(i);
    return Object.fromEntries(Object.entries(e).map(([l, a]) => [l, (...u) => {
      const d = a(...u)(s);
      return !i.getMeta("preventDispatch") && !this.hasCustomState && o.dispatch(i), d;
    }]));
  }
  get chain() {
    return () => this.createChain();
  }
  get can() {
    return () => this.createCan();
  }
  createChain(e, t = !0) {
    const { rawCommands: r, editor: o, state: i } = this, { view: s } = o, l = [], a = !!e, c = e || i.tr, u = () => (!a && t && !c.getMeta("preventDispatch") && !this.hasCustomState && s.dispatch(c), l.every((f) => f === !0)), d = {
      ...Object.fromEntries(Object.entries(r).map(([f, h]) => [f, (...m) => {
        const g = this.buildProps(c, t), y = h(...m)(g);
        return l.push(y), d;
      }])),
      run: u
    };
    return d;
  }
  createCan(e) {
    const { rawCommands: t, state: r } = this, o = !1, i = e || r.tr, s = this.buildProps(i, o);
    return {
      ...Object.fromEntries(Object.entries(t).map(([a, c]) => [a, (...u) => c(...u)({ ...s, dispatch: void 0 })])),
      chain: () => this.createChain(i, o)
    };
  }
  buildProps(e, t = !0) {
    const { rawCommands: r, editor: o, state: i } = this, { view: s } = o, l = {
      tr: e,
      editor: o,
      view: s,
      state: Hi({
        state: i,
        transaction: e
      }),
      dispatch: t ? () => {
      } : void 0,
      chain: () => this.createChain(e, t),
      can: () => this.createCan(e),
      get commands() {
        return Object.fromEntries(Object.entries(r).map(([a, c]) => [a, (...u) => c(...u)(l)]));
      }
    };
    return l;
  }
}
class zb {
  constructor() {
    this.callbacks = {};
  }
  on(e, t) {
    return this.callbacks[e] || (this.callbacks[e] = []), this.callbacks[e].push(t), this;
  }
  emit(e, ...t) {
    const r = this.callbacks[e];
    return r && r.forEach((o) => o.apply(this, t)), this;
  }
  off(e, t) {
    const r = this.callbacks[e];
    return r && (t ? this.callbacks[e] = r.filter((o) => o !== t) : delete this.callbacks[e]), this;
  }
  once(e, t) {
    const r = (...o) => {
      this.off(e, r), t.apply(this, o);
    };
    return this.on(e, r);
  }
  removeAllListeners() {
    this.callbacks = {};
  }
}
function R(n, e, t) {
  return n.config[e] === void 0 && n.parent ? R(n.parent, e, t) : typeof n.config[e] == "function" ? n.config[e].bind({
    ...t,
    parent: n.parent ? R(n.parent, e, t) : null
  }) : n.config[e];
}
function Vi(n) {
  const e = n.filter((o) => o.type === "extension"), t = n.filter((o) => o.type === "node"), r = n.filter((o) => o.type === "mark");
  return {
    baseExtensions: e,
    nodeExtensions: t,
    markExtensions: r
  };
}
function Vf(n) {
  const e = [], { nodeExtensions: t, markExtensions: r } = Vi(n), o = [...t, ...r], i = {
    default: null,
    rendered: !0,
    renderHTML: null,
    parseHTML: null,
    keepOnSplit: !0,
    isRequired: !1
  };
  return n.forEach((s) => {
    const l = {
      name: s.name,
      options: s.options,
      storage: s.storage,
      extensions: o
    }, a = R(s, "addGlobalAttributes", l);
    if (!a)
      return;
    a().forEach((u) => {
      u.types.forEach((d) => {
        Object.entries(u.attributes).forEach(([f, h]) => {
          e.push({
            type: d,
            name: f,
            attribute: {
              ...i,
              ...h
            }
          });
        });
      });
    });
  }), o.forEach((s) => {
    const l = {
      name: s.name,
      options: s.options,
      storage: s.storage
    }, a = R(s, "addAttributes", l);
    if (!a)
      return;
    const c = a();
    Object.entries(c).forEach(([u, d]) => {
      const f = {
        ...i,
        ...d
      };
      typeof (f == null ? void 0 : f.default) == "function" && (f.default = f.default()), f != null && f.isRequired && (f == null ? void 0 : f.default) === void 0 && delete f.default, e.push({
        type: s.name,
        name: u,
        attribute: f
      });
    });
  }), e;
}
function me(n, e) {
  if (typeof n == "string") {
    if (!e.nodes[n])
      throw Error(`There is no node type named '${n}'. Maybe you forgot to add the extension?`);
    return e.nodes[n];
  }
  return n;
}
function ne(...n) {
  return n.filter((e) => !!e).reduce((e, t) => {
    const r = { ...e };
    return Object.entries(t).forEach(([o, i]) => {
      if (!r[o]) {
        r[o] = i;
        return;
      }
      if (o === "class") {
        const l = i ? String(i).split(" ") : [], a = r[o] ? r[o].split(" ") : [], c = l.filter((u) => !a.includes(u));
        r[o] = [...a, ...c].join(" ");
      } else if (o === "style") {
        const l = i ? i.split(";").map((u) => u.trim()).filter(Boolean) : [], a = r[o] ? r[o].split(";").map((u) => u.trim()).filter(Boolean) : [], c = /* @__PURE__ */ new Map();
        a.forEach((u) => {
          const [d, f] = u.split(":").map((h) => h.trim());
          c.set(d, f);
        }), l.forEach((u) => {
          const [d, f] = u.split(":").map((h) => h.trim());
          c.set(d, f);
        }), r[o] = Array.from(c.entries()).map(([u, d]) => `${u}: ${d}`).join("; ");
      } else
        r[o] = i;
    }), r;
  }, {});
}
function bl(n, e) {
  return e.filter((t) => t.type === n.type.name).filter((t) => t.attribute.rendered).map((t) => t.attribute.renderHTML ? t.attribute.renderHTML(n.attrs) || {} : {
    [t.name]: n.attrs[t.name]
  }).reduce((t, r) => ne(t, r), {});
}
function jf(n) {
  return typeof n == "function";
}
function W(n, e = void 0, ...t) {
  return jf(n) ? e ? n.bind(e)(...t) : n(...t) : n;
}
function $b(n = {}) {
  return Object.keys(n).length === 0 && n.constructor === Object;
}
function Hb(n) {
  return typeof n != "string" ? n : n.match(/^[+-]?(?:\d*\.)?\d+$/) ? Number(n) : n === "true" ? !0 : n === "false" ? !1 : n;
}
function su(n, e) {
  return "style" in n ? n : {
    ...n,
    getAttrs: (t) => {
      const r = n.getAttrs ? n.getAttrs(t) : n.attrs;
      if (r === !1)
        return !1;
      const o = e.reduce((i, s) => {
        const l = s.attribute.parseHTML ? s.attribute.parseHTML(t) : Hb(t.getAttribute(s.name));
        return l == null ? i : {
          ...i,
          [s.name]: l
        };
      }, {});
      return { ...r, ...o };
    }
  };
}
function lu(n) {
  return Object.fromEntries(
    // @ts-ignore
    Object.entries(n).filter(([e, t]) => e === "attrs" && $b(t) ? !1 : t != null)
  );
}
function Wb(n, e) {
  var t;
  const r = Vf(n), { nodeExtensions: o, markExtensions: i } = Vi(n), s = (t = o.find((c) => R(c, "topNode"))) === null || t === void 0 ? void 0 : t.name, l = Object.fromEntries(o.map((c) => {
    const u = r.filter((y) => y.type === c.name), d = {
      name: c.name,
      options: c.options,
      storage: c.storage,
      editor: e
    }, f = n.reduce((y, b) => {
      const w = R(b, "extendNodeSchema", d);
      return {
        ...y,
        ...w ? w(c) : {}
      };
    }, {}), h = lu({
      ...f,
      content: W(R(c, "content", d)),
      marks: W(R(c, "marks", d)),
      group: W(R(c, "group", d)),
      inline: W(R(c, "inline", d)),
      atom: W(R(c, "atom", d)),
      selectable: W(R(c, "selectable", d)),
      draggable: W(R(c, "draggable", d)),
      code: W(R(c, "code", d)),
      whitespace: W(R(c, "whitespace", d)),
      linebreakReplacement: W(R(c, "linebreakReplacement", d)),
      defining: W(R(c, "defining", d)),
      isolating: W(R(c, "isolating", d)),
      attrs: Object.fromEntries(u.map((y) => {
        var b;
        return [y.name, { default: (b = y == null ? void 0 : y.attribute) === null || b === void 0 ? void 0 : b.default }];
      }))
    }), p = W(R(c, "parseHTML", d));
    p && (h.parseDOM = p.map((y) => su(y, u)));
    const m = R(c, "renderHTML", d);
    m && (h.toDOM = (y) => m({
      node: y,
      HTMLAttributes: bl(y, u)
    }));
    const g = R(c, "renderText", d);
    return g && (h.toText = g), [c.name, h];
  })), a = Object.fromEntries(i.map((c) => {
    const u = r.filter((g) => g.type === c.name), d = {
      name: c.name,
      options: c.options,
      storage: c.storage,
      editor: e
    }, f = n.reduce((g, y) => {
      const b = R(y, "extendMarkSchema", d);
      return {
        ...g,
        ...b ? b(c) : {}
      };
    }, {}), h = lu({
      ...f,
      inclusive: W(R(c, "inclusive", d)),
      excludes: W(R(c, "excludes", d)),
      group: W(R(c, "group", d)),
      spanning: W(R(c, "spanning", d)),
      code: W(R(c, "code", d)),
      attrs: Object.fromEntries(u.map((g) => {
        var y;
        return [g.name, { default: (y = g == null ? void 0 : g.attribute) === null || y === void 0 ? void 0 : y.default }];
      }))
    }), p = W(R(c, "parseHTML", d));
    p && (h.parseDOM = p.map((g) => su(g, u)));
    const m = R(c, "renderHTML", d);
    return m && (h.toDOM = (g) => m({
      mark: g,
      HTMLAttributes: bl(g, u)
    })), [c.name, h];
  }));
  return new Nd({
    topNode: s,
    nodes: l,
    marks: a
  });
}
function Rs(n, e) {
  return e.nodes[n] || e.marks[n] || null;
}
function au(n, e) {
  return Array.isArray(e) ? e.some((t) => (typeof t == "string" ? t : t.name) === n.name) : e;
}
function pa(n, e) {
  const t = Tn.fromSchema(e).serializeFragment(n), o = document.implementation.createHTMLDocument().createElement("div");
  return o.appendChild(t), o.innerHTML;
}
const Vb = (n, e = 500) => {
  let t = "";
  const r = n.parentOffset;
  return n.parent.nodesBetween(Math.max(0, r - e), r, (o, i, s, l) => {
    var a, c;
    const u = ((c = (a = o.type.spec).toText) === null || c === void 0 ? void 0 : c.call(a, {
      node: o,
      pos: i,
      parent: s,
      index: l
    })) || o.textContent || "%leaf%";
    t += o.isAtom && !o.isText ? u : u.slice(0, Math.max(0, r - i));
  }), t;
};
function ma(n) {
  return Object.prototype.toString.call(n) === "[object RegExp]";
}
class ji {
  constructor(e) {
    this.find = e.find, this.handler = e.handler;
  }
}
const jb = (n, e) => {
  if (ma(e))
    return e.exec(n);
  const t = e(n);
  if (!t)
    return null;
  const r = [t.text];
  return r.index = t.index, r.input = n, r.data = t.data, t.replaceWith && (t.text.includes(t.replaceWith) || console.warn('[tiptap warn]: "inputRuleMatch.replaceWith" must be part of "inputRuleMatch.text".'), r.push(t.replaceWith)), r;
};
function co(n) {
  var e;
  const { editor: t, from: r, to: o, text: i, rules: s, plugin: l } = n, { view: a } = t;
  if (a.composing)
    return !1;
  const c = a.state.doc.resolve(r);
  if (
    // check for code node
    c.parent.type.spec.code || !((e = c.nodeBefore || c.nodeAfter) === null || e === void 0) && e.marks.find((f) => f.type.spec.code)
  )
    return !1;
  let u = !1;
  const d = Vb(c) + i;
  return s.forEach((f) => {
    if (u)
      return;
    const h = jb(d, f.find);
    if (!h)
      return;
    const p = a.state.tr, m = Hi({
      state: a.state,
      transaction: p
    }), g = {
      from: r - (h[0].length - i.length),
      to: o
    }, { commands: y, chain: b, can: w } = new Wi({
      editor: t,
      state: m
    });
    f.handler({
      state: m,
      range: g,
      match: h,
      commands: y,
      chain: b,
      can: w
    }) === null || !p.steps.length || (p.setMeta(l, {
      transform: p,
      from: r,
      to: o,
      text: i
    }), a.dispatch(p), u = !0);
  }), u;
}
function Ub(n) {
  const { editor: e, rules: t } = n, r = new ue({
    state: {
      init() {
        return null;
      },
      apply(o, i, s) {
        const l = o.getMeta(r);
        if (l)
          return l;
        const a = o.getMeta("applyInputRules");
        return !!a && setTimeout(() => {
          let { text: u } = a;
          typeof u == "string" ? u = u : u = pa(M.from(u), s.schema);
          const { from: d } = a, f = d + u.length;
          co({
            editor: e,
            from: d,
            to: f,
            text: u,
            rules: t,
            plugin: r
          });
        }), o.selectionSet || o.docChanged ? null : i;
      }
    },
    props: {
      handleTextInput(o, i, s, l) {
        return co({
          editor: e,
          from: i,
          to: s,
          text: l,
          rules: t,
          plugin: r
        });
      },
      handleDOMEvents: {
        compositionend: (o) => (setTimeout(() => {
          const { $cursor: i } = o.state.selection;
          i && co({
            editor: e,
            from: i.pos,
            to: i.pos,
            text: "",
            rules: t,
            plugin: r
          });
        }), !1)
      },
      // add support for input rules to trigger on enter
      // this is useful for example for code blocks
      handleKeyDown(o, i) {
        if (i.key !== "Enter")
          return !1;
        const { $cursor: s } = o.state.selection;
        return s ? co({
          editor: e,
          from: s.pos,
          to: s.pos,
          text: `
`,
          rules: t,
          plugin: r
        }) : !1;
      }
    },
    // @ts-ignore
    isInputRules: !0
  });
  return r;
}
function Kb(n) {
  return Object.prototype.toString.call(n).slice(8, -1);
}
function uo(n) {
  return Kb(n) !== "Object" ? !1 : n.constructor === Object && Object.getPrototypeOf(n) === Object.prototype;
}
function Ui(n, e) {
  const t = { ...n };
  return uo(n) && uo(e) && Object.keys(e).forEach((r) => {
    uo(e[r]) && uo(n[r]) ? t[r] = Ui(n[r], e[r]) : t[r] = e[r];
  }), t;
}
class Qe {
  constructor(e = {}) {
    this.type = "mark", this.name = "mark", this.parent = null, this.child = null, this.config = {
      name: this.name,
      defaultOptions: {}
    }, this.config = {
      ...this.config,
      ...e
    }, this.name = this.config.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${this.name}".`), this.options = this.config.defaultOptions, this.config.addOptions && (this.options = W(R(this, "addOptions", {
      name: this.name
    }))), this.storage = W(R(this, "addStorage", {
      name: this.name,
      options: this.options
    })) || {};
  }
  static create(e = {}) {
    return new Qe(e);
  }
  configure(e = {}) {
    const t = this.extend({
      ...this.config,
      addOptions: () => Ui(this.options, e)
    });
    return t.name = this.name, t.parent = this.parent, t;
  }
  extend(e = {}) {
    const t = new Qe(e);
    return t.parent = this, this.child = t, t.name = e.name ? e.name : t.parent.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${t.name}".`), t.options = W(R(t, "addOptions", {
      name: t.name
    })), t.storage = W(R(t, "addStorage", {
      name: t.name,
      options: t.options
    })), t;
  }
  static handleExit({ editor: e, mark: t }) {
    const { tr: r } = e.state, o = e.state.selection.$from;
    if (o.pos === o.end()) {
      const s = o.marks();
      if (!!!s.find((c) => (c == null ? void 0 : c.type.name) === t.name))
        return !1;
      const a = s.find((c) => (c == null ? void 0 : c.type.name) === t.name);
      return a && r.removeStoredMark(a), r.insertText(" ", o.pos), e.view.dispatch(r), !0;
    }
    return !1;
  }
}
function qb(n) {
  return typeof n == "number";
}
class Gb {
  constructor(e) {
    this.find = e.find, this.handler = e.handler;
  }
}
const Jb = (n, e, t) => {
  if (ma(e))
    return [...n.matchAll(e)];
  const r = e(n, t);
  return r ? r.map((o) => {
    const i = [o.text];
    return i.index = o.index, i.input = n, i.data = o.data, o.replaceWith && (o.text.includes(o.replaceWith) || console.warn('[tiptap warn]: "pasteRuleMatch.replaceWith" must be part of "pasteRuleMatch.text".'), i.push(o.replaceWith)), i;
  }) : [];
};
function Yb(n) {
  const { editor: e, state: t, from: r, to: o, rule: i, pasteEvent: s, dropEvent: l } = n, { commands: a, chain: c, can: u } = new Wi({
    editor: e,
    state: t
  }), d = [];
  return t.doc.nodesBetween(r, o, (h, p) => {
    if (!h.isTextblock || h.type.spec.code)
      return;
    const m = Math.max(r, p), g = Math.min(o, p + h.content.size), y = h.textBetween(m - p, g - p, void 0, "￼");
    Jb(y, i.find, s).forEach((w) => {
      if (w.index === void 0)
        return;
      const v = m + w.index + 1, k = v + w[0].length, E = {
        from: t.tr.mapping.map(v),
        to: t.tr.mapping.map(k)
      }, C = i.handler({
        state: t,
        range: E,
        match: w,
        commands: a,
        chain: c,
        can: u,
        pasteEvent: s,
        dropEvent: l
      });
      d.push(C);
    });
  }), d.every((h) => h !== null);
}
let fo = null;
const Xb = (n) => {
  var e;
  const t = new ClipboardEvent("paste", {
    clipboardData: new DataTransfer()
  });
  return (e = t.clipboardData) === null || e === void 0 || e.setData("text/html", n), t;
};
function Qb(n) {
  const { editor: e, rules: t } = n;
  let r = null, o = !1, i = !1, s = typeof ClipboardEvent < "u" ? new ClipboardEvent("paste") : null, l;
  try {
    l = typeof DragEvent < "u" ? new DragEvent("drop") : null;
  } catch {
    l = null;
  }
  const a = ({ state: u, from: d, to: f, rule: h, pasteEvt: p }) => {
    const m = u.tr, g = Hi({
      state: u,
      transaction: m
    });
    if (!(!Yb({
      editor: e,
      state: g,
      from: Math.max(d - 1, 0),
      to: f.b - 1,
      rule: h,
      pasteEvent: p,
      dropEvent: l
    }) || !m.steps.length)) {
      try {
        l = typeof DragEvent < "u" ? new DragEvent("drop") : null;
      } catch {
        l = null;
      }
      return s = typeof ClipboardEvent < "u" ? new ClipboardEvent("paste") : null, m;
    }
  };
  return t.map((u) => new ue({
    // we register a global drag handler to track the current drag source element
    view(d) {
      const f = (p) => {
        var m;
        r = !((m = d.dom.parentElement) === null || m === void 0) && m.contains(p.target) ? d.dom.parentElement : null, r && (fo = e);
      }, h = () => {
        fo && (fo = null);
      };
      return window.addEventListener("dragstart", f), window.addEventListener("dragend", h), {
        destroy() {
          window.removeEventListener("dragstart", f), window.removeEventListener("dragend", h);
        }
      };
    },
    props: {
      handleDOMEvents: {
        drop: (d, f) => {
          if (i = r === d.dom.parentElement, l = f, !i) {
            const h = fo;
            h != null && h.isEditable && setTimeout(() => {
              const p = h.state.selection;
              p && h.commands.deleteRange({ from: p.from, to: p.to });
            }, 10);
          }
          return !1;
        },
        paste: (d, f) => {
          var h;
          const p = (h = f.clipboardData) === null || h === void 0 ? void 0 : h.getData("text/html");
          return s = f, o = !!(p != null && p.includes("data-pm-slice")), !1;
        }
      }
    },
    appendTransaction: (d, f, h) => {
      const p = d[0], m = p.getMeta("uiEvent") === "paste" && !o, g = p.getMeta("uiEvent") === "drop" && !i, y = p.getMeta("applyPasteRules"), b = !!y;
      if (!m && !g && !b)
        return;
      if (b) {
        let { text: k } = y;
        typeof k == "string" ? k = k : k = pa(M.from(k), h.schema);
        const { from: E } = y, C = E + k.length, A = Xb(k);
        return a({
          rule: u,
          state: h,
          from: E,
          to: { b: C },
          pasteEvt: A
        });
      }
      const w = f.doc.content.findDiffStart(h.doc.content), v = f.doc.content.findDiffEnd(h.doc.content);
      if (!(!qb(w) || !v || w === v.b))
        return a({
          rule: u,
          state: h,
          from: w,
          to: v,
          pasteEvt: s
        });
    }
  }));
}
function Zb(n) {
  const e = n.filter((t, r) => n.indexOf(t) !== r);
  return Array.from(new Set(e));
}
class Hn {
  constructor(e, t) {
    this.splittableMarks = [], this.editor = t, this.extensions = Hn.resolve(e), this.schema = Wb(this.extensions, t), this.setupExtensions();
  }
  /**
   * Returns a flattened and sorted extension list while
   * also checking for duplicated extensions and warns the user.
   * @param extensions An array of Tiptap extensions
   * @returns An flattened and sorted array of Tiptap extensions
   */
  static resolve(e) {
    const t = Hn.sort(Hn.flatten(e)), r = Zb(t.map((o) => o.name));
    return r.length && console.warn(`[tiptap warn]: Duplicate extension names found: [${r.map((o) => `'${o}'`).join(", ")}]. This can lead to issues.`), t;
  }
  /**
   * Create a flattened array of extensions by traversing the `addExtensions` field.
   * @param extensions An array of Tiptap extensions
   * @returns A flattened array of Tiptap extensions
   */
  static flatten(e) {
    return e.map((t) => {
      const r = {
        name: t.name,
        options: t.options,
        storage: t.storage
      }, o = R(t, "addExtensions", r);
      return o ? [t, ...this.flatten(o())] : t;
    }).flat(10);
  }
  /**
   * Sort extensions by priority.
   * @param extensions An array of Tiptap extensions
   * @returns A sorted array of Tiptap extensions by priority
   */
  static sort(e) {
    return e.sort((r, o) => {
      const i = R(r, "priority") || 100, s = R(o, "priority") || 100;
      return i > s ? -1 : i < s ? 1 : 0;
    });
  }
  /**
   * Get all commands from the extensions.
   * @returns An object with all commands where the key is the command name and the value is the command function
   */
  get commands() {
    return this.extensions.reduce((e, t) => {
      const r = {
        name: t.name,
        options: t.options,
        storage: t.storage,
        editor: this.editor,
        type: Rs(t.name, this.schema)
      }, o = R(t, "addCommands", r);
      return o ? {
        ...e,
        ...o()
      } : e;
    }, {});
  }
  /**
   * Get all registered Prosemirror plugins from the extensions.
   * @returns An array of Prosemirror plugins
   */
  get plugins() {
    const { editor: e } = this, t = Hn.sort([...this.extensions].reverse()), r = [], o = [], i = t.map((s) => {
      const l = {
        name: s.name,
        options: s.options,
        storage: s.storage,
        editor: e,
        type: Rs(s.name, this.schema)
      }, a = [], c = R(s, "addKeyboardShortcuts", l);
      let u = {};
      if (s.type === "mark" && R(s, "exitable", l) && (u.ArrowRight = () => Qe.handleExit({ editor: e, mark: s })), c) {
        const m = Object.fromEntries(Object.entries(c()).map(([g, y]) => [g, () => y({ editor: e })]));
        u = { ...u, ...m };
      }
      const d = bb(u);
      a.push(d);
      const f = R(s, "addInputRules", l);
      au(s, e.options.enableInputRules) && f && r.push(...f());
      const h = R(s, "addPasteRules", l);
      au(s, e.options.enablePasteRules) && h && o.push(...h());
      const p = R(s, "addProseMirrorPlugins", l);
      if (p) {
        const m = p();
        a.push(...m);
      }
      return a;
    }).flat();
    return [
      Ub({
        editor: e,
        rules: r
      }),
      ...Qb({
        editor: e,
        rules: o
      }),
      ...i
    ];
  }
  /**
   * Get all attributes from the extensions.
   * @returns An array of attributes
   */
  get attributes() {
    return Vf(this.extensions);
  }
  /**
   * Get all node views from the extensions.
   * @returns An object with all node views where the key is the node name and the value is the node view function
   */
  get nodeViews() {
    const { editor: e } = this, { nodeExtensions: t } = Vi(this.extensions);
    return Object.fromEntries(t.filter((r) => !!R(r, "addNodeView")).map((r) => {
      const o = this.attributes.filter((a) => a.type === r.name), i = {
        name: r.name,
        options: r.options,
        storage: r.storage,
        editor: e,
        type: me(r.name, this.schema)
      }, s = R(r, "addNodeView", i);
      if (!s)
        return [];
      const l = (a, c, u, d, f) => {
        const h = bl(a, o);
        return s()({
          // pass-through
          node: a,
          view: c,
          getPos: u,
          decorations: d,
          innerDecorations: f,
          // tiptap-specific
          editor: e,
          extension: r,
          HTMLAttributes: h
        });
      };
      return [r.name, l];
    }));
  }
  /**
   * Go through all extensions, create extension storages & setup marks
   * & bind editor event listener.
   */
  setupExtensions() {
    this.extensions.forEach((e) => {
      var t;
      this.editor.extensionStorage[e.name] = e.storage;
      const r = {
        name: e.name,
        options: e.options,
        storage: e.storage,
        editor: this.editor,
        type: Rs(e.name, this.schema)
      };
      e.type === "mark" && (!((t = W(R(e, "keepOnSplit", r))) !== null && t !== void 0) || t) && this.splittableMarks.push(e.name);
      const o = R(e, "onBeforeCreate", r), i = R(e, "onCreate", r), s = R(e, "onUpdate", r), l = R(e, "onSelectionUpdate", r), a = R(e, "onTransaction", r), c = R(e, "onFocus", r), u = R(e, "onBlur", r), d = R(e, "onDestroy", r);
      o && this.editor.on("beforeCreate", o), i && this.editor.on("create", i), s && this.editor.on("update", s), l && this.editor.on("selectionUpdate", l), a && this.editor.on("transaction", a), c && this.editor.on("focus", c), u && this.editor.on("blur", u), d && this.editor.on("destroy", d);
    });
  }
}
class xe {
  constructor(e = {}) {
    this.type = "extension", this.name = "extension", this.parent = null, this.child = null, this.config = {
      name: this.name,
      defaultOptions: {}
    }, this.config = {
      ...this.config,
      ...e
    }, this.name = this.config.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${this.name}".`), this.options = this.config.defaultOptions, this.config.addOptions && (this.options = W(R(this, "addOptions", {
      name: this.name
    }))), this.storage = W(R(this, "addStorage", {
      name: this.name,
      options: this.options
    })) || {};
  }
  static create(e = {}) {
    return new xe(e);
  }
  configure(e = {}) {
    const t = this.extend({
      ...this.config,
      addOptions: () => Ui(this.options, e)
    });
    return t.name = this.name, t.parent = this.parent, t;
  }
  extend(e = {}) {
    const t = new xe({ ...this.config, ...e });
    return t.parent = this, this.child = t, t.name = e.name ? e.name : t.parent.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${t.name}".`), t.options = W(R(t, "addOptions", {
      name: t.name
    })), t.storage = W(R(t, "addStorage", {
      name: t.name,
      options: t.options
    })), t;
  }
}
function Uf(n, e, t) {
  const { from: r, to: o } = e, { blockSeparator: i = `

`, textSerializers: s = {} } = t || {};
  let l = "";
  return n.nodesBetween(r, o, (a, c, u, d) => {
    var f;
    a.isBlock && c > r && (l += i);
    const h = s == null ? void 0 : s[a.type.name];
    if (h)
      return u && (l += h({
        node: a,
        pos: c,
        parent: u,
        index: d,
        range: e
      })), !1;
    a.isText && (l += (f = a == null ? void 0 : a.text) === null || f === void 0 ? void 0 : f.slice(Math.max(r, c) - c, o - c));
  }), l;
}
function Kf(n) {
  return Object.fromEntries(Object.entries(n.nodes).filter(([, e]) => e.spec.toText).map(([e, t]) => [e, t.spec.toText]));
}
const e0 = xe.create({
  name: "clipboardTextSerializer",
  addOptions() {
    return {
      blockSeparator: void 0
    };
  },
  addProseMirrorPlugins() {
    return [
      new ue({
        key: new Me("clipboardTextSerializer"),
        props: {
          clipboardTextSerializer: () => {
            const { editor: n } = this, { state: e, schema: t } = n, { doc: r, selection: o } = e, { ranges: i } = o, s = Math.min(...i.map((u) => u.$from.pos)), l = Math.max(...i.map((u) => u.$to.pos)), a = Kf(t);
            return Uf(r, { from: s, to: l }, {
              ...this.options.blockSeparator !== void 0 ? { blockSeparator: this.options.blockSeparator } : {},
              textSerializers: a
            });
          }
        }
      })
    ];
  }
}), t0 = () => ({ editor: n, view: e }) => (requestAnimationFrame(() => {
  var t;
  n.isDestroyed || (e.dom.blur(), (t = window == null ? void 0 : window.getSelection()) === null || t === void 0 || t.removeAllRanges());
}), !0), n0 = (n = !1) => ({ commands: e }) => e.setContent("", n), r0 = () => ({ state: n, tr: e, dispatch: t }) => {
  const { selection: r } = e, { ranges: o } = r;
  return t && o.forEach(({ $from: i, $to: s }) => {
    n.doc.nodesBetween(i.pos, s.pos, (l, a) => {
      if (l.type.isText)
        return;
      const { doc: c, mapping: u } = e, d = c.resolve(u.map(a)), f = c.resolve(u.map(a + l.nodeSize)), h = d.blockRange(f);
      if (!h)
        return;
      const p = er(h);
      if (l.type.isTextblock) {
        const { defaultType: m } = d.parent.contentMatchAt(d.index());
        e.setNodeMarkup(h.start, m);
      }
      (p || p === 0) && e.lift(h, p);
    });
  }), !0;
}, o0 = (n) => (e) => n(e), i0 = () => ({ state: n, dispatch: e }) => zf(n, e), s0 = (n, e) => ({ editor: t, tr: r }) => {
  const { state: o } = t, i = o.doc.slice(n.from, n.to);
  r.deleteRange(n.from, n.to);
  const s = r.mapping.map(e);
  return r.insert(s, i.content), r.setSelection(new L(r.doc.resolve(Math.max(s - 1, 0)))), !0;
}, l0 = () => ({ tr: n, dispatch: e }) => {
  const { selection: t } = n, r = t.$anchor.node();
  if (r.content.size > 0)
    return !1;
  const o = n.selection.$anchor;
  for (let i = o.depth; i > 0; i -= 1)
    if (o.node(i).type === r.type) {
      if (e) {
        const l = o.before(i), a = o.after(i);
        n.delete(l, a).scrollIntoView();
      }
      return !0;
    }
  return !1;
}, a0 = (n) => ({ tr: e, state: t, dispatch: r }) => {
  const o = me(n, t.schema), i = e.selection.$anchor;
  for (let s = i.depth; s > 0; s -= 1)
    if (i.node(s).type === o) {
      if (r) {
        const a = i.before(s), c = i.after(s);
        e.delete(a, c).scrollIntoView();
      }
      return !0;
    }
  return !1;
}, c0 = (n) => ({ tr: e, dispatch: t }) => {
  const { from: r, to: o } = n;
  return t && e.delete(r, o), !0;
}, u0 = () => ({ state: n, dispatch: e }) => ca(n, e), d0 = () => ({ commands: n }) => n.keyboardShortcut("Enter"), f0 = () => ({ state: n, dispatch: e }) => kb(n, e);
function Yo(n, e, t = { strict: !0 }) {
  const r = Object.keys(e);
  return r.length ? r.every((o) => t.strict ? e[o] === n[o] : ma(e[o]) ? e[o].test(n[o]) : e[o] === n[o]) : !0;
}
function qf(n, e, t = {}) {
  return n.find((r) => r.type === e && Yo(
    // Only check equality for the attributes that are provided
    Object.fromEntries(Object.keys(t).map((o) => [o, r.attrs[o]])),
    t
  ));
}
function cu(n, e, t = {}) {
  return !!qf(n, e, t);
}
function ga(n, e, t) {
  var r;
  if (!n || !e)
    return;
  let o = n.parent.childAfter(n.parentOffset);
  if ((!o.node || !o.node.marks.some((u) => u.type === e)) && (o = n.parent.childBefore(n.parentOffset)), !o.node || !o.node.marks.some((u) => u.type === e) || (t = t || ((r = o.node.marks[0]) === null || r === void 0 ? void 0 : r.attrs), !qf([...o.node.marks], e, t)))
    return;
  let s = o.index, l = n.start() + o.offset, a = s + 1, c = l + o.node.nodeSize;
  for (; s > 0 && cu([...n.parent.child(s - 1).marks], e, t); )
    s -= 1, l -= n.parent.child(s).nodeSize;
  for (; a < n.parent.childCount && cu([...n.parent.child(a).marks], e, t); )
    c += n.parent.child(a).nodeSize, a += 1;
  return {
    from: l,
    to: c
  };
}
function tn(n, e) {
  if (typeof n == "string") {
    if (!e.marks[n])
      throw Error(`There is no mark type named '${n}'. Maybe you forgot to add the extension?`);
    return e.marks[n];
  }
  return n;
}
const h0 = (n, e = {}) => ({ tr: t, state: r, dispatch: o }) => {
  const i = tn(n, r.schema), { doc: s, selection: l } = t, { $from: a, from: c, to: u } = l;
  if (o) {
    const d = ga(a, i, e);
    if (d && d.from <= c && d.to >= u) {
      const f = L.create(s, d.from, d.to);
      t.setSelection(f);
    }
  }
  return !0;
}, p0 = (n) => (e) => {
  const t = typeof n == "function" ? n(e) : n;
  for (let r = 0; r < t.length; r += 1)
    if (t[r](e))
      return !0;
  return !1;
};
function Gf(n) {
  return n instanceof L;
}
function dn(n = 0, e = 0, t = 0) {
  return Math.min(Math.max(n, e), t);
}
function Jf(n, e = null) {
  if (!e)
    return null;
  const t = _.atStart(n), r = _.atEnd(n);
  if (e === "start" || e === !0)
    return t;
  if (e === "end")
    return r;
  const o = t.from, i = r.to;
  return e === "all" ? L.create(n, dn(0, o, i), dn(n.content.size, o, i)) : L.create(n, dn(e, o, i), dn(e, o, i));
}
function uu() {
  return navigator.platform === "Android" || /android/i.test(navigator.userAgent);
}
function Xo() {
  return [
    "iPad Simulator",
    "iPhone Simulator",
    "iPod Simulator",
    "iPad",
    "iPhone",
    "iPod"
  ].includes(navigator.platform) || navigator.userAgent.includes("Mac") && "ontouchend" in document;
}
function m0() {
  return typeof navigator < "u" ? /^((?!chrome|android).)*safari/i.test(navigator.userAgent) : !1;
}
const g0 = (n = null, e = {}) => ({ editor: t, view: r, tr: o, dispatch: i }) => {
  e = {
    scrollIntoView: !0,
    ...e
  };
  const s = () => {
    (Xo() || uu()) && r.dom.focus(), requestAnimationFrame(() => {
      t.isDestroyed || (r.focus(), m0() && !Xo() && !uu() && r.dom.focus({ preventScroll: !0 }));
    });
  };
  if (r.hasFocus() && n === null || n === !1)
    return !0;
  if (i && n === null && !Gf(t.state.selection))
    return s(), !0;
  const l = Jf(o.doc, n) || t.state.selection, a = t.state.selection.eq(l);
  return i && (a || o.setSelection(l), a && o.storedMarks && o.setStoredMarks(o.storedMarks), s()), !0;
}, y0 = (n, e) => (t) => n.every((r, o) => e(r, { ...t, index: o })), b0 = (n, e) => ({ tr: t, commands: r }) => r.insertContentAt({ from: t.selection.from, to: t.selection.to }, n, e), Yf = (n) => {
  const e = n.childNodes;
  for (let t = e.length - 1; t >= 0; t -= 1) {
    const r = e[t];
    r.nodeType === 3 && r.nodeValue && /^(\n\s\s|\n)$/.test(r.nodeValue) ? n.removeChild(r) : r.nodeType === 1 && Yf(r);
  }
  return n;
};
function ho(n) {
  const e = `<body>${n}</body>`, t = new window.DOMParser().parseFromString(e, "text/html").body;
  return Yf(t);
}
function Lr(n, e, t) {
  if (n instanceof jt || n instanceof M)
    return n;
  t = {
    slice: !0,
    parseOptions: {},
    ...t
  };
  const r = typeof n == "object" && n !== null, o = typeof n == "string";
  if (r)
    try {
      if (Array.isArray(n) && n.length > 0)
        return M.fromArray(n.map((l) => e.nodeFromJSON(l)));
      const s = e.nodeFromJSON(n);
      return t.errorOnInvalidContent && s.check(), s;
    } catch (i) {
      if (t.errorOnInvalidContent)
        throw new Error("[tiptap error]: Invalid JSON content", { cause: i });
      return console.warn("[tiptap warn]: Invalid content.", "Passed value:", n, "Error:", i), Lr("", e, t);
    }
  if (o) {
    if (t.errorOnInvalidContent) {
      let s = !1, l = "";
      const a = new Nd({
        topNode: e.spec.topNode,
        marks: e.spec.marks,
        // Prosemirror's schemas are executed such that: the last to execute, matches last
        // This means that we can add a catch-all node at the end of the schema to catch any content that we don't know how to handle
        nodes: e.spec.nodes.append({
          __tiptap__private__unknown__catch__all__node: {
            content: "inline*",
            group: "block",
            parseDOM: [
              {
                tag: "*",
                getAttrs: (c) => (s = !0, l = typeof c == "string" ? c : c.outerHTML, null)
              }
            ]
          }
        })
      });
      if (t.slice ? Ut.fromSchema(a).parseSlice(ho(n), t.parseOptions) : Ut.fromSchema(a).parse(ho(n), t.parseOptions), t.errorOnInvalidContent && s)
        throw new Error("[tiptap error]: Invalid HTML content", { cause: new Error(`Invalid element found: ${l}`) });
    }
    const i = Ut.fromSchema(e);
    return t.slice ? i.parseSlice(ho(n), t.parseOptions).content : i.parse(ho(n), t.parseOptions);
  }
  return Lr("", e, t);
}
function w0(n, e, t) {
  const r = n.steps.length - 1;
  if (r < e)
    return;
  const o = n.steps[r];
  if (!(o instanceof fe || o instanceof he))
    return;
  const i = n.mapping.maps[r];
  let s = 0;
  i.forEach((l, a, c, u) => {
    s === 0 && (s = u);
  }), n.setSelection(_.near(n.doc.resolve(s), t));
}
const v0 = (n) => !("type" in n), C0 = (n, e, t) => ({ tr: r, dispatch: o, editor: i }) => {
  var s;
  if (o) {
    t = {
      parseOptions: i.options.parseOptions,
      updateSelection: !0,
      applyInputRules: !1,
      applyPasteRules: !1,
      ...t
    };
    let l;
    const a = (g) => {
      i.emit("contentError", {
        editor: i,
        error: g,
        disableCollaboration: () => {
          i.storage.collaboration && (i.storage.collaboration.isDisabled = !0);
        }
      });
    }, c = {
      preserveWhitespace: "full",
      ...t.parseOptions
    };
    if (!t.errorOnInvalidContent && !i.options.enableContentCheck && i.options.emitContentError)
      try {
        Lr(e, i.schema, {
          parseOptions: c,
          errorOnInvalidContent: !0
        });
      } catch (g) {
        a(g);
      }
    try {
      l = Lr(e, i.schema, {
        parseOptions: c,
        errorOnInvalidContent: (s = t.errorOnInvalidContent) !== null && s !== void 0 ? s : i.options.enableContentCheck
      });
    } catch (g) {
      return a(g), !1;
    }
    let { from: u, to: d } = typeof n == "number" ? { from: n, to: n } : { from: n.from, to: n.to }, f = !0, h = !0;
    if ((v0(l) ? l : [l]).forEach((g) => {
      g.check(), f = f ? g.isText && g.marks.length === 0 : !1, h = h ? g.isBlock : !1;
    }), u === d && h) {
      const { parent: g } = r.doc.resolve(u);
      g.isTextblock && !g.type.spec.code && !g.childCount && (u -= 1, d += 1);
    }
    let m;
    if (f) {
      if (Array.isArray(e))
        m = e.map((g) => g.text || "").join("");
      else if (e instanceof M) {
        let g = "";
        e.forEach((y) => {
          y.text && (g += y.text);
        }), m = g;
      } else typeof e == "object" && e && e.text ? m = e.text : m = e;
      r.insertText(m, u, d);
    } else
      m = l, r.replaceWith(u, d, m);
    t.updateSelection && w0(r, r.steps.length - 1, -1), t.applyInputRules && r.setMeta("applyInputRules", { from: u, text: m }), t.applyPasteRules && r.setMeta("applyPasteRules", { from: u, text: m });
  }
  return !0;
}, S0 = () => ({ state: n, dispatch: e }) => Cb(n, e), x0 = () => ({ state: n, dispatch: e }) => Sb(n, e), k0 = () => ({ state: n, dispatch: e }) => Df(n, e), E0 = () => ({ state: n, dispatch: e }) => _f(n, e), M0 = () => ({ state: n, dispatch: e, tr: t }) => {
  try {
    const r = Li(n.doc, n.selection.$from.pos, -1);
    return r == null ? !1 : (t.join(r, 2), e && e(t), !0);
  } catch {
    return !1;
  }
}, T0 = () => ({ state: n, dispatch: e, tr: t }) => {
  try {
    const r = Li(n.doc, n.selection.$from.pos, 1);
    return r == null ? !1 : (t.join(r, 2), e && e(t), !0);
  } catch {
    return !1;
  }
}, A0 = () => ({ state: n, dispatch: e }) => wb(n, e), O0 = () => ({ state: n, dispatch: e }) => vb(n, e);
function Xf() {
  return typeof navigator < "u" ? /Mac/.test(navigator.platform) : !1;
}
function N0(n) {
  const e = n.split(/-(?!$)/);
  let t = e[e.length - 1];
  t === "Space" && (t = " ");
  let r, o, i, s;
  for (let l = 0; l < e.length - 1; l += 1) {
    const a = e[l];
    if (/^(cmd|meta|m)$/i.test(a))
      s = !0;
    else if (/^a(lt)?$/i.test(a))
      r = !0;
    else if (/^(c|ctrl|control)$/i.test(a))
      o = !0;
    else if (/^s(hift)?$/i.test(a))
      i = !0;
    else if (/^mod$/i.test(a))
      Xo() || Xf() ? s = !0 : o = !0;
    else
      throw new Error(`Unrecognized modifier name: ${a}`);
  }
  return r && (t = `Alt-${t}`), o && (t = `Ctrl-${t}`), s && (t = `Meta-${t}`), i && (t = `Shift-${t}`), t;
}
const R0 = (n) => ({ editor: e, view: t, tr: r, dispatch: o }) => {
  const i = N0(n).split(/-(?!$)/), s = i.find((c) => !["Alt", "Ctrl", "Meta", "Shift"].includes(c)), l = new KeyboardEvent("keydown", {
    key: s === "Space" ? " " : s,
    altKey: i.includes("Alt"),
    ctrlKey: i.includes("Ctrl"),
    metaKey: i.includes("Meta"),
    shiftKey: i.includes("Shift"),
    bubbles: !0,
    cancelable: !0
  }), a = e.captureTransaction(() => {
    t.someProp("handleKeyDown", (c) => c(t, l));
  });
  return a == null || a.steps.forEach((c) => {
    const u = c.map(r.mapping);
    u && o && r.maybeStep(u);
  }), !0;
};
function _r(n, e, t = {}) {
  const { from: r, to: o, empty: i } = n.selection, s = e ? me(e, n.schema) : null, l = [];
  n.doc.nodesBetween(r, o, (d, f) => {
    if (d.isText)
      return;
    const h = Math.max(r, f), p = Math.min(o, f + d.nodeSize);
    l.push({
      node: d,
      from: h,
      to: p
    });
  });
  const a = o - r, c = l.filter((d) => s ? s.name === d.node.type.name : !0).filter((d) => Yo(d.node.attrs, t, { strict: !1 }));
  return i ? !!c.length : c.reduce((d, f) => d + f.to - f.from, 0) >= a;
}
const D0 = (n, e = {}) => ({ state: t, dispatch: r }) => {
  const o = me(n, t.schema);
  return _r(t, o, e) ? xb(t, r) : !1;
}, P0 = () => ({ state: n, dispatch: e }) => $f(n, e), I0 = (n) => ({ state: e, dispatch: t }) => {
  const r = me(n, e.schema);
  return Lb(r)(e, t);
}, L0 = () => ({ state: n, dispatch: e }) => Ff(n, e);
function Ki(n, e) {
  return e.nodes[n] ? "node" : e.marks[n] ? "mark" : null;
}
function du(n, e) {
  const t = typeof e == "string" ? [e] : e;
  return Object.keys(n).reduce((r, o) => (t.includes(o) || (r[o] = n[o]), r), {});
}
const _0 = (n, e) => ({ tr: t, state: r, dispatch: o }) => {
  let i = null, s = null;
  const l = Ki(typeof n == "string" ? n : n.name, r.schema);
  return l ? (l === "node" && (i = me(n, r.schema)), l === "mark" && (s = tn(n, r.schema)), o && t.selection.ranges.forEach((a) => {
    r.doc.nodesBetween(a.$from.pos, a.$to.pos, (c, u) => {
      i && i === c.type && t.setNodeMarkup(u, void 0, du(c.attrs, e)), s && c.marks.length && c.marks.forEach((d) => {
        s === d.type && t.addMark(u, u + c.nodeSize, s.create(du(d.attrs, e)));
      });
    });
  }), !0) : !1;
}, B0 = () => ({ tr: n, dispatch: e }) => (e && n.scrollIntoView(), !0), F0 = () => ({ tr: n, dispatch: e }) => {
  if (e) {
    const t = new ze(n.doc);
    n.setSelection(t);
  }
  return !0;
}, z0 = () => ({ state: n, dispatch: e }) => If(n, e), $0 = () => ({ state: n, dispatch: e }) => Bf(n, e), H0 = () => ({ state: n, dispatch: e }) => Tb(n, e), W0 = () => ({ state: n, dispatch: e }) => Nb(n, e), V0 = () => ({ state: n, dispatch: e }) => Ob(n, e);
function wl(n, e, t = {}, r = {}) {
  return Lr(n, e, {
    slice: !1,
    parseOptions: t,
    errorOnInvalidContent: r.errorOnInvalidContent
  });
}
const j0 = (n, e = !1, t = {}, r = {}) => ({ editor: o, tr: i, dispatch: s, commands: l }) => {
  var a, c;
  const { doc: u } = i;
  if (t.preserveWhitespace !== "full") {
    const d = wl(n, o.schema, t, {
      errorOnInvalidContent: (a = r.errorOnInvalidContent) !== null && a !== void 0 ? a : o.options.enableContentCheck
    });
    return s && i.replaceWith(0, u.content.size, d).setMeta("preventUpdate", !e), !0;
  }
  return s && i.setMeta("preventUpdate", !e), l.insertContentAt({ from: 0, to: u.content.size }, n, {
    parseOptions: t,
    errorOnInvalidContent: (c = r.errorOnInvalidContent) !== null && c !== void 0 ? c : o.options.enableContentCheck
  });
};
function Qf(n, e) {
  const t = tn(e, n.schema), { from: r, to: o, empty: i } = n.selection, s = [];
  i ? (n.storedMarks && s.push(...n.storedMarks), s.push(...n.selection.$head.marks())) : n.doc.nodesBetween(r, o, (a) => {
    s.push(...a.marks);
  });
  const l = s.find((a) => a.type.name === t.name);
  return l ? { ...l.attrs } : {};
}
function U0(n, e) {
  const t = new Xl(n);
  return e.forEach((r) => {
    r.steps.forEach((o) => {
      t.step(o);
    });
  }), t;
}
function K0(n) {
  for (let e = 0; e < n.edgeCount; e += 1) {
    const { type: t } = n.edge(e);
    if (t.isTextblock && !t.hasRequiredAttrs())
      return t;
  }
  return null;
}
function q0(n, e, t) {
  const r = [];
  return n.nodesBetween(e.from, e.to, (o, i) => {
    t(o) && r.push({
      node: o,
      pos: i
    });
  }), r;
}
function Zf(n, e) {
  for (let t = n.depth; t > 0; t -= 1) {
    const r = n.node(t);
    if (e(r))
      return {
        pos: t > 0 ? n.before(t) : 0,
        start: n.start(t),
        depth: t,
        node: r
      };
  }
}
function ya(n) {
  return (e) => Zf(e.$from, n);
}
function G0(n, e) {
  const t = {
    from: 0,
    to: n.content.size
  };
  return Uf(n, t, e);
}
function J0(n, e) {
  const t = me(e, n.schema), { from: r, to: o } = n.selection, i = [];
  n.doc.nodesBetween(r, o, (l) => {
    i.push(l);
  });
  const s = i.reverse().find((l) => l.type.name === t.name);
  return s ? { ...s.attrs } : {};
}
function eh(n, e) {
  const t = Ki(typeof e == "string" ? e : e.name, n.schema);
  return t === "node" ? J0(n, e) : t === "mark" ? Qf(n, e) : {};
}
function Y0(n, e = JSON.stringify) {
  const t = {};
  return n.filter((r) => {
    const o = e(r);
    return Object.prototype.hasOwnProperty.call(t, o) ? !1 : t[o] = !0;
  });
}
function X0(n) {
  const e = Y0(n);
  return e.length === 1 ? e : e.filter((t, r) => !e.filter((i, s) => s !== r).some((i) => t.oldRange.from >= i.oldRange.from && t.oldRange.to <= i.oldRange.to && t.newRange.from >= i.newRange.from && t.newRange.to <= i.newRange.to));
}
function Q0(n) {
  const { mapping: e, steps: t } = n, r = [];
  return e.maps.forEach((o, i) => {
    const s = [];
    if (o.ranges.length)
      o.forEach((l, a) => {
        s.push({ from: l, to: a });
      });
    else {
      const { from: l, to: a } = t[i];
      if (l === void 0 || a === void 0)
        return;
      s.push({ from: l, to: a });
    }
    s.forEach(({ from: l, to: a }) => {
      const c = e.slice(i).map(l, -1), u = e.slice(i).map(a), d = e.invert().map(c, -1), f = e.invert().map(u);
      r.push({
        oldRange: {
          from: d,
          to: f
        },
        newRange: {
          from: c,
          to: u
        }
      });
    });
  }), X0(r);
}
function ba(n, e, t) {
  const r = [];
  return n === e ? t.resolve(n).marks().forEach((o) => {
    const i = t.resolve(n), s = ga(i, o.type);
    s && r.push({
      mark: o,
      ...s
    });
  }) : t.nodesBetween(n, e, (o, i) => {
    !o || (o == null ? void 0 : o.nodeSize) === void 0 || r.push(...o.marks.map((s) => ({
      from: i,
      to: i + o.nodeSize,
      mark: s
    })));
  }), r;
}
function Ro(n, e, t) {
  return Object.fromEntries(Object.entries(t).filter(([r]) => {
    const o = n.find((i) => i.type === e && i.name === r);
    return o ? o.attribute.keepOnSplit : !1;
  }));
}
function vl(n, e, t = {}) {
  const { empty: r, ranges: o } = n.selection, i = e ? tn(e, n.schema) : null;
  if (r)
    return !!(n.storedMarks || n.selection.$from.marks()).filter((d) => i ? i.name === d.type.name : !0).find((d) => Yo(d.attrs, t, { strict: !1 }));
  let s = 0;
  const l = [];
  if (o.forEach(({ $from: d, $to: f }) => {
    const h = d.pos, p = f.pos;
    n.doc.nodesBetween(h, p, (m, g) => {
      if (!m.isText && !m.marks.length)
        return;
      const y = Math.max(h, g), b = Math.min(p, g + m.nodeSize), w = b - y;
      s += w, l.push(...m.marks.map((v) => ({
        mark: v,
        from: y,
        to: b
      })));
    });
  }), s === 0)
    return !1;
  const a = l.filter((d) => i ? i.name === d.mark.type.name : !0).filter((d) => Yo(d.mark.attrs, t, { strict: !1 })).reduce((d, f) => d + f.to - f.from, 0), c = l.filter((d) => i ? d.mark.type !== i && d.mark.type.excludes(i) : !0).reduce((d, f) => d + f.to - f.from, 0);
  return (a > 0 ? a + c : a) >= s;
}
function Z0(n, e, t = {}) {
  if (!e)
    return _r(n, null, t) || vl(n, null, t);
  const r = Ki(e, n.schema);
  return r === "node" ? _r(n, e, t) : r === "mark" ? vl(n, e, t) : !1;
}
function fu(n, e) {
  const { nodeExtensions: t } = Vi(e), r = t.find((s) => s.name === n);
  if (!r)
    return !1;
  const o = {
    name: r.name,
    options: r.options,
    storage: r.storage
  }, i = W(R(r, "group", o));
  return typeof i != "string" ? !1 : i.split(" ").includes("list");
}
function wa(n, { checkChildren: e = !0, ignoreWhitespace: t = !1 } = {}) {
  var r;
  if (t) {
    if (n.type.name === "hardBreak")
      return !0;
    if (n.isText)
      return /^\s*$/m.test((r = n.text) !== null && r !== void 0 ? r : "");
  }
  if (n.isText)
    return !n.text;
  if (n.isAtom || n.isLeaf)
    return !1;
  if (n.content.childCount === 0)
    return !0;
  if (e) {
    let o = !0;
    return n.content.forEach((i) => {
      o !== !1 && (wa(i, { ignoreWhitespace: t, checkChildren: e }) || (o = !1));
    }), o;
  }
  return !1;
}
function ew(n) {
  return n instanceof I;
}
function tw(n, e, t) {
  var r;
  const { selection: o } = e;
  let i = null;
  if (Gf(o) && (i = o.$cursor), i) {
    const l = (r = n.storedMarks) !== null && r !== void 0 ? r : i.marks();
    return !!t.isInSet(l) || !l.some((a) => a.type.excludes(t));
  }
  const { ranges: s } = o;
  return s.some(({ $from: l, $to: a }) => {
    let c = l.depth === 0 ? n.doc.inlineContent && n.doc.type.allowsMarkType(t) : !1;
    return n.doc.nodesBetween(l.pos, a.pos, (u, d, f) => {
      if (c)
        return !1;
      if (u.isInline) {
        const h = !f || f.type.allowsMarkType(t), p = !!t.isInSet(u.marks) || !u.marks.some((m) => m.type.excludes(t));
        c = h && p;
      }
      return !c;
    }), c;
  });
}
const nw = (n, e = {}) => ({ tr: t, state: r, dispatch: o }) => {
  const { selection: i } = t, { empty: s, ranges: l } = i, a = tn(n, r.schema);
  if (o)
    if (s) {
      const c = Qf(r, a);
      t.addStoredMark(a.create({
        ...c,
        ...e
      }));
    } else
      l.forEach((c) => {
        const u = c.$from.pos, d = c.$to.pos;
        r.doc.nodesBetween(u, d, (f, h) => {
          const p = Math.max(h, u), m = Math.min(h + f.nodeSize, d);
          f.marks.find((y) => y.type === a) ? f.marks.forEach((y) => {
            a === y.type && t.addMark(p, m, a.create({
              ...y.attrs,
              ...e
            }));
          }) : t.addMark(p, m, a.create(e));
        });
      });
  return tw(r, t, a);
}, rw = (n, e) => ({ tr: t }) => (t.setMeta(n, e), !0), ow = (n, e = {}) => ({ state: t, dispatch: r, chain: o }) => {
  const i = me(n, t.schema);
  let s;
  return t.selection.$anchor.sameParent(t.selection.$head) && (s = t.selection.$anchor.parent.attrs), i.isTextblock ? o().command(({ commands: l }) => iu(i, { ...s, ...e })(t) ? !0 : l.clearNodes()).command(({ state: l }) => iu(i, { ...s, ...e })(l, r)).run() : (console.warn('[tiptap warn]: Currently "setNode()" only supports text block nodes.'), !1);
}, iw = (n) => ({ tr: e, dispatch: t }) => {
  if (t) {
    const { doc: r } = e, o = dn(n, 0, r.content.size), i = I.create(r, o);
    e.setSelection(i);
  }
  return !0;
}, sw = (n) => ({ tr: e, dispatch: t }) => {
  if (t) {
    const { doc: r } = e, { from: o, to: i } = typeof n == "number" ? { from: n, to: n } : n, s = L.atStart(r).from, l = L.atEnd(r).to, a = dn(o, s, l), c = dn(i, s, l), u = L.create(r, a, c);
    e.setSelection(u);
  }
  return !0;
}, lw = (n) => ({ state: e, dispatch: t }) => {
  const r = me(n, e.schema);
  return Fb(r)(e, t);
};
function hu(n, e) {
  const t = n.storedMarks || n.selection.$to.parentOffset && n.selection.$from.marks();
  if (t) {
    const r = t.filter((o) => e == null ? void 0 : e.includes(o.type.name));
    n.tr.ensureMarks(r);
  }
}
const aw = ({ keepMarks: n = !0 } = {}) => ({ tr: e, state: t, dispatch: r, editor: o }) => {
  const { selection: i, doc: s } = e, { $from: l, $to: a } = i, c = o.extensionManager.attributes, u = Ro(c, l.node().type.name, l.node().attrs);
  if (i instanceof I && i.node.isBlock)
    return !l.parentOffset || !xt(s, l.pos) ? !1 : (r && (n && hu(t, o.extensionManager.splittableMarks), e.split(l.pos).scrollIntoView()), !0);
  if (!l.parent.isBlock)
    return !1;
  const d = a.parentOffset === a.parent.content.size, f = l.depth === 0 ? void 0 : K0(l.node(-1).contentMatchAt(l.indexAfter(-1)));
  let h = d && f ? [
    {
      type: f,
      attrs: u
    }
  ] : void 0, p = xt(e.doc, e.mapping.map(l.pos), 1, h);
  if (!h && !p && xt(e.doc, e.mapping.map(l.pos), 1, f ? [{ type: f }] : void 0) && (p = !0, h = f ? [
    {
      type: f,
      attrs: u
    }
  ] : void 0), r) {
    if (p && (i instanceof L && e.deleteSelection(), e.split(e.mapping.map(l.pos), 1, h), f && !d && !l.parentOffset && l.parent.type !== f)) {
      const m = e.mapping.map(l.before()), g = e.doc.resolve(m);
      l.node(-1).canReplaceWith(g.index(), g.index() + 1, f) && e.setNodeMarkup(e.mapping.map(l.before()), f);
    }
    n && hu(t, o.extensionManager.splittableMarks), e.scrollIntoView();
  }
  return p;
}, cw = (n, e = {}) => ({ tr: t, state: r, dispatch: o, editor: i }) => {
  var s;
  const l = me(n, r.schema), { $from: a, $to: c } = r.selection, u = r.selection.node;
  if (u && u.isBlock || a.depth < 2 || !a.sameParent(c))
    return !1;
  const d = a.node(-1);
  if (d.type !== l)
    return !1;
  const f = i.extensionManager.attributes;
  if (a.parent.content.size === 0 && a.node(-1).childCount === a.indexAfter(-1)) {
    if (a.depth === 2 || a.node(-3).type !== l || a.index(-2) !== a.node(-2).childCount - 1)
      return !1;
    if (o) {
      let y = M.empty;
      const b = a.index(-1) ? 1 : a.index(-2) ? 2 : 3;
      for (let A = a.depth - b; A >= a.depth - 3; A -= 1)
        y = M.from(a.node(A).copy(y));
      const w = a.indexAfter(-1) < a.node(-2).childCount ? 1 : a.indexAfter(-2) < a.node(-3).childCount ? 2 : 3, v = {
        ...Ro(f, a.node().type.name, a.node().attrs),
        ...e
      }, k = ((s = l.contentMatch.defaultType) === null || s === void 0 ? void 0 : s.createAndFill(v)) || void 0;
      y = y.append(M.from(l.createAndFill(null, k) || void 0));
      const E = a.before(a.depth - (b - 1));
      t.replace(E, a.after(-w), new O(y, 4 - b, 0));
      let C = -1;
      t.doc.nodesBetween(E, t.doc.content.size, (A, B) => {
        if (C > -1)
          return !1;
        A.isTextblock && A.content.size === 0 && (C = B + 1);
      }), C > -1 && t.setSelection(L.near(t.doc.resolve(C))), t.scrollIntoView();
    }
    return !0;
  }
  const h = c.pos === a.end() ? d.contentMatchAt(0).defaultType : null, p = {
    ...Ro(f, d.type.name, d.attrs),
    ...e
  }, m = {
    ...Ro(f, a.node().type.name, a.node().attrs),
    ...e
  };
  t.delete(a.pos, c.pos);
  const g = h ? [
    { type: l, attrs: p },
    { type: h, attrs: m }
  ] : [{ type: l, attrs: p }];
  if (!xt(t.doc, a.pos, 2))
    return !1;
  if (o) {
    const { selection: y, storedMarks: b } = r, { splittableMarks: w } = i.extensionManager, v = b || y.$to.parentOffset && y.$from.marks();
    if (t.split(a.pos, 2, g).scrollIntoView(), !v || !o)
      return !0;
    const k = v.filter((E) => w.includes(E.type.name));
    t.ensureMarks(k);
  }
  return !0;
}, Ds = (n, e) => {
  const t = ya((s) => s.type === e)(n.selection);
  if (!t)
    return !0;
  const r = n.doc.resolve(Math.max(0, t.pos - 1)).before(t.depth);
  if (r === void 0)
    return !0;
  const o = n.doc.nodeAt(r);
  return t.node.type === (o == null ? void 0 : o.type) && Zt(n.doc, t.pos) && n.join(t.pos), !0;
}, Ps = (n, e) => {
  const t = ya((s) => s.type === e)(n.selection);
  if (!t)
    return !0;
  const r = n.doc.resolve(t.start).after(t.depth);
  if (r === void 0)
    return !0;
  const o = n.doc.nodeAt(r);
  return t.node.type === (o == null ? void 0 : o.type) && Zt(n.doc, r) && n.join(r), !0;
}, uw = (n, e, t, r = {}) => ({ editor: o, tr: i, state: s, dispatch: l, chain: a, commands: c, can: u }) => {
  const { extensions: d, splittableMarks: f } = o.extensionManager, h = me(n, s.schema), p = me(e, s.schema), { selection: m, storedMarks: g } = s, { $from: y, $to: b } = m, w = y.blockRange(b), v = g || m.$to.parentOffset && m.$from.marks();
  if (!w)
    return !1;
  const k = ya((E) => fu(E.type.name, d))(m);
  if (w.depth >= 1 && k && w.depth - k.depth <= 1) {
    if (k.node.type === h)
      return c.liftListItem(p);
    if (fu(k.node.type.name, d) && h.validContent(k.node.content) && l)
      return a().command(() => (i.setNodeMarkup(k.pos, h), !0)).command(() => Ds(i, h)).command(() => Ps(i, h)).run();
  }
  return !t || !v || !l ? a().command(() => u().wrapInList(h, r) ? !0 : c.clearNodes()).wrapInList(h, r).command(() => Ds(i, h)).command(() => Ps(i, h)).run() : a().command(() => {
    const E = u().wrapInList(h, r), C = v.filter((A) => f.includes(A.type.name));
    return i.ensureMarks(C), E ? !0 : c.clearNodes();
  }).wrapInList(h, r).command(() => Ds(i, h)).command(() => Ps(i, h)).run();
}, dw = (n, e = {}, t = {}) => ({ state: r, commands: o }) => {
  const { extendEmptyMarkRange: i = !1 } = t, s = tn(n, r.schema);
  return vl(r, s, e) ? o.unsetMark(s, { extendEmptyMarkRange: i }) : o.setMark(s, e);
}, fw = (n, e, t = {}) => ({ state: r, commands: o }) => {
  const i = me(n, r.schema), s = me(e, r.schema), l = _r(r, i, t);
  let a;
  return r.selection.$anchor.sameParent(r.selection.$head) && (a = r.selection.$anchor.parent.attrs), l ? o.setNode(s, a) : o.setNode(i, { ...a, ...t });
}, hw = (n, e = {}) => ({ state: t, commands: r }) => {
  const o = me(n, t.schema);
  return _r(t, o, e) ? r.lift(o) : r.wrapIn(o, e);
}, pw = () => ({ state: n, dispatch: e }) => {
  const t = n.plugins;
  for (let r = 0; r < t.length; r += 1) {
    const o = t[r];
    let i;
    if (o.spec.isInputRules && (i = o.getState(n))) {
      if (e) {
        const s = n.tr, l = i.transform;
        for (let a = l.steps.length - 1; a >= 0; a -= 1)
          s.step(l.steps[a].invert(l.docs[a]));
        if (i.text) {
          const a = s.doc.resolve(i.from).marks();
          s.replaceWith(i.from, i.to, n.schema.text(i.text, a));
        } else
          s.delete(i.from, i.to);
      }
      return !0;
    }
  }
  return !1;
}, mw = () => ({ tr: n, dispatch: e }) => {
  const { selection: t } = n, { empty: r, ranges: o } = t;
  return r || e && o.forEach((i) => {
    n.removeMark(i.$from.pos, i.$to.pos);
  }), !0;
}, gw = (n, e = {}) => ({ tr: t, state: r, dispatch: o }) => {
  var i;
  const { extendEmptyMarkRange: s = !1 } = e, { selection: l } = t, a = tn(n, r.schema), { $from: c, empty: u, ranges: d } = l;
  if (!o)
    return !0;
  if (u && s) {
    let { from: f, to: h } = l;
    const p = (i = c.marks().find((g) => g.type === a)) === null || i === void 0 ? void 0 : i.attrs, m = ga(c, a, p);
    m && (f = m.from, h = m.to), t.removeMark(f, h, a);
  } else
    d.forEach((f) => {
      t.removeMark(f.$from.pos, f.$to.pos, a);
    });
  return t.removeStoredMark(a), !0;
}, yw = (n, e = {}) => ({ tr: t, state: r, dispatch: o }) => {
  let i = null, s = null;
  const l = Ki(typeof n == "string" ? n : n.name, r.schema);
  return l ? (l === "node" && (i = me(n, r.schema)), l === "mark" && (s = tn(n, r.schema)), o && t.selection.ranges.forEach((a) => {
    const c = a.$from.pos, u = a.$to.pos;
    let d, f, h, p;
    t.selection.empty ? r.doc.nodesBetween(c, u, (m, g) => {
      i && i === m.type && (h = Math.max(g, c), p = Math.min(g + m.nodeSize, u), d = g, f = m);
    }) : r.doc.nodesBetween(c, u, (m, g) => {
      g < c && i && i === m.type && (h = Math.max(g, c), p = Math.min(g + m.nodeSize, u), d = g, f = m), g >= c && g <= u && (i && i === m.type && t.setNodeMarkup(g, void 0, {
        ...m.attrs,
        ...e
      }), s && m.marks.length && m.marks.forEach((y) => {
        if (s === y.type) {
          const b = Math.max(g, c), w = Math.min(g + m.nodeSize, u);
          t.addMark(b, w, s.create({
            ...y.attrs,
            ...e
          }));
        }
      }));
    }), f && (d !== void 0 && t.setNodeMarkup(d, void 0, {
      ...f.attrs,
      ...e
    }), s && f.marks.length && f.marks.forEach((m) => {
      s === m.type && t.addMark(h, p, s.create({
        ...m.attrs,
        ...e
      }));
    }));
  }), !0) : !1;
}, bw = (n, e = {}) => ({ state: t, dispatch: r }) => {
  const o = me(n, t.schema);
  return Rb(o, e)(t, r);
}, ww = (n, e = {}) => ({ state: t, dispatch: r }) => {
  const o = me(n, t.schema);
  return Db(o, e)(t, r);
};
var vw = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  blur: t0,
  clearContent: n0,
  clearNodes: r0,
  command: o0,
  createParagraphNear: i0,
  cut: s0,
  deleteCurrentNode: l0,
  deleteNode: a0,
  deleteRange: c0,
  deleteSelection: u0,
  enter: d0,
  exitCode: f0,
  extendMarkRange: h0,
  first: p0,
  focus: g0,
  forEach: y0,
  insertContent: b0,
  insertContentAt: C0,
  joinBackward: k0,
  joinDown: x0,
  joinForward: E0,
  joinItemBackward: M0,
  joinItemForward: T0,
  joinTextblockBackward: A0,
  joinTextblockForward: O0,
  joinUp: S0,
  keyboardShortcut: R0,
  lift: D0,
  liftEmptyBlock: P0,
  liftListItem: I0,
  newlineInCode: L0,
  resetAttributes: _0,
  scrollIntoView: B0,
  selectAll: F0,
  selectNodeBackward: z0,
  selectNodeForward: $0,
  selectParentNode: H0,
  selectTextblockEnd: W0,
  selectTextblockStart: V0,
  setContent: j0,
  setMark: nw,
  setMeta: rw,
  setNode: ow,
  setNodeSelection: iw,
  setTextSelection: sw,
  sinkListItem: lw,
  splitBlock: aw,
  splitListItem: cw,
  toggleList: uw,
  toggleMark: dw,
  toggleNode: fw,
  toggleWrap: hw,
  undoInputRule: pw,
  unsetAllMarks: mw,
  unsetMark: gw,
  updateAttributes: yw,
  wrapIn: bw,
  wrapInList: ww
});
const Cw = xe.create({
  name: "commands",
  addCommands() {
    return {
      ...vw
    };
  }
}), Sw = xe.create({
  name: "drop",
  addProseMirrorPlugins() {
    return [
      new ue({
        key: new Me("tiptapDrop"),
        props: {
          handleDrop: (n, e, t, r) => {
            this.editor.emit("drop", {
              editor: this.editor,
              event: e,
              slice: t,
              moved: r
            });
          }
        }
      })
    ];
  }
}), xw = xe.create({
  name: "editable",
  addProseMirrorPlugins() {
    return [
      new ue({
        key: new Me("editable"),
        props: {
          editable: () => this.editor.options.editable
        }
      })
    ];
  }
}), kw = new Me("focusEvents"), Ew = xe.create({
  name: "focusEvents",
  addProseMirrorPlugins() {
    const { editor: n } = this;
    return [
      new ue({
        key: kw,
        props: {
          handleDOMEvents: {
            focus: (e, t) => {
              n.isFocused = !0;
              const r = n.state.tr.setMeta("focus", { event: t }).setMeta("addToHistory", !1);
              return e.dispatch(r), !1;
            },
            blur: (e, t) => {
              n.isFocused = !1;
              const r = n.state.tr.setMeta("blur", { event: t }).setMeta("addToHistory", !1);
              return e.dispatch(r), !1;
            }
          }
        }
      })
    ];
  }
}), Mw = xe.create({
  name: "keymap",
  addKeyboardShortcuts() {
    const n = () => this.editor.commands.first(({ commands: s }) => [
      () => s.undoInputRule(),
      // maybe convert first text block node to default node
      () => s.command(({ tr: l }) => {
        const { selection: a, doc: c } = l, { empty: u, $anchor: d } = a, { pos: f, parent: h } = d, p = d.parent.isTextblock && f > 0 ? l.doc.resolve(f - 1) : d, m = p.parent.type.spec.isolating, g = d.pos - d.parentOffset, y = m && p.parent.childCount === 1 ? g === d.pos : _.atStart(c).from === f;
        return !u || !h.type.isTextblock || h.textContent.length || !y || y && d.parent.type.name === "paragraph" ? !1 : s.clearNodes();
      }),
      () => s.deleteSelection(),
      () => s.joinBackward(),
      () => s.selectNodeBackward()
    ]), e = () => this.editor.commands.first(({ commands: s }) => [
      () => s.deleteSelection(),
      () => s.deleteCurrentNode(),
      () => s.joinForward(),
      () => s.selectNodeForward()
    ]), r = {
      Enter: () => this.editor.commands.first(({ commands: s }) => [
        () => s.newlineInCode(),
        () => s.createParagraphNear(),
        () => s.liftEmptyBlock(),
        () => s.splitBlock()
      ]),
      "Mod-Enter": () => this.editor.commands.exitCode(),
      Backspace: n,
      "Mod-Backspace": n,
      "Shift-Backspace": n,
      Delete: e,
      "Mod-Delete": e,
      "Mod-a": () => this.editor.commands.selectAll()
    }, o = {
      ...r
    }, i = {
      ...r,
      "Ctrl-h": n,
      "Alt-Backspace": n,
      "Ctrl-d": e,
      "Ctrl-Alt-Backspace": e,
      "Alt-Delete": e,
      "Alt-d": e,
      "Ctrl-a": () => this.editor.commands.selectTextblockStart(),
      "Ctrl-e": () => this.editor.commands.selectTextblockEnd()
    };
    return Xo() || Xf() ? i : o;
  },
  addProseMirrorPlugins() {
    return [
      // With this plugin we check if the whole document was selected and deleted.
      // In this case we will additionally call `clearNodes()` to convert e.g. a heading
      // to a paragraph if necessary.
      // This is an alternative to ProseMirror's `AllSelection`, which doesn’t work well
      // with many other commands.
      new ue({
        key: new Me("clearDocument"),
        appendTransaction: (n, e, t) => {
          if (n.some((m) => m.getMeta("composition")))
            return;
          const r = n.some((m) => m.docChanged) && !e.doc.eq(t.doc), o = n.some((m) => m.getMeta("preventClearDocument"));
          if (!r || o)
            return;
          const { empty: i, from: s, to: l } = e.selection, a = _.atStart(e.doc).from, c = _.atEnd(e.doc).to;
          if (i || !(s === a && l === c) || !wa(t.doc))
            return;
          const f = t.tr, h = Hi({
            state: t,
            transaction: f
          }), { commands: p } = new Wi({
            editor: this.editor,
            state: h
          });
          if (p.clearNodes(), !!f.steps.length)
            return f;
        }
      })
    ];
  }
}), Tw = xe.create({
  name: "paste",
  addProseMirrorPlugins() {
    return [
      new ue({
        key: new Me("tiptapPaste"),
        props: {
          handlePaste: (n, e, t) => {
            this.editor.emit("paste", {
              editor: this.editor,
              event: e,
              slice: t
            });
          }
        }
      })
    ];
  }
}), Aw = xe.create({
  name: "tabindex",
  addProseMirrorPlugins() {
    return [
      new ue({
        key: new Me("tabindex"),
        props: {
          attributes: () => this.editor.isEditable ? { tabindex: "0" } : {}
        }
      })
    ];
  }
});
class an {
  get name() {
    return this.node.type.name;
  }
  constructor(e, t, r = !1, o = null) {
    this.currentNode = null, this.actualDepth = null, this.isBlock = r, this.resolvedPos = e, this.editor = t, this.currentNode = o;
  }
  get node() {
    return this.currentNode || this.resolvedPos.node();
  }
  get element() {
    return this.editor.view.domAtPos(this.pos).node;
  }
  get depth() {
    var e;
    return (e = this.actualDepth) !== null && e !== void 0 ? e : this.resolvedPos.depth;
  }
  get pos() {
    return this.resolvedPos.pos;
  }
  get content() {
    return this.node.content;
  }
  set content(e) {
    let t = this.from, r = this.to;
    if (this.isBlock) {
      if (this.content.size === 0) {
        console.error(`You can’t set content on a block node. Tried to set content on ${this.name} at ${this.pos}`);
        return;
      }
      t = this.from + 1, r = this.to - 1;
    }
    this.editor.commands.insertContentAt({ from: t, to: r }, e);
  }
  get attributes() {
    return this.node.attrs;
  }
  get textContent() {
    return this.node.textContent;
  }
  get size() {
    return this.node.nodeSize;
  }
  get from() {
    return this.isBlock ? this.pos : this.resolvedPos.start(this.resolvedPos.depth);
  }
  get range() {
    return {
      from: this.from,
      to: this.to
    };
  }
  get to() {
    return this.isBlock ? this.pos + this.size : this.resolvedPos.end(this.resolvedPos.depth) + (this.node.isText ? 0 : 1);
  }
  get parent() {
    if (this.depth === 0)
      return null;
    const e = this.resolvedPos.start(this.resolvedPos.depth - 1), t = this.resolvedPos.doc.resolve(e);
    return new an(t, this.editor);
  }
  get before() {
    let e = this.resolvedPos.doc.resolve(this.from - (this.isBlock ? 1 : 2));
    return e.depth !== this.depth && (e = this.resolvedPos.doc.resolve(this.from - 3)), new an(e, this.editor);
  }
  get after() {
    let e = this.resolvedPos.doc.resolve(this.to + (this.isBlock ? 2 : 1));
    return e.depth !== this.depth && (e = this.resolvedPos.doc.resolve(this.to + 3)), new an(e, this.editor);
  }
  get children() {
    const e = [];
    return this.node.content.forEach((t, r) => {
      const o = t.isBlock && !t.isTextblock, i = t.isAtom && !t.isText, s = this.pos + r + (i ? 0 : 1);
      if (s < 0 || s > this.resolvedPos.doc.nodeSize - 2)
        return;
      const l = this.resolvedPos.doc.resolve(s);
      if (!o && l.depth <= this.depth)
        return;
      const a = new an(l, this.editor, o, o ? t : null);
      o && (a.actualDepth = this.depth + 1), e.push(new an(l, this.editor, o, o ? t : null));
    }), e;
  }
  get firstChild() {
    return this.children[0] || null;
  }
  get lastChild() {
    const e = this.children;
    return e[e.length - 1] || null;
  }
  closest(e, t = {}) {
    let r = null, o = this.parent;
    for (; o && !r; ) {
      if (o.node.type.name === e)
        if (Object.keys(t).length > 0) {
          const i = o.node.attrs, s = Object.keys(t);
          for (let l = 0; l < s.length; l += 1) {
            const a = s[l];
            if (i[a] !== t[a])
              break;
          }
        } else
          r = o;
      o = o.parent;
    }
    return r;
  }
  querySelector(e, t = {}) {
    return this.querySelectorAll(e, t, !0)[0] || null;
  }
  querySelectorAll(e, t = {}, r = !1) {
    let o = [];
    if (!this.children || this.children.length === 0)
      return o;
    const i = Object.keys(t);
    return this.children.forEach((s) => {
      r && o.length > 0 || (s.node.type.name === e && i.every((a) => t[a] === s.node.attrs[a]) && o.push(s), !(r && o.length > 0) && (o = o.concat(s.querySelectorAll(e, t, r))));
    }), o;
  }
  setAttribute(e) {
    const { tr: t } = this.editor.state;
    t.setNodeMarkup(this.from, void 0, {
      ...this.node.attrs,
      ...e
    }), this.editor.view.dispatch(t);
  }
}
const Ow = `.ProseMirror {
  position: relative;
}

.ProseMirror {
  word-wrap: break-word;
  white-space: pre-wrap;
  white-space: break-spaces;
  -webkit-font-variant-ligatures: none;
  font-variant-ligatures: none;
  font-feature-settings: "liga" 0; /* the above doesn't seem to work in Edge */
}

.ProseMirror [contenteditable="false"] {
  white-space: normal;
}

.ProseMirror [contenteditable="false"] [contenteditable="true"] {
  white-space: pre-wrap;
}

.ProseMirror pre {
  white-space: pre-wrap;
}

img.ProseMirror-separator {
  display: inline !important;
  border: none !important;
  margin: 0 !important;
  width: 0 !important;
  height: 0 !important;
}

.ProseMirror-gapcursor {
  display: none;
  pointer-events: none;
  position: absolute;
  margin: 0;
}

.ProseMirror-gapcursor:after {
  content: "";
  display: block;
  position: absolute;
  top: -2px;
  width: 20px;
  border-top: 1px solid black;
  animation: ProseMirror-cursor-blink 1.1s steps(2, start) infinite;
}

@keyframes ProseMirror-cursor-blink {
  to {
    visibility: hidden;
  }
}

.ProseMirror-hideselection *::selection {
  background: transparent;
}

.ProseMirror-hideselection *::-moz-selection {
  background: transparent;
}

.ProseMirror-hideselection * {
  caret-color: transparent;
}

.ProseMirror-focused .ProseMirror-gapcursor {
  display: block;
}

.tippy-box[data-animation=fade][data-state=hidden] {
  opacity: 0
}`;
function Nw(n, e, t) {
  const r = document.querySelector("style[data-tiptap-style]");
  if (r !== null)
    return r;
  const o = document.createElement("style");
  return e && o.setAttribute("nonce", e), o.setAttribute("data-tiptap-style", ""), o.innerHTML = n, document.getElementsByTagName("head")[0].appendChild(o), o;
}
class Rw extends zb {
  constructor(e = {}) {
    super(), this.isFocused = !1, this.isInitialized = !1, this.extensionStorage = {}, this.options = {
      element: document.createElement("div"),
      content: "",
      injectCSS: !0,
      injectNonce: void 0,
      extensions: [],
      autofocus: !1,
      editable: !0,
      editorProps: {},
      parseOptions: {},
      coreExtensionOptions: {},
      enableInputRules: !0,
      enablePasteRules: !0,
      enableCoreExtensions: !0,
      enableContentCheck: !1,
      emitContentError: !1,
      onBeforeCreate: () => null,
      onCreate: () => null,
      onUpdate: () => null,
      onSelectionUpdate: () => null,
      onTransaction: () => null,
      onFocus: () => null,
      onBlur: () => null,
      onDestroy: () => null,
      onContentError: ({ error: t }) => {
        throw t;
      },
      onPaste: () => null,
      onDrop: () => null
    }, this.isCapturingTransaction = !1, this.capturedTransaction = null, this.setOptions(e), this.createExtensionManager(), this.createCommandManager(), this.createSchema(), this.on("beforeCreate", this.options.onBeforeCreate), this.emit("beforeCreate", { editor: this }), this.on("contentError", this.options.onContentError), this.createView(), this.injectCSS(), this.on("create", this.options.onCreate), this.on("update", this.options.onUpdate), this.on("selectionUpdate", this.options.onSelectionUpdate), this.on("transaction", this.options.onTransaction), this.on("focus", this.options.onFocus), this.on("blur", this.options.onBlur), this.on("destroy", this.options.onDestroy), this.on("drop", ({ event: t, slice: r, moved: o }) => this.options.onDrop(t, r, o)), this.on("paste", ({ event: t, slice: r }) => this.options.onPaste(t, r)), window.setTimeout(() => {
      this.isDestroyed || (this.commands.focus(this.options.autofocus), this.emit("create", { editor: this }), this.isInitialized = !0);
    }, 0);
  }
  /**
   * Returns the editor storage.
   */
  get storage() {
    return this.extensionStorage;
  }
  /**
   * An object of all registered commands.
   */
  get commands() {
    return this.commandManager.commands;
  }
  /**
   * Create a command chain to call multiple commands at once.
   */
  chain() {
    return this.commandManager.chain();
  }
  /**
   * Check if a command or a command chain can be executed. Without executing it.
   */
  can() {
    return this.commandManager.can();
  }
  /**
   * Inject CSS styles.
   */
  injectCSS() {
    this.options.injectCSS && document && (this.css = Nw(Ow, this.options.injectNonce));
  }
  /**
   * Update editor options.
   *
   * @param options A list of options
   */
  setOptions(e = {}) {
    this.options = {
      ...this.options,
      ...e
    }, !(!this.view || !this.state || this.isDestroyed) && (this.options.editorProps && this.view.setProps(this.options.editorProps), this.view.updateState(this.state));
  }
  /**
   * Update editable state of the editor.
   */
  setEditable(e, t = !0) {
    this.setOptions({ editable: e }), t && this.emit("update", { editor: this, transaction: this.state.tr });
  }
  /**
   * Returns whether the editor is editable.
   */
  get isEditable() {
    return this.options.editable && this.view && this.view.editable;
  }
  /**
   * Returns the editor state.
   */
  get state() {
    return this.view.state;
  }
  /**
   * Register a ProseMirror plugin.
   *
   * @param plugin A ProseMirror plugin
   * @param handlePlugins Control how to merge the plugin into the existing plugins.
   * @returns The new editor state
   */
  registerPlugin(e, t) {
    const r = jf(t) ? t(e, [...this.state.plugins]) : [...this.state.plugins, e], o = this.state.reconfigure({ plugins: r });
    return this.view.updateState(o), o;
  }
  /**
   * Unregister a ProseMirror plugin.
   *
   * @param nameOrPluginKeyToRemove The plugins name
   * @returns The new editor state or undefined if the editor is destroyed
   */
  unregisterPlugin(e) {
    if (this.isDestroyed)
      return;
    const t = this.state.plugins;
    let r = t;
    if ([].concat(e).forEach((i) => {
      const s = typeof i == "string" ? `${i}$` : i.key;
      r = r.filter((l) => !l.key.startsWith(s));
    }), t.length === r.length)
      return;
    const o = this.state.reconfigure({
      plugins: r
    });
    return this.view.updateState(o), o;
  }
  /**
   * Creates an extension manager.
   */
  createExtensionManager() {
    var e, t;
    const o = [...this.options.enableCoreExtensions ? [
      xw,
      e0.configure({
        blockSeparator: (t = (e = this.options.coreExtensionOptions) === null || e === void 0 ? void 0 : e.clipboardTextSerializer) === null || t === void 0 ? void 0 : t.blockSeparator
      }),
      Cw,
      Ew,
      Mw,
      Aw,
      Sw,
      Tw
    ].filter((i) => typeof this.options.enableCoreExtensions == "object" ? this.options.enableCoreExtensions[i.name] !== !1 : !0) : [], ...this.options.extensions].filter((i) => ["extension", "node", "mark"].includes(i == null ? void 0 : i.type));
    this.extensionManager = new Hn(o, this);
  }
  /**
   * Creates an command manager.
   */
  createCommandManager() {
    this.commandManager = new Wi({
      editor: this
    });
  }
  /**
   * Creates a ProseMirror schema.
   */
  createSchema() {
    this.schema = this.extensionManager.schema;
  }
  /**
   * Creates a ProseMirror view.
   */
  createView() {
    var e;
    let t;
    try {
      t = wl(this.options.content, this.schema, this.options.parseOptions, { errorOnInvalidContent: this.options.enableContentCheck });
    } catch (s) {
      if (!(s instanceof Error) || !["[tiptap error]: Invalid JSON content", "[tiptap error]: Invalid HTML content"].includes(s.message))
        throw s;
      this.emit("contentError", {
        editor: this,
        error: s,
        disableCollaboration: () => {
          this.storage.collaboration && (this.storage.collaboration.isDisabled = !0), this.options.extensions = this.options.extensions.filter((l) => l.name !== "collaboration"), this.createExtensionManager();
        }
      }), t = wl(this.options.content, this.schema, this.options.parseOptions, { errorOnInvalidContent: !1 });
    }
    const r = Jf(t, this.options.autofocus);
    this.view = new Nf(this.options.element, {
      ...this.options.editorProps,
      attributes: {
        // add `role="textbox"` to the editor element
        role: "textbox",
        ...(e = this.options.editorProps) === null || e === void 0 ? void 0 : e.attributes
      },
      dispatchTransaction: this.dispatchTransaction.bind(this),
      state: $n.create({
        doc: t,
        selection: r || void 0
      })
    });
    const o = this.state.reconfigure({
      plugins: this.extensionManager.plugins
    });
    this.view.updateState(o), this.createNodeViews(), this.prependClass();
    const i = this.view.dom;
    i.editor = this;
  }
  /**
   * Creates all node views.
   */
  createNodeViews() {
    this.view.isDestroyed || this.view.setProps({
      nodeViews: this.extensionManager.nodeViews
    });
  }
  /**
   * Prepend class name to element.
   */
  prependClass() {
    this.view.dom.className = `tiptap ${this.view.dom.className}`;
  }
  captureTransaction(e) {
    this.isCapturingTransaction = !0, e(), this.isCapturingTransaction = !1;
    const t = this.capturedTransaction;
    return this.capturedTransaction = null, t;
  }
  /**
   * The callback over which to send transactions (state updates) produced by the view.
   *
   * @param transaction An editor state transaction
   */
  dispatchTransaction(e) {
    if (this.view.isDestroyed)
      return;
    if (this.isCapturingTransaction) {
      if (!this.capturedTransaction) {
        this.capturedTransaction = e;
        return;
      }
      e.steps.forEach((s) => {
        var l;
        return (l = this.capturedTransaction) === null || l === void 0 ? void 0 : l.step(s);
      });
      return;
    }
    const t = this.state.apply(e), r = !this.state.selection.eq(t.selection);
    this.emit("beforeTransaction", {
      editor: this,
      transaction: e,
      nextState: t
    }), this.view.updateState(t), this.emit("transaction", {
      editor: this,
      transaction: e
    }), r && this.emit("selectionUpdate", {
      editor: this,
      transaction: e
    });
    const o = e.getMeta("focus"), i = e.getMeta("blur");
    o && this.emit("focus", {
      editor: this,
      event: o.event,
      transaction: e
    }), i && this.emit("blur", {
      editor: this,
      event: i.event,
      transaction: e
    }), !(!e.docChanged || e.getMeta("preventUpdate")) && this.emit("update", {
      editor: this,
      transaction: e
    });
  }
  /**
   * Get attributes of the currently selected node or mark.
   */
  getAttributes(e) {
    return eh(this.state, e);
  }
  isActive(e, t) {
    const r = typeof e == "string" ? e : null, o = typeof e == "string" ? t : e;
    return Z0(this.state, r, o);
  }
  /**
   * Get the document as JSON.
   */
  getJSON() {
    return this.state.doc.toJSON();
  }
  /**
   * Get the document as HTML.
   */
  getHTML() {
    return pa(this.state.doc.content, this.schema);
  }
  /**
   * Get the document as text.
   */
  getText(e) {
    const { blockSeparator: t = `

`, textSerializers: r = {} } = e || {};
    return G0(this.state.doc, {
      blockSeparator: t,
      textSerializers: {
        ...Kf(this.schema),
        ...r
      }
    });
  }
  /**
   * Check if there is no content.
   */
  get isEmpty() {
    return wa(this.state.doc);
  }
  /**
   * Get the number of characters for the current document.
   *
   * @deprecated
   */
  getCharacterCount() {
    return console.warn('[tiptap warn]: "editor.getCharacterCount()" is deprecated. Please use "editor.storage.characterCount.characters()" instead.'), this.state.doc.content.size - 2;
  }
  /**
   * Destroy the editor.
   */
  destroy() {
    if (this.emit("destroy"), this.view) {
      const e = this.view.dom;
      e && e.editor && delete e.editor, this.view.destroy();
    }
    this.removeAllListeners();
  }
  /**
   * Check if the editor is already destroyed.
   */
  get isDestroyed() {
    var e;
    return !(!((e = this.view) === null || e === void 0) && e.docView);
  }
  $node(e, t) {
    var r;
    return ((r = this.$doc) === null || r === void 0 ? void 0 : r.querySelector(e, t)) || null;
  }
  $nodes(e, t) {
    var r;
    return ((r = this.$doc) === null || r === void 0 ? void 0 : r.querySelectorAll(e, t)) || null;
  }
  $pos(e) {
    const t = this.state.doc.resolve(e);
    return new an(t, this);
  }
  get $doc() {
    return this.$pos(0);
  }
}
function Yn(n) {
  return new ji({
    find: n.find,
    handler: ({ state: e, range: t, match: r }) => {
      const o = W(n.getAttributes, void 0, r);
      if (o === !1 || o === null)
        return null;
      const { tr: i } = e, s = r[r.length - 1], l = r[0];
      if (s) {
        const a = l.search(/\S/), c = t.from + l.indexOf(s), u = c + s.length;
        if (ba(t.from, t.to, e.doc).filter((h) => h.mark.type.excluded.find((m) => m === n.type && m !== h.mark.type)).filter((h) => h.to > c).length)
          return null;
        u < t.to && i.delete(u, t.to), c > t.from && i.delete(t.from + a, c);
        const f = t.from + a + s.length;
        i.addMark(t.from + a, f, n.type.create(o || {})), i.removeStoredMark(n.type);
      }
    }
  });
}
function th(n) {
  return new ji({
    find: n.find,
    handler: ({ state: e, range: t, match: r }) => {
      const o = W(n.getAttributes, void 0, r) || {}, { tr: i } = e, s = t.from;
      let l = t.to;
      const a = n.type.create(o);
      if (r[1]) {
        const c = r[0].lastIndexOf(r[1]);
        let u = s + c;
        u > l ? u = l : l = u + r[1].length;
        const d = r[0][r[0].length - 1];
        i.insertText(d, s + r[0].length - 1), i.replaceWith(u, l, a);
      } else if (r[0]) {
        const c = n.type.isInline ? s : s - 1;
        i.insert(c, n.type.create(o)).delete(i.mapping.map(s), i.mapping.map(l));
      }
      i.scrollIntoView();
    }
  });
}
function Cl(n) {
  return new ji({
    find: n.find,
    handler: ({ state: e, range: t, match: r }) => {
      const o = e.doc.resolve(t.from), i = W(n.getAttributes, void 0, r) || {};
      if (!o.node(-1).canReplaceWith(o.index(-1), o.indexAfter(-1), n.type))
        return null;
      e.tr.delete(t.from, t.to).setBlockType(t.from, t.from, n.type, i);
    }
  });
}
function Br(n) {
  return new ji({
    find: n.find,
    handler: ({ state: e, range: t, match: r, chain: o }) => {
      const i = W(n.getAttributes, void 0, r) || {}, s = e.tr.delete(t.from, t.to), a = s.doc.resolve(t.from).blockRange(), c = a && Yl(a, n.type, i);
      if (!c)
        return null;
      if (s.wrap(a, c), n.keepMarks && n.editor) {
        const { selection: d, storedMarks: f } = e, { splittableMarks: h } = n.editor.extensionManager, p = f || d.$to.parentOffset && d.$from.marks();
        if (p) {
          const m = p.filter((g) => h.includes(g.type.name));
          s.ensureMarks(m);
        }
      }
      if (n.keepAttributes) {
        const d = n.type.name === "bulletList" || n.type.name === "orderedList" ? "listItem" : "taskList";
        o().updateAttributes(d, i).run();
      }
      const u = s.doc.resolve(t.from - 1).nodeBefore;
      u && u.type === n.type && Zt(s.doc, t.from - 1) && (!n.joinPredicate || n.joinPredicate(r, u)) && s.join(t.from - 1);
    }
  });
}
let Te = class Sl {
  constructor(e = {}) {
    this.type = "node", this.name = "node", this.parent = null, this.child = null, this.config = {
      name: this.name,
      defaultOptions: {}
    }, this.config = {
      ...this.config,
      ...e
    }, this.name = this.config.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${this.name}".`), this.options = this.config.defaultOptions, this.config.addOptions && (this.options = W(R(this, "addOptions", {
      name: this.name
    }))), this.storage = W(R(this, "addStorage", {
      name: this.name,
      options: this.options
    })) || {};
  }
  static create(e = {}) {
    return new Sl(e);
  }
  configure(e = {}) {
    const t = this.extend({
      ...this.config,
      addOptions: () => Ui(this.options, e)
    });
    return t.name = this.name, t.parent = this.parent, t;
  }
  extend(e = {}) {
    const t = new Sl(e);
    return t.parent = this, this.child = t, t.name = e.name ? e.name : t.parent.name, e.defaultOptions && Object.keys(e.defaultOptions).length > 0 && console.warn(`[tiptap warn]: BREAKING CHANGE: "defaultOptions" is deprecated. Please use "addOptions" instead. Found in extension: "${t.name}".`), t.options = W(R(t, "addOptions", {
      name: t.name
    })), t.storage = W(R(t, "addStorage", {
      name: t.name,
      options: t.options
    })), t;
  }
};
function Sn(n) {
  return new Gb({
    find: n.find,
    handler: ({ state: e, range: t, match: r, pasteEvent: o }) => {
      const i = W(n.getAttributes, void 0, r, o);
      if (i === !1 || i === null)
        return null;
      const { tr: s } = e, l = r[r.length - 1], a = r[0];
      let c = t.to;
      if (l) {
        const u = a.search(/\S/), d = t.from + a.indexOf(l), f = d + l.length;
        if (ba(t.from, t.to, e.doc).filter((p) => p.mark.type.excluded.find((g) => g === n.type && g !== p.mark.type)).filter((p) => p.to > d).length)
          return null;
        f < t.to && s.delete(f, t.to), d > t.from && s.delete(t.from + u, d), c = t.from + u + l.length, s.addMark(t.from + u, c, n.type.create(i || {})), s.removeStoredMark(n.type);
      }
    }
  });
}
function Dw(n, e) {
  const { selection: t } = n, { $from: r } = t;
  if (t instanceof I) {
    const i = r.index();
    return r.parent.canReplaceWith(i, i + 1, e);
  }
  let o = r.depth;
  for (; o >= 0; ) {
    const i = r.index(o);
    if (r.node(o).contentMatchAt(i).matchType(e))
      return !0;
    o -= 1;
  }
  return !1;
}
const {
  useState: K,
  useRef: D,
  useEffect: z,
  useCallback: V,
  useMemo: Xe,
  forwardRef: N,
  createRef: Pw,
  createElement: ct,
  createContext: xn,
  useContext: tr,
  useReducer: nh,
  useImperativeHandle: Iw,
  useLayoutEffect: Yr,
  memo: Lw,
  Fragment: nr,
  Children: $e,
  isValidElement: Et,
  cloneElement: An,
  useDebugValue: va,
  version: _w,
  useId: Bw,
  useSyncExternalStore: Fw,
  useDeferredValue: zw,
  useTransition: $w,
  startTransition: Hw
} = x, Ca = /* @__PURE__ */ Object.freeze(/* @__PURE__ */ Object.defineProperty({
  __proto__: null,
  Children: $e,
  Fragment: nr,
  ReactDOM: Pi,
  cloneElement: An,
  createContext: xn,
  createElement: ct,
  createRef: Pw,
  default: x,
  forwardRef: N,
  isValidElement: Et,
  memo: Lw,
  startTransition: Hw,
  useCallback: V,
  useContext: tr,
  useDebugValue: va,
  useDeferredValue: zw,
  useEffect: z,
  useId: Bw,
  useImperativeHandle: Iw,
  useLayoutEffect: Yr,
  useMemo: Xe,
  useReducer: nh,
  useRef: D,
  useState: K,
  useSyncExternalStore: Fw,
  useTransition: $w,
  version: _w
}, Symbol.toStringTag, { value: "Module" })), { flushSync: rh, createPortal: qM } = Pi;
function Ww(n) {
  return n && n.__esModule && Object.prototype.hasOwnProperty.call(n, "default") ? n.default : n;
}
var oh = { exports: {} }, Is = {};
/**
 * @license React
 * use-sync-external-store-shim.production.min.js
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var pu;
function Vw() {
  if (pu) return Is;
  pu = 1;
  var n = x;
  function e(d, f) {
    return d === f && (d !== 0 || 1 / d === 1 / f) || d !== d && f !== f;
  }
  var t = typeof Object.is == "function" ? Object.is : e, r = n.useState, o = n.useEffect, i = n.useLayoutEffect, s = n.useDebugValue;
  function l(d, f) {
    var h = f(), p = r({ inst: { value: h, getSnapshot: f } }), m = p[0].inst, g = p[1];
    return i(function() {
      m.value = h, m.getSnapshot = f, a(m) && g({ inst: m });
    }, [d, h, f]), o(function() {
      return a(m) && g({ inst: m }), d(function() {
        a(m) && g({ inst: m });
      });
    }, [d]), s(h), h;
  }
  function a(d) {
    var f = d.getSnapshot;
    d = d.value;
    try {
      var h = f();
      return !t(d, h);
    } catch {
      return !0;
    }
  }
  function c(d, f) {
    return f();
  }
  var u = typeof window > "u" || typeof window.document > "u" || typeof window.document.createElement > "u" ? c : l;
  return Is.useSyncExternalStore = n.useSyncExternalStore !== void 0 ? n.useSyncExternalStore : u, Is;
}
oh.exports = Vw();
var Sa = oh.exports;
const jw = (...n) => (e) => {
  n.forEach((t) => {
    typeof t == "function" ? t(e) : t && (t.current = e);
  });
}, Uw = ({ contentComponent: n }) => {
  const e = Sa.useSyncExternalStore(n.subscribe, n.getSnapshot, n.getServerSnapshot);
  return x.createElement(x.Fragment, null, Object.values(e));
};
function Kw() {
  const n = /* @__PURE__ */ new Set();
  let e = {};
  return {
    /**
     * Subscribe to the editor instance's changes.
     */
    subscribe(t) {
      return n.add(t), () => {
        n.delete(t);
      };
    },
    getSnapshot() {
      return e;
    },
    getServerSnapshot() {
      return e;
    },
    /**
     * Adds a new NodeView Renderer to the editor.
     */
    setRenderer(t, r) {
      e = {
        ...e,
        [t]: Pi.createPortal(r.reactElement, r.element, t)
      }, n.forEach((o) => o());
    },
    /**
     * Removes a NodeView Renderer from the editor.
     */
    removeRenderer(t) {
      const r = { ...e };
      delete r[t], e = r, n.forEach((o) => o());
    }
  };
}
class qw extends x.Component {
  constructor(e) {
    var t;
    super(e), this.editorContentRef = x.createRef(), this.initialized = !1, this.state = {
      hasContentComponentInitialized: !!(!((t = e.editor) === null || t === void 0) && t.contentComponent)
    };
  }
  componentDidMount() {
    this.init();
  }
  componentDidUpdate() {
    this.init();
  }
  init() {
    const e = this.props.editor;
    if (e && !e.isDestroyed && e.options.element) {
      if (e.contentComponent)
        return;
      const t = this.editorContentRef.current;
      t.append(...e.options.element.childNodes), e.setOptions({
        element: t
      }), e.contentComponent = Kw(), this.state.hasContentComponentInitialized || (this.unsubscribeToContentComponent = e.contentComponent.subscribe(() => {
        this.setState((r) => r.hasContentComponentInitialized ? r : {
          hasContentComponentInitialized: !0
        }), this.unsubscribeToContentComponent && this.unsubscribeToContentComponent();
      })), e.createNodeViews(), this.initialized = !0;
    }
  }
  componentWillUnmount() {
    const e = this.props.editor;
    if (!e || (this.initialized = !1, e.isDestroyed || e.view.setProps({
      nodeViews: {}
    }), this.unsubscribeToContentComponent && this.unsubscribeToContentComponent(), e.contentComponent = null, !e.options.element.firstChild))
      return;
    const t = document.createElement("div");
    t.append(...e.options.element.childNodes), e.setOptions({
      element: t
    });
  }
  render() {
    const { editor: e, innerRef: t, ...r } = this.props;
    return x.createElement(
      x.Fragment,
      null,
      x.createElement("div", { ref: jw(t, this.editorContentRef), ...r }),
      (e == null ? void 0 : e.contentComponent) && x.createElement(Uw, { contentComponent: e.contentComponent })
    );
  }
}
const Gw = N((n, e) => {
  const t = x.useMemo(() => Math.floor(Math.random() * 4294967295).toString(), [n.editor]);
  return x.createElement(qw, {
    key: t,
    innerRef: e,
    ...n
  });
}), Jw = x.memo(Gw);
var Yw = function n(e, t) {
  if (e === t) return !0;
  if (e && t && typeof e == "object" && typeof t == "object") {
    if (e.constructor !== t.constructor) return !1;
    var r, o, i;
    if (Array.isArray(e)) {
      if (r = e.length, r != t.length) return !1;
      for (o = r; o-- !== 0; )
        if (!n(e[o], t[o])) return !1;
      return !0;
    }
    if (e instanceof Map && t instanceof Map) {
      if (e.size !== t.size) return !1;
      for (o of e.entries())
        if (!t.has(o[0])) return !1;
      for (o of e.entries())
        if (!n(o[1], t.get(o[0]))) return !1;
      return !0;
    }
    if (e instanceof Set && t instanceof Set) {
      if (e.size !== t.size) return !1;
      for (o of e.entries())
        if (!t.has(o[0])) return !1;
      return !0;
    }
    if (ArrayBuffer.isView(e) && ArrayBuffer.isView(t)) {
      if (r = e.length, r != t.length) return !1;
      for (o = r; o-- !== 0; )
        if (e[o] !== t[o]) return !1;
      return !0;
    }
    if (e.constructor === RegExp) return e.source === t.source && e.flags === t.flags;
    if (e.valueOf !== Object.prototype.valueOf) return e.valueOf() === t.valueOf();
    if (e.toString !== Object.prototype.toString) return e.toString() === t.toString();
    if (i = Object.keys(e), r = i.length, r !== Object.keys(t).length) return !1;
    for (o = r; o-- !== 0; )
      if (!Object.prototype.hasOwnProperty.call(t, i[o])) return !1;
    for (o = r; o-- !== 0; ) {
      var s = i[o];
      if (!(s === "_owner" && e.$$typeof) && !n(e[s], t[s]))
        return !1;
    }
    return !0;
  }
  return e !== e && t !== t;
}, Xw = /* @__PURE__ */ Ww(Yw), ih = { exports: {} }, Ls = {};
/**
 * @license React
 * use-sync-external-store-shim/with-selector.production.min.js
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
var mu;
function Qw() {
  if (mu) return Ls;
  mu = 1;
  var n = x, e = Sa;
  function t(c, u) {
    return c === u && (c !== 0 || 1 / c === 1 / u) || c !== c && u !== u;
  }
  var r = typeof Object.is == "function" ? Object.is : t, o = e.useSyncExternalStore, i = n.useRef, s = n.useEffect, l = n.useMemo, a = n.useDebugValue;
  return Ls.useSyncExternalStoreWithSelector = function(c, u, d, f, h) {
    var p = i(null);
    if (p.current === null) {
      var m = { hasValue: !1, value: null };
      p.current = m;
    } else m = p.current;
    p = l(function() {
      function y(E) {
        if (!b) {
          if (b = !0, w = E, E = f(E), h !== void 0 && m.hasValue) {
            var C = m.value;
            if (h(C, E)) return v = C;
          }
          return v = E;
        }
        if (C = v, r(w, E)) return C;
        var A = f(E);
        return h !== void 0 && h(C, A) ? C : (w = E, v = A);
      }
      var b = !1, w, v, k = d === void 0 ? null : d;
      return [function() {
        return y(u());
      }, k === null ? void 0 : function() {
        return y(k());
      }];
    }, [u, d, f, h]);
    var g = o(c, p[0], p[1]);
    return s(function() {
      m.hasValue = !0, m.value = g;
    }, [g]), a(g), g;
  }, Ls;
}
ih.exports = Qw();
var Zw = ih.exports;
const ev = typeof window < "u" ? Yr : z;
class tv {
  constructor(e) {
    this.transactionNumber = 0, this.lastTransactionNumber = 0, this.subscribers = /* @__PURE__ */ new Set(), this.editor = e, this.lastSnapshot = { editor: e, transactionNumber: 0 }, this.getSnapshot = this.getSnapshot.bind(this), this.getServerSnapshot = this.getServerSnapshot.bind(this), this.watch = this.watch.bind(this), this.subscribe = this.subscribe.bind(this);
  }
  /**
   * Get the current editor instance.
   */
  getSnapshot() {
    return this.transactionNumber === this.lastTransactionNumber ? this.lastSnapshot : (this.lastTransactionNumber = this.transactionNumber, this.lastSnapshot = { editor: this.editor, transactionNumber: this.transactionNumber }, this.lastSnapshot);
  }
  /**
   * Always disable the editor on the server-side.
   */
  getServerSnapshot() {
    return { editor: null, transactionNumber: 0 };
  }
  /**
   * Subscribe to the editor instance's changes.
   */
  subscribe(e) {
    return this.subscribers.add(e), () => {
      this.subscribers.delete(e);
    };
  }
  /**
   * Watch the editor instance for changes.
   */
  watch(e) {
    if (this.editor = e, this.editor) {
      const t = () => {
        this.transactionNumber += 1, this.subscribers.forEach((o) => o());
      }, r = this.editor;
      return r.on("transaction", t), () => {
        r.off("transaction", t);
      };
    }
  }
}
function nv(n) {
  var e;
  const [t] = K(() => new tv(n.editor)), r = Zw.useSyncExternalStoreWithSelector(t.subscribe, t.getSnapshot, t.getServerSnapshot, n.selector, (e = n.equalityFn) !== null && e !== void 0 ? e : Xw);
  return ev(() => t.watch(n.editor), [n.editor, t]), va(r), r;
}
const rv = !1, xl = typeof window > "u", ov = xl || !!(typeof window < "u" && window.next);
class xa {
  constructor(e) {
    this.editor = null, this.subscriptions = /* @__PURE__ */ new Set(), this.isComponentMounted = !1, this.previousDeps = null, this.instanceId = "", this.options = e, this.subscriptions = /* @__PURE__ */ new Set(), this.setEditor(this.getInitialEditor()), this.scheduleDestroy(), this.getEditor = this.getEditor.bind(this), this.getServerSnapshot = this.getServerSnapshot.bind(this), this.subscribe = this.subscribe.bind(this), this.refreshEditorInstance = this.refreshEditorInstance.bind(this), this.scheduleDestroy = this.scheduleDestroy.bind(this), this.onRender = this.onRender.bind(this), this.createEditor = this.createEditor.bind(this);
  }
  setEditor(e) {
    this.editor = e, this.instanceId = Math.random().toString(36).slice(2, 9), this.subscriptions.forEach((t) => t());
  }
  getInitialEditor() {
    return this.options.current.immediatelyRender === void 0 ? xl || ov ? null : this.createEditor() : (this.options.current.immediatelyRender, this.options.current.immediatelyRender ? this.createEditor() : null);
  }
  /**
   * Create a new editor instance. And attach event listeners.
   */
  createEditor() {
    const e = {
      ...this.options.current,
      // Always call the most recent version of the callback function by default
      onBeforeCreate: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onBeforeCreate) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onBlur: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onBlur) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onCreate: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onCreate) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onDestroy: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onDestroy) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onFocus: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onFocus) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onSelectionUpdate: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onSelectionUpdate) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onTransaction: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onTransaction) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onUpdate: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onUpdate) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onContentError: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onContentError) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onDrop: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onDrop) === null || i === void 0 ? void 0 : i.call(o, ...r);
      },
      onPaste: (...r) => {
        var o, i;
        return (i = (o = this.options.current).onPaste) === null || i === void 0 ? void 0 : i.call(o, ...r);
      }
    };
    return new Rw(e);
  }
  /**
   * Get the current editor instance.
   */
  getEditor() {
    return this.editor;
  }
  /**
   * Always disable the editor on the server-side.
   */
  getServerSnapshot() {
    return null;
  }
  /**
   * Subscribe to the editor instance's changes.
   */
  subscribe(e) {
    return this.subscriptions.add(e), () => {
      this.subscriptions.delete(e);
    };
  }
  static compareOptions(e, t) {
    return Object.keys(e).every((r) => ["onCreate", "onBeforeCreate", "onDestroy", "onUpdate", "onTransaction", "onFocus", "onBlur", "onSelectionUpdate", "onContentError", "onDrop", "onPaste"].includes(r) ? !0 : r === "extensions" && e.extensions && t.extensions ? e.extensions.length !== t.extensions.length ? !1 : e.extensions.every((o, i) => {
      var s;
      return o === ((s = t.extensions) === null || s === void 0 ? void 0 : s[i]);
    }) : e[r] === t[r]);
  }
  /**
   * On each render, we will create, update, or destroy the editor instance.
   * @param deps The dependencies to watch for changes
   * @returns A cleanup function
   */
  onRender(e) {
    return () => (this.isComponentMounted = !0, clearTimeout(this.scheduledDestructionTimeout), this.editor && !this.editor.isDestroyed && e.length === 0 ? xa.compareOptions(this.options.current, this.editor.options) || this.editor.setOptions({
      ...this.options.current,
      editable: this.editor.isEditable
    }) : this.refreshEditorInstance(e), () => {
      this.isComponentMounted = !1, this.scheduleDestroy();
    });
  }
  /**
   * Recreate the editor instance if the dependencies have changed.
   */
  refreshEditorInstance(e) {
    if (this.editor && !this.editor.isDestroyed) {
      if (this.previousDeps === null) {
        this.previousDeps = e;
        return;
      }
      if (this.previousDeps.length === e.length && this.previousDeps.every((r, o) => r === e[o]))
        return;
    }
    this.editor && !this.editor.isDestroyed && this.editor.destroy(), this.setEditor(this.createEditor()), this.previousDeps = e;
  }
  /**
   * Schedule the destruction of the editor instance.
   * This will only destroy the editor if it was not mounted on the next tick.
   * This is to avoid destroying the editor instance when it's actually still mounted.
   */
  scheduleDestroy() {
    const e = this.instanceId, t = this.editor;
    this.scheduledDestructionTimeout = setTimeout(() => {
      if (this.isComponentMounted && this.instanceId === e) {
        t && t.setOptions(this.options.current);
        return;
      }
      t && !t.isDestroyed && (t.destroy(), this.instanceId === e && this.setEditor(null));
    }, 1);
  }
}
function iv(n = {}, e = []) {
  const t = D(n);
  t.current = n;
  const [r] = K(() => new xa(t)), o = Sa.useSyncExternalStore(r.subscribe, r.getEditor, r.getServerSnapshot);
  return va(o), z(r.onRender(e)), nv({
    editor: o,
    selector: ({ transactionNumber: i }) => n.shouldRerenderOnTransaction === !1 ? null : n.immediatelyRender && i === 0 ? 0 : i + 1
  }), o;
}
const sv = xn({
  editor: null
});
sv.Consumer;
const lv = xn({
  onDragStart: void 0
}), av = () => tr(lv);
x.forwardRef((n, e) => {
  const { onDragStart: t } = av(), r = n.as || "div";
  return (
    // @ts-ignore
    x.createElement(r, { ...n, ref: e, "data-node-view-wrapper": "", onDragStart: t, style: {
      whiteSpace: "normal",
      ...n.style
    } })
  );
});
const cv = /^\s*>\s$/, uv = Te.create({
  name: "blockquote",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  content: "block+",
  group: "block",
  defining: !0,
  parseHTML() {
    return [
      { tag: "blockquote" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["blockquote", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setBlockquote: () => ({ commands: n }) => n.wrapIn(this.name),
      toggleBlockquote: () => ({ commands: n }) => n.toggleWrap(this.name),
      unsetBlockquote: () => ({ commands: n }) => n.lift(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Shift-b": () => this.editor.commands.toggleBlockquote()
    };
  },
  addInputRules() {
    return [
      Br({
        find: cv,
        type: this.type
      })
    ];
  }
}), dv = /(?:^|\s)(\*\*(?!\s+\*\*)((?:[^*]+))\*\*(?!\s+\*\*))$/, fv = /(?:^|\s)(\*\*(?!\s+\*\*)((?:[^*]+))\*\*(?!\s+\*\*))/g, hv = /(?:^|\s)(__(?!\s+__)((?:[^_]+))__(?!\s+__))$/, pv = /(?:^|\s)(__(?!\s+__)((?:[^_]+))__(?!\s+__))/g, mv = Qe.create({
  name: "bold",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  parseHTML() {
    return [
      {
        tag: "strong"
      },
      {
        tag: "b",
        getAttrs: (n) => n.style.fontWeight !== "normal" && null
      },
      {
        style: "font-weight=400",
        clearMark: (n) => n.type.name === this.name
      },
      {
        style: "font-weight",
        getAttrs: (n) => /^(bold(er)?|[5-9]\d{2,})$/.test(n) && null
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["strong", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setBold: () => ({ commands: n }) => n.setMark(this.name),
      toggleBold: () => ({ commands: n }) => n.toggleMark(this.name),
      unsetBold: () => ({ commands: n }) => n.unsetMark(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-b": () => this.editor.commands.toggleBold(),
      "Mod-B": () => this.editor.commands.toggleBold()
    };
  },
  addInputRules() {
    return [
      Yn({
        find: dv,
        type: this.type
      }),
      Yn({
        find: hv,
        type: this.type
      })
    ];
  },
  addPasteRules() {
    return [
      Sn({
        find: fv,
        type: this.type
      }),
      Sn({
        find: pv,
        type: this.type
      })
    ];
  }
}), gv = "listItem", gu = "textStyle", yu = /^\s*([-+*])\s$/, yv = Te.create({
  name: "bulletList",
  addOptions() {
    return {
      itemTypeName: "listItem",
      HTMLAttributes: {},
      keepMarks: !1,
      keepAttributes: !1
    };
  },
  group: "block list",
  content() {
    return `${this.options.itemTypeName}+`;
  },
  parseHTML() {
    return [
      { tag: "ul" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["ul", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      toggleBulletList: () => ({ commands: n, chain: e }) => this.options.keepAttributes ? e().toggleList(this.name, this.options.itemTypeName, this.options.keepMarks).updateAttributes(gv, this.editor.getAttributes(gu)).run() : n.toggleList(this.name, this.options.itemTypeName, this.options.keepMarks)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Shift-8": () => this.editor.commands.toggleBulletList()
    };
  },
  addInputRules() {
    let n = Br({
      find: yu,
      type: this.type
    });
    return (this.options.keepMarks || this.options.keepAttributes) && (n = Br({
      find: yu,
      type: this.type,
      keepMarks: this.options.keepMarks,
      keepAttributes: this.options.keepAttributes,
      getAttributes: () => this.editor.getAttributes(gu),
      editor: this.editor
    })), [
      n
    ];
  }
}), bv = /(^|[^`])`([^`]+)`(?!`)/, wv = /(^|[^`])`([^`]+)`(?!`)/g, vv = Qe.create({
  name: "code",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  excludes: "_",
  code: !0,
  exitable: !0,
  parseHTML() {
    return [
      { tag: "code" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["code", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setCode: () => ({ commands: n }) => n.setMark(this.name),
      toggleCode: () => ({ commands: n }) => n.toggleMark(this.name),
      unsetCode: () => ({ commands: n }) => n.unsetMark(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-e": () => this.editor.commands.toggleCode()
    };
  },
  addInputRules() {
    return [
      Yn({
        find: bv,
        type: this.type
      })
    ];
  },
  addPasteRules() {
    return [
      Sn({
        find: wv,
        type: this.type
      })
    ];
  }
}), Cv = /^```([a-z]+)?[\s\n]$/, Sv = /^~~~([a-z]+)?[\s\n]$/, xv = Te.create({
  name: "codeBlock",
  addOptions() {
    return {
      languageClassPrefix: "language-",
      exitOnTripleEnter: !0,
      exitOnArrowDown: !0,
      defaultLanguage: null,
      HTMLAttributes: {}
    };
  },
  content: "text*",
  marks: "",
  group: "block",
  code: !0,
  defining: !0,
  addAttributes() {
    return {
      language: {
        default: this.options.defaultLanguage,
        parseHTML: (n) => {
          var e;
          const { languageClassPrefix: t } = this.options, i = [...((e = n.firstElementChild) === null || e === void 0 ? void 0 : e.classList) || []].filter((s) => s.startsWith(t)).map((s) => s.replace(t, ""))[0];
          return i || null;
        },
        rendered: !1
      }
    };
  },
  parseHTML() {
    return [
      {
        tag: "pre",
        preserveWhitespace: "full"
      }
    ];
  },
  renderHTML({ node: n, HTMLAttributes: e }) {
    return [
      "pre",
      ne(this.options.HTMLAttributes, e),
      [
        "code",
        {
          class: n.attrs.language ? this.options.languageClassPrefix + n.attrs.language : null
        },
        0
      ]
    ];
  },
  addCommands() {
    return {
      setCodeBlock: (n) => ({ commands: e }) => e.setNode(this.name, n),
      toggleCodeBlock: (n) => ({ commands: e }) => e.toggleNode(this.name, "paragraph", n)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Alt-c": () => this.editor.commands.toggleCodeBlock(),
      // remove code block when at start of document or code block is empty
      Backspace: () => {
        const { empty: n, $anchor: e } = this.editor.state.selection, t = e.pos === 1;
        return !n || e.parent.type.name !== this.name ? !1 : t || !e.parent.textContent.length ? this.editor.commands.clearNodes() : !1;
      },
      // exit node on triple enter
      Enter: ({ editor: n }) => {
        if (!this.options.exitOnTripleEnter)
          return !1;
        const { state: e } = n, { selection: t } = e, { $from: r, empty: o } = t;
        if (!o || r.parent.type !== this.type)
          return !1;
        const i = r.parentOffset === r.parent.nodeSize - 2, s = r.parent.textContent.endsWith(`

`);
        return !i || !s ? !1 : n.chain().command(({ tr: l }) => (l.delete(r.pos - 2, r.pos), !0)).exitCode().run();
      },
      // exit node on arrow down
      ArrowDown: ({ editor: n }) => {
        if (!this.options.exitOnArrowDown)
          return !1;
        const { state: e } = n, { selection: t, doc: r } = e, { $from: o, empty: i } = t;
        if (!i || o.parent.type !== this.type || !(o.parentOffset === o.parent.nodeSize - 2))
          return !1;
        const l = o.after();
        return l === void 0 ? !1 : r.nodeAt(l) ? n.commands.command(({ tr: c }) => (c.setSelection(_.near(r.resolve(l))), !0)) : n.commands.exitCode();
      }
    };
  },
  addInputRules() {
    return [
      Cl({
        find: Cv,
        type: this.type,
        getAttributes: (n) => ({
          language: n[1]
        })
      }),
      Cl({
        find: Sv,
        type: this.type,
        getAttributes: (n) => ({
          language: n[1]
        })
      })
    ];
  },
  addProseMirrorPlugins() {
    return [
      // this plugin creates a code block for pasted content from VS Code
      // we can also detect the copied code language
      new ue({
        key: new Me("codeBlockVSCodeHandler"),
        props: {
          handlePaste: (n, e) => {
            if (!e.clipboardData || this.editor.isActive(this.type.name))
              return !1;
            const t = e.clipboardData.getData("text/plain"), r = e.clipboardData.getData("vscode-editor-data"), o = r ? JSON.parse(r) : void 0, i = o == null ? void 0 : o.mode;
            if (!t || !i)
              return !1;
            const { tr: s, schema: l } = n.state, a = l.text(t.replace(/\r\n?/g, `
`));
            return s.replaceSelectionWith(this.type.create({ language: i }, a)), s.selection.$from.parent.type !== this.type && s.setSelection(L.near(s.doc.resolve(Math.max(0, s.selection.from - 2)))), s.setMeta("paste", !0), n.dispatch(s), !0;
          }
        }
      })
    ];
  }
}), kv = Te.create({
  name: "doc",
  topNode: !0,
  content: "block+"
});
function Ev(n = {}) {
  return new ue({
    view(e) {
      return new Mv(e, n);
    }
  });
}
class Mv {
  constructor(e, t) {
    var r;
    this.editorView = e, this.cursorPos = null, this.element = null, this.timeout = -1, this.width = (r = t.width) !== null && r !== void 0 ? r : 1, this.color = t.color === !1 ? void 0 : t.color || "black", this.class = t.class, this.handlers = ["dragover", "dragend", "drop", "dragleave"].map((o) => {
      let i = (s) => {
        this[o](s);
      };
      return e.dom.addEventListener(o, i), { name: o, handler: i };
    });
  }
  destroy() {
    this.handlers.forEach(({ name: e, handler: t }) => this.editorView.dom.removeEventListener(e, t));
  }
  update(e, t) {
    this.cursorPos != null && t.doc != e.state.doc && (this.cursorPos > e.state.doc.content.size ? this.setCursor(null) : this.updateOverlay());
  }
  setCursor(e) {
    e != this.cursorPos && (this.cursorPos = e, e == null ? (this.element.parentNode.removeChild(this.element), this.element = null) : this.updateOverlay());
  }
  updateOverlay() {
    let e = this.editorView.state.doc.resolve(this.cursorPos), t = !e.parent.inlineContent, r, o = this.editorView.dom, i = o.getBoundingClientRect(), s = i.width / o.offsetWidth, l = i.height / o.offsetHeight;
    if (t) {
      let d = e.nodeBefore, f = e.nodeAfter;
      if (d || f) {
        let h = this.editorView.nodeDOM(this.cursorPos - (d ? d.nodeSize : 0));
        if (h) {
          let p = h.getBoundingClientRect(), m = d ? p.bottom : p.top;
          d && f && (m = (m + this.editorView.nodeDOM(this.cursorPos).getBoundingClientRect().top) / 2);
          let g = this.width / 2 * l;
          r = { left: p.left, right: p.right, top: m - g, bottom: m + g };
        }
      }
    }
    if (!r) {
      let d = this.editorView.coordsAtPos(this.cursorPos), f = this.width / 2 * s;
      r = { left: d.left - f, right: d.left + f, top: d.top, bottom: d.bottom };
    }
    let a = this.editorView.dom.offsetParent;
    this.element || (this.element = a.appendChild(document.createElement("div")), this.class && (this.element.className = this.class), this.element.style.cssText = "position: absolute; z-index: 50; pointer-events: none;", this.color && (this.element.style.backgroundColor = this.color)), this.element.classList.toggle("prosemirror-dropcursor-block", t), this.element.classList.toggle("prosemirror-dropcursor-inline", !t);
    let c, u;
    if (!a || a == document.body && getComputedStyle(a).position == "static")
      c = -pageXOffset, u = -pageYOffset;
    else {
      let d = a.getBoundingClientRect(), f = d.width / a.offsetWidth, h = d.height / a.offsetHeight;
      c = d.left - a.scrollLeft * f, u = d.top - a.scrollTop * h;
    }
    this.element.style.left = (r.left - c) / s + "px", this.element.style.top = (r.top - u) / l + "px", this.element.style.width = (r.right - r.left) / s + "px", this.element.style.height = (r.bottom - r.top) / l + "px";
  }
  scheduleRemoval(e) {
    clearTimeout(this.timeout), this.timeout = setTimeout(() => this.setCursor(null), e);
  }
  dragover(e) {
    if (!this.editorView.editable)
      return;
    let t = this.editorView.posAtCoords({ left: e.clientX, top: e.clientY }), r = t && t.inside >= 0 && this.editorView.state.doc.nodeAt(t.inside), o = r && r.type.spec.disableDropCursor, i = typeof o == "function" ? o(this.editorView, t, e) : o;
    if (t && !i) {
      let s = t.pos;
      if (this.editorView.dragging && this.editorView.dragging.slice) {
        let l = Hd(this.editorView.state.doc, s, this.editorView.dragging.slice);
        l != null && (s = l);
      }
      this.setCursor(s), this.scheduleRemoval(5e3);
    }
  }
  dragend() {
    this.scheduleRemoval(20);
  }
  drop() {
    this.scheduleRemoval(20);
  }
  dragleave(e) {
    this.editorView.dom.contains(e.relatedTarget) || this.setCursor(null);
  }
}
const Tv = xe.create({
  name: "dropCursor",
  addOptions() {
    return {
      color: "currentColor",
      width: 1,
      class: void 0
    };
  },
  addProseMirrorPlugins() {
    return [
      Ev(this.options)
    ];
  }
});
class le extends _ {
  /**
  Create a gap cursor.
  */
  constructor(e) {
    super(e, e);
  }
  map(e, t) {
    let r = e.resolve(t.map(this.head));
    return le.valid(r) ? new le(r) : _.near(r);
  }
  content() {
    return O.empty;
  }
  eq(e) {
    return e instanceof le && e.head == this.head;
  }
  toJSON() {
    return { type: "gapcursor", pos: this.head };
  }
  /**
  @internal
  */
  static fromJSON(e, t) {
    if (typeof t.pos != "number")
      throw new RangeError("Invalid input for GapCursor.fromJSON");
    return new le(e.resolve(t.pos));
  }
  /**
  @internal
  */
  getBookmark() {
    return new ka(this.anchor);
  }
  /**
  @internal
  */
  static valid(e) {
    let t = e.parent;
    if (t.inlineContent || !Av(e) || !Ov(e))
      return !1;
    let r = t.type.spec.allowGapCursor;
    if (r != null)
      return r;
    let o = t.contentMatchAt(e.index()).defaultType;
    return o && o.isTextblock;
  }
  /**
  @internal
  */
  static findGapCursorFrom(e, t, r = !1) {
    e: for (; ; ) {
      if (!r && le.valid(e))
        return e;
      let o = e.pos, i = null;
      for (let s = e.depth; ; s--) {
        let l = e.node(s);
        if (t > 0 ? e.indexAfter(s) < l.childCount : e.index(s) > 0) {
          i = l.child(t > 0 ? e.indexAfter(s) : e.index(s) - 1);
          break;
        } else if (s == 0)
          return null;
        o += t;
        let a = e.doc.resolve(o);
        if (le.valid(a))
          return a;
      }
      for (; ; ) {
        let s = t > 0 ? i.firstChild : i.lastChild;
        if (!s) {
          if (i.isAtom && !i.isText && !I.isSelectable(i)) {
            e = e.doc.resolve(o + i.nodeSize * t), r = !1;
            continue e;
          }
          break;
        }
        i = s, o += t;
        let l = e.doc.resolve(o);
        if (le.valid(l))
          return l;
      }
      return null;
    }
  }
}
le.prototype.visible = !1;
le.findFrom = le.findGapCursorFrom;
_.jsonID("gapcursor", le);
class ka {
  constructor(e) {
    this.pos = e;
  }
  map(e) {
    return new ka(e.map(this.pos));
  }
  resolve(e) {
    let t = e.resolve(this.pos);
    return le.valid(t) ? new le(t) : _.near(t);
  }
}
function sh(n) {
  return n.isAtom || n.spec.isolating || n.spec.createGapCursor;
}
function Av(n) {
  for (let e = n.depth; e >= 0; e--) {
    let t = n.index(e), r = n.node(e);
    if (t == 0) {
      if (r.type.spec.isolating)
        return !0;
      continue;
    }
    for (let o = r.child(t - 1); ; o = o.lastChild) {
      if (o.childCount == 0 && !o.inlineContent || sh(o.type))
        return !0;
      if (o.inlineContent)
        return !1;
    }
  }
  return !0;
}
function Ov(n) {
  for (let e = n.depth; e >= 0; e--) {
    let t = n.indexAfter(e), r = n.node(e);
    if (t == r.childCount) {
      if (r.type.spec.isolating)
        return !0;
      continue;
    }
    for (let o = r.child(t); ; o = o.firstChild) {
      if (o.childCount == 0 && !o.inlineContent || sh(o.type))
        return !0;
      if (o.inlineContent)
        return !1;
    }
  }
  return !0;
}
function Nv() {
  return new ue({
    props: {
      decorations: Iv,
      createSelectionBetween(n, e, t) {
        return e.pos == t.pos && le.valid(t) ? new le(t) : null;
      },
      handleClick: Dv,
      handleKeyDown: Rv,
      handleDOMEvents: { beforeinput: Pv }
    }
  });
}
const Rv = aa({
  ArrowLeft: po("horiz", -1),
  ArrowRight: po("horiz", 1),
  ArrowUp: po("vert", -1),
  ArrowDown: po("vert", 1)
});
function po(n, e) {
  const t = n == "vert" ? e > 0 ? "down" : "up" : e > 0 ? "right" : "left";
  return function(r, o, i) {
    let s = r.selection, l = e > 0 ? s.$to : s.$from, a = s.empty;
    if (s instanceof L) {
      if (!i.endOfTextblock(t) || l.depth == 0)
        return !1;
      a = !1, l = r.doc.resolve(e > 0 ? l.after() : l.before());
    }
    let c = le.findGapCursorFrom(l, e, a);
    return c ? (o && o(r.tr.setSelection(new le(c))), !0) : !1;
  };
}
function Dv(n, e, t) {
  if (!n || !n.editable)
    return !1;
  let r = n.state.doc.resolve(e);
  if (!le.valid(r))
    return !1;
  let o = n.posAtCoords({ left: t.clientX, top: t.clientY });
  return o && o.inside > -1 && I.isSelectable(n.state.doc.nodeAt(o.inside)) ? !1 : (n.dispatch(n.state.tr.setSelection(new le(r))), !0);
}
function Pv(n, e) {
  if (e.inputType != "insertCompositionText" || !(n.state.selection instanceof le))
    return !1;
  let { $from: t } = n.state.selection, r = t.parent.contentMatchAt(t.index()).findWrapping(n.state.schema.nodes.text);
  if (!r)
    return !1;
  let o = M.empty;
  for (let s = r.length - 1; s >= 0; s--)
    o = M.from(r[s].createAndFill(null, o));
  let i = n.state.tr.replace(t.pos, t.pos, new O(o, 0, 0));
  return i.setSelection(L.near(i.doc.resolve(t.pos + 1))), n.dispatch(i), !1;
}
function Iv(n) {
  if (!(n.selection instanceof le))
    return null;
  let e = document.createElement("div");
  return e.className = "ProseMirror-gapcursor", se.create(n.doc, [Ne.widget(n.selection.head, e, { key: "gapcursor" })]);
}
const Lv = xe.create({
  name: "gapCursor",
  addProseMirrorPlugins() {
    return [
      Nv()
    ];
  },
  extendNodeSchema(n) {
    var e;
    const t = {
      name: n.name,
      options: n.options,
      storage: n.storage
    };
    return {
      allowGapCursor: (e = W(R(n, "allowGapCursor", t))) !== null && e !== void 0 ? e : null
    };
  }
}), _v = Te.create({
  name: "hardBreak",
  addOptions() {
    return {
      keepMarks: !0,
      HTMLAttributes: {}
    };
  },
  inline: !0,
  group: "inline",
  selectable: !1,
  linebreakReplacement: !0,
  parseHTML() {
    return [
      { tag: "br" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["br", ne(this.options.HTMLAttributes, n)];
  },
  renderText() {
    return `
`;
  },
  addCommands() {
    return {
      setHardBreak: () => ({ commands: n, chain: e, state: t, editor: r }) => n.first([
        () => n.exitCode(),
        () => n.command(() => {
          const { selection: o, storedMarks: i } = t;
          if (o.$from.parent.type.spec.isolating)
            return !1;
          const { keepMarks: s } = this.options, { splittableMarks: l } = r.extensionManager, a = i || o.$to.parentOffset && o.$from.marks();
          return e().insertContent({ type: this.name }).command(({ tr: c, dispatch: u }) => {
            if (u && a && s) {
              const d = a.filter((f) => l.includes(f.type.name));
              c.ensureMarks(d);
            }
            return !0;
          }).run();
        })
      ])
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Enter": () => this.editor.commands.setHardBreak(),
      "Shift-Enter": () => this.editor.commands.setHardBreak()
    };
  }
}), Bv = Te.create({
  name: "heading",
  addOptions() {
    return {
      levels: [1, 2, 3, 4, 5, 6],
      HTMLAttributes: {}
    };
  },
  content: "inline*",
  group: "block",
  defining: !0,
  addAttributes() {
    return {
      level: {
        default: 1,
        rendered: !1
      }
    };
  },
  parseHTML() {
    return this.options.levels.map((n) => ({
      tag: `h${n}`,
      attrs: { level: n }
    }));
  },
  renderHTML({ node: n, HTMLAttributes: e }) {
    return [`h${this.options.levels.includes(n.attrs.level) ? n.attrs.level : this.options.levels[0]}`, ne(this.options.HTMLAttributes, e), 0];
  },
  addCommands() {
    return {
      setHeading: (n) => ({ commands: e }) => this.options.levels.includes(n.level) ? e.setNode(this.name, n) : !1,
      toggleHeading: (n) => ({ commands: e }) => this.options.levels.includes(n.level) ? e.toggleNode(this.name, "paragraph", n) : !1
    };
  },
  addKeyboardShortcuts() {
    return this.options.levels.reduce((n, e) => ({
      ...n,
      [`Mod-Alt-${e}`]: () => this.editor.commands.toggleHeading({ level: e })
    }), {});
  },
  addInputRules() {
    return this.options.levels.map((n) => Cl({
      find: new RegExp(`^(#{${Math.min(...this.options.levels)},${n}})\\s$`),
      type: this.type,
      getAttributes: {
        level: n
      }
    }));
  }
});
var Qo = 200, pe = function() {
};
pe.prototype.append = function(e) {
  return e.length ? (e = pe.from(e), !this.length && e || e.length < Qo && this.leafAppend(e) || this.length < Qo && e.leafPrepend(this) || this.appendInner(e)) : this;
};
pe.prototype.prepend = function(e) {
  return e.length ? pe.from(e).append(this) : this;
};
pe.prototype.appendInner = function(e) {
  return new Fv(this, e);
};
pe.prototype.slice = function(e, t) {
  return e === void 0 && (e = 0), t === void 0 && (t = this.length), e >= t ? pe.empty : this.sliceInner(Math.max(0, e), Math.min(this.length, t));
};
pe.prototype.get = function(e) {
  if (!(e < 0 || e >= this.length))
    return this.getInner(e);
};
pe.prototype.forEach = function(e, t, r) {
  t === void 0 && (t = 0), r === void 0 && (r = this.length), t <= r ? this.forEachInner(e, t, r, 0) : this.forEachInvertedInner(e, t, r, 0);
};
pe.prototype.map = function(e, t, r) {
  t === void 0 && (t = 0), r === void 0 && (r = this.length);
  var o = [];
  return this.forEach(function(i, s) {
    return o.push(e(i, s));
  }, t, r), o;
};
pe.from = function(e) {
  return e instanceof pe ? e : e && e.length ? new lh(e) : pe.empty;
};
var lh = /* @__PURE__ */ (function(n) {
  function e(r) {
    n.call(this), this.values = r;
  }
  n && (e.__proto__ = n), e.prototype = Object.create(n && n.prototype), e.prototype.constructor = e;
  var t = { length: { configurable: !0 }, depth: { configurable: !0 } };
  return e.prototype.flatten = function() {
    return this.values;
  }, e.prototype.sliceInner = function(o, i) {
    return o == 0 && i == this.length ? this : new e(this.values.slice(o, i));
  }, e.prototype.getInner = function(o) {
    return this.values[o];
  }, e.prototype.forEachInner = function(o, i, s, l) {
    for (var a = i; a < s; a++)
      if (o(this.values[a], l + a) === !1)
        return !1;
  }, e.prototype.forEachInvertedInner = function(o, i, s, l) {
    for (var a = i - 1; a >= s; a--)
      if (o(this.values[a], l + a) === !1)
        return !1;
  }, e.prototype.leafAppend = function(o) {
    if (this.length + o.length <= Qo)
      return new e(this.values.concat(o.flatten()));
  }, e.prototype.leafPrepend = function(o) {
    if (this.length + o.length <= Qo)
      return new e(o.flatten().concat(this.values));
  }, t.length.get = function() {
    return this.values.length;
  }, t.depth.get = function() {
    return 0;
  }, Object.defineProperties(e.prototype, t), e;
})(pe);
pe.empty = new lh([]);
var Fv = /* @__PURE__ */ (function(n) {
  function e(t, r) {
    n.call(this), this.left = t, this.right = r, this.length = t.length + r.length, this.depth = Math.max(t.depth, r.depth) + 1;
  }
  return n && (e.__proto__ = n), e.prototype = Object.create(n && n.prototype), e.prototype.constructor = e, e.prototype.flatten = function() {
    return this.left.flatten().concat(this.right.flatten());
  }, e.prototype.getInner = function(r) {
    return r < this.left.length ? this.left.get(r) : this.right.get(r - this.left.length);
  }, e.prototype.forEachInner = function(r, o, i, s) {
    var l = this.left.length;
    if (o < l && this.left.forEachInner(r, o, Math.min(i, l), s) === !1 || i > l && this.right.forEachInner(r, Math.max(o - l, 0), Math.min(this.length, i) - l, s + l) === !1)
      return !1;
  }, e.prototype.forEachInvertedInner = function(r, o, i, s) {
    var l = this.left.length;
    if (o > l && this.right.forEachInvertedInner(r, o - l, Math.max(i, l) - l, s + l) === !1 || i < l && this.left.forEachInvertedInner(r, Math.min(o, l), i, s) === !1)
      return !1;
  }, e.prototype.sliceInner = function(r, o) {
    if (r == 0 && o == this.length)
      return this;
    var i = this.left.length;
    return o <= i ? this.left.slice(r, o) : r >= i ? this.right.slice(r - i, o - i) : this.left.slice(r, i).append(this.right.slice(0, o - i));
  }, e.prototype.leafAppend = function(r) {
    var o = this.right.leafAppend(r);
    if (o)
      return new e(this.left, o);
  }, e.prototype.leafPrepend = function(r) {
    var o = this.left.leafPrepend(r);
    if (o)
      return new e(o, this.right);
  }, e.prototype.appendInner = function(r) {
    return this.left.depth >= Math.max(this.right.depth, r.depth) + 1 ? new e(this.left, new e(this.right, r)) : new e(this, r);
  }, e;
})(pe);
const zv = 500;
class Ye {
  constructor(e, t) {
    this.items = e, this.eventCount = t;
  }
  // Pop the latest event off the branch's history and apply it
  // to a document transform.
  popEvent(e, t) {
    if (this.eventCount == 0)
      return null;
    let r = this.items.length;
    for (; ; r--)
      if (this.items.get(r - 1).selection) {
        --r;
        break;
      }
    let o, i;
    t && (o = this.remapping(r, this.items.length), i = o.maps.length);
    let s = e.tr, l, a, c = [], u = [];
    return this.items.forEach((d, f) => {
      if (!d.step) {
        o || (o = this.remapping(r, f + 1), i = o.maps.length), i--, u.push(d);
        return;
      }
      if (o) {
        u.push(new Rt(d.map));
        let h = d.step.map(o.slice(i)), p;
        h && s.maybeStep(h).doc && (p = s.mapping.maps[s.mapping.maps.length - 1], c.push(new Rt(p, void 0, void 0, c.length + u.length))), i--, p && o.appendMap(p, i);
      } else
        s.maybeStep(d.step);
      if (d.selection)
        return l = o ? d.selection.map(o.slice(i)) : d.selection, a = new Ye(this.items.slice(0, r).append(u.reverse().concat(c)), this.eventCount - 1), !1;
    }, this.items.length, 0), { remaining: a, transform: s, selection: l };
  }
  // Create a new branch with the given transform added.
  addTransform(e, t, r, o) {
    let i = [], s = this.eventCount, l = this.items, a = !o && l.length ? l.get(l.length - 1) : null;
    for (let u = 0; u < e.steps.length; u++) {
      let d = e.steps[u].invert(e.docs[u]), f = new Rt(e.mapping.maps[u], d, t), h;
      (h = a && a.merge(f)) && (f = h, u ? i.pop() : l = l.slice(0, l.length - 1)), i.push(f), t && (s++, t = void 0), o || (a = f);
    }
    let c = s - r.depth;
    return c > Hv && (l = $v(l, c), s -= c), new Ye(l.append(i), s);
  }
  remapping(e, t) {
    let r = new Nr();
    return this.items.forEach((o, i) => {
      let s = o.mirrorOffset != null && i - o.mirrorOffset >= e ? r.maps.length - o.mirrorOffset : void 0;
      r.appendMap(o.map, s);
    }, e, t), r;
  }
  addMaps(e) {
    return this.eventCount == 0 ? this : new Ye(this.items.append(e.map((t) => new Rt(t))), this.eventCount);
  }
  // When the collab module receives remote changes, the history has
  // to know about those, so that it can adjust the steps that were
  // rebased on top of the remote changes, and include the position
  // maps for the remote changes in its array of items.
  rebased(e, t) {
    if (!this.eventCount)
      return this;
    let r = [], o = Math.max(0, this.items.length - t), i = e.mapping, s = e.steps.length, l = this.eventCount;
    this.items.forEach((f) => {
      f.selection && l--;
    }, o);
    let a = t;
    this.items.forEach((f) => {
      let h = i.getMirror(--a);
      if (h == null)
        return;
      s = Math.min(s, h);
      let p = i.maps[h];
      if (f.step) {
        let m = e.steps[h].invert(e.docs[h]), g = f.selection && f.selection.map(i.slice(a + 1, h));
        g && l++, r.push(new Rt(p, m, g));
      } else
        r.push(new Rt(p));
    }, o);
    let c = [];
    for (let f = t; f < s; f++)
      c.push(new Rt(i.maps[f]));
    let u = this.items.slice(0, o).append(c).append(r), d = new Ye(u, l);
    return d.emptyItemCount() > zv && (d = d.compress(this.items.length - r.length)), d;
  }
  emptyItemCount() {
    let e = 0;
    return this.items.forEach((t) => {
      t.step || e++;
    }), e;
  }
  // Compressing a branch means rewriting it to push the air (map-only
  // items) out. During collaboration, these naturally accumulate
  // because each remote change adds one. The `upto` argument is used
  // to ensure that only the items below a given level are compressed,
  // because `rebased` relies on a clean, untouched set of items in
  // order to associate old items with rebased steps.
  compress(e = this.items.length) {
    let t = this.remapping(0, e), r = t.maps.length, o = [], i = 0;
    return this.items.forEach((s, l) => {
      if (l >= e)
        o.push(s), s.selection && i++;
      else if (s.step) {
        let a = s.step.map(t.slice(r)), c = a && a.getMap();
        if (r--, c && t.appendMap(c, r), a) {
          let u = s.selection && s.selection.map(t.slice(r));
          u && i++;
          let d = new Rt(c.invert(), a, u), f, h = o.length - 1;
          (f = o.length && o[h].merge(d)) ? o[h] = f : o.push(d);
        }
      } else s.map && r--;
    }, this.items.length, 0), new Ye(pe.from(o.reverse()), i);
  }
}
Ye.empty = new Ye(pe.empty, 0);
function $v(n, e) {
  let t;
  return n.forEach((r, o) => {
    if (r.selection && e-- == 0)
      return t = o, !1;
  }), n.slice(t);
}
let Rt = class ah {
  constructor(e, t, r, o) {
    this.map = e, this.step = t, this.selection = r, this.mirrorOffset = o;
  }
  merge(e) {
    if (this.step && e.step && !e.selection) {
      let t = e.step.merge(this.step);
      if (t)
        return new ah(t.getMap().invert(), t, this.selection);
    }
  }
};
class _t {
  constructor(e, t, r, o, i) {
    this.done = e, this.undone = t, this.prevRanges = r, this.prevTime = o, this.prevComposition = i;
  }
}
const Hv = 20;
function Wv(n, e, t, r) {
  let o = t.getMeta(yn), i;
  if (o)
    return o.historyState;
  t.getMeta(Uv) && (n = new _t(n.done, n.undone, null, 0, -1));
  let s = t.getMeta("appendedTransaction");
  if (t.steps.length == 0)
    return n;
  if (s && s.getMeta(yn))
    return s.getMeta(yn).redo ? new _t(n.done.addTransform(t, void 0, r, Do(e)), n.undone, bu(t.mapping.maps), n.prevTime, n.prevComposition) : new _t(n.done, n.undone.addTransform(t, void 0, r, Do(e)), null, n.prevTime, n.prevComposition);
  if (t.getMeta("addToHistory") !== !1 && !(s && s.getMeta("addToHistory") === !1)) {
    let l = t.getMeta("composition"), a = n.prevTime == 0 || !s && n.prevComposition != l && (n.prevTime < (t.time || 0) - r.newGroupDelay || !Vv(t, n.prevRanges)), c = s ? _s(n.prevRanges, t.mapping) : bu(t.mapping.maps);
    return new _t(n.done.addTransform(t, a ? e.selection.getBookmark() : void 0, r, Do(e)), Ye.empty, c, t.time, l ?? n.prevComposition);
  } else return (i = t.getMeta("rebased")) ? new _t(n.done.rebased(t, i), n.undone.rebased(t, i), _s(n.prevRanges, t.mapping), n.prevTime, n.prevComposition) : new _t(n.done.addMaps(t.mapping.maps), n.undone.addMaps(t.mapping.maps), _s(n.prevRanges, t.mapping), n.prevTime, n.prevComposition);
}
function Vv(n, e) {
  if (!e)
    return !1;
  if (!n.docChanged)
    return !0;
  let t = !1;
  return n.mapping.maps[0].forEach((r, o) => {
    for (let i = 0; i < e.length; i += 2)
      r <= e[i + 1] && o >= e[i] && (t = !0);
  }), t;
}
function bu(n) {
  let e = [];
  for (let t = n.length - 1; t >= 0 && e.length == 0; t--)
    n[t].forEach((r, o, i, s) => e.push(i, s));
  return e;
}
function _s(n, e) {
  if (!n)
    return null;
  let t = [];
  for (let r = 0; r < n.length; r += 2) {
    let o = e.map(n[r], 1), i = e.map(n[r + 1], -1);
    o <= i && t.push(o, i);
  }
  return t;
}
function jv(n, e, t) {
  let r = Do(e), o = yn.get(e).spec.config, i = (t ? n.undone : n.done).popEvent(e, r);
  if (!i)
    return null;
  let s = i.selection.resolve(i.transform.doc), l = (t ? n.done : n.undone).addTransform(i.transform, e.selection.getBookmark(), o, r), a = new _t(t ? l : i.remaining, t ? i.remaining : l, null, 0, -1);
  return i.transform.setSelection(s).setMeta(yn, { redo: t, historyState: a });
}
let Bs = !1, wu = null;
function Do(n) {
  let e = n.plugins;
  if (wu != e) {
    Bs = !1, wu = e;
    for (let t = 0; t < e.length; t++)
      if (e[t].spec.historyPreserveItems) {
        Bs = !0;
        break;
      }
  }
  return Bs;
}
const yn = new Me("history"), Uv = new Me("closeHistory");
function Kv(n = {}) {
  return n = {
    depth: n.depth || 100,
    newGroupDelay: n.newGroupDelay || 500
  }, new ue({
    key: yn,
    state: {
      init() {
        return new _t(Ye.empty, Ye.empty, null, 0, -1);
      },
      apply(e, t, r) {
        return Wv(t, r, e, n);
      }
    },
    config: n,
    props: {
      handleDOMEvents: {
        beforeinput(e, t) {
          let r = t.inputType, o = r == "historyUndo" ? uh : r == "historyRedo" ? dh : null;
          return !o || !e.editable ? !1 : (t.preventDefault(), o(e.state, e.dispatch));
        }
      }
    }
  });
}
function ch(n, e) {
  return (t, r) => {
    let o = yn.getState(t);
    if (!o || (n ? o.undone : o.done).eventCount == 0)
      return !1;
    if (r) {
      let i = jv(o, t, n);
      i && r(e ? i.scrollIntoView() : i);
    }
    return !0;
  };
}
const uh = ch(!1, !0), dh = ch(!0, !0), qv = xe.create({
  name: "history",
  addOptions() {
    return {
      depth: 100,
      newGroupDelay: 500
    };
  },
  addCommands() {
    return {
      undo: () => ({ state: n, dispatch: e }) => uh(n, e),
      redo: () => ({ state: n, dispatch: e }) => dh(n, e)
    };
  },
  addProseMirrorPlugins() {
    return [
      Kv(this.options)
    ];
  },
  addKeyboardShortcuts() {
    return {
      "Mod-z": () => this.editor.commands.undo(),
      "Shift-Mod-z": () => this.editor.commands.redo(),
      "Mod-y": () => this.editor.commands.redo(),
      // Russian keyboard layouts
      "Mod-я": () => this.editor.commands.undo(),
      "Shift-Mod-я": () => this.editor.commands.redo()
    };
  }
}), Gv = Te.create({
  name: "horizontalRule",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  group: "block",
  parseHTML() {
    return [{ tag: "hr" }];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["hr", ne(this.options.HTMLAttributes, n)];
  },
  addCommands() {
    return {
      setHorizontalRule: () => ({ chain: n, state: e }) => {
        if (!Dw(e, e.schema.nodes[this.name]))
          return !1;
        const { selection: t } = e, { $from: r, $to: o } = t, i = n();
        return r.parentOffset === 0 ? i.insertContentAt({
          from: Math.max(r.pos - 1, 0),
          to: o.pos
        }, {
          type: this.name
        }) : ew(t) ? i.insertContentAt(o.pos, {
          type: this.name
        }) : i.insertContent({ type: this.name }), i.command(({ tr: s, dispatch: l }) => {
          var a;
          if (l) {
            const { $to: c } = s.selection, u = c.end();
            if (c.nodeAfter)
              c.nodeAfter.isTextblock ? s.setSelection(L.create(s.doc, c.pos + 1)) : c.nodeAfter.isBlock ? s.setSelection(I.create(s.doc, c.pos)) : s.setSelection(L.create(s.doc, c.pos));
            else {
              const d = (a = c.parent.type.contentMatch.defaultType) === null || a === void 0 ? void 0 : a.create();
              d && (s.insert(u, d), s.setSelection(L.create(s.doc, u + 1)));
            }
            s.scrollIntoView();
          }
          return !0;
        }).run();
      }
    };
  },
  addInputRules() {
    return [
      th({
        find: /^(?:---|—-|___\s|\*\*\*\s)$/,
        type: this.type
      })
    ];
  }
}), Jv = /(?:^|\s)(\*(?!\s+\*)((?:[^*]+))\*(?!\s+\*))$/, Yv = /(?:^|\s)(\*(?!\s+\*)((?:[^*]+))\*(?!\s+\*))/g, Xv = /(?:^|\s)(_(?!\s+_)((?:[^_]+))_(?!\s+_))$/, Qv = /(?:^|\s)(_(?!\s+_)((?:[^_]+))_(?!\s+_))/g, Zv = Qe.create({
  name: "italic",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  parseHTML() {
    return [
      {
        tag: "em"
      },
      {
        tag: "i",
        getAttrs: (n) => n.style.fontStyle !== "normal" && null
      },
      {
        style: "font-style=normal",
        clearMark: (n) => n.type.name === this.name
      },
      {
        style: "font-style=italic"
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["em", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setItalic: () => ({ commands: n }) => n.setMark(this.name),
      toggleItalic: () => ({ commands: n }) => n.toggleMark(this.name),
      unsetItalic: () => ({ commands: n }) => n.unsetMark(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-i": () => this.editor.commands.toggleItalic(),
      "Mod-I": () => this.editor.commands.toggleItalic()
    };
  },
  addInputRules() {
    return [
      Yn({
        find: Jv,
        type: this.type
      }),
      Yn({
        find: Xv,
        type: this.type
      })
    ];
  },
  addPasteRules() {
    return [
      Sn({
        find: Yv,
        type: this.type
      }),
      Sn({
        find: Qv,
        type: this.type
      })
    ];
  }
}), eC = Te.create({
  name: "listItem",
  addOptions() {
    return {
      HTMLAttributes: {},
      bulletListTypeName: "bulletList",
      orderedListTypeName: "orderedList"
    };
  },
  content: "paragraph block*",
  defining: !0,
  parseHTML() {
    return [
      {
        tag: "li"
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["li", ne(this.options.HTMLAttributes, n), 0];
  },
  addKeyboardShortcuts() {
    return {
      Enter: () => this.editor.commands.splitListItem(this.name),
      Tab: () => this.editor.commands.sinkListItem(this.name),
      "Shift-Tab": () => this.editor.commands.liftListItem(this.name)
    };
  }
}), tC = "listItem", vu = "textStyle", Cu = /^(\d+)\.\s$/, nC = Te.create({
  name: "orderedList",
  addOptions() {
    return {
      itemTypeName: "listItem",
      HTMLAttributes: {},
      keepMarks: !1,
      keepAttributes: !1
    };
  },
  group: "block list",
  content() {
    return `${this.options.itemTypeName}+`;
  },
  addAttributes() {
    return {
      start: {
        default: 1,
        parseHTML: (n) => n.hasAttribute("start") ? parseInt(n.getAttribute("start") || "", 10) : 1
      },
      type: {
        default: null,
        parseHTML: (n) => n.getAttribute("type")
      }
    };
  },
  parseHTML() {
    return [
      {
        tag: "ol"
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    const { start: e, ...t } = n;
    return e === 1 ? ["ol", ne(this.options.HTMLAttributes, t), 0] : ["ol", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      toggleOrderedList: () => ({ commands: n, chain: e }) => this.options.keepAttributes ? e().toggleList(this.name, this.options.itemTypeName, this.options.keepMarks).updateAttributes(tC, this.editor.getAttributes(vu)).run() : n.toggleList(this.name, this.options.itemTypeName, this.options.keepMarks)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Shift-7": () => this.editor.commands.toggleOrderedList()
    };
  },
  addInputRules() {
    let n = Br({
      find: Cu,
      type: this.type,
      getAttributes: (e) => ({ start: +e[1] }),
      joinPredicate: (e, t) => t.childCount + t.attrs.start === +e[1]
    });
    return (this.options.keepMarks || this.options.keepAttributes) && (n = Br({
      find: Cu,
      type: this.type,
      keepMarks: this.options.keepMarks,
      keepAttributes: this.options.keepAttributes,
      getAttributes: (e) => ({ start: +e[1], ...this.editor.getAttributes(vu) }),
      joinPredicate: (e, t) => t.childCount + t.attrs.start === +e[1],
      editor: this.editor
    })), [
      n
    ];
  }
}), rC = Te.create({
  name: "paragraph",
  priority: 1e3,
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  group: "block",
  content: "inline*",
  parseHTML() {
    return [
      { tag: "p" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["p", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setParagraph: () => ({ commands: n }) => n.setNode(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Alt-0": () => this.editor.commands.setParagraph()
    };
  }
}), oC = /(?:^|\s)(~~(?!\s+~~)((?:[^~]+))~~(?!\s+~~))$/, iC = /(?:^|\s)(~~(?!\s+~~)((?:[^~]+))~~(?!\s+~~))/g, sC = Qe.create({
  name: "strike",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  parseHTML() {
    return [
      {
        tag: "s"
      },
      {
        tag: "del"
      },
      {
        tag: "strike"
      },
      {
        style: "text-decoration",
        consuming: !1,
        getAttrs: (n) => n.includes("line-through") ? {} : !1
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["s", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setStrike: () => ({ commands: n }) => n.setMark(this.name),
      toggleStrike: () => ({ commands: n }) => n.toggleMark(this.name),
      unsetStrike: () => ({ commands: n }) => n.unsetMark(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-Shift-s": () => this.editor.commands.toggleStrike()
    };
  },
  addInputRules() {
    return [
      Yn({
        find: oC,
        type: this.type
      })
    ];
  },
  addPasteRules() {
    return [
      Sn({
        find: iC,
        type: this.type
      })
    ];
  }
}), lC = Te.create({
  name: "text",
  group: "inline"
}), aC = xe.create({
  name: "starterKit",
  addExtensions() {
    const n = [];
    return this.options.bold !== !1 && n.push(mv.configure(this.options.bold)), this.options.blockquote !== !1 && n.push(uv.configure(this.options.blockquote)), this.options.bulletList !== !1 && n.push(yv.configure(this.options.bulletList)), this.options.code !== !1 && n.push(vv.configure(this.options.code)), this.options.codeBlock !== !1 && n.push(xv.configure(this.options.codeBlock)), this.options.document !== !1 && n.push(kv.configure(this.options.document)), this.options.dropcursor !== !1 && n.push(Tv.configure(this.options.dropcursor)), this.options.gapcursor !== !1 && n.push(Lv.configure(this.options.gapcursor)), this.options.hardBreak !== !1 && n.push(_v.configure(this.options.hardBreak)), this.options.heading !== !1 && n.push(Bv.configure(this.options.heading)), this.options.history !== !1 && n.push(qv.configure(this.options.history)), this.options.horizontalRule !== !1 && n.push(Gv.configure(this.options.horizontalRule)), this.options.italic !== !1 && n.push(Zv.configure(this.options.italic)), this.options.listItem !== !1 && n.push(eC.configure(this.options.listItem)), this.options.orderedList !== !1 && n.push(nC.configure(this.options.orderedList)), this.options.paragraph !== !1 && n.push(rC.configure(this.options.paragraph)), this.options.strike !== !1 && n.push(sC.configure(this.options.strike)), this.options.text !== !1 && n.push(lC.configure(this.options.text)), n;
  }
}), cC = Qe.create({
  name: "underline",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  parseHTML() {
    return [
      {
        tag: "u"
      },
      {
        style: "text-decoration",
        consuming: !1,
        getAttrs: (n) => n.includes("underline") ? {} : !1
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["u", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      setUnderline: () => ({ commands: n }) => n.setMark(this.name),
      toggleUnderline: () => ({ commands: n }) => n.toggleMark(this.name),
      unsetUnderline: () => ({ commands: n }) => n.unsetMark(this.name)
    };
  },
  addKeyboardShortcuts() {
    return {
      "Mod-u": () => this.editor.commands.toggleUnderline(),
      "Mod-U": () => this.editor.commands.toggleUnderline()
    };
  }
}), uC = "aaa1rp3bb0ott3vie4c1le2ogado5udhabi7c0ademy5centure6ountant0s9o1tor4d0s1ult4e0g1ro2tna4f0l1rica5g0akhan5ency5i0g1rbus3force5tel5kdn3l0ibaba4pay4lfinanz6state5y2sace3tom5m0azon4ericanexpress7family11x2fam3ica3sterdam8nalytics7droid5quan4z2o0l2partments8p0le4q0uarelle8r0ab1mco4chi3my2pa2t0e3s0da2ia2sociates9t0hleta5torney7u0ction5di0ble3o3spost5thor3o0s4w0s2x0a2z0ure5ba0by2idu3namex4d1k2r0celona5laycard4s5efoot5gains6seball5ketball8uhaus5yern5b0c1t1va3cg1n2d1e0ats2uty4er2rlin4st0buy5t2f1g1h0arti5i0ble3d1ke2ng0o3o1z2j1lack0friday9ockbuster8g1omberg7ue3m0s1w2n0pparibas9o0ats3ehringer8fa2m1nd2o0k0ing5sch2tik2on4t1utique6x2r0adesco6idgestone9oadway5ker3ther5ussels7s1t1uild0ers6siness6y1zz3v1w1y1z0h3ca0b1fe2l0l1vinklein9m0era3p2non3petown5ital0one8r0avan4ds2e0er0s4s2sa1e1h1ino4t0ering5holic7ba1n1re3c1d1enter4o1rn3f0a1d2g1h0anel2nel4rity4se2t2eap3intai5ristmas6ome4urch5i0priani6rcle4sco3tadel4i0c2y3k1l0aims4eaning6ick2nic1que6othing5ud3ub0med6m1n1o0ach3des3ffee4llege4ogne5m0mbank4unity6pany2re3uter5sec4ndos3struction8ulting7tact3ractors9oking4l1p2rsica5untry4pon0s4rses6pa2r0edit0card4union9icket5own3s1uise0s6u0isinella9v1w1x1y0mru3ou3z2dad1nce3ta1e1ing3sun4y2clk3ds2e0al0er2s3gree4livery5l1oitte5ta3mocrat6ntal2ist5si0gn4v2hl2iamonds6et2gital5rect0ory7scount3ver5h2y2j1k1m1np2o0cs1tor4g1mains5t1wnload7rive4tv2ubai3nlop4pont4rban5vag2r2z2earth3t2c0o2deka3u0cation8e1g1mail3erck5nergy4gineer0ing9terprises10pson4quipment8r0icsson6ni3s0q1tate5t1u0rovision8s2vents5xchange6pert3osed4ress5traspace10fage2il1rwinds6th3mily4n0s2rm0ers5shion4t3edex3edback6rrari3ero6i0delity5o2lm2nal1nce1ial7re0stone6mdale6sh0ing5t0ness6j1k1lickr3ghts4r2orist4wers5y2m1o0o0d1tball6rd1ex2sale4um3undation8x2r0ee1senius7l1ogans4ntier7tr2ujitsu5n0d2rniture7tbol5yi3ga0l0lery3o1up4me0s3p1rden4y2b0iz3d0n2e0a1nt0ing5orge5f1g0ee3h1i0ft0s3ves2ing5l0ass3e1obal2o4m0ail3bh2o1x2n1odaddy5ld0point6f2o0dyear5g0le4p1t1v2p1q1r0ainger5phics5tis4een3ipe3ocery4up4s1t1u0cci3ge2ide2tars5ru3w1y2hair2mburg5ngout5us3bo2dfc0bank7ealth0care8lp1sinki6re1mes5iphop4samitsu7tachi5v2k0t2m1n1ockey4ldings5iday5medepot5goods5s0ense7nda3rse3spital5t0ing5t0els3mail5use3w2r1sbc3t1u0ghes5yatt3undai7ibm2cbc2e1u2d1e0ee3fm2kano4l1m0amat4db2mo0bilien9n0c1dustries8finiti5o2g1k1stitute6urance4e4t0ernational10uit4vestments10o1piranga7q1r0ish4s0maili5t0anbul7t0au2v3jaguar4va3cb2e0ep2tzt3welry6io2ll2m0p2nj2o0bs1urg4t1y2p0morgan6rs3uegos4niper7kaufen5ddi3e0rryhotels6properties14fh2g1h1i0a1ds2m1ndle4tchen5wi3m1n1oeln3matsu5sher5p0mg2n2r0d1ed3uokgroup8w1y0oto4z2la0caixa5mborghini8er3nd0rover6xess5salle5t0ino3robe5w0yer5b1c1ds2ease3clerc5frak4gal2o2xus4gbt3i0dl2fe0insurance9style7ghting6ke2lly3mited4o2ncoln4k2ve1ing5k1lc1p2oan0s3cker3us3l1ndon4tte1o3ve3pl0financial11r1s1t0d0a3u0ndbeck6xe1ury5v1y2ma0drid4if1son4keup4n0agement7go3p1rket0ing3s4riott5shalls7ttel5ba2c0kinsey7d1e0d0ia3et2lbourne7me1orial6n0u2rckmsd7g1h1iami3crosoft7l1ni1t2t0subishi9k1l0b1s2m0a2n1o0bi0le4da2e1i1m1nash3ey2ster5rmon3tgage6scow4to0rcycles9v0ie4p1q1r1s0d2t0n1r2u0seum3ic4v1w1x1y1z2na0b1goya4me2vy3ba2c1e0c1t0bank4flix4work5ustar5w0s2xt0direct7us4f0l2g0o2hk2i0co2ke1on3nja3ssan1y5l1o0kia3rton4w0ruz3tv4p1r0a1w2tt2u1yc2z2obi1server7ffice5kinawa6layan0group9lo3m0ega4ne1g1l0ine5oo2pen3racle3nge4g0anic5igins6saka4tsuka4t2vh3pa0ge2nasonic7ris2s1tners4s1y3y2ccw3e0t2f0izer5g1h0armacy6d1ilips5one2to0graphy6s4ysio5ics1tet2ures6d1n0g1k2oneer5zza4k1l0ace2y0station9umbing5s3m1n0c2ohl2ker3litie5rn2st3r0axi3ess3ime3o0d0uctions8f1gressive8mo2perties3y5tection8u0dential9s1t1ub2w0c2y2qa1pon3uebec3st5racing4dio4e0ad1lestate6tor2y4cipes5d0stone5umbrella9hab3ise0n3t2liance6n0t0als5pair3ort3ublican8st0aurant8view0s5xroth6ich0ardli6oh3l1o1p2o0cks3deo3gers4om3s0vp3u0gby3hr2n2w0e2yukyu6sa0arland6fe0ty4kura4le1on3msclub4ung5ndvik0coromant12ofi4p1rl2s1ve2xo3b0i1s2c0b1haeffler7midt4olarships8ol3ule3warz5ience5ot3d1e0arch3t2cure1ity6ek2lect4ner3rvices6ven3w1x0y3fr2g1h0angrila6rp3ell3ia1ksha5oes2p0ping5uji3w3i0lk2na1gles5te3j1k0i0n2y0pe4l0ing4m0art3ile4n0cf3o0ccer3ial4ftbank4ware6hu2lar2utions7ng1y2y2pa0ce3ort2t3r0l2s1t0ada2ples4r1tebank4farm7c0group6ockholm6rage3e3ream4udio2y3yle4u0cks3pplies3y2ort5rf1gery5zuki5v1watch4iss4x1y0dney4stems6z2tab1ipei4lk2obao4rget4tamotors6r2too4x0i3c0i2d0k2eam2ch0nology8l1masek5nnis4va3f1g1h0d1eater2re6iaa2ckets5enda4ps2res2ol4j0maxx4x2k0maxx5l1m0all4n1o0day3kyo3ols3p1ray3shiba5tal3urs3wn2yota3s3r0ade1ing4ining5vel0ers0insurance16ust3v2t1ube2i1nes3shu4v0s2w1z2ua1bank3s2g1k1nicom3versity8o2ol2ps2s1y1z2va0cations7na1guard7c1e0gas3ntures6risign5mögensberater2ung14sicherung10t2g1i0ajes4deo3g1king4llas4n1p1rgin4sa1ion4va1o3laanderen9n1odka3lvo3te1ing3o2yage5u2wales2mart4ter4ng0gou5tch0es6eather0channel12bcam3er2site5d0ding5ibo2r3f1hoswho6ien2ki2lliamhill9n0dows4e1ners6me2olterskluwer11odside6rk0s2ld3w2s1tc1f3xbox3erox4ihuan4n2xx2yz3yachts4hoo3maxun5ndex5e1odobashi7ga2kohama6u0tube6t1un3za0ppos4ra3ero3ip2m1one3uerich6w2", dC = "ελ1υ2бг1ел3дети4ею2католик6ом3мкд2он1сква6онлайн5рг3рус2ф2сайт3рб3укр3қаз3հայ3ישראל5קום3ابوظبي5رامكو5لاردن4بحرين5جزائر5سعودية6عليان5مغرب5مارات5یران5بارت2زار4يتك3ھارت5تونس4سودان3رية5شبكة4عراق2ب2مان4فلسطين6قطر3كاثوليك6وم3مصر2ليسيا5وريتانيا7قع4همراه5پاکستان7ڀارت4कॉम3नेट3भारत0म्3ोत5संगठन5বাংলা5ভারত2ৰত4ਭਾਰਤ4ભારત4ଭାରତ4இந்தியா6லங்கை6சிங்கப்பூர்11భారత్5ಭಾರತ4ഭാരതം5ලංකා4คอม3ไทย3ລາວ3გე2みんな3アマゾン4クラウド4グーグル4コム2ストア3セール3ファッション6ポイント4世界2中信1国1國1文网3亚马逊3企业2佛山2信息2健康2八卦2公司1益2台湾1灣2商城1店1标2嘉里0大酒店5在线2大拿2天主教3娱乐2家電2广东2微博2慈善2我爱你3手机2招聘2政务1府2新加坡2闻2时尚2書籍2机构2淡马锡3游戏2澳門2点看2移动2组织机构4网址1店1站1络2联通2谷歌2购物2通販2集团2電訊盈科4飞利浦3食品2餐厅2香格里拉3港2닷넷1컴2삼성2한국2", kl = "numeric", El = "ascii", Ml = "alpha", Sr = "asciinumeric", gr = "alphanumeric", Tl = "domain", fh = "emoji", fC = "scheme", hC = "slashscheme", Fs = "whitespace";
function pC(n, e) {
  return n in e || (e[n] = []), e[n];
}
function fn(n, e, t) {
  e[kl] && (e[Sr] = !0, e[gr] = !0), e[El] && (e[Sr] = !0, e[Ml] = !0), e[Sr] && (e[gr] = !0), e[Ml] && (e[gr] = !0), e[gr] && (e[Tl] = !0), e[fh] && (e[Tl] = !0);
  for (const r in e) {
    const o = pC(r, t);
    o.indexOf(n) < 0 && o.push(n);
  }
}
function mC(n, e) {
  const t = {};
  for (const r in e)
    e[r].indexOf(n) >= 0 && (t[r] = !0);
  return t;
}
function Ie(n = null) {
  this.j = {}, this.jr = [], this.jd = null, this.t = n;
}
Ie.groups = {};
Ie.prototype = {
  accepts() {
    return !!this.t;
  },
  /**
   * Follow an existing transition from the given input to the next state.
   * Does not mutate.
   * @param {string} input character or token type to transition on
   * @returns {?State<T>} the next state, if any
   */
  go(n) {
    const e = this, t = e.j[n];
    if (t)
      return t;
    for (let r = 0; r < e.jr.length; r++) {
      const o = e.jr[r][0], i = e.jr[r][1];
      if (i && o.test(n))
        return i;
    }
    return e.jd;
  },
  /**
   * Whether the state has a transition for the given input. Set the second
   * argument to true to only look for an exact match (and not a default or
   * regular-expression-based transition)
   * @param {string} input
   * @param {boolean} exactOnly
   */
  has(n, e = !1) {
    return e ? n in this.j : !!this.go(n);
  },
  /**
   * Short for "transition all"; create a transition from the array of items
   * in the given list to the same final resulting state.
   * @param {string | string[]} inputs Group of inputs to transition on
   * @param {Transition<T> | State<T>} [next] Transition options
   * @param {Flags} [flags] Collections flags to add token to
   * @param {Collections<T>} [groups] Master list of token groups
   */
  ta(n, e, t, r) {
    for (let o = 0; o < n.length; o++)
      this.tt(n[o], e, t, r);
  },
  /**
   * Short for "take regexp transition"; defines a transition for this state
   * when it encounters a token which matches the given regular expression
   * @param {RegExp} regexp Regular expression transition (populate first)
   * @param {T | State<T>} [next] Transition options
   * @param {Flags} [flags] Collections flags to add token to
   * @param {Collections<T>} [groups] Master list of token groups
   * @returns {State<T>} taken after the given input
   */
  tr(n, e, t, r) {
    r = r || Ie.groups;
    let o;
    return e && e.j ? o = e : (o = new Ie(e), t && r && fn(e, t, r)), this.jr.push([n, o]), o;
  },
  /**
   * Short for "take transitions", will take as many sequential transitions as
   * the length of the given input and returns the
   * resulting final state.
   * @param {string | string[]} input
   * @param {T | State<T>} [next] Transition options
   * @param {Flags} [flags] Collections flags to add token to
   * @param {Collections<T>} [groups] Master list of token groups
   * @returns {State<T>} taken after the given input
   */
  ts(n, e, t, r) {
    let o = this;
    const i = n.length;
    if (!i)
      return o;
    for (let s = 0; s < i - 1; s++)
      o = o.tt(n[s]);
    return o.tt(n[i - 1], e, t, r);
  },
  /**
   * Short for "take transition", this is a method for building/working with
   * state machines.
   *
   * If a state already exists for the given input, returns it.
   *
   * If a token is specified, that state will emit that token when reached by
   * the linkify engine.
   *
   * If no state exists, it will be initialized with some default transitions
   * that resemble existing default transitions.
   *
   * If a state is given for the second argument, that state will be
   * transitioned to on the given input regardless of what that input
   * previously did.
   *
   * Specify a token group flags to define groups that this token belongs to.
   * The token will be added to corresponding entires in the given groups
   * object.
   *
   * @param {string} input character, token type to transition on
   * @param {T | State<T>} [next] Transition options
   * @param {Flags} [flags] Collections flags to add token to
   * @param {Collections<T>} [groups] Master list of groups
   * @returns {State<T>} taken after the given input
   */
  tt(n, e, t, r) {
    r = r || Ie.groups;
    const o = this;
    if (e && e.j)
      return o.j[n] = e, e;
    const i = e;
    let s, l = o.go(n);
    if (l ? (s = new Ie(), Object.assign(s.j, l.j), s.jr.push.apply(s.jr, l.jr), s.jd = l.jd, s.t = l.t) : s = new Ie(), i) {
      if (r)
        if (s.t && typeof s.t == "string") {
          const a = Object.assign(mC(s.t, r), t);
          fn(i, a, r);
        } else t && fn(i, t, r);
      s.t = i;
    }
    return o.j[n] = s, s;
  }
};
const H = (n, e, t, r, o) => n.ta(e, t, r, o), ie = (n, e, t, r, o) => n.tr(e, t, r, o), Su = (n, e, t, r, o) => n.ts(e, t, r, o), T = (n, e, t, r, o) => n.tt(e, t, r, o), wt = "WORD", Al = "UWORD", hh = "ASCIINUMERICAL", ph = "ALPHANUMERICAL", Fr = "LOCALHOST", Ol = "TLD", Nl = "UTLD", Po = "SCHEME", zn = "SLASH_SCHEME", Ea = "NUM", Rl = "WS", Ma = "NL", xr = "OPENBRACE", kr = "CLOSEBRACE", Zo = "OPENBRACKET", ei = "CLOSEBRACKET", ti = "OPENPAREN", ni = "CLOSEPAREN", ri = "OPENANGLEBRACKET", oi = "CLOSEANGLEBRACKET", ii = "FULLWIDTHLEFTPAREN", si = "FULLWIDTHRIGHTPAREN", li = "LEFTCORNERBRACKET", ai = "RIGHTCORNERBRACKET", ci = "LEFTWHITECORNERBRACKET", ui = "RIGHTWHITECORNERBRACKET", di = "FULLWIDTHLESSTHAN", fi = "FULLWIDTHGREATERTHAN", hi = "AMPERSAND", pi = "APOSTROPHE", mi = "ASTERISK", Bt = "AT", gi = "BACKSLASH", yi = "BACKTICK", bi = "CARET", zt = "COLON", Ta = "COMMA", wi = "DOLLAR", ot = "DOT", vi = "EQUALS", Aa = "EXCLAMATION", je = "HYPHEN", Er = "PERCENT", Ci = "PIPE", Si = "PLUS", xi = "POUND", Mr = "QUERY", Oa = "QUOTE", mh = "FULLWIDTHMIDDLEDOT", Na = "SEMI", it = "SLASH", Tr = "TILDE", ki = "UNDERSCORE", gh = "EMOJI", Ei = "SYM";
var yh = /* @__PURE__ */ Object.freeze({
  __proto__: null,
  ALPHANUMERICAL: ph,
  AMPERSAND: hi,
  APOSTROPHE: pi,
  ASCIINUMERICAL: hh,
  ASTERISK: mi,
  AT: Bt,
  BACKSLASH: gi,
  BACKTICK: yi,
  CARET: bi,
  CLOSEANGLEBRACKET: oi,
  CLOSEBRACE: kr,
  CLOSEBRACKET: ei,
  CLOSEPAREN: ni,
  COLON: zt,
  COMMA: Ta,
  DOLLAR: wi,
  DOT: ot,
  EMOJI: gh,
  EQUALS: vi,
  EXCLAMATION: Aa,
  FULLWIDTHGREATERTHAN: fi,
  FULLWIDTHLEFTPAREN: ii,
  FULLWIDTHLESSTHAN: di,
  FULLWIDTHMIDDLEDOT: mh,
  FULLWIDTHRIGHTPAREN: si,
  HYPHEN: je,
  LEFTCORNERBRACKET: li,
  LEFTWHITECORNERBRACKET: ci,
  LOCALHOST: Fr,
  NL: Ma,
  NUM: Ea,
  OPENANGLEBRACKET: ri,
  OPENBRACE: xr,
  OPENBRACKET: Zo,
  OPENPAREN: ti,
  PERCENT: Er,
  PIPE: Ci,
  PLUS: Si,
  POUND: xi,
  QUERY: Mr,
  QUOTE: Oa,
  RIGHTCORNERBRACKET: ai,
  RIGHTWHITECORNERBRACKET: ui,
  SCHEME: Po,
  SEMI: Na,
  SLASH: it,
  SLASH_SCHEME: zn,
  SYM: Ei,
  TILDE: Tr,
  TLD: Ol,
  UNDERSCORE: ki,
  UTLD: Nl,
  UWORD: Al,
  WORD: wt,
  WS: Rl
});
const yt = /[a-z]/, fr = new RegExp("\\p{L}", "u"), zs = new RegExp("\\p{Emoji}", "u"), bt = /\d/, $s = /\s/, xu = "\r", Hs = `
`, gC = "️", yC = "‍", Ws = "￼";
let mo = null, go = null;
function bC(n = []) {
  const e = {};
  Ie.groups = e;
  const t = new Ie();
  mo == null && (mo = ku(uC)), go == null && (go = ku(dC)), T(t, "'", pi), T(t, "{", xr), T(t, "}", kr), T(t, "[", Zo), T(t, "]", ei), T(t, "(", ti), T(t, ")", ni), T(t, "<", ri), T(t, ">", oi), T(t, "（", ii), T(t, "）", si), T(t, "「", li), T(t, "」", ai), T(t, "『", ci), T(t, "』", ui), T(t, "＜", di), T(t, "＞", fi), T(t, "&", hi), T(t, "*", mi), T(t, "@", Bt), T(t, "`", yi), T(t, "^", bi), T(t, ":", zt), T(t, ",", Ta), T(t, "$", wi), T(t, ".", ot), T(t, "=", vi), T(t, "!", Aa), T(t, "-", je), T(t, "%", Er), T(t, "|", Ci), T(t, "+", Si), T(t, "#", xi), T(t, "?", Mr), T(t, '"', Oa), T(t, "/", it), T(t, ";", Na), T(t, "~", Tr), T(t, "_", ki), T(t, "\\", gi), T(t, "・", mh);
  const r = ie(t, bt, Ea, {
    [kl]: !0
  });
  ie(r, bt, r);
  const o = ie(r, yt, hh, {
    [Sr]: !0
  }), i = ie(r, fr, ph, {
    [gr]: !0
  }), s = ie(t, yt, wt, {
    [El]: !0
  });
  ie(s, bt, o), ie(s, yt, s), ie(o, bt, o), ie(o, yt, o);
  const l = ie(t, fr, Al, {
    [Ml]: !0
  });
  ie(l, yt), ie(l, bt, i), ie(l, fr, l), ie(i, bt, i), ie(i, yt), ie(i, fr, i);
  const a = T(t, Hs, Ma, {
    [Fs]: !0
  }), c = T(t, xu, Rl, {
    [Fs]: !0
  }), u = ie(t, $s, Rl, {
    [Fs]: !0
  });
  T(t, Ws, u), T(c, Hs, a), T(c, Ws, u), ie(c, $s, u), T(u, xu), T(u, Hs), ie(u, $s, u), T(u, Ws, u);
  const d = ie(t, zs, gh, {
    [fh]: !0
  });
  T(d, "#"), ie(d, zs, d), T(d, gC, d);
  const f = T(d, yC);
  T(f, "#"), ie(f, zs, d);
  const h = [[yt, s], [bt, o]], p = [[yt, null], [fr, l], [bt, i]];
  for (let m = 0; m < mo.length; m++)
    Dt(t, mo[m], Ol, wt, h);
  for (let m = 0; m < go.length; m++)
    Dt(t, go[m], Nl, Al, p);
  fn(Ol, {
    tld: !0,
    ascii: !0
  }, e), fn(Nl, {
    utld: !0,
    alpha: !0
  }, e), Dt(t, "file", Po, wt, h), Dt(t, "mailto", Po, wt, h), Dt(t, "http", zn, wt, h), Dt(t, "https", zn, wt, h), Dt(t, "ftp", zn, wt, h), Dt(t, "ftps", zn, wt, h), fn(Po, {
    scheme: !0,
    ascii: !0
  }, e), fn(zn, {
    slashscheme: !0,
    ascii: !0
  }, e), n = n.sort((m, g) => m[0] > g[0] ? 1 : -1);
  for (let m = 0; m < n.length; m++) {
    const g = n[m][0], b = n[m][1] ? {
      [fC]: !0
    } : {
      [hC]: !0
    };
    g.indexOf("-") >= 0 ? b[Tl] = !0 : yt.test(g) ? bt.test(g) ? b[Sr] = !0 : b[El] = !0 : b[kl] = !0, Su(t, g, g, b);
  }
  return Su(t, "localhost", Fr, {
    ascii: !0
  }), t.jd = new Ie(Ei), {
    start: t,
    tokens: Object.assign({
      groups: e
    }, yh)
  };
}
function bh(n, e) {
  const t = wC(e.replace(/[A-Z]/g, (l) => l.toLowerCase())), r = t.length, o = [];
  let i = 0, s = 0;
  for (; s < r; ) {
    let l = n, a = null, c = 0, u = null, d = -1, f = -1;
    for (; s < r && (a = l.go(t[s])); )
      l = a, l.accepts() ? (d = 0, f = 0, u = l) : d >= 0 && (d += t[s].length, f++), c += t[s].length, i += t[s].length, s++;
    i -= d, s -= f, c -= d, o.push({
      t: u.t,
      // token type/name
      v: e.slice(i - c, i),
      // string value
      s: i - c,
      // start index
      e: i
      // end index (excluding)
    });
  }
  return o;
}
function wC(n) {
  const e = [], t = n.length;
  let r = 0;
  for (; r < t; ) {
    let o = n.charCodeAt(r), i, s = o < 55296 || o > 56319 || r + 1 === t || (i = n.charCodeAt(r + 1)) < 56320 || i > 57343 ? n[r] : n.slice(r, r + 2);
    e.push(s), r += s.length;
  }
  return e;
}
function Dt(n, e, t, r, o) {
  let i;
  const s = e.length;
  for (let l = 0; l < s - 1; l++) {
    const a = e[l];
    n.j[a] ? i = n.j[a] : (i = new Ie(r), i.jr = o.slice(), n.j[a] = i), n = i;
  }
  return i = new Ie(t), i.jr = o.slice(), n.j[e[s - 1]] = i, i;
}
function ku(n) {
  const e = [], t = [];
  let r = 0, o = "0123456789";
  for (; r < n.length; ) {
    let i = 0;
    for (; o.indexOf(n[r + i]) >= 0; )
      i++;
    if (i > 0) {
      e.push(t.join(""));
      for (let s = parseInt(n.substring(r, r + i), 10); s > 0; s--)
        t.pop();
      r += i;
    } else
      t.push(n[r]), r++;
  }
  return e;
}
const zr = {
  defaultProtocol: "http",
  events: null,
  format: Eu,
  formatHref: Eu,
  nl2br: !1,
  tagName: "a",
  target: null,
  rel: null,
  validate: !0,
  truncate: 1 / 0,
  className: null,
  attributes: null,
  ignoreTags: [],
  render: null
};
function Ra(n, e = null) {
  let t = Object.assign({}, zr);
  n && (t = Object.assign(t, n instanceof Ra ? n.o : n));
  const r = t.ignoreTags, o = [];
  for (let i = 0; i < r.length; i++)
    o.push(r[i].toUpperCase());
  this.o = t, e && (this.defaultRender = e), this.ignoreTags = o;
}
Ra.prototype = {
  o: zr,
  /**
   * @type string[]
   */
  ignoreTags: [],
  /**
   * @param {IntermediateRepresentation} ir
   * @returns {any}
   */
  defaultRender(n) {
    return n;
  },
  /**
   * Returns true or false based on whether a token should be displayed as a
   * link based on the user options.
   * @param {MultiToken} token
   * @returns {boolean}
   */
  check(n) {
    return this.get("validate", n.toString(), n);
  },
  // Private methods
  /**
   * Resolve an option's value based on the value of the option and the given
   * params. If operator and token are specified and the target option is
   * callable, automatically calls the function with the given argument.
   * @template {keyof Opts} K
   * @param {K} key Name of option to use
   * @param {string} [operator] will be passed to the target option if it's a
   * function. If not specified, RAW function value gets returned
   * @param {MultiToken} [token] The token from linkify.tokenize
   * @returns {Opts[K] | any}
   */
  get(n, e, t) {
    const r = e != null;
    let o = this.o[n];
    return o && (typeof o == "object" ? (o = t.t in o ? o[t.t] : zr[n], typeof o == "function" && r && (o = o(e, t))) : typeof o == "function" && r && (o = o(e, t.t, t)), o);
  },
  /**
   * @template {keyof Opts} L
   * @param {L} key Name of options object to use
   * @param {string} [operator]
   * @param {MultiToken} [token]
   * @returns {Opts[L] | any}
   */
  getObj(n, e, t) {
    let r = this.o[n];
    return typeof r == "function" && e != null && (r = r(e, t.t, t)), r;
  },
  /**
   * Convert the given token to a rendered element that may be added to the
   * calling-interface's DOM
   * @param {MultiToken} token Token to render to an HTML element
   * @returns {any} Render result; e.g., HTML string, DOM element, React
   *   Component, etc.
   */
  render(n) {
    const e = n.render(this);
    return (this.get("render", null, n) || this.defaultRender)(e, n.t, n);
  }
};
function Eu(n) {
  return n;
}
function wh(n, e) {
  this.t = "token", this.v = n, this.tk = e;
}
wh.prototype = {
  isLink: !1,
  /**
   * Return the string this token represents.
   * @return {string}
   */
  toString() {
    return this.v;
  },
  /**
   * What should the value for this token be in the `href` HTML attribute?
   * Returns the `.toString` value by default.
   * @param {string} [scheme]
   * @return {string}
   */
  toHref(n) {
    return this.toString();
  },
  /**
   * @param {Options} options Formatting options
   * @returns {string}
   */
  toFormattedString(n) {
    const e = this.toString(), t = n.get("truncate", e, this), r = n.get("format", e, this);
    return t && r.length > t ? r.substring(0, t) + "…" : r;
  },
  /**
   *
   * @param {Options} options
   * @returns {string}
   */
  toFormattedHref(n) {
    return n.get("formatHref", this.toHref(n.get("defaultProtocol")), this);
  },
  /**
   * The start index of this token in the original input string
   * @returns {number}
   */
  startIndex() {
    return this.tk[0].s;
  },
  /**
   * The end index of this token in the original input string (up to this
   * index but not including it)
   * @returns {number}
   */
  endIndex() {
    return this.tk[this.tk.length - 1].e;
  },
  /**
  	Returns an object  of relevant values for this token, which includes keys
  	* type - Kind of token ('url', 'email', etc.)
  	* value - Original text
  	* href - The value that should be added to the anchor tag's href
  		attribute
  		@method toObject
  	@param {string} [protocol] `'http'` by default
  */
  toObject(n = zr.defaultProtocol) {
    return {
      type: this.t,
      value: this.toString(),
      isLink: this.isLink,
      href: this.toHref(n),
      start: this.startIndex(),
      end: this.endIndex()
    };
  },
  /**
   *
   * @param {Options} options Formatting option
   */
  toFormattedObject(n) {
    return {
      type: this.t,
      value: this.toFormattedString(n),
      isLink: this.isLink,
      href: this.toFormattedHref(n),
      start: this.startIndex(),
      end: this.endIndex()
    };
  },
  /**
   * Whether this token should be rendered as a link according to the given options
   * @param {Options} options
   * @returns {boolean}
   */
  validate(n) {
    return n.get("validate", this.toString(), this);
  },
  /**
   * Return an object that represents how this link should be rendered.
   * @param {Options} options Formattinng options
   */
  render(n) {
    const e = this, t = this.toHref(n.get("defaultProtocol")), r = n.get("formatHref", t, this), o = n.get("tagName", t, e), i = this.toFormattedString(n), s = {}, l = n.get("className", t, e), a = n.get("target", t, e), c = n.get("rel", t, e), u = n.getObj("attributes", t, e), d = n.getObj("events", t, e);
    return s.href = r, l && (s.class = l), a && (s.target = a), c && (s.rel = c), u && Object.assign(s, u), {
      tagName: o,
      attributes: s,
      content: i,
      eventListeners: d
    };
  }
};
function qi(n, e) {
  class t extends wh {
    constructor(o, i) {
      super(o, i), this.t = n;
    }
  }
  for (const r in e)
    t.prototype[r] = e[r];
  return t.t = n, t;
}
const Mu = qi("email", {
  isLink: !0,
  toHref() {
    return "mailto:" + this.toString();
  }
}), Tu = qi("text"), vC = qi("nl"), yo = qi("url", {
  isLink: !0,
  /**
  	Lowercases relevant parts of the domain and adds the protocol if
  	required. Note that this will not escape unsafe HTML characters in the
  	URL.
  		@param {string} [scheme] default scheme (e.g., 'https')
  	@return {string} the full href
  */
  toHref(n = zr.defaultProtocol) {
    return this.hasProtocol() ? this.v : `${n}://${this.v}`;
  },
  /**
   * Check whether this URL token has a protocol
   * @return {boolean}
   */
  hasProtocol() {
    const n = this.tk;
    return n.length >= 2 && n[0].t !== Fr && n[1].t === zt;
  }
}), Ve = (n) => new Ie(n);
function CC({
  groups: n
}) {
  const e = n.domain.concat([hi, mi, Bt, gi, yi, bi, wi, vi, je, Ea, Er, Ci, Si, xi, it, Ei, Tr, ki]), t = [pi, zt, Ta, ot, Aa, Er, Mr, Oa, Na, ri, oi, xr, kr, ei, Zo, ti, ni, ii, si, li, ai, ci, ui, di, fi], r = [hi, pi, mi, gi, yi, bi, wi, vi, je, xr, kr, Er, Ci, Si, xi, Mr, it, Ei, Tr, ki], o = Ve(), i = T(o, Tr);
  H(i, r, i), H(i, n.domain, i);
  const s = Ve(), l = Ve(), a = Ve();
  H(o, n.domain, s), H(o, n.scheme, l), H(o, n.slashscheme, a), H(s, r, i), H(s, n.domain, s);
  const c = T(s, Bt);
  T(i, Bt, c), T(l, Bt, c), T(a, Bt, c);
  const u = T(i, ot);
  H(u, r, i), H(u, n.domain, i);
  const d = Ve();
  H(c, n.domain, d), H(d, n.domain, d);
  const f = T(d, ot);
  H(f, n.domain, d);
  const h = Ve(Mu);
  H(f, n.tld, h), H(f, n.utld, h), T(c, Fr, h);
  const p = T(d, je);
  T(p, je, p), H(p, n.domain, d), H(h, n.domain, d), T(h, ot, f), T(h, je, p);
  const m = T(h, zt);
  H(m, n.numeric, Mu);
  const g = T(s, je), y = T(s, ot);
  T(g, je, g), H(g, n.domain, s), H(y, r, i), H(y, n.domain, s);
  const b = Ve(yo);
  H(y, n.tld, b), H(y, n.utld, b), H(b, n.domain, s), H(b, r, i), T(b, ot, y), T(b, je, g), T(b, Bt, c);
  const w = T(b, zt), v = Ve(yo);
  H(w, n.numeric, v);
  const k = Ve(yo), E = Ve();
  H(k, e, k), H(k, t, E), H(E, e, k), H(E, t, E), T(b, it, k), T(v, it, k);
  const C = T(l, zt), A = T(a, zt), B = T(A, it), j = T(B, it);
  H(l, n.domain, s), T(l, ot, y), T(l, je, g), H(a, n.domain, s), T(a, ot, y), T(a, je, g), H(C, n.domain, k), T(C, it, k), T(C, Mr, k), H(j, n.domain, k), H(j, e, k), T(j, it, k);
  const G = [
    [xr, kr],
    // {}
    [Zo, ei],
    // []
    [ti, ni],
    // ()
    [ri, oi],
    // <>
    [ii, si],
    // （）
    [li, ai],
    // 「」
    [ci, ui],
    // 『』
    [di, fi]
    // ＜＞
  ];
  for (let Y = 0; Y < G.length; Y++) {
    const [X, q] = G[Y], oe = T(k, X);
    T(E, X, oe), T(oe, q, k);
    const U = Ve(yo);
    H(oe, e, U);
    const Q = Ve();
    H(oe, t), H(U, e, U), H(U, t, Q), H(Q, e, U), H(Q, t, Q), T(U, q, k), T(Q, q, k);
  }
  return T(o, Fr, b), T(o, Ma, vC), {
    start: o,
    tokens: yh
  };
}
function SC(n, e, t) {
  let r = t.length, o = 0, i = [], s = [];
  for (; o < r; ) {
    let l = n, a = null, c = null, u = 0, d = null, f = -1;
    for (; o < r && !(a = l.go(t[o].t)); )
      s.push(t[o++]);
    for (; o < r && (c = a || l.go(t[o].t)); )
      a = null, l = c, l.accepts() ? (f = 0, d = l) : f >= 0 && f++, o++, u++;
    if (f < 0)
      o -= u, o < r && (s.push(t[o]), o++);
    else {
      s.length > 0 && (i.push(Vs(Tu, e, s)), s = []), o -= f, u -= f;
      const h = d.t, p = t.slice(o - u, o);
      i.push(Vs(h, e, p));
    }
  }
  return s.length > 0 && i.push(Vs(Tu, e, s)), i;
}
function Vs(n, e, t) {
  const r = t[0].s, o = t[t.length - 1].e, i = e.slice(r, o);
  return new n(i, t);
}
const xC = typeof console < "u" && console && console.warn || (() => {
}), kC = "until manual call of linkify.init(). Register all schemes and plugins before invoking linkify the first time.", te = {
  scanner: null,
  parser: null,
  tokenQueue: [],
  pluginQueue: [],
  customSchemes: [],
  initialized: !1
};
function EC() {
  return Ie.groups = {}, te.scanner = null, te.parser = null, te.tokenQueue = [], te.pluginQueue = [], te.customSchemes = [], te.initialized = !1, te;
}
function Au(n, e = !1) {
  if (te.initialized && xC(`linkifyjs: already initialized - will not register custom scheme "${n}" ${kC}`), !/^[0-9a-z]+(-[0-9a-z]+)*$/.test(n))
    throw new Error(`linkifyjs: incorrect scheme format.
1. Must only contain digits, lowercase ASCII letters or "-"
2. Cannot start or end with "-"
3. "-" cannot repeat`);
  te.customSchemes.push([n, e]);
}
function MC() {
  te.scanner = bC(te.customSchemes);
  for (let n = 0; n < te.tokenQueue.length; n++)
    te.tokenQueue[n][1]({
      scanner: te.scanner
    });
  te.parser = CC(te.scanner.tokens);
  for (let n = 0; n < te.pluginQueue.length; n++)
    te.pluginQueue[n][1]({
      scanner: te.scanner,
      parser: te.parser
    });
  return te.initialized = !0, te;
}
function Da(n) {
  return te.initialized || MC(), SC(te.parser.start, n, bh(te.scanner.start, n));
}
Da.scan = bh;
function vh(n, e = null, t = null) {
  if (e && typeof e == "object") {
    if (t)
      throw Error(`linkifyjs: Invalid link type ${e}; must be a string`);
    t = e, e = null;
  }
  const r = new Ra(t), o = Da(n), i = [];
  for (let s = 0; s < o.length; s++) {
    const l = o[s];
    l.isLink && (!e || l.t === e) && r.check(l) && i.push(l.toFormattedObject(r));
  }
  return i;
}
const Pa = "[\0-   ᠎ -\u2029 　]", TC = new RegExp(Pa), AC = new RegExp(`${Pa}$`), OC = new RegExp(Pa, "g");
function NC(n) {
  return n.length === 1 ? n[0].isLink : n.length === 3 && n[1].isLink ? ["()", "[]"].includes(n[0].value + n[2].value) : !1;
}
function RC(n) {
  return new ue({
    key: new Me("autolink"),
    appendTransaction: (e, t, r) => {
      const o = e.some((c) => c.docChanged) && !t.doc.eq(r.doc), i = e.some((c) => c.getMeta("preventAutolink"));
      if (!o || i)
        return;
      const { tr: s } = r, l = U0(t.doc, [...e]);
      if (Q0(l).forEach(({ newRange: c }) => {
        const u = q0(r.doc, c, (h) => h.isTextblock);
        let d, f;
        if (u.length > 1)
          d = u[0], f = r.doc.textBetween(d.pos, d.pos + d.node.nodeSize, void 0, " ");
        else if (u.length) {
          const h = r.doc.textBetween(c.from, c.to, " ", " ");
          if (!AC.test(h))
            return;
          d = u[0], f = r.doc.textBetween(d.pos, c.to, void 0, " ");
        }
        if (d && f) {
          const h = f.split(TC).filter(Boolean);
          if (h.length <= 0)
            return !1;
          const p = h[h.length - 1], m = d.pos + f.lastIndexOf(p);
          if (!p)
            return !1;
          const g = Da(p).map((y) => y.toObject(n.defaultProtocol));
          if (!NC(g))
            return !1;
          g.filter((y) => y.isLink).map((y) => ({
            ...y,
            from: m + y.start + 1,
            to: m + y.end + 1
          })).filter((y) => r.schema.marks.code ? !r.doc.rangeHasMark(y.from, y.to, r.schema.marks.code) : !0).filter((y) => n.validate(y.value)).filter((y) => n.shouldAutoLink(y.value)).forEach((y) => {
            ba(y.from, y.to, r.doc).some((b) => b.mark.type === n.type) || s.addMark(y.from, y.to, n.type.create({
              href: y.href
            }));
          });
        }
      }), !!s.steps.length)
        return s;
    }
  });
}
function DC(n) {
  return new ue({
    key: new Me("handleClickLink"),
    props: {
      handleClick: (e, t, r) => {
        var o, i;
        if (r.button !== 0 || !e.editable)
          return !1;
        let s = r.target;
        const l = [];
        for (; s.nodeName !== "DIV"; )
          l.push(s), s = s.parentNode;
        if (!l.find((f) => f.nodeName === "A"))
          return !1;
        const a = eh(e.state, n.type.name), c = r.target, u = (o = c == null ? void 0 : c.href) !== null && o !== void 0 ? o : a.href, d = (i = c == null ? void 0 : c.target) !== null && i !== void 0 ? i : a.target;
        return c && u ? (window.open(u, d), !0) : !1;
      }
    }
  });
}
function PC(n) {
  return new ue({
    key: new Me("handlePasteLink"),
    props: {
      handlePaste: (e, t, r) => {
        const { state: o } = e, { selection: i } = o, { empty: s } = i;
        if (s)
          return !1;
        let l = "";
        r.content.forEach((c) => {
          l += c.textContent;
        });
        const a = vh(l, { defaultProtocol: n.defaultProtocol }).find((c) => c.isLink && c.value === l);
        return !l || !a ? !1 : n.editor.commands.setMark(n.type, {
          href: a.href
        });
      }
    }
  });
}
function on(n, e) {
  const t = [
    "http",
    "https",
    "ftp",
    "ftps",
    "mailto",
    "tel",
    "callto",
    "sms",
    "cid",
    "xmpp"
  ];
  return e && e.forEach((r) => {
    const o = typeof r == "string" ? r : r.scheme;
    o && t.push(o);
  }), !n || n.replace(OC, "").match(new RegExp(
    // eslint-disable-next-line no-useless-escape
    `^(?:(?:${t.join("|")}):|[^a-z]|[a-z0-9+.-]+(?:[^a-z+.-:]|$))`,
    "i"
  ));
}
const IC = Qe.create({
  name: "link",
  priority: 1e3,
  keepOnSplit: !1,
  exitable: !0,
  onCreate() {
    this.options.validate && !this.options.shouldAutoLink && (this.options.shouldAutoLink = this.options.validate, console.warn("The `validate` option is deprecated. Rename to the `shouldAutoLink` option instead.")), this.options.protocols.forEach((n) => {
      if (typeof n == "string") {
        Au(n);
        return;
      }
      Au(n.scheme, n.optionalSlashes);
    });
  },
  onDestroy() {
    EC();
  },
  inclusive() {
    return this.options.autolink;
  },
  addOptions() {
    return {
      openOnClick: !0,
      linkOnPaste: !0,
      autolink: !0,
      protocols: [],
      defaultProtocol: "http",
      HTMLAttributes: {
        target: "_blank",
        rel: "noopener noreferrer nofollow",
        class: null
      },
      isAllowedUri: (n, e) => !!on(n, e.protocols),
      validate: (n) => !!n,
      shouldAutoLink: (n) => !!n
    };
  },
  addAttributes() {
    return {
      href: {
        default: null,
        parseHTML(n) {
          return n.getAttribute("href");
        }
      },
      target: {
        default: this.options.HTMLAttributes.target
      },
      rel: {
        default: this.options.HTMLAttributes.rel
      },
      class: {
        default: this.options.HTMLAttributes.class
      }
    };
  },
  parseHTML() {
    return [
      {
        tag: "a[href]",
        getAttrs: (n) => {
          const e = n.getAttribute("href");
          return !e || !this.options.isAllowedUri(e, {
            defaultValidate: (t) => !!on(t, this.options.protocols),
            protocols: this.options.protocols,
            defaultProtocol: this.options.defaultProtocol
          }) ? !1 : null;
        }
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return this.options.isAllowedUri(n.href, {
      defaultValidate: (e) => !!on(e, this.options.protocols),
      protocols: this.options.protocols,
      defaultProtocol: this.options.defaultProtocol
    }) ? ["a", ne(this.options.HTMLAttributes, n), 0] : [
      "a",
      ne(this.options.HTMLAttributes, { ...n, href: "" }),
      0
    ];
  },
  addCommands() {
    return {
      setLink: (n) => ({ chain: e }) => {
        const { href: t } = n;
        return this.options.isAllowedUri(t, {
          defaultValidate: (r) => !!on(r, this.options.protocols),
          protocols: this.options.protocols,
          defaultProtocol: this.options.defaultProtocol
        }) ? e().setMark(this.name, n).setMeta("preventAutolink", !0).run() : !1;
      },
      toggleLink: (n) => ({ chain: e }) => {
        const { href: t } = n;
        return this.options.isAllowedUri(t, {
          defaultValidate: (r) => !!on(r, this.options.protocols),
          protocols: this.options.protocols,
          defaultProtocol: this.options.defaultProtocol
        }) ? e().toggleMark(this.name, n, { extendEmptyMarkRange: !0 }).setMeta("preventAutolink", !0).run() : !1;
      },
      unsetLink: () => ({ chain: n }) => n().unsetMark(this.name, { extendEmptyMarkRange: !0 }).setMeta("preventAutolink", !0).run()
    };
  },
  addPasteRules() {
    return [
      Sn({
        find: (n) => {
          const e = [];
          if (n) {
            const { protocols: t, defaultProtocol: r } = this.options, o = vh(n).filter((i) => i.isLink && this.options.isAllowedUri(i.value, {
              defaultValidate: (s) => !!on(s, t),
              protocols: t,
              defaultProtocol: r
            }));
            o.length && o.forEach((i) => e.push({
              text: i.value,
              data: {
                href: i.href
              },
              index: i.start
            }));
          }
          return e;
        },
        type: this.type,
        getAttributes: (n) => {
          var e;
          return {
            href: (e = n.data) === null || e === void 0 ? void 0 : e.href
          };
        }
      })
    ];
  },
  addProseMirrorPlugins() {
    const n = [], { protocols: e, defaultProtocol: t } = this.options;
    return this.options.autolink && n.push(RC({
      type: this.type,
      defaultProtocol: this.options.defaultProtocol,
      validate: (r) => this.options.isAllowedUri(r, {
        defaultValidate: (o) => !!on(o, e),
        protocols: e,
        defaultProtocol: t
      }),
      shouldAutoLink: this.options.shouldAutoLink
    })), this.options.openOnClick === !0 && n.push(DC({
      type: this.type
    })), this.options.linkOnPaste && n.push(PC({
      editor: this.editor,
      defaultProtocol: this.options.defaultProtocol,
      type: this.type
    })), n;
  }
}), LC = /(?:^|\s)(!\[(.+|:?)]\((\S+)(?:(?:\s+)["'](\S+)["'])?\))$/, _C = Te.create({
  name: "image",
  addOptions() {
    return {
      inline: !1,
      allowBase64: !1,
      HTMLAttributes: {}
    };
  },
  inline() {
    return this.options.inline;
  },
  group() {
    return this.options.inline ? "inline" : "block";
  },
  draggable: !0,
  addAttributes() {
    return {
      src: {
        default: null
      },
      alt: {
        default: null
      },
      title: {
        default: null
      }
    };
  },
  parseHTML() {
    return [
      {
        tag: this.options.allowBase64 ? "img[src]" : 'img[src]:not([src^="data:"])'
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["img", ne(this.options.HTMLAttributes, n)];
  },
  addCommands() {
    return {
      setImage: (n) => ({ commands: e }) => e.insertContent({
        type: this.name,
        attrs: n
      })
    };
  },
  addInputRules() {
    return [
      th({
        find: LC,
        type: this.type,
        getAttributes: (n) => {
          const [, , e, t, r] = n;
          return { src: t, alt: e, title: r };
        }
      })
    ];
  }
});
let Dl, Pl;
if (typeof WeakMap < "u") {
  let n = /* @__PURE__ */ new WeakMap();
  Dl = (e) => n.get(e), Pl = (e, t) => (n.set(e, t), t);
} else {
  const n = [];
  let t = 0;
  Dl = (r) => {
    for (let o = 0; o < n.length; o += 2) if (n[o] == r) return n[o + 1];
  }, Pl = (r, o) => (t == 10 && (t = 0), n[t++] = r, n[t++] = o);
}
var ae = class {
  constructor(n, e, t, r) {
    this.width = n, this.height = e, this.map = t, this.problems = r;
  }
  findCell(n) {
    for (let e = 0; e < this.map.length; e++) {
      const t = this.map[e];
      if (t != n) continue;
      const r = e % this.width, o = e / this.width | 0;
      let i = r + 1, s = o + 1;
      for (let l = 1; i < this.width && this.map[e + l] == t; l++) i++;
      for (let l = 1; s < this.height && this.map[e + this.width * l] == t; l++) s++;
      return {
        left: r,
        top: o,
        right: i,
        bottom: s
      };
    }
    throw new RangeError(`No cell with offset ${n} found`);
  }
  colCount(n) {
    for (let e = 0; e < this.map.length; e++) if (this.map[e] == n) return e % this.width;
    throw new RangeError(`No cell with offset ${n} found`);
  }
  nextCell(n, e, t) {
    const { left: r, right: o, top: i, bottom: s } = this.findCell(n);
    return e == "horiz" ? (t < 0 ? r == 0 : o == this.width) ? null : this.map[i * this.width + (t < 0 ? r - 1 : o)] : (t < 0 ? i == 0 : s == this.height) ? null : this.map[r + this.width * (t < 0 ? i - 1 : s)];
  }
  rectBetween(n, e) {
    const { left: t, right: r, top: o, bottom: i } = this.findCell(n), { left: s, right: l, top: a, bottom: c } = this.findCell(e);
    return {
      left: Math.min(t, s),
      top: Math.min(o, a),
      right: Math.max(r, l),
      bottom: Math.max(i, c)
    };
  }
  cellsInRect(n) {
    const e = [], t = {};
    for (let r = n.top; r < n.bottom; r++) for (let o = n.left; o < n.right; o++) {
      const i = r * this.width + o, s = this.map[i];
      t[s] || (t[s] = !0, !(o == n.left && o && this.map[i - 1] == s || r == n.top && r && this.map[i - this.width] == s) && e.push(s));
    }
    return e;
  }
  positionAt(n, e, t) {
    for (let r = 0, o = 0; ; r++) {
      const i = o + t.child(r).nodeSize;
      if (r == n) {
        let s = e + n * this.width;
        const l = (n + 1) * this.width;
        for (; s < l && this.map[s] < o; ) s++;
        return s == l ? i - 1 : this.map[s];
      }
      o = i;
    }
  }
  static get(n) {
    return Dl(n) || Pl(n, BC(n));
  }
};
function BC(n) {
  if (n.type.spec.tableRole != "table") throw new RangeError("Not a table node: " + n.type.name);
  const e = FC(n), t = n.childCount, r = [];
  let o = 0, i = null;
  const s = [];
  for (let c = 0, u = e * t; c < u; c++) r[c] = 0;
  for (let c = 0, u = 0; c < t; c++) {
    const d = n.child(c);
    u++;
    for (let p = 0; ; p++) {
      for (; o < r.length && r[o] != 0; ) o++;
      if (p == d.childCount) break;
      const m = d.child(p), { colspan: g, rowspan: y, colwidth: b } = m.attrs;
      for (let w = 0; w < y; w++) {
        if (w + c >= t) {
          (i || (i = [])).push({
            type: "overlong_rowspan",
            pos: u,
            n: y - w
          });
          break;
        }
        const v = o + w * e;
        for (let k = 0; k < g; k++) {
          r[v + k] == 0 ? r[v + k] = u : (i || (i = [])).push({
            type: "collision",
            row: c,
            pos: u,
            n: g - k
          });
          const E = b && b[k];
          if (E) {
            const C = (v + k) % e * 2, A = s[C];
            A == null || A != E && s[C + 1] == 1 ? (s[C] = E, s[C + 1] = 1) : A == E && s[C + 1]++;
          }
        }
      }
      o += g, u += m.nodeSize;
    }
    const f = (c + 1) * e;
    let h = 0;
    for (; o < f; ) r[o++] == 0 && h++;
    h && (i || (i = [])).push({
      type: "missing",
      row: c,
      n: h
    }), u++;
  }
  (e === 0 || t === 0) && (i || (i = [])).push({ type: "zero_sized" });
  const l = new ae(e, t, r, i);
  let a = !1;
  for (let c = 0; !a && c < s.length; c += 2) s[c] != null && s[c + 1] < t && (a = !0);
  return a && zC(l, s, n), l;
}
function FC(n) {
  let e = -1, t = !1;
  for (let r = 0; r < n.childCount; r++) {
    const o = n.child(r);
    let i = 0;
    if (t) for (let s = 0; s < r; s++) {
      const l = n.child(s);
      for (let a = 0; a < l.childCount; a++) {
        const c = l.child(a);
        s + c.attrs.rowspan > r && (i += c.attrs.colspan);
      }
    }
    for (let s = 0; s < o.childCount; s++) {
      const l = o.child(s);
      i += l.attrs.colspan, l.attrs.rowspan > 1 && (t = !0);
    }
    e == -1 ? e = i : e != i && (e = Math.max(e, i));
  }
  return e;
}
function zC(n, e, t) {
  n.problems || (n.problems = []);
  const r = {};
  for (let o = 0; o < n.map.length; o++) {
    const i = n.map[o];
    if (r[i]) continue;
    r[i] = !0;
    const s = t.nodeAt(i);
    if (!s) throw new RangeError(`No cell with offset ${i} found`);
    let l = null;
    const a = s.attrs;
    for (let c = 0; c < a.colspan; c++) {
      const u = e[(o + c) % n.width * 2];
      u != null && (!a.colwidth || a.colwidth[c] != u) && ((l || (l = $C(a)))[c] = u);
    }
    l && n.problems.unshift({
      type: "colwidth mismatch",
      pos: i,
      colwidth: l
    });
  }
}
function $C(n) {
  if (n.colwidth) return n.colwidth.slice();
  const e = [];
  for (let t = 0; t < n.colspan; t++) e.push(0);
  return e;
}
function ke(n) {
  let e = n.cached.tableNodeTypes;
  if (!e) {
    e = n.cached.tableNodeTypes = {};
    for (const t in n.nodes) {
      const r = n.nodes[t], o = r.spec.tableRole;
      o && (e[o] = r);
    }
  }
  return e;
}
const $t = new Me("selectingCells");
function kn(n) {
  for (let e = n.depth - 1; e > 0; e--) if (n.node(e).type.spec.tableRole == "row") return n.node(0).resolve(n.before(e + 1));
  return null;
}
function HC(n) {
  for (let e = n.depth; e > 0; e--) {
    const t = n.node(e).type.spec.tableRole;
    if (t === "cell" || t === "header_cell") return n.node(e);
  }
  return null;
}
function tt(n) {
  const e = n.selection.$head;
  for (let t = e.depth; t > 0; t--) if (e.node(t).type.spec.tableRole == "row") return !0;
  return !1;
}
function Gi(n) {
  const e = n.selection;
  if ("$anchorCell" in e && e.$anchorCell) return e.$anchorCell.pos > e.$headCell.pos ? e.$anchorCell : e.$headCell;
  if ("node" in e && e.node && e.node.type.spec.tableRole == "cell") return e.$anchor;
  const t = kn(e.$head) || WC(e.$head);
  if (t) return t;
  throw new RangeError(`No cell found around position ${e.head}`);
}
function WC(n) {
  for (let e = n.nodeAfter, t = n.pos; e; e = e.firstChild, t++) {
    const r = e.type.spec.tableRole;
    if (r == "cell" || r == "header_cell") return n.doc.resolve(t);
  }
  for (let e = n.nodeBefore, t = n.pos; e; e = e.lastChild, t--) {
    const r = e.type.spec.tableRole;
    if (r == "cell" || r == "header_cell") return n.doc.resolve(t - e.nodeSize);
  }
}
function Il(n) {
  return n.parent.type.spec.tableRole == "row" && !!n.nodeAfter;
}
function VC(n) {
  return n.node(0).resolve(n.pos + n.nodeAfter.nodeSize);
}
function Ia(n, e) {
  return n.depth == e.depth && n.pos >= e.start(-1) && n.pos <= e.end(-1);
}
function Ch(n, e, t) {
  const r = n.node(-1), o = ae.get(r), i = n.start(-1), s = o.nextCell(n.pos - i, e, t);
  return s == null ? null : n.node(0).resolve(i + s);
}
function En(n, e, t = 1) {
  const r = {
    ...n,
    colspan: n.colspan - t
  };
  return r.colwidth && (r.colwidth = r.colwidth.slice(), r.colwidth.splice(e, t), r.colwidth.some((o) => o > 0) || (r.colwidth = null)), r;
}
function Sh(n, e, t = 1) {
  const r = {
    ...n,
    colspan: n.colspan + t
  };
  if (r.colwidth) {
    r.colwidth = r.colwidth.slice();
    for (let o = 0; o < t; o++) r.colwidth.splice(e, 0, 0);
  }
  return r;
}
function jC(n, e, t) {
  const r = ke(e.type.schema).header_cell;
  for (let o = 0; o < n.height; o++) if (e.nodeAt(n.map[t + o * n.width]).type != r) return !1;
  return !0;
}
var ee = class vt extends _ {
  constructor(e, t = e) {
    const r = e.node(-1), o = ae.get(r), i = e.start(-1), s = o.rectBetween(e.pos - i, t.pos - i), l = e.node(0), a = o.cellsInRect(s).filter((u) => u != t.pos - i);
    a.unshift(t.pos - i);
    const c = a.map((u) => {
      const d = r.nodeAt(u);
      if (!d) throw new RangeError(`No cell with offset ${u} found`);
      const f = i + u + 1;
      return new Kd(l.resolve(f), l.resolve(f + d.content.size));
    });
    super(c[0].$from, c[0].$to, c), this.$anchorCell = e, this.$headCell = t;
  }
  map(e, t) {
    const r = e.resolve(t.map(this.$anchorCell.pos)), o = e.resolve(t.map(this.$headCell.pos));
    if (Il(r) && Il(o) && Ia(r, o)) {
      const i = this.$anchorCell.node(-1) != r.node(-1);
      return i && this.isRowSelection() ? vt.rowSelection(r, o) : i && this.isColSelection() ? vt.colSelection(r, o) : new vt(r, o);
    }
    return L.between(r, o);
  }
  content() {
    const e = this.$anchorCell.node(-1), t = ae.get(e), r = this.$anchorCell.start(-1), o = t.rectBetween(this.$anchorCell.pos - r, this.$headCell.pos - r), i = {}, s = [];
    for (let a = o.top; a < o.bottom; a++) {
      const c = [];
      for (let u = a * t.width + o.left, d = o.left; d < o.right; d++, u++) {
        const f = t.map[u];
        if (i[f]) continue;
        i[f] = !0;
        const h = t.findCell(f);
        let p = e.nodeAt(f);
        if (!p) throw new RangeError(`No cell with offset ${f} found`);
        const m = o.left - h.left, g = h.right - o.right;
        if (m > 0 || g > 0) {
          let y = p.attrs;
          if (m > 0 && (y = En(y, 0, m)), g > 0 && (y = En(y, y.colspan - g, g)), h.left < o.left) {
            if (p = p.type.createAndFill(y), !p) throw new RangeError(`Could not create cell with attrs ${JSON.stringify(y)}`);
          } else p = p.type.create(y, p.content);
        }
        if (h.top < o.top || h.bottom > o.bottom) {
          const y = {
            ...p.attrs,
            rowspan: Math.min(h.bottom, o.bottom) - Math.max(h.top, o.top)
          };
          h.top < o.top ? p = p.type.createAndFill(y) : p = p.type.create(y, p.content);
        }
        c.push(p);
      }
      s.push(e.child(a).copy(M.from(c)));
    }
    const l = this.isColSelection() && this.isRowSelection() ? e : s;
    return new O(M.from(l), 1, 1);
  }
  replace(e, t = O.empty) {
    const r = e.steps.length, o = this.ranges;
    for (let s = 0; s < o.length; s++) {
      const { $from: l, $to: a } = o[s], c = e.mapping.slice(r);
      e.replace(c.map(l.pos), c.map(a.pos), s ? O.empty : t);
    }
    const i = _.findFrom(e.doc.resolve(e.mapping.slice(r).map(this.to)), -1);
    i && e.setSelection(i);
  }
  replaceWith(e, t) {
    this.replace(e, new O(M.from(t), 0, 0));
  }
  forEachCell(e) {
    const t = this.$anchorCell.node(-1), r = ae.get(t), o = this.$anchorCell.start(-1), i = r.cellsInRect(r.rectBetween(this.$anchorCell.pos - o, this.$headCell.pos - o));
    for (let s = 0; s < i.length; s++) e(t.nodeAt(i[s]), o + i[s]);
  }
  isColSelection() {
    const e = this.$anchorCell.index(-1), t = this.$headCell.index(-1);
    if (Math.min(e, t) > 0) return !1;
    const r = e + this.$anchorCell.nodeAfter.attrs.rowspan, o = t + this.$headCell.nodeAfter.attrs.rowspan;
    return Math.max(r, o) == this.$headCell.node(-1).childCount;
  }
  static colSelection(e, t = e) {
    const r = e.node(-1), o = ae.get(r), i = e.start(-1), s = o.findCell(e.pos - i), l = o.findCell(t.pos - i), a = e.node(0);
    return s.top <= l.top ? (s.top > 0 && (e = a.resolve(i + o.map[s.left])), l.bottom < o.height && (t = a.resolve(i + o.map[o.width * (o.height - 1) + l.right - 1]))) : (l.top > 0 && (t = a.resolve(i + o.map[l.left])), s.bottom < o.height && (e = a.resolve(i + o.map[o.width * (o.height - 1) + s.right - 1]))), new vt(e, t);
  }
  isRowSelection() {
    const e = this.$anchorCell.node(-1), t = ae.get(e), r = this.$anchorCell.start(-1), o = t.colCount(this.$anchorCell.pos - r), i = t.colCount(this.$headCell.pos - r);
    if (Math.min(o, i) > 0) return !1;
    const s = o + this.$anchorCell.nodeAfter.attrs.colspan, l = i + this.$headCell.nodeAfter.attrs.colspan;
    return Math.max(s, l) == t.width;
  }
  eq(e) {
    return e instanceof vt && e.$anchorCell.pos == this.$anchorCell.pos && e.$headCell.pos == this.$headCell.pos;
  }
  static rowSelection(e, t = e) {
    const r = e.node(-1), o = ae.get(r), i = e.start(-1), s = o.findCell(e.pos - i), l = o.findCell(t.pos - i), a = e.node(0);
    return s.left <= l.left ? (s.left > 0 && (e = a.resolve(i + o.map[s.top * o.width])), l.right < o.width && (t = a.resolve(i + o.map[o.width * (l.top + 1) - 1]))) : (l.left > 0 && (t = a.resolve(i + o.map[l.top * o.width])), s.right < o.width && (e = a.resolve(i + o.map[o.width * (s.top + 1) - 1]))), new vt(e, t);
  }
  toJSON() {
    return {
      type: "cell",
      anchor: this.$anchorCell.pos,
      head: this.$headCell.pos
    };
  }
  static fromJSON(e, t) {
    return new vt(e.resolve(t.anchor), e.resolve(t.head));
  }
  static create(e, t, r = t) {
    return new vt(e.resolve(t), e.resolve(r));
  }
  getBookmark() {
    return new UC(this.$anchorCell.pos, this.$headCell.pos);
  }
};
ee.prototype.visible = !1;
_.jsonID("cell", ee);
var UC = class xh {
  constructor(e, t) {
    this.anchor = e, this.head = t;
  }
  map(e) {
    return new xh(e.map(this.anchor), e.map(this.head));
  }
  resolve(e) {
    const t = e.resolve(this.anchor), r = e.resolve(this.head);
    return t.parent.type.spec.tableRole == "row" && r.parent.type.spec.tableRole == "row" && t.index() < t.parent.childCount && r.index() < r.parent.childCount && Ia(t, r) ? new ee(t, r) : _.near(r, 1);
  }
};
function KC(n) {
  if (!(n.selection instanceof ee)) return null;
  const e = [];
  return n.selection.forEachCell((t, r) => {
    e.push(Ne.node(r, r + t.nodeSize, { class: "selectedCell" }));
  }), se.create(n.doc, e);
}
function qC({ $from: n, $to: e }) {
  if (n.pos == e.pos || n.pos < e.pos - 6) return !1;
  let t = n.pos, r = e.pos, o = n.depth;
  for (; o >= 0 && !(n.after(o + 1) < n.end(o)); o--, t++) ;
  for (let i = e.depth; i >= 0 && !(e.before(i + 1) > e.start(i)); i--, r--) ;
  return t == r && /row|table/.test(n.node(o).type.spec.tableRole);
}
function GC({ $from: n, $to: e }) {
  let t, r;
  for (let o = n.depth; o > 0; o--) {
    const i = n.node(o);
    if (i.type.spec.tableRole === "cell" || i.type.spec.tableRole === "header_cell") {
      t = i;
      break;
    }
  }
  for (let o = e.depth; o > 0; o--) {
    const i = e.node(o);
    if (i.type.spec.tableRole === "cell" || i.type.spec.tableRole === "header_cell") {
      r = i;
      break;
    }
  }
  return t !== r && e.parentOffset === 0;
}
function JC(n, e, t) {
  const r = (e || n).selection, o = (e || n).doc;
  let i, s;
  if (r instanceof I && (s = r.node.type.spec.tableRole)) {
    if (s == "cell" || s == "header_cell") i = ee.create(o, r.from);
    else if (s == "row") {
      const l = o.resolve(r.from + 1);
      i = ee.rowSelection(l, l);
    } else if (!t) {
      const l = ae.get(r.node), a = r.from + 1, c = a + l.map[l.width * l.height - 1];
      i = ee.create(o, a + 1, c);
    }
  } else r instanceof L && qC(r) ? i = L.create(o, r.from) : r instanceof L && GC(r) && (i = L.create(o, r.$from.start(), r.$from.end()));
  return i && (e || (e = n.tr)).setSelection(i), e;
}
const YC = new Me("fix-tables");
function kh(n, e, t, r) {
  const o = n.childCount, i = e.childCount;
  e: for (let s = 0, l = 0; s < i; s++) {
    const a = e.child(s);
    for (let c = l, u = Math.min(o, s + 3); c < u; c++) if (n.child(c) == a) {
      l = c + 1, t += a.nodeSize;
      continue e;
    }
    r(a, t), l < o && n.child(l).sameMarkup(a) ? kh(n.child(l), a, t + 1, r) : a.nodesBetween(0, a.content.size, r, t + 1), t += a.nodeSize;
  }
}
function Eh(n, e) {
  let t;
  const r = (o, i) => {
    o.type.spec.tableRole == "table" && (t = XC(n, o, i, t));
  };
  return e ? e.doc != n.doc && kh(e.doc, n.doc, 0, r) : n.doc.descendants(r), t;
}
function XC(n, e, t, r) {
  const o = ae.get(e);
  if (!o.problems) return r;
  r || (r = n.tr);
  const i = [];
  for (let a = 0; a < o.height; a++) i.push(0);
  for (let a = 0; a < o.problems.length; a++) {
    const c = o.problems[a];
    if (c.type == "collision") {
      const u = e.nodeAt(c.pos);
      if (!u) continue;
      const d = u.attrs;
      for (let f = 0; f < d.rowspan; f++) i[c.row + f] += c.n;
      r.setNodeMarkup(r.mapping.map(t + 1 + c.pos), null, En(d, d.colspan - c.n, c.n));
    } else if (c.type == "missing") i[c.row] += c.n;
    else if (c.type == "overlong_rowspan") {
      const u = e.nodeAt(c.pos);
      if (!u) continue;
      r.setNodeMarkup(r.mapping.map(t + 1 + c.pos), null, {
        ...u.attrs,
        rowspan: u.attrs.rowspan - c.n
      });
    } else if (c.type == "colwidth mismatch") {
      const u = e.nodeAt(c.pos);
      if (!u) continue;
      r.setNodeMarkup(r.mapping.map(t + 1 + c.pos), null, {
        ...u.attrs,
        colwidth: c.colwidth
      });
    } else if (c.type == "zero_sized") {
      const u = r.mapping.map(t);
      r.delete(u, u + e.nodeSize);
    }
  }
  let s, l;
  for (let a = 0; a < i.length; a++) i[a] && (s == null && (s = a), l = a);
  for (let a = 0, c = t + 1; a < o.height; a++) {
    const u = e.child(a), d = c + u.nodeSize, f = i[a];
    if (f > 0) {
      let h = "cell";
      u.firstChild && (h = u.firstChild.type.spec.tableRole);
      const p = [];
      for (let g = 0; g < f; g++) {
        const y = ke(n.schema)[h].createAndFill();
        y && p.push(y);
      }
      const m = (a == 0 || s == a - 1) && l == a ? c + 1 : d - 1;
      r.insert(r.mapping.map(m), p);
    }
    c = d;
  }
  return r.setMeta(YC, { fixTables: !0 });
}
function ht(n) {
  const e = n.selection, t = Gi(n), r = t.node(-1), o = t.start(-1), i = ae.get(r);
  return {
    ...e instanceof ee ? i.rectBetween(e.$anchorCell.pos - o, e.$headCell.pos - o) : i.findCell(t.pos - o),
    tableStart: o,
    map: i,
    table: r
  };
}
function Mh(n, { map: e, tableStart: t, table: r }, o) {
  let i = o > 0 ? -1 : 0;
  jC(e, r, o + i) && (i = o == 0 || o == e.width ? null : 0);
  for (let s = 0; s < e.height; s++) {
    const l = s * e.width + o;
    if (o > 0 && o < e.width && e.map[l - 1] == e.map[l]) {
      const a = e.map[l], c = r.nodeAt(a);
      n.setNodeMarkup(n.mapping.map(t + a), null, Sh(c.attrs, o - e.colCount(a))), s += c.attrs.rowspan - 1;
    } else {
      const a = i == null ? ke(r.type.schema).cell : r.nodeAt(e.map[l + i]).type, c = e.positionAt(s, o, r);
      n.insert(n.mapping.map(t + c), a.createAndFill());
    }
  }
  return n;
}
function QC(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n);
    e(Mh(n.tr, t, t.left));
  }
  return !0;
}
function ZC(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n);
    e(Mh(n.tr, t, t.right));
  }
  return !0;
}
function eS(n, { map: e, table: t, tableStart: r }, o) {
  const i = n.mapping.maps.length;
  for (let s = 0; s < e.height; ) {
    const l = s * e.width + o, a = e.map[l], c = t.nodeAt(a), u = c.attrs;
    if (o > 0 && e.map[l - 1] == a || o < e.width - 1 && e.map[l + 1] == a) n.setNodeMarkup(n.mapping.slice(i).map(r + a), null, En(u, o - e.colCount(a)));
    else {
      const d = n.mapping.slice(i).map(r + a);
      n.delete(d, d + c.nodeSize);
    }
    s += u.rowspan;
  }
}
function tS(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n), r = n.tr;
    if (t.left == 0 && t.right == t.map.width) return !1;
    for (let o = t.right - 1; eS(r, t, o), o != t.left; o--) {
      const i = t.tableStart ? r.doc.nodeAt(t.tableStart - 1) : r.doc;
      if (!i) throw new RangeError("No table found");
      t.table = i, t.map = ae.get(i);
    }
    e(r);
  }
  return !0;
}
function nS(n, e, t) {
  var r;
  const o = ke(e.type.schema).header_cell;
  for (let i = 0; i < n.width; i++) if (((r = e.nodeAt(n.map[i + t * n.width])) === null || r === void 0 ? void 0 : r.type) != o) return !1;
  return !0;
}
function Th(n, { map: e, tableStart: t, table: r }, o) {
  let i = t;
  for (let c = 0; c < o; c++) i += r.child(c).nodeSize;
  const s = [];
  let l = o > 0 ? -1 : 0;
  nS(e, r, o + l) && (l = o == 0 || o == e.height ? null : 0);
  for (let c = 0, u = e.width * o; c < e.width; c++, u++) if (o > 0 && o < e.height && e.map[u] == e.map[u - e.width]) {
    const d = e.map[u], f = r.nodeAt(d).attrs;
    n.setNodeMarkup(t + d, null, {
      ...f,
      rowspan: f.rowspan + 1
    }), c += f.colspan - 1;
  } else {
    var a;
    const d = l == null ? ke(r.type.schema).cell : (a = r.nodeAt(e.map[u + l * e.width])) === null || a === void 0 ? void 0 : a.type, f = d == null ? void 0 : d.createAndFill();
    f && s.push(f);
  }
  return n.insert(i, ke(r.type.schema).row.create(null, s)), n;
}
function rS(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n);
    e(Th(n.tr, t, t.top));
  }
  return !0;
}
function oS(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n);
    e(Th(n.tr, t, t.bottom));
  }
  return !0;
}
function iS(n, { map: e, table: t, tableStart: r }, o) {
  let i = 0;
  for (let c = 0; c < o; c++) i += t.child(c).nodeSize;
  const s = i + t.child(o).nodeSize, l = n.mapping.maps.length;
  n.delete(i + r, s + r);
  const a = /* @__PURE__ */ new Set();
  for (let c = 0, u = o * e.width; c < e.width; c++, u++) {
    const d = e.map[u];
    if (!a.has(d)) {
      if (a.add(d), o > 0 && d == e.map[u - e.width]) {
        const f = t.nodeAt(d).attrs;
        n.setNodeMarkup(n.mapping.slice(l).map(d + r), null, {
          ...f,
          rowspan: f.rowspan - 1
        }), c += f.colspan - 1;
      } else if (o < e.height && d == e.map[u + e.width]) {
        const f = t.nodeAt(d), h = f.attrs, p = f.type.create({
          ...h,
          rowspan: f.attrs.rowspan - 1
        }, f.content), m = e.positionAt(o + 1, c, t);
        n.insert(n.mapping.slice(l).map(r + m), p), c += h.colspan - 1;
      }
    }
  }
}
function sS(n, e) {
  if (!tt(n)) return !1;
  if (e) {
    const t = ht(n), r = n.tr;
    if (t.top == 0 && t.bottom == t.map.height) return !1;
    for (let o = t.bottom - 1; iS(r, t, o), o != t.top; o--) {
      const i = t.tableStart ? r.doc.nodeAt(t.tableStart - 1) : r.doc;
      if (!i) throw new RangeError("No table found");
      t.table = i, t.map = ae.get(t.table);
    }
    e(r);
  }
  return !0;
}
function Ou(n) {
  const e = n.content;
  return e.childCount == 1 && e.child(0).isTextblock && e.child(0).childCount == 0;
}
function lS({ width: n, height: e, map: t }, r) {
  let o = r.top * n + r.left, i = o, s = (r.bottom - 1) * n + r.left, l = o + (r.right - r.left - 1);
  for (let a = r.top; a < r.bottom; a++) {
    if (r.left > 0 && t[i] == t[i - 1] || r.right < n && t[l] == t[l + 1]) return !0;
    i += n, l += n;
  }
  for (let a = r.left; a < r.right; a++) {
    if (r.top > 0 && t[o] == t[o - n] || r.bottom < e && t[s] == t[s + n]) return !0;
    o++, s++;
  }
  return !1;
}
function Nu(n, e) {
  const t = n.selection;
  if (!(t instanceof ee) || t.$anchorCell.pos == t.$headCell.pos) return !1;
  const r = ht(n), { map: o } = r;
  if (lS(o, r)) return !1;
  if (e) {
    const i = n.tr, s = {};
    let l = M.empty, a, c;
    for (let u = r.top; u < r.bottom; u++) for (let d = r.left; d < r.right; d++) {
      const f = o.map[u * o.width + d], h = r.table.nodeAt(f);
      if (!(s[f] || !h))
        if (s[f] = !0, a == null)
          a = f, c = h;
        else {
          Ou(h) || (l = l.append(h.content));
          const p = i.mapping.map(f + r.tableStart);
          i.delete(p, p + h.nodeSize);
        }
    }
    if (a == null || c == null) return !0;
    if (i.setNodeMarkup(a + r.tableStart, null, {
      ...Sh(c.attrs, c.attrs.colspan, r.right - r.left - c.attrs.colspan),
      rowspan: r.bottom - r.top
    }), l.size > 0) {
      const u = a + 1 + c.content.size, d = Ou(c) ? a + 1 : u;
      i.replaceWith(d + r.tableStart, u + r.tableStart, l);
    }
    i.setSelection(new ee(i.doc.resolve(a + r.tableStart))), e(i);
  }
  return !0;
}
function Ru(n, e) {
  const t = ke(n.schema);
  return aS(({ node: r }) => t[r.type.spec.tableRole])(n, e);
}
function aS(n) {
  return (e, t) => {
    const r = e.selection;
    let o, i;
    if (r instanceof ee) {
      if (r.$anchorCell.pos != r.$headCell.pos) return !1;
      o = r.$anchorCell.nodeAfter, i = r.$anchorCell.pos;
    } else {
      var s;
      if (o = HC(r.$from), !o) return !1;
      i = (s = kn(r.$from)) === null || s === void 0 ? void 0 : s.pos;
    }
    if (o == null || i == null || o.attrs.colspan == 1 && o.attrs.rowspan == 1) return !1;
    if (t) {
      let l = o.attrs;
      const a = [], c = l.colwidth;
      l.rowspan > 1 && (l = {
        ...l,
        rowspan: 1
      }), l.colspan > 1 && (l = {
        ...l,
        colspan: 1
      });
      const u = ht(e), d = e.tr;
      for (let h = 0; h < u.right - u.left; h++) a.push(c ? {
        ...l,
        colwidth: c && c[h] ? [c[h]] : null
      } : l);
      let f;
      for (let h = u.top; h < u.bottom; h++) {
        let p = u.map.positionAt(h, u.left, u.table);
        h == u.top && (p += o.nodeSize);
        for (let m = u.left, g = 0; m < u.right; m++, g++)
          m == u.left && h == u.top || d.insert(f = d.mapping.map(p + u.tableStart, 1), n({
            node: o,
            row: h,
            col: m
          }).createAndFill(a[g]));
      }
      d.setNodeMarkup(i, n({
        node: o,
        row: u.top,
        col: u.left
      }), a[0]), r instanceof ee && d.setSelection(new ee(d.doc.resolve(r.$anchorCell.pos), f ? d.doc.resolve(f) : void 0)), t(d);
    }
    return !0;
  };
}
function cS(n, e) {
  return function(t, r) {
    if (!tt(t)) return !1;
    const o = Gi(t);
    if (o.nodeAfter.attrs[n] === e) return !1;
    if (r) {
      const i = t.tr;
      t.selection instanceof ee ? t.selection.forEachCell((s, l) => {
        s.attrs[n] !== e && i.setNodeMarkup(l, null, {
          ...s.attrs,
          [n]: e
        });
      }) : i.setNodeMarkup(o.pos, null, {
        ...o.nodeAfter.attrs,
        [n]: e
      }), r(i);
    }
    return !0;
  };
}
function uS(n) {
  return function(e, t) {
    if (!tt(e)) return !1;
    if (t) {
      const r = ke(e.schema), o = ht(e), i = e.tr, s = o.map.cellsInRect(n == "column" ? {
        left: o.left,
        top: 0,
        right: o.right,
        bottom: o.map.height
      } : n == "row" ? {
        left: 0,
        top: o.top,
        right: o.map.width,
        bottom: o.bottom
      } : o), l = s.map((a) => o.table.nodeAt(a));
      for (let a = 0; a < s.length; a++) l[a].type == r.header_cell && i.setNodeMarkup(o.tableStart + s[a], r.cell, l[a].attrs);
      if (i.steps.length === 0) for (let a = 0; a < s.length; a++) i.setNodeMarkup(o.tableStart + s[a], r.header_cell, l[a].attrs);
      t(i);
    }
    return !0;
  };
}
function Du(n, e, t) {
  const r = e.map.cellsInRect({
    left: 0,
    top: 0,
    right: n == "row" ? e.map.width : 1,
    bottom: n == "column" ? e.map.height : 1
  });
  for (let o = 0; o < r.length; o++) {
    const i = e.table.nodeAt(r[o]);
    if (i && i.type !== t.header_cell) return !1;
  }
  return !0;
}
function $r(n, e) {
  return e = e || { useDeprecatedLogic: !1 }, e.useDeprecatedLogic ? uS(n) : function(t, r) {
    if (!tt(t)) return !1;
    if (r) {
      const o = ke(t.schema), i = ht(t), s = t.tr, l = Du("row", i, o), a = Du("column", i, o), c = (n === "column" ? l : n === "row" && a) ? 1 : 0, u = n == "column" ? {
        left: 0,
        top: c,
        right: 1,
        bottom: i.map.height
      } : n == "row" ? {
        left: c,
        top: 0,
        right: i.map.width,
        bottom: 1
      } : i, d = n == "column" ? a ? o.cell : o.header_cell : n == "row" ? l ? o.cell : o.header_cell : o.cell;
      i.map.cellsInRect(u).forEach((f) => {
        const h = f + i.tableStart, p = s.doc.nodeAt(h);
        p && s.setNodeMarkup(h, d, p.attrs);
      }), r(s);
    }
    return !0;
  };
}
$r("row", { useDeprecatedLogic: !0 });
$r("column", { useDeprecatedLogic: !0 });
const dS = $r("cell", { useDeprecatedLogic: !0 });
function fS(n, e) {
  if (e < 0) {
    const t = n.nodeBefore;
    if (t) return n.pos - t.nodeSize;
    for (let r = n.index(-1) - 1, o = n.before(); r >= 0; r--) {
      const i = n.node(-1).child(r), s = i.lastChild;
      if (s) return o - 1 - s.nodeSize;
      o -= i.nodeSize;
    }
  } else {
    if (n.index() < n.parent.childCount - 1) return n.pos + n.nodeAfter.nodeSize;
    const t = n.node(-1);
    for (let r = n.indexAfter(-1), o = n.after(); r < t.childCount; r++) {
      const i = t.child(r);
      if (i.childCount) return o + 1;
      o += i.nodeSize;
    }
  }
  return null;
}
function Pu(n) {
  return function(e, t) {
    if (!tt(e)) return !1;
    const r = fS(Gi(e), n);
    if (r == null) return !1;
    if (t) {
      const o = e.doc.resolve(r);
      t(e.tr.setSelection(L.between(o, VC(o))).scrollIntoView());
    }
    return !0;
  };
}
function hS(n, e) {
  const t = n.selection.$anchor;
  for (let r = t.depth; r > 0; r--) if (t.node(r).type.spec.tableRole == "table")
    return e && e(n.tr.delete(t.before(r), t.after(r)).scrollIntoView()), !0;
  return !1;
}
function bo(n, e) {
  const t = n.selection;
  if (!(t instanceof ee)) return !1;
  if (e) {
    const r = n.tr, o = ke(n.schema).cell.createAndFill().content;
    t.forEachCell((i, s) => {
      i.content.eq(o) || r.replace(r.mapping.map(s + 1), r.mapping.map(s + i.nodeSize - 1), new O(o, 0, 0));
    }), r.docChanged && e(r);
  }
  return !0;
}
function pS(n) {
  if (n.size === 0) return null;
  let { content: e, openStart: t, openEnd: r } = n;
  for (; e.childCount == 1 && (t > 0 && r > 0 || e.child(0).type.spec.tableRole == "table"); )
    t--, r--, e = e.child(0).content;
  const o = e.child(0), i = o.type.spec.tableRole, s = o.type.schema, l = [];
  if (i == "row") for (let a = 0; a < e.childCount; a++) {
    let c = e.child(a).content;
    const u = a ? 0 : Math.max(0, t - 1), d = a < e.childCount - 1 ? 0 : Math.max(0, r - 1);
    (u || d) && (c = Ll(ke(s).row, new O(c, u, d)).content), l.push(c);
  }
  else if (i == "cell" || i == "header_cell") l.push(t || r ? Ll(ke(s).row, new O(e, t, r)).content : e);
  else return null;
  return mS(s, l);
}
function mS(n, e) {
  const t = [];
  for (let o = 0; o < e.length; o++) {
    const i = e[o];
    for (let s = i.childCount - 1; s >= 0; s--) {
      const { rowspan: l, colspan: a } = i.child(s).attrs;
      for (let c = o; c < o + l; c++) t[c] = (t[c] || 0) + a;
    }
  }
  let r = 0;
  for (let o = 0; o < t.length; o++) r = Math.max(r, t[o]);
  for (let o = 0; o < t.length; o++)
    if (o >= e.length && e.push(M.empty), t[o] < r) {
      const i = ke(n).cell.createAndFill(), s = [];
      for (let l = t[o]; l < r; l++) s.push(i);
      e[o] = e[o].append(M.from(s));
    }
  return {
    height: e.length,
    width: r,
    rows: e
  };
}
function Ll(n, e) {
  const t = n.createAndFill();
  return new Xl(t).replace(0, t.content.size, e).doc;
}
function gS({ width: n, height: e, rows: t }, r, o) {
  if (n != r) {
    const i = [], s = [];
    for (let l = 0; l < t.length; l++) {
      const a = t[l], c = [];
      for (let u = i[l] || 0, d = 0; u < r; d++) {
        let f = a.child(d % a.childCount);
        u + f.attrs.colspan > r && (f = f.type.createChecked(En(f.attrs, f.attrs.colspan, u + f.attrs.colspan - r), f.content)), c.push(f), u += f.attrs.colspan;
        for (let h = 1; h < f.attrs.rowspan; h++) i[l + h] = (i[l + h] || 0) + f.attrs.colspan;
      }
      s.push(M.from(c));
    }
    t = s, n = r;
  }
  if (e != o) {
    const i = [];
    for (let s = 0, l = 0; s < o; s++, l++) {
      const a = [], c = t[l % e];
      for (let u = 0; u < c.childCount; u++) {
        let d = c.child(u);
        s + d.attrs.rowspan > o && (d = d.type.create({
          ...d.attrs,
          rowspan: Math.max(1, o - d.attrs.rowspan)
        }, d.content)), a.push(d);
      }
      i.push(M.from(a));
    }
    t = i, e = o;
  }
  return {
    width: n,
    height: e,
    rows: t
  };
}
function yS(n, e, t, r, o, i, s) {
  const l = n.doc.type.schema, a = ke(l);
  let c, u;
  if (o > e.width) for (let d = 0, f = 0; d < e.height; d++) {
    const h = t.child(d);
    f += h.nodeSize;
    const p = [];
    let m;
    h.lastChild == null || h.lastChild.type == a.cell ? m = c || (c = a.cell.createAndFill()) : m = u || (u = a.header_cell.createAndFill());
    for (let g = e.width; g < o; g++) p.push(m);
    n.insert(n.mapping.slice(s).map(f - 1 + r), p);
  }
  if (i > e.height) {
    const d = [];
    for (let p = 0, m = (e.height - 1) * e.width; p < Math.max(e.width, o); p++) {
      const g = p >= e.width ? !1 : t.nodeAt(e.map[m + p]).type == a.header_cell;
      d.push(g ? u || (u = a.header_cell.createAndFill()) : c || (c = a.cell.createAndFill()));
    }
    const f = a.row.create(null, M.from(d)), h = [];
    for (let p = e.height; p < i; p++) h.push(f);
    n.insert(n.mapping.slice(s).map(r + t.nodeSize - 2), h);
  }
  return !!(c || u);
}
function Iu(n, e, t, r, o, i, s, l) {
  if (s == 0 || s == e.height) return !1;
  let a = !1;
  for (let c = o; c < i; c++) {
    const u = s * e.width + c, d = e.map[u];
    if (e.map[u - e.width] == d) {
      a = !0;
      const f = t.nodeAt(d), { top: h, left: p } = e.findCell(d);
      n.setNodeMarkup(n.mapping.slice(l).map(d + r), null, {
        ...f.attrs,
        rowspan: s - h
      }), n.insert(n.mapping.slice(l).map(e.positionAt(s, p, t)), f.type.createAndFill({
        ...f.attrs,
        rowspan: h + f.attrs.rowspan - s
      })), c += f.attrs.colspan - 1;
    }
  }
  return a;
}
function Lu(n, e, t, r, o, i, s, l) {
  if (s == 0 || s == e.width) return !1;
  let a = !1;
  for (let c = o; c < i; c++) {
    const u = c * e.width + s, d = e.map[u];
    if (e.map[u - 1] == d) {
      a = !0;
      const f = t.nodeAt(d), h = e.colCount(d), p = n.mapping.slice(l).map(d + r);
      n.setNodeMarkup(p, null, En(f.attrs, s - h, f.attrs.colspan - (s - h))), n.insert(p + f.nodeSize, f.type.createAndFill(En(f.attrs, 0, s - h))), c += f.attrs.rowspan - 1;
    }
  }
  return a;
}
function _u(n, e, t, r, o) {
  let i = t ? n.doc.nodeAt(t - 1) : n.doc;
  if (!i) throw new Error("No table found");
  let s = ae.get(i);
  const { top: l, left: a } = r, c = a + o.width, u = l + o.height, d = n.tr;
  let f = 0;
  function h() {
    if (i = t ? d.doc.nodeAt(t - 1) : d.doc, !i) throw new Error("No table found");
    s = ae.get(i), f = d.mapping.maps.length;
  }
  yS(d, s, i, t, c, u, f) && h(), Iu(d, s, i, t, a, c, l, f) && h(), Iu(d, s, i, t, a, c, u, f) && h(), Lu(d, s, i, t, l, u, a, f) && h(), Lu(d, s, i, t, l, u, c, f) && h();
  for (let p = l; p < u; p++) {
    const m = s.positionAt(p, a, i), g = s.positionAt(p, c, i);
    d.replace(d.mapping.slice(f).map(m + t), d.mapping.slice(f).map(g + t), new O(o.rows[p - l], 0, 0));
  }
  h(), d.setSelection(new ee(d.doc.resolve(t + s.positionAt(l, a, i)), d.doc.resolve(t + s.positionAt(u - 1, c - 1, i)))), e(d);
}
const bS = aa({
  ArrowLeft: wo("horiz", -1),
  ArrowRight: wo("horiz", 1),
  ArrowUp: wo("vert", -1),
  ArrowDown: wo("vert", 1),
  "Shift-ArrowLeft": vo("horiz", -1),
  "Shift-ArrowRight": vo("horiz", 1),
  "Shift-ArrowUp": vo("vert", -1),
  "Shift-ArrowDown": vo("vert", 1),
  Backspace: bo,
  "Mod-Backspace": bo,
  Delete: bo,
  "Mod-Delete": bo
});
function Io(n, e, t) {
  return t.eq(n.selection) ? !1 : (e && e(n.tr.setSelection(t).scrollIntoView()), !0);
}
function wo(n, e) {
  return (t, r, o) => {
    if (!o) return !1;
    const i = t.selection;
    if (i instanceof ee) return Io(t, r, _.near(i.$headCell, e));
    if (n != "horiz" && !i.empty) return !1;
    const s = Ah(o, n, e);
    if (s == null) return !1;
    if (n == "horiz") return Io(t, r, _.near(t.doc.resolve(i.head + e), e));
    {
      const l = t.doc.resolve(s), a = Ch(l, n, e);
      let c;
      return a ? c = _.near(a, 1) : e < 0 ? c = _.near(t.doc.resolve(l.before(-1)), -1) : c = _.near(t.doc.resolve(l.after(-1)), 1), Io(t, r, c);
    }
  };
}
function vo(n, e) {
  return (t, r, o) => {
    if (!o) return !1;
    const i = t.selection;
    let s;
    if (i instanceof ee) s = i;
    else {
      const a = Ah(o, n, e);
      if (a == null) return !1;
      s = new ee(t.doc.resolve(a));
    }
    const l = Ch(s.$headCell, n, e);
    return l ? Io(t, r, new ee(s.$anchorCell, l)) : !1;
  };
}
function wS(n, e) {
  const t = n.state.doc, r = kn(t.resolve(e));
  return r ? (n.dispatch(n.state.tr.setSelection(new ee(r))), !0) : !1;
}
function vS(n, e, t) {
  if (!tt(n.state)) return !1;
  let r = pS(t);
  const o = n.state.selection;
  if (o instanceof ee) {
    r || (r = {
      width: 1,
      height: 1,
      rows: [M.from(Ll(ke(n.state.schema).cell, t))]
    });
    const i = o.$anchorCell.node(-1), s = o.$anchorCell.start(-1), l = ae.get(i).rectBetween(o.$anchorCell.pos - s, o.$headCell.pos - s);
    return r = gS(r, l.right - l.left, l.bottom - l.top), _u(n.state, n.dispatch, s, l, r), !0;
  } else if (r) {
    const i = Gi(n.state), s = i.start(-1);
    return _u(n.state, n.dispatch, s, ae.get(i.node(-1)).findCell(i.pos - s), r), !0;
  } else return !1;
}
function CS(n, e) {
  var t;
  if (e.button != 0 || e.ctrlKey || e.metaKey) return;
  const r = Bu(n, e.target);
  let o;
  if (e.shiftKey && n.state.selection instanceof ee)
    i(n.state.selection.$anchorCell, e), e.preventDefault();
  else if (e.shiftKey && r && (o = kn(n.state.selection.$anchor)) != null && ((t = js(n, e)) === null || t === void 0 ? void 0 : t.pos) != o.pos)
    i(o, e), e.preventDefault();
  else if (!r) return;
  function i(a, c) {
    let u = js(n, c);
    const d = $t.getState(n.state) == null;
    if (!u || !Ia(a, u)) if (d) u = a;
    else return;
    const f = new ee(a, u);
    if (d || !n.state.selection.eq(f)) {
      const h = n.state.tr.setSelection(f);
      d && h.setMeta($t, a.pos), n.dispatch(h);
    }
  }
  function s() {
    n.root.removeEventListener("mouseup", s), n.root.removeEventListener("dragstart", s), n.root.removeEventListener("mousemove", l), $t.getState(n.state) != null && n.dispatch(n.state.tr.setMeta($t, -1));
  }
  function l(a) {
    const c = a, u = $t.getState(n.state);
    let d;
    if (u != null) d = n.state.doc.resolve(u);
    else if (Bu(n, c.target) != r && (d = js(n, e), !d))
      return s();
    d && i(d, c);
  }
  n.root.addEventListener("mouseup", s), n.root.addEventListener("dragstart", s), n.root.addEventListener("mousemove", l);
}
function Ah(n, e, t) {
  if (!(n.state.selection instanceof L)) return null;
  const { $head: r } = n.state.selection;
  for (let o = r.depth - 1; o >= 0; o--) {
    const i = r.node(o);
    if ((t < 0 ? r.index(o) : r.indexAfter(o)) != (t < 0 ? 0 : i.childCount)) return null;
    if (i.type.spec.tableRole == "cell" || i.type.spec.tableRole == "header_cell") {
      const s = r.before(o), l = e == "vert" ? t > 0 ? "down" : "up" : t > 0 ? "right" : "left";
      return n.endOfTextblock(l) ? s : null;
    }
  }
  return null;
}
function Bu(n, e) {
  for (; e && e != n.dom; e = e.parentNode) if (e.nodeName == "TD" || e.nodeName == "TH") return e;
  return null;
}
function js(n, e) {
  const t = n.posAtCoords({
    left: e.clientX,
    top: e.clientY
  });
  if (!t) return null;
  let { inside: r, pos: o } = t;
  return r >= 0 && kn(n.state.doc.resolve(r)) || kn(n.state.doc.resolve(o));
}
var SS = class {
  constructor(e, t) {
    this.node = e, this.defaultCellMinWidth = t, this.dom = document.createElement("div"), this.dom.className = "tableWrapper", this.table = this.dom.appendChild(document.createElement("table")), this.table.style.setProperty("--default-cell-min-width", `${t}px`), this.colgroup = this.table.appendChild(document.createElement("colgroup")), _l(e, this.colgroup, this.table, t), this.contentDOM = this.table.appendChild(document.createElement("tbody"));
  }
  update(e) {
    return e.type != this.node.type ? !1 : (this.node = e, _l(e, this.colgroup, this.table, this.defaultCellMinWidth), !0);
  }
  ignoreMutation(e) {
    return e.type == "attributes" && (e.target == this.table || this.colgroup.contains(e.target));
  }
};
function _l(n, e, t, r, o, i) {
  let s = 0, l = !0, a = e.firstChild;
  const c = n.firstChild;
  if (c) {
    for (let d = 0, f = 0; d < c.childCount; d++) {
      const { colspan: h, colwidth: p } = c.child(d).attrs;
      for (let m = 0; m < h; m++, f++) {
        const g = o == f ? i : p && p[m], y = g ? g + "px" : "";
        if (s += g || r, g || (l = !1), a)
          a.style.width != y && (a.style.width = y), a = a.nextSibling;
        else {
          const b = document.createElement("col");
          b.style.width = y, e.appendChild(b);
        }
      }
    }
    for (; a; ) {
      var u;
      const d = a.nextSibling;
      (u = a.parentNode) === null || u === void 0 || u.removeChild(a), a = d;
    }
    l ? (t.style.width = s + "px", t.style.minWidth = "") : (t.style.width = "", t.style.minWidth = s + "px");
  }
}
const Fe = new Me("tableColumnResizing");
function xS({ handleWidth: n = 5, cellMinWidth: e = 25, defaultCellMinWidth: t = 100, View: r = SS, lastColumnResizable: o = !0 } = {}) {
  const i = new ue({
    key: Fe,
    state: {
      init(s, l) {
        var a;
        const c = (a = i.spec) === null || a === void 0 || (a = a.props) === null || a === void 0 ? void 0 : a.nodeViews, u = ke(l.schema).table.name;
        return r && c && (c[u] = (d, f) => new r(d, t, f)), new kS(-1, !1);
      },
      apply(s, l) {
        return l.apply(s);
      }
    },
    props: {
      attributes: (s) => {
        const l = Fe.getState(s);
        return l && l.activeHandle > -1 ? { class: "resize-cursor" } : {};
      },
      handleDOMEvents: {
        mousemove: (s, l) => {
          ES(s, l, n, o);
        },
        mouseleave: (s) => {
          MS(s);
        },
        mousedown: (s, l) => {
          TS(s, l, e, t);
        }
      },
      decorations: (s) => {
        const l = Fe.getState(s);
        if (l && l.activeHandle > -1) return DS(s, l.activeHandle);
      },
      nodeViews: {}
    }
  });
  return i;
}
var kS = class Lo {
  constructor(e, t) {
    this.activeHandle = e, this.dragging = t;
  }
  apply(e) {
    const t = this, r = e.getMeta(Fe);
    if (r && r.setHandle != null) return new Lo(r.setHandle, !1);
    if (r && r.setDragging !== void 0) return new Lo(t.activeHandle, r.setDragging);
    if (t.activeHandle > -1 && e.docChanged) {
      let o = e.mapping.map(t.activeHandle, -1);
      return Il(e.doc.resolve(o)) || (o = -1), new Lo(o, t.dragging);
    }
    return t;
  }
};
function ES(n, e, t, r) {
  if (!n.editable) return;
  const o = Fe.getState(n.state);
  if (o && !o.dragging) {
    const i = OS(e.target);
    let s = -1;
    if (i) {
      const { left: l, right: a } = i.getBoundingClientRect();
      e.clientX - l <= t ? s = Fu(n, e, "left", t) : a - e.clientX <= t && (s = Fu(n, e, "right", t));
    }
    if (s != o.activeHandle) {
      if (!r && s !== -1) {
        const l = n.state.doc.resolve(s), a = l.node(-1), c = ae.get(a), u = l.start(-1);
        if (c.colCount(l.pos - u) + l.nodeAfter.attrs.colspan - 1 == c.width - 1) return;
      }
      Oh(n, s);
    }
  }
}
function MS(n) {
  if (!n.editable) return;
  const e = Fe.getState(n.state);
  e && e.activeHandle > -1 && !e.dragging && Oh(n, -1);
}
function TS(n, e, t, r) {
  var o;
  if (!n.editable) return !1;
  const i = (o = n.dom.ownerDocument.defaultView) !== null && o !== void 0 ? o : window, s = Fe.getState(n.state);
  if (!s || s.activeHandle == -1 || s.dragging) return !1;
  const l = n.state.doc.nodeAt(s.activeHandle), a = AS(n, s.activeHandle, l.attrs);
  n.dispatch(n.state.tr.setMeta(Fe, { setDragging: {
    startX: e.clientX,
    startWidth: a
  } }));
  function c(d) {
    i.removeEventListener("mouseup", c), i.removeEventListener("mousemove", u);
    const f = Fe.getState(n.state);
    f != null && f.dragging && (NS(n, f.activeHandle, zu(f.dragging, d, t)), n.dispatch(n.state.tr.setMeta(Fe, { setDragging: null })));
  }
  function u(d) {
    if (!d.which) return c(d);
    const f = Fe.getState(n.state);
    if (f && f.dragging) {
      const h = zu(f.dragging, d, t);
      $u(n, f.activeHandle, h, r);
    }
  }
  return $u(n, s.activeHandle, a, r), i.addEventListener("mouseup", c), i.addEventListener("mousemove", u), e.preventDefault(), !0;
}
function AS(n, e, { colspan: t, colwidth: r }) {
  const o = r && r[r.length - 1];
  if (o) return o;
  const i = n.domAtPos(e);
  let s = i.node.childNodes[i.offset].offsetWidth, l = t;
  if (r)
    for (let a = 0; a < t; a++) r[a] && (s -= r[a], l--);
  return s / l;
}
function OS(n) {
  for (; n && n.nodeName != "TD" && n.nodeName != "TH"; ) n = n.classList && n.classList.contains("ProseMirror") ? null : n.parentNode;
  return n;
}
function Fu(n, e, t, r) {
  const o = t == "right" ? -r : r, i = n.posAtCoords({
    left: e.clientX + o,
    top: e.clientY
  });
  if (!i) return -1;
  const { pos: s } = i, l = kn(n.state.doc.resolve(s));
  if (!l) return -1;
  if (t == "right") return l.pos;
  const a = ae.get(l.node(-1)), c = l.start(-1), u = a.map.indexOf(l.pos - c);
  return u % a.width == 0 ? -1 : c + a.map[u - 1];
}
function zu(n, e, t) {
  const r = e.clientX - n.startX;
  return Math.max(t, n.startWidth + r);
}
function Oh(n, e) {
  n.dispatch(n.state.tr.setMeta(Fe, { setHandle: e }));
}
function NS(n, e, t) {
  const r = n.state.doc.resolve(e), o = r.node(-1), i = ae.get(o), s = r.start(-1), l = i.colCount(r.pos - s) + r.nodeAfter.attrs.colspan - 1, a = n.state.tr;
  for (let c = 0; c < i.height; c++) {
    const u = c * i.width + l;
    if (c && i.map[u] == i.map[u - i.width]) continue;
    const d = i.map[u], f = o.nodeAt(d).attrs, h = f.colspan == 1 ? 0 : l - i.colCount(d);
    if (f.colwidth && f.colwidth[h] == t) continue;
    const p = f.colwidth ? f.colwidth.slice() : RS(f.colspan);
    p[h] = t, a.setNodeMarkup(s + d, null, {
      ...f,
      colwidth: p
    });
  }
  a.docChanged && n.dispatch(a);
}
function $u(n, e, t, r) {
  const o = n.state.doc.resolve(e), i = o.node(-1), s = o.start(-1), l = ae.get(i).colCount(o.pos - s) + o.nodeAfter.attrs.colspan - 1;
  let a = n.domAtPos(o.start(-1)).node;
  for (; a && a.nodeName != "TABLE"; ) a = a.parentNode;
  a && _l(i, a.firstChild, a, r, l, t);
}
function RS(n) {
  return Array(n).fill(0);
}
function DS(n, e) {
  const t = [], r = n.doc.resolve(e), o = r.node(-1);
  if (!o) return se.empty;
  const i = ae.get(o), s = r.start(-1), l = i.colCount(r.pos - s) + r.nodeAfter.attrs.colspan - 1;
  for (let c = 0; c < i.height; c++) {
    const u = l + c * i.width;
    if ((l == i.width - 1 || i.map[u] != i.map[u + 1]) && (c == 0 || i.map[u] != i.map[u - i.width])) {
      var a;
      const d = i.map[u], f = s + d + o.nodeAt(d).nodeSize - 1, h = document.createElement("div");
      h.className = "column-resize-handle", !((a = Fe.getState(n)) === null || a === void 0) && a.dragging && t.push(Ne.node(s + d, s + d + o.nodeAt(d).nodeSize, { class: "column-resize-dragging" })), t.push(Ne.widget(f, h));
    }
  }
  return se.create(n.doc, t);
}
function PS({ allowTableNodeSelection: n = !1 } = {}) {
  return new ue({
    key: $t,
    state: {
      init() {
        return null;
      },
      apply(e, t) {
        const r = e.getMeta($t);
        if (r != null) return r == -1 ? null : r;
        if (t == null || !e.docChanged) return t;
        const { deleted: o, pos: i } = e.mapping.mapResult(t);
        return o ? null : i;
      }
    },
    props: {
      decorations: KC,
      handleDOMEvents: { mousedown: CS },
      createSelectionBetween(e) {
        return $t.getState(e.state) != null ? e.state.selection : null;
      },
      handleTripleClick: wS,
      handleKeyDown: bS,
      handlePaste: vS
    },
    appendTransaction(e, t, r) {
      return JC(r, Eh(r, t), n);
    }
  });
}
function Bl(n, e) {
  return e ? ["width", `${Math.max(e, n)}px`] : ["min-width", `${n}px`];
}
function Hu(n, e, t, r, o, i) {
  var s;
  let l = 0, a = !0, c = e.firstChild;
  const u = n.firstChild;
  if (u !== null)
    for (let d = 0, f = 0; d < u.childCount; d += 1) {
      const { colspan: h, colwidth: p } = u.child(d).attrs;
      for (let m = 0; m < h; m += 1, f += 1) {
        const g = o === f ? i : p && p[m], y = g ? `${g}px` : "";
        if (l += g || r, g || (a = !1), c) {
          if (c.style.width !== y) {
            const [b, w] = Bl(r, g);
            c.style.setProperty(b, w);
          }
          c = c.nextSibling;
        } else {
          const b = document.createElement("col"), [w, v] = Bl(r, g);
          b.style.setProperty(w, v), e.appendChild(b);
        }
      }
    }
  for (; c; ) {
    const d = c.nextSibling;
    (s = c.parentNode) === null || s === void 0 || s.removeChild(c), c = d;
  }
  a ? (t.style.width = `${l}px`, t.style.minWidth = "") : (t.style.width = "", t.style.minWidth = `${l}px`);
}
class IS {
  constructor(e, t) {
    this.node = e, this.cellMinWidth = t, this.dom = document.createElement("div"), this.dom.className = "tableWrapper", this.table = this.dom.appendChild(document.createElement("table")), this.colgroup = this.table.appendChild(document.createElement("colgroup")), Hu(e, this.colgroup, this.table, t), this.contentDOM = this.table.appendChild(document.createElement("tbody"));
  }
  update(e) {
    return e.type !== this.node.type ? !1 : (this.node = e, Hu(e, this.colgroup, this.table, this.cellMinWidth), !0);
  }
  ignoreMutation(e) {
    return e.type === "attributes" && (e.target === this.table || this.colgroup.contains(e.target));
  }
}
function LS(n, e, t, r) {
  let o = 0, i = !0;
  const s = [], l = n.firstChild;
  if (!l)
    return {};
  for (let d = 0, f = 0; d < l.childCount; d += 1) {
    const { colspan: h, colwidth: p } = l.child(d).attrs;
    for (let m = 0; m < h; m += 1, f += 1) {
      const g = t === f ? r : p && p[m];
      o += g || e, g || (i = !1);
      const [y, b] = Bl(e, g);
      s.push([
        "col",
        { style: `${y}: ${b}` }
      ]);
    }
  }
  const a = i ? `${o}px` : "", c = i ? "" : `${o}px`;
  return { colgroup: ["colgroup", {}, ...s], tableWidth: a, tableMinWidth: c };
}
function Wu(n, e) {
  return n.createAndFill();
}
function _S(n) {
  if (n.cached.tableNodeTypes)
    return n.cached.tableNodeTypes;
  const e = {};
  return Object.keys(n.nodes).forEach((t) => {
    const r = n.nodes[t];
    r.spec.tableRole && (e[r.spec.tableRole] = r);
  }), n.cached.tableNodeTypes = e, e;
}
function BS(n, e, t, r, o) {
  const i = _S(n), s = [], l = [];
  for (let c = 0; c < t; c += 1) {
    const u = Wu(i.cell);
    if (u && l.push(u), r) {
      const d = Wu(i.header_cell);
      d && s.push(d);
    }
  }
  const a = [];
  for (let c = 0; c < e; c += 1)
    a.push(i.row.createChecked(null, r && c === 0 ? s : l));
  return i.table.createChecked(null, a);
}
function FS(n) {
  return n instanceof ee;
}
const Co = ({ editor: n }) => {
  const { selection: e } = n.state;
  if (!FS(e))
    return !1;
  let t = 0;
  const r = Zf(e.ranges[0].$from, (i) => i.type.name === "table");
  return r == null || r.node.descendants((i) => {
    if (i.type.name === "table")
      return !1;
    ["tableCell", "tableHeader"].includes(i.type.name) && (t += 1);
  }), t === e.ranges.length ? (n.commands.deleteTable(), !0) : !1;
}, zS = Te.create({
  name: "table",
  // @ts-ignore
  addOptions() {
    return {
      HTMLAttributes: {},
      resizable: !1,
      renderWrapper: !1,
      handleWidth: 5,
      cellMinWidth: 25,
      // TODO: fix
      View: IS,
      lastColumnResizable: !0,
      allowTableNodeSelection: !1
    };
  },
  content: "tableRow+",
  tableRole: "table",
  isolating: !0,
  group: "block",
  parseHTML() {
    return [{ tag: "table" }];
  },
  renderHTML({ node: n, HTMLAttributes: e }) {
    const { colgroup: t, tableWidth: r, tableMinWidth: o } = LS(n, this.options.cellMinWidth), i = [
      "table",
      ne(this.options.HTMLAttributes, e, {
        style: r ? `width: ${r}` : `min-width: ${o}`
      }),
      t,
      ["tbody", 0]
    ];
    return this.options.renderWrapper ? ["div", { class: "tableWrapper" }, i] : i;
  },
  addCommands() {
    return {
      insertTable: ({ rows: n = 3, cols: e = 3, withHeaderRow: t = !0 } = {}) => ({ tr: r, dispatch: o, editor: i }) => {
        const s = BS(i.schema, n, e, t);
        if (o) {
          const l = r.selection.from + 1;
          r.replaceSelectionWith(s).scrollIntoView().setSelection(L.near(r.doc.resolve(l)));
        }
        return !0;
      },
      addColumnBefore: () => ({ state: n, dispatch: e }) => QC(n, e),
      addColumnAfter: () => ({ state: n, dispatch: e }) => ZC(n, e),
      deleteColumn: () => ({ state: n, dispatch: e }) => tS(n, e),
      addRowBefore: () => ({ state: n, dispatch: e }) => rS(n, e),
      addRowAfter: () => ({ state: n, dispatch: e }) => oS(n, e),
      deleteRow: () => ({ state: n, dispatch: e }) => sS(n, e),
      deleteTable: () => ({ state: n, dispatch: e }) => hS(n, e),
      mergeCells: () => ({ state: n, dispatch: e }) => Nu(n, e),
      splitCell: () => ({ state: n, dispatch: e }) => Ru(n, e),
      toggleHeaderColumn: () => ({ state: n, dispatch: e }) => $r("column")(n, e),
      toggleHeaderRow: () => ({ state: n, dispatch: e }) => $r("row")(n, e),
      toggleHeaderCell: () => ({ state: n, dispatch: e }) => dS(n, e),
      mergeOrSplit: () => ({ state: n, dispatch: e }) => Nu(n, e) ? !0 : Ru(n, e),
      setCellAttribute: (n, e) => ({ state: t, dispatch: r }) => cS(n, e)(t, r),
      goToNextCell: () => ({ state: n, dispatch: e }) => Pu(1)(n, e),
      goToPreviousCell: () => ({ state: n, dispatch: e }) => Pu(-1)(n, e),
      fixTables: () => ({ state: n, dispatch: e }) => (e && Eh(n), !0),
      setCellSelection: (n) => ({ tr: e, dispatch: t }) => {
        if (t) {
          const r = ee.create(e.doc, n.anchorCell, n.headCell);
          e.setSelection(r);
        }
        return !0;
      }
    };
  },
  addKeyboardShortcuts() {
    return {
      Tab: () => this.editor.commands.goToNextCell() ? !0 : this.editor.can().addRowAfter() ? this.editor.chain().addRowAfter().goToNextCell().run() : !1,
      "Shift-Tab": () => this.editor.commands.goToPreviousCell(),
      Backspace: Co,
      "Mod-Backspace": Co,
      Delete: Co,
      "Mod-Delete": Co
    };
  },
  addProseMirrorPlugins() {
    return [
      ...this.options.resizable && this.editor.isEditable ? [
        xS({
          handleWidth: this.options.handleWidth,
          cellMinWidth: this.options.cellMinWidth,
          defaultCellMinWidth: this.options.cellMinWidth,
          View: this.options.View,
          lastColumnResizable: this.options.lastColumnResizable
        })
      ] : [],
      PS({
        allowTableNodeSelection: this.options.allowTableNodeSelection
      })
    ];
  },
  extendNodeSchema(n) {
    const e = {
      name: n.name,
      options: n.options,
      storage: n.storage
    };
    return {
      tableRole: W(R(n, "tableRole", e))
    };
  }
}), $S = Te.create({
  name: "tableRow",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  content: "(tableCell | tableHeader)*",
  tableRole: "row",
  parseHTML() {
    return [
      { tag: "tr" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["tr", ne(this.options.HTMLAttributes, n), 0];
  }
}), HS = Te.create({
  name: "tableCell",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  content: "block+",
  addAttributes() {
    return {
      colspan: {
        default: 1
      },
      rowspan: {
        default: 1
      },
      colwidth: {
        default: null,
        parseHTML: (n) => {
          const e = n.getAttribute("colwidth");
          return e ? e.split(",").map((r) => parseInt(r, 10)) : null;
        }
      }
    };
  },
  tableRole: "cell",
  isolating: !0,
  parseHTML() {
    return [
      { tag: "td" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["td", ne(this.options.HTMLAttributes, n), 0];
  }
}), WS = Te.create({
  name: "tableHeader",
  addOptions() {
    return {
      HTMLAttributes: {}
    };
  },
  content: "block+",
  addAttributes() {
    return {
      colspan: {
        default: 1
      },
      rowspan: {
        default: 1
      },
      colwidth: {
        default: null,
        parseHTML: (n) => {
          const e = n.getAttribute("colwidth");
          return e ? e.split(",").map((r) => parseInt(r, 10)) : null;
        }
      }
    };
  },
  tableRole: "header_cell",
  isolating: !0,
  parseHTML() {
    return [
      { tag: "th" }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["th", ne(this.options.HTMLAttributes, n), 0];
  }
}), VS = (n) => {
  if (!n.children.length)
    return;
  const e = n.querySelectorAll("span");
  e && e.forEach((t) => {
    var r, o;
    const i = t.getAttribute("style"), s = (o = (r = t.parentElement) === null || r === void 0 ? void 0 : r.closest("span")) === null || o === void 0 ? void 0 : o.getAttribute("style");
    t.setAttribute("style", `${s};${i}`);
  });
}, jS = Qe.create({
  name: "textStyle",
  priority: 101,
  addOptions() {
    return {
      HTMLAttributes: {},
      mergeNestedSpanStyles: !1
    };
  },
  parseHTML() {
    return [
      {
        tag: "span",
        getAttrs: (n) => n.hasAttribute("style") ? (this.options.mergeNestedSpanStyles && VS(n), {}) : !1
      }
    ];
  },
  renderHTML({ HTMLAttributes: n }) {
    return ["span", ne(this.options.HTMLAttributes, n), 0];
  },
  addCommands() {
    return {
      removeEmptyTextStyle: () => ({ tr: n }) => {
        const { selection: e } = n;
        return n.doc.nodesBetween(e.from, e.to, (t, r) => {
          if (t.isTextblock)
            return !0;
          t.marks.filter((o) => o.type === this.type).some((o) => Object.values(o.attrs).some((i) => !!i)) || n.removeMark(r, r + t.nodeSize, this.type);
        }), !0;
      }
    };
  }
}), US = xe.create({
  name: "color",
  addOptions() {
    return {
      types: ["textStyle"]
    };
  },
  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          color: {
            default: null,
            parseHTML: (n) => {
              var e;
              return (e = n.style.color) === null || e === void 0 ? void 0 : e.replace(/['"]+/g, "");
            },
            renderHTML: (n) => n.color ? {
              style: `color: ${n.color}`
            } : {}
          }
        }
      }
    ];
  },
  addCommands() {
    return {
      setColor: (n) => ({ chain: e }) => e().setMark("textStyle", { color: n }).run(),
      unsetColor: () => ({ chain: n }) => n().setMark("textStyle", { color: null }).removeEmptyTextStyle().run()
    };
  }
});
function F(n, e, { checkForDefaultPrevented: t = !0 } = {}) {
  return function(o) {
    if (n == null || n(o), t === !1 || !o.defaultPrevented)
      return e == null ? void 0 : e(o);
  };
}
function Vu(n, e) {
  if (typeof n == "function")
    return n(e);
  n != null && (n.current = e);
}
function Xr(...n) {
  return (e) => {
    let t = !1;
    const r = n.map((o) => {
      const i = Vu(o, e);
      return !t && typeof i == "function" && (t = !0), i;
    });
    if (t)
      return () => {
        for (let o = 0; o < r.length; o++) {
          const i = r[o];
          typeof i == "function" ? i() : Vu(n[o], null);
        }
      };
  };
}
function ge(...n) {
  return V(Xr(...n), n);
}
const S = x.createElement, KS = x.createElement, qS = x.Fragment;
function On(n, e = []) {
  let t = [];
  function r(i, s) {
    const l = xn(s), a = t.length;
    t = [...t, s];
    const c = (d) => {
      var y;
      const { scope: f, children: h, ...p } = d, m = ((y = f == null ? void 0 : f[n]) == null ? void 0 : y[a]) || l, g = Xe(() => p, Object.values(p));
      return /* @__PURE__ */ S(m.Provider, { value: g, children: h });
    };
    c.displayName = i + "Provider";
    function u(d, f) {
      var m;
      const h = ((m = f == null ? void 0 : f[n]) == null ? void 0 : m[a]) || l, p = tr(h);
      if (p) return p;
      if (s !== void 0) return s;
      throw new Error(`\`${d}\` must be used within \`${i}\``);
    }
    return [c, u];
  }
  const o = () => {
    const i = t.map((s) => xn(s));
    return function(l) {
      const a = (l == null ? void 0 : l[n]) || i;
      return Xe(
        () => ({ [`__scope${n}`]: { ...l, [n]: a } }),
        [l, a]
      );
    };
  };
  return o.scopeName = n, [r, GS(o, ...e)];
}
function GS(...n) {
  const e = n[0];
  if (n.length === 1) return e;
  const t = () => {
    const r = n.map((o) => ({
      useScope: o(),
      scopeName: o.scopeName
    }));
    return function(i) {
      const s = r.reduce((l, { useScope: a, scopeName: c }) => {
        const d = a(i)[`__scope${c}`];
        return { ...l, ...d };
      }, {});
      return Xe(() => ({ [`__scope${e.scopeName}`]: s }), [s]);
    };
  };
  return t.scopeName = e.scopeName, t;
}
var Yt = globalThis != null && globalThis.document ? Yr : () => {
}, JS = Ca[" useInsertionEffect ".trim().toString()] || Yt;
function Ji({
  prop: n,
  defaultProp: e,
  onChange: t = () => {
  },
  caller: r
}) {
  const [o, i, s] = YS({
    defaultProp: e,
    onChange: t
  }), l = n !== void 0, a = l ? n : o;
  {
    const u = D(n !== void 0);
    z(() => {
      const d = u.current;
      d !== l && console.warn(
        `${r} is changing from ${d ? "controlled" : "uncontrolled"} to ${l ? "controlled" : "uncontrolled"}. Components should not switch from controlled to uncontrolled (or vice versa). Decide between using a controlled or uncontrolled value for the lifetime of the component.`
      ), u.current = l;
    }, [l, r]);
  }
  const c = V(
    (u) => {
      var d;
      if (l) {
        const f = XS(u) ? u(n) : u;
        f !== n && ((d = s.current) == null || d.call(s, f));
      } else
        i(u);
    },
    [l, n, i, s]
  );
  return [a, c];
}
function YS({
  defaultProp: n,
  onChange: e
}) {
  const [t, r] = K(n), o = D(t), i = D(e);
  return JS(() => {
    i.current = e;
  }, [e]), z(() => {
    var s;
    o.current !== t && ((s = i.current) == null || s.call(i, t), o.current = t);
  }, [t, o]), [t, r, i];
}
function XS(n) {
  return typeof n == "function";
}
// @__NO_SIDE_EFFECTS__
function Hr(n) {
  const e = /* @__PURE__ */ QS(n), t = N((r, o) => {
    const { children: i, ...s } = r, l = $e.toArray(i), a = l.find(e1);
    if (a) {
      const c = a.props.children, u = l.map((d) => d === a ? $e.count(c) > 1 ? $e.only(null) : Et(c) ? c.props.children : null : d);
      return /* @__PURE__ */ S(e, { ...s, ref: o, children: Et(c) ? An(c, void 0, u) : null });
    }
    return /* @__PURE__ */ S(e, { ...s, ref: o, children: i });
  });
  return t.displayName = `${n}.Slot`, t;
}
// @__NO_SIDE_EFFECTS__
function QS(n) {
  const e = N((t, r) => {
    const { children: o, ...i } = t;
    if (Et(o)) {
      const s = n1(o), l = t1(i, o.props);
      return o.type !== nr && (l.ref = r ? Xr(r, s) : s), An(o, l);
    }
    return $e.count(o) > 1 ? $e.only(null) : null;
  });
  return e.displayName = `${n}.SlotClone`, e;
}
var Nh = Symbol("radix.slottable");
// @__NO_SIDE_EFFECTS__
function ZS(n) {
  const e = ({ children: t }) => /* @__PURE__ */ S(qS, { children: t });
  return e.displayName = `${n}.Slottable`, e.__radixId = Nh, e;
}
function e1(n) {
  return Et(n) && typeof n.type == "function" && "__radixId" in n.type && n.type.__radixId === Nh;
}
function t1(n, e) {
  const t = { ...e };
  for (const r in e) {
    const o = n[r], i = e[r];
    /^on[A-Z]/.test(r) ? o && i ? t[r] = (...l) => {
      const a = i(...l);
      return o(...l), a;
    } : o && (t[r] = o) : r === "style" ? t[r] = { ...o, ...i } : r === "className" && (t[r] = [o, i].filter(Boolean).join(" "));
  }
  return { ...n, ...t };
}
function n1(n) {
  var r, o;
  let e = (r = Object.getOwnPropertyDescriptor(n.props, "ref")) == null ? void 0 : r.get, t = e && "isReactWarning" in e && e.isReactWarning;
  return t ? n.ref : (e = (o = Object.getOwnPropertyDescriptor(n, "ref")) == null ? void 0 : o.get, t = e && "isReactWarning" in e && e.isReactWarning, t ? n.props.ref : n.props.ref || n.ref);
}
var r1 = [
  "a",
  "button",
  "div",
  "form",
  "h2",
  "h3",
  "img",
  "input",
  "label",
  "li",
  "nav",
  "ol",
  "p",
  "select",
  "span",
  "svg",
  "ul"
], de = r1.reduce((n, e) => {
  const t = /* @__PURE__ */ Hr(`Primitive.${e}`), r = N((o, i) => {
    const { asChild: s, ...l } = o, a = s ? t : e;
    return typeof window < "u" && (window[Symbol.for("radix-ui")] = !0), /* @__PURE__ */ S(a, { ...l, ref: i });
  });
  return r.displayName = `Primitive.${e}`, { ...n, [e]: r };
}, {});
function Rh(n, e) {
  n && rh(() => n.dispatchEvent(e));
}
function Dh(n) {
  const e = n + "CollectionProvider", [t, r] = On(e), [o, i] = t(
    e,
    { collectionRef: { current: null }, itemMap: /* @__PURE__ */ new Map() }
  ), s = (m) => {
    const { scope: g, children: y } = m, b = x.useRef(null), w = x.useRef(/* @__PURE__ */ new Map()).current;
    return /* @__PURE__ */ S(o, { scope: g, itemMap: w, collectionRef: b, children: y });
  };
  s.displayName = e;
  const l = n + "CollectionSlot", a = /* @__PURE__ */ Hr(l), c = x.forwardRef(
    (m, g) => {
      const { scope: y, children: b } = m, w = i(l, y), v = ge(g, w.collectionRef);
      return /* @__PURE__ */ S(a, { ref: v, children: b });
    }
  );
  c.displayName = l;
  const u = n + "CollectionItemSlot", d = "data-radix-collection-item", f = /* @__PURE__ */ Hr(u), h = x.forwardRef(
    (m, g) => {
      const { scope: y, children: b, ...w } = m, v = x.useRef(null), k = ge(g, v), E = i(u, y);
      return x.useEffect(() => (E.itemMap.set(v, { ref: v, ...w }), () => void E.itemMap.delete(v))), /* @__PURE__ */ S(f, { [d]: "", ref: k, children: b });
    }
  );
  h.displayName = u;
  function p(m) {
    const g = i(n + "CollectionConsumer", m);
    return x.useCallback(() => {
      const b = g.collectionRef.current;
      if (!b) return [];
      const w = Array.from(b.querySelectorAll(`[${d}]`));
      return Array.from(g.itemMap.values()).sort(
        (E, C) => w.indexOf(E.ref.current) - w.indexOf(C.ref.current)
      );
    }, [g.collectionRef, g.itemMap]);
  }
  return [
    { Provider: s, Slot: c, ItemSlot: h },
    p,
    r
  ];
}
var o1 = xn(void 0);
function Ph(n) {
  const e = tr(o1);
  return n || e || "ltr";
}
function Mt(n) {
  const e = D(n);
  return z(() => {
    e.current = n;
  }), Xe(() => (...t) => {
    var r;
    return (r = e.current) == null ? void 0 : r.call(e, ...t);
  }, []);
}
function i1(n, e = globalThis == null ? void 0 : globalThis.document) {
  const t = Mt(n);
  z(() => {
    const r = (o) => {
      o.key === "Escape" && t(o);
    };
    return e.addEventListener("keydown", r, { capture: !0 }), () => e.removeEventListener("keydown", r, { capture: !0 });
  }, [t, e]);
}
var s1 = "DismissableLayer", Fl = "dismissableLayer.update", l1 = "dismissableLayer.pointerDownOutside", a1 = "dismissableLayer.focusOutside", ju, Ih = xn({
  layers: /* @__PURE__ */ new Set(),
  layersWithOutsidePointerEventsDisabled: /* @__PURE__ */ new Set(),
  branches: /* @__PURE__ */ new Set()
}), Yi = N(
  (n, e) => {
    const {
      disableOutsidePointerEvents: t = !1,
      onEscapeKeyDown: r,
      onPointerDownOutside: o,
      onFocusOutside: i,
      onInteractOutside: s,
      onDismiss: l,
      ...a
    } = n, c = tr(Ih), [u, d] = K(null), f = (u == null ? void 0 : u.ownerDocument) ?? (globalThis == null ? void 0 : globalThis.document), [, h] = K({}), p = ge(e, (C) => d(C)), m = Array.from(c.layers), [g] = [...c.layersWithOutsidePointerEventsDisabled].slice(-1), y = m.indexOf(g), b = u ? m.indexOf(u) : -1, w = c.layersWithOutsidePointerEventsDisabled.size > 0, v = b >= y, k = d1((C) => {
      const A = C.target, B = [...c.branches].some((j) => j.contains(A));
      !v || B || (o == null || o(C), s == null || s(C), C.defaultPrevented || l == null || l());
    }, f), E = f1((C) => {
      const A = C.target;
      [...c.branches].some((j) => j.contains(A)) || (i == null || i(C), s == null || s(C), C.defaultPrevented || l == null || l());
    }, f);
    return i1((C) => {
      b === c.layers.size - 1 && (r == null || r(C), !C.defaultPrevented && l && (C.preventDefault(), l()));
    }, f), z(() => {
      if (u)
        return t && (c.layersWithOutsidePointerEventsDisabled.size === 0 && (ju = f.body.style.pointerEvents, f.body.style.pointerEvents = "none"), c.layersWithOutsidePointerEventsDisabled.add(u)), c.layers.add(u), Uu(), () => {
          t && c.layersWithOutsidePointerEventsDisabled.size === 1 && (f.body.style.pointerEvents = ju);
        };
    }, [u, f, t, c]), z(() => () => {
      u && (c.layers.delete(u), c.layersWithOutsidePointerEventsDisabled.delete(u), Uu());
    }, [u, c]), z(() => {
      const C = () => h({});
      return document.addEventListener(Fl, C), () => document.removeEventListener(Fl, C);
    }, []), /* @__PURE__ */ S(
      de.div,
      {
        ...a,
        ref: p,
        style: {
          pointerEvents: w ? v ? "auto" : "none" : void 0,
          ...n.style
        },
        onFocusCapture: F(n.onFocusCapture, E.onFocusCapture),
        onBlurCapture: F(n.onBlurCapture, E.onBlurCapture),
        onPointerDownCapture: F(
          n.onPointerDownCapture,
          k.onPointerDownCapture
        )
      }
    );
  }
);
Yi.displayName = s1;
var c1 = "DismissableLayerBranch", u1 = N((n, e) => {
  const t = tr(Ih), r = D(null), o = ge(e, r);
  return z(() => {
    const i = r.current;
    if (i)
      return t.branches.add(i), () => {
        t.branches.delete(i);
      };
  }, [t.branches]), /* @__PURE__ */ S(de.div, { ...n, ref: o });
});
u1.displayName = c1;
function d1(n, e = globalThis == null ? void 0 : globalThis.document) {
  const t = Mt(n), r = D(!1), o = D(() => {
  });
  return z(() => {
    const i = (l) => {
      if (l.target && !r.current) {
        let a = function() {
          Lh(
            l1,
            t,
            c,
            { discrete: !0 }
          );
        };
        const c = { originalEvent: l };
        l.pointerType === "touch" ? (e.removeEventListener("click", o.current), o.current = a, e.addEventListener("click", o.current, { once: !0 })) : a();
      } else
        e.removeEventListener("click", o.current);
      r.current = !1;
    }, s = window.setTimeout(() => {
      e.addEventListener("pointerdown", i);
    }, 0);
    return () => {
      window.clearTimeout(s), e.removeEventListener("pointerdown", i), e.removeEventListener("click", o.current);
    };
  }, [e, t]), {
    // ensures we check React component tree (not just DOM tree)
    onPointerDownCapture: () => r.current = !0
  };
}
function f1(n, e = globalThis == null ? void 0 : globalThis.document) {
  const t = Mt(n), r = D(!1);
  return z(() => {
    const o = (i) => {
      i.target && !r.current && Lh(a1, t, { originalEvent: i }, {
        discrete: !1
      });
    };
    return e.addEventListener("focusin", o), () => e.removeEventListener("focusin", o);
  }, [e, t]), {
    onFocusCapture: () => r.current = !0,
    onBlurCapture: () => r.current = !1
  };
}
function Uu() {
  const n = new CustomEvent(Fl);
  document.dispatchEvent(n);
}
function Lh(n, e, t, { discrete: r }) {
  const o = t.originalEvent.target, i = new CustomEvent(n, { bubbles: !1, cancelable: !0, detail: t });
  e && o.addEventListener(n, e, { once: !0 }), r ? Rh(o, i) : o.dispatchEvent(i);
}
var Us = 0;
function _h() {
  z(() => {
    const n = document.querySelectorAll("[data-radix-focus-guard]");
    return document.body.insertAdjacentElement("afterbegin", n[0] ?? Ku()), document.body.insertAdjacentElement("beforeend", n[1] ?? Ku()), Us++, () => {
      Us === 1 && document.querySelectorAll("[data-radix-focus-guard]").forEach((e) => e.remove()), Us--;
    };
  }, []);
}
function Ku() {
  const n = document.createElement("span");
  return n.setAttribute("data-radix-focus-guard", ""), n.tabIndex = 0, n.style.outline = "none", n.style.opacity = "0", n.style.position = "fixed", n.style.pointerEvents = "none", n;
}
var Ks = "focusScope.autoFocusOnMount", qs = "focusScope.autoFocusOnUnmount", qu = { bubbles: !1, cancelable: !0 }, h1 = "FocusScope", La = N((n, e) => {
  const {
    loop: t = !1,
    trapped: r = !1,
    onMountAutoFocus: o,
    onUnmountAutoFocus: i,
    ...s
  } = n, [l, a] = K(null), c = Mt(o), u = Mt(i), d = D(null), f = ge(e, (m) => a(m)), h = D({
    paused: !1,
    pause() {
      this.paused = !0;
    },
    resume() {
      this.paused = !1;
    }
  }).current;
  z(() => {
    if (r) {
      let m = function(w) {
        if (h.paused || !l) return;
        const v = w.target;
        l.contains(v) ? d.current = v : It(d.current, { select: !0 });
      }, g = function(w) {
        if (h.paused || !l) return;
        const v = w.relatedTarget;
        v !== null && (l.contains(v) || It(d.current, { select: !0 }));
      }, y = function(w) {
        if (document.activeElement === document.body)
          for (const k of w)
            k.removedNodes.length > 0 && It(l);
      };
      document.addEventListener("focusin", m), document.addEventListener("focusout", g);
      const b = new MutationObserver(y);
      return l && b.observe(l, { childList: !0, subtree: !0 }), () => {
        document.removeEventListener("focusin", m), document.removeEventListener("focusout", g), b.disconnect();
      };
    }
  }, [r, l, h.paused]), z(() => {
    if (l) {
      Ju.add(h);
      const m = document.activeElement;
      if (!l.contains(m)) {
        const y = new CustomEvent(Ks, qu);
        l.addEventListener(Ks, c), l.dispatchEvent(y), y.defaultPrevented || (p1(w1(Bh(l)), { select: !0 }), document.activeElement === m && It(l));
      }
      return () => {
        l.removeEventListener(Ks, c), setTimeout(() => {
          const y = new CustomEvent(qs, qu);
          l.addEventListener(qs, u), l.dispatchEvent(y), y.defaultPrevented || It(m ?? document.body, { select: !0 }), l.removeEventListener(qs, u), Ju.remove(h);
        }, 0);
      };
    }
  }, [l, c, u, h]);
  const p = V(
    (m) => {
      if (!t && !r || h.paused) return;
      const g = m.key === "Tab" && !m.altKey && !m.ctrlKey && !m.metaKey, y = document.activeElement;
      if (g && y) {
        const b = m.currentTarget, [w, v] = m1(b);
        w && v ? !m.shiftKey && y === v ? (m.preventDefault(), t && It(w, { select: !0 })) : m.shiftKey && y === w && (m.preventDefault(), t && It(v, { select: !0 })) : y === b && m.preventDefault();
      }
    },
    [t, r, h.paused]
  );
  return /* @__PURE__ */ S(de.div, { tabIndex: -1, ...s, ref: f, onKeyDown: p });
});
La.displayName = h1;
function p1(n, { select: e = !1 } = {}) {
  const t = document.activeElement;
  for (const r of n)
    if (It(r, { select: e }), document.activeElement !== t) return;
}
function m1(n) {
  const e = Bh(n), t = Gu(e, n), r = Gu(e.reverse(), n);
  return [t, r];
}
function Bh(n) {
  const e = [], t = document.createTreeWalker(n, NodeFilter.SHOW_ELEMENT, {
    acceptNode: (r) => {
      const o = r.tagName === "INPUT" && r.type === "hidden";
      return r.disabled || r.hidden || o ? NodeFilter.FILTER_SKIP : r.tabIndex >= 0 ? NodeFilter.FILTER_ACCEPT : NodeFilter.FILTER_SKIP;
    }
  });
  for (; t.nextNode(); ) e.push(t.currentNode);
  return e;
}
function Gu(n, e) {
  for (const t of n)
    if (!g1(t, { upTo: e })) return t;
}
function g1(n, { upTo: e }) {
  if (getComputedStyle(n).visibility === "hidden") return !0;
  for (; n; ) {
    if (e !== void 0 && n === e) return !1;
    if (getComputedStyle(n).display === "none") return !0;
    n = n.parentElement;
  }
  return !1;
}
function y1(n) {
  return n instanceof HTMLInputElement && "select" in n;
}
function It(n, { select: e = !1 } = {}) {
  if (n && n.focus) {
    const t = document.activeElement;
    n.focus({ preventScroll: !0 }), n !== t && y1(n) && e && n.select();
  }
}
var Ju = b1();
function b1() {
  let n = [];
  return {
    add(e) {
      const t = n[0];
      e !== t && (t == null || t.pause()), n = Yu(n, e), n.unshift(e);
    },
    remove(e) {
      var t;
      n = Yu(n, e), (t = n[0]) == null || t.resume();
    }
  };
}
function Yu(n, e) {
  const t = [...n], r = t.indexOf(e);
  return r !== -1 && t.splice(r, 1), t;
}
function w1(n) {
  return n.filter((e) => e.tagName !== "A");
}
var v1 = Ca[" useId ".trim().toString()] || (() => {
}), C1 = 0;
function Wr(n) {
  const [e, t] = K(v1());
  return Yt(() => {
    t((r) => r ?? String(C1++));
  }, [n]), e ? `radix-${e}` : "";
}
const S1 = ["top", "right", "bottom", "left"], Xt = Math.min, _e = Math.max, Mi = Math.round, So = Math.floor, ut = (n) => ({
  x: n,
  y: n
}), x1 = {
  left: "right",
  right: "left",
  bottom: "top",
  top: "bottom"
};
function zl(n, e, t) {
  return _e(n, Xt(e, t));
}
function Tt(n, e) {
  return typeof n == "function" ? n(e) : n;
}
function At(n) {
  return n.split("-")[0];
}
function rr(n) {
  return n.split("-")[1];
}
function _a(n) {
  return n === "x" ? "y" : "x";
}
function Ba(n) {
  return n === "y" ? "height" : "width";
}
function at(n) {
  const e = n[0];
  return e === "t" || e === "b" ? "y" : "x";
}
function Fa(n) {
  return _a(at(n));
}
function k1(n, e, t) {
  t === void 0 && (t = !1);
  const r = rr(n), o = Fa(n), i = Ba(o);
  let s = o === "x" ? r === (t ? "end" : "start") ? "right" : "left" : r === "start" ? "bottom" : "top";
  return e.reference[i] > e.floating[i] && (s = Ti(s)), [s, Ti(s)];
}
function E1(n) {
  const e = Ti(n);
  return [$l(n), e, $l(e)];
}
function $l(n) {
  return n.includes("start") ? n.replace("start", "end") : n.replace("end", "start");
}
const Xu = ["left", "right"], Qu = ["right", "left"], M1 = ["top", "bottom"], T1 = ["bottom", "top"];
function A1(n, e, t) {
  switch (n) {
    case "top":
    case "bottom":
      return t ? e ? Qu : Xu : e ? Xu : Qu;
    case "left":
    case "right":
      return e ? M1 : T1;
    default:
      return [];
  }
}
function O1(n, e, t, r) {
  const o = rr(n);
  let i = A1(At(n), t === "start", r);
  return o && (i = i.map((s) => s + "-" + o), e && (i = i.concat(i.map($l)))), i;
}
function Ti(n) {
  const e = At(n);
  return x1[e] + n.slice(e.length);
}
function N1(n) {
  return {
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    ...n
  };
}
function Fh(n) {
  return typeof n != "number" ? N1(n) : {
    top: n,
    right: n,
    bottom: n,
    left: n
  };
}
function Ai(n) {
  const {
    x: e,
    y: t,
    width: r,
    height: o
  } = n;
  return {
    width: r,
    height: o,
    top: t,
    left: e,
    right: e + r,
    bottom: t + o,
    x: e,
    y: t
  };
}
function Zu(n, e, t) {
  let {
    reference: r,
    floating: o
  } = n;
  const i = at(e), s = Fa(e), l = Ba(s), a = At(e), c = i === "y", u = r.x + r.width / 2 - o.width / 2, d = r.y + r.height / 2 - o.height / 2, f = r[l] / 2 - o[l] / 2;
  let h;
  switch (a) {
    case "top":
      h = {
        x: u,
        y: r.y - o.height
      };
      break;
    case "bottom":
      h = {
        x: u,
        y: r.y + r.height
      };
      break;
    case "right":
      h = {
        x: r.x + r.width,
        y: d
      };
      break;
    case "left":
      h = {
        x: r.x - o.width,
        y: d
      };
      break;
    default:
      h = {
        x: r.x,
        y: r.y
      };
  }
  switch (rr(e)) {
    case "start":
      h[s] -= f * (t && c ? -1 : 1);
      break;
    case "end":
      h[s] += f * (t && c ? -1 : 1);
      break;
  }
  return h;
}
async function R1(n, e) {
  var t;
  e === void 0 && (e = {});
  const {
    x: r,
    y: o,
    platform: i,
    rects: s,
    elements: l,
    strategy: a
  } = n, {
    boundary: c = "clippingAncestors",
    rootBoundary: u = "viewport",
    elementContext: d = "floating",
    altBoundary: f = !1,
    padding: h = 0
  } = Tt(e, n), p = Fh(h), g = l[f ? d === "floating" ? "reference" : "floating" : d], y = Ai(await i.getClippingRect({
    element: (t = await (i.isElement == null ? void 0 : i.isElement(g))) == null || t ? g : g.contextElement || await (i.getDocumentElement == null ? void 0 : i.getDocumentElement(l.floating)),
    boundary: c,
    rootBoundary: u,
    strategy: a
  })), b = d === "floating" ? {
    x: r,
    y: o,
    width: s.floating.width,
    height: s.floating.height
  } : s.reference, w = await (i.getOffsetParent == null ? void 0 : i.getOffsetParent(l.floating)), v = await (i.isElement == null ? void 0 : i.isElement(w)) ? await (i.getScale == null ? void 0 : i.getScale(w)) || {
    x: 1,
    y: 1
  } : {
    x: 1,
    y: 1
  }, k = Ai(i.convertOffsetParentRelativeRectToViewportRelativeRect ? await i.convertOffsetParentRelativeRectToViewportRelativeRect({
    elements: l,
    rect: b,
    offsetParent: w,
    strategy: a
  }) : b);
  return {
    top: (y.top - k.top + p.top) / v.y,
    bottom: (k.bottom - y.bottom + p.bottom) / v.y,
    left: (y.left - k.left + p.left) / v.x,
    right: (k.right - y.right + p.right) / v.x
  };
}
const D1 = 50, P1 = async (n, e, t) => {
  const {
    placement: r = "bottom",
    strategy: o = "absolute",
    middleware: i = [],
    platform: s
  } = t, l = s.detectOverflow ? s : {
    ...s,
    detectOverflow: R1
  }, a = await (s.isRTL == null ? void 0 : s.isRTL(e));
  let c = await s.getElementRects({
    reference: n,
    floating: e,
    strategy: o
  }), {
    x: u,
    y: d
  } = Zu(c, r, a), f = r, h = 0;
  const p = {};
  for (let m = 0; m < i.length; m++) {
    const g = i[m];
    if (!g)
      continue;
    const {
      name: y,
      fn: b
    } = g, {
      x: w,
      y: v,
      data: k,
      reset: E
    } = await b({
      x: u,
      y: d,
      initialPlacement: r,
      placement: f,
      strategy: o,
      middlewareData: p,
      rects: c,
      platform: l,
      elements: {
        reference: n,
        floating: e
      }
    });
    u = w ?? u, d = v ?? d, p[y] = {
      ...p[y],
      ...k
    }, E && h < D1 && (h++, typeof E == "object" && (E.placement && (f = E.placement), E.rects && (c = E.rects === !0 ? await s.getElementRects({
      reference: n,
      floating: e,
      strategy: o
    }) : E.rects), {
      x: u,
      y: d
    } = Zu(c, f, a)), m = -1);
  }
  return {
    x: u,
    y: d,
    placement: f,
    strategy: o,
    middlewareData: p
  };
}, I1 = (n) => ({
  name: "arrow",
  options: n,
  async fn(e) {
    const {
      x: t,
      y: r,
      placement: o,
      rects: i,
      platform: s,
      elements: l,
      middlewareData: a
    } = e, {
      element: c,
      padding: u = 0
    } = Tt(n, e) || {};
    if (c == null)
      return {};
    const d = Fh(u), f = {
      x: t,
      y: r
    }, h = Fa(o), p = Ba(h), m = await s.getDimensions(c), g = h === "y", y = g ? "top" : "left", b = g ? "bottom" : "right", w = g ? "clientHeight" : "clientWidth", v = i.reference[p] + i.reference[h] - f[h] - i.floating[p], k = f[h] - i.reference[h], E = await (s.getOffsetParent == null ? void 0 : s.getOffsetParent(c));
    let C = E ? E[w] : 0;
    (!C || !await (s.isElement == null ? void 0 : s.isElement(E))) && (C = l.floating[w] || i.floating[p]);
    const A = v / 2 - k / 2, B = C / 2 - m[p] / 2 - 1, j = Xt(d[y], B), G = Xt(d[b], B), Y = j, X = C - m[p] - G, q = C / 2 - m[p] / 2 + A, oe = zl(Y, q, X), U = !a.arrow && rr(o) != null && q !== oe && i.reference[p] / 2 - (q < Y ? j : G) - m[p] / 2 < 0, Q = U ? q < Y ? q - Y : q - X : 0;
    return {
      [h]: f[h] + Q,
      data: {
        [h]: oe,
        centerOffset: q - oe - Q,
        ...U && {
          alignmentOffset: Q
        }
      },
      reset: U
    };
  }
}), L1 = function(n) {
  return n === void 0 && (n = {}), {
    name: "flip",
    options: n,
    async fn(e) {
      var t, r;
      const {
        placement: o,
        middlewareData: i,
        rects: s,
        initialPlacement: l,
        platform: a,
        elements: c
      } = e, {
        mainAxis: u = !0,
        crossAxis: d = !0,
        fallbackPlacements: f,
        fallbackStrategy: h = "bestFit",
        fallbackAxisSideDirection: p = "none",
        flipAlignment: m = !0,
        ...g
      } = Tt(n, e);
      if ((t = i.arrow) != null && t.alignmentOffset)
        return {};
      const y = At(o), b = at(l), w = At(l) === l, v = await (a.isRTL == null ? void 0 : a.isRTL(c.floating)), k = f || (w || !m ? [Ti(l)] : E1(l)), E = p !== "none";
      !f && E && k.push(...O1(l, m, p, v));
      const C = [l, ...k], A = await a.detectOverflow(e, g), B = [];
      let j = ((r = i.flip) == null ? void 0 : r.overflows) || [];
      if (u && B.push(A[y]), d) {
        const q = k1(o, s, v);
        B.push(A[q[0]], A[q[1]]);
      }
      if (j = [...j, {
        placement: o,
        overflows: B
      }], !B.every((q) => q <= 0)) {
        var G, Y;
        const q = (((G = i.flip) == null ? void 0 : G.index) || 0) + 1, oe = C[q];
        if (oe && (!(d === "alignment" ? b !== at(oe) : !1) || // We leave the current main axis only if every placement on that axis
        // overflows the main axis.
        j.every(($) => at($.placement) === b ? $.overflows[0] > 0 : !0)))
          return {
            data: {
              index: q,
              overflows: j
            },
            reset: {
              placement: oe
            }
          };
        let U = (Y = j.filter((Q) => Q.overflows[0] <= 0).sort((Q, $) => Q.overflows[1] - $.overflows[1])[0]) == null ? void 0 : Y.placement;
        if (!U)
          switch (h) {
            case "bestFit": {
              var X;
              const Q = (X = j.filter(($) => {
                if (E) {
                  const P = at($.placement);
                  return P === b || // Create a bias to the `y` side axis due to horizontal
                  // reading directions favoring greater width.
                  P === "y";
                }
                return !0;
              }).map(($) => [$.placement, $.overflows.filter((P) => P > 0).reduce((P, re) => P + re, 0)]).sort(($, P) => $[1] - P[1])[0]) == null ? void 0 : X[0];
              Q && (U = Q);
              break;
            }
            case "initialPlacement":
              U = l;
              break;
          }
        if (o !== U)
          return {
            reset: {
              placement: U
            }
          };
      }
      return {};
    }
  };
};
function ed(n, e) {
  return {
    top: n.top - e.height,
    right: n.right - e.width,
    bottom: n.bottom - e.height,
    left: n.left - e.width
  };
}
function td(n) {
  return S1.some((e) => n[e] >= 0);
}
const _1 = function(n) {
  return n === void 0 && (n = {}), {
    name: "hide",
    options: n,
    async fn(e) {
      const {
        rects: t,
        platform: r
      } = e, {
        strategy: o = "referenceHidden",
        ...i
      } = Tt(n, e);
      switch (o) {
        case "referenceHidden": {
          const s = await r.detectOverflow(e, {
            ...i,
            elementContext: "reference"
          }), l = ed(s, t.reference);
          return {
            data: {
              referenceHiddenOffsets: l,
              referenceHidden: td(l)
            }
          };
        }
        case "escaped": {
          const s = await r.detectOverflow(e, {
            ...i,
            altBoundary: !0
          }), l = ed(s, t.floating);
          return {
            data: {
              escapedOffsets: l,
              escaped: td(l)
            }
          };
        }
        default:
          return {};
      }
    }
  };
}, zh = /* @__PURE__ */ new Set(["left", "top"]);
async function B1(n, e) {
  const {
    placement: t,
    platform: r,
    elements: o
  } = n, i = await (r.isRTL == null ? void 0 : r.isRTL(o.floating)), s = At(t), l = rr(t), a = at(t) === "y", c = zh.has(s) ? -1 : 1, u = i && a ? -1 : 1, d = Tt(e, n);
  let {
    mainAxis: f,
    crossAxis: h,
    alignmentAxis: p
  } = typeof d == "number" ? {
    mainAxis: d,
    crossAxis: 0,
    alignmentAxis: null
  } : {
    mainAxis: d.mainAxis || 0,
    crossAxis: d.crossAxis || 0,
    alignmentAxis: d.alignmentAxis
  };
  return l && typeof p == "number" && (h = l === "end" ? p * -1 : p), a ? {
    x: h * u,
    y: f * c
  } : {
    x: f * c,
    y: h * u
  };
}
const F1 = function(n) {
  return n === void 0 && (n = 0), {
    name: "offset",
    options: n,
    async fn(e) {
      var t, r;
      const {
        x: o,
        y: i,
        placement: s,
        middlewareData: l
      } = e, a = await B1(e, n);
      return s === ((t = l.offset) == null ? void 0 : t.placement) && (r = l.arrow) != null && r.alignmentOffset ? {} : {
        x: o + a.x,
        y: i + a.y,
        data: {
          ...a,
          placement: s
        }
      };
    }
  };
}, z1 = function(n) {
  return n === void 0 && (n = {}), {
    name: "shift",
    options: n,
    async fn(e) {
      const {
        x: t,
        y: r,
        placement: o,
        platform: i
      } = e, {
        mainAxis: s = !0,
        crossAxis: l = !1,
        limiter: a = {
          fn: (y) => {
            let {
              x: b,
              y: w
            } = y;
            return {
              x: b,
              y: w
            };
          }
        },
        ...c
      } = Tt(n, e), u = {
        x: t,
        y: r
      }, d = await i.detectOverflow(e, c), f = at(At(o)), h = _a(f);
      let p = u[h], m = u[f];
      if (s) {
        const y = h === "y" ? "top" : "left", b = h === "y" ? "bottom" : "right", w = p + d[y], v = p - d[b];
        p = zl(w, p, v);
      }
      if (l) {
        const y = f === "y" ? "top" : "left", b = f === "y" ? "bottom" : "right", w = m + d[y], v = m - d[b];
        m = zl(w, m, v);
      }
      const g = a.fn({
        ...e,
        [h]: p,
        [f]: m
      });
      return {
        ...g,
        data: {
          x: g.x - t,
          y: g.y - r,
          enabled: {
            [h]: s,
            [f]: l
          }
        }
      };
    }
  };
}, $1 = function(n) {
  return n === void 0 && (n = {}), {
    options: n,
    fn(e) {
      const {
        x: t,
        y: r,
        placement: o,
        rects: i,
        middlewareData: s
      } = e, {
        offset: l = 0,
        mainAxis: a = !0,
        crossAxis: c = !0
      } = Tt(n, e), u = {
        x: t,
        y: r
      }, d = at(o), f = _a(d);
      let h = u[f], p = u[d];
      const m = Tt(l, e), g = typeof m == "number" ? {
        mainAxis: m,
        crossAxis: 0
      } : {
        mainAxis: 0,
        crossAxis: 0,
        ...m
      };
      if (a) {
        const w = f === "y" ? "height" : "width", v = i.reference[f] - i.floating[w] + g.mainAxis, k = i.reference[f] + i.reference[w] - g.mainAxis;
        h < v ? h = v : h > k && (h = k);
      }
      if (c) {
        var y, b;
        const w = f === "y" ? "width" : "height", v = zh.has(At(o)), k = i.reference[d] - i.floating[w] + (v && ((y = s.offset) == null ? void 0 : y[d]) || 0) + (v ? 0 : g.crossAxis), E = i.reference[d] + i.reference[w] + (v ? 0 : ((b = s.offset) == null ? void 0 : b[d]) || 0) - (v ? g.crossAxis : 0);
        p < k ? p = k : p > E && (p = E);
      }
      return {
        [f]: h,
        [d]: p
      };
    }
  };
}, H1 = function(n) {
  return n === void 0 && (n = {}), {
    name: "size",
    options: n,
    async fn(e) {
      var t, r;
      const {
        placement: o,
        rects: i,
        platform: s,
        elements: l
      } = e, {
        apply: a = () => {
        },
        ...c
      } = Tt(n, e), u = await s.detectOverflow(e, c), d = At(o), f = rr(o), h = at(o) === "y", {
        width: p,
        height: m
      } = i.floating;
      let g, y;
      d === "top" || d === "bottom" ? (g = d, y = f === (await (s.isRTL == null ? void 0 : s.isRTL(l.floating)) ? "start" : "end") ? "left" : "right") : (y = d, g = f === "end" ? "top" : "bottom");
      const b = m - u.top - u.bottom, w = p - u.left - u.right, v = Xt(m - u[g], b), k = Xt(p - u[y], w), E = !e.middlewareData.shift;
      let C = v, A = k;
      if ((t = e.middlewareData.shift) != null && t.enabled.x && (A = w), (r = e.middlewareData.shift) != null && r.enabled.y && (C = b), E && !f) {
        const j = _e(u.left, 0), G = _e(u.right, 0), Y = _e(u.top, 0), X = _e(u.bottom, 0);
        h ? A = p - 2 * (j !== 0 || G !== 0 ? j + G : _e(u.left, u.right)) : C = m - 2 * (Y !== 0 || X !== 0 ? Y + X : _e(u.top, u.bottom));
      }
      await a({
        ...e,
        availableWidth: A,
        availableHeight: C
      });
      const B = await s.getDimensions(l.floating);
      return p !== B.width || m !== B.height ? {
        reset: {
          rects: !0
        }
      } : {};
    }
  };
};
function Xi() {
  return typeof window < "u";
}
function or(n) {
  return $h(n) ? (n.nodeName || "").toLowerCase() : "#document";
}
function He(n) {
  var e;
  return (n == null || (e = n.ownerDocument) == null ? void 0 : e.defaultView) || window;
}
function pt(n) {
  var e;
  return (e = ($h(n) ? n.ownerDocument : n.document) || window.document) == null ? void 0 : e.documentElement;
}
function $h(n) {
  return Xi() ? n instanceof Node || n instanceof He(n).Node : !1;
}
function Ze(n) {
  return Xi() ? n instanceof Element || n instanceof He(n).Element : !1;
}
function Ot(n) {
  return Xi() ? n instanceof HTMLElement || n instanceof He(n).HTMLElement : !1;
}
function nd(n) {
  return !Xi() || typeof ShadowRoot > "u" ? !1 : n instanceof ShadowRoot || n instanceof He(n).ShadowRoot;
}
function Qr(n) {
  const {
    overflow: e,
    overflowX: t,
    overflowY: r,
    display: o
  } = et(n);
  return /auto|scroll|overlay|hidden|clip/.test(e + r + t) && o !== "inline" && o !== "contents";
}
function W1(n) {
  return /^(table|td|th)$/.test(or(n));
}
function Qi(n) {
  try {
    if (n.matches(":popover-open"))
      return !0;
  } catch {
  }
  try {
    return n.matches(":modal");
  } catch {
    return !1;
  }
}
const V1 = /transform|translate|scale|rotate|perspective|filter/, j1 = /paint|layout|strict|content/, sn = (n) => !!n && n !== "none";
let Gs;
function za(n) {
  const e = Ze(n) ? et(n) : n;
  return sn(e.transform) || sn(e.translate) || sn(e.scale) || sn(e.rotate) || sn(e.perspective) || !$a() && (sn(e.backdropFilter) || sn(e.filter)) || V1.test(e.willChange || "") || j1.test(e.contain || "");
}
function U1(n) {
  let e = Qt(n);
  for (; Ot(e) && !Xn(e); ) {
    if (za(e))
      return e;
    if (Qi(e))
      return null;
    e = Qt(e);
  }
  return null;
}
function $a() {
  return Gs == null && (Gs = typeof CSS < "u" && CSS.supports && CSS.supports("-webkit-backdrop-filter", "none")), Gs;
}
function Xn(n) {
  return /^(html|body|#document)$/.test(or(n));
}
function et(n) {
  return He(n).getComputedStyle(n);
}
function Zi(n) {
  return Ze(n) ? {
    scrollLeft: n.scrollLeft,
    scrollTop: n.scrollTop
  } : {
    scrollLeft: n.scrollX,
    scrollTop: n.scrollY
  };
}
function Qt(n) {
  if (or(n) === "html")
    return n;
  const e = (
    // Step into the shadow DOM of the parent of a slotted node.
    n.assignedSlot || // DOM Element detected.
    n.parentNode || // ShadowRoot detected.
    nd(n) && n.host || // Fallback.
    pt(n)
  );
  return nd(e) ? e.host : e;
}
function Hh(n) {
  const e = Qt(n);
  return Xn(e) ? n.ownerDocument ? n.ownerDocument.body : n.body : Ot(e) && Qr(e) ? e : Hh(e);
}
function Vr(n, e, t) {
  var r;
  e === void 0 && (e = []), t === void 0 && (t = !0);
  const o = Hh(n), i = o === ((r = n.ownerDocument) == null ? void 0 : r.body), s = He(o);
  if (i) {
    const l = Hl(s);
    return e.concat(s, s.visualViewport || [], Qr(o) ? o : [], l && t ? Vr(l) : []);
  } else
    return e.concat(o, Vr(o, [], t));
}
function Hl(n) {
  return n.parent && Object.getPrototypeOf(n.parent) ? n.frameElement : null;
}
function Wh(n) {
  const e = et(n);
  let t = parseFloat(e.width) || 0, r = parseFloat(e.height) || 0;
  const o = Ot(n), i = o ? n.offsetWidth : t, s = o ? n.offsetHeight : r, l = Mi(t) !== i || Mi(r) !== s;
  return l && (t = i, r = s), {
    width: t,
    height: r,
    $: l
  };
}
function Ha(n) {
  return Ze(n) ? n : n.contextElement;
}
function jn(n) {
  const e = Ha(n);
  if (!Ot(e))
    return ut(1);
  const t = e.getBoundingClientRect(), {
    width: r,
    height: o,
    $: i
  } = Wh(e);
  let s = (i ? Mi(t.width) : t.width) / r, l = (i ? Mi(t.height) : t.height) / o;
  return (!s || !Number.isFinite(s)) && (s = 1), (!l || !Number.isFinite(l)) && (l = 1), {
    x: s,
    y: l
  };
}
const K1 = /* @__PURE__ */ ut(0);
function Vh(n) {
  const e = He(n);
  return !$a() || !e.visualViewport ? K1 : {
    x: e.visualViewport.offsetLeft,
    y: e.visualViewport.offsetTop
  };
}
function q1(n, e, t) {
  return e === void 0 && (e = !1), !t || e && t !== He(n) ? !1 : e;
}
function Mn(n, e, t, r) {
  e === void 0 && (e = !1), t === void 0 && (t = !1);
  const o = n.getBoundingClientRect(), i = Ha(n);
  let s = ut(1);
  e && (r ? Ze(r) && (s = jn(r)) : s = jn(n));
  const l = q1(i, t, r) ? Vh(i) : ut(0);
  let a = (o.left + l.x) / s.x, c = (o.top + l.y) / s.y, u = o.width / s.x, d = o.height / s.y;
  if (i) {
    const f = He(i), h = r && Ze(r) ? He(r) : r;
    let p = f, m = Hl(p);
    for (; m && r && h !== p; ) {
      const g = jn(m), y = m.getBoundingClientRect(), b = et(m), w = y.left + (m.clientLeft + parseFloat(b.paddingLeft)) * g.x, v = y.top + (m.clientTop + parseFloat(b.paddingTop)) * g.y;
      a *= g.x, c *= g.y, u *= g.x, d *= g.y, a += w, c += v, p = He(m), m = Hl(p);
    }
  }
  return Ai({
    width: u,
    height: d,
    x: a,
    y: c
  });
}
function es(n, e) {
  const t = Zi(n).scrollLeft;
  return e ? e.left + t : Mn(pt(n)).left + t;
}
function jh(n, e) {
  const t = n.getBoundingClientRect(), r = t.left + e.scrollLeft - es(n, t), o = t.top + e.scrollTop;
  return {
    x: r,
    y: o
  };
}
function G1(n) {
  let {
    elements: e,
    rect: t,
    offsetParent: r,
    strategy: o
  } = n;
  const i = o === "fixed", s = pt(r), l = e ? Qi(e.floating) : !1;
  if (r === s || l && i)
    return t;
  let a = {
    scrollLeft: 0,
    scrollTop: 0
  }, c = ut(1);
  const u = ut(0), d = Ot(r);
  if ((d || !d && !i) && ((or(r) !== "body" || Qr(s)) && (a = Zi(r)), d)) {
    const h = Mn(r);
    c = jn(r), u.x = h.x + r.clientLeft, u.y = h.y + r.clientTop;
  }
  const f = s && !d && !i ? jh(s, a) : ut(0);
  return {
    width: t.width * c.x,
    height: t.height * c.y,
    x: t.x * c.x - a.scrollLeft * c.x + u.x + f.x,
    y: t.y * c.y - a.scrollTop * c.y + u.y + f.y
  };
}
function J1(n) {
  return Array.from(n.getClientRects());
}
function Y1(n) {
  const e = pt(n), t = Zi(n), r = n.ownerDocument.body, o = _e(e.scrollWidth, e.clientWidth, r.scrollWidth, r.clientWidth), i = _e(e.scrollHeight, e.clientHeight, r.scrollHeight, r.clientHeight);
  let s = -t.scrollLeft + es(n);
  const l = -t.scrollTop;
  return et(r).direction === "rtl" && (s += _e(e.clientWidth, r.clientWidth) - o), {
    width: o,
    height: i,
    x: s,
    y: l
  };
}
const rd = 25;
function X1(n, e) {
  const t = He(n), r = pt(n), o = t.visualViewport;
  let i = r.clientWidth, s = r.clientHeight, l = 0, a = 0;
  if (o) {
    i = o.width, s = o.height;
    const u = $a();
    (!u || u && e === "fixed") && (l = o.offsetLeft, a = o.offsetTop);
  }
  const c = es(r);
  if (c <= 0) {
    const u = r.ownerDocument, d = u.body, f = getComputedStyle(d), h = u.compatMode === "CSS1Compat" && parseFloat(f.marginLeft) + parseFloat(f.marginRight) || 0, p = Math.abs(r.clientWidth - d.clientWidth - h);
    p <= rd && (i -= p);
  } else c <= rd && (i += c);
  return {
    width: i,
    height: s,
    x: l,
    y: a
  };
}
function Q1(n, e) {
  const t = Mn(n, !0, e === "fixed"), r = t.top + n.clientTop, o = t.left + n.clientLeft, i = Ot(n) ? jn(n) : ut(1), s = n.clientWidth * i.x, l = n.clientHeight * i.y, a = o * i.x, c = r * i.y;
  return {
    width: s,
    height: l,
    x: a,
    y: c
  };
}
function od(n, e, t) {
  let r;
  if (e === "viewport")
    r = X1(n, t);
  else if (e === "document")
    r = Y1(pt(n));
  else if (Ze(e))
    r = Q1(e, t);
  else {
    const o = Vh(n);
    r = {
      x: e.x - o.x,
      y: e.y - o.y,
      width: e.width,
      height: e.height
    };
  }
  return Ai(r);
}
function Uh(n, e) {
  const t = Qt(n);
  return t === e || !Ze(t) || Xn(t) ? !1 : et(t).position === "fixed" || Uh(t, e);
}
function Z1(n, e) {
  const t = e.get(n);
  if (t)
    return t;
  let r = Vr(n, [], !1).filter((l) => Ze(l) && or(l) !== "body"), o = null;
  const i = et(n).position === "fixed";
  let s = i ? Qt(n) : n;
  for (; Ze(s) && !Xn(s); ) {
    const l = et(s), a = za(s);
    !a && l.position === "fixed" && (o = null), (i ? !a && !o : !a && l.position === "static" && !!o && (o.position === "absolute" || o.position === "fixed") || Qr(s) && !a && Uh(n, s)) ? r = r.filter((u) => u !== s) : o = l, s = Qt(s);
  }
  return e.set(n, r), r;
}
function ex(n) {
  let {
    element: e,
    boundary: t,
    rootBoundary: r,
    strategy: o
  } = n;
  const s = [...t === "clippingAncestors" ? Qi(e) ? [] : Z1(e, this._c) : [].concat(t), r], l = od(e, s[0], o);
  let a = l.top, c = l.right, u = l.bottom, d = l.left;
  for (let f = 1; f < s.length; f++) {
    const h = od(e, s[f], o);
    a = _e(h.top, a), c = Xt(h.right, c), u = Xt(h.bottom, u), d = _e(h.left, d);
  }
  return {
    width: c - d,
    height: u - a,
    x: d,
    y: a
  };
}
function tx(n) {
  const {
    width: e,
    height: t
  } = Wh(n);
  return {
    width: e,
    height: t
  };
}
function nx(n, e, t) {
  const r = Ot(e), o = pt(e), i = t === "fixed", s = Mn(n, !0, i, e);
  let l = {
    scrollLeft: 0,
    scrollTop: 0
  };
  const a = ut(0);
  function c() {
    a.x = es(o);
  }
  if (r || !r && !i)
    if ((or(e) !== "body" || Qr(o)) && (l = Zi(e)), r) {
      const h = Mn(e, !0, i, e);
      a.x = h.x + e.clientLeft, a.y = h.y + e.clientTop;
    } else o && c();
  i && !r && o && c();
  const u = o && !r && !i ? jh(o, l) : ut(0), d = s.left + l.scrollLeft - a.x - u.x, f = s.top + l.scrollTop - a.y - u.y;
  return {
    x: d,
    y: f,
    width: s.width,
    height: s.height
  };
}
function Js(n) {
  return et(n).position === "static";
}
function id(n, e) {
  if (!Ot(n) || et(n).position === "fixed")
    return null;
  if (e)
    return e(n);
  let t = n.offsetParent;
  return pt(n) === t && (t = t.ownerDocument.body), t;
}
function Kh(n, e) {
  const t = He(n);
  if (Qi(n))
    return t;
  if (!Ot(n)) {
    let o = Qt(n);
    for (; o && !Xn(o); ) {
      if (Ze(o) && !Js(o))
        return o;
      o = Qt(o);
    }
    return t;
  }
  let r = id(n, e);
  for (; r && W1(r) && Js(r); )
    r = id(r, e);
  return r && Xn(r) && Js(r) && !za(r) ? t : r || U1(n) || t;
}
const rx = async function(n) {
  const e = this.getOffsetParent || Kh, t = this.getDimensions, r = await t(n.floating);
  return {
    reference: nx(n.reference, await e(n.floating), n.strategy),
    floating: {
      x: 0,
      y: 0,
      width: r.width,
      height: r.height
    }
  };
};
function ox(n) {
  return et(n).direction === "rtl";
}
const ix = {
  convertOffsetParentRelativeRectToViewportRelativeRect: G1,
  getDocumentElement: pt,
  getClippingRect: ex,
  getOffsetParent: Kh,
  getElementRects: rx,
  getClientRects: J1,
  getDimensions: tx,
  getScale: jn,
  isElement: Ze,
  isRTL: ox
};
function qh(n, e) {
  return n.x === e.x && n.y === e.y && n.width === e.width && n.height === e.height;
}
function sx(n, e) {
  let t = null, r;
  const o = pt(n);
  function i() {
    var l;
    clearTimeout(r), (l = t) == null || l.disconnect(), t = null;
  }
  function s(l, a) {
    l === void 0 && (l = !1), a === void 0 && (a = 1), i();
    const c = n.getBoundingClientRect(), {
      left: u,
      top: d,
      width: f,
      height: h
    } = c;
    if (l || e(), !f || !h)
      return;
    const p = So(d), m = So(o.clientWidth - (u + f)), g = So(o.clientHeight - (d + h)), y = So(u), w = {
      rootMargin: -p + "px " + -m + "px " + -g + "px " + -y + "px",
      threshold: _e(0, Xt(1, a)) || 1
    };
    let v = !0;
    function k(E) {
      const C = E[0].intersectionRatio;
      if (C !== a) {
        if (!v)
          return s();
        C ? s(!1, C) : r = setTimeout(() => {
          s(!1, 1e-7);
        }, 1e3);
      }
      C === 1 && !qh(c, n.getBoundingClientRect()) && s(), v = !1;
    }
    try {
      t = new IntersectionObserver(k, {
        ...w,
        // Handle <iframe>s
        root: o.ownerDocument
      });
    } catch {
      t = new IntersectionObserver(k, w);
    }
    t.observe(n);
  }
  return s(!0), i;
}
function lx(n, e, t, r) {
  r === void 0 && (r = {});
  const {
    ancestorScroll: o = !0,
    ancestorResize: i = !0,
    elementResize: s = typeof ResizeObserver == "function",
    layoutShift: l = typeof IntersectionObserver == "function",
    animationFrame: a = !1
  } = r, c = Ha(n), u = o || i ? [...c ? Vr(c) : [], ...e ? Vr(e) : []] : [];
  u.forEach((y) => {
    o && y.addEventListener("scroll", t, {
      passive: !0
    }), i && y.addEventListener("resize", t);
  });
  const d = c && l ? sx(c, t) : null;
  let f = -1, h = null;
  s && (h = new ResizeObserver((y) => {
    let [b] = y;
    b && b.target === c && h && e && (h.unobserve(e), cancelAnimationFrame(f), f = requestAnimationFrame(() => {
      var w;
      (w = h) == null || w.observe(e);
    })), t();
  }), c && !a && h.observe(c), e && h.observe(e));
  let p, m = a ? Mn(n) : null;
  a && g();
  function g() {
    const y = Mn(n);
    m && !qh(m, y) && t(), m = y, p = requestAnimationFrame(g);
  }
  return t(), () => {
    var y;
    u.forEach((b) => {
      o && b.removeEventListener("scroll", t), i && b.removeEventListener("resize", t);
    }), d == null || d(), (y = h) == null || y.disconnect(), h = null, a && cancelAnimationFrame(p);
  };
}
const ax = F1, cx = z1, ux = L1, dx = H1, fx = _1, sd = I1, hx = $1, px = (n, e, t) => {
  const r = /* @__PURE__ */ new Map(), o = {
    platform: ix,
    ...t
  }, i = {
    ...o.platform,
    _c: r
  };
  return P1(n, e, {
    ...o,
    platform: i
  });
};
var mx = typeof document < "u", gx = function() {
}, _o = mx ? Yr : gx;
function Oi(n, e) {
  if (n === e)
    return !0;
  if (typeof n != typeof e)
    return !1;
  if (typeof n == "function" && n.toString() === e.toString())
    return !0;
  let t, r, o;
  if (n && e && typeof n == "object") {
    if (Array.isArray(n)) {
      if (t = n.length, t !== e.length) return !1;
      for (r = t; r-- !== 0; )
        if (!Oi(n[r], e[r]))
          return !1;
      return !0;
    }
    if (o = Object.keys(n), t = o.length, t !== Object.keys(e).length)
      return !1;
    for (r = t; r-- !== 0; )
      if (!{}.hasOwnProperty.call(e, o[r]))
        return !1;
    for (r = t; r-- !== 0; ) {
      const i = o[r];
      if (!(i === "_owner" && n.$$typeof) && !Oi(n[i], e[i]))
        return !1;
    }
    return !0;
  }
  return n !== n && e !== e;
}
function Gh(n) {
  return typeof window > "u" ? 1 : (n.ownerDocument.defaultView || window).devicePixelRatio || 1;
}
function ld(n, e) {
  const t = Gh(n);
  return Math.round(e * t) / t;
}
function Ys(n) {
  const e = D(n);
  return _o(() => {
    e.current = n;
  }), e;
}
function yx(n) {
  n === void 0 && (n = {});
  const {
    placement: e = "bottom",
    strategy: t = "absolute",
    middleware: r = [],
    platform: o,
    elements: {
      reference: i,
      floating: s
    } = {},
    transform: l = !0,
    whileElementsMounted: a,
    open: c
  } = n, [u, d] = K({
    x: 0,
    y: 0,
    strategy: t,
    placement: e,
    middlewareData: {},
    isPositioned: !1
  }), [f, h] = K(r);
  Oi(f, r) || h(r);
  const [p, m] = K(null), [g, y] = K(null), b = V(($) => {
    $ !== E.current && (E.current = $, m($));
  }, []), w = V(($) => {
    $ !== C.current && (C.current = $, y($));
  }, []), v = i || p, k = s || g, E = D(null), C = D(null), A = D(u), B = a != null, j = Ys(a), G = Ys(o), Y = Ys(c), X = V(() => {
    if (!E.current || !C.current)
      return;
    const $ = {
      placement: e,
      strategy: t,
      middleware: f
    };
    G.current && ($.platform = G.current), px(E.current, C.current, $).then((P) => {
      const re = {
        ...P,
        // The floating element's position may be recomputed while it's closed
        // but still mounted (such as when transitioning out). To ensure
        // `isPositioned` will be `false` initially on the next open, avoid
        // setting it to `true` when `open === false` (must be specified).
        isPositioned: Y.current !== !1
      };
      q.current && !Oi(A.current, re) && (A.current = re, rh(() => {
        d(re);
      }));
    });
  }, [f, e, t, G, Y]);
  _o(() => {
    c === !1 && A.current.isPositioned && (A.current.isPositioned = !1, d(($) => ({
      ...$,
      isPositioned: !1
    })));
  }, [c]);
  const q = D(!1);
  _o(() => (q.current = !0, () => {
    q.current = !1;
  }), []), _o(() => {
    if (v && (E.current = v), k && (C.current = k), v && k) {
      if (j.current)
        return j.current(v, k, X);
      X();
    }
  }, [v, k, X, j, B]);
  const oe = Xe(() => ({
    reference: E,
    floating: C,
    setReference: b,
    setFloating: w
  }), [b, w]), U = Xe(() => ({
    reference: v,
    floating: k
  }), [v, k]), Q = Xe(() => {
    const $ = {
      position: t,
      left: 0,
      top: 0
    };
    if (!U.floating)
      return $;
    const P = ld(U.floating, u.x), re = ld(U.floating, u.y);
    return l ? {
      ...$,
      transform: "translate(" + P + "px, " + re + "px)",
      ...Gh(U.floating) >= 1.5 && {
        willChange: "transform"
      }
    } : {
      position: t,
      left: P,
      top: re
    };
  }, [t, l, U.floating, u.x, u.y]);
  return Xe(() => ({
    ...u,
    update: X,
    refs: oe,
    elements: U,
    floatingStyles: Q
  }), [u, X, oe, U, Q]);
}
const bx = (n) => {
  function e(t) {
    return {}.hasOwnProperty.call(t, "current");
  }
  return {
    name: "arrow",
    options: n,
    fn(t) {
      const {
        element: r,
        padding: o
      } = typeof n == "function" ? n(t) : n;
      return r && e(r) ? r.current != null ? sd({
        element: r.current,
        padding: o
      }).fn(t) : {} : r ? sd({
        element: r,
        padding: o
      }).fn(t) : {};
    }
  };
}, wx = (n, e) => {
  const t = ax(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
}, vx = (n, e) => {
  const t = cx(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
}, Cx = (n, e) => ({
  fn: hx(n).fn,
  options: [n, e]
}), Sx = (n, e) => {
  const t = ux(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
}, xx = (n, e) => {
  const t = dx(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
}, kx = (n, e) => {
  const t = fx(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
}, Ex = (n, e) => {
  const t = bx(n);
  return {
    name: t.name,
    fn: t.fn,
    options: [n, e]
  };
};
var Mx = "Arrow", Jh = N((n, e) => {
  const { children: t, width: r = 10, height: o = 5, ...i } = n;
  return /* @__PURE__ */ S(
    de.svg,
    {
      ...i,
      ref: e,
      width: r,
      height: o,
      viewBox: "0 0 30 10",
      preserveAspectRatio: "none",
      children: n.asChild ? t : /* @__PURE__ */ S("polygon", { points: "0,0 30,0 15,10" })
    }
  );
});
Jh.displayName = Mx;
var Tx = Jh;
function Ax(n) {
  const [e, t] = K(void 0);
  return Yt(() => {
    if (n) {
      t({ width: n.offsetWidth, height: n.offsetHeight });
      const r = new ResizeObserver((o) => {
        if (!Array.isArray(o) || !o.length)
          return;
        const i = o[0];
        let s, l;
        if ("borderBoxSize" in i) {
          const a = i.borderBoxSize, c = Array.isArray(a) ? a[0] : a;
          s = c.inlineSize, l = c.blockSize;
        } else
          s = n.offsetWidth, l = n.offsetHeight;
        t({ width: s, height: l });
      });
      return r.observe(n, { box: "border-box" }), () => r.unobserve(n);
    } else
      t(void 0);
  }, [n]), e;
}
var Wa = "Popper", [Yh, ir] = On(Wa), [Ox, Xh] = Yh(Wa), Qh = (n) => {
  const { __scopePopper: e, children: t } = n, [r, o] = K(null);
  return /* @__PURE__ */ S(Ox, { scope: e, anchor: r, onAnchorChange: o, children: t });
};
Qh.displayName = Wa;
var Zh = "PopperAnchor", ep = N(
  (n, e) => {
    const { __scopePopper: t, virtualRef: r, ...o } = n, i = Xh(Zh, t), s = D(null), l = ge(e, s), a = D(null);
    return z(() => {
      const c = a.current;
      a.current = (r == null ? void 0 : r.current) || s.current, c !== a.current && i.onAnchorChange(a.current);
    }), r ? null : /* @__PURE__ */ S(de.div, { ...o, ref: l });
  }
);
ep.displayName = Zh;
var Va = "PopperContent", [Nx, Rx] = Yh(Va), tp = N(
  (n, e) => {
    var mt, ar, We, cr, oc, ic;
    const {
      __scopePopper: t,
      side: r = "bottom",
      sideOffset: o = 0,
      align: i = "center",
      alignOffset: s = 0,
      arrowPadding: l = 0,
      avoidCollisions: a = !0,
      collisionBoundary: c = [],
      collisionPadding: u = 0,
      sticky: d = "partial",
      hideWhenDetached: f = !1,
      updatePositionStrategy: h = "optimized",
      onPlaced: p,
      ...m
    } = n, g = Xh(Va, t), [y, b] = K(null), w = ge(e, (ur) => b(ur)), [v, k] = K(null), E = Ax(v), C = (E == null ? void 0 : E.width) ?? 0, A = (E == null ? void 0 : E.height) ?? 0, B = r + (i !== "center" ? "-" + i : ""), j = typeof u == "number" ? u : { top: 0, right: 0, bottom: 0, left: 0, ...u }, G = Array.isArray(c) ? c : [c], Y = G.length > 0, X = {
      padding: j,
      boundary: G.filter(Px),
      // with `strategy: 'fixed'`, this is the only way to get it to respect boundaries
      altBoundary: Y
    }, { refs: q, floatingStyles: oe, placement: U, isPositioned: Q, middlewareData: $ } = yx({
      // default to `fixed` strategy so users don't have to pick and we also avoid focus scroll issues
      strategy: "fixed",
      placement: B,
      whileElementsMounted: (...ur) => lx(...ur, {
        animationFrame: h === "always"
      }),
      elements: {
        reference: g.anchor
      },
      middleware: [
        wx({ mainAxis: o + A, alignmentAxis: s }),
        a && vx({
          mainAxis: !0,
          crossAxis: !1,
          limiter: d === "partial" ? Cx() : void 0,
          ...X
        }),
        a && Sx({ ...X }),
        xx({
          ...X,
          apply: ({ elements: ur, rects: sc, availableWidth: Em, availableHeight: Mm }) => {
            const { width: Tm, height: Am } = sc.reference, io = ur.floating.style;
            io.setProperty("--radix-popper-available-width", `${Em}px`), io.setProperty("--radix-popper-available-height", `${Mm}px`), io.setProperty("--radix-popper-anchor-width", `${Tm}px`), io.setProperty("--radix-popper-anchor-height", `${Am}px`);
          }
        }),
        v && Ex({ element: v, padding: l }),
        Ix({ arrowWidth: C, arrowHeight: A }),
        f && kx({ strategy: "referenceHidden", ...X })
      ]
    }), [P, re] = op(U), Ae = Mt(p);
    Yt(() => {
      Q && (Ae == null || Ae());
    }, [Q, Ae]);
    const nt = (mt = $.arrow) == null ? void 0 : mt.x, sr = (ar = $.arrow) == null ? void 0 : ar.y, lr = ((We = $.arrow) == null ? void 0 : We.centerOffset) !== 0, [oo, rn] = K();
    return Yt(() => {
      y && rn(window.getComputedStyle(y).zIndex);
    }, [y]), /* @__PURE__ */ S(
      "div",
      {
        ref: q.setFloating,
        "data-radix-popper-content-wrapper": "",
        style: {
          ...oe,
          transform: Q ? oe.transform : "translate(0, -200%)",
          // keep off the page when measuring
          minWidth: "max-content",
          zIndex: oo,
          "--radix-popper-transform-origin": [
            (cr = $.transformOrigin) == null ? void 0 : cr.x,
            (oc = $.transformOrigin) == null ? void 0 : oc.y
          ].join(" "),
          // hide the content if using the hide middleware and should be hidden
          // set visibility to hidden and disable pointer events so the UI behaves
          // as if the PopperContent isn't there at all
          ...((ic = $.hide) == null ? void 0 : ic.referenceHidden) && {
            visibility: "hidden",
            pointerEvents: "none"
          }
        },
        dir: n.dir,
        children: /* @__PURE__ */ S(
          Nx,
          {
            scope: t,
            placedSide: P,
            onArrowChange: k,
            arrowX: nt,
            arrowY: sr,
            shouldHideArrow: lr,
            children: /* @__PURE__ */ S(
              de.div,
              {
                "data-side": P,
                "data-align": re,
                ...m,
                ref: w,
                style: {
                  ...m.style,
                  // if the PopperContent hasn't been placed yet (not all measurements done)
                  // we prevent animations so that users's animation don't kick in too early referring wrong sides
                  animation: Q ? void 0 : "none"
                }
              }
            )
          }
        )
      }
    );
  }
);
tp.displayName = Va;
var np = "PopperArrow", Dx = {
  top: "bottom",
  right: "left",
  bottom: "top",
  left: "right"
}, rp = N(function(e, t) {
  const { __scopePopper: r, ...o } = e, i = Rx(np, r), s = Dx[i.placedSide];
  return (
    // we have to use an extra wrapper because `ResizeObserver` (used by `useSize`)
    // doesn't report size as we'd expect on SVG elements.
    // it reports their bounding box which is effectively the largest path inside the SVG.
    /* @__PURE__ */ S(
      "span",
      {
        ref: i.onArrowChange,
        style: {
          position: "absolute",
          left: i.arrowX,
          top: i.arrowY,
          [s]: 0,
          transformOrigin: {
            top: "",
            right: "0 0",
            bottom: "center 0",
            left: "100% 0"
          }[i.placedSide],
          transform: {
            top: "translateY(100%)",
            right: "translateY(50%) rotate(90deg) translateX(-50%)",
            bottom: "rotate(180deg)",
            left: "translateY(50%) rotate(-90deg) translateX(50%)"
          }[i.placedSide],
          visibility: i.shouldHideArrow ? "hidden" : void 0
        },
        children: /* @__PURE__ */ S(
          Tx,
          {
            ...o,
            ref: t,
            style: {
              ...o.style,
              // ensures the element can be measured correctly (mostly for if SVG)
              display: "block"
            }
          }
        )
      }
    )
  );
});
rp.displayName = np;
function Px(n) {
  return n !== null;
}
var Ix = (n) => ({
  name: "transformOrigin",
  options: n,
  fn(e) {
    var g, y, b;
    const { placement: t, rects: r, middlewareData: o } = e, s = ((g = o.arrow) == null ? void 0 : g.centerOffset) !== 0, l = s ? 0 : n.arrowWidth, a = s ? 0 : n.arrowHeight, [c, u] = op(t), d = { start: "0%", center: "50%", end: "100%" }[u], f = (((y = o.arrow) == null ? void 0 : y.x) ?? 0) + l / 2, h = (((b = o.arrow) == null ? void 0 : b.y) ?? 0) + a / 2;
    let p = "", m = "";
    return c === "bottom" ? (p = s ? d : `${f}px`, m = `${-a}px`) : c === "top" ? (p = s ? d : `${f}px`, m = `${r.floating.height + a}px`) : c === "right" ? (p = `${-a}px`, m = s ? d : `${h}px`) : c === "left" && (p = `${r.floating.width + a}px`, m = s ? d : `${h}px`), { data: { x: p, y: m } };
  }
});
function op(n) {
  const [e, t = "center"] = n.split("-");
  return [e, t];
}
var ja = Qh, ts = ep, Ua = tp, Ka = rp, Lx = "Portal", ns = N((n, e) => {
  var l;
  const { container: t, ...r } = n, [o, i] = K(!1);
  Yt(() => i(!0), []);
  const s = t || o && ((l = globalThis == null ? void 0 : globalThis.document) == null ? void 0 : l.body);
  return s ? Pi.createPortal(/* @__PURE__ */ S(de.div, { ...r, ref: e }), s) : null;
});
ns.displayName = Lx;
function _x(n, e) {
  return nh((t, r) => e[t][r] ?? t, n);
}
var Nt = (n) => {
  const { present: e, children: t } = n, r = Bx(e), o = typeof t == "function" ? t({ present: r.isPresent }) : $e.only(t), i = ge(r.ref, Fx(o));
  return typeof t == "function" || r.isPresent ? An(o, { ref: i }) : null;
};
Nt.displayName = "Presence";
function Bx(n) {
  const [e, t] = K(), r = D(null), o = D(n), i = D("none"), s = n ? "mounted" : "unmounted", [l, a] = _x(s, {
    mounted: {
      UNMOUNT: "unmounted",
      ANIMATION_OUT: "unmountSuspended"
    },
    unmountSuspended: {
      MOUNT: "mounted",
      ANIMATION_END: "unmounted"
    },
    unmounted: {
      MOUNT: "mounted"
    }
  });
  return z(() => {
    const c = xo(r.current);
    i.current = l === "mounted" ? c : "none";
  }, [l]), Yt(() => {
    const c = r.current, u = o.current;
    if (u !== n) {
      const f = i.current, h = xo(c);
      n ? a("MOUNT") : h === "none" || (c == null ? void 0 : c.display) === "none" ? a("UNMOUNT") : a(u && f !== h ? "ANIMATION_OUT" : "UNMOUNT"), o.current = n;
    }
  }, [n, a]), Yt(() => {
    if (e) {
      let c;
      const u = e.ownerDocument.defaultView ?? window, d = (h) => {
        const m = xo(r.current).includes(CSS.escape(h.animationName));
        if (h.target === e && m && (a("ANIMATION_END"), !o.current)) {
          const g = e.style.animationFillMode;
          e.style.animationFillMode = "forwards", c = u.setTimeout(() => {
            e.style.animationFillMode === "forwards" && (e.style.animationFillMode = g);
          });
        }
      }, f = (h) => {
        h.target === e && (i.current = xo(r.current));
      };
      return e.addEventListener("animationstart", f), e.addEventListener("animationcancel", d), e.addEventListener("animationend", d), () => {
        u.clearTimeout(c), e.removeEventListener("animationstart", f), e.removeEventListener("animationcancel", d), e.removeEventListener("animationend", d);
      };
    } else
      a("ANIMATION_END");
  }, [e, a]), {
    isPresent: ["mounted", "unmountSuspended"].includes(l),
    ref: V((c) => {
      r.current = c ? getComputedStyle(c) : null, t(c);
    }, [])
  };
}
function xo(n) {
  return (n == null ? void 0 : n.animationName) || "none";
}
function Fx(n) {
  var r, o;
  let e = (r = Object.getOwnPropertyDescriptor(n.props, "ref")) == null ? void 0 : r.get, t = e && "isReactWarning" in e && e.isReactWarning;
  return t ? n.ref : (e = (o = Object.getOwnPropertyDescriptor(n, "ref")) == null ? void 0 : o.get, t = e && "isReactWarning" in e && e.isReactWarning, t ? n.props.ref : n.props.ref || n.ref);
}
var Xs = "rovingFocusGroup.onEntryFocus", zx = { bubbles: !1, cancelable: !0 }, Zr = "RovingFocusGroup", [Wl, ip, $x] = Dh(Zr), [Hx, sp] = On(
  Zr,
  [$x]
), [Wx, Vx] = Hx(Zr), lp = N(
  (n, e) => /* @__PURE__ */ S(Wl.Provider, { scope: n.__scopeRovingFocusGroup, children: /* @__PURE__ */ S(Wl.Slot, { scope: n.__scopeRovingFocusGroup, children: /* @__PURE__ */ S(jx, { ...n, ref: e }) }) })
);
lp.displayName = Zr;
var jx = N((n, e) => {
  const {
    __scopeRovingFocusGroup: t,
    orientation: r,
    loop: o = !1,
    dir: i,
    currentTabStopId: s,
    defaultCurrentTabStopId: l,
    onCurrentTabStopIdChange: a,
    onEntryFocus: c,
    preventScrollOnEntryFocus: u = !1,
    ...d
  } = n, f = D(null), h = ge(e, f), p = Ph(i), [m, g] = Ji({
    prop: s,
    defaultProp: l ?? null,
    onChange: a,
    caller: Zr
  }), [y, b] = K(!1), w = Mt(c), v = ip(t), k = D(!1), [E, C] = K(0);
  return z(() => {
    const A = f.current;
    if (A)
      return A.addEventListener(Xs, w), () => A.removeEventListener(Xs, w);
  }, [w]), /* @__PURE__ */ S(
    Wx,
    {
      scope: t,
      orientation: r,
      dir: p,
      loop: o,
      currentTabStopId: m,
      onItemFocus: V(
        (A) => g(A),
        [g]
      ),
      onItemShiftTab: V(() => b(!0), []),
      onFocusableItemAdd: V(
        () => C((A) => A + 1),
        []
      ),
      onFocusableItemRemove: V(
        () => C((A) => A - 1),
        []
      ),
      children: /* @__PURE__ */ S(
        de.div,
        {
          tabIndex: y || E === 0 ? -1 : 0,
          "data-orientation": r,
          ...d,
          ref: h,
          style: { outline: "none", ...n.style },
          onMouseDown: F(n.onMouseDown, () => {
            k.current = !0;
          }),
          onFocus: F(n.onFocus, (A) => {
            const B = !k.current;
            if (A.target === A.currentTarget && B && !y) {
              const j = new CustomEvent(Xs, zx);
              if (A.currentTarget.dispatchEvent(j), !j.defaultPrevented) {
                const G = v().filter((U) => U.focusable), Y = G.find((U) => U.active), X = G.find((U) => U.id === m), oe = [Y, X, ...G].filter(
                  Boolean
                ).map((U) => U.ref.current);
                up(oe, u);
              }
            }
            k.current = !1;
          }),
          onBlur: F(n.onBlur, () => b(!1))
        }
      )
    }
  );
}), ap = "RovingFocusGroupItem", cp = N(
  (n, e) => {
    const {
      __scopeRovingFocusGroup: t,
      focusable: r = !0,
      active: o = !1,
      tabStopId: i,
      children: s,
      ...l
    } = n, a = Wr(), c = i || a, u = Vx(ap, t), d = u.currentTabStopId === c, f = ip(t), { onFocusableItemAdd: h, onFocusableItemRemove: p, currentTabStopId: m } = u;
    return z(() => {
      if (r)
        return h(), () => p();
    }, [r, h, p]), /* @__PURE__ */ S(
      Wl.ItemSlot,
      {
        scope: t,
        id: c,
        focusable: r,
        active: o,
        children: /* @__PURE__ */ S(
          de.span,
          {
            tabIndex: d ? 0 : -1,
            "data-orientation": u.orientation,
            ...l,
            ref: e,
            onMouseDown: F(n.onMouseDown, (g) => {
              r ? u.onItemFocus(c) : g.preventDefault();
            }),
            onFocus: F(n.onFocus, () => u.onItemFocus(c)),
            onKeyDown: F(n.onKeyDown, (g) => {
              if (g.key === "Tab" && g.shiftKey) {
                u.onItemShiftTab();
                return;
              }
              if (g.target !== g.currentTarget) return;
              const y = qx(g, u.orientation, u.dir);
              if (y !== void 0) {
                if (g.metaKey || g.ctrlKey || g.altKey || g.shiftKey) return;
                g.preventDefault();
                let w = f().filter((v) => v.focusable).map((v) => v.ref.current);
                if (y === "last") w.reverse();
                else if (y === "prev" || y === "next") {
                  y === "prev" && w.reverse();
                  const v = w.indexOf(g.currentTarget);
                  w = u.loop ? Gx(w, v + 1) : w.slice(v + 1);
                }
                setTimeout(() => up(w));
              }
            }),
            children: typeof s == "function" ? s({ isCurrentTabStop: d, hasTabStop: m != null }) : s
          }
        )
      }
    );
  }
);
cp.displayName = ap;
var Ux = {
  ArrowLeft: "prev",
  ArrowUp: "prev",
  ArrowRight: "next",
  ArrowDown: "next",
  PageUp: "first",
  Home: "first",
  PageDown: "last",
  End: "last"
};
function Kx(n, e) {
  return e !== "rtl" ? n : n === "ArrowLeft" ? "ArrowRight" : n === "ArrowRight" ? "ArrowLeft" : n;
}
function qx(n, e, t) {
  const r = Kx(n.key, t);
  if (!(e === "vertical" && ["ArrowLeft", "ArrowRight"].includes(r)) && !(e === "horizontal" && ["ArrowUp", "ArrowDown"].includes(r)))
    return Ux[r];
}
function up(n, e = !1) {
  const t = document.activeElement;
  for (const r of n)
    if (r === t || (r.focus({ preventScroll: e }), document.activeElement !== t)) return;
}
function Gx(n, e) {
  return n.map((t, r) => n[(e + r) % n.length]);
}
var Jx = lp, Yx = cp, Xx = function(n) {
  if (typeof document > "u")
    return null;
  var e = Array.isArray(n) ? n[0] : n;
  return e.ownerDocument.body;
}, In = /* @__PURE__ */ new WeakMap(), ko = /* @__PURE__ */ new WeakMap(), Eo = {}, Qs = 0, dp = function(n) {
  return n && (n.host || dp(n.parentNode));
}, Qx = function(n, e) {
  return e.map(function(t) {
    if (n.contains(t))
      return t;
    var r = dp(t);
    return r && n.contains(r) ? r : (console.error("aria-hidden", t, "in not contained inside", n, ". Doing nothing"), null);
  }).filter(function(t) {
    return !!t;
  });
}, Zx = function(n, e, t, r) {
  var o = Qx(e, Array.isArray(n) ? n : [n]);
  Eo[t] || (Eo[t] = /* @__PURE__ */ new WeakMap());
  var i = Eo[t], s = [], l = /* @__PURE__ */ new Set(), a = new Set(o), c = function(d) {
    !d || l.has(d) || (l.add(d), c(d.parentNode));
  };
  o.forEach(c);
  var u = function(d) {
    !d || a.has(d) || Array.prototype.forEach.call(d.children, function(f) {
      if (l.has(f))
        u(f);
      else
        try {
          var h = f.getAttribute(r), p = h !== null && h !== "false", m = (In.get(f) || 0) + 1, g = (i.get(f) || 0) + 1;
          In.set(f, m), i.set(f, g), s.push(f), m === 1 && p && ko.set(f, !0), g === 1 && f.setAttribute(t, "true"), p || f.setAttribute(r, "true");
        } catch (y) {
          console.error("aria-hidden: cannot operate on ", f, y);
        }
    });
  };
  return u(e), l.clear(), Qs++, function() {
    s.forEach(function(d) {
      var f = In.get(d) - 1, h = i.get(d) - 1;
      In.set(d, f), i.set(d, h), f || (ko.has(d) || d.removeAttribute(r), ko.delete(d)), h || d.removeAttribute(t);
    }), Qs--, Qs || (In = /* @__PURE__ */ new WeakMap(), In = /* @__PURE__ */ new WeakMap(), ko = /* @__PURE__ */ new WeakMap(), Eo = {});
  };
}, fp = function(n, e, t) {
  t === void 0 && (t = "data-aria-hidden");
  var r = Array.from(Array.isArray(n) ? n : [n]), o = Xx(n);
  return o ? (r.push.apply(r, Array.from(o.querySelectorAll("[aria-live], script"))), Zx(r, o, t, "aria-hidden")) : function() {
    return null;
  };
}, st = function() {
  return st = Object.assign || function(e) {
    for (var t, r = 1, o = arguments.length; r < o; r++) {
      t = arguments[r];
      for (var i in t) Object.prototype.hasOwnProperty.call(t, i) && (e[i] = t[i]);
    }
    return e;
  }, st.apply(this, arguments);
};
function hp(n, e) {
  var t = {};
  for (var r in n) Object.prototype.hasOwnProperty.call(n, r) && e.indexOf(r) < 0 && (t[r] = n[r]);
  if (n != null && typeof Object.getOwnPropertySymbols == "function")
    for (var o = 0, r = Object.getOwnPropertySymbols(n); o < r.length; o++)
      e.indexOf(r[o]) < 0 && Object.prototype.propertyIsEnumerable.call(n, r[o]) && (t[r[o]] = n[r[o]]);
  return t;
}
function ek(n, e, t) {
  if (t || arguments.length === 2) for (var r = 0, o = e.length, i; r < o; r++)
    (i || !(r in e)) && (i || (i = Array.prototype.slice.call(e, 0, r)), i[r] = e[r]);
  return n.concat(i || Array.prototype.slice.call(e));
}
var Bo = "right-scroll-bar-position", Fo = "width-before-scroll-bar", tk = "with-scroll-bars-hidden", nk = "--removed-body-scroll-bar-size";
function Zs(n, e) {
  return typeof n == "function" ? n(e) : n && (n.current = e), n;
}
function rk(n, e) {
  var t = K(function() {
    return {
      // value
      value: n,
      // last callback
      callback: e,
      // "memoized" public interface
      facade: {
        get current() {
          return t.value;
        },
        set current(r) {
          var o = t.value;
          o !== r && (t.value = r, t.callback(r, o));
        }
      }
    };
  })[0];
  return t.callback = e, t.facade;
}
var ok = typeof window < "u" ? Yr : z, ad = /* @__PURE__ */ new WeakMap();
function ik(n, e) {
  var t = rk(null, function(r) {
    return n.forEach(function(o) {
      return Zs(o, r);
    });
  });
  return ok(function() {
    var r = ad.get(t);
    if (r) {
      var o = new Set(r), i = new Set(n), s = t.current;
      o.forEach(function(l) {
        i.has(l) || Zs(l, null);
      }), i.forEach(function(l) {
        o.has(l) || Zs(l, s);
      });
    }
    ad.set(t, n);
  }, [n]), t;
}
function sk(n) {
  return n;
}
function lk(n, e) {
  e === void 0 && (e = sk);
  var t = [], r = !1, o = {
    read: function() {
      if (r)
        throw new Error("Sidecar: could not `read` from an `assigned` medium. `read` could be used only with `useMedium`.");
      return t.length ? t[t.length - 1] : n;
    },
    useMedium: function(i) {
      var s = e(i, r);
      return t.push(s), function() {
        t = t.filter(function(l) {
          return l !== s;
        });
      };
    },
    assignSyncMedium: function(i) {
      for (r = !0; t.length; ) {
        var s = t;
        t = [], s.forEach(i);
      }
      t = {
        push: function(l) {
          return i(l);
        },
        filter: function() {
          return t;
        }
      };
    },
    assignMedium: function(i) {
      r = !0;
      var s = [];
      if (t.length) {
        var l = t;
        t = [], l.forEach(i), s = t;
      }
      var a = function() {
        var u = s;
        s = [], u.forEach(i);
      }, c = function() {
        return Promise.resolve().then(a);
      };
      c(), t = {
        push: function(u) {
          s.push(u), c();
        },
        filter: function(u) {
          return s = s.filter(u), t;
        }
      };
    }
  };
  return o;
}
function ak(n) {
  n === void 0 && (n = {});
  var e = lk(null);
  return e.options = st({ async: !0, ssr: !1 }, n), e;
}
var pp = function(n) {
  var e = n.sideCar, t = hp(n, ["sideCar"]);
  if (!e)
    throw new Error("Sidecar: please provide `sideCar` property to import the right car");
  var r = e.read();
  if (!r)
    throw new Error("Sidecar medium not found");
  return ct(r, st({}, t));
};
pp.isSideCarExport = !0;
function ck(n, e) {
  return n.useMedium(e), pp;
}
var mp = ak(), el = function() {
}, rs = N(function(n, e) {
  var t = D(null), r = K({
    onScrollCapture: el,
    onWheelCapture: el,
    onTouchMoveCapture: el
  }), o = r[0], i = r[1], s = n.forwardProps, l = n.children, a = n.className, c = n.removeScrollBar, u = n.enabled, d = n.shards, f = n.sideCar, h = n.noRelative, p = n.noIsolation, m = n.inert, g = n.allowPinchZoom, y = n.as, b = y === void 0 ? "div" : y, w = n.gapMode, v = hp(n, ["forwardProps", "children", "className", "removeScrollBar", "enabled", "shards", "sideCar", "noRelative", "noIsolation", "inert", "allowPinchZoom", "as", "gapMode"]), k = f, E = ik([t, e]), C = st(st({}, v), o);
  return ct(
    nr,
    null,
    u && ct(k, { sideCar: mp, removeScrollBar: c, shards: d, noRelative: h, noIsolation: p, inert: m, setCallbacks: i, allowPinchZoom: !!g, lockRef: t, gapMode: w }),
    s ? An($e.only(l), st(st({}, C), { ref: E })) : ct(b, st({}, C, { className: a, ref: E }), l)
  );
});
rs.defaultProps = {
  enabled: !0,
  removeScrollBar: !0,
  inert: !1
};
rs.classNames = {
  fullWidth: Fo,
  zeroRight: Bo
};
var uk = function() {
  if (typeof __webpack_nonce__ < "u")
    return __webpack_nonce__;
};
function dk() {
  if (!document)
    return null;
  var n = document.createElement("style");
  n.type = "text/css";
  var e = uk();
  return e && n.setAttribute("nonce", e), n;
}
function fk(n, e) {
  n.styleSheet ? n.styleSheet.cssText = e : n.appendChild(document.createTextNode(e));
}
function hk(n) {
  var e = document.head || document.getElementsByTagName("head")[0];
  e.appendChild(n);
}
var pk = function() {
  var n = 0, e = null;
  return {
    add: function(t) {
      n == 0 && (e = dk()) && (fk(e, t), hk(e)), n++;
    },
    remove: function() {
      n--, !n && e && (e.parentNode && e.parentNode.removeChild(e), e = null);
    }
  };
}, mk = function() {
  var n = pk();
  return function(e, t) {
    z(function() {
      return n.add(e), function() {
        n.remove();
      };
    }, [e && t]);
  };
}, gp = function() {
  var n = mk(), e = function(t) {
    var r = t.styles, o = t.dynamic;
    return n(r, o), null;
  };
  return e;
}, gk = {
  left: 0,
  top: 0,
  right: 0,
  gap: 0
}, tl = function(n) {
  return parseInt(n || "", 10) || 0;
}, yk = function(n) {
  var e = window.getComputedStyle(document.body), t = e[n === "padding" ? "paddingLeft" : "marginLeft"], r = e[n === "padding" ? "paddingTop" : "marginTop"], o = e[n === "padding" ? "paddingRight" : "marginRight"];
  return [tl(t), tl(r), tl(o)];
}, bk = function(n) {
  if (n === void 0 && (n = "margin"), typeof window > "u")
    return gk;
  var e = yk(n), t = document.documentElement.clientWidth, r = window.innerWidth;
  return {
    left: e[0],
    top: e[1],
    right: e[2],
    gap: Math.max(0, r - t + e[2] - e[0])
  };
}, wk = gp(), Un = "data-scroll-locked", vk = function(n, e, t, r) {
  var o = n.left, i = n.top, s = n.right, l = n.gap;
  return t === void 0 && (t = "margin"), `
  .`.concat(tk, ` {
   overflow: hidden `).concat(r, `;
   padding-right: `).concat(l, "px ").concat(r, `;
  }
  body[`).concat(Un, `] {
    overflow: hidden `).concat(r, `;
    overscroll-behavior: contain;
    `).concat([
    e && "position: relative ".concat(r, ";"),
    t === "margin" && `
    padding-left: `.concat(o, `px;
    padding-top: `).concat(i, `px;
    padding-right: `).concat(s, `px;
    margin-left:0;
    margin-top:0;
    margin-right: `).concat(l, "px ").concat(r, `;
    `),
    t === "padding" && "padding-right: ".concat(l, "px ").concat(r, ";")
  ].filter(Boolean).join(""), `
  }
  
  .`).concat(Bo, ` {
    right: `).concat(l, "px ").concat(r, `;
  }
  
  .`).concat(Fo, ` {
    margin-right: `).concat(l, "px ").concat(r, `;
  }
  
  .`).concat(Bo, " .").concat(Bo, ` {
    right: 0 `).concat(r, `;
  }
  
  .`).concat(Fo, " .").concat(Fo, ` {
    margin-right: 0 `).concat(r, `;
  }
  
  body[`).concat(Un, `] {
    `).concat(nk, ": ").concat(l, `px;
  }
`);
}, cd = function() {
  var n = parseInt(document.body.getAttribute(Un) || "0", 10);
  return isFinite(n) ? n : 0;
}, Ck = function() {
  z(function() {
    return document.body.setAttribute(Un, (cd() + 1).toString()), function() {
      var n = cd() - 1;
      n <= 0 ? document.body.removeAttribute(Un) : document.body.setAttribute(Un, n.toString());
    };
  }, []);
}, Sk = function(n) {
  var e = n.noRelative, t = n.noImportant, r = n.gapMode, o = r === void 0 ? "margin" : r;
  Ck();
  var i = Xe(function() {
    return bk(o);
  }, [o]);
  return ct(wk, { styles: vk(i, !e, o, t ? "" : "!important") });
}, Vl = !1;
if (typeof window < "u")
  try {
    var Mo = Object.defineProperty({}, "passive", {
      get: function() {
        return Vl = !0, !0;
      }
    });
    window.addEventListener("test", Mo, Mo), window.removeEventListener("test", Mo, Mo);
  } catch {
    Vl = !1;
  }
var Ln = Vl ? { passive: !1 } : !1, xk = function(n) {
  return n.tagName === "TEXTAREA";
}, yp = function(n, e) {
  if (!(n instanceof Element))
    return !1;
  var t = window.getComputedStyle(n);
  return (
    // not-not-scrollable
    t[e] !== "hidden" && // contains scroll inside self
    !(t.overflowY === t.overflowX && !xk(n) && t[e] === "visible")
  );
}, kk = function(n) {
  return yp(n, "overflowY");
}, Ek = function(n) {
  return yp(n, "overflowX");
}, ud = function(n, e) {
  var t = e.ownerDocument, r = e;
  do {
    typeof ShadowRoot < "u" && r instanceof ShadowRoot && (r = r.host);
    var o = bp(n, r);
    if (o) {
      var i = wp(n, r), s = i[1], l = i[2];
      if (s > l)
        return !0;
    }
    r = r.parentNode;
  } while (r && r !== t.body);
  return !1;
}, Mk = function(n) {
  var e = n.scrollTop, t = n.scrollHeight, r = n.clientHeight;
  return [
    e,
    t,
    r
  ];
}, Tk = function(n) {
  var e = n.scrollLeft, t = n.scrollWidth, r = n.clientWidth;
  return [
    e,
    t,
    r
  ];
}, bp = function(n, e) {
  return n === "v" ? kk(e) : Ek(e);
}, wp = function(n, e) {
  return n === "v" ? Mk(e) : Tk(e);
}, Ak = function(n, e) {
  return n === "h" && e === "rtl" ? -1 : 1;
}, Ok = function(n, e, t, r, o) {
  var i = Ak(n, window.getComputedStyle(e).direction), s = i * r, l = t.target, a = e.contains(l), c = !1, u = s > 0, d = 0, f = 0;
  do {
    if (!l)
      break;
    var h = wp(n, l), p = h[0], m = h[1], g = h[2], y = m - g - i * p;
    (p || y) && bp(n, l) && (d += y, f += p);
    var b = l.parentNode;
    l = b && b.nodeType === Node.DOCUMENT_FRAGMENT_NODE ? b.host : b;
  } while (
    // portaled content
    !a && l !== document.body || // self content
    a && (e.contains(l) || e === l)
  );
  return (u && Math.abs(d) < 1 || !u && Math.abs(f) < 1) && (c = !0), c;
}, To = function(n) {
  return "changedTouches" in n ? [n.changedTouches[0].clientX, n.changedTouches[0].clientY] : [0, 0];
}, dd = function(n) {
  return [n.deltaX, n.deltaY];
}, fd = function(n) {
  return n && "current" in n ? n.current : n;
}, Nk = function(n, e) {
  return n[0] === e[0] && n[1] === e[1];
}, Rk = function(n) {
  return `
  .block-interactivity-`.concat(n, ` {pointer-events: none;}
  .allow-interactivity-`).concat(n, ` {pointer-events: all;}
`);
}, Dk = 0, _n = [];
function Pk(n) {
  var e = D([]), t = D([0, 0]), r = D(), o = K(Dk++)[0], i = K(gp)[0], s = D(n);
  z(function() {
    s.current = n;
  }, [n]), z(function() {
    if (n.inert) {
      document.body.classList.add("block-interactivity-".concat(o));
      var m = ek([n.lockRef.current], (n.shards || []).map(fd), !0).filter(Boolean);
      return m.forEach(function(g) {
        return g.classList.add("allow-interactivity-".concat(o));
      }), function() {
        document.body.classList.remove("block-interactivity-".concat(o)), m.forEach(function(g) {
          return g.classList.remove("allow-interactivity-".concat(o));
        });
      };
    }
  }, [n.inert, n.lockRef.current, n.shards]);
  var l = V(function(m, g) {
    if ("touches" in m && m.touches.length === 2 || m.type === "wheel" && m.ctrlKey)
      return !s.current.allowPinchZoom;
    var y = To(m), b = t.current, w = "deltaX" in m ? m.deltaX : b[0] - y[0], v = "deltaY" in m ? m.deltaY : b[1] - y[1], k, E = m.target, C = Math.abs(w) > Math.abs(v) ? "h" : "v";
    if ("touches" in m && C === "h" && E.type === "range")
      return !1;
    var A = window.getSelection(), B = A && A.anchorNode, j = B ? B === E || B.contains(E) : !1;
    if (j)
      return !1;
    var G = ud(C, E);
    if (!G)
      return !0;
    if (G ? k = C : (k = C === "v" ? "h" : "v", G = ud(C, E)), !G)
      return !1;
    if (!r.current && "changedTouches" in m && (w || v) && (r.current = k), !k)
      return !0;
    var Y = r.current || k;
    return Ok(Y, g, m, Y === "h" ? w : v);
  }, []), a = V(function(m) {
    var g = m;
    if (!(!_n.length || _n[_n.length - 1] !== i)) {
      var y = "deltaY" in g ? dd(g) : To(g), b = e.current.filter(function(k) {
        return k.name === g.type && (k.target === g.target || g.target === k.shadowParent) && Nk(k.delta, y);
      })[0];
      if (b && b.should) {
        g.cancelable && g.preventDefault();
        return;
      }
      if (!b) {
        var w = (s.current.shards || []).map(fd).filter(Boolean).filter(function(k) {
          return k.contains(g.target);
        }), v = w.length > 0 ? l(g, w[0]) : !s.current.noIsolation;
        v && g.cancelable && g.preventDefault();
      }
    }
  }, []), c = V(function(m, g, y, b) {
    var w = { name: m, delta: g, target: y, should: b, shadowParent: Ik(y) };
    e.current.push(w), setTimeout(function() {
      e.current = e.current.filter(function(v) {
        return v !== w;
      });
    }, 1);
  }, []), u = V(function(m) {
    t.current = To(m), r.current = void 0;
  }, []), d = V(function(m) {
    c(m.type, dd(m), m.target, l(m, n.lockRef.current));
  }, []), f = V(function(m) {
    c(m.type, To(m), m.target, l(m, n.lockRef.current));
  }, []);
  z(function() {
    return _n.push(i), n.setCallbacks({
      onScrollCapture: d,
      onWheelCapture: d,
      onTouchMoveCapture: f
    }), document.addEventListener("wheel", a, Ln), document.addEventListener("touchmove", a, Ln), document.addEventListener("touchstart", u, Ln), function() {
      _n = _n.filter(function(m) {
        return m !== i;
      }), document.removeEventListener("wheel", a, Ln), document.removeEventListener("touchmove", a, Ln), document.removeEventListener("touchstart", u, Ln);
    };
  }, []);
  var h = n.removeScrollBar, p = n.inert;
  return ct(
    nr,
    null,
    p ? ct(i, { styles: Rk(o) }) : null,
    h ? ct(Sk, { noRelative: n.noRelative, gapMode: n.gapMode }) : null
  );
}
function Ik(n) {
  for (var e = null; n !== null; )
    n instanceof ShadowRoot && (e = n.host, n = n.host), n = n.parentNode;
  return e;
}
const Lk = ck(mp, Pk);
var qa = N(function(n, e) {
  return ct(rs, st({}, n, { ref: e, sideCar: Lk }));
});
qa.classNames = rs.classNames;
var jl = ["Enter", " "], _k = ["ArrowDown", "PageUp", "Home"], vp = ["ArrowUp", "PageDown", "End"], Bk = [..._k, ...vp], Fk = {
  ltr: [...jl, "ArrowRight"],
  rtl: [...jl, "ArrowLeft"]
}, zk = {
  ltr: ["ArrowLeft"],
  rtl: ["ArrowRight"]
}, eo = "Menu", [jr, $k, Hk] = Dh(eo), [Nn, Cp] = On(eo, [
  Hk,
  ir,
  sp
]), is = ir(), Sp = sp(), [Wk, Rn] = Nn(eo), [Vk, to] = Nn(eo), xp = (n) => {
  const { __scopeMenu: e, open: t = !1, children: r, dir: o, onOpenChange: i, modal: s = !0 } = n, l = is(e), [a, c] = K(null), u = D(!1), d = Mt(i), f = Ph(o);
  return z(() => {
    const h = () => {
      u.current = !0, document.addEventListener("pointerdown", p, { capture: !0, once: !0 }), document.addEventListener("pointermove", p, { capture: !0, once: !0 });
    }, p = () => u.current = !1;
    return document.addEventListener("keydown", h, { capture: !0 }), () => {
      document.removeEventListener("keydown", h, { capture: !0 }), document.removeEventListener("pointerdown", p, { capture: !0 }), document.removeEventListener("pointermove", p, { capture: !0 });
    };
  }, []), /* @__PURE__ */ S(ja, { ...l, children: /* @__PURE__ */ S(
    Wk,
    {
      scope: e,
      open: t,
      onOpenChange: d,
      content: a,
      onContentChange: c,
      children: /* @__PURE__ */ S(
        Vk,
        {
          scope: e,
          onClose: V(() => d(!1), [d]),
          isUsingKeyboardRef: u,
          dir: f,
          modal: s,
          children: r
        }
      )
    }
  ) });
};
xp.displayName = eo;
var jk = "MenuAnchor", Ga = N(
  (n, e) => {
    const { __scopeMenu: t, ...r } = n, o = is(t);
    return /* @__PURE__ */ S(ts, { ...o, ...r, ref: e });
  }
);
Ga.displayName = jk;
var Ja = "MenuPortal", [Uk, kp] = Nn(Ja, {
  forceMount: void 0
}), Ep = (n) => {
  const { __scopeMenu: e, forceMount: t, children: r, container: o } = n, i = Rn(Ja, e);
  return /* @__PURE__ */ S(Uk, { scope: e, forceMount: t, children: /* @__PURE__ */ S(Nt, { present: t || i.open, children: /* @__PURE__ */ S(ns, { asChild: !0, container: o, children: r }) }) });
};
Ep.displayName = Ja;
var Je = "MenuContent", [Kk, Ya] = Nn(Je), Mp = N(
  (n, e) => {
    const t = kp(Je, n.__scopeMenu), { forceMount: r = t.forceMount, ...o } = n, i = Rn(Je, n.__scopeMenu), s = to(Je, n.__scopeMenu);
    return /* @__PURE__ */ S(jr.Provider, { scope: n.__scopeMenu, children: /* @__PURE__ */ S(Nt, { present: r || i.open, children: /* @__PURE__ */ S(jr.Slot, { scope: n.__scopeMenu, children: s.modal ? /* @__PURE__ */ S(qk, { ...o, ref: e }) : /* @__PURE__ */ S(Gk, { ...o, ref: e }) }) }) });
  }
), qk = N(
  (n, e) => {
    const t = Rn(Je, n.__scopeMenu), r = D(null), o = ge(e, r);
    return z(() => {
      const i = r.current;
      if (i) return fp(i);
    }, []), /* @__PURE__ */ S(
      Xa,
      {
        ...n,
        ref: o,
        trapFocus: t.open,
        disableOutsidePointerEvents: t.open,
        disableOutsideScroll: !0,
        onFocusOutside: F(
          n.onFocusOutside,
          (i) => i.preventDefault(),
          { checkForDefaultPrevented: !1 }
        ),
        onDismiss: () => t.onOpenChange(!1)
      }
    );
  }
), Gk = N((n, e) => {
  const t = Rn(Je, n.__scopeMenu);
  return /* @__PURE__ */ S(
    Xa,
    {
      ...n,
      ref: e,
      trapFocus: !1,
      disableOutsidePointerEvents: !1,
      disableOutsideScroll: !1,
      onDismiss: () => t.onOpenChange(!1)
    }
  );
}), Jk = /* @__PURE__ */ Hr("MenuContent.ScrollLock"), Xa = N(
  (n, e) => {
    const {
      __scopeMenu: t,
      loop: r = !1,
      trapFocus: o,
      onOpenAutoFocus: i,
      onCloseAutoFocus: s,
      disableOutsidePointerEvents: l,
      onEntryFocus: a,
      onEscapeKeyDown: c,
      onPointerDownOutside: u,
      onFocusOutside: d,
      onInteractOutside: f,
      onDismiss: h,
      disableOutsideScroll: p,
      ...m
    } = n, g = Rn(Je, t), y = to(Je, t), b = is(t), w = Sp(t), v = $k(t), [k, E] = K(null), C = D(null), A = ge(e, C, g.onContentChange), B = D(0), j = D(""), G = D(0), Y = D(null), X = D("right"), q = D(0), oe = p ? qa : nr, U = p ? { as: Jk, allowPinchZoom: !0 } : void 0, Q = (P) => {
      var mt, ar;
      const re = j.current + P, Ae = v().filter((We) => !We.disabled), nt = document.activeElement, sr = (mt = Ae.find((We) => We.ref.current === nt)) == null ? void 0 : mt.textValue, lr = Ae.map((We) => We.textValue), oo = lE(lr, re, sr), rn = (ar = Ae.find((We) => We.textValue === oo)) == null ? void 0 : ar.ref.current;
      (function We(cr) {
        j.current = cr, window.clearTimeout(B.current), cr !== "" && (B.current = window.setTimeout(() => We(""), 1e3));
      })(re), rn && setTimeout(() => rn.focus());
    };
    z(() => () => window.clearTimeout(B.current), []), _h();
    const $ = V((P) => {
      var Ae, nt;
      return X.current === ((Ae = Y.current) == null ? void 0 : Ae.side) && cE(P, (nt = Y.current) == null ? void 0 : nt.area);
    }, []);
    return /* @__PURE__ */ S(
      Kk,
      {
        scope: t,
        searchRef: j,
        onItemEnter: V(
          (P) => {
            $(P) && P.preventDefault();
          },
          [$]
        ),
        onItemLeave: V(
          (P) => {
            var re;
            $(P) || ((re = C.current) == null || re.focus(), E(null));
          },
          [$]
        ),
        onTriggerLeave: V(
          (P) => {
            $(P) && P.preventDefault();
          },
          [$]
        ),
        pointerGraceTimerRef: G,
        onPointerGraceIntentChange: V((P) => {
          Y.current = P;
        }, []),
        children: /* @__PURE__ */ S(oe, { ...U, children: /* @__PURE__ */ S(
          La,
          {
            asChild: !0,
            trapped: o,
            onMountAutoFocus: F(i, (P) => {
              var re;
              P.preventDefault(), (re = C.current) == null || re.focus({ preventScroll: !0 });
            }),
            onUnmountAutoFocus: s,
            children: /* @__PURE__ */ S(
              Yi,
              {
                asChild: !0,
                disableOutsidePointerEvents: l,
                onEscapeKeyDown: c,
                onPointerDownOutside: u,
                onFocusOutside: d,
                onInteractOutside: f,
                onDismiss: h,
                children: /* @__PURE__ */ S(
                  Jx,
                  {
                    asChild: !0,
                    ...w,
                    dir: y.dir,
                    orientation: "vertical",
                    loop: r,
                    currentTabStopId: k,
                    onCurrentTabStopIdChange: E,
                    onEntryFocus: F(a, (P) => {
                      y.isUsingKeyboardRef.current || P.preventDefault();
                    }),
                    preventScrollOnEntryFocus: !0,
                    children: /* @__PURE__ */ S(
                      Ua,
                      {
                        role: "menu",
                        "aria-orientation": "vertical",
                        "data-state": Wp(g.open),
                        "data-radix-menu-content": "",
                        dir: y.dir,
                        ...b,
                        ...m,
                        ref: A,
                        style: { outline: "none", ...m.style },
                        onKeyDown: F(m.onKeyDown, (P) => {
                          const Ae = P.target.closest("[data-radix-menu-content]") === P.currentTarget, nt = P.ctrlKey || P.altKey || P.metaKey, sr = P.key.length === 1;
                          Ae && (P.key === "Tab" && P.preventDefault(), !nt && sr && Q(P.key));
                          const lr = C.current;
                          if (P.target !== lr || !Bk.includes(P.key)) return;
                          P.preventDefault();
                          const rn = v().filter((mt) => !mt.disabled).map((mt) => mt.ref.current);
                          vp.includes(P.key) && rn.reverse(), iE(rn);
                        }),
                        onBlur: F(n.onBlur, (P) => {
                          P.currentTarget.contains(P.target) || (window.clearTimeout(B.current), j.current = "");
                        }),
                        onPointerMove: F(
                          n.onPointerMove,
                          Ur((P) => {
                            const re = P.target, Ae = q.current !== P.clientX;
                            if (P.currentTarget.contains(re) && Ae) {
                              const nt = P.clientX > q.current ? "right" : "left";
                              X.current = nt, q.current = P.clientX;
                            }
                          })
                        )
                      }
                    )
                  }
                )
              }
            )
          }
        ) })
      }
    );
  }
);
Mp.displayName = Je;
var Yk = "MenuGroup", Qa = N(
  (n, e) => {
    const { __scopeMenu: t, ...r } = n;
    return /* @__PURE__ */ S(de.div, { role: "group", ...r, ref: e });
  }
);
Qa.displayName = Yk;
var Xk = "MenuLabel", Tp = N(
  (n, e) => {
    const { __scopeMenu: t, ...r } = n;
    return /* @__PURE__ */ S(de.div, { ...r, ref: e });
  }
);
Tp.displayName = Xk;
var Ni = "MenuItem", hd = "menu.itemSelect", ss = N(
  (n, e) => {
    const { disabled: t = !1, onSelect: r, ...o } = n, i = D(null), s = to(Ni, n.__scopeMenu), l = Ya(Ni, n.__scopeMenu), a = ge(e, i), c = D(!1), u = () => {
      const d = i.current;
      if (!t && d) {
        const f = new CustomEvent(hd, { bubbles: !0, cancelable: !0 });
        d.addEventListener(hd, (h) => r == null ? void 0 : r(h), { once: !0 }), Rh(d, f), f.defaultPrevented ? c.current = !1 : s.onClose();
      }
    };
    return /* @__PURE__ */ S(
      Ap,
      {
        ...o,
        ref: a,
        disabled: t,
        onClick: F(n.onClick, u),
        onPointerDown: (d) => {
          var f;
          (f = n.onPointerDown) == null || f.call(n, d), c.current = !0;
        },
        onPointerUp: F(n.onPointerUp, (d) => {
          var f;
          c.current || (f = d.currentTarget) == null || f.click();
        }),
        onKeyDown: F(n.onKeyDown, (d) => {
          const f = l.searchRef.current !== "";
          t || f && d.key === " " || jl.includes(d.key) && (d.currentTarget.click(), d.preventDefault());
        })
      }
    );
  }
);
ss.displayName = Ni;
var Ap = N(
  (n, e) => {
    const { __scopeMenu: t, disabled: r = !1, textValue: o, ...i } = n, s = Ya(Ni, t), l = Sp(t), a = D(null), c = ge(e, a), [u, d] = K(!1), [f, h] = K("");
    return z(() => {
      const p = a.current;
      p && h((p.textContent ?? "").trim());
    }, [i.children]), /* @__PURE__ */ S(
      jr.ItemSlot,
      {
        scope: t,
        disabled: r,
        textValue: o ?? f,
        children: /* @__PURE__ */ S(Yx, { asChild: !0, ...l, focusable: !r, children: /* @__PURE__ */ S(
          de.div,
          {
            role: "menuitem",
            "data-highlighted": u ? "" : void 0,
            "aria-disabled": r || void 0,
            "data-disabled": r ? "" : void 0,
            ...i,
            ref: c,
            onPointerMove: F(
              n.onPointerMove,
              Ur((p) => {
                r ? s.onItemLeave(p) : (s.onItemEnter(p), p.defaultPrevented || p.currentTarget.focus({ preventScroll: !0 }));
              })
            ),
            onPointerLeave: F(
              n.onPointerLeave,
              Ur((p) => s.onItemLeave(p))
            ),
            onFocus: F(n.onFocus, () => d(!0)),
            onBlur: F(n.onBlur, () => d(!1))
          }
        ) })
      }
    );
  }
), Qk = "MenuCheckboxItem", Op = N(
  (n, e) => {
    const { checked: t = !1, onCheckedChange: r, ...o } = n;
    return /* @__PURE__ */ S(Ip, { scope: n.__scopeMenu, checked: t, children: /* @__PURE__ */ S(
      ss,
      {
        role: "menuitemcheckbox",
        "aria-checked": Ri(t) ? "mixed" : t,
        ...o,
        ref: e,
        "data-state": ec(t),
        onSelect: F(
          o.onSelect,
          () => r == null ? void 0 : r(Ri(t) ? !0 : !t),
          { checkForDefaultPrevented: !1 }
        )
      }
    ) });
  }
);
Op.displayName = Qk;
var Np = "MenuRadioGroup", [Zk, eE] = Nn(
  Np,
  { value: void 0, onValueChange: () => {
  } }
), Rp = N(
  (n, e) => {
    const { value: t, onValueChange: r, ...o } = n, i = Mt(r);
    return /* @__PURE__ */ S(Zk, { scope: n.__scopeMenu, value: t, onValueChange: i, children: /* @__PURE__ */ S(Qa, { ...o, ref: e }) });
  }
);
Rp.displayName = Np;
var Dp = "MenuRadioItem", Pp = N(
  (n, e) => {
    const { value: t, ...r } = n, o = eE(Dp, n.__scopeMenu), i = t === o.value;
    return /* @__PURE__ */ S(Ip, { scope: n.__scopeMenu, checked: i, children: /* @__PURE__ */ S(
      ss,
      {
        role: "menuitemradio",
        "aria-checked": i,
        ...r,
        ref: e,
        "data-state": ec(i),
        onSelect: F(
          r.onSelect,
          () => {
            var s;
            return (s = o.onValueChange) == null ? void 0 : s.call(o, t);
          },
          { checkForDefaultPrevented: !1 }
        )
      }
    ) });
  }
);
Pp.displayName = Dp;
var Za = "MenuItemIndicator", [Ip, tE] = Nn(
  Za,
  { checked: !1 }
), Lp = N(
  (n, e) => {
    const { __scopeMenu: t, forceMount: r, ...o } = n, i = tE(Za, t);
    return /* @__PURE__ */ S(
      Nt,
      {
        present: r || Ri(i.checked) || i.checked === !0,
        children: /* @__PURE__ */ S(
          de.span,
          {
            ...o,
            ref: e,
            "data-state": ec(i.checked)
          }
        )
      }
    );
  }
);
Lp.displayName = Za;
var nE = "MenuSeparator", _p = N(
  (n, e) => {
    const { __scopeMenu: t, ...r } = n;
    return /* @__PURE__ */ S(
      de.div,
      {
        role: "separator",
        "aria-orientation": "horizontal",
        ...r,
        ref: e
      }
    );
  }
);
_p.displayName = nE;
var rE = "MenuArrow", Bp = N(
  (n, e) => {
    const { __scopeMenu: t, ...r } = n, o = is(t);
    return /* @__PURE__ */ S(Ka, { ...o, ...r, ref: e });
  }
);
Bp.displayName = rE;
var oE = "MenuSub", [JM, Fp] = Nn(oE), yr = "MenuSubTrigger", zp = N(
  (n, e) => {
    const t = Rn(yr, n.__scopeMenu), r = to(yr, n.__scopeMenu), o = Fp(yr, n.__scopeMenu), i = Ya(yr, n.__scopeMenu), s = D(null), { pointerGraceTimerRef: l, onPointerGraceIntentChange: a } = i, c = { __scopeMenu: n.__scopeMenu }, u = V(() => {
      s.current && window.clearTimeout(s.current), s.current = null;
    }, []);
    return z(() => u, [u]), z(() => {
      const d = l.current;
      return () => {
        window.clearTimeout(d), a(null);
      };
    }, [l, a]), /* @__PURE__ */ S(Ga, { asChild: !0, ...c, children: /* @__PURE__ */ S(
      Ap,
      {
        id: o.triggerId,
        "aria-haspopup": "menu",
        "aria-expanded": t.open,
        "aria-controls": o.contentId,
        "data-state": Wp(t.open),
        ...n,
        ref: Xr(e, o.onTriggerChange),
        onClick: (d) => {
          var f;
          (f = n.onClick) == null || f.call(n, d), !(n.disabled || d.defaultPrevented) && (d.currentTarget.focus(), t.open || t.onOpenChange(!0));
        },
        onPointerMove: F(
          n.onPointerMove,
          Ur((d) => {
            i.onItemEnter(d), !d.defaultPrevented && !n.disabled && !t.open && !s.current && (i.onPointerGraceIntentChange(null), s.current = window.setTimeout(() => {
              t.onOpenChange(!0), u();
            }, 100));
          })
        ),
        onPointerLeave: F(
          n.onPointerLeave,
          Ur((d) => {
            var h, p;
            u();
            const f = (h = t.content) == null ? void 0 : h.getBoundingClientRect();
            if (f) {
              const m = (p = t.content) == null ? void 0 : p.dataset.side, g = m === "right", y = g ? -5 : 5, b = f[g ? "left" : "right"], w = f[g ? "right" : "left"];
              i.onPointerGraceIntentChange({
                area: [
                  // Apply a bleed on clientX to ensure that our exit point is
                  // consistently within polygon bounds
                  { x: d.clientX + y, y: d.clientY },
                  { x: b, y: f.top },
                  { x: w, y: f.top },
                  { x: w, y: f.bottom },
                  { x: b, y: f.bottom }
                ],
                side: m
              }), window.clearTimeout(l.current), l.current = window.setTimeout(
                () => i.onPointerGraceIntentChange(null),
                300
              );
            } else {
              if (i.onTriggerLeave(d), d.defaultPrevented) return;
              i.onPointerGraceIntentChange(null);
            }
          })
        ),
        onKeyDown: F(n.onKeyDown, (d) => {
          var h;
          const f = i.searchRef.current !== "";
          n.disabled || f && d.key === " " || Fk[r.dir].includes(d.key) && (t.onOpenChange(!0), (h = t.content) == null || h.focus(), d.preventDefault());
        })
      }
    ) });
  }
);
zp.displayName = yr;
var $p = "MenuSubContent", Hp = N(
  (n, e) => {
    const t = kp(Je, n.__scopeMenu), { forceMount: r = t.forceMount, ...o } = n, i = Rn(Je, n.__scopeMenu), s = to(Je, n.__scopeMenu), l = Fp($p, n.__scopeMenu), a = D(null), c = ge(e, a);
    return /* @__PURE__ */ S(jr.Provider, { scope: n.__scopeMenu, children: /* @__PURE__ */ S(Nt, { present: r || i.open, children: /* @__PURE__ */ S(jr.Slot, { scope: n.__scopeMenu, children: /* @__PURE__ */ S(
      Xa,
      {
        id: l.contentId,
        "aria-labelledby": l.triggerId,
        ...o,
        ref: c,
        align: "start",
        side: s.dir === "rtl" ? "left" : "right",
        disableOutsidePointerEvents: !1,
        disableOutsideScroll: !1,
        trapFocus: !1,
        onOpenAutoFocus: (u) => {
          var d;
          s.isUsingKeyboardRef.current && ((d = a.current) == null || d.focus()), u.preventDefault();
        },
        onCloseAutoFocus: (u) => u.preventDefault(),
        onFocusOutside: F(n.onFocusOutside, (u) => {
          u.target !== l.trigger && i.onOpenChange(!1);
        }),
        onEscapeKeyDown: F(n.onEscapeKeyDown, (u) => {
          s.onClose(), u.preventDefault();
        }),
        onKeyDown: F(n.onKeyDown, (u) => {
          var h;
          const d = u.currentTarget.contains(u.target), f = zk[s.dir].includes(u.key);
          d && f && (i.onOpenChange(!1), (h = l.trigger) == null || h.focus(), u.preventDefault());
        })
      }
    ) }) }) });
  }
);
Hp.displayName = $p;
function Wp(n) {
  return n ? "open" : "closed";
}
function Ri(n) {
  return n === "indeterminate";
}
function ec(n) {
  return Ri(n) ? "indeterminate" : n ? "checked" : "unchecked";
}
function iE(n) {
  const e = document.activeElement;
  for (const t of n)
    if (t === e || (t.focus(), document.activeElement !== e)) return;
}
function sE(n, e) {
  return n.map((t, r) => n[(e + r) % n.length]);
}
function lE(n, e, t) {
  const o = e.length > 1 && Array.from(e).every((c) => c === e[0]) ? e[0] : e, i = t ? n.indexOf(t) : -1;
  let s = sE(n, Math.max(i, 0));
  o.length === 1 && (s = s.filter((c) => c !== t));
  const a = s.find(
    (c) => c.toLowerCase().startsWith(o.toLowerCase())
  );
  return a !== t ? a : void 0;
}
function aE(n, e) {
  const { x: t, y: r } = n;
  let o = !1;
  for (let i = 0, s = e.length - 1; i < e.length; s = i++) {
    const l = e[i], a = e[s], c = l.x, u = l.y, d = a.x, f = a.y;
    u > r != f > r && t < (d - c) * (r - u) / (f - u) + c && (o = !o);
  }
  return o;
}
function cE(n, e) {
  if (!e) return !1;
  const t = { x: n.clientX, y: n.clientY };
  return aE(t, e);
}
function Ur(n) {
  return (e) => e.pointerType === "mouse" ? n(e) : void 0;
}
var uE = xp, dE = Ga, fE = Ep, hE = Mp, pE = Qa, mE = Tp, gE = ss, yE = Op, bE = Rp, wE = Pp, vE = Lp, CE = _p, SE = Bp, xE = zp, kE = Hp, ls = "DropdownMenu", [EE] = On(
  ls,
  [Cp]
), Pe = Cp(), [ME, Vp] = EE(ls), jp = (n) => {
  const {
    __scopeDropdownMenu: e,
    children: t,
    dir: r,
    open: o,
    defaultOpen: i,
    onOpenChange: s,
    modal: l = !0
  } = n, a = Pe(e), c = D(null), [u, d] = Ji({
    prop: o,
    defaultProp: i ?? !1,
    onChange: s,
    caller: ls
  });
  return /* @__PURE__ */ S(
    ME,
    {
      scope: e,
      triggerId: Wr(),
      triggerRef: c,
      contentId: Wr(),
      open: u,
      onOpenChange: d,
      onOpenToggle: V(() => d((f) => !f), [d]),
      modal: l,
      children: /* @__PURE__ */ S(uE, { ...a, open: u, onOpenChange: d, dir: r, modal: l, children: t })
    }
  );
};
jp.displayName = ls;
var Up = "DropdownMenuTrigger", Kp = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, disabled: r = !1, ...o } = n, i = Vp(Up, t), s = Pe(t);
    return /* @__PURE__ */ S(dE, { asChild: !0, ...s, children: /* @__PURE__ */ S(
      de.button,
      {
        type: "button",
        id: i.triggerId,
        "aria-haspopup": "menu",
        "aria-expanded": i.open,
        "aria-controls": i.open ? i.contentId : void 0,
        "data-state": i.open ? "open" : "closed",
        "data-disabled": r ? "" : void 0,
        disabled: r,
        ...o,
        ref: Xr(e, i.triggerRef),
        onPointerDown: F(n.onPointerDown, (l) => {
          !r && l.button === 0 && l.ctrlKey === !1 && (i.onOpenToggle(), i.open || l.preventDefault());
        }),
        onKeyDown: F(n.onKeyDown, (l) => {
          r || (["Enter", " "].includes(l.key) && i.onOpenToggle(), l.key === "ArrowDown" && i.onOpenChange(!0), ["Enter", " ", "ArrowDown"].includes(l.key) && l.preventDefault());
        })
      }
    ) });
  }
);
Kp.displayName = Up;
var TE = "DropdownMenuPortal", qp = (n) => {
  const { __scopeDropdownMenu: e, ...t } = n, r = Pe(e);
  return /* @__PURE__ */ S(fE, { ...r, ...t });
};
qp.displayName = TE;
var Gp = "DropdownMenuContent", Jp = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, ...r } = n, o = Vp(Gp, t), i = Pe(t), s = D(!1);
    return /* @__PURE__ */ S(
      hE,
      {
        id: o.contentId,
        "aria-labelledby": o.triggerId,
        ...i,
        ...r,
        ref: e,
        onCloseAutoFocus: F(n.onCloseAutoFocus, (l) => {
          var a;
          s.current || (a = o.triggerRef.current) == null || a.focus(), s.current = !1, l.preventDefault();
        }),
        onInteractOutside: F(n.onInteractOutside, (l) => {
          const a = l.detail.originalEvent, c = a.button === 0 && a.ctrlKey === !0, u = a.button === 2 || c;
          (!o.modal || u) && (s.current = !0);
        }),
        style: {
          ...n.style,
          "--radix-dropdown-menu-content-transform-origin": "var(--radix-popper-transform-origin)",
          "--radix-dropdown-menu-content-available-width": "var(--radix-popper-available-width)",
          "--radix-dropdown-menu-content-available-height": "var(--radix-popper-available-height)",
          "--radix-dropdown-menu-trigger-width": "var(--radix-popper-anchor-width)",
          "--radix-dropdown-menu-trigger-height": "var(--radix-popper-anchor-height)"
        }
      }
    );
  }
);
Jp.displayName = Gp;
var AE = "DropdownMenuGroup", OE = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
    return /* @__PURE__ */ S(pE, { ...o, ...r, ref: e });
  }
);
OE.displayName = AE;
var NE = "DropdownMenuLabel", RE = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
    return /* @__PURE__ */ S(mE, { ...o, ...r, ref: e });
  }
);
RE.displayName = NE;
var DE = "DropdownMenuItem", Yp = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
    return /* @__PURE__ */ S(gE, { ...o, ...r, ref: e });
  }
);
Yp.displayName = DE;
var PE = "DropdownMenuCheckboxItem", IE = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(yE, { ...o, ...r, ref: e });
});
IE.displayName = PE;
var LE = "DropdownMenuRadioGroup", _E = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(bE, { ...o, ...r, ref: e });
});
_E.displayName = LE;
var BE = "DropdownMenuRadioItem", FE = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(wE, { ...o, ...r, ref: e });
});
FE.displayName = BE;
var zE = "DropdownMenuItemIndicator", $E = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(vE, { ...o, ...r, ref: e });
});
$E.displayName = zE;
var HE = "DropdownMenuSeparator", WE = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(CE, { ...o, ...r, ref: e });
});
WE.displayName = HE;
var VE = "DropdownMenuArrow", jE = N(
  (n, e) => {
    const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
    return /* @__PURE__ */ S(SE, { ...o, ...r, ref: e });
  }
);
jE.displayName = VE;
var UE = "DropdownMenuSubTrigger", KE = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(xE, { ...o, ...r, ref: e });
});
KE.displayName = UE;
var qE = "DropdownMenuSubContent", GE = N((n, e) => {
  const { __scopeDropdownMenu: t, ...r } = n, o = Pe(t);
  return /* @__PURE__ */ S(
    kE,
    {
      ...o,
      ...r,
      ref: e,
      style: {
        ...n.style,
        "--radix-dropdown-menu-content-transform-origin": "var(--radix-popper-transform-origin)",
        "--radix-dropdown-menu-content-available-width": "var(--radix-popper-available-width)",
        "--radix-dropdown-menu-content-available-height": "var(--radix-popper-available-height)",
        "--radix-dropdown-menu-trigger-width": "var(--radix-popper-anchor-width)",
        "--radix-dropdown-menu-trigger-height": "var(--radix-popper-anchor-height)"
      }
    }
  );
});
GE.displayName = qE;
var Xp = jp, Qp = Kp, Zp = qp, em = Jp, Ul = Yp, as = "Popover", [tm] = On(as, [
  ir
]), no = ir(), [JE, nn] = tm(as), nm = (n) => {
  const {
    __scopePopover: e,
    children: t,
    open: r,
    defaultOpen: o,
    onOpenChange: i,
    modal: s = !1
  } = n, l = no(e), a = D(null), [c, u] = K(!1), [d, f] = Ji({
    prop: r,
    defaultProp: o ?? !1,
    onChange: i,
    caller: as
  });
  return /* @__PURE__ */ S(ja, { ...l, children: /* @__PURE__ */ S(
    JE,
    {
      scope: e,
      contentId: Wr(),
      triggerRef: a,
      open: d,
      onOpenChange: f,
      onOpenToggle: V(() => f((h) => !h), [f]),
      hasCustomAnchor: c,
      onCustomAnchorAdd: V(() => u(!0), []),
      onCustomAnchorRemove: V(() => u(!1), []),
      modal: s,
      children: t
    }
  ) });
};
nm.displayName = as;
var rm = "PopoverAnchor", YE = N(
  (n, e) => {
    const { __scopePopover: t, ...r } = n, o = nn(rm, t), i = no(t), { onCustomAnchorAdd: s, onCustomAnchorRemove: l } = o;
    return z(() => (s(), () => l()), [s, l]), /* @__PURE__ */ S(ts, { ...i, ...r, ref: e });
  }
);
YE.displayName = rm;
var om = "PopoverTrigger", im = N(
  (n, e) => {
    const { __scopePopover: t, ...r } = n, o = nn(om, t), i = no(t), s = ge(e, o.triggerRef), l = /* @__PURE__ */ S(
      de.button,
      {
        type: "button",
        "aria-haspopup": "dialog",
        "aria-expanded": o.open,
        "aria-controls": o.contentId,
        "data-state": dm(o.open),
        ...r,
        ref: s,
        onClick: F(n.onClick, o.onOpenToggle)
      }
    );
    return o.hasCustomAnchor ? l : /* @__PURE__ */ S(ts, { asChild: !0, ...i, children: l });
  }
);
im.displayName = om;
var tc = "PopoverPortal", [XE, QE] = tm(tc, {
  forceMount: void 0
}), sm = (n) => {
  const { __scopePopover: e, forceMount: t, children: r, container: o } = n, i = nn(tc, e);
  return /* @__PURE__ */ S(XE, { scope: e, forceMount: t, children: /* @__PURE__ */ S(Nt, { present: t || i.open, children: /* @__PURE__ */ S(ns, { asChild: !0, container: o, children: r }) }) });
};
sm.displayName = tc;
var Qn = "PopoverContent", lm = N(
  (n, e) => {
    const t = QE(Qn, n.__scopePopover), { forceMount: r = t.forceMount, ...o } = n, i = nn(Qn, n.__scopePopover);
    return /* @__PURE__ */ S(Nt, { present: r || i.open, children: i.modal ? /* @__PURE__ */ S(eM, { ...o, ref: e }) : /* @__PURE__ */ S(tM, { ...o, ref: e }) });
  }
);
lm.displayName = Qn;
var ZE = /* @__PURE__ */ Hr("PopoverContent.RemoveScroll"), eM = N(
  (n, e) => {
    const t = nn(Qn, n.__scopePopover), r = D(null), o = ge(e, r), i = D(!1);
    return z(() => {
      const s = r.current;
      if (s) return fp(s);
    }, []), /* @__PURE__ */ S(qa, { as: ZE, allowPinchZoom: !0, children: /* @__PURE__ */ S(
      am,
      {
        ...n,
        ref: o,
        trapFocus: t.open,
        disableOutsidePointerEvents: !0,
        onCloseAutoFocus: F(n.onCloseAutoFocus, (s) => {
          var l;
          s.preventDefault(), i.current || (l = t.triggerRef.current) == null || l.focus();
        }),
        onPointerDownOutside: F(
          n.onPointerDownOutside,
          (s) => {
            const l = s.detail.originalEvent, a = l.button === 0 && l.ctrlKey === !0, c = l.button === 2 || a;
            i.current = c;
          },
          { checkForDefaultPrevented: !1 }
        ),
        onFocusOutside: F(
          n.onFocusOutside,
          (s) => s.preventDefault(),
          { checkForDefaultPrevented: !1 }
        )
      }
    ) });
  }
), tM = N(
  (n, e) => {
    const t = nn(Qn, n.__scopePopover), r = D(!1), o = D(!1);
    return /* @__PURE__ */ S(
      am,
      {
        ...n,
        ref: e,
        trapFocus: !1,
        disableOutsidePointerEvents: !1,
        onCloseAutoFocus: (i) => {
          var s, l;
          (s = n.onCloseAutoFocus) == null || s.call(n, i), i.defaultPrevented || (r.current || (l = t.triggerRef.current) == null || l.focus(), i.preventDefault()), r.current = !1, o.current = !1;
        },
        onInteractOutside: (i) => {
          var a, c;
          (a = n.onInteractOutside) == null || a.call(n, i), i.defaultPrevented || (r.current = !0, i.detail.originalEvent.type === "pointerdown" && (o.current = !0));
          const s = i.target;
          ((c = t.triggerRef.current) == null ? void 0 : c.contains(s)) && i.preventDefault(), i.detail.originalEvent.type === "focusin" && o.current && i.preventDefault();
        }
      }
    );
  }
), am = N(
  (n, e) => {
    const {
      __scopePopover: t,
      trapFocus: r,
      onOpenAutoFocus: o,
      onCloseAutoFocus: i,
      disableOutsidePointerEvents: s,
      onEscapeKeyDown: l,
      onPointerDownOutside: a,
      onFocusOutside: c,
      onInteractOutside: u,
      ...d
    } = n, f = nn(Qn, t), h = no(t);
    return _h(), /* @__PURE__ */ S(
      La,
      {
        asChild: !0,
        loop: !0,
        trapped: r,
        onMountAutoFocus: o,
        onUnmountAutoFocus: i,
        children: /* @__PURE__ */ S(
          Yi,
          {
            asChild: !0,
            disableOutsidePointerEvents: s,
            onInteractOutside: u,
            onEscapeKeyDown: l,
            onPointerDownOutside: a,
            onFocusOutside: c,
            onDismiss: () => f.onOpenChange(!1),
            children: /* @__PURE__ */ S(
              Ua,
              {
                "data-state": dm(f.open),
                role: "dialog",
                id: f.contentId,
                ...h,
                ...d,
                ref: e,
                style: {
                  ...d.style,
                  "--radix-popover-content-transform-origin": "var(--radix-popper-transform-origin)",
                  "--radix-popover-content-available-width": "var(--radix-popper-available-width)",
                  "--radix-popover-content-available-height": "var(--radix-popper-available-height)",
                  "--radix-popover-trigger-width": "var(--radix-popper-anchor-width)",
                  "--radix-popover-trigger-height": "var(--radix-popper-anchor-height)"
                }
              }
            )
          }
        )
      }
    );
  }
), cm = "PopoverClose", nM = N(
  (n, e) => {
    const { __scopePopover: t, ...r } = n, o = nn(cm, t);
    return /* @__PURE__ */ S(
      de.button,
      {
        type: "button",
        ...r,
        ref: e,
        onClick: F(n.onClick, () => o.onOpenChange(!1))
      }
    );
  }
);
nM.displayName = cm;
var rM = "PopoverArrow", um = N(
  (n, e) => {
    const { __scopePopover: t, ...r } = n, o = no(t);
    return /* @__PURE__ */ S(Ka, { ...o, ...r, ref: e });
  }
);
um.displayName = rM;
function dm(n) {
  return n ? "open" : "closed";
}
var oM = nm, iM = im, sM = sm, lM = lm, aM = um, cM = Object.freeze({
  // See: https://github.com/twbs/bootstrap/blob/main/scss/mixins/_visually-hidden.scss
  position: "absolute",
  border: 0,
  width: 1,
  height: 1,
  padding: 0,
  margin: -1,
  overflow: "hidden",
  clip: "rect(0, 0, 0, 0)",
  whiteSpace: "nowrap",
  wordWrap: "normal"
}), uM = "VisuallyHidden", fm = N(
  (n, e) => /* @__PURE__ */ S(
    de.span,
    {
      ...n,
      ref: e,
      style: { ...cM, ...n.style }
    }
  )
);
fm.displayName = uM;
var dM = fm, [cs] = On("Tooltip", [
  ir
]), us = ir(), hm = "TooltipProvider", fM = 700, Kl = "tooltip.open", [hM, nc] = cs(hm), pm = (n) => {
  const {
    __scopeTooltip: e,
    delayDuration: t = fM,
    skipDelayDuration: r = 300,
    disableHoverableContent: o = !1,
    children: i
  } = n, s = D(!0), l = D(!1), a = D(0);
  return z(() => {
    const c = a.current;
    return () => window.clearTimeout(c);
  }, []), /* @__PURE__ */ S(
    hM,
    {
      scope: e,
      isOpenDelayedRef: s,
      delayDuration: t,
      onOpen: V(() => {
        window.clearTimeout(a.current), s.current = !1;
      }, []),
      onClose: V(() => {
        window.clearTimeout(a.current), a.current = window.setTimeout(
          () => s.current = !0,
          r
        );
      }, [r]),
      isPointerInTransitRef: l,
      onPointerInTransitChange: V((c) => {
        l.current = c;
      }, []),
      disableHoverableContent: o,
      children: i
    }
  );
};
pm.displayName = hm;
var Kr = "Tooltip", [pM, ro] = cs(Kr), mm = (n) => {
  const {
    __scopeTooltip: e,
    children: t,
    open: r,
    defaultOpen: o,
    onOpenChange: i,
    disableHoverableContent: s,
    delayDuration: l
  } = n, a = nc(Kr, n.__scopeTooltip), c = us(e), [u, d] = K(null), f = Wr(), h = D(0), p = s ?? a.disableHoverableContent, m = l ?? a.delayDuration, g = D(!1), [y, b] = Ji({
    prop: r,
    defaultProp: o ?? !1,
    onChange: (C) => {
      C ? (a.onOpen(), document.dispatchEvent(new CustomEvent(Kl))) : a.onClose(), i == null || i(C);
    },
    caller: Kr
  }), w = Xe(() => y ? g.current ? "delayed-open" : "instant-open" : "closed", [y]), v = V(() => {
    window.clearTimeout(h.current), h.current = 0, g.current = !1, b(!0);
  }, [b]), k = V(() => {
    window.clearTimeout(h.current), h.current = 0, b(!1);
  }, [b]), E = V(() => {
    window.clearTimeout(h.current), h.current = window.setTimeout(() => {
      g.current = !0, b(!0), h.current = 0;
    }, m);
  }, [m, b]);
  return z(() => () => {
    h.current && (window.clearTimeout(h.current), h.current = 0);
  }, []), /* @__PURE__ */ S(ja, { ...c, children: /* @__PURE__ */ S(
    pM,
    {
      scope: e,
      contentId: f,
      open: y,
      stateAttribute: w,
      trigger: u,
      onTriggerChange: d,
      onTriggerEnter: V(() => {
        a.isOpenDelayedRef.current ? E() : v();
      }, [a.isOpenDelayedRef, E, v]),
      onTriggerLeave: V(() => {
        p ? k() : (window.clearTimeout(h.current), h.current = 0);
      }, [k, p]),
      onOpen: v,
      onClose: k,
      disableHoverableContent: p,
      children: t
    }
  ) });
};
mm.displayName = Kr;
var ql = "TooltipTrigger", gm = N(
  (n, e) => {
    const { __scopeTooltip: t, ...r } = n, o = ro(ql, t), i = nc(ql, t), s = us(t), l = D(null), a = ge(e, l, o.onTriggerChange), c = D(!1), u = D(!1), d = V(() => c.current = !1, []);
    return z(() => () => document.removeEventListener("pointerup", d), [d]), /* @__PURE__ */ S(ts, { asChild: !0, ...s, children: /* @__PURE__ */ S(
      de.button,
      {
        "aria-describedby": o.open ? o.contentId : void 0,
        "data-state": o.stateAttribute,
        ...r,
        ref: a,
        onPointerMove: F(n.onPointerMove, (f) => {
          f.pointerType !== "touch" && !u.current && !i.isPointerInTransitRef.current && (o.onTriggerEnter(), u.current = !0);
        }),
        onPointerLeave: F(n.onPointerLeave, () => {
          o.onTriggerLeave(), u.current = !1;
        }),
        onPointerDown: F(n.onPointerDown, () => {
          o.open && o.onClose(), c.current = !0, document.addEventListener("pointerup", d, { once: !0 });
        }),
        onFocus: F(n.onFocus, () => {
          c.current || o.onOpen();
        }),
        onBlur: F(n.onBlur, o.onClose),
        onClick: F(n.onClick, o.onClose)
      }
    ) });
  }
);
gm.displayName = ql;
var rc = "TooltipPortal", [mM, gM] = cs(rc, {
  forceMount: void 0
}), ym = (n) => {
  const { __scopeTooltip: e, forceMount: t, children: r, container: o } = n, i = ro(rc, e);
  return /* @__PURE__ */ S(mM, { scope: e, forceMount: t, children: /* @__PURE__ */ S(Nt, { present: t || i.open, children: /* @__PURE__ */ S(ns, { asChild: !0, container: o, children: r }) }) });
};
ym.displayName = rc;
var Zn = "TooltipContent", bm = N(
  (n, e) => {
    const t = gM(Zn, n.__scopeTooltip), { forceMount: r = t.forceMount, side: o = "top", ...i } = n, s = ro(Zn, n.__scopeTooltip);
    return /* @__PURE__ */ S(Nt, { present: r || s.open, children: s.disableHoverableContent ? /* @__PURE__ */ S(wm, { side: o, ...i, ref: e }) : /* @__PURE__ */ S(yM, { side: o, ...i, ref: e }) });
  }
), yM = N((n, e) => {
  const t = ro(Zn, n.__scopeTooltip), r = nc(Zn, n.__scopeTooltip), o = D(null), i = ge(e, o), [s, l] = K(null), { trigger: a, onClose: c } = t, u = o.current, { onPointerInTransitChange: d } = r, f = V(() => {
    l(null), d(!1);
  }, [d]), h = V(
    (p, m) => {
      const g = p.currentTarget, y = { x: p.clientX, y: p.clientY }, b = CM(y, g.getBoundingClientRect()), w = SM(y, b), v = xM(m.getBoundingClientRect()), k = EM([...w, ...v]);
      l(k), d(!0);
    },
    [d]
  );
  return z(() => () => f(), [f]), z(() => {
    if (a && u) {
      const p = (g) => h(g, u), m = (g) => h(g, a);
      return a.addEventListener("pointerleave", p), u.addEventListener("pointerleave", m), () => {
        a.removeEventListener("pointerleave", p), u.removeEventListener("pointerleave", m);
      };
    }
  }, [a, u, h, f]), z(() => {
    if (s) {
      const p = (m) => {
        const g = m.target, y = { x: m.clientX, y: m.clientY }, b = (a == null ? void 0 : a.contains(g)) || (u == null ? void 0 : u.contains(g)), w = !kM(y, s);
        b ? f() : w && (f(), c());
      };
      return document.addEventListener("pointermove", p), () => document.removeEventListener("pointermove", p);
    }
  }, [a, u, s, c, f]), /* @__PURE__ */ S(wm, { ...n, ref: i });
}), [bM, wM] = cs(Kr, { isInside: !1 }), vM = /* @__PURE__ */ ZS("TooltipContent"), wm = N(
  (n, e) => {
    const {
      __scopeTooltip: t,
      children: r,
      "aria-label": o,
      onEscapeKeyDown: i,
      onPointerDownOutside: s,
      ...l
    } = n, a = ro(Zn, t), c = us(t), { onClose: u } = a;
    return z(() => (document.addEventListener(Kl, u), () => document.removeEventListener(Kl, u)), [u]), z(() => {
      if (a.trigger) {
        const d = (f) => {
          const h = f.target;
          h != null && h.contains(a.trigger) && u();
        };
        return window.addEventListener("scroll", d, { capture: !0 }), () => window.removeEventListener("scroll", d, { capture: !0 });
      }
    }, [a.trigger, u]), /* @__PURE__ */ S(
      Yi,
      {
        asChild: !0,
        disableOutsidePointerEvents: !1,
        onEscapeKeyDown: i,
        onPointerDownOutside: s,
        onFocusOutside: (d) => d.preventDefault(),
        onDismiss: u,
        children: /* @__PURE__ */ KS(
          Ua,
          {
            "data-state": a.stateAttribute,
            ...c,
            ...l,
            ref: e,
            style: {
              ...l.style,
              "--radix-tooltip-content-transform-origin": "var(--radix-popper-transform-origin)",
              "--radix-tooltip-content-available-width": "var(--radix-popper-available-width)",
              "--radix-tooltip-content-available-height": "var(--radix-popper-available-height)",
              "--radix-tooltip-trigger-width": "var(--radix-popper-anchor-width)",
              "--radix-tooltip-trigger-height": "var(--radix-popper-anchor-height)"
            },
            children: [
              /* @__PURE__ */ S(vM, { children: r }),
              /* @__PURE__ */ S(bM, { scope: t, isInside: !0, children: /* @__PURE__ */ S(dM, { id: a.contentId, role: "tooltip", children: o || r }) })
            ]
          }
        )
      }
    );
  }
);
bm.displayName = Zn;
var vm = "TooltipArrow", Cm = N(
  (n, e) => {
    const { __scopeTooltip: t, ...r } = n, o = us(t);
    return wM(
      vm,
      t
    ).isInside ? null : /* @__PURE__ */ S(Ka, { ...o, ...r, ref: e });
  }
);
Cm.displayName = vm;
function CM(n, e) {
  const t = Math.abs(e.top - n.y), r = Math.abs(e.bottom - n.y), o = Math.abs(e.right - n.x), i = Math.abs(e.left - n.x);
  switch (Math.min(t, r, o, i)) {
    case i:
      return "left";
    case o:
      return "right";
    case t:
      return "top";
    case r:
      return "bottom";
    default:
      throw new Error("unreachable");
  }
}
function SM(n, e, t = 5) {
  const r = [];
  switch (e) {
    case "top":
      r.push(
        { x: n.x - t, y: n.y + t },
        { x: n.x + t, y: n.y + t }
      );
      break;
    case "bottom":
      r.push(
        { x: n.x - t, y: n.y - t },
        { x: n.x + t, y: n.y - t }
      );
      break;
    case "left":
      r.push(
        { x: n.x + t, y: n.y - t },
        { x: n.x + t, y: n.y + t }
      );
      break;
    case "right":
      r.push(
        { x: n.x - t, y: n.y - t },
        { x: n.x - t, y: n.y + t }
      );
      break;
  }
  return r;
}
function xM(n) {
  const { top: e, right: t, bottom: r, left: o } = n;
  return [
    { x: o, y: e },
    { x: t, y: e },
    { x: t, y: r },
    { x: o, y: r }
  ];
}
function kM(n, e) {
  const { x: t, y: r } = n;
  let o = !1;
  for (let i = 0, s = e.length - 1; i < e.length; s = i++) {
    const l = e[i], a = e[s], c = l.x, u = l.y, d = a.x, f = a.y;
    u > r != f > r && t < (d - c) * (r - u) / (f - u) + c && (o = !o);
  }
  return o;
}
function EM(n) {
  const e = n.slice();
  return e.sort((t, r) => t.x < r.x ? -1 : t.x > r.x ? 1 : t.y < r.y ? -1 : t.y > r.y ? 1 : 0), MM(e);
}
function MM(n) {
  if (n.length <= 1) return n.slice();
  const e = [];
  for (let r = 0; r < n.length; r++) {
    const o = n[r];
    for (; e.length >= 2; ) {
      const i = e[e.length - 1], s = e[e.length - 2];
      if ((i.x - s.x) * (o.y - s.y) >= (i.y - s.y) * (o.x - s.x)) e.pop();
      else break;
    }
    e.push(o);
  }
  e.pop();
  const t = [];
  for (let r = n.length - 1; r >= 0; r--) {
    const o = n[r];
    for (; t.length >= 2; ) {
      const i = t[t.length - 1], s = t[t.length - 2];
      if ((i.x - s.x) * (o.y - s.y) >= (i.y - s.y) * (o.x - s.x)) t.pop();
      else break;
    }
    t.push(o);
  }
  return t.pop(), e.length === 1 && t.length === 1 && e[0].x === t[0].x && e[0].y === t[0].y ? e : e.concat(t);
}
var TM = pm, ds = mm, fs = gm, hs = ym, ps = bm, ms = Cm, AM = Symbol.for("react.lazy"), Di = Ca[" use ".trim().toString()];
function OM(n) {
  return typeof n == "object" && n !== null && "then" in n;
}
function Sm(n) {
  return n != null && typeof n == "object" && "$$typeof" in n && n.$$typeof === AM && "_payload" in n && OM(n._payload);
}
// @__NO_SIDE_EFFECTS__
function NM(n) {
  const e = /* @__PURE__ */ RM(n), t = N((r, o) => {
    let { children: i, ...s } = r;
    Sm(i) && typeof Di == "function" && (i = Di(i._payload));
    const l = $e.toArray(i), a = l.find(PM);
    if (a) {
      const c = a.props.children, u = l.map((d) => d === a ? $e.count(c) > 1 ? $e.only(null) : Et(c) ? c.props.children : null : d);
      return /* @__PURE__ */ S(e, { ...s, ref: o, children: Et(c) ? An(c, void 0, u) : null });
    }
    return /* @__PURE__ */ S(e, { ...s, ref: o, children: i });
  });
  return t.displayName = `${n}.Slot`, t;
}
// @__NO_SIDE_EFFECTS__
function RM(n) {
  const e = N((t, r) => {
    let { children: o, ...i } = t;
    if (Sm(o) && typeof Di == "function" && (o = Di(o._payload)), Et(o)) {
      const s = LM(o), l = IM(i, o.props);
      return o.type !== nr && (l.ref = r ? Xr(r, s) : s), An(o, l);
    }
    return $e.count(o) > 1 ? $e.only(null) : null;
  });
  return e.displayName = `${n}.SlotClone`, e;
}
var DM = Symbol("radix.slottable");
function PM(n) {
  return Et(n) && typeof n.type == "function" && "__radixId" in n.type && n.type.__radixId === DM;
}
function IM(n, e) {
  const t = { ...e };
  for (const r in e) {
    const o = n[r], i = e[r];
    /^on[A-Z]/.test(r) ? o && i ? t[r] = (...l) => {
      const a = i(...l);
      return o(...l), a;
    } : o && (t[r] = o) : r === "style" ? t[r] = { ...o, ...i } : r === "className" && (t[r] = [o, i].filter(Boolean).join(" "));
  }
  return { ...n, ...t };
}
function LM(n) {
  var r, o;
  let e = (r = Object.getOwnPropertyDescriptor(n.props, "ref")) == null ? void 0 : r.get, t = e && "isReactWarning" in e && e.isReactWarning;
  return t ? n.ref : (e = (o = Object.getOwnPropertyDescriptor(n, "ref")) == null ? void 0 : o.get, t = e && "isReactWarning" in e && e.isReactWarning, t ? n.props.ref : n.props.ref || n.ref);
}
var _M = [
  "a",
  "button",
  "div",
  "form",
  "h2",
  "h3",
  "img",
  "input",
  "label",
  "li",
  "nav",
  "ol",
  "p",
  "select",
  "span",
  "svg",
  "ul"
], BM = _M.reduce((n, e) => {
  const t = /* @__PURE__ */ NM(`Primitive.${e}`), r = N((o, i) => {
    const { asChild: s, ...l } = o, a = s ? t : e;
    return typeof window < "u" && (window[Symbol.for("radix-ui")] = !0), /* @__PURE__ */ S(a, { ...l, ref: i });
  });
  return r.displayName = `Primitive.${e}`, { ...n, [e]: r };
}, {}), FM = "Separator", pd = "horizontal", zM = ["horizontal", "vertical"], xm = N((n, e) => {
  const { decorative: t, orientation: r = pd, ...o } = n, i = $M(r) ? r : pd, l = t ? { role: "none" } : { "aria-orientation": i === "vertical" ? i : void 0, role: "separator" };
  return /* @__PURE__ */ S(
    BM.div,
    {
      "data-orientation": i,
      ...l,
      ...o,
      ref: e
    }
  );
});
xm.displayName = FM;
function $M(n) {
  return zM.includes(n);
}
var Ao = xm;
const km = {
  "js.wysiwyg.bold": "Bold",
  "js.wysiwyg.italic": "Italic",
  "js.wysiwyg.underline": "Underline",
  "js.wysiwyg.strikethrough": "Strikethrough",
  "js.wysiwyg.heading": "Heading",
  "js.wysiwyg.paragraph": "Paragraph",
  "js.wysiwyg.heading1": "Heading 1",
  "js.wysiwyg.heading2": "Heading 2",
  "js.wysiwyg.heading3": "Heading 3",
  "js.wysiwyg.heading4": "Heading 4",
  "js.wysiwyg.heading5": "Heading 5",
  "js.wysiwyg.heading6": "Heading 6",
  "js.wysiwyg.bulletList": "Bullet List",
  "js.wysiwyg.orderedList": "Numbered List",
  "js.wysiwyg.lists": "Lists",
  "js.wysiwyg.blockquote": "Blockquote",
  "js.wysiwyg.link": "Link",
  "js.wysiwyg.linkUrl": "URL",
  "js.wysiwyg.linkApply": "Apply",
  "js.wysiwyg.linkRemove": "Remove link",
  "js.wysiwyg.image": "Image",
  "js.wysiwyg.table": "Table",
  "js.wysiwyg.codeBlock": "Code Block",
  "js.wysiwyg.undo": "Undo",
  "js.wysiwyg.redo": "Redo"
};
function J(n, e) {
  return n["js.wysiwyg." + e] || km["js.wysiwyg." + e] || e;
}
const rt = ({ icon: n, tooltip: e, active: t, disabled: r, onClick: o }) => /* @__PURE__ */ x.createElement(ds, { delayDuration: 400 }, /* @__PURE__ */ x.createElement(fs, { asChild: !0 }, /* @__PURE__ */ x.createElement(
  "button",
  {
    type: "button",
    className: "tlWysiwygToolbar__btn" + (t ? " tlWysiwygToolbar__btn--active" : ""),
    disabled: r,
    onMouseDown: (i) => {
      i.preventDefault(), o();
    },
    "aria-label": e
  },
  /* @__PURE__ */ x.createElement("i", { className: n })
)), /* @__PURE__ */ x.createElement(hs, null, /* @__PURE__ */ x.createElement(ps, { className: "tlWysiwygToolbar__tooltip", sideOffset: 6 }, e, /* @__PURE__ */ x.createElement(ms, { className: "tlWysiwygToolbar__tooltipArrow" })))), HM = ({ editor: n, labels: e }) => {
  const t = [
    { label: J(e, "paragraph"), level: null, icon: "ri-paragraph" },
    { label: J(e, "heading1"), level: 1, icon: "ri-h-1" },
    { label: J(e, "heading2"), level: 2, icon: "ri-h-2" },
    { label: J(e, "heading3"), level: 3, icon: "ri-h-3" },
    { label: J(e, "heading4"), level: 4, icon: "ri-h-4" },
    { label: J(e, "heading5"), level: 5, icon: "ri-h-5" },
    { label: J(e, "heading6"), level: 6, icon: "ri-h-6" }
  ];
  let r = "ri-paragraph", o = J(e, "paragraph");
  for (let i = 1; i <= 6; i++)
    if (n.isActive("heading", { level: i })) {
      r = "ri-h-" + i, o = J(e, "HEADING_" + i);
      break;
    }
  return /* @__PURE__ */ x.createElement(Xp, null, /* @__PURE__ */ x.createElement(ds, { delayDuration: 400 }, /* @__PURE__ */ x.createElement(fs, { asChild: !0 }, /* @__PURE__ */ x.createElement(Qp, { asChild: !0 }, /* @__PURE__ */ x.createElement("button", { type: "button", className: "tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown", "aria-label": J(e, "heading") }, /* @__PURE__ */ x.createElement("i", { className: r }), /* @__PURE__ */ x.createElement("i", { className: "ri-arrow-down-s-line tlWysiwygToolbar__chevron" })))), /* @__PURE__ */ x.createElement(hs, null, /* @__PURE__ */ x.createElement(ps, { className: "tlWysiwygToolbar__tooltip", sideOffset: 6 }, o, /* @__PURE__ */ x.createElement(ms, { className: "tlWysiwygToolbar__tooltipArrow" })))), /* @__PURE__ */ x.createElement(Zp, null, /* @__PURE__ */ x.createElement(em, { className: "tlWysiwygToolbar__dropdown", sideOffset: 4, align: "start" }, t.map((i) => {
    const s = i.level === null ? !n.isActive("heading") : n.isActive("heading", { level: i.level });
    return /* @__PURE__ */ x.createElement(
      Ul,
      {
        key: i.level ?? "p",
        className: "tlWysiwygToolbar__dropdownItem" + (s ? " tlWysiwygToolbar__dropdownItem--active" : ""),
        onSelect: () => {
          i.level === null ? n.chain().focus().setParagraph().run() : n.chain().focus().toggleHeading({ level: i.level }).run();
        }
      },
      /* @__PURE__ */ x.createElement("i", { className: i.icon + " tlWysiwygToolbar__dropdownIcon" }),
      /* @__PURE__ */ x.createElement("span", null, i.label)
    );
  }))));
}, WM = ({ editor: n, labels: e }) => {
  const t = n.isActive("bulletList"), r = n.isActive("orderedList"), o = r ? "ri-list-ordered" : "ri-list-unordered";
  return /* @__PURE__ */ x.createElement(Xp, null, /* @__PURE__ */ x.createElement(ds, { delayDuration: 400 }, /* @__PURE__ */ x.createElement(fs, { asChild: !0 }, /* @__PURE__ */ x.createElement(Qp, { asChild: !0 }, /* @__PURE__ */ x.createElement(
    "button",
    {
      type: "button",
      className: "tlWysiwygToolbar__btn tlWysiwygToolbar__btn--dropdown" + (t || r ? " tlWysiwygToolbar__btn--active" : ""),
      "aria-label": J(e, "lists")
    },
    /* @__PURE__ */ x.createElement("i", { className: o }),
    /* @__PURE__ */ x.createElement("i", { className: "ri-arrow-down-s-line tlWysiwygToolbar__chevron" })
  ))), /* @__PURE__ */ x.createElement(hs, null, /* @__PURE__ */ x.createElement(ps, { className: "tlWysiwygToolbar__tooltip", sideOffset: 6 }, J(e, "lists"), /* @__PURE__ */ x.createElement(ms, { className: "tlWysiwygToolbar__tooltipArrow" })))), /* @__PURE__ */ x.createElement(Zp, null, /* @__PURE__ */ x.createElement(em, { className: "tlWysiwygToolbar__dropdown", sideOffset: 4, align: "start" }, /* @__PURE__ */ x.createElement(
    Ul,
    {
      className: "tlWysiwygToolbar__dropdownItem" + (t ? " tlWysiwygToolbar__dropdownItem--active" : ""),
      onSelect: () => n.chain().focus().toggleBulletList().run()
    },
    /* @__PURE__ */ x.createElement("i", { className: "ri-list-unordered tlWysiwygToolbar__dropdownIcon" }),
    /* @__PURE__ */ x.createElement("span", null, J(e, "bulletList"))
  ), /* @__PURE__ */ x.createElement(
    Ul,
    {
      className: "tlWysiwygToolbar__dropdownItem" + (r ? " tlWysiwygToolbar__dropdownItem--active" : ""),
      onSelect: () => n.chain().focus().toggleOrderedList().run()
    },
    /* @__PURE__ */ x.createElement("i", { className: "ri-list-ordered tlWysiwygToolbar__dropdownIcon" }),
    /* @__PURE__ */ x.createElement("span", null, J(e, "orderedList"))
  ))));
}, VM = ({ editor: n, labels: e }) => {
  const [t, r] = x.useState(!1), [o, i] = x.useState(""), s = x.useRef(null), l = n.isActive("link"), a = x.useCallback((f) => {
    if (f) {
      const h = n.getAttributes("link");
      i(h.href || "");
    }
    r(f);
  }, [n]), c = x.useCallback(() => {
    o.trim() && n.chain().focus().setLink({ href: o.trim() }).run(), r(!1);
  }, [n, o]), u = x.useCallback(() => {
    n.chain().focus().unsetLink().run(), r(!1);
  }, [n]), d = x.useCallback((f) => {
    f.key === "Enter" && (f.preventDefault(), c());
  }, [c]);
  return /* @__PURE__ */ x.createElement(oM, { open: t, onOpenChange: a }, /* @__PURE__ */ x.createElement(ds, { delayDuration: 400 }, /* @__PURE__ */ x.createElement(fs, { asChild: !0 }, /* @__PURE__ */ x.createElement(iM, { asChild: !0 }, /* @__PURE__ */ x.createElement(
    "button",
    {
      type: "button",
      className: "tlWysiwygToolbar__btn" + (l ? " tlWysiwygToolbar__btn--active" : ""),
      "aria-label": J(e, "link")
    },
    /* @__PURE__ */ x.createElement("i", { className: "ri-link" })
  ))), /* @__PURE__ */ x.createElement(hs, null, /* @__PURE__ */ x.createElement(ps, { className: "tlWysiwygToolbar__tooltip", sideOffset: 6 }, J(e, "link"), /* @__PURE__ */ x.createElement(ms, { className: "tlWysiwygToolbar__tooltipArrow" })))), /* @__PURE__ */ x.createElement(sM, null, /* @__PURE__ */ x.createElement(lM, { className: "tlWysiwygToolbar__linkPopover", sideOffset: 6, align: "start" }, /* @__PURE__ */ x.createElement("div", { className: "tlWysiwygToolbar__linkForm" }, /* @__PURE__ */ x.createElement("label", { className: "tlWysiwygToolbar__linkLabel" }, J(e, "linkUrl")), /* @__PURE__ */ x.createElement(
    "input",
    {
      ref: s,
      type: "url",
      className: "tlWysiwygToolbar__linkInput",
      placeholder: "https://...",
      value: o,
      onChange: (f) => i(f.target.value),
      onKeyDown: d,
      autoFocus: !0
    }
  ), /* @__PURE__ */ x.createElement("div", { className: "tlWysiwygToolbar__linkActions" }, /* @__PURE__ */ x.createElement(
    "button",
    {
      type: "button",
      className: "tlWysiwygToolbar__linkApply",
      onClick: c,
      disabled: !o.trim()
    },
    J(e, "linkApply")
  ), l && /* @__PURE__ */ x.createElement(
    "button",
    {
      type: "button",
      className: "tlWysiwygToolbar__linkRemove",
      onClick: u
    },
    J(e, "linkRemove")
  ))), /* @__PURE__ */ x.createElement(aM, { className: "tlWysiwygToolbar__popoverArrow" }))));
}, jM = ({ editor: n, onImageUpload: e }) => {
  const t = Om(km);
  return n ? /* @__PURE__ */ x.createElement(TM, { delayDuration: 400 }, /* @__PURE__ */ x.createElement("div", { className: "tlWysiwygToolbar", role: "toolbar", "aria-label": "Editor toolbar" }, /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-bold",
      tooltip: J(t, "bold"),
      active: n.isActive("bold"),
      onClick: () => n.chain().focus().toggleBold().run()
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-italic",
      tooltip: J(t, "italic"),
      active: n.isActive("italic"),
      onClick: () => n.chain().focus().toggleItalic().run()
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-underline",
      tooltip: J(t, "underline"),
      active: n.isActive("underline"),
      onClick: () => n.chain().focus().toggleUnderline().run()
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-strikethrough",
      tooltip: J(t, "strikethrough"),
      active: n.isActive("strike"),
      onClick: () => n.chain().focus().toggleStrike().run()
    }
  ), /* @__PURE__ */ x.createElement(Ao, { className: "tlWysiwygToolbar__sep", orientation: "vertical", decorative: !0 }), /* @__PURE__ */ x.createElement(HM, { editor: n, labels: t }), /* @__PURE__ */ x.createElement(WM, { editor: n, labels: t }), /* @__PURE__ */ x.createElement(Ao, { className: "tlWysiwygToolbar__sep", orientation: "vertical", decorative: !0 }), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-double-quotes-l",
      tooltip: J(t, "blockquote"),
      active: n.isActive("blockquote"),
      onClick: () => n.chain().focus().toggleBlockquote().run()
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-code-s-slash-line",
      tooltip: J(t, "codeBlock"),
      active: n.isActive("codeBlock"),
      onClick: () => n.chain().focus().toggleCodeBlock().run()
    }
  ), /* @__PURE__ */ x.createElement(Ao, { className: "tlWysiwygToolbar__sep", orientation: "vertical", decorative: !0 }), /* @__PURE__ */ x.createElement(VM, { editor: n, labels: t }), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-image-line",
      tooltip: J(t, "image"),
      onClick: e
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-table-line",
      tooltip: J(t, "table"),
      onClick: () => n.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: !0 }).run()
    }
  ), /* @__PURE__ */ x.createElement(Ao, { className: "tlWysiwygToolbar__sep", orientation: "vertical", decorative: !0 }), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-arrow-go-back-line",
      tooltip: J(t, "undo"),
      disabled: !n.can().undo(),
      onClick: () => n.chain().focus().undo().run()
    }
  ), /* @__PURE__ */ x.createElement(
    rt,
    {
      icon: "ri-arrow-go-forward-line",
      tooltip: J(t, "redo"),
      disabled: !n.can().redo(),
      onClick: () => n.chain().focus().redo().run()
    }
  ))) : null;
}, UM = ({ controlId: n }) => {
  const e = Nm(), t = Rm(), r = Dm(), o = Pm(), i = e.value || "", s = e.editable !== !1, l = !!e.hasError, a = e.imageUrl || null, c = x.useRef(null), u = x.useRef(null), d = iv({
    extensions: [
      aC,
      cC,
      IC.configure({ openOnClick: !1 }),
      _C.configure({ allowBase64: !0, inline: !0 }),
      zS.configure({ resizable: !0 }),
      $S,
      HS,
      WS,
      jS,
      US
    ],
    content: i,
    editable: s,
    onUpdate: ({ editor: m }) => {
      c.current && clearTimeout(c.current), c.current = setTimeout(() => {
        t("valueChanged", { value: m.getHTML() });
      }, 300);
    },
    onBlur: ({ editor: m }) => {
      c.current && (clearTimeout(c.current), c.current = null), t("valueChanged", { value: m.getHTML() });
    }
  }, [s]);
  x.useEffect(() => {
    d && !d.isFocused && d.getHTML() !== i && d.commands.setContent(i, !1);
  }, [i, d]), x.useEffect(() => {
    if (d && a) {
      const m = o + "&key=" + encodeURIComponent(a);
      d.chain().focus().setImage({ src: m }).run();
    }
  }, [a, d, o]);
  const f = x.useCallback(() => {
    var m;
    (m = u.current) == null || m.click();
  }, []), h = x.useCallback((m) => {
    var y;
    const g = (y = m.target.files) == null ? void 0 : y[0];
    if (g) {
      const b = new FormData();
      b.append("file", g), r(b);
    }
    m.target.value = "";
  }, [r]);
  if (x.useEffect(() => () => {
    c.current && clearTimeout(c.current);
  }, []), !s)
    return /* @__PURE__ */ x.createElement("div", { className: "tlWysiwygEditor tlWysiwygEditor--immutable" }, /* @__PURE__ */ x.createElement(
      "div",
      {
        className: "tlWysiwygEditor__immutableContent",
        dangerouslySetInnerHTML: { __html: i }
      }
    ));
  const p = "tlWysiwygEditor" + (l ? " tlWysiwygEditor--error" : "");
  return /* @__PURE__ */ x.createElement("div", { className: p }, /* @__PURE__ */ x.createElement(jM, { editor: d, onImageUpload: f }), /* @__PURE__ */ x.createElement("div", { className: "tlWysiwygEditor__content" }, /* @__PURE__ */ x.createElement(Jw, { editor: d })), /* @__PURE__ */ x.createElement(
    "input",
    {
      ref: u,
      type: "file",
      accept: "image/*",
      style: { display: "none" },
      onChange: h
    }
  ));
};
Im("TLWysiwygEditor", UM);
