# Vertrag: Knopfleiste

Die untere Befehlsleiste einer Fläche — Fuß des Dialogs, Knopfleiste des Panels. Kein Control für sich: die Fläche
rendert sie um ihre Toolbar. TopLogic garantiert Lage und Verschwinden, nicht das Aussehen.

## Tokens

| Teil | Token |
|---|---|
| Abstand | `--tl-space-xs` zwischen den Knöpfen, `--tl-space-sm` `--tl-space-md` innen |
| Linie | `--tl-border-width-sm` `--tl-border-separator` oben |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-button-bar` | um die Toolbar am Fuß einer Fläche (`tl-dialog__surface`, `tl-panel`); im Dialog direktes Kind der Fläche |

## Konformität

Eine Knopfleiste besteht den Vertrag, wenn sie die Knöpfe rechtsbündig zeigt und ohne Knopf samt Linie verschwindet.

## Leser

Der Picker (`tl-picker__actions`) trägt seine Knöpfe in einer Knopfleiste.
