import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Andy on 2017/7/15.
 */

class TimeStore {
    private long lastSendTime;

    public synchronized long getLastSendTime() {
        return lastSendTime;
    }

    public synchronized void setLastSendTime(long lastSendTime) {
        this.lastSendTime = lastSendTime;

        System.out.println("The last send time" + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(lastSendTime)));
    }
}

class SendHeartbeat implements Runnable{
    //private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private TimeStore timeStore;
    public SendHeartbeat(/*ObjectInputStream ois,*/ ObjectOutputStream oos, TimeStore timeStore){
        //this.ois = ois;
        this.oos = oos;
        this.timeStore = timeStore;
    }

    @Override
    public void run(){
//        try{
//            while (true){
//                Thread.sleep(1000);
//                if ((System.currentTimeMillis() - timeStore.getLastSendTime()) >=10*1000){
//                    oos.writeByte(1);
//                    oos.flush();
//                    timeStore.setLastSendTime(System.currentTimeMillis());
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
}

class ReceiveMsgFromServer implements Runnable
{
    private Socket socket;
    private boolean flag = true;
    public ReceiveMsgFromServer(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            InputStream in = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            while (flag)
            {
                System.out.println(ois.readUTF());
            }
        }catch (Exception e)
        {
            e.printStackTrace();;
        }
    }
}

public class client {
    private static final String host = "127.0.1";
    private static final int port = 30000;

    public static void main(String[] args){
        Socket socket = new Socket();
        try{
            socket.connect(new InetSocketAddress(host, port));
            OutputStream out = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            //ObjectInputStream ois = new ObjectInputStream(input);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            DataOutputStream dos = new DataOutputStream(out);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            TimeStore timeStore = new TimeStore();

            new Thread(new SendHeartbeat(/*ois,*/oos,timeStore)).start();

            new Thread(new ReceiveMsgFromServer(socket)).start();

            String line = new String();
            String three = new String("3");
            String received = new String();

            while ((line = bufferedReader.readLine() ) != null){
                if (line.compareTo(three) == 0)
                {
                    oos.writeByte(3);
                    oos.flush();
                    File file = new File("d:\\20151129_160235.jpg");
                    FileInputStream fin = new FileInputStream(file);
                    byte[] sendByte = new byte[1024];
                    dos.writeUTF( file.getName());
                    int length = 0;
                    while ((length = fin.read(sendByte, 0, sendByte.length)) > 0)
                    {
                        dos.write(sendByte, 0, length);
                        dos.flush();
                    }
                    fin.close();
                }
                else
                {
                    oos.writeByte(2);
                    oos.writeUTF(line);
                    oos.flush();
                    timeStore.setLastSendTime(System.currentTimeMillis());
                }
            }
            bufferedReader.close();
            oos.close();
            dos.close();
        }catch(IOException e){
            System.out.println("An exception thrown when write data");
        }finally {
            try{
                socket.close();
            }catch (IOException e2){
                e2.printStackTrace();
            }
        }
    }
}
