package sir.dev.client.screen;

import net.minecraft.screen.ScreenHandlerType;
import sir.dev.client.screen.dev.DevScreenHandler;

public class ModScreenHandlers
{
    public static ScreenHandlerType<DevScreenHandler> DEV_SCREEN_HANDLER_TYPE;

    public static void register()
    {
        DEV_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(DevScreenHandler::new);
    }
}
