# Vertrag: Side Rail

Eine einklappbare Spalte neben einem Inhalt, mit Abschnitten (`<side-rail>`). TopLogic garantiert Zugänglichkeit und
Verhalten, nicht das Aussehen.

## Zustand

Die Controls sind `TLSideRail` (`ReactSideRailControl`, Zustand `SideRailState`) und für die Spalte
`TLSideRailSections` (`SideRailSectionsState`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `side` | `left` \| `right` | Seite des Nachbarn; gerendert als `tl-side-rail--start`, `tl-side-rail--end`. |
| `strip` | boolean | Streifen mit einem Icon je Abschnitt. |
| `open` | boolean | Offen: die Spalte, als Schublade die Schublade (`data-tl-state~="open"`). |
| `drawerBelow` | number | Breite des Bereichs in rem, unter der die Leiste eine Schublade ist; 0: nie. |
| `sections` | Liste | Je Abschnitt `name`, `label`, `icon`, `expanded`, `hidden`, `badge`. |
| `body` | Kind | Die Spalte, ein `TLSideRailSections`. |
| `drawer` | Kind | Die modale Schublade (`TLDrawer` mit `modal`) mit der Spalte. |
| `resizable` | boolean | Eigene Breite mit Griff (`data-tl-state~="resizable"`); sonst füllt die Spalte den Bereich. |
| `width` | number | Breite der Spalte in px. |
| `sectionWidth` | number | Breite, die ein gezeigter Abschnitt verlangt; geht `width` vor. |

## Aktionen

| Aktion | Wann |
|---|---|
| `showSection` | Klick auf ein Icon des Streifens, Argument `name`: öffnet die Leiste mit dem Abschnitt, schließt sie, wenn er schon gezeigt wird |
| `toggleSection` | Klick auf den Kopf eines Abschnitts (an `TLSideRailSections`), Argument `name` |
| `setDrawer` | Der Bereich wird schmaler oder breiter als `drawerBelow`, Argument `drawer` |
| `setWidth` | Ziehen oder Pfeiltasten am Griff, Argument `width` |

## Verhalten

- **Streifen:** je Abschnitt ein Knopf ohne Rahmen, nur Icon; sein Name ist das Label des Abschnitts samt Badge. Der
  gezeigte Abschnitt ist gedrückt (`aria-pressed="true"`). Das Badge steht an der Ecke des Icons (`tl-icon-badge`).
- **Spalte:** die Abschnitte untereinander, jeder mit einem Kopf, einem Knopf mit `aria-expanded` und
  `aria-controls` auf seinen Körper (`role="region"`). Ein zugeklappter Abschnitt bleibt gemountet und ist `hidden`;
  die geschlossene Spalte ebenso.
- **Griff:** `role="separator"`, waagrecht ziehbar und mit den Pfeiltasten bedienbar; nur mit `resizable`.
- **Schublade:** ist der Bereich der Leiste - das positionierte Element um sie, an dem die Schublade verankert ist -
  schmaler als `drawerBelow`, bleibt nur der Streifen, und an Stelle der Spalte steht die modale Schublade
  (`drawer.contract.md`): Scrim, Fokus darin, Escape und Scrim schließen, der Fokus kehrt zum Streifen-Icon zurück.
  Die Leiste selbst hat keine Scrim- oder Fokuslogik.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-layer`, Schrift `--tl-text-primary` |
| Streifen | Abstand `--tl-space-2xs`, Linie `--tl-border-separator` in `--tl-border-width-sm` |
| Kopf | Höhe `--tl-size-bar`, Innenabstand `--tl-space-control-x`, Zeigen `--tl-surface-interactive-hover`, Chevron `--tl-icon-primary`, gedreht über `--tl-duration-fast` mit `--tl-easing-standard` |
| Ring | an Kopf und Griff innen, um `--tl-focus-offset` eingerückt |
| Griff | Breite `--tl-space-xs`, Linie `--tl-border-separator`, gezeigt `--tl-border-interactive` in `--tl-border-width-md` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-side-rail` + `tl-side-rail--start` \| `tl-side-rail--end` | immer, an der Wurzel |
| `data-tl-state~="open"` | offen |
| `data-tl-state~="drawer"` | der Bereich ist schmaler als `drawerBelow` |
| `data-tl-state~="resizable"` | eigene Breite mit Griff |
| `tl-side-rail__strip` | der Streifen (`nav`) |
| `tl-side-rail__column` | die Spalte |
| `tl-side-rail__handle` | der Griff |
| `tl-side-rail__sections` | die Abschnitte (`TLSideRailSections`) |
| `tl-side-rail__section`, `tl-side-rail__header`, `tl-side-rail__chevron`, `tl-side-rail__label`, `tl-side-rail__body` | ein Abschnitt |

## Konformität

Eine Side Rail besteht den Vertrag, wenn der Streifen jeden nicht verborgenen Abschnitt nennt und den gezeigten als
gedrückt meldet, zugeklappte Abschnitte gemountet und `hidden` bleiben, die Schublade die modale `tl-drawer` ist und im
Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
