package org.knx;

import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class FunctionalMatcher<T> extends TypeSafeMatcher<T>
{

    private final Function<T, Boolean> consumer;
    private final String description;

    public FunctionalMatcher(final Function<T, Boolean> consumer, final String description)
    {
        this.consumer = consumer;
        this.description = description;
    }

    @Override
    protected boolean matchesSafely(final T item)
    {
        return consumer.apply(item);
    }

    @Override
    public void describeTo(final Description description)
    {
        description.appendText(this.description);
    }

    public static <T> Matcher<T> isTrue(final Function<T, Boolean> consumer, final String description)
    {
        return new FunctionalMatcher<>(consumer, description);
    }

}
