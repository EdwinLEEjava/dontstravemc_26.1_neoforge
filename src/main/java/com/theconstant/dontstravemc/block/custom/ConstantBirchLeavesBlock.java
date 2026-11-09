package com.theconstant.dontstravemc.block.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 永恒桦木叶：与原版桦木叶机制相近，额外 {@link #HUE} 区分秋色，供世界生成一树单色。
 */
public class ConstantBirchLeavesBlock extends LeavesBlock {

	public static final EnumProperty<BirchAutumnHue> HUE = EnumProperty.create("hue", BirchAutumnHue.class);

	public static final MapCodec<ConstantBirchLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter(b -> b.leafParticleChance),
					propertiesCodec()
			).apply(instance, ConstantBirchLeavesBlock::new)
	);

	public ConstantBirchLeavesBlock(float leafParticleChance, Properties properties) {
		super(leafParticleChance, properties);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HUE);
	}

	@Override
	protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
		BlockState state = level.getBlockState(pos);
		int argb = state.getValue(HUE).tintArgb();
		ColorParticleOption particle = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, argb);
		ParticleUtils.spawnParticleBelow(level, pos, random, particle);
	}

	@Override
	public MapCodec<? extends ConstantBirchLeavesBlock> codec() {
		return CODEC;
	}

	public enum BirchAutumnHue implements StringRepresentable {
		RED("red", 129, 38, 35),
		ORANGE("orange", 192, 95, 5),
		YELLOW("yellow", 170, 138, 60);

		private final String id;
		private final int tintArgb;

		BirchAutumnHue(String id, int r, int g, int b) {
			this.id = id;
			this.tintArgb = ARGB.color(r, g, b);
		}

		public int tintArgb() {
			return this.tintArgb;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}

		@Override
		public String toString() {
			return this.id;
		}
	}
}
