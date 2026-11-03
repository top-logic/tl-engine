# Vertrag: Tooltip

Der Hinweis an einem Element: Text oder erlaubtes HTML vom Server, mit oder ohne Überschrift, beim Zeigen und beim
Tastaturfokus (Spec 5d §5). Der Tooltip ist invertiert — im hellen Modus dunkel auf hell, im dunklen hell auf dunkel —
und die einzige invertierte Fläche des Pakets: ein Hinweis, kein Menü. TopLogic garantiert Zugänglichkeit, nicht das
Aussehen; wer ins Bild passen will, liest die Tokens.

## Leser

| Teil | Form |
|---|---|
| `TooltipPopover` | zeichnet den Tooltip in einem Portal, die Lage rechnet floating-ui (Abstand, Umklappen, Pfeil) und setzt sie inline |
| `tooltip-host.ts` | öffnet und schließt ihn an Elementen mit `data-tooltip`; wechselt der Zeiger zum nächsten solchen Element, steht der Tooltip am neuen |

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `caption` | string, optional | Überschrift; fehlt sie, gibt es keine Überschriftzeile |
| `text` / `html` | string | der Text, oder erlaubtes HTML vom Server; das HTML erbt Farbe und Schrift des Tooltips |
| `interactive` | boolean | der Zeiger darf in den Tooltip (Link, Text zum Kopieren); sonst geht er durch ihn hindurch |

## Verhalten

- Ein interaktiver Tooltip bleibt offen, während der Zeiger vom Anker in ihn wandert und auf ihm steht; ein anderer
  schließt kurz nachdem der Zeiger den Anker verlässt.
- Escape und ein Drücken außerhalb schließen.
- Er klappt um, wenn der Platz fehlt, und bleibt im Viewport; der Pfeil zeigt auf den Anker.
- Er blendet in `--tl-duration-fast` ein (`@keyframes tl-tooltip-in`); unter reduzierter Bewegung sofort (`base.css`).

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-inverse` |
| Text | `--tl-text-inverse` (das Paar steht in der Paarliste, 4,5:1 in beiden Modi; `derive` rechnet beide) |
| Pfeil | Füllung `--tl-surface-inverse`, ohne Schatten |
| Schatten | `--tl-shadow-popover` als `box-shadow` am Tooltip (nicht `drop-shadow()`: die zweischichtige Liste geht dort nicht) |
| Ebene | `--tl-layer-tooltip` — über dem Popover, in dem das erklärte Element liegen kann, und über dem Toast |
| Ecke | `--tl-radius-md` |
| Breite | höchstens `--tl-size-13` |
| Abstände | `--tl-space-2xs` oben und unten, `--tl-space-sm` seitlich; Einzug einer Liste im Text `--tl-space-sm` |
| Einblenden | `--tl-duration-fast` mit `--tl-easing-entrance` |

Die Schrift steht nicht im Stylesheet: die Überschrift trägt `tl-type-label-strong`, der Text `tl-type-label`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-tooltip` | immer; an der schwebenden Fläche mit `role="tooltip"` |
| `role="tooltip"` | an der Fläche |
| `tl-tooltip__caption` | an der Überschrift, mit `tl-type-label-strong` |
| `tl-tooltip__body` | am Text, mit `tl-type-label`; Elemente darin erben Farbe und Schrift, der erste und letzte Block schließen bündig an |
| `tl-tooltip__arrow` | am Pfeil (SVG von floating-ui) |
| `data-tl-state~="interactive"` | der Tooltip nimmt den Zeiger an, der Text ist auswählbar (`cursor: text`, `user-select: text`); ohne ihn geht der Zeiger hindurch (`pointer-events: none`) |

## Konformität

Ein Tooltip besteht den Vertrag, wenn die Fläche `tl-tooltip` mit `role="tooltip"` trägt, Überschrift und Text ihre
Typo-Klasse tragen, „interaktiv“ ein Zustand ist und keine Klasse, die Schrift nicht im Stylesheet steht und im
Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
