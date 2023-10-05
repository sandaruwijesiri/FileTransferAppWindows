import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JProgressBar;

public class App {
    

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    public static UI ui;
    private static int portNumber = 8085;
    private static int bufferSize = 100*1024;
    public static void main(String[] args) {
        
        ui = new UI(1000, 1000);
        ui.run();

    }

    public static void runSocketForFileSending(File[] files, String ipString){

        try(Socket socket = new Socket(ipString,portNumber)) {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());


            for(int i=0;i<files.length;++i){
                long start = System.currentTimeMillis();
                ui.filename.setText("Sending file " + (i+1) + " of " + files.length);
                sendFile(i+1, files.length, files[i].getName(), files[i].getAbsolutePath());
                System.out.println((System.currentTimeMillis()-start)/1000);
            }
            
            dataInputStream.close();
            dataInputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void runSocketForFileReceiving(){

        try(ServerSocket serverSocket = new ServerSocket(portNumber)){
            Socket clientSocket = serverSocket.accept();
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());

            int fileNumber=0;
            int totalFiles=1;
            while(fileNumber<totalFiles) {
                fileNumber = dataInputStream.readInt();
                totalFiles = dataInputStream.readInt();
                ui.filename.setText("Receiving file " + fileNumber + " of " + totalFiles);
                receiveFile();
            }

            dataInputStream.close();
            dataOutputStream.close();
            clientSocket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void sendFile(int fileNumber, int totalFiles, String fileName, String path) throws Exception{
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        
        dataOutputStream.writeInt(fileNumber);
        dataOutputStream.writeInt(totalFiles);

        long length = file.length();
        // send file size
        dataOutputStream.writeLong(length);
        dataOutputStream.writeChars(fileName + "\n");
        // break file into chunks
        byte[] buffer = new byte[bufferSize];
        int i=0;

        while ((bytes=fileInputStream.read(buffer))!=-1){
            dataOutputStream.write(buffer,0,bytes);
            dataOutputStream.flush();
            ++i;
            if(i%100 ==0){
                System.out.println(((float) buffer.length*i)*100/length + " % done");
                ui.progressBar.setValue((int) (((float) buffer.length*i)*100/length) );
            }

        }
        ui.progressBar.setValue(100);
        System.out.println("Over!");
        fileInputStream.close();
    }
    
    private static void receiveFile() throws Exception{
        int bytes = 0;
        
        long originalSize = dataInputStream.readLong();
        long sizeLeft = originalSize;     // read file size

        StringBuilder stringBuilder = new StringBuilder();
        char c;
        while((c=dataInputStream.readChar()) != '\n'){
            stringBuilder.append(c);
        }
        String fileName = stringBuilder.toString();
        System.out.println(stringBuilder.toString());

        String home = System.getProperty("user.home");
        File f = new File(home + "\\Downloads",stringBuilder.toString());
        String filePath = f.getAbsolutePath();
        System.out.println(filePath);
        for(int i=1;;i++){
            if(f.exists()){
                int index = fileName.lastIndexOf(".");
                String s = fileName.substring(0, index);
                f = new File(filePath.substring(0,filePath.lastIndexOf("\\")+1) + s + "(" + i + ")" + fileName.substring(index));
            }else{
                break;
            }
        }

        FileOutputStream fileOutputStream = new FileOutputStream(f.getAbsolutePath());

        int i=0;
        byte[] buffer = new byte[bufferSize];
        while (sizeLeft > 0 && (bytes = dataInputStream.read(buffer, 0, (int)Math.min(buffer.length, sizeLeft))) != -1) {
            fileOutputStream.write(buffer,0,bytes);
            sizeLeft -= bytes;      // read upto file size

            ++i;
            if(i%100 ==0){
                int progr = (int) (((float) (originalSize-sizeLeft))*100/originalSize);
                System.out.println(progr + " % done");
                ui.progressBar.setValue((int) (((float) (originalSize-sizeLeft))*100/originalSize) );
            }
        }
        ui.progressBar.setValue(100);
        fileOutputStream.close();
    }
}
