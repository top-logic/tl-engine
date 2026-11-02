# Vertrag: Skeleton

Ein Platzhalter für Inhalt, der noch lädt: eine vertiefte Fläche mit langsamem Puls. TopLogic garantiert
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Erster Aufrufer: die Tabelle zeichnet Zeilen, die der Server noch nicht geschickt hat, als `tl-skeleton` in `tl-table__placeholder` (3b §10); das Raster trägt `aria-busy`.

Was ein Skeleton bekäme:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| Maße | vom Ort | Breite und Höhe setzt der Ort, an dem der Platzhalter steht (Zeile, Block, Kreis); das Stylesheet kennt nur Mindesthöhe und Radius. |

## Aktionen

Keine.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-interactive-selected` (die vertiefte Fläche der Progress-Spur) |
| Form | `--tl-radius-sm`, Mindesthöhe `--tl-size-icon-sm` |
| Puls | `--tl-duration-loop`, `--tl-easing-standard`, endlos, `alternate` zwischen Deckkraft 1 und 0,5 |

Auf gehobenen Flächen (`surface-overlay`, dunkel) ist der Platzhalter kaum sichtbar, weil `surface-interactive-selected` für Panels abgestimmt ist; eigene Rollen für gehobene Flächen kommen mit Welle 3 (Menü).

Ohne Bewegung (Reduced-Motion) beendet der globale Schalter aus `base.css` die Animation nach einem Durchlauf;
die Ruhelage ist Deckkraft 1, weil kein Fill-Mode gesetzt ist.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-skeleton` | immer; es gibt keine Modifier |
| `aria-hidden="true"` | am Platzhalter selbst: er sagt nichts |
| `aria-busy="true"` | am Behälter, der lädt: Hilfstechnik wartet auf den Inhalt |

## Konformität

Ein Skeleton besteht den Vertrag, wenn der Platzhalter `aria-hidden="true"` trägt, sein Behälter
`aria-busy="true"`, die Maße nicht im Stylesheet stehen und im Klassen-Gate keine Klasse ohne `tl-`-Präfix
hinterlässt.
