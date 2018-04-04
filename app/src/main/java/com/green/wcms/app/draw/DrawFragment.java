package com.green.wcms.app.draw;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxStatus;
import com.green.wcms.app.R;
import com.green.wcms.app.adaptor.DrawAdapter;
import com.green.wcms.app.retrofit.Datas;
import com.green.wcms.app.retrofit.RetrofitService;
import com.green.wcms.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DrawFragment extends Fragment {
    private static final String TAG = "DrawFragment";
    private ProgressDialog pDlalog = null;
    private String title;

    private ArrayList<HashMap<String,Object>> arrayList;
    private DrawAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.editText1) EditText et_search;
    String search_column;	//검색 컬럼

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        title= getArguments().getString("title");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_list, container, false);
        ButterKnife.bind(this, view);

        layout.setVisibility(View.GONE);

        async_progress_dialog();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }//onCreateView

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Equipment","drawInfoList");
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        arrayList = new ArrayList<>();
                        arrayList.clear();
                        for(int i=0; i<response.body().getList().size();i++){
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("key",response.body().getList().get(i).get("DRAW_CD").toString());
                            hashMap.put("data1",response.body().getList().get(i).get("DRAW_CD").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("DRAW_NM").toString());
                            arrayList.add(hashMap);
                        }

                        mAdapter = new DrawAdapter(getActivity(), arrayList, "Draw");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Draw 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Equipment",Toast.LENGTH_SHORT).show();
            }
        });
    }

    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag = null;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new DrawViewFragment());
            bundle.putString("title",title+"상세");
            String key= arrayList.get(position).get("key").toString();
            bundle.putString("draw_cd", key);

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack(title+"상세");
            fragmentTransaction.commit();
        }
    }

}
