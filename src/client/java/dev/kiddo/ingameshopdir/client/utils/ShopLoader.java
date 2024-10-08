package dev.kiddo.ingameshopdir.client.utils;

import java.io.IOException;
import java.util.*;

interface CsvRecord {
    int lineNumber();

    String[] fields();
}

record CsvRecordImpl(int lineNumber, String[] fields) implements CsvRecord {
    CsvRecordImpl(int lineNumber, String[] fields) {
        this.lineNumber = lineNumber;
        this.fields = fields.clone(); // Clone to avoid modifying the original array
    }

    @Override
    public String[] fields() {
        return fields.clone(); // Return a clone to prevent modifications
    }
}

public class ShopLoader {
    private static Map<String, ShopItem> shopItems;

    public ShopLoader() {
        shopItems = new HashMap<>();
    }

    public static List<ShopItem> findAllShopsWithItem(String shopItem) {
        String lowerCaseInput = shopItem.trim().toLowerCase();
        List<ShopItem> matchingShops = new ArrayList<>();

        for (Map.Entry<String, ShopItem> entry : shopItems.entrySet()) {
            String[] inventoryArray = entry.getValue().getInventory().split(",");

            for (String item : inventoryArray) {
                if (item.trim().toLowerCase().equals(lowerCaseInput)) {
                    matchingShops.add(entry.getValue());
                    break;
                }
            }
        }

        return matchingShops.isEmpty() ? null : matchingShops;
    }

    public void loadCSV(String filePath) {
        try (CsvParser parser = new CsvParser(filePath)) {
            Iterable<CsvRecord> records = parser.parse();

            for (CsvRecord record : records) {
                if (record.fields().length < 5) {
                    System.err.println("Skipping invalid line " + record.lineNumber());
                    continue;
                }
                try {
                    ShopItem item = parseShopItem(record);
                    shopItems.put(item.getShopName().toLowerCase(), item);
                    System.out.println("Loaded item: " + item.getShopName());
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parsing line " + record.lineNumber() + ": " + record.fields()[0] + ". " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }

    private ShopItem parseShopItem(CsvRecord record) throws IllegalArgumentException {
        if (record.fields().length < 5) {
            throw new IllegalArgumentException("Invalid number of fields");
        }

        ShopItem item = new ShopItem();
        System.out.println(Arrays.toString(record.fields()));
        item.setShopName(record.fields()[0]);
        item.setInventory(record.fields()[1].replaceAll("\\s+", " ").toLowerCase());
        item.setOwnerIGN(record.fields()[2]);
        item.setCoords(record.fields()[3]);
        item.setSpawn(record.fields()[4]);

        return item;
    }
}
