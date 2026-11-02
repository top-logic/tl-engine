# Vertrag: Sidebar

Die Navigationsleiste einer Anwendung: Seiten, Befehle, Gruppen; eingeklappt eine Icon-Rail, auf schmalen
Bildschirmen eine modale Schublade. TopLogic garantiert Tastatur, Landmarke und „hier bist du“, nicht das Aussehen.

## Zustand

Das Control ist `TLSidebar` (`ReactSidebarControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `items` | Liste | Einträge: `nav` (Seite), `command` (Befehl), `header`, `separator`, `group` mit `children` und `expanded` |
| `activeItemId` | string | die aktuelle Seite: `aria-current="page"` |
| `collapsed` | boolean | eingeklappt (gespeichert): `data-tl-state~="collapsed"` |
| `drawerOpen` | boolean | kompakt: die Schublade ist offen (flüchtig) |
| `headerContent`, `footerContent` | Kind, optional | Kopf und Fuß; eingeklappt `headerCollapsedContent`, `footerCollapsedContent` |
| `activeContent` | Kind | die Ansicht der aktuellen Seite |

## Aktionen

| Aktion | Wann |
|---|---|
| `selectItem` | Klick, Enter oder Leertaste auf einer Seite |
| `executeCommand` | dasselbe auf einem Befehl |
| `toggleGroup` | eine Gruppe auf- oder zuklappen (ausgeklappte Rail) |
| `toggleCollapse` | der Knopf am Fuß der Rail |
| `toggleDrawer` | Escape oder Klick auf den Scrim schließt die Schublade; der Hamburger (`TLDrawerToggle`) sendet `toggle` |

## Verhalten

- **Rail:** Roving-Tabindex; Pfeil auf/ab zyklisch, Pos1, Ende; Enter und Leertaste lösen aus; Pfeil rechts/links
  öffnet und schließt eine Gruppe. Eingeklappt öffnet eine Gruppe ihr Flyout: das Menü (`Menu`, `role="menu"`) an der
  Lage `right-start`, mit dem ersten Eintrag im Fokus; Escape und Außenklick geben den Fokus an den Gruppenknopf zurück.
- **Hier bist du:** die aktuelle Seite trägt `aria-current="page"` (Fläche und Gewicht); liegt sie in einer
  zugeklappten Gruppe, trägt der Gruppenkopf `data-tl-state~="contains-current"` (Fläche).
- **Kompakt** (die AppShell meldet `compact`): die Nav steht in einem `tl-drawer` mit Scrim (`tl-dialog`),
  `role="dialog"`, `aria-modal="true"`; der Fokus ist gefangen, Escape und Scrim schließen, der Fokus kehrt zum
  Hamburger zurück. Der Hamburger trägt `aria-expanded` und, solange offen, `aria-controls` auf die Nav.

## Tokens

| Teil | Token |
|---|---|
| Fläche, Linie | `--tl-surface-layer`, `--tl-border-separator` |
| Breite | `--tl-size-sidebar`; eingeklappt `--tl-size-row` plus `--tl-space-2xs` je Seite |
| Aktuelle Seite (`aria-current="page"`) | `--tl-surface-selected` (gezeigt `--tl-surface-selected-hover`) und die Kante (innerer Schatten `--tl-border-width-accent` links (Anfangsseite in LTR; RTL ist nicht gelöst) in `--tl-border-selected`), `tl-type-body-strong` |
| Kopf einer zugeklappten Gruppe, die die Seite enthält (`contains-current`) | nur die Fläche `--tl-surface-selected` (gezeigt `--tl-surface-selected-hover`), keine Kante |
| Bewegung | Breite `--tl-duration-base` `--tl-easing-standard`; Chevron `--tl-duration-fast` |
| Fuß | `--tl-size-bar` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-sidebar` | Wurzel |
| `data-tl-state~="collapsed"` | eingeklappt |
| `tl-sidebar__nav` | an `<nav aria-label>`; `id` = Control-Id + `-nav` |
| `tl-menu__item tl-sidebar__item` | jeder Eintrag, `button type="button"` |
| `aria-current="page"` | an der aktuellen Seite |
| `aria-expanded` | am Gruppenkopf; eingeklappt mit `aria-haspopup="menu"` (Flyout offen) |
| `data-tl-state~="contains-current"` | am Kopf einer zugeklappten Gruppe mit der aktuellen Seite |
| `tl-sidebar__chevron` | Chevron im Gruppenkopf |
| `tl-icon-badge` | eingeklappt um Icon und Zähler |
| `tl-sidebar__collapse` | um den Knopf am Fuß; `aria-controls` des Knopfs auf die Nav |
| `tl-drawer tl-drawer--start tl-drawer--narrow` in `tl-dialog`, `role="dialog"`, `aria-modal="true"` | kompakt, offen |

## Konformität

Eine Sidebar besteht den Vertrag, wenn die aktuelle Seite `aria-current="page"` trägt, jeder Gruppenkopf
`aria-expanded`, die Rail per Tastatur vollständig bedienbar ist, kompakt die Schublade modal ist und den Fokus
zurückgibt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
