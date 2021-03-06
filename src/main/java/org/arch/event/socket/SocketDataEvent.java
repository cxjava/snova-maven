/**
 * 
 */
package org.arch.event.socket;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 * @author qiyingwang
 * 
 */
@EventType(SocketEventContants.SOCKET_CHUNK_EVENT_TYPE)
@EventVersion(1)
public class SocketDataEvent extends Event {
	public byte[] content = new byte[0];

	public SocketDataEvent() {
	}

	@Override
	protected boolean onDecode(Buffer buffer) {
		try {
			int contenlen = BufferHelper.readVarInt(buffer);
			if (contenlen > 0) {
				content = new byte[contenlen];
				buffer.read(content);
			}
		} catch (Exception e) {
			return false;
		}
		return true;

	}

	@Override
	protected boolean onEncode(Buffer buffer) {
		BufferHelper.writeVarInt(buffer, content.length);
		buffer.write(content);
		return true;
	}

}
