# Vertrag: Status

Eine Zeile, die sagt, was statt Inhalt da ist: es lädt, es ist nichts da, oder das Laden ist
gescheitert. TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest
die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `loading` | boolean | Es lädt. Die Zeile trägt einen unbestimmten `tl-progress` mit `aria-busy="true"` oder den Spinner (`tl-spinner`, Part `Spinner`, `role="progressbar"` mit Namen); die Beschriftung des `tl-progress` ist optional und sagt, was lädt. |
| `empty` | boolean | Es ist nichts da. Die Zeile trägt nur Text („Keine Einträge“, „Nichts gefunden“). |
| `error` | boolean | Das Laden ist gescheitert. Die Zeile trägt `role="alert"` und darf einen `tl-button--link` zum Wiederholen tragen. |

## Aktionen

| Aktion | Wirkung |
|---|---|
| `retry` | nur im Fehlerzustand, über den `tl-button--link`; lädt erneut |

## Tokens

| Teil | Token |
|---|---|
| Höhe der Zeile | `--tl-size-row` (als `min-height`) |
| Lücke | `--tl-space-xs` |
| Innenabstand seitlich | `--tl-space-control-x` |
| Text | `--tl-text-secondary` |
| Fehlertext | `--tl-status-error-text` |

Die Schrift setzt der Ort (`.tl-type-body`); `tl-status` hat keine Schriftregel. Der Fortschritt in
der Zeile füllt sie.

## Leser

| Ort | Form |
|---|---|
| Select-Popup | eine `tl-status`-Zeile statt der Optionen |
| Baum | eine `li` mit `role="treeitem"`, eine Ebene unter einem Knoten mit `aria-busy`; im Baum mit `aria-multiselectable` trägt sie `aria-selected="false"` (sie ist nie wählbar); die `li` trägt den Namen (`aria-label` aus `js.tree.loading`), der `tl-progress` darin ist `aria-hidden="true"` |
| Icon-Picker | alle drei Zustände an der Stelle des Rasters: laden mit dem Spinner und dem Text `js.iconSelect.loading`, leer mit `js.iconSelect.noResults`, Fehler mit `js.iconSelect.loadError` und dem Part `Button` (`tl-button--link`) `js.iconSelect.retry` |
| Tabelle | nur leer: eine `role="row"` (im `treegrid` mit `aria-level="1"`), deren einzige `role="gridcell"` selbst die `tl-status`-Zeile ist, Text `js.table.empty`. Laden zeigt die Tabelle mit Platzhalterzeilen (`tl-skeleton`), nicht mit einer Statuszeile (`table.contract.md`). |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-status` | immer; an einem Block-Element |
| `aria-busy="true"` | am `tl-progress` im Ladezustand |
| `aria-label` | Der Name der Ladezeile, Pflicht — am Element, das der Screenreader ansagt. Hat die Zeile selbst eine Rolle mit Namen (im Baum die `li role="treeitem"`), trägt sie den Namen, und der `tl-progress` darin ist `aria-hidden="true"` ohne eigenen Namen; sonst trägt ihn der `tl-progress` (Select-Popup). Der Text stammt aus dem Resource-Key des Lesers, im Baum `js.tree.loading`. Steht eine sichtbare Beschriftung da, geht stattdessen `aria-labelledby` auf deren `id`. |
| `aria-valuetext` | nie am `tl-progress` (siehe `progress.contract.md`): die Beschriftung ist der Name, der Wert kommt aus `aria-valuenow` |
| `role="alert"` | am `tl-status` des Fehlers |
| `tl-button--link` | im Fehler, zum Wiederholen |
| `tl-icon-sm` | am Fehler-Glyph, `aria-hidden` |

## Konformität

Eine Statuszeile besteht den Vertrag, wenn sie im Ladezustand einen `tl-progress` mit
`aria-busy="true"` (oder den Spinner mit eigenem Namen) trägt und der Name (`aria-label` oder `aria-labelledby`) am Element sitzt, das der Screenreader ansagt (der `tl-progress`, in einer Zeile mit eigener Rolle diese Zeile), im Fehlerzustand `role="alert"` hat und im
Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
