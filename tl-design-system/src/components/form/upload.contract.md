# Vertrag: Upload

Eine Kundenkomponente, die unter einem der TopLogic-Namen `TLFileUpload`, `TLBinaryField`, `TLUploadButton`,
`TLFileChips`, `TLAudioRecorder` oder `TLPhotoCapture` registriert ist (Stufe C der Spec), bekommt vom Server den Zustand unten, löst die
genannten Aktionen aus und trägt die genannten Attribute. TopLogic garantiert dann Funktion, Tastatur und
Fokus — nicht das Aussehen. Der Knopf ist immer ein `tl-button` (`button.contract.md`); hier liegen nur die
Hülle `tl-upload` um den Knopf und die Drop-Zone `tl-file-upload` des Datei-Feldes.

## Zustand

Vom Server über `useTLState()`.

| Control | Schlüssel | Typ | Bedeutung |
|---|---|---|---|
| `TLFileUpload` (`ReactFileUploadControl`), `TLBinaryField` (`ReactBinaryFieldControl`) | `status` | string | `idle` oder `received`: die Datei ist eingegangen. Den Zustand „lädt hoch“ hält der Client selbst. |
| `TLFileUpload`, `TLBinaryField`, `TLFileChips` | `error` | string, optional | Text des Fehlers beim letzten Hochladen: `tl-file-upload__status` mit `role="alert"`. Bei `TLFileChips` gilt er bis zum nächsten erfolgreichen Hochladen und ist ausgeblendet, sobald das nächste beginnt, bis der Server dessen Ausgang meldet. |
| `TLFileUpload`, `TLBinaryField`, `TLFileChips` | `accept` | string, optional | Filter der erlaubten MIME-Typen, am verborgenen `input type="file"`. |
| `TLFileUpload`, `TLBinaryField`, `TLUploadButton`, `TLFileChips`, `TLAudioRecorder`, `TLPhotoCapture` | `maxUploadSize` | number, optional | Höchstgröße jeder einzelnen Datei in Byte (`UploadSupport`, 0: keine). Eine Auswahl mit einer größeren Datei schickt der Upload-Kanal nicht ab, sondern meldet sie dem Server, der die Grenze nennt. |
| `TLFileChips` (`ReactFileChipsControl`) | `compact` | boolean, optional | Das Feld hat wenig Platz, etwa in der Tabellenzelle (`FieldState`): eine Zeile, die Chips rollen seitwärts, ohne Größenangabe, der Knopf zeigt nur sein Icon, der Fehler ist gekürzt mit vollem Text als Tooltip. |
| `TLBinaryField` (`ReactBinaryFieldControl`) | `hasData` | boolean | Ob eine Datei zum Herunterladen da ist; sonst „Keine Datei“. |
| `TLBinaryField` | `fileName` | string | Vorgeschlagener Dateiname des Herunterladens. |
| `TLBinaryField` | `dataRevision` | number | Zähler, der bei jedem Ersetzen der Daten steigt; der Client hängt ihn an die Abruf-Adresse, um Zwischenspeicher zu umgehen. |
| `TLBinaryField` | `editable` | boolean | Aus `ReactFormFieldControl`: bearbeitbar; sonst nur der Verweis zum Herunterladen. |
| `TLUploadButton` (`ReactUploadButtonControl`) | `accept` | string, optional | Filter am verborgenen `input type="file"`; `*` heißt kein Filter. |
| `TLUploadButton` | `multiple` | boolean, optional | Mehrere Dateien auf einmal. |
| `TLUploadButton` | `label`, `image`, `disabled`, `displayMode`, `appearance`, `tone`, `size` | wie `TLButton` | Geerbt von `ReactButtonControl`; der Knopf sieht aus wie jeder `tl-button`. |

Die Basisschlüssel des Feldes (`value`, `editable`, `label` und die übrigen) stehen in `field.contract.md`.

## Leser und Teilnamen

Alle Leser laden über denselben Upload-Kanal hoch; der Name des Teils im `multipart` sagt dem Server, was kommt.

| Control | Teil | Form |
|---|---|---|
| `TLFileUpload`, `TLBinaryField`, `TLUploadButton` | `file` | Knopf in `tl-upload` oder Drop-Zone `tl-file-upload` |
| `TLFileChips` (`ReactFileChipsControl`) | `file` | die Chip-Gruppe `tl-chip-group` ist die Drop-Zone (`choice.contract.md`); die Chips stehen in `tl-chip-group__chips`, danach der Knopf; Fehler in `tl-file-upload__status` als letztes Kind der Gruppe, das unter die Chips bricht |
| `TLValueList` (`ReactValueListControl`) bei `upload` | `file` | die Werteliste ist die Drop-Zone (`value-list.contract.md`) |
| `TLAudioRecorder` (`ReactAudioRecorderControl`) | `audio` | Knopf (`button.contract.md`) in `tl-upload`; Fehler in `tl-file-upload__status` |
| `TLPhotoCapture` (`ReactPhotoCaptureControl`) | `photo` | Knopf in `tl-upload`; Fehler in `tl-file-upload__status` |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Klick auf den Knopf | öffnet den verborgenen Dateidialog; ist der Knopf deaktiviert oder lädt er hoch, geschieht nichts. |
| Datei gewählt oder auf die Drop-Zone gelegt | Upload über den Upload-Kanal (`/react-api/upload`, `multipart`, Teil `file`); der Server liefert die Datei an `handleUpload`. Keine Server-Aktion per `sendCommand`. |
| Datei entfernen | Es gibt keinen `clear` und keine andere Entfernen-Aktion: das Control kennt nur Hochladen (und bei `TLBinaryField` Herunterladen). |
| Datei über die Drop-Zone | `data-tl-state="dragover"` bis sie verlassen oder abgelegt wird — an `tl-file-upload` wie an der Chip-Gruppe `tl-chip-group` von `TLFileChips`. |

## Tokens

| Teil | Token |
|---|---|
| Hülle `tl-upload` | Abstand `--tl-space-control-gap` |
| Drop-Zone `tl-file-upload` | Abstand `--tl-space-xs`, Ecke `--tl-radius-sm` |
| `dragover` (`tl-file-upload`, `tl-chip-group`, `tl-value-list`, eine Regel) | Linie `--tl-border-width-md`, gestrichelt, Farbe `--tl-border-interactive`, Abstand `--tl-focus-offset`; kein Fokusring |
| Status | Text `--tl-text-secondary`; als `role="alert"` `--tl-status-error-text` |
| kompakte Chip-Gruppe | Abstand `--tl-space-2xs`; Fokusringe innen (`outline-offset` −`--tl-focus-offset`): Reihe und Tabellenzelle schneiden, was hinausragt |

## Attribute

| Attribut | Wann |
|---|---|
| `aria-busy="true"` | am `tl-button`, solange hochgeladen wird; der Knopf ist dabei `disabled`. |
| `data-tl-state="dragover"` | an `tl-file-upload`, an der `tl-chip-group` von `TLFileChips` oder an der hochladenden `tl-value-list`, solange eine Datei darüber gezogen wird. |
| `data-tl-state~="compact"` | an der `tl-chip-group` von `TLFileChips` bei `compact`. |
| `disabled` | am `tl-button`: `TLUploadButton` mit `disabled` oder während des Hochladens. |
| `role="alert"` | an `tl-file-upload__status`, wenn `error` gesetzt ist oder ein Gerätefehler (Mikrofon, Kamera) vorliegt. |
| `:focus-visible` | den Ring liefert `base.css`; `dragover` ist kein Fokusring. |

Klassen:

| Klasse | Element |
|---|---|
| `tl-upload` | Hülle um den Knopf von `TLUploadButton` und das verborgene `input`; ebenso um den Knopf von `TLAudioRecorder` und `TLPhotoCapture` und ihre Statuszeile |
| `tl-file-upload` | Wurzel und Drop-Zone von `TLFileUpload` und von `TLBinaryField` im Bearbeiten (lesend ist die Wurzel `tl-file`, `file.contract.md`) |
| `tl-file-upload__status` | Status- oder Fehlertext neben dem Knopf; in der umbrechenden `tl-chip-group` von `TLFileChips` volle Breite (`flex-basis: 100%`), also unter den Chips; kompakt in der Zeile, gekürzt mit Ellipse |
| `tl-chip-group__chips` | Hülle der Chips von `TLFileChips` (`choice.contract.md`) |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets `aria-busy="true"` am Knopf
während des Hochladens, `data-tl-state="dragover"` an der Drop-Zone beim Darüberziehen und `role="alert"`
am Fehlertext trägt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt. Der Test dazu folgt
mit der ersten Kundenkomponente; bis dahin gilt dieser Text.
