import { register } from 'tl-react-bridge'
import { TLChatConversation } from './chat-conversation'
import { TLChatDock } from './chat-dock'
import { TLChatSources } from './chat-sources'
import { TLPresentationPane } from './presentation-pane'
import './styles.css'

register('TLChatConversation', TLChatConversation)
register('TLChatDock', TLChatDock)
register('TLChatSources', TLChatSources)
register('TLPresentationPane', TLPresentationPane)
// Draws nothing: <chat-channels> and <page-selection> exist on the server only.
register('TLChatInvisible', () => null)
