import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { dtcg } from '../tools/dtcg.mjs';

const P = JSON.parse(readFileSync(new URL('../src/primitives.json', import.meta.url)));
const T = JSON.parse(readFileSync(new URL('../src/tokens.json', import.meta.url)));
const D = dtcg(P, T);

function tokens(o, path = []) {
  const out = [];
  for (const [k, v] of Object.entries(o)) {
    if (k.startsWith('$')) continue;
    if (v && typeof v === 'object' && '$value' in v) out.push([[...path, k].join('.'), v]);
    else if (v && typeof v === 'object') out.push(...tokens(v, [...path, k]));
  }
  return out;
}

test('jedes Token hat $value und $type', () => {
  for (const [path, t] of tokens(D)) {
    assert.ok('$value' in t, path);
    assert.ok(typeof t.$type === 'string', `${path} ohne $type`);
  }
});

test('Primitive tragen Werte, Rollen tragen Aliase in geschweiften Klammern', () => {
  for (const [path, t] of tokens(D.primitive)) assert.ok(!String(t.$value).startsWith('{'), path);
  // Beschriftungen auf Flächen (text-on-*, icon-on-*) sind gerechnet, kein Alias — sie tragen
  // stattdessen $extensions['com.top-logic.derived-from'] und sind hier ausgenommen.
  const derivedNames = new Set(Object.keys(T.derived.light));
  for (const [path, t] of tokens(D.semantic.light.color)) {
    const name = path.split('.').pop();
    if (derivedNames.has(name)) {
      assert.match(String(t.$value), /^#[0-9A-F]{6}$/, path);
      assert.equal(t.$extensions?.['com.top-logic.derived-from'], T.derived.light[name].surface, path);
    } else {
      assert.match(String(t.$value), /^\{primitive\.color\.[a-z]+-\d+\}$/, path);
    }
  }
});

test('Wertformen je $type: cubicBezier, number, shadow, duration, dimension, fontFamily', () => {
  for (const [p, t] of tokens(D.primitive.motion)) {
    if (t.$type === 'cubicBezier') assert.equal(t.$value.length, 4, p);
    if (t.$type === 'duration') assert.ok(typeof t.$value === 'object' && 'value' in t.$value && 'unit' in t.$value, p);
  }
  for (const [p, t] of tokens(D.primitive.measure)) {
    if (t.$type === 'number') assert.equal(typeof t.$value, 'number', p);
    if (t.$type === 'dimension') assert.ok(typeof t.$value === 'object' && 'value' in t.$value && 'unit' in t.$value, p);
  }
  for (const [p, t] of tokens(D.primitive.effect)) {
    if (t.$type === 'shadow') {
      assert.ok(Array.isArray(t.$value), p);
      for (const layer of t.$value) assert.deepEqual(Object.keys(layer).sort(), ['blur', 'color', 'offsetX', 'offsetY', 'spread'], p);
    }
  }
  for (const [p, t] of tokens(D.primitive.font)) assert.ok(Array.isArray(t.$value), p);
  const percent = D.primitive.measure['corner-4'];
  assert.deepEqual(percent.$value, { value: 50, unit: '%' });
  assert.equal(percent.$extensions?.['com.top-logic.note'], 'percent, not a DTCG dimension');
  const viewport = D.primitive.measure['view-1'];
  assert.deepEqual(viewport.$value, { value: 70, unit: 'vh' });
  assert.equal(viewport.$extensions?.['com.top-logic.note'], 'viewport unit, not a DTCG dimension');
  assert.deepEqual(D.primitive.measure['view-2'].$value, { value: 100, unit: 'dvw' });
  assert.deepEqual(D.primitive.measure['view-3'].$value, { value: 100, unit: 'dvh' });
});

test('jeder Name aus T.derived.light steht im Export', () => {
  for (const name of Object.keys(T.derived.light)) assert.ok(name in D.semantic.light.color, name);
  for (const name of Object.keys(T.derived.dark)) assert.ok(name in D.semantic.dark.color, name);
});

test('jede Typo-Klasse steht im Export als $type typography', () => {
  for (const [n, k] of Object.entries(T.type)) {
    if (n.startsWith('_')) continue;
    const t = D.semantic.type[n];
    assert.ok(t, n);
    assert.equal(t.$type, 'typography', n);
    assert.equal(t.$value.fontWeight, k.weight, n);
    assert.equal(t.$value.lineHeight, k.lineHeight, n);
    assert.equal(t.$value.letterSpacing, k.tracking, n);
    assert.match(t.$value.fontFamily, /^\{semantic\.measure\.font-family-[a-z]+\}$/, n);
    assert.match(t.$value.fontSize, /^\{semantic\.measure\.font-size-[a-z-]+\}$/, n);
  }
});

test('jeder Alias zeigt auf ein vorhandenes Token', () => {
  const all = new Set(tokens(D).map(([p]) => p));
  for (const [path, t] of tokens(D)) {
    const m = /^\{(.+)\}$/.exec(String(t.$value));
    if (m) assert.ok(all.has(m[1]), `${path} → ${m[1]} fehlt`);
  }
});

test('Farben sind $type color, Maße dimension, Schriften fontFamily, Dauern duration, Kurven cubicBezier', () => {
  for (const [p, t] of tokens(D.primitive.color)) assert.equal(t.$type, 'color', p);
  for (const [p, t] of tokens(D.primitive.measure)) assert.ok(['dimension', 'number'].includes(t.$type), p);
  for (const [p, t] of tokens(D.primitive.font)) assert.equal(t.$type, 'fontFamily', p);
  for (const [p, t] of tokens(D.primitive.motion)) assert.ok(['duration', 'cubicBezier'].includes(t.$type), p);
});

test('beide Modi sind vollständig und die Blattnamen sind die Tokennamen', () => {
  const light = tokens(D.semantic.light).map(([p]) => p.replace('semantic.light.', '')).sort();
  const dark = tokens(D.semantic.dark).map(([p]) => p.replace('semantic.dark.', '')).sort();
  assert.deepEqual(dark, light);
  assert.ok(light.some((p) => p.endsWith('.surface-brand')));
});
