# Vertrag: Formular

Eine Kundenkomponente, die unter einem der TopLogic-Namen `TLFormField`, `TLFormGroup`, `TLFormLayout`
oder `TLFieldList` registriert ist (Stufe C der Spec), bekommt vom Server den Zustand unten, löst die
genannten Aktionen aus und trägt die genannten Attribute. TopLogic garantiert dann Funktion, Tastatur und
Fokus — nicht das Aussehen. Wer ins Bild passen will, liest die Tokens. Der Feldrahmen `tl-form-field`
legt Label, Eingabe und Meldungen um jedes Eingabefeld (`field.contract.md`, `choice.contract.md`); die
Eingabe selbst sitzt im Element `tl-form-field__input`.

## Zustand

Vom Server über `useTLState()`, aus `ReactFormFieldChromeControl`, `ReactFormGroupControl`,
`ReactFormLayoutControl` und `ReactFieldListControl`.

`TLFormField`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `label` | string | Text der Beschriftung: das `label` `tl-form-field__label-text`, das per `for` auf die Eingabe zeigt. |
| `required` | boolean | Pflichtfeld: Stern `tl-form-field__required` (`aria-hidden`) an der Beschriftung, die Eingabe trägt `aria-required="true"`. Im Lesemodus kein Stern. |
| `error` | string, optional | Text des Fehlers, `null` ohne Fehler: `tl-form-field__message` mit `role="alert"`. |
| `errorIcon` | string, optional | Kodiertes ThemeImage vor dem Fehlertext (`ThemeIcon` in `tl-icon-sm`). |
| `warnings` | Liste von string, optional | Warnungen, nur ohne Fehler: jede Warnung ist ein eigenes `tl-form-field__message` mit `aria-live="polite"`, mit Icon und Text. |
| `warningIcon` | string, optional | Kodiertes ThemeImage vor jeder Warnung. |
| `helpText` | string, optional | Hilfetext: ein Hilfeknopf (`tl-button tl-button--ghost tl-button--sm tl-button--icon tl-form-field__help`, Glyphe `fa-regular fa-circle-question`) in der Beschriftung blendet `tl-form-field__help-text` ein und aus. |
| `hasTooltip` | boolean | Die Beschriftung bietet einen reichen Tooltip, den der Server unter dem Schlüssel `tooltip` hält. |
| `tooltipText` | string, optional | Schlichter Text als Tooltip der Beschriftung; der reiche Tooltip geht vor. |
| `dirty` | boolean | Das Feld ist geändert: `data-tl-state="dirty"` an der Wurzel, der Punkt `tl-form-field__dirty` erscheint. |
| `labelPosition` | string, optional | `side`, `top`, `after` oder `hidden`; fehlt er (`null`), gilt die Lage des Layouts. Klassen `tl-form-field--side`, `--top`, `--after`, `--hidden`. |
| `fullLine` | boolean | Das Feld füllt die ganze Zeile des Rasters: `tl-form-field--full`. |
| `visible` | boolean | Ob das Feld sichtbar ist. `false`: es bleibt im DOM, aber nicht zu sehen, damit das Kind seine Verbindung hält. |
| `field` | Kinddeskriptor | Das Eingabe-Control. |

Die Lage `hidden` zeigt keine Beschriftung, hält den Text aber in `tl-visually-hidden`, damit die Eingabe
ihren Namen behält. Die Lage `after` setzt die Eingabe (Kästchen, Schalter) vor die Beschriftung.

Der Feldrahmen hat keinen Schlüssel `disabled`; inaktiv ist das Eingabe-Control selbst
(`state.disabled`, `field.contract.md`). Schreibgeschützt ist etwas anderes: das Feld zeigt den Lesewert
`tl-field-value`, und das Layout nimmt im Lesemodus die Meldungen und den Stern weg.

`TLFormGroup`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `headerControl` | Kinddeskriptor, optional | Das Control der Überschrift, in `tl-form-group__title`. |
| `headerActions` | Liste von Kinddeskriptoren | Knöpfe neben der Überschrift, in `tl-form-group__actions`. |
| `collapsible` | boolean | Die Gruppe lässt sich einklappen: ein `tl-toggle` steht in der Kopfzeile. |
| `collapsed` | boolean | Die Gruppe ist eingeklappt: `aria-expanded="false"` am Knopf, `hidden` am Körper `tl-form-group__body`. |
| `border` | string | `none`, `subtle` oder `outlined`; bildet sich ab auf: keine Klasse, `tl-form-group--separator`, `tl-form-group--outlined`. |
| `fullLine` | boolean | Die Gruppe füllt alle Spalten des umgebenden Rasters: `tl-form-group--full`. |
| `children` | Liste von Kinddeskriptoren | Die Felder und Untergruppen im Körper. |

`TLFormLayout`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `maxColumns` | number | Höchstzahl der Spalten des Rasters; sie ist Datenwert, das Control setzt sie inline (`grid-template-columns`). |
| `labelPosition` | string | `side`, `top` oder `auto` (das Layout wählt nach der Breite zwischen `side` und `top`); die Felder erben sie. |
| `readOnly` | boolean | Die Felder zeigen Werte: weder Pflichtstern noch Meldungen noch Hilfe. |
| `children` | Liste von Kinddeskriptoren | `TLFormGroup` oder `TLFormField`. |
| `noModelMessage` | string, optional | Hat das Formular kein Modell, steht an Stelle der Felder diese Meldung in `tl-form-layout__empty`. |

`TLFieldList`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `title` | string, optional | Überschrift: `tl-field-list__title`. |
| `fields` | Liste von Kinddeskriptoren | Die Felder, je in `tl-field-list__item` innerhalb von `tl-field-list__fields`. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Klick auf den Einklappknopf (`tl-toggle`) | `sendCommand('toggleCollapse')`; der Server kehrt `collapsed` um. |
| Klick auf die Beschriftung | Fokus in die Eingabe (oder Umschalten des Kästchens); keine Server-Aktion. |
| Klick auf den Hilfeknopf | blendet `tl-form-field__help-text` im Client ein oder aus; keine Server-Aktion. |

## Tokens

| Teil | Token |
|---|---|
| Label-Spalte der Seitenlage | `--tl-size-field-label-min` bis `--tl-size-field-label-max` als `minmax(…)`, danach die Eingabe `minmax(0, 1fr)` |
| Abstände | `--tl-space-2xs` (Raster im Feld, Gruppen der Meldung), `--tl-space-xs`, `--tl-space-sm` (Spaltenabstand, Gruppen, Feldliste), `--tl-space-md` (Layout, Innenabstand der umrandeten Gruppe) |
| Höhe von Beschriftung und Kopfzeile | `--tl-size-control` (folgt der Dichte) |
| Lesewert im Feld | Mindesthöhe `--tl-size-control`, auch die Lesewert-Liste `tl-select__values` (mittig): die Zeile springt nicht zwischen bearbeitbar und schreibgeschützt |
| Beschriftung | Text `--tl-text-secondary`; Pflichtstern `--tl-status-error-text` |
| Geändert-Punkt | Größe `--tl-size-indicator`, Form `--tl-radius-round`, Fläche `--tl-surface-brand` |
| Fehler / Warnung | Text `--tl-status-error-text` / `--tl-status-warning-text` |
| Hilfeknopf | `--tl-icon-secondary`, unter dem Zeiger `--tl-icon-primary` |
| Hilfetext | `--tl-text-helper` |
| Gruppe, Trennlinie | `--tl-border-separator`, Breite `--tl-border-width-sm` |
| Gruppe, umrandet | Rand `--tl-border-control` (er begrenzt ein Bedienfeld), Ecke `--tl-radius-md` |
| Kopfzeile, Überschrift | Text `--tl-text-primary` |
| Einklappknopf | aus `tl-toggle` (`toggle.contract.md`) |
| Feldliste | Überschrift `--tl-text-primary` mit Linie `--tl-border-separator` |
| leeres Formular | `--tl-text-secondary` |

Schrift setzt das Control von außen: `tl-type-label` an der Beschriftung und den Meldungen,
`tl-type-heading-sm` an Gruppen- und Listentiteln, `tl-type-body` an den Eingaben.

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state="dirty"` | `dirty`, an der Wurzel `tl-form-field`. Der Punkt ist `aria-hidden="true"`. |
| `aria-required="true"` | `required`, an der Eingabe; der sichtbare Stern ist `aria-hidden="true"`. |
| `role="alert"` | `error`, an `tl-form-field__message`. |
| `aria-live="polite"` | `warnings`, je Warnung an einem eigenen `tl-form-field__message`. Der Fehler verdrängt die Warnung: steht ein Fehler, rendert keine Warnung. |
| `aria-invalid="true"` | `hasError` an der Eingabe — das Feld zeichnet den Rand selbst (`field.contract.md`). |
| `aria-expanded` | am Einklappknopf `tl-toggle`: `true` offen, `false` eingeklappt; dazu `aria-controls` auf den Körper. |
| `hidden` | am `tl-form-group__body`, wenn `collapsed`. |
| `:focus-visible` | den Ring liefert `base.css`; das Formular setzt und entfernt keinen Fokusstil. |

Im Lesemodus (`readOnly`) weder Pflichtstern noch Meldungen noch Hilfe. Fehler verdrängt Warnung.

Klassen:

| Klasse | Element |
|---|---|
| `tl-form-field` | Wurzel des Feldrahmens; `--side`, `--top`, `--after`, `--hidden`, `--full` |
| `tl-form-field__label` | `div` um Beschriftung, Stern, Punkt und Hilfeknopf; kein `label`, denn ein `button` darf nicht in einem `label` stehen und fließt sonst in den Namen der Eingabe |
| `tl-form-field__label-text` | das `label for="…"` mit dem Beschriftungstext, endet mit Ellipse |
| `tl-form-field__required` | der Pflichtstern, `aria-hidden="true"`, im `tl-form-field__label` neben dem Text |
| `tl-form-field__dirty` | der Geändert-Punkt, `aria-hidden="true"`, nur sichtbar unter `data-tl-state="dirty"` |
| `tl-form-field__input` | der Platz der Eingabe; trägt `tl-type-body`, die Schrift, die ein Lesewert darin erbt (`field.contract.md`) |
| `tl-form-field__message` | der Fehler oder je eine Warnung |
| `tl-form-field__help` | der Hilfeknopf in der Beschriftung |
| `tl-form-field__help-text` | der Hilfetext |
| `tl-form-group` | Wurzel der Gruppe; `--separator`, `--outlined`, `--full` |
| `tl-form-group__header`, `__title`, `__actions`, `__body` | Kopfzeile (darin der Einklappknopf `tl-toggle`), Überschrift, Knöpfe, Körper; die Felder stehen unmittelbar im Körper, Gruppe und Körper sind Subgrids und behalten die Spalten des Layouts |
| `tl-form-layout` | das Raster; `--fill` für ein Formular mit füllender Region; Element `__empty` |
| `tl-field-list` | die Feldliste; Elemente `__title`, `__fields`, `__item` |

Der Einklappknopf ist ein `tl-toggle`: ein `button type="button"` mit `aria-label`, sein Chevron ein
Icon `tl-icon-sm`, das rechts (zu) zeigt und bei `aria-expanded="true"` nach unten dreht. Zustände sind Attribute, nie Klassen: `--dirty`, `--collapsed`, `--error`,
`--warning` und `--readonly` gibt es nicht.

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jeden Zustand oben das
jeweilige Attribut trägt, der Fehler die Warnung verdrängt, im Lesemodus weder Stern noch Meldung noch
Hilfe rendert, der Einklappknopf `aria-expanded` und der Körper `hidden` im Takt halten und im
Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt. Der Test dazu folgt mit der ersten
Kundenkomponente; bis dahin gilt dieser Text.
