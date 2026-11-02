# Vertrag: Breadcrumb

Der Pfad zur aktuellen Seite, jede Stufe ein Sprung zurück. TopLogic garantiert Zugänglichkeit, nicht
das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `entries` | Liste | Die Stufen von der Wurzel bis zur Seite davor; je Stufe `id`, `label`. |
| `current` | string | Beschriftung der aktuellen Seite; sie ist kein Sprung. |

Leser: `TLBreadcrumb` und die Leiste von `tl-adaptive-detail` (Darstellungsteil `Breadcrumb`).

## Aktionen

| Aktion | Wirkung |
|---|---|
| `navigate` | springt zur Stufe `entryId` |

## Tastatur

Die Stufen sind native Knöpfe; es gibt keine eigene Belegung. Jede Stufe liegt in der Tab-Reihenfolge,
die aktuelle Seite ist kein Ziel.

| Taste | Wirkung |
|---|---|
| `Tab`, `Shift+Tab` | nächste, vorherige Stufe |
| `Enter`, Leertaste | `navigate` der fokussierten Stufe |

## Tokens

| Teil | Token |
|---|---|
| Abstand zwischen Stufe, Trenner und Stufe | `--tl-space-2xs` |
| Trenner | `--tl-icon-secondary` |
| Aktuelle Seite | `--tl-text-primary` |

Die Stufen sind `tl-button--link` und tragen dessen Tokens; Text liefert `tl-type-body` an Stufe und
aktueller Seite. Das Breadcrumb hat keine eigene Schrift und keine eigene Fokusregel; den Ring liefert
`base.css` über `:focus-visible`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-breadcrumb` | am `<nav>`, mit `aria-label` aus dem Schlüssel `js.breadcrumb.label` |
| `tl-breadcrumb__list` | an der `<ol>` |
| `tl-breadcrumb__entry` | an jedem `<li>`: Stufe samt ihrem Trenner |
| `tl-button tl-button--link tl-type-body` | an der Stufe, ein `<button type="button">` |
| `tl-breadcrumb__separator` | am Trenner, zusammen mit `tl-icon-sm`; `aria-hidden="true"` |
| `tl-breadcrumb__current tl-type-body` | an der aktuellen Seite, ein `<span>` mit `aria-current="page"` |

## Konformität

Ein Breadcrumb besteht den Vertrag, wenn es als `nav` mit `aria-label` über einer `ol` steht, jede
Stufe ein `<button>` ist, die aktuelle Seite `aria-current="page"` trägt und kein Sprung ist, jeder
Trenner `aria-hidden="true"` hat und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
