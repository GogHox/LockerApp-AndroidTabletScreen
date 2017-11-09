//package com.test;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.util.Log;
//import android.view.View;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.ss.testserial.Startwrite;
//
//public class MainActivity extends AppCompatActivity {
//    private Button[] btnArray = null;
//    CheckBox[] checkBoxArray = null;
//    TextView edBoardAddr;
//    Button btn1;
//    Button btn2;
//    private Startwrite ss;
//    private static final int MSG_CHECK_STATE = 1;
//    private static final int MSG_OPEN_ALLDOOR = 2;
//    int[] stateBuf = new int[7];
//
//    private int index = 0;
//    InputThread mInputThread;
//    boolean bOpenAll = false;
//
//    private Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.arg1) {
//                case MSG_CHECK_STATE:
//                    showDoorState();
//                    break;
//                case MSG_OPEN_ALLDOOR:
//
//                    break;
//                default:
//
//                    break;
//            }
//        }
//    };
//
//    private class InputThread extends Thread {
//        @Override
//        public void run() {
//            Log.i("door test##########", "start thread-------------------");
//            index = -1;
//            int ret = 0;
//            while (!isInterrupted()) {
//                try {
//                    Thread.sleep(100);
//
//                    if (bOpenAll) {
//                        index++;
//                        index = index % 24;
//                        openDoor(index);
////						CheckState();
////						Message msginMessage = new Message();
////						msginMessage.arg1 = MSG_CHECK_STATE;
////						handler.sendMessage(msginMessage);
////						Message msgoutMessage = new Message();
////						msgoutMessage.arg1 = MSG_OPEN_ALLDOOR;
////						msgoutMessage.arg2 = ret;
////						handler.sendMessage(msgoutMessage);
//                    }
//                    {
//                        CheckState();
//                        Message msginMessage = new Message();
//                        msginMessage.arg1 = MSG_CHECK_STATE;
//                        handler.sendMessage(msginMessage);
//                    }
//
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    public boolean openDoor(int doorIndex) {
//        try {
//            int boardNum = 0;
//            boardNum += checkBoxArray[0].isChecked() ? 1 : 0;
//            boardNum += checkBoxArray[1].isChecked() ? 2 : 0;
//            boardNum += checkBoxArray[2].isChecked() ? 4 : 0;
//            boardNum += checkBoxArray[3].isChecked() ? 8 : 0;
//            if (boardNum <= 0) {
//                return false;
//            }
//
//            int[] buf = new int[7];
//            synchronized (ss) {
//                ss.getDoorState(boardNum, doorIndex + 1, buf);
//            }
//            if (buf[3] == 0) {
//                int[] retbuf = new int[7];
//                synchronized (ss) {
//                    ss.openGrid(boardNum, doorIndex + 1, retbuf);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return true;
//    }
//
//    private boolean showDoorState() {
//        for (int i = 0; i < 3; i++) {
//            int state = (int)stateBuf[4 - i];
//            int t = 1;
//            for (int j = 0; j < 8; j++) {
//                int doorIndex = i * 8 + j;
//                if (  (t & state) >0 ) {
//                    btnArray[doorIndex].setBackgroundColor(Color.GREEN);
//                } else {
//                    btnArray[doorIndex].setBackgroundColor(Color.DKGRAY);
//                }
//                t *= 2;
//            }
//        }
//        return true;
//    }
//
//    private boolean CheckState() {
////		Log.i("door test##########","CheckState-------------------");
//        try {
//            int boardNum = 0;
//            boardNum += checkBoxArray[0].isChecked() ? 1 : 0;
//            boardNum += checkBoxArray[1].isChecked() ? 2 : 0;
//            boardNum += checkBoxArray[2].isChecked() ? 4 : 0;
//            boardNum += checkBoxArray[3].isChecked() ? 8 : 0;
//            if (boardNum <= 0) {
//                return false;
//            }
////			Log.i("door test##########","CheckState-------------------boardNum="+boardNum);
//
//
//            synchronized (ss) {
//                ss.getDoorState(boardNum, 0, stateBuf);
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        super.onCreate(savedInstanceState);
//
//        btnArray = new Button[24];
//        ss = new Startwrite();
//        ss.uartInit();
//
//        final LinearLayout layout = new LinearLayout(this);
//
//        layout.setOrientation(LinearLayout.VERTICAL);
//
//        final LinearLayout ctlPan = new LinearLayout(this);
//        ctlPan.setPadding(20, 5, 20, 5);
//        ctlPan.setOrientation(LinearLayout.HORIZONTAL);
//        TextView bk = new TextView(this);
//        bk.setText("版卡地址：");
//        ctlPan.addView(bk);
//        checkBoxArray = new CheckBox[4];
//        for (int i = 0; i < 4; i++) {
//            checkBoxArray[i] = new CheckBox(this);
//            ctlPan.addView(checkBoxArray[i]);
//        }
//        btn1 = new Button(this);
//        btn1.setText("循环打开锁");
//        btn1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bOpenAll = true;
//                btn1.setEnabled(false);
//                btn2.setEnabled(true);
//            }
//        });
//        btn2 = new Button(this);
//        btn2.setText("关闭循环打开锁");
//        btn2.setEnabled(false);
//        btn2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bOpenAll = false;
//                btn1.setEnabled(true);
//                btn2.setEnabled(false);
//            }
//        });
//
//        TextView aa = new TextView(this);
//        aa.setText("              ");
//        ctlPan.addView(aa);
//        ctlPan.addView(btn1);
//        ctlPan.addView(btn2);
//
//        layout.addView(ctlPan);
//
//        final LinearLayout firstＲow = new LinearLayout(this);
//        firstＲow.setOrientation(LinearLayout.HORIZONTAL);
//        firstＲow.setPadding(20, 5, 5, 5);
//        index = 0;
//        for (; index < 12; index++) {
//            btnArray[index] = new Button(this);
//            if (index < 9)
//                btnArray[index].setText("锁-0" + (index + 1));
//            else
//                btnArray[index].setText("锁-" + (index + 1));
//            btnArray[index].setBackgroundColor(Color.DKGRAY);
//            firstＲow.addView(btnArray[index]);
//            btnArray[index].setOnClickListener(new View.OnClickListener() {
//                int btnindex = index;
//
//                @Override
//                public void onClick(View v) {
//                    openDoor(btnindex);
//                    // Drawable background = btnArray[btnindex].getBackground();
//                    // ColorDrawable colorDrawable = (ColorDrawable) background;
//                    // int color = colorDrawable.getColor();
//                    // if(Color.DKGRAY == color)
//                    // {
//                    // cotrlDoor(btnindex,true);
//                    // }
//                    // else {
//                    // cotrlDoor(btnindex,false);
//                    // }
//                }
//            });
//            TextView a = new TextView(this);
//            a.setText("  ");
//            firstＲow.addView(a);
//        }
//
//        final LinearLayout secondＲow = new LinearLayout(this);
//        secondＲow.setOrientation(LinearLayout.HORIZONTAL);
//        secondＲow.setPadding(20, 5, 5, 5);
//        for (; index < 24; index++) {
//            btnArray[index] = new Button(this);
//            btnArray[index].setText("锁-" + (index + 1));
//            btnArray[index].setBackgroundColor(Color.DKGRAY);
//            secondＲow.addView(btnArray[index]);
//            btnArray[index].setOnClickListener(new View.OnClickListener() {
//                int btnindex = index;
//
//                @Override
//                public void onClick(View v) {
//                    openDoor(btnindex);
//                    // Drawable background = btnArray[btnindex].getBackground();
//                    // ColorDrawable colorDrawable = (ColorDrawable) background;
//                    // int color = colorDrawable.getColor();
//                    // if(Color.DKGRAY == color)
//                    // {
//                    // cotrlDoor(btnindex,true);
//                    // }
//                    // else if(Color.GREEN == color){
//                    // cotrlDoor(btnindex,false);
//                    // }
//                }
//            });
//            TextView a = new TextView(this);
//            a.setText("  ");
//            secondＲow.addView(a);
//        }
//
//        layout.addView(firstＲow);
//        layout.addView(secondＲow);
//
//        setContentView(layout);
//
//        mInputThread = new InputThread();
//        mInputThread.start();
//    }
//
//}
