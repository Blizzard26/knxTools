package org.openhab.support.knx2openhab;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tools
{
    public static <T> void applyRecursive(final T t, final Function<T, ? extends Collection<T>> recurse,
            final Consumer<T> consumer)
    {
        consumer.accept(t);
        recurse.apply(t).forEach(t1 -> applyRecursive(t1, recurse, consumer));
    }

    public static <T> Stream<T> recursiveFlatMap(final T t, final Function<T, Stream<T>> recurse)
    {
        return Stream.concat(Stream.of(t), recurse.apply(t).map(t1 -> recursiveFlatMap(t1, recurse))
                .collect(Collectors.reducing(Stream::concat)).orElse(Stream.empty()));
    }
}
