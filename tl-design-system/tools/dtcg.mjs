// W3C Design Tokens (DTCG). Quelle für Figma und Kundenwerkzeuge. Aliase bleiben Aliase.
import { resolve } from './resolve.mjs';

const REF = /^\{([a-z0-9-]+)\}$/;

function primType(group, name, value) {
  if (group === 'color') return 'color';
  if (group === 'font') return 'fontFamily';
  if (group === 'motion') return name.startsWith('dur-') ? 'duration' : 'cubicBezier';
  if (group === 'effect') return name.startsWith('shadow-') ? 'shadow' : 'color';
  return /^\d+$/.test(value) ? 'number' : 'dimension';
}

function primGroupOf(P, name) {
  for (const [g, o] of Object.entries(P)) if (typeof o === 'object' && name in o) return g;
  throw new Error(`Primitive ${name} in keiner Gruppe`);
}

function alias(P, v) {
  const m = REF.exec(String(v));
  return m ? { alias: `{primitive.${primGroupOf(P, m[1])}.${m[1]}}`, name: m[1] } : null;
}

function semantic(P, o, typeOf) {
  const out = {};
  for (const [n, v] of Object.entries(o)) {
    if (n.startsWith('_')) continue;
    const a = alias(P, v);
    out[n] = { $type: typeOf(n, a?.name), $value: a ? a.alias : v };
  }
  return out;
}

// Kommas außerhalb von Klammern trennen — rgba(0,0,0,0.12) darf nicht in drei Teile zerfallen.
function splitTopLevel(str) {
  const out = [];
  let depth = 0;
  let cur = '';
  for (const ch of str) {
    if (ch === '(') depth++;
    if (ch === ')') depth--;
    if (ch === ',' && depth === 0) { out.push(cur); cur = ''; } else cur += ch;
  }
  out.push(cur);
  return out.map((s) => s.trim()).filter(Boolean);
}

// Ein $type — ein Parser. Jeder liest genau die Wertform, die primitives.json für diesen
// Typ verwendet, und wirft, statt eine unlesbare Form stillschweigend durchzureichen.

function parseCubicBezier(raw) {
  const s = raw.trim();
  if (s === 'linear') return [0, 0, 1, 1];
  const m = /^cubic-bezier\(([^)]+)\)$/.exec(s);
  if (!m) throw new Error(`cubicBezier nicht lesbar: ${raw}`);
  return m[1].split(',').map((n) => parseFloat(n.trim()));
}

function parseDuration(raw) {
  const m = /^([\d.]+)(ms|s)$/.exec(raw.trim());
  if (!m) throw new Error(`duration nicht lesbar: ${raw}`);
  return { value: parseFloat(m[1]), unit: m[2] };
}

function parseDimension(raw) {
  const m = /^(-?[\d.]+)(rem|px|em|%|vh|dvw|dvh)$/.exec(raw.trim());
  if (!m) throw new Error(`dimension nicht lesbar: ${raw}`);
  return { value: parseFloat(m[1]), unit: m[2] };
}

function parseFontFamily(raw) {
  return raw.split(',').map((s) => s.trim().replace(/^['"]|['"]$/g, ''));
}

function parseShadow(raw) {
  return splitTopLevel(raw).map((layer) => {
    const parts = layer.split(/\s+/);
    const color = parts.pop();
    const [offsetX, offsetY, blur, spread] = parts;
    if (!offsetX || !offsetY) throw new Error(`shadow nicht lesbar: ${raw}`);
    return { color, offsetX, offsetY, blur: blur ?? '0px', spread: spread ?? '0px' };
  });
}

// Die Wertform je $type für eine Primitive: {$value, $extensions?}. Aliase (Rollen) tragen
// dagegen nur die Referenz als String und werden hier nicht angefasst.
function primValue(type, raw) {
  switch (type) {
    case 'cubicBezier': return { $value: parseCubicBezier(raw) };
    case 'number': return { $value: Number(raw) };
    case 'duration': return { $value: parseDuration(raw) };
    case 'shadow': return { $value: parseShadow(raw) };
    case 'fontFamily': return { $value: parseFontFamily(raw) };
    case 'dimension': {
      const d = parseDimension(raw);
      if (d.unit === '%')
        return { $value: d, $extensions: { 'com.top-logic.note': 'percent, not a DTCG dimension' } };
      if (d.unit === 'vh' || d.unit === 'dvw' || d.unit === 'dvh')
        return { $value: d, $extensions: { 'com.top-logic.note': 'viewport unit, not a DTCG dimension' } };
      return { $value: d };
    }
    case 'color':
      if (/^#/.test(raw)) return { $value: raw };
      // scrim-*, veil-*: rgba in CSS-Notation, keine DTCG-Hex-Farbe — bewusst so exportiert.
      return { $value: raw, $extensions: { 'com.top-logic.note': 'alpha, CSS notation' } };
    default:
      return { $value: raw };
  }
}

export function dtcg(P, T) {
  const R = resolve(P, T);

  const primitive = {};
  for (const [g, o] of Object.entries(P)) {
    if (typeof o !== 'object') continue;
    primitive[g] = {};
    for (const [n, v] of Object.entries(o)) {
      const type = primType(g, n, v);
      primitive[g][n] = { $type: type, ...primValue(type, v) };
    }
  }

  const colorType = () => 'color';
  const measureType = (n) => n.startsWith('font-family') ? 'fontFamily' : n.startsWith('duration') ? 'duration' : n.startsWith('easing') ? 'cubicBezier' : 'dimension';
  const effectType = (n) => n.startsWith('shadow') ? 'shadow' : 'color';
  const mode = (m) => {
    const color = semantic(P, T.color[m], colorType);
    // Beschriftungen auf Flächen: nicht referenziert, sondern gerechnet (resolve). Fester
    // Hex-Wert plus die Fläche, aus der er stammt — kein Alias, keine Ausnahme, die die
    // Test-Assertion "Rollen tragen Aliase" stillschweigend bräche.
    for (const [name, { surface }] of Object.entries(T.derived[m]))
      color[name] = { $type: 'color', $value: R.derived[m][name], $extensions: { 'com.top-logic.derived-from': surface } };
    return {
      $extensions: { 'com.top-logic.mode': m },
      color,
      effect: semantic(P, T.effect[m], effectType),
    };
  };
  const measure = semantic(P, T.measure, measureType);
  const layer = semantic(P, T.layer, () => 'number');
  const density = {
    normal: semantic(P, T.density.normal, () => 'dimension'),
    compact: semantic(P, T.density.compact, () => 'dimension'),
  };

  // Typografie-Klassen: keine Aliase auf einzelne Primitive, sondern ein zusammengesetztes
  // $type: typography-Token je .tl-type-*-Klasse.
  const type = {};
  for (const [n, k] of Object.entries(T.type)) {
    if (n.startsWith('_')) continue;
    type[n] = {
      $type: 'typography',
      $value: {
        fontFamily: `{semantic.measure.font-family-${k.family}}`,
        fontSize: `{semantic.measure.font-size-${k.size}}`,
        fontWeight: k.weight,
        lineHeight: k.lineHeight,
        letterSpacing: k.tracking,
      },
    };
  }

  return {
    $description: 'TopLogic Design System. Quelle: design-system/src. Erzeugt, nicht von Hand ändern.',
    primitive,
    semantic: { light: mode('light'), dark: mode('dark'), measure, layer, density, type },
  };
}
