export interface GroupChatRoomDTO {
  id: number;
  name: string;
  ownerUsername: string;
  createdAt: Date | undefined;
  members: GroupChatMemberDTO[];
}

export interface GroupChatMemberDTO {
  id: number;
  username: string;
}
