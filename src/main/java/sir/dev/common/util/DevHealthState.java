package sir.dev.common.util;

public enum DevHealthState
{
    NORMAL,
    BIT_INFECTED,
    NEAR_INFECTED,
    ALMOST_INFECTED,
    COMPLETELY_INFECTED;

    public static DevHealthState getHealthState(float hp, float maxHP)
    {
        int percent = (int)((hp/maxHP) * 100);
        DevHealthState state = DevHealthState.COMPLETELY_INFECTED;

        if (percent > 8)
        {
            state = DevHealthState.ALMOST_INFECTED;
        }
        if (percent > 15)
        {
            state = DevHealthState.NEAR_INFECTED;
        }
        if (percent > 25)
        {
            state = DevHealthState.BIT_INFECTED;
        }
        if (percent > 40)
        {
            state = DevHealthState.NORMAL;
        }

        return state;
    }
}
