package sir.dev.common.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds
{
    public static final Identifier DEV_AMBIENT_ID = new Identifier("devmod:dev_ambient");
    public static SoundEvent DEV_AMBIENT = SoundEvent.of(DEV_AMBIENT_ID);

    public static void register()
    {
        Registry.register(Registries.SOUND_EVENT, DEV_AMBIENT_ID, DEV_AMBIENT);
    }
}
