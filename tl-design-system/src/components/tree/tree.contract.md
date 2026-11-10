# Vertrag: Baum

Eine flache Liste von Knoten, die Ebene steht in `aria-level` (3b §7); Leser ist `TLTreeView`. Cursor und
Auswahl wie in der Tabelle: der Cursor ist ein Attribut mit Ring, die Auswahl Markenton und Kante, wie in der Tabelle.
TopLogic garantiert Funktion, Tastatur und Fokus, nicht das Aussehen; wer ins Bild passen will, liest
die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `nodes` | Liste | Die sichtbaren Knoten in Reihenfolge, je Knoten: |
| `nodes[].id` | string | Kennung; Grundlage der Id `<controlId>-node-<id>`. |
| `nodes[].depth` | number | Ebene ab 0; `aria-level` = `depth` + 1, Einzug = Ebene × `--tl-size-control-sm`. |
| `nodes[].expandable` | boolean | Der Knoten trägt einen `tl-toggle`; sonst hält `tl-tree__spacer` seinen Platz. |
| `nodes[].expanded` | boolean | `aria-expanded` an Knoten und Toggle. |
| `nodes[].loading` | boolean | Die Kinder kommen: `aria-busy="true"` am Knoten, darunter die Ladezeile. |
| `nodes[].selected` | boolean | `aria-selected="true"`. |
| `nodes[].content` | Server-Control | Inhalt des Knotens (`tl-tree__content`). |
| `selectionMode` | `single` \| `multi` | Bei `multi` `aria-multiselectable="true"`. |
| `dragEnabled` | boolean | Knoten lassen sich ziehen (`draggable="true"`). |
| `dropEnabled` | boolean | Auf Knoten darf abgelegt werden. |
| `dropIndicatorNodeId` | string | Knoten, an dem die Einfügemarke steht. |
| `dropIndicatorPosition` | `above` \| `within` \| `below` | Wird zu `data-tl-state` `drop-before` \| `drop-onto` \| `drop-after`. |

## Aktionen

| Aktion | Auslöser |
|---|---|
| `expand` | Toggle, Pfeil rechts an einem zugeklappten Knoten |
| `collapse` | Toggle, Pfeil links an einem aufgeklappten Knoten |
| `select` | Klick auf einen Knoten, Leertaste am Cursor |
| `activate` | Doppelklick, Enter |
| `contextMenu` | Kontextmenü eines Knotens |
| `dragOver` | Ziehen über einen Knoten |
| `drop` | Ablegen auf, vor oder hinter einem Knoten |
| `dragEnd` | Ziehen beendet oder abgebrochen |

## Tastatur

Der Baum `tl-tree` ist fokussierbar (`tabindex="0"`) und hält den Fokus; der Cursor ist virtuell
(`aria-activedescendant`). Der Toggle in einem Knoten trägt `tabindex="-1"`.

| Taste | im Baum |
|---|---|
| `ArrowUp` / `ArrowDown` | Cursor einen Knoten |
| `ArrowRight` | klappt einen zugeklappten Knoten auf; an einem aufgeklappten springt der Cursor zum ersten Kind |
| `ArrowLeft` | klappt einen aufgeklappten Knoten zu; an einem zugeklappten springt der Cursor zum Elternknoten |
| `Home` / `End` | erster bzw. letzter Knoten |
| `Enter` | öffnet den Cursorknoten (`activate`) |
| `Space` | wählt den Cursorknoten |

## Semantik

| Element | Rolle und Attribute |
|---|---|
| `tl-tree` | `ul` mit `role="tree"`, `aria-label` = `js.tree.label`; `aria-multiselectable="true"` bei `multi`; `aria-activedescendant` = `<controlId>-node-<id>` |
| `tl-tree__node` | `li` mit `role="treeitem"`, `id`, `aria-level`, `aria-selected`; an aufklappbaren Knoten `aria-expanded`; `aria-busy="true"`, solange die Kinder kommen |
| Ladezeile | `li.tl-status` mit `role="treeitem"`, `aria-level` = `depth` + 2 des wartenden Knotens (`depth` zählt ab 0, sein `aria-level` ist `depth` + 1; die Ladezeile steht eine Ebene tiefer), `aria-label` = `js.tree.loading`, im Baum mit `aria-multiselectable` `aria-selected="false"` (nie wählbar, kein Cursorhalt); darin der unbestimmte `tl-progress` mit `aria-hidden="true"` und ohne eigenen Namen, damit nichts doppelt angesagt wird. Der Einzug der Ladezeile ist der ihres `aria-level`: (`aria-level` − 1) × `--tl-size-control-sm`. |

**Cursor und Fokus (WCAG 2.4.7).** `tl-tree` setzt `aria-activedescendant` nur, solange ein Knoten mit
dieser Id gerendert ist und `data-tl-state~="cursor"` trägt. Im leeren Baum oder ohne Cursor fehlt das
Attribut — dann zeigt der Baum selbst den Ring aus `base.css`. Mit dem Attribut gibt er den Ring an den
Cursorknoten ab: `base.css` zeichnet ihn innen auf dessen `::after`, darum ist der Knoten positioniert.
Ein `aria-activedescendant` auf eine Id, die es nicht gibt, ließe den Fokus unsichtbar.

## Tokens

| Teil | Token |
|---|---|
| Schrift | `--tl-text-primary` |
| Knoten | `--tl-size-row` hoch (`min-height`), Lücke `--tl-space-2xs`, rechts `--tl-space-control-x`; gezeigt `--tl-surface-interactive-hover`, ausgewählt `--tl-surface-selected`, ausgewählt und gezeigt `--tl-surface-selected-hover`, dazu die Kante (innerer Schatten `--tl-border-width-accent` links (Anfangsseite in LTR; RTL ist nicht gelöst) des Knotens in `--tl-border-selected`) |
| Einzug, Platzhalter | `--tl-size-control-sm` breit |
| Stapel | `tl-tree` isoliert (`isolation: isolate`); Ring und Marke eines Knotens liegen auf `--tl-layer-sticky` |

Schrift: `.tl-type-body` am Baum, von außen gesetzt. Ein Knoten schwebt nicht: kein Schatten; die Kante
der Auswahl ist ein innerer Schatten, kein `border-left`. Ein Knoten bricht nicht um und endet nicht mit Ellipse: `tl-tree__content` steht auf einer
Zeile (`white-space: nowrap`, auch für einen `tl-text` darin, dessen `pre-line` aus `text.css` sonst
umbräche); ist ein Knoten breiter als der Baum, rollt `tl-tree` waagerecht (`overflow: auto`). `tl-tree`
ist ein Raster aus einer Spalte (`grid-template-columns: minmax(max-content, 100%)`): so breit wie der
breiteste Knoten, mindestens aber so breit wie sichtbar. Jeder Knoten und die Ladezeile füllen diese
Spalte, Fläche und Ablagemarken reichen so über die ganze gerollte Breite; `align-content: start` hält
die Zeilen oben. Ziehen und Ablegen zeichnet `drag.css` (`drag.contract.md`), den Cursor-Ring `base.css`.

## Attribute

| Attribut | Wann |
|---|---|
| `aria-selected` | an jedem Knoten: `"true"` ausgewählt, sonst `"false"` |
| `aria-expanded` | an aufklappbaren Knoten und ihrem `tl-toggle` |
| `aria-busy` | am Knoten, dessen Kinder kommen |
| `data-tl-state~="cursor"` | am Cursorknoten (genau einer, die Id aus `aria-activedescendant`) |
| `data-tl-state~="dragging"`, `"drop-before"`, `"drop-after"`, `"drop-onto"` | am Knoten (`drag.contract.md`) |
| `draggable="true"` | an ziehbaren Knoten |
| `hidden` | an jedem Element, das gerade nicht steht |

Cursor und Ziehen sind Zustände: das Wort `cursor` und die des Ziehens stehen in `data-tl-state`, nie als
Modifier (Klassen-Gate).

Bauteile anderer Blöcke:

| Bauteil | Wo und wie |
|---|---|
| `tl-toggle` | Auf-/Zuklapper: `tabindex="-1"`, `aria-expanded`, `aria-label` wie jeder Toggle, ohne `aria-controls` — er zeigte auf den eigenen Knoten. Zeigen und Drücken mit dem veil (`toggle.contract.md`). |
| `tl-status` | Ladezeile unter einem ladenden Knoten: `li` mit `role="treeitem"`, benannt über `aria-label`; darin `tl-progress` unbestimmt und `aria-hidden`. |

Klassen:

| Klasse | Element |
|---|---|
| `tl-tree` | die Wurzel (`ul`) |
| `tl-tree__node` | ein Knoten (`li`) |
| `tl-tree__spacer` | hält den Platz des Toggles an einem Blatt frei |
| `tl-tree__content` | der Inhalt des Knotens, auf einer Zeile und ungekürzt; ein `tl-text` darin bleibt ebenso auf seiner Zeile |
