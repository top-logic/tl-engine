# Vertrag: Progress

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLProgress` registriert ist (Stufe C der Spec),
bekommt den Zustand unten, rendert einen Balken mit optionaler Beschriftung und trägt die genannten
Attribute. TopLogic garantiert dann Funktion und Zugänglichkeit — nicht das Aussehen. Wer ins Bild
passen will, liest die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `fraction` | number 0…1 \| null | Der gefüllte Anteil der Spur. `null` ist der unbestimmte Balken: etwas läuft, ohne zu sagen, wie weit. |
| `label` | string, optional | Beschriftung neben dem Balken; ohne sie steht der Balken allein. |

Die Breite der Füllung ist Daten, kein Gestaltungswert: die Komponente setzt sie am `__fill` als
`style="width: <Anteil in Prozent>%"` — dasselbe, was `aria-valuenow` sagt. Farbe, Höhe, Form und Bewegung
setzt sie nie inline. Ein Anteil außerhalb 0…1 ist Sache des Controls (es klemmt), nicht des Stylesheets.

## Aktionen

Keine. Ein Progress ist Anzeige.

## Tokens

| Teil | Token |
|---|---|
| Spur | `--tl-surface-interactive-strong` (die neutrale Fläche von Pill und Avatar), Höhe `--tl-size-progress` (8 px), Mindestbreite `--tl-size-control`, Form `--tl-radius-full` |
| Füllung | `--tl-surface-brand`, Übergang `--tl-duration-base` `--tl-easing-standard` |
| Beschriftung | `--tl-text-secondary`; Klasse `.tl-type-label .tl-nums`, von außen gesetzt |
| Abstand | `--tl-space-xs` zwischen Balken und Beschriftung |
| Unbestimmt | Schwung `--tl-duration-loop` (1200 ms) `--tl-easing-standard`, endlos; ohne Bewegung ruht die Füllung bei drei Vierteln der Spur |

Keine Tönung: ein Balken sagt, wie weit, nicht ob gut oder schlecht. Das sagt der Text daneben oder
der Status, der den Balken zeigt.

## Attribute

| Attribut | Wann |
|---|---|
| `role="progressbar"` | immer, am Wurzelelement |
| `aria-valuemin="0"`, `aria-valuemax="100"`, `aria-valuenow` | bestimmter Balken: `aria-valuenow` ist der Anteil in Prozent, gerundet |
| `aria-valuetext` | nie: die Beschriftung ist der Name, derselbe Text als Wertetext würde doppelt angesagt; den Wert sagt `aria-valuenow` |
| `aria-labelledby` | immer, außer bei `aria-hidden`: die `id` des `tl-progress__label`, wenn die Beschriftung sagt, was voranschreitet; sonst die `id` des Elements am Ort, das es sagt (siehe unten) |
| `aria-hidden="true"` | wenn ein umgebendes Element den Namen trägt (Ladezeile im Baum, `status.contract.md`); dann ohne eigenen Namen |
| `aria-busy="true"` | unbestimmter Balken; `aria-valuenow` fehlt dann. Das Attribut ist zugleich der Selektor der Schwung-Animation — es gibt keine Klasse dafür. |

**Name (ARIA 1.2, `progressbar` verlangt einen).** Ein Balken hat eine Beschriftung neben sich (09.10.). Sagt sie,
was voranschreitet („2 / 8“, „In Arbeit…“), ist sie der Name: `aria-labelledby` zeigt auf die `id` des
`tl-progress__label`, die die Komponente selbst vergibt. Ist sie nur der Wert („42 %“), gibt der Ort den Namen:
`aria-labelledby` zeigt auf das Element, das sagt, was voranschreitet (der Status eines Jobs), und die Beschriftung
ist `aria-hidden="true"` — den Wert sagt `aria-valuenow`, und in einer Live-Region würde jede Änderung angesagt.
Unbestimmt und ohne Wert steht der Balken ohne Beschriftung, vom Ort benannt. Kein `aria-valuetext`: es wäre der
Name oder der Wert ein zweites Mal. In einem Element, das den Namen trägt (Ladezeile im Baum,
`status.contract.md`), ist der Balken `aria-hidden="true"` ohne eigenen Namen, damit nichts doppelt angesagt wird.

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets `role="progressbar"` mit den
drei Wertattributen trägt, inline nur die Breite des `__fill` setzt, unbestimmt `aria-busy="true"`
ohne `aria-valuenow` trägt, die Beschriftung als `tl-progress__label` rendert, über `aria-labelledby`
benannt oder `aria-hidden` ist, kein `aria-valuetext` trägt und im Klassen-Gate keine
Klasse ohne `tl-`-Präfix hinterlässt.

## Spinner (Part `Spinner`, #29787)

Beschäftigt ohne Anteil: ein Ring, auf dem ein Bogen in der Marke kreist (`tl-spinner`). Klein (`tl-spinner--sm`)
steht er an der Stelle eines kleinen Icons und dreht den Bogen in der Textfarbe. Mit eigenem Namen trägt er
`role="progressbar"` und `aria-label`; als Verzierung neben einem Text, der den Zustand sagt, ist er
`aria-hidden="true"`. Maße: `--tl-size-icon-lg`, klein `--tl-size-icon-sm`; Ring `--tl-border-width-md` in der Spur
(`--tl-surface-interactive-strong`), Bogen `--tl-surface-brand`, klein `currentColor`; rund `--tl-radius-full`.
Dauer und Kurve sind `--tl-duration-loop` und `--tl-easing-linear`.
