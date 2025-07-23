package mod.arcomit.lucisrenderlib.core.postprocessing.target;

import java.util.Objects;

@FunctionalInterface
public interface ScreenResizeEventHandler {
    void consume(float w, float h);

    default ScreenResizeEventHandler andThen(ScreenResizeEventHandler after) {
        Objects.requireNonNull(after);
        return (width, height) -> {
            consume(width, height);
            after.consume(width, height);
        };
    }
}
