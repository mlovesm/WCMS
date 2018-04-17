package com.green.wcms.app.check;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.green.wcms.app.R;
import com.green.wcms.app.adaptor.AnyExpandableAdapter;
import com.green.wcms.app.fragment.FragMenuActivity;
import com.green.wcms.app.menu.MainActivity;
import com.green.wcms.app.retrofit.Datas;
import com.green.wcms.app.retrofit.RetrofitService;
import com.green.wcms.app.util.KeyValueArrayAdapter;
import com.green.wcms.app.util.UtilClass;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangerReportFragment extends Fragment {
    private final String TAG = "DangerReportFragment";
    private String userId="";
    private ProgressDialog pDlalog = null;

    private AnyExpandableAdapter mMenuAdapter;

    @Bind(R.id.textView1) TextView tvData1;
    @Bind(R.id.spinner1) Spinner spn_usePart1;

    private String[] usePart1KeyList;
    private String[] usePart1ValueList;
    String selectUsePart1Key="";
    private String[] usePart2KeyList;
    private String[] usePart2ValueList;
    String selectUsePart2Key="";
    private String[] equipKeyList;
    private String[] equipValueList;
    String selectequipKey="";

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedPostionKey2;
    private int selectedPostion2=0;
    private String selectedPostionKey3;
    private int selectedPostion3=0;
    private String selectUsePart2NM;
    private String selectEquipNM;

    private RetrofitService service;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.danger_report, container, false);
        ButterKnife.bind(this, view);
        UtilClass.setToolbar(getActivity(), getArguments().getString("title"));

        service = RetrofitService.rest_api.create(RetrofitService.class);



        return view;
    }//onCreateView

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            alertDialogSave();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UtilClass.logD(TAG, "onDestroy");
    }


    public void async_progress_dialog(String callback){
        if(callback.equals("getCheckMInfo")){
            pDlalog = new ProgressDialog(getActivity());
//            UtilClass.showProcessingDialog(pDlalog);
            Call<Datas> call = service.listData("Check","checkMInfoList", "checkInfo=");
            call.enqueue(new Callback<Datas>() {
                @Override
                public void onResponse(Call<Datas> call, Response<Datas> response) {
                    UtilClass.logD(TAG, "response="+response);
                    if (response.isSuccessful()) {
                        UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                        String status= response.body().getStatus();
                        try {
                            getUsePart1Data();

                            userId= response.body().getList().get(0).get("USER_ID").toString();
                            if(MainActivity.loginUserId.equals(userId)){
                            }else{
//                                getActivity().findViewById(R.id.layout_bottom).setVisibility(View.GONE);
                            }
                            selectedPostionKey = response.body().getList().get(0).get("USE_PART1").toString();
                            selectedPostionKey2 = response.body().getList().get(0).get("USE_PART2").toString();
                            selectedPostionKey3 = response.body().getList().get(0).get("EQUIP_NO").toString();

                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 CheckWrite 5", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public void getUsePart1Data() {
        Call<Datas> call = service.listData("Check","usePart1List");
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
                        usePart1KeyList = new String[response.body().getList().size()];
                        usePart1ValueList = new String[response.body().getList().size()];
                        for(int i=0; i<response.body().getList().size();i++){
                            usePart1KeyList[i]= response.body().getList().get(i).get("CHILD_CD").toString();
                            if(usePart1KeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            usePart1ValueList[i]= response.body().getList().get(i).get("CHILD_NM").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(usePart1ValueList);
                        adapter.setEntryValues(usePart1KeyList);

                        spn_usePart1.setPrompt("사업장");
                        spn_usePart1.setAdapter(adapter);
                        spn_usePart1.setSelection(selectedPostion);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 CheckWrite 3", Toast.LENGTH_SHORT).show();
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

    //날짜설정
    @OnClick(R.id.textView1)
    public void getDateDialog() {
        getDialog("D");
    }

    public void getDialog(String gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        if(gubun.equals("D")){
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);
            dialog.show();
        }else{
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), time_listener, hour, minute, false);
            dialog.show();
        }

    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//            Toast.makeText(getActivity(), year + "년" + (monthOfYear+1) + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);

            tvData1.setText(year+"."+month+"."+day);
        }
    };

    private TimePickerDialog.OnTimeSetListener time_listener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // 설정버튼 눌렀을 때
            Toast.makeText(getActivity(), hourOfDay + "시 " + minute + "분", Toast.LENGTH_SHORT).show();
            String hour= UtilClass.addZero(hourOfDay);
            String min= UtilClass.addZero(minute);

//            tvData2.setText(hour+":"+min);

        }
    };

    @OnClick(R.id.button1)
    public void alertDialogSave(){
        alertDialog("S");

    }


    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("전송하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else if(gubun.equals("D")){
                    deleteData();
                }else{
                    onPushData();
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // AlertDialog를 닫는다.
            }
        });
        alertDlg.show();
    }

    //삭제
    public void deleteData() {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.deleteData("Check","checkInfo", "", "");

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse CheckWrite",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성,수정
    public void postData() {
        String check_date = tvData1.getText().toString();
        String use_part1 = selectUsePart1Key;
        String use_part2 = selectUsePart2Key;
        String equip_no = selectequipKey;

        Map<String, Object> map = new HashMap();
        map.put("loginUserId",MainActivity.loginUserId);
        map.put("check_date",check_date);
        map.put("use_part1",use_part1);
        map.put("use_part2",use_part2);
        map.put("equip_no",equip_no);
        map.put("use_part2_nm",selectUsePart2NM);
        map.put("equip_nm",selectEquipNM);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;

        call = service.updateData("Check","checkInfo", map);
        map.put("cu_equip_no",selectedPostionKey3);


        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure CheckWrite",Toast.LENGTH_LONG).show();
            }
        });

    }

    //푸시 전송
    public void onPushData() {
        Map<String, Object> map = new HashMap();
        map.put("writer_sabun",MainActivity.loginUserId);
        map.put("writer_name",MainActivity.loginName);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.sendData("Board","sendPushData",map);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse Push",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        UtilClass.logD(TAG,"response="+response);
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                Intent intent = new Intent(getActivity(),FragMenuActivity.class);
                intent.putExtra("title", "점검관리");
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }else if(status.equals("successOnPush")){

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
