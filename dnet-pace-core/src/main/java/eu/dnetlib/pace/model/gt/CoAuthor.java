package eu.dnetlib.pace.model.gt;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CoAuthor extends Author {

	private static final Log log = LogFactory.getLog(CoAuthor.class);
	private String anchorId = null;

	public CoAuthor() {
		super();
	}

	public CoAuthor(final Author author) {
		super(author);
	}

	public boolean hasAnchorId() {
		return StringUtils.isNotBlank(getAnchorId());
	}

	public String getAnchorId() {
		return anchorId;
	}

	public void setAnchorId(final String anchorId) {
		this.anchorId = anchorId;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

	@Override
	public int hashCode() {
		return getId() != null ? getId().hashCode() : getFullname().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		return (o instanceof CoAuthor) && StringUtils.isNotBlank(getId()) ?
				getId().equals(((CoAuthor) o).getId()) :
				getFullname().equals(((CoAuthor) o).getFullname());
	}

}
