package taka7646.swf;

import static org.junit.Assert.*;

import org.junit.Test;

public class SwfTest {

	@Test
	public void test() {
		try{
			SwfFile swfFile = new SwfFile("src/test/resources/ethnyan-blue-mask.swf");
			swfFile.load();
			swfFile.dumpTags();
			swfFile.exportImages();
		}catch(Throwable e){
			e.printStackTrace();
			fail();
		}
	}

}
