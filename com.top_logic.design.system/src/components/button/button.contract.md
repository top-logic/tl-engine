# Vertrag: Button

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLButton` registriert ist (Stufe C der Spec),
bekommt vom Server den Zustand unten, löst die genannten Aktionen aus und trägt die genannten
Attribute. TopLogic garantiert dann Funktion, Tastatur und Fokus — nicht das Aussehen. Wer ins Bild
passen will, liest die Tokens.

## Zustand

Vom Server über `useTLState()`:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `label` | string | Beschriftung; bei icon-only der zugängliche Name (`aria-label`). |
| `image` | string, optional | Kodiertes ThemeImage für das Icon (`ThemeIcon`). |
| `displayMode` | `icon-only` \| `icon-label` \| `label-only`, optional | Standard `label-only`. Ohne `image` fällt `icon-only` auf `label-only` zurück. |
| `appearance` | `primary` \| `ghost` \| `link`, optional | Fehlt er, gilt die Vorgabe des Containers (`ButtonDefaults`), sonst `secondary`. |
| `tone` | `danger`, optional | Destruktive Aktion. Nicht mit `link`. |
| `size` | `small`, optional | Nur mit `displayMode: icon-only` wirksam (`--sm`). |
| `disabled` | boolean | Der Button ist `disabled`. |
| `active` | boolean | Der Button ist gedrückt oder die geltende Alternative: `aria-pressed="true"`. |
| `hidden` | boolean | Rendert nichts. Die Tastenbindung bleibt registriert und lehnt ab. |
| `tooltip` | string, optional | Tooltip-Text; bei icon-only Rückfall auf `label`. |
| `keyGesture` | string, optional | Tastengeste im umgebenden Scope (`useKeyboardBinding`). |
| `navigateUrl`, `navigateNewWindow` | string, boolean, optional | Klick navigiert statt zu senden. |
| `cssClasses` | string, optional | Zusätzliche Klassen des Kommandos; werden an `className` durchgereicht. |

## Aktionen

| Auslöser | Aktion |
|---|---|
| Klick, Enter, Leertaste | `sendCommand('click')` — oder `navigateUrl` öffnen, wenn gesetzt. |
| Tastengeste `keyGesture` | dasselbe wie Klick; `false` zurückgeben, wenn `disabled` oder `hidden`. |

## Tokens

Fläche, Beschriftung und Linie je Erscheinung; Hover und Active eine Stufe weiter auf derselben Rampe.

| Erscheinung | Fläche | Beschriftung | Linie |
|---|---|---|---|
| `secondary` (Default) | `--tl-surface-interactive-hover` / `--tl-surface-interactive-active` beim Zeigen und Drücken | `--tl-text-primary` | `--tl-border-control`, Breite `--tl-border-width-sm` |
| `primary` | `--tl-surface-brand`, `--tl-surface-brand-hover`, `--tl-surface-brand-active` | `--tl-text-on-brand`, `--tl-text-on-brand-hover`, `--tl-text-on-brand-active` | — |
| `ghost` | wie secondary beim Zeigen und Drücken | `--tl-text-primary` | — |
| `link` | — | `--tl-text-link`, `--tl-text-link-hover` | — |
| `danger` + `primary` | `--tl-status-error-surface`, `--tl-status-error-surface-hover`, `--tl-status-error-surface-active` | `--tl-text-on-status-error`, `--tl-text-on-status-error-hover`, `--tl-text-on-status-error-active` | — |
| `danger` + `secondary` / `ghost` | Zeigen: `--tl-status-error-surface` (Beschriftung `--tl-text-on-status-error`); Drücken: `--tl-status-error-surface-active` (Beschriftung `--tl-text-on-status-error-active`) | `--tl-status-error-text` (Grundzustand, nicht gezeigt/gedrückt) | `--tl-status-error-border` (nur secondary; folgt beim Zeigen und Drücken der Fläche) |
| gedrückt (`aria-pressed`) | `--tl-surface-brand-subtle`, `--tl-surface-brand-subtle-hover` | `--tl-text-primary` | `--tl-border-interactive` |
| `danger` + gedrückt (`aria-pressed`) | gefüllt rot wie `danger` + `primary`: `--tl-status-error-surface`, `--tl-status-error-surface-hover`, `--tl-status-error-surface-active` | `--tl-text-on-status-error`, `--tl-text-on-status-error-hover`, `--tl-text-on-status-error-active` | folgt der Fläche (nicht `--tl-border-interactive`) |
| `disabled` (vom Kontrast-Gate ausgenommen) | `--tl-surface-disabled` | `--tl-text-disabled` | `--tl-border-disabled` |
| `ghost` / `link` + `disabled` (vom Kontrast-Gate ausgenommen) | — | `--tl-text-disabled` | — |

Maße: Höhe `--tl-size-control` (klein `--tl-size-control-sm`), Innenabstand `--tl-space-control-x` (klein `--tl-space-2xs`), Abstand Icon–Text `--tl-space-xs`, Ecke `--tl-radius-sm`. Bewegung: `--tl-duration-fast` mit `--tl-easing-standard`; der Puls von `aria-busy` `--tl-duration-loop`. Icon: Klasse `.tl-icon-sm` neben Text, `.tl-icon-md` allein. Text: Klasse `.tl-type-body`.

`--tl-status-error-subtle` wird vom Button nicht gelesen — die getönte Fläche bleibt dem
Hinweisstreifen vorbehalten (siehe Kommentarkopf in `button.css`).

## Attribute

| Attribut | Wann |
|---|---|
| `type="button"` | immer — kein Submit. |
| `disabled` | `state.disabled`. Nicht `aria-disabled`: der Button soll wirklich nicht bedienbar sein. Der Zeiger erreicht ihn trotzdem (kein `pointer-events: none`), damit sein Tooltip den Grund nennt (#29675); Zeigen und Drücken ändern sein Aussehen nicht. |
| `aria-pressed="true"` | `state.active`. Fehlt sonst ganz (nicht `false`), damit ein gewöhnlicher Button kein Umschalter wird. |
| `aria-busy="true"` | während einer laufenden Aktion (Upload). Das Icon pulsiert (`tl-button-busy`, `duration-loop`); der Zeiger zeigt Fortschritt nur dort, wo der Knopf nicht `disabled` ist — bei `disabled` ist der Puls der einzige Hinweis, weil `:disabled:hover` den Zeiger auf `default` hält. Der Handler ignoriert Klicks, solange die Aktion läuft. |
| `tl-button--icon` | quadratisch, ohne `__label`; Pflicht `aria-label` und Tooltip; mit `--sm` 24 px |
| Kopfknopf | `tl-button--ghost tl-button--sm tl-button--icon` mit `ThemeIcon` in `tl-titlebar` (Maximieren, Schließen) und als `tl-alert__close`: 24 px (`--tl-size-control-sm`), `aria-label` und Tooltip Pflicht |
| `aria-label` | bei icon-only oder wenn ein Icon gesetzt ist: der `label`. |
| `data-tooltip` | `text:<tooltip>`; das Tooltip-System liest es. |
| `:focus-visible` | den Ring liefert `base.css`; die Komponente setzt und entfernt keinen Fokusstil. |

## Leser: Medienknöpfe

Die Medien-Controls (Welle 5b §3) zeichnen keine eigenen Knöpfe; jeder ist ein Part `Button`, icon-only
(`tl-button--icon`).

| Control | Erscheinung | Zustände |
|---|---|---|
| `TLAudioRecorder` | `secondary`, Tönung `danger` | Aufnahme läuft: `aria-pressed="true"` — gedrückt in `danger` ist gefüllt rot, das Icon wird zum Stopp; Upload: `aria-busy="true"` und `disabled` |
| `TLAudioPlayer` | `secondary` | lädt: `aria-busy="true"`; ohne Audio `disabled` mit Tooltip |
| `TLPhotoCapture` | `secondary` | Upload: `aria-busy="true"` |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jeden Zustand
oben das jeweilige Attribut trägt, auf Klick und Enter `click` sendet, bei `hidden` nichts rendert
und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt. Ein Icon-Knopf ohne `aria-label` fällt durch. Der Test dazu folgt mit der
ersten Kundenkomponente; bis dahin gilt dieser Text.

## Container-Kontext (`ButtonDefaults`)

`appearance` ist die Erscheinung; `menu-item` rendert als `button.tl-menu__item` (siehe
`menu.contract.md`) und **schlägt die Server-Erscheinung** — ein Eintrag ist Struktur. `iconOnly`:
ein Knopf, der sein Icon zeigt, lässt sein Label weg und wird `--icon`; ein Knopf ohne Icon oder mit
`label-only` bleibt, wie er ist.
