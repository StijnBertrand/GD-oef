package filemanagement;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;


import org.junit.Test;


public class WorkingDirectoryTest {

	private WorkingDirectory wd;
	

	public WorkingDirectoryTest() {

		String root = "./root/";
		File previous = new File(root);
		delete(previous);
		previous.mkdir();
		wd = new WorkingDirectory(root);
	}

	void delete(File f)  {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				delete(c);
		}
		f.delete();
	}



	@Test
	public void testChangeWorkingDir() {
		try {
			wd.changeWorkingDir("../src");
			String wd_path = wd.getWorkingDir();
			assertEquals(wd_path, new File("./src").getCanonicalPath());
			wd.changeWorkingDir("../root");
		} catch (IOException e) {
			fail("Could not change working dir");
		}

	}

	@Test
	public void testCreateDir() {
		try {
			boolean created = wd.createDir("Dir1");
			String wd_path = wd.getWorkingDir();
			File dir = new File( wd_path + File.separator + "Dir1");
			boolean exists = dir.exists();
			assertEquals(created, exists);
		} catch (Exception e) {
			fail("Could not create directory Dir1");
		}

	}

	@Test
	public void testCreateFile() {
		try {
			boolean created = wd.createFile("test.txt");
			String wd_path = wd.getWorkingDir();
			File fil = new File(wd_path + File.separator + "test.txt");
			boolean exists = fil.exists();
			assertEquals(created, exists);
			assertEquals(1,wd.listFiles().length);
		} catch (Exception e) {
			fail("Could not create file test.txt");
		}

	}

	@Test
	public void testGetFile() throws IOException {
		String wd_path = wd.getWorkingDir();
		File fil = new File(wd_path + File.separator + "test.txt");
		boolean created = wd.getFile("test.txt", "./src/test.txt");
		boolean exists = fil.exists();
		assertEquals(created, exists);
	}

	@Test
	public void testPutFile() {
		try {
			boolean created = wd.putFile("./src/WorkingDirectory.java");
			String wd_path = wd.getWorkingDir();
			File fil = new File(wd_path + File.separator + "WorkingDirectory.java");
			boolean exists = fil.exists();
			assertEquals(created, exists);
		} catch (Exception e) {
			fail("Could not put file FileManipulator.java");
		}
		
	}

}
