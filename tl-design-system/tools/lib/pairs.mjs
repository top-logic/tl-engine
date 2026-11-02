import { ueberlagert } from './contrast.mjs';

// Die Paarliste des Kontrast-Gates als Daten. tokens.test.mjs prüft das Paket damit, derive das
// Kundentheme — dieselben Paare, dieselben Schwellen (docs/skalen.md).
const GRUENDE_TEXT = ['surface-layer', 'surface-layer-nested', 'surface-overlay',
  'surface-interactive-hover', 'surface-interactive-strong', 'surface-interactive-active',
  'surface-selected', 'surface-selected-hover'];
// Die gewählte Fläche in ihren beiden Zuständen (UI-Durchgang 1): Zeile, Knoten und Eintrag, gewählt und gezeigt.
const AUSWAHL = ['surface-selected', 'surface-selected-hover'];
// Ein durchscheinender Hof liegt unter dem Ring, wenn eine fokussierte Marke gezeigt oder gedrückt wird. Er hat
// keine eigene Farbe: gemessen wird die Schicht über jedem Grund, auf dem eine Marke stehen kann, als
// Zusammensetzung `<Schicht>/<Grund>` (siehe werte).
const SCHICHTEN = ['surface-state-hover', 'surface-state-hover-strong', 'surface-state-active'];
const GRUENDE_HOF = ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay', 'surface-field'];
const GRUENDE_FOKUS = ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay',
  'surface-field', 'surface-brand-subtle', ...SCHICHTEN.flatMap((s) => GRUENDE_HOF.map((g) => `${s}/${g}`))];
// Wo ein Feld liegt: auf dem Grund, auf Panels, verschachtelt, im Dialog — und seine eigene Fläche,
// denn der Rand grenzt beides ab (WCAG 1.4.11, „angrenzende Farben“).
const GRUENDE_FELD = ['surface-field', 'surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay'];
const STATUS = ['error', 'warning', 'success', 'info'];
const KATEGORIEN = [1, 2, 3, 4, 5, 6, 7, 8];

export function kontrastPaare(T) {
  // Nennen zwei Schleifen dasselbe Paar, zählt es einmal, mit der höheren Schwelle — sonst meldete
  // derive denselben Befund doppelt. Die Reihenfolge der Schleifen spielt damit keine Rolle.
  const p = [];
  const index = new Map();
  const paar = (fg, bg, soll) => {
    const schluessel = `${fg}|${bg}`;
    if (index.has(schluessel)) {
      const vorhanden = index.get(schluessel);
      vorhanden.soll = Math.max(vorhanden.soll, soll);
      return;
    }
    const eintrag = { fg, bg, soll };
    index.set(schluessel, eintrag);
    p.push(eintrag);
  };
  paar('text-primary', 'surface-base', 16.0); paar('text-secondary', 'surface-base', 7.0);
  paar('text-helper', 'surface-base', 5.2); paar('text-placeholder', 'surface-field', 4.6);
  paar('text-link', 'surface-base', 4.5); paar('text-link-hover', 'surface-base', 4.5);
  paar('icon-primary', 'surface-base', 3.0); paar('icon-secondary', 'surface-base', 3.0);
  paar('border-emphasis', 'surface-base', 4.6);
  paar('border-interactive', 'surface-base', 3.0); paar('focus-ring', 'surface-base', 3.0);
  paar('text-link', 'surface-brand-subtle', 4.5); paar('text-primary', 'surface-brand-subtle', 4.5);
  for (const bg of GRUENDE_TEXT) paar('text-primary', bg, 4.5);
  for (const s of STATUS) {
    // Status-Text steht auch im Dialog: dieselben vier Flächen wie text-primary.
    for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay']) paar(`status-${s}-text`, bg, 4.5);
    for (const bg of ['surface-base', 'surface-layer']) paar(`status-${s}-border`, bg, 3.0);
    paar('text-primary', `status-${s}-subtle`, 4.5);
  }
  for (const bg of GRUENDE_FELD) {
    paar('border-control', bg, 3.0);
    paar('status-error-border', bg, 3.0);
    paar('status-warning-border', bg, 3.0);
  }
  // Hilfe- und Nebentext stehen auch im Panel und im Dialog.
  for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay']) {
    paar('text-helper', bg, 4.5); paar('text-secondary', bg, 4.5);
  }
  paar('text-secondary', 'surface-overlay', 4.5); // Popup-Status des Select
  // Hervorhebung im Select-Popup und Link im Status
  paar('border-interactive', 'surface-layer', 3.0); // Griff des Splits auf einer Fläche (Welle 4c)
  paar('border-interactive', 'surface-layer-nested', 3.0); // Ring einer ausgewählten Option in einer Fläche (Q24)
  paar('border-interactive', 'surface-overlay', 3.0); paar('border-interactive', 'surface-selected', 3.0); paar('text-link', 'surface-overlay', 4.5); // Linie des Zelleditors auf der gewählten Zeile
  paar('text-primary', 'surface-field', 4.5); paar('icon-secondary', 'surface-field', 3.0); // Schalterknopf
  // Griff des Mischers auf jeder Datenfarbe: 9:1 zwischen den Ringen sichert auf jeder Farbe einen Ring mit
  // 3:1 (Spec 5a §4).
  paar('border-handle-light', 'border-handle-dark', 9.0);
  // Auswahlzelle von Palette und Icon-Raster: Text und Icon auf der gewählten Fläche (Welle 5a).
  paar('text-primary', 'surface-interactive-strong', 4.5); paar('icon-primary', 'surface-interactive-strong', 3.0);
  // Knöpfe im Feld: der Pfeil des Select steht beim Zeigen und Drücken auf der Fläche seines Quadrats.
  paar('icon-secondary', 'surface-interactive-strong', 3.0); paar('icon-secondary', 'surface-interactive-active', 3.0);
  paar('border-interactive', 'surface-brand-subtle', 3.0); // gewählter Chip, gewähltes Segment
  // Daten (3b §13): ausgewählte Zeile und Knoten, Cursor-Ring, Marken im Kopf, Kante fixierter Spalten,
  // Fehler- und Warnlinie im Zelleditor und Ablehnung beim Ziehen auf einer ausgewählten oder gezeigten
  // Zeile (Stufen seit 07.10.: hell red-41/amber-43, dunkel red-34/amber-36), Platzhalter-Ton im Text.
  for (const bg of [...AUSWAHL, 'surface-interactive-hover', 'surface-interactive-strong']) paar('focus-ring', bg, 3.0);
  // Auswahl (UI-Durchgang 1): Icons und die Kante `border-selected` auf beiden gewählten Flächen; die Kante ist nur
  // Vordergrund. Kopfzeile, Zebra und Gruppenzeile der Tabelle tragen Text auf surface-layer-muted.
  for (const bg of AUSWAHL) {
    paar('icon-primary', bg, 3.0); paar('icon-secondary', bg, 3.0); paar('border-selected', bg, 3.0);
    paar('border-interactive', bg, 3.0); // Linie des Zelleditors auf der gewählten Zeile, auch gezeigt
    // Nebentext, Hilfetext und Link in einer gewählten Zeile (09.10.): 4,5 auf beiden Zuständen.
    paar('text-secondary', bg, 4.5); paar('text-link', bg, 4.5); paar('text-helper', bg, 4.5);
  }
  // Der Ring `border-selected` steht auch außen: um eine gewählte Karte (tl-item) und einen Termin-Chip, auf dem Grund,
  // auf Panels und im Dialog.
  for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested', 'surface-overlay', 'surface-interactive-hover']) paar('border-selected', bg, 3.0);
  paar('text-primary', 'surface-layer-muted', 4.5); paar('text-secondary', 'surface-layer-muted', 4.5);
  // Die Icons der Kopfzeile (Trichter, Sortierpfeil, Zahnrad) stehen auf surface-layer-muted.
  paar('icon-primary', 'surface-layer-muted', 3.0); paar('icon-secondary', 'surface-layer-muted', 3.0);
  paar('text-link', 'surface-layer-muted', 3.0); // der Trichter einer gefilterten Spalte ist Markenblau (#29999)
  // icon-secondary auf surface-interactive-strong steht seit Bernhards Knöpfen im Feld schon in der Liste (b985047).
  paar('icon-secondary', 'surface-layer', 3.0);
  paar('border-emphasis', 'surface-layer', 3.0);
  for (const bg of [...AUSWAHL, 'surface-interactive-hover']) {
    paar('status-error-border', bg, 3.0);
    paar('status-warning-border', bg, 3.0);
  }
  for (const bg of ['surface-base', 'surface-layer', 'surface-layer-nested']) paar('text-placeholder', bg, 4.5);
  // Der Auf-/Zuklapper zeigt mit dem Schleier (07.10.): sein Glyph über der Schicht auf jedem Grund, auf dem er
  // steht — Formulargruppe, Zeile gezeigt und ausgewählt. Kein Ring auf diesen Zusammensetzungen: in Zeile und
  // Knoten hat der Toggle tabindex=-1; der fokussierbare der Formulargruppe steht auf den Hof-Gründen oben.
  for (const s of SCHICHTEN) for (const g of ['surface-layer', 'surface-layer-nested', 'surface-interactive-hover', ...AUSWAHL])
    paar('icon-primary', `${s}/${g}`, 3.0);
  for (const bg of GRUENDE_FOKUS) paar('focus-ring', bg, 3.0);
  for (const k of KATEGORIEN) {
    paar('text-primary', `category-${k}-subtle`, 4.5);
    for (const bg of ['surface-base', 'surface-layer']) paar(`category-${k}-border`, bg, 3.0);
    // Diagrammmarke (Spec Diagrammfarben §2): nicht-textliche Grafik, 3:1 auf Grund und Layer.
    for (const bg of ['surface-base', 'surface-layer']) paar(`category-${k}-mark`, bg, 3.0);
  }
  // Tooltip: Text auf der inversen Fläche (Welle 5d).
  paar('text-inverse', 'surface-inverse', 4.5);
  for (const [name, { surface }] of Object.entries(T.derived.light))
    paar(name, surface, name.startsWith('icon-') ? 3.0 : 4.5);
  return p;
}

/**
 * Hex einer Rolle aus einem aufgelösten Ergebnis, Farbe oder Beschriftung — oder einer Zusammensetzung
 * `<Schicht>/<Grund>`: die durchscheinende Effekt-Rolle über der deckenden Fläche, so wie sie gezeichnet wird.
 */
export function werte(R, mode, name) {
  const teile = name.split('/');
  if (teile.length === 2) {
    const [schicht, grund] = teile;
    if (!(schicht in R.effect[mode])) throw new Error(`${mode}: Schicht ${schicht} unbekannt`);
    return ueberlagert(R.effect[mode][schicht], werte(R, mode, grund));
  }
  if (name in R.color[mode]) return R.color[mode][name];
  if (name in R.derived[mode]) return R.derived[mode][name];
  throw new Error(`${mode}: Rolle ${name} unbekannt`);
}
