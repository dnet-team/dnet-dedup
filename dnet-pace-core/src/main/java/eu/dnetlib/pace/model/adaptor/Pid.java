package eu.dnetlib.pace.model.adaptor;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by claudio on 01/03/16.
 */
public class Pid {

	private static final Log log = LogFactory.getLog(Pid.class);

	private String value;

	private String type;

	public static List<Pid> fromOafJson(final List<String> json) {

		log.debug(String.format("\nPid: %s", json));

		final GsonBuilder gb = new GsonBuilder();
		gb.registerTypeAdapter(Pid.class, new PidOafSerialiser());
		final Gson gson = gb.create();

		return Lists.newArrayList(Iterables.transform(json, new Function<String, Pid>() {
			@Override
			public Pid apply(final String s) {
				return gson.fromJson(s, Pid.class);
			}
		}));
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

}
