package com.theconstant.dontstravemc.world;

import com.theconstant.dontstravemc.world.gen.caves.CaveLayout;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * 永恒领域 ↔ 洞穴：
 * <ul>
 *     <li>永恒领域里低于 {@link #ENTER_CAVES_Y} → 进洞穴，落在同坐标附近最近的岛面上。</li>
 *     <li>洞穴里高于顶部基岩 → 回永恒领域地表。</li>
 *     <li>洞穴里掉进深渊（低于 {@link #ABYSS_Y}）→ 回永恒领域地表并扣血。</li>
 * </ul>
 */
public final class CaveTransitions {

    public static final int ENTER_CAVES_Y = -56;
    public static final int ABYSS_Y = -16;
    public static final float ABYSS_DAMAGE = 6.0F;

    private static final int SEARCH_RADIUS = 512;
    private static final int SEARCH_STEP = 4;

    private CaveTransitions() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(CaveTransitions::onPlayerTick);
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isSpectator() || player.isPassenger()) {
            return;
        }
        ServerLevel level = player.level();
        double y = player.getY();
        TeleportTransition transition = null;
        if (level.dimension() == ConstantDimensions.THE_CONSTANT && y < ENTER_CAVES_Y) {
            transition = toCaves(level, player);
        } else if (level.dimension() == ConstantDimensions.THE_CAVES) {
            if (y > CaveLayout.TOP_Y + 1) {
                transition = toSurface(level, player, false);
            } else if (y < ABYSS_Y) {
                transition = toSurface(level, player, true);
            }
        }
        if (transition != null) {
            player.teleport(transition);
        }
    }

    private static @Nullable TeleportTransition toCaves(ServerLevel from, Entity entity) {
        ServerLevel target = from.getServer().getLevel(ConstantDimensions.THE_CAVES);
        if (target == null) {
            return null;
        }
        CaveLayout layout = CaveLayout.get();
        int[] spot = findLand(layout, entity.getBlockX(), entity.getBlockZ());
        int x = spot[0];
        int z = spot[1];
        target.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int y = layout.sample(x, z).surface() + 1;
        return transition(target, entity, new Vec3(x + 0.5, y, z + 0.5), TeleportTransition.DO_NOTHING);
    }

    private static @Nullable TeleportTransition toSurface(ServerLevel from, Entity entity, boolean hurt) {
        ServerLevel target = from.getServer().getLevel(ConstantDimensions.THE_CONSTANT);
        if (target == null) {
            return null;
        }
        int x = entity.getBlockX();
        int z = entity.getBlockZ();
        target.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int y = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        TeleportTransition.PostTeleportTransition after = hurt
                ? moved -> {
                    moved.resetFallDistance();
                    if (moved.level() instanceof ServerLevel level) {
                        moved.hurtServer(level, level.damageSources().fall(), ABYSS_DAMAGE);
                    }
                }
                : Entity::resetFallDistance;
        return transition(target, entity, new Vec3(x + 0.5, y, z + 0.5), after);
    }

    private static TeleportTransition transition(ServerLevel target, Entity entity, Vec3 pos, TeleportTransition.PostTeleportTransition after) {
        return new TeleportTransition(
                target,
                pos,
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                Set.<Relative>of(),
                after.then(TeleportTransition.PLACE_PORTAL_TICKET)
        );
    }

    /**
     * 从 (x, z) 起按方环向外找第一块岛心地面（避开岸边和石柱）。找不到就回原点方向。
     */
    private static int[] findLand(CaveLayout layout, int x, int z) {
        for (int r = 0; r <= SEARCH_RADIUS; r += SEARCH_STEP) {
            for (int dx = -r; dx <= r; dx += SEARCH_STEP) {
                for (int dz = -r; dz <= r; dz += SEARCH_STEP) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    int cx = x + dx;
                    int cz = z + dz;
                    CaveLayout.Column column = layout.sample(cx, cz);
                    if (column.land() && column.edge() >= 0.5
                            && !layout.insidePillar(cx, column.surface() + 1, cz)
                            && !layout.insidePillar(cx, column.surface() + 2, cz)) {
                        return new int[]{cx, cz};
                    }
                }
            }
        }
        return new int[]{0, 0};
    }
}
