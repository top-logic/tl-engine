# Vertrag: Dialog

Eine modale Fläche über der Seite: Rückfrage, Formular, Spaltenwahl, Editor. TopLogic garantiert Zugänglichkeit und
Verhalten, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

Zwei Controls, ein Baustein: `TLDialog` (`ReactDialogControl`) rendert den Scrim `tl-dialog`, `TLWindow`
(`ReactWindowControl`) die Fläche `tl-dialog__surface` darin. `TLDialogManager` stapelt die offenen Dialoge.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `open` (Dialog) | boolean | Der Scrim steht. |
| `closable` | boolean | Escape, Klick auf den Scrim und der Schließen-Knopf schließen; `false`, solange Arbeit läuft (Knopf deaktiviert). |
| `closeOnBackdrop` | boolean | Ob ein Klick auf den Scrim schließt. |
| `title` | string | Titel der Fläche. |
| `width`, `height` | CSS-Maß | Größe vom Server. |
| `customWidth`, `customHeight` | number | Vom Benutzer gezogene Größe; gilt, solange sie in den Viewport passt. |
| `resizable` | boolean | Griffe, Maximieren, Doppelklick. |
| `toolbar`, `footer`, `child` | Control | Aktionen im Kopf, Knopfleiste im Fuß, Inhalt. |

## Aktionen

| Aktion | Wann |
|---|---|
| `close` | Escape, Scrim, Schließen-Knopf |
| `resize` | Ende eines Zugs an einem Griff, mit `width`/`height` in px |
| `resetSize` | Doppelklick auf einen Griff |

## Verhalten

- Beim Öffnen geht der Fokus auf das erste Feld; ohne Feld auf den primären Knopf im Fuß (er ist der Enter-Standard),
  sonst auf das erste Bedienelement im Körper, sonst auf den ersten Fußknopf, der nicht `--danger` ist; erst ohne all
  das auf die Fläche. Nie auf einen Knopf der Titelleiste. Tab bleibt in der Fläche; beim Schließen kehrt er zum
  Auslöser zurück.
- Escape schließt den obersten Dialog; ein Popup darin (Select, Menü) schließt zuerst.
- Ziehen am Kopf verschiebt die Fläche im Viewport; die Griffe ändern die Größe (mindestens 200 × 100 px, höchstens
  Viewport minus Rand); Doppelklick auf den Kopf maximiert.
- Ein- und Ausblenden: `data-tl-state~="entering"` am neuen Scrim, eine inerte Kopie mit `~="exiting"` beim Schließen;
  das Paket blendet über `--tl-duration-base` ein und aus, eine Anwendung überschreibt das. Bei reduzierter Bewegung
  entfällt beides.

## Tokens

| Teil | Token |
|---|---|
| Scrim | `--tl-surface-scrim`, Ebene `--tl-layer-overlay`; der Rand `--tl-space-lg` steckt in den Höchstmaßen der Fläche (`--tl-size-viewport-width` / `--tl-size-viewport-height` minus 2 × Rand), nicht im Scrim |
| Fläche | `--tl-surface-overlay`, Rahmen `--tl-border-width-sm` `--tl-border-separator`, Ecke `--tl-radius-md`, Schatten `--tl-shadow-dialog` |
| Kopf | `tl-titlebar` |
| Fuß | Abstand `--tl-space-sm` `--tl-space-md`, Lücke `--tl-space-xs`, Linie oben `--tl-border-separator` |
| Griffe | Kanten `--tl-space-xs`, Ecken `--tl-space-sm`, über den Rand um `--tl-space-2xs` |
| Bewegung | `--tl-duration-base`, `--tl-easing-entrance`, `--tl-easing-exit` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-dialog` | am Scrim |
| `tl-dialog__surface` + `role="dialog"` + `aria-modal="true"` + `aria-labelledby` | an der Fläche; `aria-labelledby` zeigt auf `tl-titlebar__title` |
| `data-tl-state~="entering"` / `~="exiting"` | am Scrim, während er kommt bzw. als Kopie geht |
| `data-tl-state~="maximized"` | an der Fläche, maximiert |
| `tl-dialog__body`, `tl-button-bar` | Körper; Fuß als direktes Kind der Fläche (ohne Knopf verborgen, Vertrag `button-bar`) |
| `tl-dialog__resize` + `data-tl-edge` | Griff je Kante und Ecke: `n`, `ne`, `e`, `se`, `s`, `sw`, `w`, `nw` |

## Konformität

Ein Dialog besteht den Vertrag, wenn die Fläche `role="dialog"`, `aria-modal="true"` und `aria-labelledby` trägt, der
Fokus gefangen ist und zurückkehrt, Escape von innen nach außen schließt und im Klassen-Gate keine Klasse ohne
`tl-`-Präfix bleibt.
