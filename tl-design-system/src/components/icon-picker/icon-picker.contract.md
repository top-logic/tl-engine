# Vertrag: Icon-Picker

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLIconSelect` registriert ist (Stufe C der Spec), zeigt ein Icon
als Feldknopf und öffnet ein Popup mit Filter und Raster. Feldknopf ist `tl-swatch`, die Hülle `tl-picker`; Laden, leer
und Fehler sind `tl-status`.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `value` | string \| null | Das kodierte Icon; `null` zeigt das Wahl-Icon, der Feldknopf trägt `data-tl-state~="empty"`. |
| `editable`, `disabled` | boolean | Lesend `span`, deaktiviert `button` mit `disabled`; beide öffnen kein Popup. |
| `hasError`, `hasWarnings`, `mandatory` | boolean | Feldzustand wie bei jedem Feld. |

## Aktionen

| Befehl | Wann |
|---|---|
| `loadIcons` | Popup öffnet, Filter ändert sich, „Erneut laden“ nach einem Fehler: fordert die Treffer an. |
| `valueChanged` | Einfach: Wahl einer Zelle sofort; Erweitert: OK. Leeren sendet `null`. |

## Attribute

| Attribut | Wo |
|---|---|
| `role="listbox"` mit `aria-label` | `tl-icon-picker__grid` |
| `role="option"` mit `aria-selected` und `aria-label` | `tl-icon-picker__cell`; genau eine Zelle trägt `tabindex="0"` (`useRovingFocus`: Pfeile, Pos1, Ende; Enter und Leertaste wählen) |

## Klassen und Tokens

| Klasse | Teil |
|---|---|
| `tl-icon-picker` | Popup an `tl-picker`: `--tl-size-13` im Quadrat, `resize: both` |
| `tl-icon-picker__filter` | Filterfeld (Part `TextInput`) |
| `tl-icon-picker__grid` | Raster, Spalten aus `--tl-size-control` |
| `tl-icon-picker__cell` | Zelle `--tl-size-control`; gewählt in `--tl-surface-interactive-selected` |
| `tl-icon-picker__advanced` | Reiter „Erweitert“: Klassenfeld und Vorschau |
| `tl-icon-picker__preview` | Vorschau des Icons |

Laden, keine Treffer und Fehler stehen als `tl-status` an der Stelle des Rasters (ohne Treffer gibt es kein `listbox`); der Fehler nennt „Erneut laden“ als Part `Button`.

## Konformität

Ein Icon-Picker besteht den Vertrag, wenn das Raster ein `listbox` mit genau einem Tab-Halt ist, Auswahl `aria-selected`
trägt und Zustände Attribute sind.
