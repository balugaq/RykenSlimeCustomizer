package org.lins.mmmjjkx.rykenslimefuncustomizer.bulit_in;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricFurnace;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.machines.ElectricGoldPan;
import org.jspecify.annotations.Nullable;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AbstractEmptyMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.AdvancedCustomMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomArmorPiece;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomCapacitor;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomEnergyGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomFood;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomGeoResource;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomItem;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomMobDrop;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomMultiBlockMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomSolarGenerator;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.CustomWorkbench;
import org.lins.mmmjjkx.rykenslimefuncustomizer.customs.script_machine.ScriptMachine;
import org.lins.mmmjjkx.rykenslimefuncustomizer.utils.Constants;

public enum ObjectType {
    ItemGroup(Constants.GROUPS_FILE),
    RecipeType(Constants.RECIPE_TYPES_FILE),
    GeoResource(Constants.GEO_RESOURCES_FILE),
    MobDrop(Constants.MOB_DROPS_FILE),
    Item(Constants.ITEMS_FILE),
    Armor(Constants.ARMORS_FILE),
    Capacitor(Constants.CAPACITORS_FILE),
    Food(Constants.FOODS_FILE),
    Menu(Constants.MENUS_FILE),
    Machine(Constants.MACHINES_FILE),
    Generator(Constants.GENERATORS_FILE),
    SolarGenerator(Constants.SOLAR_GENERATORS_FILE),
    MaterialGenerator(Constants.MATERIAL_GENERATORS_FILE),
    RecipeMachine(Constants.RECIPE_MACHINES_FILE),
    SimpleMachine(Constants.SIMPLE_MACHINES_FILE),
    MultiBlock(Constants.MULTI_BLOCK_MACHINES_FILE),
    Super(Constants.SUPERS_FILE),
    TemplateMachine(Constants.TEMPLATE_MACHINES_FILE),
    LinkedRecipeMachine(Constants.LINKED_RECIPE_MACHINES_FILE),
    Workbench(Constants.WORKBENCHES_FILE),
    SuperMultiBlockMachine(Constants.SUPER_MULTI_BLOCK_MACHINES_FILE),
    Generation(Constants.GENERATIONS_FILE),
    Research(Constants.RESEARCHES_FILE);

    private final String file;

    ObjectType(String file) {
        this.file = file;
    }

    @Nullable
    public static ObjectType fromString(String s) {
        for (var e : values()) {
            if (e.file.startsWith(s)) return e;
        }
        return null;
    }

    public static ObjectType fromSlimefunItem(SlimefunItem sf) {
        ObjectType o = null;
        if (sf instanceof ScriptMachine) {
            o = ObjectType.Machine;
        } else if (sf instanceof CustomGenerator) {
            o = ObjectType.Generator;
        } else if (sf instanceof CustomArmorPiece) {
            o = ObjectType.Armor;
        } else if (sf instanceof CustomCapacitor) {
            o = ObjectType.Capacitor;
        } else if (sf instanceof CustomFood) {
            o = ObjectType.Food;
        } else if (sf instanceof CustomGeoResource) {
            o = ObjectType.GeoResource;
        } else if (sf instanceof CustomMobDrop) {
            o = ObjectType.MobDrop;
        } else if (sf instanceof CustomItem) {
            o = ObjectType.Item;
        } else if (sf instanceof CustomMultiBlockMachine) {
            o = ObjectType.MultiBlock;
        } else if (sf instanceof CustomSolarGenerator) {
            o = ObjectType.SolarGenerator;
        } else if (sf instanceof CustomWorkbench) {
            o = ObjectType.Workbench;
        }

        if (o != null) return o;

        if (sf instanceof AdvancedCustomMachine acm) {
            return switch (acm.getType()) {
                case RECIPE -> ObjectType.RecipeMachine;
                case LINKED_RECIPE -> ObjectType.LinkedRecipeMachine;
                case TEMPLATE_RECIPE -> ObjectType.TemplateMachine;
                case MATERIAL_GENERATOR -> ObjectType.MaterialGenerator;
                case WORKBENCH -> ObjectType.Workbench;
            };
        }

        if (sf.getClass().getSimpleName().endsWith("$$Bytebuddy")) {
            return ObjectType.Super;
        }

        return ObjectType.SimpleMachine;
    }
}
