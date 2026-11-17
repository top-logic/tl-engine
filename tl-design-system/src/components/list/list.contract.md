# Vertrag: Liste

Eine auswählbare, gruppierbare, durchsuchbare Liste von Objekten im Stil einer Kontakt- oder Nachrichtenliste; Leser
ist `TLDataList` (das Element `<data-list>`). Jeder Eintrag folgt einer festen Vorlage: vorn ein Avatar oder Icon,
dann eine oder mehrere Zeilen Text (primär, sekundär), hinten Metadaten oder ein Badge, daneben der Auslöser der
Befehle des Eintrags. TopLogic garantiert Funktion, Tastatur, Fokus und Ansage, nicht das Aussehen; wer ins Bild
passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `rows` | Liste | Die Einträge in Reihenfolge, je Eintrag: |
| `rows[].id` | string | Kennung; damit kommen `select` und `rowMenu` zurück. |
| `rows[].lines` | Liste | Die Zeilen, je Zeile `text` und `emphasis` (`primary` \| `secondary`); fehlt `emphasis`, ist die erste Zeile primär, jede weitere sekundär. Eine leere Zeile nach der ersten steht nicht. |
| `rows[].leading` | optional | Avatar (Name und Bild, Part `Avatar`) oder Icon vorn im Eintrag. |
| `rows[].trailing` | optional | Metadaten (Text) oder Zähler (Part `Badge`) hinten im Eintrag. |
| `rows[].group` | string, optional | Zwischenüberschrift der Gruppe; Gruppen stehen in der Reihenfolge ihres ersten Eintrags. |
| `rows[].cssClass` | string, optional | Klassen der Anwendung am Eintrag; sie gehören nicht zum Paket. |
| `selectedId` | string | Der gewählte Eintrag. |
| `title` | string | Titel im Kopf; leer steht keiner. |
| `emptyText` | string | Text der leeren Liste. |
| `search` | boolean | Die Suche steht; sie filtert auf dem Client über den Text aller Zeilen. |
| `collapsible` | boolean | Der Kopf trägt den Ein-/Ausklapper. |
| `collapsed` | boolean | Der erste Stand: eingeklappt. |
| `buttons` | Liste | Befehle der Liste, je ein Command-Button-Control. |
| `hasRowMenu` | boolean | Es gibt Befehle je Eintrag. |
| `rowActions` | `hover` \| `always` \| `none` | Wann der Auslöser der Befehle eines Eintrags steht. |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `select` | Klick auf einen Eintrag (`tl-list__main`), Enter oder Leertaste darauf |
| `rowMenu` | der Auslöser der Befehle eines Eintrags; mit der Lage des Klicks |

Suchen und Ein-/Ausklappen bleiben auf dem Client.

## Tastatur

Jeder Eintrag ist ein Knopf und liegt in der Tab-Folge, dahinter sein Auslöser. Enter und Leertaste wählen.

## Semantik

| Element | Rolle und Attribute |
|---|---|
| `tl-list` | `div`; der Titel ist der Name der Liste (`aria-label`), wenn es einen gibt |
| `tl-list__header` | `div`; steht weder Titel noch Ein-/Ausklapper, trägt er `hidden` |
| Ein-/Ausklapper | Part `Button` (ghost, klein, icon-only) mit `aria-expanded`, Name je nach Stand „Einklappen“ bzw. „Ausklappen“ |
| `tl-list__body` | `div` mit `aria-live="polite"` und `tabindex="-1"` (Ziel des Fokus, wenn der letzte Eintrag verschwindet) |
| `tl-list__group` | `section`, benannt über ihre Zwischenüberschrift (`aria-labelledby`); die einzige Gruppe ohne Überschrift über `aria-label` = Titel |
| `tl-list__subheader` | `h2` |
| `tl-list__items` | `ul` |
| `tl-list__item` | `li` |
| `tl-list__main` | `button`; am gewählten Eintrag `aria-current="true"`; `title` = die erste Zeile |
| Auslöser der Befehle | Part `Button` (ghost, klein, icon-only) mit `aria-haspopup="menu"` und dem Namen „Aktionen: “ + erste Zeile, bei `rowActions` = `hover` mit `tl-reveal-on-hover` |
| `tl-list__message` | `p`: leer, oder kein Treffer der Suche |

Verschwindet der Eintrag, dessen Befehle offen waren (Löschen), geht der Fokus auf den Auslöser des Nachbarn davor,
sonst dahinter, sonst auf `tl-list__body`.

## Tokens

| Teil | Token |
|---|---|
| Schrift | `--tl-text-primary`; Nebentext, Metadaten, Zwischenüberschrift und Meldung `--tl-text-secondary` |
| Kopf | mindestens `--tl-size-bar` hoch, innen `--tl-space-control-x` am Anfang, `--tl-space-2xs` am Ende und als Lücke |
| Befehle, Suche | innen `--tl-space-2xs` und `--tl-space-xs`, Lücke `--tl-space-2xs` |
| Körper | innen `--tl-space-2xs` und `--tl-space-xs` |
| Zwischenüberschrift, Meldung | innen `--tl-space-xs`, `--tl-space-2xs` und `--tl-space-control-x`; Abstand der Gruppen `--tl-space-xs` |
| Eintrag | Form `--tl-radius-sm`, Lücke `--tl-space-2xs`; gezeigt `--tl-surface-interactive-hover`, gewählt `--tl-surface-selected`, gewählt und gezeigt `--tl-surface-selected-hover`, dazu die Kante (innerer Schatten `--tl-border-width-accent` an der Anfangsseite in `--tl-border-selected`) |
| Knopf des Eintrags | mindestens `--tl-size-row` hoch, innen `--tl-space-2xs` und `--tl-space-control-x`, Lücke zwischen Avatar, Text und Metadaten `--tl-space-sm`; Ring innen (`--tl-focus-offset` nach innen) |
| Eingeklappt | so breit wie ein Icon-Knopf mit Rand: `--tl-size-control` + 2 × `--tl-space-2xs`; die Beschriftung eines Befehls ist nur noch sein Name (`--tl-size-hidden`) |

Typo, von außen gesetzt: Titel `.tl-type-heading-sm`, primäre Zeile `.tl-type-body`, sekundäre Zeile, Metadaten und
Meldung `.tl-type-label`, Zwischenüberschrift `.tl-type-label-strong`. Jede Zeile steht auf einer Zeile und endet mit
Ellipse. Die Liste setzt keine Fläche und keinen Rand: Breite, Fläche und Linie gibt ihr Ort.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-current="true"` | am `tl-list__main` des gewählten Eintrags |
| `data-tl-state~="collapsed"` | an der Wurzel, eingeklappt: es stehen nur der Kopf mit dem Ein-/Ausklapper und die Befehle als Icon |
| `hidden` | an jedem Element, das gerade nicht steht |

Gewählt und eingeklappt sind Zustände: Attribute, nie Modifier (Klassen-Gate).

Bauteile anderer Blöcke:

| Bauteil | Wo und wie |
|---|---|
| `tl-avatar` | Part `Avatar` in `tl-list__leading` |
| `tl-badge` | Part `Badge` in `tl-list__trailing` |
| `tl-button` | Ein-/Ausklapper, Befehle der Liste (`tl-list__commands`), Auslöser der Befehle eines Eintrags (direktes Kind von `tl-list__item`, damit `tl-reveal-on-hover` ihn beim Zeigen auf den Eintrag und bei Tastaturfokus darin zeigt) |
| `tl-field-group` | die Suche (`SearchField`) in `tl-list__search` |

Klassen:

| Klasse | Element |
|---|---|
| `tl-list` | die Wurzel |
| `tl-list__header` | Kopf: Titel und Ein-/Ausklapper |
| `tl-list__title` | Titel, auf einer Zeile mit Ellipse |
| `tl-list__commands` | die Befehle der Liste, untereinander |
| `tl-list__search` | Hülle der Suche |
| `tl-list__body` | der rollende Körper |
| `tl-list__group` | eine Gruppe |
| `tl-list__subheader` | Zwischenüberschrift einer Gruppe |
| `tl-list__items` | die Einträge einer Gruppe |
| `tl-list__item` | ein Eintrag: Knopf und Auslöser |
| `tl-list__main` | der Knopf des Eintrags: Avatar, Text, Metadaten |
| `tl-list__leading` | vorn: Avatar oder Icon |
| `tl-list__text` | die Zeilen, untereinander |
| `tl-list__primary` | eine primäre Zeile |
| `tl-list__secondary` | eine sekundäre Zeile |
| `tl-list__trailing` | hinten: Metadaten oder Badge |
| `tl-list__message` | Meldung: leer, kein Treffer |

## Konformität

Eine Liste besteht den Vertrag, wenn jeder Eintrag ein Knopf ist, der gewählte `aria-current="true"` trägt, der
Auslöser der Befehle eines Eintrags ein Part mit Namen ist und auch mit der Tastatur erscheint, Avatar und Badge
Parts sind und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
