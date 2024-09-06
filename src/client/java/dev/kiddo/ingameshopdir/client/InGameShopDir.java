package dev.kiddo.ingameshopdir.client;

import dev.kiddo.ingameshopdir.client.commands.Shop;
import dev.kiddo.ingameshopdir.client.utils.ShopLoader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class InGameShopDir implements ClientModInitializer {

    public static final String CONFIG_DIR = Path.of(FabricLoader.getInstance().getConfigDir().toString(), "/InGameShopDir").toString();
    public static final String SHOPS_FILE_NAME = "shops.csv";
    public static final String VERSION_FILE_NAME = "shops_version.txt";
    public static final String LATEST_CSV_URL = "https://kiddo.dev/files/shops.csv"; // Replace with actual URL
    public static final String VERSION_CHECK_URL = "https://kiddo.dev/files/shops_version.txt"; // Replace with actual URL
    private static final Logger LOGGER = LoggerFactory.getLogger(InGameShopDir.class);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing InGameShopDir client");

        ensureConfigDirectoryExists();

        try {
            updateAndLoadCSV();
            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
                LOGGER.info("Registering shop command");
                Shop.register(dispatcher, registryAccess);
            });
        } catch (Exception e) {
            LOGGER.error("Failed to initialize InGameShopDir", e);
        }
    }

    private void ensureConfigDirectoryExists() {
        try {
            Files.createDirectories(Path.of(InGameShopDir.CONFIG_DIR));
            LOGGER.info("Config directory '{}' exists or was created successfully.", InGameShopDir.CONFIG_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory '{}'. This may cause issues.", InGameShopDir.CONFIG_DIR, e);
        }
    }

    private boolean safeLoadCSV() {
        try {
            ShopLoader loader = new ShopLoader();
            loader.loadCSV(CONFIG_DIR + "/shops.csv");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to load shops.csv", e);
            return false;
        }
    }

    private void updateAndLoadCSV() throws Exception {
        Path shopsFilePath = Path.of(CONFIG_DIR, SHOPS_FILE_NAME);
        Path versionFilePath = Path.of(CONFIG_DIR, VERSION_FILE_NAME);

        String localVersion = readLocalVersion(versionFilePath);
        String remoteVersion = getRemoteVersion();

        if (localVersion == null || !localVersion.equals(remoteVersion)) {
            LOGGER.info("New version of shops.csv available. Downloading...");
            downloadLatestCSV(shopsFilePath, versionFilePath);
        } else {
            LOGGER.info("Shops.csv is up-to-date.");
        }

        // Load the CSV file after updating
        boolean csvLoaded = safeLoadCSV();
        if (!csvLoaded) {
            throw new RuntimeException("Failed to load shops.csv");
        }
        LOGGER.info("Successfully loaded shops.csv");
    }

    private String readLocalVersion(Path versionFilePath) throws IOException {
        if (Files.exists(versionFilePath)) {
            return Files.readString(versionFilePath).trim();
        }
        return null; // Return null if file doesn't exist
    }

    private String getRemoteVersion() throws IOException, URISyntaxException {
        URI versionUri = new URI(VERSION_CHECK_URL);
        URL versionUrl = versionUri.toURL();

        try (InputStream inputStream = versionUrl.openStream()) {
            return new BufferedReader(new InputStreamReader(inputStream)).readLine().trim();
        }
    }

    private void downloadLatestCSV(Path shopsFilePath, Path versionFilePath) throws Exception {
        LOGGER.info("Starting update of shops.csv");

        try {
            // Delete the old CSV file
            if (Files.exists(shopsFilePath)) {
                Files.delete(shopsFilePath);
                LOGGER.debug("Deleted old shops.csv");
            }

            // Create a cache-busting URL
            String csvUrl = LATEST_CSV_URL + "?t=" + System.currentTimeMillis();

            // Download the new CSV file
            URI csvUri = new URI(csvUrl);
            URL url = csvUri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Cache-Control", "no-cache");

            File tempFile = File.createTempFile("shops_temp", ".csv");
            FileUtils.copyInputStreamToFile(connection.getInputStream(), tempFile);
            LOGGER.debug("Downloaded new shops.csv");

            // Move the downloaded file to the final destination
            Files.move(tempFile.toPath(), shopsFilePath);
            LOGGER.info("Moved new shops.csv to final location");

            // Update the version file
            URI versionUri = new URI(VERSION_CHECK_URL);
            URL versionUrl = versionUri.toURL();
            String remoteVersion = new BufferedReader(new InputStreamReader(versionUrl.openStream())).readLine().trim();
            Files.writeString(versionFilePath, remoteVersion);
            LOGGER.info("Updated shops_version.txt");

            LOGGER.info("Successfully updated shops.csv");
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Failed to update shops.csv", e);
            throw e; // Re-throw the exception so it can be handled by the caller
        }
    }
}
