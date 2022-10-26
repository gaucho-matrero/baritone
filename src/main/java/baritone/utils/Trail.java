package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.pathing.goals.GoalXZ;
import baritone.api.pathing.path.IPathExecutor;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.BetterBlockPos;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.RandomSpotNearby;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

/**
 * @author Meloweh
 */
public final class Trail {
    private final Queue<Pack> traversedChunks;
    private BetterBlockPos prev;
    private final IPlayerContext player;
    private final static int VECTOR_LIMIT = 350;
    private final static int CROSS_LIMIT = 10;
    private final static int CHUNK_TAIL_MAX = 4*4;
    private final static Trail trail = new Trail();

    public Trail() {
        this.player = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();
        this.traversedChunks = new ArrayDeque<>();
    }

    public static Trail getInstance() {
        return trail;
    }

    private boolean equalPos(final BetterBlockPos pos1, final BetterBlockPos pos2) {
        if (isNull(pos1) || isNull(pos2)) return false;
        return pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }

    private boolean equalChunk(final ChunkPos c1, final ChunkPos c2) {
        return c1.x == c2.x && c1.z == c2.z;
    }

    public boolean passedLimits() {
        return traversedChunks.stream().anyMatch(e ->
                e.crossings.entrySet().stream().anyMatch(a -> a.getValue() > CROSS_LIMIT));
    }

    private int getVectorCount() {
        return traversedChunks.stream().mapToInt(e -> e.crossings.size()).sum();
    }

    private boolean passedVectorCountLimit() {
        return getVectorCount() > VECTOR_LIMIT;
    }
    private Optional<Pack> getRefFromTraversedChunks(final ChunkPos pos) {
        return traversedChunks.stream().filter(e -> equalChunk(e.chunkPos, pos)).findFirst();
    }

    public void reset() {
        traversedChunks.clear();
        prev = null;
        pathing = false;
        stuckPos = null;
        System.out.println("target null");
        targetPos = null;
    }

    private Pack removeOutsiders() {
        if (traversedChunks.size() > CHUNK_TAIL_MAX) return traversedChunks.poll();
        if (passedVectorCountLimit()) traversedChunks.clear();
        return null;
    }

    private boolean pathing = false;
    private BetterBlockPos stuckPos;
    private RandomSpotNearby randSpot = new RandomSpotNearby(16d);
    private BetterBlockPos targetPos;

    public boolean reactivateRunAway() {
        final BetterBlockPos pos = getCurrent();
        if (isNull(pos)) return false;
        stuckPos = pos;
        System.out.println("target set");
        targetPos = randSpot.next(stuckPos);
        pathing = true;
        return true;
    }

    public boolean isRunAwayActive() {
        return pathing;
    }

    public boolean updateAndCheck() {
        tick();

        if (!passedLimits()) {
            return false;
        }

        final BetterBlockPos curr = getCurrent();
        if (isNull(curr)) return false;

        if (targetPos != null && curr.closerThan(new Vec3i(targetPos.getX(), curr.getY(), targetPos.getZ()), 3d)) {
            reset();
            return false;
        }
        if (!pathing) {
            reactivateRunAway();
        }

        return true;
    }

    public PathingCommand getRunAwayCommand() {
        if (targetPos == null) {
            throw new IllegalStateException("targetPos is null");
        }
        return new PathingCommand(new GoalXZ(targetPos), PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
    }

    private int addOrInc(final Pack pack, final BetterBlockPos blockPos, final boolean shouldAddPack) {
        final Optional<BetterBlockPos> optRefBlockPos = pack.crossings.keySet().stream().filter(e -> equalPos(e, blockPos)).findFirst();
        final BetterBlockPos refBlockPos = (optRefBlockPos.isEmpty()) ? blockPos : optRefBlockPos.get();
        final int counter = (optRefBlockPos.isEmpty()) ? 1 : pack.crossings.get(refBlockPos) + 1;
        pack.crossings.put(refBlockPos, counter);
        if (shouldAddPack) traversedChunks.add(pack);
        return counter;
    }


    //TODO tick() may be called multiple times in one tick. This shouldn't be limited to one possible update per tick.
    public void tick() {
        final BetterBlockPos curr = getCurrent();
        if (isNull(curr) || equalPos(prev, curr)) return;
        prev = curr;
        final ChunkPos chunkPos = this.player.world().getChunk(curr).getPos();
        final Optional<Pack> optPack = getRefFromTraversedChunks(chunkPos);
        final Pack pack = optPack.isEmpty() ? new Pack(chunkPos) : optPack.get();
        final int counts = addOrInc(pack, curr, optPack.isEmpty());
        removeOutsiders();

        //debug
        //System.out.println(curr.getX() + ":" + curr.getY() + ":" + curr.getZ() + " Counts: "
        //        + counts + " Size: " + getVectorCount());
    }

    private static boolean isSet(final Object o) {
        return o != null;
    }

    private static boolean isNull(final Object o) { return !isSet(o);}

    private static BetterBlockPos getCurrent() {
        final IPathingBehavior p = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior();
        if (isNull(p)) return null;
        final IPathExecutor c = p.getCurrent();
        if (isNull(c)) return null;
        final List<BetterBlockPos> bbp = c.getPath().positions();
        if (isNull(bbp)) return null;
        final int posIndex = c.getPosition();
        if (c.getPath().positions().size() <= posIndex) return null;
        final BetterBlockPos pos = c.getPath().positions().get(posIndex);
        return pos;
    }

    private class Pack {
        public Pack(final ChunkPos chunkPos) {
            this.chunkPos = chunkPos;
            this.crossings = new HashMap<>();
        }
        public ChunkPos chunkPos;
        public Map<BetterBlockPos, Integer> crossings;
    }
}
