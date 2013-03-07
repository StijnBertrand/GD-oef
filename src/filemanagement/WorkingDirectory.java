package filemanagement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;


public class WorkingDirectory {
	
	File workingDirectory;
	
	
	public WorkingDirectory(String path){
		this.workingDirectory = new File(path); 	
	}
	
	public String getWorkingDir()throws IOException{
		return workingDirectory.getCanonicalPath();
	}
	
	
	public boolean changeWorkingDir(String name)throws IOException{
		File temp = new File(workingDirectory.getCanonicalPath()+ File.separator + name);
		if(temp.exists()){
			this.workingDirectory = temp;	
			return true;
		}else{
			return false;
		}
	}

	public String[] listFiles(){
		return workingDirectory.list();
	}
	
	
	public boolean getFile(String filename, String to){
		try{
			copyFile(workingDirectory.getPath() + File.separator + filename,to );
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	
	public boolean getFile(String filename, PrintWriter to){
		try{
			FileInputStream in = new FileInputStream(this.getWorkingDir()+ filename);
			int c;
			while ((c = in.read()) != -1) {
				to.write(c);
			}
			in.close();
			return true;
		}catch(Exception e){
			return false;
		}
	}
	
	public boolean putFile(String filename){
		try{
			File from = new File(filename);
			copyFile(this.getWorkingDir()+ File.separator + from.getName(), filename);
			return true;
		}catch(Exception e){
			return false;
		}	
	}

	public boolean createDir(String name){
		try{
			File nieuw = new File(workingDirectory.getPath()+ File.separator+name);
			nieuw.mkdir();
			return true;
		}catch(Exception e){
			return false;
		}	
	}

	public boolean createFile(String name){
		try{
			File nieuw = new File(workingDirectory.getPath()+ File.separator +name);
			nieuw.createNewFile();
			return true;
		}catch(Exception e){
			return false;
		}	
	}
	
	
	
	private boolean copyFile(String from, String to){
		try{
			FileInputStream in = new FileInputStream(from);
			

			File toFile = new File(to);
			toFile.createNewFile();
			FileOutputStream out = new FileOutputStream(to);
			
			int c;
			while ((c = in.read()) != -1) {
			out.write(c);
			}
			in.close();
			out.close();

			return true;
		}catch(Exception e){
			return false;	
		}			
	}




}
