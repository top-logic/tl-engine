# Vertrag: Media

Ein Block für eingebettete Inhalte: Bild, PDF, HTML-Dokument und -Vorschau, Kamera-Video (Spec 5b §5). TopLogic
garantiert, dass der Inhalt seinen Platz füllt und sich wie verlangt einpasst; das Aussehen des Inhalts selbst kommt
nicht vom Paket.

## Leser

| Control | Form |
|---|---|
| `TLImage` | `tl-media` mit `tl-media--cover` oder `tl-media--contain` (Zustand `fit`); das Bild ist `tl-media__frame`. Größe (`width`, `height`, `aspectRatio`) gibt der Server inline an der Wurzel. Ohne Bild ein leeres `tl-media__placeholder`. |
| `TLPdfViewer` | `tl-media`, das `iframe` ist `tl-media__frame`; die Ansicht füllt (`useFill`). Ohne Dokument (`hasPdf` falsch) eine leere `tl-status`-Zeile mit `js.pdfViewer.noDocument`. |
| `TLHtml` | Dokument und Vorschau: `tl-media tl-media--document`, das `iframe` ist `tl-media__frame` auf dem Papier `--tl-surface-document`. Die Vorschau trägt zusätzlich `tl-media--preview`; inline stehen nur Daten: `aspectRatio` an der Wurzel, `width`, `height` und `transform` am Rahmen. Eingebettet (`display` `inline`) ist es kein `tl-media`, sondern `tl-prose` (`prose.contract.md`). |
| `TLPhotoCapture` | das Video im Kamera-Dialog ist `tl-media__frame`; gespiegelt ist `data-tl-state~="mirrored"` am Video. |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-media` | Wurzel; Block, höchstens so breit wie der Behälter |
| `tl-media__frame` | am `img`, `iframe` oder `video`; füllt die Wurzel, ohne Rahmen, Ecke `--tl-radius-sm` |
| `tl-media__placeholder` | reservierter Platz ohne Bild, leer; füllt die Wurzel |
| `tl-media--cover` | Einpassung: der Inhalt deckt die Fläche (`object-fit: cover`); die Wurzel ist so breit wie das Bild (`inline-size: fit-content`), solange der Server keine Größe angibt |
| `tl-media--contain` | Einpassung: der Inhalt liegt ganz in der Fläche (`object-fit: contain`); Breite wie bei `tl-media--cover` |
| `tl-media--document` | fremdes HTML: der Rahmen liegt auf `--tl-surface-document` |
| `tl-media--preview` | Vorschau einer Seite: die Wurzel schneidet ab (`overflow: hidden`), der Rahmen liegt außerhalb des Flusses links oben, wird von dort verkleinert und nimmt keinen Zeiger (`pointer-events: none`) |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="mirrored"` | am `tl-media__frame` des Kamera-Videos, solange gespiegelt |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Ecke des Rahmens | `--tl-radius-sm` |
| Papier hinter fremdem HTML | `--tl-surface-document` — in beiden Modi weiß, denn fremde Dokumente setzen Weiß voraus. Kein Textpaar: der Inhalt kommt nicht vom Paket. |

## Konformität

Ein Medium besteht den Vertrag, wenn die Einpassung eine Variante und das Spiegeln ein Attribut ist, fremdes HTML auf
`--tl-surface-document` liegt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
