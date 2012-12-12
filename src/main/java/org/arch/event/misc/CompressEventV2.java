/**
 * This file is part of the hyk-proxy-gae project.
 * Copyright (c) 2011 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: CompressEvent.java 
 *
 * @author yinqiwen [ 2011-12-3 | ����02:09:00 ]
 *
 */
package org.arch.event.misc;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.compress.fastlz.JFastLZ;
import org.arch.compress.jsnappy.SnappyBuffer;
import org.arch.compress.jsnappy.SnappyCompressor;
import org.arch.compress.jsnappy.SnappyDecompressor;
import org.arch.compress.lzf.LZFDecoder;
import org.arch.compress.lzf.LZFEncoder;
import org.arch.compress.quicklz.QuickLZ;
import org.arch.event.Event;
import org.arch.event.EventConstants;
import org.arch.event.EventDispatcher;
import org.arch.event.EventType;
import org.arch.event.EventVersion;

/**
 *
 */
@EventType(EventConstants.COMPRESS_EVENT_TYPE)
@EventVersion(2)
public class CompressEventV2 extends Event {
	// protected static Logger logger = LoggerFactory
	// .getLogger(CompressEvent.class);
	public CompressEventV2() {

	}

	public CompressEventV2(CompressorType type, Event ev) {
		this.type = type;
		this.ev = ev;
	}

	public CompressorType type;
	public Event ev;

	@Override
	protected boolean onDecode(Buffer buffer) {
		try {
			int t = BufferHelper.readVarInt(buffer);
			type = CompressorType.fromInt(t);
			int size = BufferHelper.readVarInt(buffer);
			Buffer content = buffer;
			byte[] raw = buffer.getRawBuffer();

			switch (type) {
			case QUICKLZ: {
				try {
					byte[] newbuf = QuickLZ.decompress(raw, buffer.getReadIndex(), size);
					content = Buffer.wrapReadableContent(newbuf, 0, newbuf.length);
				} catch (Exception e) {
					// logger.error("Failed to uncompress by QuickLZ.", e);
					return false;
				}
				break;
			}
			case FASTLZ: {
				int len = buffer.readableBytes() * 10;
				try {
					JFastLZ fastlz = new JFastLZ();
					byte[] newbuf = new byte[len];
					int decompressed = fastlz.fastlzDecompress(raw, buffer.getReadIndex(), size, newbuf, 0, len);
					content = Buffer.wrapReadableContent(newbuf, 0, decompressed);
				} catch (Exception e) {
					// logger.error("Failed to uncompress by SNAPPY.", e);
					return false;
				}
				break;
			}
			case SNAPPY: {
				try {
					SnappyBuffer newbuf = SnappyDecompressor.decompress(raw, buffer.getReadIndex(), size);
					content = Buffer.wrapReadableContent(newbuf.getData(), 0, newbuf.getLength());
				} catch (Exception e) {
					// logger.error("Failed to uncompress by SNAPPY.", e);
					return false;
				}

				break;
			}
			case LZF: {
				try {
					byte[] newbuf = LZFDecoder.decode(raw, buffer.getReadIndex(), size);
					content = Buffer.wrapReadableContent(newbuf);
				} catch (Exception e) {
					// logger.error("Failed to uncompress by " + type, e);
					return false;
				}
				break;
			}
			default: {
				break;
			}
			}
			buffer.advanceReadIndex(size);
			ev = EventDispatcher.getSingletonInstance().parse(content);
			return true;
		} catch (Exception e) {
			// logger.error("Failed to decode compress event", e);
			return false;
		}
	}

	@Override
	protected boolean onEncode(Buffer outbuf) {
		BufferHelper.writeVarInt(outbuf, type.getValue());
		Buffer content = new Buffer(256);
		ev.encode(content);
		byte[] raw = content.getRawBuffer();
		switch (type) {
		case NONE: {
			BufferHelper.writeVarInt(outbuf, content.readableBytes());
			outbuf.write(raw, content.getReadIndex(), content.readableBytes());
			break;
		}
		case QUICKLZ: {
			try {
				byte[] newbuf = QuickLZ.compress(raw, content.getReadIndex(), content.readableBytes(), 1);
				BufferHelper.writeVarInt(outbuf, newbuf.length);
				outbuf.write(newbuf);
			} catch (Exception e) {
				// logger.error("Failed to compress by QuickLZ.", e);
				return false;
			}

			break;
		}
		case FASTLZ: {
			byte[] newbuf = new byte[raw.length];
			JFastLZ fastlz = new JFastLZ();
			int afterCompress;
			try {
				afterCompress = fastlz.fastlzCompress(raw, content.getReadIndex(), content.readableBytes(), newbuf, 0,
						newbuf.length);
				BufferHelper.writeVarInt(outbuf, afterCompress);
				outbuf.write(newbuf, 0, afterCompress);
			} catch (IOException e) {
				// logger.error("Failed to compress by FastLZ.", e);
				return false;
			}
			break;
		}
		case SNAPPY: {
			try {
				SnappyBuffer newbuf = SnappyCompressor.compress(raw, content.getReadIndex(), content.readableBytes());
				BufferHelper.writeVarInt(outbuf, newbuf.getLength());
				outbuf.write(newbuf.getData(), 0, newbuf.getLength());
			} catch (Exception e) {
				// logger.error("Failed to compress by Snappy.", e);
				return false;
			}

			break;
		}
		case LZF: {
			try {
				byte[] newbuf = LZFEncoder.encode(raw, content.readableBytes());
				BufferHelper.writeVarInt(outbuf, newbuf.length);
				outbuf.write(newbuf);
			} catch (Exception e) {
				// logger.error("Failed to compress by LZF.", e);
				return false;
			}
			break;
		}
		default: {
			// logger.error("Unsupported compress type.", type);
			return false;
		}
		}
		return true;
	}

}
