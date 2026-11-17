# Vertrag: Skeleton

Ein Platzhalter für Inhalt, der noch lädt: eine vertiefte Fläche mit langsamem Puls. TopLogic garantiert
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Der Platzhalter ist der ersetzbare Part `Skeleton` (`SkeletonPart`); eine Kunden-Komponentenbibliothek zeichnet
ihn mit ihrem eigenen Platzhalter. Er steht dort, wo der Client während des Ladens die Form des Inhalts kennt:
die Tabelle zeichnet Zeilen, die der Server noch nicht geschickt hat, als Textzeile in `tl-table__placeholder`
(3b §10), das Raster trägt `aria-busy`; ein Bild mit bekannten Maßen trägt bis zum Laden ein Rechteck, das
seinen Rahmen `tl-media` füllt, der Rahmen trägt `aria-busy`.

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `shape` | `text` \| `rect` \| `circle` | Die Form. Fehlt sie, ist der Platzhalter ein Rechteck. |
| `lines` | Zahl | Nur bei `text`: so viele Zeilen untereinander; fehlt sie, eine. |
| Maße | vom Ort | Breite und Höhe setzt der Ort, an dem der Platzhalter steht (Block, Kreis, Breite der Zeile); das Stylesheet kennt nur Mindesthöhe, Radius und die Höhe einer Textzeile. |

## Aktionen

Keine.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-interactive-strong` (die vertiefte Fläche der Progress-Spur) |
| Form | `--tl-radius-sm`, Mindesthöhe `--tl-size-icon-sm`; Kreis `border-radius: 50%` |
| Textzeile | Höhe der Zeilenbox des Fließtexts (`--tl-font-size-body` mal `--tl-font-line-height-body`), Zeilenabstand `--tl-space-2xs`, die letzte von mehreren Zeilen halb so breit |
| Puls | `--tl-duration-loop`, `--tl-easing-standard`, endlos, `alternate` zwischen Deckkraft 1 und 0,5 |

Auf gehobenen Flächen (`surface-overlay`, dunkel) ist der Platzhalter kaum sichtbar, weil `surface-interactive-strong` für Panels abgestimmt ist; eigene Rollen für gehobene Flächen kommen mit Welle 3 (Menü).

Ohne Bewegung (Reduced-Motion) beendet der globale Schalter aus `base.css` die Animation nach einem Durchlauf;
die Ruhelage ist Deckkraft 1, weil kein Fill-Mode gesetzt ist.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-skeleton` | immer; allein ist er ein Rechteck |
| `tl-skeleton--text` | `shape="text"`: eine Zeile des Fließtexts; bei `lines` größer eins steht je Zeile ein `tl-skeleton tl-skeleton--text` in einem gemeinsamen Behälter, der sonst nichts enthält |
| `tl-skeleton--circle` | `shape="circle"`: ein Kreis |
| `aria-hidden="true"` | am Platzhalter selbst, bei mehreren Zeilen an ihrem Behälter: er sagt nichts |
| `aria-busy="true"` | am Behälter, der lädt: Hilfstechnik wartet auf den Inhalt |

## Konformität

Ein Skeleton besteht den Vertrag, wenn der Platzhalter `aria-hidden="true"` trägt (bei mehreren Zeilen ihr
gemeinsamer Behälter), der Behälter des ladenden Inhalts `aria-busy="true"`, die Maße nicht im Stylesheet
stehen und er im Klassen-Gate keine Klasse ohne `tl-`-Präfix und keinen Modifier außer `text` und `circle`
hinterlässt.
