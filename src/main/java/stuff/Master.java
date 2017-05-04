package stuff;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import stuff.ChatRoom.Command;

@ManagedBean
@ApplicationScoped
public class Master implements Serializable, ChatOwner
{
	/*
	 * master owns the global ChatRoom instances that coordinate the messages
	 * shared between the participants. It registers users and allocates them
	 * personal ChatRoom instances which connect to the global ones.
	 */

	private static final long serialVersionUID = 5149106134986395072L;
	private List<ChatRoom> chatRooms;
	private Map<String, UserBean> users;
	public static String name = "Master";
	private Map<String, String> mapUserPasswords;

	@PostConstruct
	public void initialise()
	{
		mapUserPasswords = new HashMap<String, String>();
		chatRooms = new LinkedList<ChatRoom>();
		users = new HashMap<String, UserBean>();
		ChatRoom c = new ChatRoom("Lobby");
		chatRooms.add(c);
		c.setOwner(this);
		c.addMessage(new Message("Started chat room"));
	}
	public String getName()
	{
		return name;
	}
	public String getUsersInRoom(ChatRoom chatRoom)
	{
		ChatRoom master = getMasterChatRoomFromUserChatRoom(chatRoom);
		List<UserBean> ubs = new ArrayList<UserBean>();
		for(UserBean ub: users.values())
		{
			ChatRoom userChatRoom = ub.getObservingChatRoom(master);
			if(userChatRoom != null)
			{
				ubs.add(ub);
			}
		}
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for(UserBean ub : ubs)
		{
			i++;
		    builder.append(ub.getName());
		    if(i < ubs.size()) builder.append(", ");
		}
		return builder.toString();
	}

	//this method will return a local chat room for the user to talk to
	//and have it linked to the equivalent global one (lobby)
	public ChatRoom newUser(UserBean user)
	{
		//first check if username is used
		String password = mapUserPasswords.get(user.getName());
		if(password != null)//username exists already
		{
			if(!password.equals(user.getPassword()))
			{
				return null; //wrong password for existing user
			}//otherwise all good so continue logging in the user
		}
		else
		{
			//user doesn't exist yet so add them to the list
			mapUserPasswords.put(user.getName(), user.getPassword());//use username as the key since that's the one that has to be unique
		}
		ChatRoom chatRoom = new ChatRoom();
		chatRoom.addObserver(chatRooms.get(0));
		chatRooms.get(0).addObserver(chatRoom);
		users.put(user.getName(), user);

		//addMessage was locking things up so do it in new thread
		final String n = user.getName();
		Thread t = new Thread() {
		    public void run() {
		    	addMessage(new Message("User " + n + " joined the chat."), 0);
		    }
		};
		t.start();

		return chatRoom;
	}
	public List<Message> getMessages(int chatRoomIndex)
	{
		if(chatRooms.size() < chatRoomIndex) return null;
		return chatRooms.get(chatRoomIndex).getMessages();
	}
	public void addMessage(Message message, int chatRoomIndex)
	{
		addMessage(message, chatRoomIndex, true);
	}
	public void addMessage(Message message, int chatRoomIndex, boolean showOthers)
	{
		chatRooms.get(chatRoomIndex).addMessage(message, showOthers);
	}
	public ChatRoom getMasterChatRoomFromUserChatRoom(ChatRoom chatRoom)
	{
		ChatRoom master = null;
		for (Iterator<ChatRoom> iter = chatRooms.listIterator(); iter.hasNext(); )
		{
		    ChatRoom c = iter.next();
		    if(c.isObserving(chatRoom))
		    {
		    	master = c;
		    	break;
		    }
		}
		return master;
	}
	public void quitRoom(UserBean user, ChatRoom chatRoom)
	{
		ChatRoom master = getMasterChatRoomFromUserChatRoom(chatRoom);
		if(master != null) //if it were null this would mean the chat room wasn't attached to anything which would be strange
		{
			//now we should check how many people
			//have this master one as an observer
			//(not the other way around - we couldn't
			//sever that connection from the UserBean side)

			ChatRoom userChatRoom = null;
			List<UserBean> ubs = new ArrayList<UserBean>();
			List<ChatRoom> ucrs = new ArrayList<ChatRoom>();
			for(UserBean ub: users.values())
			{
				userChatRoom = ub.getObservingChatRoom(master);
				if(userChatRoom != null)
				{
					ubs.add(ub);
					ucrs.add(userChatRoom);
				}
			}
			if(ubs.size() > 2)//there's exactly one person left
			{
				//1. tell JUST original user to delink from his original chat room
				user.removeChatRoom(chatRoom);
			}
			else //only 1 or 2 people - tell them both to delink, and remove the master version
			{
				ubs.get(0).removeChatRoom(ucrs.get(0));
				ubs.get(1).removeChatRoom(ucrs.get(1));
				removeChatRoom(master);
			}
		}
	}
	public boolean removeChatRoom(ChatRoom c)
	{
		int index = 0;
		for (Iterator<ChatRoom> iter = chatRooms.listIterator(); iter.hasNext(); )
		{
		    ChatRoom a = iter.next();
		    if (c.equals(a) && index > 0 /*not the lobby*/)
		    {
		        a.removeObservers(); //disconnect it from the users ones
		    	iter.remove(); //remove from list of chat rooms
		        return true;
		    }
		    index ++;
		}
		return false; //could not remove chat room
	}
	public void initiateChat(UserBean initiator, String requestedUser, ChatRoom chatRoom)
	{
		//from which room did this request originate?
		ChatRoom master = getMasterChatRoomFromUserChatRoom(chatRoom);

		//what index is this?
		int index = chatRooms.indexOf(master);

		//find requested user
		UserBean requestedUserBean = users.get(requestedUser);
		requestedUserBean.setInvitation(new Invitation("invites you to a chat. Accept?", initiator.getName(), index));
	}
	public ChatRoom acceptInvitation(Invitation invitation) throws IOException
	{
		ChatRoom chatRoomInitiator = null;
		ChatRoom chatRoomAcceptor = null;
		ChatRoom chatRoomMaster = null;

		if(invitation.getMasterChatRoomIndex() == 0) //originating from lobby so need new room
		{
			chatRoomMaster = new ChatRoom("Chat Room - " + invitation.getName());
			chatRoomMaster.setOwner(this);
			chatRoomInitiator = new ChatRoom();
			chatRoomMaster.addObserver(chatRoomInitiator);
			chatRoomInitiator.addObserver(chatRoomMaster);
			chatRooms.add(chatRoomMaster);

			//find initiator
			UserBean initiator = users.get(invitation.getName());
			initiator.invitationAccepted(chatRoomInitiator);
			//the above is to create a new room for the initiator
			//as well - this is only needed if we are creating a
			//new room. In other cases the initiator just stays
			//where he is, and it's only the acceptor that needs
			//a new room.
		}
		else //an already existing room
		{
			chatRoomMaster = chatRooms.get(invitation.getMasterChatRoomIndex());
		}

		//the acceptor will need a new room regardless of whether
		//we are starting a new one or joining an existing one
		chatRoomAcceptor = new ChatRoom();
		chatRoomMaster.addObserver(chatRoomAcceptor);
		chatRoomAcceptor.addObserver(chatRoomMaster);

		return chatRoomAcceptor;
	}
	public void command(Command command, String argument, String originalSender, ChatRoom chatRoom) {
		// TODO Auto-generated method stub

	}
}
