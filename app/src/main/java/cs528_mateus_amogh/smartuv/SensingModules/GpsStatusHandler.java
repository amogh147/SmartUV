package cs528_mateus_amogh.smartuv.SensingModules;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.util.Log;


/**
 * Created by mateus on 4/8/2016.
 */
public class GpsStatusHandler {
    private GpsStatus mStatus;
    private int mNSatellites = 0;

    //Parameters
    private double mSrn30 = 0;
    private double mSrn2030 = 0;
    private double mSrn1020 = 0;
    private double mSrn10 = 0;
    private double mSrnAvg = 0;
    private double mSrnStd = 0;
    private double mSrnNorm = 0;
    private double mUsedInFix = 0;

    //GPS Status. Logistic function coefficients
    private static final double SRN_AVG_COEFF = -1.1895;
    private static final double SRN_STD_COEFF = 0;
    private static final double SRN_NORM_COEFF = -1.1819;

    private static final double SRN10_COEFF = -27.779;
    private static final double SRN1020_COEFF = 0;
    private static final double SRN2030_COEFF = 0;
    private static final double SRN30_COEFF = -6.0335;

    private static final double USED_IN_FIX_COEFF = 0;//6.8

    private static final double SIGMOID_INTERCEPT = 31.6767;

    public double sigmoid(){
        return 1./(1.+Math.exp(-(SIGMOID_INTERCEPT +
                SRN1020_COEFF*mSrn1020 +
                SRN2030_COEFF*mSrn2030 +
                SRN30_COEFF*mSrn30 +
                SRN10_COEFF*mSrn10 +
                SRN_AVG_COEFF*mSrnAvg +
                SRN_STD_COEFF*mSrnStd +
                SRN_NORM_COEFF*mSrnNorm +
                USED_IN_FIX_COEFF*mUsedInFix)));
    }

    public boolean isOutdoor(){
        Log.v("IO", String.valueOf(sigmoid()));
        return sigmoid()<.5;
    }

    public double snr30(){
        return mSrn30;
    }

    public GpsStatusHandler(GpsStatus status) {
        this.mStatus = status;
        calcParameters();
    }


    public GpsStatus getGpsStatus() {
        return mStatus;
    }

    public int getNSatellites(){
        return mNSatellites;
    }

    private void calcParameters(){
        for(GpsSatellite satellite : mStatus.getSatellites()) {
            double srn = satellite.getSnr();
            mSrnAvg += srn;
            if(srn>=30)
                mSrn30++;
            else if(srn>=20)
                mSrn2030++;
            else if(srn>=10)
                mSrn1020++;
            else//less than 10
                mSrn10++;

            if(satellite.usedInFix())
                mUsedInFix++;

            mNSatellites++;
        }

        mSrnAvg /= mNSatellites;
        calcStdNorm();
        mSrn30 /= mNSatellites;
        mSrn2030 /= mNSatellites;
        mSrn1020 /= mNSatellites;
        mSrn10 /= mNSatellites;
        mUsedInFix /= mNSatellites;
    }

    private void calcStdNorm(){
        double sum = 0;
        for(GpsSatellite satellite : mStatus.getSatellites()) {
            sum += (satellite.getSnr()-mSrnAvg)*(satellite.getSnr()-mSrnAvg);
        }
        mSrnStd = Math.sqrt(sum/(mNSatellites-1));
        mSrnNorm = mSrnAvg/mSrnStd;
    }


    public String toString(int count) {
        String str = new String();
        str += "GPS Status " + String.valueOf(count) + ": ";
        for (GpsSatellite satellite : mStatus.getSatellites())
            str += satellite.getSnr() + " ";
        str += "\nFix:" + String.valueOf(mStatus.getTimeToFirstFix());
        str += "\nAvailable:" + String.valueOf(mNSatellites);
        //str += "\nMean: " + String.valueOf(mMean);
        str += "\nNSatellites: " + String.valueOf(mNSatellites);
        return str;
    }
}
