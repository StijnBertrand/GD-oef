/*
Copyright (c) 2013, Tom Van Cutsem, Vrije Universiteit Brussel
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Vrije Universiteit Brussel nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/
//package edu.vub.distsys.server.echo_v1;

package client;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;




/**
 * A simple client to an {@link EchoServer}.
 * Sends a single message to the server, then terminates.
 * 
 * Illustrates the use of TCP/IP sockets.
 */
public class EchoClient {
	private PrintWriter output;
	private BufferedReader input;
	String serverReply;
	
	
	/**
	 * Connect to server at the given ip:port combination,
	 * send the msg string and await a reply.
	 * 
	 * Client and server sockets communicate via input and output streams,
	 * as shown schematically below:
	 * 
	 * <pre>
     *   Client                             Server
     *    cs = new Socket(addr,port)        ss = socket.accept()
     *       cs.in <-------------------------- ss.out
     *       cs.out -------------------------> ss.in
     * </pre>
	 * 
	 * @param ip the server IP address
	 * @param port the server port number
	 * @param msg the message to be sent to the server
	 * @throws UnknownHostException if the server IP could not be found
	 * @throws IOException if there is an error in setting up or communicating
	 *         with the server.
	 */
	public void connectToServer(InetAddress ip, int port, String msg)
		        throws UnknownHostException, IOException {
		
		InetSocketAddress serverAddress = new InetSocketAddress(ip, port);		
		Socket socket = new Socket();
		socket.connect(serverAddress);
		
		try {
			// get raw input and output streams
			InputStream rawInput = socket.getInputStream();
			OutputStream rawOutput = socket.getOutputStream();
			
			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			input = new BufferedReader(
					new InputStreamReader(rawInput));
			
			output = new PrintWriter(rawOutput);

			

	
			
			//GET("testfile");
			
			
			
		} finally {
			// tear down communication
			output.println("Quit");
			output.flush();
			socket.close();
		}
	}
	
	//werkt niet
	private void GET(String name) throws IOException{
		output.println("GET");
		output.println(name);
		output.flush();
		while(!input.ready());
		serverReply = input.readLine();
		if(serverReply.equals("150")){
			FileOutputStream outraw = new FileOutputStream("./server-folder/" + name );
			PrintWriter out = new PrintWriter(outraw); 
			while(!input.ready());
			serverReply = input.readLine();
			while(serverReply != null){
				out.write(serverReply);
			}	
		}	
	}
	
	
	private void LIST() throws IOException{
		output.println("LIST");
		output.flush();
		while(!input.ready());
		serverReply = input.readLine();
		while(serverReply.equals("202")){
			
			serverReply = input.readLine();
			System.out.println(serverReply);
			while(!input.ready());
			serverReply = input.readLine();
		}
		nextCommand();
	}
	
	
	private void CWD(String path)throws IOException{
		output.println("CWD");
		output.println(path);
		output.flush();
		
		// wait for and read the reply of the server
		serverReply = input.readLine();
		if(serverReply.equals("200")){
			//System.out.println("succesfully changed directory");
		}
		nextCommand();
	}
	
	
	private void logIn(String user, String pass)throws IOException {
		output.println("user");
		output.println(user);
		output.flush();
		// wait for and read the reply of the server
		serverReply = input.readLine();

		if(serverReply.equals("201")){
			output.println("pass");
			output.println(pass);
			output.flush();
		}
		// wait for and read the reply of the server
		serverReply = input.readLine();
		if(serverReply.equals("200")){
			System.out.println("succesfully logged in"); 
		}else{
			System.out.println("logging in failed");
		}
		nextCommand();
	}
	
	private void nextCommand(){
		
	}
	
	
	/**
	 * Usage: java EchoClient ip port message
	 * 
	 * Where ip is the IP address of the server, port is
	 * the port number, and message is a string to be
	 * sent to the server.
	 * 
	 * Example:
	 *   java EchoClient 127.0.0.1 6789 message
	 *   
	 * @throws IOException if there was an error connecting with or
	 *         communicating with the server. 
	 */
	public static void main(String[] args) throws IOException {
		/*
		if (args.length != 3) {
			System.out.println("Usage: java EchoClient ip port message");
			return;
		}*/
		
		InetAddress ip = InetAddress.getByName("localhost");//InetAddress.getByName(args[0]);
		int port = 2234;//Integer.parseInt(args[1]);
		String message = "helloworld";//args[2];
		
		System.out.println("Client: connecting to server at "+ip+":"+port);
		
		EchoClient client = new EchoClient();
		client.connectToServer(ip, port, message);
		
		System.out.println("Client: terminating");
	}

}
