package notryken.chatnotify.config;

import java.util.HashMap;
import java.util.Map;

public class Config
{
    public Map<Integer, NotifyOption> optionList;

    public Config()
    {
        optionList = new HashMap<>();

        optionList.put(1, new NotifyOption("username", 16757761, false, false, false, false, false));
        optionList.put(2, new NotifyOption("anvil", 3551023, false, true, false, false, false));
        optionList.put(3, new NotifyOption("wither", 10027008, true, false, false, false, false));
    }

    public NotifyOption getOption(int key)
    {
        return optionList.get(key);
    }
}
