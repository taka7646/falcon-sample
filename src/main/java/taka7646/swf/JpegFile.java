package taka7646.swf;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class JpegFile {
	List<JpegChunk> chunkList = new LinkedList<JpegChunk>();
	int width;
	int height;
	int srcSize;

	public JpegFile(){
	}
	public void parse(byte[] src)throws IOException{
		srcSize = src.length;
		ByteArrayInputStream is = new ByteArrayInputStream(src);
		int data;
		while((data=is.read()) != -1){
			if(data != 0xff){
				throw new RuntimeException("Format Error!!");
			}
			data = is.read();
			JpegChunk chunk = createChunk(data);
			chunk.parse(is);
			if(data == 0xc0){	// SOF0
				int offset = 1;
				width = chunk.readU16(chunk.data, offset);
				height = chunk.readU16(chunk.data, offset+2);
			}
			chunkList.add(chunk);
		}
	}
	
	protected JpegChunk createChunk(int chunkTag){
		if(chunkTag == SOIChunk.TAG_CODE){
			return new SOIChunk();
		}
		if(chunkTag == EOIChunk.TAG_CODE){
			return new EOIChunk();
		}
		if(NoLengthChunk.isNoLength(chunkTag)){
			return new NoLengthChunk(chunkTag);
		}
		NormalChunk chunk = new NormalChunk(chunkTag);
		return chunk;
	}
	
	public void save(String fileName)throws IOException{
		FileOutputStream os = new FileOutputStream(fileName);
		os.write(0xff);
		os.write(SOIChunk.TAG_CODE);
		for(JpegChunk chunk: chunkList){
			chunk.write(os);
		}
		
		os.write(0xff);
		os.write(EOIChunk.TAG_CODE);
		os.close();
	}
	
	@Override
	public String toString() {
		return String.format("Jpeg:%dx%d %dbytes", width, height, srcSize);
	}
}


abstract class JpegChunk{
	public int chunkTag;
	public int length;
	public byte[] data;
	
	public JpegChunk(int chunkTag){
		this.chunkTag = chunkTag;
	}
	
	abstract public void parse(InputStream is)throws IOException;
	
	abstract public void write(OutputStream os)throws IOException;

	public int readU8(InputStream is)throws IOException{
		return is.read() & 0xff;
	}
	
	public int readU16(InputStream is)throws IOException{
		return ((is.read() & 0xff)<<8)
				| (is.read() & 0xff)
			;
	}
	
	public int readU16(byte[] src, int offset){
		return ((src[offset]&0xff)<<8)
				| ((src[offset+1]&0xff)<<0);
	}
	
	public void writeU16(OutputStream os, int data)throws IOException{
		os.write((data>>8)&0xff);
		os.write((data>>0)&0xff);
	}
	
	@Override
	public String toString() {
		return String.format("%02x:%4dbytes %s", chunkTag, length, getClass().getSimpleName());
	}
}

/// Start Of Image
class SOIChunk extends JpegChunk{
	public static final int TAG_CODE = 0xd8;
	public SOIChunk(){
		super(TAG_CODE);
	}

	@Override
	public void parse(InputStream is) throws IOException {
	}
	@Override
	public void write(OutputStream os)throws IOException{
	}
}
/// End Of Image
class EOIChunk extends JpegChunk{
	public static final int TAG_CODE = 0xd9;
	public EOIChunk(){
		super(TAG_CODE);
	}

	@Override
	public void parse(InputStream is) throws IOException {
	}
	@Override
	public void write(OutputStream os)throws IOException{
	}
}

/// Lengthを持たないChunk
class NoLengthChunk extends JpegChunk{
	static final Set<Integer> tagSet = new HashSet<Integer>(
			Arrays.asList(
					0xda,	// SOS(Start of Scan)
					0xd0, 0xd1, 0xd2, 0xd3,	// RST
					0x04, 0x05, 0x06, 0x07	// RST
				)
			);

	static public boolean isNoLength(int chunkTag){
		return tagSet.contains(Integer.valueOf(chunkTag));
	}
	
	public NoLengthChunk(int chunkTag) {
		super(chunkTag);
	}

	@Override
	public void parse(InputStream is) throws IOException {
		int c;
		is.mark(0);
		while((c=is.read())!= -1){
			if(c != 0xff){
				continue;
			}
			c = is.read();
			if(c == 0x00){
				continue;
			}
			// 0xffの後に 0x01以上の値が続く場合は、次のchunk
			break;
		}
		int end = is.available();
		is.reset();
		length = is.available() - end;
		data = new byte[length];
		is.read(data);
	}
	@Override
	public void write(OutputStream os)throws IOException{
		os.write(0xff);
		os.write(chunkTag);
		os.write(data);
	}
}

class NormalChunk extends JpegChunk{
	public NormalChunk(int chunkTag) {
		super(chunkTag);
	}
	@Override
	public void parse(InputStream is) throws IOException {
		length = readU16(is);
		data = new byte[length-2];
		is.read(data);
	}
	@Override
	public void write(OutputStream os)throws IOException{
		os.write(0xff);
		os.write(chunkTag);
		writeU16(os, length);
		os.write(data);
	}
}
