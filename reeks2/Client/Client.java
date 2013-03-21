package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class Client {
	private PrintWriter output;
	private BufferedReader input;
	private PvszClient game;
	private boolean gedaan = false;
	
	public void connectToServer(InetAddress ip, int port)throws UnknownHostException, IOException, NumberFormatException, InterruptedException {
	
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

			startReadLoop();
		
		
		} finally {
			// tear down communication
			output.println("Quit");
			output.flush();
			socket.close();
		}
	}
	
	
	private void startReadLoop() throws IOException, NumberFormatException, InterruptedException{
		String reply;
		
		
		while(!input.ready());
		reply = input.readLine();
		dispatch( Integer.parseInt( reply ));	
		
		while(!gedaan){
			while(!input.ready());
			reply = input.readLine();
			dispatch( Integer.parseInt( reply ));
		}		
	}
	
	
	private void dispatch(int i) throws NumberFormatException, IOException, InterruptedException{
		int a,b,c,d,e,x,y,power,level,health;
		boolean dead = false;
		
		switch(i){
		//game data (groote van het spel enzo)
		case 1 :
			while(!input.ready());
			a = Integer.parseInt(input.readLine());
			while(!input.ready());
			b = Integer.parseInt(input.readLine());
			while(!input.ready());
			c = Integer.parseInt(input.readLine());
			while(!input.ready());
			d = Integer.parseInt(input.readLine());
			while(!input.ready());
			e = Integer.parseInt(input.readLine());
			while(!input.ready());
			if(input.readLine().equals("true")){
				dead = true;
				
			}
			game = new PvszClient(output,a,b,c,d,e,dead);
			break;
		//een plant wordt doorgestuurd	
		case 2 :
			while(!input.ready());
			x = Integer.parseInt(input.readLine());
			while(!input.ready());
			y = Integer.parseInt(input.readLine());
			while(!input.ready());
			Plants type = Plants.valueOf(input.readLine());		
			while(!input.ready());
			power = Integer.parseInt(input.readLine());
			while(!input.ready());
			level = Integer.parseInt(input.readLine());
			
			game.addPlant(new Plant(power, type, level), x, y);
			break;
		
		case 3:

			while(!input.ready());
			x = Integer.parseInt(input.readLine());
			while(!input.ready());
			y = Integer.parseInt(input.readLine());
			while(!input.ready());
			health = Integer.parseInt(input.readLine());
			while(!input.ready());
			level = Integer.parseInt(input.readLine());
		
			game.addZombie(x,y,health,level);	
			break;
		
		//er waren al te veel spelers
		case 4: 
			System.out.println("case 4");

			gedaan = true;
			break;
		
		case 5: 
			while(!input.ready());
			x = Integer.parseInt(input.readLine());
			while(!input.ready());
			y = Integer.parseInt(input.readLine());
			
			game.advanceZombie(x,y);
			break;
		
		case 6: 
			
			while(!input.ready());
			x = Integer.parseInt(input.readLine());
			while(!input.ready());
			y = Integer.parseInt(input.readLine());
			
			game.decreaseZombieHealth(x, y);
			break;
		
		case 7:
			while(!input.ready());
			x = Integer.parseInt(input.readLine());
			while(!input.ready());
			y = Integer.parseInt(input.readLine());
			
			game.decreacePower(x, y);
			break;
		
		
		
		}
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	static public void main( String args[] ) throws InterruptedException, IOException {
		InetAddress ip = InetAddress.getByName("localhost");//InetAddress.getByName(args[0]);
		int port = 5002;//Integer.parseInt(args[1]);

		
		Client client = new Client();
		client.connectToServer(ip, port);
		
	
	
	
	
	}
	
	
}
