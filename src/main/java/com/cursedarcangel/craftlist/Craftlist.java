package com.cursedarcangel.craftlist;

import com.google.gson.Gson;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;
import static net.minecraft.command.argument.ItemStackArgumentType.getItemStackArgument;
import static net.minecraft.command.argument.ItemStackArgumentType.itemStack;

public class Craftlist implements ModInitializer {
    @Override
    public void onInitialize() {
        Gson gson = new Gson();
        Reader reader = null;

        InputStream input = Craftlist.class.getResourceAsStream("/recipes.json");
        reader = new InputStreamReader(input);

        Map<String, Object > recipes = gson.fromJson(reader, Map.class);

        HashMap<String, Object> materialsPerItem = new HashMap<>();
        for (Map.Entry<String, Object > index : recipes.entrySet()) {
            materialsPerItem.put(String.valueOf(index.getKey()), index.getValue());
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientCommandManager.DISPATCHER.register(
                literal("craftlist")
                        .then(argument("item", itemStack())
                                .then(argument("amount", integer())
                                        .executes(ctx -> {
                                            String item = String.valueOf(getItemStackArgument(ctx, "item").createStack(1, false).toString().split(" ")[1]);
                                            int amount = getInteger(ctx, "amount");
                                            Map<String, Double> mats = (Map<String, Double>) materialsPerItem.get(item);
                                            if (mats == null) {
                                                String msg = "recipe not found";
                                                mc.inGameHud.addChatMessage(MessageType.SYSTEM, Text.of(msg), mc.player.getUuid());
                                            } else {
                                                for (Map.Entry<String, Double> index : mats.entrySet()) {
                                                    int numOfMats = (int) Math.round(index.getValue() * amount);

                                                    int stacks = numOfMats / 64;
                                                    int remainder = (numOfMats % 64);
                                                    String total = "(§2" + String.valueOf(numOfMats) + "§r)";
                                                    String msg = index.getKey() + " : " + total + " " + String.valueOf(stacks) + " stack(s) and " + String.valueOf(remainder) + " items";
                                                    mc.inGameHud.addChatMessage(MessageType.SYSTEM, Text.of(msg), mc.player.getUuid());
                                                }
                                            }
                                            return 1;
                                        }))));
    }

}
