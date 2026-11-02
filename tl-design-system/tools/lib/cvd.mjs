/**
 * Farbsehschwäche simulieren und Farbabstand messen.
 *
 * Simuliert wird Rot-Grün-Schwäche nach Machado 2009 (Schwere 1,0) auf linearem sRGB. Der Weg
 * geht von der simulierten Farbe direkt nach OKLab, nie zurück über Hex: die 8-Bit-Rundung
 * verschöbe ΔE sonst messbar (protan 4,60 statt 4,67).
 */

import { hexZuLinearRgb, linearRgbZuOklab } from './color.mjs';

const MATRIZEN = {
  protan: [[0.152286, 1.052583, -0.204868], [0.114503, 0.786281, 0.099216], [-0.003882, -0.048116, 1.051998]],
  deutan: [[0.367322, 0.860646, -0.227968], [0.280085, 0.672501, 0.047413], [-0.011820, 0.042940, 0.968881]],
};

const klemme = (x) => Math.max(0, Math.min(1, x));

/** Hexwert so, wie ihn Protanopie oder Deuteranopie sieht: lineares RGB, geklemmt auf 0…1. */
export function simuliere(hex, art) {
  const m = MATRIZEN[art];
  if (!m) throw new Error(`Art der Farbsehschwäche unbekannt: ${art}`);
  const lin = hexZuLinearRgb(hex);
  return m.map((zeile) => klemme(zeile[0] * lin[0] + zeile[1] * lin[1] + zeile[2] * lin[2]));
}

/** ΔE: euklidischer Abstand in OKLab × 100; ohne `art` bei normalem Sehen. */
export function deltaE(a, b, art) {
  const lab = (hex) => linearRgbZuOklab(art ? simuliere(hex, art) : hexZuLinearRgb(hex));
  const x = lab(a), y = lab(b);
  return 100 * Math.hypot(x.L - y.L, x.a - y.a, x.b - y.b);
}

/** Abstand für Rot-Grün-Schwäche: der kleinere aus protan und deutan. */
export function rotGruen(a, b) {
  return Math.min(deltaE(a, b, 'protan'), deltaE(a, b, 'deutan'));
}
