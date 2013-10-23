/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.invoker.filter;

import com.dianping.dpsf.exception.NetException;
import com.dianping.pigeon.component.invocation.InvocationRequest;
import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.remoting.common.filter.ServiceInvocationHandler;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.component.InvokerMetaData;
import com.dianping.pigeon.remoting.invoker.component.RemoteInvocationBean;
import com.dianping.pigeon.remoting.invoker.component.async.Callback;
import com.dianping.pigeon.remoting.invoker.component.async.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceCallbackWrapper;
import com.dianping.pigeon.remoting.invoker.component.async.ServiceFutureImpl;
import com.dianping.pigeon.remoting.invoker.component.context.InvokerContext;
import com.dianping.pigeon.remoting.invoker.service.ServiceInvocationRepository;

/**
 * 执行实际的Remote Call，包括Sync, Future，Callback，Oneway
 * 
 * @author danson.liu
 */
public class RemoteCallInvokeFilter extends InvocationInvokeFilter {

	private static ServiceInvocationRepository invocationRepository = ServiceInvocationRepository.getInstance();
	private static final InvocationResponse NO_RETURN_RESPONSE = new NoReturnResponse();

	@Override
	public InvocationResponse invoke(ServiceInvocationHandler handler, InvokerContext invocationContext)
			throws Throwable {

		Client client = invocationContext.getClient();
		InvocationRequest request = invocationContext.getRequest();
		InvokerMetaData metaData = invocationContext.getMetaData();
		String callMethod = metaData.getCallMethod();
		beforeInvoke(request, client.getAddress());
		InvocationResponse response = null;
		if (Constants.CALL_SYNC.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new CallbackFuture();
			sendRequest(client, request, future);
			response = future.get(metaData.getTimeout());
		} else if (Constants.CALL_CALLBACK.equalsIgnoreCase(callMethod)) {
			sendRequest(client, request, new ServiceCallbackWrapper(metaData.getCallback()));
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_FUTURE.equalsIgnoreCase(callMethod)) {
			CallbackFuture future = new ServiceFutureImpl(metaData.getTimeout());
			sendRequest(client, request, future);
			invocationContext.putTransientContextValue(Constants.CONTEXT_FUTURE, future);
			response = NO_RETURN_RESPONSE;
		} else if (Constants.CALL_ONEWAY.equalsIgnoreCase(callMethod)) {
			sendRequest(client, request, null);
			response = NO_RETURN_RESPONSE;
		} else {
			throw new RuntimeException("Call method[" + callMethod + "] is not supported!");
		}
		afterInvoke(request, client);
		return response;
	}

	private void sendRequest(Client client, InvocationRequest request, Callback callback) {
		if (request.getCallType() == Constants.CALLTYPE_REPLY) {
			RemoteInvocationBean invocationBean = new RemoteInvocationBean();
			invocationBean.request = request;
			invocationBean.callback = callback;
			callback.setRequest(request);
			callback.setClient(client);
			invocationRepository.put(request.getSequence(), invocationBean);
		}
		try {
			// request.setUniformContextHeaders(setupUniformContext(client,
			// request));//构建统一上下文
			client.write(request, callback);
			// 记录当前外围服务调用情况分析
		} catch (RuntimeException e) {
			invocationRepository.remove(request.getSequence());
			throw new NetException("Send request to service provider failed.", e);
		}
	}

	/**
	 * TODO 请继续完善....
	 * 
	 * @param client
	 * @param request
	 * @return
	 */
	// private UniformContextHeaders setupUniformContext(Client client,
	// DPSFRequest request) {
	//
	// //serviceinvokeid必须保持一致...查询出来
	// UniformContextHeaders uniformContextHeaders =
	// ContextLifeManager.checkAPIQuery();
	// if (uniformContextHeaders == null) {
	// uniformContextHeaders =
	// ContextLifeManager.checkEntrance(null);//恭喜你，第一个入口
	// }
	// uniformContextHeaders.getCaller().setIp("ip");//从配置文件中获得
	// uniformContextHeaders.getCaller().setAppName("appName");//从配置文件中获得
	// uniformContextHeaders.getCaller().setRequestTime(ContextUtils.getCurrentTime());
	// uniformContextHeaders.getCallee().setAppName(client.getHost());//当前还没有构建好下来的appname
	// uniformContextHeaders.getCallee().setIp(client.getAddress());
	// uniformContextHeaders.setVersion("2.0.0");//从配置文件中获得，或者version类里面得到
	//
	// return uniformContextHeaders;
	//
	// }

	static class NoReturnResponse implements InvocationResponse {

		/**
		 * serialVersionUID
		 */
		private static final long serialVersionUID = 4348389641787057819L;

		@Override
		public void setMessageType(int messageType) {
		}

		@Override
		public int getMessageType() {
			return 0;
		}

		@Override
		public String getCause() {
			return null;
		}

		@Override
		public Object getReturn() {
			return null;
		}

		@Override
		public void setReturn(Object obj) {
		}

		@Override
		public byte getSerializ() {
			return 0;
		}

		@Override
		public void setSequence(long seq) {
		}

		@Override
		public long getSequence() {
			return 0;
		}

		@Override
		public Object getObject() {
			return null;
		}

		@Override
		public Object getContext() {
			return null;
		}

		@Override
		public void setContext(Object context) {
		}
	}

}
