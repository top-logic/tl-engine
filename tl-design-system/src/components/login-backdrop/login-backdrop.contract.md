# Vertrag: Login-Backdrop

Der Hintergrund der Anmeldeseite: eine Zeichenfläche hinter dem Inhalt, auf der ein Halbton-Raster steht, dazu
darüber das Logo oder der Name der Anwendung auf einer eigenen Unterlage und am unteren Rand „Powered by“ mit der TopLogic-Wortmarke. TopLogic garantiert Zugänglichkeit, nicht das Aussehen; wer ins
Bild passen will, liest die Tokens.

## Zustand

Das Control heißt `TLLoginBackdrop` (Client). Es trägt diese Schlüssel:

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `child` | Control | Der Inhalt vor dem Raster, meist die Anmeldekarte. |
| `logo` | kodiertes Theme-Bild | Das Logo: URL eines Bildes, `data:`-URI oder Icon (`css:`); fehlt es, wird `tl-login-backdrop__brand` nicht gezeichnet. |
| `logoDark` | kodiertes Theme-Bild | Die Fassung für den dunklen Modus; fehlt sie, gilt `logo`. |
| `logoAlt` | Text | Der Alternativtext des Logos; fehlt er, gilt `appTitle`. |
| `appTitle` | Text | Der Name der Anwendung (`title` von `<login-backdrop>`, sonst der Ressourcenschlüssel `tl.title`). Ohne Logo steht er als `h1` auf der Unterlage. |

Fehlen Logo und Name, wird `tl-login-backdrop__brand` nicht gezeichnet. Die Fußzeile steht immer da: der Text des
Schlüssels `js.loginBackdrop.poweredBy`, dahinter die Wortmarke als `svg` mit `role="img"` und `aria-label="TopLogic"`,
gefüllt mit `currentColor` — sie folgt Modus und Theme ohne eigene Fassung.

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
| Platz für den Fokusring an der Kante der scrollenden Wurzel | `--tl-focus-width` und `--tl-focus-offset` |
| Unterlage des Logos | `--tl-surface-layer`, Rand `--tl-border-separator` in `--tl-border-width-sm`, Radius `--tl-radius-md`, Innenabstand `--tl-space-sm` und `--tl-space-md` |
| Höhe des Logos | `--tl-size-login-logo` |
| Name der Anwendung | `--tl-text-primary`, Schrift `h1` (`heading-lg`) |
| Fußzeile | `--tl-text-secondary`, Abstand nach oben `--tl-space-md`, zur Wortmarke `--tl-space-xs` |
| Höhe der Wortmarke | `--tl-size-login-wordmark` |
| Punkte (nur der Client liest sie) | `--tl-surface-halftone`, Hervorhebung `--tl-surface-halftone-strong` |
| Raster (nur der Client) | Abstand `--tl-size-halftone-step`, Radius der Hervorhebung `--tl-size-halftone-glow` |
| Bewegung (nur der Client) | Welle `--tl-duration-ripple` |

Eine Dekoration schuldet keinen Kontrast: die Punkte tragen keine Information. In Ruhe sind sie klein und blass, eine
feine Textur; erst Hervorhebung und Welle heben sie in `--tl-surface-halftone-strong` an. Bei
`prefers-reduced-motion: reduce` entfallen Hervorhebung und Welle.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-login-backdrop` | immer; die Wurzel |
| `tl-login-backdrop__canvas` | die Zeichenfläche, `aria-hidden="true"` |
| `tl-login-backdrop__content` | der Inhalt über dem Raster; ist er höher als das Fenster, scrollt die Wurzel, nicht er; was darin steht, wird nie breiter als er |
| `tl-login-backdrop__brand` | die Unterlage, nur mit Logo oder Name |
| `tl-login-backdrop__logo` | das Logo in der Unterlage: das `img` eines Bildes, oder der `span` um die Glyphe (`tl-icon-lg`) eines Icons, mit `role="img"` und `aria-label` (ohne Text `aria-hidden`) |
| `tl-login-backdrop__title` | das `h1` mit dem Namen der Anwendung, nur ohne Logo |
| `tl-login-backdrop__footer` | „Powered by“ und Wortmarke am unteren Rand |
| `tl-login-backdrop__wordmark` | das `svg` der TopLogic-Wortmarke in der Fußzeile |

## Konformität

Ein Login-Backdrop besteht den Vertrag, wenn die Zeichenfläche `aria-hidden` ist, nichts auf ihr Information trägt
und der Inhalt auch ohne Raster bedienbar bleibt.
