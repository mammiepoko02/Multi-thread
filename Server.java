import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    static int PORT = 49230;
    static String serverDir = "C:\\Users\\smamm\\OneDrive\\Desktop\\VdThreads";

    public static void main(String[] args) {
        ServerSocket serverSocket = null; 
        try {
            serverSocket = new ServerSocket(PORT); 
            System.out.println("Server started on port " + PORT + " waiting for client");

            while (true) {
                Socket clientSocket = serverSocket.accept(); 
                Thread clientHandler = new ClientHandler(clientSocket);                                          
                clientHandler.start(); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
             try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                 }
        }
    }

    //การทำงานของ thread
    public static class ClientHandler extends Thread {
        Socket clientSocket; 

        public ClientHandler(Socket socket) { 
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {     

                // Send the list of files to the client
                File directory = new File(serverDir);
                File[] files = directory.listFiles(); 
                List<String> fileNames = new ArrayList<>(); 
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            fileNames.add(file.getName());
                        }
                    }
                }
                dos.writeInt(fileNames.size()); 
                for (String fileName : fileNames) {
                    dos.writeUTF(fileName); 
                }
                //Receive the requested file name from the client
                String requestedFileName = dis.readUTF(); 
                System.out.println("Client requested file : " + requestedFileName);
                File fileToSend = new File(serverDir + File.separator + requestedFileName); 

                //เขียนไฟล์
                if (fileToSend.exists()) {
                    dos.writeLong(fileToSend.length());
                    try (FileInputStream fis = new FileInputStream(fileToSend)) { 
                        byte[] buffer = new byte[8192]; 
                        int read = 0;
                        while ((read = fis.read(buffer)) != -1) { //อ่านbytesจากfis(requested file from client)ลงbufferทีละ 8192
                            dos.write(buffer, 0, read); //ดึงจากbufferเขียนให้dosส่งไปclient
                            
                        }
                    }
                    System.out.println("Sent file " + requestedFileName + " to client.");
                } else {
                    dos.writeLong(0);
                    System.out.println("Requested file not found : " + requestedFileName);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
