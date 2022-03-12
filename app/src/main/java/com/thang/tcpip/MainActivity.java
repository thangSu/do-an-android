package com.thang.tcpip;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    Button btnConnect,btnSend;
    TextView textViewStatus,textViewMsg;
    EditText editTextIp,editTextPort,editTextMsg;
    LinearLayout linearLayoutConnect,layoutConnect;
    Socket socket;
    String SERVER_IP;
    int SERVER_PORT;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ánh xạ
        anhxa();
        textViewMsg.setMovementMethod(new ScrollingMovementMethod());
        //lấy giá trị trong SharedPreferences
        sharedPreferences=getSharedPreferences("ID",MODE_PRIVATE);
        editTextIp.setText(sharedPreferences.getString("IP",""));
        editTextPort.setText(sharedPreferences.getString("PO",""));
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg=editTextMsg.getText().toString();
                textViewMsg.append(msg+"\n");
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(btnConnect.getText().toString().equals("Connect")) {
                    textViewStatus.setText("");
                    SERVER_IP = editTextIp.getText().toString().trim();
                    SERVER_PORT = Integer.parseInt(editTextPort.getText().toString().trim());
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("IP",SERVER_IP);
                    editor.putString("PO",String.valueOf(SERVER_PORT));
                    editor.commit();
                    new Thread(new Thread1()).start();
                }else{
                    btnConnect.setText("Connect");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editTextMsg.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                }
            }
        });
    }
    private PrintWriter output;
    private BufferedReader input;
    private  void anhxa(){
        btnConnect=(Button) findViewById(R.id.buttonConnect);
        btnSend=(Button) findViewById(R.id.buttonSend);
        textViewMsg=(TextView) findViewById(R.id.textviewMsg);
        textViewStatus=(TextView) findViewById(R.id.textViewStatus);
        editTextIp=(EditText) findViewById(R.id.editTextTextIp);
        editTextMsg=(EditText) findViewById(R.id.editTextMsg);
        editTextPort=(EditText) findViewById(R.id.editTextPort);
        linearLayoutConnect=(LinearLayout) findViewById(R.id.linearLayoutConnect);
        layoutConnect=(LinearLayout) findViewById(R.id.LinearConnect);
    }
    class Thread1 implements Runnable {
        public void run() {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText("Connected\n");
                        btnConnect.setText("Disconnect");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message!= null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewMsg.append("server: " + message + "\n");
                            }
                        });
                    } else {
                        new Thread(new Thread1()).start();
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textViewMsg.append("client: " + message + "\n");
                    editTextMsg.setText("");
                }
            });
        }
    }
}