package chronosacaria.mcdw.enchants.summons.registry;

import chronosacaria.mcdw.enchants.summons.render.SummonedBeeRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class SummonedEntityRenderRegistry {
    public static void register(){
        registerSummonedEntitiesRenderer();
    }

    private static void registerSummonedEntitiesRenderer(){
        registerSummonedBeeEntityRenderer();
    }

    private static void registerSummonedBeeEntityRenderer(){
        EntityRendererRegistry.INSTANCE.register(SummonedEntityRegistry.SUMMONED_BEE_ENTITY,
                SummonedBeeRenderer::new);
    }

}