package cs528_mateus_amogh.smartuv;

import android.graphics.Color;
import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mateus on 4/16/2016.
 */
public class HealthInfo {

    //Ids for the risk map
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

    public static Resource getRiskResource(int risk){
        return resMap.get(risk);
    }


    public final static class Risk{
        public static final int NONE = 0;
        public static final int LOW = 1;
        public static final int MODERATE = 3;
        public static final int HIGH = 6;
        public static final int VERY_HIGH = 8;
        public static final int EXTREME = 11;
    }

    public static String getProtectionMessage(int risk){
        return null;
    }

    //in minutes
    public static double calcSunBurnTime(int skinType, int uvIndex){
        //source: http://www.himaya.com/solar/avoidsunburn.html
        if(uvIndex == 0)
            return 0;
        if (skinType == 1)
            return 67. / uvIndex;
        else
            return (skinType - 1) * 100. / uvIndex;
    }



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

    public static int getRiskStrId(int risk){
        return riskStringIdMap.get(risk).first;
    }

    public static int getRiskColorId(int risk){
        return riskStringIdMap.get(risk).second;
    }

    private static final Map<Integer, Pair<Integer,Integer>> riskStringIdMap = new HashMap<Integer, Pair<Integer,Integer>>();
    static {
        riskStringIdMap.put(Risk.NONE, new Pair<Integer, Integer>(R.string.noRisk, R.color.noRisk));
        riskStringIdMap.put(Risk.LOW, new Pair<Integer, Integer>(R.string.lowRisk, R.color.lowRisk));
        riskStringIdMap.put(Risk.MODERATE, new Pair<Integer, Integer>(R.string.moderateRisk, R.color.moderateRisk));
        riskStringIdMap.put(Risk.HIGH, new Pair<Integer, Integer>(R.string.highRisk, R.color.highRisk));
        riskStringIdMap.put(Risk.VERY_HIGH, new Pair<Integer, Integer>(R.string.veryHighRisk, R.color.veryHighRisk));
        riskStringIdMap.put(Risk.EXTREME, new Pair<Integer, Integer>(R.string.extremeRisk, R.color.extremeRisk));
    }
}
