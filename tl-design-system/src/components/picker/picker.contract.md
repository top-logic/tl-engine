# Vertrag: Picker

Die Hülle der Popups von Farb- und Icon-Picker. Sie liegt auf `tl-popover` (Portal, `data-tl-anchored-overlay`) und
trägt keine feste Breite.

## Aufbau

| Klasse | Wann |
|---|---|
| `tl-picker` | die Wurzel im Popover; `role="dialog"` mit `aria-label` |
| `tl-picker__tabs` | die Reiter (`tl-tabs`); Pfeile wählen über `useTabList` |
| `tl-picker__scroll` | der Rollbereich statt der Reiter (`flex: 1 1 auto`, `min-block-size: 0`, `overflow-y: auto`): Inhalt, der allein rollt, während Suche davor und Fuß dahinter stehen bleiben (Werteliste, `filter-list.contract.md`) |
| `tl-picker__actions` | die Knopfleiste (`tl-button-bar`) am Fuß |

Das TSX baut das Popup als `PickerPopover`: `Escape` schließt nur den Picker und gibt den Fokus an den Feldknopf.

**Höhe.** Die Hülle hat keine feste Höhe: `usePopover` setzt `maxHeight` an der Hülle (Raum bis zum Fensterrand abzüglich eines Randes). Der Rollbereich (`tl-picker__scroll`) und die Reiter schrumpfen mit ihr und rollen selbst; das Popup als Ganzes scrollt nicht. `tl-popover` trägt dafür nichts Eigenes — `.tl-picker` ist schon die Flex-Spalte.

## Konformität

Ein Picker besteht den Vertrag, wenn er `role="dialog"` mit Namen trägt, die Reiter aus `tl-tabs` nimmt, die Leiste aus
`tl-button-bar` und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
