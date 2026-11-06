# Vertrag: Popover

Die eine Fläche für alles, was über der Oberfläche schwebt und einen Anker hat: Menü, Select-Popup,
Toolbar-Überlauf. TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will,
liest die Tokens.

Ein Popover ist kein eigenes Control. Es ist die Fläche, die ein Control öffnet; die Rolle (`menu`,
`listbox`) bringt das Control mit. Die Lage rechnet `bridge/popover.ts` (floating-ui, Abstand zum Anker
`space-xs`) und setzt sie inline; das Stylesheet trägt Fläche, Ebene `layer-popover` und Grenzen.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `open` | boolean | Die Fläche ist sichtbar. Fehlt er, ist sie zu. |
| Anker | Element oder Punkt | Woran die Fläche hängt: das Rechteck eines Elements (Auslöser, Feld) oder ein Punkt (Kontextmenü an der Mausposition). |

## Aktionen

| Aktion | Wirkung |
|---|---|
| `close` | setzt `open` auf `false` |

## Verhalten

- Escape schließt.
- Ein Drücken außerhalb der Fläche und ihres Auslösers schließt.
- Beim Schließen kehrt der Fokus zum Auslöser zurück — oder zu dem Rückgabeziel, das der Aufrufer nennt: das
  Link-Popup des Wysiwyg-Editors gibt den Fokus in den Text zurück, nicht an den Knopf der Leiste
  (`rich-text.contract.md`).
- Die Fläche folgt ihrem Anker bei Scroll und Resize.
- Fehlt der Platz auf der bevorzugten Seite, klappt sie um.
- Sie bleibt im Viewport; reicht die Höhe nicht, scrollt sie bis `--tl-size-popover-max`.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-overlay` |
| Text | `--tl-text-primary` |
| Rand | `--tl-border-separator`, Breite `--tl-border-width-sm` |
| Ecke | `--tl-radius-md` |
| Schatten | `--tl-shadow-popover` |
| Ebene | `--tl-layer-popover` |
| Innenabstand | `--tl-space-xs` |
| Mindestbreite | `--tl-size-popover-min` |
| Höchste Höhe | `--tl-size-popover-max` |
| Inhalts-Popover: höchste Breite | `--tl-size-popover-content-max`, höchstens Viewport minus 2 × `--tl-space-xs`; kein Innenabstand |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-popover` | immer; an der schwebenden Fläche im Portal unter `document.body` |
| `data-tl-anchored-overlay` | immer; im Portal. Die Fläche liegt unter `document.body`, gehört aber zur Fläche ihres Auslösers: Fokus, der in sie wandert, gilt nicht als Ausbruch aus der Fokusfalle eines Dialogs und wird nicht zurückgeholt |
| `tl-popover--content` | Inhalts-Popover (`TLPopover`, View-Element `<popover>`, #29659): beliebiger Inhalt, bündig, begrenzte Breite |
| `hidden` | zu, wenn die Fläche gemountet bleibt (Toolbar-Überlauf: die Controls darin leben weiter) |

## Konformität

Ein Popover besteht den Vertrag, wenn es im Portal unter `document.body` liegt, `tl-popover` und
`data-tl-anchored-overlay` trägt, mit Escape und Drücken außerhalb schließt, den Fokus an den Auslöser
zurückgibt (oder an das Rückgabeziel des Aufrufers) und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.

## Leser

Der Picker (`tl-picker`) liegt als Hülle auf einem Popover.
