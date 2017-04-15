import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.MulticastSocket;
import java.io.IOException;
import java.net.DatagramPacket;

public class MulticastPeer {
    static InetAddress group;;
    static String host = "224.2.2.3";
    static int port = 6789;
    static MulticastSocket s;
    static String nickname = "anonymous";
    static Thread receiveThread = null;
    volatile static String message = "";

    public static void main(String[] args) {
        connect(args);

        while(true) {
            try {
                message = nickname + ": " + System.console().readLine();
//                System.out.print(message);
                if(message.equals(nickname + ": " + "close")) { switchGroup(); continue; }
                byte[] m = message.getBytes();
                DatagramPacket messageOut = new DatagramPacket(m, m.length, group, port);
                s.send(messageOut);
            } catch(IOException e) {
                System.out.println("Connection error, message was not sent.");
                try { Thread.sleep(1000); }
                catch (InterruptedException e1) { }
            }
        }
    }

    static void parseArgs(String[] args) throws Exception{
        for(int i=0, j=0; i<args.length; i++) {
            if(j<2) {
                if(args[i].charAt(0)=='-') j=2;
                if(j==0 && args[i].matches("^\\d+$")) j=1;
                if(j==0) host=args[i];
                if(j==1) port=Integer.parseInt(args[i]);
            }
            if(j%2==0) {
                //if(args[i].charAt(1)=='n') { }
                if(args[i].charAt(1)=='h') j=4;
            }
            if(j==3) nickname=args[i];
            if(j==5) throw new Exception("help");
            j+=1;
        }

    }

    static void connect(String[] args) {
        String usageMsg = "Usage: MulticastPeer [group_address] [port] [-n nickname] [-h]\n" +
                "\t group_address: Multicast group hostname or IP address\n" +
                "\t port: group port number\n" +
                "\t\t default: host=localhost, port=1555\n" +
                "\t nickname: nickname of the user\n" +
                "\t\t default: anonymous\n" +
                "\t -h: prints this help message";

        try {
            parseArgs(args);

            joinGroup();

        } catch (Exception e) {
            System.out.println(usageMsg);
            //e.printStackTrace();
            System.exit(1);
        }
    }

    static void joinGroup() throws Exception{
        try {
            group = InetAddress.getByName(host);

            if(port>65535) {
                System.out.println("Please enter a valid port number (0:65535)");
                throw new Exception("port out of range");
            }

            boolean portChanged=false;
            do {
                try {
                    port%=65535;
                    s = new MulticastSocket(port);
                    if(portChanged) System.out.println("Using port " + port);
                    break;
                } catch (IOException e) {
                    portChanged=true;
                    System.out.println("port " + port + " is busy, trying port " + ++port);
                    //e.printStackTrace();
                }
            } while(true);

            try { s.joinGroup(group); }
            catch (IOException e) {
                System.out.println("Failed to join group, address is not a multicast address");
                throw new Exception("not a multicast address");
            }
            receiveThread = new Thread(new Receiver(s));
            receiveThread.start();
            System.out.println("<connected>");
        } catch (UnknownHostException e) {
            System.out.println("Please enter a valid host name or IP address");
            throw new Exception("invalid address");  //Used to end the program if it was started with a wrong hostname
        }
    }

    static void switchGroup() {
        receiveThread.interrupt();
        while(true) { //keep trying to leave
            try {
                s.leaveGroup(group);
                break;
            } catch (IOException e) {
                System.out.println("Connection error");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
        }

        while(true) { //keep trying to get a correct hostname
            boolean host_found=false, port_found=false;
            try {
                String newHost = System.console().readLine("Enter a group hostname or IP Address:");
                if(!newHost.isEmpty()) host=newHost; //else use last host
                String newPort = System.console().readLine("Enter group port number:");
                if(!newPort.isEmpty()) port=Integer.parseInt(newPort); //else use last port
                joinGroup();
                break;
            } catch(Exception e) {} //No need to end the program
        }
    }
}

class Receiver implements Runnable {
    public MulticastSocket s;
    Receiver(MulticastSocket s) {
        this.s = s;
    }

    @Override
    public void run() {
        while (true) { // get messages from others in group
            try {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                s.receive(messageIn);
                String msgIn = new String(messageIn.getData()).trim();
//                System.out.println(msgIn.length());
//                System.out.println(MulticastPeer.message.charAt(9));
                if(msgIn.equals(MulticastPeer.message.trim())) { msgIn="<sent>"; MulticastPeer.message=""; }
                System.out.println(msgIn);
            } catch(IOException e) {
                System.out.println("Connection error");
                try { Thread.sleep(1000); }
                catch (InterruptedException e1) { }
            }
        }
    }
}
