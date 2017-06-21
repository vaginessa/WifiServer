package com.zmm.wifiserver.server;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SocketServer {


    static final String TAG = "SocketServer";
    private SConnectListener mListener;
    Socket socket;

    private ServerHandler handler = new ServerHandler();
    private ServerSocket serverSocket;

    /**
     * 用来保存不同的客户端
     */
//    private static Map<String, Socket> socketClients = new LinkedHashMap<>();
    private static ArrayList<Socket> socketClients = new ArrayList<>();

    public void createServer() {

        new ServerListener().run();

    }

    /**
     * 释放端口
     *
     * @throws IOException
     */
    public void closeSocket() throws IOException {
        Log.i(TAG,"施放端口");
        if (serverSocket != null) {
            serverSocket.close();
            serverSocket=new ServerSocket();
        } else {

        }
    }

    /**
     * 监听客户端连接的线程
     */
    class ServerListener extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(4000);
                serverSocket = new ServerSocket(12345);//默认端口为12345
                Log.i(TAG, "---服务端开启---");
                while (true) {
                    socket = serverSocket.accept();
                    new HandleThread(socket).start();
                    socketClients.add(socket);
                    Log.i(TAG, "有客户端连接到了本机的12345端口");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理来自客户端的消息
     *
     * @param data 未经处理的消息
     * @throws IOException
     */
    private void handleMessageFromClient(String data) throws IOException {
        mListener.onSReceiveData(data);
    }

    class ServerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mListener.onClientConnected();
        }
    }

    class HandleThread extends Thread {

        private Socket socket;

        public HandleThread(Socket socket) {
            this.socket = socket;

        }

        @Override
        public void run() {
            try {

                Log.i(TAG, "新客户端连接");
                Message msg = new Message();
                handler.sendMessage(msg);

                //读取从客户端发送过来的数据
                InputStream inputStream = socket.getInputStream();
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println(inetAddress.getHostAddress());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {

                    String data = new String(buffer, 0, len);

                    if (mListener != null) {

                        System.out.println("data = "+data);
                        handleMessageFromClient(data);
                    } else {
                        Log.i(TAG, "server_null");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * 发送消息给客户端
     *
     * @param msg  消息内容
     * @throws IOException
     */
    public void sendMessageToClient(String msg) throws IOException {

        if (mListener != null) {
            mListener.onSNotify(msg);
        } else {
            Log.i(TAG, "服务器自己显示消息出错");
        }

        try {
            Log.i(TAG, "给一个客户端发送了消息：" + msg);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(msg.getBytes("utf-8"));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            socket.getOutputStream().flush();
            Log.i(TAG, "pipe broken??");
        }

    }

    /**
     * 设置监听器函数
     *
     * @param linstener
     */
    public void setOnSConnectListener(SConnectListener linstener) {
        this.mListener = linstener;
    }


    /**
     * 数据接收回调接口
     */
    public interface SConnectListener {
        void onSReceiveData(String data);

        void onSNotify(String msg);

        void onClientConnected();
    }

}



