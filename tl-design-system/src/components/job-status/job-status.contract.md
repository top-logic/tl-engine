# Vertrag: JobStatus

Was ein langer Auftrag meldet (Spec 5b §7): Zustand, Laufzeit, Schritte, Fortschritt, Meldung, Fehler und Ergebnis.
TopLogic garantiert, dass nur Text, der etwas sagt, angesagt wird — nicht das Aussehen.

## Leser

| Control | Form |
|---|---|
| `TLJobStatus` (`ReactJobStatusControl`) | `tl-job-status`; Abbrechen ist ein Part `Button`, der Balken ein Part `ProgressBar` |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-job-status` | Wurzel, eine Spalte; trägt selbst keine Rolle und kein `aria-live` |
| `tl-job-status__header` | Kopf: Zustand, Laufzeit, Abbrechen in einer Zeile |
| `tl-job-status__state` | der Zustand, mit `tl-type-body-strong` |
| `tl-job-status__elapsed` | die Laufzeit, mit `tl-nums`; außerhalb jedes Live-Bereichs |
| `tl-job-status__cancel` | Hülle des Abbrechen-Knopfs, am Ende des Kopfs |
| `tl-job-status__live` | die einzige Zeile mit `role="status"`: Zustand und Name des aktuellen Schritts (Präfix „Schritt“ als `tl-visually-hidden`), dazu die Meldung |
| `tl-steps--vertical` | die Schritte (`steps.contract.md`), außerhalb der Live-Zeile |
| `tl-job-status__bar` | Hülle des Fortschrittsbalkens, außerhalb der Live-Zeile |
| `tl-job-status__message` | die Meldung des Auftrags, in der Live-Zeile unter der Schrittzeile |
| `tl-job-status__error` | der Fehler als eigene Zeile mit `role="alert"`, außerhalb der Live-Zeile |
| `tl-job-status__result` | das Ergebnis nach dem Abschluss |

## Attribute

| Attribut | Wann |
|---|---|
| `role="status"` | nur an `tl-job-status__live` |
| `role="alert"` | an `tl-job-status__error` |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Abstände | `--tl-space-xs` zwischen den Zeilen, `--tl-space-sm` im Kopf |
| Laufzeit, Meldung | `--tl-text-secondary` |
| Fehler | `--tl-status-error-text` |

## Konformität

Ein JobStatus besteht den Vertrag, wenn die sekündlich wechselnde Laufzeit in keinem Live-Bereich liegt, ein
Schrittwechsel den Text der `role="status"`-Zeile ändert, der Fehler in `role="alert"` steht und im Klassen-Gate keine
Klasse ohne `tl-`-Präfix bleibt.
