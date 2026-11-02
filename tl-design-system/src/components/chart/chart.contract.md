# Vertrag: Chart

Das Diagramm: Balken, Linien, Torten und ihre Verwandten, von Chart.js auf ein Canvas gezeichnet, der Inhalt vom Server
(Spec 5d §8). Das Paket zeichnet nicht das Canvas, sondern was im DOM darum liegt — Wurzel, Tooltip, Zurücksetzen-Knopf,
leer und Fehler — und liefert die Farben, die das Control liest. TopLogic garantiert Zugänglichkeit, nicht das Aussehen;
wer ins Bild passen will, liest die Tokens.

## Leser

| Control | Form |
|---|---|
| `TLChart` | die Wurzel `tl-chart` mit dem Canvas (`role="img"` mit Namen), dem Tooltip am Datenpunkt und, bei aktivem Zoom, dem Zurücksetzen-Knopf. Leer oder Fehler ersetzt Canvas und Rest durch eine `tl-status`-Zeile |

## Farben im Canvas

Ein Canvas nimmt kein `var()`; das Control liest die Werte im Browser aus den Eigenschaften am `<html>`, rechnet sie in die
Konfiguration von Chart.js und liest neu, sobald `data-tl-mode` oder `data-tl-theme` am `<html>` wechselt — ohne
Neuladen, und auch im Kundentheme in dessen Farben.

| Teil | Token |
|---|---|
| Reihe 1 bis 8 | `--tl-category-1-mark` bis `--tl-category-8-mark`; die neunte Reihe nimmt wieder die erste, die zehnte die zweite |
| Füllung von Balken und Segmenten | die Farbe der Reihe, deckend (kein Deckkraft-Suffix) |
| Punkte (Linie, Streuung, Radar) | `pointBackgroundColor` in der Farbe der Reihe, deckend |
| Fläche unter einer Linie oder einem Radar, wenn der Server `fill` einschaltet | die Farbe der Reihe mit 20 % Deckkraft |
| Fuge zwischen den Segmenten von Ring, Torte und Polar | `--tl-surface-layer` als `borderColor` |
| Gitter und Achsenlinien (auch die Speichen und Ringe eines Radars) | `--tl-border-separator` |
| Beschriftung, Teilstriche, Achsentitel, Titel, Untertitel und Legende | `--tl-text-secondary` |
| Hinterlegung der Teilstriche einer Radialachse (Radar, Polar) | `--tl-surface-layer` mit 75 % Deckkraft |

Die Werte sind `#RRGGBB`; Deckkraft hängt das Control als Hex-Suffix an, nur wo die Tabelle sie nennt.
Die Reihenfarben sind gerechnet, nicht gewählt: `category-N-mark` hat gegen `--tl-surface-base` und `--tl-surface-layer`
mindestens 3:1, und Nachbarn in der Reihenfolge sind unterscheidbar; die Reihenfolge ist in Hell und Dunkel dieselbe. Im
Kundentheme rechnet `derive` sie aus Hausfarbe und Grauwahl neu. Eine Reihe, deren Farbe der Server in der
Konfiguration mitgibt, behält sie, ebenso jede Farbe an einer Achse oder einem Plugin; nur was fehlt, kommt aus dem Theme.
Die Farben stehen in der Konfiguration dieses einen Diagramms, nie in den globalen Voreinstellungen von Chart.js: mehrere
Diagramme in verschiedenen Themes teilen sich eine Seite. Gefärbt werden nur die Achsen, die Chart.js für die Konfiguration
baut (die konfigurierten, die an den Reihen benannten `xAxisID`, `yAxisID`, `rAxisID` und die Achsen des Typs): keine
zusätzliche, leere Achse. Die Achsen der Konfiguration bleiben, wie der Server sie schickt: Chart.js baut dieselben
Achsen in derselben Reihenfolge, bindet jede Reihe an dieselbe Achse und zeichnet jeden Punkt an dieselbe Stelle wie ohne
Theme. Die Farben der Achsen malt ein Plugin dieses einen Diagramms (nicht global registriert), nachdem Chart.js die
Achsen zusammengeführt und gebaut hat, bei jeder Aktualisierung: neue Konfiguration, neue Größe, Wechsel des Modus. Ob
der Server eine Farbe gesetzt hat, als Wert oder als Funktion, liest es an dessen Konfiguration.

Kein Fallback: Eine fremde Palette gibt es nicht, im Java-Code nicht und auf dem Canvas nicht — das Plugin `colors` von
Chart.js ist je Diagramm ausgeschaltet (der Server darf es einschalten). Ist eine Kategorie keine lesbare Farbe, zeichnet
ihre Reihe oder ihr Punkt in der neutralen Trennerfarbe (`--tl-border-separator`), sichtbar ohne Kategorie, und die
übrigen behalten ihre Kategorie. Ist gar keine Kategorie lesbar (kein Design System), setzt das Control keine Farbe, auch
kein Feld mit Lücken.

## Aufbau

| Teil | Form |
|---|---|
| Wurzel | `tl-chart`: relativ gelegt, füllt ihren Behälter, Mindesthöhe `--tl-size-12` |
| Canvas | `role="img"`, der Name aus dem Resource-Key des Controls (`js.chart.label`, in der Sprache der Person); Chart.js zeichnet darin |
| Tooltip | `tl-popover` `tl-chart__tooltip` `tl-type-label`: die Fläche des Popovers (`popover.contract.md`), absolut am Datenpunkt, ohne Zeiger. Der Inhalt ist HTML vom Server; er erscheint erst, wenn er zum Datenpunkt unter dem Zeiger gehört |
| Zurücksetzen | `tl-chart__zoom-reset` oben rechts, darin der Part `Button` (secondary, nur Text); nur bei aktivem Zoom |
| Leer | eine `tl-status`-Zeile mit `role="status"` und der Meldung, mittig in der Wurzel |
| Fehler | eine `tl-status`-Zeile mit `role="alert"` und der Meldung, mittig in der Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Mindesthöhe der Wurzel | `--tl-size-12` |
| Ebene des Tooltips | `--tl-layer-sticky` |
| Ebene des Zurücksetzen-Knopfs | `--tl-layer-raised` |
| Fläche des Zurücksetzen-Knopfs | `--tl-surface-layer`, Ecke `--tl-radius-md` |
| Lage des Zurücksetzen-Knopfs | `--tl-space-xs` von oben und vom Ende |

Fläche, Rand, Schatten und Innenabstand des Tooltips kommen von `tl-popover`; `chart.css` schreibt keinen davon ab.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-chart` | an der Wurzel, immer |
| `tl-chart__tooltip` | am Tooltip, zusammen mit `tl-popover` und `tl-type-label` |
| `tl-chart__zoom-reset` | an der Hülle des Zurücksetzen-Knopfs |
| `role="img"` | am Canvas, mit Namen |
| `role="status"` | an der `tl-status`-Zeile „keine Daten“ |
| `role="alert"` | an der `tl-status`-Zeile des Fehlers |

## Konformität

Ein Diagramm besteht den Vertrag, wenn die Wurzel `tl-chart` trägt, das Canvas `role="img"` mit Namen hat, leer
`role="status"` und Fehler `role="alert"` als `tl-status`-Zeile sagen, der Tooltip `tl-popover` `tl-chart__tooltip` trägt,
die Reihen ihre Farben aus `--tl-category-N-mark` nehmen (die neunte wieder aus der ersten), Flächen von Balken und
Segmenten und Punkte deckend füllen, die Fläche unter Linie oder Radar (bei `fill`) mit 20 % Deckkraft, zwischen den
Segmenten von Ring, Torte und Polar eine Fuge in `--tl-surface-layer` stehen lassen, Gitter und Beschriftung aus
`--tl-border-separator` und `--tl-text-secondary`, nach einem Wechsel von `data-tl-mode` oder `data-tl-theme` neu gelesen
werden und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
