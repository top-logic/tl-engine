# Vertrag: Divider

Eine Trennlinie zwischen Abschnitten oder Werkzeugen. TopLogic garantiert Zugänglichkeit, nicht das
Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Das Control ist `TLDivider` (`ReactDividerControl`, View-Element `<divider>`). Es zeichnet den Part `Divider`; den
Part zeichnen auch Werkzeugleiste, Seitenleiste und Farbauswahl.

Was es bekommt:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `orientation` | `horizontal` \| `vertical` | Fehlt sie, ist die Linie horizontal. |
| `label` | string, optional | Text in der Linie, nur bei `horizontal`; Element `tl-divider__label` mit `tl-type-label`. Mit Text ist die Linie ein `<div>` (ein `<hr>` trägt keinen Inhalt). |
| `labelPosition` | `start` \| `center` \| `end` | Lage des Texts; fehlt sie, steht er in der Mitte. Klassen `tl-divider--label-start`, `tl-divider--label-end`. |
| `inset` | `none` \| `start` | `start`: vorn um den Einzug eines Listeneintrags verkürzt, Klasse `tl-divider--inset`. Fehlt er, ist es `none`. |

## Aktionen

Keine.

## Tokens

| Teil | Token |
|---|---|
| Linie | `--tl-border-separator`, Breite `--tl-border-width-sm` |
| Text | `--tl-text-secondary`, Abstand zur Linie `--tl-space-sm` |
| Einzug | Einzug = eine Ebene, `--tl-size-control-sm`, wie Baum und Tabelle |

Die Linie schuldet keinen Kontrast: sie trennt nur. Der Text ist Sekundärtext und hält dessen Kontrast.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-divider` | immer; am `<hr>` oder an einem `<div>` |
| `tl-divider--vertical` | die Linie steht in einer Zeile und streckt sich auf deren Höhe |
| `tl-divider--labeled` | mit Text: Linie, Text, Linie; an einem `<div>` |
| `tl-divider__label` | der Text, mit `tl-type-label` |
| `tl-divider--label-start` | Text am Anfang, ohne Linie davor |
| `tl-divider--label-end` | Text am Ende, ohne Linie danach |
| `tl-divider--inset` | vorn eingerückt |
| `role="separator"` | an einem `<div>`; ein `<hr>` trägt die Rolle von selbst |
| `aria-orientation="vertical"` | bei `tl-divider--vertical` |

## Konformität

Ein Divider besteht den Vertrag, wenn er `role="separator"` trägt (oder ein `<hr>` ist), vertikal
`aria-orientation="vertical"` setzt, einen Text nur waagrecht und nur in `tl-divider__label` trägt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
