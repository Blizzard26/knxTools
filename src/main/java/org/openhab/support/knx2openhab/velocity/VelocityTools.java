package org.openhab.support.knx2openhab.velocity;

import java.util.List;

public class VelocityTools
{
    public static boolean containsPrefix(final List<String> list, final String item)
    {
        for (String str : list)
        {
            if (item.startsWith(str))
                return true;
        }
        return false;
    }
}
