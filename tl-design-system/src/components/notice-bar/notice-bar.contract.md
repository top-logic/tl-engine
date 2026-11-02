# Vertrag: NoticeBar

Ein Hinweis der ganzen Anwendung als Balken über die volle Breite, z. B. eine angekündigte Wartung mit Countdown. Ein
`tl-alert` mit eigener Hülle. Um den Balken steht eine bleibende Hülle, `div[role="status"]` (bei `error`:
`role="alert"`); sie ist die Live-Region und bleibt leer, solange nichts zu zeigen ist.

## Zustand

Das Control ist `TLNoticeBar` (`ReactNoticeBarControl`).

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `severity` | `info` \| `warning` \| `error` | Bedeutung, gerendert als `data-tl-role`. |
| `text` | string | Der Hinweis. |
| `deadline`, `serverNow`, `leadMs`, `pingGraceMs` | number | Countdown gegen die Serverzeit, Vorlauf, Nachfrage nach Ablauf. |
| `actionLabel` | string, optional | Macht den Balken zum Knopf; das Label ist sein Tooltip. |

## Aktionen

| Aktion | Wann |
|---|---|
| `action` | Klick, Enter oder Leertaste auf den klickbaren Balken |
| `deadlinePassed` | einmal je Frist, nach Ablauf und Nachfrist |

## Verhalten

- Die Ansage sitzt auf der Hülle: `role="alert"` bei `error`, sonst `role="status"` — mit und ohne Aktion. Die Hülle
  bleibt im DOM, auch wenn nichts gezeigt wird (unsichtbar oder vor dem Vorlauf); der Balken kommt in sie hinein. Grund:
  ein klickbarer Balken ist ein Knopf und keine Live-Region, und eine Region, die schon gefüllt eingefügt wird, wird oft
  nicht angesagt.
- Ohne Aktion trägt der Balken keine Rolle.
- Mit Aktion: der Balken trägt `role="button"`, `tabindex="0"`, Enter und Leertaste lösen aus; der Ring kommt aus `base.css`.
- Der Countdown steht in `tl-notice-bar__countdown` mit `tl-nums` und `tl-type-body-strong` und trägt `aria-live="off"`: die erste Ansage nennt die Zeit, das Ticken wird nicht angesagt.

## Tokens

Aus `tl-alert`; der Balken hat keine Ecke und die Linie nur unten und am Zeilenanfang.

## Attribute

| Attribut | Wann |
|---|---|
| `div[role="status"]` / `div[role="alert"]` (bei `error`) | immer, bleibende Hülle um den Balken, leer ohne Balken |
| `tl-alert` + `tl-notice-bar` + `data-tl-role` | am Balken, wenn er gezeigt wird |
| `role="button"` + `tabindex="0"` + `aria-label` | am Balken, nur mit Aktion |
| `tl-notice-bar__countdown` + `aria-live="off"` | mit Frist |

## Konformität

Eine NoticeBar besteht den Vertrag, wenn sie den Alert-Vertrag erfüllt, klickbar mit der Tastatur bedienbar ist und
einen sichtbaren Ring hat, und im Klassen-Gate keine Klasse ohne `tl-`-Präfix bleibt.
