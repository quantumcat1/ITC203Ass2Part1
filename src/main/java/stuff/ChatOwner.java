package stuff;

import java.util.List;

public interface ChatOwner
{
	public void addMessage(Message message, int chatRoomIndex, boolean showOthers);
	public List<Message> getMessages(int chatRoomIndex);
	public void command(ChatRoom.Command command, String argument, String originalSender, ChatRoom chatRoom);
	public boolean removeChatRoom(ChatRoom c);
	public String getName();
}
