package baritone.api.utils;

import java.util.Random;

public class RandomSpotNearby {
    private BetterBlockPos end;
    private final Random rand;
    private double r;
    private double old_r;

    public RandomSpotNearby(final double r) {
        this.rand = new Random();
        this.r = r;
        this.old_r = r;
    }

    private final double MAX_DIST_INCREASE() {
        return old_r * 2;
    }

    private BetterBlockPos calc(final BetterBlockPos start) {
        final double phi = rand.nextInt(360) + rand.nextDouble();
        final double radius = rand.nextInt((int) Math.round(r)) + rand.nextDouble(); //rand.nextDouble(r) + rand.nextDouble();
        final int x = (int) Math.round(radius * Math.sin(phi));
        final int z = (int) Math.round(radius * Math.cos(phi));

        this.end = new BetterBlockPos(start.getX() + x, start.getY(), start.getZ() + z);
        return this.end;
    }

    public BetterBlockPos next(final BetterBlockPos start) {
        this.r = (this.r - this.old_r > MAX_DIST_INCREASE()) ? this.old_r : this.r;
        return next(start, 0.3d);
    }

    public BetterBlockPos next(final BetterBlockPos start, final double increaseRadius) {
        this.r += increaseRadius;
        return calc(start);
    }
}
