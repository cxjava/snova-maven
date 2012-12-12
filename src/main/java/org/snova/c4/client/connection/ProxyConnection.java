/**
 * 
 */
package org.snova.c4.client.connection;

import java.util.concurrent.ThreadPoolExecutor;

import org.arch.buffer.Buffer;
import org.arch.event.Event;
import org.arch.event.EventConstants;
import org.arch.event.EventDispatcher;
import org.arch.event.TypeVersion;
import org.arch.event.http.HTTPEventContants;
import org.arch.event.http.HTTPRequestEvent;
import org.arch.event.misc.CompressEvent;
import org.arch.event.misc.CompressEventV2;
import org.arch.event.misc.EncryptEvent;
import org.arch.event.misc.EncryptEventV2;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snova.c4.client.config.C4ClientConfiguration;
import org.snova.c4.client.config.C4ClientConfiguration.C4ServerAuth;
import org.snova.c4.client.handler.ProxySession;
import org.snova.c4.common.C4Constants;
import org.snova.c4.common.event.SocketConnectionEvent;
import org.snova.c4.common.event.SocketReadEvent;
import org.snova.framework.util.SharedObjectHelper;

/**
 * @author qiyingwang
 * 
 */
public abstract class ProxyConnection
{
	protected static Logger logger = LoggerFactory
	        .getLogger(ProxyConnection.class);
	protected static C4ClientConfiguration cfg = C4ClientConfiguration
	        .getInstance();
	protected static ClientSocketChannelFactory clientChannelFactory;
	protected C4ServerAuth auth = null;
	protected boolean isPullConnection;
	protected boolean isRunning;

	public void setPullConnection(boolean isPullConnection)
	{
		this.isPullConnection = isPullConnection;
	}

	private ProxySession session = null;

	public ProxySession getSession()
	{
		return session;
	}

	public void setSession(ProxySession session)
	{
		this.session = session;
	}

	protected static ClientSocketChannelFactory getClientSocketChannelFactory()
	{
		if (null == clientChannelFactory)
		{
			if (null == SharedObjectHelper.getGlobalThreadPool())
			{
				ThreadPoolExecutor workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(
				        20, 0, 0);
				SharedObjectHelper.setGlobalThreadPool(workerExecutor);

			}
			clientChannelFactory = new NioClientSocketChannelFactory(
			        SharedObjectHelper.getGlobalThreadPool(),
			        SharedObjectHelper.getGlobalThreadPool());

		}
		return clientChannelFactory;
	}

	protected ProxyConnection(C4ServerAuth auth)
	{
		this.auth = auth;
	}

	public C4ServerAuth getC4ServerAuth()
	{
		return auth;
	}

	protected abstract boolean doSend(Buffer msgbuffer);

	protected abstract void doClose();

	public void close()
	{
		doClose();
	}

	public void start()
	{
		isRunning = true;
	}

	public void stop()
	{
		isRunning = false;
	}

	public boolean send(Event event)
	{
		if (event instanceof HTTPRequestEvent)
		{
			CompressEventV2 tmp = new CompressEventV2(cfg.getCompressor(),
			        event);
			tmp.setHash(event.getHash());
			event = tmp;
		}

		EncryptEventV2 enc = new EncryptEventV2(cfg.getEncrypter(), event);
		enc.setHash(event.getHash());
		Buffer msgbuffer = new Buffer(1024);
		enc.encode(msgbuffer);
		return doSend(msgbuffer);
	}

	public void pullData()
	{
		if (isPullConnection && isRunning && null != session)
		{
			SocketReadEvent ev = new SocketReadEvent();
			ev.setHash(session.getSessionID());
			ev.maxread = cfg.getMaxReadBytes();
			ev.timeout = cfg.getHTTPRequestTimeout();
			send(ev);
//			if (logger.isDebugEnabled())
//			{
//				logger.debug("Send pull data request.");
//			}
		}
	}

	protected void handleRecvEvent(Event ev)
	{
		if (null == ev)
		{
			logger.error("NULL event to handle!");
			// close();
			return;
		}

		TypeVersion typever = Event.getTypeVersion(ev.getClass());

		switch (typever.type)
		{
			case EventConstants.COMPRESS_EVENT_TYPE:
			{
				if (typever.version == 1)
				{
					handleRecvEvent(((CompressEvent) ev).ev);
				}
				else if (typever.version == 2)
				{
					handleRecvEvent(((CompressEventV2) ev).ev);
				}

				return;
			}
			case EventConstants.ENCRYPT_EVENT_TYPE:
			{
				if (typever.version == 1)
				{
					handleRecvEvent(((EncryptEvent) ev).ev);
				}
				else if (typever.version == 2)
				{
					handleRecvEvent(((EncryptEventV2) ev).ev);
				}
				return;
			}
			case C4Constants.EVENT_TCP_CHUNK_TYPE:
			case C4Constants.EVENT_TCP_CONNECTION_TYPE:
			case HTTPEventContants.HTTP_CHUNK_EVENT_TYPE:
			case HTTPEventContants.HTTP_RESPONSE_EVENT_TYPE:
			{
				break;
			}
			default:
			{
				logger.error("Unsupported event type:" + typever.type
				        + " for proxy connection");
				break;
			}
		}

		if (null != session)
		{
			session.handleResponse(ev);
		}
		else
		{
			if (logger.isDebugEnabled())
			{
				logger.error("Failed o find session or handle to handle received session["
				        + ev.getHash()
				        + "] response event:"
				        + ev.getClass().getName());
			}
			if (typever.type != C4Constants.EVENT_TCP_CONNECTION_TYPE)
			{
				SocketConnectionEvent tmp = new SocketConnectionEvent();
				tmp.status = SocketConnectionEvent.TCP_CONN_CLOSED;
				tmp.setHash(ev.getHash());
				send(tmp);
			}
		}
	}

	protected void doRecv(Buffer content)
	{
		Event ev = null;
		try
		{
			// int i = 0;
			while (content.readable())
			{
				ev = EventDispatcher.getSingletonInstance().parse(content);
				handleRecvEvent(ev);
				// i++;
			}
		}
		catch (Exception e)
		{
			logger.error(
			        "Failed to parse event while content rest:"
			                + content.readableBytes(), e);
			return;
		}
	}
}
