package taka7646.swf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.flex.swf.ISWF;
import org.apache.flex.swf.SWFFrame;
import org.apache.flex.swf.io.SWFReader;
import org.apache.flex.swf.tags.CharacterTag;
import org.apache.flex.swf.tags.DefineBitsJPEG2Tag;
import org.apache.flex.swf.tags.ITag;

public class SwfFile {
	String fileName;
	ISWF swf;
	
	public SwfFile(String name){
		fileName = name;
	}

	/**
	 * SWFファイルをロードする.
	 * @throws IOException
	 */
	public void load()throws IOException{
		FileInputStream is = new FileInputStream(fileName);
		load(is);
		is.close();
	}

	/**
	 * ストリームからロードする
	 * @param is
	 * @throws IOException
	 */
	public void load(InputStream is)throws IOException{
		SWFReader reader = new SWFReader();
		swf = reader.readFrom(is, fileName);
	}

	public void exportImage(ITag tag, String fileName)throws IOException{
		switch(tag.getTagType()){
		case DefineBitsJPEG2:
			{
				byte[] src = ((DefineBitsJPEG2Tag)tag).getImageData();
				JpegFile jpeg = new JpegFile();
				jpeg.parse(src);
				jpeg.save(fileName);
				System.out.println(jpeg.toString());
			}
			break;
		case DefineBitsJPEG3:
			break;
		
		case DefineBitsLossless:
			break;
		case DefineBitsLossless2:
			break;
		}
	}
	
	public void exportImages()throws IOException{
		int frameCount = swf.getFrameCount();
		for(int i = 0; i < frameCount; i++){
			SWFFrame frame = swf.getFrameAt(i);
			Iterator<ITag> it = frame.iterator();
			while(it.hasNext()){
				ITag tag = it.next();
				switch(tag.getTagType()){
				case DefineBitsJPEG2:
				case DefineBitsJPEG3:
					{
						CharacterTag ctag = (CharacterTag)tag;
						exportImage(tag, "target/j"+ctag.getCharacterID()+".jpg");
					}
					break;
				}
			}
		}
	}
	
	/**
	 * タグ内容を標準出力にDUMPする.
	 */
	public void dumpTags(){
		int frameCount = swf.getFrameCount();
		for(int i = 0; i < frameCount; i++){
			SWFFrame frame = swf.getFrameAt(i);
			System.out.println("Frame:"+(i+1));
			Iterator<ITag> it = frame.iterator();
			while(it.hasNext()){
				ITag tag = it.next();
				System.out.println(tag);
			}
			System.out.println("----------------------");
		}
	}
}
