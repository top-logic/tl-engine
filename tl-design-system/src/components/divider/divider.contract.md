# Vertrag: Divider

Eine Trennlinie zwischen Abschnitten oder Werkzeugen. TopLogic garantiert Zugänglichkeit, nicht das
Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Es gibt noch kein Control; ein Control entsteht, wenn eine Ansicht es braucht, und trägt dann diese Klassen und Attribute.

Was ein Divider bekäme:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `orientation` | `horizontal` \| `vertical` | Fehlt sie, ist die Linie horizontal. |

## Aktionen

Keine.

## Tokens

| Teil | Token |
|---|---|
| Linie | `--tl-border-separator`, Breite `--tl-border-width-sm` |

Eine Trennlinie schuldet keinen Kontrast: sie trennt nur.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-divider` | immer; am `<hr>` oder an einem `<div>` |
| `tl-divider--vertical` | die Linie steht in einer Zeile und streckt sich auf deren Höhe |
| `role="separator"` | an einem `<div>`; ein `<hr>` trägt die Rolle von selbst |
| `aria-orientation="vertical"` | bei `tl-divider--vertical` |

## Konformität

Ein Divider besteht den Vertrag, wenn er `role="separator"` trägt (oder ein `<hr>` ist), vertikal
`aria-orientation="vertical"` setzt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
