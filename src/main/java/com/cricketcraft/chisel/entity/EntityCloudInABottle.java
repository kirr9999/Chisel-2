package com.cricketcraft.chisel.entity;

import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;

import com.cricketcraft.chisel.init.ChiselBlocks;
import com.mojang.authlib.GameProfile;

public class EntityCloudInABottle extends EntityThrowable {

	Random rand = new Random();
	EntityPlayer player;

	public EntityCloudInABottle(World par1World) {
		super(par1World);
		if(!par1World.isRemote)
			player = FakePlayerFactory.get((WorldServer) par1World, new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[Chisel]"));
	}

	public EntityCloudInABottle(World par1World, EntityLivingBase par2EntityLiving) {
		super(par1World, par2EntityLiving);
		if (par2EntityLiving instanceof EntityPlayer)
			player = (EntityPlayer)par2EntityLiving;
		else {
			if(!par1World.isRemote)
				player = FakePlayerFactory.get((WorldServer) par1World, new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[Chisel]"));
		}
	}

	public EntityCloudInABottle(World par1World, double x, double y, double z) {
		super(par1World, x, y, z);
		if(!par1World.isRemote)
			player = FakePlayerFactory.get((WorldServer) par1World, new GameProfile(UUID.fromString("745dd166-13e9-41db-999d-6af5bacba7fd"), "[Chisel]"));
	}

	@Override
	protected void onImpact(MovingObjectPosition movingobjectposition) {
		if (worldObj.isRemote)
			return;

		int x = movingobjectposition.blockX;
		int y = movingobjectposition.blockY;
		int z = movingobjectposition.blockZ;

		switch (movingobjectposition.sideHit) {
		case 0:
			y--;
			break;
		case 1:
			y++;
			break;
		case 2:
			z--;
			break;
		case 3:
			z++;
			break;
		case 4:
			x--;
			break;
		case 5:
			x++;
			break;
		}

		generate(worldObj, rand, x, y, z, 40);

		worldObj.playAuxSFX(2002, (int) Math.round(this.posX), (int) Math.round(this.posY), (int) Math.round(this.posZ), 2);
		setDead();
	}

	public boolean generate(World world, Random random, int gx, int gy, int gz, int numberOfBlocks) {
		int X[] = new int[9];
		int Y[] = new int[9];
		int Z[] = new int[9];

		for (int dir = 0; dir < 9; dir++) {
			X[dir] = gx;
			Y[dir] = gy;
			Z[dir] = gz;
		}
		int count = 0;
		while (count < numberOfBlocks) {
			for (int dir = 0; dir < 9; dir++) {
				if (count >= numberOfBlocks)
					break;

				int dx = dir % 3 - 1;
				int dz = dir / 3 - 1;

				if (dx == 0 && dz == 0)
					continue;

				X[dir] += random.nextInt(3) - 1 + dx;
				Z[dir] += random.nextInt(3) - 1 + dz;
				Y[dir] += random.nextInt(2) * (random.nextInt(3) - 1);

				int x = X[dir];
				int y = Y[dir];
				int z = Z[dir];

				for (int j2 = x; j2 < x + random.nextInt(4) + 1; j2++) {
					for (int k2 = y; k2 < y + random.nextInt(1) + 2; k2++) {
						for (int l2 = z; l2 < z + random.nextInt(4) + 1; l2++) {
							if (world.getBlock(j2, k2, l2).isAir(world, j2, k2, l2) && Math.abs(j2 - x) + Math.abs(k2 - y) + Math.abs(l2 - z) < 4 * 1 + random.nextInt(2)) {
								BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(j2, k2, l2, world, world.getBlock(j2, k2, l2), world.getBlockMetadata(j2, k2, l2), player);
								MinecraftForge.EVENT_BUS.post(event);
								if (!event.isCanceled()) world.setBlock(j2, k2, l2, ChiselBlocks.cloud);
								count++;
							}
						}
					}
				}
			}
		}

		// System.out.println("Created " + count + " cloud blocks.");

		return true;
	}

}
