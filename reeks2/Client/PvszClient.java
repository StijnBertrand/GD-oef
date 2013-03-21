package Client;



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
	
	public Plant(int power, Plants type, int level) throws IOException {
		this.power = power;
		this.level = level;
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

	public void eat() {
		if(type != Plants.ZOMBIE_GRASS){
			type = Plants.GRASS;
		}	
	}
		
	public void decreasePower(){
		power --;
		if (power <= 0) {
			type = Plants.GRASS;
		}
	}
}
	
	
	
	
	
	
public class PvszClient extends Canvas {
		
	public int XSIZ;
	public int YSIZ;
	public int ZOMBIE_BORDER;
	public int MAX_NR_ZOMBIES;
	public int SQ_SIZE;
	
	//int killed, planted;
	boolean dead;
	Plant field[ ][ ];
	ArrayList<Zombie> zombies;
	BufferedImage zimg;
	
	PrintWriter host;
	
	
	public PvszClient(PrintWriter host,int XSIZ, int YSIZ, int ZOMBIE_BORDER,int MAX_NR_ZOMBIES,int SQ_SIZE, boolean dead) throws InterruptedException, IOException {
		super( );
		//variabelen initialiseren
		this.host = host;
		this.XSIZ = XSIZ;
		this.YSIZ = YSIZ;
		this.ZOMBIE_BORDER = ZOMBIE_BORDER;
		this.MAX_NR_ZOMBIES = MAX_NR_ZOMBIES;
		this.SQ_SIZE = SQ_SIZE;
		this.dead = dead;
		
		
		// create a window
		zimg = ImageIO.read(new File("./zombie.png"));
		Frame frame = new Frame( "Plants vs. Zombies" );
		frame.add( this );
		frame.setSize( SQ_SIZE * XSIZ , SQ_SIZE * YSIZ + 21);
		frame.addWindowListener( new SweeperWindowListener( ) );
		
		
		// We store the zombies in a list
		zombies = new ArrayList<Zombie>(MAX_NR_ZOMBIES);
		int x, y;
		
		// Field creation
		field = new Plant[XSIZ][YSIZ];
		for (x = 0; x < ZOMBIE_BORDER; x++)
			for( y = 0; y < YSIZ; y++ )
				field[x][y] = new Plant(0,Plants.GRASS,0);
		for (x = ZOMBIE_BORDER; x < XSIZ; x++)
			for(y = 0; y < YSIZ; y++)
				field[x][y] = new Plant(0,Plants.ZOMBIE_GRASS,0);
		frame.setVisible(true);

		update(getGraphics());

		// Enable incoming mouse events 
		enableEvents(MouseEvent.MOUSE_CLICKED);
	}


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
		
		
	public void addPlant(Plant plant, int x, int y){
		field[x][y] = plant;	
		field[x][y].paint(getGraphics(), x, y);
	}

	
	public void decreacePower(int x,int y){
		field[x][y].decreasePower();
		field[x][y].paint(getGraphics(), x, y);
	}
		
		

	public void decreaseZombieHealth(int x, int y)throws InterruptedException{
		Zombie zombie = null;
		for (Zombie z: zombies) {
			if ( z.y == y && z.x == x) {
				// Zombie died?
				if (z.decreaseHealth()) {
					zombie = z;
					field[x][y].paint(getGraphics(), x, y);
				}
			}
		}
		if (zombie != null) 
			zombies.remove(zombie);
	}
	
	public void advanceZombie(int x, int y)throws InterruptedException{
		Zombie zombie = null;
		for (Zombie z: zombies) {
			if ( z.y == y && z.x == x) {
				// out of field
				if (z.advance()) {
					zombie = z;
				}
			}
		}
		if (zombie != null) {
			zombies.remove(zombie);
		}else{
			field[x-1][y].eat();
		}
			
	}
		
	public void addZombie(int x ,int y ,int health ,int level) throws InterruptedException{
		zombies.add(new Zombie(x,y, health, level));
		
	}
		
		


	protected void processMouseEvent(MouseEvent event) {

		if ( event.getID() == MouseEvent.MOUSE_CLICKED ) {
			// left mouse button is pressed : shoot
			if ( event.getButton() == MouseEvent.BUTTON1 ) {		
				int x = event.getX()/SQ_SIZE;
				int y = event.getY()/SQ_SIZE;
				host.println("1");
				host.println(Integer.toString(x));
				host.println(Integer.toString(y));
					
			// right mouse button is pressed : plant 
			}else {
				int x = event.getX()/SQ_SIZE;
				int y = event.getY()/SQ_SIZE;
				host.println("2");
				host.println(Integer.toString(x));
				host.println(Integer.toString(y));

			}
		}else{
			super.processMouseEvent( event );
		}	
	}


	/*
	 * A zombie has a position, health and level (= initial health)
	 */
	class Zombie{
		
		int level,health,x,y;
			
		public Zombie(int x,int y,int health, int level) throws InterruptedException {
			this.health = health;
			this.level = level;
			this.x = x;
			this.y = y;
			Graphics g = PvszClient.this.getGraphics();	
			this.paint(g);	
		}
			
		public boolean advance(){
			Graphics g = PvszClient.this.getGraphics();
			field[x][y].paint(g, x, y);
			x--;
			this.paint(g);		
			return x < 0;
		}
			
		public boolean decreaseHealth()throws InterruptedException{
			Graphics g = PvszClient.this.getGraphics();
			health--;
			// Make the zombie flash red for an instant
			g.setColor(Color.red);
			g.fillRect(x*SQ_SIZE, y*SQ_SIZE, SQ_SIZE-1, SQ_SIZE-1);
			Thread.sleep(200);
			paint(g);
			return health <= 0;
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
	}
}

class SweeperWindowListener extends WindowAdapter {
	public void windowClosing(WindowEvent event) {
		System.exit(0);
	}
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
