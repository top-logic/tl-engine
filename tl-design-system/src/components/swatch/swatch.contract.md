# Vertrag: Swatch

Ein Quadrat, das eine Auswahl zeigt: der Feldknopf eines Pickers, eine Zelle der Palette. (Die Zelle des Icon-Rasters ist `tl-icon-picker__cell`.)
TopLogic garantiert Maß, Rahmen und Zustände, nicht die Datenfarbe — die setzt das Control am Element.

## Klassen

| Klasse | Wann |
|---|---|
| `tl-swatch` | die Wurzel; `--tl-size-control` im Quadrat, Rahmen `--tl-border-control` |
| `tl-swatch--color` | der Swatch zeigt eine Farbe; nur er malt bei „leer“ das Schachbrett |
| `tl-swatch--wide` | füllt die Breite |

## Zustand

| Attribut | Wirkung |
|---|---|
| `data-tl-state~="empty"` | keine Farbe: am Farb-Swatch ein Schachbrett aus Tokens |
| `data-tl-state~="warning"` | Rahmen `--tl-status-warning-border` |
| `aria-invalid="true"` | Rahmen `--tl-status-error-border` |
| `aria-required` | Pflichtfeld; ohne eigene Zeichnung |

Lesend ist der Swatch ein `span` ohne Knopf, deaktiviert ein `button` mit `disabled`.

## Konformität

Ein Swatch besteht den Vertrag, wenn seine Zustände Attribute sind (nie `--empty`, `--error`), die Datenfarbe nicht aus
dem Paket-CSS kommt und der Fokus über `:focus-visible` aus `base.css` läuft.
