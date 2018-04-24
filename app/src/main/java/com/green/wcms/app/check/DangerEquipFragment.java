package com.green.wcms.app.check;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.green.wcms.app.R;
import com.green.wcms.app.adaptor.CheckAdapter;
import com.green.wcms.app.retrofit.Datas;
import com.green.wcms.app.retrofit.DatasB;
import com.green.wcms.app.retrofit.RetrofitService;
import com.green.wcms.app.util.UtilClass;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment Life Style
 * 1. Fragment is added
 * 2. onAttach()                    Fragment가 Activty에 붙을때 호출
 * 3. onCreate()                    Activty에서의 onCreate()와 비슷하나, ui 관련 작업은 할 수 없다.
 * 4. onCreateView()                Layout을 inflater을 하여 View 작업을 하는 곳
 * 5. onActivityCreated()           Activity에서 Fragment를 모두 생성하고난 다음에 호출됨. Activty의 onCreate()에서 setContentView()한 다음과 같다
 * 6. onStart()                     Fragment가 화면에 표시될때 호출, 사용자의 Action과 상호 작용이 불가능함
 * 7. onResume()                    Fragment가 화면에 완전히 그렸으며, 사용자의 Action과 상호 작용이 가능함
 * 8. Fragment is active
 * 9. User navigates backward or fragment is removed/replaced  or Fragment is added to the back stack, then removed/replaced
 * 10. onPause()
 * 11. onStop()                     Fragment가 화면에서 더이상 보여지지 않게됬을때
 * 12. onDestroy()                  View 리소스를 해제할수있도록 호출. backstack을 사용했다면 Fragment를 다시 돌아갈때 onCreateView()가 호출됨
 * 13. onDetached()
 * 14. Fragment is destroyed
 */

/**
 * Google Map CallStack
 * 1. onCreate()
 * 2. onCreateView()
 * 3. onActivityCreated()
 * 4. onStart();
 * 5. onResume();
 * 5-2. onMapReady();
 * 6. onPause();
 * 7. onSaveInstanceState();
 * 8. onMapReady();
 */

public class DangerEquipFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "DangerEquipFragment";
    private ProgressDialog pDlalog = null;

    private Animation slideUp;
    private Animation slideDown;
    private boolean isDown = false;

    @Bind(R.id.imageView1) ImageView exButton;
    @Bind(R.id.linearTop) LinearLayout layout;
    @Bind(R.id.tabHost1) TabHost tabHost;
    @Bind(android.R.id.tabcontent) FrameLayout frameLayout;
    @Bind(R.id.fab_menu) FloatingActionMenu fab;
    @Bind(R.id.fab1) FloatingActionButton fab1;
    @Bind(R.id.fab2) FloatingActionButton fab2;

    private GoogleMap mMap;
    @Bind(R.id.map_view) MapView mapView;
    private MarkerOptions[] mMarkerArray;
    private LatLng[] markerPoint;
    private LatLngBounds.Builder bounds;
    private Marker lastClicked = null;

    //탭
    @Bind(R.id.textView1) TextView tvData1;
    @Bind(R.id.textView2) TextView tvData2;
    @Bind(R.id.textView3) TextView tvData3;
    @Bind(R.id.textView4) TextView tvData4;

    private ArrayList<HashMap<String,Object>> arrayList;
    private CheckAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;

    @Bind(R.id.textView5) TextView tvData5;
    @Bind(R.id.textView6) TextView tvData6;


    private double latitd= 0.0;
    private double longtd= 0.0;
    private String msdsId="";
    private boolean onMakerClick= false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.danger_map, container, false);
        ButterKnife.bind(this, view);

        layout.setVisibility(View.GONE);

        slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        slideUp.setAnimationListener(animationListener);
        slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        slideDown.setAnimationListener(animationListener);

        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("0").setContent(R.id.content1).setIndicator("사고설비");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("1").setContent(R.id.content2).setIndicator("대응차량");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("2").setContent(R.id.content3).setIndicator("방제");

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        tabHost.setCurrentTab(0);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
//                tabHost.getTabWidget().getChildAt(Integer.parseInt(s)).setBackgroundColor(Color.MAGENTA);
                switch (s) {
                    case "0":
                        UtilClass.logD(TAG, "탭1");
                        if(msdsId!="") getTabData1();
                        break;
                    case "1":
                        UtilClass.logD(TAG, "탭2");
                        if(msdsId!="") getTabData2();
                        break;
                    case "2":
                        UtilClass.logD(TAG, "탭3");
                        if(msdsId!="") getTabData3();
                        break;
                }

            }
        });

        fab.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (opened) {

                } else {

                }
            }
        });
        fab.setClosedOnTouchOutside(true);

        mapView.getMapAsync(this);

        return view;
    }//onCreateView

    public void startAnimation() {
        isDown = !isDown;

        if (isDown) {
            layout.startAnimation(slideDown);
            exButton.setImageResource(R.drawable.ic_stop);
        } else {
            layout.startAnimation(slideUp);
            exButton.setImageResource(R.drawable.ic_more);
        }
    }

    Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            layout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!isDown) {
                layout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        if(mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        // These are both viewgroups containing an ImageView with id "badge" and two TextViews with id
        // "title" and "snippet".
        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {

            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {

            render(marker, mContents);
            return null;
        }

        private void render(Marker marker, View view) {
            int badge;
            // Use the equals() method on a Marker to check for equals.  Do not use ==.
//            if (marker.equals(mBrisbane)) {
//                badge = R.drawable.badge_qld;
//            } else {
//                // Passing 0 to setImageResource will clear the image view.
//                badge = 0;
//            }
            badge = R.drawable.ic_map_black_24dp;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

            String title = marker.getTitle();
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                // Spannable string allows us to edit the formatting of the text.
                SpannableString titleText = new SpannableString(title);
                titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
                titleUi.setText(titleText);
            } else {
                titleUi.setText("");
            }

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));

            SpannableString snippetText = new SpannableString(snippet);
//                snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 10, 0);
//                snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 12, snippet.length(), 0);
            snippetUi.setText(snippetText);

//                snippetUi.setText("");

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getMapData(mMap);

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

    }

    @OnClick(R.id.imageView1)
    public void expandableView() {
        startAnimation();
    }


    @OnClick({R.id.fab1, R.id.fab2})
    public void flotingAction(View v){
        int id = v.getId();
        switch (id){
            case R.id.fab1:
                Log.d("Raj", "Fab 1");
                Fragment frag = null;
                Bundle bundle = new Bundle();

                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentReplace, frag = new DangerReportFragment());
                bundle.putString("title","사고발생상세");
                bundle.putString("chk_no", "");

                frag.setArguments(bundle);
                fragmentTransaction.addToBackStack("사고발생상세");
                fragmentTransaction.commit();
                fab.close(true);
                break;
            case R.id.fab2:

                Log.d("Raj", "Fab 2");
                break;
        }
    }

    private void getMapData(final GoogleMap mMap){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","accEquipInfoList");
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        mMarkerArray = new MarkerOptions[response.body().getList().size()];
                        markerPoint = new LatLng[response.body().getList().size()];
                        bounds = new LatLngBounds.Builder();
                        for(int i=0; i<response.body().getList().size();i++){
                            mMarkerArray[i] = new MarkerOptions();
                            mMarkerArray[i].title(response.body().getList().get(i).get("EQUIP_NM").toString());
                            mMarkerArray[i].snippet(response.body().getList().get(i).get("EQUIP_NO").toString());

                            if(response.body().getList().get(i).get("LATITD")!=null){
                                latitd= Double.parseDouble(response.body().getList().get(i).get("LATITD").toString());
                            }else{
                                latitd= 0.0;
                            }
                            if(response.body().getList().get(i).get("LONGTD")!=null){
                                longtd= Double.parseDouble(response.body().getList().get(i).get("LONGTD").toString());
                            }else{
                                longtd= 0.0;
                            }

                            LatLng position = new LatLng(latitd, longtd);
                            markerPoint[i]= position;

                            mMarkerArray[i].position(markerPoint[i]);
                            Marker marker= mMap.addMarker(mMarkerArray[i]);
                            marker.setTag(Double.valueOf((double) response.body().getList().get(i).get("MSDS_ID")).intValue());
                            bounds.include(new LatLng(latitd, longtd));

                        }
//                        mMap.moveCamera(CameraUpdateFactory.newLatLng(markerPoint[0]));
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                        mMap.setContentDescription("Map with lots of markers.");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 70));


                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DangerEquip Map 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure DangerEquip Map",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getTabData1(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listDataQuery("Check","accEquipInfoList", msdsId);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {

                        tvData1.setText(response.body().getList().get(0).get("EQUIP_NM").toString());
                        tvData2.setText(response.body().getList().get(0).get("MSDS_NM").toString());
                        tvData3.setText(response.body().getList().get(0).get("MSDS_TYPE").toString());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DangerEquip 1", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure DangerEquip",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getTabData2(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Check","accEquipInfoViewCar", msdsId);
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
                            hashMap.put("key",response.body().getList().get(i).get("CAR_NO").toString());
                            hashMap.put("data1",response.body().getList().get(i).get("CAR_NB").toString());
                            hashMap.put("data2",response.body().getList().get(i).get("CAR_NM").toString());
                            arrayList.add(hashMap);
                        }

                        mAdapter = new CheckAdapter(getActivity(), arrayList, "DangerEquip");
                        listView.setAdapter(mAdapter);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DangerEquip 2", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure DangerEquip 2",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getTabData3(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<DatasB> call = service.listDataB("Check","accEquipInfoViewDrug", msdsId);
        call.enqueue(new Callback<DatasB>() {
            @Override
            public void onResponse(Call<DatasB> call, Response<DatasB> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                    try {
                        tvData5.setText(response.body().getDatasA().get(0).get("DRUG").toString());
                        tvData6.setText(response.body().getDatasB().get(0).get("KITS").toString());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 DangerEquip 3", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<DatasB> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure DangerEquip",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        onMakerClick= true;
        msdsId= String.valueOf(marker.getTag());
        UtilClass.logD(TAG, "tag="+marker.getTag());
        if(msdsId!="") getTabData1();

            // This causes the marker at Perth to bounce into position when it is clicked.
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(
                            1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker.setAnchor(0.5f, 1.0f + 2 * t);

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        if (lastClicked!=null)
            lastClicked.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        lastClicked = marker;
        // Markers have a z-index that is settable and gettable.
        float zIndex = marker.getZIndex() + 1.0f;
        marker.setZIndex(zIndex);
//        Toast.makeText(getActivity(), marker.getTitle() + " z-index set to " + zIndex, Toast.LENGTH_SHORT).show();

        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(getActivity(), "Click Info Window="+marker.getSnippet()+", "+marker.getZIndex(), Toast.LENGTH_SHORT).show();
    }


}
