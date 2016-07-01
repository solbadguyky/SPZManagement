package solstudios.app.spzmanagement;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.orm.SugarContext;

/**
 * Created by solbadguyky on 6/30/16.
 */
public abstract class BaseActivity extends AppCompatActivity{
    public abstract void initValue();
    public abstract void initView();
    public abstract void setupValue();
    public abstract void setupView();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initValue();
        initView();
        setupValue();
        setupView();
    }
}
