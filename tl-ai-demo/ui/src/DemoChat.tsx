import { React, useTLState } from 'tl-react-bridge'
import type { TLCellProps } from 'tl-react-bridge'
import { App, bundle, toLanguage } from 'tl-chat-seams'
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
    // An invoice is something to read, so it opens in document mode: two thirds pane, one third
    // thread. The header is the host's: name, version, actions, close.
    const paneOptions = (invoice: Invoice): PaneOpenOptions => ({
      title: invoice.name,
      backLabel: 'Belege',
      sectionId: 'sources',
      mode: 'document',
      version: invoice.meta ? `Beleg vom ${invoice.meta}` : undefined,
      actions: [
        {
          id: 'export',
          label: 'Export',
          onSelect: () => setCreateNote(`„${invoice.name}" wird in dieser Demo nicht exportiert.`),
        },
      ],
    })
    return {
      // The host's own list beside the framework's Quellen: what this application holds.
      sourceTabs: [
        {
          id: 'invoices',
          label: 'Belege',
          render: (ctx) => (
            <>
              <InvoiceList
                invoices={invoices}
                emptyText="Diese Anwendung hat noch keine Belege."
                onOpen={(invoice) =>
                  ctx.openInPane(
                    <InvoicePane
                      invoice={invoice}
                      // Marking a passage puts it into the composer where the caret stands.
                      onQuote={(invoiceId, label, location, passage) =>
                        ctx.insertReference({ kind: 'invoice', id: invoiceId, label, location, text: passage })
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
      // The seam is enough for a host action beside copy / rate / regenerate.
      messageActions: [
        {
          id: 'open-in-editor',
          label: 'Im Editor öffnen',
          action: () => setCreateNote('Der Editor gehört der Anwendung; die Demo zeigt hier nur die Naht.'),
        },
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
          // Nothing of the host's for a web source: the framework opens its page.
          return null
        },
      },
    }
  }, [createNote, invoices, labels, onCreate])

  return <App {...props} extensions={extensions} />
}
