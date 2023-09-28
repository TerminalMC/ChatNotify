package notryken.chatnotify;

import net.fabricmc.api.ClientModInitializer;

public class ChatNotifyFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ChatNotify.init();
    }
}