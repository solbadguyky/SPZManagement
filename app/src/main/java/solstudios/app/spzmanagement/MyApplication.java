package solstudios.app.spzmanagement;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Created by solbadguyky on 7/1/16.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        ///sugar regist
        SugarContext.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();

    }
}
