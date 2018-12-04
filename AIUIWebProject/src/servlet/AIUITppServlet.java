package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSONObject;

import utils.AESUtils;
import utils.Sha1EncodeUtils;


/**
 * Servlet implementation class AIUITppServlet
 */
@WebServlet("/AIUITppServlet")
public class AIUITppServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String token = "c49287381948d874";
	private static final String aeskey = "2a8cbe7ba85d6344";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public AIUITppServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
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

		List<String> list = new ArrayList<String>();

		list.add(token);
		list.add(timestamp);
		list.add(rand);
		Collections.sort(list);

		String localSig = "";
		for (int i = 0; i < list.size(); i++) {
			localSig += list.get(i);
		}

		String sigtureSha1 = Sha1EncodeUtils.encode(localSig);
		if (sigtureSha1.equals(signature)) {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().append(Sha1EncodeUtils.encode(token));
		} else {
			response.getWriter().append("check token failed" + sigtureSha1);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Logger aiui = Logger.getLogger("aiui_get");

		String encrypttype = request.getParameter("encrypttype");
		
		// NOTE !!!! 
		// AES decrypt and encrypt may mot be used in your server
		// you may debug it again and again to make it.
		// welcome to email me: lmliu@iflytek.com if something wrong with you.
		
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
