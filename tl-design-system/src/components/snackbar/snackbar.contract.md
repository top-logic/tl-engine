# Vertrag: Snackbar

Eine kurze Meldung unten über der Seite, die von selbst geht: „Gespeichert“, ein Fehler eines Befehls. Ein `tl-alert`
mit eigener Hülle: Bedeutung, Farbe und Ansage wie beim Alert, dazu Lage, Ebene, Schatten und Bewegung.

## Zustand

Das Control ist `TLSnackbar` (`ReactSnackbarControl`, `SnackbarStateJson`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `variant` | `info` \| `success` \| `warning` \| `error` | Bedeutung, gerendert als `data-tl-role`. |
| `message` / `content` | string / HTML | Text oder HTML (z. B. Titel `tl-alert__title` und Liste aus `CommandErrors`). |
| `duration` | number | ms bis zum Gehen; `0` bleibt stehen. |
| `visible` | boolean | Sichtbar. |
| `generation` | number | Fassung der Meldung; `dismiss` nennt sie. |

## Aktionen

| Aktion | Argument | Wann |
|---|---|---|
| `dismiss` | `generation` | nach der Dauer, oder Klick auf `tl-alert__close`, jeweils nach dem Ausblenden |

## Verhalten

- Zeigen hält die Meldung an; nach dem Verlassen geht sie nach kurzer Frist.
- Beim Gehen trägt sie `data-tl-state~="exiting"` für `--tl-duration-base`; das TSX liest die Dauer aus dem Token.
- Der Fokus bleibt, wo er ist; die Meldung wird angesagt: `role="alert"` bei `error`, sonst `role="status"`.

## Tokens

| Teil | Token |
|---|---|
| Fläche, Linie, Text, Knopf | aus `tl-alert` |
| Lage | unten mittig, Abstand `--tl-space-md`, höchstens `--tl-size-toast-max` breit |
| Ebene | `--tl-layer-toast` |
| Schatten | `--tl-shadow-drag` |
| Bewegung | `--tl-duration-base`, `--tl-easing-entrance`, `--tl-easing-exit` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-alert` + `tl-snackbar` + `data-tl-role` | immer |
| `role="alert"` | `error` |
| `role="status"` | `info`, `success`, `warning` |
| `data-tl-state~="exiting"` | während sie geht |

## Konformität

Eine Snackbar besteht den Vertrag, wenn sie den Alert-Vertrag erfüllt, Fehler sofort und alles andere höflich ansagt,
den Fokus nicht nimmt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
