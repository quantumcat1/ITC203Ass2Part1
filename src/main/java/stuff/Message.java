package stuff;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message
{
	private String name;
	private String ts;
	private String message;
	private String colour;

	public Message (String message, String name)
	{
		colour = "";
		this.name = name;
		this.message = message;
		ts = new SimpleDateFormat("HH.mm.ss").format(new Date());
	}
	public Message (String message, String name, String colour)
	{
		this.colour = colour;
		this.name = name;
		this.message = message;
		ts = new SimpleDateFormat("HH.mm.ss").format(new Date());
	}
	public Message(String message)
	{
		this(message, "System");
	}
	@Override
	public String toString() {
		return "[" + ts + "] " + name + ": " + message;
	}
	@Override
	public boolean equals(Object obj)
	{
		Message other = (Message) obj;
		if (!name.equals(other.name))
			return false;
		if (!ts.equals(other.ts))
			return false;
		if (!message.equals(other.message))
			return false;
		return true;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTs() {
		return ts;
	}
	public void setTs(String ts) {
		this.ts = ts;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getColour()
	{
		if(colour.trim().equals("")) return "000000";
		return colour;
	}
	public void setColour(String colour) {
		this.colour = colour;
	}

}
