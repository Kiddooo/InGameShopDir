package dev.kiddo.ingameshopdir.client.utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShopItem {
    private String shopName;
    private String inventory;
    private String coords;
    private String ownerIGN;
    private double spawn;

}
