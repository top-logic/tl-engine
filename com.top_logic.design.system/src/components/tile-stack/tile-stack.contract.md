# Vertrag: TileStack

Rahmen übereinander, der aktive oben. Verdeckte Rahmen behalten Steuerbaum und Box. TopLogic garantiert Sichtbarkeit
und Fokus, nicht das Aussehen.

## Zustand

`frames[]` und `activeIndex`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-tile-stack` | Wurzel; zurück: `data-tl-state~="backward"` (vorwärts ohne Marke) |
| `tl-tile-stack__frame` | je Rahmen; verdeckt: `data-tl-state~="covered"` (unsichtbar, nicht fokussierbar); ankommend `data-tl-state~="entering"`, gehend `data-tl-state~="exiting"` |

Die Marken setzt `useKeyedTransition`. Die Kopie des gehenden Rahmens ist ein inertes Bild (`useKeyedTransition`):
`aria-hidden`, `inert`, ohne ids. Der Wechsel gleitet mit den Keyframes von `slide`.

## Tokens

`--tl-duration-slow`, `--tl-easing-entrance`, `--tl-easing-exit`.
