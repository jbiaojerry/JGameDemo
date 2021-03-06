package com.metazion.jgd.logic;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.metazion.jgd.action.LogicAction;
import com.metazion.jgd.action.RequestAction;
import com.metazion.jgd.info.ServerConfig;
import com.metazion.jgd.info.ServerInfoManager;
import com.metazion.jgd.util.DbUtil;
import com.metazion.jgd.util.JgdLogger;
import com.metazion.jm.task.TaskManager;
import com.metazion.object.ServerManager;
import com.metazion.object.UserManager;;

public class LogicService {

	private volatile boolean stopDesired = false; // 优雅关闭

	private ConcurrentLinkedQueue<RequestAction> requestActionQueue = new ConcurrentLinkedQueue<RequestAction>();
	private ConcurrentLinkedQueue<LogicAction> logicActionQueue = new ConcurrentLinkedQueue<LogicAction>();
	private TaskManager taskManager = new TaskManager();

	private ServerManager serverManager = new ServerManager(); // 服务器组
	private UserManager userManager = new UserManager(); // 用户

	public boolean init() {
		JgdLogger.getLogger().fatal("Logic service init...");

		boolean result = ServerConfig.getInstance().load();
		if (!result) {
			return false;
		}

		result = ServerInfoManager.getInstance().load();
		if (!result) {
			return false;
		}

		result = DbUtil.init();
		if (!result) {
			return false;
		}

		return true;
	}

	public void start() {
		JgdLogger.getLogger().fatal("Logic service start...");
	}

	public void stop() {
		JgdLogger.getLogger().fatal("Logic service stop...");
	}

	public void tick(long interval) {
		processRequestAction();
		processLogicAction();

		taskManager.tick(interval);
	}

	public void shutdownGracefully() {
		JgdLogger.getLogger().fatal("Logic service shutdownGracefully...");

		stopDesired = true;
	}

	public void pushRequestAction(RequestAction requestAction) {
		// 优雅关闭状态下，不再接受网络请求
		if (stopDesired) {
			return;
		}

		requestActionQueue.add(requestAction);
	}

	public void pushLogicAction(LogicAction logicAction) {
		logicActionQueue.add(logicAction);
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	private void processRequestAction() {
		while (!requestActionQueue.isEmpty()) {
			RequestAction requestAction = requestActionQueue.poll();
			requestAction.execute();
		}
	}

	private void processLogicAction() {
		while (!logicActionQueue.isEmpty()) {
			LogicAction logicAction = logicActionQueue.poll();
			logicAction.execute();
		}
	}
}