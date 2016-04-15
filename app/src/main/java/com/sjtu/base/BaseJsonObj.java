package com.sjtu.base;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

/**
 * json 自动序列化类
 * 需要序列化的 成员类型 为下面代码支持的类型。
 * 作用域需要为public
 * 新的类型，需要更改下面相关代码
 * @author CharlesZhu
 *
 */
public class BaseJsonObj implements Serializable{
	private static final long serialVersionUID = -2023318290351159040L;
	String TAG="BaseJsonObj";
	public BaseJsonObj(JSONObject obj) {
		parse(obj);
	}
	public void parse(JSONObject obj){
		if(obj==null)return;
		Class clasz = getClass();
		Iterator<String> it = obj.keys();
		while (it.hasNext()) {
			try {
				String jkey = it.next();
				Field f = clasz.getField(jkey);
				Class fc = f.getType();
				if (fc.equals(String.class)) {
					f.set(this, obj.getString(jkey));
				}else if (fc.equals(int.class)) {
					f.setInt(this, obj.getInt(jkey));
				} else if (fc.equals(long.class)) {
					f.setLong(this, obj.getLong(jkey));
				} else if (fc.equals(boolean.class)) {
					f.setBoolean(this, obj.getBoolean(jkey));
				} else if (fc.equals(double.class)) {
					f.setDouble(this, obj.getDouble(jkey));
				} else if (fc.equals(float.class)) {
					f.setFloat(this, (float)obj.getDouble(jkey));
				} else if (fc.equals(String[].class)) {
					JSONArray array = (JSONArray) obj.get(jkey);
					int len = array.length();
					String[] values = new String[len];
					for (int i = 0; i< len; i++) {
						values[i] = array.getString(i);
					}
					f.set(this, values);
				} else if (fc.equals(long[].class)) {
					JSONArray array = (JSONArray) obj.get(jkey);
					int len = array.length();
					long[] values = new long[len];
					for (int i = 0; i< len; i++) {
						values[i] = array.optLong(i);
					}
					f.set(this, values);
				} else if (fc.isArray()) { // must be JSONObject array.
					JSONArray array = (JSONArray) obj.get(jkey);
					int len = array.length();
					Class comClass = fc.getComponentType();
					Object newArray = Array.newInstance(comClass, len);
					for (int i = 0; i < len; i++) {
						try {
							JSONObject jsonItemObj = array.getJSONObject(i);
							Object itemObj = comClass.getConstructor(
									JSONObject.class).newInstance(jsonItemObj);
							Array.set(newArray, i, itemObj);
						} catch (NullPointerException e) {
							e.printStackTrace();
						}catch (Exception e) {
							e.printStackTrace();
							Object itemObj = comClass.getConstructor(
									String.class).newInstance(array.get(i).toString());
							Array.set(newArray, i, itemObj);
						}
					}
					f.set(this, newArray);
				}else if(Modifier.isAbstract(fc.getModifiers())){// abstract class,need another way to parse
					Method m = fc.getMethod("parseInternal", JSONObject.class);
					if (m != null) {
						Object object = m.invoke(null, obj.get(jkey));// obj.getString(jkey));
						f.set(this, object);
					}
				} else {// custom class extends BaseJsonObj
					Object object = fc.getConstructor(JSONObject.class)
							.newInstance(obj.get(jkey));
					f.set(this, object);
				}
			} catch(NoSuchFieldException e){
				//一般大家按照wiki定义来实现，是不会出现问题的。
			}catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	public JSONObject toJSONObjectValue(String key) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(key, toJSONObject());
		return obj;
	}
	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		Field[] fields = getClass().getFields();
		for (Field field : fields) {
			try {
				if(Modifier.isPrivate(field.getModifiers())){
					continue;
				}
				String name = field.getName();
				Class fc = field.getType();
				if(Modifier.isFinal(field.getModifiers())) {
					 continue;
				}else if (fc.equals(String[].class)) {
					JSONArray array = new JSONArray();
					String[]  tmp = (String[])field.get(this);
					if(tmp != null){
						int len = tmp.length;
						for (int i = 0; i< len; i++) {
							array.put(tmp[i]);
						}
					}
					obj.put(name, array);
				}  else if(fc.isArray()) {
					JSONArray array = new JSONArray();
					BaseJsonObj[] tmp = (BaseJsonObj[])field.get(this);
					if(tmp != null){
						for(BaseJsonObj m:tmp)
							array.put(m.toJSONObject());
					}
					obj.put(name, array);
				} else {
					if((field.get(this)) instanceof BaseJsonObj) {
						obj.put(name, ((BaseJsonObj)field.get(this)).toJSONObject());
					} else{
						/*Object val =  field.get(this);
						if(val!=null)
							obj.put(name, val.toString());*/
						obj.put(name, field.get(this));
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return obj;
	}
public void test(){
	Field[] fields = getClass().getFields();
	System.out.println("class: "+getClass().getName());
	for (Field field : fields) {
		try {
			String name = field.getName();
			System.out.println("\tfield : "+name);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
}
