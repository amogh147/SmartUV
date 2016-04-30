package cs528_mateus_amogh.smartuv.Database;

/**
 * Created by mateus on 4/20/2016.
 */
public class UVIndexDbSchema {
    public static final class uvUpdateTable {
        public static final String NAME = "uvUpdateTable";

        public static final String date = "Date";
        ;;public static final String zipcode = "Locality";
        public static final String time = "Time";
        public static final String uvIndex =  "UVIndex";
        public static final String sunExposureTime =  "SunExposureTime";
    }

    public static final class ioStateTable {
        public static final String NAME = "ioStateTable";

        public static final String timestamp = "timestamp";
        public static final String ioState = "ioState";
    }
}
