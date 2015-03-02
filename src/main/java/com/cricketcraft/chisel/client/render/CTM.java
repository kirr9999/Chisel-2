package com.cricketcraft.chisel.client.render;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.cricketcraft.chisel.api.IFacade;
/**
 * The CTM renderer will draw the block's FACE using by assembling 4 quadrants from the 5 available block
 * textures.  The normal Texture.png is the blocks "unconnected" texture, and is used when CTM is disabled or the block
 * has nothing to connect to.  This texture has all of the outside corner quadrants  The texture-ctm.png contains the
 * rest of the quadrants.
 *
 *  ┌─────────────────┐ ┌────────────────────────────────┐
 *  │ texture.png     │ │ texture-ctm.png                │
 *  │ ╔══════╤══════╗ │ │  ──────┼────── ║ ─────┼───── ║ │
 *  │ ║      │      ║ │ │ │      │      │║      │      ║ │
 *  │ ║ 16   │ 17   ║ │ │ │ 0    │ 1    │║ 2    │ 3    ║ │
 *  │ ╟──────┼──────╢ │ │ ┼──────┼──────┼╟──────┼──────╢ │
 *  │ ║      │      ║ │ │ │      │      │║      │      ║ │
 *  │ ║ 18   │ 19   ║ │ │ │ 4    │ 5    │║ 6    │ 7    ║ │
 *  │ ╚══════╧══════╝ │ │  ──────┼────── ║ ─────┼───── ║ │
 *  └─────────────────┘ │ ═══════╤═══════╝ ─────┼───── ╚ │
 *                      │ │      │      ││      │      │ │
 *                      │ │ 8    │ 9    ││ 10   │ 11   │ │
 *                      │ ┼──────┼──────┼┼──────┼──────┼ │
 *                      │ │      │      ││      │      │ │
 *                      │ │ 12   │ 13   ││ 14   │ 15   │ │
 *                      │ ═══════╧═══════╗ ─────┼───── ╔ │
 *                      └────────────────────────────────┘
 *
 * combining { 18, 13,  9, 16 }, we can generate a texture connected to the right!
 *
 *  ╔══════╤═══════
 *  ║      │      │
 *  ║ 16   │ 9    │
 *  ╟──────┼──────┼
 *  ║      │      │
 *  ║ 18   │ 13   │
 *  ╚══════╧═══════
 *
 *
 * combining { 18, 13, 11,  2 }, we can generate a texture, in the shape of an L (connected to the right, and up
 *
 *  ║ ─────┼───── ╚
 *  ║      │      │
 *  ║ 2    │ 11   │
 *  ╟──────┼──────┼
 *  ║      │      │
 *  ║ 18   │ 13   │
 *  ╚══════╧═══════
 *
 *
 * HAVE FUN!
 * -CptRageToaster-
 *
 * TODO: Everything
 */


public class CTM {

    /**
     *  The quads are ordered the same here, and in the renderer:
     *  1. Lower left
     *  2. Lower Right
     *  3. Top Right
     *  4. Top Left
     */
	static int submaps[][] = {
            { 18, 19, 17, 16 }, //  0 - No connection, with border

            { 18, 19,  3,  2 }, //  1 - Connected from above
            { 18, 13,  9, 16 }, //  2 - Connected to the right
            {  6,  7, 17, 16 }, //  3 - Connected from below
            { 12, 19, 17,  8 }, //  4 - Connected to the left

            {  6,  7,  3,  2 }, //  5 - ║
            { 12, 13,  9,  8 }, //  6 - ═
            { 18, 13, 11,  2 }, //  7 - ╚  with inside corner
            {  6, 15,  9, 16 }, //  8 - ╔  with inside corner
            { 14,  7, 17,  8 }, //  9 - ╗  with inside corner
            { 12, 19,  3, 10 }, // 10 - ╝  with inside corner

            { 18, 13,  1,  2 }, // 11 - ╚  no inside corner
            {  6,  5,  9, 16 }, // 12 - ╔  no inside corner
            {  4,  7, 17,  8 }, // 13 - ╗  no inside corner
            { 12, 19,  3,  0 }, // 14 - ╝  no inside corner

            {  6, 15, 11,  2 }, // 15 - ╠  with inside corners
            { 14, 15,  9,  8 }, // 16 - ╦  with inside corners
            { 14,  7,  3, 10 }, // 17 - ╣  with inside corners
            { 12, 13, 11, 10 }, // 18 - ╩  with inside corners

            {  6,  5, 11,  2 }, // 23 - ╠  with top right inside corner
            {  4, 15,  9,  8 }, // 24 - ╦  with bottom right inside corner
            { 14,  7,  3,  0 }, // 25 - ╣  with bottom left inside corner
            { 12, 13,  1, 10 }, // 26 - ╩  with top left inside corner

            {  6, 15,  1,  2 }, // 19 - ╠  with bottom right inside corner
            { 14,  5,  9,  8 }, // 20 - ╦  with bottom left inside corner
            {  4,  7,  3, 10 }, // 21 - ╣  with top left inside corner
            { 12, 13, 11,  0 }, // 22 - ╩  with top right inside corner

            {  6,  5,  1,  2 }, // 27 - ╠  no inside corners
            {  4,  5,  9,  8 }, // 28 - ╦  no inside corners
            {  4,  7,  3,  0 }, // 29 - ╣  no inside corners
            { 12, 13,  1,  0 }, // 30 - ╩  no inside corners

            // We're counting in binary here...
            //        Top Left  Top Right  Bottom Right  Bottom Left
            //           │           │           │            │
            //           │           └──────┐ ┌──┘            │
            //           └────────────────┐ │ │ ┌─────────────┘
            { 14, 15, 11, 10 }, // 31 - ╬ 0 0 0 0
            {  4, 15, 11, 10 }, // 32 - ╬ 0 0 0 1
            { 14,  5, 11, 10 }, // 33 - ╬ 0 0 1 0
            {  4,  5, 11, 10 }, // 34 - ╬ 0 0 1 1
            { 14, 15,  1, 10 }, // 35 - ╬ 0 1 0 0
            {  4, 15,  1, 10 }, // 36 - ╬ 0 1 0 1
            { 14,  5,  1, 10 }, // 37 - ╬ 0 1 1 0
            {  4,  5,  1, 10 }, // 38 - ╬ 0 1 1 1
            { 14, 15, 11,  0 }, // 39 - ╬ 1 0 0 0
            {  4, 15, 11,  0 }, // 40 - ╬ 1 0 0 1
            { 14,  5, 11,  0 }, // 41 - ╬ 1 0 1 0
            {  4,  5, 11,  0 }, // 42 - ╬ 1 0 1 1
            { 14, 15,  1,  0 }, // 43 - ╬ 1 1 0 0
            {  4, 15,  1,  0 }, // 44 - ╬ 1 1 0 1
            { 14,  5,  1,  0 }, // 45 - ╬ 1 1 1 0
            {  4,  5,  1,  0 }, // 46 - ╬ 1 1 1 1
    };

	public static int[] getSubmapIndices(IBlockAccess world, int x, int y, int z, int side) {
		int index = getTexture(world, x, y, z, side);

		return submaps[index];
	}

	public static int getTexture(IBlockAccess world, int x, int y, int z, int side) {
		if (world == null)
			return 0;

		Block block = world.getBlock(x, y, z);
		int blockMetadata = world.getBlockMetadata(x, y, z);

		boolean b[] = new boolean[8];
        /**
         * b[0]    b[1]    b[2]
         *
         *
         *
         * b[3]    FACE    b[4]
         *
         *
         *
         * b[5]    b[6]    b[7]
         */
        if (side == 0) {
            b[0] = isConnected(world, x - 1, y, z + 1, side, block, blockMetadata);
            b[1] = isConnected(world, x, y, z + 1, side, block, blockMetadata);
            b[2] = isConnected(world, x + 1, y, z + 1, side, block, blockMetadata);
            b[3] = isConnected(world, x - 1, y, z, side, block, blockMetadata);
            b[4] = isConnected(world, x + 1, y, z, side, block, blockMetadata);
            b[5] = isConnected(world, x - 1, y, z - 1, side, block, blockMetadata);
            b[6] = isConnected(world, x, y, z - 1, side, block, blockMetadata);
            b[7] = isConnected(world, x + 1, y, z - 1, side, block, blockMetadata);
        } else if (side == 1) {
            b[0] = isConnected(world, x - 1, y, z - 1, side, block, blockMetadata);
            b[1] = isConnected(world, x, y, z - 1, side, block, blockMetadata);
            b[2] = isConnected(world, x + 1, y, z - 1, side, block, blockMetadata);
            b[3] = isConnected(world, x - 1, y, z, side, block, blockMetadata);
            b[4] = isConnected(world, x + 1, y, z, side, block, blockMetadata);
            b[5] = isConnected(world, x - 1, y, z + 1, side, block, blockMetadata);
            b[6] = isConnected(world, x, y, z + 1, side, block, blockMetadata);
            b[7] = isConnected(world, x + 1, y, z + 1, side, block, blockMetadata);
		} else if (side == 2) {
			b[0] = isConnected(world, x + 1, y + 1, z, side, block, blockMetadata);
			b[1] = isConnected(world, x, y + 1, z, side, block, blockMetadata);
			b[2] = isConnected(world, x - 1, y + 1, z, side, block, blockMetadata);
			b[3] = isConnected(world, x + 1, y, z, side, block, blockMetadata);
            b[4] = isConnected(world, x - 1, y, z, side, block, blockMetadata);
            b[5] = isConnected(world, x + 1, y - 1, z, side, block, blockMetadata);
            b[6] = isConnected(world, x, y - 1, z, side, block, blockMetadata);
            b[7] = isConnected(world, x - 1, y - 1, z, side, block, blockMetadata);
		} else if (side == 3) {
            b[0] = isConnected(world, x - 1, y + 1, z, side, block, blockMetadata);
            b[1] = isConnected(world, x, y + 1, z, side, block, blockMetadata);
            b[2] = isConnected(world, x + 1, y + 1, z, side, block, blockMetadata);
            b[3] = isConnected(world, x - 1, y, z, side, block, blockMetadata);
            b[4] = isConnected(world, x + 1, y, z, side, block, blockMetadata);
            b[5] = isConnected(world, x - 1, y - 1, z, side, block, blockMetadata);
            b[6] = isConnected(world, x, y - 1, z, side, block, blockMetadata);
            b[7] = isConnected(world, x + 1, y - 1, z, side, block, blockMetadata);
		} else if (side == 4) {
			b[0] = isConnected(world, x, y + 1, z - 1, side, block, blockMetadata);
			b[1] = isConnected(world, x, y + 1, z, side, block, blockMetadata);
			b[2] = isConnected(world, x, y + 1, z + 1, side, block, blockMetadata);
			b[3] = isConnected(world, x, y, z - 1, side, block, blockMetadata);
            b[4] = isConnected(world, x, y, z + 1, side, block, blockMetadata);
            b[5] = isConnected(world, x, y - 1, z - 1, side, block, blockMetadata);
            b[6] = isConnected(world, x, y - 1, z, side, block, blockMetadata);
            b[7] = isConnected(world, x, y - 1, z + 1, side, block, blockMetadata);
		} else if (side == 5) {
            b[0] = isConnected(world, x, y + 1, z + 1, side, block, blockMetadata);
            b[1] = isConnected(world, x, y + 1, z, side, block, blockMetadata);
            b[2] = isConnected(world, x, y + 1, z - 1, side, block, blockMetadata);
            b[3] = isConnected(world, x, y, z + 1, side, block, blockMetadata);
            b[4] = isConnected(world, x, y, z - 1, side, block, blockMetadata);
            b[5] = isConnected(world, x, y - 1, z + 1, side, block, blockMetadata);
            b[6] = isConnected(world, x, y - 1, z, side, block, blockMetadata);
            b[7] = isConnected(world, x, y - 1, z - 1, side, block, blockMetadata);
		}

        int numConnectedSides = (b[1] ? 1 : 0) + (b[4] ? 1 : 0) + (b[6] ? 1 : 0) + (b[3] ? 1 : 0);

        if (numConnectedSides == 1) {
            if (b[1]) {
                return 1;
            } else if (b[4]) {
                return 2;
            } else if (b[6]) {
                return 3;
            } else if (b[3]) {
                return 4;
            }
        }

        if (numConnectedSides == 2) {
            if (b[1] && b[6]) {
                return 5;
            } else if (b[4] && b[3]) {
                return 6;
            } else if (b[1] && b[4]) {
                return 7 + (b[2] ? 4 : 0);
            } else if (b[4] && b[6]) {
                return 8 + (b[7] ? 4 : 0);
            } else if (b[6] && b[3]) {
                return 9 + (b[5] ? 4 : 0);
            } else if (b[3] && b[1]) {
                return 10 + (b[0] ? 4 : 0);
            }
        }

        if (numConnectedSides == 3) {
            if (b[1] && b[4] && b[6]) {
                return 15 + (b[7] ? 4 : 0) + (b[2] ? 8 : 0);
            } else if (b[4] && b[6] && b[3]) {
                return 16 + (b[5] ? 4 : 0) + (b[7] ? 8 : 0);
            } else if (b[6] && b[3] && b[1]) {
                return 17 + (b[0] ? 4 : 0) + (b[5] ? 8 : 0);
            } else if (b[3] && b[1] && b[4]) {
                return 18 + (b[2] ? 4 : 0) + (b[0] ? 8 : 0);
            }
        }

        if (numConnectedSides == 4) {
            return 31 + (b[5] ? 1 : 0) + (b[7] ? 2 : 0) + (b[2] ? 4 : 0) + (b[0] ? 8 : 0);
        }

        return 0;
	}

	public static boolean isConnected(IBlockAccess world, int x, int y, int z, int side, Block block, int meta) {

		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];

		int x2 = x + dir.offsetX;
		int y2 = y + dir.offsetY;
		int z2 = z + dir.offsetZ;
		
		Block con = getBlockOrFacade(world, x, y, z, side);
		Block obscuring = getBlockOrFacade(world, x2, y2, z2, side);
		
		// no block or a bad API user
		if (con == null) {
			return false;
		}
		
		boolean ret = con.equals(block) && getBlockOrFacadeMetadata(world, x, y, z, side) == meta;
		
		// no block obscuring this face
		if (obscuring == null) {
			return true;
		}
		
		// check that we aren't already connected outwards from this side
		ret &= !(obscuring.equals(block) && getBlockOrFacadeMetadata(world, x2, y2, z2, side) == meta);
		
		return ret;
	}

	public static int getBlockOrFacadeMetadata(IBlockAccess world, int x, int y, int z, int side) {
		Block blk = world.getBlock(x, y, z);
		if (blk instanceof IFacade) {
			return ((IFacade) blk).getFacadeMetadata(world, x, y, z, side);
		}
		return world.getBlockMetadata(x, y, z);
	}

	public static Block getBlockOrFacade(IBlockAccess world, int x, int y, int z, int side) {
		Block blk = world.getBlock(x, y, z);
		if (blk instanceof IFacade) {
			blk = ((IFacade) blk).getFacade(world, x, y, z, side);
		}
		return blk;
	}
}
