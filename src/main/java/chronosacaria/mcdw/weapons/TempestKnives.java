package chronosacaria.mcdw.weapons;

import chronosacaria.mcdw.bases.McdwDagger;
import chronosacaria.mcdw.configs.McdwStatsConfig;
import net.minecraft.item.ToolMaterials;

public class TempestKnives {
    public static McdwDagger DAGGER_TEMPEST_KNIFE;
    public static McdwDagger DAGGER_CHILL_GALE_KNIFE;
    public static McdwDagger DAGGER_RESOLUTE_TEMPEST_KNIFE;

    public static void init() {
        DAGGER_TEMPEST_KNIFE = new McdwDagger(ToolMaterials.IRON,
                McdwStatsConfig.config.getTempestKnifeDamage(),
                McdwStatsConfig.config.getTempestKnifeSpeed(),
                "dagger_tempest_knife");
        DAGGER_RESOLUTE_TEMPEST_KNIFE = new McdwDagger(ToolMaterials.IRON,
                McdwStatsConfig.config.getResoluteTempestKnifeDamage(),
                McdwStatsConfig.config.getResoluteTempestKnifeSpeed(),
                "dagger_resolute_tempest_knife");
        DAGGER_CHILL_GALE_KNIFE = new McdwDagger(ToolMaterials.IRON,
                McdwStatsConfig.config.getChillGaleKnifeDamage(),
                McdwStatsConfig.config.getChillGaleKnifeSpeed(),
                "dagger_chill_gale_knife");

    }
}


