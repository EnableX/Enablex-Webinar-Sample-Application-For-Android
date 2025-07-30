package com.enablex.webinar.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.enablex.webinar.R;
import com.enablex.webinar.adapter.FloorRequestDialogAdapter;
import com.enablex.webinar.adapter.HorizontalViewAdapter;
import com.enablex.webinar.model.FloorRequestModel;
import com.enablex.webinar.model.HorizontalViewModel;
import com.enablex.webinar.model.UserModel;
import com.enablex.webinar.utilities.OnDragTouchListener;
import com.enablex.webinar.web_communication.WebConstants;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import enx_rtc_android.Controller.EnxActiveTalkerViewObserver;
import enx_rtc_android.Controller.EnxChairControlObserver;
import enx_rtc_android.Controller.EnxPlayerView;
import enx_rtc_android.Controller.EnxReconnectObserver;
import enx_rtc_android.Controller.EnxRoom;
import enx_rtc_android.Controller.EnxRoomObserver;
import enx_rtc_android.Controller.EnxRtc;
import enx_rtc_android.Controller.EnxStream;
import enx_rtc_android.Controller.EnxStreamObserver;


public class VideoConferenceActivity extends AppCompatActivity implements EnxRoomObserver, EnxStreamObserver, View.OnClickListener,
        EnxReconnectObserver, EnxActiveTalkerViewObserver, EnxChairControlObserver,FloorRequestDialogAdapter.RequestItemClickListener {
    EnxRtc enxRtc;
    String token;
    String name;
    EnxPlayerView enxPlayerView;
    FrameLayout moderator;
    FrameLayout participant;
    ImageView disconnect;
    ImageView mute, video, camera, volume;
    private TextView audioOnlyText, dummyText;
    EnxRoom enxRooms;
    boolean isVideoMuted = false;
    boolean isAudioMuted = false;
    RelativeLayout rl;
    ArrayList<UserModel> userArrayList;
    Gson gson;
    EnxStream localStream;
    int PERMISSION_ALL = 1;
    boolean isFrontCamera = true;
    List<HorizontalViewModel> list;
    private RecyclerView mHorizontalRecyclerView;
    private HorizontalViewAdapter horizontalAdapter;
    private LinearLayoutManager horizontalLayoutManager;
    private int screenWidth;
    ActionBar actionBar;
    RelativeLayout bottomView;
    boolean touchView;
    ProgressDialog progressDialog;
    RecyclerView mRecyclerView;
    boolean touch = false;
    private String role,roomId;
    private Dialog dialog;
    private MenuItem raiseHandMenuItem,notificationMenuItem;
    private TextView textNotificationCount;
    private ArrayList<FloorRequestModel> floorRequestArrayList;
    private FloorRequestDialogAdapter adapter;
    private int currentClientPosition = 0;
    private FloorRequestModel floorRequestModel;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_conference);

        actionBar = getSupportActionBar();
        floorRequestArrayList = new ArrayList<>();
        getPreviousIntent();
        actionBar.setTitle(name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                initialize();
            }
        }
        if(role.equalsIgnoreCase("participant")){
            dummyText.setText("Please wait for moderator to join");
        }else{
            dummyText.setText("");
        }
    }

    private void initialize() {
        setUI();
        setClickListener();
        userArrayList = new ArrayList<>();
        list = new ArrayList<>();
        gson = new Gson();
        enxRtc = new EnxRtc(this, this, this);
        localStream = enxRtc.joinRoom(token, getLocalStreamJsonObjet(), getReconnectInfo(), new JSONArray());

        progressDialog = new ProgressDialog(this);
        mHorizontalRecyclerView = (RecyclerView) findViewById(R.id.horizontalRecyclerView);

        horizontalAdapter = new HorizontalViewAdapter(list, this, screenWidth, screenWidth, false);

        horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mHorizontalRecyclerView.setLayoutManager(horizontalLayoutManager);
        mHorizontalRecyclerView.setAdapter(horizontalAdapter);
        adapter = new FloorRequestDialogAdapter(this, this, floorRequestArrayList);
    }

    private void setClickListener() {
        disconnect.setOnClickListener(this);
        mute.setOnClickListener(this);
        video.setOnClickListener(this);
        camera.setOnClickListener(this);
        volume.setOnClickListener(this);
        moderator.setOnTouchListener(new OnDragTouchListener(moderator));

        participant.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleTouchListner();
                return false;
            }
        });

    }

    private void setUI() {
        moderator = (FrameLayout) findViewById(R.id.moderator);
        participant = (FrameLayout) findViewById(R.id.participant);
        disconnect = (ImageView) findViewById(R.id.disconnect);
        mute = (ImageView) findViewById(R.id.mute);
        video = (ImageView) findViewById(R.id.video);
        camera = (ImageView) findViewById(R.id.camera);
        volume = (ImageView) findViewById(R.id.volume);
        dummyText = (TextView) findViewById(R.id.dummyText);
        audioOnlyText = (TextView) findViewById(R.id.audioonlyText);
        rl = (RelativeLayout) findViewById(R.id.rl);
        bottomView = (RelativeLayout) findViewById(R.id.bottomView);

        audioOnlyText.setVisibility(View.GONE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels / 3;
    }
    private Menu menu;
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_actions, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
             menuBuilder.setOptionalIconsVisible(true);
        }
        this.menu = menu;
        raiseHandMenuItem = menu.findItem(R.id.raisehand);
        notificationMenuItem = menu.findItem(R.id.action_notification_badge);
        if(role.equalsIgnoreCase("participant")){
            raiseHandMenuItem.setVisible(true);
        }else {
            raiseHandMenuItem.setVisible(false);
        }
        View actionView = MenuItemCompat.getActionView(notificationMenuItem);
        textNotificationCount = (TextView) actionView.findViewById(R.id.notification_badge);
        setupBadge();
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(notificationMenuItem);
            }
        });
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

            if(id== R.id.raisehand) {
                if (enxRooms != null) {
                    if (participantfloorAction == 0) {
                        if (validateAudioVideo())
                            enxRooms.requestFloor();
                        else
                            showTextMessage(getResources().getString(R.string.error_msg_raise_hand), true);
                    } else if (participantfloorAction == 1) {
                        openFloorRequestDialog(participantfloorAction);
                    } else if (participantfloorAction == 2) {
                        openFloorRequestDialog(participantfloorAction);
                    }
                } else {
                    roomNotConnected();
                }
                return true;
            }

            else if(id== R.id.action_notification_badge) {
                if (enxRooms != null) {
                    if (floorRequestArrayList.size() > 0) {
                        openRequestedFloorDialogs();
                    } else {
                        showTextMessage("No Participants", false);
                    }
                } else {
                    roomNotConnected();
                }
                return true;




        }

        return super.onOptionsItemSelected(item);
    }
    private JSONObject getLocalStreamJsonObjet() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("audio", true);
            jsonObject.put("video", true);
            jsonObject.put("data", true);
            JSONObject videoSize = new JSONObject();
            videoSize.put("minWidth", 320);
            videoSize.put("minHeight", 180);
            videoSize.put("maxWidth", 1280);
            videoSize.put("maxHeight", 720);
            jsonObject.put("videoSize", videoSize);
            jsonObject.put("audioMuted", "false");
            jsonObject.put("videoMuted", "false");
            JSONObject attributes = new JSONObject();
            attributes.put("name", "myStream");
            jsonObject.put("attributes", attributes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void getPreviousIntent() {
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            name = getIntent().getStringExtra("name");
            role = getIntent().getStringExtra("role");
            roomId = getIntent().getStringExtra("roomId");
        }
    }

    @Override
    public void onRoomConnected(EnxRoom enxRoom, JSONObject jsonObject) {
//received when user connected with Enablex room
        enxRooms = enxRoom;
        if (enxRooms != null) {
            enxPlayerView = new EnxPlayerView(this, EnxPlayerView.ScalingType.SCALE_ASPECT_BALANCED, true);
            localStream.attachRenderer(enxPlayerView);
            moderator.addView(enxPlayerView);
            /*enxRooms.publish(localStream);*/
            enxRooms.setChairControlObserver(this);
            enxRooms.setReconnectObserver(this);
            enxRoom.setActiveTalkerViewObserver(this);
            try {
                new Handler(getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(role.equalsIgnoreCase("moderator"))
                            enxRoom.publish(localStream);
                    }
                }, 2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRoomError(JSONObject jsonObject) {
        //received when any error occurred while connecting to the Enablex room
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onUserConnected(JSONObject jsonObject) {
        // received when a new remote participant joins the call
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        userArrayList.add(userModel);
    }

    @Override
    public void onUserDisConnected(JSONObject jsonObject) {
        // received when a  remote participant left the call
        UserModel userModel = gson.fromJson(jsonObject.toString(), UserModel.class);
        for (UserModel userModel1 : userArrayList) {
            if (userModel1.getClientId().equalsIgnoreCase(userModel.getClientId())) {
                userArrayList.remove(userModel);
            }
        }
    }

    @Override
    public void onPublishedStream(EnxStream enxStream) {
        //received when audio video published successfully to the other remote users
    }

    @Override
    public void onUnPublishedStream(EnxStream enxStream) {
//received when audio video unpublished successfully to the other remote users
    }

    @Override
    public void onStreamAdded(EnxStream enxStream) {
        //received when a new stream added
        if (enxStream != null) {
            enxRooms.subscribe(enxStream);
        }
    }

    @Override
    public void onSubscribedStream(EnxStream enxStream) {
        //received when a remote stream subscribed successfully
    }

    @Override
    public void onUnSubscribedStream(EnxStream enxStream) {
//received when a remote stream unsubscribed successfully
    }

    public void onRoomDisConnected(JSONObject jsonObject) {
        //received when Enablex room successfully disconnected
        this.finish();
    }

    @Override
    public void onActiveTalkerView(RecyclerView recyclerView) {

        mRecyclerView = recyclerView;
        if (recyclerView == null) {
            participant.removeAllViews();
            dummyText.setVisibility(View.VISIBLE);

        } else {
            dummyText.setVisibility(View.GONE);
            participant.removeAllViews();
            participant.addView(recyclerView);

        }

        if (touch) {
            return;
        }
        if (mRecyclerView != null) {
            touch = true;
            mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                    if (motionEvent.getAction() == 1) {
                        handleTouchListner();
                    }
                    return false;
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean b) {

                }
            });
        }

    }

    @Override
    public void onActiveTalkerView(RecyclerView recyclerView, EnxRoom enxRoom) {

    }

    @Override
    public void onAvailable(Integer integer) {

    }

    @Override
    public void onEventError(JSONObject jsonObject) {
//received when any error occurred for any room event
        Toast.makeText(VideoConferenceActivity.this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEventInfo(JSONObject jsonObject) {
// received for different events update
    }

    @Override
    public void onNotifyDeviceUpdate(String s) {
// received when when new media device changed
    }

    @Override
    public void onAcknowledgedSendData(JSONObject jsonObject) {
// received your chat data successfully sent to the other end
    }

    @Override
    public void onMessageReceived(JSONObject jsonObject) {
// received when chat data received at room
    }

    @Override
    public void onACKSendMessage(JSONObject jsonObject) {

    }

    @Override
    public void onMessageDelete(JSONObject jsonObject) {

    }

    @Override
    public void onACKDeleteMessage(JSONObject jsonObject) {

    }

    @Override
    public void onMessageUpdate(JSONObject jsonObject) {

    }

    @Override
    public void onACKUpdateMessage(JSONObject jsonObject) {

    }


    @Override
    public void onUserDataReceived(JSONObject jsonObject) {
// received when custom data received at room
    }

    @Override
    public void onUserStartTyping(JSONObject jsonObject) {

    }




    @Override
    public void onConferencessExtended(JSONObject jsonObject) {

    }

    @Override
    public void onConferenceRemainingDuration(JSONObject jsonObject) {

    }

    @Override
    public void onAckDropUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckDestroy(JSONObject jsonObject) {

    }

    private boolean getAudioOnly(String str) {
        if (str.equalsIgnoreCase("audio") || str.equalsIgnoreCase("audioOnly")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onAudioEvent(JSONObject jsonObject) {
        //received when audio mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Audio On")) {
                mute.setImageResource(R.drawable.unmute);
                isAudioMuted = false;
            } else if (message.equalsIgnoreCase("Audio Off")) {
                mute.setImageResource(R.drawable.mute);
                isAudioMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoEvent(JSONObject jsonObject) {
        //received when video mute/unmute happens
        try {
            String message = jsonObject.getString("msg");
            if (message.equalsIgnoreCase("Video On")) {
                video.setImageResource(R.drawable.ic_videocam);
                isVideoMuted = false;
            } else if (message.equalsIgnoreCase("Video Off")) {
                video.setImageResource(R.drawable.ic_videocam_off);
                isVideoMuted = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedData(JSONObject jsonObject) {
//received when chat data received at room level
    }

    @Override
    public void onRemoteStreamAudioMute(JSONObject jsonObject) {
//received when any remote stream mute audio
    }

    @Override
    public void onRemoteStreamAudioUnMute(JSONObject jsonObject) {
//received when any remote stream unmute audio
    }

    @Override
    public void onRemoteStreamVideoMute(JSONObject jsonObject) {
//received when any remote stream mute video
    }

    @Override
    public void onRemoteStreamVideoUnMute(JSONObject jsonObject) {
//received when any remote stream unmute video
    }

    @Override
    public void onAckPinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckUnpinUsers(JSONObject jsonObject) {

    }

    @Override
    public void onPinnedUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomAwaited(EnxRoom enxRoom, JSONObject jsonObject) {
        
    }

    @Override
    public void onUserAwaited(JSONObject jsonObject) {

    }

    @Override
    public void onAckForApproveAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckForDenyAwaitedUser(JSONObject jsonObject) {

    }

    @Override
    public void onAckAddSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onAckRemoveSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onUpdateSpotlightUsers(JSONObject jsonObject) {

    }

    @Override
    public void onRoomBandwidthAlert(JSONObject jsonObject) {

    }

    @Override
    public void onStopAllSharingACK(JSONObject jsonObject) {

    }



    @Override
    public void onClick(View view) {
        int id = view.getId();

            if(id== R.id.disconnect) {
                if (enxRooms != null && enxRooms.isConnected()) {
                    if (enxPlayerView != null) {
                        enxPlayerView.release();
                        enxPlayerView = null;
                    }
                    enxRooms.disconnect();
                } else {
                    finish();
                }
            }
            else if(id== R.id.mute) {
                if (localStream != null) {
                    if (!isAudioMuted) {
                        localStream.muteSelfAudio(true);
                    } else {
                        localStream.muteSelfAudio(false);
                    }
                }
            }
            else if(id== R.id.video) {
                if (localStream != null) {
                    if (!isVideoMuted) {
                        localStream.muteSelfVideo(true);
                    } else {
                        localStream.muteSelfVideo(false);
                    }
                }
            }
            else if(id== R.id.camera) {
                if (localStream != null) {
                    if (!isVideoMuted) {
                        if (isFrontCamera) {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.rear_camera);
                            isFrontCamera = false;
                        } else {
                            localStream.switchCamera();
                            camera.setImageResource(R.drawable.front_camera);
                            isFrontCamera = true;
                        }
                    } else {
                        Toast.makeText(VideoConferenceActivity.this, "Please turn on the video to switch camera.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if(id== R.id.volume) {
                if (enxRooms != null) {
                    showRadioButtonDialog();
                }
            }
            if(id==R.id.clickEvent) {
                if (enxRooms != null) {
                    if (participantfloorAction == 1) {
                        enxRooms.cancelFloor();
                    } else {
                        enxRooms.finishFloor();
                    }
                    participantfloorAction = 0;
                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                    }
                } else {
                    roomNotConnected();
                }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                } else {
                    Toast.makeText(this, "Please enable permissions to further proceed.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void handleTouchListner() {
        if (touchView) {
            bottomView.setVisibility(View.VISIBLE);
            touchView = false;
        } else {
            bottomView.setVisibility(View.GONE);
            touchView = true;
        }
    }

    private void showRadioButtonDialog() {

        // custom dialog
        final Dialog dialog = new Dialog(VideoConferenceActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.radiogroup);
        List<String> stringList = new ArrayList<>();  // here is list

        List<String> deviceList = enxRooms.getDevices();
        for (int i = 0; i < deviceList.size(); i++) {
            stringList.add(deviceList.get(i));
        }
        RadioGroup rg = (RadioGroup) dialog.findViewById(R.id.radio_group);
        String selectedDevice = enxRooms.getSelectedDevice();
        if (selectedDevice != null) {
            for (int i = 0; i < stringList.size(); i++) {
                RadioButton rb = new RadioButton(VideoConferenceActivity.this); // dynamically creating RadioButton and adding to RadioGroup.
                rb.setText(stringList.get(i));
                rg.addView(rb);
                if (selectedDevice.equalsIgnoreCase(stringList.get(i))) {
                    rb.setChecked(true);
                }

            }
            dialog.show();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int childCount = group.getChildCount();
                for (int x = 0; x < childCount; x++) {
                    RadioButton btn = (RadioButton) group.getChildAt(x);
                    if (btn.getId() == checkedId) {
                        enxRooms.switchMediaDevice(btn.getText().toString());
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (enxRooms != null) {
            enxRooms = null;
        }
        if (enxRtc != null) {
            enxRtc = null;
        }
    }

    public JSONObject getReconnectInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("allow_reconnect", true);
            jsonObject.put("number_of_attempts", 3);
            jsonObject.put("timeout_interval", 15);
            jsonObject.put("activeviews", "view");//view

            JSONObject object = new JSONObject();
            object.put("audiomute", true);
            object.put("videomute", true);
            object.put("bandwidth", true);
            object.put("screenshot", true);
            object.put("avatar", true);

            object.put("iconColor", getResources().getColor(R.color.colorPrimary));
            object.put("iconHeight", 30);
            object.put("iconWidth", 30);
            object.put("avatarHeight", 200);
            object.put("avatarWidth", 200);
            jsonObject.put("playerConfiguration", object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onReconnect(String message) {
        // received when room tries to reconnect due to low bandwidth or any connection interruption
        try {
            if (message.equalsIgnoreCase("Reconnecting")) {
                progressDialog.setMessage("Wait, Reconnecting");
                progressDialog.show();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUserReconnectSuccess(EnxRoom enxRoom, JSONObject jsonObject) {
        // received when reconnect successfully completed
        try {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    EnxPlayerView playerView = list.get(i).getEnxPlayerView();
                    EnxStream enxStream = list.get(i).getEnxStream();
                    if (playerView != null) {
                        playerView.release();
                    }
                    if (enxStream != null) {
                        enxStream.detachRenderer();
                    }
                }
                list.removeAll(list);
                list = null;
            }
            Toast.makeText(this, "Reconnect Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void roomNotConnected() {
        Toast.makeText(this,"Please Wait! Room is connecting", Toast.LENGTH_LONG).show();
    }

    int participantfloorAction;
    boolean isFloorRequestIsInProgress;
    @Override
    public void onFloorRequested(JSONObject jsonObject) {
         isFloorRequestIsInProgress = true;
        Log.e("onFloorRequested", jsonObject.toString());
        try {
            final int result = jsonObject.getInt("result");
            switch (result) {
                case 1719:
                    showTextMessage(jsonObject.optString("msg"), true);
                    break;
                case 1718:
                    showTextMessage(jsonObject.optString("msg"), true);
                    break;
                case 1701:
                case 0:
                case 1702:
                    participantfloorAction = 1;
                    menu.findItem(R.id.raisehand).setIcon(R.drawable.hand_raised);
                    showTextMessage(jsonObject.optString("msg"), false);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onFloorRequestReceived(JSONObject jsonObject) {
        Log.e("onFloorRequestReceived", jsonObject.toString());
        FloorRequestModel floorRequestModel = new FloorRequestModel();
        floorRequestModel.setClientId(jsonObject.optString("clientId"));
        floorRequestModel.setClientName(jsonObject.optString("name"));
        floorRequestModel.setRequestAccepted(true);
        floorRequestModel.setRequestRejected(true);
        floorRequestModel.setRequestReleased(false);
        floorRequestArrayList.add(floorRequestModel);
        adapter.notifyDataSetChanged();
        setupBadge();
        showTextMessage(jsonObject.optString("name") + " is requested to grant or deny", false);
    }

    @Override
    public void onFloorCancelled(JSONObject jsonObject) {
        isFloorRequestIsInProgress = false;
        menu.findItem(R.id.raisehand).setIcon(R.drawable.raise_hand);
        showTextMessage(jsonObject.optString("msg"), false);
    }

    @Override
    public void onFloorFinished(JSONObject jsonObject) {
        menu.findItem(R.id.raisehand).setIcon(R.drawable.raise_hand);
        showTextMessage(jsonObject.optString("msg"), false);
        moderator.setVisibility(View.GONE);
        moderator.removeView(localStream.mEnxPlayerView);
    }

    @Override
    public void onProcessFloorRequested(JSONObject jsonObject) {
        Log.e("onProcessFloorRequested", jsonObject.toString());
        try {
            int result = jsonObject.optInt("result");
            String msg = jsonObject.getString("msg");
            JSONObject request = jsonObject.getJSONObject("request");
            JSONObject params = request.getJSONObject("params");
            String action = params.getString("action");

            if (floorRequestArrayList.size() > currentClientPosition) {
                floorRequestModel = floorRequestArrayList.get(currentClientPosition);

                if (action.equalsIgnoreCase("grantFloor") && result == 0) {
                    floorRequestModel.setRequestAccepted(false);
                    floorRequestModel.setRequestRejected(false);
                    floorRequestModel.setRequestReleased(true);
                    floorRequestModel.setMute(true);
                    floorRequestModel.setUnMute(false);
                    adapter.notifyDataSetChanged();
                    if (dialog != null)
                        dialog.dismiss();
                } else if ((action.equalsIgnoreCase("denyFloor") && result == 0) ||
                        (action.equalsIgnoreCase("releaseFloor") && result == 0) ||
                        result == 1711 || result == 1707 || result == 1709 || result == 1712) {
                    floorRequestArrayList.remove(floorRequestModel);
                    adapter.notifyDataSetChanged();
                    setupBadge();
                    if (dialog != null)
                        dialog.dismiss();

                }
            }
            showTextMessage(action + ": " + msg, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGrantedFloorRequest(JSONObject jsonObject) {
        try {
            isFloorRequestIsInProgress = false;
            if (role != null && role.equalsIgnoreCase("moderator")) {
                onGrantDenyRealeaseManagement(jsonObject);
            } else {
                participantfloorAction = 2;
                //startCountDownAnimation();
                showTextMessage(jsonObject.getString("msg"), true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeniedFloorRequest(JSONObject jsonObject) {
        try {
            isFloorRequestIsInProgress = false;
            if (role != null && role.equalsIgnoreCase("moderator")) {
                onGrantDenyRealeaseManagement(jsonObject);
            } else {
                participantfloorAction = 0;
                menu.findItem(R.id.raisehand).setIcon(R.drawable.raise_hand);
                showTextMessage(jsonObject.optString("msg"), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReleasedFloorRequest(JSONObject jsonObject) {
        try {

            if (role != null && role.equalsIgnoreCase("moderator")) {
                onGrantDenyRealeaseManagement(jsonObject);
            } else {
                participantfloorAction = 0;
                moderator.setVisibility(View.GONE);
                localStream.mEnxPlayerView.setZOrderMediaOverlay(false);
                moderator.removeView(localStream.mEnxPlayerView);
                showTextMessage(jsonObject.optString("msg"), true);
                menu.findItem(R.id.raisehand).setIcon(R.drawable.raise_hand);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onCancelledFloorRequest(JSONObject jsonObject) {

    }

    @Override
    public void onFinishedFloorRequest(JSONObject jsonObject) {

    }

    @Override
    public void onACKInviteToFloorRequested(JSONObject jsonObject) {

    }

    @Override
    public void onInviteToFloorRequested(JSONObject jsonObject) {

    }

    @Override
    public void onInvitedForFloorAccess(JSONObject jsonObject) {

    }

    @Override
    public void onCanceledFloorInvite(JSONObject jsonObject) {

    }

    @Override
    public void onRejectedInviteFloor(JSONObject jsonObject) {

    }

    @Override
    public void onAcceptedFloorInvite(JSONObject jsonObject) {

    }


    private void showTextMessage(String msg, boolean value) {
        if (value) {
            Toast.makeText(VideoConferenceActivity.this, msg, Toast.LENGTH_SHORT).show();
            return;
        }
    }
    private void onGrantDenyRealeaseManagement(JSONObject jsonObject) {
        try {
            int result = jsonObject.optInt("result");
            String msg = jsonObject.getString("msg");

            if (floorRequestArrayList.size() > currentClientPosition) {
                floorRequestModel = floorRequestArrayList.get(currentClientPosition);

                if (result == 1708) {
                    floorRequestModel.setRequestAccepted(false);
                    floorRequestModel.setRequestRejected(false);
                    floorRequestModel.setRequestReleased(true);
                    floorRequestModel.setMute(true);
                    floorRequestModel.setUnMute(false);
                    adapter.notifyDataSetChanged();
                    if (dialog != null)
                        dialog.dismiss();
                } else if (result == 1709 || result == 1712 || result == 1711 || result == 1707) {
                    floorRequestArrayList.remove(floorRequestModel);
                    adapter.notifyDataSetChanged();
                    setupBadge();
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
            showTextMessage(msg, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupBadge() {
        if (role.equalsIgnoreCase("moderator")) {
            notificationMenuItem.setVisible(true);
        }
        if (floorRequestArrayList != null && floorRequestArrayList.size() > 0) {
            textNotificationCount.setVisibility(View.VISIBLE);
            textNotificationCount.setText(String.valueOf(floorRequestArrayList.size()));
        } else {
            textNotificationCount.setVisibility(View.GONE);
        }
    }
    public boolean validateAudioVideo(){// return false to stop raise hand
        if(isAudioMuted)
            return false;
        else
            return true;
    }
    private void openFloorRequestDialog(int state) {
        dialog = new Dialog(VideoConferenceActivity.this);
        dialog.setContentView(R.layout.floor_request_dialog);
        TextView tv = (TextView) dialog.findViewById(R.id.TV);
        TextView name = (TextView) dialog.findViewById(R.id.clickEvent);
        if(state == 1){
            tv.setText("You want to cancel floor request");
            name.setText("Cancel");
        }else{
            tv.setText("You want to finish floor request");
            name.setText("Finish");
        }
        name.setOnClickListener(this::onClick);
        dialog.show();
    }
    private void openRequestedFloorDialogs() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Participants:");

        RecyclerView listView = (RecyclerView) dialog.findViewById(R.id.dialoglistview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(linearLayoutManager);
        listView.setAdapter(adapter);
        dialog.show();
    }

    @Override
    public void onFloorRequestAccepted(int position) {
        Log.e("onFloorRequestAccepted", String.valueOf(position));
        currentClientPosition = position;
        enxRooms.grantFloor(floorRequestArrayList.get(position).getClientId());
    }

    @Override
    public void onFloorRequestRejected(int position) {
        Log.e("onFloorRequestRejected", String.valueOf(position));
        currentClientPosition = position;
        enxRooms.denyFloor(floorRequestArrayList.get(position).getClientId());
    }

    @Override
    public void onFloorRequestRevoked(int position) {
        currentClientPosition = position;
        enxRooms.releaseFloor(floorRequestArrayList.get(position).getClientId());
    }
}
