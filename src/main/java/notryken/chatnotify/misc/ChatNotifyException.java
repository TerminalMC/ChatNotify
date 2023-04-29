package notryken.chatnotify.misc;

/**
 * Generic exception class for ChatNotify.
 */
public class ChatNotifyException extends Exception
{
    public ChatNotifyException()
    {
        super();
    }

    public ChatNotifyException(String msg)
    {
        super(msg);
    }

    public ChatNotifyException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
