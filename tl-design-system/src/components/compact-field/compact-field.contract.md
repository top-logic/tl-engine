# Vertrag: Compact field

Ein Wert, der in der Zeile nur als Vorschau Platz hat: die Vorschau und ein Knopf, der den Wert in einem Dialog
zeigt oder bearbeitet (Spec 5d §6). TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will,
liest die Tokens.

## Leser

| Control | Form |
|---|---|
| `ReactCompactFieldControl` | die Wurzel ist eine Reihe (`TLStack`, `tl-stack` `tl-stack--row`) mit `tl-compact-field`; darin die Vorschau (`TLText` mit Ellipse) und der Öffner (`TLButton`, ghost, klein, nur Icon). Bearbeitbar trägt die Wurzel zusätzlich `tl-field` und `data-tl-state~="edit"` |

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| editierbar | boolean | Bearbeitbar ist die Wurzel ein Feld wie ein Select, der Öffner öffnet den Editor; lesend ist sie Text, der Öffner zeigt den ganzen Wert |
| Öffner sichtbar | boolean | Lesend fehlt der Öffner (`hidden`), wenn die Vorschau schon alles zeigt oder der Wert leer ist |

## Verhalten

- Die Vorschau nimmt die Breite, die der Öffner lässt, und endet mit Ellipse; ihr ganzer Text ist dann ihr Tooltip.
- Der Öffner behält seine Größe, wie lang die Vorschau auch ist.
- Bearbeitbar sitzt der Öffner im Rahmen am Ende, `--tl-space-2xs` vom Rand, und zeigt Zeigen und Drücken eine Stufe
  kräftiger — mit derselben Regel wie die Knöpfe in `tl-select__controls` (`select.css`: ein Selektor, zwei Leser).
- Lesend erscheint der Öffner erst beim Zeigen auf die Zeile oder das Feld, beim Tastaturfokus und immer ohne Zeigen:
  er trägt `tl-reveal-on-hover` (`layout.contract.md`). Dieser Block blendet nichts selbst ein.

## Tokens

| Teil | Token |
|---|---|
| Öffner, Icon | `--tl-icon-secondary`, Icon `tl-icon-sm` (kleiner Icon-Knopf) |
| bearbeitbar, Abstand Vorschau–Öffner | `--tl-space-xs` |
| bearbeitbar, Abstand zum Rand am Ende | `--tl-space-2xs` |
| bearbeitbar, Öffner gezeigt | `--tl-surface-interactive-strong` |
| bearbeitbar, Öffner gedrückt | `--tl-surface-interactive-active` |
| Öffner, Fokusring | innen (`outline-offset` −`--tl-focus-offset`): die Tabellenzelle schneidet, was über sie hinausragt, und im Feld läge er auf dem Rahmen |
| Rahmen, Fehler | die von `tl-field` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-compact-field` | immer, an der Wurzel (zusammen mit `tl-stack`) |
| `tl-compact-field__preview` | an der Vorschau |
| `tl-compact-field__opener` | am Öffner: ein `button` mit `tl-button` `tl-button--ghost` `tl-button--icon` `tl-button--sm` und `aria-label` |
| `tl-reveal-on-hover` | am Öffner, nur lesend |
| `tl-field` | an der Wurzel, nur bearbeitbar |
| `data-tl-state~="edit"` | an der Wurzel, nur bearbeitbar |

## Konformität

Ein Compact field besteht den Vertrag, wenn „bearbeitbar“ ein Zustand ist (`data-tl-state~="edit"`, keine Klasse
`--edit`/`--view`), der lesende Öffner seine Sichtbarkeit nur aus `tl-reveal-on-hover` hat und im Klassen-Gate keine
Klasse ohne `tl-`-Präfix bleibt.
