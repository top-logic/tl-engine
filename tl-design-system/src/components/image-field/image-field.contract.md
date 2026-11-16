# Vertrag: Bildfeld

Ein Formularfeld für ein Bild oder mehrere, nach dem Bild-Upload-Feld der klassischen Oberfläche: mit Bild seine
Vorschau und darüber die Knöpfe, ohne Bild eine gestrichelte Fläche, die eine Datei aufnimmt oder per Klick die
Dateiauswahl öffnet. Eine Kundenkomponente, die unter dem Namen `TLImageField` registriert ist, bekommt vom Server den
Zustand unten, löst die genannten Aktionen aus und trägt die genannten Klassen und Attribute. TopLogic garantiert dann
Funktion, Tastatur und Fokus — nicht das Aussehen. Die Knöpfe sind Parts `Button` (`button.contract.md`).

## Zustand

Vom Server (`ReactImageFieldControl`) über `useTLState()`; die Basisschlüssel des Feldes stehen in `field.contract.md`.

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `images` | Liste von `{ key, name }` | Ein Eintrag je Bild, in der Reihenfolge des Werts. Die Vorschau liefert die Daten-Adresse des Controls mit `key`; ein Schlüssel bleibt dem Bild, solange es angezeigt wird, und wird nie für ein anderes Bild vergeben. |
| `multiple` | boolean | Das Feld hält mehrere Bilder; sonst eines. |
| `ordered` | boolean | Der Nutzer ordnet mehrere Bilder an. |
| `accept`, `maxUploadSize`, `error` | wie `upload.contract.md` | Filter der Dateiauswahl, Höchstgröße jeder Datei, Fehler des letzten Hochladens. |

## Leser

| Control | Form |
|---|---|
| `TLImageField` (`ReactImageFieldControl`) | Wurzel `tl-image-field` mit `role="group"`. Je Bild eine Kachel `tl-image-field__tile` mit der Vorschau `tl-image-field__image` (`img`, `alt` der Dateiname); im Bearbeiten darüber `tl-image-field__actions` mit dem Griff (nur bei `ordered` und mehr als einem Bild), dem Knopf, der das Bild ersetzt, und dem Knopf „✕“, der es entfernt. Bei einem Bild ohne Bild, bei mehreren nach der letzten Kachel die Fläche `tl-image-field__drop` (`label` um ein verborgenes `input type="file"` in `tl-visually-hidden`). Der Fehler steht in `tl-file-upload__status` mit `role="alert"` und bricht unter die Kacheln. Lesend nur die Kacheln mit ihren Vorschauen. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Klick auf die Fläche, Datei gewählt oder auf die Fläche gelegt | Upload über den Upload-Kanal, Teile `file`; ein einzelnes Bild wird ersetzt, mehrere werden angefügt. |
| Knopf „ersetzen“ an einer Kachel, Datei gewählt oder auf die Kachel gelegt | Upload mit einem Teil `file` und einem Teil `replace`, dessen Inhalt der `key` der Kachel ist: die Datei ersetzt deren Bild. |
| Knopf „✕“ | `removeImage` mit `key`. |
| Griff ziehen oder Alt + Pfeiltaste am Griff | `moveImage` mit `index` und `targetIndex` (die Geste von `useListReorder`). |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-image-field` | Wurzel; die Kacheln und die Fläche in einer umbrechenden Reihe |
| `tl-image-field__tile` | die Kachel eines Bilds, einzeln wie in der Reihe gleich groß |
| `tl-image-field__image` | die Vorschau |
| `tl-image-field__actions` | die Knöpfe über der Vorschau, in der oberen Ecke am Zeilenende |
| `tl-image-field__drop` | die Fläche, die ein Bild aufnimmt, so groß wie eine Kachel |
| `tl-image-field__label` | die Beschriftung der Fläche, einzeilig mit Ellipse |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-axis="horizontal"` | an der Wurzel im Bearbeiten: die Einfügemarke beim Anordnen steht neben der Kachel (`drag.css`) |
| `data-tl-state~="dragover"` | an der Kachel oder der Fläche, über der eine Datei schwebt (`upload.css`) |
| `data-tl-state~="dragging"`, `drop-before`, `drop-after` | an der Kachel beim Anordnen (`drag.css`) |
| `aria-disabled="true"` | an der Fläche eines deaktivierten Felds |
| `aria-busy="true"` | an der Fläche, solange hochgeladen wird |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Reihe | Abstand `--tl-space-sm` |
| Kachel, Vorschau, Fläche | Ecke `--tl-radius-sm` |
| Kachel, Fläche | Größe `--tl-size-11` in beide Richtungen; das Bild eingepasst auf der Fläche der Kachel `--tl-surface-field` |
| Knöpfe | Abstand zur Ecke und untereinander `--tl-space-2xs` |
| Fläche | Rand `--tl-border-width-md` gestrichelt in `--tl-border-control`, Innenabstand `--tl-space-sm`, Abstand Icon–Beschriftung `--tl-space-xs`, Fläche `--tl-surface-field`, Text `--tl-text-secondary`; beim Zeigen Rand `--tl-border-emphasis`, Fläche `--tl-surface-interactive-hover`; deaktiviert Rand `--tl-border-disabled`, Text `--tl-text-disabled` |

## Konformität

Ein Bildfeld besteht den Vertrag, wenn jedes Bild eine Kachel mit einer Vorschau ist, die Fläche ein Bild per Klick
und per Ablegen aufnimmt, Zustände Attribute sind und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
