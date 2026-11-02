# Vertrag: Karte

Eine Fläche, die in der Oberfläche liegt: Karte, Panel als Karte, Accordion. TopLogic garantiert Fläche nach Lage und
Kopf, nicht das Aussehen.

## Zustand

Das Control ist `TLCard` (`CardState` in `state.proto`): `title`, `headerActions`, `child`, `padding`
(`default` \| `compact` \| `none` → `tl-card__body--pad-<stufe>`), `variant` (`outlined` \| `elevated`; `elevated`
zeichnet wie `outlined` — eine Karte schwebt nicht).

## Fläche nach Lage (Weg B)

Die Stufe folgt der Verschachtelung, nicht einer Variante. Das TSX liest sie aus `useSurface()`:

| Lage | Klasse | Fläche |
|---|---|---|
| auf der Seite | `tl-card` | `--tl-surface-layer` |
| in einer Fläche (Karte, Accordion, Panel als Karte) | `tl-card` `tl-card--nested` | `--tl-surface-layer-nested` |
| in einem schlichten Panel | wie dort, wo das Panel liegt | (das Panel malt keine Fläche) |
| im Dialog oder Drawer | `tl-card` `tl-card--overlay` | `--tl-surface-overlay` |

Hell sind `layer` und `nested` weiß; dort trennt die Linie (`--tl-border-width-sm` `--tl-border-separator`).

Leser auch `TLDashboard` (`dashboard.contract.md`): die Kachel ist `tl-card` mit Fläche nach Lage wie oben; sie rollt
(`overflow: auto`) statt zu beschneiden, ihre Höhe gibt das Raster.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-card` | die Wurzel; beschneidet am Radius (`--tl-radius-md`) |
| `tl-titlebar` | Kopf mit Titel (`h2`) und Kopfaktionen (`tl-titlebar__actions`) |
| `tl-card__body` | Körper, mit `tl-card__body--pad-default` (`--tl-space-md`), `--pad-compact` (`--tl-space-xs`) oder `--pad-none` |

## Konformität

Eine Karte besteht den Vertrag, wenn sie keinen Schatten trägt und ihre Fläche aus der Tabelle oben nimmt.
