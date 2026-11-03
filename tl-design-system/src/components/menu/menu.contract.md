# Vertrag: Menu

Eine Liste von Befehlen auf einem `tl-popover`: Popup-Menü an einem Auslöser, Kontextmenü an einer
Stelle, Spalten-Kontextmenü der Tabelle, Überlauf der Werkzeugleiste. TopLogic garantiert
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

Drei Menüs, eine Komponente: jedes ist eine Instanz von `TLMenu`. Ein `TLButton` unter
`ButtonDefaults appearance="menu-item"` ist ein Eintrag; der Container schlägt die Server-Erscheinung.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `items` | Liste | Die Einträge in ihrer Reihenfolge; je Eintrag `type` (`item`, `separator`, `header`), `id`, `label`, `icon`, `disabled`, `active` (in Kraft), `tone` (`danger` für eine destruktive Aktion; fehlt er, ist der Eintrag gewöhnlich). |
| `open` | boolean | Das Menü ist sichtbar. Fehlt er, ist es zu. |
| `anchorId` | string | Id des Elements, an dem das Menü hängt; es klappt um, wenn der Platz fehlt. |
| `anchorX`, `anchorY` | number | Punkt, an dem das Menü hängt, wenn es kein Element gibt (Kontextmenü an der Mausposition). |

## Aktionen

| Aktion | Wirkung |
|---|---|
| `selectItem` | führt den Befehl des Eintrags `itemId` aus und schließt das Menü |
| `close` | setzt `open` auf `false`; der Fokus kehrt zum Auslöser zurück |

## Tastatur

Der Fokus ist echt (Roving-Tabindex): der fokussierte Eintrag trägt `tabindex="0"`, alle anderen
`tabindex="-1"`. Beim Öffnen liegt der Fokus auf dem ersten wählbaren Eintrag.

| Taste | Wirkung |
|---|---|
| `ArrowDown`, `ArrowUp` | nächster, vorheriger Eintrag; am Ende geht es vorn weiter (zyklisch) |
| `Home`, `End` | erster, letzter Eintrag |
| `Enter`, Leertaste | `selectItem` des fokussierten Eintrags |
| `Escape` | `close` |

Der DOM-Fokus wandert mit, der Browser scrollt den fokussierten Eintrag ins Bild. Deaktivierte
Einträge, Kopfzeilen und Trenner werden übersprungen.

## Verhalten

- Die Rail der Sidebar (`tl-sidebar`) trägt ihre Einträge in derselben Anatomie (`tl-menu__item`, `tl-menu__header`, `tl-menu__separator`); dort heißt `aria-current="page"` „hier bist du“ und bekommt zusätzlich die Fläche der Auswahl.

## Tokens

| Teil | Token |
|---|---|
| Fläche, Rand, Schatten, Ebene, Maße | die von `tl-popover` |
| Eintrag, Höhe | `--tl-size-row` |
| Eintrag, Abstände | `--tl-space-control-x` seitlich, `--tl-space-xs` zwischen Icon und Label |
| Eintrag, Ecke | `--tl-radius-sm` |
| Eintrag, Text | `--tl-text-primary`, deaktiviert `--tl-text-disabled` |
| Zeiger darüber | `--tl-surface-interactive-hover` |
| gedrückt | `--tl-surface-interactive-active` |
| Eintrag destruktiv, Text | `--tl-status-error-text` |
| destruktiv, Zeiger darüber | `--tl-status-error-surface` mit `--tl-text-on-status-error` |
| destruktiv, gedrückt | `--tl-status-error-surface-active` mit `--tl-text-on-status-error-active` |
| Fokusring, innen | `--tl-focus-offset` (negativ; Ring und Breite aus `base.css`) |
| Kopfzeile | `--tl-text-secondary`, Abstände `--tl-space-2xs` und `--tl-space-control-x` |
| Trenner | `--tl-space-2xs` darüber und darunter; die Linie trägt `tl-divider` |

Den Fokusring liefert `base.css` über `:focus-visible`; im Menü liegt er innen, weil der Behälter
scrollt. In Kraft hat kein eigenes Gewicht im Stylesheet: es trägt die Typo-Klasse
`tl-type-body-strong`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-popover tl-menu` | immer; an der Fläche mit `role="menu"` |
| `tl-menu__item` | an jedem Eintrag; ein `<button type="button">` mit `role="menuitem"` und `tl-type-body` |
| `aria-current="true"` | am Eintrag, dessen Befehl in Kraft ist; dazu `tl-type-body-strong` statt `tl-type-body` |
| `tl-menu__item--danger` | am Eintrag einer destruktiven Aktion; folgt dem Ton des Kommandos (`tone`), nie vom Autor gesetzt. Deaktiviert gewinnt `--tl-text-disabled`; ein Eintrag kann in Kraft und destruktiv zugleich sein |
| `role="menuitemcheckbox"` | statt `menuitem` an einem Umschalter; dazu `aria-checked`. Auch die Listenart im Wysiwyg: zwei Umschalter, von denen höchstens einer an ist (keiner, solange der Text keine Liste ist); wer die gewählte Listenart wählt, hebt die Liste auf |
| `menuitemradio` | als `role` statt `menuitem` an einem Eintrag einer Gruppe, aus der genau einer gilt (Absatzformat im Wysiwyg); dazu `aria-checked`, `true` an genau einem Eintrag der Gruppe; ein Eintrag, der sich selbst abwählt, ist kein Radio, sondern ein Umschalter (`menuitemcheckbox`). Die Gruppe entsteht über `ButtonDefaults` mit `radio`: jeder `TLButton` darunter wird ein Eintrag mit dieser Rolle, `aria-checked` folgt seinem `checked` |
| `disabled` | am Eintrag, der nicht gewählt werden kann |
| `tl-menu__icon` | am Icon des Eintrags, zusammen mit `tl-icon-sm`; `aria-hidden="true"` |
| `tl-menu__label` | am Text des Eintrags; er schneidet mit Ellipse ab |
| `tl-menu__header` | an einer Kopfzeile, `role="presentation"`, mit `tl-type-label` |
| `tl-divider` | am Trenner zwischen Gruppen (`<hr>`) |
| `tl-menu-region` | am Auslöser, immer mit `data-tl-trigger` |
| `data-tl-trigger="click"` | der Auslöser ist ein `button.tl-button.tl-button--ghost` mit `aria-haspopup="menu"` und `aria-expanded` (`true` offen, `false` zu); seine Kinder sind nicht interaktiv (Avatar, Text) |
| `data-tl-trigger="contextmenu"` | die Region ist reine Durchreiche ohne eigene Box; das Menü öffnet mit der rechten Maustaste, mit `Shift+F10` und mit der Kontextmenü-Taste am fokussierten Element |

## Konformität

Ein Menü besteht den Vertrag, wenn die Fläche `role="menu"` trägt, jeder Eintrag ein `<button>` mit
`role="menuitem"` (oder `menuitemcheckbox` bzw. `menuitemradio` mit `aria-checked`) ist, in einer Radio-Gruppe genau
ein Eintrag `aria-checked="true"` trägt, der Eintrag in Kraft
`aria-current="true"` und `tl-type-body-strong` trägt, ein destruktiver Eintrag (`tone` ist
`danger`) `tl-menu__item--danger` trägt, die Tastatur oben vollständig wirkt, ein Klick-Auslöser
`aria-haspopup="menu"` und `aria-expanded` setzt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix
hinterlässt.
