# Vertrag: Icon

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLIcon` registriert ist (Stufe C der Spec),
bekommt den Zustand unten, rendert ein Icon und trägt die genannten Attribute. TopLogic garantiert
dann Funktion und Zugänglichkeit — nicht das Aussehen. Wer ins Bild passen will, liest die Klassen.

Das Icon hat **kein eigenes Stylesheet**. Die vier Stufen stehen als Klassen in `base.css`
(skalen.md „Icons: drei Stufen nach Rolle“): `.tl-icon-sm`, `.tl-icon-md`, `.tl-icon-lg`, `.tl-icon-glyph`.
Jede setzt `font-size` aus `--tl-size-icon-sm`, `--tl-size-icon-md`, `--tl-size-icon-lg`,
`--tl-size-icon-glyph`, dazu `line-height: 1` und das 1-em-Quadrat. Ein Bild-Icon (`<img>`) wird durch
dieselbe Klasse quadratisch — keine Breite inline.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `image` | string | Kodiertes ThemeImage: `css:<klassen>`, `colored:<klassen>`, ein Pfad oder `theme:<name>`; `none` zeichnet nichts. |
| `size` | `small` \| `medium` \| `large` \| `glyph`, optional | Die Stufe nach Rolle: `small` steht neben Text (Default), `medium` ist selbst das Bedienelement, `large` ist eine Aussage, `glyph` ein Richtungszeichen im Control. |
| `tooltip` | string, optional | Der Text, der das Icon benennt. Ohne ihn ist das Icon Dekoration. |

Die Größennamen im Zustand sind Wörter; die Klassen des Pakets tragen die Kürzel der Skala
(`small` → `.tl-icon-sm`, `medium` → `.tl-icon-md`, `large` → `.tl-icon-lg`, `glyph` → `.tl-icon-glyph`).
Die Komponente bildet das eine auf das andere ab.

## Aktionen

Keine. Ein bedienbares Icon ist ein Button (`tl-button--ghost`, ohne `__label`).

## Tokens

Nur über die Klassen: `--tl-size-icon-sm`, `--tl-size-icon-md`, `--tl-size-icon-lg`, `--tl-size-icon-glyph`.
Farbe: `currentColor` — das Icon erbt die Farbe seines Textes, eine eigene Icon-Farbe gibt es nicht
(skalen.md: `icon-primary` war eine Dublette von `text-primary`).

## Attribute

| Attribut | Wann |
|---|---|
| `role="img"` + `aria-label` | mit `tooltip`: der Text ist der Name |
| `aria-hidden="true"` | ohne `tooltip`: das Icon dekoriert; ebenso jeder Font-Glyph `<i>` innerhalb einer Komponente |
| `alt=""` | am `<img>` eines Bild-Icons; der Name steht am Wurzelelement |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets je Stufe genau eine der
vier Klassen trägt, ohne `size` die Klasse `tl-icon-sm` (die Stufe `small`), mit `tooltip` `role="img"` und sonst
`aria-hidden`, kein `style`-Attribut setzt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
