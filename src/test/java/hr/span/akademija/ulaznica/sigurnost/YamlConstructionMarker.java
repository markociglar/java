package hr.span.akademija.ulaznica.sigurnost;

import java.util.concurrent.atomic.AtomicBoolean;

/** Bezopasni testni objekt: pokazuje je li YAML parser konstruirao proizvoljnu Java klasu. */
public class YamlConstructionMarker {

    public static final AtomicBoolean KONSTRUIRAN = new AtomicBoolean();

    public YamlConstructionMarker() {
        KONSTRUIRAN.set(true);
    }
}
