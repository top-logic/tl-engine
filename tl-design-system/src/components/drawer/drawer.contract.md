# Vertrag: Drawer

Eine Schublade vom Rand: Detail zu einer Liste, Navigation auf kleinen Bildschirmen. TopLogic garantiert
Zugänglichkeit und Verhalten, nicht das Aussehen.

## Zustand

Das Control ist `TLDrawer` (`ReactDrawerControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `open` | boolean | Offen: `data-tl-state~="open"`. |
| `position` | `left` \| `right` \| `bottom` | Rand; gerendert als `tl-drawer--start`, `--end`, `--bottom`. |
| `size` | `narrow` \| `medium` \| `wide` | Maß. |
| `anchor` | `viewport` \| `container` | Am Viewport oder im Behälter (`tl-drawer--container`). |
| `width` | number, optional | Ausdehnung in px statt des Maßes (Breite, unten Höhe). |
| `title` | string, optional | Mit Titel steht ein `tl-titlebar` mit Schließen-Knopf. |

## Aktionen

| Aktion | Wann |
|---|---|
| `close` | Escape, Schließen-Knopf |

## Verhalten

- **Ohne Scrim (nicht modal):** beim Öffnen geht der Fokus auf das erste fokussierbare Element, sonst auf die
  Schublade; keine Falle, die Seite bleibt bedienbar; beim Schließen kehrt der Fokus zurück. Geschlossen ist sie
  `inert`.
- **Mit Scrim (modal):** sie steht in einem `tl-dialog`, trägt `role="dialog"` und `aria-modal="true"`, der Fokus ist
  gefangen, ein Klick auf den Scrim schließt.
- Escape schließt.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-overlay`; Schatten `--tl-shadow-dialog` nur offen |
| Ebene | `--tl-layer-overlay`; im Behälter `--tl-layer-raised` |
| Maße | `--tl-size-drawer-narrow`, `--tl-size-drawer-medium`, `--tl-size-drawer-wide` |
| Bewegung | `transform` über `--tl-duration-base`, kommend `--tl-easing-entrance`, gehend `--tl-easing-exit` |
| Kopf | `tl-titlebar` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-drawer` + `tl-drawer--start` \| `--end` \| `--bottom` + `tl-drawer--narrow` \| `--medium` \| `--wide` | immer, an `aside` |
| `tl-drawer--container` | im Behälter verankert; der Fokusring liegt innen (der Behälter beschneidet) |
| `data-tl-state~="open"` | offen |
| `inert` | geschlossen |
| `aria-labelledby` | mit Titel, auf `tl-titlebar__title` |
| `role="dialog"` + `aria-modal="true"` | nur mit Scrim |
| `tl-drawer__body` | Inhalt |

## Konformität

Ein Drawer besteht den Vertrag, wenn er geschlossen `inert` ist, ohne Scrim den Fokus nicht fängt und zurückgibt, mit
Scrim modal ist, Escape schließt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
