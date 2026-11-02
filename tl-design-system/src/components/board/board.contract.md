# Vertrag: Board

Karten in Spalten (Spec 5c §5, V23, V5, V6, V29); Leser ist `TLKanbanBoard` (`ReactKanbanBoardControl`). Eine Spalte ist
eine Auswahlliste mit einem Tab-Halt, eine Karte ein Eintrag wie jeder andere (`tl-item`, `layout.contract.md`):
gewählt heißt Ring und Haken. TopLogic garantiert Funktion, Tastatur, Fokus und Ansage, nicht das Aussehen; wer ins
Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `columns[]` | Liste | je Spalte `key`, `label`, `count` und `cards[]` (je Karte `key`, `content`, `draggable`) |
| `selected` | Liste | die Schlüssel der gewählten Karten: `aria-selected="true"` |
| `multiSelect` | boolean | mehrere Karten wählbar: `aria-multiselectable="true"` an der Karten-Liste |
| `dragEnabled`, `dragKind` | boolean, string | Karten lassen sich ziehen; die Art der Nutzlast |
| `dropAcceptsAny`, `dropAccepts` | boolean, Liste | welche Nutzlast eine Spalte annimmt |
| `reorder` | boolean | Karten lassen sich in ihrer Spalte umordnen (Ablegen vor oder hinter einer Karte) |
| `dropVerdicts` | Objekt | Antwort des Servers je Probe: angenommen oder abgelehnt mit Grund (`tl-drop-hint`) |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `selectCard` | Klick auf eine Karte (`toggle` mit Strg/Cmd, `range` mit Umschalt), Enter oder Leertaste auf der fokussierten Karte, langes Drücken (Touch, mehrfach wählbar) |
| `dropProbe` | Ziehen über einer Spalte: fragt, ob dort abgelegt werden darf |
| `drop` | Ablegen mit dem Zeiger; Alt+Pfeil auf der fokussierten Karte (derselbe Befehl, `selection: false`, die Schlüsselliste wie beim Ziehen) |

## Tastatur

| Taste | Wirkung |
|---|---|
| `Tab` | je Spalte ein Halt: die Karten-Liste (`useRovingFocus`) |
| Pfeil hoch, runter | vorige, nächste Karte der Spalte |
| `Enter`, Leertaste | wählt die Karte wie ein Klick (`selectCard`); mit Strg/Cmd bzw. Umschalt wie beim Klick |
| Alt+Pfeil hoch, runter | verschiebt die Karte (mit den gewählten der Spalte, wenn sie gewählt ist) eine Stelle früher bzw. später; nur mit `dragEnabled` und `reorder` |
| Alt+Pfeil links, rechts | verschiebt sie in die Nachbarspalte an dieselbe Stelle, gibt es sie dort nicht, ans Ende; nur mit `dragEnabled` |

Nach dem Verschieben hat die Karte den Fokus, und `tl-board__live` sagt ihre neue Lage an (Spalte, Position). Lehnt
der Server ab, kommt seine Warnung wie beim Ziehen, und der Fokus bleibt.

## Semantik

| Element | Rolle und Attribute |
|---|---|
| Spalte | `section` mit `tl-board__column`, Name über `aria-labelledby` auf `tl-board__label` |
| Karten-Liste | `tl-board__cards` mit `role="listbox"`, Name über `aria-labelledby`, `aria-multiselectable` bei `multiSelect` |
| Karte | `tl-item` mit `tl-board__card`, `role="option"`, `aria-selected`; gewählt mit `tl-item__check` (`aria-hidden`), Markup wie `useItems` |
| Zähler | `tl-badge` (Part `Badge`) mit `tl-type-label-strong tl-nums` im Kopf |
| Ansage | `tl-board__live` mit `tl-visually-hidden`, `role="status"` |
| Grund einer Ablehnung | `tl-popover` mit `tl-drop-hint`, `role="status"` (`DropHint`, `drag.contract.md`) |

Ein Klick auf ein Bedienelement in der Karte (Link, Knopf der Kartenansicht) gehört dem Element: er wählt die Karte
nicht und beginnt kein Ziehen.

## Klassen

| Klasse | Element |
|---|---|
| `tl-board` | die Wurzel: Spalten nebeneinander, rollt waagerecht |
| `tl-board__column` | eine Spalte, `section`; Breite von `--tl-size-12` bis `--tl-size-13` |
| `tl-board__header` | der Kopf der Spalte: Beschriftung und Zähler, `tl-type-body-strong` |
| `tl-board__label` | die Beschriftung; endet mit Ellipse |
| `tl-board__cards` | die Karten-Liste; rollt senkrecht |
| `tl-board__card` | die Karte, am `tl-item`: Fläche, Rand und Innenabstand der Karte |
| `tl-board__live` | der Live-Bereich der Ansage, mit `tl-visually-hidden` |

Die Schrift setzen die Typo-Klassen im TSX; `board.css` setzt keine.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-selected="true"` | an der gewählten Karte (Ring und Haken aus `layout.css`) |
| `draggable="true"` | an einer Karte, die sich ziehen lässt (Zeiger `grab`) |
| `data-tl-state~="dragging"` | an der gezogenen Karte |
| `data-tl-state~="drop-before"`, `~="drop-after"` | an der Karte, vor bzw. hinter der abgelegt wird (Einfügelinie) |
| `data-tl-state~="drop-onto"` | an der Spalte unter dem Ziehen, solange sie annimmt (Hendrik 19.10., wie das alte Board); landet die Karte neben einer anderen, trägt diese zusätzlich die Einfügelinie |
| `data-tl-state~="drop-refused"` | an der Spalte, die ablehnt; dazu `tl-drop-hint` mit dem Grund |
| `hidden` | verbirgt Board, Spalte, Kopf oder Liste |

Die Ziehen-Zustände zeichnet allein `drag.css` (`drag.contract.md`). Die Ablageposition rechnet das Board wie bisher:
die nächste Kartengrenze irgendwo in der Spalte, auch in Lücken und unter der letzten Karte.

## Tokens

| Teil | Token |
|---|---|
| Wurzel | Abstand `--tl-space-md`, Innenabstand `--tl-space-sm` |
| Spalte | `--tl-surface-base` (Mulde; die Karte in `--tl-surface-layer` liegt in beiden Modi heller darauf), Rand `--tl-border-width-sm` in `--tl-border-separator`, Ecke `--tl-radius-md` |
| Kopf | Innenabstand `--tl-space-sm` `--tl-space-md`, Linie unten `--tl-border-width-sm` in `--tl-border-separator`, Schrift `--tl-text-primary`, Abstand `--tl-space-xs` |
| Karten-Liste | Abstand und Innenabstand `--tl-space-sm` |
| Karte | `--tl-surface-layer`, Rand `--tl-border-width-sm` in `--tl-border-separator`, Innenabstand `--tl-space-sm` `--tl-space-md`, rechts die Ecke des Hakens frei (`--tl-space-xs` + `--tl-size-icon-md` + `--tl-space-xs`, immer); Ecke, Hof beim Zeigen, Ring und Haken aus `tl-item` |

Kein Schatten, kein `z-index`. Den Fokusring liefert `base.css`; der Innenabstand der Karten-Liste hält ihn in der
rollenden Liste.

## Konformität

Ein Board besteht den Vertrag, wenn jede Spalte eine Auswahlliste mit einem Tab-Halt ist, Karten `tl-item` mit
`role="option"` und `aria-selected` sind, Alt+Pfeil denselben `drop` schickt wie das Ziehen, Ziehen und Ablegen allein
über `data-tl-state` gezeichnet werden, eine Ablehnung `tl-drop-hint` zeigt und im Klassen-Gate keine Klasse ohne
`tl-`-Präfix bleibt.
