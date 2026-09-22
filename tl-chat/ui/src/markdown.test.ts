// @vitest-environment jsdom
import { afterEach, describe, expect, it } from 'vitest'
import { React } from 'tl-react-bridge'
import { createRoot, type Root } from 'react-dom/client'
import { bundle } from './i18n'
import { Markdown, renderMarkdown, stablePrefixEnd } from './markdown'

const labels = bundle('en')

const render = (text: string) => renderMarkdown(text, labels)

/** The spellings of a script URL a markdown link can hide. */
const SCRIPT_URLS = ['javascript:alert(1)', 'JaVaScRiPt:alert(1)', 'vbscript:msgbox(1)']

const scriptLink = (url: string) => `[click](${url})`

/**
 * An answer of the shape a model writes one: headings, prose, a tight list, a fenced block, two
 * quotations, a table and a loose list - and the last block unfinished, as a stream leaves it.
 */
const LONG_ANSWER = `# Report

Here is the first paragraph of the answer, which runs on for a while.

## Findings

- the first finding
- the second finding **emphasised**
- the third one, with \`code\` in it

Prose between the blocks, so that there is a boundary to find.

\`\`\`java
int x = 1;

int y = 2;
\`\`\`

> A quotation.

> A second quotation, right behind the first one.

| a | b |
|:--|--:|
| 1 | 2 |

1. one

2. two

    indented under the second item

Closing words, and then one more sentence that is still being written`

/** The same shapes again, but with a link definition, which no boundary may cut a reader off from. */
const DEFINED_LINKS = `See [the spec][s] and [the guide][g].

[s]: https://example.com/spec
[g]: https://example.com/guide

More prose that mentions [the spec][s] once more.`

/** A link label defined behind the prose that uses it - what turns text already written into a link. */
const LATE_DEFINITION = `First paragraph.

Second paragraph, mentioning [the spec][s].

[s]: https://example.com/spec

Done.`

/** Raw HTML a model wrote, whose opening and closing tags sit in different blocks. */
const RAW_HTML = `<div class="note">

Inside the note.

</div>

After the note.`

/**
 * Every answer this file renders.
 *
 * Named rather than written into each test, so that `the streamed split` below can put all of them
 * through the streaming path: this corpus is what says the split renders what the whole render did.
 */
const CORPUS: Record<string, string> = {
  script: 'hello\n\n<script>alert(1)</script>\n',
  handler: '<img src=x onerror="alert(1)">',
  frame: '<iframe src="https://evil.example"></iframe>',
  svgScript: '<svg><script>alert(1)</script></svg>',
  answer: '# Title\n\n- item **bold**\n\n| a | b |\n|:--|--:|\n| 1 | 2 |\n\n[link](https://example.com)',
  screenshot: '![page.png](/screenshot/a7f3b2)',
  scriptImage: '![x](javascript:alert(1))',
  code: '```java\nint x = 1;\n```',
  longAnswer: LONG_ANSWER,
  definedLinks: DEFINED_LINKS,
  lateDefinition: LATE_DEFINITION,
  rawHtml: RAW_HTML,
  ...Object.fromEntries(SCRIPT_URLS.map((url) => [`link ${url}`, scriptLink(url)])),
}

describe('renderMarkdown', () => {
  it('drops a script element', () => {
    const html = render(CORPUS.script)
    expect(html).not.toContain('<script')
    expect(html).toContain('hello')
  })

  it('drops an event handler', () => {
    expect(render(CORPUS.handler)).not.toContain('onerror')
  })

  it('drops a frame', () => {
    expect(render(CORPUS.frame)).not.toContain('<iframe')
  })

  it('drops a script URL, however it is spelled', () => {
    for (const url of SCRIPT_URLS) {
      const html = render(scriptLink(url))
      expect(html, url).not.toContain('href="' + url)
      expect(html, url).not.toMatch(/script:/i)
    }
  })

  it('drops an svg with a script in it', () => {
    const html = render(CORPUS.svgScript)
    expect(html).not.toContain('<script')
  })

  it('keeps what a markdown answer is made of', () => {
    const html = render(CORPUS.answer)
    expect(html).toContain('<h1>Title</h1>')
    expect(html).toContain('<li>')
    expect(html).toContain('<strong>bold</strong>')
    expect(html).toContain('<table>')
    expect(html).toContain('<a href="https://example.com"')
  })

  it('keeps a screenshot of this application', () => {
    // What makes a captured screenshot visible at all (issue #216): the tool hands the model a
    // /screenshot/<id> URL and the model writes it as a markdown image.
    const html = render(CORPUS.screenshot)
    expect(html).toContain('src="/screenshot/a7f3b2"')
    expect(html).toContain('alt="page.png"')
  })

  it('drops an image whose source is a script', () => {
    const html = render(CORPUS.scriptImage)
    expect(html).not.toMatch(/src="javascript:/i)
  })

  it('keeps the code block this file builds itself', () => {
    const html = render(CORPUS.code)
    expect(html).toContain('class="code-block"')
    expect(html).toContain('<button')
    expect(html).toContain('data-copy')
    expect(html).toContain('language-java')
  })
})

describe('stablePrefixEnd', () => {
  it('finds nothing to commit in one unfinished block', () => {
    expect(stablePrefixEnd('An answer that is still bein')).toBe(0)
    expect(stablePrefixEnd('')).toBe(0)
  })

  it('commits everything in front of the block being written', () => {
    // `Thi` is the block being written and `Second.` the one whose first line is the newest
    // finished one - and an unfinished first line is no boundary, because `2` is a paragraph and
    // `2.` is a list.
    const text = 'First.\n\nSecond.\n\nThi'
    expect(stablePrefixEnd(text)).toBe(text.indexOf('Second.'))
    expect(stablePrefixEnd(`${text}rd line finished\n`)).toBe(text.indexOf('Thi'))
  })

  it('does not cut into an unclosed code fence', () => {
    const text = 'Before.\n\n```java\nint x = 1;\n\nint y = 2;'
    expect(stablePrefixEnd(text)).toBe(text.indexOf('```'))
  })

  it('cuts behind a closed code fence', () => {
    const text = 'Before.\n\n```java\nint x = 1;\n```\n\nAfter.\n'
    expect(stablePrefixEnd(text)).toBe(text.indexOf('After.'))
  })

  it('keeps a loose list in one piece', () => {
    // Two list blocks with a blank line between them are one loose list, so the boundary between
    // them is not one; the boundary in front of the whole list is.
    const text = 'Intro.\n\n- one\n\n- two\n\n- thr'
    expect(stablePrefixEnd(text)).toBe(text.indexOf('- one'))
  })

  it('keeps what is indented with the block in front of it', () => {
    // The indented paragraph is the list item's own content, which makes the list loose; a boundary
    // in front of it would render the item tight and the paragraph as a code block.
    const text = 'Intro.\n\n- one\n\n    still the first item\n\nBack at the margin.\n'
    expect(stablePrefixEnd(text)).toBe(text.indexOf('Back at the margin.'))
    expect(stablePrefixEnd(text.slice(0, text.indexOf('Back')))).toBe(text.indexOf('- one'))
  })

  it('commits nothing at all when a link label is defined', () => {
    expect(stablePrefixEnd(DEFINED_LINKS)).toBe(0)
  })

  it('does not cut raw HTML apart', () => {
    expect(stablePrefixEnd(RAW_HTML)).toBe(0)
  })
})

/**
 * Every shape a block of an answer has, one sample each.
 *
 * What the pair matrix below is built from: which of these, put next to which, `marked` reads as
 * one block rather than two is the whole justification for what `stablePrefixEnd` refuses.
 */
const SHAPES: Record<string, string> = {
  para: 'Some prose here.',
  para2: 'Other prose.',
  h1: '# Heading',
  h2: '## Sub',
  ul: '- a\n- b',
  ulStar: '* c',
  ol: '1. a\n2. b',
  ol2: '3. c',
  oneItem: '- only',
  quote: '> q one',
  quote2: '> q two',
  fence: '```js\nlet x = 1;\n```',
  fencePlain: '```\nplain\n```',
  table: '| a | b |\n|---|---|\n| 1 | 2 |',
  table2: '| c |\n|---|\n| 3 |',
  rule: '---',
  indented: '    indented',
  indented2: '    more',
  html: '<div>x</div>',
  htmlOpen: '<div class="note">',
  htmlClose: '</div>',
  image: '![a](/x.png)',
  link: '[a](https://e.example)',
  setext: 'Title\n=====',
}

describe('what a boundary may cut', () => {
  /**
   * The matrix `merges` in `markdown.tsx` is written from: every shape behind every other shape,
   * with a blank line between them, asked of the renderer itself.
   *
   * A pair the renderer reads as one block must never be split - that is the correctness half, and
   * it is what the refusals in `stablePrefixEnd` are for. The other direction is reported but not
   * asserted: refusing a pair that would have been safe costs a boundary, never a wrong answer.
   */
  it('never releases a boundary the renderer reads across', () => {
    const unsafe: string[] = []
    for (const [beforeName, before] of Object.entries(SHAPES)) {
      for (const [afterName, after] of Object.entries(SHAPES)) {
        const source = `${before}\n\n${after}`
        const at = before.length + 2
        const oneBlock = render(source) !== render(source.slice(0, at)) + render(source.slice(at))
        // The trailing newline finishes the last line, which is the other thing a boundary needs.
        if (oneBlock && stablePrefixEnd(`${source}\n`) === at) unsafe.push(`${beforeName} | ${afterName}`)
      }
    }
    expect(unsafe).toEqual([])
  })
})

describe('the streamed split', () => {
  /**
   * What the component does per delta, as `applyStreamed` does it (#510): commit the blocks
   * `stablePrefixEnd` released, render only the tail behind them, never touch committed HTML
   * again - and start over where the prefix is not an extension of what was committed.
   *
   * What it must produce at every step is what one render of the whole text produces, which is what
   * the chat showed before this split existed.
   */
  it('builds every answer of the corpus out of its blocks, into the whole render of it', () => {
    for (const [name, source] of Object.entries(CORPUS)) {
      // Every prefix a stream passes through, in steps that do not land on block boundaries.
      const step = source.length > 400 ? 7 : 1
      let committedSource = ''
      let committed = ''
      for (let cut = 1; cut <= source.length; cut += step) {
        const seen = source.slice(0, cut)
        const at = stablePrefixEnd(seen)
        const prefix = seen.slice(0, at)
        if (!prefix.startsWith(committedSource)) {
          committedSource = ''
          committed = ''
        }
        committed += render(prefix.slice(committedSource.length))
        committedSource = prefix
        expect(committed + render(seen.slice(at)), `${name} @ ${cut}`).toBe(render(seen))
      }
    }
  })

  it('never takes back a block it committed', () => {
    // The whole saving rests on this: a prefix that is an extension of the last one is the case
    // `applyStreamed` appends in, and anything else costs it a full rebuild.
    let boundary = 0
    for (let cut = 1; cut <= LONG_ANSWER.length; cut++) {
      const at = stablePrefixEnd(LONG_ANSWER.slice(0, cut))
      expect(at, `@ ${cut}`).toBeGreaterThanOrEqual(boundary)
      boundary = at
    }
    expect(boundary).toBeGreaterThan(0)
  })
})

describe('a streaming answer', () => {
  let mounted: Root | null = null

  afterEach(() => {
    mounted?.unmount()
    mounted = null
    document.body.replaceChildren()
  })

  /** What a React commit needs before the DOM is what it will be. */
  const settle = () => new Promise<void>((resolve) => setTimeout(resolve, 0))

  /**
   * The issue's own measurement (#510): a `MutationObserver` over the answer while 200 deltas of it
   * arrive, one short sentence each.
   *
   * Counted in nodes rather than in records, because a `innerHTML` that replaces a whole answer is
   * one record however big the answer is: what grows, and what the reader sees flickering, is the
   * nodes in it.
   *
   * What this says is not a budget but a shape - a delta must not cost more because more has
   * already been written. Before the split it did: the same 200 deltas churned 79998 nodes, 5200
   * of them over the first fifty deltas and 35000 over the last fifty.
   */
  it('extends the answer instead of rebuilding it', async () => {
    const container = document.createElement('div')
    document.body.append(container)
    const root = createRoot(container)
    mounted = root

    const deltas = 200
    const answer = (n: number) =>
      Array.from({ length: n }, (_, i) => `Sentence number ${i + 1} of the answer.`).join('\n\n')

    const write = async (text: string) => {
      root.render(React.createElement(Markdown, { text, streaming: true, labels }))
      await settle()
    }

    await write(answer(1))
    const counts: number[] = []
    let seen = 0
    const observer = new MutationObserver((records) => {
      for (const record of records) {
        seen +=
          record.type === 'childList' ? record.addedNodes.length + record.removedNodes.length : 1
      }
    })
    observer.observe(container, { subtree: true, childList: true, attributes: true, characterData: true })
    for (let n = 2; n <= deltas; n++) {
      await write(answer(n))
      counts.push(seen)
      seen = 0
    }
    observer.disconnect()

    const sum = (from: number, to: number) => counts.slice(from, to).reduce((a, b) => a + b, 0)
    const first = sum(0, 50)
    const last = sum(counts.length - 50, counts.length)
    // A flat count: the late deltas of a long answer cost what the early ones of a short one cost,
    // and no delta costs more than the handful of nodes an append and a tail swap are.
    expect(Math.max(...counts)).toBeLessThanOrEqual(12)
    expect(last).toBeLessThanOrEqual(first + 50)

    // And what is standing there is still the whole answer, rendered as it always was.
    root.render(React.createElement(Markdown, { text: answer(deltas), labels }))
    await settle()
    expect(container.querySelector('.md')?.innerHTML).toBe(render(answer(deltas)))
    expect(container.querySelectorAll('.md > p')).toHaveLength(deltas)
  })

  /**
   * Nothing but the component writes that container - but the component is the only thing that
   * knows it, and the host supplies the React this bundle runs on. A React that re-applies the
   * element's `dangerouslySetInnerHTML` on every render of the parent wipes the committed nodes,
   * which is what happened in the browser while the test-run React spared them: the answer then
   * showed its first block and its last two, and nothing in between.
   *
   * `Surface` is what stops it happening, and this is what stops it mattering: a container that is
   * not what the component recorded is written again from the start.
   */
  it('recovers when something else empties the container', async () => {
    const container = document.createElement('div')
    document.body.append(container)
    const root = createRoot(container)
    mounted = root
    const answer = (n: number) =>
      Array.from({ length: n }, (_, i) => `Sentence number ${i + 1} of the answer.`).join('\n\n')

    for (let n = 1; n <= 8; n++) {
      root.render(React.createElement(Markdown, { text: answer(n), streaming: true, labels }))
      await settle()
      // What a React that re-applies the first render would leave behind.
      container.querySelector('.md')!.replaceChildren()
    }
    root.render(React.createElement(Markdown, { text: answer(9), streaming: true, labels }))
    await settle()
    expect(container.querySelector('.md')?.innerHTML).toBe(render(answer(9)))
  })
})
