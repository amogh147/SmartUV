package cs528_mateus_amogh.smartuv.Database;

/**
 Database Schemas
 */
public class UVIndexDbSchema {
    //Store UV Index and UV Exposure Time for each date, time and locality
    public static final class uvUpdateTable {
        public static final String NAME = "uvUpdateTable";

        public static final String date = "Date";
        public static final String zipcode = "Locality";//TODO:not implemented yet
        public static final String time = "Time";
        public static final String uvIndex =  "UVIndex";
        public static final String sunExposureTime =  "SunExposureTime";
    }

    //Store the Indoor/Outdoor transitions (for evaluation)
    public static final class ioStateTable {
        public static final String NAME = "ioStateTable";

        public static final String timestamp = "timestamp";
        public static final String ioState = "ioState";
    }
}
