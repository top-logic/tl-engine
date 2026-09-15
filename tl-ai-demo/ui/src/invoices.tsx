import { React } from 'tl-react-bridge'
import type { Invoice } from './invoice-model'

export { invoiceOf, parseInvoices, type Invoice } from './invoice-model'

export function InvoiceList({
  invoices,
  onOpen,
  onCreate,
  emptyText,
}: {
  invoices: Invoice[]
  onOpen: (invoice: Invoice) => void
  onCreate: () => void
  emptyText: string
}) {
  if (invoices.length === 0) {
    return <p className="demo-invoice-empty">{emptyText}</p>
  }
  return (
    <div className="demo-invoices">
      <ul className="demo-invoice-list">
        {invoices.map((invoice) => (
          <li key={invoice.id}>
            <button type="button" className="demo-invoice-row" onClick={() => onOpen(invoice)}>
              <span className="demo-invoice-name">{invoice.name}</span>
              <span className="demo-invoice-meta">{invoice.meta}</span>
              <span className="demo-invoice-state">{invoice.state}</span>
            </button>
          </li>
        ))}
      </ul>
      <button type="button" className="demo-invoice-create" onClick={onCreate}>
        + Beleg anlegen
      </button>
    </div>
  )
}

/**
 * The invoice, read-only, with the "mark a passage" gesture of the layout spec.
 *
 * Selecting text in the pane offers "Mit Albert besprechen"; taking it puts a chip with the
 * invoice's name and the marked passage into the composer, where the user can remove it again.
 */
export function InvoicePane({
  invoice,
  onQuote,
}: {
  invoice: Invoice
  onQuote: (invoiceId: string, label: string, location: string, passage: string) => void
}) {
  const body = React.useRef<HTMLElement>(null)
  const [passage, setPassage] = React.useState('')
  /** Which row the marked passage came from - the token's location. */
  const [field, setField] = React.useState('')

  const readSelection = React.useCallback(() => {
    const selection = window.getSelection()
    const text = selection?.toString().trim() ?? ''
    const inside =
      text !== '' &&
      selection?.anchorNode != null &&
      body.current != null &&
      body.current.contains(selection.anchorNode)
    setPassage(inside ? text : '')
    if (!inside) {
      setField('')
      return
    }
    const node = selection?.anchorNode
    const element = node?.nodeType === Node.TEXT_NODE ? node.parentElement : (node as HTMLElement | null)
    const row = element?.closest('dd')
    setField(row?.previousElementSibling?.textContent?.trim() ?? '')
  }, [])

  return (
    <article className="demo-invoice-pane">
      {/* No heading: the pane's own header already names the invoice. */}
      <section ref={body} onMouseUp={readSelection} onKeyUp={readSelection}>
        <dl className="demo-invoice-facts">
          <dt>Beschreibung</dt>
          <dd>{invoice.name}</dd>
          <dt>Datum</dt>
          <dd>{invoice.meta || 'nicht erfasst'}</dd>
          <dt>Betrag</dt>
          <dd>{invoice.state || 'nicht erfasst'}</dd>
        </dl>
        <p className="demo-invoice-note">
          Nur lesend. Die Bearbeitung gehört der Anwendung, nicht dem Chat. Markieren Sie eine
          Stelle, um sie in den Chat zu übernehmen.
        </p>
      </section>
      {passage ? (
        <div className="demo-invoice-quote">
          <span className="demo-invoice-quote-text">
            {field ? <strong>{field}: </strong> : null}„{passage}“
          </span>
          <button
            type="button"
            className="demo-invoice-quote-action"
            onClick={() => {
              // The selection is cleared BEFORE the insert: afterwards it is the composer's caret,
              // and wiping it sent the next keystroke to the start of the draft.
              window.getSelection()?.removeAllRanges()
              onQuote(invoice.id, invoice.name, field, passage)
              setPassage('')
              setField('')
            }}
          >
            Mit Albert besprechen
          </button>
        </div>
      ) : null}
    </article>
  )
}

/** Opens the cited invoice in the side pane instead of showing a popover. */
export function InvoiceCite({
  invoice,
  open,
}: {
  invoice: Invoice | undefined
  open: (invoice: Invoice) => void
}) {
  React.useEffect(() => {
    if (invoice) open(invoice)
  }, [invoice, open])
  if (invoice) return null
  return <p className="cite-popover-empty">Der Beleg ist in dieser Anwendung nicht (mehr) vorhanden.</p>
}
