/**
 * 
 */
package org.snova.c4.client.handler;

import java.util.Map;

import org.arch.common.Pair;
import org.arch.event.http.HTTPChunkEvent;
import org.arch.event.http.HTTPConnectionEvent;
import org.arch.event.http.HTTPRequestEvent;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qiyingwang
 * 
 */
public class ProxySessionManager
{
	protected static Logger	           logger	     = LoggerFactory
	                                                         .getLogger(ProxySessionManager.class);
	private static ProxySessionManager	instance	 = new ProxySessionManager();
	
	private Map<Integer, ProxySession>	sessionTable	= new ConcurrentHashMap<Integer, ProxySession>();
	
	private ProxySessionManager()
	{

		// new Thread(this).start();
	}
	
	public static ProxySessionManager getInstance()
	{
		return instance;
	}
	
	public void removeSession(ProxySession session)
	{
		synchronized (sessionTable)
		{
			if (null != sessionTable.remove(session.getSessionID()))
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("Destroy Session:" + session.getSessionID());
				}
			}
		}
		
	}
	
	public ProxySession getProxySession(Integer sessionID)
	{
		synchronized (sessionTable)
		{
			return sessionTable.get(sessionID);
		}
	}
	
	private  ProxySession createSession(Integer id, Channel ch)
	{
		ProxySession session = getProxySession(id);
		if (null == session)
		{
			session = new ProxySession(id, ch);
			synchronized (sessionTable)
			{
				sessionTable.put(id, session);
			}
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("Create Session:" + id);
		}
		return session;
	}
	
	public ProxySession handleConnectionEvent(HTTPConnectionEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event
		        .getAttachment();
		// Channel localChannel = attach.first;
		Integer handleID = attach.second;
		// handleID = attach.first.getId();
		ProxySession session = getProxySession(handleID);
		if (null != session)
		{
			if (event.status == HTTPConnectionEvent.CLOSED)
			{
				// removeSession(session);
				session.close();
			}
		}
		else
		{
			// logger.error("Can not find session with session ID:" + handleID);
		}
		return session;
	}
	
	public ProxySession handleRequest(HTTPRequestEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event
		        .getAttachment();
		Channel localChannel = attach.first;
		Integer handleID = attach.second;
		// handleID = attach.first.getId();
		ProxySession session = getProxySession(handleID);
		if (null == session)
		{
			session = createSession(handleID, localChannel);
		}
		event.setHash(handleID);
		session.handle(event);
		return session;
	}
	
	public ProxySession handleChunk(HTTPChunkEvent event)
	{
		Pair<Channel, Integer> attach = (Pair<Channel, Integer>) event
		        .getAttachment();
		// Channel localChannel = attach.first;
		Integer handleID = attach.second;
		// handleID = attach.first.getId();
		ProxySession session = getProxySession(handleID);
		if (null != session)
		{
			session.handle(event);
		}
		return session;
	}
}
