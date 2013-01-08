/* SystemoutCollector.java

	Purpose:
		
	Description:
		
	History:
		2013/1/7 Created by Dennis Chen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.bind.sys.debugger.impl;

import java.util.Map;

import org.zkoss.bind.sys.Binding;
import org.zkoss.bind.sys.debugger.BindingExecutionInfoCollector;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
/**
 * 
 * @author dennis
 *
 */
public class SystemoutCollector implements BindingExecutionInfoCollector{

	@Override
	public void addExecutionInfo(Binding binding, String type, String fromExpr, String toExpr, Object value,
			Map<String, Object> args) {
		JSONObject json = new JSONObject();
		if(binding!=null && binding.getComponent()!=null){
			json.put("widget", binding.getComponent().getDefinition().getName());
			json.put("uuid", binding.getComponent().getUuid());
		}
		json.put("type", type);
		json.put("fromExpr", fromExpr);
		json.put("toExpr", toExpr);
		String valstr = value==null?null:value.toString();
		if(valstr!=null && valstr.length()>100){
			valstr = valstr.substring(0, 97)+"...";
		}
		json.put("value", valstr);
		
		Execution exec = Executions.getCurrent();
		json.put("sid", exec.getHeader("ZK-SID"));
		
		System.out.println("["+json.get("sid")+"]\t["+json.get("widget")+"]\t[$"+json.get("uuid") + 
			"]\t["+json.get("type")+"]\t"+json.get("fromExpr")+" > "+json.get("toExpr")+"\t= "+
			json.get("value"));
	}

}
