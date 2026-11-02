// Deklarationen aus CSS lesen: Selektor, Eigenschaft, Wert, Zeile. Kein vollständiger Parser —
// genug für Stylesheets, die wir selbst schreiben. Kommentare werden vorher entfernt.
export function declarations(css) {
  const out = [];
  const clean = css.replace(/\/\*[\s\S]*?\*\//g, (m) => m.replace(/[^\n]/g, ' '));
  const re = /([^{}]+)\{([^{}]*)\}/g;
  let m;
  while ((m = re.exec(clean))) {
    const selector = m[1].trim().replace(/\s+/g, ' ');
    const line = clean.slice(0, m.index).split('\n').length;
    const before = out.length;
    for (const d of m[2].split(';')) {
      const i = d.indexOf(':');
      if (i < 0) continue;
      out.push({ selector, property: d.slice(0, i).trim(), value: d.slice(i + 1).trim(), line });
    }
    // Regel ohne Deklarationen (z. B. leerer Body): Selektor trotzdem sichtbar machen,
    // sonst prüft niemand seine Klassennamen (BEM-Check hängt sich an declarations()).
    if (out.length === before) out.push({ selector, property: '', value: '', line });
  }
  return out;
}
