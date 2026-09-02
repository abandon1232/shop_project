package com.example.db;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogMigrationTest {

    private static final List<String> CATEGORY_SLUGS = List.of(
            "computers-tablets",
            "phones-wearables",
            "tv-audio",
            "gaming",
            "kitchen-appliances",
            "smart-home"
    );

    private static final List<String> CATEGORY_NAMES = List.of(
            "Computers & Tablets",
            "Phones & Wearables",
            "TV & Audio",
            "Gaming",
            "Kitchen Appliances",
            "Smart Home"
    );

    private static final List<String> PRODUCT_SLUGS = List.of(
            "nordbook-air-14",
            "fjordview-27-monitor",
            "birch-wireless-keyboard",
            "aurora-phone",
            "pulse-smartwatch",
            "pocket-usbc-charger",
            "horizon-55-tv",
            "quietwave-headphones",
            "roombeat-speaker",
            "arcade-controller",
            "velocity-headset",
            "glide-gaming-mouse",
            "compact-air-fryer",
            "barista-coffee-maker",
            "nordic-kettle",
            "glow-smart-bulbs",
            "homeguard-camera",
            "climate-sensor"
    );

    private static final List<String> PRODUCT_NAMES = List.of(
            "NordBook Air 14 Laptop",
            "FjordView 27 QHD Monitor",
            "Birch Wireless Keyboard",
            "Aurora 5G Smartphone",
            "Pulse Active Smartwatch",
            "Pocket 65W USB-C Charger",
            "Horizon 55 4K Smart TV",
            "QuietWave Wireless Headphones",
            "RoomBeat Portable Speaker",
            "Arcade Pro Wireless Controller",
            "Velocity Surround Gaming Headset",
            "Glide Precision Gaming Mouse",
            "Compact Air Fryer 5L",
            "Barista Filter Coffee Maker",
            "Nordic Temperature Kettle 1.7L",
            "Glow Smart Bulb Starter Set",
            "HomeGuard Indoor Camera",
            "Climate Mini Sensor"
    );

    @Test
    void migrationReplacesOldCatalogueWithSixCategoriesAndEighteenProducts() throws IOException {
        String sql = new ClassPathResource("db/migration/V6__replace_demo_catalog.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        int goodsDelete = sql.indexOf("DELETE FROM goods");
        int typeDelete = sql.indexOf("DELETE FROM type");
        assertTrue(goodsDelete >= 0, "The migration must remove old products");
        assertTrue(typeDelete > goodsDelete, "Products must be removed before their categories");
        assertEquals(6, CATEGORY_NAMES.size());
        assertEquals(18, PRODUCT_NAMES.size());
        assertAll(CATEGORY_NAMES.stream().map(name ->
                () -> assertTrue(sql.contains(name), "Missing category: " + name)));
        assertAll(PRODUCT_NAMES.stream().map(name ->
                () -> assertTrue(sql.contains(name), "Missing product: " + name)));
    }

    @Test
    void everySeededCatalogueEntryHasItsOwnPublicImage() {
        Path catalogueRoot = Path.of("..", "vue", "public", "images", "catalog");

        assertAll(CATEGORY_SLUGS.stream().map(slug ->
                () -> assertTrue(Files.isRegularFile(
                        catalogueRoot.resolve("categories").resolve(slug + ".webp")),
                        "Missing category image: " + slug)));
        assertAll(PRODUCT_SLUGS.stream().map(slug ->
                () -> assertTrue(Files.isRegularFile(
                        catalogueRoot.resolve("products").resolve(slug + ".webp")),
                        "Missing product image: " + slug)));
    }

    @Test
    void springBootFourLoadsFlywayAndCanBaselineTheExistingSchema() throws IOException {
        String pom = Files.readString(Path.of("pom.xml"));
        String configuration = new ClassPathResource("application.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(pom.contains("<artifactId>spring-boot-starter-flyway</artifactId>"));
        assertTrue(configuration.contains("baseline-on-migrate: true"));
    }

    @Test
    void catalogueSellerMigrationRenamesOnlyBlankOrNumericCatalogueOwners() throws IOException {
        String sql = new ClassPathResource("db/migration/V8__normalize_catalog_seller_name.sql")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(sql.matches("(?s).*UPDATE\\s+business\\s+SET\\s+name\\s*=\\s*'NorrByte Electronics'"
                        + "\\s+WHERE\\s+EXISTS\\s*\\(\\s*SELECT\\s+1\\s+FROM\\s+goods\\s+WHERE"
                        + "\\s+goods\\.business_id\\s*=\\s*business\\.id\\s+AND\\s+goods\\.img"
                        + "\\s+LIKE\\s*'/images/catalog/products/%'\\s*\\)\\s+AND\\s*\\(\\s*TRIM\\(name\\)"
                        + "\\s*=\\s*''\\s+OR\\s+TRIM\\(name\\)\\s+REGEXP\\s*'\\^\\[0-9\\]\\+\\$'\\s*\\).*"),
                "The seller migration must rename only blank or numeric catalogue owners");
    }
}
