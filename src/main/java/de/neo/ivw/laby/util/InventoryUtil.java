package de.neo.ivw.laby.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.neo.ivw.laby.InventoryWebViewClient;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTypes;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class InventoryUtil {

    public static JsonObject serializeInventory(String name, IInventory inv) {
        List<ItemStack> items = new ArrayList<>();
        for(int i = 0; i < inv.getSizeInventory(); i++) {
            items.add(inv.getStackInSlot(i));
        }
        return serializeInventory(name, Minecraft.getInstance().player.getUniqueID().toString(), "PLAYER", items);
    }

    public static JsonObject serializeInventory(String name, String holder, String type, List<ItemStack> itemsList) {
        JsonObject json = new JsonObject();
        json.addProperty("holder", holder);
        json.addProperty("type", type);
        json.addProperty("title", name);
        json.addProperty("size", itemsList.size());

        JsonArray items = new JsonArray();
        for (int i = 0; i < itemsList.size(); i++) {
            items.add(serializeItem(i, itemsList.get(i)));
        }
        json.add("items", items);
        return json;
    }

    public static JsonObject serializeItem(int slot, ItemStack stack) {
        JsonObject itemJson = new JsonObject();

        String type = Registry.ITEM.getKey(stack.getItem()).getPath().toUpperCase(Locale.ROOT);
        if(type.equals("AIR")) {
            return itemJson;
        }

        itemJson.addProperty("type", type);
        itemJson.addProperty("amount", stack.getCount());
        itemJson.addProperty("slot", slot);

        JsonObject meta = serializeItemMeta(stack);
        itemJson.add("meta", meta);
        return itemJson;
    }

    public static JsonObject serializeItemMeta(ItemStack stack) {
        JsonObject meta = new JsonObject();

        if(stack.getTag() == null) {
            return meta;
        }

        meta.addProperty("nbt", stack.getTag().toString());

        meta.addProperty("unbreakable", stack.getTag().getBoolean("Unbreakable"));
        meta.addProperty("damage", stack.getDamage());

        if(Registry.ITEM.getKey(stack.getItem()).getPath().equals("player_head")) {
            CompoundNBT texture = stack.getChildTag("SkullOwner");
            if(texture == null) {
                meta.addProperty("texture", stack.getTag().getString("SkullOwner"));
            }else {
                CompoundNBT properties = texture.getCompound("Properties");
                ListNBT textures = properties.getList("textures", 10);

                meta.addProperty("SkullOwner", textures.getCompound(0).getString("Value"));
            }
        }

        String displayName = ITextComponent.Serializer.toJson(stack.getDisplayName());
        if(stack.hasDisplayName()) {
            displayName = stack.getChildTag("display").getString("Name");
        }
        meta.addProperty("displayName", displayName);

        JsonArray enchantments = new JsonArray();
        for(int i = 0; i < stack.getEnchantmentTagList().size(); i++) {
            JsonObject enchantmentJson = new JsonObject();
            CompoundNBT enchantment = stack.getEnchantmentTagList().getCompound(i);
            enchantmentJson.addProperty("enchant",
                    ResourceLocation.tryCreate(enchantment.getString("id")).getPath().toUpperCase(Locale.ROOT));
            enchantmentJson.addProperty("level", MathHelper.clamp(enchantment.getInt("lvl"), 0, 255));
            enchantments.add(enchantmentJson);
        }
        meta.add("enchantments", enchantments);

        JsonArray loreJson = new JsonArray();
        ListNBT lore = stack.getTag().getList("Lore", 8);
        for(int i = 0; i < lore.size(); i++) {
            loreJson.add(ITextComponent.Serializer.getComponentFromJson(lore.getString(i)).getString());
        }
        meta.add("lores", loreJson);
        meta.addProperty("containsLore", true);

        return meta;
    }

    public static CompletableFuture<JsonObject> paste(JsonObject inv) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                JsonObject reqJson = new JsonObject();
                reqJson.addProperty("api.type", "paste");
                reqJson.add("api.data", inv);
                String invString = reqJson.toString();

                JsonObject config = InventoryWebViewClient.getInstance().getConfig();

                String invPasteUrl = config.get("paste.inv.url").getAsString();
                String url = invPasteUrl + (invPasteUrl.endsWith("/") ? "" : "/") + "api.php";
                HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                con.connect();

                OutputStream os = con.getOutputStream();
                os.write(invString.getBytes(StandardCharsets.UTF_8));
                os.flush();

                InputStream is = con.getInputStream();
                String json = readAllBytesAsString(is);

                future.complete(new Gson().fromJson(json, JsonObject.class));
            }catch (IOException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        }).start();

        return future;
    }

    private static String readAllBytesAsString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString().trim();
    }

}
