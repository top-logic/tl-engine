# Vertrag: Bildfeld

Ein Formularfeld für ein Bild oder mehrere. Das Formular zeigt nur die Bühne: ein Bild auf der ganzen Breite des Felds;
mehrere Bilder werden geblättert, ein Zähler sagt, wo das Feld steht. Ein Klick auf das Bild öffnet die ganzseitige
Ansicht, lesend wie im Bearbeiten. Im Bearbeiten steht unter der Bühne eine Leiste mit Knöpfen: Bild hinzufügen,
Ersetzen, Entfernen und, bei mehreren Bildern, Verwalten. „Verwalten“ öffnet den Editor über der Seite: je Bild eine
Kachel mit ihren Knöpfen, danach eine gestrichelte Fläche, die Dateien aufnimmt oder per Klick die Dateiauswahl öffnet.
Kacheln stehen nie im Formular. Eine Kundenkomponente, die unter dem Namen `TLImageField` registriert ist, bekommt vom
Server den Zustand unten, löst die genannten Aktionen aus und trägt die genannten Klassen und Attribute. TopLogic
garantiert dann Funktion, Tastatur und Fokus — nicht das Aussehen. Die Knöpfe sind Parts `Button`
(`button.contract.md`), der Spinner ist der Part `Spinner`, der Kopf des Editors ist eine Titelleiste (`titlebar.contract.md`).

## Zustand

Vom Server (`ReactImageFieldControl`) über `useTLState()`; die Basisschlüssel des Feldes stehen in `field.contract.md`.

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `images` | Liste von `{ key, name }` | Ein Eintrag je Bild, in der Reihenfolge des Werts. Das Bild liefert die Daten-Adresse des Controls mit `key`; ein Schlüssel bleibt dem Bild, solange es angezeigt wird, und wird nie für ein anderes Bild vergeben. |
| `multiple` | boolean | Das Feld hält mehrere Bilder; sonst eines. |
| `ordered` | boolean | Der Nutzer ordnet mehrere Bilder an. |
| `width`, `height` | CSS-Maß, optional | Aus der Annotation `dimensions`: höchstens so breit zeigt die Bühne ein Bild, so hoch ist die Bühne mehrerer Bilder, höchstens so hoch ein einzelnes Bild. Ohne sie: die Breite des Felds, die Höhe `--tl-size-image-stage`. |
| `accept`, `maxUploadSize`, `error` | wie `upload.contract.md` | Filter der Dateiauswahl, Höchstgröße jeder Datei, Fehler des letzten Hochladens. |

## Leser

| Control | Form |
|---|---|
| `TLImageField` (`ReactImageFieldControl`) | Wurzel `tl-image-field` mit `role="group"`, bei einem einzelnen Bild (`multiple` falsch) zusätzlich `tl-image-field--single`. **Bühne:** `tl-image-field__stage`, darin das Bild `tl-image-field__image` (`img`, `alt` der Dateiname) in einem Knopf `tl-image-field__show`; bei mehreren Bildern die Knöpfe `tl-image-field__step--previous` und `--next` (Part `Button`) und der Zähler `tl-image-field__counter`. Ohne Bild trägt die Bühne `data-tl-state~="empty"` und im Bearbeiten die Fläche `tl-image-field__drop`, bei mehreren Bildern so groß wie die Bühne mit Bild, bei einem einzelnen `--tl-size-11` hoch, (`label` um ein verborgenes `input type="file"` in `tl-visually-hidden`); lesend ist sie leer. **Leiste** (nur im Bearbeiten, mit Bild oder bei mehreren Bildern): `tl-image-field__bar` unter der Bühne mit den Knöpfen „Bild hinzufügen“ (nur bei mehreren Bildern), „Ersetzen“, „Entfernen“ und „Bilder verwalten“ (nur bei mehreren Bildern), je mit Icon und Beschriftung; ohne Bild nur „Bild hinzufügen“, damit das erste Bild nichts verschiebt. **Ebene** über der Seite, im `body`: `tl-image-overlay` mit `role="dialog"` und `aria-modal`, darin der Inhalt `tl-image-overlay__content`. **Ganzseitig:** die Ebene trägt zusätzlich `tl-image-overlay--view`, der Inhalt ist das Bild `tl-image-overlay__image`; bei mehreren Bildern `tl-image-overlay__step--previous`, `--next` und `tl-image-overlay__counter`, dazu der Knopf `tl-image-overlay__close`. **Editor** (nur im Bearbeiten): der Inhalt ist `tl-image-editor`, benannt durch den Titel seiner Titelleiste `tl-titlebar` (die Zahl der Bilder) mit dem Knopf „Fertig“; darunter die Kacheln `tl-image-editor__tiles`, je Bild eine Kachel `tl-image-editor__tile` mit dem Bild `tl-image-editor__image` im Knopf `tl-image-editor__show` und darunter der Leiste `tl-image-editor__actions` mit dem Griff (nur bei `ordered` und mehr als einem Bild), dem Knopf, der das Bild ersetzt, und dem Knopf „✕“, der es entfernt; nach der letzten Kachel die Fläche `tl-image-field__drop`. Der Fehler steht in `tl-file-upload__status` mit `role="alert"` unter Bühne und Leiste, bei offenem Editor unter dessen Kacheln. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Knöpfe zum Blättern, ← und → auf der Bühne oder in der ganzseitigen Ansicht | Das vorige oder nächste Bild, nach dem letzten wieder das erste; nur im Client. |
| Klick auf das Bild der Bühne | Die ganzseitige Ansicht, lesend, deaktiviert und im Bearbeiten. |
| Escape, Klick auf den Rand der Ebene, Knopf „Schließen“ | Schließt die ganzseitige Ansicht; der Fokus geht zurück, wo er war. |
| „Bild hinzufügen“, Dateien gewählt | Upload über den Upload-Kanal, Teile `file` und ein Teil `after` mit dem `key` des gezeigten Bilds: die Dateien kommen dahinter. Die Bühne zeigt danach das erste neue Bild. |
| „Ersetzen“, Datei gewählt | Upload mit einem Teil `file` und einem Teil `replace`, dessen Inhalt der `key` des gezeigten Bilds ist. |
| „Entfernen“ | `removeImage` mit dem `key` des gezeigten Bilds. |
| „Bilder verwalten“ | Öffnet den Editor. |
| „Fertig“, Escape, Klick auf den Rand der Ebene | Schließt den Editor; der Fokus geht zurück, wo er war. |
| Klick auf das Bild einer Kachel | Schließt den Editor; die Bühne zeigt das Bild der Kachel. |
| Datei auf die Bühne gelegt | Ein einzelnes Bild wird ersetzt (Teil `replace` mit dem `key` des Bilds); bei mehreren Bildern ein Teil `after` mit dem `key` des gezeigten Bilds, die Dateien kommen dahinter. Die Bühne zeigt danach das erste neue Bild. |
| Klick auf die Fläche, Datei gewählt oder auf die Fläche gelegt | Upload, Teile `file`; ein einzelnes Bild wird ersetzt, mehrere werden angefügt. |
| Knopf „ersetzen“ an einer Kachel, Datei gewählt oder auf die Kachel gelegt | Upload mit einem Teil `file` und einem Teil `replace`, dessen Inhalt der `key` der Kachel ist: die Datei ersetzt deren Bild. |
| Knopf „✕“ an einer Kachel | `removeImage` mit `key`. |
| Griff ziehen oder Alt + Pfeiltaste am Griff | `moveImage` mit `index` und `targetIndex` (die Geste von `useListReorder`). |

## Klassen

| Klasse | Teil |
|---|---|
| `tl-image-field` | Wurzel; Bühne, darunter die Leiste und der Fehler |
| `tl-image-field--single` | die Wurzel eines Felds für ein einzelnes Bild: die Bühne genau so groß wie das Bild |
| `tl-image-field__stage` | die Bühne: so breit wie das Feld, fest so hoch wie die Höhe der Bühne, ohne Fläche; ein kleineres Bild mittig in seiner eigenen Größe. Bei `--single` genau so groß wie das Bild, höchstens so breit wie das Feld |
| `tl-image-field__show` | der Knopf um das Bild der Bühne |
| `tl-image-field__image` | das Bild der Bühne, eingepasst, nie vergrößert |
| `tl-image-field__step` | ein Knopf zum Blättern, mit `--previous` am Zeilenanfang, `--next` am Zeilenende, mittig |
| `tl-image-field__counter` | der Zähler, unten am Zeilenende über dem Bild |
| `tl-image-field__bar` | die Leiste unter der Bühne, die Knöpfe nebeneinander, umbrechend |
| `tl-image-field__drop` | die Fläche, die ein Bild aufnimmt: auf der Bühne so groß wie sie, im Editor so groß wie eine Kachel |
| `tl-image-field__label` | die Beschriftung der Fläche, einzeilig mit Ellipse |
| `tl-image-overlay` | die Ebene über der Seite auf dem Scrim; Ränder so breit wie ein Knopf um die Mitte |
| `tl-image-overlay--view` | die Ebene der ganzseitigen Ansicht: deckt die Seite deckend dunkel, Knöpfe und Zähler auf einer eigenen Fläche |
| `tl-image-overlay__content` | der Inhalt in der Mitte der Ebene |
| `tl-image-overlay__image` | das Bild der ganzseitigen Ansicht, eingepasst in die Mitte, auch vergrößert |
| `tl-image-overlay__step` | ein Knopf zum Blättern im Rand, mit `--previous` am Zeilenanfang und `--next` am Zeilenende |
| `tl-image-overlay__counter` | der Zähler, im unteren Rand mittig, auf einer eigenen Fläche |
| `tl-image-overlay__close` | der Knopf „Schließen“, im oberen Rand am Zeilenende |
| `tl-image-editor` | der Editor: eine Fläche wie ein Dialog, die die Mitte der Ebene füllt |
| `tl-image-editor__tiles` | das umbrechende Raster der Kacheln, rollt, wenn es nicht passt |
| `tl-image-editor__tile` | die Kachel eines Bilds, alle gleich breit: oben das Bild, darunter die Knöpfe |
| `tl-image-editor__show` | der Knopf um das Bild einer Kachel |
| `tl-image-editor__image` | das Bild einer Kachel, eingepasst, nie vergrößert |
| `tl-image-editor__actions` | die Leiste der Knöpfe unter dem Bild einer Kachel, am Zeilenende |
| `tl-image-field__busy` | die durchscheinende Decke über der Bühne, solange hochgeladen wird oder die Daten des gezeigten Bilds noch nicht da sind |
| `tl-image-editor__busy` | dieselbe Decke über dem Bild einer Kachel, deren Bild ersetzt wird oder noch lädt |
| `tl-image-field__indicator` | die kleine schwebende Fläche mit dem Spinner in der Mitte der Decke |

## Attribute

| Attribut | Wann |
|---|---|
| `data-tl-state~="empty"` | an der Bühne ohne Bild |
| `data-tl-axis="horizontal"` | an den Kacheln: die Einfügemarke beim Anordnen steht neben der Kachel (`drag.css`) |
| `data-tl-state~="dragover"` | an der Bühne, der Kachel oder der Fläche, über der eine Datei schwebt (`upload.css`) |
| `data-tl-state~="dragging"`, `drop-before`, `drop-after` | an der Kachel beim Anordnen (`drag.css`) |
| `aria-current="true"` | am Knopf der Kachel des Bilds, das die Bühne zeigt |
| `aria-disabled="true"` | an der Fläche eines deaktivierten Felds |
| `aria-busy="true"` | an der Fläche, solange hochgeladen wird; an der Bühne außerdem, bis die Daten des gezeigten Bilds da sind |
| `hidden` | verbirgt die Wurzel |

## Tokens

| Teil | Token |
|---|---|
| Wurzel | Abstand `--tl-space-sm` |
| Bühne | Höhe `--tl-size-image-stage`, keine Fläche, Ecke `--tl-radius-sm`; leer ebenso hoch, ganz von der Fläche gefüllt. Bei `--single` so groß wie das Bild, das Bild höchstens `--tl-size-image-stage` hoch; leer `--tl-size-11` hoch |
| Knöpfe zum Blättern, Zähler der Bühne | Abstand zum Rand `--tl-space-sm` |
| Zähler | Innenabstand `--tl-space-2xs` `--tl-space-xs`, Fläche `--tl-surface-inverse`, Text `--tl-text-inverse`, Ecke `--tl-radius-sm` |
| Leiste | Abstand der Knöpfe `--tl-space-2xs` |
| Bild, Kachel, Fläche | Ecke `--tl-radius-sm` |
| Fläche | Rand `--tl-border-width-md` gestrichelt in `--tl-border-control`, Innenabstand `--tl-space-sm`, Abstand Icon–Beschriftung `--tl-space-xs`, Fläche `--tl-surface-field`, Text `--tl-text-secondary`; beim Zeigen Rand `--tl-border-emphasis`, Fläche `--tl-surface-interactive-hover`; deaktiviert Rand `--tl-border-disabled`, Text `--tl-text-disabled`; auf der leeren Bühne und im Editor ohne eigene Größe, sonst `--tl-size-11` in beide Richtungen |
| Ebene | Ebene `--tl-layer-overlay`, Fläche `--tl-surface-scrim`, in der ganzseitigen Ansicht `--tl-surface-lightbox` (deckend, dunkel in beiden Modi), Rand zum Viewport `--tl-space-lg`, Ränder um die Mitte so breit wie ein Knopf `--tl-size-control`, Abstand zur Mitte `--tl-space-sm` |
| Decke beim Laden | Fläche `--tl-surface-scrim`, Ecke `--tl-radius-sm`; der Spinner auf `--tl-surface-overlay` mit Innenabstand `--tl-space-sm`, Ecke `--tl-radius-md`, Rand `--tl-border-width-sm` in `--tl-border-separator` |
| Editor | Rand `--tl-border-width-sm` in `--tl-border-separator`, Ecke `--tl-radius-md`, Fläche `--tl-surface-overlay`, Schatten `--tl-shadow-dialog`, Text `--tl-text-primary`; Innenabstand der Kacheln und des Fehlers `--tl-space-md` |
| Kacheln | Spaltenbreite `--tl-size-12`, Abstand `--tl-space-sm`; Kachel auf `--tl-surface-field`, Bild `--tl-size-11` hoch mit Innenabstand `--tl-space-2xs`; Leiste mindestens `--tl-size-control-sm` hoch, Innenabstand und Abstand der Knöpfe `--tl-space-2xs` |
| Knöpfe und Zähler der ganzseitigen Ansicht | Fläche `--tl-surface-overlay`, Ecke `--tl-radius-sm`; Zähler Text `--tl-text-primary`, Innenabstand `--tl-space-2xs` `--tl-space-xs` |

## Rückmeldung beim Hochladen

Von der gewählten oder abgelegten Datei, bis das Bild, zu dem sie wird, zu sehen ist (Ereignis `load` oder `error` des
`img`), liegt über der Bühne die Decke mit dem Spinner, die Knöpfe der Leiste und der Kacheln sind deaktiviert, die
Fläche zeigt den Spinner statt des Icons und „Wird hochgeladen…“. Im Editor trägt die Kachel, deren Bild ersetzt wird,
die Decke. Jedes Bild, dessen Daten noch nicht da sind (etwa nach dem Blättern), trägt sie ebenso.

## Konformität

Ein Bildfeld besteht den Vertrag, wenn das Formular nur die Bühne zeigt, die ein Bild auf der Breite des Felds zeigt und
mehrere blättert, das Bild per Klick ganzseitig öffnet, im Bearbeiten die Leiste mit den Knöpfen trägt und mehrere Bilder
im Editor über der Seite verwaltet, die Fläche ein Bild per Klick und per Ablegen aufnimmt, Zustände Attribute sind und
im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
