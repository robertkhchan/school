package cs276.assignments;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VBIndex implements BaseIndex {

	@Override
	public PostingList readPosting(FileChannel fc) {
		PostingList result = null;
		
		try {			
			ByteBuffer buffer = ByteBuffer.allocate(2 * 4);
			if (fc.read(buffer) > 0) {
				buffer.flip();
				int termId = buffer.getInt();
				int freq = buffer.getInt();				
				result = new PostingList(termId, VBDecode(Channels.newInputStream(fc), freq));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	List<Integer> VBDecode(InputStream is, int freq) throws IOException {
		List<Integer> list = new ArrayList<Integer>();
		int lastNumber = 0;
		for (int i = 0; i < freq; i++) {
			int decodedGap = 0;
			while (true) {
				byte[] nextByte = new byte[1];
				is.read(nextByte,0,1);
				
				int nextInt = (int)nextByte[0];
				if (nextInt < 0) {
					decodedGap = (128 * decodedGap) + (nextInt + 128);
					break;
				} else {
					decodedGap = (128 * decodedGap) + nextInt;
				}
			}
			lastNumber += decodedGap;
			list.add(lastNumber);
		}
		return list;
	}

	@Override
	public void writePosting(FileChannel fc, PostingList p) {
		try {		
			
			ByteBuffer buffer = ByteBuffer.allocate((2 + p.getList().size()) * 4);
			buffer.putInt(p.getTermId());
			buffer.putInt(p.getList().size());
			buffer.put(VBEncode(p.getList()));
			buffer.flip();
			fc.write(buffer);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	byte[] VBEncode(List<Integer> numbers) {
		Integer lastNumber = numbers.get(0);
		byte[] result = VBEncodeNumber(lastNumber);
				
		for (int i = 1; i < numbers.size(); i++) {
			byte[] encodedGap = VBEncodeNumber(numbers.get(i)-lastNumber);
			lastNumber = numbers.get(i);
			
			int resultLen = result.length;
			int encodedNumberLen = encodedGap.length;			
			byte[] tmpResult = new byte[resultLen + encodedNumberLen];
			System.arraycopy(result, 0, tmpResult, 0, resultLen);
			System.arraycopy(encodedGap, 0, tmpResult, resultLen, encodedNumberLen);
			
			result = tmpResult;
		}
		return result;
	}

	byte[] VBEncodeNumber(Integer n) {
		byte[] result = new byte[4];
		int position = result.length;
		while (true) {
			result[--position] = (byte) (n % 128);
			if (n < 128) break;
			n = n / 128;
		}
		result[3] += 128;
		return Arrays.copyOfRange(result, position, result.length);
	}


}
