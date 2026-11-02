# Vertrag: Dashboard

Kacheln in einem gepackten Raster (Spec 5c §6, V5, V6, V12, V20); Leser ist `TLDashboard` (`DashboardElement`). Eine
Kachel ist eine Karte (`tl-card`, `card.contract.md`) und ein Overlay (`tl-overlay`, `overlay.contract.md`). TopLogic
garantiert Funktion, Tastatur, Fokus und Ansage, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `children[]` | Liste | je Kachel `id`, `width` (`quarter` … `full`), `rowSpan`, `control` und optional `action` (`label`, `disabled`, `tooltip`) |
| `minColWidth` | Länge | Mindestbreite einer Spalte (etwa `16rem`); daraus die Spaltenzahl |
| `rowHeight` | Länge | Höhe einer Rasterzeile |
| `editMode` | boolean | Bearbeiten: `data-tl-state~="edit"` an der Wurzel |

Die Packung (Spalten, Zeilen, Lage jeder Kachel) rechnet die Engine und setzt sie inline; das Paket hat dafür keine
Form. Den Abstand der Kacheln liest das TSX aus `--tl-space-md` (`useTokenPx`) — für die Rechnung und das Raster
derselbe Wert.

## Aktionen

| Aktion | Auslöser |
|---|---|
| `activate` | Klick auf eine Kachel mit Aktion; Enter oder Leertaste am Knopf `tl-dashboard__action`. Nicht im Bearbeiten, nicht bei `disabled` |
| `hide` | der Ausblenden-Knopf im Bearbeiten |
| `reorder` | Ziehen einer Kachel im Bearbeiten; Alt+Pfeil links, rechts an der fokussierten Kachel (`useListReorder`, `axis: 'horizontal'`) |

## Tastatur

| Taste | Wirkung |
|---|---|
| `Tab` | je Kachel mit Aktion ihr Knopf (außer Sicht, der Ring liegt an der Kachel); im Bearbeiten jede Kachel und ihr Ausblenden-Knopf |
| `Enter`, Leertaste am Knopf der Kachel | führt die Aktion aus (`activate`) |
| Alt+Pfeil links, rechts an einer Kachel im Bearbeiten | schiebt sie eine Stelle früher bzw. später (`reorder`); die Ansage nennt die neue Position („Position n von m“), der Fokus bleibt an der Kachel |

## Semantik

| Element | Rolle und Attribute |
|---|---|
| Wurzel | `tl-dashboard` mit `tl-inset` (Innenabstand); im Bearbeiten `data-tl-state~="edit"` |
| Raster | `tl-dashboard__grid`, `data-tl-axis="horizontal"`: die Einfügemarke steht links und rechts |
| Kachel | `div` mit `tl-card`, `tl-dashboard__tile`, `tl-overlay`, `tl-overlay--fill`; der Inhalt in `tl-overlay__base`. Im Bearbeiten `role="group"`, fokussierbar (`tabindex="0"`), mit Name und Beschreibung aus `useListReorder` (ohne Rolle dürfte das `div` keinen Namen tragen) |
| Aktion | `button` mit `tl-dashboard__action` und `tl-visually-hidden`, direkt in der Kachel, Name `label`; der Tooltip über `tooltipProps` sitzt an der Kachel (der Knopf ist außer Sicht und bekommt keinen Zeiger); deaktiviert `disabled` bzw. `aria-disabled="true"` |
| Abdeckung | im Bearbeiten `div` `tl-dashboard__cover` in einer Ebene `tl-overlay__layer` `tl-overlay__layer--fill`, im DOM vor der Ebene des Ausblenden-Knopfs |
| Ausblenden-Knopf | Part `Button` (ghost, icon-only, Name `js.dashboard.hideTile`) in `tl-overlay__action` in einer Ebene `tl-overlay__layer` `tl-overlay__layer--top-right` |

## Klassen

| Klasse | Element |
|---|---|
| `tl-dashboard` | die Wurzel, mit `tl-inset` |
| `tl-dashboard__grid` | das Raster; Spalten, Zeilenhöhe und Abstand inline (Daten der Packung) |
| `tl-dashboard__tile` | die Kachel, mit `tl-card` und `tl-overlay tl-overlay--fill`; Lage im Raster inline |
| `tl-dashboard__action` | der verborgene Knopf der Aktion, mit `tl-visually-hidden` |
| `tl-dashboard__cover` | die Abdeckung im Bearbeiten |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="edit"` | an der Wurzel, solange das Dashboard bearbeitet wird |
| `data-tl-axis="horizontal"` | am Raster: die Einfügemarke einer gezogenen Kachel steht links und rechts |
| `data-tl-state~="dragging"` | an der gezogenen Kachel (`drag.css`) |
| `data-tl-state~="drop-before"`, `~="drop-after"` | an der Kachel, vor bzw. hinter die abgelegt wird (`drag.css`) |
| `disabled`, `aria-disabled="true"` | am Knopf einer Aktion, die gerade nicht geht: die Kachel bleibt, wie sie ist (kein Zeigen, kein Zeiger) |
| `hidden` | verbirgt Dashboard oder Kachel |

## Bild

- Die Kachel ist eine Karte: Fläche, Rand und Ecke aus `tl-card`, **kein Schatten**. Was nicht in ihre Höhe passt,
  rollt (`overflow: auto` statt `clip` der Karte).
- Eine Kachel mit Aktion nimmt den Klick auf ihrer ganzen Fläche (Zeiger `pointer`); beim Zeigen trägt ihr Rand
  `--tl-border-interactive` — nicht im Bearbeiten, nicht bei deaktivierter Aktion.
- Fokus: hat der verborgene Knopf den Tastaturfokus, trägt die Kachel den Ring. Die Regel steht in `base.css` als
  Alternative der einen Ring-Regel (`.tl-dashboard__tile:has(> .tl-dashboard__action:focus-visible)`); `dashboard.css`
  liest kein Fokus-Token. Der Knopf steht mit `--tl-space-sm` Abstand zur Kachelkante, sein eigener Ring endet so
  nicht an der rollenden Kachel.
- Bearbeiten: die Abdeckung nimmt den Zeiger (`cursor: move`), ein Klick erreicht den Inhalt nicht. Der
  Ausblenden-Knopf liegt darüber, weil seine Ebene im DOM später steht — kein `z-index`.
- Ziehen: Einfügemarke und gezogene Kachel zeichnet `drag.css`; der leuchtende Balken fällt.

## Tokens

| Teil | Token |
|---|---|
| Kachel | aus `tl-card`: `--tl-surface-layer`, Rand `--tl-border-width-sm` in `--tl-border-separator`, Ecke `--tl-radius-md` |
| Zeigen | Rand `--tl-border-interactive` |
| Knopf der Aktion | Abstand `--tl-space-sm` zur Kachelkante |
| Abstand der Kacheln | `--tl-space-md` (inline, über `useTokenPx`) |
| Ausblenden-Knopf | aus `tl-overlay__action` |

## Konformität

Ein Dashboard besteht den Vertrag, wenn Kacheln `tl-card` ohne eigenen Schatten sind, die Aktion ein Knopf mit
`tl-visually-hidden` ist, dessen Fokus den Ring der Kachel zeigt, Bearbeiten ein Attribut der Wurzel ist, der
Ausblenden-Knopf in `tl-overlay__action` sitzt, Ziehen und Alt+Pfeil denselben `reorder` schicken und im Klassen-Gate
keine Klasse ohne `tl-`-Präfix bleibt.
