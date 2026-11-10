# Vertrag: Pill

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLPill` registriert ist (Stufe C der Spec),
bekommt die Rolle unten, rendert eine Kapsel mit Beschriftung und trägt die genannten Attribute.
TopLogic garantiert dann Funktion und Zugänglichkeit — nicht das Aussehen. Wer ins Bild passen
will, liest die Tokens.

## Zustand

Die Pill ist kein eigenes Control: `TLText` zeichnet einen Wert als Pill, wenn das Modell ihm eine
Rolle gibt oder der View `appearance="pill"` setzt. Was sie bekommt:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `role` | `neutral` \| `brand` \| `error` \| `warning` \| `success` \| `info` \| `category-1` … `category-8`, optional | Die Farbrolle; fehlt sie, ist die Pill `neutral`. Ein Tokenname oder ein Farbwert ist kein gültiger Wert. |
| `label` | string | Beschriftung. Eine leere Beschriftung rendert keine Pill. |
| `image` | string, optional | Kodiertes ThemeImage für ein Icon vor der Beschriftung (`ThemeIcon`, Klasse `.tl-icon-sm`). |

## Aktionen

Keine. Eine Pill ist Anzeige; eine bedienbare Pill ist ein Button mit `radius-full` und hier nicht
vorgesehen.

## Tokens

| Rolle | Fläche | Linie |
|---|---|---|
| `neutral` (Default) | `--tl-surface-interactive-strong` | `--tl-border-control` |
| `brand` | `--tl-surface-brand-subtle` | `--tl-border-interactive` |
| `error` | `--tl-status-error-subtle` | `--tl-status-error-border` |
| `warning` | `--tl-status-warning-subtle` | `--tl-status-warning-border` |
| `success` | `--tl-status-success-subtle` | `--tl-status-success-border` |
| `info` | `--tl-status-info-subtle` | `--tl-status-info-border` |
| `category-1` … `category-8` | `--tl-category-1-subtle` … `--tl-category-8-subtle` | `--tl-category-1-border` … `--tl-category-8-border` |

Beschriftung in jeder Rolle: `--tl-text-primary`. Die acht Kategorie-Tokens im Einzelnen:
`--tl-category-1-subtle`, `--tl-category-1-border`, `--tl-category-2-subtle`, `--tl-category-2-border`,
`--tl-category-3-subtle`, `--tl-category-3-border`, `--tl-category-4-subtle`, `--tl-category-4-border`,
`--tl-category-5-subtle`, `--tl-category-5-border`, `--tl-category-6-subtle`, `--tl-category-6-border`,
`--tl-category-7-subtle`, `--tl-category-7-border`, `--tl-category-8-subtle`, `--tl-category-8-border`.

Maße: Mindesthöhe `--tl-size-control-sm`, Innenabstand `--tl-space-xs`, Abstand Icon–Text
`--tl-space-2xs`, Linie `--tl-border-width-sm`, Form `--tl-radius-full`. Text: Klasse `.tl-type-label`,
von außen gesetzt wie beim Button; eine Pill im Fließtext erbt die Größe ihres Texts.

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-role` | Die Rolle als Attribut, kein Modifier: `brand`, `error`, `warning`, `success`, `info`, `category-1` … `category-8`. Ohne Attribut ist die Pill neutral. |
| sonst keines | Die Pill ist ein `span` mit Text; die Rolle trägt keine Bedeutung, die der Text nicht auch trägt (WCAG 1.4.1), darum kein `aria-label` und kein `role`. |
| `title` / Tooltip | nicht von der Pill; ein abgeschnittener Text bekommt seinen Tooltip vom umgebenden `TLText`. |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jede Rolle die
Attribut `data-tl-role` seiner Rolle trägt, ohne Rolle die neutrale Pill zeigt, bei leerer Beschriftung nichts
rendert und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
