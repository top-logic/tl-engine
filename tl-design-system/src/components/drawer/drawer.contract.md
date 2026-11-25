# Vertrag: Drawer

Eine Schublade vom Rand: Detail zu einer Liste, Navigation auf kleinen Bildschirmen. TopLogic garantiert
Zugänglichkeit und Verhalten, nicht das Aussehen.

## Zustand

Das Control ist `TLDrawer` (`ReactDrawerControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `open` | boolean | Offen: `data-tl-state~="open"`. |
| `position` | `left` \| `right` \| `bottom` | Rand; gerendert als `tl-drawer--start`, `--end`, `--bottom`. |
| `size` | `narrow` \| `medium` \| `wide` | Maß. |
| `anchor` | `viewport` \| `container` | Am Viewport oder im Behälter (`tl-drawer--container`). |
| `width` | number, optional | Ausdehnung in px statt des Maßes (Breite, unten Höhe). |
| `title` | string, optional | Mit Titel steht ein `tl-titlebar` mit Schließen-Knopf. |
| `modal` | boolean, optional | Modal: mit Scrim, Fokus gefangen (`ReactDrawerControl.setModal`). |
| `label` | string, optional | Zugänglicher Name einer Schublade ohne Titel (`aria-label`). |

## Aktionen

| Aktion | Wann |
|---|---|
| `close` | Escape, Schließen-Knopf, Klick auf den Scrim einer modalen Schublade |

## Verhalten

- **Ohne Scrim (nicht modal):** beim Öffnen geht der Fokus auf das erste fokussierbare Element, sonst auf die
  Schublade; keine Falle, die Seite bleibt bedienbar; beim Schließen kehrt der Fokus zurück. Geschlossen ist sie
  `inert`.
- **Mit Scrim (modal, `modal`):** vor der offenen Schublade steht ein `tl-drawer__scrim`, sie trägt `role="dialog"`
  und `aria-modal="true"`, der Fokus ist gefangen, ein Klick auf den Scrim schließt, beim Schließen kehrt der Fokus
  zurück. Am Viewport deckt der Scrim (`tl-drawer__scrim--viewport`) die Seite, im Behälter nur den Behälter. Leser
  sind `TLDataList` unter seiner Schwelle (`list.contract.md`) und `TLSideRail` (`side-rail.contract.md`).
- Escape schließt.

## Tokens

| Teil | Token |
|---|---|
| Fläche | `--tl-surface-overlay`, Schrift `--tl-text-primary`; Schatten `--tl-shadow-dialog` nur offen |
| Ebene | `--tl-layer-overlay`; im Behälter `--tl-layer-raised` |
| Scrim im Behälter | `--tl-surface-scrim`, Ebene `--tl-layer-raised` |
| Scrim am Viewport | `--tl-surface-scrim`, Ebene `--tl-layer-overlay` |
| Maße | `--tl-size-drawer-narrow`, `--tl-size-drawer-medium`, `--tl-size-drawer-wide` |
| Bewegung | `transform` über `--tl-duration-base`, kommend `--tl-easing-entrance`, gehend `--tl-easing-exit` |
| Kopf | `tl-titlebar` |
| Körper | Innenabstand `--tl-space-md` |
| Ring im Behälter | innen, um `--tl-focus-offset` eingerückt |

## Attribute

| Attribut | Wann |
|---|---|
| `tl-drawer` + `tl-drawer--start` \| `--end` \| `--bottom` + `tl-drawer--narrow` \| `--medium` \| `--wide` | immer, an `aside` |
| `tl-drawer--container` | im Behälter verankert; der Fokusring liegt innen (der Behälter beschneidet) |
| `data-tl-state~="open"` | offen |
| `inert` | geschlossen |
| `aria-labelledby` | mit Titel, auf `tl-titlebar__title` |
| `role="dialog"` + `aria-modal="true"` | nur mit Scrim |
| `tl-drawer__body` | Inhalt |
| `tl-drawer__scrim` | Scrim einer modalen Schublade, vor ihr im DOM |
| `tl-drawer__scrim--viewport` | der Scrim einer am Viewport verankerten modalen Schublade |

## Konformität

Ein Drawer besteht den Vertrag, wenn er geschlossen `inert` ist, ohne Scrim den Fokus nicht fängt und zurückgibt, mit
Scrim modal ist, Escape schließt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
