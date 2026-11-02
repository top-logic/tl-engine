# Vertrag: Werteliste

Die Werte eines mehrwertigen Felds (Spec 5c §7, V28); Leser ist `TLValueList` (`ReactValueListControl`). TopLogic
garantiert Funktion, Tastatur, Fokus und Ansage, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `elements[]` | Liste | die Werte, je ein Control |
| `layout` | `inline` \| `block` | in einer Zeile (Standard) oder untereinander (`tl-value-list--block`, etwa Texte mehrerer Zeilen) |
| `editable` | boolean | bearbeitend: Zeilen mit Entfernen-Knopf, darunter Hinzufügen |
| `disabled` | boolean | die Knöpfe sind deaktiviert |
| `ordered` | boolean | die Reihenfolge zählt: jede Zeile hat einen Griff (ab zwei Werten) |
| `upload` | boolean | die Werte kommen durch Hochladen (#29852): Hinzufügen öffnet die Dateiauswahl (mehrere Dateien, Name `js.valueList.addFiles`), auf die bearbeitende Liste gelegte Dateien werden hochgeladen; der Server fügt je Datei einen Wert an |
| `accept` | string, optional | bei `upload`: Filter der Dateiauswahl (HTML `accept`) |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `addElement` | der Hinzufügen-Knopf, außer bei `upload` |
| Upload über den Upload-Kanal, Teil `file` (`upload.contract.md`) | bei `upload`: gewählte oder auf die Liste gelegte Dateien |
| `removeElement` | der Entfernen-Knopf einer Zeile |
| `moveElement` | Ziehen am Griff; Alt+Pfeil hoch, runter auf dem Griff (`useListReorder`) |

## Tastatur

| Taste | Wirkung |
|---|---|
| Alt+Pfeil hoch, runter auf dem Griff | verschiebt den Wert eine Stelle; der Fokus folgt ihm auf den Griff seiner neuen Zeile, die Ansage nennt die Position (`js.reorder.*`) |

## Semantik

| Element | Rolle und Attribute |
|---|---|
| lesend | `span` mit `tl-value-list` (bzw. `tl-value-list--block`); zwischen zwei Werten `span` `tl-select__separator` mit „, “ — ausgeschrieben, damit kopiert wird, was zu sehen ist |
| bearbeitend | `div` mit `tl-value-list`, `role="group"`, Name über das Formularfeld |
| Zeile | `tl-value-list__row`, Ziehen-Zustände über `data-tl-state` (`drag.css`) |
| Griff | Part `Button` (ghost, klein, icon-only, `ICON_HANDLE`), Name `js.valueList.move`, Tooltip, die `handleProps` von `useListReorder` — wie der Griff der Spaltenwahl (`tl-reorder-list__handle`) |
| Entfernen, Hinzufügen | Part `Button` (ghost, klein, icon-only), Namen `js.valueList.remove`, `js.valueList.add` |

## Klassen

| Klasse | Element |
|---|---|
| `tl-value-list` | die Wurzel: lesend ein Text in einer Zeile, bearbeitend eine Spalte von Zeilen in ganzer Breite |
| `tl-value-list--block` | lesend untereinander; der Zeilenumbruch trennt, `tl-select__separator` ist ausgeblendet |
| `tl-value-list__item` | ein Wert; in der Zeile nimmt er den Platz zwischen Griff und Entfernen-Knopf |
| `tl-value-list__row` | eine Zeile beim Bearbeiten: Griff, Wert, Entfernen-Knopf |

Der Griff und die Knöpfe sind Parts und haben keine eigene Regel. Die Werteliste setzt keine Schrift: sie erbt sie von ihrem Ort (Tabellenzelle, Formularfeld), wie der Text, den sie dort ersetzt.

## In der Tabellenzelle

`table.css` füllt eine lesende Liste wie jeden Lesewert: ganze Breite, eine Zeile, Ellipse an der Spaltenkante. Die
Variante `--block` steht in der Zelle in einer Zeile, mit Trenner (`table.contract.md`, Zellinhalt).

## Attribute

| Attribut | Wann |
|---|---|
| `role="group"` | an der bearbeitenden Liste |
| `data-tl-state~="dragging"`, `~="drop-before"`, `~="drop-after"` | an der Zeile beim Ziehen (`drag.css`) |
| `data-tl-state~="dragover"` | an der Wurzel bei `upload`, solange Dateien darüber gezogen werden: dieselbe gestrichelte Linie wie `tl-file-upload` (`upload.css`, eine Regel); ein Wert, der umsortiert wird, ist kein Datei-Ziehen |
| `aria-busy="true"` | am Hinzufügen-Knopf, solange hochgeladen wird; der Knopf ist dabei `disabled` |
| `hidden` | verbirgt Liste oder Zeile |

## Tokens

| Teil | Token |
|---|---|
| Schrift | `--tl-text-primary`, auch der Trenner (wie im Select) |
| Zeilen und Knöpfe | Abstand `--tl-space-2xs` |

## Konformität

Eine Werteliste besteht den Vertrag, wenn lesend der Trenner ausgeschrieben ist, bearbeitend die Liste `role="group"`
trägt, Griff und Knöpfe Parts sind, sich jeder ziehbare Wert auch mit Alt+Pfeil verschieben lässt und im Klassen-Gate
keine Klasse ohne `tl-`-Präfix bleibt.
