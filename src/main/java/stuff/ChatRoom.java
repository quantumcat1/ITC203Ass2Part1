package stuff;

import java.util.Observer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class ChatRoom extends Observable implements Observer
{
	private String name;
	private List<Observer> observers;
	private List<Message> messages;
	ChatOwner owner;
	//MString tempMessage;
	String tempMessage;
	String colour;

	public enum Command
	{
		INITIATE_CHAT("chat"), QUIT("quit"), WHO("who");
		private String c;
		Command(String c)
		{
			this.c = c;
		}
		public String getValue()
		{
			return c;
		}
		public static Command fromString(String text) {
		    for (Command b : Command.values()) {
		      if (b.c.equalsIgnoreCase(text)) {
		        return b;
		      }
		    }
		    return null;
		  }
	}

	public String getColour()
	{
		if(colour.trim().equals("")) return "000000";
		return colour;
	}
	public void setColour(String colour) {
		this.colour = colour;
	}
	public ChatRoom()
	{
		observers = new ArrayList<Observer>();
		messages = new LinkedList<Message>();
		tempMessage = "";
		name = "";
		colour="";
	}
	public ChatRoom(String name)
	{
		this.name = name;
		observers = new ArrayList<Observer>();
		messages = new LinkedList<Message>();
		tempMessage = "";
		colour="";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isObserving(ChatRoom chatRoom)
	{
		boolean isObserving = false;
		for(Observer o: observers)
		{
			if(o.equals(chatRoom))
			{
				isObserving = true;
				break;
			}
		}
		return isObserving;
	}

	public String getTempMessage() {
		//return tempMessage.getString();
		return tempMessage;
	}

	public void setTempMessage(String tempMessage) {
		//this.tempMessage.setString(tempMessage);
		this.tempMessage = tempMessage;
	}

	public ChatOwner getOwner() {
		return owner;
	}

	public void setOwner(ChatOwner owner) {
		this.owner = owner;
	}

	public List<Message> getMessages()
	{
		return messages;
	}
	public void removeName(String userName)
	{
		name = name.replace(userName, "");
		name = name.replace(", ,", ","); //e.g. mary, bob, jane -> mary, , jane -> mary, jane
	}

	//for adding a user that will be notified of incoming messages
	public void addObserver(Observer o)
	{
		if(name.equals("") && !((ChatRoom)o).getName().equals(""))
		{
			name = ((ChatRoom)o).getName();
		}
		observers.add(o);
	}
	public void removeObservers()
	{
		for (Iterator<Observer> iter = observers.listIterator(); iter.hasNext(); )
		{
		    Observer a = iter.next();
		    iter.remove();
		}
	}
	public void removeObserver(Observer o)
	{
		for (Iterator<Observer> iter = observers.listIterator(); iter.hasNext(); )
		{
		    Observer a = iter.next();
		    if (o.equals(a))
		    {
		        iter.remove();
		    }
		}
	}
	//inform all users about the new message
	public void notifyObserver()
	{
		for(Observer o: observers)
		{
			o.update(this, messages.get(messages.size()-1));
		}
	}
	//for when a user posts a new message
	public void update(Observable o, Object message)
	{
		name = ((ChatRoom)o).getName();
		//if we don't already have this message, update everybody else
		//if we already have this message, do nothing
		if(messages.size() == 0 || !messages.get(messages.size()-1).equals((Message)message))
		{
			addMessage((Message)message);
		}
	}
	//either the user inputting a message, or being input from an
	//update (see above method). We add the message then tell everyone else
	//(either the master, or the other users in the chat room if this
	//chat room instance belongs to the master)
	public void addMessage(Message message)
	{
		addMessage(message, true);
	}
	public void addMessage(Message message, boolean showOthers)
	{
		String m = message.getMessage();

		String[] possibleCommands = m.split(" ");
		Command c = null;
		c = Command.fromString(possibleCommands[0]);
		if(c == null)
		{
			//wasn't a command, just a normal message
			messages.add(message);
			if(showOthers) notifyObserver();
		}
		else
		{
			String argument = "";
			if(possibleCommands.length > 1) argument = possibleCommands[1];
			owner.command(c, argument, message.getName(), this);
		}
	}
}
