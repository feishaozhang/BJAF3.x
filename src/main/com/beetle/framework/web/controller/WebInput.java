/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.beetle.framework.AppProperties;
import com.beetle.framework.AppRuntimeException;
import com.beetle.framework.util.ConvertUtil;
import com.beetle.framework.util.ObjectUtil;
import com.beetle.framework.util.encrypt.RSAEncrypt;
import com.beetle.framework.web.common.CommonUtil;
import com.beetle.framework.web.common.WebUtil;
import com.beetle.framework.web.common.XssHtmlWhitelist;

/**
 * <p>
 * Title: BeetleWeb
 * </p>
 * 
 * <p>
 * Description: Web输入参数对象，对request的封装
 * 
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: 甲壳虫软件
 * 
 * </p>
 * 
 * @author 余浩东(hdyu@beetlesoft.net)
 * @version 1.0
 */
/**
 * @author yuhaodong@gmail.com
 *
 */
public class WebInput {
	private HttpServletRequest request;

	private HttpServletResponse response;

	public WebInput(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		/*
		 * Object o = request.getAttribute("WEB_ENCODE_CHARSET"); if (o == null)
		 * { o = System.getProperty("file.encoding"); } try {
		 * this.request.setCharacterEncoding(o.toString()); } catch
		 * (UnsupportedEncodingException ex) { ex.printStackTrace(); }
		 */
	}

	/**
	 * 检查请求是否为Http的Get方法请求
	 * 
	 * @return true-yes
	 */
	public boolean isHttpGetRequest() {
		if (request.getMethod().equalsIgnoreCase("get")) {
			return true;
		}
		return false;
	}

	/**
	 * 取消控制器的Session检查功能,此方法提供了一个在原有控制器已设置了Session检查的情况下，
	 * 针对某个请求，取消此控制器Session检查功能的可能。我们一般会在前置回调ICutFrontAction中使用
	 */
	public void disableControllerSessionCheck() {
		request.setAttribute(CommonUtil.CANCEL_SESSION_CHECK_FLAG, "yesCancel");
	}

	public Principal getUserPrincipal() {
		return request.getUserPrincipal();
	}

	public boolean isUserInRole(java.lang.String role) {
		return request.isUserInRole(role);
	}

	/**
	 * 获取当然控制器的名称
	 * 
	 * @return String
	 */
	public String getControllerName() {
		return (String) request.getAttribute(CommonUtil.controllname);
	}

	public String getAuthType() {
		return request.getAuthType();
	}

	public String getServletPath() {
		return request.getServletPath();
	}

	public String getRemoteUser() {
		return request.getRemoteHost();
	}

	/**
	 * 返回控制器的动作名称（针对多动作控制器）
	 * 
	 * @return
	 */
	public String getActionName() {
		String a = request.getParameter(CommonUtil.ACTION_STR);
		if (a == null || a.length() < 1) {
			a = "default";
		}
		return a;
	}

	public String getRemoteAddr() {
		return request.getRemoteAddr();
	}

	public String getRemoteHost() {
		return request.getRemoteHost();
	}

	public boolean isSecure() {
		return request.isSecure();
	}

	public String getCharacterEncoding() {
		return request.getCharacterEncoding();
	}

	public String getSessionId() {
		HttpSession ss = this.getSession();
		if (ss == null) {
			return null;
		} else {
			return ss.getId();
		}
	}

	public Cookie getCookie(String cookieName) {
		Cookie cks[] = getCookies();
		if (cks == null) {
			return null;
		} else {
			for (int i = 0; i < cks.length; i++) {
				Cookie ck = cks[i];
				String name = ck.getName();
				if (name.equals(cookieName)) {
					return ck;
				}
			}
			return null;
		}
	}

	/**
	 * 获取验证码值 与com.beetle.framework.web.draw.VerifyCodeDraw一起使用
	 * 
	 * 
	 * @return String
	 */
	public String getVerifyCode() {
		Cookie ck = getCookie("VerifyCodeDraw");
		if (ck != null) {
			return ck.getValue();
		} else {
			return null;
		}
	}

	public void removeVerifyCodeValue() {
		Cookie ck = getCookie("VerifyCodeDraw");
		if (ck != null) {
			ck.setMaxAge(0);
			ck.setValue("");
			ck.setPath("/");
			this.addCookie(ck);
		}
	}

	/**
	 * Adds the specified cookie to the response. This method can be called
	 * multiple times to set more than one cookie.
	 * 
	 * @param cookie
	 *            cookie - the Cookie to return to the client
	 */
	public void addCookie(Cookie cookie) {
		response.addCookie(cookie);
	}

	/**
	 * 根据cookie名称删除此cookie，如果cookie不存在，则跳过（不处理）
	 * 
	 * @param cookieName
	 */
	public void deleteCookie(String cookieName) {
		Cookie ck = getCookie(cookieName);
		if (ck != null) {
			ck.setMaxAge(0);
			ck.setValue("");
			ck.setPath("/");
			this.addCookie(ck);
		}
	}

	public void addHeader(String key, String value) {
		response.addHeader(key, value);
	}

	public void addIntHeader(java.lang.String name, int value) {
		response.addIntHeader(name, value);
	}

	public void addDateHeader(java.lang.String name, long date) {
		response.addDateHeader(name, date);
	}

	/**
	 * 在服务端设置请求参数，其参数放在httprequest对象里面<br>
	 * 此方便一般不使用，只有在客户端请求不存在此参数，而这个请求在本控制器或处理方面内不完全处理，需要传递到后续的类处理时，
	 * 有缺少一些请求参数实时使用
	 * @param key
	 * @param value
	 */
	public void setRequestParameter(String key, String value) {
		request.setAttribute(key, value);
	}

	public void setHeader(String key, String value) {
		response.setHeader(key, value);
	}

	/**
	 * Returns an array containing all of the Cookie objects the client sent
	 * with this request. This method returns null if no cookies were sent.
	 * 
	 * @return Cookie[]
	 */
	public Cookie[] getCookies() {
		return request.getCookies();
	}

	/**
	 * Returns the value of the specified request header as a String. If the
	 * request did not include a header of the specified name, this method
	 * returns null. The header name is case insensitive. You can use this
	 * method with any request header.
	 * 
	 * @param name
	 *            String
	 * @return String
	 */
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	/**
	 * Returns the current session associated with this request, or if the
	 * request does not have a session, creates one.
	 * 
	 * @return HttpSession
	 */
	public HttpSession getSession() {
		return request.getSession();
	}

	/**
	 * 针对jwt验证，获取登录用户id，如果不存在返回未null
	 * 
	 * @return
	 */
	public String getJwtTokenLoginUserId() {
		return (String) request.getAttribute("getJwtTokenLoginUserId");
	}

	public void bindJwtTokenLoginUserIdInRequest(String jwtTokenLoginUserId) {
		request.setAttribute("getJwtTokenLoginUserId", jwtTokenLoginUserId);
	}

	/**
	 * Returns the current HttpSession associated with this request or, if if
	 * there is no current session and create is true, returns a new session. If
	 * create is false and the request has no valid HttpSession, this method
	 * returns null. To make sure the session is properly maintained, you must
	 * call this method before the response is committed. If the container is
	 * using cookies to maintain session integrity and is asked to create a new
	 * session when the response is committed, an IllegalStateException is
	 * thrown.
	 * 
	 * @param create
	 *            boolean
	 * @return HttpSession
	 */
	public HttpSession getSession(boolean create) {
		return request.getSession(create);
	}

	/**
	 * 获取字段值 注意使用本方法会自动trim()调字符串前后的空格，如果你想保留可以使用getParameterWithoutTrim()方法<br>
	 * 为了防止XSS脚本攻击，默认进行了html标签过滤，过滤等级可通过application.properties[
	 * web_xss_html_filter_defaultLevel]配置<br>
	 * 
	 * @name - a String specifying the name of the parameter
	 * @return a String representing the single value of the parameter
	 */
	public String getParameter(String name) {
		String r = request.getParameter(name);
		// 当年针对tomcat低版本做编码转换解决乱码问题，现在tomcat已经成熟，注释掉提供性能，2016-10-10
		// if (request.getMethod().toLowerCase().equals(CommonUtil.GET_STR)) {
		// String info = (String)
		// request.getAttribute(CommonUtil.WEB_SERVER_INFO);
		// if (info != null) {
		// if (info.indexOf(CommonUtil.TOMCAT_STR) > 0) {
		// try {
		// if (r != null) {
		// r = new String(r.getBytes("8859_1"));
		// }
		// } catch (UnsupportedEncodingException ex) {
		// r = request.getParameter(name);
		// }
		// }
		// }
		// }
		if (r != null) {
			r = r.trim();
			return WebUtil.xssFilter(r);
		}
		return r;
	}

	/**
	 * 根据name获取参数值（trim过），如果此值为null或者空字符串，则返回输入参数的默认值
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getParameter(String name, String defaultValue) {
		String x = getParameter(name);
		if (x == null || x.trim().length() == 0) {
			return defaultValue;
		}
		return x;
	}

	public String getParameterWithoutTrim(String name) {
		String x = request.getParameter(name);
		if (x == null) {
			return null;
		}
		return WebUtil.xssFilter(x);
		// return request.getParameter(name);
	}

	/**
	 * 以当前系统默认文件编码解码
	 * 
	 * @param name
	 * @return
	 */
	public String getParameterWithDecode(String name) {
		return WebUtil.decodeURL(this.getParameter(name));
	}

	/**
	 * 根据html过滤级别获取过滤后的字符值
	 * 
	 * @param name
	 *            字段名
	 * @param level
	 *            过滤级别
	 * @return
	 */
	public String getParameterWithHtmlFilterLevel(String name, XssHtmlWhitelist level) {
		String x = request.getParameter(name);
		if (x == null) {
			return null;
		}
		return WebUtil.xssFilter(x, level.toString());
	}

	public String getParameterWithDecode(String name, String charset) {
		return WebUtil.decodeURL(this.getParameter(name), charset);
	}

	/**
	 * 获取页面输入参数原始值（没有做任何xxs等处理）<br>
	 * 为了安全性，一般不推荐使用，除非要做特殊处理的时候才可以使用
	 * 
	 * @param name
	 * @return
	 */
	public String getParameterOriginalValue(String name) {
		return request.getParameter(name);
	}

	public Float getParameterAsFloat(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Float.valueOf(r.trim());
		// return Float.parseFloat(r.trim());
	}

	public Float getParameterAsFloat(String name, float defaultValue) {
		String r = request.getParameter(name);
		if (r == null) {
			return defaultValue;
		} else if (r.trim().equals("")) {
			return defaultValue;
		}
		return Float.valueOf(r.trim());
		// return Float.parseFloat(r.trim());
	}

	public Integer getParameterAsInteger(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Integer.valueOf(r.trim());
	}

	public int getParameterAsInteger(String name, int defaultValue) {
		String r = request.getParameter(name);
		if (r == null) {
			return defaultValue;
		} else if (r.trim().equals("")) {
			return defaultValue;
		}
		// return Integer.parseInt(r.trim());
		return Integer.valueOf(r.trim());
	}

	public Double getParameterAsDouble(String name, double defaultValue) {
		String r = request.getParameter(name);
		if (r == null) {
			return defaultValue;
		} else if (r.trim().equals("")) {
			return defaultValue;
		}
		return Double.valueOf(r.trim());
	}

	public Double getParameterAsDouble(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}
		return Double.valueOf(r.trim());
	}

	public Long getParameterAsLong(String name, long defaultValue) {
		String r = request.getParameter(name);
		if (r == null) {
			return defaultValue;
		} else if (r.trim().equals("")) {
			return defaultValue;
		} else {
			return Long.valueOf(r.trim());
		}
	}

	public Long getParameterAsLong(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return Long.valueOf(r.trim());
		}
	}

	/**
	 * yyyy-mm-dd hh:mm:ss.fffffffff
	 * 
	 * @param name
	 * @return
	 */
	public Timestamp getParameterAsTimestamp(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			String a = r.trim();
			if (a.indexOf(':') < 0) {
				a = a + " 00:00:00";
			}
			return Timestamp.valueOf(a);
		}
	}

	public Timestamp getParameterAsTimestamp(String name, String formatStr) {
		Date d = ConvertUtil.toDateByFormat(request.getParameter(name), formatStr);
		return new Timestamp(d.getTime());
	}

	public BigDecimal getParameterAsBigDecimal(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		} else {
			return new BigDecimal(r.trim());
		}
	}

	public BigDecimal getParameterAsBigDecimal(String name, BigDecimal defaultValue) {
		String r = request.getParameter(name);
		if (r == null) {
			return defaultValue;
		} else if (r.trim().equals("")) {
			return defaultValue;
		} else {
			return new BigDecimal(r.trim());
		}
	}

	public Time getParameterAsTime(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}

		else {
			return Time.valueOf(r.trim());
		}
	}

	/**
	 * 获取整个页面的内容（不包括header） 如果出现异常，则返回null<br>
	 * 为了防止XSS脚本攻击，默认进行了html标签过滤，过滤等级可通过application.properties[
	 * web_xss_html_filter_defaultLevel]配置<br>
	 * 
	 * @return
	 */
	public String getBodyContent() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(this.getInputStream(), this.getCharacterEncoding()));
			StringBuilder sb = new StringBuilder();
			while (true) {
				String x = reader.readLine();
				if (x == null) {
					break;
				}
				sb.append(x);
			}
			// return sb.toString();
			return WebUtil.xssFilter(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取整个页面的内容（不包括header） 如果出现异常，则返回null<br>
	 * 可根据html过滤级别
	 * 
	 * @param level
	 * @return
	 */
	public String getBodyContentWithHtmlFilterLevel(XssHtmlWhitelist level) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(this.getInputStream(), this.getCharacterEncoding()));
			StringBuilder sb = new StringBuilder();
			while (true) {
				String x = reader.readLine();
				if (x == null) {
					break;
				}
				sb.append(x);
			}
			// return sb.toString();
			return WebUtil.xssFilter(sb.toString(), level.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取json格式内容，并转换成javabean 如果出错或没有数据则返回null<br>
	 * 为了防止XSS脚本攻击，默认进行了html标签过滤，过滤等级可通过application.properties[
	 * web_xss_html_filter_defaultLevel]配置<br>
	 * 
	 * @param formBeanClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getJsonBodyContentAsBean(Class<T> formBeanClass) {
		String x = this.getBodyContent();
		if (x == null) {
			return null;
		}
		String jflag = AppProperties.get("web_ws_jsonProcessor", "Jackson");
		if (jflag.equalsIgnoreCase("XStream")) {
			Object o = ObjectUtil.jsonToObjectWithXStream(x);
			return (T) o;
		} else if (jflag.equalsIgnoreCase("Jackson")) {
			return ObjectUtil.jsonToObjectWithJackson(x, formBeanClass);
		} else {
			throw new AppRuntimeException("not support yet!");
		}
	}

	/**
	 * 获取xml格式内容，并转化成相应的javabean<br>
	 * 为了防止XSS脚本攻击，默认进行了html标签过滤，过滤等级可通过application.properties[
	 * web_xss_html_filter_defaultLevel]配置<br>
	 * 
	 * @param formBeanClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getXmlBodyContentAsBean(Class<T> formBeanClass) {
		String x = this.getBodyContent();
		if (x == null) {
			return null;
		}
		return (T) ObjectUtil.xmlToObject(x);
	}

	public java.sql.Date getParameterAsDate(String name) {
		String r = request.getParameter(name);
		if (r == null) {
			return null;
		} else if (r.trim().equals("")) {
			return null;
		}

		else {
			return java.sql.Date.valueOf(r.trim());
		}
	}

	/**
	 * eturns a java.util.Map of the parameters of this request. Request
	 * parameters are extra information sent with the request. For HTTP
	 * servlets, parameters are contained in the query string or posted form
	 * data.
	 * 
	 * @return an immutable java.util.Map containing parameter names as keys and
	 *         parameter values as map values. The keys in the parameter map are
	 *         of type String. The values in the parameter map are of type
	 *         String array
	 */
	@SuppressWarnings("rawtypes")
	public java.util.Map getParameterMap() {
		Map<String, String[]> xx = new HashMap<String, String[]>();
		xx.putAll(request.getParameterMap());
		Set<?> e = xx.entrySet();
		Iterator<?> it = e.iterator();
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			String key = (String) me.getKey();
			String values[] = getParameterValues(key);
			xx.put(key, values);
		}
		return Collections.unmodifiableMap(xx);
	}

	/**
	 * Returns an Enumeration of String objects containing the names of the
	 * parameters contained in this request. If the request has no parameters,
	 * the method returns an empty Enumeration.
	 * 
	 * @return Enumeration
	 */
	@SuppressWarnings("rawtypes")
	public java.util.Enumeration getParameterNames() {
		return request.getParameterNames();
	}

	/**
	 * Returns an array of String objects containing all of the values the given
	 * request parameter has, or null if the parameter does not exist. If the
	 * parameter has a single value, the array has a length of 1.
	 * 
	 * @paramn - a String containing the name of the parameter whose value is
	 *         requested
	 * @return an array of String objects containing the parameter's values
	 */
	public String[] getParameterValues(String name) {
		String[] x = request.getParameterValues(name);
		if (x == null || x.length == 0) {
			return x;
		}
		for (int i = 0; i < x.length; i++) {
			String s = WebUtil.xssFilter(x[i]);
			x[i] = s;
		}
		// return request.getParameterValues(name);
		return x;
	}

	/**
	 * 为了免除每次都通过vgetParametervAsXXX方法获取页面输入的field值 我们可以针对html form表单建立一个form
	 * bean，通过此方法可以自动填充页面的各个输入参数，以formbean的对象返回。 (注：formbean的属性名称必须和页面form的field
	 * name保持一致)
	 * 
	 * @param formBeanClass
	 *            formbean值对象对应的类Class
	 * @return 返回填充好数据的formbean值对象
	 */
	public <T> T getParameterValuesAsFormBean(Class<T> formBeanClass) {
		T obj;
		try {
			obj = formBeanClass.newInstance();
			Map<?, ?> m = getParameterMap();
			Set<?> e = m.entrySet();
			Iterator<?> it = e.iterator();
			while (it.hasNext()) {
				@SuppressWarnings("rawtypes")
				Map.Entry me = (Map.Entry) it.next();
				String key = (String) me.getKey();
				String values[] = getParameterValues(key);
				if (values.length == 1) {
					try {
						ObjectUtil.setValue(key, obj, WebUtil.xssFilter(values[0]));
					} catch (IllegalArgumentException ille) {
						Class<?> type = ObjectUtil.getType(key, obj);
						String tstr = type.toString();
						// System.out.println("---->:"+tstr);
						// System.out.println("key="+key+"
						// value="+this.getParameter(key));
						if (tstr.equals(Integer.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsInteger(key));
						} else if (tstr.equals(Long.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsLong(key));
						} else if (tstr.equals(Float.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsFloat(key));
						} else if (tstr.equals(Double.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsDouble(key));
						} else if (tstr.equals(Timestamp.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsTimestamp(key));
						} else if (tstr.equals(java.sql.Date.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsDate(key));
						} else if (tstr.equals(BigDecimal.class.toString())) {
							ObjectUtil.setValue(key, obj, getParameterAsBigDecimal(key));
						} else {
							com.beetle.framework.log.AppLogger.getInstance(this.getClass())
									.warn("[" + key + "]not support,please deal it yourselef!");
							ille.printStackTrace();
						}
						tstr = null;
						type = null;
					}
				} else {
					try {
						ObjectUtil.setValue(key, obj, values);
					} catch (IllegalArgumentException ille) {
						ille.printStackTrace();
						com.beetle.framework.log.AppLogger.getInstance(this.getClass())
								.warn("[" + key + "]not support,please deal it yourselef!");
					}
				}
			}
		} catch (Throwable ex) {
			throw new AppRuntimeException(ex);
		}
		return obj;
	}

	/**
	 * 从session中取值
	 * 
	 * 
	 * @param valueName
	 *            名称
	 * @return 对应的值Object
	 */
	public Object getDataFromSession(String valueName) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		} else {
			return session.getAttribute(valueName);
		}
	}

	public String encryptByRsaPublicKey(String data) {
		try {
			String key = CommonUtil.getRsaPublicKey(this.getServletContext());
			return RSAEncrypt.encryptByPublicKey(key, data);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			// throw new AppRuntimeException("can't read privatekey file",e);
		}
	}

	public String decryptByRsaPrivateKey(String data) {
		try {
			String key = CommonUtil.getRsaPrivateKey(this.getServletContext());
			return RSAEncrypt.decryptByPrivateKey(data, key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据字段名获取解密后的值
	 * 
	 * @param name
	 * @return 如果找到值或者密匙不对解码失败则返回null
	 */
	public String decryptFieldValueByRsaPrivateKey(String fieldName) {
		String r = request.getParameter(fieldName);
		try {
			String key = CommonUtil.getRsaPrivateKey(this.getServletContext());
			return RSAEncrypt.decryptByPrivateKey(r, key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			// throw new AppRuntimeException("can't read privatekey file",e);
		}
	}

	HttpServletRequest getRequest() {
		return request;
	}

	HttpServletResponse getResponse() {
		return response;
	}

	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	public ServletContext getServletContext() {
		return request.getServletContext();
	}
}
