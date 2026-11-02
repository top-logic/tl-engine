# Vertrag: Alert

Eine Meldung, die im Fluss einer Seite oder eines Dialogs steht: Icon, Titel, Text. TopLogic garantiert
Zugänglichkeit, nicht das Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

Das Control ist `TLAlert` (`ReactAlertControl`, `AlertStateJson`, master #29652; View-Element `<alert>`).

Zwei Controls tragen den Alert mit eigener Hülle (Welle 4a): `tl-snackbar` (schwebend, geht von selbst; Vertrag
`snackbar`) und `tl-notice-bar` (Balken über die volle Breite; Vertrag `notice-bar`). Für alle drei gilt dieselbe
Ansage-Regel: nur `error` ist `role="alert"`. Bei der NoticeBar sitzt die Rolle auf einer bleibenden Hülle um den Balken
(ein klickbarer Balken ist ein Knopf, `role="button"`); bei Snackbar und Alert am Alert selbst.

Was es bekommt (`AlertStateJson`):

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `variant` | `error` \| `warning` \| `success` \| `info` | Die Bedeutung, gerendert als `data-tl-role`. Pflicht im Markup: es gibt keinen Alert ohne Bedeutung; die Basisklasse `tl-alert` trägt darum keine Fläche, erst die Rolle. Fehlt sie im Zustand, ist es `info`. |
| `title` | string, optional | Kurzer Titel, Element `tl-alert__title`, Klasse `.tl-type-body-strong`, von außen gesetzt. |
| `message` | string | Der Text der Meldung, Element `tl-alert__message`; Zeilenumbrüche des Servers bleiben stehen. |
| `icon` | string | Kodiertes ThemeImage passend zur Bedeutung, Element `tl-alert__icon`. Ein Theme darf es durch das unsichtbare Bild (`none`) ersetzen; dann steht kein Icon da, und der Körper rückt in die erste Spalte. |
| `actions` | Liste von Controls, optional | Knöpfe, was man gegen die Meldung tun kann, Element `tl-alert__actions`, in dieser Reihenfolge. |
| `closable` | boolean | Ob der Benutzer die Meldung schließen kann; dann steht der Schließen-Knopf `tl-alert__close` in einer Spalte hinter dem Körper. |
| `generation` | number | Die Fassung der Meldung; `dismiss` nennt sie, eine veraltete wird ignoriert. |
| `hidden` | boolean | Verborgen: der Alert rendert nichts. |

Leser auch: `ViewLoadError` (eine Ansicht, die nicht lädt, `data-tl-role="error"`) und `TLHtml` (ein Fragment, das nicht gezeigt wird).

## Aktionen

| Aktion | Argument | Wann |
|---|---|---|
| `dismiss` | `generation` | Klick auf den Schließen-Knopf; schließt die Meldung dieser Fassung. |

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-status-error-subtle`, `--tl-status-warning-subtle`, `--tl-status-success-subtle`, `--tl-status-info-subtle` |
| Linie | `--tl-status-error-border`, `--tl-status-warning-border`, `--tl-status-success-border`, `--tl-status-info-border`; Breite `--tl-border-width-sm`, am Zeilenanfang `--tl-border-width-accent` |
| Icon | `--tl-status-error-text`, `--tl-status-warning-text`, `--tl-status-success-text`, `--tl-status-info-text` (3,0 auf der Fläche, Nicht-Text) |
| Text | `--tl-text-primary` (4,5 auf jeder subtle-Fläche) |
| Form und Abstand | `--tl-radius-md`, `--tl-space-xs` zwischen Icon und Text, `--tl-space-sm` und `--tl-space-md` Innenabstand; im Körper `--tl-space-2xs` zwischen Titel, Text und Aktionen, über den Aktionen `--tl-space-xs` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-alert` + `data-tl-role` | `tl-alert` immer, `data-tl-role` genau eine Bedeutung (`error`, `warning`, `success`, `info`), Pflicht |
| `tl-alert__icon`, `tl-alert__body`, `tl-alert__title`, `tl-alert__message`, `tl-alert__actions` | Elemente; das Icon ist `aria-hidden="true"`, denn die Bedeutung steht im Text. Die Spalten folgen dem Inhalt (`:has`): das Icon bekommt eine vor dem Körper, wo es da ist; ohne Icon kein Platzhalter |
| `tl-alert__close` | Schließen-Knopf, `tl-button tl-button--ghost tl-button--sm tl-button--icon` (dieselbe Form wie Entfernen und Leeren im Select), Glyphe über `ThemeIcon` mit `tl-icon-sm`, mit `aria-label` und Tooltip „Dismiss“; die Spalte hinter dem Körper folgt ihm (`:has`), kein Modifier |
| `role="alert"` | `error`: die Meldung wird sofort vorgelesen |
| `role="status"` | `warning`, `success`, `info`: höflich vorgelesen |

## Konformität

Ein Alert besteht den Vertrag, wenn er genau eine Bedeutung in `data-tl-role` trägt, `role="alert"` bei `error` und
sonst `role="status"`, das Icon mit `aria-hidden="true"` rendert, Bedeutung nie allein über die Farbe
vermittelt (Titel oder Text nennt sie) und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
