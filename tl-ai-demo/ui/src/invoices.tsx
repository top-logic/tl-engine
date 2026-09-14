import { React } from 'tl-react-bridge'

/** One invoice of the demo model, as `DemoChatControl` puts it into the control state. */
export interface Invoice {
  /** The object's own id in the application's model. */
  id: string
  /** `description` of the invoice, or a fallback when it carries none. */
  name: string
  /** Formatted `date`, or empty. */
  meta: string
  /** Formatted `amount`, or empty. */
  state: string
}

/**
 * Reads the invoice list out of the control state.
 *
 * The list is the application's own data; when the type is not deployed, or nobody is signed in,
 * it is empty and the section says so rather than showing invented rows.
 */
export function parseInvoices(raw: unknown): Invoice[] {
  if (typeof raw !== 'string' || raw.trim() === '') return []
  try {
    const data = JSON.parse(raw) as Invoice[]
    if (!Array.isArray(data)) return []
    return data.filter((item) => typeof item?.id === 'string')
  } catch {
    return []
  }
}

/**
 * The invoice a citation points at.
 *
 * Matched on the object id first, then on the name: the id the agent quotes is whatever the tool it
 * read the invoice with put in its result, and for a TL-Script read of the demo model that is the
 * description rather than the internal id.
 */
export function invoiceOf(invoices: Invoice[], id: string, label: string): Invoice | undefined {
  const wanted = id.trim().toLowerCase()
  if (!wanted) return undefined
  return (
    invoices.find((item) => item.id.toLowerCase() === wanted) ??
    invoices.find((item) => item.name.toLowerCase() === wanted) ??
    invoices.find((item) => item.name.toLowerCase() === label.trim().toLowerCase())
  )
}

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
              onQuote(invoice.id, invoice.name, field, passage)
              setPassage('')
              setField('')
              window.getSelection()?.removeAllRanges()
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
