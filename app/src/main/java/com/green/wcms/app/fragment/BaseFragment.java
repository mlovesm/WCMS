package com.green.wcms.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.green.wcms.app.R;
import com.green.wcms.app.util.UtilClass;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by GS on 2017-08-21.
 */
public class BaseFragment extends Fragment {
    public final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        UtilClass.logD(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        UtilClass.logD(TAG, "onResume");
        super.onResume();
    }


    @Override
    public void onDestroy() {
        UtilClass.logD(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @Override
    public void onStart() {
        super.onStart();
        UtilClass.logD(TAG, "onStart");

    }
}
