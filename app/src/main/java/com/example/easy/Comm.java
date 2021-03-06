package com.example.easy;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class Comm {
    private static final int PORT = 5001;
    private static final int TIMEOUT = 4000;

    synchronized
    public String sendMessage(String input, String ip){
        String result = "";
        Socket client = new Socket();
        PrintWriter outputStream;
        BufferedReader bufferedReader;

        try{
            InetAddress address = InetAddress.getByName(ip);
            SocketAddress socketAddress = new InetSocketAddress(address, PORT);
            client.connect(socketAddress, TIMEOUT);

            outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            Log.d("sendMessage->Sending", "Sending...");

            outputStream.println(input + ";");
            outputStream.flush();

            Log.d("sendMessage->Preparing", "Preparing...");
            result = bufferedReader.readLine();
            Log.d("sendMessage->Response", result);

            if ( result.equals("CONNECTED") ){
                outputStream.println("GET-PRINT");
                outputStream.flush();

                result = bufferedReader.readLine();
                Log.d("sendMessage->Receiving", result);

                return result + ";" + ip;
            }
            else{
                outputStream.flush();
                outputStream.close();
                bufferedReader.close();
                client.close();
                return "closed";
            }
        } catch(IllegalArgumentException ex){
            Log.e("sendMessage", ex.getMessage());
            return "socket-invalid-timeout";
        } catch(IOException ex){
            Log.e("sendMessage", ex.getMessage());
            return "error-connection";
        }
    }

    synchronized
    public String sendFile(File file, String ip){
        String result;
        int n;
        int sended = 0;
        boolean getFile = false;

        try{
            Socket client = new Socket();
            InetAddress address = InetAddress.getByName(ip);
            SocketAddress socketAddress = new InetSocketAddress(address, PORT);
            client.connect(socketAddress, TIMEOUT);

            PrintWriter outputStream = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));

            //send name, length name and length file
            String temp = file.getName().toString();
            temp = temp + "#" + temp.length() + "#" + file.length();
            outputStream.println(temp);
            outputStream.flush();
            objectOutputStream.flush();
            result = "";
            result = bufferedReader.readLine();

            if ( file.getName().endsWith(".txt") ){
                byte[] buffer = new byte[2048];

                Log.d("SendFile->fileName", result);
                if ( result.equals("CORRECT") ){
                    //sends the file
                    n = 0;
                    sended = 0;
                    while ( (n = bufferedInputStream.read(buffer)) > -1 ){
                        sended += n;

                        Log.d("Sending txt", "Sent=" + n + "; Total=" + sended);

                        objectOutputStream.write(buffer, 0, n);
                        objectOutputStream.flush();
                        getFile = true;
                    }

                    if ( sended <= 0 && !getFile ){
                        throw new Exception("File with no size");
                    }
                    else{
                        objectOutputStream.flush();

                        //read the server response
                        result = bufferedReader.readLine();

                        bufferedReader.close();
                        client.close();
                        return "sended";
                    }
                }
                else{
                    bufferedReader.close();
                    client.close();
                    return "no-correct";
                }
            }
            else{
                byte[] buffer = new byte[2048];
                OutputStream os = client.getOutputStream();

                Log.d("SendFile->fileName", result);
                if ( result.equals("CORRECT") ){
                    //read the file and send it, a solution

                    while ( sended < file.length() && (n = bufferedInputStream.read(buffer)) >= 0 ){
                        sended += n;
                        //Log.d("Read from file", "Read=" + n + "; Total=" + sended);
                        os.write(buffer, 0, n);
                        os.flush();
                    }

                    Log.d("SendFile pdf->", "Leidos " + sended);

                    if ( sended > 0 ){
                        //wait for server
                        result = bufferedReader.readLine();
                        //Log.d("received", result);

                        bufferedReader.close();
                        client.close();
                        return "sended";
                    }
                    else{
                        bufferedReader.close();
                        client.close();
                        return "error-minus-one";
                    }
                }
                else{
                    bufferedReader.close();
                    client.close();
                    return "no-correct";
                }
            }
        }catch(IllegalArgumentException ex){
            Log.e("sendFile", ex.getMessage());
            return "socket-invalid-timeout";
        }catch(IOException ex){
            Log.e("sendFile", ex.getMessage());
            return "error-connection";
        }catch (Exception ex) {
            Log.e("sendFile", ex.getMessage());
            return "error";
        }
    }
}
