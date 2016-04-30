package cs528_mateus_amogh.smartuv.SensingModules;

/**
 * Created by mateus on 4/23/2016.
 */
public interface IModule {

    boolean isTracking();

    void requestUpdates();

    void removeUpdates();

    void setRequestInterval(long time);
}
