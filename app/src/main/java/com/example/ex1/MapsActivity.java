package com.example.ex1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationListener, PlacesListener {

    GoogleMap mMap;
    EditText editPlace;
    LatLng currentPosition;//현재위치
    List<Marker> prevMarkers = null;//마커
    double lat;
    double lng;
    LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        editPlace = (EditText) findViewById(R.id.editPlace);

        prevMarkers = new ArrayList<>();

        //주변 가게 찾기 버튼
        Button btnSearch2 = (Button) findViewById(R.id.btnSearch2);
        btnSearch2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareMap();
                drawMap();
                showPlaceInformation(currentPosition);
            }
        });

    }

    //장소 검색 함수
    private void showPlaceInformation(LatLng location) {
        mMap.clear();//기존 지도 지우기
        if (prevMarkers != null) {
            prevMarkers.clear(); // 기존에 표시됐던 마커 지우기
        }
        //구글 장소 검색 API 요청
        new NRPlaces.Builder()
                .listener(this)
                .key("AIzaSyDVrgJHEj7aTXeZ9Fu4mWeyLNT3VKLhe9I")
                .latlng(lat, lng)//현재 위치 기준
                .radius(5000) //5키로 이내 검색
                .type(PlaceType.SHOE_STORE)//신발 가게
                .build()
                .execute();
        //서버에서 결과가 도착하면 onPlacesSuccess()가 호출됨.
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawMap();
        prepareMap();
    }

    private void drawMap() {
        if (ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_FINE_LOCATION//자세한 위치정보
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)//대략적인 위치정보
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                   1);
            return;
        }
        if(currentPosition == null){
            //지도 종류
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            //현재 위치 표현
            mMap.setMyLocationEnabled(true);
            //줌컨드롤 표시 여부 ( 1 ~ 21 )
            mMap.getUiSettings().setZoomControlsEnabled(true);
            //좌표 지정 new LatLng(위도, 경도)
            LatLng point = new LatLng(36.8372051,127.1680158);
            //지도를 바라보는 카메라 이동
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
            //카메라 이동의 애니메이션 효과
            //줌레벨 : 1 ~ 21 숫자가 클수록 자세한 지도
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            //상명대 : 36.8336012,127.176977
            //서울역 : 37.5536109,126.9674308
            //맵에 마커 표시
            MarkerOptions marker = new MarkerOptions()
                    .position(point)
                    .title("천호지")
                    .snippet("수달이 살고 있어요");
            mMap.addMarker(marker);
        }else {
            //지도를 바라보는 카메라 이동
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            //카메라 이동의 애니메이션 효과
            //줌레벨 : 1 ~ 21 숫자가 클수록 자세한 지도
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 13));
            //상명대 : 36.8336012,127.176977
            //맵에 마커 표시
            MarkerOptions marker = new MarkerOptions()
                    .position(currentPosition)
                    .title("현재 위치");
            mMap.addMarker(marker);

        }

    }


    private void prepareMap() {
        int check = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(check != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//일반지도
            mMap.setMyLocationEnabled(true);//현재 위치 버튼
            mMap.getUiSettings().setZoomControlsEnabled(true);//줌 컨트롤

            //위치정보 관리자 객체
            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //리스너 등록
            locationManager.requestLocationUpdates(//리스너 등록
                    LocationManager.NETWORK_PROVIDER,0,
                    0,this);
            locationManager.requestLocationUpdates(//GPS 값이 바뀌면 감지하는 리스너
                    LocationManager.GPS_PROVIDER, 0,0,this);

            //수동으로 위치 구하기(최근에 방문했던 장소를 보여줌)
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastLocation =
                    locationManager.getLastKnownLocation(locationProvider);

            //최근 gps 좌표를 저장
            if (lastLocation != null){
                lat = lastLocation.getLatitude();
                lng = lastLocation.getLongitude();
            }
            currentPosition = new LatLng(lat, lng);//현재좌표
            Log.i("test", "currentPosition: " + currentPosition);
        }
    }

    //서버에서 장소 검색 결과가 도착하면 호출
    @Override
    public void onPlacesSuccess(final List<Place> places) {
        //매인 화면을 수정해야 하므로 runOnUiThread()에서 처리
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //서버에서 조회된 장소들을 마커 리스트에 등록
                for(Place place : places){
                    LatLng latLng = new LatLng(place.getLatitude(),
                            place.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions
                            .position(latLng)
                            .title(place.getName())
                            .snippet(place.getVicinity());
                    Marker item = mMap.addMarker(markerOptions);
                    prevMarkers.add(item);
                }
                //중복된 값들을 제거하기 위해 HashSet 사용
                HashSet<Marker> hashSet = new HashSet<>();
                hashSet.addAll(prevMarkers);
                prevMarkers.clear();
                prevMarkers.addAll(hashSet);
            }
        });

    }

    public void search(View view) {
        String place = editPlace.getText().toString();

        //임의의 위치 : 남산타워
        Geocoder coder = new Geocoder(getApplicationContext());//텍스트를 좌표로 좌표를 텍스트를 뽑아냄.
        List<Address> list = null;
        try {
            list = coder.getFromLocationName(place, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address = list.get(0);
        double lat = address.getLatitude();//위도
        double lng = address.getLongitude();//경도
        LatLng geoPoint = new LatLng(lat, lng);//좌표 객체
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 15));

        //Marker 추가
        MarkerOptions markerOptions = new MarkerOptions()
                .position(geoPoint)
                .title(place)
                .snippet(geoPoint.toString());
        mMap.addMarker(markerOptions);
    }


    //위치가 변경되면
    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude(); //변경된 위도
        lng = location.getLongitude(); //변경된 경도
        //새로운 현재 좌표
        currentPosition = new LatLng(lat, lng);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)//자세한 위치정보
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)//대락적인 위치정보
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 거부 됨", Toast.LENGTH_SHORT).show();
                }
                else{
                    drawMap();
                    prepareMap();
                }
        }
    }




    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }



    @Override
    public void onPlacesFinished() {

    }
}

