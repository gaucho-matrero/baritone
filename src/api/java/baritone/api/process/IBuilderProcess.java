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

package baritone.api.process;

import baritone.api.schematic.ISchematic;
import baritone.api.utils.BetterBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Brady
 * @since 1/15/2019
 */
public interface IBuilderProcess extends IBaritoneProcess {

    /**
     * Requests a build for the specified schematic, labeled as specified, with the specified origin.
     *
     * @param name      A user-friendly name for the schematic
     * @param schematic The object representation of the schematic
     * @param origin    The origin position of the schematic being built
     */
    void build(String name, ISchematic schematic, Vec3i origin);

    /**
     * Requests a build for the specified schematic, labeled as specified, with the specified origin.
     *
     * @param name      A user-friendly name for the schematic
     * @param schematic The file path of the schematic
     * @param origin    The origin position of the schematic being built
     * @return Whether or not the schematic was able to load from file
     */
    boolean build(String name, File schematic, Vec3i origin);

    default boolean build(String schematicFile, BlockPos origin) {
        File file = new File(new File(Minecraft.getInstance().gameDirectory, "schematics"), schematicFile);
        return build(schematicFile, file, origin);
    }

    void build(String name, ISchematic schematic, Vec3i origin, boolean fromAltoclef);

    boolean build(String name, File schematic, Vec3i origin, boolean fromAltoclef);

    default boolean build(String schematicFile, BlockPos origin, boolean fromAltoclef) {
        File file = new File(new File(Minecraft.getInstance().gameDirectory, "schematics"), schematicFile);
        return build(schematicFile, file, origin, fromAltoclef);
    }

    void buildOpenSchematic();

    void pause();

    boolean isPaused();

    void resume();

    void clearArea(BlockPos corner1, BlockPos corner2);

    /**
     * @return A list of block states that are estimated to be placeable by this builder process. You can use this in
     * schematics, for example, to pick a state that the builder process will be happy with, because any variation will
     * cause it to give up. This is updated every tick, but only while the builder process is active.
     */
    List<BlockState> getApproxPlaceable();

    Map<BlockState, Integer> getMissing();

    Vec3i getSchemSize();

    boolean isFromAltoclefFinished();

    boolean isFromAltoclef();

    void reset();

    boolean clearState();

    void noteInsert(final BlockPos pos);

    void popStack();
}
