package baritone.api.pathing.goals;

import baritone.api.BaritoneAPI;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.RandomSpotNearby;
import baritone.api.utils.SettingsUtil;
import net.minecraft.core.BlockPos;

public class GoalRandomSpotNearby implements Goal {

    private static final double SQRT_2 = Math.sqrt(2);
    private final int x;
    private final int z;

    public GoalRandomSpotNearby() {
        this(16d);
    }

    public GoalRandomSpotNearby(final double r) {
        this(BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext().player().blockPosition(), r);
    }

    public GoalRandomSpotNearby(final BlockPos pos, final double r) {
        if (pos == null) throw new IllegalStateException("No player position found.");
        final BetterBlockPos bbpCurr = (new RandomSpotNearby(r)).next(new BetterBlockPos(pos.getX(), pos.getY(), pos.getZ()));
        this.x = bbpCurr.getX();
        this.z = bbpCurr.getZ();
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return x == this.x && z == this.z;
    }

    @Override
    public double heuristic(int x, int y, int z) {//mostly copied from GoalBlock
        int xDiff = x - this.x;
        int zDiff = z - this.z;
        return calculate(xDiff, zDiff);
    }

    @Override
    public String toString() {
        return String.format(
                "GoalRandomSpotNearby{x=%s,z=%s}",
                SettingsUtil.maybeCensor(x),
                SettingsUtil.maybeCensor(z)
        );
    }

    public static double calculate(double xDiff, double zDiff) {
        //This is a combination of pythagorean and manhattan distance
        //It takes into account the fact that pathing can either walk diagonally or forwards

        //It's not possible to walk forward 1 and right 2 in sqrt(5) time
        //It's really 1+sqrt(2) because it'll walk forward 1 then diagonally 1
        double x = Math.abs(xDiff);
        double z = Math.abs(zDiff);
        double straight;
        double diagonal;
        if (x < z) {
            straight = z - x;
            diagonal = x;
        } else {
            straight = x - z;
            diagonal = z;
        }
        diagonal *= SQRT_2;
        return (diagonal + straight) * BaritoneAPI.getSettings().costHeuristic.value; // big TODO tune
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
