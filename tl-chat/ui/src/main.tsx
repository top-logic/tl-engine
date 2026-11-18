import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatConversation } from './chat-conversation'
import { TLChatDock } from './chat-dock'
import { TLChatRail } from './chat-rail'
import { TLChatSources } from './chat-sources'
import './styles.css'

register('TLAgentChat', App)
register('TLChatConversation', TLChatConversation)
register('TLChatDock', TLChatDock)
register('TLChatRail', TLChatRail)
register('TLChatSources', TLChatSources)
// Draws nothing: <chat-channels> and <page-selection> exist on the server only.
register('TLChatInvisible', () => null)
