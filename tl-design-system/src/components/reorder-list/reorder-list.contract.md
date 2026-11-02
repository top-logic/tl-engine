# Vertrag: Umsortierbare Liste

Eine Liste, deren Einträge sich ziehen und mit der Tastatur verschieben lassen; ihr erster Leser ist die
Spaltenwahl der Tabelle (3b §8). Jeder Eintrag trägt vorn einen Griff, dahinter die Beschriftung und rechts Schloss und Auge; Griff
und Knöpfe erscheinen beim Zeigen oder Fokus. TopLogic garantiert Funktion, Tastatur und Fokus, nicht
das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `entries` | Liste | Die Einträge in ihrer Reihenfolge, je Eintrag: |
| `entries[].name` | string | Name des Eintrags (Spaltenname); er steht in den Befehlen und ist der Schlüssel des Eintrags. |
| `entries[].label` | string | Beschriftung und Teil des Griff-Namens. |
| `entries[].visible` | boolean | Sichtbar; ausgeblendet trägt der Eintrag `data-tl-state="off"`. |
| `entries[].locked` | boolean | Fixiert (Zustand `locked`); fixierte Einträge stehen als Block vorn, der letzte trägt `section-end`. |

Ab elf Einträgen steht vor der Liste eine Suche — dieselbe wie in der Tabelle: `tl-field-group` mit Lupe,
`input.tl-field type="search"` und, solange Text steht, Löschknopf (`js.textInput.clear`), nach dem Löschen
bleibt der Fokus im Feld. Sie filtert mit `filterOptions` (3b §9), und die
Liste rendert nur die passenden Einträge. Jeder behält seine Position in der ganzen Liste: Ziehen und
Alt + Pfeil rechnen mit ihr, ein Eintrag lässt sich also neben einen ausgefilterten ablegen. Mit der Suche
davor füllen Suche und Liste ihren Behälter als Spalte mit Innenabstand `--tl-space-md` (oben und
seitlich): die Suche bleibt stehen, nur die Liste scrollt. Die Höhe gibt der Behälter — die Spaltenwahl
öffnet ihr Fenster 70 % hoch, so schrumpft es beim Tippen nicht (Sichtprüfung 12.10.; vorher war die
Liste fest `--tl-size-popover-max` hoch und lief im kleineren Fensterkörper über). Alt + Pfeil verschiebt in einer gefilterten Liste um eine Stelle der **ganzen** Liste;
liegt ein ausgefilterter Eintrag dazwischen, ändert sich sichtbar nichts, nur die Ansage.

## Aktionen

| Auslöser | Aktion |
|---|---|
| Ziehen eines Eintrags an eine neue Stelle, Alt + Pfeil auf dem Griff | `sendCommand('columnReorder', …)` mit der neuen Reihenfolge — derselbe Befehl für Maus und Tastatur. |
| Auge (`tl-reorder-list__eye`) | `sendCommand('columnVisible', …)` für diesen Eintrag; an der letzten sichtbaren Spalte disabled mit Tooltip `js.table.columnLastVisible`. |
| Schloss (`tl-reorder-list__lock`) | Befehl `columnLock` (`sendCommand('columnLock', …)`): fixieren stellt den Eintrag ans Ende des Blocks, lösen direkt dahinter. |

## Tastatur

Der Griff ist ein `button` mit `aria-label` „<Name> verschieben“ und `aria-describedby` auf den
Hinweis („Alt + Pfeil verschiebt“); er ist fokussierbar wie jeder Knopf.

| Taste | auf dem Griff |
|---|---|
| `Alt` + `ArrowUp` / `ArrowDown` | verschiebt den Eintrag um eine Stelle nach oben bzw. unten und sendet denselben Befehl wie das Ziehen (`columnReorder`); der Fokus bleibt am Griff. Waagerecht (`data-tl-axis="horizontal"`): `Alt` + `ArrowLeft` / `ArrowRight`. |
| an den Enden | nichts: der erste Eintrag geht nicht weiter nach oben, der letzte nicht weiter nach unten. |
| `Tab` | weiter zu Schloss und Auge des Eintrags, dann zum nächsten Griff; ein unsichtbarer Knopf erscheint mit dem Fokus. |

Nach jedem Verschieben sagt ein Live-Bereich mit `role="status"` „<Name>, Position n von m“ an
(Resource-Key `js.reorder.moved`).

## Tokens

| Teil | Token |
|---|---|
| Liste | höchstens `--tl-size-popover-max` hoch, dann scrollt sie in sich; mit Suche davor füllt sie den Rest ihres Behälters (`flex: 1 1 auto`, `min-block-size: 0`) |
| Behälter mit Suche | Spalte, Abstand `--tl-space-xs`, Innenabstand `--tl-space-md` oben und seitlich, so hoch wie sein Behälter |
| Eintrag | Höhe `--tl-size-row` (als `min-height`), Innenabstand seitlich `--tl-space-control-x`, Lücke `--tl-space-xs`; beim Zeigen `--tl-surface-interactive-hover`; Beschriftung `--tl-text-secondary` bei `off` |
| Knöpfe rechts | `tl-reorder-list__actions`, Lücke `--tl-space-2xs`; Griff und Knöpfe tragen `tl-reveal-on-hover` (`layout.contract.md`): `opacity` 0, beim Zeigen und bei Tastaturfokus in der Zeile (`:has(:focus-visible)`, nicht `:focus-within`) 1, Übergang `--tl-duration-fast`; ohne Zeigen (`hover: none`) immer 1 |
| Block „Fixiert“ | Überschrift `tl-reorder-list__section` in `tl-type-label`, `--tl-text-secondary`; Linie unter `section-end` `--tl-border-width-sm` `--tl-border-separator` |
| Griff | `tl-button--icon --sm --ghost`, Icon in `--tl-icon-secondary`; der Fokusring aus `base.css` liegt innen (`outline-offset` −`--tl-focus-offset`), weil die Liste scrollt; das gilt auch für Schloss und Auge |
| Suche | Abstand zur Liste `--tl-space-xs`, an der `tl-field-group` |
| Gezogen, Einfügemarke | aus `drag.css` (`drag.contract.md`) |

Schrift: `.tl-type-body` an der Liste, von außen gesetzt; `tl-reorder-list` hat keine Schriftregel.
Die Beschriftung endet mit Ellipse.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-label` | am Griff: „<Name> verschieben“; am Suchfeld. |
| `aria-label` | an Schloss und Auge: was der Klick tut („<Name> fixieren“/„lösen“, „<Name> ausblenden“/„einblenden“); kein `aria-pressed`. |
| `aria-describedby` | am Griff: die Id des Hinweises „Alt + Pfeil verschiebt“. |
| `hidden` | am Hinweis: nie gezeigt; `aria-describedby` liest ihn trotzdem vor. |
| `disabled` | am Auge der letzten sichtbaren Spalte. |
| `data-tl-state="off"` / `locked` / `section-end` | am Eintrag: ausgeblendet, fixiert, letzter des Blocks. |
| `role="status"` | am Live-Bereich, der das Verschieben ansagt. |
| `data-tl-state="dragging"` | am Eintrag, der gerade gezogen wird (`drag.contract.md`). |
| `data-tl-state="drop-before"` / `"drop-after"` | am Eintrag, vor bzw. hinter dem der gezogene landet (`drag.contract.md`). |
| `:focus-visible` | den Ring liefert `base.css`; der Griff rückt ihn nur nach innen. |

Klassen:

| Klasse | Element |
|---|---|
| `tl-reorder-list` | das `ul` |
| `tl-reorder-list__item` | jedes `li` |
| `tl-reorder-list__handle` | der Griff, zusammen mit `tl-button tl-button--ghost tl-button--sm tl-button--icon` und `tl-reveal-on-hover` |
| `tl-reorder-list__label` | die Beschriftung |
| `tl-reorder-list__search` | an der `tl-field-group` der Suche vor der Liste |
| `tl-reorder-list__actions` | `span` am Zeilenende um Schloss und Auge |
| `tl-reorder-list__lock` | das Schloss, mit `tl-button tl-button--ghost tl-button--sm tl-button--icon` und `tl-reveal-on-hover` |
| `tl-reorder-list__eye` | das Auge, ebenso |
| `tl-reorder-list__section` | `li` mit der Überschrift „Fixiert“, `role="presentation"`, `tl-type-label` |
| `tl-visually-hidden` | der Live-Bereich: gelesen, nicht gezeigt (der Hinweis trägt `hidden`, siehe Attribute) |

Symbole (Griff, Schloss, Auge, Suche, Löschen) sind `ThemeIcon` in `tl-icon-sm`, Knopf-Icons mit `tl-button__icon`.

## Konformität

Eine Liste besteht den Vertrag, wenn jeder Eintrag einen Griff als `button` mit Namen und Hinweis
trägt, Alt + Pfeil denselben Befehl sendet wie das Ziehen und die neue Position über `role="status"`
ansagt, Ziehen und Einfügemarke allein über `data-tl-state` ausgedrückt sind und im Klassen-Gate keine
Klasse ohne `tl`-Präfix bleibt.
