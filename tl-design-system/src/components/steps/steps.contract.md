# Vertrag: Steps

Die Schrittliste (Spec 5b §7, V22): wo man in einer Folge von Schritten steht. Ein Block für den Wizard (waagrecht)
und den JobStatus (senkrecht); die Ausrichtung ändert nur die Anordnung, nicht die Farben. TopLogic garantiert die
Zugänglichkeit der Position, nicht das Aussehen.

## Leser

| Control | Form |
|---|---|
| `TLWizard` | `tl-steps` waagrecht im Kopf des Wizards (`wizard.contract.md`); der Name eines erledigten Schritts ist ein Knopf `tl-button--link` |
| `TLJobStatus` | `tl-steps` mit `tl-steps--vertical` im JobStatus (`job-status.contract.md`) |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-steps` | an `ol`, mit `aria-label`; waagrecht, umbrechend |
| `tl-steps--vertical` | senkrecht; kleinere Scheibe, die aktuelle pulsiert |
| `tl-steps__item` | an `li`, ein Schritt |
| `tl-steps__disc` | an `span`, die Scheibe mit der Nummer, mit `tl-type-label` `tl-nums`, `aria-hidden="true"` |
| `tl-steps__name` | an `span`, der Name, mit `tl-type-body` oder (aktuell) `tl-type-body-strong` |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="done"` | an `tl-steps__item`, der Schritt ist erledigt |
| `aria-current="step"` | an `tl-steps__item`, der aktuelle Schritt |
| — | offen: ohne Attribut |
| `aria-hidden` | an `tl-steps__disc`; die Nummer liest die Liste selbst vor |
| `hidden` | verbirgt die Liste |

## Tokens

| Teil | Token |
|---|---|
| Scheibe | `--tl-size-icon-lg`, senkrecht `--tl-size-icon-md`; `--tl-radius-full` |
| Scheibe offen | `--tl-surface-layer-nested`, `--tl-border-control`, `--tl-text-secondary` |
| Scheibe erledigt | `--tl-surface-brand-subtle`, `--tl-text-primary` |
| Scheibe aktuell | `--tl-surface-brand`, `--tl-text-on-brand` |
| Name | `--tl-text-secondary`, aktuell `--tl-text-primary` |
| Abstand waagrecht | `--tl-space-xs` zwischen Zeilen, `--tl-space-xl` zwischen Schritten |
| Abstand senkrecht | `--tl-space-2xs` |
| Puls (nur senkrecht, aktuell) | `--tl-duration-loop`, `--tl-easing-standard`, endlos, `alternate` zwischen Deckkraft 1 und 0,4; bei `prefers-reduced-motion` aus (`base.css`) |

## Konformität

Eine Schrittliste besteht den Vertrag, wenn „erledigt“ und „aktuell“ Attribute am `li` sind, die Scheibe still ist
(`aria-hidden`) und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
