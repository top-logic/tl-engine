# Vertrag: Filter-Werteliste (Spaltenfilter)

Das Popover am Trichter einer Tabellenspalte, deren Filter eine Auswahl aus Werten ist. Es ist nicht modal, wirkt sofort
(jede Zeile ändert den Filter) und liegt auf `tl-popover tl-picker` (`picker.contract.md`); jede Zeile ist ein Eintrag des
Menüs (`menu.contract.md`). Der Fokus ist echt (Roving-Tabindex) wie im Menü.

Jedes Filter-Popover am Trichter trägt die Wurzel `tl-filter-list`, auch das eines Filters, der ein Formular ist (Text,
Bereich, Ja/Nein, die Filter-UI einer Anwendung): die Tabelle zeichnet das Popover und kennt die Art des Filters nicht. Der
Inhalt unterscheidet sich — Werteliste oder Formular (`tl-filter-list__form`, Abschnitt „Formular“) —, der Fuß ist derselbe.

## Aufbau

```
div.tl-popover.tl-picker.tl-filter-list   role="dialog" aria-label="Filter <Spalte>"
  span.tl-field-group                      die Suche, nur ab acht Werten (steht fest)
  div.tl-picker__scroll [role="menu"]      der Rollbereich
    button.tl-menu__item.tl-menu__item--check.tl-filter-list__all   „Alle“
    button.tl-menu__item.tl-menu__item--check                       je Wert
  div.tl-filter-list__footer               zwei Aktionen (steht fest)
```

Passt bei einer Suche kein Wert, steht an Stelle des Menüs eine Zeile `div.tl-status.tl-type-body` mit `role="status"`
(„Kein Wert passt“, `status.contract.md`); „Alle“ entfällt, solange gesucht wird.

| Klasse | Wann |
|---|---|
| `tl-filter-list` | die Wurzel des Popovers, zusammen mit `tl-popover tl-picker`; `role="dialog"` mit `aria-label` |
| `tl-picker__scroll` | der Rollbereich um „Alle“ und die Werte (`picker.contract.md`); Suche davor und Fuß dahinter bleiben stehen |
| `tl-menu__item`, `tl-menu__item--check` | jede Zeile: ein `<button type="button">` mit `role="menuitemcheckbox"`, `aria-checked` und `tl-type-body`; darin ein gezeichnetes `<span class="tl-checkbox" aria-hidden="true">` ohne Eingabe, `tl-menu__label`, `tl-filter-list__count`. Rolle `menuitemcheckbox` |
| `tl-filter-list__all` | die Zeile „Alle“, zusätzlich zu den Klassen der Zeile: steht über den Werten, durch eine Linie getrennt; `aria-checked="mixed"`, solange nur ein Teil der Werte gewählt ist |
| `tl-filter-list__count` | die Anzahl der Treffer je Wert, rechts am Zeilenende, mit `tl-type-label` und `tl-nums` (das Ziffernbild setzt das Element, `base.css`) |
| `tl-filter-list__footer` | der Fuß mit zwei Aktionen („Filter entfernen“, „Auswahl umkehren“), unter einer Linie; „Filter entfernen“ ist `disabled`, solange die Spalte nicht gefiltert ist. Die Aktionen sind Knöpfe des Part `Button` wie jeder andere (`button.contract.md`): `tl-button tl-button--ghost` (die Hauptaktion „Anwenden“ `tl-button--primary`) in `tl-type-body`, die Beschriftung in `tl-button__label`; eine Komponentenbibliothek zeichnet sie um |
| `tl-filter-list__form` | der Rollbereich eines Formular-Filters, zusammen mit `tl-picker__scroll` (Abschnitt „Formular“) |
| `tl-status` | an Stelle des Menüs, wenn bei einer Suche kein Wert passt (`role="status"`) |

Das Kästchen ist das der Auswahl (`tl-checkbox`, `choice.contract.md`), gezeichnet nach `aria-checked` der Zeile; es nimmt keinen
Zeiger an, die ganze Zeile klickt.

## Formular

Ist der Filter einer Spalte keine Auswahl aus Werten, steht in demselben Popover sein Formular:

```
div.tl-popover.tl-picker.tl-filter-list         role="dialog" aria-label="Filter <Spalte>"
  div.tl-picker__scroll.tl-filter-list__form    das Formular (tl-form-layout), rollt
  div.tl-filter-list__footer                    „Filter entfernen“ [, „Anwenden“]
```

- Der Rollbereich hat den Rand `--tl-space-2xs`: der Fokusring eines Felds an der Kante liegt im overflow des Rollbereichs,
  wie im Reiterfeld des Pickers.
- Felder der eingebauten Filter wirken von selbst (Text nach einer Tipp-Pause oder mit `Enter`, Auswahl und Kästchen
  sofort). Die Filter-UI einer Anwendung wirkt erst mit „Anwenden“ (`tl-button`, `tl-button--primary` im Fuß, auch per `Enter`
  in einem einzeiligen Feld); die Hauptaktion steht am Ende des Fußes (`margin-inline-start: auto`), „Filter entfernen“
  davor.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-checked` | an jeder Zeile: `true` (Wert gewählt), `false`, an „Alle“ auch `mixed` |
| `data-tl-state~="empty"` | an der Zeile eines Werts ohne Treffer: leiser (`--tl-text-helper`), bleibt wählbar. Kein Modifier, kein `disabled` |

## Tastatur

Wie im Menü (`menu.contract.md`): Pfeile, `Home`, `End` wandern, `Enter` und Leertaste schalten die Zeile um, `Escape` schließt das
Popover und gibt den Fokus an den Trichter zurück; der Filter bleibt. Der Trichter trägt `aria-expanded` (`true`, solange das Popover
offen ist) und bleibt dann sichtbar (`layout.contract.md`).

## Tokens

| Teil | Token |
|---|---|
| Zeile, Zeigen, Drücken, Fokusring | die des Menüeintrags (`menu.contract.md`) |
| Kästchen | die der Auswahl (`choice.contract.md`) |
| Anzahl, Wert ohne Treffer | `--tl-text-helper` |
| Linie unter „Alle“, Linie über dem Fuß | `--tl-border-separator`, Breite `--tl-border-width-sm` |
| Abstände | Lücke der Zeilen `--tl-space-2xs`, Lücke im Fuß `--tl-space-xs`, Rand des Formulars `--tl-space-2xs` |

## Konformität

Eine Werteliste besteht den Vertrag, wenn jede Zeile ein `<button>` mit `role="menuitemcheckbox"` und `aria-checked` ist, ihr Kästchen
gezeichnet ist (kein `<input>`, kein `tl-choice` in der Zeile), Anzahl und leiser Wert über `tl-filter-list__count` und
`data-tl-state~="empty"` ausgedrückt sind, das Popover `role="dialog"` mit Namen trägt, `Escape` es schließt und im Klassen-Gate keine
Klasse ohne `tl-`-Präfix hinterlässt.
