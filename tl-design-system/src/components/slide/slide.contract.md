# Vertrag: Slide

Ein Versatz für jeden Wechsel, der vorwärts oder zurück führt. Das Ankommende kommt von der Seite, zu der es geht,
das Gehende blendet zur anderen aus. TopLogic garantiert die Richtung, nicht das Aussehen.

## Keyframes

| Name | Wann |
|---|---|
| `tl-slide-in-forward` | ankommend, vorwärts |
| `tl-slide-in-backward` | ankommend, zurück |
| `tl-slide-out-forward` | gehend, vorwärts |
| `tl-slide-out-backward` | gehend, zurück |

Leser: `tl-wizard`, `tl-tile-stack`. Der Versatz ist `--tl-space-md`; Dauer und Kurve setzt die Regel des Lesers.

Ein Wechsel ohne Bewegung (reduzierte Bewegung) setzt keine Marken (`useKeyedTransition`); die Keyframes laufen dann nie.
