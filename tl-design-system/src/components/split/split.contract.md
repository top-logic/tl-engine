# Vertrag: Split

Flächen nebeneinander (`horizontal`) oder untereinander (`vertical`), dazwischen ein Griff. TopLogic garantiert
Bedienung mit Maus und Tastatur, nicht das Aussehen.

## Zustand

Das Control ist `TLSplitPanel` (`ReactSplitPanelControl`): `orientation`, `resizable`, `children[]` mit `size`,
`unit`, `minSize`, `scrolling`, `collapsed`.

## Aktionen

| Befehl | Wann |
|---|---|
| `updateSizes` | nach dem Ziehen und nach dem Loslassen einer Taste; Argument `sizes` (Pixel je Fläche) |
| `resetSizes` | Doppelklick auf den Griff: die konfigurierte Aufteilung gilt wieder, die persönliche wird vergessen |

Tasten am Griff: Pfeile quer zum Griff um `--tl-space-md` (rechts-nach-links gespiegelt), `Home` an die kleinste,
`End` an die größte Größe der Fläche davor.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-split` `tl-split--horizontal` \| `tl-split--vertical` | Wurzel; alle eingeklappt: `data-tl-state~="all-collapsed"` |
| `tl-split__pane` | je Fläche, mit `id`; eingeklappt im waagrechten Split `data-tl-state~="collapsed"` |
| `tl-split__handle` | zwischen zwei offenen Flächen: `role="separator"`, `tabindex="0"`, `aria-orientation` (`vertical` zwischen Flächen nebeneinander), `aria-controls` auf die Fläche davor, `aria-valuenow` (Anteil der Fläche davor in Prozent), `aria-valuemin`, `aria-valuemax`, `aria-label` aus i18n |

## Tokens

| Teil | Token |
|---|---|
| Trefferzone | `--tl-space-xs` |
| Linie | `--tl-border-width-sm` `--tl-border-separator`; aktiv `--tl-border-width-md` `--tl-border-interactive` |

## Konformität

Ein Split besteht den Vertrag, wenn jeder Griff mit der Tastatur bedienbar ist und seinen Wert meldet.
