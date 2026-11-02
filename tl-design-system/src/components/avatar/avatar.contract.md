# Vertrag: Avatar

Eine Kundenkomponente, die unter dem TopLogic-Namen `TLAvatar` registriert ist (Stufe C der Spec),
bekommt den Zustand unten, rendert einen Kreis und trägt die genannten Attribute. TopLogic
garantiert dann Funktion und Zugänglichkeit — nicht das Aussehen. Wer ins Bild passen will, liest
die Tokens.

## Zustand

| Schlüssel | Typ | Bedeutung |
|---|---|---|
| `name` | string \| null | Anzeigename dessen, was der Avatar vertritt. Aus ihm kommen die Initialen (höchstens zwei Zeichen) und die Kategorie. `null`: ein Avatar ohne Namen, neutral und leer. |
| `size` | `small` \| `medium` \| `large` \| `x-large`, optional | Durchmesser; fehlt er, `medium`. |
| `url` / `hasData` / `dataRevision` | siehe `ImageSource` | Das Bild. Gibt es eines, zeigt der Avatar es statt der Initialen. |

Die Kategorie ist **keine Eingabe**: die Komponente rechnet sie aus dem Namen (`1 + hash(name) mod 8`),
und derselbe Name ergibt überall dieselbe Kategorie. Ein Farbwert erreicht die Komponente nie.

Die Größennamen im Zustand sind Wörter; die Klassen des Pakets tragen die Kürzel der Skala
(`small` → `tl-avatar--sm`, `large` → `tl-avatar--lg`, `x-large` → `tl-avatar--xl`, `medium` ohne
Modifier). Die Komponente bildet das eine auf das andere ab.

## Aktionen

Keine. Ein Avatar ist Anzeige; ein bedienbarer Avatar (Menüauslöser) ist ein Button mit
`radius-round`, der einen Avatar enthält.

## Tokens

| Zustand | Fläche | Linie |
|---|---|---|
| ohne Namen, oder mit Bild | `--tl-surface-interactive-selected` | `--tl-border-control` |
| `category-1` … `category-8` | `--tl-category-1-subtle` … `--tl-category-8-subtle` | `--tl-category-1-border` … `--tl-category-8-border` |

Initialen: `--tl-text-primary`. Die acht Kategorie-Tokens im Einzelnen:
`--tl-category-1-subtle`, `--tl-category-1-border`, `--tl-category-2-subtle`, `--tl-category-2-border`,
`--tl-category-3-subtle`, `--tl-category-3-border`, `--tl-category-4-subtle`, `--tl-category-4-border`,
`--tl-category-5-subtle`, `--tl-category-5-border`, `--tl-category-6-subtle`, `--tl-category-6-border`,
`--tl-category-7-subtle`, `--tl-category-7-border`, `--tl-category-8-subtle`, `--tl-category-8-border`.

Maße: `--tl-size-avatar-sm` (24 px), `--tl-size-avatar-md` (32 px), `--tl-size-avatar-lg` (48 px),
`--tl-size-avatar-xl` (64 px); Linie `--tl-border-width-sm`; Form `--tl-radius-round`. Die Größen
folgen nicht der Dichte: ein Avatar ist kein Bedienelement.

Typo der Initialen, von außen gesetzt wie beim Button, je Größe: `small` `.tl-type-label-strong`,
`medium` `.tl-type-body-strong`, `large` `.tl-type-heading-md`, `x-large` `.tl-type-heading-lg`. Keine Größe
unterschreitet 12 px.

## Attribute

| Attribut | Wann |
|---|---|
| `role="img"` + `aria-label` | Initialen-Avatar: der Name ist die Bedeutung, die Initialen sind Bild. |
| `alt` am `__image` | Bild-Avatar: der Name; leer, wenn kein Name. |
| `aria-hidden="true"` | Avatar ohne Namen und ohne Bild: rein dekorativ. |
| `title` / Tooltip | der volle Name, weil die Initialen ihn nur andeuten. |

## Konformität

Eine Komponente besteht den Vertrag, wenn sie im Specimen-Harness des Pakets für jeden Namen die
Attribut `data-tl-role="category-<n>"` seines Hashes trägt, für jede Größe außer `medium` den Modifier ihres Kürzels, ohne
Namen und ohne Bild die neutrale Fläche zeigt, mit Bild das `__image` rendert, kein `style`-Attribut
setzt und im Klassen-Gate keine Klasse ohne `tl-`-Präfix hinterlässt.
