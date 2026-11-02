# Vertrag: AppShell

Der Rahmen einer Anwendung: Kopf, systemweite Hinweise, Inhalt, Fuß, Snackbar. Er misst als einziger, ob der
Bildschirm schmal ist. TopLogic garantiert Landmarken und Verhalten, nicht das Aussehen.

## Zustand

Das Control ist `TLAppShell` (`ReactAppShellControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `header` | Kind, optional | Kopf, meist die AppBar |
| `notices` | Kind, optional | Hinweise zwischen Kopf und Inhalt |
| `content` | Kind | Inhalt, füllt den Rest |
| `footer` | Kind, optional | Fuß, meist die BottomBar |
| `snackbar` | Kind | Meldungsdienst |

## Aktionen

| Aktion | Wann |
|---|---|
| `reportDisplayClass` | beim Start und bei jedem Wechsel über den Breakpoint: `COMPACT` oder `REGULAR` |

## Verhalten

- Der Breakpoint (768 px Fensterbreite) steht nur hier. Die Shell meldet ihn dem Server, gibt ihn den Komponenten in
  ihr als Kontext (`useDisplayClass`) und setzt `data-tl-display` an ihrer Wurzel. Eine Komponente außerhalb einer
  Shell ist `regular`.
- Der Inhalt ist das Landmark `<main>`.

## Tokens

Keine Fläche, kein Maß der Shell selbst; Kopf und Fuß bringen ihre (`--tl-size-shell-bar`).

## Attribute

| Attribut | Wann |
|---|---|
| `tl-app-shell` | immer |
| `data-tl-display="compact"` \| `"regular"` | immer; eine Regel für schmale Bildschirme liest `[data-tl-display~="compact"]` |
| `tl-app-shell__header`, `tl-app-shell__notices`, `tl-app-shell__footer` | um Kopf, Hinweise, Fuß |
| `tl-app-shell__content` | an `<main>` um den Inhalt |

## Konformität

Eine Shell besteht den Vertrag, wenn ihr Inhalt in `<main>` steht, sie `data-tl-display` setzt und keine Regel des
Pakets den Breakpoint wiederholt.
