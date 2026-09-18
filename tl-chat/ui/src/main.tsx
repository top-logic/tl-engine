import { register } from 'tl-react-bridge'
import { App } from './App'
import { TLChatConversation } from './chat-conversation'
import { TLChatRail } from './chat-rail'
import { TLChatSessions } from './chat-sessions'
import { TLDataList } from './data-list'
import './styles.css'

register('TLAgentChat', App)
register('TLChatSessions', TLChatSessions)
register('TLChatConversation', TLChatConversation)
register('TLChatRail', TLChatRail)
register('TLDataList', TLDataList)
