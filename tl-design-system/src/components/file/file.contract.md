# Vertrag: File

Die Dateizeile (Spec 5b §6): der Name einer Datei und die Knöpfe daneben. TopLogic garantiert Lage, Ellipse und den
Leer-Zustand; die Knöpfe sind Parts `Button` (`button.contract.md`).

## Leser

| Control | Form |
|---|---|
| `TLDownload` (`ReactDownloadControl`) | `tl-file` mit dem Namen in `tl-file__name` und dem Download-Knopf daneben. Ohne Datei `data-tl-state~="empty"` an `tl-file`. |
| `TLBinaryField` (`ReactBinaryFieldControl`) | Lesend ist die Wurzel `tl-file`: der Download-Knopf und der Name in `tl-file__name`; ohne Datei (`hasData` falsch) `data-tl-state~="empty"` mit dem Hinweis „Keine Datei“ in `tl-file__name`. Im Bearbeiten ist die Wurzel die Drop-Zone `tl-file-upload` (`upload.contract.md`) mit dem Upload-Knopf; die Zeile `tl-file` (Download-Knopf und Name) steht darin nur, wenn eine Datei da ist, danach der Fehler in `tl-file-upload__status` mit `role="alert"`. |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-file` | Wurzel; Zeile aus Name und Knöpfen, mindestens so hoch wie ein Knopf |
| `tl-file__name` | der Dateiname oder der Leer-Hinweis; einzeilig mit Ellipse, höchstens `--tl-size-13` breit |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="empty"` | an `tl-file`, wenn keine Datei da ist; der Text steht in `--tl-text-secondary`, nicht kursiv |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Abstand Name–Knöpfe | `--tl-space-control-gap` |
| Mindesthöhe | `--tl-size-control` |
| Höchstbreite des Namens | `--tl-size-13` |
| Leer | Text `--tl-text-secondary` |

## Konformität

Eine Dateizeile besteht den Vertrag, wenn der Name einzeilig mit Ellipse endet, „leer“ ein Attribut an `tl-file` ist
und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
