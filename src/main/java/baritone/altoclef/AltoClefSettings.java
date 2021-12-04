/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.altoclef;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class AltoClefSettings {

    private final Object breakMutex = new Object();
    private final Object placeMutex = new Object();

    private final Object propertiesMutex = new Object();

    private final HashSet<BlockPos> _blocksToAvoidBreaking = new HashSet<>();
    private final List<Predicate<BlockPos>> _breakAvoiders = new ArrayList<>();

    private final List<Predicate<BlockPos>> _placeAvoiders = new ArrayList<>();

    private final List<Predicate<BlockPos>> _forceCanWalkOn = new ArrayList<>();

    private final List<BiPredicate<BlockState, ItemStack>> _forceUseTool = new ArrayList<>();

    private final HashSet<Item> _protectedItems = new HashSet<>();

    private boolean _allowFlowingWaterPass;

    private boolean _pauseInteractions;

    private boolean _dontPlaceBucketButStillFall;

    private boolean _allowShears = true;

    private boolean _allowSwimThroughLava = false;

    public void avoidBlockBreak(BlockPos pos) {
        synchronized (breakMutex) {
            _blocksToAvoidBreaking.add(pos);
        }
    }
    public void avoidBlockBreak(Predicate<BlockPos> avoider) {
        synchronized (breakMutex) {
            _breakAvoiders.add(avoider);
        }
    }

    public void configurePlaceBucketButDontFall(boolean allow) {
        synchronized (propertiesMutex) {
            _dontPlaceBucketButStillFall = allow;
        }
    }

    public void avoidBlockPlace(Predicate<BlockPos> avoider) {
        synchronized (placeMutex) {
            _placeAvoiders.add(avoider);
        }
    }

    public boolean shouldAvoidBreaking(int x, int y, int z) {
        return shouldAvoidBreaking(new BlockPos(x, y, z));
    }
    public boolean shouldAvoidBreaking(BlockPos pos) {
        synchronized (breakMutex) {
            if (_blocksToAvoidBreaking.contains(pos))
                return true;
            return (_breakAvoiders.stream().anyMatch(pred -> pred.test(pos)));
        }
    }
    public boolean shouldAvoidPlacingAt(BlockPos pos) {
        synchronized (placeMutex) {
            return _placeAvoiders.stream().anyMatch(pred -> pred.test(pos));
        }
    }
    public boolean shouldAvoidPlacingAt(int x, int y, int z) {
        return shouldAvoidPlacingAt(new BlockPos(x, y, z));
    }

    public boolean canWalkOnForce(int x, int y, int z) {
        synchronized (propertiesMutex) {
            return _forceCanWalkOn.stream().anyMatch(pred -> pred.test(new BlockPos(x, y, z)));
        }
    }

    public boolean shouldForceUseTool(BlockState state, ItemStack tool) {
        synchronized (propertiesMutex) {
            return _forceUseTool.stream().anyMatch(pred -> pred.test(state, tool));
        }
    }

    public boolean shouldNotPlaceBucketButStillFall() {
        synchronized (propertiesMutex) {
            return _dontPlaceBucketButStillFall;
        }
    }

    public boolean isInteractionPaused() {
        synchronized (propertiesMutex) {
            return _pauseInteractions;
        }
    }
    public boolean isFlowingWaterPassAllowed() {
        synchronized (propertiesMutex) {
            return _allowFlowingWaterPass;
        }
    }
    /**
     * @deprecated
     * Use `shouldForceUseToo` instead.
     */
    public boolean areShearsAllowed() {
        synchronized (propertiesMutex) {
            return _allowShears;
        }
    }
    public boolean canSwimThroughLava() {
        synchronized (propertiesMutex) {
            return _allowSwimThroughLava;
        }
    }

    public void setInteractionPaused(boolean paused) {
        synchronized (propertiesMutex) {
            _pauseInteractions = paused;
        }
    }
    public void setFlowingWaterPass(boolean pass) {
        synchronized (propertiesMutex) {
            _allowFlowingWaterPass = pass;
        }
    }

    /**
     * @deprecated
     * Use `getForceUseToolPredicates` instead.
     */
    @Deprecated
    public void allowShears(boolean allow) {
        synchronized (propertiesMutex) {
            _allowShears = allow;
        }
    }

    public void allowSwimThroughLava(boolean allow) {
        synchronized (propertiesMutex) {
            _allowSwimThroughLava = allow;
        }
    }

    public HashSet<BlockPos> getBlocksToAvoidBreaking() {
        return _blocksToAvoidBreaking;
    }
    public List<Predicate<BlockPos>> getBreakAvoiders() {
        return _breakAvoiders;
    }
    public List<Predicate<BlockPos>> getPlaceAvoiders() {
        return _placeAvoiders;
    }
    public List<Predicate<BlockPos>> getForceWalkOnPredicates() {
        return _forceCanWalkOn;
    }
    public List<BiPredicate<BlockState, ItemStack>> getForceUseToolPredicates() {
        return _forceUseTool;
    }

    public boolean isItemProtected(Item item) {
        return _protectedItems.contains(item);
    }
    public HashSet<Item> getProtectedItems() {
        return _protectedItems;
    }
    public void protectItem(Item item) {
        _protectedItems.add(item);
    }
    public void stopProtectingItem(Item item) {
        _protectedItems.remove(item);
    }

    public Object getBreakMutex() {
        return breakMutex;
    }
    public Object getPlaceMutex() {
        return placeMutex;
    }
    public Object getPropertiesMutex() {return propertiesMutex;}
}