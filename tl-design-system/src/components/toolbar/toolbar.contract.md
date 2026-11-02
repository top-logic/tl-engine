# Vertrag: Toolbar

Eine Reihe von Befehlen über einem Inhalt: Aktionen eines Tabellenkopfs, einer Karte, eines Editors.
TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

Die Einträge sind Button-Controls (`tl-button`). Die Toolbar gibt ihnen die Erscheinung
(`ButtonDefaults`, siehe `button.contract.md`) und sagt ihnen, wann sie kompakt sind (`iconOnly`);
sie hat keine eigene Knopfregel. Trenner sind `tl-divider--vertical`.

Leser auch: `TLCalendar` — die Leiste des Kalenders ist ein `tl-toolbar` mit `tl-calendar__toolbar`; darin
Heute, zurück, vor als Part `Button`, der Titel `tl-calendar__title` und die Ansichtswahl `tl-segmented`
(`calendar.contract.md`, `choice.contract.md`).

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `items` | Liste | Die Einträge in ihrer Reihenfolge, in Gruppen (`tl-toolbar__group`) und durch Trenner geteilt. |
| `compact` | boolean | Jeder Knopf, der sein Icon zeigt, lässt sein Label weg und wird `tl-button--icon`. Fehlt er, bleibt jeder Knopf, wie er ist. |
| `overflow` | `trailing`, `leading`, optional | Die Toolbar kollabiert zu diesem Ende; Einträge, die nicht passen, wandern in ein `tl-menu`. Fehlt er, kollabiert sie nicht. |

## Aktionen

| Aktion | Wirkung |
|---|---|
| Aktion des Eintrags | wie beim Button: `click` führt den Befehl des Eintrags aus |
| Überlauf öffnen | der Überlauf-Trigger öffnet das Menü mit den Einträgen, die nicht passen (`menu.contract.md`) |

## Tastatur

Die Toolbar hat keine eigene Tastenbelegung: jeder Knopf liegt in der Tab-Reihenfolge, in der Reihenfolge
des Dokuments; der Überlauf-Trigger ist der letzte (oder erste) Knopf. Das Überlauf-Menü
folgt `menu.contract.md`.

| Taste | Wirkung |
|---|---|
| `Tab`, `Shift+Tab` | nächster, vorheriger Knopf |
| `Enter`, Leertaste | Befehl des fokussierten Knopfs; am Überlauf-Trigger öffnet das Menü |

## Tokens

| Teil | Token |
|---|---|
| Abstand zwischen Gruppen und Einträgen | `--tl-space-xs` |
| Abstand innerhalb einer Gruppe | `--tl-space-2xs` |
| Höhe | mindestens `--tl-size-bar` (so hoch wie die Reiterleiste) |
| Abstand oben und unten | `--tl-space-2xs` |
| Mindestbreite bei Überlauf | `--tl-size-control` (Platz für den Überlauf-Trigger) |

Den Fokusring der Knöpfe liefert `base.css` über `:focus-visible`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-toolbar` | immer, mit `role="toolbar"` und `aria-label` |
| `tl-toolbar__group` | an einer Gruppe zusammengehöriger Einträge; eine leere Gruppe verschwindet |
| `tl-toolbar__group:has(> .tl-toolbar)` | eine Gruppe, die eine eingebettete Toolbar hält, schrumpft unter ihren Inhalt (`flex-shrink: 1`, `min-width: 0`): die eingebettete Toolbar bekommt weniger als ihre natürliche Breite und kollabiert in ihren Überlauf. Leser: die Befehle des Servers in der Leiste des Wysiwyg-Editors (`rich-text.contract.md`) |
| `tl-toolbar--wrap` | die Toolbar bricht um, statt zu kollabieren (nur `flex-wrap: wrap`): eine Gruppe geht ganz in die nächste Zeile, der Zeilenabstand ist `--tl-space-xs`. Leser: die Leiste des Wysiwyg-Editors (`rich-text.contract.md`). Nicht zusammen mit `data-tl-overflow` |
| `data-tl-overflow="trailing"` / `"leading"` | die Toolbar kollabiert zu diesem Ende; der Inhalt sitzt dort |
| `data-tl-compact` | Knöpfe zeigen nur Icons; jeder trägt `tl-button--icon` mit `aria-label` |
| `tl-divider tl-divider--vertical` | an einem Trenner zwischen Gruppen, mit `role="separator"` und `aria-orientation="vertical"` |
| `data-tl-break` | an einem Trenner einer `tl-toolbar--wrap`, an dem die Leiste umbricht (die Gruppe danach steht in der nächsten Zeile): der Trenner ist unsichtbar, behält aber seinen Platz, damit das Verbergen keinen neuen Umbruch auslöst. Das Control misst die Zeilen und setzt das Attribut bei jeder Größenänderung neu |
| Überlauf-Trigger | ein `button.tl-button.tl-button--icon` mit `bi bi-three-dots`, `aria-haspopup="menu"` und `aria-expanded` |

## Konformität

Eine Toolbar besteht den Vertrag, wenn sie `role="toolbar"` mit `aria-label` trägt, jeder Eintrag ein
Button-Control ist, im kompakten Zustand jeder Icon-Knopf `tl-button--icon` mit `aria-label` trägt, der
Überlauf-Trigger `aria-haspopup="menu"` und `aria-expanded` setzt, kein Zustand als Klasse
geführt wird und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
