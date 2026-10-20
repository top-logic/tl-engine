import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import diff from 'highlight.js/lib/languages/diff'
import java from 'highlight.js/lib/languages/java'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import markdown from 'highlight.js/lib/languages/markdown'
import plaintext from 'highlight.js/lib/languages/plaintext'
import python from 'highlight.js/lib/languages/python'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import yaml from 'highlight.js/lib/languages/yaml'

/**
 * The languages this application's answers actually contain, registered one by one.
 *
 * highlight.js ships close to 200 grammars and the full bundle is over a megabyte, so the core is
 * imported alone and only these are added. A language outside the list is not an error: the block
 * is rendered unhighlighted under the name the author wrote.
 */
const LANGUAGES: Record<string, unknown> = {
  bash,
  css,
  diff,
  java,
  javascript,
  json,
  markdown,
  plaintext,
  python,
  sql,
  typescript,
  xml,
  yaml,
}

for (const [name, definition] of Object.entries(LANGUAGES)) {
  hljs.registerLanguage(name, definition as never)
}

hljs.registerAliases(['js', 'jsx', 'mjs'], { languageName: 'javascript' })
hljs.registerAliases(['ts', 'tsx'], { languageName: 'typescript' })
hljs.registerAliases(['sh', 'shell', 'zsh', 'console'], { languageName: 'bash' })
hljs.registerAliases(['html', 'svg', 'xsd', 'xhtml'], { languageName: 'xml' })
hljs.registerAliases(['yml'], { languageName: 'yaml' })
hljs.registerAliases(['py'], { languageName: 'python' })
hljs.registerAliases(['md'], { languageName: 'markdown' })
hljs.registerAliases(['text', 'txt', 'none'], { languageName: 'plaintext' })

const LABELS: Record<string, string> = {
  bash: 'Shell',
  css: 'CSS',
  diff: 'Diff',
  java: 'Java',
  javascript: 'JavaScript',
  json: 'JSON',
  markdown: 'Markdown',
  plaintext: 'Text',
  python: 'Python',
  sql: 'SQL',
  typescript: 'TypeScript',
  xml: 'XML',
  yaml: 'YAML',
}

/**
 * Highlights a code block.
 *
 * @param code
 *        the block's source
 * @param language
 *        the language the author wrote after the fence, possibly empty or unknown
 * @returns the HTML for the block and the language actually used, empty when none was
 */
export function highlight(code: string, language: string): { html: string; used: string } {
  const name = language.toLowerCase()
  if (name && hljs.getLanguage(name)) {
    try {
      const result = hljs.highlight(code, { language: name, ignoreIllegals: true })
      return { html: result.value, used: result.language ?? name }
    } catch {
      /* fall through to the unhighlighted block */
    }
  }
  return { html: escapeHtml(code), used: '' }
}

/** The name shown above a block: a proper spelling where we know one, the author's word otherwise. */
export function languageLabel(language: string): string {
  if (!language) return 'Text'
  const resolved = hljs.getLanguage(language)?.name
  return LABELS[language.toLowerCase()] ?? resolved ?? language
}

function escapeHtml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}
