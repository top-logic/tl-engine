# Vertrag: Spalten

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLColumns` registriert ist, rendert eine
umbrechende Reihe von Spalten. TopLogic garantiert Funktion und Zugänglichkeit, nicht das Aussehen.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `weights` | Liste von Zahlen | Das Gewicht jeder Spalte; sie wächst nach ihrem Anteil (`flex-grow`). |
| `breakpoint` | Länge | Die Umbruchbreite: darunter steht jede Spalte in einer eigenen Zeile. |
| `gap` | `compact` \| `default` \| `loose` | Der Abstand, als Modifier `tl-columns--gap-<stufe>`. |
| `children` | Inhalte | Je Spalte ein Element `tl-columns__column`. |

Die Flex-Basis einer Spalte setzt das Control inline, weil sie mit der Umbruchbreite rechnet: solange
der Behälter mindestens so breit ist, ist der Term negativ und damit null, die Reihe teilt sich nach
Gewicht; darunter übersteigt er die Reihe, und jede Spalte steht in einer eigenen Zeile. Die Spalten
stehen oben und behalten die Höhe ihres Inhalts.

## Aktionen

Keine.

## Tokens

| Teil | Token |
|---|---|
| `gap-compact` | `--tl-space-sm` |
| `gap-default` | `--tl-space-md` |
| `gap-loose` | `--tl-space-lg` |

## Klassen

| Klasse | Wann |
|---|---|
| `tl-columns` | die Reihe |
| `tl-columns--gap-compact`, `tl-columns--gap-default`, `tl-columns--gap-loose` | genau eine, der Abstand |
| `tl-columns__column` | jede Spalte |
| `tl-columns__column` mit `role="option"` und `aria-selected` | die Reihe ist auswählbar (`selectable`, `useItems`): die Spalte ist Option; Zeigen und Auswahl zeichnet die Regel der Einträge in `layout.css` (Vertrag `layout`), die ausgewählte Spalte trägt auch den Haken `tl-item__check` |

## Konformität

Die Reihe besteht den Vertrag, wenn sie genau einen Abstands-Modifier trägt, jede Spalte
`tl-columns__column` ist und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
