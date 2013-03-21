import java.awt.*; 
import java.util.*; 
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread;

import javax.imageio.ImageIO;

/*
 * Different types of plants
 */
enum Plants {
	GRASS, SUNFLOWER, PEASHOOTER, ZOMBIE_GRASS
}

/*
 * A plant has a power (which decreases when shooting), 
 * a level (its initial power) and a type.
 */
class Plant {
	int power, level;
	Plants type;
	BufferedImage img;

	public Plant(int power, Plants type) throws IOException {
		this.power = power;
		this.level = power;
		this.type = type;
		// Load corresponding image
		switch (this.type) {
		case SUNFLOWER:
			img = ImageIO.read(new File("./Sunflower2.png"));
			break;
		case PEASHOOTER:
			img = ImageIO.read(new File("./Snow_Pea2.png"));
			break;
		default:
			break;
		}
	}
	
	public Plants getType(){
		return type;
	}

	/* Grass and zombiegrass is simply a color
	 * Other plants are drawn with their corresponding image
	 * and a box, showing its remaining power
	 */
	public void paint( Graphics g, int x, int y ) {
		switch (this.type) {
		case GRASS:
			g.setColor(Color.green);
			g.fillRect( x * 35, y * 35, 34, 34 );
			break;
		case ZOMBIE_GRASS:
			g.setColor(Color.lightGray);
			g.fillRect( x * 35, y * 35, 34, 34 );
			break;
		case SUNFLOWER: case PEASHOOTER:
			g.setColor(Color.white);
			g.drawImage(img, x*35,y*35,null);
			float rect = (float) power/level;
			g.fillRect(x*35+7, y*35+30, (int)(rect*20), 3);
			break;
		}
	}

	
	public boolean shoot() {
		power--;
		// Plants without power become grass
		if (power <= 0) {
			type = Plants.GRASS;
			return false;
		}
		return true;
	}

	public boolean isShooter() {
		return (type == Plants.PEASHOOTER || type == Plants.SUNFLOWER);
	}

	public void eat() {
		type = Plants.GRASS;
	}
	
	public void sendData(PrintWriter stream){
		stream.println(Integer.toString(power));
		stream.println(Integer.toString(level));
	}
	
}




public class PvszServer extends Canvas {
	
	public final static int XSIZ = 20;
	public final static int YSIZ = 6;
	public final static int ZOMBIE_BORDER = 15;
	public final static int MAX_NR_ZOMBIES = 5;
	public final static int SQ_SIZE = 35;
	public final static int MAX_PLAYERS = 4;
	
	int killed, planted;
	boolean dead;
	Plant field[ ][ ];
	ArrayList<Zombie> zombies;
	BufferedImage zimg;
	
	PrintWriter players[] = new PrintWriter[MAX_PLAYERS];
	int aantal = 0;

	public PvszServer() throws InterruptedException, IOException {
		super( );
		// create a window
		zimg = ImageIO.read(new File("./zombie.png"));
		Frame frame = new Frame( "Plants vs. Zombies" );
		frame.add( this );
		frame.setSize( SQ_SIZE * XSIZ , SQ_SIZE * YSIZ + 21);
		frame.addWindowListener( new SweeperWindowListener( ) );
		
		killed = planted = 0;
		dead = false;
		// We store the zombies in a list
		zombies = new ArrayList<Zombie>(MAX_NR_ZOMBIES);
		int x, y;
		int aantalplanten = 5;
		// Field creation
		field = new Plant[XSIZ][YSIZ];
		for (x = 0; x < ZOMBIE_BORDER; x++)
			for( y = 0; y < YSIZ; y++ )
				field[x][y] = new Plant(0,Plants.GRASS);
		for (x = ZOMBIE_BORDER; x < XSIZ; x++)
			for(y = 0; y < YSIZ; y++)
				field[x][y] = new Plant(0,Plants.ZOMBIE_GRASS);
		frame.setVisible(true);
		// Add 5 sunflowers on random places
		Random r = new Random();
		while( aantalplanten-- > 0 ) {
			x = r.nextInt(ZOMBIE_BORDER);
			y = r.nextInt(YSIZ);
			field[x][y] = new Plant(6,Plants.SUNFLOWER);
		}
		update(getGraphics());
		// Spawn a first zombie
		spawnZombies();
		// Enable incoming mouse events 
		enableEvents(MouseEvent.MOUSE_CLICKED);
	}

	/*
	 * code om het scherm te tekenen
	 * en om interactie met het scherm af te handelen
	 * 
	 */
	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		int x, y;
		// Paint all the plants
		for( x = 0; x < XSIZ; x++ )
			for( y = 0; y < YSIZ; y++ ) {
				field[x][y].paint( g, x, y );
			}
		// Paint all the zombies
		for (Zombie z: zombies) 
			z.paint(getGraphics());
	}


	public void shoot(int x, int y) throws InterruptedException {
		if ( x < 0 || x > XSIZ || y < 0 || y > YSIZ ) return;
		Plant p = field[x][y];
		Zombie zombie = null;
		for (Zombie z: zombies) {
			// Zombie on same line and plant is a shooter
			if ( z.y == y && p.isShooter()) {
				p.shoot();
				// Zombie died?
				if (z.hit()) {
					zombie = z;
					killed++;
				}
				p.paint(getGraphics(), x, y);
			}
		}
		// Zombie is dead
		if (zombie != null) 
			zombies.remove(zombie);
		spawnZombies();
	}
	
	/*
	 * EINDE code voor het scherm
	 * 
	 */

	public void plant(int x, int y) throws IOException {
		int level = killed / 5 + 1;
		// The amount of plants cannot exceed the number of zombies
		if (planted <= zombies.size() ) {
			field[x][y] = new Plant(level*3, Plants.PEASHOOTER);
			planted++;
			update(getGraphics());
		}
	}

	/*
	 * Depending on the current amount of zombies and the level,
	 * new zombies are spawned
	 */
	void spawnZombies() throws InterruptedException {
		int current = zombies.size();
		int level = killed / 5 + 1;
		if (level-current > 0) planted = 0;
		for (int i = 0; i < level - current; i++) {
			Zombie nieuwe = new Zombie(level*3);
			nieuwe.start();
			zombies.add(nieuwe);
		}
	}

	protected void processMouseEvent(MouseEvent event) {
		if(dead)return;
		if ( event.getID() == MouseEvent.MOUSE_CLICKED ) {
			// left mouse button is pressed : shoot
			if ( event.getButton() == MouseEvent.BUTTON1 ) {
				try {
					int x = event.getX()/SQ_SIZE;
					int y = event.getY()/SQ_SIZE;
					shoot(x, y);
					sendDecreacePower(x,y);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} }
			// right mouse button is pressed : plant 
			else {
				try {
					int x = event.getX()/SQ_SIZE;
					int y = event.getY()/SQ_SIZE;
					plant(x, y);
					sendPlantToAll(x ,y);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else super.processMouseEvent( event );
	}


	/*
	 * A zombie has a position, health and level (= initial health)
	 */
	class Zombie extends Thread{

		int level,health,x,y;

		public Zombie(int health) throws InterruptedException {
			this.health = health;
			this.level = health;
			x = PvszServer.XSIZ;
			Random r = new Random();
			y = r.nextInt(PvszServer.YSIZ);
			x = r.nextInt(x - ZOMBIE_BORDER);
			x += ZOMBIE_BORDER;
			this.sendNewZombie();
		}
		
		@Override
		public void run(){
			Graphics g = PvszServer.this.getGraphics();
			while(health > 0 && x >= 0 && !dead) {	
				this.paint(g);
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (field[x][y].isShooter())
					field[x][y].eat();
				field[x][y].paint(g, x, y);
				sendAdvance();
				x--;
				if(x < 0)dead = true;	
			}
			if(health >0 && x >= 0)this.paint(g);
		}

		public void paintColor(Graphics g, Color c) {
			g.setColor(c);
		}

		public void paint(Graphics g) {
			g.drawImage(zimg, x*SQ_SIZE, y*SQ_SIZE, null);
			g.setColor(Color.red);
			float rect = (float) health/level;
			g.fillRect(x*SQ_SIZE+5, y*SQ_SIZE+20, (int)(rect*15), 3);
		}

		public boolean hit() throws InterruptedException  {
			Graphics g = PvszServer.this.getGraphics();
			health--;
			sendDecreaceHealth();
			// Make the zombie flash red for an instant
			g.setColor(Color.red);
			g.fillRect(x*SQ_SIZE, y*SQ_SIZE, SQ_SIZE-1, SQ_SIZE-1);
			Thread.sleep(200);
			paint(g);
			return health <= 0; 
		}
		
		private synchronized void sendNewZombie(){
			for(PrintWriter p : players){
				if(p != null){
					this.sendData(p);
				}else{
					return;
				}	
			}	
		}
		
		//stuurt naar alle spelers dat deze zombie vooruit moet gaan
		private synchronized void sendDecreaceHealth(){
			for(PrintWriter p : players){
				if(p != null){
					p.println("6");
					p.println(Integer.toString(x));
					p.println(Integer.toString(y));
					p.flush();
				}else{
					return;
				}
				
			}	
		}
		
		
		//stuurt naar alle spelers dat deze zombie vooruit moet gaan
		private synchronized void sendAdvance(){
			for(PrintWriter p : players){
				if(p != null){
					p.println("5");
					p.println(Integer.toString(x));
					p.println(Integer.toString(y));
					p.flush();
				}else{
					return;
				}	
			}	
		}
		
		//stuurt de data van de zombie naar de stream
		public synchronized void sendData(PrintWriter stream){
			stream.println("3");
			stream.println(Integer.toString(x));
			stream.println(Integer.toString(y));
			stream.println(Integer.toString(health));
			stream.println(Integer.toString(level));
		}
	}
	/*
	 * EINDE ZOMBIE KLASSE
	 */
	
	/*
	 * Meerdere spelers
	 */
	
	private void sendPlantToAll(int x ,int y){
		for(PrintWriter p : players){
			if(p != null){
				sendPlant(p,x,y);
				p.flush();
			}else{
				return;
			}	
		}	
	}
	
	public boolean addPlayer(PrintWriter stream){
		if(aantal < MAX_PLAYERS){
			players[aantal] = stream;
			aantal ++;
			//initialiseer de client
			sendGameData(stream);
			return true;
		}else{
			return false;
		}
	}
	
	//deze 3 functie dienen om de client site te initialiseren
	private synchronized void sendGameData(PrintWriter stream) {
		stream.println("1");
		stream.println(Integer.toString(XSIZ));
		stream.println(Integer.toString(YSIZ));
		stream.println(Integer.toString(ZOMBIE_BORDER));
		stream.println(Integer.toString(MAX_NR_ZOMBIES));
		stream.println(Integer.toString(SQ_SIZE));
		if(dead){
			stream.println("true");
		}else{
			stream.println("false");
		}
		
		//planten doorsturen
		sendPlants(stream);
		//zombies doorsturen
		sendZombies(stream);

		stream.flush();
	}
	//dit stuurt de planten naar de inputstream(dient om de client te initialiseren)
	//opm ik weet niet zeker of dit synchronized moet zijn of niet
	private void sendPlants(PrintWriter stream){
		int x,y;
		for (x = 0; x < ZOMBIE_BORDER; x++){
			for( y = 0; y < YSIZ; y++ ){
				Plants type = field[x][y].getType();
				if(type != Plants.GRASS && type != Plants.ZOMBIE_GRASS){
					sendPlant( stream, x, y);

				}
						
			}
		}
	}
	
	private void sendPlant(PrintWriter stream,int x,int y){
		stream.println("2");
		stream.println(Integer.toString(x));
		stream.println(Integer.toString(y));
		stream.println(field[x][y].getType().name());
		field[x][y].sendData(stream);	
	}
		
		
	//dit stuurt de zombies naar de strem(dient om de client te initialiseren
	//opm ik weet niet zeker of dit synchronized moet zijn of niet
	private void sendZombies(PrintWriter stream){
		for(Zombie z: zombies){
			z.sendData(stream);
		}
	}
	
	private void sendDecreacePower(int x, int y){
		for(PrintWriter p : players){
			if(p != null){
				p.println("7");
				p.println(Integer.toString(x));
				p.println(Integer.toString(y));
				p.flush();
			}else{
				return;
			}	
		}
	}
}

class SweeperWindowListener extends WindowAdapter {
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}
}