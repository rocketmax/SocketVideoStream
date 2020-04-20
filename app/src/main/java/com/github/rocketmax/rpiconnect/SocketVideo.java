package com.github.rocketmax.rpiconnect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.telecom.Call;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class SocketVideo {

    private Socket socket;

    private final int PORT;
    private final String HOST;

    private boolean socket_open = false;
    private boolean streaming = false;

    private Context context;


    SocketVideo(Context ctx, String host, int port){
        this.context = ctx;
        this.PORT = port;
        this.HOST =  host;
    }


    public void startSocket(){
        if(!socket_open) {
            new SocketConnect().execute();
        }
    }

    public void stopSocket(){
        if(socket_open) {
            socket_open = false;
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public boolean startStream(){
        if(socket_open && !streaming) {
            streaming = true;
            new Thread(new ReceiveTask()).start();
            return true;
        }
        return false;
    }

    public boolean endStream(){
        if(socket_open && streaming) {
            streaming = false;
            return true;
        }
        return false;
    }

    private class SocketConnect extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... voids) {
            Boolean success = false;
            try {
                InetAddress serverAddr = InetAddress.getByName(HOST);

                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr, PORT), 0);
                success = true;

            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return success;
        }

        protected void onPostExecute(Boolean success) {
            if(success){
                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                socket_open = true;
            }
            else{
                Toast.makeText(context, "Error Connecting", Toast.LENGTH_SHORT).show();
                socket_open = false;
            }
        }
    }

    public class ReceiveTask implements Runnable{
        private Exception exception;
        boolean err = false;

        @Override
        public void run(){
            while(!err && streaming) {
                try {
                    String msg = "ack";
                    PrintWriter out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

                    out.println(msg);

                    msg = in.readLine();
                    int size = Integer.parseInt(msg);
                    out.println(msg);

                    InputStream is = socket.getInputStream();
                    byte[] temp = new byte[size];
                    final byte[] bimg = new byte[size];

                    int bytesRead = 0;
                    int read;
                    while (bytesRead < size) {
                        read = is.read(temp);
                        System.arraycopy(temp, 0, bimg, bytesRead, read);
                        bytesRead += read;
                        Log.i("BYTES RECEIVED", Integer.toString(bytesRead));
                    }
                    Log.i("BYTES RECEIVED", Integer.toString(bytesRead));

                    final Bitmap bm = BitmapFactory.decodeByteArray(bimg, 0, bimg.length);

                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView iv = (ImageView) ((Activity)context).findViewById(R.id.iv);
                            iv.setImageBitmap(Bitmap.createScaledBitmap(bm, 960, 540, false));
                        }
                    });

                } catch (Exception e) {
                    this.exception = e;
                    e.printStackTrace();
                    err = true;
                }
            }

            streaming = false;
        }
    }
}
