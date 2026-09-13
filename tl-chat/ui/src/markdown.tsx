import { React } from 'tl-react-bridge'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

const { useMemo } = React

marked.setOptions({ gfm: true, breaks: true })

export function Markdown({ text }: { text: string }) {
  const html = useMemo(() => {
    const raw = marked.parse(text || '') as string
    return DOMPurify.sanitize(raw)
  }, [text])
  return <div className="md" dangerouslySetInnerHTML={{ __html: html }} />
}
