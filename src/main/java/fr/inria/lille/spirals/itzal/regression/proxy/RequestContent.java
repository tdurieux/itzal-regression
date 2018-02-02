package fr.inria.lille.spirals.itzal.regression.proxy;

import org.eclipse.jetty.client.api.ContentProvider;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;

public class RequestContent {
	private int status;
	private byte[] body;


	public byte[] getBody() {
		return body;
	}

	public void addBody(int length, byte... body) {
		int lastIndex = 0;
		if (this.body == null) {
			this.body = new byte[length];
		} else {
			lastIndex = this.body.length - 1;
			this.body = Arrays.copyOf(this.body, this.body.length + length);
		}
		System.arraycopy(body, 0, this.body, lastIndex, length);
	}

	public void addBody(ContentProvider byteBuffers, int length) {
		if (length <= 0) {
			return;
		}
		int lastIndex = 0;
		if (this.body == null) {
			this.body = new byte[length];
		} else {
			lastIndex = this.body.length;
			this.body = Arrays.copyOf(this.body, this.body.length + length);
		}
		Iterator<ByteBuffer> iterator = byteBuffers.iterator();
		while (iterator.hasNext()) {
			ByteBuffer byteBuffer = iterator.next();
			byte[] array = byteBuffer.array();
			for (int i = 0; i < length; i++, lastIndex++) {
				this.body[lastIndex] = array[i];
			}

		}
	}
}