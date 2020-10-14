package org.knx.xml;

public class TypeMapping
{
    public static Boolean parseEnableT(String enable)
    {
        if (enable == null)
            return null;
        else if ("Enabled".equals(enable))
            return Boolean.TRUE;
        else
            return Boolean.FALSE;
    }

    public static String printEnableT(Boolean enable)
    {
        if (enable == null)
            return null;
        else if (Boolean.TRUE.equals(enable))
            return "Enabled";
        else
            return "Disabled";
    }
}
