# Vertrag: Kalender

Termine in Tag, Arbeitswoche, Woche, Monat und Jahr (Spec 5c §3, V8, V24–V27); Leser ist `TLCalendar`
(`CalendarViewControl`). Ein Block `tl-calendar` für alle Ansichten. TopLogic garantiert Funktion, Tastatur, Fokus und
Ansage, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `granularity` | `DAY` \| `WORK_WEEK` \| `WEEK` \| `MONTH` \| `YEAR` | Die Ansicht; die gewählte trägt in der Ansichtswahl `aria-checked="true"`. |
| `title` | string | Der Titel des Zeitraums in `tl-calendar__title`. |
| `rangeStart`, `rangeEnd`, `anchor` | number | Der gezeigte Zeitraum und sein Bezugstag (ms). |
| `now` | number | Jetzt (ms): heute und die Jetzt-Linie. |
| `firstDayOfWeek` | number | Erster Wochentag (0 = Sonntag). |
| `dayStartHour`, `dayEndHour` | number | Die Arbeitszeit (`tl-calendar__work`). |
| `nonWorkingDays` | Liste | Arbeitsfreie Wochentage: `data-tl-state~="nonworking"`. |
| `locale` | string | Sprache für Datum, Uhrzeit und die Namen der Tage. |
| `editable` | boolean | Termine lassen sich anlegen, verschieben und in der Länge ändern (Zeiger). |
| `events[]` | Liste | je Termin `id`, `start`, `end`, `allDay`, `title`, `tooltip`, `category`, `color`, `selected`, `movable`, `resizable`. |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `navigate` | Heute, zurück, vor in der Leiste (`direction`: `TODAY`, `PREV`, `NEXT`) |
| `switchGranularity` | Wahl einer Ansicht im `tl-segmented` |
| `selectEvent` | Klick auf einen Termin; Enter oder Leertaste am fokussierten Termin |
| `goto` | Tageskopf, Tageszahl im Monat, „+n weitere“, Tag des Mini-Monats (`DAY`); Titel des Mini-Monats (`MONTH`) |
| `moveEvent`, `resizeEvent` | Ziehen eines Termins bzw. seines Griffs (nur Zeiger) |
| `createSlot` | Enter im Feld des Platzhalters nach Ziehen über freie Zeit oder Klick auf einen Tag (nur Zeiger); ebenso ein Druck daneben, wenn ein Titel eingegeben ist (ohne Titel verschwindet der Platzhalter). Ein Druck auf den Platzhalter selbst behält ihn und den Fokus im Feld (23.10.) |

## Tastatur

| Taste | Wirkung |
|---|---|
| `Tab` | Leiste (Knöpfe, Ansichtswahl), dann je Raster ein Halt: Tagesköpfe, eine Termin-Liste je Raster (ein Tab-Halt), Tageszahlen |
| Pfeiltasten in einer Termin-Liste | nächster, vorheriger Termin in der Reihenfolge der Zeit (`useRovingFocus`, im Zeitraster `linear`) |
| `Enter`, Leertaste am Termin | wählt ihn (`selectEvent`) |
| Pfeiltasten im Mini-Monat | nächster Tag, Woche darüber und darunter; der Mini-Monat ist ein Tab-Halt |
| `Enter` an einem Tag | öffnet ihn (`goto`) |
| Pfeiltasten in der Ansichtswahl | wie `tl-segmented` (`choice.contract.md`) |
| `Enter` im Anlegen-Feld | legt den Termin an (`createSlot`) |
| `Escape` im Anlegen-Feld | verwirft nur den Platzhalter, über `useStandaloneKeyboardScope` (der Dispatcher); ein umgebendes Fenster bleibt offen |

Verschieben, Länge ändern und Anlegen sind Zeiger-Bedienung, nicht Tastatur (Spec 5c §1).

## Semantik

| Element | Rolle und Attribute |
|---|---|
| Leiste | `tl-toolbar` mit `tl-calendar__toolbar`, `role="toolbar"`, Name `js.calendar.toolbar`; Heute, zurück, vor als Part `Button` (`toolbar.contract.md`) |
| Ansichtswahl | `tl-segmented` mit `role="radiogroup"`, Name `js.calendar.view`; je Ansicht `tl-segmented__segment` mit `aria-checked` |
| Termin-Liste | `tl-calendar__events` mit `role="listbox"`, Name `js.calendar.events`; eine je Raster (Ganztagszeile, Zeitraster, Monat) |
| Termin | `tl-calendar__event` bzw. im Monat mit Uhrzeit `tl-calendar__chip`, `role="option"`, `aria-selected`; Name: Zeit und Titel, ganztägig Titel und `js.calendar.allDay` |
| Tageskopf, Tageszahl im Monat, Titel und Tag des Mini-Monats | `button`, Name: das volle Datum (Intl, `locale`) bzw. der Monat |
| „+n weitere“ | `button`, Name: der sichtbare Text, dann „, “ und das volle Datum (der Name beginnt mit dem, was zu sehen ist) |
| Heute | `aria-current="date"` am Tageskopf, an der Tageszelle des Monats, am Tag des Mini-Monats |
| Anlegen-Feld | `input` mit `tl-field` und `tl-calendar__create` im Platzhalter-Termin; hat nach dem Öffnen den Fokus |

## Klassen

| Klasse | Element |
|---|---|
| `tl-calendar` | die Wurzel, füllt ihren Behälter |
| `tl-calendar__toolbar` | am `tl-toolbar` der Leiste |
| `tl-calendar__title` | der Titel, mit `tl-type-heading-sm`; endet mit Ellipse |
| `tl-calendar__body` | der Bereich unter der Leiste |
| `tl-calendar__events` | die Termin-Liste; nur Semantik, keine Box (`display: contents`) — sie umschließt Spalten, Zellen bzw. Wochen |
| `tl-calendar__time-grid` | das Zeitraster (Tag, Arbeitswoche, Woche) |
| `tl-calendar__gutter` | der Platz der Stundenspalte in Kopf und Ganztagszeile, `--tl-size-9` |
| `tl-calendar__head` | die Zeile der Tagesköpfe |
| `tl-calendar__day-head` | Tageskopf, `button` |
| `tl-calendar__weekday` | Wochentag im Tageskopf, `tl-type-label`, Großbuchstaben |
| `tl-calendar__day-number` | Tageszahl, `tl-type-heading-sm`; im Tageskopf `span`, im Monat `button` |
| `tl-calendar__all-day` | die Ganztagszeile |
| `tl-calendar__all-day-label` | „ganztägig“ in der Stundenspalte, mit `tl-calendar__gutter`, `tl-type-label` |
| `tl-calendar__all-day-cell` | Zelle eines Tages in der Ganztagszeile |
| `tl-calendar__scroll` | der Rollbereich der Stunden |
| `tl-calendar__time-body` | Stundenspalte und Tage; Höhe inline (24 × Stundenhöhe) |
| `tl-calendar__hours` | die Stundenspalte |
| `tl-calendar__hour` | Beschriftung einer Stunde, `tl-type-label`; Lage inline |
| `tl-calendar__columns` | die Tagesspalten; Spaltenzahl inline |
| `tl-calendar__column` | eine Tagesspalte |
| `tl-calendar__work` | die Arbeitszeit in einer Spalte; Lage inline |
| `tl-calendar__hour-line` | Stundenlinie; Lage inline |
| `tl-calendar__now` | die Jetzt-Linie; Lage inline |
| `tl-calendar__event` | ein gefüllter Termin: im Zeitraster (absolut, Lage und Größe inline), in der Ganztagszeile, als Balken im Monat; `tl-type-label` |
| `tl-calendar__event-time` | die Zeit im Termin bzw. im Chip, `tl-type-label` |
| `tl-calendar__event-title` | der Titel im gefüllten Termin `tl-type-label-strong`, im Chip `tl-type-label` (der Chip ist nicht gefüllt, der Punkt trägt die Farbe); endet mit Ellipse |
| `tl-calendar__resize` | Griff zum Längenändern am unteren Rand (`--tl-space-xs` hoch) |
| `tl-calendar__create` | das Feld im Platzhalter beim Anlegen (mit `tl-field`), randlos auf der Fläche |
| `tl-calendar__month` | der Monat |
| `tl-calendar__month-head` | die Zeile der Wochentage |
| `tl-calendar__month-weekday` | ein Wochentag, `tl-type-label`, Großbuchstaben |
| `tl-calendar__month-body` | die Wochen |
| `tl-calendar__week` | eine Woche |
| `tl-calendar__days` | die untere Schicht einer Woche: sieben Tageszellen |
| `tl-calendar__day` | eine Tageszelle des Monats |
| `tl-calendar__overlay` | die obere Schicht einer Woche: Balken und Chips, Zeile und Spalte inline |
| `tl-calendar__chip` | Termin mit Uhrzeit im Monat, nicht gefüllt, `tl-type-label` |
| `tl-calendar__chip-dot` | der Punkt in der Farbe des Termins |
| `tl-calendar__more` | „+n weitere“, `button`, `tl-type-label` |
| `tl-calendar__year` | das Jahr: Raster der Mini-Monate |
| `tl-calendar__mini` | ein Mini-Monat |
| `tl-calendar__mini-title` | sein Name, `button`, `tl-type-label-strong` |
| `tl-calendar__mini-grid` | sein Raster, ein Tab-Halt; `role="group"` mit dem Monat als Name (ein Name braucht eine Rolle) |
| `tl-calendar__mini-weekday` | Wochentag, `tl-type-label` |
| `tl-calendar__mini-day` | Tag, `button`, `tl-type-label`; Termintag `tl-type-label-strong` |

Die Schrift setzen die Typo-Klassen im TSX; `calendar.css` setzt keine.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-current="date"` | heute: Tageskopf, Tageszelle des Monats, Tag des Mini-Monats |
| `aria-selected="true"` | der gewählte Termin (`selected`) |
| `data-tl-role="category-N"` | Termin und Punkt einer Kategorie, `N` = `categoryOf(category)` (1–8); ohne Kategorie kein Attribut |
| `data-tl-state~="nonworking"` | arbeitsfreier Tag: Tageskopf, Spalte, Tageszelle, Tag des Mini-Monats |
| `data-tl-state~="other"` | Tag eines anderen Monats (Monat, Mini-Monat) |
| `data-tl-state~="event"` | Tag des Mini-Monats mit Termin |
| `data-tl-state~="preview"` | die Vorschau am Zielort und der Platzhalter beim Anlegen |
| `data-tl-state~="dragging"`, `~="drop-onto"` | der gezogene Termin bzw. die Tageszelle unter ihm im Monat; gezeichnet von `drag.css` (`drag.contract.md`) |
| `hidden` | verbirgt den Kalender bzw. einen Teil |

## Farbe eines Termins

| Fall | Fläche | Schrift |
|---|---|---|
| ohne Kategorie, ohne Serverfarbe | `--tl-surface-brand` | `--tl-text-on-brand` |
| Kategorie `N` | `--tl-category-N-surface` | `--tl-text-on-category-N` (gerechnet) |
| Serverfarbe `color` | inline, die Datenfarbe | inline, `textOn(color)`: `--tl-text-contrast-light` oder `--tl-text-contrast-dark`; ist die Farbe nicht lesbar, gilt die Kategorie bzw. die Marke |

Der Punkt eines Chips liest dieselbe Fläche, ohne Schrift. Die Regeln je Kategorie schreibt der Generator
(`@tl-roles: category-surface` am Termin, `category-fill` am Punkt). Keine Custom Property im Namensraum `--tl`
außerhalb des Pakets.

## Tokens

| Teil | Token |
|---|---|
| Wurzel | `--tl-surface-layer`, `--tl-text-primary` |
| Ränder, Stundenlinien | `--tl-border-width-sm` in `--tl-border-separator` |
| Tagesspalte, arbeitsfreier Tag | `--tl-surface-base` |
| Arbeitszeit | `--tl-surface-layer` |
| Jetzt-Linie | `--tl-border-width-md` in `--tl-status-error-border`, Punkt `--tl-size-2` |
| Wochentag, Stunde, anderer Monat, Zeit im Chip | `--tl-text-secondary` |
| Termin | Ecke `--tl-radius-sm`, Innenabstand `--tl-space-2xs` `--tl-space-xs` |
| Ausgewählt | Ring innen `--tl-border-width-md` auf `::after`, wie `tl-item`: am Termin in der Schriftfarbe (`currentColor`, hält gegen jede Füllung), am Chip in `--tl-border-interactive` |
| Vorschau | `--tl-surface-brand` mit Deckkraft 0,4 |
| Heute | Kreis `--tl-size-icon-lg`, `--tl-radius-full`, `--tl-surface-brand`, `--tl-text-on-brand` |
| Termintag im Mini-Monat | Punkt `--tl-size-1` in `--tl-surface-brand` (heute: `--tl-text-on-brand`) |
| Zeigen | `--tl-surface-interactive-hover` (Tageskopf, Chip, Tag des Mini-Monats) |
| „+n weitere“ | `--tl-text-link`, gezeigt `--tl-text-link-hover` |
| Stundenspalte | `--tl-size-9` |
| Ganztagszeile | mindestens `--tl-size-6`, höchstens `--tl-size-10` |
| Monat | Schicht der Termine ab `--tl-size-6`, Zeilen `--tl-size-5`, Lücke `--tl-space-2xs` |
| Jahr | Mini-Monat mindestens `--tl-size-12`, Lücke und Rand `--tl-space-md`; Mini-Monat `--tl-radius-md`, `--tl-space-xs` |
| Stapel | `--tl-layer-raised` für Jetzt-Linie, gewählten, fokussierten Termin und Vorschau; sonst Quellreihenfolge in der Spalte: `tl-calendar__work`, `tl-calendar__hour-line`, Termine |

Den Fokusring liefert `base.css`. Was in einem schneidenden Behälter bündig liegt — Tageskopf, Tageszahl, Termin,
Chip, „+n weitere“, Anlegen-Feld — trägt ihn innen (`outline-offset` −`--tl-focus-offset`).

## Konformität

Ein Kalender besteht den Vertrag, wenn heute, arbeitsfrei, anderer Monat, Termintag, Vorschau und Auswahl Attribute
sind, die Termine eines Rasters eine Liste mit einem Tab-Halt bilden, Tage Knöpfe mit dem vollen Datum als Namen
sind, die Kategorie über `data-tl-role` und `categoryOf` kommt, eine Serverfarbe ihre Schrift über `textOn` bekommt
und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
