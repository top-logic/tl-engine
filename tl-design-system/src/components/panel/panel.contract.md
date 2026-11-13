# Vertrag: Panel

Eine Fläche mit Kopf, Körper und Knopfleiste. TopLogic garantiert Kopf, Zustände und Bedienung, nicht das Aussehen.

## Zustand

Das Control ist `TLPanel` (`ReactPanelControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `title` | string | Titel, `h2` im `tl-titlebar` |
| `explanation` | string, optional | Erklärung dessen, was das Panel zeigt (etwa die Beschreibung des Attributs einer Tabelle): Tooltip des Titels. Ohne Titel steht neben dem Körper ein Info-Icon (`tl-panel__info`, Glyphe `bi bi-info-circle`, Name „Hilfe“, die Erklärung als Tooltip); Körper und Icon liegen dann in `tl-panel__explained`. |
| `explanationHelp` | boolean | Die Erklärung ist auch Hilfetext (Bearbeiten-Modus): ein Hilfeknopf (Part `Button`, `small`, Glyphe `fa-regular fa-circle-question`, Name „Hilfe“, die Erklärung als Tooltip, `aria-expanded`, `aria-controls`) in `tl-titlebar__explanation` hinter dem Titel — ohne Titel das Info-Icon als Knopf — blendet `tl-panel__help-text` unter dem Kopf ein und aus. Ohne `explanationHelp` steht am Titel kein Icon. |
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
| — | Hilfeknopf der Erklärung oder Info-Icon im Bearbeiten-Modus: blendet `tl-panel__help-text` im Client ein oder aus |

## Tokens

| Teil | Token |
|---|---|
| Ebene maximiert | `--tl-layer-raised` |
| Grund maximiert | `--tl-surface-base` |
| Knöpfe beim Zeigen | `--tl-duration-base`, `--tl-easing-standard` |
| Hilfetext der Erklärung | Text `--tl-text-helper`, Innenabstand `--tl-space-2xs` oben und unten, `--tl-space-md` seitlich |
| Info-Icon | Fläche `--tl-size-control-sm`, Abstand zum Körper `--tl-space-2xs`; Glyphe `--tl-icon-secondary`, unter dem Zeiger `--tl-icon-primary` (`tl-panel__help`) |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-panel` | immer |
| `tl-titlebar` | der Kopf, wenn er etwas trägt |
| `tl-titlebar__buttons` | um die Kopfknöpfe; Minimieren trägt `aria-expanded` (zu = minimiert) und, solange offen, `aria-controls` auf den Körper |
| `tl-panel__body` | der Körper, mit `id` |
| `tl-panel__explained` | Zeile aus Körper und Info-Icon eines Panels ohne Titel mit Erklärung |
| `tl-panel__info` | um das Info-Icon, oben am Ende neben dem Körper |
| `tl-panel__help` | am Info-Icon selbst |
| `tl-panel__help-text` | der Hilfetext der Erklärung unter dem Kopf, `hidden` solange eingeklappt |
| `tl-button-bar` | um die Knopfleiste |
| `data-tl-state~="minimized"`, `data-tl-state~="maximized"` | Zustand |

## Konformität

Ein Panel besteht den Vertrag, wenn sein Kopf `size-bar` hoch ist, Minimieren `aria-expanded` meldet und
Doppelklick, Escape und Kopfklick wie oben wirken, ohne einen Kopfknopf doppelt auszulösen.
