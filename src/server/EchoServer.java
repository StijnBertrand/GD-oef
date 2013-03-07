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

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import filemanagement.WorkingDirectory;

/**
 * A simple echo server. Accepts a single client connection, waits
 * for a single String-valued message, echoes back the same message
 * and then terminates.
 * 
 * Illustrates the use of TCP/IP sockets.
 */
public class EchoServer {
	private BufferedReader input;
	private PrintWriter output;
	
	private static WorkingDirectory wd = new WorkingDirectory("./server-folder/");
	
	
	/**
	 * Block and wait until a client arrives.
	 * 
	 * Once a client arrives, listen for client requests and
	 * send back echoed replies.
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
	 * @param port the port on which to listen for client connections.
	 * @throws IOException when unable to setup a connection or
	 *         unable to communicate with the client. 
	 */
	public void acceptClient(int port) throws IOException {
		
		InetSocketAddress serverAddress = new InetSocketAddress(port);
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(serverAddress);

		Socket clientSocket = serverSocket.accept(); // blocks
		
		try {
			// get raw input and output streams
			InputStream rawInput = clientSocket.getInputStream();
			OutputStream rawOutput = clientSocket.getOutputStream();

			// wrap streams in Readers and Writers to read and write
			// text Strings rather than individual bytes 
			input = new BufferedReader(
					new InputStreamReader(rawInput));
			output = new PrintWriter(rawOutput);			
			
			listen();
			
		} finally {
			// tear down communication
			clientSocket.close();
			serverSocket.close();
		}
	}
	
	
	private void listen()throws IOException{
		// read string from client
		while(!input.ready());
		String  command = input.readLine();
		dispatch(command);
		
	}
	
	
	private void dispatch(String command)throws IOException{
		if(command.equals("user")){
			String user = input.readLine();
			//send your password now
			output.println("201");
			output.flush();
			listen();
		}else if(command.equals("pass")){
			String pass = input.readLine();
			output.println("200");
			output.flush();
			listen();
		}else if(command.equals("CWD")){
			String map = input.readLine();
			wd.changeWorkingDir(map);
			output.println("200");
			output.flush();
			listen();
		}else if(command.equals("LIST")){
			String[] list = wd.listFiles();
			if(list != null){
				for(String curr : list){
					output.println("202");
					output.println(curr);
				}	
			}
			output.println("200");
			output.flush();
			listen();
		}else if(command.equals("GET")){
			String name = input.readLine();
			output.println("150");
			wd.getFile(name, output);
			output.flush();
			
		}else if(command.equals("QUIT")){
			return;
		}
		
		
	}
	

	
	/**
	 * Usage: java EchoServer port
	 * 
	 * Where port is the port on which the server should listen
	 * for requests.
	 * 
	 * Example:
	 *   java EchoServer 6789
	 *   
	 * @throws IOException when unable to setup connection or communicate
	 *         with the client. 
	 */
	public static void main(String[] args) throws IOException {
		/*
		if (args.length != 1) {
			System.out.println("Usage: java EchoServer port");
			return;
		}*/
		
		int port = 2234;//Integer.parseInt(args[0]);
		
		EchoServer server = new EchoServer();

		server.acceptClient(port);

	}

}
