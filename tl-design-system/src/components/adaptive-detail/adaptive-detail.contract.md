# Vertrag: AdaptiveDetail

Responsives Master-Detail. Den Wechsel zwischen Split, Auswahl mit Drawer und Auswahl/Detail entscheidet der Server.
TopLogic garantiert Navigation und Fokus, nicht das Aussehen.

## Zustand

`content`, `overlay`, `breadcrumb[{depth,label}]`. Der erste Eintrag heißt ohne Konfiguration „Übersicht“.

## Aktionen

| Befehl | Wann |
|---|---|
| `navigate` | Stufe der Breadcrumb; Argument `depth` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-adaptive-detail` | Wurzel, Behälter und Anker des Drawers |
| `tl-adaptive-detail__bar` | Leiste um `tl-breadcrumb`; die letzte Krume trägt `aria-current="page"`, `aria-label` aus `js.breadcrumb.label` |
| `tl-adaptive-detail__content` | Inhalt |
| `tl-drawer--container` | der Detail-Drawer, im Behälter verankert |
