package impl.lib.optional;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * TODO: write a test for me
 * Created by chris on 7/25/16.
 */
public class OptionalConsumer<T> {
    private Optional<T> optional;

    private OptionalConsumer(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> OptionalConsumer<T> of(Optional<T> optional) {
        return new OptionalConsumer<T>(optional);
    }

    public OptionalConsumer<T> ifPresent(Consumer<T> c) {
        optional.ifPresent(c);
        return this;
    }

    public OptionalConsumer<T> ifNotPresent(Runnable r) {
        if(!optional.isPresent()) {
            r.run();
        }
        return this;
    }
}
