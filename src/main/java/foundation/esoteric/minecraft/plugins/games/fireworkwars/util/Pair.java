package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

public class Pair<A, B> {
    private final A a;
    private final B b;

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getLeft() {
        return a;
    }

    public B getRight() {
        return b;
    }
}
