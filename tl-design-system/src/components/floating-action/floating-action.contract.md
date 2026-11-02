# Vertrag: Floating Action

Aktionen, die über einer Ecke des Körpers eines Panels oder Fensters schweben und dort stehen bleiben, während der
Körper rollt. Eine Aktion ist ein runder Knopf (mit Beschriftung, wenn ihr Befehl sie verlangt: extended FAB),
mehrere sind ein Speed Dial: ein runder Auslöser, der die Aktionen als kleine runde Knöpfe daneben zeigt.

## Zustand

Das Control ist `TLFloatingAction` (`ReactFloatingActionControl`, `FloatingActionStateJson`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `actions` | Controls (`TLButton`) | Die Aktionen; Beschriftung, Icon, Tooltip, deaktiviert, verborgen kommen aus ihrem Zustand. |
| `position` | `bottom-end` \| `bottom-start` \| `top-end` \| `top-start` | Die Ecke, gerendert als `data-tl-position`; fehlt: `bottom-end`. |
| `triggerImage` | Bild | Icon des Auslösers des Dials. |
| `triggerLabel` | string | Name und Tooltip des Auslösers. |

## Aufbau

| Klasse | Teil |
|---|---|
| `tl-floating-rail` | Schiene des Rahmens ohne Höhe, direkt vor (`data-tl-edge="top"`) und nach (`data-tl-edge="bottom"`) seinem Körper; nur da, solange etwas darin schwebt (`useFloatingFrame`, `useFloatingAnchor`). |
| `tl-fab` | Wurzel, in der Schiene ihrer Kante; ohne Rahmen im `body` mit `data-tl-anchor="viewport"`. `data-tl-state~="open"`: der Dial ist offen. |
| `tl-fab__main` | Hülle des Hauptknopfs: die eine Aktion oder der Auslöser (Part `Button` `primary`, icon-only, `aria-expanded`, `aria-controls`). |
| `tl-fab__actions` | Die Aktionen des Dials; `hidden`, solange er zu ist. |
| `tl-fab__action` | Hülle einer Aktion des Dials (Part `Button` `secondary`, icon-only, Beschriftung im Tooltip). |

## Verhalten

- Keine sichtbare Aktion: nichts. Eine: der runde Knopf. Mehrere: der Dial.
- Der Dial schließt mit Escape, mit einem Druck außerhalb und nach einer Aktion; per Tastatur geöffnet, geht der Fokus
  auf die erste Aktion, beim Schließen aus einer Aktion zurück zum Auslöser.
- Unten öffnet der Dial nach oben, oben nach unten.
- Verdeckte die Wurzel eine Snackbar (`useTransientOverlay`), schiebt das TSX sie per `transform` um `--tl-space-sm`
  daneben, nur bei tatsächlicher Überlappung.

## Tokens

| Teil | Token |
|---|---|
| Lage | Abstand `--tl-space-md` zu Kante und Seite |
| Ebene | `--tl-layer-overlay` |
| Hauptknopf | `--tl-size-fab`, `--tl-radius-full`, `--tl-shadow-popover` |
| Aktion | `--tl-size-fab-action`, `--tl-radius-full`, `--tl-surface-overlay`, `--tl-shadow-popover` |
| Abstand der Knöpfe | `--tl-space-sm` |
| Bewegung | `--tl-duration-base`, `--tl-easing-standard` (Ausweichen), `--tl-easing-entrance` (Öffnen) |
