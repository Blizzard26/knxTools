package org.openhab.support.knx2openhab;

public class ThingExtractorException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = -3677469994400752075L;

    public ThingExtractorException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public ThingExtractorException(final String message)
    {
        super(message);
    }

    public ThingExtractorException(final Throwable cause)
    {
        super(cause);
    }

}
