# Vertrag: Reihen und Abstände

Stack, Grid, Inset, Bounded, Item, Deck und Region — die Bausteine, mit denen Ansichten ihre Kinder ordnen. Kein
Aussehen, nur Lage und Abstand. Die Gap-Skala teilen sie mit `tl-columns`. Dazu zwei Marken, die jedes Control tragen
kann: füllen (`tl-fill`) und erst beim Zeigen erscheinen (`tl-reveal-on-hover`).

## Zustand

| Control | Schlüssel | Klasse |
|---|---|---|
| `TLStack` | `direction`, `gap`, `align`, `justify`, `wrap`, `growFirst`, `fill`, `maxWidth`, `itemClass` | `tl-stack` mit `tl-stack--<richtung>`, `--gap-<stufe>`, `--align-<wert>`, `--justify-<wert>`, `--wrap`, `--grow-first`; mit `fill` zusätzlich `tl-fill` (nimmt die Höhe des Behälters, unabhängig von den Kindern); mit `maxWidth` zusätzlich `tl-bounded`. |
| `TLGrid` | `columns`, `minColumnWidth`, `maxColumns`, `gap`, `maxWidth`, `itemClass` | `tl-grid tl-grid--gap-<stufe>`; die Spaltenvorlage inline; Abstand im TSX aus derselben Stufe |
| `TLInset` | `child` | `tl-inset`, in einem Panel `tl-inset--panel`, in einer Karte `tl-inset--card` (aus `useSurface()`) |
| `TLDeckPane` | `activeChild` | `tl-deck`: ohne eigene Box (`display: contents`), das gezeigte Kind steht im Behälter des Decks |
| begrenzte Region | — | `tl-region` an der Fläche, die ihren einen Inhalt hält: Split-Fläche (`tl-split__pane`), Reiterinhalt der TabBar (`tl-tabs__panel`), `tl-app-shell__content`, `tl-sidebar__content`, Körper von Fenster (`tl-dialog__body`) und Drawer (`tl-drawer__body`) |
| Hülle wiederholter Kinder | `itemClass` | `tl-item` und die konfigurierte Klasse; Position als `--item-index` (0-basiert) |
| Haken der ausgewählten Option | `selectable`, `selected` | `tl-item__check`: ein dekoratives `<span aria-hidden="true">` in der Hülle, nur bei `aria-selected="true"`; sitzt oben am Ende, fängt keinen Klick; die Auswahl bleibt allein in `aria-selected` |
| Auswahl unter den Einträgen | `selectable`, `itemKeys`, `selected`, `multiSelect` | der Behälter `role="listbox"` (mehrfach: `aria-multiselectable`), jede Hülle `role="option"` mit `aria-selected`; kein Modifier |

## Füllen

Ein Kind, das die übrige Höhe seines Behälters nehmen soll, trägt `tl-fill` (`FILL_CLASS` in `bridge/fill.ts`):
`flex: 1` und `min-height: 0`, damit es unter seinen Inhalt schrumpft und selbst scrollt. Ob ein Kind füllt, entscheidet
der Control-Baum (`useFill`, `useFillHost`): ein Behälter mit füllendem Kind füllt seinerseits, bis zur ersten Box, die
ohnehin begrenzt ist. Die Hülle eines wiederholten Kindes füllt mit, solange ihr Kind füllt
(`.tl-item:has(> .tl-fill)`). Die Box, die ohnehin begrenzt ist, trägt `tl-region` (eine Zelle aus
`minmax(0, 1fr)`): sie hält einen füllenden Inhalt auf genau ihrer Höhe und streckt einen Inhalt eigener Größe auf
ihre Höhe; ist er höher, scrollt die Region (ein Panel scrollt seinen Körper). Viewport und Mount-Punkt sind der Seitenrahmen der Engine (`tlReactBase.css`), nicht
Teil des Pakets.

## Aktion beim Zeigen

Ein Element, das erst beim Zeigen erscheint — Griff und Knöpfe der umsortierbaren Liste (`reorder-list.contract.md`),
der Öffner des Compact field —, trägt `tl-reveal-on-hover`. Es gibt diese eine Regel; kein Block verbirgt oder zeigt
eine Aktion selbst. Einzige Ergänzung: der Trichter im Tabellenkopf nimmt verborgen keine Breite
(`table.contract.md`); ob er zu sehen ist, sagt weiter `tl-reveal-on-hover`.

| Wann | Bild |
|---|---|
| sonst | `opacity: 0`, Übergang `--tl-duration-fast` mit `--tl-easing-standard` (zusammen mit Fläche, Rand und Farbe des Knopfs) |
| Zeigen auf den direkten Elternteil, auf die Tabellenzeile (`tl-table__row`) oder den Eintrag der Liste (`tl-reorder-list__item`) | `opacity: 1` |
| Tastaturfokus am Element, im direkten Elternteil oder im Eintrag der Liste (`:focus-visible`, nicht `:focus-within`) | `opacity: 1` |
| Der Knopf öffnet ein Popover, das offen ist (`aria-expanded="true"`) | `opacity: 1`: der Anker bleibt sichtbar, solange sein Popover steht, auch wenn der Zeiger darin ist |
| ohne Zeigen (`@media (hover: none)`) | immer `opacity: 1` |

`opacity`, nie `visibility`: ein unsichtbarer Knopf bleibt mit Tab erreichbar und erscheint, sobald er den Fokus hat.

## Aktivierbar

Ein Element, das der Nutzer öffnet, ist aktivierbar (#30569): eine Dashboard-Kachel mit Aktion (`dashboard.contract.md`)
und ein Eintrag eines Behälters mit `itemActions` (je Eintrag `label`, `disabled`, `tooltip`, `null` für einen
Eintrag, der sich nicht öffnen lässt). Die Bridge gibt Element und Knöpfe (`activationProps`, `ActivationButton`,
`ActivationOpenButton` in `bridge/activatable.tsx`); die Geste folgt daraus, ob der Behälter auswählbar ist.

| Behälter | Öffnen | Bild |
|---|---|---|
| nicht auswählbar | Klick auf den Eintrag (nicht auf bedienbaren Inhalt, nicht am Ende einer Textmarkierung); Enter, Leertaste am verborgenen Knopf | Hülle `tl-item tl-activatable`, darin zuerst der Knopf `button.tl-activatable__action.tl-visually-hidden` mit Name `label`; Tooltip an der Hülle |
| auswählbar | Doppelklick, Enter an der Option (der Server wählt zuerst aus); der Öffnen-Knopf, solange `activationButton` nicht `false` ist | Option wie oben; der Öffnen-Knopf (Part `Button` ghost, icon-only, Chevron, Name `label`) in `tl-overlay__action` in einer Ebene `tl-overlay__layer tl-overlay__layer--top-right`; der Haken `tl-item__check` steht in derselben Ebene davor |

Ein Eintrag mit deaktivierter Aktion zeigt das: der verborgene Knopf bzw. der Öffnen-Knopf ist `disabled`, die Hülle
bekommt keinen Zeiger und kein Zeigen; Enter an einer solchen Option wählt nur aus.

| Wann | Bild |
|---|---|
| Knopf `tl-activatable__action` nicht deaktiviert | Zeiger `pointer` an `tl-activatable` |
| Zeigen darauf | Rand `--tl-border-interactive` am Element und an der äußersten `tl-card` darin (nicht an einer Karte in einer Karte); kein Schatten |
| Tastaturfokus am verborgenen Knopf | Ring am Element (`.tl-activatable:has(> .tl-activatable__action:focus-visible)` in `base.css`) |

Leser der Auswahl auch `TLKanbanBoard` (`board.contract.md`): jede Karte ist ein `tl-item` mit `tl-board__card`,
`role="option"` und `aria-selected`, mit Haken `tl-item__check`; die Karten-Liste einer Spalte ist die Listbox.

## Aktionen

| Befehl | Wann |
|---|---|
| `selectItem` | Klick auf einen Eintrag (mit Strg/Cmd umschalten, mit Umschalt erweitern), Enter (ohne Aktivieren) oder Leertaste am Eintrag, langes Drücken (mehrfach); Argumente `key`, `toggle`, `range`. Ein Klick auf ein bedienbares Element im Eintrag gehört diesem |
| `activateItem` | Öffnen eines aktivierbaren Eintrags (siehe Aktivierbar); Argument `key` |

## Tokens

| Stufe | Token |
|---|---|
| `compact` | `--tl-space-sm` |
| `default` | `--tl-space-md` |
| `loose` | `--tl-space-lg` |
| Inset Seite / Panel / Karte | `--tl-space-lg` / `--tl-space-md` / `--tl-space-xs` `--tl-space-md` |
| Option beim Zeigen | Halo `--tl-space-2xs` in `--tl-surface-state-hover-strong` (der halo um Flächen; Marken lesen `-hover`) |
| Haken der Option | `--tl-surface-brand` (Kreis, `--tl-size-icon-md`, `--tl-radius-round`), `--tl-text-on-brand` (Haken, Rand `--tl-border-width-md`), Lage `--tl-space-xs` oben am Ende |
| Aktivierbar beim Zeigen | Rand `--tl-border-interactive` (wie die Kachel) |
| Verborgener Knopf | Abstand `--tl-space-sm` zur Kante des Elements |
| Haken neben dem Öffnen-Knopf | oben so weit eingerückt, dass er mittig neben dem Knopf steht: `--tl-space-xs` (Abstand von `tl-overlay__action`), `--tl-border-width-sm`, `--tl-space-2xs`, `--tl-size-control-sm` (Knopf), `--tl-size-icon-md` (Haken) |
| Option ausgewählt | Fläche `--tl-surface-selected` an der Hülle (eine `tl-card` darin erbt sie), Ring `--tl-border-width-md` in `--tl-border-selected` über dem Eintrag, Radius `--tl-radius-md`; der Haken bleibt |

## Konformität

Ein Behälter besteht den Vertrag, wenn er seinen Abstand aus der Skala oben nimmt und keine eigene Custom Property
im Namensraum `--tl` setzt; ein auswählbarer meldet die Auswahl über `aria-selected`, nicht über eine Klasse.
