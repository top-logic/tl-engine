# Vertrag: AppBar

Die Leiste am oberen Rand einer Anwendung. TopLogic garantiert Landmarke, Reihenfolge und Nachgeben, nicht das
Aussehen.

## Zustand

Das Control ist `TLAppBar` (`ReactAppBarControl`, Zustand `AppBarState` in `state.proto`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `title` | string | Titel, Überschrift `h1` |
| `variant` | `flat` \| `elevated` | `elevated`: `tl-app-bar--elevated`, eine Linie unten |
| `leading` | Kind, optional | Anfang, z. B. der Hamburger der Sidebar |
| `children` | Kinder | neben dem Titel, z. B. die Breadcrumb |
| `actions` | Kind, optional | eine `tl-toolbar`, Knöpfe ghost |
| `trailing` | Kind, optional | Ende, z. B. das Benutzermenü |

## Aktionen

Keine eigenen; die Kinder senden ihre.

## Verhalten

- Wird die Leiste schmal, gibt zuerst eine Toolbar mit `data-tl-overflow` nach (Labels, dann Überlaufmenü) bis zu
  ihrem Überlaufknopf; erst dann kürzt der Titel mit Ellipse.
- Eine Toolbar ohne Befehl rendert nichts; ihre Hülle verschwindet.

## Tokens

| Teil | Token |
|---|---|
| Höhe | `--tl-size-shell-bar` |
| Fläche, Schrift | `--tl-surface-layer`, `--tl-text-primary`; Titel `tl-type-heading-sm` |
| Linie (`--elevated`) | `--tl-border-width-sm`, `--tl-border-separator` |
| Abstände | `--tl-space-xs`, `--tl-space-sm` |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-app-bar` | immer, an `<header>` |
| `tl-app-bar--elevated` | Variante `elevated` |
| `tl-app-bar__leading`, `tl-app-bar__children`, `tl-app-bar__trailing` | um die Kinder |
| `tl-app-bar__title` | am `h1`, mit `tl-type-heading-sm` |
| `tl-app-bar__actions` | um die Toolbar; mit `data-tl-overflow` an der Toolbar gibt sie zuerst nach |

## Konformität

Eine AppBar besteht den Vertrag, wenn sie `<header>` mit genau einem `h1` ist, eine überlaufende Toolbar vor dem
Titel nachgibt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
