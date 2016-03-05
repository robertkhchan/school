package cs276.assignments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BasicIndex implements BaseIndex {

	@Override
	public PostingList readPosting(FileChannel fc) {
		PostingList result = null;
		try {			
			ByteBuffer buffer = ByteBuffer.allocate(2 * 4);
			if (fc.read(buffer) > 0) {
				buffer.flip();
				int termId = buffer.getInt();
				result = new PostingList(termId);
				int freq = buffer.getInt();
				
				buffer = ByteBuffer.allocate(freq * 4);
				fc.read(buffer);
				buffer.flip();
				while(buffer.hasRemaining()) {
					result.getList().add(buffer.getInt());
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) {
		try {		
			
			int freq = p.getList().size();
			ByteBuffer buffer = ByteBuffer.allocate((2 + p.getList().size()) * 4);
			buffer.putInt(p.getTermId());
			buffer.putInt(freq);
			for (Integer posting : p.getList()) {
				buffer.putInt(posting);
			}
			buffer.flip();
			fc.write(buffer);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
}
