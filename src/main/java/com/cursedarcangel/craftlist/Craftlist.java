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
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

public class Craftlist implements ModInitializer {
    @Override
    public void onInitialize() {

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientCommandManager.DISPATCHER.register(
                literal("craftlist")
                        .then(argument("item", string())
                                .then(argument("amount", integer())
                                        .executes(ctx -> {
                                            String item = getString(ctx, "item");
                                            int count = getInteger(ctx, "amount");
                                            parse(item, count);
                                            return 1;
                                        }))));
    }
    public static void parse(String item, int amount){
        Gson gson = new Gson();
        Reader reader = null;

        InputStream input = Craftlist.class.getResourceAsStream("/recipes.json");
        reader = new InputStreamReader(input);

        Map<String, Object > recipes = gson.fromJson(reader, Map.class);

        HashMap<String, Object> materialsPerItem = new HashMap<>();
        for (Map.Entry<String, Object > index : recipes.entrySet()) {
            materialsPerItem.put(String.valueOf(index.getKey()), index.getValue());
        }

        Map<String, Double> mats = (Map<String, Double>) materialsPerItem.get(item);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mats == null) {
            String msg = "recipe not found";
            mc.inGameHud.addChatMessage(MessageType.SYSTEM, Text.of(msg), mc.player.getUuid());
        } else {
            for (Map.Entry<String, Double> index : mats.entrySet()) {
                int numOfMats = (int) Math.round(index.getValue() * amount);

                int stacks = numOfMats / 64;
                int remainder = (numOfMats % 64);
                String msg = index.getKey() + " : " + "(" + String.valueOf(numOfMats) + ") " + String.valueOf(stacks) + " stack(s) and " + String.valueOf(remainder) + " items";
                mc.inGameHud.addChatMessage(MessageType.SYSTEM, Text.of(msg), mc.player.getUuid());
            }
        }
    }
}
