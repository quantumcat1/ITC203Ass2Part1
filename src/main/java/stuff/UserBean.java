package stuff;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import stuff.ChatRoom.Command;

@ManagedBean
@SessionScoped
public class UserBean implements Serializable, ChatOwner
{
	private static final long serialVersionUID = 7263384829907778312L;

	@ManagedProperty(value="#{master}")
    private Master master;

	private String name;
	private String password;
	private List<ChatRoom> chatRooms;
	private Invitation invitation;
	private String errorMessage;

	@PostConstruct
	public void initialise()
	{
		chatRooms = new LinkedList<ChatRoom>();
		name = "";
		invitation = null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setInvitation(Invitation invitation)
	{
		this.invitation = invitation;
	}
	public Invitation getInvitation()
	{
		return invitation;
	}
	public void acceptInvitation() //throws IOException
	{
		ChatRoom chatRoom = null;
		try{chatRoom = master.acceptInvitation(invitation);}catch(Exception e){}
		chatRoom.setOwner(this);
		chatRoom.setName(chatRoom.getName() + ", " + name);
		chatRoom.addMessage(new Message ("User " + name + " has joined the chat."));
		chatRoom.addMessage(new Message("Users present: " + master.getUsersInRoom(chatRoom)), false); //don't show to others
		chatRooms.add(chatRoom);
		invitation = null;
		chatRoom.notifyObservers();//for change of name
		/*ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());*/
	}
	public void declineInvitation()
	{
		invitation = null;
	}
	public void invitationAccepted(ChatRoom chatRoom) //throws IOException
	{
		chatRoom.setOwner(this);
		chatRoom.addMessage(new Message("User " + name + " joined the chat."));
		chatRooms.add(chatRoom);
		chatRoom.addMessage(new Message("Users present: " + master.getUsersInRoom(chatRoom)), false); //don't show to others
		/*ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
	    ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());*/
	}

	public List<ChatRoom> getChatRooms() {
		return chatRooms;
	}

	public void setChatRooms(List<ChatRoom> chatRooms) {
		this.chatRooms = chatRooms;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public void startSession()
	{
		if(chatRooms.size() > 0)
		{
			errorMessage = "You are already logged in this session. Please go <a href=\"/faces/welcome.xhtml\">here</a>.";
			return;
		}
		if(name.trim().equals("") || password.trim().equals(""))
		{
			errorMessage = "Neither user name nor password can be blank";
			return;
		}
		ChatRoom cr = master.newUser(this);
		if(cr == null)
		{
			errorMessage = "Wrong password for existing user. Please try again";
			return;
		}
		cr.setOwner(this);
		chatRooms.add(cr);
		cr.addMessage(new Message("Users present: " + master.getUsersInRoom(cr)), false); //don't show to others
		try {
			//FacesContext.getCurrentInstance().getExternalContext().redirect("/simple-webapp/faces/welcome.xhtml");
			FacesContext.getCurrentInstance().getExternalContext().redirect("faces/welcome.xhtml");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public List<Message> getMessages(int chatRoomIndex)
	{
		if(chatRooms.size() < chatRoomIndex) return null;
		return chatRooms.get(chatRoomIndex).getMessages();
	}
	public Master getMaster()
	{
		return master;
	}
	public void setMaster(Master master)
	{
		this.master = master;
	}
	public void addMessage(int chatRoomIndex)
	{
		addMessage(new Message(chatRooms.get(chatRoomIndex).getTempMessage(), name, chatRooms.get(chatRoomIndex).getColour()), chatRoomIndex, true); //default: show others the message
	}
	public void addMessage(Message message, int chatRoomIndex, boolean showOthers)
	{
		chatRooms.get(chatRoomIndex).addMessage(message, showOthers);
	}
	public boolean removeChatRoom(ChatRoom c)
	{
		int index = 0;
		for (Iterator<ChatRoom> iter = chatRooms.listIterator(); iter.hasNext(); )
		{
		    ChatRoom a = iter.next();
		    if (c.equals(a) && index > 0 /*not the lobby*/)
		    {
		    	addMessage(new Message("User " + name + " left the chat."), index, true);
		    	c.removeName(name);
		    	c.notifyObservers(); //tell them the name has changed
		        a.removeObservers(); //disconnect it from the master one. It will still receive updates from the master one (the master one just won't receive updates from it) since it is observing it but we can't do much about that, from here.
		    	iter.remove(); //remove from list of chat rooms
		        return true;
		    }
		    index ++;
		}
		return false; //could not remove chat room
	}
	public ChatRoom getObservingChatRoom(ChatRoom master)
	{
		for(ChatRoom c: chatRooms)
		{
			if(c.isObserving(master))
			{
				return c;
			}
		}
		return null;
	}

	public void command(Command command, String argument, String originalSender, ChatRoom chatRoom)
	{
		//if(!name.equalsIgnoreCase(originalSender)) return; //command is not meant for us - just in case it gets redistributed. Remove this when sure that it won't get redistributed.
		switch(command)
		{
		case QUIT:
			master.quitRoom(this, chatRoom); //master will tell the user to quit his chat room so we don't need to do this as well
			break;

		case INITIATE_CHAT:
			if(!name.equals(argument)) master.initiateChat(this, argument, chatRoom);
			break;

		case WHO:
			chatRoom.addMessage(new Message("Users present: " + master.getUsersInRoom(chatRoom)), false); //don't show to others
			break;

		}

	}
}
