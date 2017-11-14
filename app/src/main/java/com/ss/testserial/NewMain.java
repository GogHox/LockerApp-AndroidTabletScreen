package com.ss.testserial;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import com.testapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import android.annotation.SuppressLint;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.androiddevelop.cycleviewpager.lib.CycleViewPager;
import cn.androiddevelop.cycleviewpager.lib.CycleViewPager.ImageCycleViewListener;


public class NewMain extends Activity implements View.OnClickListener {
    private static final int MSG_CHECK_STATE = 1;
    private static final int MSG_OPEN_ALLDOOR = 2;
    private static final int MSG_REQUEST_SUCCESS = 3;
    private static final int MSG_REQUEST_ERROR = 4;

    private static final String TAG = "NewMain";

    CheckBox[] checkBoxArray = null;
    private Startwrite ss;
    int[] stateBuf = new int[7];

    private int LockIndex = 0;
    InputThread mInputThread;

    boolean bOpenAllLock = false;

    private String resJson;
    private String mInputNumber;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case MSG_CHECK_STATE:
                    showDoorState();
                    break;
                case MSG_OPEN_ALLDOOR:
                    break;
                case MSG_REQUEST_SUCCESS:
                    confirmPwd(resJson, Integer.parseInt(mInputNumber));
                    //openDoor(Integer.valueOf(number));
                    break;
                case MSG_REQUEST_ERROR:
                    Toast.makeText(getApplication(), "You input PIN is error!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void confirmPwd(String resJson, int pwd) {
        try {
            JSONArray jsonArray = new JSONArray(resJson);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                int pin = jo.getInt("PIN");
                int nr = jo.getInt("nr");
                if(pin == pwd){
                    // PIN is correct
                    // open the door
                    openDoor(nr);
                    // function should return if open the door
                    return;
                }
            }
            // don't have door open
            mHandler.sendEmptyMessage(MSG_REQUEST_ERROR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * if bOpenAllLock equal true, will open all Lock.
     */
    // 一个后台线程，监听是否是开启所有的锁，如果bOpenAll是true则开启
    private class InputThread extends Thread {
        @Override
        public void run() {
            // Log.i("door test##########", "start thread-------------------");
            LockIndex = -1;
            int ret = 0;
            while (!isInterrupted()) {
                try {
                    Thread.sleep(100);
                    if (bOpenAllLock) {  // Open all lock
                        //对应多少个锁 Corresponds to how much lock（96）
                        LockIndex++;
                        LockIndex = LockIndex % 96;

                        // try to unlock
                        openDoor(LockIndex);
                        CheckState();
                        Message msginMessage = new Message();
                        msginMessage.arg1 = MSG_CHECK_STATE;
                        mHandler.sendMessage(msginMessage);
                        Message msgoutMessage = new Message();
                        msgoutMessage.arg1 = MSG_OPEN_ALLDOOR;
                        msgoutMessage.arg2 = ret;
                        mHandler.sendMessage(msgoutMessage);
                    }
                    {
                        // 持续检查板卡状态（native） always check board status (native method)
                        CheckState();
                        Message msginMessage = new Message();
                        msginMessage.arg1 = MSG_CHECK_STATE;
                        mHandler.sendMessage(msginMessage);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean openDoor(int doorIndex) {
        try {
            int boardNum = 0;
            boardNum += chb1.isChecked() ? 1 : 0;
            boardNum += chb2.isChecked() ? 2 : 0;
            boardNum += chb3.isChecked() ? 4 : 0;
            boardNum += chb4.isChecked() ? 8 : 0;
            if (boardNum <= 0) {
                return false;
            }
            int[] buf = new int[7];
            synchronized (ss) {
                //ss.getDoorState(boardNum, doorIndex + 1, buf);
                // don't need +1
                ss.getDoorState(boardNum, doorIndex, buf);
            }
            if (buf[3] == 0) {
                int[] retbuf = new int[7];
                int openDoorReturn;
                synchronized (ss) {

                    //openDoorReturn = ss.openGrid(boardNum, doorIndex + 1, retbuf);
                    // don't need +1
                    openDoorReturn = ss.openGrid(boardNum, doorIndex, retbuf);
                    Log.i(TAG, "openDoor: *****************" + openDoorReturn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean showDoorState() {
        for (int i = 0; i < 3; i++) {
            int state = (int) stateBuf[4 - i];
            int t = 1;
            for (int j = 0; j < 8; j++) {
                int doorIndex = i * 8 + j;
                t *= 2;
            }
        }
        return true;
    }

    private boolean CheckState() {
        Log.i("door test##########", "CheckState-------------------");
        try {
            int boardNum = 0;
            boardNum += chb1.isChecked() ? 1 : 0;
            boardNum += chb2.isChecked() ? 2 : 0;
            boardNum += chb3.isChecked() ? 4 : 0;
            boardNum += chb4.isChecked() ? 8 : 0;
            if (boardNum <= 0) {
                return false;
            }
            Log.i("door test##########", "CheckState-------------------boardNum=" + boardNum);
            synchronized (ss) {
                ss.getDoorState(boardNum, 0, stateBuf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, btndelete, btnenter;
    private EditText edtpassword;
    private CheckBox chb1, chb2, chb3, chb4;

    private List<ImageView> views = new ArrayList<ImageView>();
    private List<ADInfo> adInfoList = new ArrayList<ADInfo>();
    private CycleViewPager cycleViewPager;
    //下面是五张轮播图片的网络地址 Here are five rounds of the network address of the image
    private String[] imageUrls = {
            "http://img0.imgtn.bdimg.com/it/u=2799491813,588820357&fm=21&gp=0.jpg",
            "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1508003910275&di=c601b32bdd5b4b4229838260daa8548f&imgtype=0&src=http%3A%2F%2Fpic.58pic.com%2F58pic%2F11%2F19%2F16%2F73N58PICwxg.jpg",
            "http://img3.imgtn.bdimg.com/it/u=2414756802,531263205&fm=21&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=2280771936,3888134874&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=2327585548,1525749689&fm=21&gp=0.jpg",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //控制底部虚拟键盘   Control at the bottom of the virtual keyboard
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        initConfig();
        initView();
    }

    private void initConfig() {
        // 设置在主线程里直接运行子线程,下面主线程内不能用new thread
        // Set the child thread run directly in the main thread, the following main thread cannot be used in the new thread
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());

        // set activity view
        setContentView(R.layout.key);

        //  ss is native obj, using c/c++ lib
        ss = new Startwrite();
        ss.uartInit();

        // 初始化广告轮播图  init the scroll image.
        configImageLoader();
        initialize();

        // create a thread that is open all door when bOpenAll is true
        mInputThread = new InputThread();
        mInputThread.start();
    }

    private void initView() {
        (findViewById(R.id.btn_test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bOpenAllLock = true;
            }
        });
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);
        btndelete = (Button) findViewById(R.id.btndelete);
        btnenter = (Button) findViewById(R.id.btnenter);
        edtpassword = (EditText) findViewById(R.id.edtpassword);
        edtpassword.setInputType(InputType.TYPE_NULL);
        edtpassword.requestFocus();

        //数字键盘的按钮       Digital keyboard buttons
        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        btndelete.setOnClickListener(this);
        btnenter.setOnClickListener(this);

        //四个被隐藏的CheckBox     Four hidden CheckBox
        chb1 = (CheckBox) findViewById(R.id.chb1);
        chb2 = (CheckBox) findViewById(R.id.chb2);
        chb3 = (CheckBox) findViewById(R.id.chb3);
        chb4 = (CheckBox) findViewById(R.id.chb4);

        chb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxArray[0] = chb1;
            }
        });
        chb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxArray[1] = chb2;
            }
        });
        chb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxArray[2] = chb3;
            }
        });
        chb4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkBoxArray[3] = chb4;
            }
        });
    }

    // on click the button will be called
    @Override
    public void onClick(View v) {
        boolean isInputPIN = false;
        int inputNumber = 0;
        switch (v.getId()) {
            case R.id.button0:
                isInputPIN = true;
                inputNumber = 0;
                break;
            case R.id.button1:
                isInputPIN = true;
                inputNumber = 1;
                break;
            case R.id.button2:
                isInputPIN = true;
                inputNumber = 2;
                break;
            case R.id.button3:
                isInputPIN = true;
                inputNumber = 3;
                break;
            case R.id.button4:
                isInputPIN = true;
                inputNumber = 4;
                break;
            case R.id.button5:
                isInputPIN = true;
                inputNumber = 5;
                break;
            case R.id.button6:
                isInputPIN = true;
                inputNumber = 6;
                break;
            case R.id.button7:
                isInputPIN = true;
                inputNumber = 7;
                break;
            case R.id.button8:
                isInputPIN = true;
                inputNumber = 8;
                break;
            case R.id.button9:
                isInputPIN = true;
                inputNumber = 9;
                break;
            case R.id.btndelete:
                if (edtpassword.isFocused()) {
                    edtpassword = (EditText) findViewById(R.id.edtpassword);
                    String str = edtpassword.getText().toString();
                    if (str.length() > 0)
                        edtpassword.setText(str.substring(0, str.length() - 1));
                }
                break;
            case R.id.btnenter:
                //点击确认按钮获取EditText中的字符串，发送给服务器   Click on the button for the EditText string, sent to the server
                Log.i(TAG, "onClick: you click the confirm button");
                final String number = edtpassword.getText().toString().trim();
                if (number.equals("")) {  //判断获取的字符串是否为空   Whether for the string is empty
                    Toast.makeText(getApplicationContext(), "Please input the PIN!", Toast.LENGTH_SHORT).show();    // show information of "password input the PIN"
                    edtpassword.setText("");
                } else {
                    mInputNumber = number;
                    // TODO connect server to get the door number which should open
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String pwd = NetworkUtils.checkPwd();
                                if(pwd != null){
                                    resJson = pwd;
                                    mHandler.sendEmptyMessage(MSG_REQUEST_SUCCESS);
                                }else
                                    mHandler.sendEmptyMessage(MSG_REQUEST_ERROR);
                            } catch (IOException e) {
                                e.printStackTrace();
                                mHandler.sendEmptyMessage(MSG_REQUEST_ERROR);
                            }
                        }
                    }).start();
                }
                break;
        }
        if (isInputPIN) {
            if (edtpassword.isFocused()) {
                edtpassword = (EditText) findViewById(R.id.edtpassword);
                edtpassword.setText(edtpassword.getText().toString() + inputNumber);
                String str = edtpassword.getText().toString();
                edtpassword.setText(str);
            } else {
            }
        }
    }

    //图片轮播相关，包括设置图片内容   Images play continuously
    @SuppressLint("NewApi")
    private void initialize() {
        cycleViewPager = (CycleViewPager) getFragmentManager()
                .findFragmentById(R.id.fragment_cycle_viewpager_content);
        for (int i = 0; i < imageUrls.length; i++) {
            ADInfo info = new ADInfo();
            info.setUrl(imageUrls[i]);
            info.setContent("图片-->" + i);
            adInfoList.add(info);
        }
        // 将最后一个ImageView添加进来     Will be the last one ImageView added
        views.add(ViewFactory.getImageView(this, adInfoList.get(adInfoList.size() - 1).getUrl()));
        for (int i = 0; i < adInfoList.size(); i++) {
            views.add(ViewFactory.getImageView(this, adInfoList.get(i).getUrl()));
        }
        views.add(ViewFactory.getImageView(this, adInfoList.get(0).getUrl()));  // 将第一个ImageView添加进来     The first ImageView added
        cycleViewPager.setCycle(true);  // 设置循环，在调用setData方法前调用    Set up the cycle, before calling setData method call
        cycleViewPager.setData(views, adInfoList, mAdCycleViewListener);  // 在加载数据前设置是否循环    在装货前的数据集是循环
        cycleViewPager.setWheel(true);  //设置轮播    Set the picture looping
        cycleViewPager.setTime(2000);  // 设置轮播时间，默认5000ms     Set the picture playing time, the default 5000 ms
        cycleViewPager.setIndicatorCenter();  //设置圆点指示图标组居中显示，默认靠右    Set the dot indicates the icon group centered, right by default
    }

    // on click the image will be called
    private ImageCycleViewListener mAdCycleViewListener = new ImageCycleViewListener() {
        @Override
        public void onImageClick(ADInfo info, int position, View imageView) {
            if (cycleViewPager.isCycle()) {
                position = position - 1;
                Toast.makeText(NewMain.this, "position-->" + info.getContent(), Toast.LENGTH_SHORT) .show();
            }
        }
    };

    /**
     * 配置ImageLoder  Configuration ImageLoder
     */
    private void configImageLoader() {
        // 初始化ImageLoader    initialize ImageLoader
        @SuppressWarnings("deprecation")
        DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.icon_stub) // 设置图片下载期间显示的图片  Set the picture image during the download
                .showImageForEmptyUri(R.drawable.icon_empty) // 设置图片Uri为空或是错误的时候显示的图片    Set the image Uri is empty or wrong image
                .showImageOnFail(R.drawable.icon_error) // 设置图片加载或解码过程中发生错误显示的图片     Set the picture loaded or an error occurred in the process of decoding image
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中   Set the download images are cached in memory
                .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中    Sets whether download images are cached in SD card
                //.displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片    Set into a rounded picture
                .build(); // 创建配置过得DisplayImageOption对象     Create a configuration DisplayImageOption object

        // 加载图片到本地
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()  // Thread priority
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build(); // md5 code the file name
        ImageLoader.getInstance().init(config);
    }


}
