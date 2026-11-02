# Vertrag: Toggle

Ein Knopf, der einen Bereich auf- und zuklappt. TopLogic garantiert Zugänglichkeit, nicht das
Aussehen; wer ins Bild passen will, liest die Tokens.

Zwei Wörter, zwei Richtungen: ein **Toggle** zeigt rechts = zu, unten = offen; ein
**Dropdown-Indikator** (Select-Pfeil, `aria-hidden`) zeigt unten = zu, oben = offen. Ein Indikator
ist kein Toggle und trägt `tl-toggle` nicht.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `expanded` | boolean | Der gesteuerte Bereich ist offen. Fehlt er, ist er zu. |
| `disabled` | boolean | Der Knopf nimmt keine Eingabe an. |

## Aktionen

| Aktion | Wirkung |
|---|---|
| `toggle` | schaltet `expanded` um |

## Tastatur

Enter und Leertaste lösen `toggle` aus; das leistet das native `<button>`.

## Tokens

| Teil | Token |
|---|---|
| Maß, quadratisch | `--tl-size-control-sm` |
| Rand | `--tl-border-width-sm` |
| Ecke | `--tl-radius-sm` |
| Glyph | `--tl-icon-primary`, deaktiviert `--tl-icon-disabled` |
| Zeiger darüber | `--tl-surface-state-hover` (durchscheinend, sichtbar auf jedem Grund: Formulargruppe, gezeigte und ausgewählte Zeile) |
| gedrückt | `--tl-surface-state-active` |
| Drehung | `--tl-duration-fast`, `--tl-easing-standard` (Reduced Motion setzt `base.css`) |

Den Fokusring liefert `base.css` über `:focus-visible`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-toggle` | immer; an einem `<button type="button">` |
| `aria-expanded` | immer; `true` offen, `false` zu |
| `aria-controls` | immer; Id des gesteuerten Bereichs — außer in Tabellenzeile und Baumknoten: dort klappt er die eigene Zeile auf (`table.contract.md`) |
| `aria-label` | immer; der Knopf trägt keinen Text, dazu ein Tooltip |
| `tl-icon-sm` | am Kind; der Chevron-rechts-Glyph der Theme-Konfiguration |
| `tl-toggle--end` | Variante: der Inhalt öffnet zum Zeilenende (Seitenleiste); offen zeigt der Chevron zum Anfang |

## Konformität

Ein Toggle besteht den Vertrag, wenn er ein `<button>` ist, `aria-expanded`, `aria-controls` (außer in
Tabellenzeile und Baumknoten) und `aria-label` trägt, genau ein `tl-icon-sm`-Kind mit dem Chevron-rechts-Glyph hat und im Klassen-Gate
keine Klasse ohne `tl-`-Präfix hinterlässt.
