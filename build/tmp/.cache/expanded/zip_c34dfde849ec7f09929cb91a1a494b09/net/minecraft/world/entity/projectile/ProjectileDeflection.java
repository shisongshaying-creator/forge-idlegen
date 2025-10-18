package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface ProjectileDeflection {
    ProjectileDeflection NONE = (p_335766_, p_335741_, p_334113_) -> {};
    ProjectileDeflection REVERSE = (p_421914_, p_421915_, p_421916_) -> {
        float f = 170.0F + p_421916_.nextFloat() * 20.0F;
        p_421914_.setDeltaMovement(p_421914_.getDeltaMovement().scale(-0.5));
        p_421914_.setYRot(p_421914_.getYRot() + f);
        p_421914_.yRotO += f;
        p_421914_.hasImpulse = true;
    };
    ProjectileDeflection AIM_DEFLECT = (p_421917_, p_421918_, p_421919_) -> {
        if (p_421918_ != null) {
            Vec3 vec3 = p_421918_.getLookAngle().normalize();
            p_421917_.setDeltaMovement(vec3);
            p_421917_.hasImpulse = true;
        }
    };
    ProjectileDeflection MOMENTUM_DEFLECT = (p_421920_, p_421921_, p_421922_) -> {
        if (p_421921_ != null) {
            Vec3 vec3 = p_421921_.getDeltaMovement().normalize();
            p_421920_.setDeltaMovement(vec3);
            p_421920_.hasImpulse = true;
        }
    };

    void deflect(Projectile p_332034_, @Nullable Entity p_330319_, RandomSource p_333938_);
}