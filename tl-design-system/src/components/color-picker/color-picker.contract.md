# Vertrag: Farb-Picker

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLColorInput` registriert ist (Stufe C der Spec), zeigt eine Farbe
als Feldknopf und öffnet ein Popup mit Palette und Mischer. TopLogic garantiert Funktion, Tastatur und Fokus, nicht das
Aussehen. Feldknopf, Zelle und Vorschau sind `tl-swatch` (`swatch.contract.md`), die Hülle des Popups ist `tl-picker`
(`picker.contract.md`).

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `value` | string \| null | Die Farbe als `#rrggbb`; `null` ist „keine Farbe“. |
| `palette` | Liste von string \| null | Die Zellen der Palette; `null` ist eine leere Zelle (nur Ablageziel). |
| `paletteColumns` | number | Spalten der Palette. |
| `editable`, `disabled` | boolean | Lesend ist der Swatch ein `span` ohne Knopf, deaktiviert ein `button` mit `disabled`; beide öffnen kein Popup. |
| `hasError`, `hasWarnings`, `mandatory` | boolean | Feldzustand wie bei jedem Feld (`aria-invalid`, `data-tl-state~="warning"`, `aria-required`). |

## Aktionen

| Befehl | Wann |
|---|---|
| `valueChanged` | OK im Popup, Enter auf einer Palettenzelle, Leeren: sendet die neue Farbe oder `null`. |
| `paletteChanged` | Ziehen und Tauschen in der Palette; OK trägt die Farbe in die erste freie Zelle ein. |

Alles im Popup ändert nur den Entwurf „Neu“; Abbrechen und Escape verwerfen ihn.

## Attribute

| Attribut | Wo |
|---|---|
| `role="listbox"` mit `aria-label` | `tl-color-picker__palette` |
| `role="option"` mit `aria-selected` | gefüllte Zellen der Palette; genau eine trägt `tabindex="0"` (`useRovingFocus`: Pfeile, Pos1, Ende; Leertaste übernimmt in „Neu“, Enter bestätigt) |
| `role="slider"` mit `aria-label` und `aria-valuetext` | `tl-color-picker__field`: links/rechts Sättigung, hoch/runter Helligkeit, je 1 %, Bild auf/ab 10 % |
| `role="slider"` mit `aria-orientation="vertical"` | `tl-color-picker__hue`: `aria-valuenow` 0–360, Pfeile 1°, Bild auf/ab 10°, Pos1/Ende; der Farbton bleibt über Schwarz und Grau hinweg erhalten |

## Klassen und Tokens

| Klasse | Teil |
|---|---|
| `tl-color-picker__palette` | Raster der Palette, Spalten inline |
| `tl-color-picker__mixer` | Mischfeld und Farbtonregler, `flex: none`: die Stapelzeile rechnet seine Breite ein |
| `tl-color-picker__field` | Sättigung/Helligkeit, festes Quadrat `--tl-size-12` (Breite und Höhe), gibt nicht nach; unter einer niedrigen Kappe scrollt das Reiterfeld des Popups |
| `tl-color-picker__hue` | Farbton, `--tl-size-control-sm` breit, `--tl-size-12` hoch |
| `tl-color-picker__handle` | Doppelring, außen `--tl-border-handle-dark` (Rand), innen `--tl-border-handle-light` (inset-Linie) |
| `tl-color-picker__preview` | Aktuell und Neu, je ein breiter Swatch |
| `tl-color-picker__inputs` | Rot, Grün, Blau, Hex |

Die gewählte Palettenoption trägt einen Rand in `--tl-border-interactive`. Datenfarben (Wert, Farbton, Verläufe,
Spektrum) setzt das Control inline; das CSS des Pakets enthält keine Farbe.

## Konformität

Ein Farb-Picker besteht den Vertrag, wenn Palette und Mischer die Rollen oben tragen, genau eine Palettenzelle einen
Tab-Halt hat, der Griff auf jeder Farbe sichtbar ist und Zustände Attribute sind.
