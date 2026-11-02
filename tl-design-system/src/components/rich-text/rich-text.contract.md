# Vertrag: Rich Text

Der Wysiwyg-Editor: formatierter Text mit Leiste (Spec 5d §7). Tiptap ist headless — die Bibliothek liefert die
Schreibfläche und die Befehle, Leiste, Menüs und Link-Popup zeichnet das Control aus den Teilen des Pakets. Ein Update
der Bibliothek bricht darum keine Regel des Pakets. TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins
Bild passen will, liest die Tokens.

## Leser

| Control | Form |
|---|---|
| `TLWysiwygEditor` | bearbeitbar: der Rahmen `tl-field` `tl-rich-text`, darin als erstes Kind die Leiste und darunter die Schreibfläche mit `tl-prose`. Die Ansicht (nicht bearbeitbar) ist nur `tl-prose`, ohne Rahmen und ohne diesen Block |

## Aufbau

| Teil | Form |
|---|---|
| Rahmen | `tl-field` `tl-rich-text`: Rand, Zeigen und Fehler (`aria-invalid="true"`) aus `tl-field`; Leiste und Fläche untereinander. Den Fehler zeigt der Rahmen, ansagen lässt ihn die Schreibfläche: sie trägt `aria-invalid="true"` ebenso |
| Leiste | `tl-toolbar` `tl-toolbar--wrap` mit `role="toolbar"` und übersetztem `aria-label`; Gruppen `tl-toolbar__group`, dazwischen `tl-divider` `tl-divider--vertical`. Die Knöpfe sind der Part `Button` (ghost, nur Icon, klein: `small`, 24 px mit `tl-icon-sm`) unter `ButtonDefaults`, die Trigger der Menüs und des Links ebenso klein (Part `MenuTrigger`, `small`) — eine gewollte Ausnahme von der Standard-Icon-Schaltfläche, denn die Leiste gehört zum Feld (`docs/skalen.md`, Icons); ein Trenner am Umbruch trägt `data-tl-break` (`toolbar.contract.md`); Umschalter (fett, kursiv, unterstrichen, durchgestrichen, Zitat, Code) tragen `aria-pressed`. Die Befehle des Servers bleiben die eingebettete `TLToolbar`, in einer eigenen Gruppe, die schrumpfen darf (`tl-toolbar__group:has(> .tl-toolbar)`, `toolbar.contract.md`): die eingebettete Toolbar kollabiert dann in ihren Überlauf |
| Menüs | Absatzformat und Listenart: Part `MenuTrigger` und `tl-menu` (`menu.contract.md`); jeder Eintrag des Absatzformats, von dem genau einer gilt, ist `menuitemradio` mit `aria-checked`; die Einträge der Listenart sind Umschalter, `menuitemcheckbox` mit `aria-checked` (keiner gilt, solange der Text keine Liste ist; die gewählte Listenart wählen hebt die Liste auf) |
| Link | `tl-popover` `tl-popover--content` mit `label` und `tl-field` (`type="url"`), Knöpfe „Anwenden“ (primary) und, auf einem Link, „Link entfernen“ (danger). Beim Schließen kehrt der Fokus in den Text zurück (Rückgabeziel, `popover.contract.md`) |
| Schreibfläche | die Fläche der Bibliothek (`.ProseMirror`, `role="textbox"`) mit `tl-prose`: Überschriften, Listen, Code, Zitat, Link und Tabelle aus `prose.contract.md`; den Fokusring liefert `base.css`. Bei einem Fehler trägt sie `aria-invalid="true"`, folgt ihm auch, wenn er später kommt oder geht |
| Quelle | wo der Server Quellbearbeitung anbietet (master #29860): ein Umschalter der Leiste (Part `Button`, `aria-pressed`) zeigt statt der Schreibfläche eine `textarea` `tl-rich-text__source` `tl-type-code` mit `id` und `aria-labelledby` des Felds und, bei einem Fehler, `aria-invalid="true"`; die Schreibfläche ist so lange `hidden`, die Leiste zeigt nur den Umschalter |
| Behaltenes Markup | Markup ohne Schema im Editor (master #29860) steht als Quelltext im Text, nicht bearbeitbar (`contenteditable="false"`): `tl-rich-text__markup` `tl-type-code`, im Fließtext zusätzlich `tl-rich-text__markup--inline` |

## Was das Paket zeichnet

| Teil | Token |
|---|---|
| Leiste, Einzug seitlich | `--tl-space-xs` |
| Leiste, Linie zum Text | `--tl-border-width-sm` in `--tl-border-separator` |
| Schreibfläche, Innenabstand | `--tl-space-sm` oben und unten, `--tl-space-md` seitlich — an der Fläche selbst, damit ein Klick bis an den Rand die Schreibmarke setzt |
| Schreibfläche, Mindesthöhe | `--tl-size-10` |
| Schreibfläche, Fokusring | innen (`outline-offset` −`--tl-focus-offset`): die Fläche reicht bis an den Rand des Rahmens, außen läge der Ring auf ihm |
| Kopfzelle einer Tabelle | Fläche `--tl-surface-layer-nested`; Schrift und Linie aus `tl-prose` |
| Bild | höchstens so breit wie die Fläche |
| Quelle | Innenabstand und Mindesthöhe der Schreibfläche, kein eigener Rand, Fokusring innen |
| Behaltenes Markup | Linie `--tl-border-width-sm` gestrichelt in `--tl-border-separator`, Ecke `--tl-radius-sm`, Fläche `--tl-surface-layer-nested`, Schrift `--tl-text-secondary`; Abstand `--tl-space-xs` darüber und darunter, Innenabstand `--tl-space-2xs` / `--tl-space-xs`; im Fließtext ohne Abstand oben und unten |

Der Rahmen hat kein `overflow`: er schnitte den Fokusring der Knöpfe am Rand ab.

## Was das Engine-Sheet des Editors zeichnet

Klassen der Bibliothek stehen nicht im Paket (Plan-Review K3). `TLWysiwygEditor.css` zeichnet sie aus Paket-Tokens,
ohne eigene `--tl-`-Variable, ohne `outline: none`, ohne z-index:

| Teil | Regel |
|---|---|
| ausgewähltes Bild und ausgewähltes behaltenes Markup (`.ProseMirror-selectednode`) | `outline` aus `--tl-focus-width` in `--tl-focus-ring` |
| Tabelle (`.ProseMirror table`) | `width: 100%`: auch eine frisch eingefügte, leere Tabelle ist erreichbar |
| Zellen (`.ProseMirror td`, `th`) | `position: relative`: sie tragen den Spaltengriff an ihrer Endkante |
| Spaltengriff einer Tabelle (`.column-resize-handle`) | an der Endkante der Zelle, Breite `--tl-border-width-md`, Fläche `--tl-border-interactive`; fängt keinen Zeiger |
| Zeiger beim Ziehen der Spaltenbreite (`.ProseMirror.resize-cursor`) | `cursor: col-resize` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-rich-text` | am Rahmen des bearbeitbaren Editors, immer zusammen mit `tl-field` |
| `tl-field` | am Rahmen; Rand, Zeigen, Fehler |
| `tl-prose` | an der Schreibfläche, und allein an der Ansicht |
| `aria-invalid="true"` | am Rahmen (Rand) und an der Schreibfläche (`.ProseMirror`, `role="textbox"`, Ansage), wenn der Wert fehlerhaft ist |
| `tl-toolbar--wrap` | an der Leiste: sie bricht um, statt zu kollabieren |
| `tl-rich-text__source` | an der Textarea der Quelle |
| `tl-rich-text__markup`, `--inline` | am Quelltext behaltenen Markups; `--inline` im Fließtext |

## Konformität

Ein Editor besteht den Vertrag, wenn sein Rahmen `tl-field` `tl-rich-text` trägt, der Fehler `aria-invalid` an Rahmen
und Schreibfläche ist und keine Klasse, die Schreibfläche `tl-prose` trägt, die Leiste `role="toolbar"` mit Namen
trägt und jeden Knopf mit Enter und Leertaste bedienen lässt, Umschalter `aria-pressed`, die Einträge des Absatzformats
`menuitemradio` und die der Listenart `menuitemcheckbox`, beide mit `aria-checked`, setzen und im Klassen-Gate keine Klasse ohne
`tl-`-Präfix bleibt.
