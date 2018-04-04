package com.green.wcms.app.menu;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.green.wcms.app.R;
import com.green.wcms.app.fragment.FragMenuActivity;
import com.green.wcms.app.fragment.NFCMenuActivity;
import com.green.wcms.app.retrofit.Datas;
import com.green.wcms.app.retrofit.RetrofitService;
import com.green.wcms.app.util.BackPressCloseSystem;
import com.green.wcms.app.util.SettingPreference;
import com.green.wcms.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

//    adb shell dumpsys activity activities | findstr "Run"
    private static final String TAG = "MainActivity";
    public static String ipAddress= "http://w-cms.co.kr:9090";
//    public static String ipAddress= "http://192.168.0.22:9191";
    public static String contextPath= "/m_wcms";
    public static String comp_database;
    public static String ori_comp_database;
    private ProgressDialog pDlalog = null;
    private RetrofitService service;
    private String fileDir= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator;
    private String fileNm;

    //FCM 관련
    private String token=null;
    private String phone_num=null;
    public static boolean onAppCheck= false;
    public static String pendingPath= "";
    public static String pendingPathKey= "";

    private BackPressCloseSystem backPressCloseSystem;
    private PermissionListener permissionlistener;

    private SettingPreference pref = new SettingPreference("loginData",this);
    public static String loginUserId;
    public static String loginName;
    public static String latestAppVer;
    public static String comp_id;
    public static String use_part1;
    private int user_auth;

    @Bind(R.id.textView1) TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        backPressCloseSystem = new BackPressCloseSystem(this);
        service= RetrofitService.rest_api.create(RetrofitService.class);

        comp_database = pref.getValue("COMP_DATABASE","");
        ori_comp_database = pref.getValue("ORI_COMP_DATABASE","");
        loginUserId = pref.getValue("userId","");
        latestAppVer = pref.getValue("LATEST_APP_VER","");
        comp_id = pref.getValue("comp_id","");
        user_auth= pref.getValue("user_auth", 0);
        use_part1= pref.getValue("use_part1", "");
        UtilClass.logD(TAG, "user_auth= "+user_auth);

        String currentAppVer= getAppVersion(this);
        UtilClass.logD(TAG, "currentAppVer="+currentAppVer+", latestAppVer="+latestAppVer);
        if(!currentAppVer.equals(latestAppVer)){
            //최신버전이 아닐때
            fileNm= "wcms_"+latestAppVer+"-debug.apk";
//            alertDialog();
        }

        token = FirebaseInstanceId.getInstance().getToken();
        UtilClass.logD(TAG, "Refreshed token: " + token);

        async_progress_dialog();

        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
                phone_num= getPhoneNumber();
                postData();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                phone_num="";
                postData();
            }
        };
        new TedPermission(MainActivity.this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("전화번호 정보를 가져오기 위해선 권한이 필요합니다.")
                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                .setGotoSettingButtonText("권한확인")
                .setPermissions(Manifest.permission.CALL_PHONE)
                .check();

        onAppCheck= true;

    }

    public static String getAppVersion(Context context) {
        // application version
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            UtilClass.logD(TAG, "getAppVersion Error");
        }

        return versionName;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    @OnClick({R.id.textView1, R.id.textButton1})
    public void getUnCheckList() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
//        intent.putExtra("title", "미점검리스트");
        intent.putExtra("title", "테스트");
        startActivity(intent);
    }

    @OnClick(R.id.imageView1)
    public void getMenu1() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "장치관리");
        startActivity(intent);
    }

    @OnClick(R.id.imageView2)
    public void getMenu2() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "도면관리");
        startActivity(intent);
    }

    @OnClick(R.id.imageView3)
    public void getMenu3() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "MSDS관리");
        startActivity(intent);
    }

    @OnClick(R.id.imageView4)
    public void getMenu4() {
        if(user_auth==0){
            Toast.makeText(getApplicationContext(), "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
            intent.putExtra("title", "점검관리");
            startActivity(intent);
        }

    }

    @OnClick(R.id.imageView5)
    public void getMenu5() {
        if(user_auth==0){
            Toast.makeText(getApplicationContext(), "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(getBaseContext(), NFCMenuActivity.class);
            intent.putExtra("title", "NFC관리");
            startActivity(intent);
        }
    }

    @OnClick(R.id.imageView6)
    public void getMenu6() {
        if(user_auth==0){
            Toast.makeText(getApplicationContext(), "해당 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }else {
            Intent intent = new Intent(getBaseContext(), FragMenuActivity.class);
            intent.putExtra("title", "점검승인");
            startActivity(intent);
        }
    }

    public void async_progress_dialog(){
        service = RetrofitService.rest_api.create(RetrofitService.class);

        Call<Datas> call = service.listData("Check","unCheckList", use_part1);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        textView1.setText(response.body().getCount()+"");
//                        BadgeClass.setBadge(getApplicationContext(), response.body().getCount());

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 UnCheck 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure UnCheck",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //푸시 데이터 전송
    public void postData() {
        String userId= pref.getValue("userId","");
        String android_id = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        Map<String, Object> map = new HashMap();
        map.put("Token",token);
        map.put("phone_num",phone_num);
        map.put("userId",userId);
        map.put("and_id",android_id);

        pDlalog = new ProgressDialog(MainActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        service = RetrofitService.rest_api.create(RetrofitService.class);
        Call<Datas> call = service.sendData("Board","fcmTokenData",map);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                }else{
                    Toast.makeText(getApplicationContext(), "handleResponse Main",Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "토큰 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 단말기 핸드폰번호 얻어오기
    public String getPhoneNumber() {
        String num = null;
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            num = tm.getLine1Number();
            if(num!=null&&num.startsWith("+82")){
                num = num.replace("+82", "0");
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "오류가 발생 하였습니다!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return num;
    }

    public void alertDialog(){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDlg.setTitle("알림");
        alertDlg.setMessage("현재 앱의 버전보다 높은 최신 버전이 있습니다.");

        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("지금 설치", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installAPK();
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("다음에 설치", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    //파일 다운로드
    public void downloadFile(String fileUrl) {
        pDlalog = new ProgressDialog(MainActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        Call<ResponseBody> call = service.downloadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    boolean writtenToDisk = UtilClass.writeResponseBodyToDisk(response.body(), fileDir, fileNm);
                    UtilClass.logD(TAG, "file download was a success? " + writtenToDisk);

                    if(writtenToDisk){
                        installAPK();
                    }else{
                        Toast.makeText(getApplicationContext(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure downloadFile",Toast.LENGTH_LONG).show();
            }
        });

    }

    public void installAPK() {
        UtilClass.logD("InstallApk", "Start");
        File apkFile = new File(fileDir + fileNm);
        if(apkFile.exists()) {
            try {
                Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = null;

                // So you have to use Provider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", apkFile);

                    // Add in case of if We get Uri from fileProvider.
                    webLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    webLinkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }else{
                    uri = Uri.fromFile(apkFile);
                }

                webLinkIntent.setDataAndType(uri, "application/vnd.android.package-archive");

                startActivity(webLinkIntent);
            } catch (ActivityNotFoundException ex){
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(), "설치에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(), "최신버전 파일을 다운로드 합니다.", Toast.LENGTH_SHORT).show();
            try {
                permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        try {
                            downloadFile("http://w-cms.co.kr:9090/app/apkDown.do?appGubun="+fileNm);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(getApplicationContext(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                    }
                };
                new TedPermission(getApplicationContext())
                        .setPermissionListener(permissionlistener)
                        .setRationaleMessage("파일을 다운받기 위해선 권한이 필요합니다.")
                        .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                        .setGotoSettingButtonText("권한확인")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();

            }catch (Exception e){

            }
        }

    }
}
