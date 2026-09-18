import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatConversation } from './chat-conversation'
import { TLChatDock } from './chat-dock'
import { TLChatRail } from './chat-rail'
import { TLChatSources } from './chat-sources'
import { TLDataList } from './data-list'
import './styles.css'

register('TLAgentChat', App)
register('TLChatConversation', TLChatConversation)
register('TLChatDock', TLChatDock)
register('TLChatRail', TLChatRail)
register('TLChatSources', TLChatSources)
register('TLDataList', TLDataList)
