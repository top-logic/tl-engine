# Vertrag: Bildfeld

Ein Formularfeld für ein Bild oder mehrere. Die Bühne zeigt ein Bild auf der ganzen Breite des Felds; mehrere Bilder
werden geblättert, ein Zähler sagt, wo das Feld steht. Ein Klick auf das Bild öffnet lesend die ganzseitige Ansicht,
im Bearbeiten die Ansicht zum Anordnen: je Bild eine Kachel mit den Knöpfen, danach eine gestrichelte Fläche, die eine
Datei aufnimmt oder per Klick die Dateiauswahl öffnet. Eine Kundenkomponente, die unter dem Namen `TLImageField`
registriert ist, bekommt vom Server den Zustand unten, löst die genannten Aktionen aus und trägt die genannten Klassen
und Attribute. TopLogic garantiert dann Funktion, Tastatur und Fokus — nicht das Aussehen. Die Knöpfe sind Parts
`Button` (`button.contract.md`).

## Zustand

Vom Server (`ReactImageFieldControl`) über `useTLState()`; die Basisschlüssel des Feldes stehen in `field.contract.md`.

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `images` | Liste von `{ key, name }` | Ein Eintrag je Bild, in der Reihenfolge des Werts. Das Bild liefert die Daten-Adresse des Controls mit `key`; ein Schlüssel bleibt dem Bild, solange es angezeigt wird, und wird nie für ein anderes Bild vergeben. |
| `multiple` | boolean | Das Feld hält mehrere Bilder; sonst eines. |
| `ordered` | boolean | Der Nutzer ordnet mehrere Bilder an. |
| `width`, `height` | CSS-Maß, optional | Aus der Annotation `dimensions`: höchstens so breit zeigt die Bühne ein Bild, so hoch ist die Bühne. Ohne sie: die Breite des Felds, die Höhe `--tl-size-image-stage`. |
| `accept`, `maxUploadSize`, `error` | wie `upload.contract.md` | Filter der Dateiauswahl, Höchstgröße jeder Datei, Fehler des letzten Hochladens. |

## Leser

| Control | Form |
|---|---|
| `TLImageField` (`ReactImageFieldControl`) | Wurzel `tl-image-field` mit `role="group"`. **Bühne:** `tl-image-field__stage`, darin das Bild `tl-image-field__image` (`img`, `alt` der Dateiname) in einem Knopf `tl-image-field__show`; bei mehreren Bildern die Knöpfe `tl-image-field__step--previous` und `--next` (Part `Button`) und der Zähler `tl-image-field__counter`. Ohne Bild trägt die Bühne `data-tl-state~="empty"` und im Bearbeiten die Fläche `tl-image-field__drop` (`label` um ein verborgenes `input type="file"` in `tl-visually-hidden`); lesend ist sie leer. **Anordnen** (nur im Bearbeiten): Wurzel zusätzlich `tl-image-field--arrange`, die Reihe `tl-image-field__tiles`, je Bild eine Kachel `tl-image-field__tile` mit dem Bild im Knopf `tl-image-field__show` und darüber `tl-image-field__actions` mit dem Griff (nur bei `ordered` und mehr als einem Bild), dem Knopf, der das Bild ersetzt, und dem Knopf „✕“, der es entfernt; bei mehreren Bildern oder ohne Bild nach der letzten Kachel die Fläche; darunter die Leiste `tl-image-field__bar` mit „Fertig“. **Ganzseitig** (lesend oder deaktiviert): `tl-image-viewer` mit `role="dialog"` und `aria-modal`, im `body`, darin `tl-image-viewer__image`, bei mehreren Bildern `tl-image-viewer__step--previous`, `--next` und `tl-image-viewer__counter`, und der Knopf `tl-image-viewer__close`. Der Fehler steht in `tl-file-upload__status` mit `role="alert"` unter Bühne oder Kacheln. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Knöpfe zum Blättern, ← und → auf der Bühne oder in der ganzseitigen Ansicht | Das vorige oder nächste Bild, nach dem letzten wieder das erste; nur im Client. |
| Klick auf das Bild der Bühne | Lesend oder deaktiviert die ganzseitige Ansicht, im Bearbeiten die Ansicht zum Anordnen. |
| Escape, Klick auf den Scrim, Knopf „Schließen“ | Schließt die ganzseitige Ansicht; der Fokus geht an das Bild der Bühne zurück. |
| „Fertig“, Escape, Klick auf das Bild einer Kachel | Zurück zur Bühne, beim Bild der Kachel. |
| Datei auf die Bühne gelegt | Upload über den Upload-Kanal, Teile `file`: ein einzelnes Bild wird ersetzt (Teil `replace` mit dem `key` des Bilds); bei mehreren Bildern ein Teil `after` mit dem `key` des gezeigten Bilds, die Dateien kommen dahinter. Die Bühne zeigt danach das erste neue Bild. |
| Klick auf die Fläche, Datei gewählt oder auf die Fläche gelegt | Upload, Teile `file`; ein einzelnes Bild wird ersetzt, mehrere werden angefügt. |
| Knopf „ersetzen“ an einer Kachel, Datei gewählt oder auf die Kachel gelegt | Upload mit einem Teil `file` und einem Teil `replace`, dessen Inhalt der `key` der Kachel ist: die Datei ersetzt deren Bild. |
| Knopf „✕“ | `removeImage` mit `key`. |
| Griff ziehen oder Alt + Pfeiltaste am Griff | `moveImage` mit `index` und `targetIndex` (die Geste von `useListReorder`). |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-image-field` | Wurzel; Bühne oder Kacheln, darunter der Fehler |
| `tl-image-field--arrange` | die Wurzel in der Ansicht zum Anordnen |
| `tl-image-field__stage` | die Bühne: so breit wie das Feld, fest so hoch wie die Höhe der Bühne |
| `tl-image-field__show` | der Knopf um ein Bild |
| `tl-image-field__image` | das Bild, eingepasst |
| `tl-image-field__step` | ein Knopf zum Blättern, mit `--previous` am Zeilenanfang, `--next` am Zeilenende, mittig |
| `tl-image-field__counter` | der Zähler, unten am Zeilenende über dem Bild |
| `tl-image-field__tiles` | die umbrechende Reihe der Kacheln |
| `tl-image-field__tile` | die Kachel eines Bilds, alle gleich groß |
| `tl-image-field__actions` | die Knöpfe über dem Bild einer Kachel, in der oberen Ecke am Zeilenende |
| `tl-image-field__bar` | die Leiste mit „Fertig“, am Zeilenende |
| `tl-image-field__drop` | die Fläche, die ein Bild aufnimmt: in der Reihe so groß wie eine Kachel, auf der Bühne so groß wie sie |
| `tl-image-field__label` | die Beschriftung der Fläche, einzeilig mit Ellipse |
| `tl-image-viewer` | die ganzseitige Ansicht über dem Scrim |
| `tl-image-viewer__image` | das Bild, eingepasst in den Viewport |
| `tl-image-viewer__step` | ein Knopf zum Blättern, mit `--previous` und `--next` |
| `tl-image-viewer__counter` | der Zähler, unten mittig |
| `tl-image-viewer__close` | der Knopf „Schließen“, oben am Zeilenende |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="empty"` | an der Bühne ohne Bild |
| `data-tl-axis="horizontal"` | an der Reihe der Kacheln: die Einfügemarke beim Anordnen steht neben der Kachel (`drag.css`) |
| `data-tl-state~="dragover"` | an der Bühne, der Kachel oder der Fläche, über der eine Datei schwebt (`upload.css`) |
| `data-tl-state~="dragging"`, `drop-before`, `drop-after` | an der Kachel beim Anordnen (`drag.css`) |
| `aria-current="true"` | am Knopf der Kachel des Bilds, das die Bühne zeigt |
| `aria-disabled="true"` | an der Fläche eines deaktivierten Felds |
| `aria-busy="true"` | an Bühne und Fläche, solange hochgeladen wird |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Wurzel, Reihe | Abstand `--tl-space-sm` |
| Bühne | Höhe `--tl-size-image-stage`, Fläche `--tl-surface-field`, Ecke `--tl-radius-sm`; leer so hoch wie eine Kachel `--tl-size-11` ohne Fläche |
| Knöpfe zum Blättern, Zähler der Bühne | Abstand zum Rand `--tl-space-sm` |
| Zähler | Innenabstand `--tl-space-2xs` `--tl-space-xs`, Fläche `--tl-surface-inverse`, Text `--tl-text-inverse`, Ecke `--tl-radius-sm` |
| Kachel, Bild, Fläche | Ecke `--tl-radius-sm` |
| Kachel, Fläche in der Reihe | Größe `--tl-size-11` in beide Richtungen; das Bild eingepasst auf der Fläche der Kachel `--tl-surface-field` |
| Knöpfe der Kachel | Abstand zur Ecke und untereinander `--tl-space-2xs` |
| Fläche | Rand `--tl-border-width-md` gestrichelt in `--tl-border-control`, Innenabstand `--tl-space-sm`, Abstand Icon–Beschriftung `--tl-space-xs`, Fläche `--tl-surface-field`, Text `--tl-text-secondary`; beim Zeigen Rand `--tl-border-emphasis`, Fläche `--tl-surface-interactive-hover`; deaktiviert Rand `--tl-border-disabled`, Text `--tl-text-disabled` |
| Ganzseitige Ansicht | Ebene `--tl-layer-overlay`, Scrim `--tl-surface-scrim`, Rand zum Viewport und Abstand der Knöpfe und des Zählers `--tl-space-lg` |

## Konformität

Ein Bildfeld besteht den Vertrag, wenn die Bühne ein Bild auf der Breite des Felds zeigt und mehrere blättert, das Bild
lesend ganzseitig und im Bearbeiten zum Anordnen öffnet, die Fläche ein Bild per Klick und per Ablegen aufnimmt,
Zustände Attribute sind und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
