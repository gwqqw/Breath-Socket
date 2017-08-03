import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Andy on 2017/7/15.
 */

class Worker implements Runnable{
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean flag = true;

    public Worker(Socket socket){
        try{
            this.socket = socket;

            in = socket.getInputStream();

            out = socket.getOutputStream();

            ois = new ObjectInputStream(in);

            oos  = new ObjectOutputStream(out);
        }catch(Exception e){
            System.out.println("worker constructor throws exception.");
        }

    }

    @Override
    public void run(){
        try{
            while(flag){
                int type = ois.readByte();
                if (type == 1){
                    System.out.println(" Receive breath message from" + socket.getInetAddress().getHostAddress());
                }else if(type == 2){
                    System.out.println(socket.getInetAddress().getHostAddress() + " says " + ois.readUTF());
                    oos.writeUTF("Message Recieved");
                    oos.flush();
                }else if(type == 3){
                    DataInputStream din = null;
                    FileOutputStream fos = null;
                    try {
                        System.out.println("Start to receive image file data...");
                        din = new DataInputStream(in);
                        fos = new FileOutputStream(new File("D:2_" + din.readUTF()));
                        byte[] inputByte = new byte[1024];
                        int length = 0;
                        while (true) {
                            if (din != null) {
                                length = din.read(inputByte, 0, inputByte.length);
                            }
                            if (length == -1) {
                                break;
                            }
                            System.out.println(length);
                            fos.write(inputByte, 0, length);
                            fos.flush();
                        }
                        System.out.println("Receiving image file is done.");
                    } catch (Exception ex){
                        ex.printStackTrace();;
                    }finally {
                        if (null != din){
                            din.close();
                        }
                        if (null != fos){
                            fos.close();
                        }
                    }
                }
            }
        }catch(EOFException e){
            System.out.println("Client is disconnected.");
            flag = false;
        }catch(IOException ioe){
            ioe.printStackTrace();
        }finally {
            if (socket != null){
                try{
                    ois.close();
                    oos.close();
                    socket.close();
                }catch (Exception e){

                }
            }
        }
    }
}

public class server
{
    public static void main(String[] args)
    {
        try
        {
            ServerSocket serverSocket = new ServerSocket(30000); // listen port 30000 of local machine
            //while(true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket.getInetAddress().getHostName() + " connects to server...");
                Worker worker = new Worker(socket);

                new Thread(worker).start();
            //}
        }catch(Exception e) {
            System.out.println("An exception is thrown from main thread.");
            e.printStackTrace();
        }
    }
}
