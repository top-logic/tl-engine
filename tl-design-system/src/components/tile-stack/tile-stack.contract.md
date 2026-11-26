# Vertrag: TileStack

Rahmen übereinander, der aktive oben. Verdeckte Rahmen behalten Steuerbaum und Box. TopLogic garantiert Sichtbarkeit
und Fokus, nicht das Aussehen.

## Zustand

`frames[]` und `activeIndex`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-tile-stack` | Wurzel; zurück: `data-tl-state~="backward"` (vorwärts ohne Marke) |
| `tl-tile-stack__frame` | je Rahmen; verdeckt: `data-tl-state~="covered"` (unsichtbar, nicht fokussierbar, nicht höher als der Stapel); ankommend `data-tl-state~="entering"`, gehend `data-tl-state~="exiting"` |

Die Marken setzt `useKeyedTransition`. Die Kopie des gehenden Rahmens ist ein inertes Bild (`useKeyedTransition`):
`aria-hidden`, `inert`, ohne ids. Der Wechsel gleitet mit den Keyframes von `slide`.

Ein verdeckter und ein gehender Rahmen schneiden ab, was ihr Inhalt über die Box des Stapels hinaus zeigt: sie
verlängern die Box, die den Stapel scrollt, nicht, und der aktive Rahmen lässt sich nicht aus dem Bild schieben. Den
Scrollstand dieser Box hält `TLTileStack` je Rahmen: ein neuer Rahmen beginnt oben, ein wieder aufgedeckter zeigt,
was er beim Verdecken zeigte.

## Tokens

`--tl-duration-slow`, `--tl-easing-entrance`, `--tl-easing-exit`.
