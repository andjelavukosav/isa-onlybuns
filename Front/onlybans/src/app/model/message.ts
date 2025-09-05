export interface ChatMessage {
    message: string,
    senderId: string,
    recipientId?: string,
    chatRoomId?: number,
    timestamp?: Date | undefined,
}
