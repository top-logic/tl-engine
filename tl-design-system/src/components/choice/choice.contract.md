# Vertrag: Auswahl

Eine Kundenkomponente, die unter einem der TopLogic-Namen `TLCheckbox`, `TLChoiceGroup`,
`TLSegmentedChoice` oder `TLOptionChips` registriert ist (Stufe C der Spec), bekommt vom Server den
Zustand unten, löst die genannten Aktionen aus und trägt die genannten Attribute. TopLogic garantiert
dann Funktion, Tastatur und Fokus — nicht das Aussehen. Wer ins Bild passen will, liest die Tokens.
Checkbox, Radio und Schalter zeichnen sich selbst (`appearance: none`); gewählt ist, was das Attribut
sagt, nie eine Klasse.

## Zustand

Vom Server über `useTLState()`. Die Basisschlüssel aus `ReactFormFieldControl` — `value`, `editable`,
`mandatory`, `hasError`, `hasWarnings`, `label`, `hidden` und die übrigen — stehen im Vertrag des
Feldes (`field.contract.md`, Abschnitt Zustand) und gelten hier unverändert. Zusätze je Control:

| Control | Schlüssel | Typ | Bedeutung |
|---|---|---|---|
| `TLCheckbox` (`ReactCheckboxControl`) | `value` | boolean oder `null` | Angekreuzt, nicht angekreuzt; `null` nur bei `triState`: kein Wert. |
| `TLCheckbox` | `triState` | boolean, optional | Das Kästchen hat einen dritten Zustand „kein Wert“: gemischt dargestellt, Klick läuft angekreuzt → nicht angekreuzt → kein Wert. Fehlt er, gilt zweiwertig. |
| `TLCheckbox` | `display` | string, optional | Die Form: `switch` für den Schalter, `tl-checkbox--switch`. Fehlt er, ist es das Kästchen. Mit `triState` nie `switch` — ein Schalter hat keine dritte Stellung. |
| `TLChoiceGroup` (`ReactDropdownSelectControl` mit `SelectDisplay.RADIO`) | `presentation` | string | Radios bei einem Wert, Kästchen bei mehreren, je in `label.tl-choice` in einer `tl-choice-group`, Rolle `radiogroup` bzw. `group`. |
| `TLChoiceGroup` | `options` | Liste | Die angebotenen Werte, je `value` (boolean oder `null` für „kein Wert“) und `label`. |
| `TLSegmentedChoice`, `TLOptionChips` (`ReactDropdownSelectControl`) | `display` | string | `segmented` bzw. `chips`: welche Form die Auswahl hat. |
| `TLSegmentedChoice`, `TLOptionChips` | `options` | Liste | Jede Option als Deskriptor: `value` (Id), `label`, optional `image` (kodiertes ThemeImage), `colorRole`, `link`. |
| `TLSegmentedChoice`, `TLOptionChips` | `value` | Liste | Die gewählten Optionen als Deskriptoren. |
| `TLSegmentedChoice`, `TLOptionChips` | `multiSelect` | boolean | Mehrfachwahl: `role="group"` mit `aria-pressed`. Sonst Einfachwahl: `role="radiogroup"` mit `aria-checked`. |
| `TLSegmentedChoice`, `TLOptionChips` | `optionsLoaded` | boolean | Ob die Optionsliste des Clients aktuell ist; diese Formen bekommen sie sofort. |
| `TLSegmentedChoice`, `TLOptionChips` | `customOrder` | boolean | Die Wahl behält ihre Reihenfolge, statt sortiert zu werden. |
| `TLSegmentedChoice`, `TLOptionChips` | `emptyOptionLabel` | string | Beschriftung für „nichts gewählt“. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Klick auf Kästchen oder Schalter | `sendCommand('valueChanged', { value })`, `value` boolean oder bei `triState` `null`. |
| Wahl eines Radio (`TLChoiceGroup`) | `sendCommand('valueChanged', { value })`, `value` der Option (boolean oder `null`). |
| Wahl eines Segments oder Chips | `sendCommand('valueChanged', { value })`, `value` die Liste der gewählten Options-Ids. |
| Link einer Option im Lesewert | `sendCommand('goto', { option })`, nur bei `editable: false` und `link` im Deskriptor. |

## Tokens

| Teil | Token |
|---|---|
| Kästchen, Kreis | Maß `--tl-size-choice` (folgt nicht der Dichte); Ecke `--tl-radius-sm` bzw. `--tl-radius-round` |
| Rand | `--tl-border-control`, Breite `--tl-border-width-sm`; beim Zeigen `--tl-border-emphasis` |
| Halo (Kästchen, Radio, Schalter) | beim Zeigen `--tl-surface-state-hover`, beim Drücken `--tl-surface-state-active` — durchscheinende Schichten, was darunter liegt bleibt sichtbar; angekreuzt wie leer; als `box-shadow` ohne Versatz und Unschärfe, Ausdehnung (`--tl-size-control` − `--tl-size-choice`) / 2, folgt der Form der Marke (Quadrat mit `--tl-radius-sm`, Kreis, Pille um die Spur); Übergang `--tl-duration-fast`, `--tl-easing-standard` |
| Fläche | `--tl-surface-field` |
| Angekreuzt, gemischt, gewähltes Radio | Fläche und Rand `--tl-surface-brand`; Haken, Strich, Punkt `--tl-text-on-brand`, Strichstärke `--tl-border-width-md` |
| Schalter | Spur `--tl-size-switch` × `--tl-size-choice`, Ecke `--tl-radius-full`; Knopf `--tl-size-switch-knob`, rund `--tl-radius-round`, aus `--tl-icon-secondary`, an `--tl-text-on-brand`; Bewegung `--tl-duration-fast`, `--tl-easing-standard` (Reduced Motion über den globalen Schalter) |
| Trefferfläche `tl-choice` | Höhe `--tl-size-control` (folgt der Dichte), als `min-height`; Abstand zum Label `--tl-space-xs`; Text `--tl-text-primary` |
| Gruppe `tl-choice-group` | Abstand `--tl-space-control-gap` |
| Segmente | Rahmen `--tl-border-control`, `--tl-radius-sm`, `--tl-surface-field`; Segment `--tl-size-control` abzüglich 2 × `--tl-border-width-sm`, Innenabstand `--tl-space-control-x`, Text `--tl-text-primary`; auf der Feldfläche eine Stufe vor wie die Knöpfe im Feld: beim Zeigen `--tl-surface-interactive-selected`, beim Drücken `--tl-surface-interactive-active` (12.10.) |
| Entfernbare Kapsel | Rahmen und Fläche wie der Chip; gedrückt `--tl-surface-brand`, Text `--tl-text-on-brand`, Zeigen `--tl-surface-brand-hover`/`--tl-text-on-brand-hover`; Entfernen gedrückt beim Zeigen `--tl-surface-brand-active`/`--tl-text-on-brand-active`. Das Entfernen ist derselbe Kreis wie im Select-Chip (eine Regel, `select.css`, Selektor `.tl-chip > .tl-chip__remove`, damit er `.tl-button--icon.tl-button--sm` schlägt): Durchmesser `--tl-size-control-sm` − 2 × `--tl-border-width-sm` − `--tl-space-2xs`, `--tl-radius-round`, Lücke `--tl-space-2xs` / 2 am Ende; Zeigen und Drücken `--tl-surface-interactive-active` |
| Chips | Höhe `--tl-size-control-sm`, Innenabstand `--tl-space-sm`, Lücke `--tl-space-2xs` (auch im Segment zwischen Bild und Label), Ecke `--tl-radius-full`; Gruppe `--tl-space-xs`; Fläche `--tl-surface-field`, darum beim Zeigen `--tl-surface-interactive-selected`, beim Drücken `--tl-surface-interactive-active` (12.10.) |
| Gewähltes Segment | Fläche `--tl-surface-brand-subtle`, Rand `--tl-border-interactive` |
| Gewählter Chip | wie angekreuzt: Fläche und Rand `--tl-surface-brand`, Text `--tl-text-on-brand`; beim Zeigen Fläche `--tl-surface-brand-hover`, Text `--tl-text-on-brand-hover` |
| Fehler | Rand `--tl-status-error-border` |
| Warnung | Rand `--tl-status-warning-border` |
| deaktiviert (vom Kontrast-Gate ausgenommen) | Fläche `--tl-surface-disabled`, Text `--tl-text-disabled`, Rand `--tl-border-disabled`; angekreuzt deaktiviert und gewählter Chip deaktiviert: Fläche `--tl-border-disabled` |

Schrift: Klasse `.tl-type-body` am `tl-choice`, `.tl-type-label` an Segment und Chip, von außen
gesetzt; die Auswahl hat keine Schriftregel.

## Attribute

| Attribut | Wann |
|---|---|
| `:checked` | `value: true` an Kästchen, Schalter, Radio — der native Zustand des `input`, kein Attribut von Hand. |
| `:indeterminate` | `triState` und `value: null`. `indeterminate` ist eine Eigenschaft des `input` (kein HTML-Attribut); der Browser meldet es dem Screenreader als „gemischt“. **Kein** `aria-checked` am nativen Kästchen (ARIA in HTML; entschieden 07.10.). |
| `role="switch"` + `aria-checked` | `display: switch`; am `input type="checkbox"` mit `tl-checkbox tl-checkbox--switch`. |
| `role="radiogroup"` | an der `tl-choice-group` der Radios (`TLChoiceGroup`) und an `tl-segmented` bei Einfachwahl. |
| `role="group"` | an `tl-chip-group` und an `tl-segmented` bei `multiSelect`. |
| `aria-checked` | am Segment (`role="radio"`) bei Einfachwahl: `"true"` für das gewählte. |
| `aria-pressed` | am Segment bei Mehrfachwahl und an jedem Chip: `"true"` für die gewählten. |
| `aria-invalid="true"` | `hasError`, wo ARIA es erlaubt: am Kästchen, am Schalter, an der `role="radiogroup"` (Radios, Segmente mit Einfachwahl). Das CSS liest es selbst. |
| `data-tl-state="error"` | `hasError` an einer `role="group"` (Chips, Segmente mit Mehrfachwahl) — dort erlaubt ARIA `aria-invalid` nicht. In der Chip-Gruppe zeigen nur ungewählte, nicht deaktivierte Chips den Fehler- oder Warnrand; ein gewählter trägt die Marke. |
| `data-tl-state="warning"` | `hasWarnings && !hasError`, am Kästchen, an der Radiogruppe, an `tl-segmented`, an `tl-chip-group`. Der Fehler verdrängt die Warnung. |
| `data-tl-state~="dragover"` | an der `tl-chip-group` von `TLFileChips`, solange eine Datei darüber gezogen wird: die Chip-Gruppe ist dessen Drop-Zone, mit derselben gestrichelten Linie wie `tl-file-upload` (`upload.contract.md`, eine Regel). |
| `data-tl-state~="compact"` | an der `tl-chip-group` von `TLFileChips`, wenn das Feld wenig Platz hat (Feldzustand `compact`, etwa in der Tabellenzelle): eine Zeile, die Chips rollen seitwärts in `tl-chip-group__chips` (`upload.contract.md`). |
| `aria-required="true"` | `mandatory`, am Kästchen, am Schalter, an der `role="radiogroup"`. Eine `role="group"` (Chips, Segmente mit Mehrfachwahl) trägt es nicht — ARIA erlaubt es dort so wenig wie `aria-invalid`. An `role="group"` gibt es kein `aria-required`; die Pflicht zeigt der Stern im Feld-Label an, das die Gruppe über `aria-labelledby` benennt. |
| `tabIndex` | roving in Segmenten mit Einfachwahl: nur das gewählte (ohne Wahl das erste) hat `0`, die übrigen `-1`; Pfeiltasten wechseln die Wahl. Bei Mehrfachwahl und an Chips ist jedes Element erreichbar. |
| `aria-readonly="true"` | `editable: false` an `TLCheckbox`: das Kästchen bzw. der Schalter rendert **ohne** `disabled`, mit `aria-readonly="true"` und einem Änderungs-Handler, der nichts tut — die Angekreuzt-Füllung behält den Kontrast der Marke. Kein Zeigen, Zeiger `default`. |
| `disabled` | der inaktive Zustand, nicht der schreibgeschützte: der Client liest `state.disabled`, wie beim Button (`button.contract.md`), und setzt `disabled` an Kästchen, Schalter, Radio, Segment und Chip. |
| `:focus-visible` | den Ring liefert `base.css`; die Auswahl setzt und entfernt keinen Fokusstil. Fokus allein zeigt keinen halo; wird eine fokussierte Marke gezeigt oder gedrückt, liegt der Ring auf dem halo. |
| `:hover`, `:active` | an der Marke oder am `tl-choice` um sie: der halo. Nicht bei `disabled` und nicht bei `aria-readonly="true"`. |

Deaktiviert schlägt Fehler und Warnung: ein `disabled` Kästchen, Radio, Segment oder Chip zeigt keinen
Fehler- oder Warnrand, auch wenn seine Gruppe `aria-invalid="true"` bzw. `data-tl-state` trägt, und
kein Zeigen.

Der halo gehört den Auswahlmarken: Kästchen, Radio und Schalter zeigen beim Zeigen und Drücken eine Fläche
um die Marke, in ihrer Form und bis zur Trefferfläche — wie MUI, durchscheinend: eine Linie unter dem halo bleibt
sichtbar, und er hebt sich auf jedem Grund gleich stark ab (`docs/skalen.md` „Halos sind durchscheinend“). Segmente, Chips, Toggles und Icon-Knöpfe haben keinen —
sie tragen das Zeigen als eigene Fläche. Der halo verschiebt nichts; ein Behälter mit `overflow: hidden`
schneidet ihn wie den Fokusring.

Bei `editable: false` rendert `TLChoiceGroup` die Beschriftung seines Werts als Lesewert
(`tl-field-value`, siehe `field.contract.md`). `TLSegmentedChoice` und `TLOptionChips` rendern wie der
DropdownSelect (`select.contract.md`) eine Liste von `tl-field-value`-Einträgen in `tl-select__values`:
je gewählter Wert Text oder `tl-pill` mit `data-tl-role` seiner `colorRole`, mit Bild, Werte mit `link` als `a`, das
`goto` sendet. `TLCheckbox` zeigt das Kästchen schreibgeschützt: `aria-readonly="true"`, nicht
`disabled` (siehe Attribute).

Klassen:

| Klasse | Element |
|---|---|
| `tl-checkbox` | das `input type="checkbox"` |
| `tl-checkbox--switch` | dasselbe `input` bei `display: switch`, mit `role="switch"` |
| `tl-radio` | das `input type="radio"` |
| `tl-choice` | das `label` um `input` und Text — Trefferfläche |
| `tl-choice-group` | die Gruppe mehrerer `tl-choice`; bei Radios mit `role="radiogroup"` |
| `tl-segmented` | die Leiste der Segmente, `role="radiogroup"` oder `role="group"` |
| `tl-segmented__segment` | `button type="button"` je Option |
| `tl-segmented__segment` + `data-tl-role` | die Rolle des Werts (`colorRole`); keine Pill im Chip oder Segment, Bild und Beschriftung stehen nebeneinander mit `--tl-space-2xs` |
| `tl-segmented__label` | `span` um das Label im Segment; endet mit Ellipse |
| `tl-chip-group` | die Wolke der Chips, `role="group"` |
| `tl-chip-group__chips` | Hülle der Chips von `TLFileChips`; sonst `display: contents`, kompakt die rollende Reihe |
| `tl-chip` | `button type="button"` je Option, mit `aria-pressed` |
| `tl-chip` + `data-tl-role` | die Rolle des Werts (`colorRole`); keine Pill im Chip oder Segment, Bild und Beschriftung stehen nebeneinander mit `--tl-space-2xs` |
| `tl-chip__label` | `span` um das Label im Chip; endet mit Ellipse |
| `tl-chip__detail` | `span` hinter dem Label, der leisere Text (etwa die Größe einer Datei, Part `Chip`, #29787); `--tl-text-secondary`, auf einem gewählten Chip die Farbe der Beschriftung (#29968); kürzt nicht |
| `tl-chip__toggle` | `button` in einem `span.tl-chip`, `aria-pressed`; trägt das Label |
| `tl-chip__remove` | `tl-button tl-button--ghost tl-button--sm tl-button--icon`, `aria-label` und Tooltip Pflicht; Kreis innerhalb der Kapsellinie wie im Select-Chip |
| `tl-choice-group--vertical` | die Optionen untereinander; ohne den Modifier nebeneinander mit Umbruch, Zustand `orientation` |

## Leser außerhalb der Auswahl

| Control | Form |
|---|---|
| `TLCalendar` | die Ansichtswahl (Tag, Arbeitswoche, Woche, Monat, Jahr) in der Leiste: `tl-segmented` mit `role="radiogroup"`, je Ansicht ein `tl-segmented__segment` mit `aria-checked`; Wahl sendet `switchGranularity` (`calendar.contract.md`) |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jeden Zustand oben
das jeweilige Attribut trägt, Gewählt allein über `:checked`, `aria-checked` oder `aria-pressed`
ausdrückt, der Fehler die Warnung verdrängt und im Klassen-Gate keine Klasse ohne `tl`-Präfix
hinterlässt. Der Test dazu folgt mit der ersten Kundenkomponente; bis dahin gilt dieser Text.
