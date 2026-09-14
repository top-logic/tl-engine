import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { App, DefaultCitationPopover, bundle, toLanguage } from 'tl-chat-seams'
import type { ChatExtensions, PaneOpenOptions } from 'tl-chat-seams'
import { InvoiceCite, InvoiceList, InvoicePane, invoiceOf, parseInvoices, type Invoice } from './invoices'

const { useCallback, useMemo, useState } = React

/**
 * Host composition of the chat: a Rechnungen rail on this application's own invoices, a read-only
 * invoice pane, and the chip the pane's "mark a passage" gesture produces.
 *
 * The control still binds the session; this component only registers as {@code DemoChat} and
 * passes an {@code extensions} object to the exported {@code App}.
 */
export function DemoChat(props: TLCellProps) {
  const state = useTLState()
  const labels = bundle(toLanguage(state.language))
  const invoices = useMemo(() => parseInvoices(state.invoicesJson), [state.invoicesJson])
  const [createNote, setCreateNote] = useState('')

  const onCreate = useCallback(() => {
    setCreateNote('Belege werden unter „Belege“ angelegt; der Chat liest sie nur.')
  }, [])

  const extensions: ChatExtensions = useMemo(() => {
    const paneOptions = (invoice: Invoice): PaneOpenOptions => ({
      title: invoice.name,
      backLabel: 'Rechnungen',
      sectionId: 'invoices',
    })
    return {
      railSections: [
        {
          id: 'invoices',
          title: 'Rechnungen',
          contributor: 'Demo',
          accent: true,
          icon: (
            <svg viewBox="0 0 16 16" aria-hidden="true">
              <path d="M3.5 2h9v12l-2-1.2-2 1.2-2-1.2-2 1.2z" />
              <path d="M6 5.5h4M6 8.2h4" />
            </svg>
          ),
          // A count, not news: the invoices of this application simply exist, so they say nothing
          // about what changed since the rail was last opened and contribute no Leiste badge.
          count: () => invoices.length,
          render: (ctx) => (
            <>
              <InvoiceList
                invoices={invoices}
                emptyText="Diese Anwendung hat noch keine Belege."
                onOpen={(invoice) =>
                  ctx.openInPane(
                    <InvoicePane
                      invoice={invoice}
                      onQuote={(label, passage) =>
                        ctx.addComposerChip({ id: `quote-${label}-${passage.slice(0, 24)}`, label, payload: passage })
                      }
                    />,
                    paneOptions(invoice),
                  )
                }
                onCreate={onCreate}
              />
              {createNote ? <p className="demo-invoice-note">{createNote}</p> : null}
            </>
          ),
        },
      ],
      contextFacts: () => [
        { id: 'invoices', label: 'Belege', value: String(invoices.length) },
      ],
      messageAnnotations: {
        render(_marker, citation, _message, openInPane) {
          if (citation?.kind === 'invoice') {
            const invoice = invoiceOf(invoices, citation.id, citation.label)
            return (
              <InvoiceCite
                invoice={invoice}
                open={(item) =>
                  openInPane(<InvoicePane invoice={item} onQuote={() => undefined} />, paneOptions(item))
                }
              />
            )
          }
          return <DefaultCitationPopover citation={citation} labels={labels} />
        },
      },
    }
  }, [createNote, invoices, labels, onCreate])

  return <App {...props} extensions={extensions} />
}
