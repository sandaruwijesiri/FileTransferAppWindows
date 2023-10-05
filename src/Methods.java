import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
	
import javax.swing.DefaultListModel;
import javax.swing.JList;

public class Methods {
    
    public static void findDevices(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    String myip=InetAddress.getLocalHost().getHostAddress(); // IP of the PC in which the code is running/localhost 
                    if(myip.equals("127.0.0.1")){ 
                        System.out.println("This PC is not connected to any network!");
                    }else{
                        String subnet = myip.substring(0, myip.lastIndexOf(".")+1);
                        int timeout=1000;
                        for (int i=1;i<255;i++){
                            String host=subnet + i;
                            InetAddress inetAddress = InetAddress.getByName(host);
                            new Thread(new Runnable() {
                                
                                @Override
                                public void run(){
                                    try {
                                        if (inetAddress.isReachable(timeout)){
                                            App.ui.lm.addElement(host);
                                            System.out.println(/*inetAddress.getHostName() + ": " + */host + " : " + App.ui.lm.getSize());
                                        }
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                }catch(UnknownHostException h){
                    h.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "IPAddress: "  + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}
