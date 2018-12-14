package com.demo.liguiye.mobilegis;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LatitudeLongitudeGrid;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import static java.lang.Double.valueOf;

public class EsriMap extends AppCompatActivity {
    private MapView mMapView;
    private ArcGISMap mMap;
    private Portal mPortal;
    private PortalItem mPortalItem;

    private EditText et_longitude;
    private EditText et_latitude;
    private Button uploadBT, drawBT, clearBT, locateBT;
    private GraphicsOverlay graphicsOverlay;
    private Double x, y;
    private static final String TAG = EsriMap.class.getSimpleName();
    private double latitude = 0.0;
    private double longitude = 0.0;
    private TextView info;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_esri_map);
        //绑定地图容器
        mMapView = findViewById(R.id.mapView);
        //添加底图
        // get the portal url for ArcGIS Online
        mPortal = new Portal(getResources().getString(R.string.portal_url));
        // get the pre-defined portal id and portal url
        mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.testmap1));
        // create a map from a PortalItem
        mMap = new ArcGISMap(mPortalItem);
        //set the map to be displayed in this view
        mMapView.setMap(mMap);
        //初始化绘画层
        // create a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        // add graphics overlay to the map view
        mMapView.getGraphicsOverlays().add(graphicsOverlay);


/////////根据输入开始画点//////////////////////////////////////////////////////////////
        et_longitude = findViewById(R.id.editText_longitude);
        et_latitude = findViewById(R.id.editText_latitude);
        drawBT = findViewById(R.id.drawBT);
        drawBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x = valueOf(et_longitude.getText().toString());
                y = valueOf(et_latitude.getText().toString());
                DrawPoint(x,y);
            }
        });
/////////////清空/////////////////////////////////////////////////////////////////////////////////
        clearBT = findViewById(R.id.clearBT);
        clearBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicsOverlay.getGraphics().clear();
            }
        });
////////////////////上传至服务器/////////////////////////////////////////////////////////////////////////////////////////
        uploadBT = findViewById(R.id.uploadBT);
        uploadBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection conn;
                        try {
                            conn = (HttpURLConnection) new URL("http://192.168.43.83:80/uploadService_latitude_longitude.php?latitude=" + et_latitude.getText().toString() + "&longitude=" + et_longitude.getText().toString()).openConnection();
                            conn.setRequestMethod("GET");
                            conn.getInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        locateBT = findViewById(R.id.locateBT);
        info = (TextView) findViewById(R.id.textView_info);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locateBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

//    /**
//     * 权限申请返回结果
//     * @param requestCode 请求码
//     * @param permissions 权限数组
//     * @param grantResults  申请结果数组，里面都是int类型的数
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case 1:
//                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) { //同意权限申请
//                    getLocation();
//                } else { //拒绝权限申请
//                    Toast.makeText(this, "权限被拒绝了", Toast.LENGTH_SHORT).show();
//                }
//                break;
//            default:
//                break;
//        }
//    }

    public void DrawPoint(double x,double y){
        final SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();
        Point buoy1Loc = new Point(x, y, SPATIAL_REFERENCE);
        // create a red (0xFFFF0000) circle simple marker symbol
        SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 50);
        // create graphics and add to graphics overlay
        Graphic buoyGraphic1 = new Graphic(buoy1Loc, redCircleSymbol);
        graphicsOverlay.getGraphics().addAll(Arrays.asList(buoyGraphic1));
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //进行授权
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            info.setText("正在获取授权");
        }else{
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                info.setText("纬度：" + latitude + "\n" + "经度：" + longitude);
                DrawPoint(longitude,latitude);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                info.setText("纬度更新后：" + latitude + "\n" + "经度：" + longitude);
                DrawPoint(longitude,latitude);
            }
        }

    }

    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            info.setText("Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数");
        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, provider);
            info.setText("Provider被enable时触发此函数，比如GPS被打开");
        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, provider);
            info.setText("Provider被disable时触发此函数");
        }

        // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.e("Map", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                latitude = location.getLatitude(); // 经度
                longitude = location.getLongitude(); // 纬度
                info.setText("222");
            }
            info.setText("111");
        }
    };

}


//    @Override
//    protected void onPause(){
//        super.onPause();
//        mMapView.pause();
//        //mSceneView.pause();
//    }
//
//    @Override
//    protected void onResume(){
//        super.onResume();
//        mMapView.resume();
//        //mSceneView.resume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mMapView.dispose();
//        //mSceneView.dispose();
//    }

