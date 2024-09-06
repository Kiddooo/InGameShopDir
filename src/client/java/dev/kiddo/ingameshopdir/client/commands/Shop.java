package dev.kiddo.ingameshopdir.client.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.kiddo.ingameshopdir.client.utils.ShopItem;
import dev.kiddo.ingameshopdir.client.utils.ShopLoader;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class Shop {

    private static final List<String> POTTERY_SHERD_NAMES = Arrays.asList(
            "angler_pottery_sherd",
            "archer_pottery_sherd",
            "arms_up_pottery_sherd",
            "blade_pottery_sherd",
            "brewer_pottery_sherd",
            "burn_pottery_sherd",
            "danger_pottery_sherd",
            "explorer_pottery_sherd",
            "friend_pottery_sherd",
            "heart_pottery_sherd",
            "heartbreak_pottery_sherd",
            "howl_pottery_sherd",
            "miner_pottery_sherd",
            "mourner_pottery_sherd",
            "plenty_pottery_sherd",
            "prize_pottery_sherd",
            "sheaf_pottery_sherd",
            "shelter_pottery_sherd",
            "skull_pottery_sherd",
            "snort_pottery_sherd",
            "flow_pottery_sherd",
            "guster_pottery_sherd",
            "scrape_pottery_sherd"
    );

    private static final List<String> ARMOR_TRIM_TEMPLATE_NAMES = Arrays.asList(
            "ward_armor_trim_smithing_template",
            "spire_armor_trim_smithing_template",
            "coast_armor_trim_smithing_template",
            "eye_armor_trim_smithing_template",
            "dune_armor_trim_smithing_template",
            "wild_armor_trim_smithing_template",
            "rib_armor_trim_smithing_template",
            "tide_armor_trim_smithing_template",
            "sentry_armor_trim_smithing_template",
            "vex_armor_trim_smithing_template",
            "snout_armor_trim_smithing_template",
            "wayfinder_armor_trim_smithing_template",
            "sharper_armor_trim_smithing_template",
            "silence_armor_trim_smithing_template",
            "raiser_armor_trim_smithing_template",
            "host_armor_trim_smithing_template",
            "flow_armor_trim_smithing_template",
            "bolt_armor_trim_smithing_template"
    );

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(literal("shop")
                .then(argument("item", ItemStackArgumentType.itemStack(commandRegistryAccess))
                        .executes(context -> {
                            ItemStackArgument itemStack = ItemStackArgumentType.getItemStackArgument(context, "item");
                            String itemStackName = itemStack.getItem().toString().replace("minecraft:", "");
                            String itemStackMCName = itemStack.getItem().getName().getString();
                            List<ShopItem> shops = ShopLoader.findAllShopsWithItem(itemStackName.toLowerCase());

                            if (shops == null || shops.isEmpty()) {
                                sendNotFoundMessage(context.getSource(), itemStackMCName);
                                return 0;
                            }

                            sendFoundMessage(context.getSource(), shops.size(), itemStackMCName, itemStackName);

                            for (int i = 0; i < shops.size(); i++) {
                                sendShopInfo(context.getSource(), shops.get(i), i + 1);
                            }
                            return 1;
                        })));
    }

    private static void sendNotFoundMessage(FabricClientCommandSource context, String itemStackMCName) {
        MutableText message = Text.literal("Couldn't find any shops selling ").styled(style ->
                style.withColor(TextColor.fromRgb(0xffca800))).append(Text.literal(Formatting.YELLOW + itemStackMCName));
        context.sendFeedback(message);
    }

    private static void sendFoundMessage(FabricClientCommandSource context, int count, String itemStackMCName, String itemStackName) {
        MutableText baseText = Text.literal("Found ").styled(style -> style.withColor(TextColor.fromRgb(0xffca800)));

        String countString = count == 1 ? "shop" : "shops";

        baseText = baseText.append(Text.literal(count + " ").styled(style -> style.withColor(Formatting.YELLOW)))
                .append(countString).styled(style -> style.withColor(TextColor.fromRgb(0xffca800)));


        MutableText shopsText = Text.literal(" selling ").styled(style -> style.withColor(TextColor.fromRgb(0xffca800)));
        MutableText itemNameText = Text.literal(itemStackMCName).styled(style -> style.withColor(Formatting.YELLOW));

        MutableText FoundMessage = baseText.append(shopsText).append(itemNameText);

        if (isArmorTrim(itemStackName)) {
            FoundMessage = FoundMessage.append(Text.literal("\nNote: The " + countString + " may not sell this exact trim.")
                    .styled(style -> style.withColor(Formatting.RED).withBold(true)));
        }

        if (isPotterySherd(itemStackName)) {
            FoundMessage = FoundMessage.append(Text.literal("\nNote: The " + countString + " may not sell this exact sherd.")
                    .styled(style -> style.withColor(Formatting.RED).withBold(true)));
        }

        context.sendFeedback(FoundMessage);
    }

    private static boolean isArmorTrim(String itemName) {
        return ARMOR_TRIM_TEMPLATE_NAMES.contains(itemName);
    }

    private static boolean isPotterySherd(String itemName) {
        return POTTERY_SHERD_NAMES.contains(itemName);
    }

    private static void sendShopInfo(FabricClientCommandSource context, ShopItem shop, int index) {
        MutableText shopText = Text.literal("Shop " + index + ": ").styled(style -> style.withColor(TextColor.fromRgb(0x4dd676)));
        MutableText shopComma = Text.literal(", ").styled(style -> style.withColor(Formatting.WHITE));
        MutableText shopInfo = Text.literal(shop.getShopName()).styled(style -> style.withColor(TextColor.fromRgb(0x79BAEC)));
        MutableText shopCoords = Text.literal(shop.getCoords()).styled(style -> style.withColor(TextColor.fromRgb(0x6fb6d6)));

        MutableText message = shopText.append(shopInfo).append(shopComma).append(shopCoords);
        context.sendFeedback(message);
    }
}