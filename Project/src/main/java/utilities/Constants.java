package utilities;

import java.util.ArrayList;
import java.util.Arrays;

public class Constants {

    // Skills
    public static final String SKILL_A = "Skill_A";
    public static final String SKILL_B = "Skill_B";
    public static final String SKILL_PICKUP = "Skill_PickUp";
    public static final String SKILL_DROP = "Skill_Drop";
    public static final String SKILL_MOVE = "Skill_Move";

    // Stations
    public static final String STATION_3 = "Station_3";
    public static final String STATION_4 = "Station_4";

    // Transport
    public static final String TRANSPORT = "Conveyor";

    // Products
    public static final String PROD_A = "Product_A";
    public static final String PROD_B = "Product_B";
    public static final String PROD_C = "Product_C";

    // Locations
    public static final String LOCATION_SOURCE = "Source";
    public static final String LOCATION_C1 = "C1";
    public static final String LOCATION_C2 = "C2";
    public static final String LOCATION_C3 = "C3";
    public static final String LOCATION_C4 = "C4";
    public static final String LOCATION_STORAGE = "Storage";

    // Station Skills
    public static final ArrayList<String> STATION_3_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_A
    ));
    public static final ArrayList<String> STATION_4_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_B
    ));

    // Transport Skills
    public static final ArrayList<String> TRANSPORT_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_MOVE, SKILL_PICKUP, SKILL_DROP
    ));

    // Products Skills
    public static final ArrayList<String> PROD_A_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_A
    ));
    public static final ArrayList<String> PROD_B_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_B
    ));
    public static final ArrayList<String> PROD_C_SKILLS = new ArrayList<>(Arrays.asList(
            SKILL_B, SKILL_A
    ));

    // Product Types
    public static final ArrayList<String> PRODUCT_TYPES = new ArrayList<>(Arrays.asList(
        PROD_A, PROD_B, PROD_C
    ));

    // Retrieve List of Station/Transport Skills
    public static ArrayList<String> getStationTransportSkills(String stationName){
        return switch (stationName){
            case STATION_3 -> STATION_3_SKILLS;
            case STATION_4 -> STATION_4_SKILLS;
            case TRANSPORT -> TRANSPORT_SKILLS;
            default -> null;
        };
    }

    // Retrieve Location of Station
    public static String getStationLocation(String stationName){
        return switch (stationName){
            case STATION_3 -> LOCATION_C2;
            case STATION_4 -> LOCATION_C3;
            default -> null;
        };
    }

    // Retrieve Station at Location
    public static String getLocationStation(String locationName){
        return switch (locationName){
            case LOCATION_C2 -> STATION_3;
            case LOCATION_C3 -> STATION_4;
            default -> null;
        };
    }

    // Retrieve List of Product Skills
    public static ArrayList<String> getProdSkills(String prodName){
        return switch (prodName) {
            case PROD_A -> PROD_A_SKILLS;
            case PROD_B -> PROD_B_SKILLS;
            case PROD_C -> PROD_C_SKILLS;
            default -> null;
        };
    }
}
