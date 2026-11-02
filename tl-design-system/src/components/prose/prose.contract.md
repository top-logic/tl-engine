# Vertrag: Prose

Fließtext vom Server: das HTML, das `TLHtml` eingebettet zeigt (`display` `inline`), mit Überschriften, Absätzen,
Listen, Links, Code, Tabellen und Zitaten (Spec 5b §5). Das Paket gibt diesem fremden Markup Schrift, Abstände und
Farben; es gibt ihm keine eigenen Größen.

## Leser

| Control | Form |
|---|---|
| `TLHtml` | eingebettet: der Behälter des Server-HTML trägt `tl-prose`. Der Fehlermodus bleibt `tl-alert` mit `role="alert"`. |

Die Anzeige des Wysiwyg-Inhalts (Welle 5d) kann `tl-prose` wiederverwenden.

## Schrift

Die Schrift kommt nur aus den Typo-Klassen: der Generator stellt die Selektoren `.tl-prose <element>` vor die Regel
der Typo-Klasse in `base.css` (Abschnitt `prose` in `src/tokens.json`). `prose.css` setzt keine Schriftgröße, kein
Gewicht, keine Zeilenhöhe.

| Element | Typo-Klasse |
|---|---|
| `h1` | `heading-lg` |
| `h2` | `heading-md` |
| `h3`–`h6` | `heading-sm` |
| `p`, `li`, `td`, `th`, `blockquote` | `body` |
| `code`, `pre` | `code` |

## Abstände und Farben

| Teil | Token |
|---|---|
| Abstand über Absatz, Liste, Code-Block, Tabelle, Zitat | `--tl-space-sm` |
| Abstand über einer Überschrift | `--tl-space-md`; das erste Kind steht bündig |
| Link, gezeigt | `--tl-text-link`, `--tl-text-link-hover` |
| Code und Code-Block | Fläche `--tl-surface-layer-nested`, Ecke `--tl-radius-sm`; der Block scrollt (`overflow: auto`) |
| Zellen von Tabellen | Linie `--tl-border-width-sm` in `--tl-border-separator`, Innenabstand `--tl-space-2xs` `--tl-space-xs` |
| Umbruch und Sprungziel | lange Wörter brechen um (`overflow-wrap: break-word`); die Wurzel und jedes Element mit `id` halten beim Springen Abstand zur Oberkante (`scroll-margin-block-start` `--tl-space-lg`, #29765) |
| Zitat | Linie am Anfang `--tl-border-width-md` in `--tl-border-separator`, Einzug `--tl-space-sm` |

Kurz: Text `text-link` für Links, Linie `border-separator` für Tabellen und Zitate.

## Klassen

| Klasse | Teil |
|---|---|
| `tl-prose` | der Behälter des fremden HTML; keine Modifier, keine Elemente |

## Konformität

Fließtext besteht den Vertrag, wenn jede Schrift aus einer Typo-Klasse kommt, jede Farbe und jeder Abstand aus einem
Token und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
