import java.io.*;
import java.net.*;

public class Client {
    static String ADDRES_S = "192.168.238.35"; 
    static int PORT_S = 49230; 
    static String clientDIR = "C:\\Users\\smamm\\OneDrive\\Desktop\\TC"; 

    public static void main(String[] args) {
        Socket socket = null; 
        try {
            socket = new Socket(ADDRES_S, PORT_S);  
            DataInputStream dis = new DataInputStream(socket.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); 
    
            //list file from server
            int fileCount = dis.readInt(); 
            String[] fileNames = new String[fileCount]; 
            for (int i = 0; i < fileCount; i++) {
                fileNames[i] = dis.readUTF(); 
            }
            System.out.println("Files on the server : ");
            for (String fileName : fileNames) {
                System.out.println(fileName);
            }

            //Request a file 
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
            System.out.print("Enter the name of the file you want to download : ");
            String requestedFileName = br.readLine(); 
            dos.writeUTF(requestedFileName); 

            //Receive the file from the server
            long fileSize = dis.readLong(); 
            System.out.println("File size : "+ fileSize +" bytes");
            if (fileSize > 0) { 
                File clientDir = new File(clientDIR); 
                if (!clientDir.exists()) { 
                    clientDir.mkdirs(); 
                }

                FileOutputStream fos = new FileOutputStream(clientDIR + File.separator + requestedFileName);//สร้างออบเจ็คที่จะเขียนไฟล์ที่รับมาจาก server ลงไป
                byte[] buffer = new byte[8192]; 
                long totalRead = 0; 
                int read = 0; 
                int previousProgress = 0; 
                while (fileSize > 0 && (read = dis.read(buffer, 0, Math.min( buffer.length, (int) fileSize) ) ) != -1) { //อ่านbytesจากserverลงbuffers
                    //เขียนไฟล์ลง directory ของ client
                    fos.write(buffer, 0, read); //เอาbytesในบัฟเฟอร์เขียนลง fosของเครื่องclient
                    fileSize -= read;
                    totalRead += read; 

                    //แสดง % ที่โหลดไปแล้ว
                    int progress = (int) ((totalRead * 100) / (totalRead + fileSize));
                    if (progress != previousProgress) { 
                        System.out.print("\rDownloading : " + progress + "%"); 
                        previousProgress = progress;`
                    }
                }
                System.out.println("\rDownloading : 100%"); 
                fos.close();
                System.out.println("File downloaded successfully.");
            } else {
                System.out.println("File not found on the server.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
                try {
                    socket.close();
                    System.out.println("socket close");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}