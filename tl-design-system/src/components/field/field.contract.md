# Vertrag: Feld

Eine Kundenkomponente, die unter einem der TopLogic-Namen `TLTextInput`, `TLPasswordInput`,
`TLNumberInput`, `TLDatePicker` oder `TLSelect` registriert ist (Stufe C der Spec), bekommt vom
Server den Zustand unten, löst die genannten Aktionen aus und trägt die genannten Attribute.
TopLogic garantiert dann Funktion, Tastatur und Fokus — nicht das Aussehen. Wer ins Bild passen
will, liest die Tokens. Alle Felder teilen einen Rahmen, `tl-field`; die Wurzel von
`TLDropdownSelect` trägt ihn ebenso.

## Zustand

Vom Server über `useTLState()`, gemeinsam aus `ReactFormFieldControl`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `value` | je Control | Der Wert des Feldes. |
| `placeholder` | string, optional | Platzhalter, solange das Feld leer ist (nur beim Bearbeiten). |
| `icon` | string, optional | Icon im Feld, vor dem Getippten: kodiertes ThemeImage (`ThemeIcon` in `tl-icon-sm`) im `tl-field-group__icon`. |
| `clearable` | boolean, optional | Das Feld bietet einen Knopf, der es leert: die Aktion der Gruppe, ein `tl-button--icon`. |
| `debounceMs` | number, optional | Wie lange ein getippter Wert zurückgehalten wird, bevor er gesendet wird, in Millisekunden. |
| `multiline` | boolean, optional | Das Textfeld ist ein mehrzeiliges `textarea`, Klasse `tl-field--multiline`. |
| `rows` | number, optional | Sichtbare Zeilen des mehrzeiligen Feldes. |
| `editable` | boolean | Ob das Feld bearbeitbar ist. `false`: kein Eingabefeld, sondern der Lesewert `tl-field-value`. |
| `disabled` | boolean | Inaktiv, wie beim Button (`button.contract.md`): das Feld rendert `disabled` (siehe Attribute). |
| `mandatory` | boolean | Pflichtfeld: `aria-required="true"`. |
| `nullable` | boolean | Ob `null` ein gültiger Wert des Feldes ist. |
| `hasError` | boolean | Das Feld hat einen Validierungsfehler: `aria-invalid="true"`. |
| `errorMessage` | string, optional | Text des Fehlers. |
| `hasWarnings` | boolean | Das Feld hat Validierungswarnungen: `data-tl-state="warning"`, nur ohne Fehler. |
| `label` | string | Beschriftung des Feldes. |
| `tooltip` | string, optional | Tooltip-Text. |
| `hidden` | boolean | Das Control ist auf dem Client verborgen: es bleibt im DOM, aber nicht zu sehen (wie `visible` in `form.contract.md`) — die Brücke blendet seinen Einhängepunkt aus, das Control bleibt montiert und behält seinen Zustand. |
| `submitOnEnter` | boolean, optional | Enter im Feld sendet `submit`. |
| `commitOnBlur` | boolean, optional | Verlässt der Fokus das Feld, sendet es `commit`. |
| `sendValueOnBlur` | boolean, optional | Der getippte Wert wird bis zum Verlassen des Feldes zurückgehalten, statt schon beim Tippen gesendet zu werden. |

Zusätze je Control:

| Control | Schlüssel | Bedeutung |
|---|---|---|
| `TLTextInput`, `TLPasswordInput` | `inputType` | Welche Art Wert das Feld bearbeitet: das HTML-`type` des `input`. Fehlt er, gilt `text`. |
| `TLDatePicker` | `inputType` | Welches HTML-`input` der Client rendert. |
| `TLDatePicker` | `displayValue` | Der Wert als lokalisierter Text für die Leseansicht. |
| `TLNumberInput` | `inputMode` | Welche Bildschirmtastatur das Feld anfordert: `numeric`, `decimal` oder `text`. |
| `TLSelect` | `options` | Die Auswahlmöglichkeiten, je ein Eintrag mit `value` und `label`. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Eingabe | `sendCommand('valueChanged', { value })`, entprellt um `debounceMs`; bei `sendValueOnBlur: true` erst beim Verlassen des Feldes. |
| Verlassen des Feldes (Blur) | `sendCommand('commit')`, wenn `commitOnBlur: true`. |
| Enter | `sendCommand('submit')`, wenn `submitOnEnter: true`. |

## Tokens

| Teil | Token |
|---|---|
| Höhe | `--tl-size-control` (folgt der Dichte), als `min-height`; einzeilig ohne Innenabstand oben und unten, der Text steht in der Mitte |
| Innenabstand | seitlich `--tl-space-control-x`; oben und unten `--tl-space-control-y` nur mehrzeilig (`tl-field--multiline`) |
| Ecke | `--tl-radius-sm` |
| Fläche | `--tl-surface-field` |
| Rand | `--tl-border-control`, Breite `--tl-border-width-sm` |
| Rand beim Zeigen | `--tl-border-emphasis` |
| Text / Platzhalter | `--tl-text-primary` / `--tl-text-placeholder` |
| Fehler | Rand `--tl-status-error-border` |
| Warnung | Rand `--tl-status-warning-border` |
| deaktiviert (vom Kontrast-Gate ausgenommen) | Fläche `--tl-surface-disabled`, Text `--tl-text-disabled`, Rand `--tl-border-disabled` |
| Icon | Farbe `--tl-icon-secondary`, Abstand zum Rand `--tl-space-control-x` |
| Endknopf | ein `tl-button--icon` mit `--sm` und `--ghost` (`button.contract.md`): Farben und Deaktiviert von dort; Abstand zum Rand `--tl-space-2xs`, senkrecht in der Mitte. Als Knopf im Feld zeigt er `--tl-surface-interactive-selected` und drückt `--tl-surface-interactive-active` — die Ghost-Stufe `--tl-surface-interactive-hover` ist auf `--tl-surface-field` hell nicht zu sehen (`docs/skalen.md` „Knöpfe im Feld zeigen eine Stufe kräftiger“) |
| Kalenderknopf (Datum, Zeit, Zeitpunkt) | das native `::-webkit-calendar-picker-indicator` als Knopf im Feld: Quadrat `--tl-size-control-sm`, Ecke `--tl-radius-sm`, das native Icon in `--tl-size-icon-sm` mittig, Abstand zum Text `--tl-space-xs`, zum Rand `--tl-space-2xs`; Zeigen `--tl-surface-interactive-selected`, Drücken `--tl-surface-interactive-active`, Übergang `--tl-duration-fast` `--tl-easing-standard`; deaktiviert ohne Fläche. Das Icon zeichnet der Browser und folgt `color-scheme` aus `tokens.css`. Nur Chromium und WebKit; Firefox zeichnet einen eigenen Knopf, den kein Stylesheet erreicht |
| reservierter Platz im Feld | Icon: `--tl-space-control-x` + `--tl-size-icon-sm` + `--tl-space-xs`. Endknopf: `--tl-space-2xs` + `--tl-size-control-sm` (der Knopf) + eine Lücke `--tl-space-xs` |
| Lesewert | Text `--tl-text-primary`, Abstand `--tl-space-xs` |
| Lesewert mit Aktion | Lücke zwischen Wert und Aktion `--tl-space-xs` |
| Lesewert als Link | `--tl-text-link`, `--tl-text-link-hover`; unterstrichen |

Schrift: Klasse `.tl-type-body` am Feld, von außen gesetzt; `tl-field` hat keine Schriftregel.
Rand-Rollen halten am Gate 3,0 auf `surface-field` und jeder umgebenden Fläche (`surface-base`,
`surface-layer`, `surface-layer-nested`, `surface-overlay`).

## Attribute

| Attribut | Wann |
|---|---|
| `aria-invalid="true"` | `hasError`. Das CSS liest es selbst — Screenreader und Bild haben eine Quelle. Wo ARIA es an der Rolle nicht erlaubt: `data-tl-state="error"`. |
| `data-tl-state="warning"` | `hasWarnings && !hasError`. Der Fehler verdrängt die Warnung. ARIA kennt keine Warnung. |
| `disabled` | `state.disabled`, wie beim Button: das native `disabled` am `input`, `textarea` oder `select`, ohne Löschknopf; die Regel `.tl-field:disabled` liest es. `editable: false` ist **kein** Disabled: es rendert den Lesewert `tl-field-value`. |
| `aria-required="true"` | `mandatory`. |
| `placeholder` | `placeholder`, wenn gesetzt. |
| `:focus-visible` | den Ring liefert `base.css`; das Feld setzt und entfernt keinen Fokusstil. |

Bei `editable: false` rendert kein Eingabefeld, sondern der Lesewert: ein `span` (als Link ein `a`)
mit `tl-field-value`, der Text darin in `tl-field-value__text`. Ein mehrzeiliger Text rendert ein
`div` mit `tl-field-value tl-field-value--multiline` und je Zeile ein `tl-field-value__text`; eine
leere Zeile behält ihre Höhe. Mehrzeilig bricht jede Zeile um, statt mit Ellipse zu enden; in der
Tabellenzelle stellt die Engine die Zeilen in eine Reihe ohne Umbruch. Ein Link-Lesewert ist unterstrichen — Farbe allein kennzeichnet keinen
Link.

**Schrift vom Ort (09.10., Hendrik).** Der Lesewert setzt keine Typo-Klasse; er erbt sie von dem Ort, an dem er
steht: `tl-form-field__input` und der Tabellenkörper tragen `tl-type-body`, der Baum ebenso, eine Gruppenzeile
`tl-type-body-strong`. So nimmt ein Lesewert die Schrift seines Ortes an, ohne dass jeder Ort sie ihm einzeln
durchreicht. Ein Eingabefeld (`tl-field`) setzt seine Klasse weiter selbst: Eingaben erben die Schrift nicht.

Jeden Lesewert — im Formular, im Select und in der Tabellenzelle — zeichnet ein Renderer (`FieldValue`):
äußerer Link `href` (neuer Tab), innerer Link `onGoto` (Kommando `goto`), Icon über `ThemeIcon`, Tönung als
`tl-pill` mit `data-tl-role`.

Die Aktion in der Gruppe ist ein `tl-button--icon --sm --ghost` (`tl-button tl-button--ghost tl-button--sm tl-button--icon`),
unmittelbares Kind von `tl-field-group`: `button type="button"` (Löschen) oder `a` (Öffnen), mit
`aria-label`. Die Feldgruppe hat **einen** Endplatz: solange Text steht und `clearable` gilt, ersetzt der
Löschknopf den Öffnen-Link. Ein Suchfeld (`type="search"`) mit Löschknopf (`button`) zeigt das Löschen des
Browsers nicht daneben; ein Öffnen-Link lässt es stehen. Icon und Endknopf ändern nur die Klasse der Gruppe, nicht die Lage des
Feldes im DOM, damit das Feld beim Tippen den Fokus behält.

Klassen:

| Klasse | Element |
|---|---|
| `tl-field` | das echte `input`, `textarea` oder `select`; die Wurzel von `TLDropdownSelect` |
| `tl-field--multiline` | das `textarea` bei `multiline` |
| `tl-field--preformatted` | das `textarea` eines vorformatierten Textes (mit `tl-type-code`): keine Zeile bricht um, eine breite rollt waagrecht |
| `tl-field-group` | `span` um Feld mit Icon oder Endknopf (`tl-button--icon`); das Feld darin trägt Rahmen und Fokusring |
| `tl-field-group__icon` | `span` mit dem Icon vor dem Wert |
| `tl-field-value` | der Lesewert bei `editable: false` |
| `tl-field-value--multiline` | der mehrzeilige Lesewert (`div`): die Zeilen untereinander |
| `tl-field-value--preformatted` | zum mehrzeiligen Lesewert eines vorformatierten Textes (mit `tl-type-code`): die Zeilen brechen nicht um, der Wert rollt waagrecht |
| `tl-field-value__text` | der Text im Lesewert, endet mit Ellipse; im mehrzeiligen eine Zeile |
| `tl-field-value-group` | Raster um einen mehrzeiligen Lesewert und seine Aktion: die Aktion oben am Ende in einer eigenen Spalte |
| `tl-field-value-group__action` | die Aktion neben dem Wert, ohne Hover sichtbar |
| `tl-copy-button` | der Kopierknopf eines vorformatierten Lesewerts |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jeden Zustand
oben das jeweilige Attribut trägt, der Fehler die Warnung verdrängt, bei `editable: false` kein
`input` rendert und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt. Der Test dazu folgt
mit der ersten Kundenkomponente; bis dahin gilt dieser Text.
