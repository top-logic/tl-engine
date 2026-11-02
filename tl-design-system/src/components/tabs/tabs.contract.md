# Vertrag: Tabs

Reiter, die zwischen Inhalten desselben Orts wechseln. TopLogic garantiert Zugänglichkeit, nicht das
Aussehen; wer ins Bild passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `tabs` | Liste | Die Reiter in ihrer Reihenfolge; je Reiter `id`, `label`, optional `icon`. |
| `selectedId` | string | Id des gewählten Reiters; genau einer ist gewählt. |

## Aktionen

| Aktion | Wirkung |
|---|---|
| `selectTab` | wählt den Reiter `tabId`; sein Panel erscheint, die anderen sind `hidden` |

## Tastatur

Roving-Tabindex: der gewählte Reiter trägt `tabindex="0"`, alle anderen `tabindex="-1"`. Pfeile wählen
und fokussieren in einem Schritt.

| Taste | Wirkung |
|---|---|
| `ArrowRight`, `ArrowLeft` | nächster, vorheriger Reiter wird gewählt und fokussiert; am Ende geht es vorn weiter (zyklisch) |
| `Home`, `End` | erster, letzter Reiter wird gewählt und fokussiert |
| `Tab` | vom Reiter ins Panel |

## Überlauf

Sind die Reiter breiter als ihr Behälter, scrollt die Reiterreihe (`tl-tabs__list`) waagerecht in sich, ohne
sichtbaren Balken; kein Vorfahr scrollt mit. Behälter und Leiste schrumpfen auf ihren Platz (`min-width: 0`).

Die Leiste (`tl-tabs__strip`) trägt die Reiterreihe zwischen zwei Scrollknöpfen. Die Knöpfe stehen außerhalb der
Reiterreihe und bleiben stehen, während sie scrollt. Ob es sie gibt, hängt nur am Überlauf: passen die Reiter in die
Leiste (mit dem Platz der Knöpfe), tragen beide `hidden` und nehmen keinen Platz; laufen sie über, sind beide da und
nehmen ihren Platz. Ein Knopf ohne Verdecktes in seiner Richtung ist `disabled` und unsichtbar, behält aber seinen
Platz — sichtbar ist ein Knopf also nur, solange in seiner Richtung etwas verdeckt ist. So ändert Scrollen die Breite
der Reiterreihe nie; sie ändert sich nur, wenn der Überlauf beginnt oder endet oder der Behälter seine Breite ändert.
Das Umschalten pendelt nicht: die Knöpfe erscheinen erst, wenn die Reiter auch ohne sie nicht passen, und gehen erst,
wenn sie ohne sie passen. Ein Druck scrollt um etwa eine sichtbare Breite der Reiterreihe.

Die Knöpfe sind eine Hilfe für den Zeiger: `tabindex="-1"`, kein Tab-Halt. Die Tastatur bleibt bei den Pfeilen,
`Home` und `End` (oben); der Fokus wandert ohne Scrollen der Vorfahren (`preventScroll`).

Der gewählte Reiter wird sichtbar gehalten, sobald die Wahl wechselt (Zeiger, Tastatur, Server), beim Erscheinen
wenn der Überlauf beginnt oder endet und wenn sich der Platz der Leiste oder ihr Inhalt ändert (Behälter schmaler
oder breiter, aus `hidden` gezeigt, Reiter hinzu, weg, umbenannt). Ein Scrollen mit den Knöpfen macht nichts davon
rückgängig. Gescrollt wird dafür nur die Reiterreihe (`scrollLeft`), nie über
`scrollIntoView`, das jeden Vorfahren mitscrollte. Ein schon sichtbarer Reiter bewegt nichts.

Die Engine bündelt das in `useScrollStrip` und der Leiste `TabStrip`; alle Reiterleisten nutzen beide.

## Tokens

| Teil | Token |
|---|---|
| Reiterreihe, Abstand der Reiter | `--tl-space-xs` |
| Leiste, Innenabstand seitlich | `--tl-space-sm` |
| Leiste, Linie darunter (läuft unter den Scrollknöpfen durch) | `--tl-border-width-sm`, `--tl-border-separator` |
| Scrollknopf | `tl-button--icon --sm --ghost`; Icon Chevron links, rechts |
| Reiter, Höhe | `--tl-size-bar` (so hoch wie die Toolbar) |
| Reiter, Abstände | `--tl-space-md` seitlich, `--tl-space-xs` zwischen Icon und Label |
| Reiter, Text | `--tl-text-secondary`; der gewählte `--tl-text-primary` |
| Zeiger darüber | `--tl-text-primary`, `--tl-surface-interactive-hover` |
| Akzentlinie unter dem gewählten Reiter | `--tl-border-width-accent`, `--tl-border-interactive` |

Den Fokusring liefert `base.css` über `:focus-visible`; am Reiter liegt er innen (`outline-offset`
−`--tl-focus-offset`), weil die Reiterreihe scrollt und schneidet. Der gewählte Reiter hat kein eigenes Gewicht:
nichts springt beim Wechsel; alle Reiter tragen `tl-type-body`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-tabs` | am Behälter aus Leiste und Panels |
| `tl-tabs__strip` | an der Leiste aus den Scrollknöpfen und der Reiterreihe |
| `tl-tabs__list` | an der Reiterreihe, mit `role="tablist"`; einziges scrollendes Element |
| `tl-tabs__scroll` + `data-tl-edge` | an jedem Scrollknopf, `start` vor, `end` hinter der Reiterreihe; ein `<button type="button">` mit `tl-button tl-button--icon tl-button--sm tl-button--ghost`, `tabindex="-1"` und `aria-label` (`js.tabs.scrollStart`, `js.tabs.scrollEnd`); `hidden` ohne Überlauf, `disabled` (unsichtbar, Platz bleibt) bei Überlauf, solange in seiner Richtung nichts verdeckt ist |
| `tl-tabs__tab` | an jedem Reiter; ein `<button type="button">` mit `role="tab"` und `tl-type-body` |
| `aria-selected="true"` | am gewählten Reiter; die anderen tragen `aria-selected="false"` |
| `aria-controls` | am Reiter, mit der Id seines Panels; das Panel trägt `aria-labelledby` mit der Id des Reiters |
| `tabindex` | `0` am gewählten, `-1` an den anderen Reitern |
| `tl-tabs__panel` | am Inhalt; `role="tabpanel"`; ein nicht gewähltes Panel trägt `hidden` |

## Konformität

Tabs bestehen den Vertrag, wenn die Reiterreihe `role="tablist"` trägt, jeder Reiter ein `<button>` mit
`role="tab"`, `aria-selected` und `aria-controls` ist, genau einer `aria-selected="true"` trägt, jedes
Panel `role="tabpanel"` mit `aria-labelledby` hat, die Reiterreihe bei Überlauf allein scrollt und die Scrollknöpfe nur
in verdeckter Richtung zeigt, ohne dass Scrollen ihre Breite ändert, die Tastatur oben vollständig wirkt und im
Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.

## Leser

Der Picker (`tl-picker__tabs`) liest die Reiter und ihre Tastatur über `useTabList`.
