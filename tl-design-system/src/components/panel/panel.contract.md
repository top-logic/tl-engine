# Vertrag: Panel

Eine Fläche mit Kopf, Körper und Knopfleiste. TopLogic garantiert Kopf, Zustände und Bedienung, nicht das Aussehen.

## Zustand

Das Control ist `TLPanel` (`ReactPanelControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `title` | string | Titel, `h2` im `tl-titlebar` |
| `titleContent` | Kind, optional | frei gestalteter Inhalt neben dem Titel (`tl-titlebar__content`) |
| `toolbar` | Kind, optional | Toolbar im Kopf (`tl-titlebar__actions`) |
| `buttonBar` | Kind, optional | Toolbar am Fuß (`tl-button-bar`, Knöpfe secondary) |
| `expansionState` | `NORMALIZED` \| `MINIMIZED` \| `MAXIMIZED` \| `HIDDEN` | `data-tl-state~="minimized"`, `data-tl-state~="maximized"`; versteckt rendert nichts |
| `showMinimize`, `showMaximize`, `showPopOut` | boolean | Kopfknöpfe (`tl-titlebar__buttons`, Part `Button`, `small`) |
| `appearance` | `default` \| `card` | `card`: die Wurzel trägt `tl-card` (Vertrag `card`) |
| `hoverActions` | boolean | `tl-panel--hover-actions` |
| `fullLine` | boolean | `tl-panel--full-line` |

## Aktionen

| Befehl | Wann |
|---|---|
| `toggleMinimize` | Minimieren-Knopf; ein Klick auf den Kopf außerhalb seiner Knöpfe öffnet nur ein minimiertes Panel, er minimiert nicht |
| `toggleMaximize` | Maximieren-Knopf; Doppelklick auf den Kopf außerhalb seiner Knöpfe; `Escape` im maximierten Panel über den Tastatur-Verteiler: ein Popup darin schließt zuerst, ein von einem Element darin schon verbrauchtes Escape stellt nicht her |
| `popOut` | Lösen-Knopf |

## Tokens

| Teil | Token |
|---|---|
| Ebene maximiert | `--tl-layer-raised` |
| Grund maximiert | `--tl-surface-base` |
| Knöpfe beim Zeigen | `--tl-duration-base`, `--tl-easing-standard` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-panel` | immer |
| `tl-titlebar` | der Kopf, wenn er etwas trägt |
| `tl-titlebar__buttons` | um die Kopfknöpfe; Minimieren trägt `aria-expanded` (zu = minimiert) und, solange offen, `aria-controls` auf den Körper |
| `tl-panel__body` | der Körper, mit `id` |
| `tl-button-bar` | um die Knopfleiste |
| `data-tl-state~="minimized"`, `data-tl-state~="maximized"` | Zustand |

## Konformität

Ein Panel besteht den Vertrag, wenn sein Kopf `size-bar` hoch ist, Minimieren `aria-expanded` meldet und
Doppelklick, Escape und Kopfklick wie oben wirken, ohne einen Kopfknopf doppelt auszulösen.
