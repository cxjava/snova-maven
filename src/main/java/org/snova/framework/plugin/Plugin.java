/**
 * This file is part of the hyk-proxy project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Plugin.java 
 *
 * @author yinqiwen [ 2010-6-14 | 07:30:14 PM ]
 *
 */
package org.snova.framework.plugin;

/**
 *
 */
public interface Plugin {
	public void onLoad(PluginContext context) throws Exception;

	public void onActive(PluginContext context) throws Exception;

	public void onUnload(PluginContext context) throws Exception;

	public void onDeactive(PluginContext context) throws Exception;

	public void onStart() throws Exception;

	public void onStop() throws Exception;

	public Runnable getAdminInterface();

}
