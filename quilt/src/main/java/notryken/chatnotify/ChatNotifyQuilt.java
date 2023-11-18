package notryken.chatnotify;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ChatNotifyQuilt implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        Constants.LOG.info("Quilt Loader Initializing ChatNotify");
        ChatNotify.init();
    }
}