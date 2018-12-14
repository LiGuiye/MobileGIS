package com.demo.liguiye.mobilegis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import static com.demo.liguiye.mobilegis.HttpUtils.upload;

public class EsriMap extends AppCompatActivity {
    private MapView mMapView;
    private ArcGISMap mMap;
    private Portal mPortal;
    private PortalItem mPortalItem;

    private EditText et_longitude;
    private EditText et_latitude;
    private Button uploadBT,drawBT,clearBT;

    private GraphicsOverlay graphicsOverlay;

    private Double x,y;

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
        mPortalItem = new PortalItem(mPortal, getResources().getString(R.string.webmap_houses_with_mortgages_id));
        // create a map from a PortalItem
        mMap = new ArcGISMap(mPortalItem);
        //set the map to be displayed in this view
        mMapView.setMap(mMap);
        //初始化绘画层
        // create a graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        // add graphics overlay to the map view
        mMapView.getGraphicsOverlays().add(graphicsOverlay);
        //点的参考
        final SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();
        et_longitude = findViewById(R.id.editText_longitude);
        et_latitude = findViewById(R.id.editText_latitude);
        //开始绘制
        drawBT = findViewById(R.id.drawBT);
        drawBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x = Double.valueOf(et_longitude.getText().toString());
                y = Double.valueOf(et_latitude.getText().toString());
                Point buoy1Loc = new Point(x, y,SPATIAL_REFERENCE);
                // create a red (0xFFFF0000) circle simple marker symbol
                SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF0000, 50);
                // create graphics and add to graphics overlay
                Graphic buoyGraphic1 = new Graphic(buoy1Loc, redCircleSymbol);
                graphicsOverlay.getGraphics().addAll(Arrays.asList(buoyGraphic1));
            }
        });
        //清空
        clearBT = findViewById(R.id.clearBT);
        clearBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphicsOverlay.getGraphics().clear();
            }
        });
        //上传至服务器
        uploadBT = findViewById(R.id.uploadBT);
        uploadBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpURLConnection conn;
                        try {
                            conn = (HttpURLConnection) new URL("http://192.168.43.83:80/uploadService_latitude_longitude.php?latitude=" + et_latitude.getText().toString() + "&longitude="+ et_longitude.getText().toString()).openConnection();
                            conn.setRequestMethod("GET");
                            conn.getInputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

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
}
