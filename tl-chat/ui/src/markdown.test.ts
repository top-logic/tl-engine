// @vitest-environment jsdom
import { describe, expect, it } from 'vitest'
import { bundle } from './i18n'
import { renderMarkdown } from './markdown'

const labels = bundle('en')

const render = (text: string) => renderMarkdown(text, labels)

describe('renderMarkdown', () => {
  it('drops a script element', () => {
    const html = render('hello\n\n<script>alert(1)</script>\n')
    expect(html).not.toContain('<script')
    expect(html).toContain('hello')
  })

  it('drops an event handler', () => {
    expect(render('<img src=x onerror="alert(1)">')).not.toContain('onerror')
  })

  it('drops a frame', () => {
    expect(render('<iframe src="https://evil.example"></iframe>')).not.toContain('<iframe')
  })

  it('drops a script URL, however it is spelled', () => {
    for (const url of ['javascript:alert(1)', 'JaVaScRiPt:alert(1)', 'vbscript:msgbox(1)']) {
      const html = render(`[click](${url})`)
      expect(html, url).not.toContain('href="' + url)
      expect(html, url).not.toMatch(/script:/i)
    }
  })

  it('drops an svg with a script in it', () => {
    const html = render('<svg><script>alert(1)</script></svg>')
    expect(html).not.toContain('<script')
  })

  it('keeps what a markdown answer is made of', () => {
    const html = render(
      '# Title\n\n- item **bold**\n\n| a | b |\n|:--|--:|\n| 1 | 2 |\n\n[link](https://example.com)',
    )
    expect(html).toContain('<h1>Title</h1>')
    expect(html).toContain('<li>')
    expect(html).toContain('<strong>bold</strong>')
    expect(html).toContain('<table>')
    expect(html).toContain('<a href="https://example.com"')
  })

  it('keeps the code block this file builds itself', () => {
    const html = render('```java\nint x = 1;\n```')
    expect(html).toContain('class="code-block"')
    expect(html).toContain('<button')
    expect(html).toContain('data-copy')
    expect(html).toContain('language-java')
  })
})
