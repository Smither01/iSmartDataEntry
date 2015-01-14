/**
 * 
 */
package com.guogu.ismartdataentry;

/**
 *@ClassName:     AsyncHttpResponseHandler.java
 * @Description:   TODO
 * @author xieguangwei
 * @date 2014-12-4
 * 
 */
public interface AsyncHttpResponseHandler {
	public void onFailure(Throwable error, String content);
	public void onSuccess(int statusCode, String content);
}
