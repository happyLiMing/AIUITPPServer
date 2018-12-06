# AIUITPPServer
# 教你如何一文玩转AIUI后处理

-	刘立明 		 2018/12/6

- 该工程是讯飞AIUI开放平台后处理接入的web服务器示例工程。

本文将介绍如何使用AIUI开放平台后处理功能。如果这是你现在想做或正在做的，那么这篇文章适合你。
如果你是服务开发小白，但是因公司业务需要你不得不临危受命从零开始接入后处理，那么这篇文章同样非常适合你。

##目录：
### 0. AIUI为什么要支持后处理
### 1. AIUI与后处理服务通信
### 2. 申请公网云服务器
### 3. 开发部署后处理服务
### 4. AIUI后处理协议及代码解读
### 5. 反思总结

## AIUI为什么要支持后处理
开发者在集成使用AIUI的过程中，因为业务需求需要请求自己或第三方的业务服务器从而实现业务扩展。假如A公司需要集成AIUI的能力，但同时也需要结合自己的业务数据进行产品服务升级。通常情况下，A公司的业务服务器在数据格式、协议都是严格制定好并且不能轻易修改的。第三方服务也类似，他们一般会返回一个通用的、标准的格式给用户。在这种情境下，A公司的开发人员不想因为接入AIUI而去修改这些既定的数据格式和协议。于是，AIUI后处理在这种需求场景下孕育诞生了。为了满足后处理和AIUI中间通信需求但又避免更改后处理业务数据的情况，AIUI服务和后处理服务之间的通信设计非常之巧妙。那么他们是如何通信的呢？

## AIUI与后处理服务如何器通信
AIUI与后处理服务器进行数据通信之前需要先经过“握手”认证校验，从而标记该请求来自AIUI服务器。具体的认证校验方法我们在AIUI后处理协议解读中会展开介绍，先做先做整体把握。音频或文本从客户端（SDK/Webapi/智能硬件）上传至AIUI服务器之后，AIUI经过识别+语义理解引擎后将识别文本和语义理解结果以标准HTTP请求的方式POST到配置的后处理服务器，后处理服务器接到请求后做相关业务处理后返回语义结果数据给AIUI服务器，AIUI服务器将后处理结果推送至客户端，客户端根据根据[AIUI结果格式](https://aiui.xfyun.cn/access_docs/aiui-sdk/smart_doc/%E7%BB%93%E6%9E%9C%E8%A7%A3%E6%9E%90.html)解析后处理结果。


## 申请公网云服务器
现在提供云服务的大厂非常多，笔者调研了一番，从阿里云购买了一周的云服务，镜像选择ubuntu 16.06 64bit。申请服务后需要创建实例，这个过程不同的云服务大厂提供的方法大同小异，也提供较为全面的操作文档。经过一番折腾，一个云端ubuntu便跑起来了，登陆后发现的确是一个ubuntu，更关键的是成功拿到一个公网ip，心中一喜：有戏！接下来就要搭建web服务了，因为我开始的时候计划好通过java来写服务，所以去下载了jdk和tomcat，安装在Ubuntu上，过程比较顺利。安装方法和验证步骤不在此赘述，需要的朋友请移步xxxxx（ 记得回来的路:) ） 服务就绪之后，tomcat运行起来了。
通过远程浏览器输入 公网ip/8080 我期待会出现tomcat的页面出来，结果却发现失败。一番排查，发现阿里云服务需要手动添加8080端口进入安全组。添加步骤：xxxxx 开通之后，回来浏览器刷新页面，OK tomcat出来了！至此，我相信我可以通过远程访问tomcat了，那么接下来要做的就是开发服务并部署到服务器让他运行起来。

	注意：笔者在调研这几个云服务大厂时，发现网易云服务虽可以免费可以测试，但是不提供公网ip，这个是比较痛苦的。华为云、腾讯云等申请免费的体验服务要等到第二天上午10:00 为了节省时间，就直接在阿里云买了一周的时间来验证使用。

## 开发部署后处理服务

笔者之前没有做过服务开发，一直在AIUI客户端SDK搬砖。对服务端开发完全小白，在调研之后大概知道比较初级的做法是开发部署servlet，既然如此，了解了基本工具，下载了Myeclipse安装来开发。大家对Myeclipse的使用应该非常熟悉了，就不在此赘述。创建好一个web项目，添加servlet之后简单打点日志直接在浏览器页面输出，从而验证链路是否连通。工程Export出war包之后，通过ssh工具或者图形化工具FileZilla传到tomcat安装目录下的webapp下面。
为了模拟http请求，于是写了一个python 脚本：

```
#### -*- coding:utf-8 -*-
import requests
 
###### 以下为GET请求
url = 'http://XXXXXXXXX:8080/AIUIWebProject/AIUITppServlet'
requests = requests.get(url)
print requests.content  # 返回字节形式
```


F5运行，发现浏览器输出了成功接收请求的状态。
到此，我们可以确定Web服务接入是成功的，并且可以正常接收HTTP请求。

##AIUI后处理协议及代码解读
AIUI后处理协议在开启后处理选项之后能看到入口。接入AIUI后处理有两项工作验证：

1. 服务器接入验证
2. 交互时通信
服务器接入验证的目的是为了能够使得我们自己部署的服务器能够区分该请求来自AIUI服务器从而做处理

### 服务器接入验证
我们在前面章节提到AIUI服务与后处理服务通信之前需要进行认证校验，认证校验的目的有两个，一个是标志后处理服务是可以正常运行的，另一个是有办法让后处理服务知道什么请求来自AIUI服务器。
所以，我们在分析AIUI后处理协议文档时能看到AIUI服务于后处理服务认证时，看到协议文档说明的校验流程大致如下：

1. AIUI会以HTTP GET请求的方式携带signature/timestamp和rand随机数到后处理服务。
2. AIUI后处理服务接收该请求后将三个字符做字典排序后串拼成一个字符串进行sha1加密。
3. AIUI后处理将加密后的字符串与signature进行对比，如果一致则表示该请求来自AIUI服务。
4. 校验通过后将token与sha1进行加密，将加密后的消息放在body中返回给AIUI服务器。

消息格式我们不在此赘述，线上文档写的比较清晰，大家可以参考翻阅。

### 后处理服务接收消息
AIUI服务讲识别和语义结果以标准的协议格式POST到后处理服务器上，AIUI开放平台可以勾选消息是否以AES加密的方式来发送，如果不做加密，那么传送的就是明文的base64编码，如果加密需要注意在后处理端以AEC/CBC/PKSC7padding的解密方式解密，秘钥和初始化向量公用，均为16位。
为了更直观的进行说明，我们将关键代码附上：


```
public class AIUITppServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String token = "XXXXXXXXXXX";
	private static final String aeskey = "YYYYYYYYYY";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AIUITppServlet() {
		super();
	}
	
	//get请求，用于服务校验认证阶段
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String signature = request.getParameter("signature");
		String timestamp = request.getParameter("timestamp");
		String rand = request.getParameter("rand");

		if (signature == null || timestamp == null || rand == null) {
			return;
		}
		if (signature.isEmpty() || rand.isEmpty() || timestamp.isEmpty()) {
			return;
		}

		//对三个参数进行字典排序
		List<String> list = new ArrayList<String>();

		list.add(token);
		list.add(timestamp);
		list.add(rand);
		Collections.sort(list);

		String localSig = "";
		for (int i = 0; i < list.size(); i++) {
			localSig += list.get(i);
		}
		//sha1加密
		String sigtureSha1 = Sha1EncodeUtils.encode(localSig);
		
		//参数校验通过，返回token给AIUI，否则自行处理
		if (sigtureSha1.equals(signature)) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().append(Sha1EncodeUtils.encode(token));
		} else {
			//这里可以不用发送，写入日志系统或告警组件即可，AIUI不会接收处理。
			response.getWriter().append("check token failed" + sigtureSha1);
		}
	}

	//消息通信阶段
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Logger aiui = Logger.getLogger("aiui_get");

		String encrypttype = request.getParameter("encrypttype");
		
		// NOTE !!!! 
		// AES decrypt and encrypt may mot be used in your server
		// you may debug it again and again to make it.
		// welcome to email me: lmliu@iflytek.com if something wrong with you.
		
		// 消息加密，解密
		if (encrypttype.equals("aes")) {
			
			aiui.log(Level.INFO, "AES Decrypt Mode.");
			
			int len = request.getContentLength();
			ServletInputStream inputStream = request.getInputStream();
			byte[] buffer = new byte[len];
			inputStream.read(buffer, 0, len);
			
			//FileUtils.saveBytesToFile(buffer);
			try {
				String content = AESUtils.decrypt(aeskey, aeskey, buffer);
				aiui.log(Level.INFO, "decrypt data: " + content);
			} catch (Exception e) {
				e.printStackTrace();
				aiui.log(Level.WARNING, "error when decrypt data " + e.getMessage());
			}
			//构建自定义格式数据给AIUI服务器，AIUI服务器全量下发给客户端。
			JSONObject customData = new JSONObject();
			customData.put("key", "custome");
			customData.put("content", "这是一条来自后处理的测试结果");
			
			
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().append(AESUtils.encrypt(aeskey, aeskey, customData.toString()));
			
		} else {
			aiui.log(Level.INFO, "Normal Mode.");
			// get request body.
			ServletInputStream inputStream = request.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line = "";
			StringBuilder aiuiPostData = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				aiuiPostData.append(line);
			}

			aiui.log(Level.INFO, "aiui data post " + aiuiPostData.toString());
			
			JSONObject aiuiPostJson = (JSONObject) JSONObject.parse(aiuiPostData.toString());
			String sub = aiuiPostJson.getString("FromSub");

			aiui.log(Level.INFO, "aiui data sub  = " + sub);

			JSONObject msgJson = aiuiPostJson.getJSONObject("Msg");
			String content = msgJson.getString("Content");

			content = new String(Base64.decodeBase64(content.getBytes()));
			
			aiui.log(Level.INFO, "content = " + content);
			
			// do something what you wanted.
			// For example, request third platform or your own server, etc.
			
			// ....
			
			// build your custom data used to transmit to client(sdk/webapi) which can get
			// these data from sub "tpp". "key" and "content" are just samples, not necessary.
			JSONObject customData = new JSONObject();
			customData.put("key", "custom");
			customData.put("content", "这是一条来自后处理的测试结果");
			
			aiui.log(Level.INFO, "resonse: customData  = " + customData.toJSONString());
			
			//repsonse to AIUI server
			response.setContentType("application/json;charset=utf-8");
			response.getWriter().append(customData.toString());
		}
	}
}
```
-

##反思总结

集成AIUI示例的过程中遇到一个比较头疼的问题就是后处理服务是java开发的，而jdk默认是不支持AEC/CBC/PKSC7padding加解密方式，在解密AIUI服务POST过来的数据时反复解密验证始终包。这里有两个需要注意的问题点是使用java在进行加解密时需要添加BouncyCastleProvider,需要导入jar包bcprov-jdkXXX.jar
关键代码如下：

```
 public static String decrypt(String secretKey, String ivKey, byte[] content){
        String encryptMode = "AES/CBC/PKCS7Padding";
        
        byte[] secrecKeyByte = secretKey.getBytes(CHARSET_U8);
       
        String decryptContent = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            SecretKeySpec keyspec = new SecretKeySpec(secrecKeyByte, "AES");
            Cipher cipher = Cipher.getInstance(encryptMode, "BC");
            
            IvParameterSpec iv = new IvParameterSpec(ivKey.getBytes(CHARSET_U8));
            cipher.init(Cipher.DECRYPT_MODE, keyspec, iv);
            byte[] byte_content = cipher.doFinal(content);

            decryptContent = new String(byte_content, CHARSET_U8_STR);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return decryptContent;
    }
```   







