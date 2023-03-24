package sir.dev.client.screen;

import net.minecraft.screen.ScreenHandlerType;
import sir.dev.client.screen.dev.*;

public class ModScreenHandlers
{
    public static ScreenHandlerType<DevScreenHandler> DEV_SCREEN_HANDLER_TYPE;
    public static ScreenHandlerType<NormalDevScreenHandler> DEV_SCREEN_HANDLER_TYPE_NORMAL;
    public static ScreenHandlerType<Damaged1DevScreenHandler> DEV_SCREEN_HANDLER_TYPE_DMG_1;
    public static ScreenHandlerType<Damaged2DevScreenHandler> DEV_SCREEN_HANDLER_TYPE_DMG_2;
    public static ScreenHandlerType<Damaged3DevScreenHandler> DEV_SCREEN_HANDLER_TYPE_DMG_3;
    public static ScreenHandlerType<Damaged4DevScreenHandler> DEV_SCREEN_HANDLER_TYPE_DMG_4;

    public static void register()
    {
        DEV_SCREEN_HANDLER_TYPE = new ScreenHandlerType<>(DevScreenHandler::new);
        DEV_SCREEN_HANDLER_TYPE_NORMAL = new ScreenHandlerType<>(NormalDevScreenHandler::new);
        DEV_SCREEN_HANDLER_TYPE_DMG_1 = new ScreenHandlerType<>(Damaged1DevScreenHandler::new);
        DEV_SCREEN_HANDLER_TYPE_DMG_2 = new ScreenHandlerType<>(Damaged2DevScreenHandler::new);
        DEV_SCREEN_HANDLER_TYPE_DMG_3 = new ScreenHandlerType<>(Damaged3DevScreenHandler::new);
        DEV_SCREEN_HANDLER_TYPE_DMG_4 = new ScreenHandlerType<>(Damaged4DevScreenHandler::new);
    }
}
