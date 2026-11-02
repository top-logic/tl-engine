# Vertrag: Text

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLText` registriert ist, rendert einen Wert als
`span` mit der Typo-Klasse ihrer Variante und dem Ton als Attribut. TopLogic garantiert Funktion und
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `text` | string | Der Wert. Ein leerer Wert nimmt keinen Platz (siehe unten). |
| `variant` | `display` \| `headline` \| `title` \| `body` \| `label` \| `caption` | Wird zur Typo-Klasse: `display` → `tl-type-display`, `headline` → `tl-type-heading-lg`, `title` → `tl-type-heading-sm`, `body` → `tl-type-body`, `label` → `tl-type-label`, `caption` → `tl-type-label`. |
| `tone` | `primary` \| `secondary` \| `helper` \| `placeholder` \| `success` \| `warning` \| `error` \| `on-color` | `primary` setzt kein Attribut, der Text erbt. `on-color` wird `data-tl-tone="on-brand"`. Alle anderen werden `data-tl-tone` mit ihrem Namen. `accent` gibt es nicht (02.10.). Platzhalter ist ein Ton, keine Kursive: die Typo kennt keinen Schriftstil. |
| `overflow` | `ellipsis` | Wird zur Klasse `tl-text--truncate`: eine Zeile, die mit Ellipse endet. Ohne Wert bricht der Text um und behält die Zeilenumbrüche seines Werts. |
| `appearance` | `pill` | Oder eine Farbrolle: der Text wird als Pill gezeichnet. |

Als Pill (`appearance: pill` oder eine Farbrolle) ist `span.tl-pill` **selbst die Wurzel**, mit
`data-tl-role` und den übrigen Text-Attributen (`id`, `role`, `data-tooltip`, Klasse aus
`css-class`); die Beschriftung steht in `tl-pill__label`. Es gibt keine Hülle `tl-text` darum
(entschieden 07.10.). Ton und Rolle: `success`, `warning`, `error` behalten ihren Namen, alles andere
bleibt ohne `data-tl-role` (neutral: die Pill ohne Attribut ist neutral, siehe `pill.contract.md`).

Stark ist die `-strong`-Klasse der Variante als `css-class`: `tl-type-body-strong` oder `tl-type-label-strong`. Überschriften sind schon fett genug und brauchen keine. Die Varianten-Klasse bleibt daneben stehen: `base.css` stellt jede `-strong`-Klasse hinter ihre Grundklasse, bei gleicher Spezifität gewinnt sie (Test `text.test.mjs`). So macht die Tabelle ihre Gruppenzeile stark: der Server gibt dem Text jeder Zelle der Gruppenzeile `tl-type-body-strong` (`table.contract.md`).

Ein Text ohne Wert (`text` leer) nimmt keinen Platz, außer er trägt `role` (Live-Bereich); master #29752.

Ein Text behält die Zeilenumbrüche seines Werts (`white-space: pre-line`): ein `\n` ist ein Umbruch,
Folgen von Leerzeichen fallen zusammen, und der Text bricht an der verfügbaren Breite um. Mit Ellipse
(`--truncate`) und in der Tabellenzelle bleibt er einzeilig (#29886).

Abgeschnittener Text (`--truncate`) zeigt seinen vollen Text als Tooltip über den Tooltip-Host, ohne
Deklaration (master #29752).

## Aktionen

Keine. Text ist Anzeige.

## Tokens

| Ton | Token |
|---|---|
| ohne | erbt |
| `secondary` | `--tl-text-secondary` |
| `helper` | `--tl-text-helper` |
| `placeholder` | `--tl-text-placeholder` |
| `success` | `--tl-status-success-text` |
| `warning` | `--tl-status-warning-text` |
| `error` | `--tl-status-error-text` |
| `on-brand` | `--tl-text-on-brand` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-text` | immer, außer als Pill |
| `tl-type-<variante>` | immer; die Schrift setzt allein die Typo-Klasse |
| `data-tl-tone` | wenn der Ton nicht `primary` ist |
| `tl-text--truncate` | bei Ellipse |
| `role` | an einem Live-Bereich; hält den Text auf der Seite, auch wenn er leer ist |
| `data-tl-role` | an `tl-pill`, wenn der Text als Pill gezeichnet wird |

## Konformität

Ein Text besteht den Vertrag, wenn er den Ton nur über `data-tl-tone` setzt, keine Klasse ohne
`tl-`-Präfix und keine Farbe oder Schrift eigener Wahl trägt und als Pill ohne Hülle `tl-text`
gezeichnet wird.
