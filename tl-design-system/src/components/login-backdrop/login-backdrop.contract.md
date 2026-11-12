# Vertrag: Login-Backdrop

Der Hintergrund der Anmeldeseite: eine Zeichenfläche hinter dem Inhalt, auf der ein Halbton-Raster steht, dazu
optional das Logo auf einer eigenen Unterlage. TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins
Bild passen will, liest die Tokens.

## Zustand

Das Control heißt `TLLoginBackdrop` (Client). Es trägt diese Schlüssel:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `child` | Control | Der Inhalt vor dem Raster, meist die Anmeldekarte. |
| `logo` | URL | Das Logo; fehlt es, wird `tl-login-backdrop__brand` nicht gezeichnet. |
| `logoDark` | URL | Die Fassung für den dunklen Modus; fehlt sie, gilt `logo`. |
| `logoAlt` | Text | Der Alternativtext des Logos. |

Es gibt nur ein `img`: seine Quelle folgt dem Modus (`data-tl-mode` an `<html>`), das Stylesheet kennt keine
Modus-Fassungen.

## Aktionen

Keine Server-Aktion. Ein Klick auf den Hintergrund (nicht auf den Inhalt) löst im Client eine Welle aus.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-base` |
| Rand der Wurzel | `--tl-space-lg` |
| Abstand im Inhalt | `--tl-space-md` |
| Platz für den Fokusring im scrollenden Inhalt | `--tl-focus-width` und `--tl-focus-offset` |
| Unterlage des Logos | `--tl-surface-layer`, Rand `--tl-border-separator` in `--tl-border-width-sm`, Radius `--tl-radius-md`, Innenabstand `--tl-space-sm` und `--tl-space-md` |
| Höhe des Logos | `--tl-size-login-logo` |
| Punkte (nur der Client liest sie) | `--tl-surface-halftone`, Hervorhebung `--tl-surface-halftone-strong` |
| Raster (nur der Client) | Abstand `--tl-size-halftone-step`, Radius der Hervorhebung `--tl-size-halftone-glow` |
| Bewegung (nur der Client) | Welle `--tl-duration-ripple`, Formwechsel `--tl-duration-morph` |

Eine Dekoration schuldet keinen Kontrast: die Punkte tragen keine Information. Bei `prefers-reduced-motion: reduce`
entfallen Hervorhebung und Welle, der Formwechsel geschieht ohne Überblenden.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-login-backdrop` | immer; die Wurzel |
| `tl-login-backdrop__canvas` | die Zeichenfläche, `aria-hidden="true"` |
| `tl-login-backdrop__content` | der Inhalt über dem Raster; scrollt, wenn er höher ist als das Fenster |
| `tl-login-backdrop__brand` | die Unterlage des Logos, nur mit Logo |
| `tl-login-backdrop__logo` | das `img` in der Unterlage |

## Konformität

Ein Login-Backdrop besteht den Vertrag, wenn die Zeichenfläche `aria-hidden` ist, nichts auf ihr Information trägt
und der Inhalt auch ohne Raster bedienbar bleibt.
