package com.theconstant.dontstravemc.model;

import com.theconstant.dontstravemc.entity.monster.MonsterSpiderEntity;
import com.theconstant.dontstravemc.render.state.MonsterSpiderRenderState;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class MonsterSpiderModel extends GeoModel<MonsterSpiderEntity> {
	private Identifier spiderTex(String name) {
		return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/spider/spider_" + name + ".png");
	}

	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return Identifier.fromNamespaceAndPath("dontstravemc", "entity/monster_spider");
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		if (renderState instanceof MonsterSpiderRenderState s) {
			if (s.isDead) return spiderTex("dead");
			if (s.isHurt) return spiderTex("hurt");
			if (s.isAggressive) return spiderTex("aggressive");
			if (s.isAsleep) return spiderTex("sleeping");
			if (s.isEating) return spiderTex("eating");
		}
		return Identifier.fromNamespaceAndPath("dontstravemc", "textures/entity/spider/spider.png");
	}

	@Override
	public Identifier getAnimationResource(MonsterSpiderEntity animatable) {
		return Identifier.fromNamespaceAndPath("dontstravemc", "entity/monster_spider");
	}
}