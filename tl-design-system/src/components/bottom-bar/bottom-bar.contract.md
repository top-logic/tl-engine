# Vertrag: BottomBar

Die Navigation am unteren Rand kleiner Bildschirme: wenige Seiten mit Icon und Label. TopLogic garantiert Landmarke
und „hier bist du“, nicht das Aussehen.

## Zustand

Das Control ist `TLBottomBar` (`ReactBottomBarControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `items` | Liste `{id, label, icon, badge?}` | Einträge; `icon` als Theme-Icon oder CSS-Klasse |
| `activeItemId` | string | die aktuelle Seite: `aria-current="page"` |

## Aktionen

| Aktion | Wann |
|---|---|
| `selectItem` | Klick, Enter oder Leertaste auf einem anderen als dem aktuellen Eintrag |

## Tokens

| Teil | Token |
|---|---|
| Höhe | `--tl-size-shell-bar` |
| Fläche, Linie | `--tl-surface-layer`, `--tl-border-separator` |
| Aktuelle Seite | `--tl-surface-interactive-selected` hinter dem Icon, Label `tl-type-label-strong` |
| Icon, Label | `tl-icon-md`, `tl-type-label` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-bottom-bar` | an `<nav>` mit übersetztem `aria-label` |
| `tl-bottom-bar__item` | jeder Eintrag, `button type="button"` |
| `aria-current="page"` | an der aktuellen Seite |
| `tl-bottom-bar__icon` | um das Icon; mit Zähler darin `tl-icon-badge` |
| `tl-bottom-bar__label` | um das Label |

## Konformität

Eine BottomBar besteht den Vertrag, wenn sie `<nav>` mit übersetztem Namen ist, genau die aktuelle Seite
`aria-current="page"` trägt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
