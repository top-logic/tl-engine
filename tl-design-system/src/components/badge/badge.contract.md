# Vertrag: Badge

Ein Zähler an einem Icon oder einem Reiter: eine gefüllte Pille mit Ziffern, in der Fläche der Marke oder einer
Bedeutung; oder ein Punkt ohne Ziffern. TopLogic garantiert
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Es gibt kein Control; das Part `Badge` (`BadgePart.tsx`, `usePart('Badge')`) zeichnet es, Sidebar,
BottomBar und Reiter setzen es. (`feedback.test.mjs` verlangt den Satz „kein Control“.)

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `content` | integer oder kurzer string | Die Ziffern oder das Kürzel; Text gehört in eine Pill. |
| `dot` | boolean | Ein Punkt statt Ziffern: markiert, ohne zu zählen. |
| `role` | `brand`, `error`, `warning`, `success`, `info` | Die Farbrolle; ohne Angabe die Marke. |
| `label` | string | Der zugängliche Name, wo die Ziffern ihn nicht sagen (ein Punkt, ein Badge allein an einem Icon). |

Leser auch `TLKanbanBoard` (`board.contract.md`): der Zähler der Karten im Kopf einer Spalte.

## Aktionen

Keine. Ein Badge ist Anzeige.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-brand`; mit einer Bedeutung `--tl-status-<s>-surface` |
| Beschriftung | `--tl-text-on-brand`; mit einer Bedeutung `--tl-text-on-status-<s>` (abgeleitet, nie geschrieben) |
| Fläche des Punkts | `--tl-border-interactive`; mit einer Bedeutung `--tl-status-<s>-text` |
| Größe des Punkts | `--tl-space-xs` |
| Form | `--tl-radius-full`, Höhe und Mindestbreite `--tl-size-icon-sm`, Innenabstand `--tl-space-2xs` |
| Schrift | `.tl-type-label-strong .tl-nums`, von außen gesetzt; die Ziffern stehen tabellarisch |
| Ecke (`tl-icon-badge__count`) | `--tl-space-2xs` nach oben, `--tl-space-xs` zum Ende |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-badge` | immer |
| `tl-badge--dot` | der Punkt: ohne Ziffern, ohne Inhalt; die einzige Form |
| `data-tl-role` | `error`, `warning`, `success`, `info`; fehlt oder `brand` für die Marke. Die Bedeutung ist ein Attribut, kein Modifier |
| `tl-icon-badge` | Behälter um ein Icon und seinen Zähler an der Ecke (eingeklappte Sidebar, BottomBar); positioniert |
| `tl-icon-badge__count` | Element um das Part an der Ecke oben am Ende; die Lage gehört hierher, nicht ans Part |
| `role="img"` und `aria-label` | wenn das Badge allein an einem Icon steht: der Name sagt, was gezählt wird („3 neue Meldungen“); ein `aria-label` an einem `<span>` ohne Rolle wird ignoriert |
| nichts | wenn es Teil eines beschrifteten Reiters ist: die Zahl ist dann sichtbarer Text und Inhalt, weder `aria-hidden` noch eigener Name |
| `role="img"` und `aria-label` am Punkt | immer: der Punkt hat keinen Text, sein Name („Neue Einträge“) ist das Einzige, was er sagt; die Farbe allein sagt nichts |

## Farbe des Punkts

Der Punkt hat keine Schrift, also muss seine Fläche selbst 3:1 zum Grund erreichen (WCAG 1.4.11); deshalb liest er
den Text der Bedeutung (`--tl-status-<s>-text`), nicht ihre gefüllte Fläche, die das im hellen Modus nicht schafft. Die
Marke zeichnet ihn in ihrer Linie `--tl-border-interactive`, wie die getönte Rolle `brand` ihre Linie; ihre Fläche
`--tl-surface-brand` ist Grund der Beschriftung und kein Vordergrund der Paarliste. Hell und dunkel, auf jedem Grund, an
dem ein Badge steht, prüft das Kontrast-Gate (`tools/lib/pairs.mjs`, auch für ein Kundentheme) jeden Punkt.

## Konformität

Ein Badge besteht den Vertrag, wenn es die Schrift über `tl-type-label-strong tl-nums` bekommt, allein an einem
Icon `role="img"` mit einem `aria-label` trägt, der das Gezählte nennt (im beschrifteten Reiter bleibt die Zahl schlichter Text), und im Klassen-Gate keine Klasse ohne `tl-`-Präfix
hinterlässt. Ein Punkt trägt immer `role="img"` mit einem `aria-label`.
