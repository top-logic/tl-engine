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

## Attribute

| Attribut | Wann |
|---|---|
| `tl-card` `tl-accordion` | an der Wurzel; die Fläche ist die Karte (Vertrag `card`) |
| `tl-accordion__section`, `tl-accordion__header`, `tl-accordion__heading` (`role="heading"`) | je Abschnitt |
| `tl-accordion__trigger` | der Auslöser, `button` mit `aria-expanded` und `aria-controls` auf den Körper |
| `tl-accordion__panel` | der Körper, `role="region"`, `aria-labelledby` auf den Auslöser, zu: `hidden` |

## Konformität

Ein Accordion besteht den Vertrag, wenn jeder Auslöser `aria-expanded` meldet und auf seinen Körper zeigt.
