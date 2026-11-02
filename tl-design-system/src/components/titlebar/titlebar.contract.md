# Vertrag: Titelleiste

Der Kopf über einer Fläche, die einen Titel trägt: Dialog, Drawer; Panel und Karte (Welle 4c). Titel links, Aktionen
rechts, eine Linie darunter. Kein Control für sich — die Fläche rendert ihn.

## Tokens

| Teil | Token |
|---|---|
| Höhe | `--tl-size-bar`, mit und ohne Toolbar (eine Toolbar im Kopf hat keine eigene Leistenhöhe) |
| Abstand | `--tl-space-2xs` oben und unten, `--tl-space-md` vor dem Titel, `--tl-space-xs` hinter den Aktionen und zwischen ihnen |
| Linie | `--tl-border-width-sm` `--tl-border-separator` unten |
| Titel | `tl-type-heading-sm`, Ellipse |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-titlebar` | immer, am Kopf |
| `tl-titlebar__title` | am Titel; ein `h2` mit `id`, auf die die Fläche mit `aria-labelledby` zeigt |
| `tl-titlebar__actions` | um eine Toolbar der Fläche (Fensteraktionen), wenn es eine gibt |
| `tl-titlebar__content` | um frei gestalteten Inhalt neben dem Titel (Panel: `titleContent`) |
| `tl-titlebar__buttons` | um die Kopfknöpfe eines Behälters (Part `Button`, `small`): Minimieren mit `aria-expanded`, Maximieren, Lösen |

Kopfknöpfe (Maximieren, Schließen) stehen hinter den Aktionen als `tl-button tl-button--ghost tl-button--sm tl-button--icon`
mit `ThemeIcon` (`tl-icon-sm`), `aria-label` und Tooltip.

## Konformität

Eine Titelleiste besteht den Vertrag, wenn der Titel ein `h2` mit `id` ist, auf den die Fläche zeigt, und die
Kopfknöpfe 24 px groß (`--tl-size-control-sm`) und benannt sind.
Ein Kopf ist `--tl-size-bar` hoch, mit und ohne Toolbar; eine Toolbar mit `data-tl-overflow` gibt vor dem Titel nach.
