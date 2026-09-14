import { React } from 'tl-react-bridge'

export interface InvoiceFixture {
  id: string
  name: string
  meta: string
  state: string
  writing?: boolean
}

/** Fixture rows so the composition walk does not depend on uploaded invoices. */
export const INVOICES: InvoiceFixture[] = [
  {
    id: 'inv-berlin',
    name: 'Reise Berlin',
    meta: '12.03.2026 · 184,90 €',
    state: 'offen',
  },
  {
    id: 'inv-hosting',
    name: 'Hosting April',
    meta: '01.04.2026 · 29,00 €',
    state: 'wird geschrieben',
    writing: true,
  },
  {
    id: 'inv-license',
    name: 'Software-Lizenz',
    meta: '28.02.2026 · 960,00 €',
    state: 'bezahlt',
  },
]

export function InvoiceList({
  invoices,
  onOpen,
  onCreate,
}: {
  invoices: InvoiceFixture[]
  onOpen: (invoice: InvoiceFixture) => void
  onCreate: () => void
}) {
  return (
    <div className="demo-invoices">
      <ul className="demo-invoice-list">
        {invoices.map((invoice) => (
          <li key={invoice.id}>
            <button
              type="button"
              className={`demo-invoice-row${invoice.writing ? ' is-writing' : ''}`}
              onClick={() => onOpen(invoice)}
            >
              <span className="demo-invoice-name">{invoice.name}</span>
              <span className="demo-invoice-meta">{invoice.meta}</span>
              <span className="demo-invoice-state">{invoice.state}</span>
            </button>
          </li>
        ))}
      </ul>
      <button type="button" className="demo-invoice-create" onClick={onCreate}>
        + Dokument anlegen
      </button>
    </div>
  )
}

export function InvoicePane({ invoice }: { invoice: InvoiceFixture }) {
  return (
    <article className="demo-invoice-pane">
      <h3>{invoice.name}</h3>
      <p>{invoice.meta}</p>
      <p>
        <strong>Status:</strong> {invoice.state}
      </p>
      <p className="demo-invoice-note">Nur lesend. Die Bearbeitung gehört der Anwendung, nicht dem Chat.</p>
    </article>
  )
}

export function InvoiceCite({
  invoice,
  openInPane,
}: {
  invoice: InvoiceFixture
  openInPane: (
    content: React.ReactNode,
    options: { title: string; backLabel?: string; sectionId?: string },
  ) => void
}) {
  React.useEffect(() => {
    openInPane(<InvoicePane invoice={invoice} />, {
      title: invoice.name,
      backLabel: 'Rechnungen',
      sectionId: 'invoices',
    })
  }, [invoice.id, invoice.name, invoice.meta, invoice.state, invoice.writing, openInPane])
  return null
}
