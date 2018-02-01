package com.green.wcms.app.work;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.green.wcms.app.R;
import com.green.wcms.app.adaptor.WorkAdapter;
import com.green.wcms.app.menu.MainActivity;
import com.green.wcms.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkStandardFragment extends Fragment {
    private static final String TAG = "WorkStandardFragment";
    private String url = MainActivity.ipAddress+MainActivity.contextPath+"/rest/Safe/workMenuList";

    private ArrayList<HashMap<String, Object>> workMenuArray;
    private WorkAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    private AQuery aq = new AQuery(getActivity());

    @Override
    public void onStart() {
        super.onStart();
    }//onStart

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_work, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_home).setVisibility(View.VISIBLE);
        async_progress_dialog("getMenuInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getMenuInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {

        if(!object.get("count").equals(0)) {
            try {
                workMenuArray = new ArrayList<>();
                workMenuArray.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("title",object.getJSONArray("datas").getJSONObject(i).get("kind_nm").toString());
                    hashMap.put("code",object.getJSONArray("datas").getJSONObject(i).get("kind_cd").toString());
                    workMenuArray.add(hashMap);
                }

                mAdapter = new WorkAdapter(getActivity(), workMenuArray);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "에러코드 WorkStand 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }


    public void onFragment(Fragment fragment, Bundle bundle,String title){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment fragment = new WorkStandardMainFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title",workMenuArray.get(position).get("title").toString());
            bundle.putString("code",workMenuArray.get(position).get("code").toString());
            onFragment(fragment,bundle,"작업기준상세");
        }
    }

}
