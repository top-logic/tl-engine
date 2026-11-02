# Vertrag: Slider

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLSlider` registriert ist (Stufe C der Spec), zeigt eine Zahl als
natives `range` mit dem Wert als Text daneben. TopLogic garantiert Tastatur, Screenreader und Zustände, nicht das
Aussehen.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `value`, `min`, `max`, `step` | number | Wert und Grenzen des `range`. |
| `valueText` | string | Der Wert als Text, auch als `aria-valuetext`. |
| `editable`, `disabled` | boolean | Der Slider ist deaktiviert, wenn nicht bearbeitbar. |
| `hasError`, `hasWarnings`, `mandatory` | boolean | Feldzustand. |

## Aktionen

| Befehl | Wann |
|---|---|
| `valueChanged` | Der Wert ändert sich durch Ziehen oder Tastatur. |

## Attribute

| Attribut | Wann |
|---|---|
| `aria-valuetext` | am `range`, der Wert als Text |
| `aria-invalid="true"` | Fehler, am `range` |
| `aria-required="true"` | Pflichtfeld, am `range` |
| `data-tl-state~="warning"` | Warnung, an der Wurzel `tl-slider`, nur ohne Fehler |

## Klassen und Tokens

| Klasse | Teil |
|---|---|
| `tl-slider` | Wurzel, Gitter aus Spur und Wert |
| `tl-slider__track` | Ruhespur in `--tl-border-control`, um den halben Daumen eingerückt |
| `tl-slider__fill` | Füllung in `--tl-surface-brand`; das Control setzt ihre Breite inline als Anteil |
| `tl-slider__input` | das native `range`; seine Spur ist durchsichtig, der Daumen `--tl-size-choice` in `--tl-surface-brand` |
| `tl-slider__value` | Wert als Text |
| `tl-slider__text` | der aktuelle Wert |
| `tl-slider__sizer` | die Grenzen, unsichtbar: sie geben dem Kasten seine Breite |

Der Daumen zeigt beim Zeigen und Drücken den Hof aus `--tl-surface-state-hover` und `--tl-surface-state-active` (ein
Daumen ist eine Marke). Den Fokusring um das ganze `range` liefert `base.css`.

## Konformität

Ein Slider besteht den Vertrag, wenn die Füllung ein Element ist, Zustände Attribute sind und der Fokus über
`:focus-visible` aus `base.css` läuft.
