# Vertrag: Tabelle

Ein Raster aus Kopf und virtuell gerendertem Körper (3b §3–§6), Leser ist `TLTableView`. Darüber
optional eine Filterleiste, im Rahmen die Spaltenwahl und der Griff der Fixiergrenze. Zustände sind
Attribute: ausgewählt, sortiert, aufgeklappt, Cursor, Lage einer Spalte, aktiver Filter, Ziehen.
TopLogic garantiert Funktion, Tastatur und Fokus, nicht das Aussehen; wer ins Bild passen will, liest
die Tokens.

## Zustand

Die Schlüssel von `TableViewControl` (`TLTableView.tsx`). Die Zeilenhöhe ist kein Zustand: das TSX
rechnet sie aus `--tl-size-row` (`useTokenPx`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `columns` | Liste | Die angezeigten Spalten in ihrer Reihenfolge, je Spalte: |
| `columns[].name` | string | Name der Spalte; Schlüssel der Zellen. |
| `columns[].label` | string | Überschrift. |
| `columns[].width` | number | Breite in Pixeln; steht inline an Kopf- und Zelle. |
| `columns[].sortable` | boolean | Die Überschrift ist ein Sortierknopf (`tl-table__sort`). |
| `columns[].sortDirection` | `asc` \| `desc` | Richtung, solange nach der Spalte sortiert wird. |
| `columns[].sortPriority` | number | Stufe einer mehrstufigen Sortierung (1 = erste); ab zwei Stufen als `tl-badge`. |
| `columns[].filterable` | boolean | Die Kopfzelle trägt den Trichter (`tl-table__filter`). |
| `columns[].filterActive` | boolean | Ein Filter der Spalte wirkt: `data-tl-state~="filtered"`, der Trichter heißt `js.table.filterActive` statt `js.table.filter`. |
| `columns[].groupable` | boolean | Nach der Spalte darf gruppiert werden. |
| `columns[].pinnedEnd` | boolean | Angeheftet am rechten Ende: `data-tl-state~="pinned"`, die erste davon `"pinned-first"`. |
| `columns[].cssClass` | string | Klasse an jeder Zelle der Spalte, Kopf eingeschlossen. |
| `columns[].tooltip` | string | Was die Überschrift über ihren Text hinaus sagt. |
| `totalRowCount` | number | Zeilen insgesamt; Grundlage von `aria-rowcount`. |
| `rows` | Liste | Die Zeilen im sichtbaren Fenster, je Zeile: |
| `rows[].id` | string | Kennung der Zeile. |
| `rows[].index` | number | Position in der ganzen Tabelle; Grundlage von `aria-rowindex` und der Id `<controlId>-row-<index>`. |
| `rows[].selected` | boolean | `aria-selected="true"`. |
| `rows[].cells` | Objekt | Die Zellen nach Spaltenname (Server-Controls). |
| `rows[].tooltips` | Objekt | Tooltip je Spaltenname, wo die Zelle mehr sagt, als sie zeigt. |
| `rows[].treeDepth` | number | Ebene im Baum; `aria-level` = `treeDepth` + 1, Einzug = Ebene × `--tl-size-control-sm`. |
| `rows[].expandable` | boolean | Die Zeile trägt einen `tl-toggle`; sonst hält `tl-table__spacer` seinen Platz. |
| `rows[].expanded` | boolean | `aria-expanded` an Zeile und Toggle. |
| `rows[].groupCount` | number | Nur an einer Gruppenzeile: wie viele Zeilen die Gruppe hält (`tl-table__count`). |
| `rows[].draggable` | boolean | Die Zeile lässt sich ziehen (`draggable="true"`). |
| `rows[].cssClass` | string | Klasse der Anwendung an der Zeile (`row-class`, #29815); `tl-status-error\|warning\|success\|info` tönt sie. |
| `rows[].cellClasses` | Objekt | Klasse je Spaltenname an einzelnen Zellen der Zeile, zusätzlich zur Klasse der Spalte; dieselben vier tönen. |
| `selectionMode` | `single` \| `multi` | Bei `multi` die Auswahlspalte mit `tl-checkbox` und `aria-multiselectable="true"`. |
| `selectAllState` | `none` \| `some` \| `all` | Was das Kopf-Kästchen zeigt (leer, teilweise, gesetzt); der Server zählt die Zeilen, auf die „alle auswählen“ wirkt — die der Filter durchlässt, eingeklappte Gruppen eingeschlossen (#29757). |
| `rowCount` | string | Zeilenzahl unter der Tabelle (`tl-table__row-count`), „von …“ solange gefiltert; leer: keine Zeilenzahl. |
| `rowCountSelected` | string | Ausgewählte Zeilen (`tl-table__selection-count`); leer, solange keine oder nur Einzelauswahl. |
| `cursorIndex` | number | Zeile des Tastatur-Cursors; −1: kein Cursor. |
| `frozenColumnCount` | number | Wie viele Spalten links fixiert sind (`frozen`, die letzte `frozen-last`). |
| `treeMode` | boolean | Baum: `role="treegrid"`. |
| `grouping` | string | Spalte, nach der gruppiert wird; leer: nicht gruppiert. Gruppiert: `role="treegrid"`. |
| `columnSelect` | boolean | Die Spaltenwahl (`tl-table__columns`) steht im Rahmen. |
| `filterBar` | boolean | Die Filterleiste steht über dem Rahmen. |
| `namedFilters` | Liste | Benannte Filter als `tl-chip` (`id`, `label`, `deletable`). |
| `activeNamedFilter` | string | Id des wirkenden benannten Filters; sein `tl-chip__toggle` trägt `aria-pressed="true"`. |
| `search` | string | Suchtext über die angezeigten Spalten. |
| `filterSaving` | boolean | Die aktuellen Kriterien lassen sich speichern (`tl-table__save`). |
| `dragEnabled` | boolean | Zeilen lassen sich ziehen. |
| `dragKind` | string | Art der gezogenen Daten (#29776). |
| `dropAcceptsAny` | boolean | Die Tabelle nimmt jede Art an. |
| `dropAccepts` | Liste | Arten, die die Tabelle als Ziel annimmt. |
| `dropModes` | Liste | Modi der Ablage-Operationen: `ordered` (vor/hinter einer Zeile), `onto` (auf eine Zeile), `control` (auf die Tabelle als Ganzes). Danach teilt das TSX eine Zeile: beide → Drittel, nur `ordered` → Hälften, nur `onto` → ganz. |
| `dropModesByKind` | Objekt | Die Modi je angenommener Art. Geteilt wird nach den Modi der gezogenen Art; eine Art, die hier fehlt, nach `dropModes`. So bekommt eine Liste, die Knoten einordnet und Notizen auf eine Zeile legt, für Knoten Hälften und für Notizen die ganze Zeile — nicht Drittel, deren Mitte den Knoten ablehnte (Review !1838). |
| `dropVerdicts` | Objekt | Antwort des Servers je Probe: angenommen oder abgelehnt mit Grund (`tl-drop-hint`, `drag.contract.md`). |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `select` | Klick auf eine Zeile oder ihr Kästchen, Leertaste am Cursor |
| `selectAll` | Kopf-Kästchen, Strg + A |
| `moveSelection` | Pfeile, Pos1/Ende, Bild auf/ab — mit Richtung, Umschalt (erweitern), Strg (nur Cursor) |
| `activate` | Doppelklick, Enter |
| `expand` | Toggle, Pfeil rechts/links an einer Baum- oder Gruppenzeile |
| `sort` | Sortierknopf; mit Umschalt eine Stufe dazu |
| `columnResize` | Griff `tl-table__resize` losgelassen |
| `columnReorder` | Kopfzelle an eine neue Stelle gezogen: die Hälfte der Überschrift unter dem Zeiger entscheidet die Seite (`useListReorder`, `data-tl-axis="horizontal"`); angeheftete Spalten ziehen nicht und nehmen nichts an |
| `setFrozenColumnCount` | Griff `tl-table__freeze` losgelassen, Kontextmenü „Bis hier fixieren“ / „Alle lösen“ |
| `openFilter` | Trichter |
| `openColumnSelect` | Spaltenwahl |
| `search` | Eingabe in der Suche (verzögert) |
| `applyNamedFilter` | `tl-chip__toggle` eines benannten Filters |
| `clearFilter` | `tl-chip__toggle` des wirkenden Filters |
| `saveNamedFilter` | Speichern in `tl-table__save` |
| `deleteNamedFilter` | `tl-chip__remove` |
| `drop` | Ablegen auf, vor oder hinter einer Zeile oder auf der Tabelle |
| `dropProbe` | Ziehen über ein Ziel: fragt einmal je Ziel, ob dort abgelegt werden darf |
| `scroll` | Rollen des Körpers: das neue sichtbare Fenster |

`TLTableView` sendet heute zusätzlich `group` aus dem Kontextmenü der Kopfzelle („Nach dieser Spalte
gruppieren“, „Gruppierung aufheben“); die Spaltenwahl sendet dafür `groupBy` (`reorder-list.contract.md`).

## Tastatur

Das Raster `tl-table__grid` ist fokussierbar (`tabindex="0"`) und hält den Fokus; der Cursor ist
virtuell (`aria-activedescendant`). Knöpfe im Kopf (Kopf-Kästchen, Sortieren, Trichter, Spaltenwahl)
sind eigene Tab-Halte; Kästchen und Toggles in Zeilen tragen `tabindex="-1"`.

Ein Editor im Raster (`input`, `textarea`, `select`, `contenteditable`) behält seine Tasten: steht der
Fokus auf ihm, lehnt das Raster Pfeile, Pos1/Ende, Bild auf/ab, Enter und Strg + A ab. Pfeil
links/rechts und die Leertaste wirken nur, solange das Raster selbst den Fokus hat — an einem Knopf
oder Kästchen im Raster gehören sie dem Bedienelement.

| Taste | im Raster |
|---|---|
| `ArrowUp` / `ArrowDown` | Cursor eine Zeile; mit Umschalt erweitert sich die Auswahl, mit Strg bewegt sich nur der Cursor |
| `Home` / `End` | erste bzw. letzte Zeile (Umschalt, Strg wie oben) |
| `PageUp` / `PageDown` | eine Fensterhöhe (Umschalt, Strg wie oben) |
| `Space` | wählt die Cursorzeile bzw. wählt sie ab |
| Strg + `A` | alle Zeilen (`selectAll`) |
| `Enter` | öffnet die Cursorzeile (`activate`) |
| `ArrowRight` / `ArrowLeft` | klappt eine Baum- oder Gruppenzeile auf bzw. zu; an jeder anderen Zeile schiebt der Körper waagerecht |

| Taste | im Kopf |
|---|---|
| `Enter` am Sortierknopf | sortiert nach der Spalte; Umschalt + `Enter` fügt eine Stufe an |

## Semantik

| Element | Rolle und Attribute |
|---|---|
| `tl-table__grid` | `role="grid"`, im Baum- und Gruppenmodus `role="treegrid"`; `aria-rowcount` = Zeilen + 1 (ohne Zeilen 2: die Statuszeile `aria-rowindex="2"` zählt mit); `aria-colcount`; `aria-multiselectable="true"` bei `multi`; `aria-busy="true"`, solange Zeilen im Fenster nachgeladen werden; `aria-activedescendant` = `<controlId>-row-<index>` |
| `tl-table__header` | `role="rowgroup"` |
| `tl-table__header-row` | `role="row"`, `aria-rowindex="1"`, `data-tl-axis="horizontal"` (Einfügemarke links/rechts) |
| `tl-table__header-cell` | `role="columnheader"`; `aria-sort` (`ascending` / `descending`) nur an der ersten Sortierstufe |
| `tl-table__body` | `role="rowgroup"`, `tabindex="-1"`: er scrollt nur und ist kein eigener Tab-Halt |
| `tl-table__row` | `role="row"` mit `id="<controlId>-row-<index>"`, `aria-rowindex`, `aria-selected`; im Baum `aria-level` und an aufklappbaren Zeilen `aria-expanded` |
| `tl-table__cell` | `role="gridcell"` |
| `tl-table__footer` | `role="status"`, `aria-live="polite"`: ein Screenreader hört die Zahl, die ein Filter übrig lässt |

**Cursor und Fokus (WCAG 2.4.7).** Das Raster setzt `aria-activedescendant` nur, solange eine Zeile
mit dieser Id gerendert und im sichtbaren Bereich des Körpers ist und `data-tl-state~="cursor"` trägt.
Ohne Cursor (`cursorIndex` −1), in der leeren Tabelle oder solange die Cursorzeile nicht sichtbar ist
(gerollt oder noch nicht geschickt), fehlt das Attribut — dann
zeigt das Raster selbst den Ring aus `base.css`, nach innen: `tl-table` schneidet an seiner Kante, ein Ring
außen wäre weg. Ebenso innen: der Körper und die Spaltenwahl in der Ecke (`focus-clip.test.mjs`). Mit dem Attribut gibt das Raster den Ring an die
Cursorzeile ab: `base.css` zeichnet ihn innen auf ihrem `::after`. Ein `aria-activedescendant` auf eine
Id, die es nicht gibt, ließe den Fokus unsichtbar.

## Ansicht (Popover des Zahnrads)

Das Zahnrad (`tl-table__columns`) öffnet ein Popover `tl-popover tl-picker tl-menu tl-table-view-options` mit
`role="dialog"`. Die Einträge, die Anatomie und das Verhalten des Menüs gelten (`menu.contract.md`); was dazukommt:

| Element | Aufbau |
|---|---|
| `tl-table-view-options__title` | Titel „Ansicht“: `tl-type-label-strong` in Primärtext, kein Abschnittskopf |
| `tl-menu__header` | Abschnittskopf „Dichte“ (`tl-type-label`, leiser Sekundärtext); er benennt die Gruppe der Segmente (`aria-labelledby`) |
| `tl-segmented tl-table-view-options__segments` | die drei Dichten als Radiogruppe (`choice.contract.md`), von den Rändern um `--tl-space-control-x` eingerückt wie der Text der Einträge |
| `tl-table-view-options__switch` | Schalter (`tl-choice` mit `tl-checkbox--switch`) für Streifen und Spaltenlinien, mit derselben Einrückung |
| `tl-divider` | die Trennlinie vor den Einträgen („Spalten …“, „Zurücksetzen“): eine Linie in `--tl-border-separator` |

Die Ansichtsoptionen (Dichte, Streifen, Spaltenlinien) gibt es nur, wo sie bleiben: Der Server schickt `viewOptions`
nur für eine Tabelle, die ihre Ansicht speichert (`TableView.isPersonalized()`). Ohne sie zeigt das Popover nur
„Spalten …“ (falls `columnSelect`) und „Zurücksetzen“; ohne beides steht das Zahnrad nicht da.

## Stapel

Ring (`::after`, `base.css`) und Einfügemarke (`::before`, `drag.css`) einer Zeile liegen auf
`--tl-layer-sticky`, über den haftenden Zellen ihrer Zeile (`--tl-layer-raised`). Die Zeile ist
positioniert (`position: relative`, im TSX `absolute`), bildet aber keinen eigenen Stapel; der Körper
isoliert ihn (`isolation: isolate`): nichts aus einer Zeile liegt über Filterleiste, Spaltenwahl oder
Fixiergrenze. Der Rahmen `tl-table__frame` isoliert ebenso (`isolation: isolate`): haftende Kopfzellen
(`--tl-layer-raised`), Fixiergrenze und Vorschau (`--tl-layer-sticky`) liegen nie über einem Popover
oder einem Nachbarn der Seite. Was die Tabelle öffnet (Kontextmenü, Grund einer Ablehnung), steht als
Portal im `body`, nicht im Rahmen. Der Kopf liegt **außerhalb** des Rollbehälters `tl-table__body`, der seine Zeilen
abschneidet (`overflow: auto`): Ring und Marke einer Zeile, die unter den Kopf rollt, werden mit ihr
abgeschnitten und können den Kopf nicht übermalen. Darum braucht der Kopf keine eigene Ebene. Jede
Kopfzelle isoliert ihren Stapel (`isolation: isolate`): die Einfügemarke eines Spaltenzugs (`::before`,
`drag.css`) bleibt in ihrer Zelle und malt nicht über fixierte Kopfzellen, unter die die Überschrift
gerollt ist. Fixierte und angeheftete Kopfzellen sind deckend: sie erben die Fläche der Kopfzeile. Kopf und
Körper scrollen waagerecht gemeinsam: das TSX überträgt `scrollLeft` des Körpers auf den Kopf
(`overflow: hidden`).

## Tokens

| Teil | Token |
|---|---|
| Tabelle | abgesetzte Fläche (außer `tl-fill`): `--tl-surface-layer`, Rundung `--tl-radius-md`, Rand `--tl-border-width-sm` in `--tl-border-separator`, Schatten `--tl-shadow-1` (die leichteste Stufe der Skala; eine Tabelle, die eine ganze Fläche füllt, schwebt nicht und trägt weder Schatten noch Rundung noch Rand — ihre Tiefe kommt vom Kopf) |
| Schrift | `--tl-text-primary`; Titel im Kopf `--tl-text-secondary` in Großbuchstaben (`text-transform: uppercase`), die Sperrung trägt `tl-type-overline` am `tl-table__label`; Anzahl der Gruppenzeile `--tl-text-primary` |
| Filterleiste | Lücke `--tl-space-xs`, Innenabstand `--tl-space-xs` `--tl-space-sm`, Fläche `--tl-surface-layer`, Linie darunter `--tl-border-width-sm` in `--tl-border-separator`; Speichern mit Lücke `--tl-space-2xs` |
| Kopf | Linie darunter `--tl-border-width-sm` in `--tl-border-emphasis`; Kopfzeile `--tl-size-bar` hoch auf `--tl-surface-layer-muted` (dieselbe Fläche wie Zebra und Gruppenzeile; Text darauf `--tl-text-secondary`, `--tl-text-primary` mit Kontrast gegen sie geprüft) |
| Kopfzelle | Innenabstand `--tl-space-control-x`, Lücke `--tl-space-2xs`; ohne Trennlinie, mit `data-tl-column-lines` rechts `--tl-border-width-sm` in `--tl-border-separator`; Sortierknopf unter dem Zeiger `--tl-surface-interactive-hover` |
| Sortiermarke, Trichter | `--tl-icon-secondary`; Trichter bei aktivem Filter `--tl-icon-primary`. Der Trichter trägt `tl-reveal-on-hover` (`layout.contract.md`; das TSX setzt die Klasse, Task 9) und steht direkt in der Kopfzelle: verborgen per `opacity` (Tab erreicht ihn), sichtbar beim Zeigen auf die Kopfzelle, bei Tastaturfokus und ohne Zeigen (Touch). An einer Spalte mit `data-tl-state~="filtered"` steht er immer (`table.css`, schlägt `tl-reveal-on-hover` durch Spezifität) Sortierpfeil und Gruppenmarke (`tl-table__mark`) bleiben stets sichtbar: sie stehen nur dort, wo sie gelten |
| Spaltenwahl | `--tl-size-control` breit (`tl-button--icon`), `--tl-size-bar` hoch, Fläche `--tl-surface-layer-muted` (wie der Kopf; auch der Verlauf davor), unter dem Zeiger `--tl-surface-interactive-hover`; die Reserve, mit der Kopf und Körper hinter der letzten Spalte enden, ist dasselbe Token (das TSX liest `--tl-size-control`) und passt so in jeder Dichte |
| Griffe (Breite, Fixiergrenze) | `--tl-space-xs` breit, Fixiergrenze `--tl-size-bar` hoch; unter dem Zeiger und beim Ziehen eine Linie `--tl-border-width-md` in `--tl-border-interactive` |
| Vorschau der Fixiergrenze | `--tl-border-width-md` in `--tl-border-interactive` |
| Ebenen | Griff der Breite, haftende Zellen `--tl-layer-raised`; Fixiergrenze, Vorschau, Ring und Marke der Zeile `--tl-layer-sticky` |
| Zeile | `--tl-size-row` hoch (`min-height`), Fläche `--tl-surface-layer`, Linie darunter `--tl-border-width-sm` in `--tl-border-separator`; gerade Zeile mit `data-tl-striped` an der Tabelle (`data-tl-state~="even"`) und Gruppenzeile (`data-tl-state~="group"`) auf `--tl-surface-layer-muted`; gezeigt `--tl-surface-interactive-hover`, ausgewählt `--tl-surface-selected`, ausgewählt und gezeigt `--tl-surface-selected-hover`, dazu an der ersten Zelle die Kante (innerer Schatten `--tl-border-width-accent` links (Anfangsseite in LTR; RTL ist nicht gelöst) in `--tl-border-selected`; haftende Zellen erben die Fläche, die Kante sitzt auf der Zelle; ohne fixierte Spalte rollt sie mit der ersten Zelle weg); jede Fläche der Zeile steht in `:where`, Ablegen darauf (`drop-onto`, `drag.css`) übermalt sie. **Vorrang: gewählt vor gezeigt vor Zebra und Gruppe** — gleiche Spezifität, die Reihenfolge in `table.css` entscheidet; eine getönte Zeile (`data-tl-role`) übermalt Zebra und Gruppe |
| Zelle | Innenabstand `--tl-space-control-x`; ohne Trennlinie, mit `data-tl-column-lines` rechts `--tl-border-width-sm` in `--tl-border-separator` (die letzte Zelle nie; die Kante von `frozen-last` bleibt `--tl-border-emphasis`). Zahlenspalte (`data-tl-align="end"`): rechtsbündig, `justify-content: flex-end`, `font-variant-numeric: tabular-nums` |
| Kante zur scrollenden Fläche | `frozen-last` rechts, `pinned-first` links: `--tl-border-width-sm` in `--tl-border-emphasis` |
| Feld in der Zelle | randlos; ein beschreibbares Feld (nicht `disabled`, nicht `readonly`) zeigt eine innere Linie unten in `--tl-border-width-sm`, `--tl-border-control`, auf gezeigter und ausgewählter Zeile `--tl-border-interactive` (23.10.); Fokusring innen (`outline-offset` −`--tl-focus-offset`); Warnung innere Linie `--tl-border-width-md` in `--tl-status-warning-border`, Fehler in `--tl-status-error-border` |
| Baum | Toggle und Einzug je Ebene `--tl-size-control-sm` (Platzhalter `tl-table__spacer`); Abstand der Anzahl `--tl-space-xs` |
| Platzhalter | Innenabstand `--tl-space-control-x`, Linie darunter wie die Zeile |
| Ablehnung | Rand `--tl-status-error-border`, Text `--tl-status-error-text` |
| Ansicht, Popover | Fläche, Rand und Schatten die von `tl-popover`; Lücke zwischen den Zeilen `--tl-space-2xs`; Titel `--tl-text-primary` in `tl-type-label-strong`, Innenabstand `--tl-space-2xs` `--tl-space-control-x`; Segmente und Schalter rücken um `--tl-space-control-x` ein; Trennlinie `--tl-border-width-sm` in `--tl-border-separator` (`tl-divider`) |
| „+ n weitere“ | der Chevron steht im Label, die Lücke zum Text ist `--tl-space-2xs` |

Der Kopf scrollt waagerecht ohne eigenen Balken (`scrollbar-width: none`); Kopf und Körper folgen einander in beide Richtungen (12.10.). Schrift: `.tl-type-body` am `tl-table__body`, von der Komponente gesetzt. Die Gruppenzeile ist stark: jede ihrer Zellen
trägt `tl-type-body-strong`, und ein Lesewert darin erbt sie (`field.contract.md`, 09.10.). Ein Text setzt seine
Variante selbst; ihm gibt der Server dieselbe Klasse als `css-class` (`text.contract.md`). Ihre Anzahl ist `tl-type-label` und steht als Kapsel (`--tl-radius-full`, `--tl-surface-interactive-strong`, Schrift `--tl-text-primary`, so hoch wie `tl-badge`). Eine Zeile schwebt nicht: kein Schatten, Kanten sind Linien; nur die Tabelle als Ganzes steht abgesetzt (siehe oben). Ziehen und Ablegen
zeichnet `drag.css` (`drag.contract.md`), den Cursor-Ring `base.css`.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-selected` | an jeder Zeile: `"true"` ausgewählt, sonst `"false"` |
| `aria-sort` | an der Kopfzelle der ersten Sortierstufe |
| `aria-expanded` | an aufklappbaren Zeilen und ihrem `tl-toggle` |
| `aria-busy` | am Raster, solange Zeilen nachgeladen werden |
| `data-tl-state~="cursor"` | an der Cursorzeile (genau eine, die Id aus `aria-activedescendant`) |
| `data-tl-state~="group"` | an einer Gruppenzeile: eigene Zeilenform auf `--tl-surface-layer-muted`, der Gruppenwert halbfett (`tl-type-body-strong`), die Anzahl als Kapsel (`tl-table__count`) |
| `data-tl-state~="even"` | an jeder zweiten Zeile, gezählt aus `rows[].index` (nicht aus dem DOM: die Zeilen sind virtuell gerendert und absolut gelegt, `nth-child` zählte die Fenster-Position); sie liest nur mit `data-tl-striped` an der Tabelle |
| `data-tl-striped` | an `tl-table`, vorhanden = an: Zebra (die geraden Zeilen auf `--tl-surface-layer-muted`); ab Werk aus |
| `data-tl-column-lines` | an `tl-table`, vorhanden = an: Linien zwischen den Spalten in Kopf und Zeilen; ab Werk aus |
| `data-tl-density="normal"` \| `"compact"` | an `tl-table`: die Dichte dieser Tabelle, gegen die Dichte der Seite (`tokens.css` hat einen Block für `normal`, damit eine Tabelle eine kompakte Seite überstimmt); fehlt das Attribut, gilt die Dichte der Seite |
| `data-tl-align="end"` | an Kopf- und Datenzelle einer Zahlenspalte (`ColumnKind.NUMBER`): rechtsbündig, Ziffern gleich breit; fehlt es, steht die Spalte am Anfang |
| `data-tl-state~="frozen"` | an Kopf- und Zellen der fixierten Spalten; die letzte zusätzlich `"frozen-last"` |
| `data-tl-state~="pinned"` | an Kopf- und Zellen der angehefteten Spalten; die erste zusätzlich `"pinned-first"` |
| `data-tl-state~="filtered"` | an der Kopfzelle einer Spalte mit aktivem Filter |
| `data-tl-state~="covered"` | am Kopf, solange Spalten unter der Spaltenwahl weiterlaufen (Kopf breiter als sein Fenster, nicht ganz nach rechts gescrollt); vor der Spaltenwahl blenden die Überschriften dann aus |
| `data-tl-state~="dragging"`, `"drop-before"`, `"drop-after"`, `"drop-onto"`, `"drop-refused"` | an Zeile, Kopfzelle oder `tl-table` als Ganzem (`drag.contract.md`) |
| `draggable="true"` | an ziehbaren Zeilen und verschiebbaren Kopfzellen |
| `role="separator"` | an `tl-table__resize` mit `aria-label` `js.table.columnWidth` („Breite von {0} ändern“, {0} die Überschrift der Spalte) und an `tl-table__freeze` mit `aria-label` `js.table.freezeSplitter`; `aria-orientation="vertical"`, Zeigerbedienung; der Griff der Breite zusätzlich `tabindex="0"`, `aria-valuenow` (Breite in px), `aria-valuemin`; Pfeile links/rechts um `--tl-space-md`, Home an die Mindestbreite, End passt die Spalte an ihren Inhalt an (wie der Doppelklick); das Loslassen einer Pfeiltaste sendet `columnResize` |
| `hidden` | an jedem Element, das gerade nicht steht |

Cursor, Lage, Filter, Überdeckung und Ziehen sind Zustände: die Wörter `cursor`, `group`, `even`, `frozen`, `frozen-last`, `pinned`,
`pinned-first`, `filtered`, `covered` und die des Ziehens stehen in `data-tl-state`, nie als Modifier (Klassen-Gate).

Bauteile anderer Blöcke:

| Bauteil | Wo und wie |
|---|---|
| `tl-checkbox` | Auswahlspalte (`tl-table__select`): im Kopf mit `aria-label` `js.table.selectAll`, in der Zeile `js.table.selectRow` und `tabindex="-1"`. Kopf „teilweise“ nur als `indeterminate` — **kein** `aria-checked`: ARIA in HTML verbietet es am nativen Kästchen, der Browser meldet `indeterminate` als „gemischt“ (entschieden 07.10.). |
| `tl-toggle` | Auf-/Zuklapper in Baum- und Gruppenzeilen: `tabindex="-1"`, `aria-expanded`, `aria-label` wie jeder Toggle, **ohne** `aria-controls` — er zeigte auf die eigene Zeile. Zeigen und Drücken mit dem veil (`toggle.contract.md`): sichtbar auch auf gezeigter und ausgewählter Zeile. Ein Druck mit der Maus lässt den Fokus, wo er ist (`mousedown` ohne Standardaktion): am Raster, dessen Ring an der Cursorzeile bleibt. |
| `tl-button--icon` | Trichter (`--sm --ghost`, `aria-label` und Tooltip `js.table.filter`, bei aktivem Filter `js.table.filterActive`), Spaltenwahl (`--ghost`, `js.table.columns`, in angehefteter letzter Kopfzelle `--sm`), Speichern; Icons `tl-icon-sm` |
| `tl-badge` | Stufe einer mehrstufigen Sortierung im Sortierknopf |
| `tl-status` | statt Zeilen in der leeren Tabelle: `role="row"` `aria-rowindex="2"` (im `treegrid` mit `aria-level="1"`) > `div.tl-status` mit `role="gridcell"` — die Zelle ist die Statuszeile —, Text `js.table.empty`; die Zeile klebt am linken Rand des Körpers (`position: sticky`), auch wenn er waagerecht gescrollt ist. Die Tabelle hat keine Ladezeile: Zeilen im Fenster, die noch fehlen, sind `tl-table__placeholder` (`aria-hidden`), das Raster trägt `aria-busy`. |
| `tl-skeleton` | in `tl-table__placeholder` für Zeilen im Fenster, die der Server noch nicht geschickt hat; der Platzhalter ist `aria-hidden`, das Raster trägt `aria-busy` |
| `tl-chip` | benannter Filter in der Filterleiste: `span.tl-chip` > `tl-chip__toggle` (`aria-pressed`) und, wenn löschbar, `tl-chip__remove`. Verschwindet die Kapsel mit ihrem fokussierten `tl-chip__remove`, geht der Fokus zum Entfernen-Knopf der Nachbarkapsel (gleicher Platz, sonst der vorige), ohne einen ins Suchfeld, sonst ins Raster (WCAG 2.4.3; derselbe Helfer wie beim Wert-Chip des Select) |
| `tl-field-group` | Suche (`tl-table__search`) mit Lupe und, solange Text steht, Löschknopf (`js.textInput.clear`), nach dem Löschen bleibt der Fokus im Feld — dieselbe Suche wie in der Spaltenwahl (`reorder-list.contract.md`); der Name beim Speichern ist ein `tl-field` in `tl-table__save` |
| `tl-popover` | Grund einer Ablehnung beim Ziehen, mit `tl-drop-hint` (`drag.contract.md`; vorher `tl-table__hint`), Lage aus `bridge/popover.ts` |

Klassen:

| Klasse | Element |
|---|---|
| `tl-table` | die Wurzel; Ziel, wenn auf der Tabelle als Ganzem abgelegt wird |
| `tl-table__filter-bar` | die Filterleiste über dem Rahmen |
| `tl-table__more` | der Chip „+ n weitere“ in der Filterleiste: ein `tl-chip` (`tl-chip__toggle`, `aria-haspopup="menu"`), der die übrigen benannten Filter als Menü öffnet; die Klasse gibt ihm nur `flex: none`, die Form liefert `tl-chip` |
| `tl-table__search` | die `tl-field-group` der Suche, am rechten Ende |
| `tl-table__save` | Speichern der Kriterien (Name, Speichern, Abbrechen) |
| `tl-table__frame` | der Rahmen um Raster, Spaltenwahl und Fixiergrenze |
| `tl-table__grid` | das Raster (`role="grid"` / `"treegrid"`), Fokus |
| `tl-table__header` | der Kopf (`role="rowgroup"`) |
| `tl-table__header-row` | die Kopfzeile |
| `tl-table__header-cell` | jede Kopfzelle |
| `tl-table__sort` | der Sortierknopf, ein `button` |
| `tl-table__label` | die Überschrift, Großbuchstaben in `--tl-text-secondary`; endet mit Ellipse. Das TSX setzt `tl-type-overline` daran (Sperrung `0.04em`) |
| `tl-table__mark` | Sortierpfeil, Gruppierungsmarke: `tl-icon-sm`, `aria-hidden` |
| `tl-table__filter` | der Trichter |
| `tl-table__columns` | die Spaltenwahl (Zahnrad) |
| `tl-table__resize` | Griff der Spaltenbreite |
| `tl-table__freeze` | Griff der Fixiergrenze |
| `tl-table__freeze-preview` | wo die Fixiergrenze beim Loslassen landet |
| `tl-table__body` | der rollende Körper (`role="rowgroup"`) |
| `tl-table__canvas` | trägt die volle Höhe aller Zeilen; die Zeilen stehen absolut darin |
| `tl-table__row` | jede Zeile |
| `tl-table__cell` | jede Zelle |
| `tl-table__select` | die Zelle der Auswahlspalte (mit `tl-table__cell` bzw. `tl-table__header-cell`) |
| `tl-table__cell--command` | die Zelle einer Spalte mit einem Befehl je Zeile (`RowCommandColumn`): ohne Innenabstand, der Knopf (ghost, klein) mittig. Die Java-Klasse der Spalte landet an Kopf- und Datenzelle, darum gilt die Regel für `tl-table__cell` und `tl-table__header-cell`. Eine Form, kein Zustand |
| `tl-table__tree` | Toggle, Einzug und Wert in der Baumspalte; ein `div`, kein `span`: er ist eine Flex-Reihe, kein Lesewert — als Lesewert füllt die Zelle nur ein `span` ohne Klasse oder ein `tl-text` (siehe „Zellinhalt“) |
| `tl-table__spacer` | Platzhalter eines Blatts an der Stelle des Toggles |
| `tl-table__value` | der Wert neben dem Toggle; endet mit Ellipse |
| `tl-table__count` | Anzahl der Gruppenzeile, eine Kapsel |
| `tl-table__placeholder` | Zeile im Fenster, die der Server noch nicht geschickt hat: `aria-hidden`, trägt ein `tl-skeleton`; keine Ladezeile mit `tl-progress` |
| `tl-table__footer` | die Zeilenzahl unter dem Rahmen, `tl-type-label`; steht nur, solange `rowCount` nicht leer ist |
| `tl-table__row-count` | wie viele Zeilen (von wie vielen) |
| `tl-table__selection-count` | wie viele ausgewählt, nach einem Mittelpunkt |

Klassen der Anwendung (#29815): `row-class` und die Klasse je Zelle stehen zusätzlich an `tl-table__row` bzw.
`tl-table__cell`. Vier davon übersetzt `TLTableView` in eine Rolle: `tl-status-error|warning|success|info` →
`data-tl-role="error|warning|success|info"` an Zeile bzw. Zelle, gezeichnet über `@tl-roles: status` (Fläche
`--tl-status-<rolle>-subtle`, Linie `--tl-status-<rolle>-border`). An einer ausgewählten Zeile und ihren Zellen
steht keine Rolle: die Auswahl gewinnt. Zeigen ändert die Tönung nicht, haftende Zellen erben die Tönung ihrer
Zeile.

## Zellinhalt

| Wurzel in der Zelle | Wie |
|---|---|
| `span` ohne Klasse, `tl-text` | Lesewert ohne eigene Form: füllt die Zelle als Block (volle Breite, `nowrap`), endet mit Ellipse. Spezifität (0,2,0), damit `text.css`, das im Bundle danach steht, das Füllen nicht kippt; ein leerer `tl-text` und `hidden` blenden weiterhin aus. |
| `tl-pill`, `tl-badge`, `tl-chip` | Kapsel: behält ihr `display` und ihre Form, an die Zelle gebunden mit `max-width: 100%`. Die Pill (auch die Wurzel eines Texts als Pill, `text.contract.md`) kürzt ihre Beschriftung `tl-pill__label` mit Ellipse. |
| `a` | so breit wie sein Text, höchstens die Zelle, endet mit Ellipse. |
| `tl-field-value`, `tl-select__values`, `tl-choice-group` | bleibt eine Reihe und bricht nicht um; eine Werteliste wird an der Spaltenkante abgeschnitten. |
| `tl-value-list` (lesend, `span`) | ein Lesewert wie ein Text: füllt die Zelle als Block, eine Zeile, endet mit Ellipse; die Variante `tl-value-list--block` steht in der Zelle in einer Zeile, Einträge und Trenner im Fluss (`value-list.contract.md`). Spezifität (0,2,0), `hidden` blendet aus. |
| `tl-stack--row` (Wert mit Zusatzknopf, etwa dem Sprachknopf eines mehrsprachigen Texts) | eine Reihe in Spaltenbreite; mit `tl-stack--grow-first` nimmt der Wert den Platz und endet mit Ellipse. |
| `tl-field`, `tl-field-group` | ist die Zelle: randlos, so hoch wie die Zeile (siehe Tokens). |

Breiten, Versätze haftender Zellen, Einzug und die Lage der Zeilen sind Daten und stehen inline
(`style`), alles andere in `table.css`. Inline wird nichts im Namensraum `--tl` definiert.

## Konformität

Eine Tabelle besteht den Vertrag, wenn das Raster fokussierbar ist und `aria-activedescendant` nur auf
eine gerenderte, sichtbare Cursorzeile setzt, jede Zeile `aria-selected` und `aria-rowindex` trägt, Sortierung,
Auswahl, Lage, Filter und Ziehen allein über `aria-*` und `data-tl-state` ausgedrückt sind, das
Kopf-Kästchen „teilweise“ nur als `indeterminate` meldet und im Klassen-Gate keine Klasse ohne
`tl`-Präfix bleibt.
