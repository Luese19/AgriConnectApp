// Enhanced chat feature with full backend integration
class ChatApp {
    constructor() {
        this.stompClient = null;
        this.currentConversationId = null;
        this.currentUser = null;
        this.conversations = [];
        this.typingTimeout = null;
        this.init();
    }

    async init() {
        try {
            // Get current user info
            await this.getCurrentUser();
            
            // Connect to WebSocket
            this.connect();
            
            // Load conversations
            await this.loadConversations();
            
            // Setup event listeners
            this.setupEventListeners();
            
            console.log('Chat app initialized successfully');
        } catch (error) {
            console.error('Failed to initialize chat app:', error);
        }
    }

    async getCurrentUser() {
        try {
            const response = await fetch('/api/chat/current-user');
            if (response.ok) {
                this.currentUser = await response.json();
                console.log('Current user:', this.currentUser);
            } else {
                console.error('Failed to get current user');
                this.currentUser = { id: 'farmer-1', name: 'Current Farmer', role: 'FARMER' };
            }
        } catch (error) {
            console.error('Error getting current user:', error);
            this.currentUser = { id: 'farmer-1', name: 'Current Farmer', role: 'FARMER' };
        }
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected to WebSocket:', frame);
            
            // Subscribe to personal messages
            this.stompClient.subscribe(`/user/queue/messages`, (message) => {
                this.handleIncomingMessage(JSON.parse(message.body));
            });
            
            // Subscribe to typing indicators
            this.stompClient.subscribe(`/user/queue/typing`, (message) => {
                this.handleTypingIndicator(JSON.parse(message.body));
            });
            
        }, (error) => {
            console.error('WebSocket connection error:', error);
            setTimeout(() => this.connect(), 5000); // Reconnect after 5 seconds
        });
    }

    async loadConversations() {
        try {
            const response = await fetch('/api/chat/conversations');
            if (response.ok) {
                this.conversations = await response.json();
                this.renderConversations();
                
                // Select first conversation if available
                if (this.conversations.length > 0) {
                    this.selectConversation(this.conversations[0].id);
                }
            } else {
                console.error('Failed to load conversations');
            }
        } catch (error) {
            console.error('Error loading conversations:', error);
        }
    }

    renderConversations() {
        const container = document.querySelector('.list-group-flush');
        if (!container) return;
        
        container.innerHTML = '';
        
        this.conversations.forEach(conversation => {
            const contactElement = this.createContactElement(conversation);
            container.appendChild(contactElement);
        });
    }

    createContactElement(conversation) {
        const otherParticipant = conversation.participants.find(p => p.id !== this.currentUser.id);
        const lastMessage = conversation.lastMessage;
        const unreadCount = conversation.unreadCount || 0;
        
        const contactDiv = document.createElement('a');
        contactDiv.href = '#';
        contactDiv.className = 'list-group-item list-group-item-action py-3';
        contactDiv.dataset.conversationId = conversation.id;
        
        contactDiv.innerHTML = `
            <div class="d-flex w-100 align-items-center">
                <img src="https://via.placeholder.com/50x50?text=${otherParticipant.name.charAt(0)}" 
                     class="rounded-circle me-3" alt="${otherParticipant.name}">
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between align-items-center">
                        <h6 class="mb-0">${otherParticipant.name}</h6>
                        <small class="text-muted">${this.formatTime(lastMessage?.timestamp)}</small>
                    </div>
                    <div class="d-flex justify-content-between align-items-center">
                        <p class="mb-0 text-truncate small">${lastMessage?.content || 'No messages yet'}</p>
                        ${unreadCount > 0 ? `<span class="badge bg-primary rounded-pill">${unreadCount}</span>` : ''}
                    </div>
                </div>
            </div>
        `;
        
        contactDiv.addEventListener('click', (e) => {
            e.preventDefault();
            this.selectConversation(conversation.id);
        });
        
        return contactDiv;
    }

    async selectConversation(conversationId) {
        // Update UI to show selected conversation
        document.querySelectorAll('.list-group-item').forEach(item => {
            item.classList.remove('active');
        });
        
        const selectedItem = document.querySelector(`[data-conversation-id="${conversationId}"]`);
        if (selectedItem) {
            selectedItem.classList.add('active');
        }
        
        this.currentConversationId = conversationId;
        
        // Load messages for this conversation
        await this.loadMessages(conversationId);
        
        // Update chat header
        this.updateChatHeader(conversationId);
        
        // Mark messages as read
        await this.markMessagesAsRead(conversationId);
    }

    async loadMessages(conversationId) {
        try {
            const response = await fetch(`/api/chat/conversations/${conversationId}/messages`);
            if (response.ok) {
                const messages = await response.json();
                this.renderMessages(messages);
            } else {
                console.error('Failed to load messages');
            }
        } catch (error) {
            console.error('Error loading messages:', error);
        }
    }

    renderMessages(messages) {
        const container = document.querySelector('.chat-messages');
        if (!container) return;
        
        container.innerHTML = `
            <div class="text-center mb-3">
                <span class="badge bg-light text-dark">Today, ${new Date().toLocaleDateString()}</span>
            </div>
        `;
        
        messages.forEach(message => {
            const messageElement = this.createMessageElement(message);
            container.appendChild(messageElement);
        });
        
        // Scroll to bottom
        container.scrollTop = container.scrollHeight;
    }

    createMessageElement(message) {
        const isOwnMessage = message.senderId === this.currentUser.id;
        const messageDiv = document.createElement('div');
        messageDiv.className = `d-flex mb-3 ${isOwnMessage ? 'justify-content-end' : ''}`;
        
        if (isOwnMessage) {
            messageDiv.innerHTML = `
                <div class="chat-message-right">
                    <div class="message-bubble bg-primary text-white">
                        ${message.content}
                    </div>
                    <div class="text-end">
                        <small class="text-muted">${this.formatTime(message.timestamp)}</small>
                        ${message.readStatus === 'READ' ? '<i class="bi bi-check2-all text-primary ms-1"></i>' : '<i class="bi bi-check2 text-muted ms-1"></i>'}
                    </div>
                </div>
            `;
        } else {
            messageDiv.innerHTML = `
                <img src="https://via.placeholder.com/40x40?text=${message.senderName.charAt(0)}" 
                     class="rounded-circle me-2" width="40" height="40" alt="${message.senderName}">
                <div class="chat-message-left">
                    <div class="message-bubble bg-light">
                        ${message.content}
                    </div>
                    <small class="text-muted">${this.formatTime(message.timestamp)}</small>
                </div>
            `;
        }
        
        return messageDiv;
    }

    updateChatHeader(conversationId) {
        const conversation = this.conversations.find(c => c.id === conversationId);
        if (!conversation) return;
        
        const otherParticipant = conversation.participants.find(p => p.id !== this.currentUser.id);
        const headerContainer = document.querySelector('.card-header .d-flex .d-flex');
        
        if (headerContainer) {
            headerContainer.innerHTML = `
                <img src="https://via.placeholder.com/40x40?text=${otherParticipant.name.charAt(0)}" 
                     class="rounded-circle me-2" alt="${otherParticipant.name}">
                <div>
                    <h5 class="mb-0">${otherParticipant.name}</h5>
                    <small class="text-muted">
                        <i class="bi bi-circle-fill text-success me-1" style="font-size: 0.5rem;"></i>Online
                    </small>
                </div>
            `;
        }
    }

    async sendMessage() {
        const messageInput = document.getElementById('chat-message-input');
        const content = messageInput.value.trim();
        
        if (!content || !this.currentConversationId || !this.stompClient) {
            return;
        }
        
        const message = {
            conversationId: this.currentConversationId,
            senderId: this.currentUser.id,
            senderName: this.currentUser.name,
            content: content,
            type: 'CHAT',
            timestamp: new Date().toISOString()
        };
        
        // Send via WebSocket
        this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
        
        // Clear input
        messageInput.value = '';
        
        // Add message to UI immediately
        this.addMessageToUI(message);
        
        // Update conversation in list
        this.updateConversationInList(this.currentConversationId, content);
    }

    addMessageToUI(message) {
        const container = document.querySelector('.chat-messages');
        if (!container) return;
        
        const messageElement = this.createMessageElement(message);
        container.appendChild(messageElement);
        container.scrollTop = container.scrollHeight;
    }

    handleIncomingMessage(message) {
        console.log('Incoming message:', message);
        
        // If message is for current conversation, add to UI
        if (message.conversationId === this.currentConversationId) {
            this.addMessageToUI(message);
            // Mark as read immediately
            this.markMessagesAsRead(message.conversationId);
        }
        
        // Update conversation list
        this.updateConversationInList(message.conversationId, message.content);
        
        // Reload conversations to update unread counts
        this.loadConversations();
    }

    updateConversationInList(conversationId, lastMessageContent) {
        const conversation = this.conversations.find(c => c.id === conversationId);
        if (conversation) {
            conversation.lastMessage = {
                content: lastMessageContent,
                timestamp: new Date().toISOString()
            };
            conversation.updatedAt = new Date().toISOString();
        }
    }

    async markMessagesAsRead(conversationId) {
        try {
            await fetch(`/api/chat/conversations/${conversationId}/mark-read`, {
                method: 'POST'
            });
        } catch (error) {
            console.error('Error marking messages as read:', error);
        }
    }

    sendTypingIndicator() {
        if (this.currentConversationId && this.stompClient) {
            const typingMessage = {
                conversationId: this.currentConversationId,
                senderId: this.currentUser.id,
                senderName: this.currentUser.name,
                type: 'TYPING'
            };
            
            this.stompClient.send('/app/chat.typing', {}, JSON.stringify(typingMessage));
        }
    }

    handleTypingIndicator(message) {
        if (message.conversationId === this.currentConversationId && message.senderId !== this.currentUser.id) {
            this.showTypingIndicator(message.senderName);
        }
    }

    showTypingIndicator(senderName) {
        // Implementation for showing typing indicator
        console.log(`${senderName} is typing...`);
    }

    async searchUsers(query) {
        try {
            const response = await fetch(`/api/chat/search-users?q=${encodeURIComponent(query)}`);
            if (response.ok) {
                return await response.json();
            }
            return [];
        } catch (error) {
            console.error('Error searching users:', error);
            return [];
        }
    }

    async createConversation(participantId) {
        try {
            const response = await fetch('/api/chat/conversations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    participantIds: [this.currentUser.id, participantId]
                })
            });
            
            if (response.ok) {
                const conversation = await response.json();
                await this.loadConversations();
                this.selectConversation(conversation.id);
                return conversation;
            } else {
                console.error('Failed to create conversation');
            }
        } catch (error) {
            console.error('Error creating conversation:', error);
        }
    }

    setupEventListeners() {
        // Send message button
        const sendBtn = document.getElementById('chat-send-btn');
        if (sendBtn) {
            sendBtn.addEventListener('click', () => this.sendMessage());
        }
        
        // Message input
        const messageInput = document.getElementById('chat-message-input');
        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
            
            // Typing indicator
            messageInput.addEventListener('input', () => {
                this.sendTypingIndicator();
                
                // Clear previous timeout
                if (this.typingTimeout) {
                    clearTimeout(this.typingTimeout);
                }
                
                // Set new timeout
                this.typingTimeout = setTimeout(() => {
                    // Stop typing indicator after 3 seconds
                }, 3000);
            });
        }
        
        // Search functionality
        const searchInput = document.querySelector('.search-container input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filterConversations(e.target.value);
            });
        }
        
        // New chat modal functionality
        this.setupNewChatModal();
    }

    setupNewChatModal() {
        const newChatModal = document.getElementById('newChatModal');
        if (!newChatModal) return;
        
        const searchInput = newChatModal.querySelector('#userSearch');
        const resultsContainer = newChatModal.querySelector('#searchResults');
        
        if (searchInput && resultsContainer) {
            searchInput.addEventListener('input', async (e) => {
                const query = e.target.value.trim();
                if (query.length > 2) {
                    const users = await this.searchUsers(query);
                    this.renderSearchResults(users, resultsContainer);
                } else {
                    resultsContainer.innerHTML = '';
                }
            });
        }
    }

    renderSearchResults(users, container) {
        container.innerHTML = '';
        
        users.forEach(user => {
            const userDiv = document.createElement('div');
            userDiv.className = 'list-group-item list-group-item-action';
            userDiv.innerHTML = `
                <div class="d-flex align-items-center">
                    <img src="https://via.placeholder.com/40x40?text=${user.name.charAt(0)}" 
                         class="rounded-circle me-3" alt="${user.name}">
                    <div>
                        <h6 class="mb-0">${user.name}</h6>
                        <small class="text-muted">${user.role}</small>
                    </div>
                </div>
            `;
            
            userDiv.addEventListener('click', () => {
                this.createConversation(user.id);
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('newChatModal'));
                if (modal) modal.hide();
            });
            
            container.appendChild(userDiv);
        });
    }

    filterConversations(query) {
        const items = document.querySelectorAll('.list-group-item');
        items.forEach(item => {
            const name = item.querySelector('h6').textContent.toLowerCase();
            const lastMessage = item.querySelector('p').textContent.toLowerCase();
            
            if (name.includes(query.toLowerCase()) || lastMessage.includes(query.toLowerCase())) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        });
    }

    formatTime(timestamp) {
        if (!timestamp) return '';
        
        const date = new Date(timestamp);
        const now = new Date();
        const diffInHours = (now - date) / (1000 * 60 * 60);
        
        if (diffInHours < 1) {
            return `${Math.floor(diffInHours * 60)} min`;
        } else if (diffInHours < 24) {
            return `${Math.floor(diffInHours)} hrs`;
        } else if (diffInHours < 168) {
            return date.toLocaleDateString('en-US', { weekday: 'short' });
        } else {
            return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
        }
    }
}

// Initialize chat app when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
    window.chatApp = new ChatApp();
});
