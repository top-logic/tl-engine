# Vertrag: Picker

Die Hülle der Popups von Farb- und Icon-Picker. Sie liegt auf `tl-popover` (Portal, `data-tl-anchored-overlay`) und
trägt keine feste Breite.

## Aufbau

| Klasse | Wann |
|---|---|
| `tl-picker` | die Wurzel im Popover; `role="dialog"` mit `aria-label` |
| `tl-picker__tabs` | die Reiter (`tl-tabs`); Pfeile wählen über `useTabList` |
| `tl-picker__actions` | die Knopfleiste (`tl-button-bar`) am Fuß |

Das TSX baut das Popup als `PickerPopover`: `Escape` schließt nur den Picker und gibt den Fokus an den Feldknopf.

## Konformität

Ein Picker besteht den Vertrag, wenn er `role="dialog"` mit Namen trägt, die Reiter aus `tl-tabs` nimmt, die Leiste aus
`tl-button-bar` und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
