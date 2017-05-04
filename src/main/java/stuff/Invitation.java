package stuff;

public class Invitation extends Message
{
	private int masterChatRoomIndex;

	public Invitation(String message)
	{
		super(message);
	}

	public Invitation(String message, String name)
	{
		super(message, name);
	}

	public Invitation(String message, String name, int index)
	{
		super(message, name);
		masterChatRoomIndex = index;
	}

	public int getMasterChatRoomIndex() {
		return masterChatRoomIndex;
	}

	public void setMasterChatRoomIndex(int masterChatRoomIndex) {
		this.masterChatRoomIndex = masterChatRoomIndex;
	}

}
