# Vertrag: Accordion

Abschnitte untereinander, jeder klappt seinen Körper auf und zu (WAI-ARIA Accordion). Gebaut von Bernhard
(#29651), seit Welle 4c im Paket.

## Zustand

Das Control ist `TLAccordion` (`AccordionState`): `sections[]` mit `id`, `label`, `icon`, `expanded`, `actions`,
`content`; `exclusive`. Der Inhalt eines Abschnitts kommt beim ersten Öffnen und bleibt dann gemountet.

## Aktionen

| Befehl | Wann |
|---|---|
| `toggleSection` | Klick oder Enter/Leertaste am Auslöser; Argumente `sectionId`, `expanded` |

Pfeil auf/ab, Home und End bewegen den Fokus zwischen den Auslösern.

## Tokens

| Teil | Token |
|---|---|
| Linie | `--tl-border-width-sm` in `--tl-border-separator` zwischen den Abschnitten und über dem Körper |
| Kopf | Mindesthöhe `--tl-size-bar`, Lücke `--tl-space-xs`; unter dem Zeiger `--tl-surface-interactive-hover` |
| Auslöser | Innenabstand `--tl-space-md`, Lücke `--tl-space-xs`; der Ring liegt innen, um `--tl-focus-offset` eingerückt |
| Pfeil, Icon | `--tl-icon-primary`; der Pfeil dreht über `--tl-duration-fast` mit `--tl-easing-standard` |
| Befehle | `--tl-space-sm` am Ende |
| Körper | Innenabstand `--tl-space-md` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-card` `tl-accordion` | an der Wurzel; die Fläche ist die Karte (Vertrag `card`) |
| `tl-accordion__section`, `tl-accordion__header`, `tl-accordion__heading` (`role="heading"`) | je Abschnitt |
| `tl-accordion__trigger` | der Auslöser, `button` mit `aria-expanded` und `aria-controls` auf den Körper |
| `tl-accordion__panel` | der Körper, `role="region"`, `aria-labelledby` auf den Auslöser, zu: `hidden` |

## Konformität

Ein Accordion besteht den Vertrag, wenn jeder Auslöser `aria-expanded` meldet und auf seinen Körper zeigt.
