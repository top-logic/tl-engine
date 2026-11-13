// Attrappe für tools/lib/color.mjs, nur für test/derive.test.mjs: alles wie das Original, aber aufKontrast wirft einen
// Programmfehler (TypeError) — an der einen Aufrufstelle von derive.mjs, deren Zeile der Test in
// globalThis.tlAttrappeZeile setzt; an jeder anderen rechnet sie wie das Original. Der Test lenkt den Import von
// ./lib/color.mjs für derive.mjs?attrappe hierher um (registerHooks), derive.mjs selbst bleibt unverändert.
import { aufKontrast as echt } from '../../tools/lib/color.mjs';
export * from '../../tools/lib/color.mjs';

export function aufKontrast(...args) {
  const zeile = globalThis.tlAttrappeZeile;
  if (zeile !== undefined && new RegExp(`derive\\.mjs\\?attrappe:${zeile}:`).test(new Error().stack))
    throw new TypeError('Programmfehler in der Kontrastrechnung (Attrappe)');
  return echt(...args);
}
