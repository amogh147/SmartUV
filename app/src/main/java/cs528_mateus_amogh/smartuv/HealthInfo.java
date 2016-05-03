package cs528_mateus_amogh.smartuv;

import java.util.HashMap;
import java.util.Map;

/**
 * Store all information got from Health Organization Resources
 * It includes interface resources id (color, images, messages), formulas and conventions
 */
public class HealthInfo {

    //This class store all interface resources indexed by Id
    public static class Resource{
        public Resource(int nameId, int colorId, int msgId, int msgImgId[]){
            this.nameId = nameId;
            this.colorId = colorId;
            this.msgId = msgId;
            this.msgImgViewId = msgImgId;
        }
        public int nameId;
        public int colorId;
        public int msgId;
        public int msgImgViewId[];
    }

    //Static map (got from https://www.epa.gov/sites/production/files/documents/uviguide.pdf)
    private static final Map<Integer, Resource> resMap= new HashMap<Integer, Resource>();
    static {
        resMap.put(Risk.NONE, new Resource(R.string.noRisk,R.color.noRisk,R.array.noRiskMsgs,new int[]{}));
        resMap.put(Risk.LOW, new Resource(R.string.lowRisk,R.color.lowRisk,R.array.lowRiskMsgs,new int[]{}));
        resMap.put(Risk.MODERATE, new Resource(R.string.moderateRisk,R.color.moderateRisk,R.array.moderateRiskMsgs,
                new int[]{R.id.shirtView,R.id.hatView,R.id.sunscreenView}));
        resMap.put(Risk.HIGH, new Resource(R.string.highRisk,R.color.highRisk,R.array.highRiskMsgs,
                new int[]{R.id.shirtView,R.id.hatView,R.id.sunscreenView, R.id.sunglassView, R.id.houseView}));
        resMap.put(Risk.VERY_HIGH, new Resource(R.string.veryHighRisk,R.color.veryHighRisk,R.array.veryHighRiskMsgs,
                new int[]{R.id.shirtView,R.id.hatView,R.id.sunscreenView, R.id.sunglassView, R.id.houseView,R.id.alertView}));
        resMap.put(Risk.EXTREME, new Resource(R.string.extremeRisk,R.color.extremeRisk,R.array.extremeRiskMsgs,
                new int[]{R.id.shirtView,R.id.hatView,R.id.sunscreenView, R.id.sunglassView, R.id.houseView,R.id.alertView}));
    }

    //Get the resources associated with the specified risk
    public static Resource getRiskResource(int risk){
        return resMap.get(risk);
    }

    //Risk Levels along with the thresholds
    //TODO: separate Risk Ids from Risk thresholds
    public final static class Risk{
        public static final int NONE = 0;
        public static final int LOW = 1;
        public static final int MODERATE = 3;
        public static final int HIGH = 6;
        public static final int VERY_HIGH = 8;
        public static final int EXTREME = 11;
    }

    //Calculate the expected sunburn time for specific skinType and uvIndex
    public static double calcSunBurnTime(int skinType, int uvIndex){
        //source: http://www.himaya.com/solar/avoidsunburn.html
        if(uvIndex == 0)
            return 0;
        if (skinType == 1)
            return 67. / uvIndex;
        else
            return (skinType - 1) * 100. / uvIndex;
    }

    //Get the risk ID based on the uvIndex
    //https://www.epa.gov/sites/production/files/documents/uviguide.pdf
    public static int getRisk(int uvIndex){
        if(uvIndex == Risk.NONE)
            return Risk.NONE;
        else if (uvIndex < Risk.MODERATE)
            return Risk.LOW;
        else if(uvIndex < Risk.HIGH)
            return Risk.MODERATE;
        else if(uvIndex < Risk.VERY_HIGH)
            return Risk.HIGH;
        else if(uvIndex < Risk.EXTREME)
            return Risk.VERY_HIGH;
        return Risk.EXTREME;
    }

}
