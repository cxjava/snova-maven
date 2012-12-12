/**
 * This file is part of the hyk-proxy-framework project.
 * Copyright (c) 2010 Yin QiWen <yinqiwen@gmail.com>
 *
 * Description: Misc.java 
 *
 * @author yinqiwen [ 2010-8-17 | 11:36:30 AM ]
 *
 */
package org.snova.framework.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 *
 */
public class SharedObjectHelper {
	private static ExecutorService globalThreadPool;
	private static ScheduledExecutorService globalTimer;

	public static ScheduledExecutorService getGlobalTimer() {
		if (null == globalTimer) {
			globalTimer = new ScheduledThreadPoolExecutor(10);
		}
		return globalTimer;
	}

	public static void setGlobalTimer(ScheduledExecutorService globalTimer) {
		if (null != SharedObjectHelper.globalTimer) {
			SharedObjectHelper.globalTimer.shutdown();
			SharedObjectHelper.globalTimer = null;
		}
		SharedObjectHelper.globalTimer = globalTimer;
	}

	public static ExecutorService getGlobalThreadPool() {
		return globalThreadPool;
	}

	public static void setGlobalThreadPool(ExecutorService globalThreadPool) {
		if (null != SharedObjectHelper.globalThreadPool) {
			SharedObjectHelper.globalThreadPool.shutdown();
		}
		SharedObjectHelper.globalThreadPool = globalThreadPool;
	}

}
