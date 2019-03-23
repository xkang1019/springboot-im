package org.jim.server.server;

import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImConfig;
import org.jim.common.ImConst;
import org.jim.common.config.PropertyImConfigBuilder;
import org.jim.common.packets.Command;
import org.jim.server.ImServerStarter;
import org.jim.server.command.CommandManager;
import org.jim.server.command.DemoWsHandshakeProcessor;
import org.jim.server.command.handler.HandshakeReqHandler;
import org.jim.server.command.handler.LoginReqHandler;
import org.jim.server.listener.ImDemoGroupListener;
import org.jim.server.service.LoginServiceProcessor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.tio.core.ssl.SslConfig;

import com.jfinal.kit.PropKit;

@Component
public class ImServerDemoStart implements CommandLineRunner{

	/**
	 * HTTP协议发送消息接口
	 * 		http://localhost:8888/api/message/send
	 * 		消息格式：
	 *			    {
	 *				   "from": "来源ID",
	 *				   "to": "目标ID",
	 *				   "cmd":"命令码(11)int类型",
	 *				   "createTime": "消息创建时间long类型",
	 *				   "msgType": "消息类型int类型(0:text、1:image、2:voice、3:vedio、4:music、5:news)",
	 *				   "chatType":"聊天类型int类型(0:未知,1:公聊,2:私聊)",
	 *				   "group_id":"群组id仅在chatType为(1)时需要,String类型",
	 *				   "content": "内容",
	 *				   "extras" : "扩展字段,JSON对象格式如：{'扩展字段名称':'扩展字段value'}"
	 *				}
	 *		消息示例：
	 * 				{
	 * 					'from':'15313882282382032',
	 *					'to':'1531388228238203227',
	 *					'cmd':11,
	 *					'msgType':0,
	 *					'content':"123", 
	 *					"chatType":2
	 *			  	}
	 */
	@Override
	public void run(String... args) throws Exception {
		ImConfig imConfig = new PropertyImConfigBuilder("jim.properties").build();
		//初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
		initSsl(imConfig);
		//设置群组监听器，非必须，根据需要自己选择性实现;
		imConfig.setImGroupListener(new ImDemoGroupListener());
		ImServerStarter imServerStarter = new ImServerStarter(imConfig);
		/*****************start 以下处理器根据业务需要自行添加与扩展，每个Command都可以添加扩展,此处为demo中处理**********************************/
		HandshakeReqHandler handshakeReqHandler = CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
		//添加自定义握手处理器;
		handshakeReqHandler.addProcessor(new DemoWsHandshakeProcessor());
		LoginReqHandler loginReqHandler = CommandManager.getCommand(Command.COMMAND_LOGIN_REQ,LoginReqHandler.class);
		//添加登录业务处理器;
		loginReqHandler.addProcessor(new LoginServiceProcessor());
		/*****************end *******************************************************************************************/
		imServerStarter.start();
	}
	
	/**
	 * 开启SSL之前，你要保证你有SSL证书哦！
	 * @param imConfig
	 * @throws Exception
	 */
	private static void initSsl(ImConfig imConfig) throws Exception {
		//开启SSL
		if(ImConst.ON.equals(imConfig.getIsSSL())){
			String keyStorePath = PropKit.get("jim.store.path");
			String keyStoreFile = keyStorePath;
			String trustStoreFile = keyStorePath;
			String keyStorePwd = PropKit.get("jim.key.store.pwd");
			if (StringUtils.isNotBlank(keyStoreFile) && StringUtils.isNotBlank(trustStoreFile)) {
				SslConfig sslConfig = SslConfig.forServer(keyStoreFile, trustStoreFile, keyStorePwd);
				imConfig.setSslConfig(sslConfig);
			}
		}
	}
}